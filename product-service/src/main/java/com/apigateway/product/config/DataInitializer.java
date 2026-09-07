package com.apigateway.product.config;

import com.apigateway.product.entity.Product;
import com.apigateway.product.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final ProductRepository productRepository;

    public DataInitializer(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public void run(String... args) {
        try {
            if (productRepository.count() == 0) {
                log.info("Seeding initial products into Product database...");

                List<Product> sampleProducts = List.of(
                        new Product("Edge API Gateway Accelerator X1", "GATEWAY-X1-PRO", "Hardware",
                                new BigDecimal("1299.99"), 45, "High-throughput hardware accelerator for microservice TLS and load balancing."),
                        new Product("Cloud Cluster Node Enterprise", "CLOUD-NODE-ENT", "Infrastructure",
                                new BigDecimal("2499.50"), 20, "Scalable compute node with redundant 10GbE network interfaces."),
                        new Product("AI Inference Accelerator Card", "AI-INF-ACC-V2", "AI & ML",
                                new BigDecimal("899.00"), 80, "Tensor processing unit card for real-time anomaly detection and token analysis."),
                        new Product("Quantum Security HSM Token", "SEC-HSM-TOKEN", "Security",
                                new BigDecimal("349.75"), 150, "FIPS-certified Hardware Security Module for cryptographic key storage."),
                        new Product("Low-Latency Microservices Switch", "NET-SWITCH-48P", "Networking",
                                new BigDecimal("1850.00"), 30, "48-port Layer 3 microsecond low-latency datacenter switch."),
                        new Product("Enterprise Distributed Cache Node", "CACHE-NODE-64G", "Infrastructure",
                                new BigDecimal("650.00"), 110, "High-density RAM memory cache node optimized for token-bucket rate limiting.")
                );

                productRepository.saveAll(sampleProducts);
                log.info("Seeded {} sample products successfully.", sampleProducts.size());
            }
        } catch (Exception e) {
            log.warn("Product database initialization notice: {}", e.getMessage());
        }
    }
}
