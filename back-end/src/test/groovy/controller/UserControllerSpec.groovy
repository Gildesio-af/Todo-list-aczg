package controller

import spock.lang.Specification
import zg.acelera.controller.UserController
import zg.acelera.dto.UserDTO
import zg.acelera.dto.UserUpdateDTO
import zg.acelera.service.UserService
import zg.acelera.util.interface_user.UserInterface

class UserControllerSpec extends Specification {

    UserController controller
    UserService userService
    UserInterface userInterface
    UserDTO createDTO
    UserUpdateDTO updateDTO

    def setup() {
        userService = Mock(UserService)
        userInterface = Mock(UserInterface)

        controller = new UserController(userService, userInterface)

        createDTO = UserDTO.builder()
                .name("Test")
                .email("test@email.com")
                .age(20)
                .build()
        updateDTO = UserUpdateDTO.builder().name("Updated").build()
    }

    def "viewProfile should return 200 and show user data when user exists"() {
        when: "calling viewProfile"
        int statusCode = controller.viewProfile()

        then: "service returns user, interface displays it, and status is 200"
        1 * userService.getUser() >> "User;user@gmail.com;20"
        1 * userInterface.showMessage("User;user@gmail.com;20")
        statusCode == 200
    }

    def "viewProfile should return 400 and show error when user is not found"() {
        when: "calling viewProfile"
        int statusCode = controller.viewProfile()

        then: "service returns null, interface displays error, and status is 400"
        1 * userService.getUser() >> null
        1 * userInterface.showMessage("Error retrieving user profile.")
        statusCode == 400
    }

    def "createProfile should return 201 and show success when creation works"() {
        when: "calling createProfile with valid data"
        int statusCode = controller.createProfile(createDTO)

        then: "service creates user, interface displays success, and status is 201"
        1 * userService.createUser(createDTO) >> "Test"
        1 * userInterface.showMessage("User profile created successfully - Test")
        statusCode == 201
    }

    def "createProfile should return 400 and show error when creation fails"() {
        when: "calling createProfile but something goes wrong"
        int statusCode = controller.createProfile(createDTO)

        then: "service returns null, interface displays error, and status is 400"
        1 * userService.createUser(createDTO) >> null
        1 * userInterface.showMessage("Error creating user profile.")
        statusCode == 400
    }

    def "updateUser should return 200 and show success when update works"() {
        when: "calling updateUser with new data"
        int statusCode = controller.updateUser(updateDTO)

        then: "service updates user, interface displays success, and status is 200"
        1 * userService.updateUser(updateDTO) >> "Updated"
        1 * userInterface.showMessage("User profile updated successfully - Updated")
        statusCode == 200
    }

    def "updateUser should return 400 and show error when update fails"() {
        when: "calling updateUser but it fails"
        int statusCode = controller.updateUser(updateDTO)

        then: "service returns null, interface displays error, and status is 400"
        1 * userService.updateUser(updateDTO) >> null
        1 * userInterface.showMessage("Error updating user profile.")
        statusCode == 400
    }

    def "deleteUser should return 204 when deletion is successful"() {
        when: "calling deleteUser"
        int statusCode = controller.deleteUser()

        then: "service executes, no message is displayed, and status is 204"
        1 * userService.deleteUser()
        0 * userInterface.showMessage(_)
        statusCode == 204
    }

    def "deleteUser should return 400 and show error when service throws exception"() {
        when: "calling deleteUser"
        int statusCode = controller.deleteUser()

        then: "service throws exception, interface displays error, and status is 400"
        1 * userService.deleteUser() >> { throw new RuntimeException("DB offline") }
        1 * userInterface.showMessage("Error deleting user profile.")
        statusCode == 400
    }
}
