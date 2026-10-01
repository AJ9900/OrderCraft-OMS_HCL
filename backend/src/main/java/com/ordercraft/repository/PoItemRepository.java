package com.ordercraft.repository;

import com.ordercraft.entity.PoItem;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PoItemRepository extends JpaRepository<PoItem, Long> {
    List<PoItem> findByPurchaseOrderId(Long poId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT item FROM PoItem item WHERE item.id = :id")
    Optional<PoItem> findByIdForUpdate(@Param("id") Long id);
}
