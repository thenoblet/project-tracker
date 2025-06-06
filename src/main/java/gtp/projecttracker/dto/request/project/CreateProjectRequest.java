package gtp.projecttracker.dto.request.project;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record CreateProjectRequest(
        @NotBlank(message = "Project name is required")
        @Size(min = 2, max = 255, message = "Project name must be between 2 and 255 characters")
        String name,

        @Size(min = 2, max = 500, message = "Description must be between 2 and 500 characters")
        String description,

        @NotNull(message = "Start date is required")
        @FutureOrPresent(message = "Start date must be in the present or future")
        LocalDate startDate,

        @NotNull(message = "Deadline is required")
        @FutureOrPresent(message = "Deadline must be in the present or future")
        LocalDate deadline
) {
    public CreateProjectRequest {
        if (startDate != null && deadline != null && deadline.isBefore(startDate)) {
            throw new IllegalArgumentException("Deadline must be after start date");
        }
    }
}
