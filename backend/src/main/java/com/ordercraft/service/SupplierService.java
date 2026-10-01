package com.ordercraft.service;

import com.ordercraft.dto.SupplierRequest;
import com.ordercraft.entity.Supplier;
import com.ordercraft.exception.BadRequestException;
import com.ordercraft.exception.ResourceNotFoundException;
import com.ordercraft.repository.SupplierRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final AuditLogService auditLogService;

    public SupplierService(SupplierRepository supplierRepository, AuditLogService auditLogService) {
        this.supplierRepository = supplierRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public Page<Supplier> getAllSuppliers(String query, String status, Pageable pageable) {
        return supplierRepository.searchSuppliers(query, status, pageable);
    }

    @Transactional(readOnly = true)
    public List<Supplier> getActiveSuppliers() {
        return supplierRepository.findAll().stream()
                .filter(s -> "ACTIVE".equalsIgnoreCase(s.getStatus()))
                .toList();
    }

    @Transactional(readOnly = true)
    public Supplier getSupplierById(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with ID: " + id));
    }

    @Transactional
    public Supplier createSupplier(SupplierRequest req) {
        if (supplierRepository.existsBySupplierCode(req.getSupplierCode())) {
            throw new BadRequestException("Supplier code already exists: " + req.getSupplierCode());
        }

        Supplier supplier = new Supplier();
        mapRequestToEntity(req, supplier);
        Supplier saved = supplierRepository.save(supplier);

        auditLogService.record("SUPPLIER_CREATED", "SUPPLIERS", "Supplier", saved.getId().toString(), null, saved.getSupplierName());
        return saved;
    }

    @Transactional
    public Supplier updateSupplier(Long id, SupplierRequest req) {
        Supplier supplier = getSupplierById(id);

        if (supplierRepository.existsBySupplierCodeAndIdNot(req.getSupplierCode(), id)) {
            throw new BadRequestException("Supplier code is already used by another supplier: " + req.getSupplierCode());
        }

        String oldVal = supplier.getSupplierName();
        mapRequestToEntity(req, supplier);
        Supplier updated = supplierRepository.save(supplier);

        auditLogService.record("SUPPLIER_UPDATED", "SUPPLIERS", "Supplier", id.toString(), oldVal, updated.getSupplierName());
        return updated;
    }

    @Transactional
    public void deleteSupplier(Long id) {
        Supplier supplier = getSupplierById(id);
        if (!"INACTIVE".equalsIgnoreCase(supplier.getStatus())) {
            supplier.setStatus("INACTIVE");
            supplierRepository.save(supplier);
            auditLogService.record("SUPPLIER_DEACTIVATED", "SUPPLIERS", "Supplier", id.toString(), "ACTIVE", "INACTIVE");
        }
    }

    private void mapRequestToEntity(SupplierRequest req, Supplier entity) {
        entity.setSupplierCode(req.getSupplierCode().trim());
        entity.setSupplierName(req.getSupplierName().trim());
        entity.setEmail(req.getEmail().trim());
        entity.setPhone(req.getPhone().trim());
        entity.setAddress(req.getAddress().trim());
        entity.setTaxIdentifier(req.getTaxIdentifier());
        if (req.getStatus() != null && !req.getStatus().isBlank()) {
            entity.setStatus(req.getStatus().trim().toUpperCase());
        }
    }
}
