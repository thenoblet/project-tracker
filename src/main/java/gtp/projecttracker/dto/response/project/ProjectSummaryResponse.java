package gtp.projecttracker.dto.response.project;

import gtp.projecttracker.model.jpa.Project.ProjectStatus;

import java.time.LocalDate;
import java.util.UUID;
import java.time.LocalDateTime;

public record ProjectSummaryResponse(
        UUID id,
        String name,
        ProjectStatus status
) {
}