package com.ercot.cp.ews.config.repository;

import com.ercot.cp.ews.config.domin.HourlyResourceOutageCapacityReport;
import com.ercot.cp.ews.config.domin.HourlyResourceOutageCapacityReportId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HourlyResourceOutageCapacityReportDataRepository extends JpaRepository<HourlyResourceOutageCapacityReport, HourlyResourceOutageCapacityReportId> {
}
