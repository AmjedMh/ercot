package com.ercot.cp.ews.config.service.impl;

import com.ercot.cp.ews.config.domin.HourlyResourceOutageCapacityReport;
import com.ercot.cp.ews.config.dto.HourlyResourceOutageCapacityReportDTO;
import com.ercot.cp.ews.config.repository.HourlyResourceOutageCapacityReportDataRepository;
import com.ercot.cp.ews.config.service.HourlyResourceOutageCapacityReportService;
import com.ercot.cp.ews.config.transformer.HourlyResourceOutageCapacityReportTransformer;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Log4j2
@Service
@RequiredArgsConstructor
public class HourlyResourceOutageCapacityReportServiceImpl implements HourlyResourceOutageCapacityReportService {

    private final HourlyResourceOutageCapacityReportTransformer hourlyResourceOutageCapacityReportTransformer;
    private final HourlyResourceOutageCapacityReportDataRepository hourlyResourceOutageCapacityReportDataRepository;
    private final HourlyResourceOutageCapacityReportSaveHelper hourlyResourceOutageCapacityReportSaveHelper;

    @Override
    public <T> void processHourlyResourceOutageCapacityData(List<T> reportRows) {
        List<HourlyResourceOutageCapacityReport> entityList = new ArrayList<>();

        reportRows.forEach(reportRow -> {
            final var row = ((HourlyResourceOutageCapacityReportDTO) reportRow);
            HourlyResourceOutageCapacityReport entity = hourlyResourceOutageCapacityReportTransformer.toEntity(row);
            entityList.add(entity);
        });

        log.info("processHourlyResourceOutageCapacityData: totalInput={} toSave={}", reportRows.size(), entityList.size());
        if (entityList.isEmpty()) {
            log.warn("processHourlyResourceOutageCapacityData: 0 rows to save");
            return;
        }

        try {
            // Fast path: bulk insert - succeeds when no duplicates exist
            hourlyResourceOutageCapacityReportDataRepository.saveAll(entityList);
            log.info("processHourlyResourceOutageCapacityData: saved {} rows to DB", entityList.size());
        } catch (DataIntegrityViolationException e) {
            // Fallback: row-by-row insert, each in its own transaction so duplicates are skipped gracefully
            log.warn("processHourlyResourceOutageCapacityData: bulk saveAll hit duplicate key - falling back to row-by-row insert for {} rows", entityList.size());
            AtomicInteger saved = new AtomicInteger(0);
            AtomicInteger skipped = new AtomicInteger(0);
            entityList.forEach(entity -> {
                if (hourlyResourceOutageCapacityReportSaveHelper.saveOne(entity)) {
                    saved.incrementAndGet();
                } else {
                    skipped.incrementAndGet();
                }
            });
            log.info("processHourlyResourceOutageCapacityData: row-by-row complete - saved={} skipped(duplicates)={}", saved.get(), skipped.get());
        }
    }
}
