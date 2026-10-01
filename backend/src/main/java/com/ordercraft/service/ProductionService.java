package com.ordercraft.service;

import com.ordercraft.dto.ProductionOrderRequest;
import com.ordercraft.dto.ProductionProgressRequest;
import com.ordercraft.dto.OrderRequirementResponse;
import com.ordercraft.entity.*;
import com.ordercraft.exception.BadRequestException;
import com.ordercraft.exception.ResourceNotFoundException;
import com.ordercraft.repository.BomRepository;
import com.ordercraft.repository.CustomerOrderRepository;
import com.ordercraft.repository.ProductRepository;
import com.ordercraft.repository.ProductionOrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProductionService {

    private final ProductionOrderRepository productionOrderRepository;
    private final CustomerOrderRepository customerOrderRepository;
    private final ProductRepository productRepository;
    private final BomRepository bomRepository;
    private final InventoryService inventoryService;
    private final AuditLogService auditLogService;
    private final MaterialRequirementService materialRequirementService;

    public ProductionService(ProductionOrderRepository productionOrderRepository,
                             CustomerOrderRepository customerOrderRepository,
                             ProductRepository productRepository,
                             BomRepository bomRepository,
                             InventoryService inventoryService,
                             AuditLogService auditLogService,
                             MaterialRequirementService materialRequirementService) {
        this.productionOrderRepository = productionOrderRepository;
        this.customerOrderRepository = customerOrderRepository;
        this.productRepository = productRepository;
        this.bomRepository = bomRepository;
        this.inventoryService = inventoryService;
        this.auditLogService = auditLogService;
        this.materialRequirementService = materialRequirementService;
    }

    @Transactional(readOnly = true)
    public Page<ProductionOrder> getAllProductionOrders(String query, ProductionStatus status, Pageable pageable) {
        return productionOrderRepository.searchProductionOrders(query, status, pageable);
    }

    @Transactional(readOnly = true)
    public ProductionOrder getProductionOrderById(Long id) {
        return productionOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Production order not found with ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<ProductionOrder> getByCustomerOrderId(Long customerOrderId) {
        return productionOrderRepository.findByCustomerOrderId(customerOrderId);
    }

    @Transactional
    public ProductionOrder createProductionOrder(ProductionOrderRequest req) {
        Product product = productRepository.findById(req.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + req.getProductId()));

        CustomerOrder customerOrder = customerOrderRepository.findById(req.getCustomerOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer order not found with ID: " + req.getCustomerOrderId()));
        if (customerOrder.getStatus() != OrderStatus.READY_FOR_PRODUCTION) {
            throw new BadRequestException("Customer order must pass material availability checks before production can be planned");
        }
        if (customerOrder.getItems().stream().noneMatch(item -> item.getProduct().getId().equals(product.getId()))) {
            throw new BadRequestException("Production product is not part of the selected customer order");
        }
        OrderRequirementResponse requirements = materialRequirementService.checkOrderMaterialRequirement(customerOrder.getId(), false);
        if (requirements.isHasAnyShortage()) {
            throw new BadRequestException("Materials are no longer available for this customer order");
        }

        BigDecimal orderQuantity = customerOrder.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .map(OrderItem::getQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal alreadyPlanned = productionOrderRepository.findByCustomerOrderId(customerOrder.getId()).stream()
                .filter(existing -> existing.getProduct().getId().equals(product.getId()))
                .filter(existing -> existing.getStatus() != ProductionStatus.CANCELLED)
                .map(ProductionOrder::getPlannedQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (alreadyPlanned.add(req.getPlannedQuantity()).compareTo(orderQuantity) > 0) {
            throw new BadRequestException("Planned production quantity exceeds the unplanned quantity on the customer order");
        }

        String prodNum = (req.getProductionOrderNumber() != null && !req.getProductionOrderNumber().isBlank())
                ? req.getProductionOrderNumber().trim()
                : "PROD-" + System.currentTimeMillis();

        if (productionOrderRepository.existsByProductionOrderNumber(prodNum)) {
            prodNum = "PROD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }

        ProductionOrder po = new ProductionOrder();
        po.setProductionOrderNumber(prodNum);
        po.setProduct(product);
        po.setCustomerOrder(customerOrder);
        po.setPlannedQuantity(req.getPlannedQuantity());
        po.setProducedQuantity(BigDecimal.ZERO);
        po.setStartDate(req.getStartDate() != null ? req.getStartDate() : LocalDate.now());
        po.setEndDate(req.getEndDate());
        po.setStatus(ProductionStatus.PLANNED);
        po.setNotes(req.getNotes());

        ProductionOrder saved = productionOrderRepository.save(po);
        auditLogService.record("PRODUCTION_ORDER_CREATED", "PRODUCTION", "ProductionOrder", saved.getId().toString(),
                null, saved.getProductionOrderNumber() + " for " + product.getProductName());

        return saved;
    }

    @Transactional
    public ProductionOrder updateProductionProgress(Long id, ProductionProgressRequest req) {
        ProductionOrder po = getProductionOrderById(id);
        ProductionStatus oldStatus = po.getStatus();
        ProductionStatus newStatus = req.getStatus();

        if (oldStatus == ProductionStatus.COMPLETED) {
            throw new BadRequestException("Completed production orders cannot be altered");
        }
        if (oldStatus == ProductionStatus.CANCELLED) {
            throw new BadRequestException("Cancelled production orders cannot be updated");
        }
        if (!isAllowedTransition(oldStatus, newStatus)) {
            throw new BadRequestException("Production status cannot change from " + oldStatus + " to " + newStatus);
        }

        if (req.getProducedQuantity().compareTo(po.getPlannedQuantity()) > 0) {
            throw new BadRequestException("Produced quantity (" + req.getProducedQuantity() +
                    ") cannot exceed planned quantity (" + po.getPlannedQuantity() + ")");
        }
        if (req.getProducedQuantity().compareTo(po.getProducedQuantity()) < 0) {
            throw new BadRequestException("Produced quantity cannot be reduced");
        }
        if (newStatus == ProductionStatus.MATERIAL_READY || newStatus == ProductionStatus.IN_PROGRESS) {
            if (po.getCustomerOrder() != null && materialRequirementService
                    .checkOrderMaterialRequirement(po.getCustomerOrder().getId(), false).isHasAnyShortage()) {
                throw new BadRequestException("Materials are no longer available for this customer order");
            }
        }

        po.setProducedQuantity(req.getProducedQuantity());
        po.setStatus(newStatus);
        if (req.getNotes() != null) po.setNotes(req.getNotes());
        if (newStatus == ProductionStatus.IN_PROGRESS && po.getCustomerOrder() != null) {
            po.getCustomerOrder().setStatus(OrderStatus.IN_PRODUCTION);
            customerOrderRepository.save(po.getCustomerOrder());
        }

        // When production reaches COMPLETED: consume raw materials and add finished goods
        if (newStatus == ProductionStatus.COMPLETED) {
            if (po.getProducedQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BadRequestException("Completed production must include a positive produced quantity");
            }
            po.setEndDate(LocalDate.now());

            consumeMaterialsForProduction(po);

            // Increase finished good stock
            inventoryService.addStock(
                    po.getProduct(),
                    po.getProducedQuantity(),
                    "PRODUCTION",
                    po.getProductionOrderNumber(),
                    "Finished goods produced from production order " + po.getProductionOrderNumber()
            );

            // If linked to customer order, update its status
            if (po.getCustomerOrder() != null) {
                CustomerOrder co = po.getCustomerOrder();
                co.setStatus(OrderStatus.QUALITY_CHECK);
                customerOrderRepository.save(co);
            }
        }

        ProductionOrder saved = productionOrderRepository.save(po);
        auditLogService.record("PRODUCTION_PROGRESS_UPDATED", "PRODUCTION", "ProductionOrder", id.toString(),
                oldStatus.name(), newStatus.name() + " (Produced: " + saved.getProducedQuantity() + "/" + saved.getPlannedQuantity() + ")");

        return saved;
    }

    private void consumeMaterialsForProduction(ProductionOrder po) {
        Optional<Bom> bomOpt = bomRepository.findFirstByProductIdAndStatusOrderByVersionDesc(po.getProduct().getId(), "ACTIVE");
        if (bomOpt.isEmpty()) {
            throw new BadRequestException("Cannot complete production without an ACTIVE BOM for " + po.getProduct().getProductName());
        }

        Bom bom = bomOpt.get();
        for (BomItem item : bom.getItems()) {
            BigDecimal wastageFactor = BigDecimal.ONE;
            if (item.getWastagePercentage() != null && item.getWastagePercentage().compareTo(BigDecimal.ZERO) > 0) {
                wastageFactor = BigDecimal.ONE.add(item.getWastagePercentage().divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
            }
            BigDecimal requiredQty = item.getQuantity().multiply(po.getProducedQuantity()).multiply(wastageFactor).setScale(2, RoundingMode.CEILING);

            inventoryService.consumeStock(
                    item.getMaterial(),
                    requiredQty,
                    "PRODUCTION",
                    po.getProductionOrderNumber(),
                    "Consumed for production order " + po.getProductionOrderNumber()
            );
        }
    }

    private boolean isAllowedTransition(ProductionStatus current, ProductionStatus next) {
        return switch (current) {
            case PLANNED -> next == ProductionStatus.MATERIAL_READY || next == ProductionStatus.CANCELLED;
            case MATERIAL_READY -> next == ProductionStatus.IN_PROGRESS || next == ProductionStatus.CANCELLED;
            case IN_PROGRESS -> next == ProductionStatus.QUALITY_CHECK;
            case QUALITY_CHECK -> next == ProductionStatus.COMPLETED;
            case COMPLETED, CANCELLED -> false;
        };
    }
}
