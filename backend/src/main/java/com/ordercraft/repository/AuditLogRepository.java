package com.ordercraft.repository;

import com.ordercraft.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    @Query("SELECT a FROM AuditLog a WHERE " +
           "(:query IS NULL OR :query = '' OR " +
           "LOWER(a.username) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(a.action) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(a.entityName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(a.entityId) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:module IS NULL OR :module = '' OR a.module = :module) AND " +
           "(:startDate IS NULL OR a.timestamp >= :startDate) AND " +
           "(:endDate IS NULL OR a.timestamp <= :endDate)")
    Page<AuditLog> searchAuditLogs(@Param("query") String query,
                                  @Param("module") String module,
                                  @Param("startDate") LocalDateTime startDate,
                                  @Param("endDate") LocalDateTime endDate,
                                  Pageable pageable);
}
