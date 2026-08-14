package zg.acelera.repository;

import zg.acelera.domain.User;
import zg.acelera.dto.UserDTO;
import zg.acelera.dto.UserUpdateDTO;

import java.io.IOException;

public interface UserRepository {
    User viewUser() throws IOException;
    User createUser(UserDTO userDTO) throws IOException;
    User updateUser(UserUpdateDTO userDTO) throws IOException;
    void deleteUser() throws IOException;
}
