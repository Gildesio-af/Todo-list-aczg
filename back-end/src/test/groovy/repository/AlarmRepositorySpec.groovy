package repository

import spock.lang.Specification
import zg.acelera.domain.Alarm
import zg.acelera.repository.AlarmRepositoryImpl
import zg.acelera.util.exception.AlarmPersistenceException

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

    def "reports the original cause when the storage file is missing"() {
        given:
        def path = Files.createTempFile("alarms", ".csv")
        def repository = new AlarmRepositoryImpl(path)
        Files.delete(path)

        when:
        repository.findAll()

        then:
        def failure = thrown(AlarmPersistenceException)
        failure.cause instanceof java.nio.file.NoSuchFileException
    }

    def "reports unreadable storage failures"() {
        given:
        def directory = Files.createTempDirectory("alarms")
        def repository = new AlarmRepositoryImpl(directory)

        when:
        repository.findAll()

        then:
        def failure = thrown(AlarmPersistenceException)
        failure.cause instanceof java.io.IOException
    }

    def "reports write failures and preserves the original cause"() {
        given:
        def path = Files.createTempFile("alarms", ".csv")
        def repository = new AlarmRepositoryImpl(path)
        Files.delete(path)
        Files.createDirectory(path)
        def alarm = Alarm.builder().id("a1").taskId("task").advanceMinutes(5)
                .triggerAt(LocalDateTime.of(2030, 1, 1, 10, 0)).build()

        when:
        repository.save(alarm)

        then:
        def failure = thrown(AlarmPersistenceException)
        failure.cause instanceof java.io.IOException
    }

    def "rejects malformed typed and nullable fields while preserving the parse cause"() {
        given:
        def path = Files.createTempFile("alarms", ".csv")
        Files.writeString(path, record)
        def repository = new AlarmRepositoryImpl(path)

        when:
        repository.findAll()

        then:
        def failure = thrown(AlarmPersistenceException)
        expectedCause.isInstance(failure.cause)

        where:
        record                                                                                 | expectedCause
        "a1;task;5;2030-01-01T10:00;not-bool;false;;"                                      | IllegalArgumentException
        "a1;task;not-int;2030-01-01T10:00;true;false;;"                                     | NumberFormatException
        "a1;task;5;not-a-timestamp;true;false;;"                                             | java.time.format.DateTimeParseException
        "a1;task;5;2030-01-01T10:00;true;false;not-a-timestamp;"                             | java.time.format.DateTimeParseException
        "a1;task;5;2030-01-01T10:00;true;false;;not-a-timestamp"                             | java.time.format.DateTimeParseException
    }

    def "rejects records with an exact field count"() {
        given:
        def path = Files.createTempFile("alarms", ".csv")
        Files.writeString(path, "a1;task;5;2030-01-01T10:00;true;false;")
        def repository = new AlarmRepositoryImpl(path)

        when:
        repository.findAll()

        then:
        def failure = thrown(AlarmPersistenceException)
        failure.cause instanceof IllegalArgumentException
    }
}
