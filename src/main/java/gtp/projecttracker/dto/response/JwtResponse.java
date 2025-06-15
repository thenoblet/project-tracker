package gtp.projecttracker.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

/**
 * JWT authentication response containing token and user details
 *
 * @param token JWT access token
 * @param userId Authenticated user's ID
 * @param email Authenticated user's email
 * @param role User's assigned role (ADMIN/DEVELOPER/etc.)
 * @param expiresInMs Milliseconds until token expiration
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "JWT authentication response")
public record JwtResponse(
        @Schema(description = "JWT access token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String token,

        @Schema(description = "Authenticated user's ID", example = "123")
        UUID userId,

        @Schema(description = "Authenticated user's email", example = "user@example.com")
        String email,

        @Schema(description = "User's assigned role", example = "DEVELOPER")
        String role,

        @Schema(description = "Milliseconds until token expiration", example = "900000")
        long expiresInMs
) {
    /**
     * Creates a response with additional calculated fields
     */
    public JwtResponse {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token cannot be null or blank");
        }
    }

    /**
     * @return Expiration time in seconds (for client convenience)
     */
    public long expiresInSec() {
        return expiresInMs / 1000;
    }
}