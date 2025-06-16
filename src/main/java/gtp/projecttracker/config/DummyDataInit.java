package gtp.projecttracker.config;

import gtp.projecttracker.model.jpa.Task;
import gtp.projecttracker.model.jpa.Task.Priority;
import gtp.projecttracker.model.jpa.Task.Status;
import gtp.projecttracker.repository.jpa.TaskRepository;
import gtp.projecttracker.service.TaskService;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import java.time.LocalDate;

/**
 * Configuration class for initializing sample data in the application.
 * Creates dummy tasks for testing and development purposes when the application starts.
 * Only initializes data if the database is empty to avoid duplicate entries.
 */
@Configuration
@DependsOn("entityManagerFactory")
public class DummyDataInit {
    private final TaskRepository taskRepository;
    private final TaskService taskService;

    /**
     * Constructs a new DummyDataInit with required dependencies.
     *
     * @param taskRepository Repository for accessing task data
     * @param taskService Service for task operations including saving with proper event handling
     */
    public DummyDataInit(TaskRepository taskRepository, TaskService taskService) {
        this.taskRepository = taskRepository;
        this.taskService = taskService;
    }

    /**
     * Initializes the application with sample task data.
     * This method is automatically called after the bean is constructed.
     * Creates 5 sample tasks with different statuses, priorities, and due dates,
     * but only if the database is empty to prevent duplicate entries.
     */
    @PostConstruct
    public void init() {
        // Check if data already exists to avoid duplicates
        if (taskRepository.count() == 0) {
            // Create some dummy tasks
            Task task1 = new Task("Implement user authentication",
                    "Add login and registration functionality",
                    Status.TODO,
                    Priority.HIGH,
                    LocalDate.now().plusDays(7));

            Task task2 = new Task("Design database schema",
                    "Create ERD and table definitions",
                    Status.IN_PROGRESS,
                    Priority.MEDIUM,
                    LocalDate.now().plusDays(5));

            Task task3 = new Task("Write unit tests",
                    "Add comprehensive test coverage",
                    Status.TODO,
                    Priority.LOW,
                    LocalDate.now().plusDays(14));

            Task task4 = new Task("Fix bug in payment processing",
                    "Resolve issue with failed transactions",
                    Status.BLOCKED,
                    Priority.HIGH,
                    LocalDate.now().plusDays(2));

            Task task5 = new Task("Update documentation",
                    "Keep API docs up to date",
                    Status.DONE,
                    Priority.LOW,
                    LocalDate.now().plusDays(1));

            // Save all tasks
            taskService.saveTask(task1);
            taskService.saveTask(task2);
            taskService.saveTask(task3);
            taskService.saveTask(task4);
            taskService.saveTask(task5);

            System.out.println("Dummy data initialized: 5 tasks created");
        }
    }
}
