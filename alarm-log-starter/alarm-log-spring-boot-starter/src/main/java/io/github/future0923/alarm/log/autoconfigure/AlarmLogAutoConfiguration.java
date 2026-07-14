package io.github.future0923.alarm.log.autoconfigure;

import io.github.future0923.alarm.log.common.context.AlarmLogContext;
import io.github.future0923.alarm.log.common.context.AlarmMessageContext;
import io.github.future0923.alarm.log.warn.common.factory.AlarmLogWarnServiceFactory;
import io.github.future0923.alarm.log.warn.dingtalk.DingtalkWarnService;
import io.github.future0923.alarm.log.warn.mail.MailWarnService;
import io.github.future0923.alarm.log.warn.workweixin.WorkWeixinWarnService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

/**
 * @author weilai
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(AlarmLogConfig.class)
public class AlarmLogAutoConfiguration {

    @Configuration
    @ConditionalOnProperty(name = "spring.alarm-log.warn.mail.enabled", havingValue = "true")
    @EnableConfigurationProperties(MailConfig.class)
    static class MailWarnServiceMethod {

        @Bean
        @ConditionalOnMissingBean(MailWarnService.class)
        public MailWarnService mailWarnService(final MailConfig mailConfig) {
            MailWarnService mailWarnService = new MailWarnService(mailConfig.getSmtpHost(), mailConfig.getSmtpPort(), mailConfig.getTo(), mailConfig.getFrom(), mailConfig.getUsername(), mailConfig.getPassword());
            mailWarnService.setSsl(mailConfig.getSsl());
            mailWarnService.setDebug(mailConfig.getDebug());
            return mailWarnService;
        }

        @Autowired
        void setDataChangedListener(MailWarnService mailWarnService) {
            AlarmLogWarnServiceFactory.setAlarmLogWarnService(mailWarnService);
        }
    }

    @Configuration
    @ConditionalOnProperty(value = "spring.alarm-log.warn.workweixin.enabled", havingValue = "true")
    @EnableConfigurationProperties(WorkWeixinConfig.class)
    static class WorkWeixinWarnServiceMethod {

        @Bean
        @ConditionalOnMissingBean(MailWarnService.class)
        public WorkWeixinWarnService workWeixinWarnService(final WorkWeixinConfig workWeixinConfig) {
            return new WorkWeixinWarnService(workWeixinConfig.getTo(), workWeixinConfig.getApplicationId(), workWeixinConfig.getCorpid(), workWeixinConfig.getCorpsecret());
        }

        @Autowired
        void setDataChangedListener(WorkWeixinWarnService workWeixinWarnService) {
            AlarmLogWarnServiceFactory.setAlarmLogWarnService(workWeixinWarnService);
        }
    }

    @Configuration
    @ConditionalOnProperty(value = "spring.alarm-log.warn.dingtalk.enabled", havingValue = "true")
    @EnableConfigurationProperties(DingtalkConfig.class)
    static class DingtalkWarnServiceMethod {

        @Bean
        @ConditionalOnMissingBean(DingtalkWarnService.class)
        public DingtalkWarnService dingtalkWarnService(final DingtalkConfig dingtalkConfig) {
            return new DingtalkWarnService(dingtalkConfig.getToken(), dingtalkConfig.getSecret());
        }

        @Autowired
        void setDataChangedListener(DingtalkWarnService dingtalkWarnService) {
            AlarmLogWarnServiceFactory.setAlarmLogWarnService(dingtalkWarnService);
        }
    }

    /**
     * The code position is important, in order after BaseWarnService.
     */
    @Configuration
    static class AlarmLogInit {

        @Autowired
        void setAlarmLogConfig(AlarmLogConfig alarmLogConfig) {
            Optional.ofNullable(alarmLogConfig.getException()).map(AlarmLogConfig.ExceptionConfig::getInclude).map(AlarmLogConfig.ExceptionMatcherConfig::getClasses).ifPresent(AlarmLogContext::setIncludeExceptionList);
            Optional.ofNullable(alarmLogConfig.getException()).map(AlarmLogConfig.ExceptionConfig::getInclude).map(AlarmLogConfig.ExceptionMatcherConfig::getExtend).ifPresent(AlarmLogContext::setIncludeExceptionExtend);
            Optional.ofNullable(alarmLogConfig.getException()).map(AlarmLogConfig.ExceptionConfig::getExclude).map(AlarmLogConfig.ExceptionMatcherConfig::getClasses).ifPresent(AlarmLogContext::setExcludeExceptionList);
            Optional.ofNullable(alarmLogConfig.getException()).map(AlarmLogConfig.ExceptionConfig::getExclude).map(AlarmLogConfig.ExceptionMatcherConfig::getExtend).ifPresent(AlarmLogContext::setExcludeExceptionExtend);
            Optional.ofNullable(alarmLogConfig.getPrintStackTrace()).ifPresent(AlarmLogContext::setPrintStackTrace);
            Optional.ofNullable(alarmLogConfig.getSimpleWarnInfo()).ifPresent(AlarmLogContext::setSimpleWarnInfo);
            Optional.ofNullable(alarmLogConfig.getMaxRetryTimes()).ifPresent(AlarmLogContext::setMaxRetryTimes);
            Optional.ofNullable(alarmLogConfig.getRetrySleepMillis()).ifPresent(AlarmLogContext::setRetrySleepMillis);
            Optional.ofNullable(alarmLogConfig.getIncludeContextKeys()).ifPresent(AlarmLogContext::setIncludeContextKeys);
        }

        @Autowired
        void setAlarmMessageContext(ObjectProvider<AlarmMessageContext> alarmMessageContext) {
            alarmMessageContext.ifAvailable(AlarmLogContext::setAlarmMessageContext);
        }
    }
}
