package gtp.projecttracker.aspect;

import gtp.projecttracker.dto.request.user.RegisterRequest;
import gtp.projecttracker.dto.response.ErrorResponse;
import gtp.projecttracker.model.mongodb.AuditLog;
import gtp.projecttracker.repository.mongodb.AuditLogRepository;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;

/**
 * Aspect for comprehensive security event logging that captures authentication, authorization,
 * and user management events across the application.
 * <p>
 * This aspect serves as the centralized auditing mechanism for all security-related operations,
 * creating detailed audit trails for compliance, monitoring, and forensic analysis purposes.
 * It intercepts key security events through AOP pointcuts and persists them as structured
 * audit logs in the database.
 * </p>
 *
 * <p><b>Key Features:</b></p>
 * <ul>
 *   <li>Tracks user registration attempts (both successful and failed)</li>
 *   <li>Records authentication events (logins, failed logins, logouts)</li>
 *   <li>Monitors authorization failures and access denied events</li>
 *   <li>Captures comprehensive context including timestamps, IP addresses, and user agents</li>
 * </ul>
 */
@Aspect
@Component
public class SecurityLoggingAspect {
    private static final Logger logger = LoggerFactory.getLogger(SecurityLoggingAspect.class);

    private final AuditLogRepository auditLogRepository;

    /**
     * Constructs a new SecurityLoggingAspect with the required repository.
     *
     * @param auditLogRepository the repository used to persist audit logs
     */
    @Autowired
    public SecurityLoggingAspect(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Logs successful user registrations when a 200 OK response is returned.
     *
     * @param request  the registration request containing user details
     * @param response the response entity returned by the registration endpoint
     */
    @AfterReturning(
            pointcut = "execution(* gtp.projecttracker.controller.AuthController.register(..)) && args(request)",
            returning = "response",
            argNames = "request,response"
    )
    public void logSuccessfulRegistration(RegisterRequest request, ResponseEntity<?> response) {
        try {
            AuditLog log = createBaseAuditLog(
                    AuditLog.ActionType.REGISTRATION_SUCCESS,
                    request.email(),
                    "User registered successfully"
            );

            auditLogRepository.save(log);
            logger.info("Successful registration for: {}", request.email());
        } catch (Exception e) {
            logger.error("Failed to log registration success", e);
        }
    }

    /**
     * Logs failed registration attempts including cases where email already exists.
     * Excludes cases where the response status is 200 OK.
     *
     * @param request  the registration request containing user details
     * @param response the response entity returned by the registration endpoint
     */
    @AfterReturning(
            pointcut = "execution(* gtp.projecttracker.controller.AuthController.register(..)) && args(request)",
            returning = "response",
            argNames = "request,response"
    )
    public void logFailedRegistration(RegisterRequest request, ResponseEntity<?> response) {
        if (response.getStatusCode() == HttpStatus.OK) {
            return;
        }

        try {
            String payload;
            if (response.getBody() instanceof ErrorResponse errorResponse) {
                payload = "Failure reason: " + errorResponse.message();
            } else {
                payload = "Failure reason: " + response.getStatusCode();
            }

            AuditLog log = createBaseAuditLog(
                    AuditLog.ActionType.REGISTRATION_FAILURE,
                    request.email(),
                    payload
            );

            auditLogRepository.save(log);
            logger.warn("Failed registration attempt for: {}", request.email());
        } catch (Exception e) {
            logger.error("Failed to log registration failure", e);
        }
    }

    /**
     * Logs successful user login events.
     *
     * @param joinPoint the join point representing the login method execution
     * @param result    the result returned by the login method
     */
    @AfterReturning(
            pointcut = "execution(* gtp.projecttracker.security.service.AuthService.login(..))",
            returning = "result"
    )
    public void logSuccessfulLogin(JoinPoint joinPoint, Object result) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AuditLog log = createBaseAuditLog(
                AuditLog.ActionType.LOGIN_SUCCESS,
                auth != null ? auth.getName() : "anonymous",
                null
        );
        auditLogRepository.save(log);
    }

    /**
     * Logs failed login attempts including the exception message.
     *
     * @param joinPoint the join point representing the login method execution
     * @param ex        the exception thrown by the failed login attempt
     */
    @AfterThrowing(
            pointcut = "execution(* gtp.projecttracker.controller.AuthController.login(..))",
            throwing = "ex"
    )
    public void logFailedLogin(JoinPoint joinPoint, Exception ex) {
        Object[] args = joinPoint.getArgs();
        String username = (args != null && args.length > 0) ? args[0].toString() : "unknown";

        AuditLog log = createBaseAuditLog(
                AuditLog.ActionType.LOGIN_FAILURE,
                username,
                "Error: " + ex.getMessage()
        );
        auditLogRepository.save(log);
    }

    /**
     * Logs access denied events when authorization fails.
     *
     * @param joinPoint the join point representing the access denied handler
     */
    @Before("execution(* gtp.projecttracker.exception.AuthExceptionHandler.handleAccessDenied(..))")
    public void logAccessDenied(JoinPoint joinPoint) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AuditLog log = createBaseAuditLog(
                AuditLog.ActionType.ACCESS_DENIED,
                auth != null ? auth.getName() : "anonymous",
                null
        );
        auditLogRepository.save(log);
    }

    /**
     * Logs user logout events.
     *
     * @param joinPoint the join point representing the logout method execution
     */
    @After("execution(* gtp.projecttracker.controller.AuthController.logout(..))")
    public void logLogout(JoinPoint joinPoint) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AuditLog log = createBaseAuditLog(
                AuditLog.ActionType.LOGOUT,
                auth != null ? auth.getName() : "anonymous",
                null
        );
        auditLogRepository.save(log);
    }

    /**
     * Retrieves the current HTTP request from the request context.
     *
     * @return the current HttpServletRequest
     */
    private HttpServletRequest getCurrentRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();
    }

    /**
     * Creates a base audit log with common fields populated.
     * <p>
     * This helper method centralizes the creation of audit log entries
     * with standard fields like timestamp, IP address, user agent, and endpoint.
     * </p>
     *
     * @param actionType the type of action being logged
     * @param actorName  the name of the user performing the action
     * @param payload    additional details about the event (optional, may be null)
     * @return a populated AuditLog object ready for persistence
     */
    private AuditLog createBaseAuditLog(AuditLog.ActionType actionType, String actorName, String payload) {
        HttpServletRequest request = getCurrentRequest();

        AuditLog log = new AuditLog();
        log.setActionType(actionType);
        log.setTimestamp(Instant.now());
        log.setActorName(actorName);
        log.setIpAddress(request.getRemoteAddr());
        log.setUserAgent(request.getHeader("User-Agent"));
        log.setEndpoint(request.getRequestURI());

        if (payload != null) {
            log.setPayload(payload);
        }

        return log;
    }
}