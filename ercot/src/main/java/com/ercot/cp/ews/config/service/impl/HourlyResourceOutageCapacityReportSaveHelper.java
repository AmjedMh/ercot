package com.ercot.cp.ews.config.service.impl;

import com.ercot.cp.ews.config.domin.HourlyResourceOutageCapacityReport;
import com.ercot.cp.ews.config.repository.HourlyResourceOutageCapacityReportDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Component
@RequiredArgsConstructor
public class HourlyResourceOutageCapacityReportSaveHelper {

    private final HourlyResourceOutageCapacityReportDataRepository hourlyResourceOutageCapacityReportDataRepository;

    /**
     * Saves a single entity in its own independent transaction.
     * REQUIRES_NEW ensures a PK violation on one row does not roll back other rows.
     *
     * @return true if saved, false if skipped (duplicate)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean saveOne(HourlyResourceOutageCapacityReport entity) {
        try {
            hourlyResourceOutageCapacityReportDataRepository.save(entity);
            return true;
        } catch (DataIntegrityViolationException e) {
            log.debug("saveOne: duplicate skipped (opdate={}, HE={}) - already exists in DB",
                    entity.getOpDate(), entity.getHe());
            return false;
        }
    }
}
