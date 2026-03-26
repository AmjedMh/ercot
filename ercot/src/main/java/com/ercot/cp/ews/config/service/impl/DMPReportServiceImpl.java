package com.ercot.cp.ews.config.service.impl;

import com.ercot.cp.ews.config.domin.DMPReport;
import com.ercot.cp.ews.config.dto.DMPReportDTO;
import com.ercot.cp.ews.config.repository.DMPReportDataRepository;
import com.ercot.cp.ews.config.service.DMPReportService;
import com.ercot.cp.ews.config.transformer.DMPReportTransformer;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Log4j2
@Service
@RequiredArgsConstructor
public class DMPReportServiceImpl implements DMPReportService {

    private final DMPReportTransformer dmpReportTransformer;
    private final DMPReportDataRepository dmpReportDataRepository;

    @Override
    public <T> void processDTMListingsData(List<T> reportRows) {
        List<DMPReport> dmpReportList = new ArrayList<>();

        reportRows.forEach(reportRow -> {
            final var row = ((DMPReportDTO) reportRow);
            // getOrCreate auto-registers any new settlement point in SPPNodes table
            dmpReportList.add(dmpReportTransformer.toEntity(row));
        });

        log.info("processDTMListingsData: totalInput={} saved={}",
                reportRows.size(), dmpReportList.size());
        if (!dmpReportList.isEmpty()) {
            dmpReportDataRepository.saveAll(dmpReportList);
            log.info("processDTMListingsData: saved {} rows to DB", dmpReportList.size());
        } else {
            log.warn("processDTMListingsData: 0 rows qualified for DB insert");
        }
    }
}