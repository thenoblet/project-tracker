package gtp.projecttracker.controller;

import gtp.projecttracker.dto.response.task.TaskSummaryResponse;
import gtp.projecttracker.mapper.TaskMapper;
import gtp.projecttracker.model.jpa.Task;
import gtp.projecttracker.service.TaskService;
import gtp.projecttracker.dto.request.task.AssignTaskRequest;
import gtp.projecttracker.dto.request.task.CreateTaskRequest;
import gtp.projecttracker.dto.request.task.UpdateTaskRequest;
import gtp.projecttracker.dto.response.task.TaskResponse;

import jakarta.validation.Valid;
import org.apache.coyote.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for managing tasks.
 * Provides endpoints for creating, retrieving, updating, and deleting task records,
 * as well as assigning tasks to developers and retrieving overdue tasks.
 * All endpoints return data in JSON format and are accessible under the /api/v1/tasks path.
 */
@RestController
@RequestMapping("/api/v1/tasks")
public class TaskController {

    private static final Logger log = LoggerFactory.getLogger(TaskController.class);

    private final TaskService taskService;
    private final TaskMapper taskMapper;

    /**
     * Constructs a TaskController with the required service and mapper dependencies.
     *
     * @param taskService The service for managing task operations
     * @param taskMapper The mapper for converting between task entities and DTOs
     */
    public TaskController(TaskService taskService, TaskMapper taskMapper) {
        this.taskService = taskService;
        this.taskMapper = taskMapper;
    }

    /**
     * Retrieves all tasks with pagination support.
     *
     * @param pageable Pagination information including page number, size, and sorting
     *
     * @return A paginated list of tasks wrapped in a ResponseEntity
     */
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_MANAGER')")
    public ResponseEntity<Page<TaskSummaryResponse>> getAllTasks(@PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(taskService.getTasks(pageable));
    }

    /**
     * Creates a new task.
     *
     * @param request The validated request containing task details
     * @return The created task wrapped in a ResponseEntity
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_MANAGER')")
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody CreateTaskRequest request) {
        return ResponseEntity.ok(taskService.saveTask(taskMapper.toEntity(request)));
    }

    /**
     * Retrieves a task by its unique identifier.
     *
     * @param id The UUID of the task to retrieve
     * @return The task details wrapped in a ResponseEntity
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_MANAGER') or @securityUtil.isTaskOwner(#id)")
    public ResponseEntity<TaskResponse> getTask(@PathVariable UUID id) {
        return ResponseEntity.ok(taskService.getTaskById(id));
    }

    /**
     * Deletes a task by its unique identifier.
     *
     * @param id The UUID of the task to delete
     * @return A ResponseEntity with HTTP 204 No Content status
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_MANAGER')")
    public ResponseEntity<Void> deleteTask(@PathVariable UUID id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.info("Deleting task {} by user: {} with authorities: {}", id, auth.getName(), auth.getAuthorities());
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Updates a task's information completely (full update).
     *
     * @param id The UUID of the task to update
     * @param request The request containing the updated task details
     * @return The updated task wrapped in a ResponseEntity
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_MANAGER')")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable UUID id,
            @RequestBody UpdateTaskRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        log.info("Current user: {}", auth.getName());
        log.info("User has admin role: {}",
                auth.getAuthorities().stream()
                        .anyMatch(g -> g.getAuthority().equals("ROLE_ADMIN")));
        log.info("Updating task {} by user: {} with authorities: {}", id, auth.getName(), auth.getAuthorities());
        return ResponseEntity.ok(taskService.updateTask(id, request));
    }

    /**
     * Partially updates a task's information.
     *
     * @param id The UUID of the task to update
     * @param request The request containing the fields to update
     * @return The updated task wrapped in a ResponseEntity
     */
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or @securityUtil.isTaskOwner(#id)")
    public ResponseEntity<TaskResponse> patchTask(
            @PathVariable UUID id,
            @RequestBody UpdateTaskRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.info("User roles: {}", auth.getAuthorities());
        return ResponseEntity.ok(taskService.patchTask(id, request));
    }

    /**
     * Assigns a task to a developer and/or updates its status.
     *
     * @param id The UUID of the task to assign
     * @param request The request containing assignment details and/or new status
     * @return The updated task wrapped in a ResponseEntity
     * @throws BadRequestException If the assignment request is invalid
     */
    @PostMapping("/{id}/assign")
    public ResponseEntity<TaskResponse> assignTask(@PathVariable UUID id, @RequestBody AssignTaskRequest request) throws BadRequestException {
        return ResponseEntity.ok(taskService.assignTask(id, request));
    }

    /**
     * Retrieves all overdue tasks with pagination support.
     *
     * @param pageable Pagination information including page number, size, and sorting
     * @return A paginated list of overdue tasks wrapped in a ResponseEntity
     */
    @GetMapping("/overdue")
    public ResponseEntity<Page<TaskResponse>> getOverdueTasks(
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(taskService.getOverdueTasks(pageable));
    }

    /**
     * Test endpoint for triggering overdue task notifications.
     * Used for testing the event publishing mechanism.
     *
     * @return A simple confirmation message
     */
    @GetMapping("/test-publish")
    public String testPublish() {
        Task tasked = taskService.getTaskEntityById(UUID.fromString("c067c1c2-fd74-435d-bcbd-5caaff576dfe"));
        taskService.checkAndNotifyIfOverdue(tasked);
        return "Test event published";
    }
}
