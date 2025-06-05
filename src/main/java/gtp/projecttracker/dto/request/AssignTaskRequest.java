package gtp.projecttracker.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AssignTaskRequest(
        @NotNull(message = "Developer ID is required")
        UUID developerId,

        @NotNull(message = "Task status is required")
        Status status
) {
    public enum Status {
        TODO,
        ASSIGNED,
        APPROVED,
        IN_PROGRESS,
        DONE,
        BLOCKED
    }
}