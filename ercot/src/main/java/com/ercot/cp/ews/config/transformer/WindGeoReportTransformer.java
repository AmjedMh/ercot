package com.ercot.cp.ews.config.transformer;

import com.ercot.cp.ews.config.domin.WindGeoReport;
import com.ercot.cp.ews.config.dto.WindGeoReportDTO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class WindGeoReportTransformer {

    public WindGeoReport toEntity(WindGeoReportDTO dto) {
        WindGeoReport entity = new WindGeoReport();

        entity.setOpDate(dto.getDeliveryDate());
        entity.setHe(dto.getHourEnding());

        entity.setActual(parseBigDecimal(dto.getActual()));
        entity.setCophsl(parseBigDecimal(dto.getCophsl()));
        entity.setStwpf(parseBigDecimal(dto.getStwpf()));
        entity.setWgrpp(parseBigDecimal(dto.getWgrpp()));

        entity.setActualPanhandle(parseBigDecimal(dto.getActualPanhandle()));
        entity.setCophslPanhandle(parseBigDecimal(dto.getCophslPanhandle()));
        entity.setStwpfPanhandle(parseBigDecimal(dto.getStwpfPanhandle()));
        entity.setWgrppPanhandle(parseBigDecimal(dto.getWgrppPanhandle()));

        entity.setActualCoastal(parseBigDecimal(dto.getActualCoastal()));
        entity.setCophslCoastal(parseBigDecimal(dto.getCophslCoastal()));
        entity.setStwpfCoastal(parseBigDecimal(dto.getStwpfCoastal()));
        entity.setWgrppCoastal(parseBigDecimal(dto.getWgrppCoastal()));

        entity.setActualSouth(parseBigDecimal(dto.getActualSouth()));
        entity.setCophslSouth(parseBigDecimal(dto.getCophslSouth()));
        entity.setStwpfSouth(parseBigDecimal(dto.getStwpfSouth()));
        entity.setWgrppSouth(parseBigDecimal(dto.getWgrppSouth()));

        entity.setActualWest(parseBigDecimal(dto.getActualWest()));
        entity.setCophslWest(parseBigDecimal(dto.getCophslWest()));
        entity.setStwpfWest(parseBigDecimal(dto.getStwpfWest()));
        entity.setWgrppWest(parseBigDecimal(dto.getWgrppWest()));

        entity.setActualNorth(parseBigDecimal(dto.getActualNorth()));
        entity.setCophslNorth(parseBigDecimal(dto.getCophslNorth()));
        entity.setStwpfNorth(parseBigDecimal(dto.getStwpfNorth()));
        entity.setWgrppNorth(parseBigDecimal(dto.getWgrppNorth()));

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
