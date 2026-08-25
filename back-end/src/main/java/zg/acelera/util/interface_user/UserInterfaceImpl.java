package zg.acelera.util.interface_user;

import lombok.RequiredArgsConstructor;
import zg.acelera.dto.UserDTO;
import zg.acelera.dto.UserUpdateDTO;

import java.util.Scanner;

@RequiredArgsConstructor
public class UserInterfaceImpl implements UserInterface{
    private final Scanner scanner;

    @Override
    public void showMessage(String message) {
        System.out.println("System message: " + message);
    }

    @Override
    public String readText() {
        return scanner.nextLine();
    }

    @Override
    public Integer readInteger() {
        String number = scanner.nextLine();
        try {
            return Integer.parseInt(number);
        } catch (NumberFormatException e1) {
            return null;
        }
    }

    @Override
    public UserDTO readUserDTO() {
        System.out.print("Name: ");
        String name = readText();

        System.out.print("Email: ");
        String email = readText();

        System.out.print("Age: ");
        Integer age = readInteger();

        return new UserDTO(name, email, age);
    }

    @Override
    public UserUpdateDTO readUserUpdateDTO() {
        System.out.print("New Name (or leave blank to not change): ");
        String name = readText();

        System.out.print("New Email (or leave blank to not change): ");
        String email = readText();

        System.out.print("New Age (or leave blank to not change): ");
        Integer age = readInteger();

        return new UserUpdateDTO(
                name.trim().isEmpty() ? null : name,
                email.trim().isEmpty() ? null : email,
                age
        );
    }
}
