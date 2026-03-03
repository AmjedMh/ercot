package com.ercot.cp.ews.config.controller;

import com.ercot.cp.ews.config.ReportConfig;
import com.ercot.cp.ews.config.domin.ReportData;
import com.ercot.cp.ews.config.service.DownloadReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

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
    public ResponseEntity<Map<String, Object>> dumpReport(@RequestBody ReportData reportData) {
        try {
            downloadReportService.dumpReport(reportData);
            return buildResponse(HttpStatus.OK, "Report dump completed successfully", reportData.getReportName());
        } catch (Exception e) {
            log.error("Failed to dump report: {}", e.getMessage(), e);
            return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to dump report: " + e.getMessage(), reportData.getReportName());
        }
    }

    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String message, String reportName) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("message", message);
        body.put("reportName", reportName);
        return ResponseEntity.status(status).body(body);
    }
}