package zg.acelera.service;

import zg.acelera.domain.Alarm;
import zg.acelera.domain.Task;
import zg.acelera.domain.enums.Status;
import zg.acelera.dto.AlarmDTO;
import zg.acelera.repository.AlarmRepository;
import zg.acelera.repository.TaskRepository;
import zg.acelera.util.AlarmNotifier;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Set;
import java.util.List;
import java.util.UUID;

public class AlarmService {
    private final AlarmRepository alarmRepository;
    private final TaskRepository taskRepository;
    private final Clock clock;
    private final AlarmNotifier notifier;
    private AlarmMonitor monitor;

    public AlarmService(AlarmRepository alarmRepository, TaskRepository taskRepository) {
        this(alarmRepository, taskRepository, Clock.systemDefaultZone(), (taskId, alarmId, triggerAt) ->
                System.out.printf("Warning: task '%s' alarm '%s' due at %s%n", taskId, alarmId, triggerAt));
    }

    public AlarmService(AlarmRepository alarmRepository, TaskRepository taskRepository,
                        Clock clock, AlarmNotifier notifier) {
        this.alarmRepository = alarmRepository;
        this.taskRepository = taskRepository;
        this.clock = clock;
        this.notifier = notifier;
    }

    public void attachMonitor(AlarmMonitor monitor) {
        this.monitor = monitor;
    }

    public Alarm createAlarm(AlarmDTO dto) {
        Task task = requireTask(dto.taskId());
        List<Alarm> existing = new ArrayList<>(alarmRepository.findByTask(task.getName()));
        if (existing.size() >= 3) throw new IllegalArgumentException("A task cannot have more than three alarms.");
        int advance = dto.advanceMinutes();
        LocalDateTime trigger = triggerFor(task, advance);
        Alarm alarm = Alarm.builder()
                .id(UUID.randomUUID().toString())
                .taskId(task.getName())
                .advanceMinutes(advance)
                .triggerAt(trigger)
                .build();
        existing.add(alarm);
        refreshPrimary(existing);
        persistGroup(existing, null);
        startMonitor();
        return alarmRepository.findById(alarm.getId());
    }

    public Alarm updateAlarm(String alarmId, AlarmDTO dto) {
        Alarm current = requireAlarm(alarmId);
        Task task = requireTask(dto.taskId());
        if (!current.getTaskId().equalsIgnoreCase(task.getName())) {
            throw new IllegalArgumentException("Alarm task cannot be changed.");
        }
        LocalDateTime trigger = triggerFor(task, dto.advanceMinutes());
        Alarm replacement = Alarm.builder().id(current.getId()).taskId(task.getName())
                .advanceMinutes(dto.advanceMinutes()).triggerAt(trigger).build();
        List<Alarm> group = new ArrayList<>(alarmRepository.findByTask(task.getName()));
        group.removeIf(alarm -> alarm.getId().equals(current.getId()));
        group.add(replacement);
        refreshPrimary(group);
        persistGroup(group, current);
        updateMonitorState();
        return replacement;
    }

    public List<Alarm> listAlarms(String taskId) {
        requireTask(taskId);
        return alarmRepository.findByTask(taskId);
    }

    public boolean removeAlarm(String alarmId) {
        requireAlarm(alarmId);
        boolean removed = alarmRepository.delete(alarmId);
        updateMonitorState();
        return removed;
    }

    public int deleteByTask(String taskId) {
        int deleted = alarmRepository.deleteByTask(taskId);
        updateMonitorState();
        return deleted;
    }

    public void renameTaskAlarms(String oldTaskId, Task task) {
        List<Alarm> alarms = alarmRepository.findByTask(oldTaskId);
        for (Alarm alarm : alarms) {
            alarm.setTaskId(task.getName());
            alarmRepository.update(alarm);
        }
        recalculateForTask(task);
    }

    public void recalculateForTask(Task task) {
        List<Alarm> alarms = alarmRepository.findByTask(task.getName());
        List<Alarm> replacements = new ArrayList<>();
        for (Alarm alarm : alarms) {
            LocalDateTime trigger = triggerFor(task, alarm.getAdvanceMinutes());
            replacements.add(Alarm.builder().id(alarm.getId()).taskId(task.getName())
                    .advanceMinutes(alarm.getAdvanceMinutes()).triggerAt(trigger)
                    .primary(alarm.isPrimary()).build());
        }
        refreshPrimary(replacements);
        for (Alarm alarm : replacements) alarmRepository.update(alarm);
        updateMonitorState();
    }

