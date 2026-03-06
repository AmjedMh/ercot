package com.ercot.cp.ews.config.domin;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import java.math.BigDecimal;

@Data
@Entity
@IdClass(SolarPowerReportId.class)
@Table(name = "SolarPowerProductionHourlyAveragedActualandForecastedValues")
public class SolarPowerReport {

    @Id
    @Column(name = "opdate")
    private String opDate;

    @Id
    @Column(name = "HE")
    private Integer he;

    @Column(name = "Actual")
    private BigDecimal actual;

    @Column(name = "COPHSL")
    private BigDecimal cophsl;

    @Column(name = "STPPF")
    private BigDecimal stppf;

    @Column(name = "PVGRPP")
    private BigDecimal pvgrpp;
}
