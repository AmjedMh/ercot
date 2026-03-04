package com.ercot.cp.ews.config.service.impl;

import com.ercot.cp.ews.config.domin.WindGeoReport;
import com.ercot.cp.ews.config.dto.WindGeoReportDTO;
import com.ercot.cp.ews.config.repository.WindGeoReportDataRepository;
import com.ercot.cp.ews.config.service.WindGeoReportService;
import com.ercot.cp.ews.config.transformer.WindGeoReportTransformer;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Log4j2
@Service
@RequiredArgsConstructor
public class WindGeoReportServiceImpl implements WindGeoReportService {

    private final WindGeoReportTransformer windGeoReportTransformer;
    private final WindGeoReportDataRepository windGeoReportDataRepository;

    @Override
    public <T> void processWindGeoData(List<T> reportRows) {
        List<WindGeoReport> entityList = new ArrayList<>();

        reportRows.forEach(reportRow -> {
            final var row = ((WindGeoReportDTO) reportRow);
            WindGeoReport entity = windGeoReportTransformer.toEntity(row);
            entityList.add(entity);
        });

        log.info("processWindGeoData: totalInput={} toSave={}", reportRows.size(), entityList.size());
        if (!entityList.isEmpty()) {
            windGeoReportDataRepository.saveAll(entityList);
            log.info("processWindGeoData: saved {} rows to DB", entityList.size());
        } else {
            log.warn("processWindGeoData: 0 rows to save");
        }
    }
}
