package com.orderservice.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderItemRequest(
    @NotNull(message = "Product ID is required")
    @Positive(message = "Product ID must be positive")
    Long productId,
    
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    Integer quantity,
    
    @NotNull(message = "Unit price is required")
    @Positive(message = "Unit price must be positive")
    @DecimalMin(value = "0.01", message = "Unit price must be at least 0.01")
    BigDecimal unitPrice
) {}

