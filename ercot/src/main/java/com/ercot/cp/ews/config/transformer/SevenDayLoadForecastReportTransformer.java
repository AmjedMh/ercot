package com.ercot.cp.ews.config.transformer;

import com.ercot.cp.ews.config.domin.SevenDayLoadForecastReport;
import com.ercot.cp.ews.config.dto.SevenDayLoadForecastReportDTO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class SevenDayLoadForecastReportTransformer {

    public SevenDayLoadForecastReport toEntity(SevenDayLoadForecastReportDTO dto) {
        SevenDayLoadForecastReport entity = new SevenDayLoadForecastReport();

        entity.setOpDate(dto.getDeliveryDate());
        entity.setHe(dto.getHourEnding());

        entity.setCoast(parseBigDecimal(dto.getCoast()));
        entity.setEast(parseBigDecimal(dto.getEast()));
        entity.setFarWest(parseBigDecimal(dto.getFarWest()));
        entity.setNorth(parseBigDecimal(dto.getNorth()));
        entity.setNorthCentral(parseBigDecimal(dto.getNorthCentral()));
        entity.setSouthCentral(parseBigDecimal(dto.getSouthCentral()));
        entity.setSouth(parseBigDecimal(dto.getSouth()));
        entity.setWest(parseBigDecimal(dto.getWest()));
        entity.setTotal(parseBigDecimal(dto.getTotal()));

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
