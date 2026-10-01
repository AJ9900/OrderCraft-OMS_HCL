package com.ordercraft.controller;

import com.ordercraft.dto.PaymentRequest;
import com.ordercraft.entity.Payment;
import com.ordercraft.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping
    public ResponseEntity<Page<Payment>> getPayments(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String[] sort) {

        Sort.Direction direction = sort.length > 1 && sort[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort[0]));
        return ResponseEntity.ok(paymentService.getAllPayments(query, startDate, endDate, pageable));
    }

    @GetMapping("/by-invoice/{invoiceId}")
    public ResponseEntity<List<Payment>> getPaymentsByInvoiceId(@PathVariable Long invoiceId) {
        return ResponseEntity.ok(paymentService.getPaymentsByInvoiceId(invoiceId));
    }

    @PostMapping
    public ResponseEntity<Payment> recordPayment(@Valid @RequestBody PaymentRequest request) {
        Payment recorded = paymentService.recordPayment(request);
        return new ResponseEntity<>(recorded, HttpStatus.CREATED);
    }
}
