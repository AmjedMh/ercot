package com.ercot.cp.ews.config.transformer;

import com.ercot.cp.ews.config.domin.RTMReport;
import com.ercot.cp.ews.config.dto.RTMReportDTO;
import com.ercot.cp.ews.config.service.SettlementPointLookupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RTMReportTransformer {

    private final SettlementPointLookupService settlementPointLookupService;

    public RTMReport toEntity(RTMReportDTO rtmReportDTO) {
        RTMReport rtmReport = new RTMReport();

        rtmReport.setOpDate(rtmReportDTO.getDeliveryDate());
        rtmReport.setHe(rtmReportDTO.getHourEnding());
        rtmReport.setInterval(rtmReportDTO.getDeliveryInterval());
        rtmReport.setSPPNodeId(settlementPointLookupService.getOrCreate(rtmReportDTO.getSettlementPoint()));
        rtmReport.setSPP(rtmReportDTO.getSettlementPointPrice());

        return rtmReport;
    }
}