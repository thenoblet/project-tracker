package gtp.projecttracker.mapper;

import gtp.projecttracker.dto.request.project.UpdateProjectRequest;
import gtp.projecttracker.dto.response.project.ProjectSummaryResponse;
import gtp.projecttracker.model.jpa.Project;
import gtp.projecttracker.model.jpa.Project.ProjectStatus;
import gtp.projecttracker.dto.request.project.CreateProjectRequest;
import gtp.projecttracker.dto.response.project.ProjectResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProjectMapper {

    public Project toEntity(CreateProjectRequest request) {
        Project project = new Project();
        project.setName(request.name());
        project.setDescription(request.description());
        project.setStartDate(request.startDate());
        project.setDeadline(request.deadline());
        if (request.status() != null) {
            project.setStatus(ProjectStatus.valueOf(request.status().name()));
        }
        return project;
    }

    public ProjectResponse toResponse(Project entity) {
        return new ProjectResponse(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getStartDate(),
                entity.getDeadline(),
                entity.getStatus().name(),
                entity.getTasks().size(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public List<ProjectResponse> toResponseList(List<Project> entities) {
        return entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public void updateEntity(UpdateProjectRequest request, Project entity) {
        if (request.name().isPresent()) {
            entity.setName(request.name().get());
        }
        if (request.description().isPresent()) {
            entity.setDescription(request.description().get());
        }
        if (request.startDate() != null) {
            entity.setStartDate(request.startDate());
        }
        if (request.deadline() != null) {
            entity.setDeadline(request.deadline());
        }

        if (request.status().isPresent()) {
            entity.setStatus((request.status().get()));
        }
    }

    public ProjectSummaryResponse toSummaryResponse(Project project) {
        return new ProjectSummaryResponse(
                project.getId(),
                project.getName(),
                project.getStatus()
        );
    }
}
