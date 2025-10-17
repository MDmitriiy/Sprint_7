import io.qameta.allure.*;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.List;
import java.util.stream.Stream;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Epic("Order API")
@Feature("Создание заказов")
public class OrderTest {

    @BeforeEach
    void setup() {
        RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru";
    }

    @ParameterizedTest
    @DisplayName("Создание заказа с разными цветами")
    @MethodSource("orderColorVariations")
    @Story("Создание заказа")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Проверка создания заказа с различными комбинациями цветов самоката")
    void createOrderWithDifferentColorOptions(List<String> colors) {
        Order order = new Order(
                "Naruto",
                "Uchiha",
                "Konoha, 142 apt.",
                "4",
                "+7 800 355 35 35",
                5,
                "2020-06-06",
                "Saske, come back to Konoha",
                colors
        );

        sendCreateOrderRequest(order)
                .statusCode(201)
                .body("track", allOf(
                        notNullValue(),
                        greaterThan(0),
                        instanceOf(Integer.class)
                ));
    }

    private static Stream<Arguments> orderColorVariations() {
        return Stream.of(
                // Все возможные комбинации цветов
                Arguments.of(List.of("BLACK")),
                Arguments.of(List.of("GREY")),
                Arguments.of(List.of("BLACK", "GREY")),
                Arguments.of((Object) null),
                Arguments.of(List.of())
        );
    }

    @Step("Отправка POST запроса на создание заказа с цветами: {order.color}")
    private ValidatableResponse sendCreateOrderRequest(Order order) {
        String CREATE_ENDPOINT = "/api/v1/orders";
        return given()
                .contentType(ContentType.JSON)
                .body(order)
                .when()
                .post(CREATE_ENDPOINT)
                .then();
    }

    @SuppressWarnings("unused")
    static class Order {
        private final String firstName;
        private final String lastName;
        private final String address;
        private final String metroStation;
        private final String phone;
        private final int rentTime;
        private final String deliveryDate;
        private final String comment;
        private final List<String> color;

        public Order(String firstName, String lastName, String address,
                     String metroStation, String phone, int rentTime,
                     String deliveryDate, String comment, List<String> color) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.address = address;
            this.metroStation = metroStation;
            this.phone = phone;
            this.rentTime = rentTime;
            this.deliveryDate = deliveryDate;
            this.comment = comment;
            this.color = color;
        }

        public String getFirstName() { return firstName; }
        public String getLastName() { return lastName; }
        public String getAddress() { return address; }
        public String getMetroStation() { return metroStation; }
        public String getPhone() { return phone; }
        public int getRentTime() { return rentTime; }
        public String getDeliveryDate() { return deliveryDate; }
        public String getComment() { return comment; }
        public List<String> getColor() { return color; }
    }
}