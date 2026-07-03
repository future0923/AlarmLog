package io.github.future0923.alarm.log.core.enhance.logback;

import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import io.github.future0923.alarm.log.common.context.AlarmLogContext;
import io.github.future0923.alarm.log.common.context.AlarmInfoContext;
import io.github.future0923.alarm.log.warn.common.dispatcher.AlarmLogDispatcher;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * @author weilai
 */
public class AlarmLogLogbackAsyncAppender extends AsyncAppender {

    /**
     * @param includeException parsing xml includeException param
     */
    public void setIncludeException(String includeException) {
        Optional.ofNullable(includeException).ifPresent(className -> AlarmLogContext.addIncludeExceptionList(Arrays.asList(className.split(","))));
    }

    /**
     * @param includeExceptionExtend parsing xml includeExceptionExtend param
     */
    public void setIncludeExceptionExtend(Boolean includeExceptionExtend) {
        Optional.ofNullable(includeExceptionExtend).ifPresent(AlarmLogContext::setIncludeExceptionExtend);
    }

    /**
     * @param excludeException parsing xml excludeException param
     */
    public void setExcludeException(String excludeException) {
        Optional.ofNullable(excludeException).ifPresent(className -> AlarmLogContext.addExcludeExceptionList(Arrays.asList(className.split(","))));
    }

    /**
     * @param excludeExceptionExtend parsing xml excludeExceptionExtend param
     */
    public void setExcludeExceptionExtend(Boolean excludeExceptionExtend) {
        Optional.ofNullable(excludeExceptionExtend).ifPresent(AlarmLogContext::setExcludeExceptionExtend);
    }

    @Override
    public void doAppend(ILoggingEvent eventObject) {
        if(eventObject instanceof LoggingEvent){
            LoggingEvent loggingEvent = (LoggingEvent)eventObject;
            ThrowableProxy throwableProxy = (ThrowableProxy) loggingEvent.getThrowableProxy();
            if (Objects.nonNull(throwableProxy)) {
                Throwable throwable = throwableProxy.getThrowable();
                if (AlarmLogContext.shouldWarnException(throwable)) {
                    StackTraceElement stackTraceElement = getFirstStackTraceElement(throwable);
                    AlarmLogDispatcher.dispatch(
                            AlarmInfoContext.builder()
                                    .message(loggingEvent.getFormattedMessage())
                                    .level(loggingEvent.getLevel().toString())
                                    .throwableName(throwable.getClass().getName())
                                    .threadName(loggingEvent.getThreadName())
                                    .loggerName(loggingEvent.getLoggerName())
                                    .className(stackTraceElement.getClassName())
                                    .fileName(stackTraceElement.getFileName())
                                    .methodName(stackTraceElement.getMethodName())
                                    .lineNumber(stackTraceElement.getLineNumber()).build()
                            , throwable);
                }
            }
        }
        super.doAppend(eventObject);
    }

    private StackTraceElement getFirstStackTraceElement(Throwable throwable) {
        StackTraceElement[] stackTraceElements = throwable.getStackTrace();
        if (stackTraceElements.length > 0) {
            return stackTraceElements[0];
        }
        return new StackTraceElement(throwable.getClass().getName(), "unknown", null, -1);
    }
}
