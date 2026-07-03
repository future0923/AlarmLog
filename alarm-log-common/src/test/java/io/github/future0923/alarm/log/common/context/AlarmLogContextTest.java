package io.github.future0923.alarm.log.common.context;

import io.github.future0923.alarm.log.common.exception.AlarmLogRuntimeException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AlarmLogContextTest {

    private final Boolean originalIncludeExceptionExtend = AlarmLogContext.getIncludeExceptionExtend();
    private final Boolean originalExcludeExceptionExtend = AlarmLogContext.getExcludeExceptionExtend();
    private final ArrayList<String> originalIncludeExceptionList = new ArrayList<>(AlarmLogContext.getIncludeExceptionList());
    private final ArrayList<String> originalExcludeExceptionList = new ArrayList<>(AlarmLogContext.getExcludeExceptionList());

    @AfterEach
    void tearDown() {
        AlarmLogContext.setIncludeExceptionExtend(originalIncludeExceptionExtend);
        AlarmLogContext.setIncludeExceptionList(originalIncludeExceptionList);
        AlarmLogContext.setExcludeExceptionExtend(originalExcludeExceptionExtend);
        AlarmLogContext.setExcludeExceptionList(originalExcludeExceptionList);
    }

    @Test
    void shouldWarnExceptionUsesIncludeAndExcludeRules() {
        AlarmLogContext.setIncludeExceptionExtend(true);
        AlarmLogContext.setIncludeExceptionList(Collections.singletonList(IOException.class.getName()));
        AlarmLogContext.setExcludeExceptionExtend(false);
        AlarmLogContext.setExcludeExceptionList(Collections.singletonList(FileNotFoundException.class.getName()));

        assertFalse(AlarmLogContext.shouldWarnException(new FileNotFoundException("missing")));
        assertTrue(AlarmLogContext.shouldWarnException(new IOException("io")));
    }

    @Test
    void shouldWarnExceptionKeepsIncludeAndExcludeExtendSeparate() {
        AlarmLogContext.setIncludeExceptionExtend(true);
        AlarmLogContext.setIncludeExceptionList(Collections.singletonList(Exception.class.getName()));
        AlarmLogContext.setExcludeExceptionExtend(false);
        AlarmLogContext.setExcludeExceptionList(Collections.singletonList(IOException.class.getName()));

        assertTrue(AlarmLogContext.shouldWarnException(new FileNotFoundException("missing")));
        assertFalse(AlarmLogContext.shouldWarnException(new IOException("io")));
    }

    @Test
    void shouldWarnExceptionSupportsSettingClassesBeforeExtendFlags() {
        AlarmLogContext.setIncludeExceptionList(Collections.singletonList(Exception.class.getName()));
        AlarmLogContext.setExcludeExceptionList(Collections.singletonList(IOException.class.getName()));
        AlarmLogContext.setIncludeExceptionExtend(true);
        AlarmLogContext.setExcludeExceptionExtend(true);

        assertFalse(AlarmLogContext.shouldWarnException(new FileNotFoundException("missing")));
        assertTrue(AlarmLogContext.shouldWarnException(new RuntimeException("runtime")));
    }

    @Test
    void shouldWarnExceptionExcludesMarkerExceptionsBeforeMarkerMatch() {
        AlarmLogContext.setExcludeExceptionExtend(true);
        AlarmLogContext.setExcludeExceptionList(Collections.singletonList(AlarmLogRuntimeException.class.getName()));

        assertFalse(AlarmLogContext.shouldWarnException(new TestAlarmRuntimeException()));
    }

    static class TestAlarmRuntimeException extends AlarmLogRuntimeException {

    }
}
