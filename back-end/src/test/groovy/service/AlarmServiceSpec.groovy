package service

import spock.lang.Specification
import zg.acelera.domain.Category
import zg.acelera.domain.Task
import zg.acelera.domain.enums.Status
import zg.acelera.dto.AlarmDTO
import zg.acelera.repository.AlarmRepositoryImpl
import zg.acelera.repository.TaskRepository
import zg.acelera.service.AlarmService
import zg.acelera.util.AlarmNotifier

import java.nio.file.Files
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

class AlarmServiceSpec extends Specification {
    def "calculates trigger from task end and rejects a fourth alarm"() {
        given:
        def task = new Task("task", "description", 1, Status.TODO,
                java.time.LocalDateTime.of(2030, 1, 1, 8, 0),
                java.time.LocalDateTime.of(2030, 1, 1, 12, 0), new Category("work"))
        def tasks = Stub(TaskRepository) { findAll() >> [task] }
        def repository = new AlarmRepositoryImpl(Files.createTempFile("alarms", ".csv"))
        def service = new AlarmService(repository, tasks,
                Clock.fixed(Instant.parse("2030-01-01T07:00:00Z"), ZoneOffset.UTC),
                { a, b, c -> } as AlarmNotifier)

        expect:
        service.createAlarm(new AlarmDTO("task", 1, 0)).triggerAt == LocalDateTime.of(2030, 1, 1, 11, 0)

        when:
        3.times { index -> service.createAlarm(new AlarmDTO("task", 0, 30 + index)) }
        service.createAlarm(new AlarmDTO("task", 0, 1))

        then:
        thrown(IllegalArgumentException)
    }
}
