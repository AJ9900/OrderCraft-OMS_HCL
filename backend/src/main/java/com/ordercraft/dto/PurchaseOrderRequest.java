package com.ordercraft.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;

public class PurchaseOrderRequest {

    private String poNumber;

    @NotNull(message = "Supplier ID is required")
    private Long supplierId;

    @NotNull(message = "PO date is required")
    private LocalDate poDate = LocalDate.now();

    @NotNull(message = "Expected delivery date is required")
    private LocalDate expectedDeliveryDate;

    private String notes;

    @NotEmpty(message = "Purchase order must contain at least one item")
    private List<@Valid PoItemRequest> items;

    public PurchaseOrderRequest() {}

    public String getPoNumber() { return poNumber; }
    public void setPoNumber(String poNumber) { this.poNumber = poNumber; }

    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }

    public LocalDate getPoDate() { return poDate; }
    public void setPoDate(LocalDate poDate) { this.poDate = poDate; }

    public LocalDate getExpectedDeliveryDate() { return expectedDeliveryDate; }
    public void setExpectedDeliveryDate(LocalDate expectedDeliveryDate) { this.expectedDeliveryDate = expectedDeliveryDate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public List<PoItemRequest> getItems() { return items; }
    public void setItems(List<PoItemRequest> items) { this.items = items; }
}
