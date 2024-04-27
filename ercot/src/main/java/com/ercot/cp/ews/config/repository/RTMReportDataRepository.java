package com.ercot.cp.ews.config.repository;

import com.ercot.cp.ews.config.domin.RTMReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RTMReportDataRepository extends JpaRepository<RTMReport, String> {
}