package zg.acelera;

import zg.acelera.app.CategoryMenu;
import zg.acelera.app.MainMenu;
import zg.acelera.app.TaskMenu;
import zg.acelera.app.UserMenu;
import zg.acelera.controller.CategoryController;
import zg.acelera.controller.TaskController;
import zg.acelera.controller.UserController;
import zg.acelera.repository.CategoryRepository;
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

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        UserInterface userInterface = new UserInterfaceImpl(scanner);
        TaskInterface taskInterface = new TaskInterfaceImpl(scanner);

        UserRepositoryImpl userRepository = new UserRepositoryImpl();
        UserService userService = new UserService(userRepository);
        UserController usersController = new UserController(userService, userInterface);
        UserMenu userMenu = new UserMenu(usersController, userInterface);


        MainMenu mainMenu = getMainMenu(userInterface, taskInterface, userMenu);
        mainMenu.start();

        scanner.close();
    }

    private static MainMenu getMainMenu(UserInterface userInterface, TaskInterface taskInterface, UserMenu userMenu) {
        CategoryRepository categoryRepository = new CategoryRepository();
        CategoryService categoryService = new CategoryService(categoryRepository);
        CategoryController categoryController = new CategoryController(categoryService, userInterface);
        CategoryMenu categoryMenu = new CategoryMenu(categoryController, userInterface);

        TaskRepositoryImpl taskRepository = new TaskRepositoryImpl();
        TaskService taskService = new TaskService(taskRepository);
        TaskController tasksController = new TaskController(taskService, taskInterface);
        TaskMenu taskMenu = new TaskMenu(tasksController, taskInterface, categoryMenu);

        MainMenu mainMenu = new MainMenu(userMenu, taskMenu, userInterface);
        return mainMenu;
    }
}