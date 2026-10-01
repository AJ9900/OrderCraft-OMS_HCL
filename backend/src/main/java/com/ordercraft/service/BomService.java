package com.ordercraft.service;

import com.ordercraft.dto.BomItemRequest;
import com.ordercraft.dto.BomRequest;
import com.ordercraft.entity.Bom;
import com.ordercraft.entity.BomItem;
import com.ordercraft.entity.Product;
import com.ordercraft.exception.BadRequestException;
import com.ordercraft.exception.ResourceNotFoundException;
import com.ordercraft.repository.BomItemRepository;
import com.ordercraft.repository.BomRepository;
import com.ordercraft.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BomService {

    private final BomRepository bomRepository;
    private final BomItemRepository bomItemRepository;
    private final ProductRepository productRepository;
    private final AuditLogService auditLogService;

    public BomService(BomRepository bomRepository,
                      BomItemRepository bomItemRepository,
                      ProductRepository productRepository,
                      AuditLogService auditLogService) {
        this.bomRepository = bomRepository;
        this.bomItemRepository = bomItemRepository;
        this.productRepository = productRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public Page<Bom> getAllBoms(String query, String status, Pageable pageable) {
        return bomRepository.searchBoms(query, status, pageable);
    }

    @Transactional(readOnly = true)
    public Bom getBomById(Long id) {
        return bomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BOM not found with ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<Bom> getBomsByProductId(Long productId) {
        return bomRepository.findByProductId(productId);
    }

    @Transactional(readOnly = true)
    public Bom getActiveBomForProduct(Long productId) {
        return bomRepository.findFirstByProductIdAndStatusOrderByVersionDesc(productId, "ACTIVE")
                .orElseThrow(() -> new ResourceNotFoundException("No ACTIVE BOM found for product ID: " + productId));
    }

    @Transactional
    public Bom createBom(BomRequest req) {
        if (bomRepository.existsByBomCode(req.getBomCode())) {
            throw new BadRequestException("BOM code already exists: " + req.getBomCode());
        }

        Product product = productRepository.findById(req.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + req.getProductId()));

        Bom bom = new Bom();
        bom.setBomCode(req.getBomCode().trim());
        bom.setProduct(product);
        bom.setVersion(req.getVersion().trim());
        bom.setStatus(req.getStatus() != null ? req.getStatus().trim().toUpperCase() : "ACTIVE");
        bom.setEffectiveDate(req.getEffectiveDate());
        bom.setNotes(req.getNotes());

        for (BomItemRequest itemReq : req.getItems()) {
            Product material = productRepository.findById(itemReq.getMaterialId())
                    .orElseThrow(() -> new ResourceNotFoundException("Material not found with ID: " + itemReq.getMaterialId()));
            BomItem bi = new BomItem();
            bi.setBom(bom);
            bi.setMaterial(material);
            bi.setQuantity(itemReq.getQuantity());
            bi.setUnit(itemReq.getUnit());
            bi.setWastagePercentage(itemReq.getWastagePercentage() != null ? itemReq.getWastagePercentage() : java.math.BigDecimal.ZERO);
            bom.addItem(bi);
        }

        Bom saved = bomRepository.save(bom);
        auditLogService.record("BOM_CREATED", "BOM", "BOM", saved.getId().toString(), null, saved.getBomCode() + " for " + product.getProductName());
        return saved;
    }

    @Transactional
    public Bom updateBom(Long id, BomRequest req) {
        Bom bom = getBomById(id);

        if (bomRepository.existsByBomCodeAndIdNot(req.getBomCode(), id)) {
            throw new BadRequestException("BOM code is already used by another BOM: " + req.getBomCode());
        }

        Product product = productRepository.findById(req.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + req.getProductId()));

        String oldVal = bom.getBomCode() + " v" + bom.getVersion();

        bom.setBomCode(req.getBomCode().trim());
        bom.setProduct(product);
        bom.setVersion(req.getVersion().trim());
        if (req.getStatus() != null) bom.setStatus(req.getStatus().trim().toUpperCase());
        bom.setEffectiveDate(req.getEffectiveDate());
        bom.setNotes(req.getNotes());

        // Update items
        bom.getItems().clear();
        for (BomItemRequest itemReq : req.getItems()) {
            Product material = productRepository.findById(itemReq.getMaterialId())
                    .orElseThrow(() -> new ResourceNotFoundException("Material not found with ID: " + itemReq.getMaterialId()));
            BomItem bi = new BomItem();
            bi.setBom(bom);
            bi.setMaterial(material);
            bi.setQuantity(itemReq.getQuantity());
            bi.setUnit(itemReq.getUnit());
            bi.setWastagePercentage(itemReq.getWastagePercentage() != null ? itemReq.getWastagePercentage() : java.math.BigDecimal.ZERO);
            bom.addItem(bi);
        }

        Bom updated = bomRepository.save(bom);
        auditLogService.record("BOM_UPDATED", "BOM", "BOM", id.toString(), oldVal, updated.getBomCode() + " v" + updated.getVersion());
        return updated;
    }

    @Transactional
    public void deleteBom(Long id) {
        Bom bom = getBomById(id);
        bomRepository.delete(bom);
        auditLogService.record("BOM_DELETED", "BOM", "BOM", id.toString(), bom.getBomCode(), null);
    }
}
