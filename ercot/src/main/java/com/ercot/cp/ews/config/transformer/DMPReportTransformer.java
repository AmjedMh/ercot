package com.ercot.cp.ews.config.transformer;

import com.ercot.cp.ews.config.domin.DMPReport;
import com.ercot.cp.ews.config.dto.DMPReportDTO;
import com.ercot.cp.ews.config.service.SettlementPointLookupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DMPReportTransformer {

    private final SettlementPointLookupService settlementPointLookupService;

    public DMPReport toEntity(DMPReportDTO dmpReportDTO) {
        DMPReport dmpReport = new DMPReport();

        dmpReport.setOpDate(dmpReportDTO.getDeliveryDate());
        dmpReport.setHe(dmpReportDTO.getHourEnding());
        dmpReport.setSPPNodeId(settlementPointLookupService.getOrCreate(dmpReportDTO.getSettlementPoint()));
        dmpReport.setSPP(dmpReportDTO.getSettlementPointPrice());

        return dmpReport;
    }
}