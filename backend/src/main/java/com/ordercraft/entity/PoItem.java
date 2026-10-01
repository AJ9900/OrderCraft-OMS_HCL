package com.ordercraft.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Entity
@Table(name = "po_items")
public class PoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "po_id", nullable = false)
    private PurchaseOrder purchaseOrder;

    @NotNull(message = "Material is required")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "material_id", nullable = false)
    private Product material;

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.01", message = "Quantity must be greater than 0")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal quantity;

    @Column(name = "received_quantity", nullable = false, precision = 12, scale = 2)
    private BigDecimal receivedQuantity = BigDecimal.ZERO;

    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.0", message = "Unit price cannot be negative")
    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal tax = BigDecimal.ZERO;

    @NotNull
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal total;

    public PoItem() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public PurchaseOrder getPurchaseOrder() { return purchaseOrder; }
    public void setPurchaseOrder(PurchaseOrder purchaseOrder) { this.purchaseOrder = purchaseOrder; }

    public Product getMaterial() { return material; }
    public void setMaterial(Product material) { this.material = material; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public BigDecimal getReceivedQuantity() { return receivedQuantity; }
    public void setReceivedQuantity(BigDecimal receivedQuantity) { this.receivedQuantity = receivedQuantity; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public BigDecimal getTax() { return tax; }
    public void setTax(BigDecimal tax) { this.tax = tax; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
}
