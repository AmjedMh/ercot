package com.ercot.cp.ews.config.repository;

import com.ercot.cp.ews.config.domin.SettlementPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SettlementPointRepository extends JpaRepository<SettlementPoint, Integer> {

    Optional<SettlementPoint> findByName(String name);

    @Query("SELECT COALESCE(MAX(s.id), 0) FROM SettlementPoint s")
    int findMaxId();
}
