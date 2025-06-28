package gtp.projecttracker.event;

import java.util.UUID;

public record TaskAssignmentEvent(
        UUID taskId,
        UUID projectId,
        UUID oldAssigneeId,
        UUID newAssigneeId
) {}