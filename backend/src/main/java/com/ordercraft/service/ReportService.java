package com.ordercraft.service;

import com.ordercraft.entity.*;
import com.ordercraft.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReportService {

    private final CustomerOrderRepository orderRepository;
    private final InventoryRepository inventoryRepository;
    private final PurchaseOrderRepository poRepository;
    private final ProductionOrderRepository productionRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;

    public ReportService(CustomerOrderRepository orderRepository,
                         InventoryRepository inventoryRepository,
                         PurchaseOrderRepository poRepository,
                         ProductionOrderRepository productionRepository,
                         InvoiceRepository invoiceRepository,
                         PaymentRepository paymentRepository) {
        this.orderRepository = orderRepository;
        this.inventoryRepository = inventoryRepository;
        this.poRepository = poRepository;
        this.productionRepository = productionRepository;
        this.invoiceRepository = invoiceRepository;
        this.paymentRepository = paymentRepository;
    }

    @Transactional(readOnly = true)
    public Page<CustomerOrder> getOrderReport(String query, OrderStatus status, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        return orderRepository.searchOrders(query, status, startDate, endDate, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Inventory> getInventoryReport(String query, String type, Pageable pageable) {
        return inventoryRepository.searchInventory(query, type, pageable);
    }

    @Transactional(readOnly = true)
    public List<Inventory> getLowStockReport() {
        return inventoryRepository.findLowStockInventories();
    }

    @Transactional(readOnly = true)
    public Page<PurchaseOrder> getPoReport(String query, PoStatus status, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        return poRepository.searchPurchaseOrders(query, status, startDate, endDate, pageable);
    }

    @Transactional(readOnly = true)
    public Page<ProductionOrder> getProductionReport(String query, ProductionStatus status, Pageable pageable) {
        return productionRepository.searchProductionOrders(query, status, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Invoice> getInvoiceReport(String query, InvoiceStatus status, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        return invoiceRepository.searchInvoices(query, status, startDate, endDate, pageable);
    }

    @Transactional(readOnly = true)
    public List<Invoice> getOutstandingInvoiceReport() {
        return invoiceRepository.findOutstandingInvoices();
    }

    @Transactional(readOnly = true)
    public Page<Payment> getPaymentReport(String query, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        return paymentRepository.searchPayments(query, startDate, endDate, pageable);
    }

    // CSV Generation Utility
    public byte[] generateOrdersCsv(List<CustomerOrder> orders) {
        StringBuilder sb = new StringBuilder();
        sb.append("Order Number,Customer Name,Customer Code,Order Date,Expected Delivery,Status,Total Amount,Tax Amount,Grand Total\n");
        for (CustomerOrder o : orders) {
            sb.append("\"").append(o.getOrderNumber()).append("\",")
              .append("\"").append(o.getCustomer().getName()).append("\",")
              .append("\"").append(o.getCustomer().getCustomerCode()).append("\",")
              .append(o.getOrderDate()).append(",")
              .append(o.getExpectedDeliveryDate()).append(",")
              .append(o.getStatus()).append(",")
              .append(o.getTotalAmount()).append(",")
              .append(o.getTaxAmount()).append(",")
              .append(o.getGrandTotal()).append("\n");
        }
        return sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    public byte[] generateInventoryCsv(List<Inventory> list) {
        StringBuilder sb = new StringBuilder();
        sb.append("Product Code,Product Name,Category,Type,Unit,Current Stock,Reserved Stock,Available Stock,Min Stock,Reorder Level,Unit Cost\n");
        for (Inventory i : list) {
            sb.append("\"").append(i.getProduct().getProductCode()).append("\",")
              .append("\"").append(i.getProduct().getProductName()).append("\",")
              .append("\"").append(i.getProduct().getCategory()).append("\",")
              .append(i.getProduct().getType()).append(",")
              .append(i.getProduct().getUnit()).append(",")
              .append(i.getCurrentQuantity()).append(",")
              .append(i.getReservedQuantity()).append(",")
              .append(i.getAvailableQuantity()).append(",")
              .append(i.getMinimumStock()).append(",")
              .append(i.getReorderLevel()).append(",")
              .append(i.getUnitCost()).append("\n");
        }
        return sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }
}
