package io.github.future0923.alarm.log.warn.common.dispatcher;

import io.github.future0923.alarm.log.common.context.AlarmInfoContext;
import io.github.future0923.alarm.log.common.thread.AlarmLogThreadFactory;
import io.github.future0923.alarm.log.warn.common.AlarmLogWarnService;
import io.github.future0923.alarm.log.warn.common.factory.AlarmLogWarnServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Dispatches alarm messages outside the caller thread.
 */
public class AlarmLogDispatcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(AlarmLogDispatcher.class);

    private static final int DEFAULT_QUEUE_CAPACITY = 1024;

    private static volatile ExecutorService executorService = createDefaultExecutorService();

    private static volatile boolean customExecutorService;

    private AlarmLogDispatcher() {
    }

    public static void dispatch(AlarmInfoContext context, Throwable throwable) {
        try {
            executorService.execute(() -> {
                for (AlarmLogWarnService alarmLogWarnService : AlarmLogWarnServiceFactory.getServiceList()) {
                    try {
                        alarmLogWarnService.send(context, throwable);
                    } catch (Exception e) {
                        LOGGER.warn("dispatch alarm message error", e);
                    }
                }
            });
        } catch (RuntimeException e) {
            LOGGER.warn("submit alarm message task error", e);
        }
    }

    public static synchronized void setExecutorService(ExecutorService executorService) {
        if (executorService == null) {
            throw new IllegalArgumentException("executorService must not be null");
        }
        shutdownDefaultExecutorService();
        AlarmLogDispatcher.executorService = executorService;
        customExecutorService = true;
    }

    public static synchronized void reset() {
        shutdownDefaultExecutorService();
        executorService = createDefaultExecutorService();
        customExecutorService = false;
    }

    private static ExecutorService createDefaultExecutorService() {
        return new ThreadPoolExecutor(
                1,
                Runtime.getRuntime().availableProcessors(),
                300,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(DEFAULT_QUEUE_CAPACITY),
                AlarmLogThreadFactory.create("alarm-log-dispatcher-", false),
                new ThreadPoolExecutor.DiscardPolicy()
        );
    }

    private static void shutdownDefaultExecutorService() {
        if (!customExecutorService && executorService != null) {
            executorService.shutdownNow();
        }
    }
}
