package com.ordercraft.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;

public class GoodsReceiptRequest {

    private String receiptNumber;

    @NotNull(message = "Purchase order ID is required")
    private Long poId;

    @NotNull(message = "Receipt date is required")
    private LocalDate receiptDate = LocalDate.now();

    private String receivedBy;
    private String notes;

    @NotEmpty(message = "Goods receipt must include at least one item")
    private List<@Valid GoodsReceiptItemRequest> items;

    public GoodsReceiptRequest() {}

    public String getReceiptNumber() { return receiptNumber; }
    public void setReceiptNumber(String receiptNumber) { this.receiptNumber = receiptNumber; }

    public Long getPoId() { return poId; }
    public void setPoId(Long poId) { this.poId = poId; }

    public LocalDate getReceiptDate() { return receiptDate; }
    public void setReceiptDate(LocalDate receiptDate) { this.receiptDate = receiptDate; }

    public String getReceivedBy() { return receivedBy; }
    public void setReceivedBy(String receivedBy) { this.receivedBy = receivedBy; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public List<GoodsReceiptItemRequest> getItems() { return items; }
    public void setItems(List<GoodsReceiptItemRequest> items) { this.items = items; }
}
