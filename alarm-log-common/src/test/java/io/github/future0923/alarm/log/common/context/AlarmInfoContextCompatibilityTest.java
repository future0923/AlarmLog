package io.github.future0923.alarm.log.common.context;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AlarmInfoContextCompatibilityTest {

    @Test
    void oldNineArgumentConstructorAndBuilderRemainAvailable() {
        AlarmInfoContext oldStyle = new AlarmInfoContext(
                "message", "java.lang.RuntimeException", "logger", "thread",
                "ERROR", 42, "Test.java", "example.Test", "run");
        AlarmInfoContext built = AlarmInfoContext.builder()
                .message("message")
                .throwableName("java.lang.RuntimeException")
                .loggerName("logger")
                .threadName("thread")
                .level("ERROR")
                .lineNumber(42)
                .fileName("Test.java")
                .className("example.Test")
                .methodName("run")
                .build();

        assertEquals(Collections.emptyMap(), oldStyle.getContextData());
        assertEquals(Collections.emptyMap(), built.getContextData());
        assertEquals("message", built.getMessage());
        assertEquals("java.lang.RuntimeException", built.getThrowableName());
        assertEquals("logger", built.getLoggerName());
        assertEquals("thread", built.getThreadName());
        assertEquals("ERROR", built.getLevel());
        assertEquals(42, built.getLineNumber());
        assertEquals("Test.java", built.getFileName());
        assertEquals("example.Test", built.getClassName());
        assertEquals("run", built.getMethodName());
    }

    @Test
    void contextDataSetterDefensivelyCopiesInput() {
        Map<String, String> source = new HashMap<>();
        source.put("traceId", "trace-1");
        AlarmInfoContext context = new AlarmInfoContext().setContextData(source);
        source.put("traceId", "changed");
        assertEquals("trace-1", context.getContextData().get("traceId"));
    }

    @Test
    void builderDefensivelyCopiesContextDataAndReturnsImmutableMap() {
        Map<String, String> source = new HashMap<>();
        source.put("traceId", "trace-1");

        AlarmInfoContext context = AlarmInfoContext.builder().contextData(source).build();
        source.put("traceId", "changed");

        assertEquals("trace-1", context.getContextData().get("traceId"));
        assertThrows(UnsupportedOperationException.class,
                () -> context.getContextData().put("spanId", "span-1"));
    }

    @Test
    void tenArgumentConstructorDefensivelyCopiesContextData() {
        Map<String, String> source = new HashMap<>();
        source.put("traceId", "trace-1");

        AlarmInfoContext context = new AlarmInfoContext(
                "message", "java.lang.RuntimeException", "logger", "thread",
                "ERROR", 42, "Test.java", "example.Test", "run", source);
        source.put("traceId", "changed");

        assertEquals("trace-1", context.getContextData().get("traceId"));
        assertThrows(UnsupportedOperationException.class,
                () -> context.getContextData().put("spanId", "span-1"));
    }

    @Test
    void nullContextDataUsesImmutableEmptyMap() {
        AlarmInfoContext context = new AlarmInfoContext().setContextData(null);

        assertEquals(Collections.emptyMap(), context.getContextData());
        assertThrows(UnsupportedOperationException.class,
                () -> context.getContextData().put("traceId", "trace-1"));
    }
}
