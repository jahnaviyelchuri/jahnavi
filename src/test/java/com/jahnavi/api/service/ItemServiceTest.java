package com.jahnavi.api.service;

import com.jahnavi.api.dto.ItemRequest;
import com.jahnavi.api.dto.ItemResponse;
import com.jahnavi.api.dto.PagedResponse;
import com.jahnavi.api.exception.DuplicateResourceException;
import com.jahnavi.api.exception.ResourceNotFoundException;
import com.jahnavi.api.model.Item;
import com.jahnavi.api.model.ItemStatus;
import com.jahnavi.api.repository.ItemRepository;
import com.jahnavi.api.service.impl.ItemServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ItemService Tests")
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    private Item testItem;
    private ItemRequest testRequest;

    @BeforeEach
    void setUp() {
        testItem = new Item("Test Item", "Test Description");
        testItem.setId(1L);
        testItem.setStatus(ItemStatus.ACTIVE);
        testItem.setCreatedAt(LocalDateTime.now());
        testItem.setUpdatedAt(LocalDateTime.now());

        testRequest = new ItemRequest("Test Item", "Test Description");
    }

    @Nested
    @DisplayName("getAllItems")
    class GetAllItemsTests {

        @Test
        @DisplayName("should return paginated items")
        void shouldReturnPaginatedItems() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Item> page = new PageImpl<>(List.of(testItem), pageable, 1);
            when(itemRepository.findAll(pageable)).thenReturn(page);

            PagedResponse<ItemResponse> result = itemService.getAllItems(pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).getName()).isEqualTo("Test Item");
            verify(itemRepository).findAll(pageable);
        }

        @Test
        @DisplayName("should return empty page when no items exist")
        void shouldReturnEmptyPage() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Item> emptyPage = new PageImpl<>(List.of(), pageable, 0);
            when(itemRepository.findAll(pageable)).thenReturn(emptyPage);

            PagedResponse<ItemResponse> result = itemService.getAllItems(pageable);

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }
    }

    @Nested
    @DisplayName("getItemById")
    class GetItemByIdTests {

        @Test
        @DisplayName("should return item when found")
        void shouldReturnItemWhenFound() {
            when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));

            ItemResponse result = itemService.getItemById(1L);

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getName()).isEqualTo("Test Item");
            verify(itemRepository).findById(1L);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when item not found")
        void shouldThrowWhenNotFound() {
            when(itemRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> itemService.getItemById(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Item not found with id: '99'");
        }
    }

    @Nested
    @DisplayName("createItem")
    class CreateItemTests {

        @Test
        @DisplayName("should create item successfully")
        void shouldCreateItem() {
            when(itemRepository.existsByNameIgnoreCase(anyString())).thenReturn(false);
            when(itemRepository.save(any(Item.class))).thenReturn(testItem);

            ItemResponse result = itemService.createItem(testRequest);

            assertThat(result.getName()).isEqualTo("Test Item");
            verify(itemRepository).save(any(Item.class));
        }

        @Test
        @DisplayName("should create item with status")
        void shouldCreateItemWithStatus() {
            testRequest.setStatus("INACTIVE");
            when(itemRepository.existsByNameIgnoreCase(anyString())).thenReturn(false);
            when(itemRepository.save(any(Item.class))).thenReturn(testItem);

            itemService.createItem(testRequest);

            verify(itemRepository).save(any(Item.class));
        }

        @Test
        @DisplayName("should throw DuplicateResourceException when name exists")
        void shouldThrowWhenDuplicate() {
            when(itemRepository.existsByNameIgnoreCase("Test Item")).thenReturn(true);

            assertThatThrownBy(() -> itemService.createItem(testRequest))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("Item already exists with name: 'Test Item'");

            verify(itemRepository, never()).save(any(Item.class));
        }
    }

    @Nested
    @DisplayName("updateItem")
    class UpdateItemTests {

        @Test
        @DisplayName("should update item successfully")
        void shouldUpdateItem() {
            ItemRequest updateRequest = new ItemRequest("Updated Item", "Updated Description");
            when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
            when(itemRepository.save(any(Item.class))).thenReturn(testItem);

            ItemResponse result = itemService.updateItem(1L, updateRequest);

            assertThat(result).isNotNull();
            verify(itemRepository).save(any(Item.class));
        }

        @Test
        @DisplayName("should update item with status")
        void shouldUpdateItemWithStatus() {
            ItemRequest updateRequest = new ItemRequest("Updated Item", "Updated Description");
            updateRequest.setStatus("ARCHIVED");
            when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
            when(itemRepository.save(any(Item.class))).thenReturn(testItem);

            itemService.updateItem(1L, updateRequest);

            verify(itemRepository).save(any(Item.class));
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when item not found")
        void shouldThrowWhenNotFound() {
            ItemRequest updateRequest = new ItemRequest("Updated Item", "Updated Description");
            when(itemRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> itemService.updateItem(99L, updateRequest))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(itemRepository, never()).save(any(Item.class));
        }
    }

    @Nested
    @DisplayName("deleteItem")
    class DeleteItemTests {

        @Test
        @DisplayName("should delete item successfully")
        void shouldDeleteItem() {
            when(itemRepository.existsById(1L)).thenReturn(true);

            itemService.deleteItem(1L);

            verify(itemRepository).deleteById(1L);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when item not found")
        void shouldThrowWhenNotFound() {
            when(itemRepository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> itemService.deleteItem(99L))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(itemRepository, never()).deleteById(anyLong());
        }
    }

    @Nested
    @DisplayName("getItemsByStatus")
    class GetItemsByStatusTests {

        @Test
        @DisplayName("should return items filtered by status")
        void shouldReturnItemsByStatus() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Item> page = new PageImpl<>(List.of(testItem), pageable, 1);
            when(itemRepository.findByStatus(ItemStatus.ACTIVE, pageable)).thenReturn(page);

            PagedResponse<ItemResponse> result = itemService.getItemsByStatus(ItemStatus.ACTIVE, pageable);

            assertThat(result.getContent()).hasSize(1);
            verify(itemRepository).findByStatus(ItemStatus.ACTIVE, pageable);
        }
    }

    @Nested
    @DisplayName("searchItems")
    class SearchItemsTests {

        @Test
        @DisplayName("should return items matching keyword")
        void shouldReturnMatchingItems() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Item> page = new PageImpl<>(List.of(testItem), pageable, 1);
            when(itemRepository.searchByKeyword("Test", pageable)).thenReturn(page);

            PagedResponse<ItemResponse> result = itemService.searchItems("Test", pageable);

            assertThat(result.getContent()).hasSize(1);
            verify(itemRepository).searchByKeyword("Test", pageable);
        }
    }
}
