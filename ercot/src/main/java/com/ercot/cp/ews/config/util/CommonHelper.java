package com.ercot.cp.ews.config.util;

import lombok.extern.log4j.Log4j2;

import java.util.HashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Log4j2
public class CommonHelper {

    private static HashMap<String, ThreadPoolExecutor> poolMap = new HashMap<>();
    
    // Timeout configuration: max 30 minutes for thread pool to complete
    private static final long THREAD_POOL_TIMEOUT_MINUTES = 30;
    private static final long CHECK_INTERVAL_MS = 7000;  // Check every 7 seconds

    public static void setThreadPoolExecutor(String name, ThreadPoolExecutor threadPoolExecutor) {
        poolMap.put(name, threadPoolExecutor);
    }

    public static ThreadPoolExecutor getThreadPoolExecutor(String name) {
        return poolMap.get(name);
    }

    /**
     * Wait for all tasks in the thread pool to complete, with timeout protection.
     * Prevents infinite hanging if tasks get stuck or deadlocked.
     * 
     * @param executorPool The executor pool to monitor
     * @throws RuntimeException if timeout is reached before tasks complete
     */
    public void waitForExecutorToCompleteTasks(ThreadPoolExecutor executorPool) {
        log.info("waitForExecutorToCompleteTasks: starting monitoring (timeout: {} minutes)", 
                THREAD_POOL_TIMEOUT_MINUTES);
        
        long startTime = System.currentTimeMillis();
        long timeoutMs = THREAD_POOL_TIMEOUT_MINUTES * 60 * 1000;
        long lastProgressTime = startTime;
        long lastCompletedCount = 0;
        int noProgressCycles = 0;
        
        while (true) {
            long currentTime = System.currentTimeMillis();
            long elapsedMs = currentTime - startTime;
            long elapsedMinutes = elapsedMs / 60000;
            
            long completedTasksCount = executorPool.getCompletedTaskCount();
            long totalTasksCount = executorPool.getTaskCount();
            long remaining = totalTasksCount - completedTasksCount;
            int activeThreads = executorPool.getActiveCount();
            
            log.info("ThreadPool Status: total={} completed={} remaining={} active={} elapsed={}min", 
                    totalTasksCount, completedTasksCount, remaining, activeThreads, elapsedMinutes);
            
            // Check if all tasks completed
            if (completedTasksCount == totalTasksCount) {
                log.info("waitForExecutorToCompleteTasks: ALL TASKS COMPLETED - total={} duration={}min", 
                        totalTasksCount, elapsedMinutes);
                break;
            }
            
            // Check for progress (tasks completing)
            if (completedTasksCount > lastCompletedCount) {
                lastProgressTime = currentTime;
                lastCompletedCount = completedTasksCount;
                noProgressCycles = 0;
            } else {
                noProgressCycles++;
            }
            
            // Check for timeout
            if (elapsedMs > timeoutMs) {
                log.error("waitForExecutorToCompleteTasks: TIMEOUT after {} minutes! {} of {} tasks incomplete", 
                         THREAD_POOL_TIMEOUT_MINUTES, remaining, totalTasksCount);
                log.error("Active threads: {} - Tasks may be stuck or deadlocked", activeThreads);
                
                // Force shutdown to prevent infinite hang
                log.error("Forcing executor shutdown...");
                executorPool.shutdownNow();
                
                throw new RuntimeException(String.format(
                    "Thread pool execution timeout after %d minutes (%d/%d tasks incomplete, %d active threads)",
                    THREAD_POOL_TIMEOUT_MINUTES, remaining, totalTasksCount, activeThreads));
            }
            
            // Warn if no progress for extended period (10 minutes)
            long noProgressMs = currentTime - lastProgressTime;
            if (noProgressMs > 600_000 && noProgressCycles > 5) {  // 10+ minutes without progress
                log.warn("waitForExecutorToCompleteTasks: NO PROGRESS for {} minutes! " +
                        "Completed: {}/{} | Active: {} | May be stuck on DB operations",
                        noProgressMs / 60000, completedTasksCount, totalTasksCount, activeThreads);
            }
            
            try {
                Thread.sleep(CHECK_INTERVAL_MS);
            } catch (InterruptedException e) {
                log.warn("waitForExecutorToCompleteTasks: interrupted while waiting");
                Thread.currentThread().interrupt();
                throw new RuntimeException("Thread pool wait interrupted", e);
            }
        }
    }
}