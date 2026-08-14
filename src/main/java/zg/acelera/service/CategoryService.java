package zg.acelera.service;

import lombok.RequiredArgsConstructor;
import zg.acelera.domain.Category;
import zg.acelera.repository.CategoryRepository;

import java.util.List;

@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository repository;

    public boolean createCategory(String name) {
        try {
            List<Category> existingCategories = repository.findAll();

            boolean exists = existingCategories.stream()
                    .anyMatch(c -> c.getCategory().equalsIgnoreCase(name));

            if (exists) return false;

            repository.save(new Category(name));
            return true;
        } catch (Exception e) {
            throw new RuntimeException("Error creating category", e);
        }
    }

    public List<Category> listCategories() {
        try {
            return repository.findAll();
        } catch (Exception e) {
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
