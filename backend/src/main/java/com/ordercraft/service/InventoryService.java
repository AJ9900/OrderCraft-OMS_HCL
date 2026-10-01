package com.ordercraft.service;

import com.ordercraft.dto.StockAdjustmentRequest;
import com.ordercraft.entity.*;
import com.ordercraft.exception.BadRequestException;
import com.ordercraft.exception.ResourceNotFoundException;
import com.ordercraft.repository.InventoryRepository;
import com.ordercraft.repository.ProductRepository;
import com.ordercraft.repository.StockMovementRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final StockMovementRepository stockMovementRepository;
    private final AuditLogService auditLogService;

    public InventoryService(InventoryRepository inventoryRepository,
                            ProductRepository productRepository,
                            StockMovementRepository stockMovementRepository,
                            AuditLogService auditLogService) {
        this.inventoryRepository = inventoryRepository;
        this.productRepository = productRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public Page<Inventory> getAllInventory(String query, String type, Pageable pageable) {
        return inventoryRepository.searchInventory(query, type, pageable);
    }

    @Transactional(readOnly = true)
    public List<Inventory> getLowStockItems() {
        return inventoryRepository.findLowStockInventories();
    }

    @Transactional
    public Inventory getInventoryByProductId(Long productId) {
        return inventoryRepository.findByProductId(productId)
                .orElseGet(() -> {
                    Product p = productRepository.findById(productId)
                            .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));
                    Inventory inv = new Inventory(p, BigDecimal.ZERO, new BigDecimal("10.00"), new BigDecimal("20.00"), p.getCostPrice());
                    return inventoryRepository.save(inv);
                });
    }

    @Transactional
    public Inventory adjustStock(StockAdjustmentRequest req) {
        Inventory inv = getInventoryForUpdate(req.getProductId());
        Product product = inv.getProduct();

        BigDecimal qty = req.getQuantity();
        BigDecimal oldCurrent = inv.getCurrentQuantity();

        String currentUser = getCurrentUsername();

        switch (req.getMovementType()) {
            case IN -> {
                inv.setCurrentQuantity(inv.getCurrentQuantity().add(qty));
            }
            case OUT -> {
                if (inv.getAvailableQuantity().compareTo(qty) < 0) {
                    throw new BadRequestException("Insufficient available stock for " + product.getProductName() +
                            ". Available: " + inv.getAvailableQuantity() + ", requested: " + qty);
                }
                inv.setCurrentQuantity(inv.getCurrentQuantity().subtract(qty));
            }
            case ADJUSTMENT -> {
                // Sets current stock directly
                if (qty.compareTo(inv.getReservedQuantity()) < 0) {
                    throw new BadRequestException("Stock adjustment (" + qty + ") cannot be less than currently reserved quantity (" + inv.getReservedQuantity() + ")");
                }
                inv.setCurrentQuantity(qty);
            }
            default -> throw new BadRequestException("Invalid stock adjustment movement type: " + req.getMovementType());
        }

        if (req.getMinimumStock() != null) inv.setMinimumStock(req.getMinimumStock());
        if (req.getReorderLevel() != null) inv.setReorderLevel(req.getReorderLevel());
        if (req.getUnitCost() != null) inv.setUnitCost(req.getUnitCost());

        inv.updateAvailableQuantity();
        Inventory saved = inventoryRepository.save(inv);

        StockMovement sm = new StockMovement(
                product,
                req.getMovementType(),
                qty,
                "MANUAL_ADJUSTMENT",
                product.getProductCode(),
                req.getNotes() != null ? req.getNotes() : "Manual inventory adjustment",
                currentUser
        );
        stockMovementRepository.save(sm);

        auditLogService.record("INVENTORY_UPDATED", "INVENTORY", "Product", product.getId().toString(),
                "Stock: " + oldCurrent, "New Stock: " + saved.getCurrentQuantity());

        return saved;
    }

    @Transactional
    public void reserveStock(Product product, BigDecimal quantity, String referenceId, String notes) {
        Inventory inv = getInventoryForUpdate(product.getId());
        if (inv.getAvailableQuantity().compareTo(quantity) < 0) {
            throw new BadRequestException("Cannot reserve stock. Available quantity for " + product.getProductName() +
                    " is " + inv.getAvailableQuantity() + ", requested reservation: " + quantity);
        }

        inv.setReservedQuantity(inv.getReservedQuantity().add(quantity));
        inv.updateAvailableQuantity();
        inventoryRepository.save(inv);

        StockMovement sm = new StockMovement(product, StockMovementType.RESERVED, quantity, "CUSTOMER_ORDER", referenceId, notes, getCurrentUsername());
        stockMovementRepository.save(sm);
    }

    @Transactional
    public void releaseReservedStock(Product product, BigDecimal quantity, String referenceId, String notes) {
        Inventory inv = getInventoryForUpdate(product.getId());
        BigDecimal newReserved = inv.getReservedQuantity().subtract(quantity);
        if (newReserved.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Cannot release more stock than is currently reserved");
        }
        inv.setReservedQuantity(newReserved);
        inv.updateAvailableQuantity();
        inventoryRepository.save(inv);

        StockMovement sm = new StockMovement(product, StockMovementType.RELEASED, quantity, "CUSTOMER_ORDER", referenceId, notes, getCurrentUsername());
        stockMovementRepository.save(sm);
    }

    @Transactional
    public void consumeStock(Product product, BigDecimal quantity, String referenceType, String referenceId, String notes) {
        Inventory inv = getInventoryForUpdate(product.getId());
        if (inv.getAvailableQuantity().compareTo(quantity) < 0) {
            throw new BadRequestException("Cannot consume stock. Current quantity for " + product.getProductName() +
                    " is " + inv.getAvailableQuantity() + " available, requested: " + quantity);
        }

        inv.setCurrentQuantity(inv.getCurrentQuantity().subtract(quantity));
        inv.updateAvailableQuantity();
        inventoryRepository.save(inv);

        StockMovement sm = new StockMovement(product, StockMovementType.OUT, quantity, referenceType, referenceId, notes, getCurrentUsername());
        stockMovementRepository.save(sm);
    }

    @Transactional
    public void addStock(Product product, BigDecimal quantity, String referenceType, String referenceId, String notes) {
        Inventory inv = getInventoryForUpdate(product.getId());
        inv.setCurrentQuantity(inv.getCurrentQuantity().add(quantity));
        inv.updateAvailableQuantity();
        inventoryRepository.save(inv);

        StockMovement sm = new StockMovement(product, StockMovementType.IN, quantity, referenceType, referenceId, notes, getCurrentUsername());
        stockMovementRepository.save(sm);
    }

    @Transactional(readOnly = true)
    public Page<StockMovement> getMovements(Long productId, StockMovementType movementType, LocalDateTime start, LocalDateTime end, Pageable pageable) {
        return stockMovementRepository.searchStockMovements(productId, movementType, start, end, pageable);
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) ? auth.getName() : "SYSTEM";
    }

    private Inventory getInventoryForUpdate(Long productId) {
        return inventoryRepository.findByProductIdForUpdate(productId)
                .orElseGet(() -> getInventoryByProductId(productId));
    }
}
