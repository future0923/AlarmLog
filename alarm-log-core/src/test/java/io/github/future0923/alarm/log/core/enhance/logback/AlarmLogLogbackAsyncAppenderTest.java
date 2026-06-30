package io.github.future0923.alarm.log.core.enhance.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import io.github.future0923.alarm.log.common.context.AlarmLogContext;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * @author weilai
 */
class AlarmLogLogbackAsyncAppenderTest {

    @Test
    void doAppendIgnoresAlarmContextStackFrameWhenThrowableStackTraceIsEmpty() {
        List<String> originalDoWarnExceptionList = new ArrayList<>(AlarmLogContext.getDoWarnExceptionList());
        Boolean originalWarnExceptionExtend = AlarmLogContext.getWarnExceptionExtend();

        try {
            AlarmLogContext.setWarnExceptionExtend(false);
            AlarmLogContext.setDoWarnExceptionList(Collections.singletonList(RuntimeException.class.getName()));

            RuntimeException throwable = new RuntimeException("empty stack trace");
            throwable.setStackTrace(new StackTraceElement[0]);

            LoggingEvent loggingEvent = new LoggingEvent();
            loggingEvent.setMessage("empty stack trace");
            loggingEvent.setLevel(Level.INFO);
            loggingEvent.setLoggerName("test");
            loggingEvent.setThreadName("test-thread");
            loggingEvent.setThrowableProxy(new ThrowableProxy(throwable));

            AlarmLogLogbackAsyncAppender appender = new AlarmLogLogbackAsyncAppender();

            assertDoesNotThrow(() -> appender.doAppend(loggingEvent));
        } finally {
            AlarmLogContext.setDoWarnExceptionList(originalDoWarnExceptionList);
            AlarmLogContext.setWarnExceptionExtend(originalWarnExceptionExtend);
        }
    }
}
