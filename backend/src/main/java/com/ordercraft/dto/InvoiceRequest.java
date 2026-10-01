package com.ordercraft.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.time.LocalDate;

public class InvoiceRequest {

    private String invoiceNumber;

    @NotNull(message = "Order ID is required")
    private Long orderId;

    @NotNull(message = "Invoice date is required")
    private LocalDate invoiceDate = LocalDate.now();

    @NotNull(message = "Due date is required")
    private LocalDate dueDate;

    @DecimalMin(value = "0.0", message = "Discount cannot be negative")
    private BigDecimal discount = BigDecimal.ZERO;

    public InvoiceRequest() {}

    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public LocalDate getInvoiceDate() { return invoiceDate; }
    public void setInvoiceDate(LocalDate invoiceDate) { this.invoiceDate = invoiceDate; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public BigDecimal getDiscount() { return discount; }
    public void setDiscount(BigDecimal discount) { this.discount = discount; }
}
