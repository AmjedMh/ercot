package com.ercot.cp.ews.config.service.impl;

import com.ercot.cp.ews.config.domin.RTMReport;
import com.ercot.cp.ews.config.dto.RTMReportDTO;
import com.ercot.cp.ews.config.constants.ConstantCodes;
import com.ercot.cp.ews.config.repository.RTMReportDataRepository;
import com.ercot.cp.ews.config.service.RTMReportService;
import com.ercot.cp.ews.config.transformer.RTMReportTransformer;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

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
    public <T> void processRTMListingsData(List<T> reportRows) {

        List<RTMReport> rtmReportList = new ArrayList<>();
        AtomicInteger skippedNoMapping = new AtomicInteger(0);
        AtomicInteger skippedEW = new AtomicInteger(0);

        reportRows.forEach(reportRow -> {
            final var row = ((RTMReportDTO) reportRow);

            Integer integer = ConstantCodes.sPPNodesMap
                                       .get(row.getSettlementPoint());
            if (integer == null) {
                skippedNoMapping.incrementAndGet();
                log.debug("skip Settlement (not in sPPNodesMap): {}", row.getSettlementPoint());
                return ;
            }

            boolean bPointType = row.getSettlementPointType()
                                    .endsWith("EW");
            if (bPointType) {
                skippedEW.incrementAndGet();
                return;
            }

            RTMReport rtmReport = rtmReportTransformer.toEntity(row);
            rtmReportList.add(rtmReport);
        });

        log.info("processRTMListingsData: totalInput={} mapped={} skippedNoMapping={} skippedEW={}",
                reportRows.size(), rtmReportList.size(), skippedNoMapping.get(), skippedEW.get());
        if (!rtmReportList.isEmpty()) {
            rtmReportDataRepository.saveAll(rtmReportList);
            log.info("processRTMListingsData: saved {} rows to DB", rtmReportList.size());
        } else {
            log.warn("processRTMListingsData: 0 rows qualified for DB insert - check sPPNodesMap coverage");
        }
    }
}