package com.codewithmosh.store.repositories;

import com.codewithmosh.store.entities.Category;
import com.codewithmosh.store.entities.Product;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductRepository extends JpaRepository<Product, Long> {
    @EntityGraph(attributePaths = "category")
    List<Product> findByCategoryId(Byte categoryId);

    @EntityGraph(attributePaths = "category")
    @Query("SELECT p FROM Product p")
    List<Product> findAllWithCategory();

    @EntityGraph(attributePaths = "category")
    Page<Product> findByCategoryId(Byte categoryId, Pageable pageable);

    @EntityGraph(attributePaths = "category")
    @Query("SELECT p FROM Product p")
    Page<Product> findAllWithCategory(Pageable pageable);
}