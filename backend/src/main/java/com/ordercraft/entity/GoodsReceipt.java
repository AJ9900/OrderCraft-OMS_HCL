package com.ordercraft.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "goods_receipts")
public class GoodsReceipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Receipt number is required")
    @Column(name = "receipt_number", nullable = false, unique = true, length = 50)
    private String receiptNumber;

    @NotNull(message = "Purchase order is required")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "po_id", nullable = false)
    private PurchaseOrder purchaseOrder;

    @NotNull(message = "Receipt date is required")
    @Column(name = "receipt_date", nullable = false)
    private LocalDate receiptDate = LocalDate.now();

    @NotBlank(message = "Received by is required")
    @Column(name = "received_by", nullable = false, length = 50)
    private String receivedBy;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @JsonManagedReference
    @OneToMany(mappedBy = "goodsReceipt", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<GoodsReceiptItem> items = new ArrayList<>();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public GoodsReceipt() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getReceiptNumber() { return receiptNumber; }
    public void setReceiptNumber(String receiptNumber) { this.receiptNumber = receiptNumber; }

    public PurchaseOrder getPurchaseOrder() { return purchaseOrder; }
    public void setPurchaseOrder(PurchaseOrder purchaseOrder) { this.purchaseOrder = purchaseOrder; }

    public LocalDate getReceiptDate() { return receiptDate; }
    public void setReceiptDate(LocalDate receiptDate) { this.receiptDate = receiptDate; }

    public String getReceivedBy() { return receivedBy; }
    public void setReceivedBy(String receivedBy) { this.receivedBy = receivedBy; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public List<GoodsReceiptItem> getItems() { return items; }
    public void setItems(List<GoodsReceiptItem> items) { this.items = items; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public void addItem(GoodsReceiptItem item) {
        items.add(item);
        item.setGoodsReceipt(this);
    }
}
