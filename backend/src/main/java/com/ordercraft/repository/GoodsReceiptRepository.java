package com.ordercraft.repository;

import com.ordercraft.entity.GoodsReceipt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface GoodsReceiptRepository extends JpaRepository<GoodsReceipt, Long> {
    Optional<GoodsReceipt> findByReceiptNumber(String receiptNumber);
    boolean existsByReceiptNumber(String receiptNumber);
    List<GoodsReceipt> findByPurchaseOrderId(Long poId);

    @Query("SELECT gr FROM GoodsReceipt gr WHERE " +
           "(:query IS NULL OR :query = '' OR " +
           "LOWER(gr.receiptNumber) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(gr.purchaseOrder.poNumber) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(gr.purchaseOrder.supplier.supplierName) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<GoodsReceipt> searchGoodsReceipts(@Param("query") String query, Pageable pageable);
}
