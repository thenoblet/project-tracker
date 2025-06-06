package gtp.projecttracker.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gtp.projecttracker.model.mongodb.AuditLog;
import gtp.projecttracker.repository.mongodb.AuditLogRepository;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.Instant;

@Aspect
@Component
public class AuditLoggingAspect {
    private static final Logger logger = LoggerFactory.getLogger(AuditLoggingAspect.class);

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
            try {
                AuditLog log = createBaseAuditLog();
                log.setActionType(AuditLog.ActionType.CREATE);
                log.setEntityType(result.getClass().getSimpleName());
                log.setEntityId(getEntityId(result));
                log.setPayload(convertToJson(result));
                auditLogRepository.save(log);
            } catch (Exception e) {
                logger.error("Failed to log CREATE audit event", e);
            }
        }
    }

    @AfterReturning(pointcut = "execution(* gtp.projecttracker.service.*.update*(..))", returning = "result")
    public void logUpdate(JoinPoint joinPoint, Object result) {
        if (result != null) {
            try {
                AuditLog log = createBaseAuditLog();
                log.setActionType(AuditLog.ActionType.UPDATE);
                log.setEntityType(result.getClass().getSimpleName());
                log.setEntityId(getEntityId(result));
                log.setPayload(convertToJson(result));
                auditLogRepository.save(log);
            } catch (Exception e) {
                logger.error("Failed to log UPDATE audit event", e);
            }
        }
    }

    @AfterReturning("execution(* gtp.projecttracker.service.*.delete*(..))")
    public void logDelete(JoinPoint joinPoint) {
        try {
            Object[] args = joinPoint.getArgs();
            if (args.length > 0) {
                AuditLog log = createBaseAuditLog();
                log.setActionType(AuditLog.ActionType.DELETE);

                // Extract entity type from method name or class
                String methodName = joinPoint.getSignature().getName();
                String entityType = extractEntityTypeFromMethodName(methodName);
                log.setEntityType(entityType);

                // Try to get ID from first argument
                String entityId = extractEntityId(args[0]);
                log.setEntityId(entityId);

                auditLogRepository.save(log);
            }
        } catch (Exception e) {
            logger.error("Failed to log DELETE audit event", e);
        }
    }

    private AuditLog createBaseAuditLog() {
        AuditLog log = new AuditLog();
        log.setActorName("system"); // Since no auth is needed
        log.setTimestamp(Instant.now()); // Using Instant to match your service
        return log;
    }



    private String getEntityId(Object entity) {
        if (entity == null) {
            return "unknown";
        }

        try {
            // Try common ID getter methods
            Method[] methods = entity.getClass().getDeclaredMethods();
            for (Method method : methods) {
                if (method.getName().equals("getId") && method.getParameterCount() == 0) {
                    Object id = method.invoke(entity);
                    return id != null ? id.toString() : "null";
                }
            }

            // Fallback: try to find any method that returns an ID-like type
            for (Method method : methods) {
                if (method.getName().toLowerCase().contains("id") &&
                        method.getParameterCount() == 0 &&
                        !method.getReturnType().equals(void.class)) {
                    Object id = method.invoke(entity);
                    return id != null ? id.toString() : "null";
                }
            }
        } catch (Exception e) {
            logger.warn("Could not extract entity ID from {}", entity.getClass().getSimpleName(), e);
        }

        return "unknown";
    }

    private String extractEntityId(Object arg) {
        if (arg == null) {
            return "unknown";
        }

        // If it's already a string or number, use it directly
        if (arg instanceof String || arg instanceof Number) {
            return arg.toString();
        }

        // Otherwise try to extract ID from the object
        return getEntityId(arg);
    }

    private String extractEntityTypeFromMethodName(String methodName) {
        // Remove common prefixes
        String entityName = methodName;
        if (entityName.startsWith("delete")) {
            entityName = entityName.substring(6); // Remove "delete"
        } else if (entityName.startsWith("remove")) {
            entityName = entityName.substring(6); // Remove "remove"
        }

        // Handle cases like "deleteById", "deleteByName", etc.
        if (entityName.startsWith("By")) {
            // This might be a generic delete method, try to infer from class context
            return "Entity"; // Fallback
        }

        // Capitalize first letter if needed
        if (!entityName.isEmpty()) {
            entityName = Character.toUpperCase(entityName.charAt(0)) + entityName.substring(1);
        }

        return entityName.isEmpty() ? "Entity" : entityName;
    }

    private ObjectNode convertToJson(Object obj) {
        try {
            return objectMapper.valueToTree(obj);
        } catch (Exception e) {
            logger.warn("Could not convert object to JSON", e);
            ObjectNode errorNode = objectMapper.createObjectNode();
            errorNode.put("error", "Could not serialize object");
            errorNode.put("class", obj.getClass().getSimpleName());
            return errorNode;
        }
    }
}