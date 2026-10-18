package zg.acelera.util;

import java.time.LocalDateTime;

public class ConsoleAlarmNotifier implements AlarmNotifier {
    @Override
    public void notifyAlarm(String taskId, String alarmId, LocalDateTime triggerAt) {
        System.out.printf("Warning: task '%s' alarm '%s' is due at %s%n", taskId, alarmId, triggerAt);
    }
}
