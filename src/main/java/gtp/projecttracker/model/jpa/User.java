package gtp.projecttracker.model.jpa;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.Formula;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Entity class representing a user in the project tracking system.
 *
 * A developer is a person who works on tasks within projects. Each developer
 * has a unique identifier, name, email, and a set of skills. The system also
 * tracks the number of tasks assigned to each developer and maintains creation
 * and update timestamps.
 */
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = true, nullable = false)
    private UUID id;

    @Size(max = 50)
    private String name;

    @NotBlank
    @Column(unique = true)
    private String email;

    @Column(nullable = true)
    private String password;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Role role;

    @NotNull
    private boolean oauth2user = false;

    @ElementCollection
    @CollectionTable(name = "user_skills", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "skill")
    private Set<String> skills = new HashSet<>();

    @Formula("(SELECT COUNT(*) FROM tasks t WHERE t.user_id = id)")
    Integer taskCount;

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();


    public enum Role {
        ROLE_ADMIN,
        ROLE_MANAGER,
        ROLE_DEVELOPER,
        ROLE_CONTRACTOR
    }

    public User() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public User(String email, String password, Role role) {
        this();
        this.email = email;
        this.password = password;
        setRole(role);
    }

    public User(String email, String password) {
        this(email, password, Role.ROLE_DEVELOPER);
    }

    @AssertTrue(message = "Password is required for non-OAuth users")
    private boolean isPasswordValid() {
        return oauth2user || (password != null && !password.isBlank());
    }


    /**
     * Gets the unique identifier of the developer.
     *
     * @return The developer's unique identifier
     */
    public UUID getId() {
        return id;
    }

    /**
     * Sets the unique identifier of the developer.
     * This method is typically used by JPA and not in application code.
     *
     * @param id The unique identifier to set
     */
    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * Gets the name of the developer.
     *
     * @return The developer's name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the developer.
     * The name must not be blank and must be at most 50 characters long.
     *
     * @param name The name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the email address of the developer.
     *
     * @return The developer's email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email address of the developer.
     * The email must not be blank and must be unique within the system.
     *
     * @param email The email address to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        if (!oauth2user && (password == null || password.trim().isEmpty())) {
            throw new IllegalArgumentException("Password is required for non-OAuth users");
        }
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        if (role == null) {
            role = Role.ROLE_DEVELOPER;
        }

        this.role = role;
    }

    public boolean isOauth2User() {
        return oauth2user;
    }

    public void setOauth2User(boolean oauth2user) {
        this.oauth2user = oauth2user;
    }

    /**
     * Gets the set of skills possessed by the developer.
     *
     * @return The developer's skills
     */
    public Set<String> getSkills() {
        return skills;
    }

    /**
     * Sets the skills possessed by the developer.
     *
     * @param skills The set of skills to assign to the developer
     */
    public void setSkills(Set<String> skills) {
        this.skills = skills;
    }

    /**
     * Gets the count of tasks assigned to this developer.
     * This value is calculated by a database formula.
     *
     * @return The number of tasks assigned to this developer
     */
    public Integer getTaskCount() {
        return taskCount;
    }

    /**
     * Sets the task count for this developer.
     * This method is typically not used directly as the count is calculated by a database formula.
     *
     * @param taskCount The task counts to set
     */
    public void setTaskCount(Integer taskCount) {
        this.taskCount = taskCount;
    }

    /**
     * Gets the timestamp when this developer record was created.
     *
     * @return The creation timestamp
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the creation timestamp of this developer record.
     * This method is typically used internally and not in application code.
     *
     * @param createdAt The creation timestamp to set
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Gets the timestamp when this developer record was last updated.
     *
     * @return The last update timestamp
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Sets the last update timestamp of this developer record.
     * This method is typically used internally and not in application code.
     *
     * @param updatedAt The update timestamp to set
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Compares this developer with another object for equality.
     * Two developers are considered equal if they have the same ID, name, email, and skills.
     *
     * @param o The object to compare with this developer
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id)
                && Objects.equals(name, user.name)
                && Objects.equals(email, user.email)
                && Objects.equals(skills, user.skills);
    }

    /**
     * Returns a hash code value for this developer.
     * The hash code is based on the developer's ID, name, email, and skills.
     *
     * @return A hash code value for this developer
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, name, email, skills);
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
