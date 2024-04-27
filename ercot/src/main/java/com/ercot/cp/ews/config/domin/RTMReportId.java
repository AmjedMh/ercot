package com.ercot.cp.ews.config.domin;

import lombok.Data;
import java.io.Serializable;

@Data
public class RTMReportId implements Serializable {

    private String opDate;
    private Integer he;
    private Integer interval;
    private Integer sPPNodeId;
}