package com.ercot.cp.ews.config.dto;

import com.ercot.cp.ews.config.HourConverter;
import com.opencsv.bean.CsvBindByPosition;
import com.opencsv.bean.CsvCustomBindByPosition;
import lombok.Data;

@Data
public class SolarPowerReportDTO {

    @CsvBindByPosition(position = 0)
    private String deliveryDate;

    @CsvCustomBindByPosition(position = 1, converter = HourConverter.class)
    private Integer hourEnding;

    @CsvBindByPosition(position = 2)
    private String actual;

    @CsvBindByPosition(position = 3)
    private String cophsl;

    @CsvBindByPosition(position = 4)
    private String stppf;

    @CsvBindByPosition(position = 5)
    private String pvgrpp;
}
