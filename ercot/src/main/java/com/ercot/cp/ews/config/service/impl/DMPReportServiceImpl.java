package com.ercot.cp.ews.config.service.impl;

import com.ercot.cp.ews.config.domin.DMPReport;
import com.ercot.cp.ews.config.dto.DMPReportDTO;
import com.ercot.cp.ews.config.repository.DMPReportDataRepository;
import com.ercot.cp.ews.config.service.DMPReportService;
import com.ercot.cp.ews.config.transformer.DMPReportTransformer;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;

@Log4j2
@Service
@RequiredArgsConstructor
public class DMPReportServiceImpl implements DMPReportService {

    private final DMPReportTransformer dmpReportTransformer;
    private final DMPReportDataRepository dmpReportDataRepository;

    @Override
    @Transactional(timeout = 300)  // 5 minutes timeout to prevent infinite hang
    public <T> void processDTMListingsData(List<T> reportRows) {
        long startTime = System.currentTimeMillis();
        log.info("processDTMListingsData: starting with {} input rows", reportRows.size());
        
        List<DMPReport> dmpReportList = new ArrayList<>();

        reportRows.forEach(reportRow -> {
            final var row = ((DMPReportDTO) reportRow);
            // getOrCreate auto-registers any new settlement point in SPPNodes table
            dmpReportList.add(dmpReportTransformer.toEntity(row));
        });

        log.info("processDTMListingsData: totalInput={} saved={}",
                reportRows.size(), dmpReportList.size());
        
        if (dmpReportList.isEmpty()) {
            log.warn("processDTMListingsData: 0 rows qualified for DB insert");
            return;
        }

        try {
            dmpReportDataRepository.saveAll(dmpReportList);
            long duration = System.currentTimeMillis() - startTime;
            log.info("processDTMListingsData: successfully saved {} rows to DB in {}ms", 
                    dmpReportList.size(), duration);
        } catch (QueryTimeoutException e) {
            log.error("processDTMListingsData: DATABASE TIMEOUT after 300s - saved 0/{} rows. Check DB connection!", 
                    dmpReportList.size(), e);
            throw new RuntimeException("Database operation timed out", e);
        } catch (Exception e) {
            log.error("processDTMListingsData: DATABASE ERROR saving {}/{} rows: {}", 
                    dmpReportList.size(), reportRows.size(), e.getMessage(), e);
            throw new RuntimeException("Database save failed", e);
        }
    }
}