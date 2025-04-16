package ru.yandex.practicum.intershop;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

class MainControllerTest extends TestContainerTest {

    @Autowired
    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        executeSqlScriptsBlocking(List.of("/sql/items.sql", "/sql/orders.sql", "/sql/order_items.sql"));
    }

    @Test
    void indexTest() {
        webTestClient.get()
                .uri("/")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML);
    }

    @Test
    void addToCartMainTest() {
        webTestClient.post()
                .uri("/main/items/2")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("action=plus")
                .exchange()
                .expectStatus().is3xxRedirection();
    }
}