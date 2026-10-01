package com.ordercraft.dto;

import com.ordercraft.entity.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public class PaymentRequest {

    private String paymentNumber;

    @NotNull(message = "Invoice ID is required")
    private Long invoiceId;

    @NotNull(message = "Payment date is required")
    private LocalDate paymentDate = LocalDate.now();

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    private String transactionReference;
    private String notes;

    public PaymentRequest() {}

    public String getPaymentNumber() { return paymentNumber; }
    public void setPaymentNumber(String paymentNumber) { this.paymentNumber = paymentNumber; }

    public Long getInvoiceId() { return invoiceId; }
    public void setInvoiceId(Long invoiceId) { this.invoiceId = invoiceId; }

    public LocalDate getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getTransactionReference() { return transactionReference; }
    public void setTransactionReference(String transactionReference) { this.transactionReference = transactionReference; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
