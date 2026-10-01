package com.ordercraft.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class ProductRequest {

    @NotBlank(message = "Product code is required")
    private String productCode;

    @NotBlank(message = "Product name is required")
    private String productName;

    private String description;

    @NotBlank(message = "Category is required")
    private String category;

    private String type = "FINISHED_GOOD"; // RAW_MATERIAL, FINISHED_GOOD

    @NotNull(message = "Selling price is required")
    @DecimalMin(value = "0.0", message = "Selling price cannot be negative")
    private BigDecimal sellingPrice = BigDecimal.ZERO;

    @NotNull(message = "Cost price is required")
    @DecimalMin(value = "0.0", message = "Cost price cannot be negative")
    private BigDecimal costPrice = BigDecimal.ZERO;

    @NotBlank(message = "Unit is required")
    private String unit = "PCS";

    private String status = "ACTIVE";

    // Initial inventory setup parameters (optional)
    private BigDecimal initialQuantity;
    private BigDecimal minimumStock;
    private BigDecimal reorderLevel;

    public ProductRequest() {}

    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public BigDecimal getSellingPrice() { return sellingPrice; }
    public void setSellingPrice(BigDecimal sellingPrice) { this.sellingPrice = sellingPrice; }

    public BigDecimal getCostPrice() { return costPrice; }
    public void setCostPrice(BigDecimal costPrice) { this.costPrice = costPrice; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public BigDecimal getInitialQuantity() { return initialQuantity; }
    public void setInitialQuantity(BigDecimal initialQuantity) { this.initialQuantity = initialQuantity; }

    public BigDecimal getMinimumStock() { return minimumStock; }
    public void setMinimumStock(BigDecimal minimumStock) { this.minimumStock = minimumStock; }

    public BigDecimal getReorderLevel() { return reorderLevel; }
    public void setReorderLevel(BigDecimal reorderLevel) { this.reorderLevel = reorderLevel; }
}
