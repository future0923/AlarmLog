package io.github.future0923.alarm.log.warn.common.dispatcher;

import io.github.future0923.alarm.log.common.context.AlarmInfoContext;
import io.github.future0923.alarm.log.warn.common.AlarmLogWarnService;
import io.github.future0923.alarm.log.warn.common.factory.AlarmLogWarnServiceFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AlarmLogDispatcherTest {

    @AfterEach
    void tearDown() {
        AlarmLogDispatcher.reset();
        AlarmLogWarnServiceFactory.clear();
    }

    @Test
    void dispatchUsesProvidedExecutorService() throws Exception {
        ExecutorService executorService = new ThreadPoolExecutor(
                1,
                1,
                0L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(),
                runnable -> new Thread(runnable, "custom-alarm-executor")
        );
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> threadName = new AtomicReference<>();
        AlarmLogWarnServiceFactory.setAlarmLogWarnServices(Collections.singletonList((context, throwable) -> {
            threadName.set(Thread.currentThread().getName());
            latch.countDown();
            return true;
        }));

        AlarmLogDispatcher.setExecutorService(executorService);
        AlarmLogDispatcher.dispatch(AlarmInfoContext.builder().message("boom").build(), new RuntimeException("boom"));

        assertTrue(latch.await(3, TimeUnit.SECONDS));
        assertEquals("custom-alarm-executor", threadName.get());
        executorService.shutdownNow();
    }

    @Test
    void dispatchDoesNotRunWarnServiceOnCallerThread() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> threadName = new AtomicReference<>();
        AlarmLogWarnServiceFactory.setAlarmLogWarnServices(Collections.singletonList((context, throwable) -> {
            threadName.set(Thread.currentThread().getName());
            latch.countDown();
            return true;
        }));

        String callerThreadName = Thread.currentThread().getName();
        AlarmLogDispatcher.dispatch(AlarmInfoContext.builder().message("boom").build(), new RuntimeException("boom"));

        assertTrue(latch.await(3, TimeUnit.SECONDS));
        assertTrue(!callerThreadName.equals(threadName.get()));
    }
}
