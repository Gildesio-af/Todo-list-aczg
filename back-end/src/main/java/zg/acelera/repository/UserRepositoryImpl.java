package zg.acelera.repository;

import zg.acelera.domain.User;
import zg.acelera.dto.UserDTO;
import zg.acelera.dto.UserUpdateDTO;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class UserRepositoryImpl implements UserRepository {
    private static final String USER_FILE_NAME = "user.csv";
    private final Path filePath;

    public UserRepositoryImpl() {
        this.filePath = Paths.get(USER_FILE_NAME);
        initializeFile();
    }

    private void initializeFile() {
        try {
            if (!Files.exists(filePath)) Files.createFile(filePath);
        } catch (Exception e) {
            throw new RuntimeException("Error initializing user file", e);
        }
    }

    @Override
    public User viewUser() throws IOException {
        String line = Files.lines(filePath)
                .findFirst()
                .orElse("User not Found");
        User user = new User();

        if (!line.equals("User not Found")) {
            String[] userInfo = line.split(";");
            user.setName(userInfo[0]);
            user.setEmail(userInfo[1]);
            user.setAge(Integer.parseInt(userInfo[2]));
            return user;
        }
        return null;
    }

    @Override
    public User createUser(UserDTO userDTO) throws IOException {
        User user = User.builder()
                        .name(userDTO.name())
                        .email(userDTO.email())
                        .age(userDTO.age())
                        .build();
        Files.write(filePath, userDTO.toCsvLine().getBytes());
        return user;
    }

    @Override
    public User updateUser(UserUpdateDTO userDTO) throws IOException {
        String line;
        try (Stream<String> stream = Files.lines(filePath)){
            line = stream.findFirst()
                    .orElse("User not Found");
        }

        if (!line.equals("User not Found")) {
            String[] userInfo = line.split(";");
            if(userDTO.name() != null)
                userInfo[0] = userDTO.name();
            if(userDTO.email() != null)
                userInfo[1] = userDTO.email();
            if(userDTO.age() != null)
                userInfo[2] = String.valueOf(userDTO.age());
            Files.write(filePath, String.join(";", userInfo).getBytes());

            return new User(userInfo);
        }
        return null;
    }

    @Override
    public void deleteUser() throws IOException {
        Files.writeString(filePath, "");
    }
}
