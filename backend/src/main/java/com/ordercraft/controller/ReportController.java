package com.ordercraft.controller;

import com.ordercraft.entity.*;
import com.ordercraft.service.ReportService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/orders")
    public ResponseEntity<Page<CustomerOrder>> getOrderReport(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "orderDate"));
        return ResponseEntity.ok(reportService.getOrderReport(query, status, startDate, endDate, pageable));
    }

    @GetMapping("/orders/export")
    public ResponseEntity<byte[]> exportOrdersReport(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Pageable unpaged = PageRequest.of(0, 1000, Sort.by(Sort.Direction.DESC, "orderDate"));
        List<CustomerOrder> list = reportService.getOrderReport(query, status, startDate, endDate, unpaged).getContent();
        byte[] csv = reportService.generateOrdersCsv(list);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"orders-report.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    @GetMapping("/inventory")
    public ResponseEntity<Page<Inventory>> getInventoryReport(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "product.productName"));
        return ResponseEntity.ok(reportService.getInventoryReport(query, type, pageable));
    }

    @GetMapping("/inventory/export")
    public ResponseEntity<byte[]> exportInventoryReport(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String type) {
        Pageable unpaged = PageRequest.of(0, 1000, Sort.by(Sort.Direction.ASC, "product.productName"));
        List<Inventory> list = reportService.getInventoryReport(query, type, unpaged).getContent();
        byte[] csv = reportService.generateInventoryCsv(list);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"inventory-report.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<Inventory>> getLowStockReport() {
        return ResponseEntity.ok(reportService.getLowStockReport());
    }

    @GetMapping("/purchase-orders")
    public ResponseEntity<Page<PurchaseOrder>> getPoReport(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) PoStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "poDate"));
        return ResponseEntity.ok(reportService.getPoReport(query, status, startDate, endDate, pageable));
    }

    @GetMapping("/production")
    public ResponseEntity<Page<ProductionOrder>> getProductionReport(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) ProductionStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(reportService.getProductionReport(query, status, pageable));
    }

    @GetMapping("/invoices")
    public ResponseEntity<Page<Invoice>> getInvoiceReport(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) InvoiceStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "invoiceDate"));
        return ResponseEntity.ok(reportService.getInvoiceReport(query, status, startDate, endDate, pageable));
    }

    @GetMapping("/outstanding-invoices")
    public ResponseEntity<List<Invoice>> getOutstandingInvoiceReport() {
        return ResponseEntity.ok(reportService.getOutstandingInvoiceReport());
    }

    @GetMapping("/payments")
    public ResponseEntity<Page<Payment>> getPaymentReport(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "paymentDate"));
        return ResponseEntity.ok(reportService.getPaymentReport(query, startDate, endDate, pageable));
    }
}
