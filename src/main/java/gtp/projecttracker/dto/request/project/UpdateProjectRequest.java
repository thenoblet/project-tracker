package gtp.projecttracker.dto.request.project;

import jakarta.validation.constraints.*;
import gtp.projecttracker.model.jpa.Project.ProjectStatus;

import java.time.LocalDate;
import java.util.Optional;

public record UpdateProjectRequest(
        //@Size(min = 2, max = 255, message = "Name must be between 2 and 255 characters")
        Optional<String> name,

        //@Size(max = 500, message = "Description cannot exceed 500 characters")
        Optional<String> description,

        LocalDate startDate,

        LocalDate deadline,

        Optional<ProjectStatus> status
) {
        @AssertTrue(message = "Start date cannot be in the past")
        private boolean isValidStartDate() {
                return startDate == null || !startDate.isBefore(LocalDate.now());
        }

        @AssertTrue(message = "Deadline cannot be in the past")
        private boolean isValidDeadline() {
                return deadline == null || !deadline.isBefore(LocalDate.now());
        }
}

