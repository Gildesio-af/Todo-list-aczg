package service

import spock.lang.Specification
import zg.acelera.domain.User
import zg.acelera.dto.UserDTO
import zg.acelera.dto.UserUpdateDTO
import zg.acelera.repository.UserRepository
import zg.acelera.service.UserService

class UserServiceSpec extends Specification{

    UserService userService
    UserRepository userRepository
    User user
    User userUpdated
    UserDTO createDTO
    UserUpdateDTO userUpdateDTO

    def setup() {
        userRepository = Mock()
        userService = new UserService(userRepository)
        user = User.builder()
            .name("Tanjiro Kamado")
            .age(20)
            .email("tanjiro.kamado@example.com")
            .tasks([])
            .build()

        userUpdated = User
            .builder()
            .name("Nesko Kamado")
            .age(19)
            .email("nesko.kamado@example.com")
            .tasks([])
            .build()

        createDTO = UserDTO
            .builder()
            .name("Tanjiro Kamado")
            .age(20)
            .email("tanjiro.kamado@example.com")
            .build()

        userUpdateDTO = UserUpdateDTO.builder()
            .name("Nesko Kamado")
            .age(19)
            .email("nesko.kamado@example.com")
            .build()
    }

    def "getUser should return user as string when user exists"() {
        given: "a user exists"
        userRepository.viewUser() >> user

        when: "getting the user"
        String result = userService.getUser()

        then: "result should be the user as a string"
        result == user.toString()
    }

    def "getUser should return null when user does not exist"() {
        given: "a repository returning a null user"
        userRepository.viewUser() >> null

        when: "getting the user"
        String result = userService.getUser()

        then: "result should be null"
        result == null
    }

    def "getUser should return null when repository throw a IOException"() {
        given: "a repository throwing an IOException"
        userRepository.viewUser() >> { throw new IOException("Error occurred while fetching user") }

        when: "getting the user"
        String result = userService.getUser()

        then: "result should be null"
        result == null
    }

    def "createUser should return user's to string when user is created successfully"() {
        given: "a repository creating a user"
        userRepository.createUser(createDTO) >> user

        when: "creating the user"
        String result = userService.createUser(createDTO)

        then: "result should be the user as a string"
        result == user.toString()
    }

    def "createUser should return null when user creation fails"() {
        given: "a repository throwing an IOException"
        userRepository.createUser(createDTO) >> { throw new IOException("Error occurred while saving user") }

        when: "creating the user"
        String result = userService.createUser(createDTO)

        then: "result should be null"
        result == null
    }

    def "createUser should return null when user repository return null"() {
        given: "a repository returning null"
        userRepository.createUser(createDTO) >> null

        when: "creating the user"
        String result = userService.createUser(createDTO)

        then: "result should be null"
        result == null
    }

    def "updateUser should return user's to string when user is updated successfully"() {
        given: "a repository returning the updated user"
        userRepository.updateUser(userUpdateDTO) >> userUpdated

        when: "updating the user"
        String result = userService.updateUser(userUpdateDTO)

        then: "result should be the user as a string"
        result == userUpdated.toString()
    }

    def "updateUser should return null when user update fails"() {
        given: "a repository throwing an IOException"
        userRepository.updateUser(userUpdateDTO) >> { throw new IOException("Error occurred while updating user") }

        when: "updating the user"
        String result = userService.updateUser(userUpdateDTO)

        then: "result should be null"
        result == null
    }

    def "updateUser should return null when user repository return null"() {
        given: "a repository returning null"
        userRepository.updateUser(userUpdateDTO) >> null

        when: "updating the user"
        String result = userService.updateUser(userUpdateDTO)

        then: "result should be null"
        result == null
    }

    def "deleteUser should call delete from user repository when be called"() {
        when: "deleting the user"
        userService.deleteUser()

        then: "the repository should be called"
        1 * userRepository.deleteUser()
    }

    def "deleteUser should throw a RuntimeException when the repository throws Exception"() {
        given: "a repository throwing an Exception"
        userRepository.deleteUser() >> { throw new IOException("Error occurred while deleting user") }

        when: "deleting the user"
        userService.deleteUser()

        then: "a RuntimeException should be thrown"
        RuntimeException e = thrown(RuntimeException)
        e.getMessage() == "Error deleting user"
        e.cause instanceof IOException
    }
}
