package com.ercot.cp.ews.config.service.impl;

import com.ercot.cp.ews.config.domin.SevenDayLoadForecastReport;
import com.ercot.cp.ews.config.repository.SevenDayLoadForecastReportDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Component
@RequiredArgsConstructor
public class SevenDayLoadForecastReportSaveHelper {

    private final SevenDayLoadForecastReportDataRepository sevenDayLoadForecastReportDataRepository;

    /**
     * Saves a single entity in its own independent transaction.
     * REQUIRES_NEW ensures a PK violation on one row does not roll back other rows.
     *
     * @return true if saved, false if skipped (duplicate)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean saveOne(SevenDayLoadForecastReport entity) {
        try {
            sevenDayLoadForecastReportDataRepository.save(entity);
            return true;
        } catch (DataIntegrityViolationException e) {
            log.debug("saveOne: duplicate skipped (opdate={}, HE={}) - already exists in DB",
                    entity.getOpDate(), entity.getHe());
            return false;
        }
    }
}
