package com._antra.the_bridge.scheduler;

import com._antra.the_bridge.entity.AuditLog;
import com._antra.the_bridge.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class AuditScheduler {

    private static final int BATCH_SIZE = 5000;
    private static final int RETENTION_DAYS = 90;

    private final AuditLogRepository auditLogRepository;
    private final int batchSize;

    @Autowired
    public AuditScheduler(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
        this.batchSize = BATCH_SIZE;
    }

    // Run every day at 2:00 AM
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanupOldAuditLogs() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(RETENTION_DAYS);
        long deletedCount = 0;

        while (true) {
            List<AuditLog> logs = auditLogRepository.findOldLogs(cutoffDate, batchSize);
            if (logs.isEmpty()) {
                break;
            }

            auditLogRepository.deleteAll(logs);
            deletedCount += logs.size();
            
            // Log progress for debugging
            if (deletedCount % batchSize == 0 || deletedCount == 0) {
                System.out.println("Audit cleanup: deleted " + deletedCount + " logs so far");
            }
        }

        if (deletedCount > 0) {
            System.out.println("Audit cleanup: finished, deleted " + deletedCount + " old audit logs");
        }
    }
}
