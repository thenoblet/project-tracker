package gtp.projecttracker.listener;

import gtp.projecttracker.event.TaskOverdueEvent;
import gtp.projecttracker.exception.EmailException;
import gtp.projecttracker.model.jpa.Developer;
import gtp.projecttracker.model.jpa.Task;
import gtp.projecttracker.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class TaskOverdueEventListener {
    private static final Logger logger = LoggerFactory.getLogger(TaskOverdueEventListener.class);

    private final EmailService emailService;

    public TaskOverdueEventListener(EmailService emailService) {
        this.emailService = emailService;
    }

    @Async
    @EventListener
    public void handleTaskOverdueEvent(TaskOverdueEvent event) {
        Task task = event.task();
        Developer assignee = task.getAssignee();

        if (assignee == null || assignee.getEmail() == null) {
            logger.warn("Task {} is overdue but has no assignee with email", task.getTitle());
            return;
        }

        Map<String, Object> templateContext = Map.of(
                "assigneeName", assignee.getName(),
                "daysOverdue", event.daysOverdue(),
                "taskTitle", task.getTitle(),
                "projectName", task.getProject().getName(),
                "dueDate", task.getDueDate()
        );

        try {
            emailService.sendEmailWithTemplate(
                    assignee.getEmail(),
                    String.format("Task Overdue: %s (%d days)", task.getTitle(), event.daysOverdue()),
                    "task-overdue",
                    templateContext
            );
        } catch (EmailException e) {
            logger.error("Failed to send overdue notification for task {}", task.getId(), e);
        }
    }
}