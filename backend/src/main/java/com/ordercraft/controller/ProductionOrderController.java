package com.ordercraft.controller;

import com.ordercraft.dto.ProductionOrderRequest;
import com.ordercraft.dto.ProductionProgressRequest;
import com.ordercraft.entity.ProductionOrder;
import com.ordercraft.entity.ProductionStatus;
import com.ordercraft.service.ProductionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/production-orders")
public class ProductionOrderController {

    private final ProductionService productionService;

    public ProductionOrderController(ProductionService productionService) {
        this.productionService = productionService;
    }

    @GetMapping
    public ResponseEntity<Page<ProductionOrder>> getProductionOrders(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) ProductionStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String[] sort) {

        Sort.Direction direction = sort.length > 1 && sort[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort[0]));
        return ResponseEntity.ok(productionService.getAllProductionOrders(query, status, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductionOrder> getProductionOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(productionService.getProductionOrderById(id));
    }

    @GetMapping("/by-order/{orderId}")
    public ResponseEntity<List<ProductionOrder>> getByCustomerOrderId(@PathVariable Long orderId) {
        return ResponseEntity.ok(productionService.getByCustomerOrderId(orderId));
    }

    @PostMapping
    public ResponseEntity<ProductionOrder> createProductionOrder(@Valid @RequestBody ProductionOrderRequest request) {
        ProductionOrder created = productionService.createProductionOrder(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PatchMapping("/{id}/progress")
    public ResponseEntity<ProductionOrder> updateProductionProgress(
            @PathVariable Long id,
            @Valid @RequestBody ProductionProgressRequest request) {
        return ResponseEntity.ok(productionService.updateProductionProgress(id, request));
    }
}
