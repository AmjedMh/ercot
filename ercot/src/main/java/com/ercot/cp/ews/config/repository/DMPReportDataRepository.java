package com.ercot.cp.ews.config.repository;

import com.ercot.cp.ews.config.domin.DMPReport;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Primary
@Repository
public interface DMPReportDataRepository extends JpaRepository<DMPReport, String> {
}