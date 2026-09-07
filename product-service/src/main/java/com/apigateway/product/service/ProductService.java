package com.apigateway.product.service;

import com.apigateway.product.dto.ProductDto;
import com.apigateway.product.entity.Product;
import com.apigateway.product.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<ProductDto> getAllProducts() {
        return productRepository.findByActiveTrue().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public Optional<ProductDto> getProductById(Long id) {
        return productRepository.findById(id).map(this::toDto);
    }

    public Optional<ProductDto> getProductBySku(String sku) {
        return productRepository.findBySku(sku).map(this::toDto);
    }

    public List<ProductDto> getProductsByCategory(String category) {
        return productRepository.findByCategoryIgnoreCase(category).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<ProductDto> searchProducts(String name) {
        return productRepository.findByNameContainingIgnoreCase(name).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<String> getCategories() {
        return productRepository.findDistinctCategories();
    }

    @Transactional
    public ProductDto createProduct(ProductDto dto) {
        if (productRepository.existsBySku(dto.getSku())) {
            throw new IllegalArgumentException("Product with SKU already exists: " + dto.getSku());
        }

        Product product = new Product(
                dto.getName(),
                dto.getSku(),
                dto.getCategory(),
                dto.getPrice(),
                dto.getStockQuantity(),
                dto.getDescription()
        );
        Product saved = productRepository.save(product);
        return toDto(saved);
    }

    @Transactional
    public ProductDto updateProduct(Long id, ProductDto dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + id));

        product.setName(dto.getName());
        product.setCategory(dto.getCategory());
        product.setPrice(dto.getPrice());
        product.setStockQuantity(dto.getStockQuantity());
        product.setDescription(dto.getDescription());
        product.setActive(dto.isActive());

        Product updated = productRepository.save(product);
        return toDto(updated);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + id));
        product.setActive(false); // soft delete
        productRepository.save(product);
    }

    @Transactional
    public ProductDto deductStock(Long id, int quantity) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + id));

        if (product.getStockQuantity() < quantity) {
            throw new IllegalStateException("Insufficient stock. Available: " + product.getStockQuantity() + ", Requested: " + quantity);
        }

        product.setStockQuantity(product.getStockQuantity() - quantity);
        Product saved = productRepository.save(product);
        return toDto(saved);
    }

    private ProductDto toDto(Product p) {
        return new ProductDto(
                p.getId(),
                p.getName(),
                p.getSku(),
                p.getCategory(),
                p.getPrice(),
                p.getStockQuantity(),
                p.getDescription(),
                p.isActive(),
                p.getCreatedAt()
        );
    }
}
