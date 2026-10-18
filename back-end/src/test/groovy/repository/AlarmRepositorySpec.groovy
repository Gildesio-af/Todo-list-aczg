package repository

import spock.lang.Specification
import zg.acelera.domain.Alarm
import zg.acelera.repository.AlarmRepositoryImpl

import java.nio.file.Files
import java.time.LocalDateTime

class AlarmRepositorySpec extends Specification {
    def "round trips the semicolon schema and nullable timestamps"() {
        given:
        def path = Files.createTempFile("alarms", ".csv")
        def repository = new AlarmRepositoryImpl(path)
        def alarm = Alarm.builder().id("a1").taskId("task").advanceMinutes(5)
                .triggerAt(LocalDateTime.of(2030, 1, 1, 10, 0)).primary(true).build()

        when:
        repository.save(alarm)

        then:
        repository.findById("a1") == alarm
        Files.readString(path).startsWith("a1;task;5;2030-01-01T10:00;true;false;;")
    }

    def "rejects malformed records"() {
        given:
        def path = Files.createTempFile("alarms", ".csv")
        Files.writeString(path, "bad;record")
        def repository = new AlarmRepositoryImpl(path)

        when:
        repository.findAll()

        then:
        thrown(RuntimeException)
    }
}
