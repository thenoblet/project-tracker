package gtp.projecttracker.service;

import gtp.projecttracker.exception.ResourceNotFoundException;
import gtp.projecttracker.model.jpa.Task;
import gtp.projecttracker.repository.jpa.TaskRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class TaskService {
    private final TaskRepository taskRepository;

    @Autowired
    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public Page<Task> getTasksByProjectId(UUID projectId, Pageable pageable) {
        return taskRepository.findByProjectId(projectId, pageable);
    }

    public List<Task> getOverdueTasks() {
        return taskRepository.findOverdueTasks();
    }

    public List<Task> getTasksByDeveloperId(UUID developerId) {
        return taskRepository.findByDeveloperId(developerId);
    }

    @Transactional
    public Task saveTask(Task task) {
        return taskRepository.save(task);
    }

    @Transactional
    public Task updateTask(UUID taskId, Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null");
        }

        Task existingTask = taskRepository.findById(taskId);
        if (existingTask == null) {
            throw new ResourceNotFoundException(
                    String.format("Task with id %s not found", taskId)
            );
        }

        if (task.getTitle() != null) {
            existingTask.setTitle(task.getTitle());
        }

        if (task.getDescription() != null) {
            existingTask.setDescription(task.getDescription());
        }

        if (task.getStatus() != null) {
            existingTask.setStatus(task.getStatus());
        }

        if (task.getAssignee() != null) {
            existingTask.setAssignee(task.getAssignee());
        }

        if (task.getDueDate() != null) {
            existingTask.setDueDate(task.getDueDate());
        }

        return saveTask(existingTask);

    }

    @Transactional
    public void deleteTask(UUID id) {
        taskRepository.deleteById(id);
    }
}
