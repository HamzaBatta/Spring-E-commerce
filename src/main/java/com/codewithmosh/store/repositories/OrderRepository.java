package com.codewithmosh.store.repositories;

import com.codewithmosh.store.entities.Order;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    @EntityGraph(attributePaths = {"items.product", "user", "storage"})
    @Query("SELECT o FROM Order o")
    List<Order> findAllWithItems();

    @EntityGraph(attributePaths = {"items.product", "user", "storage"})
    @Query("SELECT o FROM Order o WHERE o.id = :id")
    Optional<Order> findWithItemsById(@Param("id") Long id);
}
