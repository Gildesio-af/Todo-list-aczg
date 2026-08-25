package zg.acelera.util.interface_user;

import zg.acelera.dto.UserDTO;
import zg.acelera.dto.UserUpdateDTO;

public interface UserInterface {
    void showMessage(String message);
    String readText();
    Integer readInteger();

    UserDTO readUserDTO();
    UserUpdateDTO readUserUpdateDTO();
}
