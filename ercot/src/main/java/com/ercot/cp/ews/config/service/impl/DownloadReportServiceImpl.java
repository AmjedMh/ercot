package com.ercot.cp.ews.config.service.impl;

import com.ercot.cp.ews.EwsClient;
import com.ercot.cp.ews.config.ReportConfig;
import com.ercot.cp.ews.config.SystemConfiguration;
import com.ercot.cp.ews.config.domin.ReportData;
import com.ercot.cp.ews.config.dto.DMPReportDTO;
import com.ercot.cp.ews.config.dto.RTMReportDTO;
import com.ercot.cp.ews.config.service.DMPReportService;
import com.ercot.cp.ews.config.service.DownloadReportService;
import com.ercot.cp.ews.config.service.HourlyResourceOutageCapacityReportService;
import com.ercot.cp.ews.config.service.RTMReportService;
import com.ercot.cp.ews.config.service.SevenDayLoadForecastReportService;
import com.ercot.cp.ews.config.service.SolarPowerReportService;
import com.ercot.cp.ews.config.service.WindGeoReportService;
import com.ercot.cp.ews.config.util.CommonHelper;
import com.ercot.schema._2007_06.nodal.ews.message.HeaderType;
import com.ercot.schema._2007_06.nodal.ews.message.ReplayDetectionType;
import com.ercot.schema._2007_06.nodal.ews.message.RequestMessage;
import com.ercot.schema._2007_06.nodal.ews.message.RequestType;
import com.ercot.schema._2007_06.nodal.ews.message.ResponseMessage;
import com.ercot.schema._2007_06.nodal.ews.message.dto.Report;
import com.ercot.schema._2007_06.nodal.ews.message.dto.Reports;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.oasis_open.docs.wss._2004._01.oasis_200401_wss_wssecurity_secext_1_0.EncodedString;
import org.oasis_open.docs.wss._2004._01.oasis_200401_wss_wssecurity_utility_1_0.AttributedDateTime;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAmount;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static com.ercot.cp.ews.config.constants.ConstantCodes.DOWNLOAD_REPORT;
import static com.ercot.cp.ews.config.constants.ConstantCodes.REPORT_CLASS;
import static java.lang.Boolean.TRUE;
import java.text.ParseException;
@Log4j2
@Service
@RequiredArgsConstructor
public class DownloadReportServiceImpl implements DownloadReportService {

    private static final String _get = "get";
    private static final String soap_address = "https://misapi.ercot.com/NodalAPI/EWS/";
    private static final String soap_action_market_info = "/BusinessService/NodalService.serviceagent/HttpEndPoint/MarketInfo";

    private final EwsClient ewsClient;
    private final SystemConfiguration systemConfiguration;
    private final RTMReportService rtmReportService;
    private final DMPReportService dmpReportService;
    private final WindGeoReportService windGeoReportService;
    private final SolarPowerReportService solarPowerReportService;
    private final SevenDayLoadForecastReportService sevenDayLoadForecastReportService;
    private final HourlyResourceOutageCapacityReportService hourlyResourceOutageCapacityReportService;

    // Track download statistics for monitoring
    private static class DownloadStats {
        int attempted = 0;
        int succeeded = 0;
        int failed = 0;
        long totalBytes = 0;
        Map<String, String> failures = new ConcurrentHashMap<>();
    }

