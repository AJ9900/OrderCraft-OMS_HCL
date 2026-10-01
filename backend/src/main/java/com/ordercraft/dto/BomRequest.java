package com.ordercraft.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;

public class BomRequest {

    @NotBlank(message = "BOM code is required")
    private String bomCode;

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotBlank(message = "Version is required")
    private String version = "1.0";

    private String status = "ACTIVE";

    @NotNull(message = "Effective date is required")
    private LocalDate effectiveDate = LocalDate.now();

    private String notes;

    @NotEmpty(message = "BOM must contain at least one material item")
    private List<@Valid BomItemRequest> items;

    public BomRequest() {}

    public String getBomCode() { return bomCode; }
    public void setBomCode(String bomCode) { this.bomCode = bomCode; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public List<BomItemRequest> getItems() { return items; }
    public void setItems(List<BomItemRequest> items) { this.items = items; }
}
