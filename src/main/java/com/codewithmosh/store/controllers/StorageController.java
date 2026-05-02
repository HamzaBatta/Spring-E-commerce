package com.codewithmosh.store.controllers;

import com.codewithmosh.store.dtos.*;
import com.codewithmosh.store.exceptions.ProductNotFoundException;
import com.codewithmosh.store.exceptions.StorageItemNotFoundException;
import com.codewithmosh.store.exceptions.StorageNotFoundException;
import com.codewithmosh.store.services.StorageService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@AllArgsConstructor
@RequestMapping("/storages")
@RestController
public class StorageController {
    private final StorageService storageService;

    @GetMapping
    public List<StorageDto> getAllStorages(
            @RequestParam(required = false, name = "productId") Long productId
    ) {
        return storageService.getAllStorages(productId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StorageDto> getStorage(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(storageService.getStorage(id));
        } catch (StorageNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<StorageDto> createStorage(
            UriComponentsBuilder uriBuilder,
            @Valid @RequestBody CreateStorageRequest request
    ) {
        var created = storageService.createStorage(request);
        var uri = uriBuilder.path("/storages/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(uri).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StorageDto> updateStorage(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStorageRequest request
    ) {
        try {
            return ResponseEntity.ok(storageService.updateStorage(id, request));
        } catch (StorageNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStorage(@PathVariable Long id) {
        try {
            storageService.deleteStorage(id);
            return ResponseEntity.noContent().build();
        } catch (StorageNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/items")
    public ResponseEntity<List<StorageItemDto>> getStorageItems(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(storageService.getStorageItems(id));
        } catch (StorageNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/items")
    public ResponseEntity<StorageItemDto> addStorageItem(
            @PathVariable Long id,
            @Valid @RequestBody AddStorageItemRequest request
    ) {
        try {
            return ResponseEntity.ok(storageService.addStorageItem(id, request));
        } catch (StorageNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (ProductNotFoundException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}/items/{itemId}")
    public ResponseEntity<StorageItemDto> updateStorageItem(
            @PathVariable Long id,
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateStorageItemRequest request
    ) {
        try {
            return ResponseEntity.ok(storageService.updateStorageItem(id, itemId, request));
        } catch (StorageItemNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}/items/{itemId}")
    public ResponseEntity<Void> deleteStorageItem(
            @PathVariable Long id,
            @PathVariable Long itemId
    ) {
        try {
            storageService.deleteStorageItem(id, itemId);
            return ResponseEntity.noContent().build();
        } catch (StorageItemNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
