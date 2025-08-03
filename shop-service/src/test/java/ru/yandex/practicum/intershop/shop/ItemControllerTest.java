package ru.yandex.practicum.intershop.shop;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.reactive.server.WebTestClient;


import java.util.List;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.*;

@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class ItemControllerTest extends TestContainerTest {

    @Autowired
    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        cleanupDatabase();
        executeSqlScriptsBlocking(List.of("/sql/items.sql", "/sql/users.sql", "/sql/orders.sql", "/sql/order_items.sql"));
    }

    @Test
    void getItemById_shouldReturnHtmlWithItem() {
        webTestClient.get()
                .uri("/items/2")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML);
    }

    @Test
    void addToCart_shouldReturnRedirect() {
        webTestClient.mutateWith(mockUser("user1").roles("USER"))
                .post()
                .uri("/items/2")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("action=plus")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueMatches("Location", "/.*");
    }
}