package service

import zg.acelera.domain.Category
import zg.acelera.domain.Task
import zg.acelera.domain.enums.Status

import java.nio.file.Files
import java.nio.file.Path
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

class AlarmFixtures {
    static final Instant FIXED_INSTANT = Instant.parse('2026-10-18T12:00:00Z')
    static final ZoneOffset FIXED_OFFSET = ZoneOffset.UTC

    static Task todoTask() {
        taskWithStatus(Status.TODO)
    }

    static Task doingTask() {
        taskWithStatus(Status.DOING)
    }

    static Task doneTask() {
        taskWithStatus(Status.DONE)
    }

    static Clock fixedClock() {
        Clock.fixed(FIXED_INSTANT, FIXED_OFFSET)
    }

    static Path temporaryAlarmPath() {
        Files.createTempFile('alarms-', '.csv')
    }

    private static Task taskWithStatus(Status status) {
        Task.builder()
                .name('Alarm task')
                .description('Task used by alarm specifications')
                .priority(1)
                .status(status)
                .startDate(LocalDateTime.of(2026, 10, 18, 8, 0))
                .endDate(LocalDateTime.of(2026, 10, 18, 18, 0))
                .category(new Category('ACZG'))
                .build()
    }
}
