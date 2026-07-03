package io.github.future0923.alarm.log.common.context;

import io.github.future0923.alarm.log.common.dto.AlarmLogSimpleConfig;
import io.github.future0923.alarm.log.common.utils.ExceptionUtils;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author weilai
 */
public class AlarmLogContext {

    private static final Logger logger = LoggerFactory.getLogger(AlarmLogContext.class);

    @Getter
    @Setter
    private static int maxRetryTimes = 3;

    @Getter
    @Setter
    private static int retrySleepMillis = 1000;

    @Getter
    @Setter
    private static Boolean printStackTrace = false;

    @Getter
    @Setter
    private static Boolean simpleWarnInfo = false;

    private static Boolean includeExceptionExtend = false;

    private static Boolean excludeExceptionExtend = false;

    private static List<String> includeExceptionList = new ArrayList<>();

    private static List<String> excludeExceptionList = new ArrayList<>();

    private static List<Class<? extends Throwable>> includeExtendExceptionList = new ArrayList<>();

    private static List<Class<? extends Throwable>> excludeExtendExceptionList = new ArrayList<>();

    @Getter
    @Setter
    private static AlarmMessageContext alarmMessageContext = new DefaultAlarmMessageContext();

    private static AlarmLogSimpleConfig simpleConfig;

    public static AlarmLogSimpleConfig getSimpleConfig () {
        if (Objects.isNull(simpleConfig)) {
            simpleConfig = AlarmLogSimpleConfig.builder().printStackTrace(printStackTrace).simpleWarnInfo(simpleWarnInfo).build();
        }
        return simpleConfig;
    }

    public static Boolean getIncludeExceptionExtend() {
        return includeExceptionExtend;
    }

    public static void setIncludeExceptionExtend(Boolean includeExceptionExtend) {
        AlarmLogContext.includeExceptionExtend = includeExceptionExtend;
        AlarmLogContext.includeExtendExceptionList = Boolean.TRUE.equals(includeExceptionExtend) ? genExtendExceptionList(AlarmLogContext.includeExceptionList) : new ArrayList<>();
    }

    public static Boolean getExcludeExceptionExtend() {
        return excludeExceptionExtend;
    }

    public static void setExcludeExceptionExtend(Boolean excludeExceptionExtend) {
        AlarmLogContext.excludeExceptionExtend = excludeExceptionExtend;
        AlarmLogContext.excludeExtendExceptionList = Boolean.TRUE.equals(excludeExceptionExtend) ? genExtendExceptionList(AlarmLogContext.excludeExceptionList) : new ArrayList<>();
    }

    public static List<String> getIncludeExceptionList() {
        return includeExceptionList;
    }

    public static void addIncludeExceptionList(List<String> includeExceptionList) {
        AlarmLogContext.includeExceptionList.addAll(includeExceptionList);
        if (Boolean.TRUE.equals(AlarmLogContext.includeExceptionExtend)) {
            AlarmLogContext.includeExtendExceptionList.addAll(genExtendExceptionList(includeExceptionList));
        }
    }

    public static void setIncludeExceptionList(List<String> includeExceptionList) {
        AlarmLogContext.includeExceptionList = includeExceptionList;
        AlarmLogContext.includeExtendExceptionList = Boolean.TRUE.equals(AlarmLogContext.includeExceptionExtend) ? genExtendExceptionList(includeExceptionList) : new ArrayList<>();
    }

    public static List<String> getExcludeExceptionList() {
        return excludeExceptionList;
    }

    public static void addExcludeExceptionList(List<String> excludeExceptionList) {
        AlarmLogContext.excludeExceptionList.addAll(excludeExceptionList);
        if (Boolean.TRUE.equals(AlarmLogContext.excludeExceptionExtend)) {
            AlarmLogContext.excludeExtendExceptionList.addAll(genExtendExceptionList(excludeExceptionList));
        }
    }

    public static void setExcludeExceptionList(List<String> excludeExceptionList) {
        AlarmLogContext.excludeExceptionList = excludeExceptionList;
        AlarmLogContext.excludeExtendExceptionList = Boolean.TRUE.equals(AlarmLogContext.excludeExceptionExtend) ? genExtendExceptionList(excludeExceptionList) : new ArrayList<>();
    }

    public static boolean shouldWarnException(Throwable exception) {
        if (matchExcludeException(exception)) {
            return false;
        }
        return matchIncludeException(exception) || ExceptionUtils.matchMarkerException(exception);
    }

    public static boolean matchIncludeException(Throwable exception) {
        return Boolean.TRUE.equals(AlarmLogContext.includeExceptionExtend) ? ExceptionUtils.matchExceptionExtend(exception, AlarmLogContext.includeExtendExceptionList) : ExceptionUtils.matchExceptionName(exception, AlarmLogContext.includeExceptionList);
    }

    public static boolean matchExcludeException(Throwable exception) {
        return Boolean.TRUE.equals(AlarmLogContext.excludeExceptionExtend) ? ExceptionUtils.matchExceptionExtend(exception, AlarmLogContext.excludeExtendExceptionList) : ExceptionUtils.matchExceptionName(exception, AlarmLogContext.excludeExceptionList);
    }

    @SuppressWarnings("unchecked")
    private static List<Class<? extends Throwable>> genExtendExceptionList(List<String> exceptionList) {
        List<Class<? extends Throwable>> extendExceptionList = new ArrayList<>();
        for (String className : exceptionList) {
            try {
                extendExceptionList.add((Class<? extends Throwable>) Class.forName(className));
            } catch (ClassNotFoundException e) {
                logger.error("init AlarmLogContext classNotFoundException, className [{}]", className);
            }
        }
        return extendExceptionList;
    }
}
