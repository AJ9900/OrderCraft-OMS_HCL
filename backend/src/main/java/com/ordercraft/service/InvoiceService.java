package com.ordercraft.service;

import com.ordercraft.dto.InvoiceRequest;
import com.ordercraft.entity.CustomerOrder;
import com.ordercraft.entity.Invoice;
import com.ordercraft.entity.InvoiceStatus;
import com.ordercraft.entity.OrderStatus;
import com.ordercraft.exception.BadRequestException;
import com.ordercraft.exception.ResourceNotFoundException;
import com.ordercraft.repository.CustomerOrderRepository;
import com.ordercraft.repository.InvoiceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final CustomerOrderRepository customerOrderRepository;
    private final AuditLogService auditLogService;

    public InvoiceService(InvoiceRepository invoiceRepository,
                          CustomerOrderRepository customerOrderRepository,
                          AuditLogService auditLogService) {
        this.invoiceRepository = invoiceRepository;
        this.customerOrderRepository = customerOrderRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public Page<Invoice> getAllInvoices(String query, InvoiceStatus status, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        return invoiceRepository.searchInvoices(query, status, startDate, endDate, pageable);
    }

    @Transactional(readOnly = true)
    public Invoice getInvoiceById(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with ID: " + id));
    }

    @Transactional
    public Invoice createInvoice(InvoiceRequest req) {
        CustomerOrder order = customerOrderRepository.findById(req.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer order not found with ID: " + req.getOrderId()));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new BadRequestException("Cannot create invoice for a CANCELLED order");
        }
        if (invoiceRepository.existsByOrderId(order.getId())) {
            throw new BadRequestException("An invoice already exists for order " + order.getOrderNumber());
        }

        if (req.getDueDate() != null && req.getInvoiceDate() != null && req.getDueDate().isBefore(req.getInvoiceDate())) {
            throw new BadRequestException("Invoice due date cannot precede invoice date");
        }

        String invNum = (req.getInvoiceNumber() != null && !req.getInvoiceNumber().isBlank())
                ? req.getInvoiceNumber().trim()
                : "INV-" + System.currentTimeMillis();

        if (invoiceRepository.existsByInvoiceNumber(invNum)) {
            invNum = "INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }

        BigDecimal discount = req.getDiscount() != null ? req.getDiscount() : BigDecimal.ZERO;
        BigDecimal subtotal = order.getTotalAmount();
        BigDecimal tax = order.getTaxAmount();
        if (discount.compareTo(subtotal) > 0) {
            throw new BadRequestException("Invoice discount cannot exceed the order subtotal");
        }
        BigDecimal grandTotal = subtotal.subtract(discount).add(tax);

        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber(invNum);
        invoice.setCustomer(order.getCustomer());
        invoice.setOrder(order);
        invoice.setInvoiceDate(req.getInvoiceDate());
        invoice.setDueDate(req.getDueDate());
        invoice.setSubtotal(subtotal);
        invoice.setDiscount(discount);
        invoice.setTax(tax);
        invoice.setGrandTotal(grandTotal);
        invoice.setPaidAmount(BigDecimal.ZERO);
        invoice.setStatus(req.getDueDate().isBefore(LocalDate.now()) ? InvoiceStatus.OVERDUE : InvoiceStatus.ISSUED);

        Invoice saved = invoiceRepository.save(invoice);
        auditLogService.record("INVOICE_GENERATED", "FINANCE", "Invoice", saved.getId().toString(),
                null, saved.getInvoiceNumber() + " (Total: " + saved.getGrandTotal() + ")");

        return saved;
    }

    @Transactional
    public Invoice updateInvoiceStatus(Long id, InvoiceStatus status) {
        Invoice invoice = getInvoiceById(id);
        InvoiceStatus oldStatus = invoice.getStatus();
        if (status == InvoiceStatus.PAID || status == InvoiceStatus.PARTIALLY_PAID || status == InvoiceStatus.OVERDUE) {
            throw new BadRequestException("Invoice payment statuses are maintained automatically from payment records");
        }
        if (invoice.getPaidAmount().compareTo(BigDecimal.ZERO) > 0 && status == InvoiceStatus.CANCELLED) {
            throw new BadRequestException("An invoice with recorded payments cannot be cancelled");
        }
        if (oldStatus == InvoiceStatus.PAID || oldStatus == InvoiceStatus.CANCELLED) {
            throw new BadRequestException("Invoice status " + oldStatus + " is final");
        }
        invoice.setStatus(status);
        Invoice updated = invoiceRepository.save(invoice);

        auditLogService.record("INVOICE_STATUS_CHANGED", "FINANCE", "Invoice", id.toString(),
                oldStatus.name(), status.name());
        return updated;
    }
}
