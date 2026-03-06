package com.ercot.cp.ews.config.service.impl;

import com.ercot.cp.ews.config.domin.SolarPowerReport;
import com.ercot.cp.ews.config.dto.SolarPowerReportDTO;
import com.ercot.cp.ews.config.repository.SolarPowerReportDataRepository;
import com.ercot.cp.ews.config.service.SolarPowerReportService;
import com.ercot.cp.ews.config.transformer.SolarPowerReportTransformer;
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
public class SolarPowerReportServiceImpl implements SolarPowerReportService {

    private final SolarPowerReportTransformer solarPowerReportTransformer;
    private final SolarPowerReportDataRepository solarPowerReportDataRepository;
    private final SolarPowerReportSaveHelper solarPowerReportSaveHelper;

    @Override
    public <T> void processSolarPowerData(List<T> reportRows) {
        List<SolarPowerReport> entityList = new ArrayList<>();

        reportRows.forEach(reportRow -> {
            final var row = ((SolarPowerReportDTO) reportRow);
            SolarPowerReport entity = solarPowerReportTransformer.toEntity(row);
            entityList.add(entity);
        });

        log.info("processSolarPowerData: totalInput={} toSave={}", reportRows.size(), entityList.size());
        if (entityList.isEmpty()) {
            log.warn("processSolarPowerData: 0 rows to save");
            return;
        }

        try {
            // Fast path: bulk insert - succeeds when no duplicates exist
            solarPowerReportDataRepository.saveAll(entityList);
            log.info("processSolarPowerData: saved {} rows to DB", entityList.size());
        } catch (DataIntegrityViolationException e) {
            // Fallback: row-by-row insert, each in its own transaction so duplicates are skipped gracefully
            log.warn("processSolarPowerData: bulk saveAll hit duplicate key - falling back to row-by-row insert for {} rows", entityList.size());
            AtomicInteger saved = new AtomicInteger(0);
            AtomicInteger skipped = new AtomicInteger(0);
            entityList.forEach(entity -> {
                if (solarPowerReportSaveHelper.saveOne(entity)) {
                    saved.incrementAndGet();
                } else {
                    skipped.incrementAndGet();
                }
            });
            log.info("processSolarPowerData: row-by-row complete - saved={} skipped(duplicates)={}", saved.get(), skipped.get());
        }
    }
}
