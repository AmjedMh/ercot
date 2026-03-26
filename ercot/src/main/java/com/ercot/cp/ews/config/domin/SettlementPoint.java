package com.ercot.cp.ews.config.domin;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Represents a known ERCOT settlement point node.
 * Stored in the SPPNodes table and loaded into memory at startup.
 * New nodes seen in inbound reports are auto-registered here.
 */
@Data
@Entity
@Table(name = "SPPNodes")
public class SettlementPoint {

    /** Stable integer surrogate key — part of the RTMSPPs_Interval composite PK. */
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    /** ERCOT settlement point name (e.g. "HB_NORTH", "A4_DGR1_RN"). */
    @Column(name = "name", unique = true, nullable = false, length = 150)
    private String name;
}
