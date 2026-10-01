package com.ordercraft.controller;

import com.ordercraft.dto.GoodsReceiptRequest;
import com.ordercraft.entity.GoodsReceipt;
import com.ordercraft.service.GoodsReceiptService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/goods-receipts")
public class GoodsReceiptController {

    private final GoodsReceiptService receiptService;

    public GoodsReceiptController(GoodsReceiptService receiptService) {
        this.receiptService = receiptService;
    }

    @GetMapping
    public ResponseEntity<Page<GoodsReceipt>> getReceipts(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(receiptService.getAllReceipts(query, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GoodsReceipt> getReceiptById(@PathVariable Long id) {
        return ResponseEntity.ok(receiptService.getReceiptById(id));
    }

    @PostMapping
    public ResponseEntity<GoodsReceipt> createGoodsReceipt(@Valid @RequestBody GoodsReceiptRequest request) {
        GoodsReceipt created = receiptService.processGoodsReceipt(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }
}
