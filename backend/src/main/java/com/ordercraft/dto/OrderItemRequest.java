package com.ordercraft.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class OrderItemRequest {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.01", message = "Quantity must be greater than 0")
    private BigDecimal quantity;

    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.0", message = "Unit price cannot be negative")
    private BigDecimal unitPrice;

    private BigDecimal discount = BigDecimal.ZERO;

    @DecimalMin(value = "0.0", message = "Tax cannot be negative")
    private BigDecimal tax = BigDecimal.ZERO;

    public OrderItemRequest() {}

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public BigDecimal getDiscount() { return discount; }
    public void setDiscount(BigDecimal discount) { this.discount = discount; }

    public BigDecimal getTax() { return tax; }
    public void setTax(BigDecimal tax) { this.tax = tax; }
}
