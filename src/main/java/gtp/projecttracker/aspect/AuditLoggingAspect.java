package gtp.projecttracker.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gtp.projecttracker.model.mongodb.AuditLog;
import gtp.projecttracker.repository.mongodb.AuditLogRepository;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AuditLoggingAspect {
    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public AuditLoggingAspect(AuditLogRepository auditLogRepository, ObjectMapper objectMapper) {
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    @AfterReturning(pointcut = "execution(* gtp.projecttracker.service.*.save*(..))", returning = "result")
    public void logCreate(JoinPoint joinPoint, Object result) {
        if (result != null) {
            AuditLog log = new AuditLog();
            log.setActionType(AuditLog.ActionType.CREATE);
            log.setEntityType(result.getClass().getSimpleName());
            log.setEntityId(getEntityId(result));
            log.setActorName(getCurrentUsername());
            log.setPayload(convertToJson(result));
            auditLogRepository.save(log);
        }
    }

    @AfterReturning("execution(* gtp.projecttracker.service.*.delete*(..)) && args(id)")
    public void logDelete(JoinPoint joinPoint, Long id) {
        String entityName = joinPoint.getSignature().getName().replace("delete", "");
        AuditLog log = new AuditLog();
        log.setActionType(AuditLog.ActionType.DELETE);
        log.setEntityType(entityName);
        log.setEntityId(id.toString());
        log.setActorName(getCurrentUsername());
        auditLogRepository.save(log);
    }

    private String getEntityId(Object entity) {
        try {
            return entity.getClass().getMethod("getId").invoke(entity).toString();
        } catch (Exception e) {
            return "unknown";
        }
    }

    private String getCurrentUsername() {
        return SecurityContextHolder.getContext().getAuthentication() != null ?
                SecurityContextHolder.getContext().getAuthentication().getName() : "system";
    }

    private ObjectNode convertToJson(Object obj) {
        return objectMapper.valueToTree(obj);
    }
}
