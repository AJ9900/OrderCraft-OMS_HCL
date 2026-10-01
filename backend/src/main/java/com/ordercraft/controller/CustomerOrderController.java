package com.ordercraft.controller;

import com.ordercraft.dto.OrderRequest;
import com.ordercraft.dto.OrderStatusUpdateRequest;
import com.ordercraft.entity.CustomerOrder;
import com.ordercraft.entity.OrderStatus;
import com.ordercraft.service.CustomerOrderService;
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
@RequestMapping("/api/orders")
public class CustomerOrderController {

    private final CustomerOrderService orderService;

    public CustomerOrderController(CustomerOrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<Page<CustomerOrder>> getOrders(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String[] sort) {

        Sort.Direction direction = sort.length > 1 && sort[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort[0]));
        return ResponseEntity.ok(orderService.getAllOrders(query, status, startDate, endDate, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerOrder> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @PostMapping
    public ResponseEntity<CustomerOrder> createOrder(@Valid @RequestBody OrderRequest request) {
        CustomerOrder created = orderService.createOrder(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerOrder> updateOrder(@PathVariable Long id, @Valid @RequestBody OrderRequest request) {
        return ResponseEntity.ok(orderService.updateOrder(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<CustomerOrder> updateOrderStatus(@PathVariable Long id, @Valid @RequestBody OrderStatusUpdateRequest request) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }
}
