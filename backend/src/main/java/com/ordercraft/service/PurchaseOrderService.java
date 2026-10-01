package com.ordercraft.service;

import com.ordercraft.dto.PoItemRequest;
import com.ordercraft.dto.PoStatusUpdateRequest;
import com.ordercraft.dto.PurchaseOrderRequest;
import com.ordercraft.entity.*;
import com.ordercraft.exception.BadRequestException;
import com.ordercraft.exception.ResourceNotFoundException;
import com.ordercraft.repository.ProductRepository;
import com.ordercraft.repository.PurchaseOrderRepository;
import com.ordercraft.repository.SupplierRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final AuditLogService auditLogService;

    public PurchaseOrderService(PurchaseOrderRepository purchaseOrderRepository,
                                SupplierRepository supplierRepository,
                                ProductRepository productRepository,
                                AuditLogService auditLogService) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.supplierRepository = supplierRepository;
        this.productRepository = productRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public Page<PurchaseOrder> getAllPurchaseOrders(String query, PoStatus status, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        return purchaseOrderRepository.searchPurchaseOrders(query, status, startDate, endDate, pageable);
    }

    @Transactional(readOnly = true)
    public PurchaseOrder getPurchaseOrderById(Long id) {
        return purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase Order not found with ID: " + id));
    }

    @Transactional
    public PurchaseOrder createPurchaseOrder(PurchaseOrderRequest req) {
        Supplier supplier = supplierRepository.findById(req.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with ID: " + req.getSupplierId()));

        if (req.getExpectedDeliveryDate() != null && req.getPoDate() != null && req.getExpectedDeliveryDate().isBefore(req.getPoDate())) {
            throw new BadRequestException("Expected delivery date cannot precede PO date");
        }

        String poNum = (req.getPoNumber() != null && !req.getPoNumber().isBlank())
                ? req.getPoNumber().trim()
                : "PO-" + System.currentTimeMillis();

        if (purchaseOrderRepository.existsByPoNumber(poNum)) {
            poNum = "PO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }

        PurchaseOrder po = new PurchaseOrder();
        po.setPoNumber(poNum);
        po.setSupplier(supplier);
        po.setPoDate(req.getPoDate());
        po.setExpectedDeliveryDate(req.getExpectedDeliveryDate());
        po.setStatus(PoStatus.DRAFT);
        po.setNotes(req.getNotes());

        populateItemsAndCalculateTotals(req, po);

        PurchaseOrder saved = purchaseOrderRepository.save(po);
        auditLogService.record("PO_CREATED", "PROCUREMENT", "PurchaseOrder", saved.getId().toString(), null, saved.getPoNumber());
        return saved;
    }

    @Transactional
    public PurchaseOrder updatePurchaseOrder(Long id, PurchaseOrderRequest req) {
        PurchaseOrder po = getPurchaseOrderById(id);

        if (po.getStatus() != PoStatus.DRAFT) {
            throw new BadRequestException("Only DRAFT purchase orders can be edited");
        }

        Supplier supplier = supplierRepository.findById(req.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with ID: " + req.getSupplierId()));

        po.setSupplier(supplier);
        po.setPoDate(req.getPoDate());
        po.setExpectedDeliveryDate(req.getExpectedDeliveryDate());
        po.setNotes(req.getNotes());

        po.getItems().clear();
        populateItemsAndCalculateTotals(req, po);

        PurchaseOrder updated = purchaseOrderRepository.save(po);
        auditLogService.record("PO_UPDATED", "PROCUREMENT", "PurchaseOrder", id.toString(), null, updated.getPoNumber());
        return updated;
    }

    @Transactional
    public PurchaseOrder updatePoStatus(Long id, PoStatusUpdateRequest req) {
        PurchaseOrder po = getPurchaseOrderById(id);
        PoStatus oldStatus = po.getStatus();
        PoStatus newStatus = req.getStatus();

        boolean allowed = switch (oldStatus) {
            case DRAFT -> newStatus == PoStatus.SENT || newStatus == PoStatus.CANCELLED;
            case SENT, PARTIALLY_RECEIVED -> newStatus == PoStatus.CANCELLED;
            case RECEIVED, CANCELLED -> false;
        };
        if (!allowed) {
            throw new BadRequestException("Purchase order status cannot change from " + oldStatus + " to " + newStatus
                    + "; receipts update partial and received statuses automatically");
        }

        po.setStatus(newStatus);
        PurchaseOrder updated = purchaseOrderRepository.save(po);

        auditLogService.record("PO_STATUS_CHANGED", "PROCUREMENT", "PurchaseOrder", id.toString(), oldStatus.name(), newStatus.name());
        return updated;
    }

    @Transactional
    public void deletePurchaseOrder(Long id) {
        PurchaseOrder po = getPurchaseOrderById(id);
        if (po.getStatus() != PoStatus.DRAFT && po.getStatus() != PoStatus.CANCELLED) {
            throw new BadRequestException("Only DRAFT or CANCELLED purchase orders can be deleted");
        }
        purchaseOrderRepository.delete(po);
        auditLogService.record("PO_DELETED", "PROCUREMENT", "PurchaseOrder", id.toString(), po.getPoNumber(), null);
    }

    private void populateItemsAndCalculateTotals(PurchaseOrderRequest req, PurchaseOrder po) {
        BigDecimal total = BigDecimal.ZERO;

        for (PoItemRequest itemReq : req.getItems()) {
            Product material = productRepository.findById(itemReq.getMaterialId())
                    .orElseThrow(() -> new ResourceNotFoundException("Material not found with ID: " + itemReq.getMaterialId()));

            BigDecimal lineSub = itemReq.getUnitPrice().multiply(itemReq.getQuantity());
            BigDecimal lineTax = itemReq.getTax() != null ? itemReq.getTax() : BigDecimal.ZERO;
            BigDecimal lineTotal = lineSub.add(lineTax);

            PoItem item = new PoItem();
            item.setMaterial(material);
            item.setQuantity(itemReq.getQuantity());
            item.setReceivedQuantity(BigDecimal.ZERO);
            item.setUnitPrice(itemReq.getUnitPrice());
            item.setTax(lineTax);
            item.setTotal(lineTotal);

            po.addItem(item);
            total = total.add(lineTotal);
        }

        po.setTotalAmount(total);
    }
}
