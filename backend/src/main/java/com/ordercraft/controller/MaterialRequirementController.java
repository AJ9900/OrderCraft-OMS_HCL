package com.ordercraft.controller;

import com.ordercraft.dto.MaterialRequirementDto;
import com.ordercraft.dto.OrderRequirementResponse;
import com.ordercraft.service.MaterialRequirementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/material-requirements")
public class MaterialRequirementController {

    private final MaterialRequirementService requirementService;

    public MaterialRequirementController(MaterialRequirementService requirementService) {
        this.requirementService = requirementService;
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<OrderRequirementResponse> checkOrderRequirement(
            @PathVariable Long orderId,
            @RequestParam(defaultValue = "true") boolean updateOrderStatus) {
        return ResponseEntity.ok(requirementService.checkOrderMaterialRequirement(orderId, updateOrderStatus));
    }

    @GetMapping("/shortages")
    public ResponseEntity<List<MaterialRequirementDto>> getAllShortages() {
        return ResponseEntity.ok(requirementService.getAllSystemShortages());
    }
}
