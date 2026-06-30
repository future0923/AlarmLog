package io.github.future0923.alarm.log.warn.common;

import io.github.future0923.alarm.log.common.context.AlarmInfoContext;

/**
 * @author weilai
 */
public interface AlarmLogWarnService {

    boolean send(AlarmInfoContext context, Throwable throwable);
}
