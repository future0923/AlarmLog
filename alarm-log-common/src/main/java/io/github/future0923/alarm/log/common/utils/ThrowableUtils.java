package io.github.future0923.alarm.log.common.utils;

import io.github.future0923.alarm.log.common.context.AlarmInfoContext;
import io.github.future0923.alarm.log.common.context.AlarmLogContext;
import io.github.future0923.alarm.log.common.dto.AlarmMailContent;

import java.util.Objects;

/**
 * @author weilai
 */
public class ThrowableUtils {

    private static final String SEPARATOR = "\n";
    private static final String HTML_SEPARATOR = "<br />";

    public static String workWeixinContent(AlarmInfoContext context, Throwable throwable) {
        return defaultContent(context, throwable, SEPARATOR);
    }

    public static String dingtalkContent(AlarmInfoContext context, Throwable throwable) {
        return dingtalkMarkdownContent(context, throwable);
    }

    public static AlarmMailContent mailSubjectContent(AlarmInfoContext context, Throwable throwable) {
        return new AlarmMailContent(context.getMessage(), defaultContent(context, throwable, HTML_SEPARATOR));
    }

    private static String defaultContent(AlarmInfoContext context, Throwable throwable, String separator) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(context.getMessage()).append(separator);
        if (!AlarmLogContext.getSimpleWarnInfo()) {
            stringBuilder.append("级别:").append(context.getLevel()).append(separator);
            if (Objects.nonNull(context.getThrowableName())) {
                stringBuilder.append("异常:").append(context.getThrowableName()).append(separator);
            }
            stringBuilder.append("线程:").append(context.getThreadName()).append(separator);
            stringBuilder.append("位置信息:").append(context.getClassName()).append(".").append(context.getMethodName()).append(isNativeMethod(context.getLineNumber()) ? "(Native Method)" : context.getFileName() != null && context.getLineNumber() >= 0 ? "(" + context.getFileName() + ":" + context.getLineNumber() + ")" : context.getFileName() != null ? "(" + context.getFileName() + ")" : "(Unknown Source)");
            stringBuilder.append(separator);
        }
        if (AlarmLogContext.getPrintStackTrace()) {
            stringBuilder.append(printTrace(throwable));
        }
        return stringBuilder.toString();
    }

    private static String dingtalkMarkdownContent(AlarmInfoContext context, Throwable throwable) {
        StringBuilder stringBuilder = new StringBuilder();
        String level = nullToEmpty(context.getLevel());
        stringBuilder.append("## ").append(nullToEmpty(context.getMessage())).append(SEPARATOR).append(SEPARATOR);
        stringBuilder.append("---").append(SEPARATOR).append(SEPARATOR);
        if (!AlarmLogContext.getSimpleWarnInfo()) {
            stringBuilder.append("### Runtime").append(SEPARATOR).append(SEPARATOR);
            stringBuilder.append("| Field | Value |").append(SEPARATOR);
            stringBuilder.append("| --- | --- |").append(SEPARATOR);
            stringBuilder.append("| **Level** | ").append(level).append(" |").append(SEPARATOR);
            if (Objects.nonNull(context.getThrowableName())) {
                stringBuilder.append("| **Throwable** | ").append(markdownTableValue(context.getThrowableName())).append(" |").append(SEPARATOR);
            }
            if (Objects.nonNull(throwable) && Objects.nonNull(throwable.getMessage())) {
                stringBuilder.append("| **Message** | ").append(markdownTableValue(throwable.getMessage())).append(" |").append(SEPARATOR);
            }
            stringBuilder.append("| **Thread** | ").append(markdownTableValue(context.getThreadName())).append(" |").append(SEPARATOR);
            stringBuilder.append(SEPARATOR);
            stringBuilder.append("### Source").append(SEPARATOR).append(SEPARATOR);
            stringBuilder.append("```text").append(SEPARATOR);
            stringBuilder.append(location(context)).append(SEPARATOR);
            stringBuilder.append("```").append(SEPARATOR).append(SEPARATOR);
        }
        return stringBuilder.toString();
    }

    private static String location(AlarmInfoContext context) {
        return nullToEmpty(context.getClassName()) + "." + nullToEmpty(context.getMethodName()) + source(context);
    }

    private static String source(AlarmInfoContext context) {
        if (isNativeMethod(context.getLineNumber())) {
            return "(Native Method)";
        }
        if (context.getFileName() != null && context.getLineNumber() >= 0) {
            return "(" + context.getFileName() + ":" + context.getLineNumber() + ")";
        }
        if (context.getFileName() != null) {
            return "(" + context.getFileName() + ")";
        }
        return "(Unknown Source)";
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static String markdownTableValue(String value) {
        return nullToEmpty(value)
                .replace("\r", " ")
                .replace("\n", " ")
                .replace("|", "\\|");
    }

    private static String printTrace(Throwable throwable) {
        if (Objects.isNull(throwable)) {
            return "";
        }
        StackTraceElement[] trace = throwable.getStackTrace();
        StringBuilder content = new StringBuilder();
        content.append(throwable.toString());
        for (StackTraceElement traceElement : trace) {
            content.append("\n    at ").append(traceElement);
        }
        return content.toString();
    }

    private static boolean isNativeMethod(int lineNumber) {
        return lineNumber == -2;
    }

}
