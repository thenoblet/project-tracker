package gtp.projecttracker.event;

import java.util.UUID;

/**
 * Published when a project is updated, containing details needed for
 * targeted cache eviction and downstream processing.
 */
public record ProjectUpdatedEvent(
        UUID projectId,
        String projectName,
        boolean nameChanged,
        boolean statusChanged
) {
    /**
     * Factory method for simple updates
     */
    public static ProjectUpdatedEvent basicUpdate(UUID projectId) {
        return new ProjectUpdatedEvent(
                projectId,
                null,
                false,
                false
        );
    }

    /**
     * Determines if this update requires evicting dependent caches
     */
    public boolean requiresFullEviction() {
        return nameChanged || statusChanged;
    }
}
