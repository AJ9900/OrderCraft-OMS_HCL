package com.ordercraft.service;

import com.ordercraft.dto.GoodsReceiptItemRequest;
import com.ordercraft.dto.GoodsReceiptRequest;
import com.ordercraft.entity.*;
import com.ordercraft.exception.BadRequestException;
import com.ordercraft.exception.ResourceNotFoundException;
import com.ordercraft.repository.GoodsReceiptRepository;
import com.ordercraft.repository.PoItemRepository;
import com.ordercraft.repository.PurchaseOrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class GoodsReceiptService {

    private final GoodsReceiptRepository goodsReceiptRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PoItemRepository poItemRepository;
    private final InventoryService inventoryService;
    private final AuditLogService auditLogService;

    public GoodsReceiptService(GoodsReceiptRepository goodsReceiptRepository,
                               PurchaseOrderRepository purchaseOrderRepository,
                               PoItemRepository poItemRepository,
                               InventoryService inventoryService,
                               AuditLogService auditLogService) {
        this.goodsReceiptRepository = goodsReceiptRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.poItemRepository = poItemRepository;
        this.inventoryService = inventoryService;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public Page<GoodsReceipt> getAllReceipts(String query, Pageable pageable) {
        return goodsReceiptRepository.searchGoodsReceipts(query, pageable);
    }

    @Transactional(readOnly = true)
    public GoodsReceipt getReceiptById(Long id) {
        return goodsReceiptRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Goods Receipt not found with ID: " + id));
    }

    @Transactional
    public GoodsReceipt processGoodsReceipt(GoodsReceiptRequest req) {
        PurchaseOrder po = purchaseOrderRepository.findByIdForUpdate(req.getPoId())
                .orElseThrow(() -> new ResourceNotFoundException("Purchase Order not found with ID: " + req.getPoId()));

        if (po.getStatus() == PoStatus.CANCELLED) {
            throw new BadRequestException("Cannot receive goods for a CANCELLED purchase order");
        }
        if (po.getStatus() == PoStatus.RECEIVED) {
            throw new BadRequestException("Purchase order " + po.getPoNumber() + " is already marked as fully RECEIVED");
        }
        if (po.getStatus() != PoStatus.SENT && po.getStatus() != PoStatus.PARTIALLY_RECEIVED) {
            throw new BadRequestException("Goods can only be received against SENT or PARTIALLY_RECEIVED purchase orders");
        }

        String receiptNum = (req.getReceiptNumber() != null && !req.getReceiptNumber().isBlank())
                ? req.getReceiptNumber().trim()
                : "GR-" + System.currentTimeMillis();

        if (goodsReceiptRepository.existsByReceiptNumber(receiptNum)) {
            receiptNum = "GR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }

        String username = getCurrentUsername();
        String receiver = (req.getReceivedBy() != null && !req.getReceivedBy().isBlank()) ? req.getReceivedBy() : username;

        GoodsReceipt gr = new GoodsReceipt();
        gr.setReceiptNumber(receiptNum);
        gr.setPurchaseOrder(po);
        gr.setReceiptDate(req.getReceiptDate());
        gr.setReceivedBy(receiver);
        gr.setNotes(req.getNotes());

        java.util.Set<Long> receivedItemIds = new java.util.HashSet<>();
        for (GoodsReceiptItemRequest itemReq : req.getItems()) {
            PoItem poItem = poItemRepository.findByIdForUpdate(itemReq.getPoItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("PO Item not found with ID: " + itemReq.getPoItemId()));
            if (poItem.getPurchaseOrder() == null || !po.getId().equals(poItem.getPurchaseOrder().getId())) {
                throw new BadRequestException("PO item " + itemReq.getPoItemId() + " does not belong to purchase order " + po.getPoNumber());
            }
            if (!receivedItemIds.add(poItem.getId())) {
                throw new BadRequestException("A PO item can only appear once in a goods receipt");
            }

            BigDecimal requestedRecv = itemReq.getReceivedQuantity();
            BigDecimal remainingExpected = poItem.getQuantity().subtract(poItem.getReceivedQuantity());

            if (requestedRecv.compareTo(remainingExpected) > 0) {
                throw new BadRequestException("Received quantity (" + requestedRecv + ") exceeds remaining expected quantity (" +
                        remainingExpected + ") for material " + poItem.getMaterial().getProductName());
            }

            // Update PO Item received quantity
            poItem.setReceivedQuantity(poItem.getReceivedQuantity().add(requestedRecv));
            poItemRepository.save(poItem);

            // Increase physical inventory & create stock movement
            inventoryService.addStock(
                    poItem.getMaterial(),
                    requestedRecv,
                    "PURCHASE_ORDER",
                    po.getPoNumber() + "/" + receiptNum,
                    "Goods receipt received against PO " + po.getPoNumber()
            );

            GoodsReceiptItem gri = new GoodsReceiptItem();
            gri.setPoItem(poItem);
            gri.setReceivedQuantity(requestedRecv);
            gr.addItem(gri);
        }

        // Check if all items in PO are fully received
        boolean allFullyReceived = true;
        boolean anyReceived = false;

        for (PoItem item : po.getItems()) {
            if (item.getReceivedQuantity().compareTo(item.getQuantity()) < 0) {
                allFullyReceived = false;
            }
            if (item.getReceivedQuantity().compareTo(BigDecimal.ZERO) > 0) {
                anyReceived = true;
            }
        }

        if (allFullyReceived) {
            po.setStatus(PoStatus.RECEIVED);
        } else if (anyReceived) {
            po.setStatus(PoStatus.PARTIALLY_RECEIVED);
        }
        purchaseOrderRepository.save(po);

        GoodsReceipt saved = goodsReceiptRepository.save(gr);
        auditLogService.record("GOODS_RECEIVED", "INVENTORY", "GoodsReceipt", saved.getId().toString(),
                null, "Receipt " + saved.getReceiptNumber() + " for PO " + po.getPoNumber());

        return saved;
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) ? auth.getName() : "WAREHOUSE";
    }
}
