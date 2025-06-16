package gtp.projecttracker.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when attempting to register with an email that already exists
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class EmailAlreadyExistsException extends RuntimeException {

    private final String email;

    public EmailAlreadyExistsException(String email) {
        super(String.format("Email '%s' is already registered", email));
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}