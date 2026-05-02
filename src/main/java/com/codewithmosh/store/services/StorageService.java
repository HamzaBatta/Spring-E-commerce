package com.codewithmosh.store.services;

import com.codewithmosh.store.dtos.*;
import com.codewithmosh.store.entities.Storage;
import com.codewithmosh.store.entities.StorageItem;
import com.codewithmosh.store.exceptions.ProductNotFoundException;
import com.codewithmosh.store.exceptions.StorageItemNotFoundException;
import com.codewithmosh.store.exceptions.StorageNotFoundException;
import com.codewithmosh.store.mappers.StorageItemMapper;
import com.codewithmosh.store.mappers.StorageMapper;
import com.codewithmosh.store.repositories.ProductRepository;
import com.codewithmosh.store.repositories.StorageItemRepository;
import com.codewithmosh.store.repositories.StorageRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class StorageService {
    private StorageRepository storageRepository;
    private StorageItemRepository storageItemRepository;
    private StorageMapper storageMapper;
    private StorageItemMapper storageItemMapper;
    private ProductRepository productRepository;

    public List<StorageDto> getAllStorages(Long productId) {
        var storages = (productId != null)
                ? storageRepository.findByProductId(productId)
                : storageRepository.findAllWithItems();
        return storages.stream()
                .map(storageMapper::toDto)
                .toList();
    }

    public StorageDto getStorage(Long id) {
        var storage = storageRepository.findWithItemsById(id).orElse(null);
        if (storage == null) {
            throw new StorageNotFoundException();
        }
        return storageMapper.toDto(storage);
    }

    public StorageDto createStorage(CreateStorageRequest request) {
        var storage = new Storage();
        storage.setName(request.getName());
        storage.setLocation(request.getLocation());
        storageRepository.save(storage);
        return storageMapper.toDto(storage);
    }

    public StorageDto updateStorage(Long id, UpdateStorageRequest request) {
        var storage = storageRepository.findWithItemsById(id).orElse(null);
        if (storage == null) {
            throw new StorageNotFoundException();
        }
        storage.setName(request.getName());
        storage.setLocation(request.getLocation());
        storageRepository.save(storage);
        return storageMapper.toDto(storage);
    }

    public void deleteStorage(Long id) {
        var storage = storageRepository.findById(id).orElse(null);
        if (storage == null) {
            throw new StorageNotFoundException();
        }
        storageRepository.delete(storage);
    }

    public List<StorageItemDto> getStorageItems(Long storageId) {
        ensureStorageExists(storageId);
        return storageItemRepository.findByStorageId(storageId)
                .stream()
                .map(storageItemMapper::toDto)
                .toList();
    }

    public StorageItemDto addStorageItem(Long storageId, AddStorageItemRequest request) {
        var storage = storageRepository.findById(storageId).orElse(null);
        if (storage == null) {
            throw new StorageNotFoundException();
        }
        var product = productRepository.findById(request.getProductId()).orElse(null);
        if (product == null) {
            throw new ProductNotFoundException();
        }

        var item = storageItemRepository.findByStorageIdAndProductId(storageId, product.getId())
                .orElseGet(() -> {
                    var newItem = new StorageItem();
                    newItem.setStorage(storage);
                    newItem.setProduct(product);
                    newItem.setQuantity(0);
                    return newItem;
                });

        var current = item.getQuantity() == null ? 0 : item.getQuantity();
        item.setQuantity(current + request.getQuantity());
        storageItemRepository.save(item);
        return storageItemMapper.toDto(item);
    }

    public StorageItemDto updateStorageItem(Long storageId, Long itemId, UpdateStorageItemRequest request) {
        var item = storageItemRepository.findByIdAndStorageId(itemId, storageId).orElse(null);
        if (item == null) {
            throw new StorageItemNotFoundException();
        }
        item.setQuantity(request.getQuantity());
        storageItemRepository.save(item);
        return storageItemMapper.toDto(item);
    }

    public void deleteStorageItem(Long storageId, Long itemId) {
        var item = storageItemRepository.findByIdAndStorageId(itemId, storageId).orElse(null);
        if (item == null) {
            throw new StorageItemNotFoundException();
        }
        storageItemRepository.delete(item);
    }

    private void ensureStorageExists(Long storageId) {
        if (storageRepository.existsById(storageId)) {
            return;
        }
        throw new StorageNotFoundException();
    }
}
