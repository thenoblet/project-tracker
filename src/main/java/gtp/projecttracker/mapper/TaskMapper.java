package gtp.projecttracker.mapper;

import gtp.projecttracker.dto.request.task.CreateTaskRequest;
import gtp.projecttracker.dto.request.task.UpdateTaskRequest;
import gtp.projecttracker.dto.response.task.TaskResponse;
import gtp.projecttracker.dto.response.task.TaskSummaryResponse;
import gtp.projecttracker.model.jpa.Task;
import gtp.projecttracker.model.jpa.Project;


import gtp.projecttracker.model.jpa.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class TaskMapper {

    /**
     * Convert Task entity to TaskResponse DTO
     */
    public TaskResponse toResponse(Task task) {
        if (task == null) {
            return null;
        }

        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getDueDate(),
                task.getProject() != null ? task.getProject().getId() : null,
                task.getProject() != null ? task.getProject().getName() : null,
                task.getAssignee() != null ? task.getAssignee().getId() : null,
                task.getAssignee() != null ? task.getAssignee().getName() : null,
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }

    /**
     * Convert CreateTaskRequest DTO to Task entity
     */
    public Task toEntity(CreateTaskRequest request) {
        if (request == null) {
            return null;
        }

        Task task = new Task();
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setStatus(request.status());
        task.setPriority(request.priority());
        task.setDueDate(request.dueDate());

        if (request.projectId() != null) {
            Project project = new Project();
            project.setId(request.projectId());
            task.setProject(project);
        }

        if (request.assigneeId() != null) {
            User assignee = new User();
            assignee.setId(request.assigneeId());
            task.setAssignee(assignee);
        }

        return task;
    }

    /**
     * Update existing Task entity with CreateTaskRequest data
     */
    public void updateEntity(Task existingTask, UpdateTaskRequest request) {
        if (existingTask == null || request == null) {
            return;
        }

        request.title().ifPresent(existingTask::setTitle);
        request.description().ifPresent(existingTask::setDescription);
        request.status().ifPresent(existingTask::setStatus);
        request.priority().ifPresent(existingTask::setPriority);
        request.dueDate().ifPresent(existingTask::setDueDate);

        if (request.projectId().isPresent()) {
            UUID projectId = request.projectId().get(); // Fixed: was missing .get()
            if (projectId != null) {
                Project project = new Project();
                project.setId(projectId);
                existingTask.setProject(project);
            } else {
                existingTask.setProject(null); // Allow removing project assignment
            }
        }

        if (request.assigneeId().isPresent()) {
            UUID assigneeId = request.assigneeId().get(); // Fixed: was missing .get()
            if (assigneeId != null) {
                User assignee = new User();
                assignee.setId(assigneeId);
                existingTask.setAssignee(assignee);
            } else {
                existingTask.setAssignee(null); // Allow removing assignee
            }
        }
    }

    public TaskSummaryResponse toSummaryResponse(Task task) {
        return new TaskSummaryResponse(
                task.getId(),
                task.getTitle(),
                task.getStatus(),
                task.getPriority(),
                task.getDueDate()
        );
    }
}