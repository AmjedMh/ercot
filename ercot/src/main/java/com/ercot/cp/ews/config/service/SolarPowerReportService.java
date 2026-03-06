package com.ercot.cp.ews.config.service;

import java.util.List;

public interface SolarPowerReportService {

    <T> void processSolarPowerData(List<T> reportRows);
}
