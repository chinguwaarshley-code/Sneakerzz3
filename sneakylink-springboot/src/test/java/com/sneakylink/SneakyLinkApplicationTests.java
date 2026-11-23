package com.sneakylink;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sneakylink.dto.AuthRequest;
import com.sneakylink.dto.CartAddRequest;
import com.sneakylink.dto.CheckoutRequest;
import com.sneakylink.model.FulfillmentType;
import com.sneakylink.model.Role;
import com.sneakylink.repository.ShoeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:sneakylink_test;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class SneakyLinkApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ShoeRepository shoeRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void seedsAtLeast45ShoeModels() {
        assertThat(shoeRepository.count()).isGreaterThanOrEqualTo(45);
    }

    @Test
    void filtersCatalogByColorwaySizeRangeAndMaterial() throws Exception {
        mockMvc.perform(get("/api/shoes")
                        .param("primaryColor", "Blue")
                        .param("sizeRange", "US 7-12")
                        .param("material", "Engineered Mesh"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].primaryColor").value("Blue"))
                .andExpect(jsonPath("$[0].material").value("Engineered Mesh"));
    }

    @Test
    void rejectsManagerLoginWithWrongPasswordAndBlocksAdminEndpoint() throws Exception {
        AuthRequest badReq = new AuthRequest("Boss", Role.MANAGER, "WrongPass");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badReq)))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/admin/stats"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void allowsManagerLoginAndAdminStatsAccess() throws Exception {
        MockHttpSession session = new MockHttpSession();
        AuthRequest req = new AuthRequest("Arshley", Role.MANAGER, "SneakyAdmin2026!");

        mockMvc.perform(post("/api/auth/login")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("MANAGER"));

        mockMvc.perform(get("/api/admin/stats").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalModels").value(48));
    }

    @Test
    void completesCustomerCartAndCheckoutWithDynamicFulfillment() throws Exception {
        MockHttpSession session = new MockHttpSession();
        AuthRequest login = new AuthRequest("Alex", Role.CUSTOMER, null);
        mockMvc.perform(post("/api/auth/login")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk());

        Long firstShoeId = shoeRepository.findAll().get(0).getId();
        CartAddRequest addReq = new CartAddRequest(firstShoeId, "10", 1);
        mockMvc.perform(post("/api/cart/items")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalQuantity").value(1));

        CheckoutRequest checkout = new CheckoutRequest(
                "Alex Rivera", "alex@example.com", "+1-555-0199",
                FulfillmentType.STANDARD_DELIVERY,
                "742 Cobalt Blvd", "Metro City", "90210",
                null, "Credit Card", "4242424242424242"
        );
        mockMvc.perform(post("/api/orders/checkout")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(checkout)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderNumber").exists())
                .andExpect(jsonPath("$.deliveryWindowLabel").isNotEmpty());
    }
}
