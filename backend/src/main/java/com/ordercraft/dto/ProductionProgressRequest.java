package com.ordercraft.dto;

import com.ordercraft.entity.ProductionStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class ProductionProgressRequest {

    @NotNull(message = "Production status is required")
    private ProductionStatus status;

    @NotNull(message = "Produced quantity is required")
    @DecimalMin(value = "0.0", message = "Produced quantity cannot be negative")
    private BigDecimal producedQuantity;

    private String notes;

    public ProductionProgressRequest() {}

    public ProductionProgressRequest(ProductionStatus status, BigDecimal producedQuantity) {
        this.status = status;
        this.producedQuantity = producedQuantity;
    }

    public ProductionStatus getStatus() { return status; }
    public void setStatus(ProductionStatus status) { this.status = status; }

    public BigDecimal getProducedQuantity() { return producedQuantity; }
    public void setProducedQuantity(BigDecimal producedQuantity) { this.producedQuantity = producedQuantity; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
