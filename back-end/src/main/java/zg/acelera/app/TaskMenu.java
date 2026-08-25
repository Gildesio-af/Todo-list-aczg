package zg.acelera.app;

import lombok.RequiredArgsConstructor;
import zg.acelera.controller.TaskController;
import zg.acelera.util.interface_user.TaskInterface;

@RequiredArgsConstructor
public class TaskMenu {

    private final TaskController controller;
    private final TaskInterface taskInterface;
    private final CategoryMenu categoryMenu;

    public void start() {
        boolean running = true;

        while (running) {
            showMenu();
            Integer option = taskInterface.readInteger();

            if (option == null) {
                taskInterface.showMessage("Invalid input. Please enter a number.");
                continue;
            }

            switch (option) {
                case 1 -> controller.createTask(taskInterface.readTaskDTO());
                case 2 -> {
                    taskInterface.showMessage("Enter the exact Task Name to delete: ");
                    controller.deleteTask(taskInterface.readText());
                }
                case 3 -> controller.listAllTasks();
                case 4 -> controller.listTasksOrderedByPriority();
                case 5 -> filterMenu();
                case 6 -> controller.showTaskCounts();
                case 7 -> categoryMenu.start();
                case 0 -> {
                    taskInterface.showMessage("Returning to Main Menu...");
                    running = false;
                }
                default -> taskInterface.showMessage("Invalid option. Please try again.");
            }
        }
    }

    private void showMenu() {
        System.out.println("\n=== TASK MANAGEMENT MENU ===");
        System.out.println("1 - Create a new Task");
        System.out.println("2 - Delete a Task");
        System.out.println("3 - List all Tasks");
        System.out.println("4 - List Tasks ordered by Priority");
        System.out.println("5 - Filter Tasks (By Category or Status)");
        System.out.println("6 - Show Task Dashboard (TODO/DOING/DONE counts)");
        System.out.println("7 - Manage Categories");
        System.out.println("0 - Back to Main Menu");
        System.out.print("Choose an option: ");
    }

    private void filterMenu() {
        System.out.println("\n--- FILTER TASKS ---");
        System.out.println("1 - By Category");
        System.out.println("2 - By Status");
        System.out.print("Choose a filter: ");

        Integer opt = taskInterface.readInteger();
        if (opt == null) {
            taskInterface.showMessage("Invalid input.");
            return;
        }

        switch (opt) {
            case 1 -> {
                taskInterface.showMessage("Enter the Category name:");
                controller.listTasksByCategory(taskInterface.readText());
            }
            case 2 -> {
                taskInterface.showMessage("Enter the Status (TODO, DOING, DONE):");
                controller.listTasksByStatus(taskInterface.readText());
            }
            default -> taskInterface.showMessage("Invalid filter option.");
        }
    }
}