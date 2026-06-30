package io.github.future0923.alarm.log.core.enhance.logback;

import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import io.github.future0923.alarm.log.common.context.AlarmLogContext;
import io.github.future0923.alarm.log.common.context.AlarmInfoContext;
import io.github.future0923.alarm.log.common.utils.ExceptionUtils;
import io.github.future0923.alarm.log.warn.common.dispatcher.AlarmLogDispatcher;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * @author weilai
 */
public class AlarmLogLogbackAsyncAppender extends AsyncAppender {

    /**
     * @param doWarnException parsing xml doWarnException param
     */
    public void setDoWarnException(String doWarnException) {
        Optional.ofNullable(doWarnException).ifPresent(className -> AlarmLogContext.addDoWarnExceptionList(Arrays.asList(className.split(","))));
    }

    /**
     * @param warnExceptionExtend parsing xml warnExceptionExtend param
     */
    public void setWarnExceptionExtend(Boolean warnExceptionExtend) {
        Optional.ofNullable(warnExceptionExtend).ifPresent(AlarmLogContext::setWarnExceptionExtend);
    }

    @Override
    public void doAppend(ILoggingEvent eventObject) {
        if(eventObject instanceof LoggingEvent){
            LoggingEvent loggingEvent = (LoggingEvent)eventObject;
            ThrowableProxy throwableProxy = (ThrowableProxy) loggingEvent.getThrowableProxy();
            if (Objects.nonNull(throwableProxy)) {
                Throwable throwable = throwableProxy.getThrowable();
                if (AlarmLogContext.doWarnException(throwable)
                        || ExceptionUtils.doWarnExceptionInstance(throwable)) {
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
