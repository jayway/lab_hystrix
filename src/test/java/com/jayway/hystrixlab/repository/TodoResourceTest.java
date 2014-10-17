package com.jayway.hystrixlab.repository;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.builder.ResponseSpecBuilder;
import org.junit.*;

import static com.jayway.restassured.RestAssured.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

public class TodoResourceTest {

    @ClassRule
    public static HystrixLabServerRule hystrixLabServer = new HystrixLabServerRule();

    private TodoRepository todoRepository;

    @BeforeClass public static void
    rest_assured_is_configured_with_port_to_hystrix_lab_server_before_tests() {
        RestAssured.port = hystrixLabServer.getPort();
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        RestAssured.basePath = "/todos";
        RestAssured.responseSpecification = new ResponseSpecBuilder().expectStatusCode(anyOf(greaterThanOrEqualTo(200), lessThan(300))).build();
    }

    @Before public void
    todo_repository_is_initialized_before_each_test() {
        todoRepository = hystrixLabServer.todoRepository();
    }

    @AfterClass public static void
    rest_assured_is_reset_after_tests() {
        RestAssured.reset();
    }

    @After public void
    mongo_is_cleared_after_each_test() {
        todoRepository.clear();
    }

    @Test(timeout = 2000) public void
    create_todo_returns_id_and_todo() {
        given().
                formParam("todo", "Implement Hystrix Lab").
        when().
                post().
        then().
                body("_id", allOf(not(isEmptyOrNullString()), instanceOf(String.class))).
                body("todo", equalTo("Implement Hystrix Lab"));
    }

    @Test(timeout = 2000) public void
    create_todo_stores_todo_in_mongo() {
        String todoId = given().formParam("todo", "Implement Hystrix Lab").when().post().then().extract().path("_id");

        assertThat(todoRepository.hasTodo(todoId)).isTrue();
    }

    @Test(timeout = 2000) public void
    delete_todo_removes_todo_from_repository() {
        // Given
        String todoId = given().formParam("todo", "Implement Hystrix Lab").when().post().then().extract().path("_id");

        // When
        delete(todoId);

        // Then
        assertThat(todoRepository.hasTodo(todoId)).isFalse();
    }

    @Test(timeout = 2000) public void
    find_todo_retrieves_the_todo() {
        // Given
        String todoId = given().formParam("todo", "Implement Hystrix Lab").when().post().then().extract().path("_id");

        // When / Then
        when().
                get(todoId).
        then().
                body("_id", equalTo(todoId)).
                body("todo", equalTo("Implement Hystrix Lab"));
    }

    @Test(timeout = 2000) public void
    find_all_todos_retrieves_all_todos() {
        // Given
        String todoId1 = given().formParam("todo", "Implement Hystrix Lab").when().post().then().extract().path("_id");
        String todoId2 = given().formParam("todo", "Improve Hystrix Lab").when().post().then().extract().path("_id");
        String todoId3 = given().formParam("todo", "Improve Hystrix Lab further").when().post().then().extract().path("_id");

        // When / Then
        when().
                get().
        then().
                body("size()", is(3)).
                body("_id", containsInAnyOrder(todoId1, todoId2, todoId3));
    }
}
