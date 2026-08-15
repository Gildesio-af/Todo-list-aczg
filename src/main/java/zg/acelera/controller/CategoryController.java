package zg.acelera.controller;

import lombok.RequiredArgsConstructor;
import zg.acelera.domain.Category;
import zg.acelera.service.CategoryService;
import zg.acelera.util.interface_user.UserInterface;

import java.util.List;

@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService service;
    private final UserInterface userInterface;

    public void createCategory(String name) {
        if (name == null || name.trim().isEmpty()) {
            userInterface.showMessage("Category name cannot be empty.");
            return;
        }

        boolean success = service.createCategory(name.trim());
        if (success) {
            userInterface.showMessage("Category created successfully.");
        } else {
            userInterface.showMessage("Category already exists.");
        }
    }

    public void listCategories() {
        List<Category> categories = service.listCategories();
        if (categories.isEmpty()) {
            userInterface.showMessage("No categories found.");
            return;
        }

        userInterface.showMessage("--- Categories ---");
        categories.forEach(c -> userInterface.showMessage("- " + c.getCategory()));
    }

    public void deleteCategory(String name) {
        boolean deleted = service.deleteCategory(name.trim());
        if (deleted) {
            userInterface.showMessage("Category deleted successfully.");
        } else {
            userInterface.showMessage("Category not found.");
        }
    }
}