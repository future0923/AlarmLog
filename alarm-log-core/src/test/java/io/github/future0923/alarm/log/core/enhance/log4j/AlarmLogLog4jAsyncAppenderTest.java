package io.github.future0923.alarm.log.core.enhance.log4j;

import io.github.future0923.alarm.log.common.context.AlarmInfoContext;
import io.github.future0923.alarm.log.common.context.AlarmLogContext;
import io.github.future0923.alarm.log.warn.common.AlarmLogWarnService;
import io.github.future0923.alarm.log.warn.common.dispatcher.AlarmLogDispatcher;
import io.github.future0923.alarm.log.warn.common.factory.AlarmLogWarnServiceFactory;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AlarmLogLog4jAsyncAppenderTest {

    private List<String> originalIncludeExceptionList;
    private Boolean originalIncludeExceptionExtend;
    private Set<String> originalIncludeContextKeys;
    private List<AlarmLogWarnService> originalWarnServices;
    private ExecutorService executorService;
    private CountDownLatch releaseWorker;
    private AlarmLogLog4jAsyncAppender appender;

    @BeforeEach
    void setUp() throws Exception {
        originalIncludeExceptionList = new ArrayList<>(AlarmLogContext.getIncludeExceptionList());
        originalIncludeExceptionExtend = AlarmLogContext.getIncludeExceptionExtend();
        originalIncludeContextKeys = AlarmLogContext.getIncludeContextKeys();
        originalWarnServices = new ArrayList<>(AlarmLogWarnServiceFactory.getServiceList());
        AlarmLogContext.setIncludeExceptionExtend(false);
        AlarmLogContext.setIncludeExceptionList(Collections.singletonList(RuntimeException.class.getName()));

        CountDownLatch workerStarted = new CountDownLatch(1);
        releaseWorker = new CountDownLatch(1);
        executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            workerStarted.countDown();
            try {
                releaseWorker.await(3, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        assertTrue(workerStarted.await(3, TimeUnit.SECONDS));
        AlarmLogDispatcher.setExecutorService(executorService);
    }

    @AfterEach
    void tearDown() throws Exception {
        releaseWorker.countDown();
        if (appender != null) {
            appender.close();
        }
        executorService.shutdownNow();
        executorService.awaitTermination(3, TimeUnit.SECONDS);
        AlarmLogDispatcher.reset();
        AlarmLogWarnServiceFactory.setAlarmLogWarnServices(originalWarnServices);
        AlarmLogContext.setIncludeExceptionList(originalIncludeExceptionList);
        AlarmLogContext.setIncludeExceptionExtend(originalIncludeExceptionExtend);
        AlarmLogContext.setIncludeContextKeys(originalIncludeContextKeys);
    }

    @Test
    void doAppendSnapshotsWhitelistedPropertiesBeforeAsyncDispatch() throws Exception {
        CountDownLatch capturedLatch = new CountDownLatch(1);
        AtomicReference<AlarmInfoContext> captured = new AtomicReference<>();
        AlarmLogWarnServiceFactory.setAlarmLogWarnServices(Collections.singletonList((context, throwable) -> {
            captured.set(context);
            capturedLatch.countDown();
            return true;
        }));

        appender = new AlarmLogLog4jAsyncAppender();
        appender.setIncludeContextKeys("traceId,spanId");
        LoggingEvent event = new LoggingEvent(
                getClass().getName(), Logger.getLogger("test"), Level.ERROR, "boom", new RuntimeException("boom"));
        event.setProperty("traceId", "trace-log4j");
        event.setProperty("spanId", "span-log4j");
        event.setProperty("Authorization", "secret");
        appender.doAppend(event);
        event.setProperty("traceId", "changed");
        event.removeProperty("spanId");
        event.removeProperty("Authorization");
        releaseWorker.countDown();

        assertTrue(capturedLatch.await(3, TimeUnit.SECONDS));
        assertEquals("trace-log4j", captured.get().getContextData().get("traceId"));
        assertEquals("span-log4j", captured.get().getContextData().get("spanId"));
        assertFalse(captured.get().getContextData().containsKey("Authorization"));
    }

    @Test
    void doAppendSkipsNonStringPropertyWhoseConversionFailsAndStillDispatches() throws Exception {
        CountDownLatch capturedLatch = new CountDownLatch(1);
        AtomicReference<AlarmInfoContext> captured = new AtomicReference<>();
        AlarmLogWarnServiceFactory.setAlarmLogWarnServices(Collections.singletonList((context, throwable) -> {
            captured.set(context);
            capturedLatch.countDown();
            return true;
        }));
        AlarmLogContext.setIncludeContextKeys(Arrays.asList("invalid", "traceId"));

        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("invalid", new Object() {
            @Override
            public String toString() {
                throw new RuntimeException("cannot stringify");
            }
        });
        properties.put("traceId", "trace-log4j");
        RuntimeException throwable = new RuntimeException("boom");
        LoggingEvent event = new LoggingEvent(
                getClass().getName(), Logger.getLogger("test"), System.currentTimeMillis(), Level.ERROR,
                "boom", Thread.currentThread().getName(), new ThrowableInformation(throwable),
                null, null, properties);
        appender = new AlarmLogLog4jAsyncAppender();

        assertDoesNotThrow(() -> appender.doAppend(event));
        releaseWorker.countDown();

        assertTrue(capturedLatch.await(3, TimeUnit.SECONDS));
        assertEquals("trace-log4j", captured.get().getContextData().get("traceId"));
        assertFalse(captured.get().getContextData().containsKey("invalid"));
    }
}
