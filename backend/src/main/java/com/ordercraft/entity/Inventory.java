package com.ordercraft.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventories")
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Product is required")
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false, unique = true)
    private Product product;

    @NotNull
    @DecimalMin(value = "0.0", message = "Current quantity cannot be negative")
    @Column(name = "current_quantity", nullable = false, precision = 12, scale = 2)
    private BigDecimal currentQuantity = BigDecimal.ZERO;

    @NotNull
    @DecimalMin(value = "0.0", message = "Reserved quantity cannot be negative")
    @Column(name = "reserved_quantity", nullable = false, precision = 12, scale = 2)
    private BigDecimal reservedQuantity = BigDecimal.ZERO;

    @NotNull
    @Column(name = "available_quantity", nullable = false, precision = 12, scale = 2)
    private BigDecimal availableQuantity = BigDecimal.ZERO;

    @NotNull
    @DecimalMin(value = "0.0", message = "Minimum stock cannot be negative")
    @Column(name = "minimum_stock", nullable = false, precision = 12, scale = 2)
    private BigDecimal minimumStock = BigDecimal.ZERO;

    @NotNull
    @DecimalMin(value = "0.0", message = "Reorder level cannot be negative")
    @Column(name = "reorder_level", nullable = false, precision = 12, scale = 2)
    private BigDecimal reorderLevel = BigDecimal.ZERO;

    @NotNull
    @DecimalMin(value = "0.0", message = "Unit cost cannot be negative")
    @Column(name = "unit_cost", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitCost = BigDecimal.ZERO;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PrePersist
    @PreUpdate
    public void updateAvailableQuantity() {
        if (currentQuantity == null) currentQuantity = BigDecimal.ZERO;
        if (reservedQuantity == null) reservedQuantity = BigDecimal.ZERO;
        this.availableQuantity = currentQuantity.subtract(reservedQuantity);
        this.updatedAt = LocalDateTime.now();
    }

    public Inventory() {}

    public Inventory(Product product, BigDecimal currentQuantity, BigDecimal minimumStock, BigDecimal reorderLevel, BigDecimal unitCost) {
        this.product = product;
        this.currentQuantity = currentQuantity != null ? currentQuantity : BigDecimal.ZERO;
        this.reservedQuantity = BigDecimal.ZERO;
        this.minimumStock = minimumStock != null ? minimumStock : BigDecimal.ZERO;
        this.reorderLevel = reorderLevel != null ? reorderLevel : BigDecimal.ZERO;
        this.unitCost = unitCost != null ? unitCost : BigDecimal.ZERO;
        updateAvailableQuantity();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public BigDecimal getCurrentQuantity() { return currentQuantity; }
    public void setCurrentQuantity(BigDecimal currentQuantity) {
        this.currentQuantity = currentQuantity;
        updateAvailableQuantity();
    }

    public BigDecimal getReservedQuantity() { return reservedQuantity; }
    public void setReservedQuantity(BigDecimal reservedQuantity) {
        this.reservedQuantity = reservedQuantity;
        updateAvailableQuantity();
    }

    public BigDecimal getAvailableQuantity() { return availableQuantity; }
    public void setAvailableQuantity(BigDecimal availableQuantity) { this.availableQuantity = availableQuantity; }

    public BigDecimal getMinimumStock() { return minimumStock; }
    public void setMinimumStock(BigDecimal minimumStock) { this.minimumStock = minimumStock; }

    public BigDecimal getReorderLevel() { return reorderLevel; }
    public void setReorderLevel(BigDecimal reorderLevel) { this.reorderLevel = reorderLevel; }

    public BigDecimal getUnitCost() { return unitCost; }
    public void setUnitCost(BigDecimal unitCost) { this.unitCost = unitCost; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
