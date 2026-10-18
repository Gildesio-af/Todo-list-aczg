package controller

import spock.lang.Specification
import zg.acelera.controller.AlarmController
import zg.acelera.domain.Alarm
import zg.acelera.dto.AlarmDTO
import zg.acelera.service.AlarmMonitor
import zg.acelera.service.AlarmService
import zg.acelera.util.interface_user.TaskInterface

class AlarmControllerSpec extends Specification {

    AlarmService alarmService = Mock(AlarmService, constructorArgs: [null, null])
    TaskInterface taskInterface = Mock(TaskInterface)
    AlarmMonitor alarmMonitor = Mock(AlarmMonitor, constructorArgs: [null, null])
    AlarmController controller = new AlarmController(alarmService, taskInterface, alarmMonitor)

    def "create reports success and starts monitoring"() {
        given:
        def dto = new AlarmDTO("Task", 0, 5)
        def alarm = Alarm.builder().id("alarm-1").build()

        when:
        controller.createAlarm(dto)

        then:
        1 * alarmService.createAlarm(dto) >> alarm
        1 * alarmMonitor.start()
        1 * taskInterface.showMessage("Alarm created successfully: alarm-1")
    }

    def "edit reports persistence errors once"() {
        given:
        def dto = new AlarmDTO("Task", 0, 5)

        when:
        controller.updateAlarm("alarm-1", dto)

        then:
        1 * alarmService.updateAlarm("alarm-1", dto) >> { throw new RuntimeException("disk unavailable") }
        1 * taskInterface.showMessage("Error updating alarm: disk unavailable")
        0 * alarmMonitor._
    }

    def "list reports each alarm and handles task lookup errors"() {
        given:
        def alarm = Alarm.builder().id("alarm-1").build()

        when:
        controller.listAlarms("Task")

        then:
        1 * alarmService.listAlarms("Task") >> [alarm]
        1 * taskInterface.showMessage(alarm.toString())

        when:
        controller.listAlarms("missing")

        then:
        1 * alarmService.listAlarms("missing") >> { throw new IllegalArgumentException("Task not found: missing") }
        1 * taskInterface.showMessage("Error listing alarms: Task not found: missing")
    }

    def "remove reports not found and persistence error outcomes"() {
        when:
        controller.removeAlarm("missing")

        then:
        1 * alarmService.removeAlarm("missing") >> false
        1 * taskInterface.showMessage("Alarm 'missing' not found.")

        when:
        controller.removeAlarm("alarm-1")

        then:
        1 * alarmService.removeAlarm("alarm-1") >> { throw new RuntimeException("write failed") }
        1 * taskInterface.showMessage("Error removing alarm: write failed")
    }

    def "create reports validation and task-not-found errors"() {
        given:
        def dto = new AlarmDTO("missing", 0, 5)

        when:
        controller.createAlarm(dto)

        then:
        1 * alarmService.createAlarm(dto) >> { throw new IllegalArgumentException("Task not found: missing") }
        1 * taskInterface.showMessage("Error creating alarm: Task not found: missing")

        when:
        controller.createAlarm(dto)

        then:
        1 * alarmService.createAlarm(dto) >> { throw new IllegalArgumentException("A task cannot have more than three alarms.") }
        1 * taskInterface.showMessage("Error creating alarm: A task cannot have more than three alarms.")
    }
}
