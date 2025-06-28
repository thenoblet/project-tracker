package gtp.projecttracker.event;

import gtp.projecttracker.model.jpa.Task.Status;

import java.util.UUID;

public record TaskStatusChangedEvent(
        UUID taskId,
        UUID projectId,
        Status oldStatus,
        Status newStatus
) {}
