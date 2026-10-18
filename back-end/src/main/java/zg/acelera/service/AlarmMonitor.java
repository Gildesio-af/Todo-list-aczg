package zg.acelera.service;

import java.time.Clock;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class AlarmMonitor implements AutoCloseable {
    private final AlarmService alarmService;
    private final ScheduledExecutorService scheduler;
    private final Clock clock;
    private ScheduledFuture<?> scheduledCheck;

    public AlarmMonitor(AlarmService alarmService, ScheduledExecutorService scheduler, Clock clock) {
        this.alarmService = alarmService;
        this.scheduler = scheduler;
        this.clock = clock;
    }

    public synchronized void start() {
        if (scheduledCheck != null && !scheduledCheck.isCancelled() && !scheduledCheck.isDone()) return;
        scheduledCheck = scheduler.scheduleAtFixedRate(this::safeCheck, 0, 1, TimeUnit.MINUTES);
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
        } catch (RuntimeException ignored) {
            // A failed poll must not cancel subsequent scheduled checks.
        }
    }

    @Override
    public synchronized void close() {
        stop();
        scheduler.shutdown();
    }
}
