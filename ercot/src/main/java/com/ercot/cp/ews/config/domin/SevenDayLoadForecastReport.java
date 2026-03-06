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
@IdClass(SevenDayLoadForecastReportId.class)
@Table(name = "SevenDayLoadForecastbyWeatherZone")
public class SevenDayLoadForecastReport {

    @Id
    @Column(name = "opdate")
    private String opDate;

    @Id
    @Column(name = "HE")
    private Integer he;

    @Column(name = "Coast")
    private BigDecimal coast;

    @Column(name = "East")
    private BigDecimal east;

    @Column(name = "FarWest")
    private BigDecimal farWest;

    @Column(name = "North")
    private BigDecimal north;

    @Column(name = "NorthCentral")
    private BigDecimal northCentral;

    @Column(name = "SouthCentral")
    private BigDecimal southCentral;

    @Column(name = "South")
    private BigDecimal south;

    @Column(name = "West")
    private BigDecimal west;

    @Column(name = "Total")
    private BigDecimal total;
}
