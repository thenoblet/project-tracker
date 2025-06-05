package gtp.projecttracker.config;

import gtp.projecttracker.model.jpa.Task;
import gtp.projecttracker.repository.jpa.TaskRepository;
import gtp.projecttracker.service.TaskService;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Configuration
@DependsOn("entityManagerFactory")
public class DummyDataInit {
    private final TaskRepository taskRepository;
    private final TaskService taskService;

    public DummyDataInit(TaskRepository taskRepository, TaskService taskService) {
        this.taskRepository = taskRepository;
        this.taskService = taskService;
    }

    @PostConstruct
    public void init() {
        // Check if data already exists to avoid duplicates
        if (taskRepository.count() == 0) {
            // Create some dummy tasks
            Task task1 = new Task("Implement user authentication",
                    "Add login and registration functionality",
                    Task.Status.TODO);
            task1.setPriority(Task.Priority.HIGH);
            task1.setDueDate(LocalDate.now().plusDays(7));
            task1.setCreatedAt(LocalDateTime.now());

//            Task task2 = new Task("Design database schema",
//                    "Create ERD and table definitions",
//                    Task.Status.IN_PROGRESS);
//            task2.setPriority(Task.Priority.MEDIUM);
//            task2.setDueDate(LocalDate.now().plusDays(5));
//
//            Task task3 = new Task("Write unit tests",
//                    "Add comprehensive test coverage",
//                    Task.Status.TODO);
//            task3.setPriority(Task.Priority.LOW);
//            task3.setDueDate(LocalDate.now().plusDays(14));
//
//            Task task4 = new Task("Fix bug in payment processing",
//                    "Resolve issue with failed transactions",
//                    Task.Status.BLOCKED);
//            task4.setPriority(Task.Priority.HIGH);
//            task4.setDueDate(LocalDate.now().plusDays(2));
//
//            Task task5 = new Task("Update documentation",
//                    "Keep API docs up to date",
//                    Task.Status.DONE);
//            task5.setPriority(Task.Priority.LOW);
//            task5.setDueDate(LocalDate.now().minusDays(1));

            // Save all tasks
            taskService.saveTask(task1);
//            taskService.saveTask(task2);
//            taskService.saveTask(task3);
//            taskService.saveTask(task4);
//            taskService.saveTask(task5);

            System.out.println("Dummy data initialized: 5 tasks created");
        }
    }
}