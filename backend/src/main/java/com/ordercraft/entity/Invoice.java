package com.ordercraft.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "invoices")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Invoice number is required")
    @Column(name = "invoice_number", nullable = false, unique = true, length = 50)
    private String invoiceNumber;

    @NotNull(message = "Customer is required")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @NotNull(message = "Customer order is required")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "order_id", nullable = false)
    private CustomerOrder order;

    @NotNull(message = "Invoice date is required")
    @Column(name = "invoice_date", nullable = false)
    private LocalDate invoiceDate = LocalDate.now();

    @NotNull(message = "Due date is required")
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @NotNull
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal tax = BigDecimal.ZERO;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal discount = BigDecimal.ZERO;

    @NotNull
    @Column(name = "grand_total", nullable = false, precision = 14, scale = 2)
    private BigDecimal grandTotal = BigDecimal.ZERO;

    @Column(name = "paid_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private InvoiceStatus status = InvoiceStatus.DRAFT;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public BigDecimal getOutstandingAmount() {
        if (grandTotal == null) return BigDecimal.ZERO;
        if (paidAmount == null) return grandTotal;
        BigDecimal diff = grandTotal.subtract(paidAmount);
        return diff.compareTo(BigDecimal.ZERO) > 0 ? diff : BigDecimal.ZERO;
    }

    public Invoice() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }

    public CustomerOrder getOrder() { return order; }
    public void setOrder(CustomerOrder order) { this.order = order; }

    public LocalDate getInvoiceDate() { return invoiceDate; }
    public void setInvoiceDate(LocalDate invoiceDate) { this.invoiceDate = invoiceDate; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getTax() { return tax; }
    public void setTax(BigDecimal tax) { this.tax = tax; }

    public BigDecimal getDiscount() { return discount; }
    public void setDiscount(BigDecimal discount) { this.discount = discount; }

    public BigDecimal getGrandTotal() { return grandTotal; }
    public void setGrandTotal(BigDecimal grandTotal) { this.grandTotal = grandTotal; }

    public BigDecimal getPaidAmount() { return paidAmount; }
    public void setPaidAmount(BigDecimal paidAmount) { this.paidAmount = paidAmount; }

    public InvoiceStatus getStatus() { return status; }
    public void setStatus(InvoiceStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
