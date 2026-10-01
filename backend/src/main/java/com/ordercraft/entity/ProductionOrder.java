package com.ordercraft.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "production_orders")
public class ProductionOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Production order number is required")
    @Column(name = "production_order_number", nullable = false, unique = true, length = 50)
    private String productionOrderNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_order_id")
    private CustomerOrder customerOrder;

    @NotNull(message = "Product is required")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @NotNull(message = "Planned quantity is required")
    @DecimalMin(value = "0.01", message = "Planned quantity must be greater than 0")
    @Column(name = "planned_quantity", nullable = false, precision = 12, scale = 2)
    private BigDecimal plannedQuantity;

    @Column(name = "produced_quantity", nullable = false, precision = 12, scale = 2)
    private BigDecimal producedQuantity = BigDecimal.ZERO;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ProductionStatus status = ProductionStatus.PLANNED;

    @Column(columnDefinition = "TEXT")
    private String notes;

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

    public ProductionOrder() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getProductionOrderNumber() { return productionOrderNumber; }
    public void setProductionOrderNumber(String productionOrderNumber) { this.productionOrderNumber = productionOrderNumber; }

    public CustomerOrder getCustomerOrder() { return customerOrder; }
    public void setCustomerOrder(CustomerOrder customerOrder) { this.customerOrder = customerOrder; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public BigDecimal getPlannedQuantity() { return plannedQuantity; }
    public void setPlannedQuantity(BigDecimal plannedQuantity) { this.plannedQuantity = plannedQuantity; }

    public BigDecimal getProducedQuantity() { return producedQuantity; }
    public void setProducedQuantity(BigDecimal producedQuantity) { this.producedQuantity = producedQuantity; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public ProductionStatus getStatus() { return status; }
    public void setStatus(ProductionStatus status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
