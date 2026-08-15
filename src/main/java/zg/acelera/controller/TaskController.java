package zg.acelera.controller;

import lombok.RequiredArgsConstructor;
import zg.acelera.domain.Task;
import zg.acelera.dto.TaskDTO;
import zg.acelera.service.TaskService;
import zg.acelera.util.interface_user.TaskInterface;

import java.util.List;

@RequiredArgsConstructor
public class TasksController {

    private final TaskService taskService;
    private final TaskInterface taskInterface;

    public void createTask(TaskDTO dto) {
        try {
            Task task = taskService.createTask(dto);
            taskInterface.showMessage("Task created successfully: " + task.getName());
        } catch (RuntimeException e) {
            taskInterface.showMessage("Failed to create task: " + e.getMessage());
        }
    }

    public void deleteTask(String taskName) {
        if (taskName == null || taskName.trim().isEmpty()) {
            taskInterface.showMessage("Task name cannot be empty.");
            return;
        }

        boolean deleted = taskService.deleteTask(taskName.trim());
        if (deleted) {
            taskInterface.showMessage("Task '" + taskName + "' deleted successfully.");
        } else {
            taskInterface.showMessage("Task '" + taskName + "' not found.");
        }
    }

    public void listAllTasks() {
        taskInterface.showMessage("--- All Tasks ---");
        printTaskList(taskService.listAllTasks());
    }

    public void listTasksByCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            taskInterface.showMessage("Category cannot be empty.");
            return;
        }

        taskInterface.showMessage("--- Tasks in Category: " + category + " ---");
        printTaskList(taskService.listTasksByCategory(category.trim()));
    }

    public void listTasksOrderedByPriority() {
        taskInterface.showMessage("--- Tasks Ordered by Priority (1 to 5) ---");
        printTaskList(taskService.listTasksOrderedByPriority());
    }

    public void listTasksByStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            taskInterface.showMessage("Status cannot be empty.");
            return;
        }

        taskInterface.showMessage("--- Tasks with Status: " + status.toUpperCase() + " ---");
        printTaskList(taskService.listTasksByStatus(status.trim()));
    }

    public void showTaskCounts() {
        taskInterface.showMessage("--- Task Dashboard ---");
        String counts = taskService.countTasksByStatus();
        taskInterface.showMessage(counts);
    }

    // --- Helper Method ---

    private void printTaskList(List<Task> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            taskInterface.showMessage("No tasks found.");
            return;
        }

        // Supondo que a sua classe Task tenha um método toString() bem formatado
        for (Task task : tasks) {
            taskInterface.showMessage(task.toString());
        }
    }
}
