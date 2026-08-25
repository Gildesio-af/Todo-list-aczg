package zg.acelera.util.interface_user;

import lombok.RequiredArgsConstructor;
import zg.acelera.domain.enums.Status;
import zg.acelera.dto.TaskDTO;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

@RequiredArgsConstructor
public class TaskInterfaceImpl implements TaskInterface {

    private final Scanner scanner;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public void showMessage(String message) {
        System.out.println(message);
    }

    @Override
    public String readText() {
        return scanner.nextLine();
    }

    @Override
    public Integer readInteger() {
        String number = scanner.nextLine();
        if (number == null || number.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(number.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public TaskDTO readTaskDTO() {
        System.out.print("Task Name: ");
        String name = readText();

        System.out.print("Description: ");
        String description = readText();

        System.out.print("Priority (1 to 5): ");
        Integer priority = readInteger();

        System.out.print("Start Date (Format: yyyy-MM-dd HH:mm): ");
        LocalDateTime startDate = readDateTimeSafely();

        System.out.print("End Date (Format: yyyy-MM-dd HH:mm): ");
        LocalDateTime endDate = readDateTimeSafely();

        System.out.print("Category Name: ");
        String categoryName = readText();

        System.out.print("Status (TODO, DOING, DONE): ");
        String status = readStatusCorrectly();

        return new TaskDTO(name, description, priority, status, startDate, endDate, categoryName);
    }

    private LocalDateTime readDateTimeSafely() {
        while (true) {
            String dateInput = readText();
            try {
                return LocalDateTime.parse(dateInput, FORMATTER);
            } catch (DateTimeParseException e) {
                System.out.print("Invalid format! Please use 'yyyy-MM-dd HH:mm' (e.g., 2026-08-14 14:30): ");
            }
        }
    }

    private String readStatusCorrectly() {
        while (true) {
            String status = readText();
            if (status.equalsIgnoreCase("TODO") || status.equalsIgnoreCase("DOING")
                    || status.equalsIgnoreCase("DONE"))
                return status.trim().toUpperCase();
        }
    }
}