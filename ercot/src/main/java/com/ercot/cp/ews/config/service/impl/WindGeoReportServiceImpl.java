package com.ercot.cp.ews.config.service.impl;

import com.ercot.cp.ews.config.domin.WindGeoReport;
import com.ercot.cp.ews.config.dto.WindGeoReportDTO;
import com.ercot.cp.ews.config.repository.WindGeoReportDataRepository;
import com.ercot.cp.ews.config.service.WindGeoReportService;
import com.ercot.cp.ews.config.transformer.WindGeoReportTransformer;
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
public class WindGeoReportServiceImpl implements WindGeoReportService {

    private final WindGeoReportTransformer windGeoReportTransformer;
    private final WindGeoReportDataRepository windGeoReportDataRepository;
    private final WindGeoReportSaveHelper windGeoReportSaveHelper;

    @Override
    public <T> void processWindGeoData(List<T> reportRows) {
        List<WindGeoReport> entityList = new ArrayList<>();

        reportRows.forEach(reportRow -> {
            final var row = ((WindGeoReportDTO) reportRow);
            WindGeoReport entity = windGeoReportTransformer.toEntity(row);
            entityList.add(entity);
        });

        log.info("processWindGeoData: totalInput={} toSave={}", reportRows.size(), entityList.size());
        if (entityList.isEmpty()) {
            log.warn("processWindGeoData: 0 rows to save");
            return;
        }

        try {
            // Fast path: bulk insert - succeeds when no duplicates exist
            windGeoReportDataRepository.saveAll(entityList);
            log.info("processWindGeoData: saved {} rows to DB", entityList.size());
        } catch (DataIntegrityViolationException e) {
            // Fallback: row-by-row insert, each in its own transaction so duplicates are skipped gracefully
            log.warn("processWindGeoData: bulk saveAll hit duplicate key - falling back to row-by-row insert for {} rows", entityList.size());
            AtomicInteger saved = new AtomicInteger(0);
            AtomicInteger skipped = new AtomicInteger(0);
            entityList.forEach(entity -> {
                if (windGeoReportSaveHelper.saveOne(entity)) {
                    saved.incrementAndGet();
                } else {
                    skipped.incrementAndGet();
                }
            });
            log.info("processWindGeoData: row-by-row complete - saved={} skipped(duplicates)={}", saved.get(), skipped.get());
        }
    }
}
