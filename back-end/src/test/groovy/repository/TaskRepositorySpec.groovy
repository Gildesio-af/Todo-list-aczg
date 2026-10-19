package repository

import spock.lang.Specification
import spock.lang.TempDir
import zg.acelera.domain.Category
import zg.acelera.domain.Task
import zg.acelera.domain.enums.Status
import zg.acelera.repository.TaskRepositoryImpl

import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDateTime

class TaskRepositorySpec extends Specification{
    @TempDir
    Path tempDir

    TaskRepositoryImpl repository
    List<String> lines
    Path filePath

    def setup() {
        filePath = tempDir.resolve("tasks_test.csv")

        repository = new TaskRepositoryImpl(filePath)
        lines = List.of("Java 2;Quase terminando;2;DOING;2026-08-15T12:00;2026-08-16T12:00;ACZG",
                "Java 3;Quase lá;1;DONE;2026-08-15T12:00;2026-08-16T12:00;ACZG",
                "Java 4;Quase lá;3;DONE;2026-08-15T12:00;2026-08-16T12:00;OTHER")
    }

    def "findAll should return all tasks"() {
        given: "Three tasks in the file"
        Files.write(filePath, lines)

        when: "findAll is called"
        List<Task> tasks = repository.findAll()

        then: "All tasks should be returned and data should be correct"
        tasks.size() == 3
        tasks[0].name == "Java 2"
        tasks[0].description == "Quase terminando"
        tasks[0].priority == 2
        tasks[0].status == Status.DOING
        tasks[0].startDate == LocalDateTime.parse("2026-08-15T12:00")
        tasks[0].endDate == LocalDateTime.parse("2026-08-16T12:00")
        tasks[0].category.name == "ACZG"
    }

    def "findAll should return an empty list when no tasks are present"() {
        given: "An empty file"
        Files.writeString(filePath, "")

        when: "findAll is called with no tasks"
        List<Task> tasks = repository.findAll()

        then: "An empty list should be returned"
        tasks.isEmpty()
    }

    def "findByCategory should return tasks for a specific category"() {
        given: "Three tasks in the file, two with a specific category"
        Files.write(filePath, lines)

        when: "findByCategory is called with the specific category"
        List<Task> tasks = repository.findByCategory("ACZG")

        then: "Only tasks with the specified category should be returned"
        tasks.size() == 2
        tasks[0].name == "Java 2"
        tasks[0].category.name == "ACZG"
        tasks[1].name == "Java 3"
        tasks[1].category.name == "ACZG"
    }

    def "findByCategory should return an empty list when no tasks are found for the category"() {
        given: "A file with tasks but no tasks for the specified category"

        Files.write(filePath, lines)

        when: "findByCategory is called with a category that has no tasks"
        List<Task> tasks = repository.findByCategory("ANOTHER ONE")

        then: "An empty list should be returned"
        tasks.isEmpty()
    }

    def "findOrderedByPriority should return tasks ordered by priority"() {
        given: "Three tasks in the file with different priorities"
        Files.write(filePath, lines)

        when: "findOrderedByPriority is called"
        List<Task> tasks = repository.findOrderedByPriority()

        then: "Tasks should be returned in order of priority"
        tasks.size() == 3
        tasks[0].name == "Java 3"
        tasks[1].name == "Java 2"
        tasks[2].name == "Java 4"
    }

    def "findOrderedByPriority should return an empty list when no tasks are present"() {
        when: "findOrderedByPriority is called with no tasks"
        List<Task> tasks = repository.findOrderedByPriority()

        then: "An empty list should be returned"
        tasks.isEmpty()
    }

    def "findByStatus should return tasks for a specific status"() {
        given: "Three tasks in the file, two with a specific status"
        Files.write(filePath, lines)

        when: "findByStatus is called with the specific status"
        List<Task> tasks = repository.findByStatus("DONE")

        then: "Only tasks with the specified status should be returned"
        tasks.size() == 2
        tasks[0].name == "Java 3"
        tasks[0].status == Status.DONE
        tasks[1].name == "Java 4"
        tasks[1].status == Status.DONE
    }

    def "findByStatus should return an empty list when no tasks are found for the status"() {
        given: "A file with tasks but no tasks for the specified status"
        Files.write(filePath, lines)

        when: "findByStatus is called with a status that has no tasks"
        List<Task> tasks = repository.findByStatus("TODO")

        then: "An empty list should be returned"
        tasks.isEmpty()
    }

    def "save should save a task to the file"() {
        given: "A new task to be saved"
        LocalDateTime startDate = LocalDateTime.now()
        Task task = Task.builder()
        .name("Java 5")
        .description("Mais um teste")
        .priority(1)
        .status(Status.TODO)
        .startDate(startDate)
        .endDate(startDate.plusDays(1))
        .category(new Category("ACZG"))
        .build()

        when: "save is called with the new task"
        Task newTask = repository.save(task)

        then: "The task should be saved to the file with correct data"
        Files.lines(filePath).count() == 1

        newTask.name == "Java 5"
        newTask.description == "Mais um teste"
        newTask.priority == 1
        newTask.status == Status.TODO
        newTask.startDate == startDate
        newTask.endDate == startDate.plusDays(1)
        newTask.category.name == "ACZG"
    }

    def "update should update a task in the file"() {
        given: "a file with three tasks and a task to update and a task to update"
        Files.write(filePath, lines)
        LocalDateTime startDate = LocalDateTime.now();
        Task task = Task.builder()
                .name("Java 5")
                .description("Mais um teste")
                .priority(1)
                .status(Status.TODO)
                .startDate(startDate)
                .endDate(startDate.plusDays(1))
                .category(new Category("ACZG 2"))
                .build()

        when: "update is called with the task to update"
        Task updatedTask = repository.update("Java 4", task)

        then: "The task should be updated in the file"
        updatedTask.name == "Java 5"
        updatedTask.description == "Mais um teste"
    }

    def "update should return null when task was not found"() {
        given: "a file with three tasks and a task to update"
        Files.write(filePath, lines)

        Task task = Task.builder()
                .name("Java 5")
                .description("Mais um teste")
                .priority(1)
                .status(Status.TODO)
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(1))
                .category(new Category("ACZG 2"))
                .build()

        when: "update is called with a non-existing task name"
        Task updatedTask = repository.update("Non Existing Task", task)

        then: "The task should not be updated and null should be returned"
        updatedTask == null
    }

    def "delete should remove a task from the file when task exists"() {
        given: "a file with three tasks an existing task to delete"
        Files.write(filePath, lines)

        Task task = Task.builder()
        .name("Java 3")
        .description("Quase lá")
        .build()

        when: "delete is called with the existing task"
        repository.delete(task)

        then: "line should be removed from the file"
        Files.lines(filePath).count() == 2
    }

    def "delete should not remove a task from the file when task does not exist"() {
        given: "a file with three tasks and a non-existing task to delete"
        Files.write(filePath, lines)

        Task task = Task.builder()
        .name("Java 5")
        .description("Task not in file")
        .build()

        when: "delete is called with the non-existing task"
        repository.delete(task)

        then: "no line should be removed from the file"
        Files.lines(filePath).count() == 3
    }
}
