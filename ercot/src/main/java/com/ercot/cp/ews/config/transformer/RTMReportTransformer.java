package com.ercot.cp.ews.config.transformer;

import com.ercot.cp.ews.config.constants.ConstantCodes;
import com.ercot.cp.ews.config.domin.RTMReport;
import com.ercot.cp.ews.config.dto.RTMReportDTO;
import org.springframework.stereotype.Component;

@Component
public class RTMReportTransformer {

    public RTMReport toEntity(RTMReportDTO rtmReportDTO) {
        RTMReport rtmReport = new RTMReport();

        rtmReport.setOpDate(rtmReportDTO.getDeliveryDate());
        rtmReport.setHe(rtmReportDTO.getHourEnding());
        rtmReport.setInterval(rtmReportDTO.getDeliveryInterval());
        Integer integer = ConstantCodes.sPPNodesMap
                                       .get(rtmReportDTO.getSettlementPoint());
        rtmReport.setSPPNodeId(integer);
        rtmReport.setSPP(rtmReportDTO.getSettlementPointPrice());

        return rtmReport;
    }
}