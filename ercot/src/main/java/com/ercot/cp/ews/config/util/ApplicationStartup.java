package com.ercot.cp.ews.config.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.ercot.cp.ews.config.constants.ConstantCodes.DOWNLOAD_REPORT;

@Log4j2
@Component
@RequiredArgsConstructor
public class ApplicationStartup implements ApplicationListener<ApplicationReadyEvent> {

    private static final int CORE_POOL_SIZE = 31;
    private static final int MAX_POOL_SIZE = 31;
    private static final long UNIT_NUMBER = 5;

    @Override
    public void onApplicationEvent(final ApplicationReadyEvent event) {

        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        RejectedExecutionHandler threadRejected = (r, executor) -> log.debug("Thread rejected");

        ThreadPoolExecutor downloadAmazonProducts = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE, UNIT_NUMBER, TimeUnit.DAYS, new ArrayBlockingQueue<>(10000), threadFactory, threadRejected);
        CommonHelper.setThreadPoolExecutor(DOWNLOAD_REPORT, downloadAmazonProducts);
    }
}