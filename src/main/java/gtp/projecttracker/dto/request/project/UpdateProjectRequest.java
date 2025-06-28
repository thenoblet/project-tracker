package gtp.projecttracker.dto.request.project;

import jakarta.validation.constraints.*;
import gtp.projecttracker.model.jpa.Project.ProjectStatus;

import java.time.LocalDate;
import java.util.Optional;

public record UpdateProjectRequest(
        Optional<String> name,

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

