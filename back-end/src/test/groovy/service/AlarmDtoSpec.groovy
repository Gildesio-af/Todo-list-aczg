package service

import zg.acelera.dto.AlarmDTO
import zg.acelera.util.exception.TaskInfoWrongException
import spock.lang.Specification

class AlarmDtoSpec extends Specification {
    def "accepts a one minute advance"() {
        expect:
        new AlarmDTO("task", 0, 1).advanceMinutes() == 1
    }

    def "rejects missing task and non-positive advance"() {
        when:
        new AlarmDTO(task, hours, minutes)

        then:
        thrown(TaskInfoWrongException)

        where:
        task   | hours | minutes
        null   | 0     | 1
        "task" | 0     | 0
        "task" | -1    | 1
    }
}
