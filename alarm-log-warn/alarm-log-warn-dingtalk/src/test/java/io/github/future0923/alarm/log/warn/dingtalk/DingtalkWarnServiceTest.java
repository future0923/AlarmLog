package io.github.future0923.alarm.log.warn.dingtalk;

import io.github.future0923.alarm.log.common.context.AlarmInfoContext;
import io.github.future0923.alarm.log.common.context.AlarmLogContext;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DingtalkWarnServiceTest {

    private final int originalMaxRetryTimes = AlarmLogContext.getMaxRetryTimes();

    @AfterEach
    void tearDown() {
        AlarmLogContext.setMaxRetryTimes(originalMaxRetryTimes);
    }

    @Test
    void sendReturnsTrueWhenRobotMessageIsPosted() {
        AlarmLogContext.setMaxRetryTimes(0);
        TestDingtalkWarnService service = new TestDingtalkWarnService();

        boolean success = service.send(AlarmInfoContext.builder().message("boom").build(), new RuntimeException("boom"));

        assertTrue(success);
        assertEquals(1, service.sendRobotMessageCount);
    }

    @Test
    void createPostDataUsesMarkdownMessageType() {
        TestDingtalkWarnService service = new TestDingtalkWarnService();

        JsonObject jsonObject = new Gson().fromJson(service.createPostData("## [ERROR] Alarm Log"), JsonObject.class);

        assertEquals("markdown", jsonObject.get("msgtype").getAsString());
        assertEquals("Alarm Log", jsonObject.getAsJsonObject("markdown").get("title").getAsString());
        assertEquals("## [ERROR] Alarm Log", jsonObject.getAsJsonObject("markdown").get("text").getAsString());
    }

    private static class TestDingtalkWarnService extends DingtalkWarnService {

        private int sendRobotMessageCount;

        private TestDingtalkWarnService() {
            super("token", "secret");
        }

        @Override
        public String sendRobotMessage(String message) {
            sendRobotMessageCount++;
            return "{\"errcode\":0,\"errmsg\":\"ok\"}";
        }
    }
}
