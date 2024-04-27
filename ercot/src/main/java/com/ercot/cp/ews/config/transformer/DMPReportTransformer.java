package com.ercot.cp.ews.config.transformer;

import com.ercot.cp.ews.config.constants.ConstantCodes;
import com.ercot.cp.ews.config.domin.DMPReport;
import com.ercot.cp.ews.config.dto.DMPReportDTO;
import org.springframework.stereotype.Component;

@Component
public class DMPReportTransformer {

    public DMPReport toEntity(DMPReportDTO dmpReportDTO) {
        DMPReport dmpReport = new DMPReport();

        dmpReport.setOpDate(dmpReportDTO.getDeliveryDate());
        dmpReport.setHe(dmpReportDTO.getHourEnding());
        Integer integer = ConstantCodes.sPPNodesMap
                                       .get(dmpReportDTO.getSettlementPoint());

        dmpReport.setSPPNodeId(integer);
        dmpReport.setSPP(dmpReportDTO.getSettlementPointPrice());

        return dmpReport;
    }
}