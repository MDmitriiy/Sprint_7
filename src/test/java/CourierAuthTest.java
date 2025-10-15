import io.qameta.allure.*;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.stream.Stream;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Epic("Courier API")
@Feature("Авторизация курьеров")
public class CourierAuthTest {

    private Courier testCourier;

    @BeforeEach
    void setup() {
        RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru";
        // Создаем тестового курьера перед каждым тестом
        createTestCourier();
    }

    // Создание тестового курьера
    @Step("Создание тестового курьера")
    private void createTestCourier() {
        testCourier = new Courier(
                "test_courier_" + System.currentTimeMillis(),
                "test_password_123"
        );

        // Отправляем запрос на создание курьера
        given()
                .contentType(ContentType.JSON)
                .body(testCourier)
                .when()
                .post("/api/v1/courier")
                .then()
                .statusCode(201);
    }

    // Позитивный тест: успешная авторизация
    @Test
    @DisplayName("Успешная авторизация курьера")
    @Story("Успешная авторизация")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Проверка успешной авторизации курьера с валидными учетными данными")
    void successfulLoginReturnsId() {
        sendAuthRequest(testCourier)
                .statusCode(200)
                .body("id", allOf(
                        notNullValue(),
                        greaterThan(0)
                ));
    }

    // Параметризованные негативные тесты
    @ParameterizedTest
    @DisplayName("Ошибка авторизации")
    @MethodSource("authErrorCases")
    @Story("Ошибки авторизации")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Проверка различных сценариев ошибок авторизации")
    void authorizationErrorScenarios(Courier courier, int statusCode, String errorMessage) {
        sendAuthRequest(courier)
                .statusCode(statusCode)
                .body("message", equalTo(errorMessage));
    }

    private static Stream<Arguments> authErrorCases() {
        return Stream.of(
                // Отсутствие логина
                Arguments.of(new Courier(null, "pass123"), 400, "Недостаточно данных для входа"),
                // Отсутствие пароля
                Arguments.of(new Courier("user123", null), 400, "Недостаточно данных для входа"),
                // Неверный пароль
                Arguments.of(new Courier("existing_user", "wrong_pass"), 404, "Учетная запись не найдена"),
                // Несуществующий пользователь
                Arguments.of(new Courier("ghost_user", "any_pass"), 404, "Учетная запись не найдена")
        );
    }

    // Тест на неверные учетные данные
    @Test
    @DisplayName("Неверные данные логина")
    @Story("Ошибки авторизации")
    @Severity(SeverityLevel.NORMAL)
    @Description("Проверка авторизации с неверным паролем")
    void invalidCredentialsReturnError() {
        Courier invalidCourier = new Courier(testCourier.getLogin(), "invalid_password");

        sendAuthRequest(invalidCourier)
                .statusCode(404)
                .body("message", equalTo("Учетная запись не найдена"));
    }

    // Вспомогательный метод для отправки запроса авторизации
    @Step("Отправка запроса авторизации для курьера с логином: {courier.login}")
    private ValidatableResponse sendAuthRequest(Courier courier) {
        return given()
                .contentType(ContentType.JSON)
                .body(courier)
                .when()
                .post("/api/v1/courier/login")
                .then();
    }

    // Модель курьера
    @SuppressWarnings("unused")
    static class Courier {
        private String login;
        private String password;

        public Courier() {}

        public Courier(String login, String password) {
            this.login = login;
            this.password = password;
        }

        // Геттеры и сеттеры
        public String getLogin() { return login; }
        public void setLogin(String login) { this.login = login; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}

