package com.ordercraft.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class SupplierRequest {

    @NotBlank(message = "Supplier code is required")
    private String supplierCode;

    @NotBlank(message = "Supplier name is required")
    private String supplierName;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Phone is required")
    private String phone;

    @NotBlank(message = "Address is required")
    private String address;

    private String taxIdentifier;
    private String status = "ACTIVE";

    public SupplierRequest() {}

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
}
