package gtp.projecttracker.dto.request.developer;

import java.util.Optional;
import java.util.Set;

public record UpdateDeveloperRequest(
        Optional<String> name,
        Optional<String> email,
        Optional<Set<String>> skills
) {}