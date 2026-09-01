package controller

import spock.lang.Specification
import spock.lang.Unroll
import zg.acelera.controller.TaskController
import zg.acelera.domain.Task
import zg.acelera.dto.TaskDTO
import zg.acelera.service.TaskService
import zg.acelera.util.interface_user.TaskInterface

class TaskControllerSpec extends Specification {

    TaskDTO taskDTO
    TaskController controller
    TaskService taskService
    TaskInterface taskInterface

    def setup() {
        taskService = Mock(TaskService)
        taskInterface = Mock(TaskInterface)
        controller = new TaskController(taskService, taskInterface)

        taskDTO = TaskDTO.builder()
                .name("Test Task")
                .description("This is a test task.")
                .category("WORK")
                .priority(1)
                .status("TODO")
                .build()
    }

    def "createTask should show success message when task is created"() {
        given: "a valid mock Task"
        Task mockTask = Mock(Task)

        when: "createTask is called"
        controller.createTask(taskDTO)

        then: "service creates task and success message is shown"
        1 * taskService.createTask(taskDTO) >> mockTask
        1 * mockTask.getName() >> "Test Task"
        1 * taskInterface.showMessage("Task created successfully: Test Task")
    }

    def "createTask should show error message when service throws exception"() {
        when: "createTask is called and fails"
        controller.createTask(taskDTO)

        then: "exception is caught and error message is shown"
        1 * taskService.createTask(taskDTO) >> { throw new RuntimeException("DB error") }
        1 * taskInterface.showMessage("Error creating task: DB error")
    }

    @Unroll
    def "deleteTask should show error when task name is '#invalidName'"() {
        when: "deleteTask is called with invalid name"
        controller.deleteTask(invalidName)

        then: "service is not called and error is shown"
        0 * taskService.deleteTask(_)
        1 * taskInterface.showMessage("Task name cannot be empty.")

        where:
        invalidName << [null, "", "   "]
    }

    def "deleteTask should show success message when task is deleted"() {
        when: "deleteTask is called with existing task"
        controller.deleteTask("  My Task  ")

        then: "service deletes and success message is shown"
        1 * taskService.deleteTask("My Task") >> true
        1 * taskInterface.showMessage("Task '  My Task  ' deleted successfully.")
    }

    def "deleteTask should show not found message when task does not exist"() {
        when: "deleteTask is called with non-existing task"
        controller.deleteTask("Unknown")

        then: "service returns false and not found message is shown"
        1 * taskService.deleteTask("Unknown") >> false
        1 * taskInterface.showMessage("Task 'Unknown' not found.")
    }

    def "listAllTasks should print header and tasks when list is not empty"() {
        given: "a list with one mock task"
        Task mockTask = Mock(Task)

        when: "listAllTasks is called"
        controller.listAllTasks()

        then: "header and task string are printed"
        1 * taskService.listAllTasks() >> [mockTask]
        1 * taskInterface.showMessage("--- All Tasks ---")
        1 * mockTask.toString() >> "Task Details 1"
        1 * taskInterface.showMessage("Task Details 1")
    }

    def "listAllTasks should print empty message when list is null or empty"() {
        when: "listAllTasks is called"
        controller.listAllTasks()

        then: "header and not found message are printed"
        1 * taskService.listAllTasks() >> []
        1 * taskInterface.showMessage("--- All Tasks ---")
        1 * taskInterface.showMessage("No tasks found.")
    }

    @Unroll
    def "listTasksByCategory should show error when category is '#invalidCategory'"() {
        when: "listTasksByCategory is called with invalid category"
        controller.listTasksByCategory(invalidCategory)

        then: "service is not called and error is shown"
        0 * taskService.listTasksByCategory(_)
        1 * taskInterface.showMessage("Category cannot be empty.")

        where:
        invalidCategory << [null, "", "   "]
    }

    def "listTasksByCategory should print header and tasks when valid"() {
        given: "a list with one mock task"
        Task mockTask = Mock(Task)

        when: "listTasksByCategory is called"
        controller.listTasksByCategory("  Work  ")

        then: "header with category and tasks are printed"
        1 * taskService.listTasksByCategory("Work") >> [mockTask]
        1 * taskInterface.showMessage("--- Tasks in Category:   Work   ---")
        1 * mockTask.toString() >> "Work Task 1"
        1 * taskInterface.showMessage("Work Task 1")
    }

    def "listTasksOrderedByPriority should print header and tasks"() {
        given: "a list with one mock task"
        Task mockTask = Mock(Task)

        when: "listTasksOrderedByPriority is called"
        controller.listTasksOrderedByPriority()

        then: "header and tasks are printed"
        1 * taskService.listTasksOrderedByPriority() >> [mockTask]
        1 * taskInterface.showMessage("--- Tasks Ordered by Priority (1 to 5) ---")
        1 * mockTask.toString() >> "High Priority Task"
        1 * taskInterface.showMessage("High Priority Task")
    }

    @Unroll
    def "listTasksByStatus should show error when status is '#invalidStatus'"() {
        when: "listTasksByStatus is called with invalid status"
        controller.listTasksByStatus(invalidStatus)

        then: "service is not called and error is shown"
        0 * taskService.listTasksByStatus(_)
        1 * taskInterface.showMessage("Status cannot be empty.")

        where:
        invalidStatus << [null, "", "   "]
    }

    def "listTasksByStatus should print header and tasks when valid"() {
        given: "a list with one mock task"
        Task mockTask = Mock(Task)

        when: "listTasksByStatus is called"
        controller.listTasksByStatus(" todo ")

        then: "header with uppercase status and tasks are printed"
        1 * taskService.listTasksByStatus("todo") >> [mockTask]
        1 * taskInterface.showMessage("--- Tasks with Status:  TODO  ---")
        1 * mockTask.toString() >> "Todo Task 1"
        1 * taskInterface.showMessage("Todo Task 1")
    }

    def "showTaskCounts should print header and count string"() {
        when: "showTaskCounts is called"
        controller.showTaskCounts()

        then: "header and counts from service are printed"
        1 * taskInterface.showMessage("--- Task Dashboard ---")
        1 * taskService.countTasksByStatus() >> "TODO: 1 | DOING: 2"
        1 * taskInterface.showMessage("TODO: 1 | DOING: 2")
    }
}
