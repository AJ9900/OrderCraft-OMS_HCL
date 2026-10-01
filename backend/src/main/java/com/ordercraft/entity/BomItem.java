package com.ordercraft.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Entity
@Table(name = "bom_items")
public class BomItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bom_id", nullable = false)
    private Bom bom;

    @NotNull(message = "Material is required")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "material_id", nullable = false)
    private Product material;

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.0001", message = "Quantity must be greater than 0")
    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal quantity;

    @NotBlank(message = "Unit is required")
    @Column(nullable = false, length = 20)
    private String unit;

    @Column(name = "wastage_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal wastagePercentage = BigDecimal.ZERO;

    public BomItem() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Bom getBom() { return bom; }
    public void setBom(Bom bom) { this.bom = bom; }

    public Product getMaterial() { return material; }
    public void setMaterial(Product material) { this.material = material; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public BigDecimal getWastagePercentage() { return wastagePercentage; }
    public void setWastagePercentage(BigDecimal wastagePercentage) { this.wastagePercentage = wastagePercentage; }
}
