package com.ercot.cp.ews.config.domin;

import lombok.Data;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

@Data
@Entity
@IdClass(DMPReportId.class)
@Table(name = "DAMSPPs")
public class DMPReport {

    @Id
    @Column(name = "opdate")
    private String opDate;

    @Id
    @Column(name = "HE")
    private Integer he;

    @Id
    @Column(name = "SPPNodeId")
    private Integer sPPNodeId;

    @Id
    @Column(name = "SPP")
    private String sPP;
}