package com.codewithmosh.store.services;

import com.codewithmosh.store.dtos.requests.AddStorageItemRequest;
import com.codewithmosh.store.dtos.requests.CreateStorageRequest;
import com.codewithmosh.store.dtos.requests.UpdateStorageItemRequest;
import com.codewithmosh.store.dtos.requests.UpdateStorageRequest;
import com.codewithmosh.store.dtos.resources.StorageItemResource;
import com.codewithmosh.store.dtos.resources.StorageResource;
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

    public org.springframework.data.domain.Page<StorageResource> getAllStorages(Long productId, org.springframework.data.domain.Pageable pageable) {
        var storages = (productId != null)
                ? storageRepository.findByProductId(productId, pageable)
                : storageRepository.findAllWithItems(pageable);
        return storages.map(storageMapper::toResource);
    }

    public StorageResource getStorage(Long id) {
        var storage = storageRepository.findWithItemsById(id).orElse(null);
        if (storage == null) throw new StorageNotFoundException();
        return storageMapper.toResource(storage);
    }

    public StorageResource createStorage(CreateStorageRequest request) {
        var storage = new Storage();
        storage.setName(request.getName());
        storage.setLocation(request.getLocation());
        storageRepository.save(storage);
        return storageMapper.toResource(storage);
    }

    public StorageResource updateStorage(Long id, UpdateStorageRequest request) {
        var storage = storageRepository.findWithItemsById(id).orElse(null);
        if (storage == null) throw new StorageNotFoundException();
        storage.setName(request.getName());
        storage.setLocation(request.getLocation());
        storageRepository.save(storage);
        return storageMapper.toResource(storage);
    }

    public void deleteStorage(Long id) {
        var storage = storageRepository.findById(id).orElse(null);
        if (storage == null) throw new StorageNotFoundException();
        storageRepository.delete(storage);
    }

    public org.springframework.data.domain.Page<StorageItemResource> getStorageItems(Long storageId, org.springframework.data.domain.Pageable pageable) {
        ensureStorageExists(storageId);
        var page = storageItemRepository.findByStorageId(storageId, pageable);
        return page.map(storageItemMapper::toResource);
    }

    public StorageItemResource addStorageItem(Long storageId, AddStorageItemRequest request) {
        var storage = storageRepository.findById(storageId).orElse(null);
        if (storage == null) throw new StorageNotFoundException();

        var product = productRepository.findById(request.getProductId()).orElse(null);
        if (product == null) throw new ProductNotFoundException();

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
        return storageItemMapper.toResource(item);
    }

    public StorageItemResource updateStorageItem(Long storageId, Long itemId, UpdateStorageItemRequest request) {
        var item = storageItemRepository.findByIdAndStorageId(itemId, storageId).orElse(null);
        if (item == null) throw new StorageItemNotFoundException();
        item.setQuantity(request.getQuantity());
        storageItemRepository.save(item);
        return storageItemMapper.toResource(item);
    }

    public void deleteStorageItem(Long storageId, Long itemId) {
        var item = storageItemRepository.findByIdAndStorageId(itemId, storageId).orElse(null);
        if (item == null) throw new StorageItemNotFoundException();
        storageItemRepository.delete(item);
    }

    private void ensureStorageExists(Long storageId) {
        if (!storageRepository.existsById(storageId)) throw new StorageNotFoundException();
    }
}