    public void checkDueAlarms() {
        LocalDateTime now = LocalDateTime.now(clock);
        for (Alarm alarm : alarmRepository.findAll()) {
            Task task = findTask(alarm.getTaskId());
            if (task == null || task.getStatus() == Status.DONE) continue;
            if (alarm.isFired() && alarm.getNextTriggerAt() == null) continue;
            LocalDateTime dueAt = alarm.isFired() ? alarm.getNextTriggerAt() : alarm.getTriggerAt();
            if (dueAt == null || now.isBefore(dueAt)) continue;
            Alarm updated = Alarm.builder().id(alarm.getId()).taskId(alarm.getTaskId())
                    .advanceMinutes(alarm.getAdvanceMinutes()).triggerAt(alarm.getTriggerAt())
                    .primary(alarm.isPrimary()).fired(true).lastFiredAt(dueAt)
                    .nextTriggerAt(alarm.isPrimary() ? dueAt.plusMinutes(alarm.getAdvanceMinutes()) : null)
                    .build();
            alarmRepository.update(updated);
            notifier.notifyAlarm(alarm.getTaskId(), alarm.getId(), dueAt);
        }
    }

    public boolean hasMonitorableAlarms() {
        for (Alarm alarm : alarmRepository.findAll()) {
            Task task = findTask(alarm.getTaskId());
            if (task != null && (task.getStatus() == Status.TODO || task.getStatus() == Status.DOING)) return true;
        }
        return false;
    }

    private void persistGroup(List<Alarm> group, Alarm old) {
        List<Alarm> before = alarmRepository.findByTask(group.get(0).getTaskId()).stream()
                .map(this::copyAlarm).collect(java.util.stream.Collectors.toCollection(ArrayList::new));
        try {
            for (Alarm alarm : group) {
                if (old != null && alarm.getId().equals(old.getId())) alarmRepository.update(alarm);
                else if (old == null && alarmRepository.findById(alarm.getId()) == null) alarmRepository.save(alarm);
                else alarmRepository.update(alarm);
            }
        } catch (RuntimeException failure) {
            restoreGroup(before, group, failure);
            throw failure;
        }
    }

    private Alarm copyAlarm(Alarm alarm) {
        return Alarm.builder().id(alarm.getId()).taskId(alarm.getTaskId())
                .advanceMinutes(alarm.getAdvanceMinutes()).triggerAt(alarm.getTriggerAt())
                .primary(alarm.isPrimary()).fired(alarm.isFired())
                .lastFiredAt(alarm.getLastFiredAt()).nextTriggerAt(alarm.getNextTriggerAt())
                .build();
    }

    private void restoreGroup(List<Alarm> before, List<Alarm> attempted, RuntimeException failure) {
        Set<String> originalIds = before.stream().map(Alarm::getId).collect(java.util.stream.Collectors.toSet());
        for (Alarm alarm : attempted) {
            if (originalIds.contains(alarm.getId())) continue;
            try {
                alarmRepository.delete(alarm.getId());
            } catch (RuntimeException rollbackFailure) {
                failure.addSuppressed(rollbackFailure);
            }
        }
        for (Alarm alarm : before) {
            try {
                if (alarmRepository.findById(alarm.getId()) == null) alarmRepository.save(alarm);
                else alarmRepository.update(alarm);
            } catch (RuntimeException rollbackFailure) {
                failure.addSuppressed(rollbackFailure);
            }
        }
    }

    private void refreshPrimary(List<Alarm> alarms) {
        Alarm primary = alarms.stream().min(Comparator.comparingInt(Alarm::getAdvanceMinutes)).orElse(null);
        for (Alarm alarm : alarms) {
            alarm.setPrimary(alarm == primary);
        }
    }

    private LocalDateTime triggerFor(Task task, int advanceMinutes) {
        if (advanceMinutes < 1) throw new IllegalArgumentException("Alarm advance must be at least one minute.");
        long duration = Duration.between(task.getStartDate(), task.getEndDate()).toMinutes();
        if (duration <= 0 || advanceMinutes >= duration) {
            throw new IllegalArgumentException("Alarm advance must be shorter than task duration.");
        }
        LocalDateTime trigger = task.getEndDate().minusMinutes(advanceMinutes);
        if (!trigger.isAfter(LocalDateTime.now(clock))) {
            throw new IllegalArgumentException("Alarm trigger must be in the future.");
        }
        return trigger;
    }

    private Task requireTask(String taskId) {
        Task task = findTask(taskId);
        if (task == null) throw new IllegalArgumentException("Task not found: " + taskId);
        return task;
    }

    private Task findTask(String taskId) {
        try {
            return taskRepository.findAll().stream()
                    .filter(task -> task.getName().equalsIgnoreCase(taskId.trim())).findFirst().orElse(null);
        } catch (IOException e) {
            throw new RuntimeException("Error fetching tasks: " + e.getMessage(), e);
        }
    }

    private Alarm requireAlarm(String id) {
        Alarm alarm = alarmRepository.findById(id);
        if (alarm == null) throw new IllegalArgumentException("Alarm not found: " + id);
        return alarm;
    }

    private void startMonitor() {
        if (monitor != null) monitor.start();
    }

    private void updateMonitorState() {
        if (monitor == null) return;
        if (hasMonitorableAlarms()) monitor.start();
        else monitor.stop();
    }
}
