package com.ercot.cp.ews.config.service.impl;

import com.ercot.cp.ews.EwsClient;
import com.ercot.cp.ews.config.ReportConfig;
import com.ercot.cp.ews.config.SystemConfiguration;
import com.ercot.cp.ews.config.domin.ReportData;
import com.ercot.cp.ews.config.dto.DMPReportDTO;
import com.ercot.cp.ews.config.dto.RTMReportDTO;
import com.ercot.cp.ews.config.service.DMPReportService;
import com.ercot.cp.ews.config.service.DownloadReportService;
import com.ercot.cp.ews.config.service.RTMReportService;
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
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAmount;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static com.ercot.cp.ews.config.constants.ConstantCodes.DOWNLOAD_REPORT;
import static com.ercot.cp.ews.config.constants.ConstantCodes.REPORT_CLASS;
import static java.lang.Boolean.TRUE;

@Log4j2
@Service
@RequiredArgsConstructor
public class DownloadReportServiceImpl implements DownloadReportService {

    private static final String _get = "get";
    private static final String soap_address = "https://misapi.ercot.com/2007-08/Nodal/eEDS/EWS/";
    private static final String soap_action_market_info = "/BusinessService/NodalService.serviceagent/HttpEndPoint/MarketInfo";

    private final EwsClient ewsClient;
    private final SystemConfiguration systemConfiguration;
    private final RTMReportService rtmReportService;
    private final DMPReportService dmpReportService;

    @SneakyThrows
    private void downloadFile(Report report, String reportDuration) {
        log.debug("Inside downloadFile reportFileName: {} reportDuration: {}", report.getFileName(), reportDuration);

        String baseDirectory = systemConfiguration.getFolderPath() + File.separator + report.getReportGroup() + File.separator;

        baseDirectory += getDirectory(reportDuration, report.getCreated());

        Files.createDirectories(Paths.get(baseDirectory));
        String fileNameDirectory = baseDirectory + File.separator + report.getFileName();

        downloadFileIfNotExists(fileNameDirectory, report.getURL(), report);

        log.debug("Leaving downloadFile reportFileName: {}", report.getFileName());
    }

