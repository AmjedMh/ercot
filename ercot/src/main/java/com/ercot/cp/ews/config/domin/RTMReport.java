package com.ercot.cp.ews.config.domin;

import lombok.Data;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;

@Data
@Entity(name = "RTMSPPs_Interval")
@IdClass(RTMReportId.class)
public class RTMReport {

    @Id
    @Column(name = "opdate")
    private String opDate;

    @Id
    @Column(name = "HE")
    private Integer he;

    @Id
    @Column(name = "Interval")
    private Integer interval;

    @Id
    @Column(name = "SPPNodeId")
    private Integer sPPNodeId;

    @Column(name = "SPP")
    private String sPP;
}