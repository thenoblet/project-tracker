package gtp.projecttracker.repository.jpa;

import gtp.projecttracker.model.jpa.Task;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    Page<Task> findByProjectId(UUID projectId, Pageable pageable);

    @Query("SELECT t FROM Task t WHERE t.dueDate < CURRENT_DATE and t.status <> 'DONE'")
    List<Task> findOverdueTasks();

    @Query("SELECT t.assignee.id, COUNT(t) FROM Task t GROUP BY t.assignee.id ORDER BY COUNT(t) DESC")
    List<Object[]> countTaskByDeveloper();

    @Query("SELECT t FROM Task t WHERE t.assignee.id = :developerId")
    List<Task> findByDeveloperId(@Param("developerId") UUID developerId);

    Task findById(UUID taskId);

    void deleteById(UUID id);
}
