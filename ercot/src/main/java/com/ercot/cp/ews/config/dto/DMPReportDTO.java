package com.ercot.cp.ews.config.dto;

import com.ercot.cp.ews.config.HourConverter;
import com.opencsv.bean.CsvBindByPosition;
import com.opencsv.bean.CsvCustomBindByPosition;
import lombok.Data;

@Data
public class DMPReportDTO {

    @CsvBindByPosition(position = 0)
    private String deliveryDate;

    @CsvCustomBindByPosition(position = 1, converter = HourConverter.class)
    private Integer hourEnding;

    @CsvBindByPosition(position = 2)
    private String settlementPoint;

    @CsvBindByPosition(position = 3)
    private String settlementPointPrice;
}