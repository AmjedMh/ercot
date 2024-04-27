package com.ercot.cp.ews.config.service;

import java.util.List;

public interface DMPReportService {

    <T> void processDTMListingsData(List<T> reportRows);
}