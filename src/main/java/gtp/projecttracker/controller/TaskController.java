package gtp.projecttracker.controller;

import gtp.projecttracker.dto.request.task.AssignTaskRequest;
import gtp.projecttracker.dto.request.task.CreateTaskRequest;
import gtp.projecttracker.dto.request.task.UpdateTaskRequest;
import gtp.projecttracker.dto.response.task.TaskResponse;
import gtp.projecttracker.mapper.TaskMapper;
import gtp.projecttracker.model.jpa.Task;
import gtp.projecttracker.service.TaskService;

import jakarta.validation.Valid;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tasks")
public class TaskController {
    private final TaskService taskService;
    private final TaskMapper taskMapper;

    public TaskController(TaskService taskService, TaskMapper taskMapper) {
        this.taskService = taskService;
        this.taskMapper = taskMapper;
    }

    @GetMapping
    public ResponseEntity<Page<TaskResponse>> getAllTasks(@PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(taskService.getTasks(pageable));
    }

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody CreateTaskRequest request) {
        return ResponseEntity.ok(taskService.saveTask(taskMapper.toEntity(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTask(@PathVariable UUID id) {
        return ResponseEntity.ok(taskService.getTaskById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable UUID id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable UUID id,
            @RequestBody UpdateTaskRequest request) {
        return ResponseEntity.ok(taskService.updateTask(id, request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TaskResponse> patchTask(
            @PathVariable UUID id,
            @RequestBody UpdateTaskRequest request) {
        return ResponseEntity.ok(taskService.patchTask(id, request));
    }

    @PostMapping("/{id}/assign")
    public ResponseEntity<TaskResponse> assignTask(@PathVariable UUID id, @RequestBody AssignTaskRequest request) throws BadRequestException {
        return ResponseEntity.ok(taskService.assignTask(id, request));
    }

    @GetMapping("/overdue")
    public ResponseEntity<Page<TaskResponse>> getOverdueTasks(
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(taskService.getOverdueTasks(pageable));
    }


}
