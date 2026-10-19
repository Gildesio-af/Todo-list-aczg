package zg.acelera.dto;

import zg.acelera.util.exception.TaskInfoWrongException;

public record AlarmDTO(String taskId, Integer hours, Integer minutes) {
    public AlarmDTO {
        if (taskId == null || taskId.trim().isEmpty()) {
            throw new TaskInfoWrongException("Task identity cannot be empty.");
        }
        if (hours == null || minutes == null || hours < 0 || minutes < 0) {
            throw new TaskInfoWrongException("Alarm advance must use non-negative integer hours and minutes.");
        }
        if ((long) hours * 60 + minutes < 1) {
            throw new TaskInfoWrongException("Alarm advance must be at least one minute.");
        }
    }

    public int advanceMinutes() {
        long total = (long) hours * 60 + minutes;
        if (total > Integer.MAX_VALUE) {
            throw new TaskInfoWrongException("Alarm advance is too large.");
        }
        return (int) total;
    }
}
