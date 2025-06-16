package gtp.projecttracker.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        List<ValidationError> validationErrors,
        Map<String, Object> details
) {

    public record ValidationError(
            String field,
            Object rejectedValue,
            String message
    ) {}

    public static ErrorResponse of(int status, String error, String message, String path) {
        return new ErrorResponse(
                LocalDateTime.now(),
                status,
                error,
                message,
                path,
                null,
                null
        );
    }

    public static ErrorResponse of(int status, String error, String message, String path, Map<String, Object> details) {
        return new ErrorResponse(
                LocalDateTime.now(),
                status,
                error,
                message,
                path,
                null,
                null
        );
    }

    public static ErrorResponse withValidationErrors(int status, String error, String message,
                                                     String path, List<ValidationError> validationErrors) {
        return new ErrorResponse(
                LocalDateTime.now(),
                status,
                error,
                message,
                path,
                validationErrors,
                null
        );
    }

    public static ErrorResponse withDetails(int status, String error, String message,
                                            String path, Map<String, Object> details) {
        return new ErrorResponse(
                LocalDateTime.now(),
                status,
                error,
                message,
                path,
                null,
                details
        );
    }
}