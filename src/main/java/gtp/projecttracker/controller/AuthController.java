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
import jakarta.validation.groups.Default;
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

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;
    private final Validator validator;

    public AuthController(AuthService authService,  Validator validator) {
        this.authService = authService;
        this.validator = validator;
    }

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

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok(MessageResponse.of("Logged out successfully"));
    }

    @GetMapping("/oauth2/login/success")
    public ResponseEntity<?> loginSuccess(@RequestParam String token) {
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("message", "Login successful");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/oauth2/failure")
    public ResponseEntity<?> handleFailure(@RequestParam String error) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                        "error", "OAuth2 authentication failed",
                        "message", error
                ));
    }

    @GetMapping("/check-roles")
    public ResponseEntity<Map<String, Object>> checkRoles(Authentication authentication) {
        return ResponseEntity.ok(Map.of(
                "username", authentication.getName(),
                "roles", authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList())
        ));
    }

    @GetMapping("/auth/check-auth")
    public ResponseEntity<?> checkAuthentication(Authentication authentication) {
        return ResponseEntity.ok(Map.of(
                "name", authentication.getName(),
                "authorities", authentication.getAuthorities()
        ));
    }
}

//    @PostMapping("/token-refresh")
//    public ResponseEntity<MessageResponse> refreshToken() {
//        return ResponseEntity.ok(
//                MessageResponse.withDetails(
//                        "Token refreshed successfully",
//                        Map.of("expires_in", 3600)
//                )
//        );
//    }