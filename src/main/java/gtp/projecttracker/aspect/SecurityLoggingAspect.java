package gtp.projecttracker.aspect;

import gtp.projecttracker.dto.request.user.RegisterRequest;
import gtp.projecttracker.dto.response.ErrorResponse;
import gtp.projecttracker.exception.EmailAlreadyExistsException;
import gtp.projecttracker.model.jpa.User;
import gtp.projecttracker.model.mongodb.AuditLog;
import gtp.projecttracker.repository.mongodb.AuditLogRepository;
import gtp.projecttracker.security.service.AuthService;
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
     * Logs successful user registrations (only when 200 OK is returned)
     */
    @AfterReturning(
            pointcut = "execution(* gtp.projecttracker.controller.AuthController.register(..)) && args(request)",
            returning = "response",
            argNames = "request,response"
    )
    public void logSuccessfulRegistration(RegisterRequest request, ResponseEntity<?> response) {
        if (response.getStatusCode() != HttpStatus.OK) {
            return;
        }

        try {
            HttpServletRequest httpRequest = getCurrentRequest();

            AuditLog log = new AuditLog();
            log.setActionType(AuditLog.ActionType.REGISTRATION_SUCCESS);
            log.setTimestamp(Instant.now());
            log.setActorName(request.email());
            log.setIpAddress(httpRequest.getRemoteAddr());
            log.setUserAgent(httpRequest.getHeader("User-Agent"));
            log.setEndpoint(httpRequest.getRequestURI());
            log.setPayload("User registered successfully");

            auditLogRepository.save(log);
            logger.info("Successful registration for: {}", request.email());
        } catch (Exception e) {
            logger.error("Failed to log registration success", e);
        }
    }

    /**
     * Logs failed registration attempts (including email exists)
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
            HttpServletRequest httpRequest = getCurrentRequest();

            AuditLog log = new AuditLog();
            log.setActionType(AuditLog.ActionType.REGISTRATION_FAILURE);
            log.setTimestamp(Instant.now());
            log.setActorName(request.email());
            log.setIpAddress(httpRequest.getRemoteAddr());
            log.setUserAgent(httpRequest.getHeader("User-Agent"));
            log.setEndpoint(httpRequest.getRequestURI());

            if (response.getBody() instanceof ErrorResponse errorResponse) {
                log.setPayload("Failure reason: " + errorResponse.message());
            } else {
                log.setPayload("Failure reason: " + response.getStatusCode());
            }

            auditLogRepository.save(log);
            logger.warn("Failed registration attempt for: {}", request.email());
        } catch (Exception e) {
            logger.error("Failed to log registration failure", e);
        }
    }


    /**
     * Logs successful logins
     */
    @AfterReturning(
            pointcut = "execution(* gtp.projecttracker.security.service.AuthService.login(..))",
            returning = "result"
    )
    public void logSuccessfulLogin(JoinPoint joinPoint, Object result) {
        HttpServletRequest request = getCurrentRequest();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        AuditLog log = new AuditLog();
        log.setActionType(AuditLog.ActionType.LOGIN_SUCCESS);
        log.setTimestamp(Instant.now());
        log.setActorName(auth != null ? auth.getName() : "anonymous");
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
        log.setActorName(username);
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
        log.setActorName(auth != null ? auth.getName() : "anonymous");
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
        log.setActorName(auth != null ? auth.getName() : "anonymous");
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