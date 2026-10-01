package com.ordercraft.repository;

import com.ordercraft.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByCustomerCode(String customerCode);
    boolean existsByCustomerCode(String customerCode);
    boolean existsByCustomerCodeAndIdNot(String customerCode, Long id);

    @Query("SELECT c FROM Customer c WHERE " +
           "(:query IS NULL OR :query = '' OR " +
           "LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(c.customerCode) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(c.phone) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:status IS NULL OR :status = '' OR c.status = :status)")
    Page<Customer> searchCustomers(@Param("query") String query, @Param("status") String status, Pageable pageable);
}
