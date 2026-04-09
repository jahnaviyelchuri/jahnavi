package com.jahnavi.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jahnavi.api.dto.ItemRequest;
import com.jahnavi.api.dto.ItemResponse;
import com.jahnavi.api.dto.PagedResponse;
import com.jahnavi.api.exception.DuplicateResourceException;
import com.jahnavi.api.exception.ResourceNotFoundException;
import com.jahnavi.api.model.ItemStatus;
import com.jahnavi.api.service.ItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
@DisplayName("ItemController Tests")
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemService itemService;

    @Autowired
    private ObjectMapper objectMapper;

    private ItemResponse testResponse;

    @BeforeEach
    void setUp() {
        testResponse = new ItemResponse();
        testResponse.setId(1L);
        testResponse.setName("Test Item");
        testResponse.setDescription("Test Description");
        testResponse.setStatus(ItemStatus.ACTIVE);
        testResponse.setCreatedAt(LocalDateTime.now());
        testResponse.setUpdatedAt(LocalDateTime.now());
    }

    @Nested
    @DisplayName("GET /api/v1/items")
    class GetAllItemsTests {

        @Test
        @DisplayName("should return paginated items")
        void shouldReturnPaginatedItems() throws Exception {
            PagedResponse<ItemResponse> pagedResponse = new PagedResponse<>();
            pagedResponse.setContent(List.of(testResponse));
            pagedResponse.setPageNumber(0);
            pagedResponse.setPageSize(10);
            pagedResponse.setTotalElements(1);
            pagedResponse.setTotalPages(1);
            pagedResponse.setFirst(true);
            pagedResponse.setLast(true);

            when(itemService.getAllItems(any())).thenReturn(pagedResponse);

            mockMvc.perform(get("/api/v1/items")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].name").value("Test Item"))
                    .andExpect(jsonPath("$.totalElements").value(1));
        }

        @Test
        @DisplayName("should use default pagination params")
        void shouldUseDefaultPaginationParams() throws Exception {
            PagedResponse<ItemResponse> pagedResponse = new PagedResponse<>();
            pagedResponse.setContent(List.of());
            pagedResponse.setPageNumber(0);
            pagedResponse.setPageSize(10);
            pagedResponse.setTotalElements(0);
            pagedResponse.setTotalPages(0);
            pagedResponse.setFirst(true);
            pagedResponse.setLast(true);

            when(itemService.getAllItems(any())).thenReturn(pagedResponse);

            mockMvc.perform(get("/api/v1/items"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/items/{id}")
    class GetItemByIdTests {

        @Test
        @DisplayName("should return item when found")
        void shouldReturnItemWhenFound() throws Exception {
            when(itemService.getItemById(1L)).thenReturn(testResponse);

            mockMvc.perform(get("/api/v1/items/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Test Item"))
                    .andExpect(jsonPath("$.status").value("ACTIVE"));
        }

        @Test
        @DisplayName("should return 404 when item not found")
        void shouldReturn404WhenNotFound() throws Exception {
            when(itemService.getItemById(99L))
                    .thenThrow(new ResourceNotFoundException("Item", "id", 99L));

            mockMvc.perform(get("/api/v1/items/99"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Item not found with id: '99'"));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/items")
    class CreateItemTests {

        @Test
        @DisplayName("should create item and return 201")
        void shouldCreateItem() throws Exception {
            ItemRequest request = new ItemRequest("New Item", "New Description");
            when(itemService.createItem(any(ItemRequest.class))).thenReturn(testResponse);

            mockMvc.perform(post("/api/v1/items")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name").value("Test Item"));
        }

        @Test
        @DisplayName("should return 400 when name is blank")
        void shouldReturn400WhenNameIsBlank() throws Exception {
            ItemRequest request = new ItemRequest("", "Description");

            mockMvc.perform(post("/api/v1/items")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 400 when name is null")
        void shouldReturn400WhenNameIsNull() throws Exception {
            ItemRequest request = new ItemRequest();
            request.setDescription("Description");

            mockMvc.perform(post("/api/v1/items")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 409 when duplicate name")
        void shouldReturn409WhenDuplicate() throws Exception {
            ItemRequest request = new ItemRequest("Duplicate", "Description");
            when(itemService.createItem(any(ItemRequest.class)))
                    .thenThrow(new DuplicateResourceException("Item", "name", "Duplicate"));

            mockMvc.perform(post("/api/v1/items")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/items/{id}")
    class UpdateItemTests {

        @Test
        @DisplayName("should update item successfully")
        void shouldUpdateItem() throws Exception {
            ItemRequest request = new ItemRequest("Updated", "Updated Desc");
            when(itemService.updateItem(eq(1L), any(ItemRequest.class))).thenReturn(testResponse);

            mockMvc.perform(put("/api/v1/items/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("should return 404 when updating non-existent item")
        void shouldReturn404WhenNotFound() throws Exception {
            ItemRequest request = new ItemRequest("Updated", "Updated Desc");
            when(itemService.updateItem(eq(99L), any(ItemRequest.class)))
                    .thenThrow(new ResourceNotFoundException("Item", "id", 99L));

            mockMvc.perform(put("/api/v1/items/99")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/items/{id}")
    class DeleteItemTests {

        @Test
        @DisplayName("should delete item and return 204")
        void shouldDeleteItem() throws Exception {
            doNothing().when(itemService).deleteItem(1L);

            mockMvc.perform(delete("/api/v1/items/1"))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("should return 404 when deleting non-existent item")
        void shouldReturn404WhenNotFound() throws Exception {
            doThrow(new ResourceNotFoundException("Item", "id", 99L))
                    .when(itemService).deleteItem(99L);

            mockMvc.perform(delete("/api/v1/items/99"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/items/status/{status}")
    class GetItemsByStatusTests {

        @Test
        @DisplayName("should return items by status")
        void shouldReturnItemsByStatus() throws Exception {
            PagedResponse<ItemResponse> pagedResponse = new PagedResponse<>();
            pagedResponse.setContent(List.of(testResponse));
            pagedResponse.setPageNumber(0);
            pagedResponse.setPageSize(10);
            pagedResponse.setTotalElements(1);
            pagedResponse.setTotalPages(1);
            pagedResponse.setFirst(true);
            pagedResponse.setLast(true);

            when(itemService.getItemsByStatus(eq(ItemStatus.ACTIVE), any())).thenReturn(pagedResponse);

            mockMvc.perform(get("/api/v1/items/status/ACTIVE"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].status").value("ACTIVE"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/items/search")
    class SearchItemsTests {

        @Test
        @DisplayName("should return items matching keyword")
        void shouldReturnMatchingItems() throws Exception {
            PagedResponse<ItemResponse> pagedResponse = new PagedResponse<>();
            pagedResponse.setContent(List.of(testResponse));
            pagedResponse.setPageNumber(0);
            pagedResponse.setPageSize(10);
            pagedResponse.setTotalElements(1);
            pagedResponse.setTotalPages(1);
            pagedResponse.setFirst(true);
            pagedResponse.setLast(true);

            when(itemService.searchItems(eq("Test"), any())).thenReturn(pagedResponse);

            mockMvc.perform(get("/api/v1/items/search")
                            .param("keyword", "Test"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].name").value("Test Item"));
        }
    }
}
