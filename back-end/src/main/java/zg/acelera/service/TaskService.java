package zg.acelera.service;

import zg.acelera.domain.Category;
import zg.acelera.domain.Task;
import zg.acelera.domain.enums.Status;
import zg.acelera.dto.TaskDTO;
import zg.acelera.dto.TaskUpdateDTO;
import zg.acelera.repository.TaskRepository;

import java.io.IOException;
import java.util.List;

public class TaskService {
    private final TaskRepository taskRepository;
    private final AlarmService alarmService;

    public TaskService() {
        this.taskRepository = null;
        this.alarmService = null;
    }

    public TaskService(TaskRepository taskRepository) {
        this(taskRepository, null);
    }

    public TaskService(TaskRepository taskRepository, AlarmService alarmService) {
        this.taskRepository = taskRepository;
        this.alarmService = alarmService;
    }

    public Task createTask(TaskDTO dto) {
        try {
            Task task = new Task(
                    dto.name(),
                    dto.description(),
                    dto.priority(),
                    Status.valueOf(dto.status()),
                    dto.startDate(),
                    dto.endDate(),
                    new Category(dto.category())
            );

            return taskRepository.save(task);
        } catch (IOException e) {
            throw new RuntimeException("Error creating task: " + e.getMessage(), e);
        }
    }

    public Task updateTask(String taskName, TaskUpdateDTO dto) {
        if (taskName == null || taskName.trim().isEmpty()) {
            throw new IllegalArgumentException("Task name cannot be null or empty.");
        }

        try {
            Task existingTask = findTaskByName(taskName);
            String previousName = existingTask == null ? null : existingTask.getName();
            Task taskToUpdate = copyTask(existingTask);
            dto.update(taskToUpdate);
            Task persistedTask = taskRepository.update(taskName, taskToUpdate);
            Task updatedTask = persistedTask == null ? null : taskToUpdate;

            if (updatedTask != null) {
                if (alarmService != null) {
                    if (previousName != null && !previousName.equals(updatedTask.getName())) {
                        alarmService.renameTaskAlarms(previousName, updatedTask);
                    } else {
                        alarmService.recalculateForTask(updatedTask);
                    }
                }
                return updatedTask;
            } else {
                throw new RuntimeException("Task not found with name: " + taskName);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error updating task: " + e.getMessage(), e);
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
        try {
            return taskRepository.findByCategory(category);
        } catch (IOException e) {
            throw new RuntimeException("Error fetching tasks by category: " + e.getMessage(), e);
        }
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
            boolean deleted = taskRepository.delete(taskToDelete);
            if (deleted && alarmService != null) alarmService.deleteByTask(taskToDelete.getName());
            return deleted;
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

    private Task copyTask(Task task) {
        if (task == null) return null;
        return new Task(task.getName(), task.getDescription(), task.getPriority(), task.getStatus(),
                task.getStartDate(), task.getEndDate(), task.getCategory());
    }
}
