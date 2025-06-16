package gtp.projecttracker.listener;

import gtp.projecttracker.event.TaskOverdueEvent;
import gtp.projecttracker.exception.EmailException;
import gtp.projecttracker.model.jpa.Task;
import gtp.projecttracker.model.jpa.User;
import gtp.projecttracker.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Component
public class TaskOverdueEventListener {
    private static final Logger logger = LoggerFactory.getLogger(TaskOverdueEventListener.class);

    private final EmailService emailService;

    public TaskOverdueEventListener(EmailService emailService) {
        logger.info("EmailService is {} null", emailService == null ? "" : "NOT ");
        this.emailService = emailService;
        logger.info("TaskOverdueEventListener initialized!");
    }

    @Async
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleTaskOverdueEvent(TaskOverdueEvent event) {
        logger.debug("Received TaskOverdueEvent for task ID: {}", event.task().getId());

        Task task = event.task();
        logger.debug("Processing overdue task: {}", task.getTitle());

        User assignee = task.getAssignee();
        if (assignee == null || assignee.getEmail() == null) {
            logger.warn("Task {} is overdue but has no assignee with email", task.getTitle());
            return;
        }

        logger.info("Task {} is overdue for assignee {}", task.getTitle(), assignee);
        String projectName = (task.getProject() != null) ? task.getProject().getName() : "No Project";
        logger.info("Task {} is overdue for project {}", task.getTitle(), projectName);
        Map<String, Object> templateContext = Map.of(
                "assigneeName", assignee.getName(),
                "daysOverdue", event.daysOverdue(),
                "taskTitle", task.getTitle(),
                "projectName", projectName,
                "dueDate", task.getDueDate()
        );

        logger.info("Sending overdue notification for task '{}' to {}",
                task.getTitle(),
                assignee.getEmail());
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