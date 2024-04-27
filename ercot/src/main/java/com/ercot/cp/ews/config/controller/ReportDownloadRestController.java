package com.ercot.cp.ews.config.controller;

import com.ercot.cp.ews.config.ReportConfig;
import com.ercot.cp.ews.config.domin.ReportData;
import com.ercot.cp.ews.config.service.DownloadReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequiredArgsConstructor
public class ReportDownloadRestController {

    private final DownloadReportService downloadReportService;

    @PostMapping("/create-report")
    public String createReport(@RequestBody ReportConfig reportConfig) {
        downloadReportService.downloadReport(reportConfig);
        return "Ok";
    }

    @PostMapping("/dump-report")
    public String dumpReport(@RequestBody ReportData reportData) {
        downloadReportService.dumpReport(reportData);
        return "Ok";
    }
}