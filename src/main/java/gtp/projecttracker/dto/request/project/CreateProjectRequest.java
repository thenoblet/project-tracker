package gtp.projecttracker.dto.request.project;

import gtp.projecttracker.model.jpa.Project.ProjectStatus;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.stream.Collectors;

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
        LocalDate deadline,

        ProjectStatus status
) {
    public CreateProjectRequest {
        if (startDate != null && deadline != null && deadline.isBefore(startDate)) {
            throw new IllegalArgumentException("Deadline must be after start date");
        }

        if (status != null) {
            try {
                ProjectStatus.valueOf(String.valueOf(status).toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(
                        "Invalid status '" + status + "'. Valid values are: " +
                                Arrays.stream(ProjectStatus.values())
                                        .map(Enum::name)
                                        .collect(Collectors.joining(", "))
                );
            }
        }
    }

    public ProjectStatus getStatus() {
        return status != null ? ProjectStatus.valueOf(String.valueOf(status).toUpperCase()) : null;
    }
}
