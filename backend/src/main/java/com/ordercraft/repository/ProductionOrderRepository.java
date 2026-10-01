package com.ordercraft.repository;

import com.ordercraft.entity.ProductionOrder;
import com.ordercraft.entity.ProductionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductionOrderRepository extends JpaRepository<ProductionOrder, Long> {
    Optional<ProductionOrder> findByProductionOrderNumber(String productionOrderNumber);
    boolean existsByProductionOrderNumber(String productionOrderNumber);
    boolean existsByProductionOrderNumberAndIdNot(String productionOrderNumber, Long id);

    long countByStatus(ProductionStatus status);

    @Query("SELECT COUNT(p) FROM ProductionOrder p WHERE p.status IN (:statuses)")
    long countByStatuses(@Param("statuses") List<ProductionStatus> statuses);

    List<ProductionOrder> findByCustomerOrderId(Long customerOrderId);

    @Query("SELECT p FROM ProductionOrder p WHERE " +
           "(:query IS NULL OR :query = '' OR " +
           "LOWER(p.productionOrderNumber) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.product.productName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.product.productCode) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:status IS NULL OR p.status = :status)")
    Page<ProductionOrder> searchProductionOrders(@Param("query") String query,
                                                @Param("status") ProductionStatus status,
                                                Pageable pageable);
}
