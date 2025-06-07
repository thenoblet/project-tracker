package gtp.projecttracker.controller;

import gtp.projecttracker.model.mongodb.AuditLog;
import gtp.projecttracker.service.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {
    private final AuditService auditService;

    @Autowired
    public AuditLogController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    public ResponseEntity<Page<AuditLog>> getAllAuditLogs(Pageable pageable) {
        return ResponseEntity.ok(auditService.getAllAuditLogs(pageable));
    }

    @GetMapping("/entity/{entityType}")
    public ResponseEntity<List<AuditLog>> getLogsByEntityType(@PathVariable String entityType) {
        return ResponseEntity.ok(auditService.getAuditLogsByEntityType(entityType));
    }

    @GetMapping("/actor/{actorName}")
    public ResponseEntity<List<AuditLog>> getLogsByActor(@PathVariable String actorName) {
        return ResponseEntity.ok(auditService.getAuditLogsByActor(actorName));
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<AuditLog>> getLogsByDateRange(
            @RequestParam Instant start,
            @RequestParam Instant end) {
        return ResponseEntity.ok(auditService.getAuditLogsByDateRange(start, end));
    }
}
