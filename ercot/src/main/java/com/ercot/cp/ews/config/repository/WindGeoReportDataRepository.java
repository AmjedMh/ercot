package com.ercot.cp.ews.config.repository;

import com.ercot.cp.ews.config.domin.WindGeoReport;
import com.ercot.cp.ews.config.domin.WindGeoReportId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WindGeoReportDataRepository extends JpaRepository<WindGeoReport, WindGeoReportId> {
}
