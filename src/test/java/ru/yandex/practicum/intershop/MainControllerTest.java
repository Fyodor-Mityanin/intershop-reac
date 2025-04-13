package ru.yandex.practicum.intershop;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.yandex.practicum.intershop.controller.MainController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class MainControllerTest extends TestContainerTest {

    @Autowired
    private MainController mainController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(mainController)
                .build();
    }

    @Test
    @Sql({
            "sql/items.sql",
            "sql/orders.sql",
            "sql/order_items.sql",
    })
    void indexTest() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
    }

    @Test
    @Sql({
            "sql/items.sql",
            "sql/orders.sql",
            "sql/order_items.sql",
    })
    void addToCartMainTest() throws Exception {
        mockMvc.perform(
                post("/main/items/2")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .param("action", "plus")
                )
                .andExpect(status().is3xxRedirection());
    }
}