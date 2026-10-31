package com.inventory.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity
@Table(
        name = "products",
        uniqueConstraints = {
                // Enforces unique SKU at the database level,
                // not just application level — belt and suspenders
                @UniqueConstraint(columnNames = "sku", name = "uk_product_sku")
        }
)
@Data               // Lombok: generates getters, setters, equals, hashCode, toString
@Builder            // Lombok: enables Product.builder().name("x").build() pattern
@NoArgsConstructor  // Lombok: required by JPA (JPA needs a no-arg constructor)
@AllArgsConstructor // Lombok: required by @Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // IDENTITY = PostgreSQL SERIAL / auto-increment
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String sku; // Stock Keeping Unit — unique product code

    @Column(length = 500)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    // precision=10 → max 10 digits total
    // scale=2      → max 2 decimal places (e.g. 99999999.99)

    @Column(nullable = false)
    private Integer quantityInStock;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private Boolean active = true;
    // Soft delete flag — we never hard delete products
    // because orders may reference them historically

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    // updatable = false → once set, Hibernate never updates this column

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * @PrePersist → called by Hibernate automatically
     * just before INSERT. Sets both timestamps.
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * @PreUpdate → called by Hibernate automatically
     * just before UPDATE. Refreshes updatedAt only.
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}