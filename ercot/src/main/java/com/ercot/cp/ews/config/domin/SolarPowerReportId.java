package com.ercot.cp.ews.config.domin;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@EqualsAndHashCode
public class SolarPowerReportId implements Serializable {

    private String opDate;
    private Integer he;
}
