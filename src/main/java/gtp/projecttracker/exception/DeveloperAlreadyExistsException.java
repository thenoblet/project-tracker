package gtp.projecttracker.exception;

public class DeveloperAlreadyExistsException extends RuntimeException {
    public DeveloperAlreadyExistsException(String email) {
        super("Developer with email '" + email + "' already exists");
    }
}