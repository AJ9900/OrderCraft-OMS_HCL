package com.ordercraft.repository;

import com.ordercraft.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByProductCode(String productCode);
    boolean existsByProductCode(String productCode);
    boolean existsByProductCodeAndIdNot(String productCode, Long id);

    List<Product> findByTypeAndStatus(String type, String status);

    @Query("SELECT p FROM Product p WHERE " +
           "(:query IS NULL OR :query = '' OR " +
           "LOWER(p.productName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.productCode) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.category) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:type IS NULL OR :type = '' OR p.type = :type) AND " +
           "(:status IS NULL OR :status = '' OR p.status = :status)")
    Page<Product> searchProducts(@Param("query") String query,
                                @Param("type") String type,
                                @Param("status") String status,
                                Pageable pageable);
}
