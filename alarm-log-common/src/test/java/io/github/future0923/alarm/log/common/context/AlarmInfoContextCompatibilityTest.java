package io.github.future0923.alarm.log.common.context;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AlarmInfoContextCompatibilityTest {

    @Test
    void oldNineArgumentConstructorAndBuilderRemainAvailable() {
        AlarmInfoContext oldStyle = new AlarmInfoContext(
                "message", "java.lang.RuntimeException", "logger", "thread",
                "ERROR", 42, "Test.java", "example.Test", "run");
        AlarmInfoContext built = AlarmInfoContext.builder().message("message").build();

        assertEquals(Collections.emptyMap(), oldStyle.getContextData());
        assertEquals(Collections.emptyMap(), built.getContextData());
    }

    @Test
    void contextDataSetterDefensivelyCopiesInput() {
        Map<String, String> source = new HashMap<>();
        source.put("traceId", "trace-1");
        AlarmInfoContext context = new AlarmInfoContext().setContextData(source);
        source.put("traceId", "changed");
        assertEquals("trace-1", context.getContextData().get("traceId"));
    }
}
