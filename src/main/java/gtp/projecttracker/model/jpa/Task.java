package gtp.projecttracker.model.jpa;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entity class representing a task in the project tracking system.
 * 
 * A task is a unit of work that needs to be completed as part of a project.
 * Tasks can be assigned to developers, have different statuses and priorities,
 * and are tracked with creation and update timestamps.
 */
@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = true, nullable = false)
    private UUID id;

    @NotBlank
    @Size(min = 2, max = 255)
    private String title;

    @Size(min = 2, max = 500)
    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    private Status status = Status.TODO;

    @Enumerated(EnumType.STRING)
    private Priority priority = Priority.LOW;

    //@FutureOrPresent
    @NotNull
    private LocalDate dueDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User assignee;

    @NotNull
    private LocalDateTime createdAt;

    @NotNull
    private LocalDateTime updatedAt;


    public enum Status {
        TODO,
        ASSIGNED,
        APPROVED,
        IN_PROGRESS,
        DONE,
        BLOCKED
    }

    public enum Priority {
        LOW,
        MEDIUM,
        HIGH
    }

    /**
     * Default constructor required by JPA.
     */
    public Task() {
    }

    /**
     * Creates a new task with the specified details.
     *
     * @param title The title of the task
     * @param description The detailed description of the task
     * @param status The initial status of the task
     * @param priority The priority level of the task
     * @param dueDate The date by which the task should be completed
     */
    public Task(String title, String description, Status status, Priority priority, LocalDate dueDate) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.status = status;
        this.dueDate = dueDate;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Lifecycle callback method that is automatically called before the entity is persisted.
     * Sets the creation and update timestamps to the current time.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    /**
     * Lifecycle callback method that is automatically called before the entity is updated.
     * Updates the update timestamp to the current time.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Gets the unique identifier of the task.
     *
     * @return The task's unique identifier
     */
    public UUID getId() {
        return id;
    }

    /**
     * Sets the unique identifier of the task.
     * This method is typically used by JPA and not in application code.
     *
     * @param id The unique identifier to set
     */
    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * Gets the title of the task.
     *
     * @return The task's title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title of the task.
     * The title must not be blank and must be between 2 and 255 characters long.
     *
     * @param title The title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Gets the description of the task.
     *
     * @return The task's description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the task.
     * If provided, the description must be between 2 and 500 characters long.
     *
     * @param description The description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the current status of the task.
     *
     * @return The task's status
     * @see Status
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Sets the status of the task.
     *
     * @param status The status to set
     * @see Status
     */
    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * Gets the priority level of the task.
     *
     * @return The task's priority
     * @see Priority
     */
    public Priority getPriority() {
        return priority;
    }

    /**
     * Sets the priority level of the task.
     *
     * @param priority The priority to set
     * @see Priority
     */
    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    /**
     * Gets the due date of the task.
     *
     * @return The date by which the task should be completed
     */
    public LocalDate getDueDate() {
        return dueDate;
    }

    /**
     * Sets the due date of the task.
     * The due date must not be null and should be in the present or future.
     *
     * @param dueDate The due date to set
     */
    public void setDueDate(@FutureOrPresent @NotNull LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    /**
     * Gets the project to which this task belongs.
     *
     * @return The project associated with this task
     */
    public Project getProject() {
        return project;
    }

    /**
     * Sets the project to which this task belongs.
     *
     * @param project The project to associate with this task
     */
    public void setProject(Project project) {
        this.project = project;
    }

    /**
     * Gets the developer assigned to this task.
     *
     * @return The developer assigned to complete this task
     */
    public User getAssignee() {
        return assignee;
    }

    /**
     * Sets the developer assigned to this task.
     *
     * @param assignee The developer to assign to this task
     */
    public void setAssignee(User assignee) {
        this.assignee = assignee;
    }

    /**
     * Gets the timestamp when this task was created.
     *
     * @return The creation timestamp
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the creation timestamp of this task.
     * This method is typically used by JPA and not in application code.
     *
     * @param createdAt The creation timestamp to set
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Gets the timestamp when this task was last updated.
     *
     * @return The last update timestamp
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Sets the last update timestamp of this task.
     * This method is typically used by JPA and not in application code.
     *
     * @param updatedAt The update timestamp to set
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Compares this task with another object for equality.
     * Two tasks are considered equal if they have the same ID.
     *
     * @param o The object to compare with this task
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return Objects.equals(id, task.id);
    }

    /**
     * Returns a hash code value for this task.
     * The hash code is based on the task's ID.
     *
     * @return A hash code value for this task
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
