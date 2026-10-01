package com.ordercraft.dto;

import com.ordercraft.entity.PoStatus;
import jakarta.validation.constraints.NotNull;

public class PoStatusUpdateRequest {
    @NotNull(message = "PO Status is required")
    private PoStatus status;

    public PoStatusUpdateRequest() {}

    public PoStatusUpdateRequest(PoStatus status) {
        this.status = status;
    }

    public PoStatus getStatus() { return status; }
    public void setStatus(PoStatus status) { this.status = status; }
}
