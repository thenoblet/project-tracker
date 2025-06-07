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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final DeveloperService developerService;
    private final ApplicationEventPublisher eventPublisher;
    private final Map<UUID, LocalDate> lastNotificationSent = new ConcurrentHashMap<>();

    private static final Logger log = LoggerFactory.getLogger(TaskService.class);

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

    @Transactional
    public void deleteAllTasksByProjectId(UUID id) {
        if (!taskRepository.existsByProjectId(id)) {
            throw new ResourceNotFoundException("No tasks found for project with id: " + id);
        }
        taskRepository.deleteByProjectId(id);
    }

    public Page<TaskResponse> getOverdueTasks(Pageable pageable) {
        return taskRepository.findOverdueTasks(LocalDate.now(), pageable)
                .map(taskMapper::toResponse);
    }

    public Page<TaskResponse> getOverdueTasksByProject(UUID projectId, Pageable pageable) {
        return taskRepository.findByProjectIdAndDueDateBeforeAndStatusNot(
                projectId,
                LocalDate.now(),
                Status.DONE,
                pageable
        ).map(taskMapper::toResponse);
    }

    @Scheduled(fixedDelayString = "${app.notifications.overdue-check-interval:5000}")
    @Transactional
    public void checkAndNotifyOverdueTasks() {
        log.info("Checking for overdue tasks at {}", LocalDateTime.now());

        LocalDate today = LocalDate.now();
        Page<Task> overdueTasksPage;
        int page = 0;
        final int pageSize = 100;
        int totalTasksProcessed = 0;

        do {
            overdueTasksPage = taskRepository.findOverdueTasks(
                    today,
                    PageRequest.of(page, pageSize)
            );

            List<Task> tasksInPage = overdueTasksPage.getContent();
            totalTasksProcessed += tasksInPage.size();

            log.info("Processing overdue tasks page: {} with {} tasks (Total so far: {})",
                    page, tasksInPage.size(), totalTasksProcessed);

            log.debug("Processing overdue tasks page: {} with {} tasks", page, overdueTasksPage.getNumberOfElements());

            if (log.isDebugEnabled()) {
                int finalPage = page;
                tasksInPage.forEach(task ->
                        log.debug("Task in page {}: ID={}, Title='{}', DueDate={}, Status={}, DaysOverdue={}",
                                finalPage,
                                task.getId(),
                                task.getTitle(),
                                task.getDueDate(),
                                task.getStatus(),
                                task.getDueDate() != null ? ChronoUnit.DAYS.between(task.getDueDate(), today) : "N/A")
                );
            }

            // If you want to see basic info at INFO level (be careful with production logs)
            if (log.isInfoEnabled() && !tasksInPage.isEmpty()) {
                log.info("Tasks in page {}: [{}]", page,
                        tasksInPage.stream()
                                .map(task -> String.format("ID=%s(Due:%s)",
                                        task.getId().toString().substring(0, 8),
                                        task.getDueDate()))
                                .collect(java.util.stream.Collectors.joining(", ")));
            }

            overdueTasksPage.getContent().forEach(this::checkAndNotifyIfOverdue);
            log.info("Overdue tasks processed: {}", overdueTasksPage.getContent());
            page++;
        } while (overdueTasksPage.hasNext());

        log.info("Completed overdue task check. Processed {} pages", page);
    }

    public void checkAndNotifyIfOverdue(Task task) {
        log.info("Checking for overdue task {}", task.getId());

        try {
            LocalDate lastNotified = lastNotificationSent.get(task.getId());
            log.info("Last notification sent: {}", lastNotified);
            LocalDate today = LocalDate.now();
            log.info("Today: {}", today);
            log.info("Task due date: {}", task.getDueDate());

            if (lastNotified == null || lastNotified.isBefore(today)) {
                log.info("PUBLISHING EVENT for task {} (Due: {}, Status: {})",
                        task.getId(), task.getDueDate(), task.getStatus());

                int daysOverdue = Math.toIntExact(ChronoUnit.DAYS.between(
                        task.getDueDate(),
                        today));
                log.info("Days overdue: {}", daysOverdue);

                eventPublisher.publishEvent(new TaskOverdueEvent(task, daysOverdue));
                lastNotificationSent.put(task.getId(), today);
            } else {
                log.debug("Notification already sent today for task {}", task.getId());
            }
        } catch (Exception e) {
            log.error("Failed to process task {}: {}", task.getId(), e.getMessage());
        }
    }
}
