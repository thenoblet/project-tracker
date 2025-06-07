package gtp.projecttracker.dto.response.task;

import gtp.projecttracker.model.jpa.Task;
import gtp.projecttracker.model.jpa.Task.Priority;
import gtp.projecttracker.model.jpa.Task.Status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public record TaskResponse(
        UUID id,
        String title,
        String description,
        Status status,
        Task.Priority priority,
        LocalDate dueDate,
        UUID projectId,
        String projectName,
        UUID assigneeId,
        String assigneeName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static TaskResponse fromEntity(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus() != null ? Status.valueOf(task.getStatus().name()) : null,
                task.getPriority() != null ? Priority.valueOf(task.getPriority().name()) : null,
                task.getDueDate(),
                task.getProject() != null ? task.getProject().getId() : null,
                task.getProject() != null ? task.getProject().getName() : null,
                task.getAssignee() != null ? task.getAssignee().getId() : null,
                task.getAssignee() != null ? task.getAssignee().getName() : null,
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }

    public long daysOverdue() {
        if (dueDate == null || !dueDate.isBefore(LocalDate.now())) {
            return 0;
        }
        return ChronoUnit.DAYS.between(dueDate, LocalDate.now());
    }

}
