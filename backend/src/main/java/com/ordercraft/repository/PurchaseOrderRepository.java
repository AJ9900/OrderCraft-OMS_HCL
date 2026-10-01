package com.ordercraft.repository;

import com.ordercraft.entity.PoStatus;
import com.ordercraft.entity.PurchaseOrder;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
       @Lock(LockModeType.PESSIMISTIC_WRITE)
       @Query("SELECT p FROM PurchaseOrder p WHERE p.id = :id")
       Optional<PurchaseOrder> findByIdForUpdate(@Param("id") Long id);
    Optional<PurchaseOrder> findByPoNumber(String poNumber);
    boolean existsByPoNumber(String poNumber);
    boolean existsByPoNumberAndIdNot(String poNumber, Long id);

    long countByStatus(PoStatus status);

    @Query("SELECT COUNT(p) FROM PurchaseOrder p WHERE p.status IN (:statuses)")
    long countByStatuses(@Param("statuses") List<PoStatus> statuses);

    @Query("SELECT p FROM PurchaseOrder p WHERE " +
           "(:query IS NULL OR :query = '' OR " +
           "LOWER(p.poNumber) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.supplier.supplierName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.supplier.supplierCode) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:status IS NULL OR p.status = :status) AND " +
           "(:startDate IS NULL OR p.poDate >= :startDate) AND " +
           "(:endDate IS NULL OR p.poDate <= :endDate)")
    Page<PurchaseOrder> searchPurchaseOrders(@Param("query") String query,
                                            @Param("status") PoStatus status,
                                            @Param("startDate") LocalDate startDate,
                                            @Param("endDate") LocalDate endDate,
                                            Pageable pageable);
}
