package com.jahnavi.api.service.impl;

import com.jahnavi.api.dto.ItemRequest;
import com.jahnavi.api.dto.ItemResponse;
import com.jahnavi.api.dto.PagedResponse;
import com.jahnavi.api.exception.DuplicateResourceException;
import com.jahnavi.api.exception.ResourceNotFoundException;
import com.jahnavi.api.model.Item;
import com.jahnavi.api.model.ItemStatus;
import com.jahnavi.api.repository.ItemRepository;
import com.jahnavi.api.service.ItemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private static final Logger logger = LoggerFactory.getLogger(ItemServiceImpl.class);

    private final ItemRepository itemRepository;

    public ItemServiceImpl(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @Override
    public PagedResponse<ItemResponse> getAllItems(Pageable pageable) {
        logger.debug("Fetching all items with pageable: {}", pageable);
        Page<ItemResponse> page = itemRepository.findAll(pageable)
                .map(ItemResponse::fromEntity);
        return PagedResponse.fromPage(page);
    }

    @Override
    public ItemResponse getItemById(Long id) {
        logger.debug("Fetching item by id: {}", id);
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item", "id", id));
        return ItemResponse.fromEntity(item);
    }

    @Override
    @Transactional
    public ItemResponse createItem(ItemRequest request) {
        logger.info("Creating new item with name: {}", request.getName());

        if (itemRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateResourceException("Item", "name", request.getName());
        }

        Item item = new Item(request.getName(), request.getDescription());
        if (request.getStatus() != null) {
            item.setStatus(ItemStatus.valueOf(request.getStatus().toUpperCase()));
        }

        Item savedItem = itemRepository.save(item);
        logger.info("Item created with id: {}", savedItem.getId());
        return ItemResponse.fromEntity(savedItem);
    }

    @Override
    @Transactional
    public ItemResponse updateItem(Long id, ItemRequest request) {
        logger.info("Updating item with id: {}", id);

        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item", "id", id));

        item.setName(request.getName());
        item.setDescription(request.getDescription());
        if (request.getStatus() != null) {
            item.setStatus(ItemStatus.valueOf(request.getStatus().toUpperCase()));
        }

        Item updatedItem = itemRepository.save(item);
        logger.info("Item updated with id: {}", updatedItem.getId());
        return ItemResponse.fromEntity(updatedItem);
    }

    @Override
    @Transactional
    public void deleteItem(Long id) {
        logger.info("Deleting item with id: {}", id);

        if (!itemRepository.existsById(id)) {
            throw new ResourceNotFoundException("Item", "id", id);
        }

        itemRepository.deleteById(id);
        logger.info("Item deleted with id: {}", id);
    }

    @Override
    public PagedResponse<ItemResponse> getItemsByStatus(ItemStatus status, Pageable pageable) {
        logger.debug("Fetching items by status: {}", status);
        Page<ItemResponse> page = itemRepository.findByStatus(status, pageable)
                .map(ItemResponse::fromEntity);
        return PagedResponse.fromPage(page);
    }

    @Override
    public PagedResponse<ItemResponse> searchItems(String keyword, Pageable pageable) {
        logger.debug("Searching items with keyword: {}", keyword);
        Page<ItemResponse> page = itemRepository.searchByKeyword(keyword, pageable)
                .map(ItemResponse::fromEntity);
        return PagedResponse.fromPage(page);
    }
}
