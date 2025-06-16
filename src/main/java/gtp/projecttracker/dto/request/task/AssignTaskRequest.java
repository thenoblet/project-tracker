package gtp.projecttracker.dto.request.task;

import gtp.projecttracker.model.jpa.Task.Priority;
import gtp.projecttracker.model.jpa.Task.Status;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AssignTaskRequest(
        @NotNull(message = "User ID is required")
        UUID userId,

        @NotNull(message = "Task status is required")
        Status status,

        @NotNull(message = "Task priority is required")
        Priority priority
) {
}