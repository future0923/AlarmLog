package io.github.future0923.alarm.log.examples.spring.boot.logback.bean;

import io.github.future0923.alarm.log.common.context.AlarmInfoContext;
import io.github.future0923.alarm.log.common.context.AlarmMessageContext;
import io.github.future0923.alarm.log.common.dto.AlarmLogSimpleConfig;
import io.github.future0923.alarm.log.common.dto.AlarmMailContent;
import org.springframework.stereotype.Component;

/**
 * @author weilai
 */
//@Component
public class CustomAlarmMessageContext1 implements AlarmMessageContext {

    @Override
    public String workWeixinContent(AlarmInfoContext context, Throwable throwable, AlarmLogSimpleConfig config) {
        return context.getMessage();
    }

    @Override
    public String dingtalkContent(AlarmInfoContext context, Throwable throwable, AlarmLogSimpleConfig config) {
        return context.getMessage();
    }

    @Override
    public AlarmMailContent mailContent(AlarmInfoContext context, Throwable throwable, AlarmLogSimpleConfig config) {
        return new AlarmMailContent(context.getMessage(), context.getClassName());
    }
}
