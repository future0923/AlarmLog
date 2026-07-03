package io.github.future0923.alarm.log.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * @author weilai
 */
@Data
@ConfigurationProperties(prefix = "spring.alarm-log")
public class AlarmLogConfig {

    private Integer maxRetryTimes = 3;

    private Integer retrySleepMillis = 1000;

    private Boolean printStackTrace = false;

    private Boolean simpleWarnInfo = false;

    private ExceptionConfig exception = new ExceptionConfig();

    @Data
    public static class ExceptionConfig {

        private ExceptionMatcherConfig include = new ExceptionMatcherConfig();

        private ExceptionMatcherConfig exclude = new ExceptionMatcherConfig();
    }

    @Data
    public static class ExceptionMatcherConfig {

        private List<String> classes;

        private Boolean extend = false;
    }
}
