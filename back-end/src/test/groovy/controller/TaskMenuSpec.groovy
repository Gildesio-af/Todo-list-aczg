package controller

import spock.lang.Specification
import zg.acelera.app.CategoryMenu
import zg.acelera.app.TaskMenu
import zg.acelera.controller.AlarmController
import zg.acelera.controller.TaskController
import zg.acelera.domain.Task
import zg.acelera.dto.AlarmDTO
import zg.acelera.util.interface_user.TaskInterface

class TaskMenuSpec extends Specification {

    TaskController taskController = Mock(TaskController, constructorArgs: [null, null])
    TaskInterface taskInterface = Mock(TaskInterface)
    AlarmController alarmController = Mock(AlarmController, constructorArgs: [null, null])

    def "alarm create flow builds a complete DTO and delegates once"() {
        given:
        def menu = new TaskMenu(taskController, taskInterface, null, alarmController)
        taskInterface.readInteger() >>> [9, 1, 2, 5, 0]
        taskInterface.readText() >> "Task"

        when:
        menu.start()

        then:
        1 * alarmController.createAlarm({ AlarmDTO dto ->
            dto.taskId() == "Task" && dto.hours() == 2 && dto.minutes() == 5
        })
        1 * taskInterface.showMessage("Returning to Main Menu...")
    }

    def "invalid alarm input is reported once without reaching the controller"() {
        given:
        def menu = new TaskMenu(taskController, taskInterface, null, alarmController)
        taskInterface.readInteger() >>> [9, 1, null, 0]
        taskInterface.readText() >> "Task"

        when:
        menu.start()

        then:
        0 * alarmController._
        1 * taskInterface.showMessage("Invalid alarm input.")
        1 * taskInterface.showMessage("Returning to Main Menu...")
    }

    def "existing task flows remain delegated for filters, priority and dashboard"() {
        given:
        def menu = new TaskMenu(taskController, taskInterface, null, alarmController)
        def task = Mock(Task)
        taskInterface.readInteger() >>> [4, 5, 1, 6, 0]
        taskInterface.readText() >> "Work"

        when:
        menu.start()

        then:
        1 * taskController.listTasksOrderedByPriority()
        1 * taskController.listTasksByCategory("Work")
        1 * taskController.showTaskCounts()
        1 * taskInterface.showMessage("Returning to Main Menu...")
        0 * alarmController._
    }
}
