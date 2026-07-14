package io.github.future0923.alarm.log.autoconfigure;

import io.github.future0923.alarm.log.common.context.AlarmLogContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AlarmLogContextConfigurationTest {

    private final Set<String> originalIncludeContextKeys = new LinkedHashSet<>(AlarmLogContext.getIncludeContextKeys());

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(AlarmLogAutoConfiguration.class);

    @AfterEach
    void restoreAlarmLogContext() {
        AlarmLogContext.setIncludeContextKeys(originalIncludeContextKeys);
    }

    @Test
    void bindsIncludeContextKeysAndInitializesAlarmLogContext() {
        contextRunner
                .withPropertyValues("spring.alarm-log.include-context-keys=traceId,spanId")
                .run(context -> {
                    assertThat(context.getBean(AlarmLogConfig.class).getIncludeContextKeys())
                            .containsExactly("traceId", "spanId");
                    assertThat(AlarmLogContext.getIncludeContextKeys())
                            .containsExactly("traceId", "spanId");
                });
    }
}
