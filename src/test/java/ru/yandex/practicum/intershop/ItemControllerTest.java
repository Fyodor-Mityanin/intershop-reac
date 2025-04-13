package ru.yandex.practicum.intershop;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.yandex.practicum.intershop.controller.ItemController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class ItemControllerTest extends TestContainerTest {

    @Autowired
    private ItemController itemController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(itemController)
                .build();
    }

    @Test
    @Sql({"sql/items.sql", "sql/orders.sql", "sql/order_items.sql",})
    void getItemById_shouldReturnHtmlWithItem() throws Exception {
        mockMvc.perform(get("/items/2"))
                .andExpect(status().isOk());
    }

    @Test
    @Sql({"sql/items.sql", "sql/orders.sql", "sql/order_items.sql",})
    void addToCart_shouldReturnHtmlWithItem() throws Exception {
        mockMvc.perform(
                        post("/items/2")
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                .param("action", "plus")
                )
                .andExpect(status().is3xxRedirection());
    }
}