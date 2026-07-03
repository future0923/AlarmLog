package io.github.future0923.alarm.log.warn.dingtalk;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author weilai
 */
@Data
public class DingtalkSendParam {

    private String msgtype;

    private Text text;

    private Markdown markdown;

    @Data
    @AllArgsConstructor
    public static class Text {

        private String content;
    }

    @Data
    @AllArgsConstructor
    public static class Markdown {

        private String title;

        private String text;
    }

}
