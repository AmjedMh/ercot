package com.ercot.cp.ews.config.service.impl;

import com.ercot.cp.ews.config.constants.ConstantCodes;
import com.ercot.cp.ews.config.domin.SettlementPoint;
import com.ercot.cp.ews.config.repository.SettlementPointRepository;
import com.ercot.cp.ews.config.service.SettlementPointLookupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class SettlementPointLookupServiceImpl implements SettlementPointLookupService {

    private final SettlementPointRepository settlementPointRepository;

    /**
     * In-memory cache: settlement point name → integer surrogate ID.
     * Populated at startup and kept in sync with every new auto-registration.
     * ConcurrentHashMap ensures lock-free reads from the 31 worker threads.
     */
    private final ConcurrentHashMap<String, Integer> cache = new ConcurrentHashMap<>();

    /**
     * Monotonically-increasing ID counter.  Initialized to max(id)+1 from DB
     * so new nodes always receive an ID higher than any existing one.
     */
    private final AtomicInteger nextId = new AtomicInteger(1);

    /**
     * Coarse-grained lock used only when a name is absent from the cache,
     * which is the rare case (brand-new settlement point).  Hot-path lookups
     * never reach this lock.
     */
    private final Object writeLock = new Object();

    // -----------------------------------------------------------------------
    // Startup
    // -----------------------------------------------------------------------

    @PostConstruct
    public void init() {
        // If the SPPNodes table has never been populated, seed it from the
        // hardcoded sPPNodesMap so existing data in RTMSPPs_Interval stays valid.
        if (settlementPointRepository.count() == 0) {
            List<SettlementPoint> seeds = ConstantCodes.sPPNodesMap.entrySet().stream()
                    .map(entry -> {
                        SettlementPoint sp = new SettlementPoint();
                        sp.setId(entry.getValue());
                        sp.setName(entry.getKey());
                        return sp;
                    })
                    .collect(Collectors.toList());
            settlementPointRepository.saveAll(seeds);
            log.info("SPPNodes table seeded with {} entries from sPPNodesMap", seeds.size());
        }

        // Load everything into the in-memory cache.
        settlementPointRepository.findAll()
                .forEach(sp -> cache.put(sp.getName(), sp.getId()));

        // nextId = max existing id + 1, so auto-registered nodes never clash.
        int maxId = settlementPointRepository.findMaxId();
        nextId.set(maxId + 1);

        log.info("SettlementPointLookupService ready: {} entries cached, nextId={}", cache.size(), nextId.get());
    }

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    /**
     * Hot path: O(1) ConcurrentHashMap lookup for already-known nodes.
     * Slow path (new node): enters a synchronized block, persists to DB,
     * then adds to cache.  Handles concurrent inserts via a retry on unique
     * constraint violation.
     */
    @Override
    public Integer getOrCreate(String name) {
        // ---- Hot path (>99 % of calls) ------------------------------------
        Integer id = cache.get(name);
        if (id != null) {
            return id;
        }

        // ---- Slow path: first time we see this settlement point -----------
        synchronized (writeLock) {
            // Double-checked locking: another thread may have inserted while
            // we were waiting for the lock.
            id = cache.get(name);
            if (id != null) {
                return id;
            }

            // Check DB in case the app was restarted and the node was
            // added in a previous run but not yet in this JVM's cache.
            id = settlementPointRepository.findByName(name)
                    .map(SettlementPoint::getId)
                    .orElse(null);

            if (id != null) {
                cache.put(name, id);
                return id;
            }

            // Genuinely new node — persist and cache.
            id = persistNew(name);
            cache.put(name, id);
            return id;
        }
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    /**
     * Persists a brand-new SettlementPoint in its own transaction so the row
     * is visible to other threads immediately after commit.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Integer persistNew(String name) {
        int newId = nextId.getAndIncrement();
        SettlementPoint sp = new SettlementPoint();
        sp.setId(newId);
        sp.setName(name);
        try {
            settlementPointRepository.save(sp);
            log.info("Auto-registered new settlement point: '{}' → id={}", name, newId);
            return newId;
        } catch (DataIntegrityViolationException e) {
            // A parallel JVM instance (unlikely but possible) inserted first.
            // Roll back and fall back to the DB lookup.
            log.warn("Race condition on settlement point '{}': rolling back and re-reading", name);
            return settlementPointRepository.findByName(name)
                    .map(SettlementPoint::getId)
                    .orElseThrow(() -> new IllegalStateException(
                            "Cannot find or create settlement point: " + name, e));
        }
    }
}
