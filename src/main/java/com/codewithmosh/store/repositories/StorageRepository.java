package com.codewithmosh.store.repositories;

import com.codewithmosh.store.entities.Storage;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StorageRepository extends JpaRepository<Storage, Long> {
    @EntityGraph(attributePaths = {"items", "items.product"})
    @Query("SELECT DISTINCT s FROM Storage s JOIN s.items i WHERE i.product.id = :productId")
    List<Storage> findByProductId(@Param("productId") Long productId);

    @EntityGraph(attributePaths = {"items", "items.product"})
    @Query("SELECT s FROM Storage s")
    List<Storage> findAllWithItems();

    @EntityGraph(attributePaths = {"items", "items.product"})
    @Query("SELECT DISTINCT s FROM Storage s JOIN s.items i WHERE i.product.id = :productId")
    org.springframework.data.domain.Page<Storage> findByProductId(@Param("productId") Long productId, org.springframework.data.domain.Pageable pageable);

    @EntityGraph(attributePaths = {"items", "items.product"})
    @Query("SELECT s FROM Storage s")
    org.springframework.data.domain.Page<Storage> findAllWithItems(org.springframework.data.domain.Pageable pageable);

    @EntityGraph(attributePaths = {"items", "items.product"})
    @Query("SELECT s FROM Storage s WHERE s.id = :id")
    Optional<Storage> findWithItemsById(@Param("id") Long id);
}
