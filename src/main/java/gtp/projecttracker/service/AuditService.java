package gtp.projecttracker.service;

import gtp.projecttracker.model.mongodb.AuditLog;
import gtp.projecttracker.repository.mongodb.AuditLogRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
public class AuditService {
    private final AuditLogRepository auditLogRepository;

    @Autowired
    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public Page<AuditLog> getAllAuditLogs(Pageable pageable) {
        return auditLogRepository.findAll(pageable);
    }

    public List<AuditLog> getAuditLogsByEntityType(String entityType) {
        return auditLogRepository.findByEntityType(entityType);
    }

    public List<AuditLog> getAuditLogsByActor(String actorName) {
        return auditLogRepository.findByActorName(actorName);
    }

    public AuditLog saveAuditLog(AuditLog log) {
        return auditLogRepository.save(log);
    }

    public List<AuditLog> getAuditLogsByDateRange(String startDateStr, String endDateStr) {
        Instant start = parseDateToInstant(startDateStr, false);
        Instant end = parseDateToInstant(endDateStr, true);

        return auditLogRepository.findByTimestampBetween(start, end);
    }

    private Instant parseDateToInstant(String dateStr, boolean endOfDay) {
        try {
            LocalDate date = LocalDate.parse(dateStr);
            return endOfDay
                    ? date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant()
                    : date.atStartOfDay(ZoneOffset.UTC).toInstant();
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format. Use YYYY-MM-DD");
        }
    }

    public void logAction(AuditLog.ActionType actionType, String entityType, String entityId, String actorName, String payload) {
        AuditLog log = new AuditLog();
        log.setActionType(actionType);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setActorName(actorName);
        log.setPayload(payload);
        auditLogRepository.save(log);
    }
}
