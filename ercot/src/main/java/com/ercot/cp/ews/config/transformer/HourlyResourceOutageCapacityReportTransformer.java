package com.ercot.cp.ews.config.transformer;

import com.ercot.cp.ews.config.domin.HourlyResourceOutageCapacityReport;
import com.ercot.cp.ews.config.dto.HourlyResourceOutageCapacityReportDTO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class HourlyResourceOutageCapacityReportTransformer {

    public HourlyResourceOutageCapacityReport toEntity(HourlyResourceOutageCapacityReportDTO dto) {
        HourlyResourceOutageCapacityReport entity = new HourlyResourceOutageCapacityReport();

        entity.setOpDate(dto.getDeliveryDate());
        entity.setHe(dto.getHourEnding());

        entity.setSouthTotal(parseBigDecimal(dto.getSouthTotal()));
        entity.setNorthTotal(parseBigDecimal(dto.getNorthTotal()));
        entity.setWestTotal(parseBigDecimal(dto.getWestTotal()));
        entity.setHoustonTotal(parseBigDecimal(dto.getHoustonTotal()));

        entity.setSouthIRR(parseBigDecimal(dto.getSouthIRR()));
        entity.setNorthIRR(parseBigDecimal(dto.getNorthIRR()));
        entity.setWestIRR(parseBigDecimal(dto.getWestIRR()));
        entity.setHoustonIRR(parseBigDecimal(dto.getHoustonIRR()));

        entity.setSouthNew(parseBigDecimal(dto.getSouthNew()));
        entity.setNorthNew(parseBigDecimal(dto.getNorthNew()));
        entity.setWestNew(parseBigDecimal(dto.getWestNew()));
        entity.setHoustonNew(parseBigDecimal(dto.getHoustonNew()));

        return entity;
    }

    private BigDecimal parseBigDecimal(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
