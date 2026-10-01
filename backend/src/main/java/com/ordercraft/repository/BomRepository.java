package com.ordercraft.repository;

import com.ordercraft.entity.Bom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BomRepository extends JpaRepository<Bom, Long> {
    Optional<Bom> findByBomCode(String bomCode);
    boolean existsByBomCode(String bomCode);
    boolean existsByBomCodeAndIdNot(String bomCode, Long id);

    List<Bom> findByProductId(Long productId);
    Optional<Bom> findFirstByProductIdAndStatusOrderByVersionDesc(Long productId, String status);

    @Query("SELECT b FROM Bom b WHERE " +
           "(:query IS NULL OR :query = '' OR " +
           "LOWER(b.bomCode) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(b.product.productName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(b.product.productCode) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:status IS NULL OR :status = '' OR b.status = :status)")
    Page<Bom> searchBoms(@Param("query") String query, @Param("status") String status, Pageable pageable);
}
