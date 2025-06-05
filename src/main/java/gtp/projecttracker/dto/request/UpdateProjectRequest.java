package gtp.projecttracker.dto.request;

import jakarta.validation.constraints.*;
import gtp.projecttracker.model.jpa.Project;

import java.time.LocalDate;
import java.util.Optional;

public record UpdateProjectRequest(
        @Size(min = 2, max = 255, message = "Name must be between 2 and 255 characters")
        Optional<String> name,

        @Size(max = 500, message = "Description cannot exceed 500 characters")
        Optional<String> description,

        @FutureOrPresent(message = "Start date cannot be in the past")
        Optional<LocalDate> startDate,

        @FutureOrPresent(message = "Deadline cannot be in the past")
        Optional<LocalDate> deadline,

        Optional<Project.ProjectStatus> status
) {
}

