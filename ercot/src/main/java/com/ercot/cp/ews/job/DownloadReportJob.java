package com.ercot.cp.ews.job;

import com.ercot.cp.ews.config.ReportConfig;
import com.ercot.cp.ews.config.service.DownloadReportService;
import lombok.RequiredArgsConstructor;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DownloadReportJob implements Job {

    private final DownloadReportService downloadReportService;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        ReportConfig reportConfig = (ReportConfig) jobExecutionContext.getMergedJobDataMap()
                                                                      .get("reportConfig");
        downloadReportService.downloadReport(reportConfig);
    }
}
