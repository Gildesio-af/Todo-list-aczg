package zg.acelera.repository;

import zg.acelera.domain.Category;

import java.io.IOException;
import java.util.List;

public interface CategoryRepository {
    List<Category> findAll() throws IOException;
    void save(Category category) throws IOException;
    boolean delete(String categoryName) throws IOException;
}
