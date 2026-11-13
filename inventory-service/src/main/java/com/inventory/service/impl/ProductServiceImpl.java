package com.inventory.service.impl;

import com.inventory.constants.AppConstants;
import com.inventory.dto.request.CreateProductRequest;
import com.inventory.dto.request.UpdateStockRequest;
import com.inventory.dto.response.ProductResponse;
import com.inventory.entity.Product;
import com.inventory.exception.DuplicateResourceException;
import com.inventory.exception.InsufficientStockException;
import com.inventory.exception.ResourceNotFoundException;
import com.inventory.repository.ProductRepository;
import com.inventory.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        log.info("Creating product with SKU: {}", request.getSku());

        if (productRepository.existsBySku(request.getSku())) {
            throw new DuplicateResourceException(
                    AppConstants.DUPLICATE_SKU + request.getSku()
            );
        }

        Product product = Product.builder()
                .name(request.getName())
                .sku(request.getSku())
                .description(request.getDescription())
                .price(request.getPrice())
                .quantityInStock(request.getQuantityInStock())
                .category(request.getCategory())
                .active(true)
                .build();

        Product savedProduct = productRepository.save(product);
        log.info("Product created with id: {}", savedProduct.getId());

        return mapToResponse(savedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        log.info("Fetching product with id: {}", id);
        Product product = findProductById(id);
        return mapToResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductBySku(String sku) {
        log.info("Fetching product with SKU: {}", sku);
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with SKU: " + sku
                ));
        return mapToResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        log.info("Fetching all active products");
        return productRepository.findByActiveTrue()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getProductsByCategory(String category) {
        log.info("Fetching products in category: {}", category);
        return productRepository.findByCategoryAndActiveTrue(category)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, CreateProductRequest request) {
        log.info("Updating product with id: {}", id);
        Product product = findProductById(id);

        if (!product.getSku().equals(request.getSku())
                && productRepository.existsBySku(request.getSku())) {
            throw new DuplicateResourceException(
                    AppConstants.DUPLICATE_SKU + request.getSku()
            );
        }

        product.setName(request.getName());
        product.setSku(request.getSku());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setQuantityInStock(request.getQuantityInStock());
        product.setCategory(request.getCategory());

        Product updatedProduct = productRepository.save(product);
        log.info("Product updated with id: {}", updatedProduct.getId());

        return mapToResponse(updatedProduct);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        log.info("Soft deleting product with id: {}", id);
        Product product = findProductById(id);

        product.setActive(false);
        productRepository.save(product);
        log.info("Product soft deleted with id: {}", id);
    }

    @Override
    @Transactional
    public ProductResponse deductStock(Long id, UpdateStockRequest request) {
        log.info("Deducting {} units from product id: {}", request.getQuantity(), id);

        findProductById(id);

        int rowsUpdated = productRepository.deductStock(id, request.getQuantity());

        if (rowsUpdated == 0) {
            throw new InsufficientStockException(
                    AppConstants.INSUFFICIENT_STOCK + id
            );
        }

        return mapToResponse(findProductById(id));
    }

    @Override
    @Transactional
    public ProductResponse restoreStock(Long id, UpdateStockRequest request) {
        log.info("Restoring {} units to product id: {}", request.getQuantity(), id);

        Product product = findProductById(id);
        product.setQuantityInStock(product.getQuantityInStock() + request.getQuantity());
        Product updatedProduct = productRepository.save(product);

        return mapToResponse(updatedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isStockAvailable(Long id, Integer quantity) {
        Product product = findProductById(id);
        return product.getActive() && product.getQuantityInStock() >= quantity;
    }

    private Product findProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        AppConstants.PRODUCT_NOT_FOUND + id
                ));
    }

    private ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .sku(product.getSku())
                .description(product.getDescription())
                .price(product.getPrice())
                .quantityInStock(product.getQuantityInStock())
                .category(product.getCategory())
                .active(product.getActive())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
