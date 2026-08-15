package zg.acelera.repository;

import zg.acelera.domain.Category;
import zg.acelera.domain.Task;
import zg.acelera.domain.enums.Status;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TaskRepositoryImpl implements TaskRepository {
    private static final String FILE_NAME = "tasks.csv";
    private final Path filePath;

    public TaskRepositoryImpl() {
        this.filePath = Paths.get(FILE_NAME);
        initializeFile();
    }

    private void initializeFile() {
        try {
            if (!Files.exists(filePath)) Files.createFile(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Error initializing tasks file", e);
        }
    }

    @Override
    public List<Task> findAll() throws IOException {
        List<Task> tasks;

        try (Stream<String> stream = Files.lines(filePath)) {
            tasks = stream
                    .filter(line -> !line.trim().isEmpty())
                    .map(line -> {
                        String[] data = line.split(";");

                        if (data.length >= 8) {
                            return new Task(
                                    data[1],
                                    data[2],
                                    Integer.parseInt(data[3]),
                                    Status.valueOf(data[4]),
                                    LocalDateTime.parse(data[5]),
                                    LocalDateTime.parse(data[6]),
                                    new Category(data[7])
                            );
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .toList();
        }
        return tasks;
    }

    @Override
    public List<Task> findByCategory(String category) {
        try {
            return findAll().stream()
                    .filter(task -> task.getCategory().getCategory().equalsIgnoreCase(category))
                    .toList();
        } catch (IOException e) {
            System.err.println("Error reading tasks by category: " + e.getMessage());
            return List.of();
        }
    }

    @Override
    public List<Task> findOrderedByPriority() throws IOException {
        return findAll().stream()
                .sorted(Comparator.comparingInt(Task::getPriority))
                .toList();
    }

    @Override
    public List<Task> findByStatus(String status) throws IOException {
        return findAll().stream()
                .filter(task -> task.getStatus().name().equalsIgnoreCase(status))
                .collect(Collectors.toList());
    }

    @Override
    public Task save(Task task) throws IOException {
        List<Task> tasks = findAll();

        tasks.add(task);

        saveAll(tasks);
        return task;
    }

    @Override
    public boolean delete(Task task) throws IOException {
        List<Task> tasks = new ArrayList<>(findAll());

        boolean removed = tasks.remove(task);

        if (removed) saveAll(tasks);

        return removed;
    }

    private void saveAll(List<Task> tasks) throws IOException {
        List<String> lines = tasks.stream()
                .map(Task::toCsvLine)
                .toList();

        Files.write(filePath, lines);
    }
}
