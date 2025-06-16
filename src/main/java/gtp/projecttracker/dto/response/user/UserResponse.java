package gtp.projecttracker.dto.response.user;

import gtp.projecttracker.model.jpa.User;

import java.util.Set;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String name,
        Set<String> skills,
        boolean isOAuthUser
) {
    public UserResponse(User user) {
        this(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getSkills(),
                user.isOauth2User()
        );
    }
}
