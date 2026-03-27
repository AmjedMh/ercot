package com.ercot.cp.ews.config.util;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Monitors database connection health and logs warnings when issues are detected.
 * Helps diagnose application hangs caused by database connection problems.
 */
@Log4j2
@Component
@RequiredArgsConstructor
public class DatabaseHealthMonitor {

    private final DataSource dataSource;
    
    /**
     * Check database health every 5 minutes.
     * Logs connection pool statistics and tests connectivity.
     */
    @Scheduled(fixedRate = 300000)  // Every 5 minutes
    public void checkDatabaseHealth() {
        try {
            logConnectionPoolStats();
            testDatabaseConnectivity();
        } catch (Exception e) {
            log.error("DatabaseHealthMonitor: error during health check: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Log HikariCP connection pool statistics.
     */
    private void logConnectionPoolStats() {
        if (dataSource instanceof HikariDataSource) {
            HikariDataSource hikariDS = (HikariDataSource) dataSource;
            HikariPoolMXBean poolMXBean = hikariDS.getHikariPoolMXBean();
            
            if (poolMXBean != null) {
                int active = poolMXBean.getActiveConnections();
                int idle = poolMXBean.getIdleConnections();
                int total = poolMXBean.getTotalConnections();
                int waiting = poolMXBean.getThreadsAwaitingConnection();
                
                log.info("=== DB Connection Pool Health ===");
                log.info("  Active: {} | Idle: {} | Total: {} | Waiting: {}", 
                        active, idle, total, waiting);
                
                // Warn if pool is exhausted
                if (waiting > 0) {
                    log.warn("WARNING: {} threads waiting for DB connection! Pool may be exhausted.", waiting);
                    log.warn("  Active connections: {} / {} max", active, hikariDS.getMaximumPoolSize());
                }
                
                // Warn if pool utilization is very high
                if (active >= hikariDS.getMaximumPoolSize() * 0.9) {
                    log.warn("WARNING: Connection pool at {}% capacity ({}/{})", 
                            (active * 100 / hikariDS.getMaximumPoolSize()), 
                            active, hikariDS.getMaximumPoolSize());
                }
                
                log.info("=================================");
            }
        }
    }
    
    /**
     * Test actual database connectivity with a simple query.
     */
    private void testDatabaseConnectivity() {
        long startTime = System.currentTimeMillis();
        try (Connection conn = dataSource.getConnection()) {
            if (conn.isValid(5)) {  // 5 second timeout
                long duration = System.currentTimeMillis() - startTime;
                if (duration > 2000) {
                    log.warn("Database connectivity test SLOW: took {}ms (threshold: 2000ms)", duration);
                } else {
                    log.debug("Database connectivity: OK ({}ms)", duration);
                }
            } else {
                log.error("Database connectivity test FAILED: connection not valid");
            }
        } catch (SQLException e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("Database connectivity test FAILED after {}ms: {}", duration, e.getMessage(), e);
        }
    }
    
    /**
     * Manual health check that can be called on-demand.
     * @return true if database is healthy, false otherwise
     */
    public boolean isDatabaseHealthy() {
        try {
            testDatabaseConnectivity();
            
            if (dataSource instanceof HikariDataSource) {
                HikariDataSource hikariDS = (HikariDataSource) dataSource;
                HikariPoolMXBean poolMXBean = hikariDS.getHikariPoolMXBean();
                
                if (poolMXBean != null) {
                    // Unhealthy if threads are waiting for connections
                    if (poolMXBean.getThreadsAwaitingConnection() > 0) {
                        return false;
                    }
                }
            }
            
            return true;
        } catch (Exception e) {
            log.error("Database health check failed: {}", e.getMessage());
            return false;
        }
    }
}
