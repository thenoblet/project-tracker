package gtp.projecttracker.controller;

import gtp.projecttracker.dto.request.project.CreateProjectRequest;
import gtp.projecttracker.dto.request.project.UpdateProjectRequest;
import gtp.projecttracker.dto.response.project.ProjectResponse;
import gtp.projecttracker.dto.response.project.ProjectSummaryResponse;
import gtp.projecttracker.dto.response.task.TaskResponse;
import gtp.projecttracker.model.jpa.Project;
import gtp.projecttracker.model.jpa.Task;
import gtp.projecttracker.service.ProjectService;
import gtp.projecttracker.service.TaskService;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

/**
 * REST controller for managing projects.
 * Provides endpoints for creating, retrieving, updating, and deleting project records,
 * as well as managing project status and associated tasks.
 * All endpoints return data in JSON format and are accessible under the /api/v1/projects path.
 */
@RestController
@RequestMapping("/api/v1/projects")
public class ProjectController {
    private final ProjectService projectService;
    private final TaskService taskService;

    /**
     * Constructs a ProjectController with the required service dependencies.
     *
     * @param projectService The service for managing project operations
     * @param taskService The service for managing task operations
     */
    public ProjectController(ProjectService projectService, TaskService taskService) {
        this.projectService = projectService;
        this.taskService = taskService;
    }

    /**
     * Creates a new project.
     *
     * @param request The validated request containing project details
     * @return The created project wrapped in a ResponseEntity with HTTP 201 status
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_MANAGER')")
    public ResponseEntity<ProjectResponse> createProject(
            @Valid @RequestBody CreateProjectRequest request) {
        ProjectResponse response = projectService.saveProject(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves a project by its unique identifier.
     *
     * @param id The UUID of the project to retrieve
     * @return The project details wrapped in a ResponseEntity
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getProjectById(
            @PathVariable UUID id) {
        return ResponseEntity.ok(projectService.getProjectById(id));
    }

    /**
     * Retrieves all projects with pagination support.
     *
     * @param pageable Pagination information including page number, size, and sorting
     * @param includeTasks Flag to determine if task details should be included in the response
     * @return A paginated list of project summaries wrapped in a ResponseEntity
     */
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_MANAGER')")
    public ResponseEntity<Page<ProjectSummaryResponse>> getAllProjects(
            Pageable pageable,
            boolean includeTasks) {
        return ResponseEntity.ok(projectService.getAllProjects(pageable, includeTasks));
    }

    /**
     * Updates a project's information completely (full update).
     *
     * @param id The UUID of the project to update
     * @param request The validated request containing the updated project details
     * @return The updated project wrapped in a ResponseEntity
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_MANAGER')")
    public ResponseEntity<ProjectResponse> updateProject(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProjectRequest request) {
        return ResponseEntity.ok(projectService.updateProject(id, request));
    }

    /**
     * Partially updates a project's information.
     *
     * @param id The UUID of the project to update
     * @param request The validated request containing the fields to update
     * @return The updated project wrapped in a ResponseEntity
     */
    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_MANAGER')")
    public ResponseEntity<ProjectResponse> patchProject(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProjectRequest request){
        return ResponseEntity.ok(projectService.patchProject(id, request));
    }

    /**
     * Updates only the status of a project.
     *
     * @param id The UUID of the project to update
     * @param status The new status to set for the project
     * @return The updated project wrapped in a ResponseEntity
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_MANAGER')")
    public ResponseEntity<ProjectResponse> updateProjectStatus(
            @PathVariable UUID id,
            @RequestParam Project.ProjectStatus status) {
        return ResponseEntity.ok(projectService.updateProjectStatus(id, status));
    }

    /**
     * Deletes a project by its unique identifier.
     *
     * @param id The UUID of the project to delete
     * @return A ResponseEntity with HTTP 204 No Content status
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_MANAGER')")
    public ResponseEntity<Void> deleteProject(
            @PathVariable UUID id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Retrieves tasks associated with a specific project with filtering options.
     *
     * @param id The UUID of the project to get tasks for
     * @param status Optional filter for task status
     * @param assigneeName Optional filter for tasks assigned to a specific developer
     * @param dueDateFrom Optional filter for minimum due date
     * @param dueDateTo Optional filter for maximum due date
     * @param pageable Pagination information including page number, size, and sorting
     * @return A paginated list of tasks wrapped in a ResponseEntity
     */
    @GetMapping("/{id}/tasks")
    public ResponseEntity<Page<TaskResponse>> getProjectTasks(
            @PathVariable UUID id,
            @RequestParam(required = false) Task.Status status,
            @RequestParam(required = false) String assigneeName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDateTo,
            @PageableDefault(size = 10, sort = "dueDate", direction = Sort.Direction.ASC) Pageable pageable) {

        Page<TaskResponse> tasks = projectService.getProjectTasks(
                id, status, assigneeName, dueDateFrom, dueDateTo, pageable);
        return ResponseEntity.ok(tasks);
    }

    /**
     * Retrieves overdue tasks for a specific project.
     *
     * @param id The UUID of the project to get overdue tasks for
     * @param pageable Pagination information including page number and size
     * @return A paginated list of overdue tasks wrapped in a ResponseEntity
     */
    @GetMapping("{id}/tasks/overdue")
    public ResponseEntity<Page<TaskResponse>> getProjectOverdueTasks(
            @PathVariable UUID id,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(taskService.getOverdueTasksByProject(id, pageable));
    }
}
