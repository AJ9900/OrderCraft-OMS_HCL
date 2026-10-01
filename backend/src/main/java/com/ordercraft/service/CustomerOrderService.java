package com.ordercraft.service;

import com.ordercraft.dto.OrderItemRequest;
import com.ordercraft.dto.OrderRequest;
import com.ordercraft.dto.OrderStatusUpdateRequest;
import com.ordercraft.entity.*;
import com.ordercraft.exception.BadRequestException;
import com.ordercraft.exception.ResourceNotFoundException;
import com.ordercraft.repository.CustomerOrderRepository;
import com.ordercraft.repository.CustomerRepository;
import com.ordercraft.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class CustomerOrderService {

    private final CustomerOrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final AuditLogService auditLogService;

    public CustomerOrderService(CustomerOrderRepository orderRepository,
                                CustomerRepository customerRepository,
                                ProductRepository productRepository,
                                AuditLogService auditLogService) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public Page<CustomerOrder> getAllOrders(String query, OrderStatus status, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        return orderRepository.searchOrders(query, status, startDate, endDate, pageable);
    }

    @Transactional(readOnly = true)
    public CustomerOrder getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer order not found with ID: " + id));
    }

    @Transactional
    public CustomerOrder createOrder(OrderRequest req) {
        validateOrderDates(req.getOrderDate(), req.getExpectedDeliveryDate());

        Customer customer = customerRepository.findById(req.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + req.getCustomerId()));

        String orderNum = (req.getOrderNumber() != null && !req.getOrderNumber().isBlank())
                ? req.getOrderNumber().trim()
                : "ORD-" + System.currentTimeMillis();

        if (orderRepository.existsByOrderNumber(orderNum)) {
            orderNum = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }

        CustomerOrder order = new CustomerOrder();
        order.setOrderNumber(orderNum);
        order.setCustomer(customer);
        order.setOrderDate(req.getOrderDate());
        order.setExpectedDeliveryDate(req.getExpectedDeliveryDate());
        order.setStatus(OrderStatus.DRAFT);
        order.setNotes(req.getNotes());

        populateItemsAndCalculateTotals(req, order);

        CustomerOrder saved = orderRepository.save(order);
        auditLogService.record("ORDER_CREATED", "ORDERS", "CustomerOrder", saved.getId().toString(), null, saved.getOrderNumber() + " for " + customer.getName());
        return saved;
    }

    @Transactional
    public CustomerOrder updateOrder(Long id, OrderRequest req) {
        CustomerOrder order = getOrderById(id);

        if (order.getStatus() != OrderStatus.DRAFT && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new BadRequestException("Orders in status " + order.getStatus() + " cannot be modified");
        }

        validateOrderDates(req.getOrderDate(), req.getExpectedDeliveryDate());

        Customer customer = customerRepository.findById(req.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + req.getCustomerId()));

        String oldVal = order.getOrderNumber() + " (" + order.getGrandTotal() + ")";

        order.setCustomer(customer);
        order.setOrderDate(req.getOrderDate());
        order.setExpectedDeliveryDate(req.getExpectedDeliveryDate());
        order.setNotes(req.getNotes());

        order.getItems().clear();
        populateItemsAndCalculateTotals(req, order);

        CustomerOrder updated = orderRepository.save(order);
        auditLogService.record("ORDER_UPDATED", "ORDERS", "CustomerOrder", id.toString(), oldVal, updated.getOrderNumber() + " (" + updated.getGrandTotal() + ")");
        return updated;
    }

    @Transactional
    public CustomerOrder updateOrderStatus(Long id, OrderStatusUpdateRequest req) {
        CustomerOrder order = getOrderById(id);
        OrderStatus oldStatus = order.getStatus();
        OrderStatus newStatus = req.getStatus();

        validateStatusTransition(oldStatus, newStatus);

        order.setStatus(newStatus);
        if (req.getNotes() != null && !req.getNotes().isBlank()) {
            String combined = (order.getNotes() == null ? "" : order.getNotes() + " | ") + req.getNotes();
            order.setNotes(combined);
        }

        CustomerOrder updated = orderRepository.save(order);
        auditLogService.record("ORDER_STATUS_CHANGED", "ORDERS", "CustomerOrder", id.toString(),
                oldStatus.name(), newStatus.name());
        return updated;
    }

    @Transactional
    public void deleteOrder(Long id) {
        CustomerOrder order = getOrderById(id);
        if (order.getStatus() != OrderStatus.DRAFT && order.getStatus() != OrderStatus.CANCELLED) {
            throw new BadRequestException("Only DRAFT or CANCELLED orders can be deleted");
        }
        orderRepository.delete(order);
        auditLogService.record("ORDER_DELETED", "ORDERS", "CustomerOrder", id.toString(), order.getOrderNumber(), null);
    }

    private void validateOrderDates(LocalDate orderDate, LocalDate expectedDeliveryDate) {
        if (orderDate != null && expectedDeliveryDate != null && expectedDeliveryDate.isBefore(orderDate)) {
            throw new BadRequestException("Expected delivery date (" + expectedDeliveryDate + ") cannot precede order date (" + orderDate + ")");
        }
    }

    private void validateStatusTransition(OrderStatus current, OrderStatus next) {
        if (current == next) return;
        boolean allowed = switch (current) {
            case DRAFT -> next == OrderStatus.CONFIRMED || next == OrderStatus.CANCELLED;
            case CONFIRMED -> next == OrderStatus.MATERIAL_CHECK || next == OrderStatus.CANCELLED;
            case MATERIAL_CHECK -> next == OrderStatus.CANCELLED;
            case MATERIAL_SHORTAGE -> next == OrderStatus.MATERIAL_CHECK || next == OrderStatus.CANCELLED;
            case READY_FOR_PRODUCTION, IN_PRODUCTION, QUALITY_CHECK -> false;
            case COMPLETED, CANCELLED -> false;
        };
        if (!allowed) {
            throw new BadRequestException("Order status cannot change from " + current + " to " + next);
        }
    }

    private void populateItemsAndCalculateTotals(OrderRequest req, CustomerOrder order) {
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal totalTax = BigDecimal.ZERO;

        for (OrderItemRequest itemReq : req.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + itemReq.getProductId()));

            BigDecimal lineSub = itemReq.getUnitPrice().multiply(itemReq.getQuantity());
            BigDecimal lineDisc = itemReq.getDiscount() != null ? itemReq.getDiscount() : BigDecimal.ZERO;
            BigDecimal lineTax = itemReq.getTax() != null ? itemReq.getTax() : BigDecimal.ZERO;
            if (lineDisc.compareTo(lineSub) > 0) {
                throw new BadRequestException("Item discount cannot exceed its unit price multiplied by quantity");
            }
            BigDecimal lineTotal = lineSub.subtract(lineDisc).add(lineTax);

            OrderItem item = new OrderItem();
            item.setProduct(product);
            item.setQuantity(itemReq.getQuantity());
            item.setUnitPrice(itemReq.getUnitPrice());
            item.setDiscount(lineDisc);
            item.setTax(lineTax);
            item.setTotal(lineTotal);

            order.addItem(item);

            subtotal = subtotal.add(lineSub.subtract(lineDisc));
            totalTax = totalTax.add(lineTax);
        }

        order.setTotalAmount(subtotal);
        order.setTaxAmount(totalTax);
        order.setGrandTotal(subtotal.add(totalTax));
    }
}
