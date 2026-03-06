package com.ercot.cp.ews.config.service;

import java.util.List;

public interface SevenDayLoadForecastReportService {

    <T> void processSevenDayLoadForecastData(List<T> reportRows);
}
