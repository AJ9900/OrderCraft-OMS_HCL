package com.ordercraft.controller;

import com.ordercraft.dto.PoStatusUpdateRequest;
import com.ordercraft.dto.PurchaseOrderRequest;
import com.ordercraft.entity.PoStatus;
import com.ordercraft.entity.PurchaseOrder;
import com.ordercraft.service.PurchaseOrderService;
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
@RequestMapping("/api/purchase-orders")
public class PurchaseOrderController {

    private final PurchaseOrderService poService;

    public PurchaseOrderController(PurchaseOrderService poService) {
        this.poService = poService;
    }

    @GetMapping
    public ResponseEntity<Page<PurchaseOrder>> getPurchaseOrders(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) PoStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String[] sort) {

        Sort.Direction direction = sort.length > 1 && sort[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort[0]));
        return ResponseEntity.ok(poService.getAllPurchaseOrders(query, status, startDate, endDate, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PurchaseOrder> getPurchaseOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(poService.getPurchaseOrderById(id));
    }

    @PostMapping
    public ResponseEntity<PurchaseOrder> createPurchaseOrder(@Valid @RequestBody PurchaseOrderRequest request) {
        PurchaseOrder created = poService.createPurchaseOrder(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PurchaseOrder> updatePurchaseOrder(@PathVariable Long id, @Valid @RequestBody PurchaseOrderRequest request) {
        return ResponseEntity.ok(poService.updatePurchaseOrder(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<PurchaseOrder> updatePoStatus(@PathVariable Long id, @Valid @RequestBody PoStatusUpdateRequest request) {
        return ResponseEntity.ok(poService.updatePoStatus(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePurchaseOrder(@PathVariable Long id) {
        poService.deletePurchaseOrder(id);
        return ResponseEntity.noContent().build();
    }
}
