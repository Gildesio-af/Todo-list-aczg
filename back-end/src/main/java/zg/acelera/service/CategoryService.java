package zg.acelera.service;

import lombok.RequiredArgsConstructor;
import zg.acelera.domain.Category;
import zg.acelera.repository.CategoryRepository;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository repository;

    public boolean createCategory(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        
        try {
            List<Category> existingCategories = repository.findAll();

            boolean exists = existingCategories.stream()
                    .anyMatch(c -> c.getName().equalsIgnoreCase(name));

            if (exists) return false;

            repository.save(new Category(name));
            return true;
        } catch (IOException e) {
            throw new RuntimeException("Error creating category", e);
        }
    }

    public List<Category> listCategories() {
        try {
            return repository.findAll();
        } catch (IOException e) {
            throw new RuntimeException("Error listing categories", e);
        }
    }

    public boolean deleteCategory(String name) {
        try {
            return repository.delete(name);
        } catch (Exception e) {
            throw new RuntimeException("Error deleting category", e);
        }
    }
}
