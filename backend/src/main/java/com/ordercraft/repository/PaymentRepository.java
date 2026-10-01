package com.ordercraft.repository;

import com.ordercraft.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByPaymentNumber(String paymentNumber);
    boolean existsByPaymentNumber(String paymentNumber);

    List<Payment> findByInvoiceId(Long invoiceId);

    @Query("SELECT p FROM Payment p WHERE " +
           "(:query IS NULL OR :query = '' OR " +
           "LOWER(p.paymentNumber) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.invoice.invoiceNumber) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.invoice.customer.name) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:startDate IS NULL OR p.paymentDate >= :startDate) AND " +
           "(:endDate IS NULL OR p.paymentDate <= :endDate)")
    Page<Payment> searchPayments(@Param("query") String query,
                                @Param("startDate") LocalDate startDate,
                                @Param("endDate") LocalDate endDate,
                                Pageable pageable);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = 'COMPLETED'")
    BigDecimal sumTotalPaid();
}
