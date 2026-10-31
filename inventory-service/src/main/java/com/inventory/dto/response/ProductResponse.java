package com.inventory.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data
@Builder
public class ProductResponse {

    private Long id;
    private String name;
    private String sku;
    private String description;
    private BigDecimal price;
    private Integer quantityInStock;
    private String category;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}