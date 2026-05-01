package com.codewithmosh.store.repositories;

import com.codewithmosh.store.entities.Storage;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StorageRepository extends JpaRepository<Storage, Long> {
    @EntityGraph(attributePaths = "product")
    List<Storage> findByProductId(Long productId);

    @EntityGraph(attributePaths = "product")
    @Query("SELECT s FROM Storage s")
    List<Storage> findAllWithProduct();
}
