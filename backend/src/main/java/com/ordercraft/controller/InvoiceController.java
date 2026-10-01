package com.ordercraft.controller;

import com.ordercraft.dto.InvoiceRequest;
import com.ordercraft.entity.Invoice;
import com.ordercraft.entity.InvoiceStatus;
import com.ordercraft.service.InvoiceService;
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

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @GetMapping
    public ResponseEntity<Page<Invoice>> getInvoices(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) InvoiceStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String[] sort) {

        Sort.Direction direction = sort.length > 1 && sort[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort[0]));
        return ResponseEntity.ok(invoiceService.getAllInvoices(query, status, startDate, endDate, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Invoice> getInvoiceById(@PathVariable Long id) {
        return ResponseEntity.ok(invoiceService.getInvoiceById(id));
    }

    @PostMapping
    public ResponseEntity<Invoice> createInvoice(@Valid @RequestBody InvoiceRequest request) {
        Invoice created = invoiceService.createInvoice(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Invoice> updateInvoiceStatus(@PathVariable Long id, @RequestParam InvoiceStatus status) {
        return ResponseEntity.ok(invoiceService.updateInvoiceStatus(id, status));
    }
}
