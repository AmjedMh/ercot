package com.ercot.cp.ews.config.service;

import java.util.List;

public interface RTMReportService {

    <T> void processRTMListingsData(List<T> reportRows);
}