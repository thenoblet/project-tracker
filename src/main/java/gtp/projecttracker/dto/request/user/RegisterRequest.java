package gtp.projecttracker.dto.request.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for user registration requests.
 *
 * @apiNote Used in POST /auth/register endpoint
 */
public record RegisterRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @NotBlank(message = "Password is required", groups = NonOAuthValidation.class)
        @Size(min = 8, max = 64, message = "Password must be 8-64 characters",
                groups = NonOAuthValidation.class)
        String password,

        @NotBlank(message = "Name is required")
        @Size(max = 100, message = "Name must be under 100 characters")
        String name
) {
    public interface NonOAuthValidation {}

    /**
     * Returns a sanitised copy with trimmed strings and lowercase email
     */
    public RegisterRequest sanitized() {
        return new RegisterRequest(
                email.trim().toLowerCase(),
                password != null ? password.trim() : null,
                name.trim()
        );
    }
}
