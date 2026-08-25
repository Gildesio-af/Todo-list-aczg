package zg.acelera.app;

import lombok.RequiredArgsConstructor;
import zg.acelera.controller.CategoryController;
import zg.acelera.util.interface_user.UserInterface;

@RequiredArgsConstructor
public class CategoryMenu {
    private final CategoryController controller;
    private final UserInterface userInterface;

    public void start() {
        boolean running = true;
        while (running) {
            showMenu();
            Integer option = userInterface.readInteger();
            if (option == null) continue;

            switch (option) {
                case 1 -> {
                    userInterface.showMessage("Enter new category name: ");
                    controller.createCategory(userInterface.readText());
                }
                case 2 -> controller.listCategories();
                case 3 -> {
                    userInterface.showMessage("Enter category name to delete: ");
                    controller.deleteCategory(userInterface.readText());
                }
                case 0 -> running = false;
                default -> userInterface.showMessage("Invalid option.");
            }
        }
    }

    private void showMenu() {
        System.out.println("\n=== CATEGORY MENU ===");
        System.out.println("1 - Create Category");
        System.out.println("2 - List Categories");
        System.out.println("3 - Delete Category");
        System.out.println("0 - Back to Task Menu");
        System.out.print("Choose: ");
    }
}
