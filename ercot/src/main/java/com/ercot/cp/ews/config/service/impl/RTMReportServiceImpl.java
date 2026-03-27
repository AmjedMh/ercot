package com.ercot.cp.ews.config.service.impl;

import com.ercot.cp.ews.config.domin.RTMReport;
import com.ercot.cp.ews.config.dto.RTMReportDTO;
import com.ercot.cp.ews.config.repository.RTMReportDataRepository;
import com.ercot.cp.ews.config.service.RTMReportService;
import com.ercot.cp.ews.config.transformer.RTMReportTransformer;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Log4j2
@Service
@RequiredArgsConstructor
public class RTMReportServiceImpl implements RTMReportService {

    private final RTMReportTransformer rtmReportTransformer;
    private final RTMReportDataRepository rtmReportDataRepository;

    @Override
    @Transactional(timeout = 300)  // 5 minutes timeout to prevent infinite hang
    public <T> void processRTMListingsData(List<T> reportRows) {
        long startTime = System.currentTimeMillis();
        log.info("processRTMListingsData: starting with {} input rows", reportRows.size());

        List<RTMReport> rtmReportList = new ArrayList<>();
        AtomicInteger skippedEW = new AtomicInteger(0);

        reportRows.forEach(reportRow -> {
            final var row = ((RTMReportDTO) reportRow);

            // Skip EW-type settlement points (exchange/wheel-through, not physical nodes)
            if (row.getSettlementPointType().endsWith("EW")) {
                skippedEW.incrementAndGet();
                return;
            }

            // getOrCreate auto-registers any new settlement point in SPPNodes table
            RTMReport rtmReport = rtmReportTransformer.toEntity(row);
            rtmReportList.add(rtmReport);
        });

        log.info("processRTMListingsData: totalInput={} saved={} skippedEW={}",
                reportRows.size(), rtmReportList.size(), skippedEW.get());
        
        if (rtmReportList.isEmpty()) {
            log.warn("processRTMListingsData: 0 rows qualified for DB insert (all were EW type)");
            return;
        }

        try {
            rtmReportDataRepository.saveAll(rtmReportList);
            long duration = System.currentTimeMillis() - startTime;
            log.info("processRTMListingsData: successfully saved {} rows to DB in {}ms", 
                    rtmReportList.size(), duration);
        } catch (QueryTimeoutException e) {
            log.error("processRTMListingsData: DATABASE TIMEOUT after 300s - saved 0/{} rows. Check DB connection!", 
                    rtmReportList.size(), e);
            throw new RuntimeException("Database operation timed out", e);
        } catch (Exception e) {
            log.error("processRTMListingsData: DATABASE ERROR saving {}/{} rows: {}", 
                    rtmReportList.size(), reportRows.size(), e.getMessage(), e);
            throw new RuntimeException("Database save failed", e);
        }
    }
}