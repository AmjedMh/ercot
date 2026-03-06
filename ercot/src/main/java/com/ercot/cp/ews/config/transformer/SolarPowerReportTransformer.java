package com.ercot.cp.ews.config.transformer;

import com.ercot.cp.ews.config.domin.SolarPowerReport;
import com.ercot.cp.ews.config.dto.SolarPowerReportDTO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class SolarPowerReportTransformer {

    public SolarPowerReport toEntity(SolarPowerReportDTO dto) {
        SolarPowerReport entity = new SolarPowerReport();

        entity.setOpDate(dto.getDeliveryDate());
        entity.setHe(dto.getHourEnding());

        entity.setActual(parseBigDecimal(dto.getActual()));
        entity.setCophsl(parseBigDecimal(dto.getCophsl()));
        entity.setStppf(parseBigDecimal(dto.getStppf()));
        entity.setPvgrpp(parseBigDecimal(dto.getPvgrpp()));

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
