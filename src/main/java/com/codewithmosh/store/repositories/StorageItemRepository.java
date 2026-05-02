package com.codewithmosh.store.repositories;

import com.codewithmosh.store.entities.StorageItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StorageItemRepository extends JpaRepository<StorageItem, Long> {
    Optional<StorageItem> findByStorageIdAndProductId(Long storageId, Long productId);

    Optional<StorageItem> findByIdAndStorageId(Long id, Long storageId);

    List<StorageItem> findByStorageId(Long storageId);
}

