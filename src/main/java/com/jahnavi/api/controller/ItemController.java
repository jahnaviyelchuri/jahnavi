package com.jahnavi.api.controller;

import com.jahnavi.api.dto.ItemRequest;
import com.jahnavi.api.dto.ItemResponse;
import com.jahnavi.api.dto.PagedResponse;
import com.jahnavi.api.model.ItemStatus;
import com.jahnavi.api.service.ItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/items")
@Tag(name = "Items", description = "Item management API")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping
    @Operation(summary = "Get all items", description = "Retrieve paginated list of all items")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved items")
    public ResponseEntity<PagedResponse<ItemResponse>> getAllItems(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(itemService.getAllItems(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get item by ID", description = "Retrieve a single item by its ID")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved item")
    @ApiResponse(responseCode = "404", description = "Item not found")
    public ResponseEntity<ItemResponse> getItemById(
            @Parameter(description = "Item ID") @PathVariable Long id) {
        return ResponseEntity.ok(itemService.getItemById(id));
    }

    @PostMapping
    @Operation(summary = "Create item", description = "Create a new item")
    @ApiResponse(responseCode = "201", description = "Item created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @ApiResponse(responseCode = "409", description = "Item with same name already exists")
    public ResponseEntity<ItemResponse> createItem(
            @Valid @RequestBody ItemRequest request) {
        ItemResponse response = itemService.createItem(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update item", description = "Update an existing item")
    @ApiResponse(responseCode = "200", description = "Item updated successfully")
    @ApiResponse(responseCode = "404", description = "Item not found")
    public ResponseEntity<ItemResponse> updateItem(
            @Parameter(description = "Item ID") @PathVariable Long id,
            @Valid @RequestBody ItemRequest request) {
        return ResponseEntity.ok(itemService.updateItem(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete item", description = "Delete an item by ID")
    @ApiResponse(responseCode = "204", description = "Item deleted successfully")
    @ApiResponse(responseCode = "404", description = "Item not found")
    public ResponseEntity<Void> deleteItem(
            @Parameter(description = "Item ID") @PathVariable Long id) {
        itemService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get items by status", description = "Retrieve items filtered by status")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved items")
    public ResponseEntity<PagedResponse<ItemResponse>> getItemsByStatus(
            @Parameter(description = "Item status") @PathVariable ItemStatus status,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(itemService.getItemsByStatus(status, pageable));
    }

    @GetMapping("/search")
    @Operation(summary = "Search items", description = "Search items by keyword in name or description")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved items")
    public ResponseEntity<PagedResponse<ItemResponse>> searchItems(
            @Parameter(description = "Search keyword") @RequestParam String keyword,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(itemService.searchItems(keyword, pageable));
    }
}
