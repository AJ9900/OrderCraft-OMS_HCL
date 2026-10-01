package com.ordercraft.service;

import com.ordercraft.dto.MaterialRequirementDto;
import com.ordercraft.dto.OrderRequirementResponse;
import com.ordercraft.entity.*;
import com.ordercraft.exception.BadRequestException;
import com.ordercraft.exception.ResourceNotFoundException;
import com.ordercraft.repository.BomRepository;
import com.ordercraft.repository.CustomerOrderRepository;
import com.ordercraft.repository.InventoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class MaterialRequirementService {

    private final CustomerOrderRepository orderRepository;
    private final BomRepository bomRepository;
    private final InventoryRepository inventoryRepository;
    private final AuditLogService auditLogService;

    public MaterialRequirementService(CustomerOrderRepository orderRepository,
                                      BomRepository bomRepository,
                                      InventoryRepository inventoryRepository,
                                      AuditLogService auditLogService) {
        this.orderRepository = orderRepository;
        this.bomRepository = bomRepository;
        this.inventoryRepository = inventoryRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public OrderRequirementResponse checkOrderMaterialRequirement(Long orderId, boolean updateOrderStatus) {
        CustomerOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer order not found with ID: " + orderId));

        Map<Long, MaterialRequirementDto> requirementMap = new LinkedHashMap<>();

        for (OrderItem item : order.getItems()) {
            Product finishedGood = item.getProduct();
            BigDecimal orderQty = item.getQuantity();

            // Find active BOM for finished good
            Optional<Bom> activeBomOpt = bomRepository.findFirstByProductIdAndStatusOrderByVersionDesc(finishedGood.getId(), "ACTIVE");
            if (activeBomOpt.isEmpty()) {
                throw new BadRequestException("Cannot compute material requirement: No ACTIVE BOM configured for product "
                        + finishedGood.getProductName() + " (" + finishedGood.getProductCode() + ")");
            }

            Bom bom = activeBomOpt.get();
            for (BomItem bomItem : bom.getItems()) {
                Product material = bomItem.getMaterial();
                BigDecimal perUnitQty = bomItem.getQuantity();

                // Account for wastage percentage if any: totalNeeded = qty * (1 + wastage / 100)
                BigDecimal wastageFactor = BigDecimal.ONE;
                if (bomItem.getWastagePercentage() != null && bomItem.getWastagePercentage().compareTo(BigDecimal.ZERO) > 0) {
                    wastageFactor = BigDecimal.ONE.add(bomItem.getWastagePercentage().divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
                }

                BigDecimal totalNeeded = perUnitQty.multiply(orderQty).multiply(wastageFactor).setScale(2, RoundingMode.CEILING);

                requirementMap.compute(material.getId(), (k, existing) -> {
                    if (existing == null) {
                        MaterialRequirementDto dto = new MaterialRequirementDto();
                        dto.setMaterialId(material.getId());
                        dto.setMaterialCode(material.getProductCode());
                        dto.setMaterialName(material.getProductName());
                        dto.setUnit(bomItem.getUnit());
                        dto.setUnitCost(material.getCostPrice());
                        dto.setRequiredQuantity(totalNeeded);
                        return dto;
                    } else {
                        existing.setRequiredQuantity(existing.getRequiredQuantity().add(totalNeeded));
                        return existing;
                    }
                });
            }
        }

        boolean anyShortage = false;
        List<MaterialRequirementDto> requirements = new ArrayList<>();

        for (MaterialRequirementDto dto : requirementMap.values()) {
            Inventory inv = inventoryRepository.findByProductId(dto.getMaterialId())
                    .orElseGet(() -> {
                        Inventory fallback = new Inventory();
                        fallback.setCurrentQuantity(BigDecimal.ZERO);
                        fallback.setReservedQuantity(BigDecimal.ZERO);
                        fallback.setAvailableQuantity(BigDecimal.ZERO);
                        return fallback;
                    });

            dto.setCurrentQuantity(inv.getCurrentQuantity());
            dto.setReservedQuantity(inv.getReservedQuantity());
            dto.setAvailableQuantity(inv.getAvailableQuantity());

            BigDecimal shortage = dto.getRequiredQuantity().subtract(inv.getAvailableQuantity());
            if (shortage.compareTo(BigDecimal.ZERO) > 0) {
                dto.setShortageQuantity(shortage);
                dto.setHasShortage(true);
                anyShortage = true;
            } else {
                dto.setShortageQuantity(BigDecimal.ZERO);
                dto.setHasShortage(false);
            }

            requirements.add(dto);
        }

        OrderRequirementResponse response = new OrderRequirementResponse();
        response.setOrderId(order.getId());
        response.setOrderNumber(order.getOrderNumber());
        response.setCustomerName(order.getCustomer().getName());
        response.setOrderStatus(order.getStatus());
        response.setRequirements(requirements);
        response.setHasAnyShortage(anyShortage);

        if (anyShortage) {
            response.setSummaryMessage("Material shortage detected! One or more required components need procurement via Purchase Orders.");
            if (updateOrderStatus && (order.getStatus() == OrderStatus.CONFIRMED || order.getStatus() == OrderStatus.MATERIAL_CHECK)) {
                order.setStatus(OrderStatus.MATERIAL_SHORTAGE);
                orderRepository.save(order);
                auditLogService.record("ORDER_STATUS_UPDATED", "ORDERS", "CustomerOrder", order.getId().toString(),
                        "CONFIRMED", "MATERIAL_SHORTAGE");
            }
        } else {
            response.setSummaryMessage("All required materials are available in inventory. Ready for production scheduling!");
            if (updateOrderStatus && (order.getStatus() == OrderStatus.CONFIRMED || order.getStatus() == OrderStatus.MATERIAL_CHECK || order.getStatus() == OrderStatus.MATERIAL_SHORTAGE)) {
                order.setStatus(OrderStatus.READY_FOR_PRODUCTION);
                orderRepository.save(order);
                auditLogService.record("ORDER_STATUS_UPDATED", "ORDERS", "CustomerOrder", order.getId().toString(),
                        "MATERIAL_CHECK", "READY_FOR_PRODUCTION");
            }
        }

        return response;
    }

    @Transactional(readOnly = true)
    public List<MaterialRequirementDto> getAllSystemShortages() {
        // Collect shortage list across all confirmed or material shortage orders
        List<CustomerOrder> pendingOrders = orderRepository.findAll().stream()
                .filter(o -> o.getStatus() == OrderStatus.CONFIRMED || o.getStatus() == OrderStatus.MATERIAL_SHORTAGE || o.getStatus() == OrderStatus.MATERIAL_CHECK)
                .toList();

        Map<Long, MaterialRequirementDto> aggregateShortages = new LinkedHashMap<>();

        for (CustomerOrder o : pendingOrders) {
            try {
                OrderRequirementResponse reqResp = checkOrderMaterialRequirement(o.getId(), false);
                for (MaterialRequirementDto req : reqResp.getRequirements()) {
                    if (req.isHasShortage()) {
                        aggregateShortages.compute(req.getMaterialId(), (k, existing) -> {
                            if (existing == null) return req;
                            existing.setRequiredQuantity(existing.getRequiredQuantity().add(req.getRequiredQuantity()));
                            existing.setShortageQuantity(existing.getShortageQuantity().add(req.getShortageQuantity()));
                            return existing;
                        });
                    }
                }
            } catch (Exception ignored) {}
        }

        return new ArrayList<>(aggregateShortages.values());
    }
}
