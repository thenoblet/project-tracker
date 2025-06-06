package gtp.projecttracker.dto.request.task;

import jakarta.validation.constraints.*;
import gtp.projecttracker.model.jpa.Task.Status;
import gtp.projecttracker.model.jpa.Task.Priority;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public record UpdateTaskRequest(
        @Size(min = 2, max = 255, message = "Title must be between 2 and 255 characters")
        Optional<String> title,

        @Size(max = 500, message = "Description cannot exceed 500 characters")
        Optional<String> description,

        Optional<Status> status,

        Optional<Priority> priority,

        @Future(message = "Due date must be in the future")
        Optional<LocalDate> dueDate,

        Optional<UUID> projectId,

        Optional<UUID> assigneeId
) {
}