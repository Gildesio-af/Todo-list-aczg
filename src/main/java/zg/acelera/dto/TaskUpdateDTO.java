package zg.acelera.dto;

import zg.acelera.util.exception.TaskInfoWrongException;

import java.time.LocalDateTime;

public record TaskUpdateDTO(
        Integer id,
        String name,
        String description,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Integer priority,
        String category,
        String status
) {
    public TaskUpdateDTO {
        if (id == null || id <= 0) throw new TaskInfoWrongException("Id is required and must be greater than zero");
        if (name != null && name.trim().isEmpty()) throw new TaskInfoWrongException("Name cannot be empty when updating.");
        if (description != null && description.trim().isEmpty()) throw new TaskInfoWrongException("Description cannot be empty when updating.");
        if (category != null && category.trim().isEmpty()) throw new TaskInfoWrongException("Category cannot be empty when updating.");
        if (priority != null && (priority < 1 || priority > 5)) throw new TaskInfoWrongException("Priority must be a value between 1 and 5.");
        if (status != null) {
            String cleanStatus = status.trim().toUpperCase();
            if (!cleanStatus.equals("TODO") && !cleanStatus.equals("DOING") && !cleanStatus.equals("DONE"))
                throw new TaskInfoWrongException("Status must be one of the following: TODO, DOING, DONE.");
        }
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) throw new TaskInfoWrongException("The start date cannot be after the end date.");
    }
}
