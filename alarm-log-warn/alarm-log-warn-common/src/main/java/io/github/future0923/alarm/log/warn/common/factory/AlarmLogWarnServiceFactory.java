package io.github.future0923.alarm.log.warn.common.factory;

import io.github.future0923.alarm.log.warn.common.AlarmLogWarnService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author weilai
 */
public class AlarmLogWarnServiceFactory {

    private static List<AlarmLogWarnService> serviceList = new ArrayList<>();

    public AlarmLogWarnServiceFactory() {
    }

    public AlarmLogWarnServiceFactory(List<AlarmLogWarnService> alarmLogWarnServices) {
        setAlarmLogWarnServices(alarmLogWarnServices);
    }

    public static synchronized void setAlarmLogWarnService(AlarmLogWarnService alarmLogWarnService) {
        serviceList.add(alarmLogWarnService);
    }

    public static synchronized void setAlarmLogWarnServices(List<AlarmLogWarnService> alarmLogWarnServices) {
        serviceList = new ArrayList<>(alarmLogWarnServices);
    }

    public static synchronized List<AlarmLogWarnService> getServiceList () {
        return Collections.unmodifiableList(new ArrayList<>(serviceList));
    }

    public static synchronized void clear() {
        serviceList.clear();
    }
}
