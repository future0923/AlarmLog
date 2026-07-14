package io.github.future0923.alarm.log.aspect;

import io.github.future0923.alarm.log.common.context.AlarmLogContext;
import io.github.future0923.alarm.log.common.context.AlarmInfoContext;
import io.github.future0923.alarm.log.common.context.AlarmContextSnapshot;
import io.github.future0923.alarm.log.common.utils.ExceptionUtils;
import io.github.future0923.alarm.log.warn.common.dispatcher.AlarmLogDispatcher;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author weilai
 */
@Aspect
public class AlarmAspect {

    public static final String POINTCUT_SIGN =
            "@within(io.github.future0923.alarm.log.aspect.Alarm) || @annotation(io.github.future0923.alarm.log.aspect.Alarm)";

    @Pointcut(POINTCUT_SIGN)
    public void pointcut() {

    }

    @AfterThrowing(value = "pointcut()", throwing = "ex")
    public void doRetryProcess(JoinPoint joinPoint, Throwable ex) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Alarm alarmMethod = signature.getMethod().getAnnotation(Alarm.class);
        Alarm alarmClass = signature.getMethod().getDeclaringClass().getAnnotation(Alarm.class);
        if (doWarnProcess(alarmMethod, ex)
                || doWarnProcess(alarmClass, ex)
                || AlarmLogContext.shouldWarnException(ex)) {
            String threadName = Thread.currentThread().getName();
            StackTraceElement stackTraceElement = getFirstStackTraceElement(ex);
            AlarmLogDispatcher.dispatch(
                    AlarmInfoContext.builder()
                            .message(ex.getMessage())
                            .throwableName(ex.getClass().getName())
                            .loggerName(joinPoint.getSignature().getDeclaringTypeName())
                            .threadName(threadName)
                            .fileName(stackTraceElement.getFileName())
                            .lineNumber(stackTraceElement.getLineNumber())
                            .methodName(stackTraceElement.getMethodName())
                            .className(stackTraceElement.getClassName())
                            .contextData(AlarmContextSnapshot.capture(MDC.getCopyOfContextMap()))
                            .build()
                    , ex);
        }
        throw ex;
    }

    private StackTraceElement getFirstStackTraceElement(Throwable throwable) {
        StackTraceElement[] stackTraceElements = throwable.getStackTrace();
        if (stackTraceElements.length > 0) {
            return stackTraceElements[0];
        }
        return new StackTraceElement(throwable.getClass().getName(), "unknown", null, -1);
    }

    private boolean doWarnProcess(Alarm alarm, Throwable ex) {
        if (Objects.isNull(alarm)) {
            return false;
        }
        Class<? extends Throwable>[] excludeExceptionClasses = alarm.excludeException();
        if (matchAnnotationException(ex, excludeExceptionClasses, alarm.excludeExceptionExtend())) {
            return false;
        }
        Class<? extends Throwable>[] includeExceptionClasses = alarm.includeException();
        return matchAnnotationException(ex, includeExceptionClasses, alarm.includeExceptionExtend());
    }

    private boolean matchAnnotationException(Throwable ex, Class<? extends Throwable>[] exceptionClasses, boolean extend) {
        if (extend) {
            return ExceptionUtils.matchExceptionExtend(ex, Arrays.asList(exceptionClasses));
        } else {
            List<String> exceptionList = new ArrayList<>(exceptionClasses.length);
            for (Class<? extends Throwable> exceptionClass : exceptionClasses) {
                exceptionList.add(exceptionClass.getName());
            }
            return ExceptionUtils.matchExceptionName(ex, exceptionList);
        }
    }
}
