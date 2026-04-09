package com.jahnavi.api.service;

import com.jahnavi.api.dto.ItemRequest;
import com.jahnavi.api.dto.ItemResponse;
import com.jahnavi.api.dto.PagedResponse;
import com.jahnavi.api.model.ItemStatus;
import org.springframework.data.domain.Pageable;

public interface ItemService {

    PagedResponse<ItemResponse> getAllItems(Pageable pageable);

    ItemResponse getItemById(Long id);

    ItemResponse createItem(ItemRequest request);

    ItemResponse updateItem(Long id, ItemRequest request);

    void deleteItem(Long id);

    PagedResponse<ItemResponse> getItemsByStatus(ItemStatus status, Pageable pageable);

    PagedResponse<ItemResponse> searchItems(String keyword, Pageable pageable);
}
