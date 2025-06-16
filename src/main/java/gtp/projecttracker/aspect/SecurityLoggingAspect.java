package gtp.projecttracker.aspect;

import gtp.projecttracker.model.mongodb.AuditLog;
import gtp.projecttracker.repository.mongodb.AuditLogRepository;
import gtp.projecttracker.security.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;

@Aspect
@Component
public class SecurityLoggingAspect {
    private static final Logger logger = LoggerFactory.getLogger(SecurityLoggingAspect.class);

    private final AuditLogRepository auditLogRepository;
    private final AuthService authService;

    @Autowired
    public SecurityLoggingAspect(AuditLogRepository auditLogRepository,
                                 AuthService authService) {
        this.auditLogRepository = auditLogRepository;
        this.authService = authService;
    }

    /**
     * Logs successful logins
     */
    @AfterReturning(
            pointcut = "execution(* gtp.projecttracker.controller.AuthController.login(..))",
            returning = "result"
    )
    public void logSuccessfulLogin(JoinPoint joinPoint, Object result) {
        HttpServletRequest request = getCurrentRequest();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        AuditLog log = new AuditLog();
        log.setActionType(AuditLog.ActionType.LOGIN_SUCCESS);
        log.setTimestamp(Instant.now());
        log.setUsername(auth.getName());
        log.setIpAddress(request.getRemoteAddr());
        log.setUserAgent(request.getHeader("User-Agent"));
        log.setEndpoint(request.getRequestURI());

        auditLogRepository.save(log);
    }

    /**
     * Logs failed login attempts
     */
    @AfterThrowing(
            pointcut = "execution(* gtp.projecttracker.controller.AuthController.login(..))",
            throwing = "ex"
    )
    public void logFailedLogin(JoinPoint joinPoint, Exception ex) {
        HttpServletRequest request = getCurrentRequest();
        Object[] args = joinPoint.getArgs();
        String username = (args != null && args.length > 0) ? args[0].toString() : "unknown";

        AuditLog log = new AuditLog();
        log.setActionType(AuditLog.ActionType.LOGIN_FAILURE);
        log.setTimestamp(Instant.now());
        log.setUsername(username);
        log.setIpAddress(request.getRemoteAddr());
        log.setUserAgent(request.getHeader("User-Agent"));
        log.setEndpoint(request.getRequestURI());
        log.setPayload("Error: " + ex.getMessage());

        auditLogRepository.save(log);
    }

    /**
     * Logs access denied events
     */
    @Before("execution(* gtp.projecttracker.exception.AuthExceptionHandler.handleAccessDenied(..))")
    public void logAccessDenied(JoinPoint joinPoint) {
        HttpServletRequest request = getCurrentRequest();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        AuditLog log = new AuditLog();
        log.setActionType(AuditLog.ActionType.ACCESS_DENIED);
        log.setTimestamp(Instant.now());
        log.setUsername(auth != null ? auth.getName() : "anonymous");
        log.setIpAddress(request.getRemoteAddr());
        log.setUserAgent(request.getHeader("User-Agent"));
        log.setEndpoint(request.getRequestURI());

        auditLogRepository.save(log);
    }

    /**
     * Logs logout events
     */
    @After("execution(* gtp.projecttracker.controller.AuthController.logout(..))")
    public void logLogout(JoinPoint joinPoint) {
        HttpServletRequest request = getCurrentRequest();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        AuditLog log = new AuditLog();
        log.setActionType(AuditLog.ActionType.LOGOUT);
        log.setTimestamp(Instant.now());
        log.setUsername(auth != null ? auth.getName() : "unknown");
        log.setIpAddress(request.getRemoteAddr());
        log.setUserAgent(request.getHeader("User-Agent"));
        log.setEndpoint(request.getRequestURI());

        auditLogRepository.save(log);
    }

    private HttpServletRequest getCurrentRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();
    }
}
