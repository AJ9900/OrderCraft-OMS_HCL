package com.ordercraft.service;

import com.ordercraft.dto.ProductionProgressRequest;
import com.ordercraft.entity.ProductionOrder;
import com.ordercraft.entity.ProductionStatus;
import com.ordercraft.entity.Product;
import com.ordercraft.exception.BadRequestException;
import com.ordercraft.repository.BomRepository;
import com.ordercraft.repository.CustomerOrderRepository;
import com.ordercraft.repository.ProductRepository;
import com.ordercraft.repository.ProductionOrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductionServiceTest {

    @Mock private ProductionOrderRepository productionOrderRepository;
    @Mock private CustomerOrderRepository customerOrderRepository;
    @Mock private ProductRepository productRepository;
    @Mock private BomRepository bomRepository;
    @Mock private InventoryService inventoryService;
    @Mock private AuditLogService auditLogService;

    @InjectMocks private ProductionService productionService;

    @Test
    void cannotSkipQualityCheck() {
        ProductionOrder order = new ProductionOrder();
        order.setStatus(ProductionStatus.IN_PROGRESS);
        order.setPlannedQuantity(new BigDecimal("10"));
        order.setProducedQuantity(new BigDecimal("8"));
        when(productionOrderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(BadRequestException.class, () -> productionService.updateProductionProgress(
                1L, new ProductionProgressRequest(ProductionStatus.COMPLETED, new BigDecimal("9"))));

        verify(bomRepository, never()).findFirstByProductIdAndStatusOrderByVersionDesc(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyString());
        verify(inventoryService, never()).addStock(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void cannotCompleteWithoutActiveBom() {
        ProductionOrder order = new ProductionOrder();
        Product product = new Product();
        product.setId(3L);
        product.setProductName("Office chair");
        order.setProduct(product);
        order.setStatus(ProductionStatus.QUALITY_CHECK);
        order.setPlannedQuantity(new BigDecimal("10"));
        order.setProducedQuantity(new BigDecimal("8"));
        when(productionOrderRepository.findById(2L)).thenReturn(Optional.of(order));
        when(bomRepository.findFirstByProductIdAndStatusOrderByVersionDesc(3L, "ACTIVE")).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> productionService.updateProductionProgress(
                2L, new ProductionProgressRequest(ProductionStatus.COMPLETED, new BigDecimal("8"))));

        verify(inventoryService, never()).addStock(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString());
    }
}