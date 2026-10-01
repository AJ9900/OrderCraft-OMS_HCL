package com.ordercraft.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "purchase_orders")
public class PurchaseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "PO number is required")
    @Column(name = "po_number", nullable = false, unique = true, length = 50)
    private String poNumber;

    @NotNull(message = "Supplier is required")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @NotNull(message = "PO date is required")
    @Column(name = "po_date", nullable = false)
    private LocalDate poDate = LocalDate.now();

    @NotNull(message = "Expected delivery date is required")
    @Column(name = "expected_delivery_date", nullable = false)
    private LocalDate expectedDeliveryDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private PoStatus status = PoStatus.DRAFT;

    @Column(name = "total_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @JsonManagedReference
    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<PoItem> items = new ArrayList<>();

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

    public PurchaseOrder() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPoNumber() { return poNumber; }
    public void setPoNumber(String poNumber) { this.poNumber = poNumber; }

    public Supplier getSupplier() { return supplier; }
    public void setSupplier(Supplier supplier) { this.supplier = supplier; }

    public LocalDate getPoDate() { return poDate; }
    public void setPoDate(LocalDate poDate) { this.poDate = poDate; }

    public LocalDate getExpectedDeliveryDate() { return expectedDeliveryDate; }
    public void setExpectedDeliveryDate(LocalDate expectedDeliveryDate) { this.expectedDeliveryDate = expectedDeliveryDate; }

    public PoStatus getStatus() { return status; }
    public void setStatus(PoStatus status) { this.status = status; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public List<PoItem> getItems() { return items; }
    public void setItems(List<PoItem> items) { this.items = items; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public void addItem(PoItem item) {
        items.add(item);
        item.setPurchaseOrder(this);
    }
}
