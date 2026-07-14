package io.github.future0923.alarm.log.core.enhance.log4j2;

import io.github.future0923.alarm.log.common.context.AlarmInfoContext;
import io.github.future0923.alarm.log.common.context.AlarmLogContext;
import io.github.future0923.alarm.log.warn.common.AlarmLogWarnService;
import io.github.future0923.alarm.log.warn.common.dispatcher.AlarmLogDispatcher;
import io.github.future0923.alarm.log.warn.common.factory.AlarmLogWarnServiceFactory;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.message.SimpleMessage;
import org.apache.logging.log4j.util.SortedArrayStringMap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AlarmLog4j2AsyncAppenderTest {

    private List<String> originalIncludeExceptionList;
    private Boolean originalIncludeExceptionExtend;
    private Set<String> originalIncludeContextKeys;
    private List<AlarmLogWarnService> originalWarnServices;
    private ExecutorService executorService;
    private CountDownLatch releaseWorker;

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
        executorService.shutdownNow();
        executorService.awaitTermination(3, TimeUnit.SECONDS);
        AlarmLogDispatcher.reset();
        AlarmLogWarnServiceFactory.setAlarmLogWarnServices(originalWarnServices);
        AlarmLogContext.setIncludeExceptionList(originalIncludeExceptionList);
        AlarmLogContext.setIncludeExceptionExtend(originalIncludeExceptionExtend);
        AlarmLogContext.setIncludeContextKeys(originalIncludeContextKeys);
    }

    @Test
    void createAppenderRetainsLegacyEightArgumentFactorySignature() throws Exception {
        AlarmLog4j2AsyncAppender appender = AlarmLog4j2AsyncAppender.createAppender(
                "AlarmLog", null, null, true, null, null, null, null);

        Method factory = AlarmLog4j2AsyncAppender.class.getDeclaredMethod(
                "createAppender", String.class, org.apache.logging.log4j.core.Filter.class,
                org.apache.logging.log4j.core.Layout.class, boolean.class, String.class, Boolean.class,
                String.class, Boolean.class);

        assertNotNull(appender);
        assertTrue(Modifier.isPublic(factory.getModifiers()));
        assertTrue(Modifier.isStatic(factory.getModifiers()));
        assertFalse(factory.isAnnotationPresent(PluginFactory.class));
    }

    @Test
    void appendSnapshotsWhitelistedContextDataBeforeAsyncDispatch() throws Exception {
        CountDownLatch capturedLatch = new CountDownLatch(1);
        AtomicReference<AlarmInfoContext> captured = new AtomicReference<>();
        AlarmLogWarnServiceFactory.setAlarmLogWarnServices(Collections.singletonList((context, throwable) -> {
            captured.set(context);
            capturedLatch.countDown();
            return true;
        }));

        AlarmLog4j2AsyncAppender appender = AlarmLog4j2AsyncAppender.createAppender(
                "AlarmLog", null, null, true, null, null, null, null, "traceId,spanId");
        SortedArrayStringMap contextData = new SortedArrayStringMap();
        contextData.putValue("traceId", "trace-log4j2");
        contextData.putValue("spanId", "span-log4j2");
        contextData.putValue("Authorization", "secret");
        LogEvent event = Log4jLogEvent.newBuilder()
                .setLoggerName("test")
                .setLevel(Level.ERROR)
                .setMessage(new SimpleMessage("boom"))
                .setThrown(new RuntimeException("boom"))
                .setContextData(contextData)
                .setThreadName("test-thread")
                .build();

        appender.append(event);
        contextData.clear();
        releaseWorker.countDown();

        assertTrue(capturedLatch.await(3, TimeUnit.SECONDS));
        assertEquals("trace-log4j2", captured.get().getContextData().get("traceId"));
        assertEquals("span-log4j2", captured.get().getContextData().get("spanId"));
        assertFalse(captured.get().getContextData().containsKey("Authorization"));
    }
}
