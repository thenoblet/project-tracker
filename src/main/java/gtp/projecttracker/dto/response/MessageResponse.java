package gtp.projecttracker.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Simple DTO for returning success messages with consistent formatting
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record MessageResponse(
        LocalDateTime timestamp,
        String message,
        Map<String, Object> details
) {
    public MessageResponse(String message) {
        this(LocalDateTime.now(), message, null);
    }

    public MessageResponse(String message, Map<String, Object> details) {
        this(LocalDateTime.now(), message, details);
    }

    public static MessageResponse of(String message) {
        return new MessageResponse(message);
    }

    public static MessageResponse withDetails(String message, Map<String, Object> details) {
        return new MessageResponse(message, details);
    }
}
