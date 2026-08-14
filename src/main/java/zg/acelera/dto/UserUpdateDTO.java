package zg.acelera.dto;

import zg.acelera.util.exception.UserInfoWrongException;

public record UserUpdateDTO(String name, String email, Integer age) {
    public UserUpdateDTO {
        if (name != null && (name.length() < 3 || name.length() > 50))
            throw new UserInfoWrongException("Name must be between 3 and 50 characters");
        if (email != null && !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.(com|org|net)$"))
            throw new UserInfoWrongException("Email must be a valid email address");
        if (age != null && (age < 0 || age > 120))
            throw new UserInfoWrongException("Age must be between 18 and 100");
    }
}
