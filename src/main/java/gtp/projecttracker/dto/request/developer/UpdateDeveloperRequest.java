package gtp.projecttracker.dto.request.developer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import java.util.Optional;
import java.util.Set;

public record UpdateDeveloperRequest(
        Optional<@Size(min = 2, max = 255, message = "Name must be between 2 and 255 characters") String> name,

        Optional<@Size(min = 2, max = 255, message = "Email must be between 2 and 255 characters") @Email(message = "Email must be valid") String> email,

        Optional<Set<String>> skills
) {}