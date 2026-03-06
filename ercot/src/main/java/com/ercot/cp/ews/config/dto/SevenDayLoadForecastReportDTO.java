package com.ercot.cp.ews.config.dto;

import com.ercot.cp.ews.config.HourConverter;
import com.opencsv.bean.CsvBindByPosition;
import com.opencsv.bean.CsvCustomBindByPosition;
import lombok.Data;

@Data
public class SevenDayLoadForecastReportDTO {

    @CsvBindByPosition(position = 0)
    private String deliveryDate;

    @CsvCustomBindByPosition(position = 1, converter = HourConverter.class)
    private Integer hourEnding;

    @CsvBindByPosition(position = 2)
    private String coast;

    @CsvBindByPosition(position = 3)
    private String east;

    @CsvBindByPosition(position = 4)
    private String farWest;

    @CsvBindByPosition(position = 5)
    private String north;

    @CsvBindByPosition(position = 6)
    private String northCentral;

    @CsvBindByPosition(position = 7)
    private String southCentral;

    @CsvBindByPosition(position = 8)
    private String south;

    @CsvBindByPosition(position = 9)
    private String west;

    @CsvBindByPosition(position = 10)
    private String total;
}
