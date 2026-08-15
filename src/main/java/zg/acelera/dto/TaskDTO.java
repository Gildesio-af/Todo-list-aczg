package zg.acelera.dto;

import java.time.LocalDateTime;

public record TaskDTO(
        String name,
        String description,
        Integer priority,
        String status,
        LocalDateTime startDate,
        LocalDateTime endDate,
        String category
) {
    public TaskDTO {
        if (name == null || name.trim().isEmpty())
            throw new IllegalArgumentException("Name is required and cannot be empty.");
        if (description == null || description.trim().isEmpty())
            throw new IllegalArgumentException("Description is required and cannot be empty.");
        if (category == null || category.trim().isEmpty())
            throw new IllegalArgumentException("Category is required and cannot be empty.");
        if (priority == null || priority < 1 || priority > 5)
            throw new IllegalArgumentException("Priority is required and must be a value between 1 and 5.");
        if (startDate != null && endDate != null && startDate.isAfter(endDate))
            throw new IllegalArgumentException("The start date cannot be after the end date.");
        if (status == null || (!status.equalsIgnoreCase("TODO") &&
                !status.equalsIgnoreCase("DOING") && !status.equalsIgnoreCase("DONE")))
            throw new IllegalArgumentException("Status cannot be empty or diff than 'TODO', 'DOING' ou 'DONE'");
    }
}
