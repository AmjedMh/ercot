package com.ercot.cp.ews.config.dto;

import com.ercot.cp.ews.config.HourConverter;
import com.opencsv.bean.CsvBindByPosition;
import com.opencsv.bean.CsvCustomBindByPosition;
import lombok.Data;

@Data
public class WindGeoReportDTO {

    @CsvBindByPosition(position = 0)
    private String deliveryDate;

    @CsvCustomBindByPosition(position = 1, converter = HourConverter.class)
    private Integer hourEnding;

    // No-suffix region (CSV positions 2-5)
    @CsvBindByPosition(position = 2)
    private String actual;

    @CsvBindByPosition(position = 3)
    private String cophsl;

    @CsvBindByPosition(position = 4)
    private String stwpf;

    @CsvBindByPosition(position = 5)
    private String wgrpp;

    // Panhandle region (CSV positions 6-9)
    @CsvBindByPosition(position = 6)
    private String actualPanhandle;

    @CsvBindByPosition(position = 7)
    private String cophslPanhandle;

    @CsvBindByPosition(position = 8)
    private String stwpfPanhandle;

    @CsvBindByPosition(position = 9)
    private String wgrppPanhandle;

    // Coastal region (CSV positions 10-13)
    @CsvBindByPosition(position = 10)
    private String actualCoastal;

    @CsvBindByPosition(position = 11)
    private String cophslCoastal;

    @CsvBindByPosition(position = 12)
    private String stwpfCoastal;

    @CsvBindByPosition(position = 13)
    private String wgrppCoastal;

    // South region (CSV positions 14-17)
    @CsvBindByPosition(position = 14)
    private String actualSouth;

    @CsvBindByPosition(position = 15)
    private String cophslSouth;

    @CsvBindByPosition(position = 16)
    private String stwpfSouth;

    @CsvBindByPosition(position = 17)
    private String wgrppSouth;

    // West region (CSV positions 18-21)
    @CsvBindByPosition(position = 18)
    private String actualWest;

    @CsvBindByPosition(position = 19)
    private String cophslWest;

    @CsvBindByPosition(position = 20)
    private String stwpfWest;

    @CsvBindByPosition(position = 21)
    private String wgrppWest;

    // North region (CSV positions 22-25)
    @CsvBindByPosition(position = 22)
    private String actualNorth;

    @CsvBindByPosition(position = 23)
    private String cophslNorth;

    @CsvBindByPosition(position = 24)
    private String stwpfNorth;

    @CsvBindByPosition(position = 25)
    private String wgrppNorth;
}
