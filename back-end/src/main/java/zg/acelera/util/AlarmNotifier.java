package zg.acelera.util;

import java.time.LocalDateTime;

@FunctionalInterface
public interface AlarmNotifier {
    void notifyAlarm(String taskId, String alarmId, LocalDateTime triggerAt);
}
