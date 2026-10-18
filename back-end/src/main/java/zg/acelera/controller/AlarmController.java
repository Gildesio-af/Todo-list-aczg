package zg.acelera.controller;

import zg.acelera.domain.Alarm;
import zg.acelera.dto.AlarmDTO;
import zg.acelera.service.AlarmService;
import zg.acelera.service.AlarmMonitor;
import zg.acelera.util.interface_user.TaskInterface;

public class AlarmController {
    private final AlarmService alarmService;
    private final TaskInterface taskInterface;
    private final AlarmMonitor alarmMonitor;

    public AlarmController(AlarmService alarmService, TaskInterface taskInterface) {
        this(alarmService, taskInterface, null);
    }

    public AlarmController(AlarmService alarmService, TaskInterface taskInterface, AlarmMonitor alarmMonitor) {
        this.alarmService = alarmService;
        this.taskInterface = taskInterface;
        this.alarmMonitor = alarmMonitor;
    }

    public void createAlarm(AlarmDTO dto) {
        try {
            Alarm alarm = alarmService.createAlarm(dto);
            if (alarmMonitor != null) alarmMonitor.start();
            taskInterface.showMessage("Alarm created successfully: " + alarm.getId());
        } catch (RuntimeException e) {
            taskInterface.showMessage("Error creating alarm: " + e.getMessage());
        }
    }

    public void updateAlarm(String alarmId, AlarmDTO dto) {
        try {
            alarmService.updateAlarm(alarmId, dto);
            if (alarmMonitor != null) alarmMonitor.start();
            taskInterface.showMessage("Alarm updated successfully: " + alarmId);
        } catch (RuntimeException e) {
            taskInterface.showMessage("Error updating alarm: " + e.getMessage());
        }
    }

    public void listAlarms(String taskId) {
        try {
            for (Alarm alarm : alarmService.listAlarms(taskId)) taskInterface.showMessage(alarm.toString());
        } catch (RuntimeException e) {
            taskInterface.showMessage("Error listing alarms: " + e.getMessage());
        }
    }

    public void removeAlarm(String alarmId) {
        try {
            if (alarmService.removeAlarm(alarmId)) {
                if (alarmMonitor != null && !alarmService.hasMonitorableAlarms()) alarmMonitor.stop();
                taskInterface.showMessage("Alarm removed successfully: " + alarmId);
            }
            else taskInterface.showMessage("Alarm '" + alarmId + "' not found.");
        } catch (RuntimeException e) {
            taskInterface.showMessage("Error removing alarm: " + e.getMessage());
        }
    }
}
