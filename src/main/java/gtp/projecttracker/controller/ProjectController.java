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
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects")
public class ProjectController {
    private final ProjectService projectService;
    private final TaskService taskService;

    public ProjectController(ProjectService projectService, TaskService taskService) {
        this.projectService = projectService;
        this.taskService = taskService;
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(
            @Valid @RequestBody CreateProjectRequest request) {
        ProjectResponse response = projectService.createProject(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getProjectById(
            @PathVariable UUID id) {
        return ResponseEntity.ok(projectService.getProjectById(id));
    }

    @GetMapping
    public ResponseEntity<Page<ProjectSummaryResponse>> getAllProjects(
            Pageable pageable,
            boolean includeTasks) {
        return ResponseEntity.ok(projectService.getAllProjects(pageable, includeTasks));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponse> updateProject(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProjectRequest request) {
        return ResponseEntity.ok(projectService.updateProject(id, request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ProjectResponse> patchProject(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProjectRequest request){
        return ResponseEntity.ok(projectService.patchProject(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ProjectResponse> updateProjectStatus(
            @PathVariable UUID id,
            @RequestParam Project.ProjectStatus status) {
        return ResponseEntity.ok(projectService.updateProjectStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(
            @PathVariable UUID id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }


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

    @GetMapping("{id}/tasks/overdue") // Project-specific
    public ResponseEntity<Page<TaskResponse>> getProjectOverdueTasks(
            @PathVariable UUID id,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(taskService.getOverdueTasksByProject(id, pageable));
    }
}