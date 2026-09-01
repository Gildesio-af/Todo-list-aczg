package service

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll
import zg.acelera.domain.Category
import zg.acelera.domain.Task
import zg.acelera.domain.enums.Status
import zg.acelera.dto.TaskDTO
import zg.acelera.repository.TaskRepository
import zg.acelera.service.TaskService

import java.time.LocalDateTime

class TaskServiceSpec extends Specification{

    TaskService taskService
    TaskRepository taskRepository
    TaskDTO createDTO

    @Shared
    LocalDateTime startDate = LocalDateTime.now()

    @Shared
    LocalDateTime endDate = LocalDateTime.now().plusDays(2)

    @Shared
    Category category = new Category("ACZG")

    @Shared
    Task task = Task.builder()
            .name("Test Task")
            .description("This is a test task")
            .priority(1)
            .status(Status.TODO)
            .startDate(startDate)
            .endDate(endDate)
            .category(category)
            .build()

    @Shared
    List<Task> tasks = [task]

    @Shared
    List<Task> tasksByStatus1 = [
            task,
            new Task("Another Task", "Another test task", 2, Status.DOING, startDate, endDate, category)
    ]

    @Shared
    List<Task> tasksByStatus2 = [
            task,
            new Task("Another Task", "Another test task", 2, Status.DOING, startDate, endDate, category),
            new Task("Yet Another Task", "Yet another test task", 3, Status.DONE, startDate, endDate, category)
    ]

    def setup() {
        taskRepository = Mock()
        taskService = new TaskService(taskRepository)
        category = new Category("ACZG")

        createDTO = TaskDTO.builder()
                .name("Test Task")
                .description("This is a test task")
                .priority(1)
                .status("TODO")
                .startDate(startDate)
                .endDate(endDate)
                .category("ACZG")
                .build()
    }

    def "createTask should return a task when repository create a task successfully"() {
        when: "creating a new task"
        Task result = taskService.createTask(createDTO)

        then: "the task should be created successfully"
        1 * taskRepository.save({it.name == "Test Task"}) >> task
        result != null
        result.name == "Test Task"
    }

    def "createTask should throw an exception when save fails in repository"() {
        given:
            taskRepository.save(_) >> {throw new IOException()}

        when: "creating a new task"
            Task result = taskService.createTask(createDTO)

        then: "service should throw a RuntimeException"
           RuntimeException e = thrown(RuntimeException)
            e.getCause() instanceof IOException
    }

    def "listAllTasks should return a list of tasks when repository returns tasks successfully"() {
        given: "a list of tasks in the repository"
            taskRepository.findAll() >> tasks

        when: "listing all tasks"
            List<Task> result = taskService.listAllTasks()

        then: "the list should contain the expected tasks"
            result.size() == 1
            result[0].name == "Test Task"
    }

    def "listAllTasks should return an empty list when repository returns no tasks"() {
        given: "an empty list of tasks in the repository"
            taskRepository.findAll() >> []

        when: "listing all tasks"
            List<Task> result = taskService.listAllTasks()

        then: "the list should be empty"
            result.isEmpty()
    }

    def "listAllTasks should throw an exception when findAll fails in repository"() {
        given: "repository throws an exception"
            taskRepository.findAll() >> { throw new IOException() }

        when: "listing all tasks"
            taskService.listAllTasks()

        then: "service should throw a RuntimeException"
            RuntimeException e = thrown(RuntimeException)
            e.getCause() instanceof IOException
    }

    def "listTasksByCategory should call a method in the repository and return a list of tasks"() {
        given: "a category"
            String category = "ACZG"

        when: "listing tasks by category"
            List<Task> result = taskService.listTasksByCategory(category)

        then: "the repository method should be called"
            1 * taskRepository.findByCategory(_) >> tasks
            result.size() == 1
            result[0].name == "Test Task"
    }

    def "listTasksByCategory should throw an exception when repository method fails"() {
        given: "repository throws an exception"
            taskRepository.findByCategory("ACZG") >> { throw new IOException() }

        when: "listing tasks by category"
            taskService.listTasksByCategory("ACZG")

        then: "service should throw a RuntimeException"
            RuntimeException e = thrown(RuntimeException)
            e.getCause() instanceof IOException
    }

    def "listTasksOrderedByPriority should call a method in the repository and return a list of tasks"() {
        when: "listing tasks ordered by priority"
            List<Task> result = taskService.listTasksOrderedByPriority()

        then: "the repository method should be called"
            1 * taskRepository.findOrderedByPriority() >> tasks
            result.size() == 1
            result[0].name == "Test Task"
    }

    def "listTasksOrderedByPriority should throw an exception when repository method fails"() {
        given: "repository throws an exception"
            taskRepository.findOrderedByPriority() >> { throw new IOException() }

        when: "listing tasks ordered by priority"
            taskService.listTasksOrderedByPriority()

        then: "service should throw a RuntimeException"
            RuntimeException e = thrown(RuntimeException)
            e.getCause() instanceof IOException
    }

    def "listTasksByStatus should call a method in the repository and return a list of tasks"() {
        given: "a status"
            String status = "TODO"

        when: "listing tasks by status"
            List<Task> result = taskService.listTasksByStatus(status)

        then: "the repository method should be called"
            1 * taskRepository.findByStatus(_) >> tasks
            result.size() == 1
            result[0].name == "Test Task"
            result[0].status == Status.TODO
    }

    def "listTasksByStatus should throw an exception when repository method fails"() {
        given: "repository throws an exception"
            taskRepository.findByStatus("TODO") >> { throw new IOException() }

        when: "listing tasks by status"
            taskService.listTasksByStatus("TODO")

        then: "service should throw a RuntimeException"
            RuntimeException e = thrown(RuntimeException)
            e.getCause() instanceof IOException
    }

    @Unroll
    def "countTasksByStatus should return a string with the count of tasks by status"() {
        given: "the repository returns a list of tasks"
        taskRepository.findAll() >> returnList

        when: "counting tasks by status"
        String result = taskService.countTasksByStatus()

        then: "the result should be a string with the count"
        result == expectedResult

        where:
        returnList     || expectedResult
        tasks          || "TODO: 1 | DOING: 0 | DONE: 0"
        tasksByStatus1 || "TODO: 1 | DOING: 1 | DONE: 0"
        tasksByStatus2 || "TODO: 1 | DOING: 1 | DONE: 1"
    }

    def "countTasksByStatus should throw an exception when repository method fails"() {
        given:
        taskRepository.findAll() >> { throw new IOException() }

        when: "counting tasks by status"
        taskService.countTasksByStatus()

        then: "service should throw a RuntimeException"
        RuntimeException e = thrown(RuntimeException)
        e.getCause() instanceof IOException
    }

    def "deleteTask should call the repository method and delete the task when it exists"() {
        given:
        taskRepository.findAll() >> tasks
        when:
        boolean result = taskService.deleteTask("Test Task")

        then: "the repository method should be called"
        1 * taskRepository.delete(task) >> true
        result
    }

    def "deleteTask should throw an exception when the repository method fails"() {
        given:
        taskRepository.findAll() >> tasks

        when:
        taskService.deleteTask("Test Task")

        then: "service should throw a RuntimeException"
        1 * taskRepository.delete(task) >> { throw new IOException() }
        RuntimeException e = thrown(RuntimeException)
        e.getCause() instanceof IOException
    }

    def "deleteTask should return false when the task does not exist"() {
        given:
        taskRepository.findAll() >> []

        when: "deleting a non-existent task"
        boolean result = taskService.deleteTask("Non-existent Task")

        then: "the result should be false"
        !result
    }
}
