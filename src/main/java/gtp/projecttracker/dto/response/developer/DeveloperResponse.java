package gtp.projecttracker.dto.response.developer;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public record DeveloperResponse(
        UUID id,
        String name,
        String email,
        Set<String> skills,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}