    private <T> void zipReport(String reportName, String fileNameDirectory) {
        Class<T> aClass = REPORT_CLASS.get(reportName);
        List<T> reportRows;

        try (ZipFile zip = new ZipFile(fileNameDirectory)) {

            for (Enumeration<? extends ZipEntry> e = zip.entries(); e.hasMoreElements(); ) {
                ZipEntry entry = e.nextElement();

                InputStream in = zip.getInputStream(entry);
                Reader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.ISO_8859_1));
                CsvToBean<T> csvAmazonData = new CsvToBeanBuilder<T>(reader)
                    .withSkipLines(1)
                    .withSeparator(',')
                    .withType(aClass)
                    .withIgnoreQuotations(TRUE)
                    .withIgnoreEmptyLine(true)
                    .build();
                reportRows = csvAmazonData.parse();
                log.debug("reportRows: {}", reportRows.size());

                convertReport(reportRows);
                reader.close();
            }
        } catch (Exception exception) {
            log.error("Exception while zipReport fileNameDirectory: {} exception: {}", fileNameDirectory, exception.getMessage(), exception);
        }
    }

    private void downloadFileIfNotExists(String filePath, String url, Report report) throws IOException {
        File file = new File(filePath);
        long lSize = file.exists() ? file.length() : -1;
        long expectedSize = report.getSize();

        if (expectedSize != lSize) {
            InputStream in = new URL(url).openStream();
            long lSizeDownload = Files.copy(in, Paths.get(filePath));
            log.debug("lSizeDownload: {}", lSizeDownload);

            if (report.getFileName().contains("xml") || !REPORT_CLASS.containsKey(report.getReportGroup())) {
                log.debug("Leaving downloadFile getFileName: {} ", report.getFileName());
                return;
            }
            zipReport(report.getReportGroup(), filePath);
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
        log.debug("Inside downloadReport reportName: {}", reportConfig.getName());

        try {
            ResponseMessage responseMessage = ewsClient.callEWS(soap_address, soap_action_market_info, formRequest(reportConfig));
            List<Element> elementList = responseMessage.getPayload().getAny();

            if (elementList == null || elementList.isEmpty()) {
                log.debug("Leaving downloadReport report not found reportName: {}", reportConfig.getName());
                return;
            }

            Document document = elementList.get(0).getOwnerDocument();

            DOMImplementationLS domImplLS = (DOMImplementationLS) document.getImplementation();
            LSSerializer serializer = domImplLS.createLSSerializer();
            String str = serializer.writeToString(elementList.get(0));

            List<Report> reportList = parseXml(str);
            reportList.forEach(report -> downloadFile(report, reportConfig.getReportDuration()));

        } catch (Exception exception) {
            log.error("Exception occurred while downloadReport. Error Message: {}", exception.getMessage(), exception);
        }

        log.debug("Leaving downloadReport reportName: {}", reportConfig.getName());
    }

    @Override
    public void dumpReport(ReportData reportData) {
        log.debug("Inside dumpReport reportData: {} ", reportData);

        LocalDateTime startDate = reportData.getStartDate();
        LocalDateTime endDate = reportData.getEndDate() == null ? LocalDateTime.now() : reportData.getEndDate();

        TemporalAmount increment = getTimeDuration(reportData.getReportDuration());

        for (LocalDateTime currentDate = startDate; currentDate.isBefore(endDate) || currentDate.isEqual(endDate); currentDate = currentDate.plus(increment)) {

            String baseDirectory = systemConfiguration.getFolderPath() + File.separator + reportData.getReportName() + File.separator;
            baseDirectory += getDirectory(reportData.getReportDuration(), currentDate);
            log.debug("baseDirectory: {}", baseDirectory);

            List<String> reportFiles = getReportFiles(baseDirectory);
            log.debug("reportFiles: {} ", reportFiles.size());

            String finalBaseDirectory = baseDirectory;
            ThreadPoolExecutor threadPoolExecutor = CommonHelper.getThreadPoolExecutor(DOWNLOAD_REPORT);

            reportFiles.forEach(reportFile -> threadPoolExecutor.execute(() -> {
                String fileNameDirectory = finalBaseDirectory + "/" + reportFile;

                if (reportFile.endsWith(".zip")) {
                    zipReport(reportData.getReportName(), fileNameDirectory);
                } else {
                    csvReport(reportData.getReportName(), fileNameDirectory);
                }
            }));
            new CommonHelper().waitForExecutorToCompleteTasks(threadPoolExecutor);
        }
        log.debug("Leaving dumpReport");
    }

    private <T> void csvReport(String reportName, String fileNameDirectory) {
        Class<T> aClass = REPORT_CLASS.get(reportName);
        List<T> reportRows;
        try {
            InputStream inputStream = new FileInputStream(fileNameDirectory);
            Reader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.ISO_8859_1));
            CsvToBean<T> csvAmazonData = new CsvToBeanBuilder<T>(reader)
                .withSkipLines(1)
                .withSeparator(',')
                .withType(aClass)
                .withIgnoreQuotations(TRUE)
                .withIgnoreEmptyLine(true)
                .build();
            reportRows = csvAmazonData.parse();
            log.debug("reportRows: {}", reportRows.size());

            if (CollectionUtils.isEmpty(reportRows)) return;

            convertReport(reportRows);
            reader.close();
            inputStream.close();
        } catch (Exception exception) {
            log.error("Exception occurred csvReport fileNameDirectory: {} exception: {} ", fileNameDirectory, exception.getMessage(), exception);
        }
    }

    public <T> void convertReport(List<T> reportRows) {

        switch (reportRows.get(0).getClass().getSimpleName()) {
            case "RTMReportDTO":
                rtmReportService.processRTMListingsData(reportRows);
                break;
            case "DMPReportDTO":
                dmpReportService.processDTMListingsData(reportRows);
                break;
        }
    }

    private List<String> getReportFiles(String directoryPath) {
        File directory = new File(directoryPath);

        if (directory.exists() && directory.isDirectory()) {
            return Arrays.asList(Objects.requireNonNull(directory.list((dir, name) -> (name.contains("csv") && name.endsWith(".zip")) || name.toLowerCase().endsWith(".csv"))));
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

        GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("CST"));
        cal.setTime(new Date());

        RequestMessage request = new RequestMessage();

        HeaderType requestHeader = new HeaderType();
        AttributedDateTime created = new AttributedDateTime();

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone("CST"));

        requestHeader.setVerb(_get);
        requestHeader.setNoun("Reports");
        requestHeader.setSource("API_ZHINES@QRFNEN");
        requestHeader.setUserID("API_ZHINES");

        ReplayDetectionType rdt = new ReplayDetectionType();
        EncodedString nonce = new EncodedString();
        nonce.setValue(UUID.randomUUID().toString());
        rdt.setNonce(nonce);
        created.setValue(format.format(cal.getTime()));
        rdt.setCreated(created);
        requestHeader.setReplayDetection(rdt);
        requestHeader.setRevision(UUID.randomUUID().toString());

        RequestType requestRequest = new RequestType();
        cal.add(GregorianCalendar.MINUTE, -1 * reportConfig.getBuffer());
        XMLGregorianCalendar xCal = DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
        requestRequest.setStartTime(xCal);
        requestRequest.setOption(reportConfig.getId());

        request.setHeader(requestHeader);
        request.setRequest(requestRequest);

        return request;
    }
}