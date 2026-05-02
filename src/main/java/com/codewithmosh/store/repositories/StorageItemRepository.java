package com.codewithmosh.store.repositories;

import com.codewithmosh.store.entities.StorageItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface StorageItemRepository extends JpaRepository<StorageItem, Long> {
    Optional<StorageItem> findByStorageIdAndProductId(Long storageId, Long productId);

    Optional<StorageItem> findByIdAndStorageId(Long id, Long storageId);

    List<StorageItem> findByStorageId(Long storageId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT si FROM StorageItem si WHERE si.storage.id = :storageId AND si.product.id = :productId")
    Optional<StorageItem> findForUpdate(@Param("storageId") Long storageId, @Param("productId") Long productId);
}
