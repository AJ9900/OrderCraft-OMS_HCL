package com.ordercraft.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;

public class OrderRequest {

    private String orderNumber; // auto-generated if null

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotNull(message = "Order date is required")
    private LocalDate orderDate = LocalDate.now();

    @NotNull(message = "Expected delivery date is required")
    private LocalDate expectedDeliveryDate;

    private String notes;

    @NotEmpty(message = "Order must have at least one product item")
    private List<@Valid OrderItemRequest> items;

    public OrderRequest() {}

    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public LocalDate getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDate orderDate) { this.orderDate = orderDate; }

    public LocalDate getExpectedDeliveryDate() { return expectedDeliveryDate; }
    public void setExpectedDeliveryDate(LocalDate expectedDeliveryDate) { this.expectedDeliveryDate = expectedDeliveryDate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public List<OrderItemRequest> getItems() { return items; }
    public void setItems(List<OrderItemRequest> items) { this.items = items; }
}
