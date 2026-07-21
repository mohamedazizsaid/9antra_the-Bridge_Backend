package com._antra.the_bridge.repository;

import com._antra.the_bridge.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    @Query("""
        SELECT a FROM AuditLog a
        WHERE (:method IS NULL OR a.action LIKE %:method%)
        AND (:ip IS NULL OR a.ipAddress LIKE %:ip%)
        AND (:from IS NULL OR a.createdAt >= :from)
        AND (:to IS NULL OR a.createdAt <= :to)
        ORDER BY a.createdAt DESC
    """)
    Page<AuditLog> findFiltered(
        @Param("method") String method,
        @Param("ip") String ip,
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to,
        Pageable pageable
    );
}
