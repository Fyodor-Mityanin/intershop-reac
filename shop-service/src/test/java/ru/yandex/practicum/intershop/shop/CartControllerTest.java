package ru.yandex.practicum.intershop.shop;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockUser;

@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class CartControllerTest extends TestContainerTest {

    @Autowired
    private WebTestClient webTestClient;

    private static WireMockServer wireMockServer;

    @BeforeAll
    static void setupWireMock() {
        wireMockServer = new WireMockServer(9999);
        wireMockServer.start();
    }

    @AfterAll
    static void stopWireMock() {
        wireMockServer.stop();
    }


    @BeforeEach
    void setUp() {
        cleanupDatabase();
        executeSqlScriptsBlocking(List.of("/sql/items.sql", "/sql/users.sql", "/sql/orders.sql", "/sql/order_items.sql"));
    }

    @Test
    void getCartTest() {
        wireMockServer.stubFor(get(urlEqualTo("/api/payment/balance"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"balance\": 10000.0}")
                        .withStatus(200)));

        webTestClient.mutateWith(mockUser("user1").roles("USER"))
                .get()
                .uri("/cart/items")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML);
    }

    @Test
    void addToCartTest() {
        webTestClient.mutateWith(mockUser("user1").roles("USER"))
                .post()
                .uri("/cart/items/2")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("action=plus")
                .exchange()
                .expectStatus().is3xxRedirection();
    }
}
