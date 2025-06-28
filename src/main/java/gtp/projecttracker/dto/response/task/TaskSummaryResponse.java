package gtp.projecttracker.dto.response.task;

import gtp.projecttracker.model.jpa.Task;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record TaskSummaryResponse(
        UUID id,
        String title,
        Task.Status status,
        Task.Priority priority,
        LocalDate dueDate

) {
}
