package com.ercot.cp.ews.config.dto;

import com.opencsv.bean.CsvBindByPosition;
import lombok.Data;

@Data
public class RTMReportDTO {

    @CsvBindByPosition(position = 0)
    private String deliveryDate;

    @CsvBindByPosition(position = 1)
    private Integer hourEnding;

    @CsvBindByPosition(position = 2)
    private Integer deliveryInterval;

    @CsvBindByPosition(position = 3)
    private String settlementPoint;

    @CsvBindByPosition(position = 4)
    private String settlementPointType;

    @CsvBindByPosition(position = 5)
    private String settlementPointPrice;
}