package io.github.future0923.alarm.log.common.context;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author weilai
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Accessors(chain = true)
public class AlarmInfoContext {

    private String message;

    private String throwableName;

    private String loggerName;

    private String threadName;

    private String level;
    /**
     Caller's line number.
     */
    private int lineNumber;
    /**
     Caller's file name.
     */
    private String fileName;
    /**
     Caller's fully qualified class name.
     */
    private String className;
    /**
     Caller's method name.
     */
    private String methodName;

    private Map<String, String> contextData = Collections.emptyMap();

    public AlarmInfoContext(String message, String throwableName, String loggerName, String threadName,
                            String level, int lineNumber, String fileName, String className, String methodName) {
        this.message = message;
        this.throwableName = throwableName;
        this.loggerName = loggerName;
        this.threadName = threadName;
        this.level = level;
        this.lineNumber = lineNumber;
        this.fileName = fileName;
        this.className = className;
        this.methodName = methodName;
    }

    public Map<String, String> getContextData() {
        return contextData == null ? Collections.emptyMap() : contextData;
    }

    public AlarmInfoContext setContextData(Map<String, String> contextData) {
        this.contextData = contextData == null || contextData.isEmpty()
                ? Collections.emptyMap()
                : Collections.unmodifiableMap(new LinkedHashMap<>(contextData));
        return this;
    }

}
