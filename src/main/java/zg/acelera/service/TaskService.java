package zg.acelera.service;

import lombok.RequiredArgsConstructor;
import zg.acelera.domain.Category;
import zg.acelera.domain.Task;
import zg.acelera.domain.enums.Status;
import zg.acelera.dto.TaskDTO;
import zg.acelera.repository.TaskRepository;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;

    public Task createTask(TaskDTO dto) {
        try {
            Task task = new Task(
                    dto.name(),
                    dto.description(),
                    dto.priority(),
                    Status.TODO,
                    dto.startDate(),
                    dto.endDate(),
                    new Category(dto.category())
            );

            return taskRepository.save(task);
        } catch (IOException e) {
            throw new RuntimeException("Error creating task: " + e.getMessage(), e);
        }
    }

    public List<Task> listAllTasks() {
        try {
            return taskRepository.findAll();
        } catch (IOException e) {
            throw new RuntimeException("Error fetching tasks: " + e.getMessage(), e);
        }
    }

    public List<Task> listTasksByCategory(String category) {
        return taskRepository.findByCategory(category);
    }

    public List<Task> listTasksOrderedByPriority() {
        try {
            return taskRepository.findOrderedByPriority();
        } catch (IOException e) {
            throw new RuntimeException("Error fetching tasks ordered by priority: " + e.getMessage(), e);
        }
    }

    public List<Task> listTasksByStatus(String status) {
        try {
            return taskRepository.findByStatus(status);
        } catch (IOException e) {
            throw new RuntimeException("Error fetching tasks by status: " + e.getMessage(), e);
        }
    }

    public String countTasksByStatus() {
        try {
            List<Task> allTasks = taskRepository.findAll();

            long todo = allTasks.stream().filter(t -> t.getStatus() == Status.TODO).count();
            long doing = allTasks.stream().filter(t -> t.getStatus() == Status.DOING).count();
            long done = allTasks.stream().filter(t -> t.getStatus() == Status.DONE).count();

            return String.format("TODO: %d | DOING: %d | DONE: %d", todo, doing, done);
        } catch (IOException e) {
            throw new RuntimeException("Error counting tasks: " + e.getMessage(), e);
        }
    }

    public boolean deleteTask(String name) {
        try {
            Task taskToDelete = findTaskByName(name);
            if (taskToDelete == null) {
                return false;
            }
            return taskRepository.delete(taskToDelete);
        } catch (IOException e) {
            throw new RuntimeException("Error deleting task: " + e.getMessage(), e);
        }
    }

    private Task findTaskByName(String name) throws IOException {
        return taskRepository.findAll().stream()
                .filter(t -> t.getName().equalsIgnoreCase(name.trim()))
                .findFirst()
                .orElse(null);
    }
}
