package gtp.projecttracker.dto.response;

import gtp.projecttracker.model.jpa.Project;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record ProjectResponse (
        UUID id,
        String name,
        String description,
        LocalDate deadline,
        LocalDate startDate,
        String status,
        int taskCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ProjectResponse fromEntity(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getStartDate(),
                project.getDeadline(),
                project.getStatus().name(),
                project.getTasks().size(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }
}
