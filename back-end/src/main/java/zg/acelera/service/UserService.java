package zg.acelera.service;

import lombok.RequiredArgsConstructor;
import zg.acelera.domain.User;
import zg.acelera.dto.UserDTO;
import zg.acelera.dto.UserUpdateDTO;
import zg.acelera.repository.UserRepository;
import zg.acelera.util.exception.UserInfoWrongException;

import java.io.IOException;

@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public String getUser() {
        User user;
        try {
            user = userRepository.viewUser();
        } catch (IOException e) {
            return null;
        }
        if (user != null)
            return user.toString();
        return null;
    }

    public String createUser(UserDTO userDTO) {
        User user;
        try {
            user = userRepository.createUser(userDTO);
        } catch (IOException e) {
            return null;
        }

        return user.toString();
    }

    public String updateUser(UserUpdateDTO userUpdateDTO) {
        User user;
        try {
            user = userRepository.updateUser(userUpdateDTO);
        } catch (IOException e) {
            return null;
        }

        if (user == null) return null;

        return user.toString();
    }

    public void deleteUser() {
        try {
            userRepository.deleteUser();
        } catch (IOException e) {
            throw new RuntimeException("Error deleting user", e);
        }
    }
}