    /**
     * Download a file without processing it (to avoid blocking on DB operations).
     * Processing will be done separately in batch after all downloads complete.
     */
    private boolean downloadFileOnly(Report report, String reportDuration, DownloadStats stats) {
        stats.attempted++;
        try {
            log.debug("downloadFileOnly: fileName={} size={}", report.getFileName(), report.getSize());

            String baseDirectory = systemConfiguration.getFolderPath() + File.separator + 
                                 report.getReportGroup() + File.separator;
            baseDirectory += getDirectory(reportDuration, report.getCreated());
            
            Files.createDirectories(Paths.get(baseDirectory));
            String fileNameDirectory = baseDirectory + File.separator + report.getFileName();

            // Download file only - no processing
            long bytesDownloaded = downloadFileIfNotExists(fileNameDirectory, report.getURL(), report);
            
            if (bytesDownloaded > 0) {
                stats.succeeded++;
                stats.totalBytes += bytesDownloaded;
                log.debug("downloadFileOnly: successfully downloaded {} ({} bytes)", 
                         report.getFileName(), bytesDownloaded);
                return true;
            } else {
                log.debug("downloadFileOnly: file already exists and is valid: {}", report.getFileName());
                return true;  // File already exists, not a failure
            }
        } catch (Exception e) {
            stats.failed++;
            stats.failures.put(report.getFileName(), e.getMessage());
            log.error("downloadFileOnly: FAILED to download {} - error: {}", 
                     report.getFileName(), e.getMessage(), e);
            return false;
        }
    }

