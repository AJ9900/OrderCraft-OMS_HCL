package com.ordercraft.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Entity
@Table(name = "suppliers")
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Supplier code is required")
    @Column(name = "supplier_code", nullable = false, unique = true, length = 50)
    private String supplierCode;

    @NotBlank(message = "Supplier name is required")
    @Column(name = "supplier_name", nullable = false, length = 100)
    private String supplierName;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    @Column(nullable = false, length = 100)
    private String email;

    @NotBlank(message = "Phone is required")
    @Column(nullable = false, length = 30)
    private String phone;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(name = "tax_identifier", length = 50)
    private String taxIdentifier;

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

    public Supplier() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSupplierCode() { return supplierCode; }
    public void setSupplierCode(String supplierCode) { this.supplierCode = supplierCode; }

    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getTaxIdentifier() { return taxIdentifier; }
    public void setTaxIdentifier(String taxIdentifier) { this.taxIdentifier = taxIdentifier; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
