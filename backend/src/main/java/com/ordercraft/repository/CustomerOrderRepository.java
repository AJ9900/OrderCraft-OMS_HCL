package com.ordercraft.repository;

import com.ordercraft.entity.CustomerOrder;
import com.ordercraft.entity.OrderStatus;
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
public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {
    Optional<CustomerOrder> findByOrderNumber(String orderNumber);
    boolean existsByOrderNumber(String orderNumber);
    boolean existsByOrderNumberAndIdNot(String orderNumber, Long id);

    long countByStatus(OrderStatus status);

    @Query("SELECT COUNT(o) FROM CustomerOrder o WHERE o.status IN (:statuses)")
    long countByStatuses(@Param("statuses") List<OrderStatus> statuses);

    @Query("SELECT o FROM CustomerOrder o WHERE " +
           "(:query IS NULL OR :query = '' OR " +
           "LOWER(o.orderNumber) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(o.customer.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(o.customer.customerCode) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:status IS NULL OR o.status = :status) AND " +
           "(:startDate IS NULL OR o.orderDate >= :startDate) AND " +
           "(:endDate IS NULL OR o.orderDate <= :endDate)")
    Page<CustomerOrder> searchOrders(@Param("query") String query,
                                    @Param("status") OrderStatus status,
                                    @Param("startDate") LocalDate startDate,
                                    @Param("endDate") LocalDate endDate,
                                    Pageable pageable);

    List<CustomerOrder> findTop5ByOrderByCreatedAtDesc();

    @Query("SELECT COALESCE(SUM(o.grandTotal), 0) FROM CustomerOrder o WHERE o.status != com.ordercraft.entity.OrderStatus.CANCELLED")
    BigDecimal sumTotalRevenue();
}
