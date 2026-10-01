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
@Table(name = "boms")
public class Bom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "BOM code is required")
    @Column(name = "bom_code", nullable = false, unique = true, length = 50)
    private String bomCode;

    @NotNull(message = "Product is required")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @NotBlank(message = "Version is required")
    @Column(nullable = false, length = 20)
    private String version = "1.0";

    @Column(nullable = false, length = 30)
    private String status = "ACTIVE"; // ACTIVE, DRAFT, ARCHIVED

    @NotNull(message = "Effective date is required")
    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate = LocalDate.now();

    @Column(columnDefinition = "TEXT")
    private String notes;

    @JsonManagedReference
    @OneToMany(mappedBy = "bom", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<BomItem> items = new ArrayList<>();

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

    public Bom() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBomCode() { return bomCode; }
    public void setBomCode(String bomCode) { this.bomCode = bomCode; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public List<BomItem> getItems() { return items; }
    public void setItems(List<BomItem> items) { this.items = items; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public void addItem(BomItem item) {
        items.add(item);
        item.setBom(this);
    }
}
