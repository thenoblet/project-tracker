package gtp.projecttracker.service;

import gtp.projecttracker.dto.request.project.CreateProjectRequest;
import gtp.projecttracker.dto.request.project.UpdateProjectRequest;
import gtp.projecttracker.dto.response.project.ProjectResponse;
import gtp.projecttracker.dto.response.project.ProjectSummaryResponse;
import gtp.projecttracker.dto.response.task.TaskResponse;
import gtp.projecttracker.mapper.ProjectMapper;
import gtp.projecttracker.mapper.TaskMapper;
import gtp.projecttracker.model.jpa.Project;
import gtp.projecttracker.model.jpa.Project.ProjectStatus;
import gtp.projecttracker.model.jpa.Task;
import gtp.projecttracker.repository.jpa.ProjectRepository;
import gtp.projecttracker.repository.jpa.TaskRepository;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class ProjectService {
    private final ApplicationEventPublisher eventPublisher;

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;
    private final TaskRepository taskRepository;
    private final TaskService taskService;
    private final TaskMapper taskMapper;

    @Autowired
    public ProjectService(ProjectRepository projectRepository,
                          ProjectMapper projectMapper,
                          TaskRepository taskRepository,
                          TaskService taskService,
                          TaskMapper taskMapper,
                          ApplicationEventPublisher eventPublisher) {
        this.projectRepository = projectRepository;
        this.projectMapper = projectMapper;
        this.taskRepository = taskRepository;
        this.taskService = taskService;
        this.taskMapper = taskMapper;
        this.eventPublisher = eventPublisher;
    }

    @Cacheable(value = "projects", key = "#id")
    public ProjectResponse getProjectById(UUID id) {
        Project project = projectRepository.findProjectById(id);
        return projectMapper.toResponse(project);
    }

    @Transactional
    @CacheEvict(value = "projects", allEntries = true)
    public ProjectResponse saveProject(CreateProjectRequest projectRequest) {
        Project project = projectMapper.toEntity(projectRequest);
        Project savedProject = projectRepository.save(project);
        return projectMapper.toResponse(savedProject);
    }

    @Transactional
    @CacheEvict(value = "projects", key = "#id")
    public ProjectResponse updateProject(UUID id, UpdateProjectRequest projectRequest) {
        Project existingProject = projectRepository.findProjectById(id);

        boolean nameChanged = projectRequest.name()
                .map(newName -> !newName.equals(existingProject.getName()))
                .orElse(false);

        boolean statusChanged = projectRequest.status()
                .map(newStatus -> existingProject.getStatus() != ProjectStatus.valueOf(String.valueOf(newStatus)))
                .orElse(false);

        projectMapper.updateEntity(projectRequest, existingProject);
        Project updatedProject = projectRepository.save(existingProject);

        eventPublisher.publishEvent(new ProjectUpdatedEvent(
                id,
                updatedProject.getName(),
                nameChanged,
                statusChanged
        ));

        return projectMapper.toResponse(updatedProject);
    }

    @Transactional
    @CacheEvict(value = "projects", key = "#id")
    public ProjectResponse patchProject(UUID id, UpdateProjectRequest projectRequest) {
        Project existingProject = projectRepository.findProjectById(id);

        boolean nameChanged = projectRequest.name()
                .filter(newName -> !newName.equals(existingProject.getName()))
                .isPresent();

        boolean statusChanged = projectRequest.status()
                .filter(newStatus -> existingProject.getStatus() != ProjectStatus.valueOf(String.valueOf(newStatus)))
                .isPresent();

        projectMapper.updateEntity(projectRequest, existingProject);
        Project patchedProject = projectRepository.save(existingProject);

        eventPublisher.publishEvent(new ProjectUpdatedEvent(
                id,
                patchedProject.getName(),
                nameChanged,
                statusChanged
        ));

        return projectMapper.toResponse(patchedProject);
    }

    @Transactional
    @CacheEvict(value = "projects", key = "#id")
    public void deleteProject(UUID id) {
        if (!existsById(id)) {
            throw new EntityNotFoundException("Project not found with id: " + id);
        }
        taskService.deleteAllTasksByProjectId(id);
        projectRepository.deleteById(id);
    }

    public Page<ProjectSummaryResponse> getAllProjects(Pageable pageable, boolean includeTasks) {
        if (includeTasks) {
            return projectRepository.findAllWithTasks(pageable)
                    .map(projectMapper::toSummaryResponse);
        }
        return projectRepository.findAll(pageable)
                .map(projectMapper::toSummaryResponse);
    }


    public Page<ProjectResponse> getAllProjectsWithTasks(Pageable pageable) {
        return projectRepository.findAllWithTasks(pageable)
                .map(projectMapper::toResponse);
    }

    public List<ProjectResponse> getProjectsWithoutTasks() {
        List<Project> projects = projectRepository.findProjectsWithoutTasks();
        return projectMapper.toResponseList(projects);
    }

    public List<ProjectResponse> getProjectsByStatus(ProjectStatus status) {
        List<Project> projects = projectRepository.findByStatus(Project.ProjectStatus.valueOf(status.name()));
        return projectMapper.toResponseList(projects);
    }

    public Project getProjectEntityById(UUID uuid) {
        return projectRepository.findProjectById(uuid);
    }


    public Page<TaskResponse> getProjectTasks(
            UUID projectId,
            @RequestParam(required = false) Task.Status status,
            @RequestParam(required = false) String assigneeName,
            @RequestParam(required = false) LocalDate dueDateFrom,
            @RequestParam(required = false) LocalDate dueDateTo,
            Pageable pageable) {

        if (!existsById(projectId)) {
            throw new EntityNotFoundException("Project not found with id: " + projectId);
        }

        if (status == null && assigneeName == null && dueDateFrom == null && dueDateTo == null) {
            return taskRepository.findByProjectId(projectId, pageable)
                    .map(taskMapper::toResponse);
        }

        Specification<Task> spec = Specification.where((root, query, cb) ->
                cb.equal(root.get("project").get("id"), projectId));

        if (status != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("status"), status));
        }

        if (assigneeName != null) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("assignee").get("name")),
                            "%" + assigneeName.toLowerCase() + "%"));
        }

        if (dueDateFrom != null) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("dueDate"), dueDateFrom));
        }

        if (dueDateTo != null) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("dueDate"), dueDateTo));
        }

        return taskRepository.findAll(spec, pageable)
                .map(taskMapper::toResponse);
    }

    public Page<TaskResponse> getProjectTasksWithFilters(
            UUID projectId,
            Task.Status status,
            String assigneeName,
            LocalDate dueDateFrom,
            LocalDate dueDateTo,
            Pageable pageable) {

        if (!existsById(projectId)) {
            throw new EntityNotFoundException("Project not found with id: " + projectId);
        }

        return taskRepository.findByProjectIdAndFilters(
                projectId,
                status,
                assigneeName != null ? "%" + assigneeName.toLowerCase() + "%" : null,
                dueDateFrom,
                dueDateTo,
                pageable
        ).map(taskMapper::toResponse);
    }

    @Transactional
    @CacheEvict(value = "projects", key = "#id")
    public ProjectResponse updateProjectStatus(UUID id, ProjectStatus status) {
        Project existingProject = projectRepository.findProjectById(id);
        boolean statusChanged = existingProject.getStatus() != ProjectStatus.valueOf(status.name());

        existingProject.setStatus(ProjectStatus.valueOf(status.name()));
        Project updatedProject = projectRepository.save(existingProject);

        eventPublisher.publishEvent(new ProjectUpdatedEvent(
                id,
                updatedProject.getName(),
                false,
                statusChanged
        ));

        return projectMapper.toResponse(updatedProject);
    }

    public boolean existsById(UUID projectId) {
        return projectId != null && projectRepository.existsById(projectId);
    }

}
