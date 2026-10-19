package zg.acelera.service;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class AlarmMonitor implements AutoCloseable {
    private final AlarmService alarmService;
    private final ScheduledExecutorService scheduler;
    private ScheduledFuture<?> scheduledCheck;

    public AlarmMonitor(AlarmService alarmService, ScheduledExecutorService scheduler) {
        this.alarmService = alarmService;
        this.scheduler = scheduler;
    }

    public synchronized void start() {
        if (scheduledCheck != null && !scheduledCheck.isCancelled() && !scheduledCheck.isDone()) return;
        scheduledCheck = scheduler.scheduleAtFixedRate(this::safeCheck, 0L, 1L, TimeUnit.MINUTES);
    }

    public synchronized void stop() {
        if (scheduledCheck != null) {
            scheduledCheck.cancel(false);
            scheduledCheck = null;
        }
    }

    public synchronized boolean isRunning() {
        return scheduledCheck != null && !scheduledCheck.isCancelled() && !scheduledCheck.isDone();
    }

    private void safeCheck() {
        try {
            alarmService.checkDueAlarms();
            if (!alarmService.hasMonitorableAlarms()) stop();
        } catch (RuntimeException failure) {
            System.err.printf("Alarm monitor poll failed: %s%n", failure.getMessage());
        }
    }

    @Override
    public synchronized void close() {
        stop();
        scheduler.shutdown();
    }
}
