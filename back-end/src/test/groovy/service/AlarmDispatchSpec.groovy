package service

import spock.lang.Specification
import zg.acelera.domain.Alarm
import zg.acelera.domain.Task
import zg.acelera.domain.enums.Status
import zg.acelera.repository.AlarmRepository
import zg.acelera.repository.AlarmRepositoryImpl
import zg.acelera.repository.TaskRepository
import zg.acelera.service.AlarmService
import zg.acelera.util.AlarmNotifier

import java.time.LocalDateTime
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class AlarmDispatchSpec extends Specification {
    def "dispatches due TODO and DOING alarms with their persisted trigger"() {
        given:
        def path = AlarmFixtures.temporaryAlarmPath()
        def repository = new AlarmRepositoryImpl(path)
        def todo = task("todo", Status.TODO)
        def doing = task("doing", Status.DOING)
        def notifications = []
        def service = service(repository, [todo, doing], notifications)
        repository.save(alarm("todo-alarm", "todo", todo.endDate.minusMinutes(30), false))
        repository.save(alarm("doing-alarm", "doing", doing.endDate.minusMinutes(15), false))

        when:
        service.checkDueAlarms()

        then:
        notifications == [
                ["todo", "todo-alarm", todo.endDate.minusMinutes(30)],
                ["doing", "doing-alarm", doing.endDate.minusMinutes(15)]
        ]
        with(repository.findById("todo-alarm")) {
            fired
            lastFiredAt == todo.endDate.minusMinutes(30)
            nextTriggerAt == null
        }
        with(repository.findById("doing-alarm")) {
            fired
            lastFiredAt == doing.endDate.minusMinutes(15)
            nextTriggerAt == null
        }
    }

    def "does not dispatch a future trigger"() {
        given:
        def task = task("future", Status.TODO)
        def repository = new AlarmRepositoryImpl(AlarmFixtures.temporaryAlarmPath())
        def notifications = []
        repository.save(alarm("future-alarm", "future", LocalDateTime.of(2026, 10, 18, 17, 0), false))
        def service = service(repository, [task], notifications)

        when:
        service.checkDueAlarms()

        then:
        notifications.empty
        !repository.findById("future-alarm").fired
    }

    def "does not emit the same persisted one-shot trigger twice"() {
        given:
        def task = task("once", Status.TODO)
        def repository = new AlarmRepositoryImpl(AlarmFixtures.temporaryAlarmPath())
        def notifications = []
        repository.save(alarm("once-alarm", "once", task.endDate.minusMinutes(30), false))
        def service = service(repository, [task], notifications)

        when:
        service.checkDueAlarms()
        service.checkDueAlarms()

        then:
        notifications.size() == 1
        repository.findById("once-alarm").lastFiredAt == task.endDate.minusMinutes(30)
    }

    def "does not emit a DONE task or a fired one-shot restored from CSV"() {
        given:
        def done = task("done", Status.DONE)
        def active = task("active", Status.TODO)
        def repository = new AlarmRepositoryImpl(AlarmFixtures.temporaryAlarmPath())
        def notifications = []
        repository.save(alarm("done-alarm", "done", done.endDate.minusMinutes(30), false))
        repository.save(Alarm.builder()
                .id("restored-alarm").taskId("done").advanceMinutes(15)
                .triggerAt(done.endDate.minusMinutes(15)).fired(true)
                .lastFiredAt(done.endDate.minusMinutes(15)).build())
        repository.save(Alarm.builder()
                .id("active-restored-alarm").taskId("active").advanceMinutes(15)
                .triggerAt(active.endDate.minusMinutes(15)).fired(true)
                .lastFiredAt(active.endDate.minusMinutes(15)).build())
        def service = service(repository, [done, active], notifications)

        when:
        service.checkDueAlarms()

        then:
        notifications.empty
        !repository.findById("done-alarm").fired
        repository.findById("restored-alarm").fired
        repository.findById("active-restored-alarm").fired
    }

    def "does not notify when dispatch state cannot be persisted"() {
        given:
        def task = task("persist", Status.TODO)
        def alarm = alarm("persist-alarm", "persist", task.endDate.minusMinutes(30), false)
        def repository = Stub(AlarmRepository) {
            findAll() >> [alarm]
            update(_) >> { throw new RuntimeException("write failed") }
        }
        def notifications = []
        def service = service(repository, [task], notifications)

        when:
        service.checkDueAlarms()

        then:
        def failure = thrown(RuntimeException)
        failure.message == "write failed"
        notifications.empty
    }

    def "repeats only the primary alarm using its configured advance interval"() {
        given:
        def task = task("repeating", Status.TODO)
        def repository = new AlarmRepositoryImpl(AlarmFixtures.temporaryAlarmPath())
        def notifications = []
        repository.save(dispatchAlarm("primary", "repeating", 30, LocalDateTime.of(2026, 10, 18, 11, 30), true))
        repository.save(dispatchAlarm("secondary", "repeating", 60, LocalDateTime.of(2026, 10, 18, 11, 0), false))
        def alarmService = service(repository, [task], notifications, at("2026-10-18T12:00:00Z"))

        when:
        alarmService.checkDueAlarms()

        then:
        notifications == [
                ["repeating", "primary", LocalDateTime.of(2026, 10, 18, 11, 30)],
                ["repeating", "secondary", LocalDateTime.of(2026, 10, 18, 11, 0)]
        ]
        repository.findById("primary").nextTriggerAt == LocalDateTime.of(2026, 10, 18, 12, 0)
        repository.findById("secondary").nextTriggerAt == null

        when:
        alarmService = service(repository, [task], notifications, at("2026-10-18T12:01:00Z"))
        alarmService.checkDueAlarms()

        then:
        notifications.count { it[1] == "primary" } == 2
        notifications.count { it[1] == "secondary" } == 1
        repository.findById("primary").nextTriggerAt == LocalDateTime.of(2026, 10, 18, 12, 30)
        repository.findById("secondary").lastFiredAt == LocalDateTime.of(2026, 10, 18, 11, 0)
    }

    def "reactivates an alarm after task recalculation while preserving its identity"() {
        given:
        def originalTask = task("reactivate", Status.TODO)
        def repository = new AlarmRepositoryImpl(AlarmFixtures.temporaryAlarmPath())
        def original = Alarm.builder()
                .id("stable-id").taskId("reactivate").advanceMinutes(30)
                .triggerAt(LocalDateTime.of(2026, 10, 18, 17, 30))
                .primary(true).fired(true)
                .lastFiredAt(LocalDateTime.of(2026, 10, 18, 17, 30))
                .nextTriggerAt(LocalDateTime.of(2026, 10, 18, 18, 0))
                .build()
        repository.save(original)
        def alarmService = service(repository, [originalTask], [], AlarmFixtures.fixedClock())
        def editedTask = task("reactivate", Status.TODO)
        editedTask.endDate = LocalDateTime.of(2026, 10, 19, 18, 0)

        when:
        alarmService.recalculateForTask(editedTask)

        then:
        with(repository.findById("stable-id")) {
            id == "stable-id"
            triggerAt == LocalDateTime.of(2026, 10, 19, 17, 30)
            !fired
            lastFiredAt == null
            nextTriggerAt == null
        }
    }

    private AlarmService service(AlarmRepository repository, List<Task> tasks, List<List> notifications,
                                Clock clock = AlarmFixtures.fixedClock()) {
        def taskRepository = Stub(TaskRepository) {
            findAll() >> tasks
        }
        AlarmNotifier notifier = { taskId, alarmId, triggerAt ->
            notifications << [taskId, alarmId, triggerAt]
        }
        new AlarmService(repository, taskRepository, clock, notifier)
    }

    private static Clock at(String instant) {
        Clock.fixed(Instant.parse(instant), ZoneOffset.UTC)
    }

    private static Task task(String name, Status status) {
        Task.builder()
                .name(name)
                .description("dispatch test")
                .priority(1)
                .status(status)
                .startDate(LocalDateTime.of(2026, 10, 18, 8, 0))
                .endDate(LocalDateTime.of(2026, 10, 18, 10, 0))
                .build()
    }

    private static Alarm alarm(String id, String taskId, LocalDateTime triggerAt, boolean primary) {
        Alarm.builder()
                .id(id)
                .taskId(taskId)
                .advanceMinutes((int) java.time.Duration.between(triggerAt, LocalDateTime.of(2026, 10, 18, 10, 0)).toMinutes())
                .triggerAt(triggerAt)
                .primary(primary)
                .build()
    }

    private static Alarm dispatchAlarm(String id, String taskId, int advanceMinutes,
                                       LocalDateTime triggerAt, boolean primary) {
        Alarm.builder()
                .id(id)
                .taskId(taskId)
                .advanceMinutes(advanceMinutes)
                .triggerAt(triggerAt)
                .primary(primary)
                .build()
    }
}
