package io.github.future0923.alarm.log.common.utils;

import io.github.future0923.alarm.log.common.exception.AlarmLogDoWarnException;
import io.github.future0923.alarm.log.common.exception.AlarmLogException;
import io.github.future0923.alarm.log.common.exception.AlarmLogRuntimeException;

import java.util.List;

/**
 * @author weilai
 */
public class ExceptionUtils {

    public static boolean matchMarkerException(Throwable throwable) {
        return throwable instanceof AlarmLogDoWarnException
                || throwable instanceof AlarmLogException
                || throwable instanceof AlarmLogRuntimeException;
    }

    public static boolean matchExceptionName(Throwable exception, List<String> exceptionList) {
        return exceptionList.contains(exception.getClass().getName());
    }

    public static boolean matchExceptionExtend(Throwable exception, List<Class<? extends Throwable>> exceptionList) {
        for (Class<?> aClass : exceptionList) {
            if (aClass.isAssignableFrom(exception.getClass())) {
                return true;
            }
        }
        return false;
    }
}
