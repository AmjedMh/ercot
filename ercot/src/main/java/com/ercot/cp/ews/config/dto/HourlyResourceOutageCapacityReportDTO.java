package com.ercot.cp.ews.config.dto;

import com.ercot.cp.ews.config.HourConverter;
import com.opencsv.bean.CsvBindByPosition;
import com.opencsv.bean.CsvCustomBindByPosition;
import lombok.Data;

@Data
public class HourlyResourceOutageCapacityReportDTO {

    @CsvBindByPosition(position = 0)
    private String deliveryDate;

    @CsvCustomBindByPosition(position = 1, converter = HourConverter.class)
    private Integer hourEnding;

    @CsvBindByPosition(position = 2)
    private String southTotal;

    @CsvBindByPosition(position = 3)
    private String northTotal;

    @CsvBindByPosition(position = 4)
    private String westTotal;

    @CsvBindByPosition(position = 5)
    private String houstonTotal;

    @CsvBindByPosition(position = 6)
    private String southIRR;

    @CsvBindByPosition(position = 7)
    private String northIRR;

    @CsvBindByPosition(position = 8)
    private String westIRR;

    @CsvBindByPosition(position = 9)
    private String houstonIRR;

    @CsvBindByPosition(position = 10)
    private String southNew;

    @CsvBindByPosition(position = 11)
    private String northNew;

    @CsvBindByPosition(position = 12)
    private String westNew;

    @CsvBindByPosition(position = 13)
    private String houstonNew;
}
