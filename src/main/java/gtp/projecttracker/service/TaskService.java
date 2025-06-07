package gtp.projecttracker.service;

import gtp.projecttracker.dto.request.task.AssignTaskRequest;
import gtp.projecttracker.dto.request.task.CreateTaskRequest;
import gtp.projecttracker.dto.request.task.UpdateTaskRequest;
import gtp.projecttracker.dto.response.task.TaskResponse;
import gtp.projecttracker.event.TaskOverdueEvent;
import gtp.projecttracker.exception.ResourceNotFoundException;
import gtp.projecttracker.mapper.TaskMapper;
import gtp.projecttracker.model.jpa.Developer;
import gtp.projecttracker.model.jpa.Task;
import gtp.projecttracker.model.jpa.Task.Status;
import gtp.projecttracker.model.jpa.Task.Priority;
import gtp.projecttracker.repository.jpa.TaskRepository;

import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final DeveloperService developerService;
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public TaskService(TaskRepository taskRepository,
                       TaskMapper taskMapper,
                       DeveloperService developerService,
                       ApplicationEventPublisher eventPublisher) {
        this.taskRepository = taskRepository;
        this.taskMapper = taskMapper;
        this.developerService = developerService;
        this.eventPublisher = eventPublisher;
    }

    public Page<TaskResponse> getTasks(Pageable pageable) {
        return taskRepository.findAll(pageable)
                .map(taskMapper::toResponse);
    }

    public Page<TaskResponse> getTasksByProjectId(UUID projectId, Pageable pageable) {
        if (!taskRepository.existsByProjectId(projectId)) {
            throw new ResourceNotFoundException("Project not found with id: " + projectId);
        }
        return taskRepository.findByProjectId(projectId, pageable)
                .map(taskMapper::toResponse);
    }

    public Page<TaskResponse> getOverdueTasks(Pageable pageable) {
        Page<Task> tasks = taskRepository.findByDueDateBeforeAndStatusNot(
                LocalDate.now(),
                Status.DONE,
                pageable
        );
        return tasks.map(taskMapper::toResponse);
    }

    public List<TaskResponse> getTasksByDeveloperId(UUID developerId) {
        if (!developerService.existsById(developerId)) {
            throw new ResourceNotFoundException("Developer not found with id: " + developerId);
        }
        return taskMapper.toResponseList(
                taskRepository.findByAssigneeId(developerId)
        );
    }

    public List<Object[]> getTaskCountByDeveloper() {
        return taskRepository.countTasksByDeveloper();
    }

    public TaskResponse getTaskById(UUID taskId) {
        return taskMapper.toResponse(
                taskRepository.findTaskById(taskId)
        );
    }

    @Transactional
    public TaskResponse createTask(CreateTaskRequest request) {
        Task task = taskMapper.toEntity(request);
        task.setStatus(Status.TODO);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());

        return taskMapper.toResponse(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse updateTask(UUID taskId, UpdateTaskRequest request) {
        Task existingTask = getTaskEntityById(taskId);
        taskMapper.updateEntity(existingTask, request);
        existingTask.setUpdatedAt(LocalDateTime.now());
        return taskMapper.toResponse(taskRepository.save(existingTask));
    }

    @Transactional
    public TaskResponse patchTask(UUID taskId, UpdateTaskRequest request) {
        Task existingTask = getTaskEntityById(taskId);

        request.title().ifPresent(existingTask::setTitle);
        request.description().ifPresent(existingTask::setDescription);
        request.dueDate().ifPresent(existingTask::setDueDate);
        request.status().ifPresent(existingTask::setStatus);
        request.priority().ifPresent(existingTask::setPriority);

        existingTask.setUpdatedAt(LocalDateTime.now());
        return taskMapper.toResponse(taskRepository.save(existingTask));
    }

    @Transactional
    public void deleteTask(UUID id) {
        if (!taskRepository.existsById(id)) {
            throw new ResourceNotFoundException("Task not found with id: " + id);
        }
        taskRepository.deleteById(id);
    }

    @Transactional
    public TaskResponse assignTask(UUID taskId, AssignTaskRequest request) throws BadRequestException {
        Task task = getTaskEntityById(taskId);
        Developer developer = developerService.getDeveloperEntityById(request.developerId());

        task.setAssignee(developer);
        task.setStatus(Status.valueOf(request.status().name()));
        task.setPriority(Priority.valueOf(request.priority().name()));
        task.setUpdatedAt(LocalDateTime.now());

        return taskMapper.toResponse(taskRepository.save(task));
    }

    public Task getTaskEntityById(UUID taskId) {
        return taskRepository.findTaskById(taskId);
    }

    @Transactional
    public void unassignAllTasksFromDeveloper(UUID developerId) {
        taskRepository.unassignTasksFromDeveloper(developerId);
    }

    public boolean existsById(UUID taskId) {
        return taskRepository.existsById(taskId);
    }

    public TaskResponse saveTask(Task entity) {
        return taskMapper.toResponse(taskRepository.save(entity));
    }

    public void deleteAllTasksByProjectId(UUID id) {
    }

    public Page<TaskResponse> getOverdueTasksByProject(UUID projectId, Pageable pageable) {
        return taskRepository.findByProjectIdAndDueDateBeforeAndStatusNot(
                projectId,
                LocalDate.now(),
                Status.DONE,
                pageable
        ).map(taskMapper::toResponse);
    }

    @Scheduled(fixedDelayString = "${app.notifications.overdue-check-interval:300000}")
    @Transactional
    public void checkAndNotifyOverdueTasks() {
        LocalDate today = LocalDate.now();
        Page<Task> overdueTasksPage;
        int page = 0;
        final int pageSize = 100;

        do {
            overdueTasksPage = taskRepository.findByDueDateBeforeAndStatusNot(
                    today,
                    Status.DONE,
                    PageRequest.of(page, pageSize)
            );

            overdueTasksPage.getContent().forEach(this::checkAndNotifyIfOverdue);
            page++;
        } while (overdueTasksPage.hasNext());
    }

    private void checkAndNotifyIfOverdue(Task task) {
        if (task.getDueDate() != null
                && task.getDueDate().isBefore(LocalDate.now())
                && task.getStatus() != Status.DONE) {

            // Verify the task is still overdue (might have been updated concurrently)
            boolean isStillOverdue = taskRepository.existsByIdAndDueDateBeforeAndStatusNot(
                    task.getId(),
                    LocalDate.now(),
                    Status.DONE
            );

            if (isStillOverdue) {
                int daysOverdue = Math.toIntExact(ChronoUnit.DAYS.between(task.getDueDate(), LocalDate.now()));
                eventPublisher.publishEvent(new TaskOverdueEvent(task, daysOverdue));
            }
        }
    }

    @Transactional
    public TaskResponse createTestOverdueTask(UUID projectId, UUID developerId) {
        Task task = new Task();
        task.setTitle("Test Overdue Task - " + LocalDateTime.now());
        task.setDescription("This is a test task created for testing overdue notifications");
        task.setDueDate(LocalDate.now().minusDays(2)); // 2 days overdue
        task.setStatus(Status.IN_PROGRESS);
        task.setPriority(Priority.HIGH);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());

        // Set project (you'll need to fetch it)
        // task.setProject(projectService.getProjectEntityById(projectId));

        // Set assignee if provided
        if (developerId != null) {
            Developer developer = developerService.getDeveloperEntityById(developerId);
            task.setAssignee(developer);
        }

        return taskMapper.toResponse(taskRepository.save(task));
    }
}
