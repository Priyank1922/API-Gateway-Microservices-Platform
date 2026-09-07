package com.apigateway.product.repository;

import com.apigateway.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findBySku(String sku);
    List<Product> findByCategoryIgnoreCase(String category);
    List<Product> findByNameContainingIgnoreCase(String name);
    List<Product> findByActiveTrue();

    @Query("SELECT DISTINCT p.category FROM Product p WHERE p.active = true")
    List<String> findDistinctCategories();

    boolean existsBySku(String sku);
}
