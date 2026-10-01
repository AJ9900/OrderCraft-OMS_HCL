package com.ordercraft.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public class ProductionOrderRequest {

    private String productionOrderNumber;
    @NotNull(message = "Customer order ID is required")
    private Long customerOrderId;

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotNull(message = "Planned quantity is required")
    @DecimalMin(value = "0.01", message = "Planned quantity must be greater than 0")
    private BigDecimal plannedQuantity;

    private LocalDate startDate;
    private LocalDate endDate;
    private String notes;

    public ProductionOrderRequest() {}

    public String getProductionOrderNumber() { return productionOrderNumber; }
    public void setProductionOrderNumber(String productionOrderNumber) { this.productionOrderNumber = productionOrderNumber; }

    public Long getCustomerOrderId() { return customerOrderId; }
    public void setCustomerOrderId(Long customerOrderId) { this.customerOrderId = customerOrderId; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public BigDecimal getPlannedQuantity() { return plannedQuantity; }
    public void setPlannedQuantity(BigDecimal plannedQuantity) { this.plannedQuantity = plannedQuantity; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
