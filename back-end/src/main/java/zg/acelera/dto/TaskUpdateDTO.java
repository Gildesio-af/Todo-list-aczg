package zg.acelera.dto;

import lombok.Builder;
import zg.acelera.domain.Category;
import zg.acelera.domain.Task;
import zg.acelera.domain.enums.Status;
import zg.acelera.util.exception.TaskInfoWrongException;

import java.time.LocalDateTime;

@Builder
public record TaskUpdateDTO(
        String name,
        String description,
        Integer priority,
        String status,
        LocalDateTime startDate,
        LocalDateTime endDate,
        String category
) {
    public TaskUpdateDTO {
        if (name != null && name.trim().isEmpty())
            throw new TaskInfoWrongException("Name cannot be empty when updating.");
        if (description != null && description.trim().isEmpty())
            throw new TaskInfoWrongException("Description cannot be empty when updating.");
        if (category != null && category.trim().isEmpty())
            throw new TaskInfoWrongException("Category cannot be empty when updating.");
        if (priority != null && (priority < 1 || priority > 5))
            throw new TaskInfoWrongException("Priority must be a value between 1 and 5.");
        if (status != null) {
            String cleanStatus = status.trim().toUpperCase();
            if (!cleanStatus.equals("TODO") && !cleanStatus.equals("DOING") && !cleanStatus.equals("DONE"))
                throw new TaskInfoWrongException("Status must be one of the following: TODO, DOING, DONE.");
        }
        if (startDate != null && endDate != null && startDate.isAfter(endDate))
            throw new TaskInfoWrongException("The start date cannot be after the end date.");
    }

    public Task update(Task task) {
        if (task == null) return null;

        if (name != null) task.setName(name);
        if (description != null) task.setDescription(description);
        if (priority != null) task.setPriority(priority);
        if (status != null) task.setStatus(Status.valueOf(status.trim().toUpperCase()));
        if (startDate != null) task.setStartDate(startDate);
        if (endDate != null) task.setEndDate(endDate);
        if (category != null) task.setCategory(new Category(category));

        return task;
    }
}
