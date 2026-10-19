package zg.acelera.app;

import lombok.RequiredArgsConstructor;
import zg.acelera.controller.UserController;
import zg.acelera.dto.UserDTO;
import zg.acelera.dto.UserUpdateDTO;
import zg.acelera.util.exception.UserInfoWrongException;
import zg.acelera.util.interface_user.UserInterface;

@RequiredArgsConstructor
public class UserMenu {
    private final UserController controller;
    private final UserInterface userInterface;

    public void start() {
        boolean running = true;

        while (running) {
            showMenu();
            Integer option = userInterface.readInteger();

            if (option == null) {
                userInterface.showMessage("Invalid input. Please enter a number.");
                continue;
            }

            switch (option) {
                case 1 -> controller.viewProfile();
                case 2 -> {
                    try {
                        controller.createProfile(userInterface.readUserDTO());
                    } catch (UserInfoWrongException e) {
                        userInterface.showMessage("Error creating user: " + e.getMessage());
                    }
                }
                case 3 -> {
                    try {
                        controller.updateUser(userInterface.readUserUpdateDTO());
                    } catch (UserInfoWrongException e) {
                        userInterface.showMessage("Error updating user: " + e.getMessage());
                    }
                }
                case 4 -> controller.deleteUser();
                case 0 -> {
                    userInterface.showMessage("Exiting User Menu.");
                    running = false;
                }
                default -> userInterface.showMessage("Invalid option. Please try again.");
            }
        }
    }

    private void showMenu() {
        System.out.println("\n=== USER MENU ===");
        System.out.println("1 - Show profile");
        System.out.println("2 - Register new user");
        System.out.println("3 - Update user");
        System.out.println("4 - Delete user");
        System.out.println("0 - Exit");
        System.out.print("Choose an option: ");
    }
}