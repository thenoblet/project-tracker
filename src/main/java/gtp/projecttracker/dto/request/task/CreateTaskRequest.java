package gtp.projecttracker.dto.request.task;

import gtp.projecttracker.model.jpa.Task.Priority;
import gtp.projecttracker.model.jpa.Task.Status;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.UUID;

public record CreateTaskRequest(
        @NotBlank(message = "Title is required")
        @Size(min = 2, max = 255, message = "Title must be between 2 and 255 characters")
        String title,

        @Size(max = 500, message = "Description must not exceed 500 characters")
        String description,

        @NotNull(message = "Due date is required")
        //@FutureOrPresent(message = "Due date must be in the present or future")
        LocalDate dueDate,

        UUID projectId,

        UUID assigneeId,

        Priority priority,

        Status status
) {
    public CreateTaskRequest {
        if (status == null) {
            status = Status.TODO;
        }
        if (priority == null) {
            priority = Priority.MEDIUM;
        }
    }
}
