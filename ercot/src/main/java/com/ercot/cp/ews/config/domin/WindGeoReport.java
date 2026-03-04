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
@IdClass(WindGeoReportId.class)
@Table(name = "WindPowerProductionHourlyAveragedActualandForecastedValuesbyGeographicalRegion")
public class WindGeoReport {

    @Id
    @Column(name = "opdate")
    private String opDate;

    @Id
    @Column(name = "HE")
    private Integer he;

    // No-suffix region (columns 3-6)
    @Column(name = "Actual")
    private BigDecimal actual;

    @Column(name = "COPHSL")
    private BigDecimal cophsl;

    @Column(name = "STWPF")
    private BigDecimal stwpf;

    @Column(name = "WGRPP")
    private BigDecimal wgrpp;

    // Panhandle region (columns 7-10)
    @Column(name = "Actual_Panhandle")
    private BigDecimal actualPanhandle;

    @Column(name = "COPHSL_Panhandle")
    private BigDecimal cophslPanhandle;

    @Column(name = "STWPF_Panhandle")
    private BigDecimal stwpfPanhandle;

    @Column(name = "WGRPP_Panhandle")
    private BigDecimal wgrppPanhandle;

    // Coastal region (columns 11-14)
    @Column(name = "Actual_Coastal")
    private BigDecimal actualCoastal;

    @Column(name = "COPHSL_Coastal")
    private BigDecimal cophslCoastal;

    @Column(name = "STWPF_Coastal")
    private BigDecimal stwpfCoastal;

    @Column(name = "WGRPP_Coastal")
    private BigDecimal wgrppCoastal;

    // South region (columns 15-18)
    @Column(name = "Actual_South")
    private BigDecimal actualSouth;

    @Column(name = "COPHSL_South")
    private BigDecimal cophslSouth;

    @Column(name = "STWPF_South")
    private BigDecimal stwpfSouth;

    @Column(name = "WGRPP_South")
    private BigDecimal wgrppSouth;

    // West region (columns 19-22)
    @Column(name = "Actual_West")
    private BigDecimal actualWest;

    @Column(name = "COPHSL_West")
    private BigDecimal cophslWest;

    @Column(name = "STWPF_West")
    private BigDecimal stwpfWest;

    @Column(name = "WGRPP_West")
    private BigDecimal wgrppWest;

    // North region (columns 23-26)
    @Column(name = "Actual_North")
    private BigDecimal actualNorth;

    @Column(name = "COPHSL_North")
    private BigDecimal cophslNorth;

    @Column(name = "STWPF_North")
    private BigDecimal stwpfNorth;

    @Column(name = "WGRPP_North")
    private BigDecimal wgrppNorth;
}
