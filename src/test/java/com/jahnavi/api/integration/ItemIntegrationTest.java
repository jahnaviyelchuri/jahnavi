package com.jahnavi.api.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jahnavi.api.dto.ItemRequest;
import com.jahnavi.api.model.Item;
import com.jahnavi.api.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Item API Integration Tests")
class ItemIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ItemRepository itemRepository;

    @BeforeEach
    void setUp() {
        itemRepository.deleteAll();
    }

    @Test
    @DisplayName("should perform full CRUD lifecycle")
    void shouldPerformFullCrudLifecycle() throws Exception {
        // Create
        ItemRequest createRequest = new ItemRequest("Integration Item", "Integration Description");
        String createResponse = mockMvc.perform(post("/api/v1/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Integration Item"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andReturn().getResponse().getContentAsString();

        Long itemId = objectMapper.readTree(createResponse).get("id").asLong();

        // Read
        mockMvc.perform(get("/api/v1/items/" + itemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Integration Item"));

        // Update
        ItemRequest updateRequest = new ItemRequest("Updated Integration Item", "Updated Description");
        updateRequest.setStatus("INACTIVE");
        mockMvc.perform(put("/api/v1/items/" + itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Integration Item"))
                .andExpect(jsonPath("$.status").value("INACTIVE"));

        // Delete
        mockMvc.perform(delete("/api/v1/items/" + itemId))
                .andExpect(status().isNoContent());

        // Verify deleted
        mockMvc.perform(get("/api/v1/items/" + itemId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("should return paginated results")
    void shouldReturnPaginatedResults() throws Exception {
        for (int i = 1; i <= 15; i++) {
            Item item = new Item("Item " + i, "Description " + i);
            itemRepository.save(item);
        }

        mockMvc.perform(get("/api/v1/items")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(10))
                .andExpect(jsonPath("$.totalElements").value(15))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(false));
    }

    @Test
    @DisplayName("should search items by keyword")
    void shouldSearchItemsByKeyword() throws Exception {
        itemRepository.save(new Item("Spring Boot Guide", "A guide for Spring"));
        itemRepository.save(new Item("Angular Tutorial", "Learn Angular"));
        itemRepository.save(new Item("React Basics", "Introduction to React"));

        mockMvc.perform(get("/api/v1/items/search")
                        .param("keyword", "Angular"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Angular Tutorial"));
    }

    @Test
    @DisplayName("should filter items by status")
    void shouldFilterItemsByStatus() throws Exception {
        Item activeItem = new Item("Active Item", "Active");
        itemRepository.save(activeItem);

        Item inactiveItem = new Item("Inactive Item", "Inactive");
        inactiveItem.setStatus(com.jahnavi.api.model.ItemStatus.INACTIVE);
        itemRepository.save(inactiveItem);

        mockMvc.perform(get("/api/v1/items/status/ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Active Item"));
    }

    @Test
    @DisplayName("should reject duplicate item names")
    void shouldRejectDuplicateItemNames() throws Exception {
        ItemRequest request = new ItemRequest("Duplicate Item", "First instance");
        mockMvc.perform(post("/api/v1/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("should validate request body")
    void shouldValidateRequestBody() throws Exception {
        ItemRequest invalidRequest = new ItemRequest();
        mockMvc.perform(post("/api/v1/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    @DisplayName("should check health endpoint")
    void shouldCheckHealthEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("should handle item count correctly after operations")
    void shouldHandleItemCountCorrectly() throws Exception {
        assertThat(itemRepository.count()).isZero();

        ItemRequest request = new ItemRequest("Count Test", "Testing count");
        mockMvc.perform(post("/api/v1/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        assertThat(itemRepository.count()).isEqualTo(1);
    }
}
