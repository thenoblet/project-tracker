package gtp.projecttracker.dto.request.developer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record CreateDeveloperRequest(
        @NotBlank @Size(min = 2, max = 50)
        String name,

        @NotBlank @Email @Size(max = 150)
        String email,

        Set<@Size(min = 2, max = 50) String> skills
) {}
