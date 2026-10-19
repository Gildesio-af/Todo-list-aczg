package repository

import spock.lang.Specification
import spock.lang.TempDir
import zg.acelera.domain.User
import zg.acelera.dto.UserDTO
import zg.acelera.dto.UserUpdateDTO
import zg.acelera.repository.UserRepositoryImpl

import java.nio.file.Files
import java.nio.file.Path

class UserRepositorySpec extends Specification{

    @TempDir
    Path tempDir
    UserDTO createDTO
    UserUpdateDTO userUpdateDTO

    UserRepositoryImpl repository
    Path filePath

    def setup() {
        filePath = tempDir.resolve("user_test.csv")
        repository = new UserRepositoryImpl(filePath.toString())
        createDTO = UserDTO.builder()
                .name("User")
                .email("user@gmail.com")
                .age(20)
                .build()
        userUpdateDTO = UserUpdateDTO.builder()
                .name("Updated User")
                .email("updateduser@gmail.com")
                .age(25)
                .build()
    }

    def "viewUser should return null when user does not exist"() {
        given: "an empty file"
        Files.writeString(filePath, "")

        when: "attempting to view the user"
        User user = repository.viewUser()

        then: "the returned user is null"
        user == null
    }

    def "viewUser should work when user exists"() {
        given: "a file with existing user data"
        Files.writeString(filePath, "User;user@gmail.com;20")

        when: "viewing the user"
        User user = repository.viewUser()

        then: "the returned user matches the file data"
        user != null
        user.name == "User"
        user.email == "user@gmail.com"
        user.age == 20
    }

    def "createUser should create an user when data is valid"() {
        when: "creating a new user with valid data"
        User newUser = repository.createUser(createDTO)

        then: "the method returns the populated user object"
        newUser != null
        newUser.name == "User"
        newUser.email == "user@gmail.com"
        newUser.age == 20

        and: "the data is successfully written to the file"
        Files.readString(filePath).trim() == "User;user@gmail.com;20"
    }

    def "updateUser should update an user when the user exists"() {
        given: "a file with existing user data"
        Files.writeString(filePath, "User;user@gmail.com;20")

        when: "updating the user with new data"
        User user = repository.updateUser(userUpdateDTO)

        then: "the returned user reflects the updated data"
        user != null
        user.name == "Updated User"
        user.email == "updateduser@gmail.com"
        user.age == 25

        and: "the file content is updated accordingly"
        Files.readString(filePath).trim() == "Updated User;updateduser@gmail.com;25"
    }

    def "updateUser should return null when the user does not exist"() {
        given: "an empty file"
        Files.writeString(filePath, "")

        when: "attempting to update the user"
        User user = repository.updateUser(userUpdateDTO)

        then: "the returned user is null"
        user == null
    }

    def "deleteUser should delete an user when the user exists"() {
        given: "a file with existing user data"
        Files.writeString(filePath, "User;user@gmail.com;20")

        when: "deleting the user"
        repository.deleteUser()

        then: "the file becomes blank"
        Files.readString(filePath).isBlank()
    }
}