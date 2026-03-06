package com.ercot.cp.ews.config.service;

import java.util.List;

public interface HourlyResourceOutageCapacityReportService {

    <T> void processHourlyResourceOutageCapacityData(List<T> reportRows);
}
