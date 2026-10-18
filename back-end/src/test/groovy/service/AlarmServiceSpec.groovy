package service

import spock.lang.Specification
import zg.acelera.domain.Category
import zg.acelera.domain.Task
import zg.acelera.domain.enums.Status
import zg.acelera.dto.AlarmDTO
import zg.acelera.repository.AlarmRepositoryImpl
import zg.acelera.repository.AlarmRepository
import zg.acelera.repository.TaskRepository
import zg.acelera.service.AlarmService
import zg.acelera.util.AlarmNotifier
import zg.acelera.domain.Alarm

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

    def "restores the previous group when create persistence fails"() {
        given:
        def task = new Task("task", "description", 1, Status.TODO,
                LocalDateTime.of(2030, 1, 1, 8, 0),
                LocalDateTime.of(2030, 1, 1, 12, 0), new Category("work"))
        def existing = Alarm.builder().id("existing").taskId("task").advanceMinutes(30)
                .triggerAt(LocalDateTime.of(2030, 1, 1, 11, 30)).primary(true).build()
        def repository = new FailingAlarmRepository([existing], true)
        def tasks = Stub(TaskRepository) { findAll() >> [task] }
        def service = new AlarmService(repository, tasks,
                Clock.fixed(Instant.parse("2030-01-01T07:00:00Z"), ZoneOffset.UTC),
                { a, b, c -> } as AlarmNotifier)

        when:
        service.createAlarm(new AlarmDTO("task", 1, 0))

        then:
        thrown(RuntimeException)
        repository.alarms == [existing]
        existing.primary
    }

    def "leaves the previous alarm unchanged when edit persistence fails"() {
        given:
        def task = new Task("task", "description", 1, Status.TODO,
                LocalDateTime.of(2030, 1, 1, 8, 0),
                LocalDateTime.of(2030, 1, 1, 12, 0), new Category("work"))
        def existing = Alarm.builder().id("existing").taskId("task").advanceMinutes(30)
                .triggerAt(LocalDateTime.of(2030, 1, 1, 11, 30)).primary(true).build()
        def repository = new FailingAlarmRepository([existing], false, true)
        def tasks = Stub(TaskRepository) { findAll() >> [task] }
        def service = new AlarmService(repository, tasks,
                Clock.fixed(Instant.parse("2030-01-01T07:00:00Z"), ZoneOffset.UTC),
                { a, b, c -> } as AlarmNotifier)

        when:
        service.updateAlarm("existing", new AlarmDTO("task", 1, 0))

        then:
        thrown(RuntimeException)
        repository.alarms == [existing]
        existing.advanceMinutes == 30
        existing.primary
    }

    private static class FailingAlarmRepository implements AlarmRepository {
        List<Alarm> alarms
        boolean failOnSave
        boolean failOnUpdate

        FailingAlarmRepository(List<Alarm> alarms, boolean failOnSave, boolean failOnUpdate = false) {
            this.alarms = alarms
            this.failOnSave = failOnSave
            this.failOnUpdate = failOnUpdate
        }

        List<Alarm> findAll() { alarms }
        List<Alarm> findByTask(String taskId) {
            alarms.findAll { it.taskId.equalsIgnoreCase(taskId) }.collect { copy(it) }
        }
        Alarm findById(String id) { alarms.find { it.id == id } }
        Alarm save(Alarm alarm) {
            if (failOnSave) throw new RuntimeException("write failed")
            alarms << alarm
            alarm
        }
        Alarm update(Alarm alarm) {
            if (failOnUpdate) throw new RuntimeException("update failed")
            def index = alarms.findIndexOf { it.id == alarm.id }
            if (index >= 0) alarms[index] = alarm
            alarm
        }
        boolean delete(String id) { alarms.removeIf { it.id == id } }
        int deleteByTask(String taskId) { 0 }

        private static Alarm copy(Alarm alarm) {
            Alarm.builder().id(alarm.id).taskId(alarm.taskId).advanceMinutes(alarm.advanceMinutes)
                    .triggerAt(alarm.triggerAt).primary(alarm.primary).fired(alarm.fired)
                    .lastFiredAt(alarm.lastFiredAt).nextTriggerAt(alarm.nextTriggerAt).build()
        }
    }
}
