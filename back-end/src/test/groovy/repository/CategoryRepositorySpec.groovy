package repository

import spock.lang.Specification
import spock.lang.TempDir
import zg.acelera.repository.CategoryRepositoryImpl
import zg.acelera.domain.Category

import java.nio.file.Files
import java.nio.file.Path

class CategoryRepositorySpec extends Specification {
    @TempDir
    Path tempDir
    CategoryRepositoryImpl repository
    Path filePath
    List<String> lines

    def setup() {
        filePath = tempDir.resolve("categories_test.json")
        repository = new CategoryRepositoryImpl(filePath)
        lines = List.of("Category 1", "Category 2", "Category 3")
    }

    def "findAll should return a list of Category"() {
        given: "Several categories in the file .csv"
        Files.write(filePath, lines)

        when: "findAll is called"
        List<Category> result = repository.findAll()

        then: "The list should contain 3 categories and the data should be correct"
        result.size() == 3
        result[0].name == "Category 1"
        result[1].name == "Category 2"
        result[2].name == "Category 3"
    }

    def "findAll should return an empty list when the file is empty"() {
        given: "An empty file"
        Files.write(filePath, List.of())

        when: "findAll is called"
        List<Category> result = repository.findAll()

        then: "The list should be empty"
        result.isEmpty()
    }

    def "save should persist a category to the file with correct data"() {
        given: "a new category"
        Category category = new Category("New Category")

        when: "save is called"
        repository.save(category)

        then: "the category should be persisted with correct data"
        Files.lines(filePath).count() == 1
        Files.readAllLines(filePath).contains("New Category")
    }

    def "delete should delete a Category with name provided"() {
        given: "a file with three categories"
        Files.write(filePath, lines)

        when: "delete is called with a category name"
        repository.delete("Category 1")

        then: "the category should be removed from the file"
        Files.lines(filePath).count() == 2
        !Files.readAllLines(filePath).contains("Category 1")
    }
}
