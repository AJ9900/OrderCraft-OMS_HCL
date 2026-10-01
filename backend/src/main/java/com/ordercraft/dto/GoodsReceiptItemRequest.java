package com.ordercraft.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class GoodsReceiptItemRequest {

    @NotNull(message = "PO item ID is required")
    private Long poItemId;

    @NotNull(message = "Received quantity is required")
    @DecimalMin(value = "0.01", message = "Received quantity must be greater than 0")
    private BigDecimal receivedQuantity;

    public GoodsReceiptItemRequest() {}

    public Long getPoItemId() { return poItemId; }
    public void setPoItemId(Long poItemId) { this.poItemId = poItemId; }

    public BigDecimal getReceivedQuantity() { return receivedQuantity; }
    public void setReceivedQuantity(BigDecimal receivedQuantity) { this.receivedQuantity = receivedQuantity; }
}
