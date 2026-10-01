package com.ordercraft.dto;

import java.math.BigDecimal;

public class MaterialRequirementDto {
    private Long materialId;
    private String materialCode;
    private String materialName;
    private String unit;
    private BigDecimal unitCost;
    private BigDecimal requiredQuantity;
    private BigDecimal availableQuantity;
    private BigDecimal currentQuantity;
    private BigDecimal reservedQuantity;
    private BigDecimal shortageQuantity;
    private boolean hasShortage;

    public MaterialRequirementDto() {}

    public Long getMaterialId() { return materialId; }
    public void setMaterialId(Long materialId) { this.materialId = materialId; }

    public String getMaterialCode() { return materialCode; }
    public void setMaterialCode(String materialCode) { this.materialCode = materialCode; }

    public String getMaterialName() { return materialName; }
    public void setMaterialName(String materialName) { this.materialName = materialName; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public BigDecimal getUnitCost() { return unitCost; }
    public void setUnitCost(BigDecimal unitCost) { this.unitCost = unitCost; }

    public BigDecimal getRequiredQuantity() { return requiredQuantity; }
    public void setRequiredQuantity(BigDecimal requiredQuantity) { this.requiredQuantity = requiredQuantity; }

    public BigDecimal getAvailableQuantity() { return availableQuantity; }
    public void setAvailableQuantity(BigDecimal availableQuantity) { this.availableQuantity = availableQuantity; }

    public BigDecimal getCurrentQuantity() { return currentQuantity; }
    public void setCurrentQuantity(BigDecimal currentQuantity) { this.currentQuantity = currentQuantity; }

    public BigDecimal getReservedQuantity() { return reservedQuantity; }
    public void setReservedQuantity(BigDecimal reservedQuantity) { this.reservedQuantity = reservedQuantity; }

    public BigDecimal getShortageQuantity() { return shortageQuantity; }
    public void setShortageQuantity(BigDecimal shortageQuantity) { this.shortageQuantity = shortageQuantity; }

    public boolean isHasShortage() { return hasShortage; }
    public void setHasShortage(boolean hasShortage) { this.hasShortage = hasShortage; }
}
