package com.apigateway.order.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class OrderItemRequest {
    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotBlank(message = "Product name is required")
    private String productName;

    @NotBlank(message = "Product SKU is required")
    private String sku;

    @NotNull(message = "Price is required")
    @DecimalMin("0.01")
    private BigDecimal price;

    @NotNull(message = "Quantity is required")
    @Min(1)
    private Integer quantity;

    public OrderItemRequest() {}

    public OrderItemRequest(Long productId, String productName, String sku, BigDecimal price, Integer quantity) {
        this.productId = productId;
        this.productName = productName;
        this.sku = sku;
        this.price = price;
        this.quantity = quantity;
    }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
