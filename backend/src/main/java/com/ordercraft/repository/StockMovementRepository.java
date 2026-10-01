package com.ordercraft.repository;

import com.ordercraft.entity.StockMovement;
import com.ordercraft.entity.StockMovementType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
    List<StockMovement> findByProductIdOrderByCreatedAtDesc(Long productId);

    @Query("SELECT sm FROM StockMovement sm WHERE " +
           "(:productId IS NULL OR sm.product.id = :productId) AND " +
           "(:movementType IS NULL OR sm.movementType = :movementType) AND " +
           "(:startDate IS NULL OR sm.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR sm.createdAt <= :endDate)")
    Page<StockMovement> searchStockMovements(@Param("productId") Long productId,
                                            @Param("movementType") StockMovementType movementType,
                                            @Param("startDate") LocalDateTime startDate,
                                            @Param("endDate") LocalDateTime endDate,
                                            Pageable pageable);
}
