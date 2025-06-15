package gtp.projecttracker.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when JWT token validation fails
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class TokenValidationException extends RuntimeException {

    private final String token;
    private final String reason;

    public TokenValidationException(String token, String reason) {
        super(String.format("Token validation failed: %s", reason));
        this.token = token;
        this.reason = reason;
    }

    public TokenValidationException(String reason) {
        super(reason);
        this.token = null;
        this.reason = reason;
    }

    public String getToken() {
        return token;
    }

    public String getReason() {
        return reason;
    }
}