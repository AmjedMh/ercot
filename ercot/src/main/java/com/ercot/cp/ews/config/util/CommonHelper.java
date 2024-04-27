package com.ercot.cp.ews.config.util;

import lombok.extern.log4j.Log4j2;

import java.util.HashMap;
import java.util.concurrent.ThreadPoolExecutor;

@Log4j2
public class CommonHelper {

    private static HashMap<String, ThreadPoolExecutor> poolMap = new HashMap<>();

    public static void setThreadPoolExecutor(String name, ThreadPoolExecutor threadPoolExecutor) {
        poolMap.put(name, threadPoolExecutor);
    }

    public static ThreadPoolExecutor getThreadPoolExecutor(String name) {
        return poolMap.get(name);
    }

    public void waitForExecutorToCompleteTasks(ThreadPoolExecutor executorPool) {
        log.debug("Waiting till executor completes its work");
        while (true) {
            long lCompletedTasksCount = executorPool.getCompletedTaskCount();
            long lTasksCount = executorPool.getTaskCount();
            log.debug("Tasks: {} Completed: {} Remaining: {}", lTasksCount, lCompletedTasksCount, (lTasksCount - lCompletedTasksCount));
            if (lCompletedTasksCount == lTasksCount) {
                log.debug("All executions seems completed");
                break;
            }
            try {
                Thread.sleep(7000);
            } catch (InterruptedException ignored) {
            }
        }
    }
}