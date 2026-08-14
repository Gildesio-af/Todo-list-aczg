package zg.acelera;

import zg.acelera.app.MainMenu;
import zg.acelera.app.UserMenu;
import zg.acelera.controller.UserController;
import zg.acelera.repository.UserRepository;
import zg.acelera.repository.UserRepositoryImpl;
import zg.acelera.service.UserService;
import zg.acelera.util.interface_user.UserInterfaceImpl;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        UserInterfaceImpl userInterface = new UserInterfaceImpl(new Scanner(System.in));
        UserRepository userRepository = new UserRepositoryImpl();
        UserService userService = new UserService(userRepository);
        UserController userController = new UserController(userService, userInterface);

        UserMenu userMenu = new UserMenu(userController, userInterface);

        MainMenu mainMenu = new MainMenu(userMenu, userInterface);
        mainMenu.start();
    }
}