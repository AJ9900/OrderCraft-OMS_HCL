package com.ordercraft.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class BomItemRequest {

    @NotNull(message = "Material ID is required")
    private Long materialId;

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.0001", message = "Quantity must be greater than 0")
    private BigDecimal quantity;

    @NotBlank(message = "Unit is required")
    private String unit;

    @DecimalMin(value = "0.0", message = "Wastage percentage cannot be negative")
    @DecimalMax(value = "100.0", message = "Wastage percentage cannot exceed 100")
    private BigDecimal wastagePercentage = BigDecimal.ZERO;

    public BomItemRequest() {}

    public Long getMaterialId() { return materialId; }
    public void setMaterialId(Long materialId) { this.materialId = materialId; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public BigDecimal getWastagePercentage() { return wastagePercentage; }
    public void setWastagePercentage(BigDecimal wastagePercentage) { this.wastagePercentage = wastagePercentage; }
}
