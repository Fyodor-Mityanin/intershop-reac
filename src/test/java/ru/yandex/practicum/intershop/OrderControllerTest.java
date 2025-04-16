package ru.yandex.practicum.intershop;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

public class OrderControllerTest extends TestContainerTest {

    @Autowired
    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        executeSqlScriptsBlocking(List.of("/sql/items.sql", "/sql/orders.sql", "/sql/order_items.sql"));
    }

    @Test
    void getOrderTest() {
        webTestClient.get()
                .uri("/orders/10")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML);
    }
}
