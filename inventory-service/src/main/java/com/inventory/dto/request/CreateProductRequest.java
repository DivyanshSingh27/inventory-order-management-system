package com.inventory.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;


@Data
public class CreateProductRequest {

    @NotBlank(message = "Product name is required")
    private String name;

    @NotBlank(message = "SKU is required")
    @Size(max = 100, message = "SKU cannot exceed 100 characters")
    private String sku;

    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal price;

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantityInStock;

    @NotBlank(message = "Category is required")
    private String category;
}