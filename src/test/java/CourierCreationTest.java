import io.qameta.allure.*;
import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import static org.hamcrest.Matchers.*;
import java.util.stream.Stream;
import static io.restassured.RestAssured.*;

@Epic("Courier API")
@Feature("Создание курьеров")
public class CourierCreationTest {

    private String createdCourierId;

    @BeforeEach
    void setup() {
        RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru";
    }

    @Test
    @DisplayName("Успешное создание курьера")
    @Story("Позитивный сценарий")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Проверка успешного создания курьера с валидными данными")
    void shouldCreateCourierWithValidData() {
        Courier validCourier = new Courier();
        validCourier.setLogin("test_login_" + System.currentTimeMillis());
        validCourier.setPassword("securePass123");
        validCourier.setFirstName("Иван Иванов");

        ValidatableResponse response = sendCreateRequest(validCourier)
                .statusCode(201)
                .body("ok", equalTo(true));

        // Установка ID созданного курьера для последующей очистки
        createdCourierId = response.extract().path("id");
    }

    @ParameterizedTest(name = "Отсутствие поля: {1}")
    @MethodSource("provideInvalidCouriers")
    @DisplayName("Создание курьера без обязательных полей")
    @Story("Негативные сценарии")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Проверка ошибок при создании курьера с отсутствующими обязательными полями")
    void shouldFailWhenRequiredFieldMissing(Courier invalidCourier, String missingField, int expectedStatusCode, String expectedMessage) {
        ValidatableResponse response = sendCreateRequest(invalidCourier);

        if (expectedMessage != null) {
            response.statusCode(expectedStatusCode)
                    .body("message", equalTo(expectedMessage));
        } else {
            response.statusCode(expectedStatusCode);
        }
    }

    @Test
    @DisplayName("Создание дубликата курьера")
    @Story("Конфликт данных")
    @Severity(SeverityLevel.NORMAL)
    @Description("Проверка ошибки при попытке создания курьера с уже существующим логином")
    void shouldPreventDuplicateCourierCreation() {
        Courier originalCourier = createUniqueCourier();
        sendCreateRequest(originalCourier).statusCode(201);

        sendCreateRequest(originalCourier)
                .statusCode(409)
                .body("message", equalTo("Этот логин уже используется. Попробуйте другой."));
    }

    @AfterEach
    @Step("Очистка тестовых данных")
    void cleanup() {
        if (createdCourierId != null) {
            deleteCourier(createdCourierId);
        }
    }

    private static Stream<Arguments> provideInvalidCouriers() {
        long timestamp = System.currentTimeMillis();

        Courier courier1 = new Courier();
        courier1.setLogin(""); // Пустой логин
        courier1.setPassword("pass");
        courier1.setFirstName("Name");

        Courier courier2 = new Courier();
        courier2.setLogin("login_" + timestamp);
        courier2.setPassword(""); // Пустой пароль
        courier2.setFirstName("Name");

        Courier courier3 = new Courier();
        courier3.setLogin("login_" + timestamp);
        courier3.setPassword("pass");
        courier3.setFirstName(""); // Пустое имя

        return Stream.of(
                Arguments.of(courier1, "логин", 400, "Недостаточно данных для создания учетной записи"),
                Arguments.of(courier2, "пароль", 400, "Недостаточно данных для создания учетной записи"),
                Arguments.of(courier3, "имя", 201, null) // Имя не обязательно - ожидаем 201
        );
    }

    @Step("Отправка POST запроса на создание курьера с логином: {courier.login}")
    private ValidatableResponse sendCreateRequest(Courier courier) {
        String CREATE_ENDPOINT = "/api/v1/courier";
        return given()
                .contentType("application/json")
                .body(courier)
                .when()
                .post(CREATE_ENDPOINT)
                .then();
    }

    @Step("Создание уникального курьера")
    private Courier createUniqueCourier() {
        Courier courier = new Courier();
        courier.setLogin("unique_" + System.currentTimeMillis());
        courier.setPassword("password123");
        courier.setFirstName("Уникальный Курьер");
        return courier;
    }

    @Step("Удаление курьера с ID: {courierId}")
    private void deleteCourier(String courierId) {
        given()
                .pathParam("id", courierId)
                .when()
                .delete("/api/v1/courier/{id}")
                .then()
                .statusCode(200);
    }

    @SuppressWarnings("unused")
    static class Courier {
        private String login;
        private String password;
        private String firstName;

        // Геттеры и сеттеры
        public String getLogin() { return login; }
        public void setLogin(String login) { this.login = login; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }

        @Override
        public String toString() {
            return "Courier{" +
                    "login='" + login + '\'' +
                    ", password='" + password + '\'' +
                    ", firstName='" + firstName + '\'' +
                    '}';
        }
    }
}