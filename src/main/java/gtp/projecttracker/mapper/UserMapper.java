package gtp.projecttracker.mapper;

import gtp.projecttracker.dto.request.user.RegisterRequest;
import gtp.projecttracker.dto.response.user.UserResponse;
import gtp.projecttracker.model.jpa.User;
import org.springframework.stereotype.Component;

import java.util.Optional;


@Component
public class UserMapper {

    public User toEntity(RegisterRequest request) {
        return new User(
                request.name(),
                request.email(),
                request.password()
        );
    }

    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getSkills(),
                user.isOauth2User()
        );
    }


    public void updateEntityFromRequest(User user, RegisterRequest request) {
        if (request.name() != null) {
            user.setName(request.name().trim());
        }
        if (request.email() != null) {
            user.setEmail(request.email().trim().toLowerCase());
        }
        if (request.password() != null) {
            user.setPassword(request.password());
        }
    }
}