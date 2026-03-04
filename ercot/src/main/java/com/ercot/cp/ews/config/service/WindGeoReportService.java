package com.ercot.cp.ews.config.service;

import java.util.List;

public interface WindGeoReportService {

    <T> void processWindGeoData(List<T> reportRows);
}
