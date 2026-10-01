package com.ordercraft.service;

import com.ordercraft.dto.PaymentRequest;
import com.ordercraft.entity.Invoice;
import com.ordercraft.entity.InvoiceStatus;
import com.ordercraft.entity.OrderStatus;
import com.ordercraft.entity.Payment;
import com.ordercraft.exception.BadRequestException;
import com.ordercraft.exception.ResourceNotFoundException;
import com.ordercraft.repository.CustomerOrderRepository;
import com.ordercraft.repository.InvoiceRepository;
import com.ordercraft.repository.PaymentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final CustomerOrderRepository customerOrderRepository;
    private final AuditLogService auditLogService;

    public PaymentService(PaymentRepository paymentRepository,
                          InvoiceRepository invoiceRepository,
                          CustomerOrderRepository customerOrderRepository,
                          AuditLogService auditLogService) {
        this.paymentRepository = paymentRepository;
        this.invoiceRepository = invoiceRepository;
        this.customerOrderRepository = customerOrderRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public Page<Payment> getAllPayments(String query, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        return paymentRepository.searchPayments(query, startDate, endDate, pageable);
    }

    @Transactional(readOnly = true)
    public List<Payment> getPaymentsByInvoiceId(Long invoiceId) {
        return paymentRepository.findByInvoiceId(invoiceId);
    }

    @Transactional
    public Payment recordPayment(PaymentRequest req) {
        Invoice invoice = invoiceRepository.findByIdForUpdate(req.getInvoiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with ID: " + req.getInvoiceId()));

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new BadRequestException("Invoice " + invoice.getInvoiceNumber() + " is already fully paid!");
        }
        if (invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw new BadRequestException("Cannot record payment for a CANCELLED invoice");
        }

        BigDecimal outstanding = invoice.getOutstandingAmount();
        if (req.getAmount().compareTo(outstanding) > 0) {
            throw new BadRequestException("Payment amount (" + req.getAmount() +
                    ") cannot exceed outstanding invoice balance (" + outstanding + ")");
        }

        String payNum = (req.getPaymentNumber() != null && !req.getPaymentNumber().isBlank())
                ? req.getPaymentNumber().trim()
                : "PAY-" + System.currentTimeMillis();

        if (paymentRepository.existsByPaymentNumber(payNum)) {
            payNum = "PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }

        Payment payment = new Payment();
        payment.setPaymentNumber(payNum);
        payment.setInvoice(invoice);
        payment.setPaymentDate(req.getPaymentDate());
        payment.setAmount(req.getAmount());
        payment.setPaymentMethod(req.getPaymentMethod());
        payment.setTransactionReference(req.getTransactionReference());
        payment.setStatus("COMPLETED");
        payment.setNotes(req.getNotes());

        Payment saved = paymentRepository.save(payment);

        // Update invoice paidAmount and status
        BigDecimal newPaid = invoice.getPaidAmount().add(req.getAmount());
        invoice.setPaidAmount(newPaid);

        if (newPaid.compareTo(invoice.getGrandTotal()) >= 0) {
            invoice.setStatus(InvoiceStatus.PAID);
            // Also mark customer order completed if all items delivered and invoice paid
            if (invoice.getOrder() != null && invoice.getOrder().getStatus() == OrderStatus.QUALITY_CHECK) {
                invoice.getOrder().setStatus(OrderStatus.COMPLETED);
                customerOrderRepository.save(invoice.getOrder());
            }
        } else if (invoice.getDueDate().isBefore(LocalDate.now())) {
            invoice.setStatus(InvoiceStatus.OVERDUE);
        } else {
            invoice.setStatus(InvoiceStatus.PARTIALLY_PAID);
        }

        invoiceRepository.save(invoice);

        auditLogService.record("PAYMENT_RECEIVED", "FINANCE", "Payment", saved.getId().toString(),
                "Outstanding: " + outstanding, "Paid: " + saved.getAmount() + " via " + saved.getPaymentMethod());

        return saved;
    }
}
