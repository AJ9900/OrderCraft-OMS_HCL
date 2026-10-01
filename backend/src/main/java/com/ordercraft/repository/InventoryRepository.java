package com.ordercraft.repository;

import com.ordercraft.entity.Inventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    Optional<Inventory> findByProductId(Long productId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Inventory i WHERE i.product.id = :productId")
    Optional<Inventory> findByProductIdForUpdate(@Param("productId") Long productId);
    boolean existsByProductId(Long productId);

    @Query("SELECT i FROM Inventory i WHERE " +
           "(:query IS NULL OR :query = '' OR " +
           "LOWER(i.product.productName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(i.product.productCode) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(i.product.category) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:type IS NULL OR :type = '' OR i.product.type = :type)")
    Page<Inventory> searchInventory(@Param("query") String query, @Param("type") String type, Pageable pageable);

    @Query("SELECT i FROM Inventory i WHERE i.availableQuantity <= i.minimumStock OR i.availableQuantity <= i.reorderLevel")
    List<Inventory> findLowStockInventories();

    @Query("SELECT COUNT(i) FROM Inventory i WHERE i.availableQuantity <= i.minimumStock OR i.availableQuantity <= i.reorderLevel")
    long countLowStock();
}
