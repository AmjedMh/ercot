package com.ercot.cp.ews.config;

import lombok.Data;

@Data
public class ReportConfig {
    private String id;
    private Integer buffer;
    private String cron;
    private String name;
    private String reportDuration;
}