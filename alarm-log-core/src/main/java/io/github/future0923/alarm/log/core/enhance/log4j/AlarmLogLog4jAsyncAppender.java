package io.github.future0923.alarm.log.core.enhance.log4j;

import io.github.future0923.alarm.log.common.context.AlarmLogContext;
import io.github.future0923.alarm.log.common.context.AlarmInfoContext;
import io.github.future0923.alarm.log.common.utils.ExceptionUtils;
import io.github.future0923.alarm.log.warn.common.dispatcher.AlarmLogDispatcher;
import org.apache.log4j.AsyncAppender;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * @author weilai
 */
public class AlarmLogLog4jAsyncAppender extends AsyncAppender {

    /**
     * @param doWarnException parsing xml doWarnException param
     */
    public void setDoWarnException(String doWarnException) {
        Optional.ofNullable(doWarnException).ifPresent(className -> AlarmLogContext.addDoWarnExceptionList(Arrays.asList(className.split(","))));
    }

    /**
     * @param warnExceptionExtend parsing xml warnExceptionExtend param
     */
    public void setWarnExceptionExtend(String warnExceptionExtend) {
        Optional.ofNullable(warnExceptionExtend).map(Boolean::new).ifPresent(AlarmLogContext::setWarnExceptionExtend);
    }

    @Override
    public synchronized void doAppend(LoggingEvent event) {
        ThrowableInformation throwableInformation = event.getThrowableInformation();
        if (Objects.nonNull(throwableInformation)) {
            Throwable throwable = throwableInformation.getThrowable();
            if (Objects.nonNull(throwable)
                    && (AlarmLogContext.doWarnException(throwable) || ExceptionUtils.doWarnExceptionInstance(throwable))) {
                StackTraceElement stackTraceElement = getFirstStackTraceElement(throwable);
                AlarmLogDispatcher.dispatch(
                    AlarmInfoContext.builder()
                        .message(event.getRenderedMessage())
                        .level(event.getLevel().toString())
                        .throwableName(throwable.getClass().getName())
                        .threadName(event.getThreadName())
                        .loggerName(event.getLoggerName())
                        .className(stackTraceElement.getClassName())
                        .fileName(stackTraceElement.getFileName())
                        .lineNumber(stackTraceElement.getLineNumber())
                        .methodName(stackTraceElement.getMethodName()).build()
                    , throwable);
            }
        }
        super.doAppend(event);
    }

    private StackTraceElement getFirstStackTraceElement(Throwable throwable) {
        StackTraceElement[] stackTraceElements = throwable.getStackTrace();
        if (stackTraceElements.length > 0) {
            return stackTraceElements[0];
        }
        return new StackTraceElement(throwable.getClass().getName(), "unknown", null, -1);
    }
}
