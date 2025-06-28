package gtp.projecttracker.repository.jpa;

import gtp.projecttracker.model.jpa.Task;
import gtp.projecttracker.model.jpa.Task.Status;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID>, JpaSpecificationExecutor<Task> {
    Page<Task> findByProjectId(UUID projectId, Pageable pageable);

    @Query("SELECT t FROM Task t LEFT JOIN FETCH t.assignee WHERE t.dueDate <= :currentDate and t.status <> 'DONE'")
    Page<Task> findOverdueTasks(@Param("currentDate") LocalDate currentDate, Pageable pageable);

    @Query("SELECT t FROM Task t WHERE t.assignee.id = :userId")
    List<Task> findByUserId(@Param("userId") UUID userId);

    Task findTaskById(UUID taskId);

    @Modifying
    @Query("DELETE FROM Task t WHERE t.project.id = :projectId")
    void deleteByProjectId(@Param("projectId") UUID projectId);


    @Modifying
    @Query("UPDATE Task t SET t.assignee = NULL WHERE t.assignee.id = :userId")
    void unassignTasksFromUser(@Param("userId") UUID userId);

    Page<Task> findByDueDateBeforeAndStatusNot(
            @NotNull LocalDate dueDate,
            @NotNull Status status,
            @NotNull Pageable pageable);

    Page<Task> findByAssigneeId(UUID userId, Pageable pageable);

    @Query("SELECT t.assignee.id, COUNT(t) FROM Task t GROUP BY t.assignee.id ORDER BY COUNT(t) DESC")
    List<Object[]> countTasksByUser();

    boolean existsByProjectId(UUID projectId);

    @Query("SELECT t FROM Task t WHERE " +
            "t.project.id = :projectId AND " +
            "(:status IS NULL OR t.status = :status) AND " +
            "(:assigneeName IS NULL OR LOWER(t.assignee.name) LIKE LOWER(:assigneeName)) AND " +
            "(:dueDateFrom IS NULL OR t.dueDate >= :dueDateFrom) AND " +
            "(:dueDateTo IS NULL OR t.dueDate <= :dueDateTo)")
    Page<Task> findByProjectIdAndFilters(
            @Param("projectId") UUID projectId,
            @Param("status") Task.Status status,
            @Param("assigneeName") String assigneeName,
            @Param("dueDateFrom") LocalDate dueDateFrom,
            @Param("dueDateTo") LocalDate dueDateTo,
            Pageable pageable);

    Page<Task> findByProjectIdAndDueDateBeforeAndStatusNot(
            UUID projectId,
            LocalDate dueDate,
            Task.Status status,
            Pageable pageable);

    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END " +
            "FROM Task t WHERE t.id = :taskId " +
            "AND t.dueDate < :currentDate " +
            "AND t.status <> :status")
    boolean existsByIdAndDueDateBeforeAndStatusNot(
            @Param("taskId") UUID taskId,
            @Param("currentDate") LocalDate currentDate,
            @Param("status") Status status
    );
}
