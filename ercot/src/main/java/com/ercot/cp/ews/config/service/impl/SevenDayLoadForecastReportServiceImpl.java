package com.ercot.cp.ews.config.service.impl;

import com.ercot.cp.ews.config.domin.SevenDayLoadForecastReport;
import com.ercot.cp.ews.config.dto.SevenDayLoadForecastReportDTO;
import com.ercot.cp.ews.config.repository.SevenDayLoadForecastReportDataRepository;
import com.ercot.cp.ews.config.service.SevenDayLoadForecastReportService;
import com.ercot.cp.ews.config.transformer.SevenDayLoadForecastReportTransformer;
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
public class SevenDayLoadForecastReportServiceImpl implements SevenDayLoadForecastReportService {

    private final SevenDayLoadForecastReportTransformer sevenDayLoadForecastReportTransformer;
    private final SevenDayLoadForecastReportDataRepository sevenDayLoadForecastReportDataRepository;
    private final SevenDayLoadForecastReportSaveHelper sevenDayLoadForecastReportSaveHelper;

    @Override
    public <T> void processSevenDayLoadForecastData(List<T> reportRows) {
        List<SevenDayLoadForecastReport> entityList = new ArrayList<>();

        reportRows.forEach(reportRow -> {
            final var row = ((SevenDayLoadForecastReportDTO) reportRow);
            SevenDayLoadForecastReport entity = sevenDayLoadForecastReportTransformer.toEntity(row);
            entityList.add(entity);
        });

        log.info("processSevenDayLoadForecastData: totalInput={} toSave={}", reportRows.size(), entityList.size());
        if (entityList.isEmpty()) {
            log.warn("processSevenDayLoadForecastData: 0 rows to save");
            return;
        }

        try {
            // Fast path: bulk insert - succeeds when no duplicates exist
            sevenDayLoadForecastReportDataRepository.saveAll(entityList);
            log.info("processSevenDayLoadForecastData: saved {} rows to DB", entityList.size());
        } catch (DataIntegrityViolationException e) {
            // Fallback: row-by-row insert, each in its own transaction so duplicates are skipped gracefully
            log.warn("processSevenDayLoadForecastData: bulk saveAll hit duplicate key - falling back to row-by-row insert for {} rows", entityList.size());
            AtomicInteger saved = new AtomicInteger(0);
            AtomicInteger skipped = new AtomicInteger(0);
            entityList.forEach(entity -> {
                if (sevenDayLoadForecastReportSaveHelper.saveOne(entity)) {
                    saved.incrementAndGet();
                } else {
                    skipped.incrementAndGet();
                }
            });
            log.info("processSevenDayLoadForecastData: row-by-row complete - saved={} skipped(duplicates)={}", saved.get(), skipped.get());
        }
    }
}
