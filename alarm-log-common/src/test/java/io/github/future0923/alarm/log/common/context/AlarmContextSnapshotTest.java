package io.github.future0923.alarm.log.common.context;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AlarmContextSnapshotTest {

    @AfterEach
    void clearContextKeys() {
        AlarmLogContext.setIncludeContextKeys(Collections.emptyList());
    }

    @Test
    void captureKeepsOnlyConfiguredKeysAndReturnsImmutableCopy() {
        AlarmLogContext.setIncludeContextKeys(" traceId, spanId, traceId ");
        Map<String, Object> source = new LinkedHashMap<>();
        source.put("traceId", "trace-1");
        source.put("spanId", 3);
        source.put("Authorization", "Bearer secret");

        Map<String, String> snapshot = AlarmContextSnapshot.capture(source);
        source.put("traceId", "changed");

        assertEquals("trace-1", snapshot.get("traceId"));
        assertEquals("3", snapshot.get("spanId"));
        assertFalse(snapshot.containsKey("Authorization"));
        assertThrows(UnsupportedOperationException.class,
                () -> snapshot.put("password", "secret"));
    }
}
