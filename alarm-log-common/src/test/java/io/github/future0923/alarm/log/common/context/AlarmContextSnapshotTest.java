package io.github.future0923.alarm.log.common.context;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

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

    @Test
    void collectionSetterHandlesNullAndNormalizesKeys() {
        AlarmLogContext.setIncludeContextKeys((Collection<String>) null);
        assertEquals(Collections.emptySet(), AlarmLogContext.getIncludeContextKeys());

        AlarmLogContext.setIncludeContextKeys(Arrays.asList(
                " traceId ", null, "", "   ", "spanId", "traceId"));

        Set<String> expected = new LinkedHashSet<>(Arrays.asList("traceId", "spanId"));
        assertEquals(expected, AlarmLogContext.getIncludeContextKeys());
        assertThrows(UnsupportedOperationException.class,
                () -> AlarmLogContext.getIncludeContextKeys().add("requestId"));
    }

    @Test
    void csvSetterHandlesNullAndNormalizesKeys() {
        AlarmLogContext.setIncludeContextKeys((String) null);
        assertEquals(Collections.emptySet(), AlarmLogContext.getIncludeContextKeys());

        AlarmLogContext.setIncludeContextKeys(" traceId, , spanId,traceId,, ");

        Set<String> expected = new LinkedHashSet<>(Arrays.asList("traceId", "spanId"));
        assertEquals(expected, AlarmLogContext.getIncludeContextKeys());
    }

    @Test
    void captureReturnsEmptyForEmptyWhitelistOrSource() {
        Map<String, Object> source = Collections.singletonMap("traceId", "trace-1");

        assertEquals(Collections.emptyMap(), AlarmContextSnapshot.capture(source));

        AlarmLogContext.setIncludeContextKeys(Collections.singletonList("traceId"));
        assertEquals(Collections.emptyMap(), AlarmContextSnapshot.capture(null));
        assertEquals(Collections.emptyMap(), AlarmContextSnapshot.capture(Collections.emptyMap()));
    }

    @Test
    void captureTrimsValuesAndSkipsNullAndBlankValues() {
        AlarmLogContext.setIncludeContextKeys(Arrays.asList("traceId", "spanId", "requestId", "attempt"));
        Map<String, Object> source = new LinkedHashMap<>();
        source.put("traceId", " trace-1 ");
        source.put("spanId", null);
        source.put("requestId", "   ");
        source.put("attempt", 3);

        Map<String, String> snapshot = AlarmContextSnapshot.capture(source);

        Map<String, String> expected = new LinkedHashMap<>();
        expected.put("traceId", "trace-1");
        expected.put("attempt", "3");
        assertEquals(expected, snapshot);
    }
}