    private <T> void zipReport(String reportName, String fileNameDirectory) {
        log.info("zipReport START: reportName={} file={}", reportName, fileNameDirectory);
        long startTime = System.currentTimeMillis();
        
        Class<T> aClass = REPORT_CLASS.get(reportName);
        if (aClass == null) {
            log.warn("zipReport: No DTO mapping for reportName=[{}] - skipping DB persistence for: {}", 
                    reportName, fileNameDirectory);
            return;
        }

        try (ZipFile zip = new ZipFile(fileNameDirectory)) {
            int entriesProcessed = 0;
            int csvEntriesFound = 0;
            
            for (Enumeration<? extends ZipEntry> e = zip.entries(); e.hasMoreElements(); ) {
                ZipEntry entry = e.nextElement();
                entriesProcessed++;

                if (entry.getName().endsWith(".xml")) {
                    log.debug("zipReport: skipping XML entry: {} in file: {}", entry.getName(), fileNameDirectory);
                    continue;
                }
                
                csvEntriesFound++;
                log.info("zipReport: processing CSV entry: {} from: {}", entry.getName(), fileNameDirectory);

                try (InputStream in = zip.getInputStream(entry);
                     Reader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.ISO_8859_1))) {
                    
                    CsvToBean<T> csvAmazonData = new CsvToBeanBuilder<T>(reader)
                        .withSkipLines(1)
                        .withSeparator(',')
                        .withType(aClass)
                        .withIgnoreQuotations(TRUE)
                        .withIgnoreEmptyLine(true)
                        .build();
                    
                    List<T> reportRows = csvAmazonData.parse();
                    log.info("zipReport: parsed {} rows from entry: {} in file: {}", 
                            reportRows.size(), entry.getName(), fileNameDirectory);

                    if (reportRows.isEmpty()) {
                        log.warn("zipReport: 0 rows parsed from entry: {} - skipping DB write", entry.getName());
                    } else {
                        try {
                            convertReport(reportRows);
                            log.info("zipReport: successfully processed {} rows to DB from entry: {}", 
                                    reportRows.size(), entry.getName());
                        } catch (Exception dbEx) {
                            log.error("zipReport: DATABASE ERROR processing {} rows from entry={} file={}: {}", 
                                    reportRows.size(), entry.getName(), fileNameDirectory, dbEx.getMessage(), dbEx);
                            // Continue to next entry even if DB fails
                        }
                    }
                } catch (Exception entryEx) {
                    log.error("zipReport: ERROR processing entry={} from file={}: {}", 
                            entry.getName(), fileNameDirectory, entryEx.getMessage(), entryEx);
                    // Continue to next entry
                }
            }
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("zipReport END: file={} entriesProcessed={} csvProcessed={} duration={}ms", 
                    fileNameDirectory, entriesProcessed, csvEntriesFound, duration);
                    
        } catch (Exception exception) {
            log.error("zipReport: CRITICAL ERROR opening/reading zip file={}: {}", 
                    fileNameDirectory, exception.getMessage(), exception);
        }
    }

    private long downloadFileIfNotExists(String filePath, String url, Report report) throws IOException {
        File file = new File(filePath);
        long lSize = file.exists() ? file.length() : -1;
        long expectedSize = report.getSize();

        if (expectedSize != lSize) {
            log.info("downloadFileIfNotExists: downloading {} (expected size: {} bytes)", 
                    report.getFileName(), expectedSize);
            
            HttpURLConnection conn = null;
            InputStream in = null;
            try {
                conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setConnectTimeout(30_000);  // 30s to establish TCP connection
                conn.setReadTimeout(120_000);    // 120s max to receive data between packets
                
                in = conn.getInputStream();
                long lSizeDownload = Files.copy(in, Paths.get(filePath), StandardCopyOption.REPLACE_EXISTING);
                
                log.info("downloadFileIfNotExists: downloaded {} bytes for {}", 
                        lSizeDownload, report.getFileName());
                
                // Verify downloaded size matches expected
                if (lSizeDownload != expectedSize) {
                    log.warn("downloadFileIfNotExists: SIZE MISMATCH for {} - expected {} but got {} bytes", 
                            report.getFileName(), expectedSize, lSizeDownload);
                }
                
                return lSizeDownload;
            } finally {
                if (in != null) {
                    try { in.close(); } catch (IOException ignored) {}
                }
                if (conn != null) {
                    conn.disconnect();
                }
            }
        } else {
            log.debug("downloadFileIfNotExists: file already exists with correct size: {}", filePath);
            return 0;  // Already exists
        }
    }

    private String getDirectory(String reportDuration, LocalDateTime created) {
        String directory = "";
        switch (reportDuration.toLowerCase()) {
            case "monthly":
                directory = String.valueOf(created.getYear());
                break;
            case "daily":
                directory = created.getYear() + File.separator + created.format(DateTimeFormatter.ofPattern("MM"));
                break;
            case "hourly":
            case "minutes":
                directory = created.getYear() + File.separator + created.format(DateTimeFormatter.ofPattern("MM")) + File.separator + created.format(DateTimeFormatter.ofPattern("dd"));
                break;
        }

        return directory;
    }

    private TemporalAmount getTimeDuration(String reportDuration) {
        TemporalAmount timeDuration = null;
        switch (reportDuration.toLowerCase()) {
            case "monthly":
                timeDuration = Period.ofYears(1);
                break;
            case "daily":
                timeDuration = Period.ofMonths(1);
                break;
            case "hourly":
            case "minutes":
                timeDuration = Period.ofDays(1);
                break;
        }

        return timeDuration;
    }

    @Override
    public void downloadReport(ReportConfig reportConfig) {
        log.info("=== downloadReport START: {} ===", reportConfig.getName());
        long startTime = System.currentTimeMillis();
        DownloadStats stats = new DownloadStats();

        try {
            RequestMessage requestRequest = formRequest(reportConfig);
            log.debug("downloadReport: SOAP request created for {}", reportConfig.getName());
            
            ResponseMessage responseMessage = ewsClient.callEWS(soap_address, soap_action_market_info, requestRequest);
            List<Element> elementList = responseMessage.getPayload().getAny();

            if (elementList == null || elementList.isEmpty()) {
                log.warn("downloadReport: NO REPORTS FOUND for {}", reportConfig.getName());
                return;
            }

            Document document = elementList.get(0).getOwnerDocument();
            DOMImplementationLS domImplLS = (DOMImplementationLS) document.getImplementation();
            LSSerializer serializer = domImplLS.createLSSerializer();
            String str = serializer.writeToString(elementList.get(0));

            List<Report> reportList = parseXml(str);
            log.info("downloadReport: found {} file(s) to download for {}", 
                    reportList.size(), reportConfig.getName());
            
            // PHASE 1: Download ALL files (fast, no DB blocking)
            log.info("downloadReport: PHASE 1 - Downloading {} files...", reportList.size());
            reportList.forEach(report -> downloadFileOnly(report, reportConfig.getReportDuration(), stats));
            
            long downloadDuration = System.currentTimeMillis() - startTime;
            log.info("downloadReport: PHASE 1 COMPLETE - attempted={} succeeded={} failed={} totalMB={} durationMs={}",
                    stats.attempted, stats.succeeded, stats.failed, 
                    stats.totalBytes / 1_048_576, downloadDuration);
            
            if (!stats.failures.isEmpty()) {
                log.error("downloadReport: {} DOWNLOAD FAILURES:", stats.failures.size());
                stats.failures.forEach((fileName, error) -> 
                    log.error("  - {}: {}", fileName, error));
            }
            
            // PHASE 2: Process downloaded CSV files (DB operations separate from download)
            // This runs AFTER all downloads complete, so partial failures don't block everything
            log.info("downloadReport: PHASE 2 - Processing CSV files for DB insertion...");
            long processStartTime = System.currentTimeMillis();
            int processedCount = 0;
            
            for (Report report : reportList) {
                // Only process CSV files that were successfully downloaded
                if (!report.getFileName().contains("xml") && 
                    REPORT_CLASS.containsKey(report.getReportGroup()) &&
                    !stats.failures.containsKey(report.getFileName())) {
                    
                    try {
                        String baseDirectory = systemConfiguration.getFolderPath() + File.separator + 
                                             report.getReportGroup() + File.separator;
                        baseDirectory += getDirectory(reportConfig.getReportDuration(), report.getCreated());
                        String filePath = baseDirectory + File.separator + report.getFileName();
                        
                        log.info("downloadReport: processing CSV file for DB: {}", report.getFileName());
                        zipReport(report.getReportGroup(), filePath);
                        processedCount++;
                    } catch (Exception e) {
                        log.error("downloadReport: FAILED to process CSV {} to DB: {}", 
                                report.getFileName(), e.getMessage(), e);
                        // Continue processing other files
                    }
                }
            }
            
            long processDuration = System.currentTimeMillis() - processStartTime;
            long totalDuration = System.currentTimeMillis() - startTime;
            
            log.info("downloadReport: PHASE 2 COMPLETE - processed {} CSV files in {}ms", 
                    processedCount, processDuration);
            log.info("=== downloadReport END: {} - Total time: {}ms ===", 
                    reportConfig.getName(), totalDuration);

        } catch (Exception exception) {
            log.error("downloadReport: CRITICAL ERROR for {}: {}", 
                     reportConfig.getName(), exception.getMessage(), exception);
            throw new RuntimeException("Download report failed: " + reportConfig.getName(), exception);
        }
    }

    @Override
    public void dumpReport(ReportData reportData) {
        log.debug("Inside dumpReport reportData: {} ", reportData);

        LocalDateTime startDate = reportData.getStartDate();
        LocalDateTime endDate = reportData.getEndDate() == null ? LocalDateTime.now() : reportData.getEndDate();

        TemporalAmount increment = getTimeDuration(reportData.getReportDuration());

        int totalFilesProcessed = 0;

        for (LocalDateTime currentDate = startDate; currentDate.isBefore(endDate) || currentDate.isEqual(endDate); currentDate = currentDate.plus(increment)) {

            String baseDirectory = systemConfiguration.getFolderPath() + File.separator + reportData.getReportName() + File.separator;
            baseDirectory += getDirectory(reportData.getReportDuration(), currentDate);
            log.info("dumpReport scanning directory: {}", baseDirectory);

            List<String> reportFiles = getReportFiles(baseDirectory);
            log.info("dumpReport found {} file(s) in directory: {}", reportFiles.size(), baseDirectory);

            if (reportFiles.isEmpty()) {
                log.warn("dumpReport: no files found for date {} in directory: {}", currentDate, baseDirectory);
                continue;
            }

            totalFilesProcessed += reportFiles.size();
            String finalBaseDirectory = baseDirectory;
            ThreadPoolExecutor threadPoolExecutor = CommonHelper.getThreadPoolExecutor(DOWNLOAD_REPORT);

            reportFiles.forEach(reportFile -> threadPoolExecutor.execute(() -> {
                String fileNameDirectory = finalBaseDirectory + "/" + reportFile;
                log.info("dumpReport processing file: {}", fileNameDirectory);
                try {
                    if (reportFile.endsWith(".zip") || reportFile.endsWith("_csv")) {
                        // .zip files and ERCOT extensionless _csv files are both zip-compressed
                        zipReport(reportData.getReportName(), fileNameDirectory);
                    } else {
                        csvReport(reportData.getReportName(), fileNameDirectory);
                    }
                } catch (Exception e) {
                    log.error("dumpReport: unhandled exception processing file: {} - error: {}", fileNameDirectory, e.getMessage(), e);
                }
            }));
            new CommonHelper().waitForExecutorToCompleteTasks(threadPoolExecutor);
        }
        log.info("Leaving dumpReport - total files submitted for processing: {}", totalFilesProcessed);
    }

    private <T> void csvReport(String reportName, String fileNameDirectory) {
        log.info("csvReport START: reportName={} file={}", reportName, fileNameDirectory);
        long startTime = System.currentTimeMillis();
        
        Class<T> aClass = REPORT_CLASS.get(reportName);
        if (aClass == null) {
            log.warn("csvReport: No DTO mapping for reportName=[{}] - skipping DB persistence for: {}", 
                    reportName, fileNameDirectory);
            return;
        }
        
        try (InputStream inputStream = new FileInputStream(fileNameDirectory);
             Reader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.ISO_8859_1))) {
            
            CsvToBean<T> csvAmazonData = new CsvToBeanBuilder<T>(reader)
                .withSkipLines(1)
                .withSeparator(',')
                .withType(aClass)
                .withIgnoreQuotations(TRUE)
                .withIgnoreEmptyLine(true)
                .build();
            
            List<T> reportRows = csvAmazonData.parse();
            log.info("csvReport: parsed {} rows from file: {}", reportRows.size(), fileNameDirectory);

            if (CollectionUtils.isEmpty(reportRows)) {
                log.warn("csvReport: 0 rows parsed from file: {} - skipping DB write", fileNameDirectory);
                return;
            }

            try {
                convertReport(reportRows);
                long duration = System.currentTimeMillis() - startTime;
                log.info("csvReport END: successfully processed {} rows to DB from file={} duration={}ms", 
                        reportRows.size(), fileNameDirectory, duration);
            } catch (Exception dbEx) {
                log.error("csvReport: DATABASE ERROR processing {} rows from file={}: {}", 
                        reportRows.size(), fileNameDirectory, dbEx.getMessage(), dbEx);
                throw dbEx;  // Re-throw to signal failure
            }
            
        } catch (Exception exception) {
            log.error("csvReport: ERROR reading/parsing CSV file={}: {}", 
                    fileNameDirectory, exception.getMessage(), exception);
            throw new RuntimeException("Failed to process CSV report: " + fileNameDirectory, exception);
        }
    }

    public <T> void convertReport(List<T> reportRows) {
        String dtoType = reportRows.get(0).getClass().getSimpleName();
        log.info("convertReport dispatching {} rows of type: {}", reportRows.size(), dtoType);

        switch (dtoType) {
            case "RTMReportDTO":
                rtmReportService.processRTMListingsData(reportRows);
                break;
            case "DMPReportDTO":
                dmpReportService.processDTMListingsData(reportRows);
                break;
            case "WindGeoReportDTO":
                windGeoReportService.processWindGeoData(reportRows);
                break;
            case "SolarPowerReportDTO":
                solarPowerReportService.processSolarPowerData(reportRows);
                break;
            case "SevenDayLoadForecastReportDTO":
                sevenDayLoadForecastReportService.processSevenDayLoadForecastData(reportRows);
                break;
            case "HourlyResourceOutageCapacityReportDTO":
                hourlyResourceOutageCapacityReportService.processHourlyResourceOutageCapacityData(reportRows);
                break;
            default:
                log.warn("convertReport: no DB handler registered for DTO type: [{}] - {} rows NOT saved", dtoType, reportRows.size());
                break;
        }
    }

    private List<String> getReportFiles(String directoryPath) {
        File directory = new File(directoryPath);

        if (directory.exists() && directory.isDirectory()) {
            return Arrays.asList(Objects.requireNonNull(directory.list((dir, name) ->
                (name.endsWith(".zip") && !name.endsWith("_xml.zip"))  // standard .zip, exclude XML archives
                || name.endsWith(".csv")        // standard .csv
                || name.endsWith("_csv")        // ERCOT extensionless csv-zip (e.g. ..._0000_csv)
            )));
        } else {
            log.debug("Directory not found: {}", directoryPath);
            return List.of();
        }
    }

    private List<Report> parseXml(String str) throws JsonProcessingException {
        XmlMapper mapper = new XmlMapper();
        Reports value = mapper.readValue(str, Reports.class);

        return value.getReport();
    }

    private RequestMessage formRequest(ReportConfig reportConfig) throws DatatypeConfigurationException {
        // 1) Prepare a CST‐timezone calendar
        GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("CST"));

        // 2) If startDate is provided (non‐null, non‐empty), parse it; else use “now – buffer”
        String userStartStr = reportConfig.getStartDate();
        if (userStartStr != null && !userStartStr.trim().isEmpty()) {
            // Expecting ISO‐8601 without timezone, e.g. "2025-05-01T00:00:00"
            SimpleDateFormat sdfInput = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            // Interpret the input as UTC
            sdfInput.setTimeZone(TimeZone.getTimeZone("UTC"));
            try {
                Date parsed = sdfInput.parse(userStartStr);
                // Set that UTC‐instant into our CST calendar
                cal.setTime(parsed);
            } catch (ParseException e) {
                // Parsing failed → fallback to (now – buffer)
                log.error("Error parsing userStartStr: {}. Falling back to (now – buffer). Exception: {}", userStartStr, e.getMessage(), e);
                cal.setTime(new Date());
                cal.add(GregorianCalendar.MINUTE, -1 * reportConfig.getBuffer());
            }
        } else {
            // No user‐supplied startDate → (now – buffer)
            cal.setTime(new Date());
            cal.add(GregorianCalendar.MINUTE, -1 * reportConfig.getBuffer());
        }

        // 3) Build WS‐Security “Created” timestamp = now in CST
        ZoneId cstZone = ZoneId.of("CST", ZoneId.SHORT_IDS);
        GregorianCalendar calCreated = GregorianCalendar.from(
            java.time.ZonedDateTime.now(cstZone)
        );
        SimpleDateFormat sdfCreated = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        sdfCreated.setTimeZone(TimeZone.getTimeZone("CST"));
        String createdStr = sdfCreated.format(calCreated.getTime());

        // 4) Construct the SOAP Header
        HeaderType requestHeader = new HeaderType();
        AttributedDateTime created = new AttributedDateTime();

        requestHeader.setVerb(_get);
        requestHeader.setNoun("Reports");
        String userId = systemConfiguration.getUserID();
        requestHeader.setSource(userId + "@QLONCA");
        requestHeader.setUserID(userId);

        ReplayDetectionType rdt = new ReplayDetectionType();
        EncodedString nonce = new EncodedString();
        nonce.setValue(UUID.randomUUID().toString());
        rdt.setNonce(nonce);
        created.setValue(createdStr);
        rdt.setCreated(created);
        requestHeader.setReplayDetection(rdt);
        requestHeader.setRevision(UUID.randomUUID().toString());

        // 5) Construct the <Request> body with <StartTime>
        XMLGregorianCalendar xCalStart = DatatypeFactory
            .newInstance()
            .newXMLGregorianCalendar(cal);
        RequestType requestBody = new RequestType();
        requestBody.setStartTime(xCalStart);
        requestBody.setOption(reportConfig.getId());

        // 6) Wrap into RequestMessage
        RequestMessage request = new RequestMessage();
        request.setHeader(requestHeader);
        request.setRequest(requestBody);
        return request;
    }


}
