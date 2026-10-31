package com.inventory.repository;

import com.inventory.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Spring Data JPA derives the SQL from the method name:
    // SELECT * FROM products WHERE sku = ?
    Optional<Product> findBySku(String sku);

    // SELECT * FROM products WHERE active = true
    List<Product> findByActiveTrue();

    // SELECT * FROM products WHERE category = ? AND active = true
    List<Product> findByCategoryAndActiveTrue(String category);

    // Check if SKU already exists — used before creating a product
    boolean existsBySku(String sku);

    // Custom JPQL query — deducts stock atomically
    // @Modifying marks this as a write operation (UPDATE/DELETE)
    // @Query uses JPQL (Java Persistence Query Language), not SQL
    @Modifying
    @Query("UPDATE Product p SET p.quantityInStock = p.quantityInStock - :quantity " +
            "WHERE p.id = :id AND p.quantityInStock >= :quantity")
    int deductStock(@Param("id") Long id, @Param("quantity") Integer quantity);
    // Returns int: 1 if update succeeded, 0 if stock was insufficient
}