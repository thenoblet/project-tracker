package gtp.projecttracker.controller;

import gtp.projecttracker.model.mongodb.AuditLog;
import gtp.projecttracker.service.AuditService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing audit logs.
 * Provides endpoints for retrieving audit log data with various filtering options.
 * All endpoints return data in JSON format and are accessible under the /api/v1/logs path.
 */
@RestController
@RequestMapping("/api/v1/logs")
public class AuditLogController {
    private final AuditService auditService;

    /**
     * Constructs an AuditLogController with the required service dependency.
     *
     * @param auditService The service for retrieving and managing audit logs
     */
    @Autowired
    public AuditLogController(AuditService auditService) {
        this.auditService = auditService;

    }

    /**
     * Retrieves all audit logs with pagination support.
     *
     * @param pageable Pagination information including page number, size, and sorting
     * @return A paginated list of audit logs wrapped in a ResponseEntity
     */
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_MANAGER')")
    public ResponseEntity<Page<AuditLog>> getAllAuditLogs(Pageable pageable) {
        return ResponseEntity.ok(auditService.getAllAuditLogs(pageable));
    }

    /**
     * Retrieves audit logs filtered by entity type.
     *
     * @param entityType The type of entity to filter logs by (e.g., "Task", "Project")
     * @return A list of audit logs for the specified entity type
     */
    @GetMapping("/entity/{entityType}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_MANAGER')")
    public ResponseEntity<List<AuditLog>> getLogsByEntityType(@PathVariable String entityType) {
        return ResponseEntity.ok(auditService.getAuditLogsByEntityType(entityType));
    }

    /**
     * Retrieves audit logs filtered by the actor who performed the actions.
     *
     * @param actorName The name of the actor (user) who performed the actions
     * @return A list of audit logs for the specified actor
     */
    @GetMapping("/actor/{actorName}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_MANAGER')")
    public ResponseEntity<List<AuditLog>> getLogsByActor(@PathVariable String actorName) {
        return ResponseEntity.ok(auditService.getAuditLogsByActor(actorName));
    }

    /**
     * Retrieves audit logs within a specified date range.
     *
     * @param startDate The start date for the range in string format
     * @param endDate The end date for the range in string format
     * @return A list of audit logs that occurred within the specified date range
     */
    @GetMapping("/date-range")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_MANAGER')")
    public ResponseEntity<List<AuditLog>> getLogsByDateRange(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        return ResponseEntity.ok(
                auditService.getAuditLogsByDateRange(startDate, endDate)
        );
    }
}
