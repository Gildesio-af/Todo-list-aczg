package zg.acelera.dto;

import zg.acelera.util.exception.UserInfoWrongException;

public record UserDTO (
        String name,
        String email,
        int age
){
    public UserDTO(String name, String email, int age) {
        if(name == null || name.length() < 3 || name.length() > 50)
            throw new UserInfoWrongException("Name must be between 3 and 50 characters / not should be empty");
        if(email == null || !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.(com|org|net)$"))
            throw new UserInfoWrongException("Email must be a valid email address / not should be empty");
        if(age < 0 || age > 120)
            throw new UserInfoWrongException("Age must be between 18 and 100");
        this.name = name;
        this.email = email;
        this.age = age;
    }

    public String toCsvLine() {
        return name + ";" + email + ";" + age;
    }
}
