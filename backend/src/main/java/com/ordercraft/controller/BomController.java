package com.ordercraft.controller;

import com.ordercraft.dto.BomRequest;
import com.ordercraft.entity.Bom;
import com.ordercraft.service.BomService;
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
@RequestMapping("/api/boms")
public class BomController {

    private final BomService bomService;

    public BomController(BomService bomService) {
        this.bomService = bomService;
    }

    @GetMapping
    public ResponseEntity<Page<Bom>> getBoms(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String[] sort) {

        Sort.Direction direction = sort.length > 1 && sort[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort[0]));
        return ResponseEntity.ok(bomService.getAllBoms(query, status, pageable));
    }

    @GetMapping("/by-product/{productId}")
    public ResponseEntity<List<Bom>> getBomsByProductId(@PathVariable Long productId) {
        return ResponseEntity.ok(bomService.getBomsByProductId(productId));
    }

    @GetMapping("/active/{productId}")
    public ResponseEntity<Bom> getActiveBom(@PathVariable Long productId) {
        return ResponseEntity.ok(bomService.getActiveBomForProduct(productId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Bom> getBomById(@PathVariable Long id) {
        return ResponseEntity.ok(bomService.getBomById(id));
    }

    @PostMapping
    public ResponseEntity<Bom> createBom(@Valid @RequestBody BomRequest request) {
        Bom created = bomService.createBom(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Bom> updateBom(@PathVariable Long id, @Valid @RequestBody BomRequest request) {
        return ResponseEntity.ok(bomService.updateBom(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBom(@PathVariable Long id) {
        bomService.deleteBom(id);
        return ResponseEntity.noContent().build();
    }
}
