package gtp.projecttracker.controller;

import gtp.projecttracker.dto.request.user.LoginRequest;
import gtp.projecttracker.dto.request.user.RegisterRequest;
import gtp.projecttracker.dto.response.ErrorResponse;
import gtp.projecttracker.dto.response.JwtResponse;
import gtp.projecttracker.dto.response.MessageResponse;
import gtp.projecttracker.exception.EmailAlreadyExistsException;
import gtp.projecttracker.security.service.AuthService;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Authentication controller handling user registration, login, logout, and token operations.
 * <p>
 * Provides REST endpoints for core authentication workflows including:
 * <ul>
 *   <li>User registration and validation</li>
 *   <li>JWT-based authentication</li>
 *   <li>OAuth2 integration endpoints</li>
 *   <li>Token management</li>
 *   <li>Authorization checks</li>
 * </ul>
 * </p>
 *
 * @see AuthService
 * @see JwtResponse
 * @see org.springframework.security.authentication
 */
@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;
    private final Validator validator;

    /**
     * Constructs an AuthController with required dependencies.
     *
     * @param authService the authentication service for business logic
     * @param validator the validator for request validation
     */
    public AuthController(AuthService authService, Validator validator) {
        this.authService = authService;
        this.validator = validator;
    }

    /**
     * Registers a new user account.
     * <p>
     * Validates the registration request and creates a new user if validation passes.
     * Returns a JWT response on success or appropriate error responses for failures.
     * </p>
     *
     * @param request the registration request containing user details
     * @return ResponseEntity containing either:
     *         <ul>
     *           <li>JwtResponse on success (200 OK)</li>
     *           <li>ErrorResponse for validation failures (400 Bad Request)</li>
     *           <li>ErrorResponse for email conflicts (409 Conflict)</li>
     *           <li>ErrorResponse for server errors (500 Internal Server Error)</li>
     *         </ul>
     * @throws ConstraintViolationException if request validation fails
     * @see RegisterRequest
     * @see JwtResponse
     * @see ErrorResponse
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        Set<ConstraintViolation<RegisterRequest>> violations =
                validator.validate(request,
                        RegisterRequest.NonOAuthValidation.class);

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        try {
            JwtResponse response = authService.register(request);
            return ResponseEntity.ok(response);
        } catch (EmailAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ErrorResponse.of(
                            HttpStatus.CONFLICT.value(),
                            "Registration Failed",
                            e.getMessage(),
                            "/api/v1/auth/register",
                            Map.of("conflictingEmail", e.getEmail())
                    ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.of(
                            HttpStatus.BAD_REQUEST.value(),
                            "Invalid Request",
                            e.getMessage(),
                            "/api/v1/auth/register"
                    ));
        } catch (Exception e) {
            log.error("Registration error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Registration Error",
                            "An unexpected error occurred",
                            "/api/v1/auth/register",
                            Map.of("errorDetail", e.getMessage())
                    ));
        }
    }

    /**
     * Authenticates a user and returns JWT tokens.
     * <p>
     * Validates credentials and returns authentication tokens on success.
     * Implements proper error handling for invalid credentials and server errors.
     * </p>
     *
     * @param request the login request containing credentials
     * @return ResponseEntity containing either:
     *         <ul>
     *           <li>JwtResponse on success (200 OK)</li>
     *           <li>ErrorResponse for invalid credentials (401 Unauthorized)</li>
     *           <li>ErrorResponse for server errors (500 Internal Server Error)</li>
     *         </ul>
     * @see LoginRequest
     * @see JwtResponse
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            JwtResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            log.warn("Login failed for email: {}", request.email());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.of(
                            HttpStatus.UNAUTHORIZED.value(),
                            "Authentication Failed",
                            "Invalid email or password",
                            "/api/v1/auth/login",
                            Map.of("attemptedEmail", request.email())
                    ));
        } catch (Exception e) {
            log.error("Login error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Login Error",
                            "An unexpected error occurred",
                            "/api/v1/auth/login",
                            Map.of("errorDetail", e.getMessage())
                    ));
        }
    }

    /**
     * Handles user logout.
     *
     * @return ResponseEntity with success message (200 OK)
     * @see MessageResponse
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok(MessageResponse.of("Logged out successfully"));
    }

    /**
     * Handles successful OAuth2 authentication.
     *
     * @param token the JWT token generated after OAuth2 success
     * @return ResponseEntity containing the token and success message (200 OK)
     */
    @GetMapping("/oauth2/login/success")
    public ResponseEntity<?> loginSuccess(@RequestParam String token) {
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("message", "Login successful");
        return ResponseEntity.ok(response);
    }

    /**
     * Handles failed OAuth2 authentication attempts.
     *
     * @param error the error message from OAuth2 provider
     * @return ResponseEntity with error details (401 Unauthorized)
     */
    @GetMapping("/oauth2/failure")
    public ResponseEntity<?> handleFailure(@RequestParam String error) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                        "error", "OAuth2 authentication failed",
                        "message", error
                ));
    }

    /**
     * Returns the current user's roles for authorization checks.
     *
     * @param authentication the current authentication context
     * @return ResponseEntity containing username and roles (200 OK)
     * @throws org.springframework.security.access.AccessDeniedException if not authenticated
     */
    @GetMapping("/check-roles")
    public ResponseEntity<Map<String, Object>> checkRoles(Authentication authentication) {
        return ResponseEntity.ok(Map.of(
                "username", authentication.getName(),
                "roles", authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList())
        ));
    }

    /**
     * Checks and returns current authentication status.
     *
     * @param authentication the current authentication context
     * @return ResponseEntity with authentication details (200 OK)
     * @throws org.springframework.security.access.AccessDeniedException if not authenticated
     */
    @GetMapping("/auth/check-auth")
    public ResponseEntity<?> checkAuthentication(Authentication authentication) {
        return ResponseEntity.ok(Map.of(
                "name", authentication.getName(),
                "authorities", authentication.getAuthorities()
        ));
    }

    /**
     * Refreshes authentication tokens.
     *
     * @return ResponseEntity with success message and token details (200 OK)
     * @see MessageResponse
     */
    @PostMapping("/token-refresh")
    public ResponseEntity<MessageResponse> refreshToken() {
        return ResponseEntity.ok(
                MessageResponse.withDetails(
                        "Token refreshed successfully",
                        Map.of("expires_in", 3600)
                )
        );
    }
}