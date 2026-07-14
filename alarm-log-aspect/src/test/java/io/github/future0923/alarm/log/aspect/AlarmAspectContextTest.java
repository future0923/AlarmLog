package io.github.future0923.alarm.log.aspect;

import io.github.future0923.alarm.log.common.context.AlarmInfoContext;
import io.github.future0923.alarm.log.common.context.AlarmLogContext;
import io.github.future0923.alarm.log.warn.common.AlarmLogWarnService;
import io.github.future0923.alarm.log.warn.common.dispatcher.AlarmLogDispatcher;
import io.github.future0923.alarm.log.warn.common.factory.AlarmLogWarnServiceFactory;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AlarmAspectContextTest {

    private Set<String> originalIncludeContextKeys;
    private List<AlarmLogWarnService> originalWarnServices;
    private Map<String, String> originalMdc;
    private ExecutorService executorService;
    private CountDownLatch releaseWorker;

    @BeforeEach
    void setUp() throws Exception {
        originalIncludeContextKeys = AlarmLogContext.getIncludeContextKeys();
        originalWarnServices = new ArrayList<>(AlarmLogWarnServiceFactory.getServiceList());
        originalMdc = MDC.getCopyOfContextMap();

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
        try {
            if (releaseWorker != null) {
                releaseWorker.countDown();
            }
            if (executorService != null) {
                executorService.shutdownNow();
                executorService.awaitTermination(3, TimeUnit.SECONDS);
            }
        } finally {
            AlarmLogDispatcher.reset();
            AlarmLogWarnServiceFactory.setAlarmLogWarnServices(originalWarnServices);
            AlarmLogContext.setIncludeContextKeys(originalIncludeContextKeys);
            MDC.clear();
            if (originalMdc != null) {
                MDC.setContextMap(originalMdc);
            }
        }
    }

    @Test
    void doRetryProcessSnapshotsWhitelistedMdcBeforeAsyncDispatch() throws Exception {
        CountDownLatch capturedLatch = new CountDownLatch(1);
        AtomicReference<AlarmInfoContext> captured = new AtomicReference<>();
        AlarmLogWarnServiceFactory.setAlarmLogWarnServices(Collections.singletonList((context, throwable) -> {
            captured.set(context);
            capturedLatch.countDown();
            return true;
        }));
        AlarmLogContext.setIncludeContextKeys("traceId");
        MDC.put("traceId", "trace-aspect");
        MDC.put("password", "secret");

        RuntimeException throwable = new RuntimeException("boom");
        AlarmAspect aspect = new AlarmAspect();

        assertSame(throwable, assertThrows(RuntimeException.class,
                () -> aspect.doRetryProcess(joinPoint(), throwable)));
        MDC.clear();
        releaseWorker.countDown();

        assertTrue(capturedLatch.await(3, TimeUnit.SECONDS));
        assertEquals("trace-aspect", captured.get().getContextData().get("traceId"));
        assertFalse(captured.get().getContextData().containsKey("password"));
    }

    @Test
    void doRetryProcessPreservesThrowableWhenStackTraceIsEmpty() throws Exception {
        RuntimeException throwable = new RuntimeException("empty stack trace");
        throwable.setStackTrace(new StackTraceElement[0]);

        assertSame(throwable, assertThrows(RuntimeException.class,
                () -> new AlarmAspect().doRetryProcess(joinPoint(), throwable)));
    }

    private JoinPoint joinPoint() throws Exception {
        Method method = AlarmTarget.class.getDeclaredMethod("fail");
        MethodSignature signature = mock(MethodSignature.class);
        when(signature.getMethod()).thenReturn(method);
        when(signature.getDeclaringTypeName()).thenReturn(AlarmTarget.class.getName());
        JoinPoint joinPoint = mock(JoinPoint.class);
        when(joinPoint.getSignature()).thenReturn(signature);
        return joinPoint;
    }

    private static class AlarmTarget {

        @Alarm
        private void fail() {
        }
    }
}
