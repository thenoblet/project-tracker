package gtp.projecttracker.event;

import java.util.UUID;

/**
 * Published when user data changes, with flags indicating
 * which caches need invalidation.
 */
public record UserUpdatedEvent(
        UUID userId,
        String email,
        boolean emailChanged,
        boolean roleChanged
) {
    /**
     * Factory method for profile updates
     */
    public static UserUpdatedEvent profileUpdate(UUID userId, String email) {
        return new UserUpdatedEvent(
                userId,
                email,
                true,
                false
        );
    }

    /**
     * Determines if auth cache needs invalidation
     */
    public boolean requiresAuthEviction() {
        return emailChanged || roleChanged;
    }
}
