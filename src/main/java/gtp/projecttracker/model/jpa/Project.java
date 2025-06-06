package gtp.projecttracker.model.jpa;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.springframework.security.core.parameters.P;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "projects")
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

    @NotBlank
    private LocalDateTime createdAt = LocalDateTime.now();

    @NotBlank
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    private List<Task> tasks = new ArrayList<>();

    public enum ProjectStatus {
        ACTIVE,
        IN_PROGRESS,
        BLOCKED,
        ON_HOLD,
        IN_REVIEW,
        COMPLETED,
        CANCELLED
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public ProjectStatus getStatus() {
        return status;
    }

    public void setStatus(ProjectStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Project project = (Project) o;
        return Objects.equals(id, project.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
