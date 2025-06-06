package gtp.projecttracker.service;

import gtp.projecttracker.dto.request.task.AssignTaskRequest;
import gtp.projecttracker.dto.request.task.CreateTaskRequest;
import gtp.projecttracker.dto.request.task.UpdateTaskRequest;
import gtp.projecttracker.dto.response.task.TaskResponse;
import gtp.projecttracker.exception.ResourceNotFoundException;
import gtp.projecttracker.mapper.TaskMapper;
import gtp.projecttracker.model.jpa.Developer;
import gtp.projecttracker.model.jpa.Task;
import gtp.projecttracker.model.jpa.Task.Status;
import gtp.projecttracker.model.jpa.Task.Priority;
import gtp.projecttracker.repository.jpa.TaskRepository;

import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final DeveloperService developerService;

    @Autowired
    public TaskService(TaskRepository taskRepository, TaskMapper taskMapper, DeveloperService developerService) {
        this.taskRepository = taskRepository;
        this.taskMapper = taskMapper;
        this.developerService = developerService;
    }

    public Page<TaskResponse> getTasks(Pageable pageable) {
        Page<Task> taskPage = taskRepository.findAll(pageable);
        return taskPage.map(taskMapper::toResponse);
    }

    public Page<TaskResponse> getTasksByProjectId(UUID projectId, Pageable pageable) {
        Page<Task> taskPage = taskRepository.findByProjectId(projectId, pageable);
        return taskPage.map(taskMapper::toResponse);
    }

    public List<TaskResponse> getOverdueTasks() {
        List<Task> tasks = taskRepository.findOverdueTasks();
        return taskMapper.toResponseList(tasks);
    }

    public List<TaskResponse> getTasksByDeveloperId(UUID developerId) {
        List<Task> tasks = taskRepository.findByDeveloperId(developerId);
        return taskMapper.toResponseList(tasks);
    }

    public List<Object[]> getTaskCountByDeveloper() {
        return taskRepository.countTaskByDeveloper();
    }

    public TaskResponse getTaskById(UUID taskId) {
        Task task = taskRepository.findById(taskId);
        return taskMapper.toResponse(task);
    }

    @Transactional
    public TaskResponse createTask(CreateTaskRequest request) {
        Task task = taskMapper.toEntity(request);
        Task savedTask = taskRepository.save(task);
        return taskMapper.toResponse(savedTask);
    }

    @Transactional
    public TaskResponse updateTask(UUID taskId, UpdateTaskRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Task request cannot be null");
        }

        Task existingTask = taskRepository.findById(taskId);

        taskMapper.updateEntity(existingTask, request);
        Task updatedTask = taskRepository.save(existingTask);
        return taskMapper.toResponse(updatedTask);
    }

    public TaskResponse patchTask(UUID taskId, UpdateTaskRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Task request cannot be null");
        }

        Task existingTask = taskRepository.findById(taskId);
        taskMapper.updateEntity(existingTask, request);
        Task updatedTask = taskRepository.save(existingTask);
        return taskMapper.toResponse(updatedTask);
    }

    @Transactional
    public void deleteTask(UUID id) {
        if (!taskRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    String.format("Task with id %s not found", id)
            );
        }
        taskRepository.deleteById(id);
    }

    @Transactional
    public TaskResponse saveTask(Task task) {
        Task savedTask = taskRepository.save(task);
        return taskMapper.toResponse(savedTask);
    }

    public TaskResponse assignTask(UUID taskId, AssignTaskRequest request) throws BadRequestException {
        Objects.requireNonNull(request, "AssignTaskRequest cannot be null");

        Task task = taskRepository.findById(taskId);
        if (task == null) {
            throw new ResourceNotFoundException("Task not found with id: " + taskId);
        }

        try {
            task.setStatus(Status.valueOf(request.status().name()));
            task.setPriority(Priority.valueOf(request.priority().name()));
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid status or priority value");
        }

        Developer developer = developerService.getDeveloperEntityById(request.developerId());
        if (developer == null) {
            throw new ResourceNotFoundException("Developer not found with id: " + request.developerId());
        }

        task.setAssignee(developer);
        task.setUpdatedAt(LocalDateTime.now());

        Task savedTask = taskRepository.save(task);
        return taskMapper.toResponse(savedTask);
    }

    public Task getTaskEntityById(UUID taskId) {
        return taskRepository.findById(taskId);
    }
}