package com.ercot.cp.ews.config.service;

import com.ercot.cp.ews.config.ReportConfig;
import com.ercot.cp.ews.config.domin.ReportData;

public interface DownloadReportService {

    void downloadReport(ReportConfig reportConfig);

    void dumpReport(ReportData reportData);
}