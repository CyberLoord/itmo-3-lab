package org.itmo.testing.lab3.integration;

import io.javalin.Javalin;
import io.restassured.RestAssured;
import org.itmo.testing.lab3.controller.UserAnalyticsController;
import org.junit.jupiter.api.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import java.time.LocalDateTime;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserAnalyticsIntegrationTest {

    private Javalin app;
    private int port = 9000;

    @BeforeAll
    void setUp() {
        app = UserAnalyticsController.createApp();
        app.start(port);
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    @AfterAll
    void tearDown() {
        app.stop();
    }

    @Test
    @Order(1)
    @DisplayName("Тест регистрации пользователя")
    void testUserRegistration() {
        given()
                .queryParam("userId", "user1")
                .queryParam("userName", "Alice")
                .when()
                .post("/register")
                .then()
                .statusCode(200)
                .body(equalTo("User registered: true"));
    }

    @Test
    @Order(2)
    @DisplayName("Тест записи сессии")
    void testRecordSession() {
        LocalDateTime now = LocalDateTime.now();
        given()
                .queryParam("userId", "user1")
                .queryParam("loginTime", now.minusHours(100).toString())
                .queryParam("logoutTime", now.minusHours(70).toString())
                .when()
                .post("/recordSession")
                .then()
                .statusCode(200)
                .body(equalTo("Session recorded"));
    }

    @Test
    @Order(3)
    @DisplayName("Тест получения общего времени активности")
    void testGetTotalActivity() {
        given()
                .queryParam("userId", "user1")
                .when()
                .get("/totalActivity")
                .then()
                .statusCode(200)
                .body(containsString("Total activity:"))
                .body(containsString("1800 minutes"));
    }

    @Test
    @Order(4)
    @DisplayName("Тест регистрации пользователя без userId")
    void testUserRegistrationMissingUserId() {
        given()
                .queryParam("userName", "Alice")
                .when()
                .post("/register")
                .then()
                .statusCode(400)
                .body(equalTo("Missing parameters"));
    }

    @Test
    @Order(5)
    @DisplayName("Тест регистрации пользователя без userName")
    void testUserRegistrationMissingUserName() {
        given()
                .queryParam("userId", "user2")
                .when()
                .post("/register")
                .then()
                .statusCode(400)
                .body(equalTo("Missing parameters"));
    }

    @Test
    @Order(6)
    @DisplayName("Тест записи сессии без userId")
    void testRecordSessionMissingUserId() {
        LocalDateTime now = LocalDateTime.now();
        given()
                .queryParam("loginTime", now.minusHours(1).toString())
                .queryParam("logoutTime", now.toString())
                .when()
                .post("/recordSession")
                .then()
                .statusCode(400)
                .body(equalTo("Missing parameters"));
    }

    @Test
    @Order(7)
    @DisplayName("Тест записи сессии с некорректным loginTime")
    void testRecordSessionInvalidLoginTime() {
        given()
                .queryParam("userId", "user1")
                .queryParam("loginTime", "invalid-time")
                .queryParam("logoutTime", LocalDateTime.now().toString())
                .when()
                .post("/recordSession")
                .then()
                .statusCode(400)
                .body(containsString("Invalid data"));
    }

    @Test
    @Order(8)
    @DisplayName("Тест записи сессии с некорректным logoutTime")
    void testRecordSessionInvalidLogoutTime() {
        given()
                .queryParam("userId", "user1")
                .queryParam("loginTime", LocalDateTime.now().minusHours(1).toString())
                .queryParam("logoutTime", "invalid-time")
                .when()
                .post("/recordSession")
                .then()
                .statusCode(400)
                .body(containsString("Invalid data"));
    }

    @Test
    @Order(9)
    @DisplayName("Тест получения общего времени активности без userId")
    void testGetTotalActivityMissingUserId() {
        given()
                .when()
                .get("/totalActivity")
                .then()
                .statusCode(400)
                .body(equalTo("Missing userId"));
    }

    @Test
    @Order(10)
    @DisplayName("Тест получения списка неактивных пользователей")
    void testGetInactiveUsers() {
        given()
                .queryParam("days", "1")
                .when()
                .get("/inactiveUsers")
                .then()
                .statusCode(200)
                .body(containsString("user1"));
    }

    @Test
    @Order(11)
    @DisplayName("Тест получения списка неактивных пользователей без параметра days")
    void testGetInactiveUsersMissingDays() {
        given()
                .when()
                .get("/inactiveUsers")
                .then()
                .statusCode(400)
                .body(equalTo("Missing days parameter"));
    }

    @Test
    @Order(12)
    @DisplayName("Тест получения списка неактивных пользователей с некорректным days")
    void testGetInactiveUsersInvalidDays() {
        given()
                .queryParam("days", "invalid")
                .when()
                .get("/inactiveUsers")
                .then()
                .statusCode(400)
                .body(equalTo("Invalid number format for days"));
    }

    @Test
    @Order(13)
    @DisplayName("Тест получения метрики активности за месяц")
    void testGetMonthlyActivity() {
        given()
                .queryParam("userId", "user1")
                .queryParam("month", "2025-03")
                .when()
                .get("/monthlyActivity")
                .then()
                .statusCode(200)
                .body(containsString("\"2025-03-06\":1800"));
    }

    @Test
    @Order(14)
    @DisplayName("Тест получения метрики активности за месяц без параметра userId")
    void testGetMonthlyActivityMissingUserId() {
        given()
                .queryParam("month", "2025-03")
                .when()
                .get("/monthlyActivity")
                .then()
                .statusCode(400)
                .body(equalTo("Missing parameters"));
    }

    @Test
    @Order(15)
    @DisplayName("Тест получения метрики активности за месяц с некорректным month")
    void testGetMonthlyActivityInvalidMonth() {
        given()
                .queryParam("userId", "user1")
                .queryParam("month", "invalid-month")
                .when()
                .get("/monthlyActivity")
                .then()
                .statusCode(400)
                .body(containsString("Invalid data"));
    }

    @Test
    @Order(16)
    @DisplayName("Тест повторной регистрации пользователя")
    void testUserRegistrationDublicated() {
        given()
                .queryParam("userId", "user1")
                .queryParam("userName", "Leha")
                .when()
                .post("/register")
                .then()
                .statusCode(400)
                .body(equalTo("User with this id already exists"));
    }

    @Test
    @Order(16)
    @DisplayName("Тест регистрации пользователя Leha")
    void testUserRegistrationLeha() {
        given()
                .queryParam("userId", "user2")
                .queryParam("userName", "Leha")
                .when()
                .post("/register")
                .then()
                .statusCode(200)
                .body(equalTo("User registered: true"));
    }

    @Test
    @Order(2)
    @DisplayName("Тест записи некорректной сессии")
    void testRecordSessionWrong() {
        LocalDateTime now = LocalDateTime.now();
        given()
                .queryParam("userId", "user2")
                .queryParam("loginTime", now.minusHours(70).toString())
                .queryParam("loginTime", now.minusHours(100).toString())
                .when()
                .post("/recordSession")
                .then()
                .statusCode(400)
                .body(equalTo("Wrong session, loginTime must be earlier then loginTime"));
    }

    @Test
    @Order(10)
    @DisplayName("Тест получения списка неактивных пользователей за отрицательный период")
    void testGetInactiveUsersWrongParams() {
        given()
                .queryParam("days", "-1")
                .when()
                .get("/inactiveUsers")
                .then()
                .statusCode(400)
                .body(containsString("The number of days must be non-negative"));
    }
}
