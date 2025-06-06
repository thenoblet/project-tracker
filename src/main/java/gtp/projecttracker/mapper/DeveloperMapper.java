package gtp.projecttracker.mapper;

import gtp.projecttracker.dto.request.developer.CreateDeveloperRequest;
import gtp.projecttracker.dto.request.developer.UpdateDeveloperRequest;
import gtp.projecttracker.dto.response.developer.DeveloperResponse;
import gtp.projecttracker.model.jpa.Developer;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class DeveloperMapper {

    /**
     * Convert Developer entity to DeveloperResponse DTO
     */
    public DeveloperResponse toResponse(Developer developer) {
        if (developer == null) {
            return null;
        }

        return new DeveloperResponse(
                developer.getId(),
                developer.getName(),
                developer.getEmail(),
                developer.getSkills(),
                developer.getCreatedAt(),
                developer.getUpdatedAt()
        );
    }

    /**
     * Convert CreateDeveloperRequest DTO to Developer entity
     */
    public Developer toEntity(CreateDeveloperRequest request) {
        if (request == null) {
            return null;
        }

        Developer developer = new Developer();
        developer.setName(request.name());
        developer.setEmail(request.email());

        if (request.skills() != null) {
            developer.setSkills(request.skills());
        }

        return developer;
    }

    /**
     * Update existing Developer entity with UpdateDeveloperRequest data
     */
    public void updateEntity(Developer existingDeveloper, UpdateDeveloperRequest request) {
        if (existingDeveloper == null || request == null) {
            return;
        }

        request.name().ifPresent(existingDeveloper::setName);
        request.email().ifPresent(existingDeveloper::setEmail);
        request.skills().ifPresent(existingDeveloper::setSkills);
    }

    /**
     * Convert list of Developer entities to list of DeveloperResponse DTOs
     */
    public List<DeveloperResponse> toResponseList(List<Developer> developers) {
        if (developers == null) {
            return null;
        }

        return developers.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Convert Developer entity to simple DeveloperResponse (with minimal fields)
     */
    public DeveloperResponse toSimpleResponse(Developer developer) {
        if (developer == null) {
            return null;
        }

        return new DeveloperResponse(
                developer.getId(),
                developer.getName(),
                null, // no email
                null,  // no skills
                null,  // no createdAt
                null   // no updatedAt
        );
    }
}