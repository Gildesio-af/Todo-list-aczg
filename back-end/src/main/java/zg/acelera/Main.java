package zg.acelera;

import zg.acelera.app.CategoryMenu;
import zg.acelera.app.MainMenu;
import zg.acelera.app.TaskMenu;
import zg.acelera.app.UserMenu;
import zg.acelera.controller.CategoryController;
import zg.acelera.controller.TaskController;
import zg.acelera.controller.UserController;
import zg.acelera.controller.AlarmController;
import zg.acelera.repository.AlarmRepositoryImpl;
import zg.acelera.service.AlarmMonitor;
import zg.acelera.service.AlarmService;
import zg.acelera.util.ConsoleAlarmNotifier;
import zg.acelera.repository.CategoryRepositoryImpl;
import zg.acelera.repository.TaskRepositoryImpl;
import zg.acelera.repository.UserRepositoryImpl;
import zg.acelera.service.CategoryService;
import zg.acelera.service.TaskService;
import zg.acelera.service.UserService;
import zg.acelera.util.interface_user.TaskInterface;
import zg.acelera.util.interface_user.TaskInterfaceImpl;
import zg.acelera.util.interface_user.UserInterface;
import zg.acelera.util.interface_user.UserInterfaceImpl;

import java.util.Scanner;
import java.time.Clock;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        UserInterface userInterface = new UserInterfaceImpl(scanner);
        TaskInterface taskInterface = new TaskInterfaceImpl(scanner);

        UserRepositoryImpl userRepository = new UserRepositoryImpl("user.csv");
        UserService userService = new UserService(userRepository);
        UserController usersController = new UserController(userService, userInterface);
        UserMenu userMenu = new UserMenu(usersController, userInterface);


        ApplicationContext context = getMainMenu(userInterface, taskInterface, userMenu);
        context.monitor().start();
        try {
            context.menu().start();
        } finally {
            context.monitor().close();
            scanner.close();
        }
    }

    private static ApplicationContext getMainMenu(UserInterface userInterface, TaskInterface taskInterface,
                                                  UserMenu userMenu) {
        CategoryRepositoryImpl categoryRepositoryImpl = new CategoryRepositoryImpl();
        CategoryService categoryService = new CategoryService(categoryRepositoryImpl);
        CategoryController categoryController = new CategoryController(categoryService, userInterface);
        CategoryMenu categoryMenu = new CategoryMenu(categoryController, userInterface);

        TaskRepositoryImpl taskRepository = new TaskRepositoryImpl();
        AlarmService alarmService = new AlarmService(new AlarmRepositoryImpl(), taskRepository,
                Clock.systemDefaultZone(), new ConsoleAlarmNotifier());
        AlarmMonitor monitor = new AlarmMonitor(alarmService, Executors.newSingleThreadScheduledExecutor(),
                Clock.systemDefaultZone());
        AlarmController alarmController = new AlarmController(alarmService, taskInterface);
        TaskService taskService = new TaskService(taskRepository, alarmService);
        TaskController tasksController = new TaskController(taskService, taskInterface);
        TaskMenu taskMenu = new TaskMenu(tasksController, taskInterface, categoryMenu, alarmController);

        MainMenu mainMenu = new MainMenu(userMenu, taskMenu, userInterface);
        return new ApplicationContext(mainMenu, monitor);
    }

    private record ApplicationContext(MainMenu menu, AlarmMonitor monitor) {
    }
}