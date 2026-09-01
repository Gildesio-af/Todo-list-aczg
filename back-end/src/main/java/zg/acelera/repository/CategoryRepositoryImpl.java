package zg.acelera.repository;

import zg.acelera.domain.Category;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Collectors;

public class CategoryRepositoryImpl implements CategoryRepository {
    private static final String FILE_NAME = "categories.csv";
    private final Path filePath;

    public CategoryRepositoryImpl() {
        this.filePath = Path.of(FILE_NAME);
        initializeFile();
    }

    public CategoryRepositoryImpl(Path filePath) {
        this.filePath = filePath;
        initializeFile();
    }

    private void initializeFile() {
        try {
            if (!Files.exists(filePath)) Files.createFile(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Error initializing categories file", e);
        }
    }

    @Override
    public List<Category> findAll() throws IOException {
        return Files.lines(filePath)
                .filter(line -> !line.trim().isEmpty())
                .map(Category::new)
                .collect(Collectors.toList());
    }

    public void save(Category category) throws IOException {
        String lineToSave = category.getName() + System.lineSeparator();
        Files.writeString(filePath, lineToSave, StandardOpenOption.APPEND);
    }

    public boolean delete(String categoryName) throws IOException {
        List<Category> categories = findAll();
        boolean removed = categories.removeIf(c -> c.getName().equalsIgnoreCase(categoryName));

        if (removed) {
            List<String> lines = categories.stream()
                    .map(Category::getName)
                    .toList();
            Files.write(filePath, lines);
        }
        return removed;
    }
}
