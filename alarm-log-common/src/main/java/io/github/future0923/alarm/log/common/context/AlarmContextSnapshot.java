package io.github.future0923.alarm.log.common.context;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class AlarmContextSnapshot {

    private AlarmContextSnapshot() {
    }

    public static Map<String, String> capture(Map<?, ?> source) {
        Set<String> keys = AlarmLogContext.getIncludeContextKeys();
        if (source == null || source.isEmpty() || keys.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, String> result = new LinkedHashMap<>();
        for (String key : keys) {
            Object value = source.get(key);
            if (value != null) {
                String text = String.valueOf(value).trim();
                if (!text.isEmpty()) {
                    result.put(key, text);
                }
            }
        }
        return result.isEmpty()
                ? Collections.emptyMap()
                : Collections.unmodifiableMap(result);
    }
}
