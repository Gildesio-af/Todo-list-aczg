package controller

import spock.lang.Specification
import spock.lang.Unroll
import zg.acelera.controller.CategoryController
import zg.acelera.domain.Category
import zg.acelera.service.CategoryService
import zg.acelera.util.interface_user.UserInterface

class CategoryControllerSpec extends Specification {

    CategoryController controller
    CategoryService service
    UserInterface userInterface

    def setup() {
        service = Mock(CategoryService)
        userInterface = Mock(UserInterface)
        controller = new CategoryController(service, userInterface)
    }

    @Unroll
    def "createCategory should show error when category name is '#invalidName'"() {
        when: "createCategory is called with an invalid name"
        controller.createCategory(invalidName)

        then: "service is not called and empty name error is shown"
        0 * service.createCategory(_)
        1 * userInterface.showMessage("Category name cannot be empty.")

        where:
        invalidName << [null, "", "   "]
    }

    def "createCategory should show success message when category is created"() {
        when: "createCategory is called with a valid name"
        controller.createCategory("  Work  ")

        then: "service creates category with trimmed name and success message is shown"
        1 * service.createCategory("Work") >> true
        1 * userInterface.showMessage("Category created successfully.")
    }

    def "createCategory should show error message when category already exists"() {
        when: "createCategory is called with an existing name"
        controller.createCategory("Work")

        then: "service returns false and already exists message is shown"
        1 * service.createCategory("Work") >> false
        1 * userInterface.showMessage("Category already exists.")
    }

    def "listCategories should show error message when no categories are found"() {
        when: "listCategories is called but repository is empty"
        controller.listCategories()

        then: "service returns empty list and not found message is shown"
        1 * service.listCategories() >> []
        1 * userInterface.showMessage("No categories found.")
    }

    def "listCategories should print header and categories when list is not empty"() {
        given: "a list containing two categories"
        Category cat1 = new Category("Work")
        Category cat2 = new Category("Personal")

        when: "listCategories is called"
        controller.listCategories()

        then: "service returns the list and categories are printed with a header"
        1 * service.listCategories() >> [cat1, cat2]
        1 * userInterface.showMessage("--- Categories ---")
        1 * userInterface.showMessage("- Work")
        1 * userInterface.showMessage("- Personal")
    }

    def "deleteCategory should show success message when category is deleted"() {
        when: "deleteCategory is called with an existing category"
        controller.deleteCategory("  Education  ")

        then: "service deletes category with trimmed name and success message is shown"
        1 * service.deleteCategory("Education") >> true
        1 * userInterface.showMessage("Category deleted successfully.")
    }

    def "deleteCategory should show not found message when category does not exist"() {
        when: "deleteCategory is called with a non-existing category"
        controller.deleteCategory("Unknown")

        then: "service returns false and not found message is shown"
        1 * service.deleteCategory("Unknown") >> false
        1 * userInterface.showMessage("Category not found.")
    }
}
