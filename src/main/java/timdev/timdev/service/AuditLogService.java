package timdev.timdev.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import timdev.timdev.entity.AuditLog;
import timdev.timdev.repository.AuditLogRepository;

import java.time.LocalDateTime;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void log(String action, String entityName, Long entityId,
                    String oldValues, String newValues, String performedBy,
                    String ipAddress, String userAgent, String description) {

        AuditLog audit = new AuditLog();
        audit.setAction(action);
        audit.setEntityName(entityName);
        audit.setEntityId(entityId);
        audit.setOldValues(oldValues);
        audit.setNewValues(newValues);
        audit.setPerformedBy(performedBy);
        audit.setTimestamp(LocalDateTime.now());
        audit.setIpAddress(ipAddress);
        audit.setUserAgent(userAgent);
        audit.setDescription(description);

        auditLogRepository.save(audit);
    }
}
