package com.ordercraft.service;

import com.ordercraft.dto.ProductRequest;
import com.ordercraft.entity.Inventory;
import com.ordercraft.entity.Product;
import com.ordercraft.entity.StockMovement;
import com.ordercraft.entity.StockMovementType;
import com.ordercraft.exception.BadRequestException;
import com.ordercraft.exception.ResourceNotFoundException;
import com.ordercraft.repository.InventoryRepository;
import com.ordercraft.repository.ProductRepository;
import com.ordercraft.repository.StockMovementRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final StockMovementRepository stockMovementRepository;
    private final AuditLogService auditLogService;

    public ProductService(ProductRepository productRepository,
                          InventoryRepository inventoryRepository,
                          StockMovementRepository stockMovementRepository,
                          AuditLogService auditLogService) {
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public Page<Product> getAllProducts(String query, String type, String status, Pageable pageable) {
        return productRepository.searchProducts(query, type, status, pageable);
    }

    @Transactional(readOnly = true)
    public List<Product> getProductsByType(String type) {
        return productRepository.findByTypeAndStatus(type, "ACTIVE");
    }

    @Transactional(readOnly = true)
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
    }

    @Transactional
    public Product createProduct(ProductRequest req) {
        if (productRepository.existsByProductCode(req.getProductCode())) {
            throw new BadRequestException("Product code already exists: " + req.getProductCode());
        }

        Product product = new Product();
        mapRequestToEntity(req, product);
        Product saved = productRepository.save(product);

        // Automatically initialize inventory record
        BigDecimal initQty = req.getInitialQuantity() != null ? req.getInitialQuantity() : BigDecimal.ZERO;
        BigDecimal minStock = req.getMinimumStock() != null ? req.getMinimumStock() : new BigDecimal("10.00");
        BigDecimal reorderLvl = req.getReorderLevel() != null ? req.getReorderLevel() : new BigDecimal("20.00");
        BigDecimal unitCost = req.getCostPrice() != null ? req.getCostPrice() : BigDecimal.ZERO;

        Inventory inventory = new Inventory(saved, initQty, minStock, reorderLvl, unitCost);
        inventoryRepository.save(inventory);

        if (initQty.compareTo(BigDecimal.ZERO) > 0) {
            StockMovement sm = new StockMovement(saved, StockMovementType.IN, initQty, "INITIAL_SETUP", saved.getProductCode(), "Initial stock balance", "SYSTEM");
            stockMovementRepository.save(sm);
        }

        auditLogService.record("PRODUCT_CREATED", "PRODUCT", "Product", saved.getId().toString(), null, saved.getProductName() + " (" + saved.getProductCode() + ")");
        return saved;
    }

    @Transactional
    public Product updateProduct(Long id, ProductRequest req) {
        Product product = getProductById(id);

        if (productRepository.existsByProductCodeAndIdNot(req.getProductCode(), id)) {
            throw new BadRequestException("Product code is already used by another product: " + req.getProductCode());
        }

        String oldVal = product.getProductName() + " (" + product.getProductCode() + ")";
        mapRequestToEntity(req, product);
        Product updated = productRepository.save(product);

        auditLogService.record("PRODUCT_UPDATED", "PRODUCT", "Product", id.toString(), oldVal, updated.getProductName());
        return updated;
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = getProductById(id);
        if (!"INACTIVE".equalsIgnoreCase(product.getStatus())) {
            product.setStatus("INACTIVE");
            productRepository.save(product);
            auditLogService.record("PRODUCT_DEACTIVATED", "PRODUCT", "Product", id.toString(), "ACTIVE", "INACTIVE");
        }
    }

    private void mapRequestToEntity(ProductRequest req, Product entity) {
        entity.setProductCode(req.getProductCode().trim());
        entity.setProductName(req.getProductName().trim());
        entity.setDescription(req.getDescription());
        entity.setCategory(req.getCategory().trim());
        if (req.getType() != null && !req.getType().isBlank()) {
            entity.setType(req.getType().trim().toUpperCase());
        }
        entity.setSellingPrice(req.getSellingPrice());
        entity.setCostPrice(req.getCostPrice());
        entity.setUnit(req.getUnit().trim());
        if (req.getStatus() != null && !req.getStatus().isBlank()) {
            entity.setStatus(req.getStatus().trim().toUpperCase());
        }
    }
}
