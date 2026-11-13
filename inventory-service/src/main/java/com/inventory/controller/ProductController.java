package com.inventory.controller;

import com.inventory.constants.AppConstants;
import com.inventory.dto.request.CreateProductRequest;
import com.inventory.dto.request.UpdateStockRequest;
import com.inventory.dto.response.ApiResponse;
import com.inventory.dto.response.ProductResponse;
import com.inventory.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Valid @RequestBody CreateProductRequest request) {
        log.info("POST /api/v1/products - creating product: {}", request.getSku());
        ProductResponse response = productService.createProduct(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(AppConstants.PRODUCT_CREATED, response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getAllProducts() {
        log.info("GET /api/v1/products - fetching all products");
        List<ProductResponse> products = productService.getAllProducts();
        return ResponseEntity.ok(
                ApiResponse.success("Products fetched successfully", products)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(
            @PathVariable Long id) {
        log.info("GET /api/v1/products/{} - fetching product", id);
        ProductResponse response = productService.getProductById(id);
        return ResponseEntity.ok(
                ApiResponse.success("Product fetched successfully", response)
        );
    }

    @GetMapping("/sku/{sku}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductBySku(
            @PathVariable String sku) {
        log.info("GET /api/v1/products/sku/{} - fetching product", sku);
        ProductResponse response = productService.getProductBySku(sku);
        return ResponseEntity.ok(
                ApiResponse.success("Product fetched successfully", response)
        );
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getProductsByCategory(
            @PathVariable String category) {
        log.info("GET /api/v1/products/category/{}", category);
        List<ProductResponse> products = productService.getProductsByCategory(category);
        return ResponseEntity.ok(
                ApiResponse.success("Products fetched successfully", products)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody CreateProductRequest request) {
        log.info("PUT /api/v1/products/{} - updating product", id);
        ProductResponse response = productService.updateProduct(id, request);
        return ResponseEntity.ok(
                ApiResponse.success(AppConstants.PRODUCT_UPDATED, response)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        log.info("DELETE /api/v1/products/{} - soft deleting product", id);
        productService.deleteProduct(id);
        return ResponseEntity.ok(
                ApiResponse.success(AppConstants.PRODUCT_DELETED)
        );
    }

    @PatchMapping("/{id}/deduct-stock")
    public ResponseEntity<ApiResponse<ProductResponse>> deductStock(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStockRequest request) {
        log.info("PATCH /api/v1/products/{}/deduct-stock - quantity: {}", id, request.getQuantity());
        ProductResponse response = productService.deductStock(id, request);
        return ResponseEntity.ok(
                ApiResponse.success(AppConstants.STOCK_UPDATED, response)
        );
    }

    @PatchMapping("/{id}/restore-stock")
    public ResponseEntity<ApiResponse<ProductResponse>> restoreStock(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStockRequest request) {
        log.info("PATCH /api/v1/products/{}/restore-stock - quantity: {}", id, request.getQuantity());
        ProductResponse response = productService.restoreStock(id, request);
        return ResponseEntity.ok(
                ApiResponse.success(AppConstants.STOCK_UPDATED, response)
        );
    }

    @GetMapping("/{id}/stock-check")
    public ResponseEntity<ApiResponse<Boolean>> checkStock(
            @PathVariable Long id,
            @RequestParam Integer quantity) {
        log.info("GET /api/v1/products/{}/stock-check?quantity={}", id, quantity);
        boolean available = productService.isStockAvailable(id, quantity);
        return ResponseEntity.ok(
                ApiResponse.success("Stock check completed", available)
        );
    }
}
