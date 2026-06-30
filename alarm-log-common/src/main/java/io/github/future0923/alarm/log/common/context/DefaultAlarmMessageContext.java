package io.github.future0923.alarm.log.common.context;

import io.github.future0923.alarm.log.common.dto.AlarmLogSimpleConfig;
import io.github.future0923.alarm.log.common.dto.AlarmMailContent;
import io.github.future0923.alarm.log.common.utils.ThrowableUtils;

/**
 * @author weilai
 */
public class DefaultAlarmMessageContext implements AlarmMessageContext {

    @Override
    public String workWeixinContent(AlarmInfoContext context, Throwable throwable, AlarmLogSimpleConfig config) {
        return ThrowableUtils.workWeixinContent(context, throwable);
    }

    @Override
    public String dingtalkContent(AlarmInfoContext context, Throwable throwable, AlarmLogSimpleConfig config) {
        return ThrowableUtils.dingtalkContent(context, throwable);
    }

    @Override
    public AlarmMailContent mailContent(AlarmInfoContext context, Throwable throwable, AlarmLogSimpleConfig config) {
        return ThrowableUtils.mailSubjectContent(context, throwable);
    }
}
