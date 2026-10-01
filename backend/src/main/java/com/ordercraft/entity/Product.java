package com.ordercraft.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Product code is required")
    @Column(name = "product_code", nullable = false, unique = true, length = 50)
    private String productCode;

    @NotBlank(message = "Product name is required")
    @Column(name = "product_name", nullable = false, length = 100)
    private String productName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotBlank(message = "Category is required")
    @Column(nullable = false, length = 50)
    private String category;

    @Column(nullable = false, length = 30)
    private String type = "FINISHED_GOOD"; // RAW_MATERIAL, FINISHED_GOOD

    @NotNull(message = "Selling price is required")
    @DecimalMin(value = "0.0", message = "Selling price cannot be negative")
    @Column(name = "selling_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal sellingPrice = BigDecimal.ZERO;

    @NotNull(message = "Cost price is required")
    @DecimalMin(value = "0.0", message = "Cost price cannot be negative")
    @Column(name = "cost_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal costPrice = BigDecimal.ZERO;

    @NotBlank(message = "Unit is required")
    @Column(nullable = false, length = 20)
    private String unit = "PCS";

    @Column(nullable = false, length = 30)
    private String status = "ACTIVE";

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

    public Product() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public BigDecimal getSellingPrice() { return sellingPrice; }
    public void setSellingPrice(BigDecimal sellingPrice) { this.sellingPrice = sellingPrice; }

    public BigDecimal getCostPrice() { return costPrice; }
    public void setCostPrice(BigDecimal costPrice) { this.costPrice = costPrice; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
