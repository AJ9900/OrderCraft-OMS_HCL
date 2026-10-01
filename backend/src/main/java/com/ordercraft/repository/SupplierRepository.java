package com.ordercraft.repository;

import com.ordercraft.entity.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    Optional<Supplier> findBySupplierCode(String supplierCode);
    boolean existsBySupplierCode(String supplierCode);
    boolean existsBySupplierCodeAndIdNot(String supplierCode, Long id);

    @Query("SELECT s FROM Supplier s WHERE " +
           "(:query IS NULL OR :query = '' OR " +
           "LOWER(s.supplierName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(s.supplierCode) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(s.email) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(s.phone) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:status IS NULL OR :status = '' OR s.status = :status)")
    Page<Supplier> searchSuppliers(@Param("query") String query, @Param("status") String status, Pageable pageable);
}
