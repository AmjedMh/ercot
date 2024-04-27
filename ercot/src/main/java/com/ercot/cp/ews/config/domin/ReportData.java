package com.ercot.cp.ews.config.domin;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReportData {

    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String reportDuration;
    private String reportName;
}