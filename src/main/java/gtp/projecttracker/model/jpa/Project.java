package gtp.projecttracker.model.jpa;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Entity class representing a project in the project tracking system.
 * 
 * A project is a collection of related tasks with a defined timeline and status.
 * Each project has a unique identifier, name, description, start date, deadline,
 * and status. The system also maintains creation and update timestamps for each project.
 * Projects can contain multiple tasks, which are tracked separately.
 */
@Entity
@Table(name = "projects", indexes = {
        @Index(name = "idx_project_status", columnList = "status"),
        @Index(name = "idx_project_deadline", columnList = "deadline")
})
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(unique = true, nullable = false)
    private UUID id;

    @NotBlank
    @Size(min = 2, max = 255)
    private String name;

    @Size(min = 2, max = 500)
    @Column(length = 500)
    private String description;

    @NotNull(message = "Start date is required")
    @FutureOrPresent
    private LocalDate startDate;

    @NotNull(message = "Deadline is required")
    @FutureOrPresent
    private LocalDate deadline;

    @Enumerated(EnumType.STRING)
    private ProjectStatus status = ProjectStatus.ACTIVE;

    @NotNull
    private LocalDateTime createdAt;

    @NotNull
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Task> tasks = new ArrayList<>();

    /**
     * Lifecycle callback method that is automatically called before the entity is persisted.
     * Sets the creation and update timestamps to the current time.
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Lifecycle callback method that is automatically called before the entity is updated.
     * Updates the update timestamp to the current time.
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Validates that the project deadline is valid in relation to the start date.
     * A deadline is considered valid if it is null, the start date is null, or
     * the deadline is not before the start date.
     *
     * @return true if the deadline is valid, false otherwise
     */
    public boolean isValidDeadline() {
        return deadline == null || startDate == null || !deadline.isBefore(startDate);
    }

    /**
     * Enumeration of possible project statuses.
     * These statuses represent the different states a project can be in during its lifecycle.
     */
    public enum ProjectStatus {
        ACTIVE,
        IN_PROGRESS,
        BLOCKED,
        ON_HOLD,
        IN_REVIEW,
        COMPLETED,
        CANCELLED
    }

    /**
     * Gets the unique identifier of the project.
     *
     * @return The project's unique identifier
     */
    public UUID getId() {
        return id;
    }

    /**
     * Sets the unique identifier of the project.
     * This method is typically used by JPA and not in application code.
     *
     * @param id The unique identifier to set
     */
    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * Gets the name of the project.
     *
     * @return The project's name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the project.
     * The name must not be blank and must be between 2 and 255 characters long.
     *
     * @param name The name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the description of the project.
     *
     * @return The project's description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the project.
     * If provided, the description must be between 2 and 500 characters long.
     *
     * @param description The description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the start date of the project.
     *
     * @return The date when the project is scheduled to start
     */
    public LocalDate getStartDate() {
        return startDate;
    }

    /**
     * Sets the start date of the project.
     * The start date must not be null and should be in the present or future.
     *
     * @param startDate The start date to set
     */
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    /**
     * Gets the deadline of the project.
     *
     * @return The date by which the project should be completed
     */
    public LocalDate getDeadline() {
        return deadline;
    }

    /**
     * Sets the deadline of the project.
     * The deadline must not be null and should be in the present or future.
     * It should also not be before the start date.
     *
     * @param deadline The deadline to set
     */
    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    /**
     * Gets the current status of the project.
     *
     * @return The project's status
     * @see ProjectStatus
     */
    public ProjectStatus getStatus() {
        return status;
    }

    /**
     * Sets the status of the project.
     *
     * @param status The status to set
     * @see ProjectStatus
     */
    public void setStatus(ProjectStatus status) {
        this.status = status;
    }

    /**
     * Gets the timestamp when this project was created.
     *
     * @return The creation timestamp
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the creation timestamp of this project.
     * This method is typically used by JPA and not in application code.
     *
     * @param createdAt The creation timestamp to set
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Gets the timestamp when this project was last updated.
     *
     * @return The last update timestamp
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Sets the last update timestamp of this project.
     * This method is typically used by JPA and not in application code.
     *
     * @param updatedAt The update timestamp to set
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Gets the list of tasks associated with this project.
     *
     * @return The list of tasks in this project
     */
    public List<Task> getTasks() {
        return tasks;
    }

    /**
     * Sets the list of tasks associated with this project.
     *
     * @param tasks The list of tasks to associate with this project
     */
    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    /**
     * Compares this project with another object for equality.
     * Two projects are considered equal if they have the same ID.
     *
     * @param o The object to compare with this project
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Project project = (Project) o;
        return Objects.equals(id, project.id);
    }

    /**
     * Returns a hash code value for this project.
     * The hash code is based on the project's ID.
     *
     * @return A hash code value for this project
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
