package com.ercot.cp.ews.config.service.impl;

import com.ercot.cp.ews.config.domin.RTMReport;
import com.ercot.cp.ews.config.dto.RTMReportDTO;
import com.ercot.cp.ews.config.repository.RTMReportDataRepository;
import com.ercot.cp.ews.config.service.RTMReportService;
import com.ercot.cp.ews.config.transformer.RTMReportTransformer;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Log4j2
@Service
@RequiredArgsConstructor
public class RTMReportServiceImpl implements RTMReportService {

    private final RTMReportTransformer rtmReportTransformer;
    private final RTMReportDataRepository rtmReportDataRepository;

    @Override
    public <T> void processRTMListingsData(List<T> reportRows) {

        List<RTMReport> rtmReportList = new ArrayList<>();
        reportRows.forEach(reportRow -> {
            final var row = ((RTMReportDTO) reportRow);

            boolean bPointType = row.getSettlementPointType()
                                    .endsWith("EW");
            if (bPointType) return;

            RTMReport rtmReport = rtmReportTransformer.toEntity(row);
            rtmReportList.add(rtmReport);
        });

        log.debug("processRTMListingsData rtmReportList: {}", rtmReportList.size());
        rtmReportDataRepository.saveAll(rtmReportList);
    }
}