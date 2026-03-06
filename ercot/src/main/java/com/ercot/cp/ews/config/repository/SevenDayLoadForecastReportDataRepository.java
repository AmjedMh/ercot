package com.ercot.cp.ews.config.repository;

import com.ercot.cp.ews.config.domin.SevenDayLoadForecastReport;
import com.ercot.cp.ews.config.domin.SevenDayLoadForecastReportId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SevenDayLoadForecastReportDataRepository extends JpaRepository<SevenDayLoadForecastReport, SevenDayLoadForecastReportId> {
}
