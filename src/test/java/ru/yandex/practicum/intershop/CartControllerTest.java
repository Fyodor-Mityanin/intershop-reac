package ru.yandex.practicum.intershop;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.yandex.practicum.intershop.controller.CartController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class CartControllerTest extends TestContainerTest {

    @Autowired
    private CartController cartController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(cartController)
                .build();
    }

    @Test
    @Sql({"sql/items.sql", "sql/orders.sql", "sql/order_items.sql"})
    void getCartTest() throws Exception {
        mockMvc.perform(get("/cart/items"))
                .andExpect(status().isOk());
    }

    @Test
    @Sql({"sql/items.sql", "sql/orders.sql", "sql/order_items.sql"})
    void addToCartTest() throws Exception {
        mockMvc.perform(
                        post("/cart/items/2")
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                .param("action", "plus")
                )
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @Sql({"sql/items.sql", "sql/orders.sql", "sql/order_items.sql"})
    void buyTest() throws Exception {
        mockMvc.perform(post("/cart/items/buy"))
                .andExpect(status().is3xxRedirection());
    }
}
