package zg.acelera.controller;

import lombok.RequiredArgsConstructor;
import zg.acelera.dto.UserDTO;
import zg.acelera.dto.UserUpdateDTO;
import zg.acelera.service.UserService;
import zg.acelera.util.interface_user.UserInterface;

@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final UserInterface userInterface;


    public int viewProfile() {
        String user = userService.getUser();
        if (user == null) {
            userInterface.showMessage("Error retrieving user profile.");
            return 400;
        }
        userInterface.showMessage(user);
        return 200;
    }

    public int createProfile(UserDTO userDTO) {
        String user = userService.createUser(userDTO);
        if (user == null) {
            userInterface.showMessage("Error creating user profile.");
            return 400;
        }
        userInterface.showMessage("User profile created successfully - " + user);
        return 201;
    }

    public int updateUser(UserUpdateDTO userUpdateDTO) {
        String user = userService.updateUser(userUpdateDTO);
        if (user == null) {
            userInterface.showMessage("Error updating user profile.");
            return 400;
        }
        userInterface.showMessage("User profile updated successfully - " + user);
        return 200;
    }

    public int deleteUser() {
        try {
            userService.deleteUser();
        } catch (RuntimeException e) {
            userInterface.showMessage("Error deleting user profile.");
            return 400;
        }
        return 204;
    }
}
