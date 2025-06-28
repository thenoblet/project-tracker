package gtp.projecttracker.event;

import java.util.UUID;

public record TaskUpdatedEvent(
        UUID taskId,
        UUID projectId,
        boolean criticalUpdate // e.g., priority/due date changes
) {}
