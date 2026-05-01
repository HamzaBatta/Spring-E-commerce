package com.codewithmosh.store.controllers;

import com.codewithmosh.store.dtos.StorageDto;
import com.codewithmosh.store.exceptions.ProductNotFoundException;
import com.codewithmosh.store.exceptions.StorageNotFoundException;
import com.codewithmosh.store.services.StorageService;
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
            @RequestBody StorageDto storageDto
    ) {
        try {
            var created = storageService.createStorage(storageDto);
            var uri = uriBuilder.path("/storages/{id}").buildAndExpand(created.getId()).toUri();
            return ResponseEntity.created(uri).body(created);
        } catch (ProductNotFoundException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<StorageDto> updateStorage(
            @PathVariable Long id,
            @RequestBody StorageDto storageDto
    ) {
        try {
            return ResponseEntity.ok(storageService.updateStorage(id, storageDto));
        } catch (StorageNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (ProductNotFoundException e) {
            return ResponseEntity.badRequest().build();
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
}
