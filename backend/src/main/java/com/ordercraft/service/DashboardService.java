package com.ordercraft.service;

import com.ordercraft.dto.DashboardStatsDto;
import com.ordercraft.entity.InvoiceStatus;
import com.ordercraft.entity.OrderStatus;
import com.ordercraft.entity.PoStatus;
import com.ordercraft.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class DashboardService {

    private final CustomerOrderRepository orderRepository;
    private final InventoryRepository inventoryRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final InvoiceRepository invoiceRepository;
    private final ProductionOrderRepository productionOrderRepository;

    public DashboardService(CustomerOrderRepository orderRepository,
                            InventoryRepository inventoryRepository,
                            PurchaseOrderRepository purchaseOrderRepository,
                            InvoiceRepository invoiceRepository,
                            ProductionOrderRepository productionOrderRepository) {
        this.orderRepository = orderRepository;
        this.inventoryRepository = inventoryRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.invoiceRepository = invoiceRepository;
        this.productionOrderRepository = productionOrderRepository;
    }

    @Transactional(readOnly = true)
    public DashboardStatsDto getDashboardStats() {
        DashboardStatsDto stats = new DashboardStatsDto();

        long totalOrders = orderRepository.count();
        long pendingOrders = orderRepository.countByStatuses(List.of(
                OrderStatus.DRAFT,
                OrderStatus.CONFIRMED,
                OrderStatus.MATERIAL_CHECK,
                OrderStatus.MATERIAL_SHORTAGE,
                OrderStatus.READY_FOR_PRODUCTION
        ));
        long ordersInProd = orderRepository.countByStatuses(List.of(
                OrderStatus.IN_PRODUCTION,
                OrderStatus.QUALITY_CHECK
        ));
        long completedOrders = orderRepository.countByStatus(OrderStatus.COMPLETED);

        long lowStock = inventoryRepository.countLowStock();
        long pendingPOs = purchaseOrderRepository.countByStatuses(List.of(
                PoStatus.DRAFT,
                PoStatus.SENT,
                PoStatus.PARTIALLY_RECEIVED
        ));
        long pendingInvoices = invoiceRepository.countByStatuses(List.of(
                InvoiceStatus.DRAFT,
                InvoiceStatus.ISSUED,
                InvoiceStatus.PARTIALLY_PAID,
                InvoiceStatus.OVERDUE
        ));

        BigDecimal outstanding = invoiceRepository.sumOutstandingAmount();
        BigDecimal revenue = orderRepository.sumTotalRevenue();

        stats.setTotalOrders(totalOrders);
        stats.setPendingOrders(pendingOrders);
        stats.setOrdersInProduction(ordersInProd);
        stats.setCompletedOrders(completedOrders);
        stats.setLowStockCount(lowStock);
        stats.setPendingPurchaseOrders(pendingPOs);
        stats.setPendingInvoices(pendingInvoices);
        stats.setOutstandingPaymentsAmount(outstanding != null ? outstanding : BigDecimal.ZERO);
        stats.setTotalRevenue(revenue != null ? revenue : BigDecimal.ZERO);

        stats.setRecentOrders(orderRepository.findTop5ByOrderByCreatedAtDesc());
        stats.setLowStockItems(inventoryRepository.findLowStockInventories());

        return stats;
    }
}
