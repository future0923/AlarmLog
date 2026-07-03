package io.github.future0923.alarm.log.aspect;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author weilai
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Alarm {

    Class<? extends Throwable>[] includeException() default {Throwable.class};

    boolean includeExceptionExtend() default true;

    Class<? extends Throwable>[] excludeException() default {};

    boolean excludeExceptionExtend() default false;
}
