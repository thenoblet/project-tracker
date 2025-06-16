package gtp.projecttracker.exception;

import gtp.projecttracker.dto.response.ErrorResponse;
import gtp.projecttracker.model.mongodb.AuditLog;
import gtp.projecttracker.model.mongodb.AuditLog.ActionType;
import gtp.projecttracker.repository.mongodb.AuditLogRepository;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;

/**
 * Handles authentication and authorisation exceptions across the application
 */
@RestControllerAdvice
public class AuthExceptionHandler {

    private final AuditLogRepository auditLogRepository;

    public AuthExceptionHandler(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    // Unified security exception handler
    public ResponseEntity<ErrorResponse> handleSecurityException(Exception ex, WebRequest request) {
        if (ex instanceof BadCredentialsException) {
            return handleBadCredentials((BadCredentialsException) ex, request);
        } else if (ex instanceof AccessDeniedException) {
            return handleAccessDenied((AccessDeniedException) ex, request);
        } else if (ex instanceof EmailAlreadyExistsException) {
            return handleEmailExists((EmailAlreadyExistsException) ex, request);
        } else if (ex instanceof TokenValidationException) {
            return handleInvalidToken((TokenValidationException) ex);
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex, WebRequest request) {
        logSecurityEvent(ActionType.LOGIN_FAILURE, ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.of(
                        HttpStatus.UNAUTHORIZED.value(),
                        "Authentication Failed",
                        "Invalid credentials",
                        getCurrentRequestPath()
                ));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, WebRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String user = auth != null ? auth.getName() : "anonymous";

        logSecurityEvent(ActionType.ACCESS_DENIED,
                "User " + user + " attempted to access restricted resource");

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.of(
                        HttpStatus.FORBIDDEN.value(),
                        "Access Denied",
                        "You don't have permission to access this resource",
                        getCurrentRequestPath()
                ));
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailExists(EmailAlreadyExistsException ex, WebRequest request) {
        logSecurityEvent(ActionType.REGISTRATION_FAILURE, ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of(
                        HttpStatus.CONFLICT.value(),
                        "Registration Failed",
                        ex.getMessage(),
                        getCurrentRequestPath()
                ));
    }

    @ExceptionHandler(TokenValidationException.class)
    public ResponseEntity<ErrorResponse> handleInvalidToken(TokenValidationException ex) {
        logSecurityEvent(ActionType.INVALID_TOKEN, ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.of(
                        HttpStatus.UNAUTHORIZED.value(),
                        "Invalid Token",
                        ex.getMessage(),
                        getCurrentRequestPath()
                ));
    }

    private void logSecurityEvent(ActionType actionType, String message) {
        HttpServletRequest request = getCurrentRequest();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        AuditLog log = new AuditLog();
        log.setActionType(actionType);
        log.setTimestamp(Instant.now());
        log.setUsername(auth != null ? auth.getName() : "anonymous");
        log.setIpAddress(request.getRemoteAddr());
        log.setUserAgent(request.getHeader("User-Agent"));
        log.setEndpoint(request.getRequestURI());
        log.setPayload(message);

        auditLogRepository.save(log);
    }

    private HttpServletRequest getCurrentRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();
    }

    private String getCurrentRequestPath() {
        return getCurrentRequest().getRequestURI();
    }
}
