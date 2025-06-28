package gtp.projecttracker.aspect;

import gtp.projecttracker.model.mongodb.AuditLog;
import gtp.projecttracker.repository.mongodb.AuditLogRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.time.Instant;

/**
 * Aspect responsible for automatic audit logging of CRUD operations in the application.
 * 
 * This aspect intercepts service method calls for create, update, and delete operations
 * and automatically logs them to the audit repository. It captures information such as
 * - The type of action (CREATE, UPDATE, DELETE)
 * - The entity type being modified
 * - The entity ID
 * - The full entity data (for create and update operations)
 * - Timestamp of the operation
 * 
 * The aspect uses pointcuts to target specific service methods based on naming conventions
 * (methods starting with save*, update*, delete*).
 */
@Aspect
@Component
public class AuditLoggingAspect {
    private static final Logger logger = LoggerFactory.getLogger(AuditLoggingAspect.class);

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    /**
     * Constructs a new AuditLoggingAspect with the required dependencies.
     *
     * @param auditLogRepository Repository for storing audit log entries
     * @param objectMapper JSON mapper for converting entities to JSON format for storage
     */
    @Autowired
    public AuditLoggingAspect(AuditLogRepository auditLogRepository, ObjectMapper objectMapper) {
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Logs CREATE operations after successful execution of any service method starting with "save".
     * 
     * This advice captures the result of the service method, extracts entity information,
     * and creates an audit log entry with the action type CREATE.
     *
     * @param joinPoint The join point representing the intercepted method
     * @param result The object returned by the intercepted method (the created entity)
     */
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

    /**
     * Logs UPDATE operations after successful execution of any service method starting with "update".
     * 
     * This advice captures the result of the service method, extracts entity information,
     * and creates an audit log entry with action type UPDATE.
     *
     * @param joinPoint The join point representing the intercepted method
     * @param result The object returned by the intercepted method (the updated entity)
     */
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

    /**
     * Logs DELETE operations after successful execution of any service method starting with "delete".
     * 
     * Unlike create and update operations, this advice doesn't have access to the deleted entity
     * (as it's typically not returned). Instead, it extracts information from the method name
     * and arguments to determine what was deleted.
     *
     * @param joinPoint The join point representing the intercepted method
     */
    @AfterReturning("execution(* gtp.projecttracker.service.*.delete*(..))")
    public void logDelete(JoinPoint joinPoint) {
        try {
            Object[] args = joinPoint.getArgs();
            if (args.length > 0) {
                AuditLog log = createBaseAuditLog();
                log.setActionType(AuditLog.ActionType.DELETE);

                String methodName = joinPoint.getSignature().getName();
                String entityType = extractEntityTypeFromMethodName(methodName);
                log.setEntityType(entityType);

                String entityId = extractEntityId(args[0]);
                log.setEntityId(entityId);

                auditLogRepository.save(log);
            }
        } catch (Exception e) {
            logger.error("Failed to log DELETE audit event", e);
        }
    }

    /**
     * Creates a base AuditLog object with common properties set.
     * 
     * This helper method initializes a new AuditLog with default values
     * for the actor name and timestamp.
     *
     * @return A new AuditLog instance with common properties initialized
     */
    private AuditLog createBaseAuditLog() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AuditLog log = new AuditLog();
        log.setActorName(auth.getName());
        log.setTimestamp(Instant.now());
        return log;
    }

    /**
     * Extracts the ID from an entity object using reflection.
     * 
     * This method attempts to find and invoke a "getId" method on the entity.
     * If no such method exists, it looks for any method containing "id" in its name.
     * 
     * @param entity The entity object from which to extract the ID
     * @return The string representation of the entity's ID, or "unknown" if it cannot be determined
     */
    private String getEntityId(Object entity) {
        if (entity == null) {
            return "unknown";
        }

        try {
            Method[] methods = entity.getClass().getDeclaredMethods();
            for (Method method : methods) {
                if (method.getName().equals("getId") && method.getParameterCount() == 0) {
                    Object id = method.invoke(entity);
                    return id != null ? id.toString() : "null";
                }
            }

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

    /**
     * Extracts an entity ID from a method argument.
     * 
     * This method handles different types of arguments that might represent an entity ID:
     * - If the argument is a simple type (String, Number), it uses its string representation
     * - Otherwise, it attempts to extract the ID using the getEntityId method
     *
     * @param arg The method argument from which to extract the entity ID
     * @return The string representation of the entity ID, or "unknown" if it cannot be determined
     */
    private String extractEntityId(Object arg) {
        if (arg == null) {
            return "unknown";
        }

        if (arg instanceof String || arg instanceof Number) {
            return arg.toString();
        }

        return getEntityId(arg);
    }

    /**
     * Extracts the entity type from a method name.
     * 
     * This method parses the method name to determine what type of entity is being operated on.
     * It handles common prefixes like "delete" and "remove", and applies formatting to make
     * the entity type more readable.
     *
     * @param methodName The name of the intercepted method
     * @return The extracted entity type, or "Entity" if it cannot be determined
     */
    private String extractEntityTypeFromMethodName(String methodName) {
        String entityName = methodName;
        if (entityName.startsWith("delete")) {
            entityName = entityName.substring(6);
        } else if (entityName.startsWith("remove")) {
            entityName = entityName.substring(6);
        }

        if (entityName.startsWith("By")) {
            return "Entity"; // Fallback
        }

        if (!entityName.isEmpty()) {
            entityName = Character.toUpperCase(entityName.charAt(0)) + entityName.substring(1);
        }

        return entityName.isEmpty() ? "Entity" : entityName;
    }

    /**
     * Converts an object to its JSON string representation.
     * 
     * This method attempts to serialize the given object to JSON using the ObjectMapper.
     * If serialization fails, it creates a simple JSON object with error information.
     *
     * @param obj The object to convert to JSON
     * @return A JSON string representation of the object, or an error JSON if conversion fails
     */
    private String convertToJson(Object obj) {
        try {
            return String.valueOf(objectMapper.valueToTree(obj));
        } catch (Exception e) {
            logger.warn("Could not convert object to JSON", e);
            ObjectNode errorNode = objectMapper.createObjectNode();
            errorNode.put("error", "Could not serialize object");
            errorNode.put("class", obj.getClass().getSimpleName());
            return String.valueOf(errorNode);
        }
    }
}
