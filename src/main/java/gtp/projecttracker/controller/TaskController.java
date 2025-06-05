package gtp.projecttracker.controller;

import gtp.projecttracker.dto.request.CreateTaskRequest;
import gtp.projecttracker.model.jpa.Task;
import gtp.projecttracker.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tasks")
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody CreateTaskRequest request) {
        Task task =new Task(
                request.title(),
                request.description(),
                request.status(),
                request.priority(),
                request.dueDate()
        );
        return ResponseEntity.ok(taskService.saveTask(task));
    }
}
