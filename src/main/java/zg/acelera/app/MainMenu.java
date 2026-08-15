package zg.acelera.app;

import lombok.RequiredArgsConstructor;
import zg.acelera.util.interface_user.UserInterface;

@RequiredArgsConstructor
public class MainMenu {
    private final UserMenu userMenu;
    private final TaskMenu taskMenu;
    private final UserInterface userInterface;

    public void start() {
        boolean running = true;

        while (running) {
            System.out.println("\n=== TODO List ===");
            System.out.println("1 - Manage user profile");
            System.out.println("2 - Task management module");
            System.out.println("0 - Exit");
            System.out.print("Choose a module: ");

            Integer option = userInterface.readInteger();
            if (option == null) continue;

            switch (option) {
                case 0 -> {
                    userInterface.showMessage("Closing application. Goodbye!");
                    running = false;
                }
                case 1 -> userMenu.start();
                case 2 -> taskMenu.start();
                default -> userInterface.showMessage("Invalid option.");
            }
        }
    }
}
