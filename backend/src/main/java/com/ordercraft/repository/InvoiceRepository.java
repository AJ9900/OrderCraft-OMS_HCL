package com.ordercraft.repository;

import com.ordercraft.entity.Invoice;
import com.ordercraft.entity.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    boolean existsByOrderId(Long orderId);
    boolean existsByInvoiceNumber(String invoiceNumber);
    boolean existsByInvoiceNumberAndIdNot(String invoiceNumber, Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Invoice i WHERE i.id = :id")
    Optional<Invoice> findByIdForUpdate(@Param("id") Long id);

    long countByStatus(InvoiceStatus status);

    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.status IN (:statuses)")
    long countByStatuses(@Param("statuses") List<InvoiceStatus> statuses);

    @Query("SELECT i FROM Invoice i WHERE " +
           "(:query IS NULL OR :query = '' OR " +
           "LOWER(i.invoiceNumber) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(i.customer.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(i.order.orderNumber) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:status IS NULL OR i.status = :status) AND " +
           "(:startDate IS NULL OR i.invoiceDate >= :startDate) AND " +
           "(:endDate IS NULL OR i.invoiceDate <= :endDate)")
    Page<Invoice> searchInvoices(@Param("query") String query,
                                @Param("status") InvoiceStatus status,
                                @Param("startDate") LocalDate startDate,
                                @Param("endDate") LocalDate endDate,
                                Pageable pageable);

    @Query("SELECT COALESCE(SUM(i.grandTotal - i.paidAmount), 0) FROM Invoice i WHERE i.status NOT IN (com.ordercraft.entity.InvoiceStatus.PAID, com.ordercraft.entity.InvoiceStatus.CANCELLED)")
    BigDecimal sumOutstandingAmount();

    @Query("SELECT i FROM Invoice i WHERE i.status NOT IN (com.ordercraft.entity.InvoiceStatus.PAID, com.ordercraft.entity.InvoiceStatus.CANCELLED)")
    List<Invoice> findOutstandingInvoices();
}
