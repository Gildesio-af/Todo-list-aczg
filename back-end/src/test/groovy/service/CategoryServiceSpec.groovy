package service


import spock.lang.Specification
import zg.acelera.domain.Category
import zg.acelera.repository.CategoryRepository
import zg.acelera.service.CategoryService

class CategoryServiceSpec extends Specification{
    CategoryService categoryService
    CategoryRepository categoryRepository
    Category existingCategory
    List<Category> categories

    def setup() {
        categoryRepository = Mock()
        categoryService = new CategoryService(categoryRepository)
        existingCategory = new Category("Existing Category")
        categories = [existingCategory]
    }

    def "createCategory should return false when already exists a category with the same name"() {
        given: "a category with the same name already exists"
        categoryRepository.findAll() >> categories

        when: "service try to create a new category with the same name"
        boolean result = categoryService.createCategory("Existing Category")

        then: "the result should be false"
        !result
    }

    def "createCategory should return false when new name is: #newName"() {
        when: "service try to create a new category with a null name"
        boolean result = categoryService.createCategory(newName)

        then: "the result should be false"
        result == expected

        where:
        newName || expected
        null    || false
        "   "   || false
        ""      || false
    }

    def "createCategory should return false when a category with the same name exists but with different case"() {
        given: "a list of categories with 'Existing Category'"
        categoryRepository.findAll() >> categories

        when: "service tries to create 'EXISTING CATEGORY'"
        boolean result = categoryService.createCategory("EXISTING CATEGORY")

        then: "the result should be false"
        !result
    }

    def "createCategory should throw an exception when repository method fails"() {
        when: "service try to create a new category and the repository method fails"
        categoryService.createCategory("New Category")

        then: "an exception should be thrown"
        1 * categoryRepository.findAll() >> categories
        1 * categoryRepository.save({ it.name == "New Category" }) >> { throw new IOException() }

        RuntimeException e = thrown(RuntimeException)
        e.getCause() instanceof IOException
        e.getMessage() == "Error creating category"
    }

    def "createCategory should return true when a new valid name is provided"() {
        given: "a list of categories exists"
        categoryRepository.findAll() >> categories

        when: "service try to create a new category with a valid name"
        boolean result = categoryService.createCategory("New Category")

        then: "the result should be true and the category should be saved"
        1 * categoryRepository.save({ it.name == "New Category" })
        result
    }

    def "listCategories should return all categories"() {
        when: "service is called to list all categories"
        List<Category> result = categoryService.listCategories()

        then: "the result should be the list of categories"
        1 * categoryRepository.findAll() >> categories
        result == categories
        result.size() == categories.size()
    }

    def "listCategories should throw an RuntimeException when repository method fails"() {
        when: "service is called to list all categories and the repository method fails"
        categoryService.listCategories()

        then: "an exception should be thrown"
        1 * categoryRepository.findAll() >> { throw new IOException() }
        RuntimeException e = thrown(RuntimeException)
        e.getCause() instanceof IOException
        e.getMessage() == "Error listing categories"
    }

    def "deleteCategory should return true when category is successfully deleted"() {
        given: "a category name exists"
        String existingCategory = "ACZG"

        when: "deleteCategory in service is called"
        boolean result = categoryService.deleteCategory(existingCategory)

        then: "result should be true"
        1 * categoryRepository.delete(existingCategory) >> true
        result
    }

    def "deleteCategory should return false when category name doesn't exist"() {
        given: "a category name doesn't exist"
        String nonExistingCategory = "Non Existing Category"

        when: "deleteCategory in service is called"
        boolean result = categoryService.deleteCategory(nonExistingCategory)

        then: "result should be false"
        1 * categoryRepository.delete(nonExistingCategory) >> false
        !result
    }

    def "deleteCategory should throw an RuntimeException when repository method fails"() {
        given: "a category name exists"
        String existingCategory = "ACZG"

        when: "deleteCategory in service is called and the repository method fails"
        categoryService.deleteCategory(existingCategory)

        then: "result should be false"
        1 * categoryRepository.delete(existingCategory) >> { throw new IOException()}
        RuntimeException e = thrown(RuntimeException)
        e.getCause() instanceof IOException
        e.getMessage() == "Error deleting category"
    }
 }