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
@IdClass(HourlyResourceOutageCapacityReportId.class)
@Table(name = "HourlyResourceOutageCapacity")
public class HourlyResourceOutageCapacityReport {

    @Id
    @Column(name = "opdate")
    private String opDate;

    @Id
    @Column(name = "HE")
    private Integer he;

    @Column(name = "SouthTotal")
    private BigDecimal southTotal;

    @Column(name = "NorthTotal")
    private BigDecimal northTotal;

    @Column(name = "WestTotal")
    private BigDecimal westTotal;

    @Column(name = "HoustonTotal")
    private BigDecimal houstonTotal;

    @Column(name = "SouthIRR")
    private BigDecimal southIRR;

    @Column(name = "NorthIRR")
    private BigDecimal northIRR;

    @Column(name = "WestIRR")
    private BigDecimal westIRR;

    @Column(name = "HoustonIRR")
    private BigDecimal houstonIRR;

    @Column(name = "SouthNew")
    private BigDecimal southNew;

    @Column(name = "NorthNew")
    private BigDecimal northNew;

    @Column(name = "WestNew")
    private BigDecimal westNew;

    @Column(name = "HoustonNew")
    private BigDecimal houstonNew;
}
