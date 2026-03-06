package com.ercot.cp.ews.config.repository;

import com.ercot.cp.ews.config.domin.SolarPowerReport;
import com.ercot.cp.ews.config.domin.SolarPowerReportId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SolarPowerReportDataRepository extends JpaRepository<SolarPowerReport, SolarPowerReportId> {
}
