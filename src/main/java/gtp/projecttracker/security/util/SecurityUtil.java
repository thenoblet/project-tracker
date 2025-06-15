package gtp.projecttracker.security.util;

import gtp.projecttracker.exception.ResourceNotFoundException;
import gtp.projecttracker.model.jpa.Task;
import gtp.projecttracker.repository.jpa.TaskRepository;
import gtp.projecttracker.security.service.UserDetailsImpl;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SecurityUtil {
    private static final String ADMIN_ROLE = "ROLE_ADMIN";
    private static final String MANAGER_ROLE = "ROLE_MANAGER";

    TaskRepository taskRepository;

    public SecurityUtil(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public boolean isTaskOwner(UUID taskId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return false;

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        String currentEmail = auth.getName();
        return task.getAssignee() != null &&
                task.getAssignee().getEmail().equals(currentEmail);
    }

    public boolean isAdmin() {
        return SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(ADMIN_ROLE));
    }

    public boolean isManager() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }

        return auth.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(MANAGER_ROLE));
    }

    public boolean isAdminOrManager() {
        return isAdmin() || isManager();
    }

    public UserDetailsImpl getCurrentUser() {
        return (UserDetailsImpl) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
    }
}