package io.github.future0923.alarm.log.common.utils;

import io.github.future0923.alarm.log.common.context.AlarmInfoContext;
import io.github.future0923.alarm.log.common.context.AlarmLogContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ThrowableUtilsTest {

    private final Boolean originalSimpleWarnInfo = AlarmLogContext.getSimpleWarnInfo();
    private final Boolean originalPrintStackTrace = AlarmLogContext.getPrintStackTrace();

    @AfterEach
    void tearDown() {
        AlarmLogContext.setSimpleWarnInfo(originalSimpleWarnInfo);
        AlarmLogContext.setPrintStackTrace(originalPrintStackTrace);
    }

    @Test
    void dingtalkContentUsesMarkdownAlarmCard() {
        AlarmLogContext.setSimpleWarnInfo(false);
        AlarmLogContext.setPrintStackTrace(false);
        AlarmInfoContext context = AlarmInfoContext.builder()
                .message("完犊子了啊")
                .level("ERROR")
                .throwableName(RuntimeException.class.getName())
                .threadName("http-nio-3041-exec-1")
                .className("com.hz.zfb.data.broker.controller.BrokerYktController")
                .methodName("get123")
                .fileName("BrokerYktController.java")
                .lineNumber(40)
                .build();

        String content = ThrowableUtils.dingtalkContent(context, new RuntimeException("Server returned HTTP response code: 502 for URL: http://47.96.22.2/test?a=1|b=2"));

        assertTrue(content.contains("## 完犊子了啊"));
        assertFalse(content.contains("## [ERROR] Alarm Log"));
        assertFalse(content.contains("> **完犊子了啊**"));
        assertTrue(content.contains("---"));
        assertTrue(content.contains("### Runtime"));
        assertTrue(content.contains("| Field | Value |"));
        assertTrue(content.contains("| --- | --- |"));
        assertTrue(content.contains("| **Level** | ERROR |"));
        assertTrue(content.contains("| **Throwable** | java.lang.RuntimeException |"));
        assertTrue(content.contains("| **Message** | Server returned HTTP response code: 502 for URL: http://47.96.22.2/test?a=1\\|b=2 |"));
        assertTrue(content.contains("| **Thread** | http-nio-3041-exec-1 |"));
        assertTrue(content.contains("### Source"));
        assertTrue(content.contains("```text"));
        assertTrue(content.contains("com.hz.zfb.data.broker.controller.BrokerYktController.get123(BrokerYktController.java:40)"));
    }

    @Test
    void dingtalkContentKeepsMessageCompactWhenStackTraceEnabled() {
        AlarmLogContext.setSimpleWarnInfo(false);
        AlarmLogContext.setPrintStackTrace(true);
        AlarmInfoContext context = AlarmInfoContext.builder()
                .message("完犊子了啊")
                .level("ERROR")
                .throwableName(RuntimeException.class.getName())
                .threadName("http-nio-3041-exec-1")
                .className("com.hz.zfb.data.broker.controller.BrokerYktController")
                .methodName("get123")
                .fileName("BrokerYktController.java")
                .lineNumber(40)
                .build();
        RuntimeException throwable = new RuntimeException("boom");
        throwable.setStackTrace(new StackTraceElement[]{
                new StackTraceElement("com.hz.zfb.data.broker.controller.BrokerYktController", "get123", "BrokerYktController.java", 40),
                new StackTraceElement("sun.reflect.NativeMethodAccessorImpl", "invoke0", "NativeMethodAccessorImpl.java", -2),
                new StackTraceElement("sun.reflect.NativeMethodAccessorImpl", "invoke", "NativeMethodAccessorImpl.java", 62)
        });

        String content = ThrowableUtils.dingtalkContent(context, throwable);

        assertFalse(content.contains("### Stack Trace"));
        assertFalse(content.contains("java.lang.RuntimeException: boom"));
        assertFalse(content.contains("    at "));
        assertFalse(content.contains("#1 com.hz.zfb.data.broker.controller.BrokerYktController.get123"));
        assertTrue(content.contains("### Source"));
        assertTrue(content.contains("com.hz.zfb.data.broker.controller.BrokerYktController.get123(BrokerYktController.java:40)"));
    }
}
