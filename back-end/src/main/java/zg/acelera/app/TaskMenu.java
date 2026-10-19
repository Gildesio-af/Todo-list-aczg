package zg.acelera.app;

import zg.acelera.controller.TaskController;
import zg.acelera.controller.AlarmController;
import zg.acelera.dto.AlarmDTO;
import zg.acelera.util.interface_user.TaskInterface;

public class TaskMenu {

    private final TaskController controller;
    private final TaskInterface taskInterface;
    private final CategoryMenu categoryMenu;
    private final AlarmController alarmController;

    public TaskMenu(TaskController controller, TaskInterface taskInterface, CategoryMenu categoryMenu) {
        this(controller, taskInterface, categoryMenu, null);
    }

    public TaskMenu(TaskController controller, TaskInterface taskInterface,
                    CategoryMenu categoryMenu, AlarmController alarmController) {
        this.controller = controller;
        this.taskInterface = taskInterface;
        this.categoryMenu = categoryMenu;
        this.alarmController = alarmController;
    }

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
                case 7 -> {
                    taskInterface.showMessage("Enter the exact Task Name to update: ");
                    controller.updateTask(taskInterface.readText(), taskInterface.readTaskUpdateDTO());
                }
                case 8 -> categoryMenu.start();
                case 9 -> manageAlarms();
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
        System.out.println("7 - Update Task");
        System.out.println("8 - Manage Categories");
        System.out.println("9 - Manage Task Alarms");
        System.out.println("0 - Back to Main Menu");
        System.out.print("Choose an option: ");
    }

    private void manageAlarms() {
        if (alarmController == null) {
            taskInterface.showMessage("Alarm management is unavailable.");
            return;
        }
        System.out.println("\n--- TASK ALARMS ---");
        System.out.println("1 - Create alarm");
        System.out.println("2 - Edit alarm");
        System.out.println("3 - List alarms");
        System.out.println("4 - Remove alarm");
        Integer option = taskInterface.readInteger();
        if (option == null) {
            taskInterface.showMessage("Invalid input.");
            return;
        }
        switch (option) {
            case 1 -> {
                taskInterface.showMessage("Task name:");
                String task = taskInterface.readText();
                taskInterface.showMessage("Advance hours:");
                Integer hours = taskInterface.readInteger();
                taskInterface.showMessage("Advance minutes:");
                Integer minutes = taskInterface.readInteger();
                AlarmDTO dto = readAlarmDTO("creating", task, hours, minutes);
                if (dto != null) alarmController.createAlarm(dto);
            }
            case 2 -> {
                taskInterface.showMessage("Alarm id:");
                String id = taskInterface.readText();
                taskInterface.showMessage("Task name:");
                String task = taskInterface.readText();
                taskInterface.showMessage("Advance hours:");
                Integer hours = taskInterface.readInteger();
                taskInterface.showMessage("Advance minutes:");
                Integer minutes = taskInterface.readInteger();
                AlarmDTO dto = readAlarmDTO("updating", task, hours, minutes);
                if (dto != null) alarmController.updateAlarm(id, dto);
            }
            case 3 -> {
                taskInterface.showMessage("Task name:");
                alarmController.listAlarms(taskInterface.readText());
            }
            case 4 -> {
                taskInterface.showMessage("Alarm id:");
                alarmController.removeAlarm(taskInterface.readText());
            }
            default -> taskInterface.showMessage("Invalid alarm option.");
        }
    }

    private AlarmDTO readAlarmDTO(String operation, String task, Integer hours, Integer minutes) {
        if (hours == null || minutes == null) {
            taskInterface.showMessage("Invalid alarm input.");
            return null;
        }
        try {
            return new AlarmDTO(task, hours, minutes);
        } catch (RuntimeException e) {
            taskInterface.showMessage("Error " + operation + " alarm: " + e.getMessage());
            return null;
        }
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