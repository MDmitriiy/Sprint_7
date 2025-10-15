import io.qameta.allure.*;
import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Epic("Order API")
@Feature("Order List")
public class OrderListTest {

    @BeforeEach
    void setup() {
        RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru";
    }

    @Test
    @DisplayName("Получение корректной структуры ответа заказа")
    @Story("Получение списка заказов")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Проверка корректности структуры ответа при получении списка заказов")
    void getOrdersListReturnsValidStructure() {
        //noinspection deprecation
        sendGetOrdersRequest()
                .statusCode(200)
                // Проверка основной структуры ответа
                .body("orders", not(empty()))
                .body("pageInfo", notNullValue())
                .body("availableStations", not(empty()))

                // Проверка структуры заказов
                .body("orders.id", everyItem(notNullValue()))
                .body("orders.firstName", everyItem(not(emptyOrNullString())))
                .body("orders.lastName", everyItem(not(emptyOrNullString())))
                .body("orders.track", everyItem(allOf(
                        notNullValue(),
                        greaterThan(0)
                )))
                .body("orders.deliveryDate", everyItem(matchesRegex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z")))

                // Проверка цветов
                .body("orders.color", everyItem(
                        anyOf(
                                nullValue(),
                                everyItem(isOneOf("BLACK", "GREY", "black", "grey", "gray", "GRAY"))
                        )
                ))

                // Проверка страничной информации
                .body("pageInfo.page", equalTo(0))
                .body("pageInfo.total", greaterThanOrEqualTo(2))
                .body("pageInfo.limit", equalTo(30))

                // Проверка станций
                .body("availableStations.name", everyItem(not(emptyOrNullString())))
                .body("availableStations.number", everyItem(matchesRegex("\\d+")))
                .body("availableStations.color", everyItem(matchesRegex("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$")));
    }

    @Step("Отправка GET запроса на получение списка заказов")
    private ValidatableResponse sendGetOrdersRequest() {
        return given()
                .when()
                .get("/api/v1/orders")
                .then();
    }
}
