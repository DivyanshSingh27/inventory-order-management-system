package com.inventory.service;

import com.inventory.dto.request.CreateProductRequest;
import com.inventory.dto.request.UpdateStockRequest;
import com.inventory.dto.response.ProductResponse;

import java.util.List;

public interface ProductService {

    ProductResponse createProduct(CreateProductRequest request);

    ProductResponse getProductById(Long id);

    ProductResponse getProductBySku(String sku);

    List<ProductResponse> getAllProducts();

    List<ProductResponse> getProductsByCategory(String category);

    ProductResponse updateProduct(Long id, CreateProductRequest request);

    void deleteProduct(Long id);

    ProductResponse deductStock(Long id, UpdateStockRequest request);

    ProductResponse restoreStock(Long id, UpdateStockRequest request);

    boolean isStockAvailable(Long id, Integer quantity);
}
