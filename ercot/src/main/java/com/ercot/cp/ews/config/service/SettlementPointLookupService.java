package com.ercot.cp.ews.config.service;

public interface SettlementPointLookupService {

    /**
     * Returns the integer surrogate ID for the given settlement point name.
     * If the name has never been seen before, it is automatically persisted
     * to the SPPNodes table and registered in the in-memory cache so that
     * subsequent lookups are instant.
     *
     * @param name ERCOT settlement point name (e.g. "HB_NORTH", "A4_DGR1_RN")
     * @return the stable integer ID used as SPPNodeId in RTMSPPs_Interval
     */
    Integer getOrCreate(String name);
}
