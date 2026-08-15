package zg.acelera.repository;

import zg.acelera.domain.Task;

import java.io.IOException;
import java.util.List;

public interface TaskRepository {
    List<Task> findAll() throws IOException;
    List<Task> findByCategory(String category);
    List<Task> findOrderedByPriority() throws IOException;
    List<Task> findByStatus(String status) throws IOException;
    Task save(Task task) throws IOException;
    boolean delete(Task task) throws IOException;
}
