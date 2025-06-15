package gtp.projecttracker.dto.request.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO for user login requests.
 *
 * @apiNote Used in POST /auth/login endpoint
 */
public record LoginRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @NotBlank(message = "Password is required")
        String password
) {
    /**
     * Returns a sanitized copy with trimmed strings and lowercase email
     */
    public LoginRequest sanitized() {
        return new LoginRequest(
                email.trim().toLowerCase(),
                password.trim()
        );
    }
}