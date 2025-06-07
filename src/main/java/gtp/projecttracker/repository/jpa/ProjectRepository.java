package gtp.projecttracker.repository.jpa;

import gtp.projecttracker.model.jpa.Project;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {
    @Query("SELECT p FROM Project p WHERE SIZE(p.tasks) = 0")
    List<Project> findProjectsWithoutTasks();

    Project findProjectById(UUID id);

    List<Project> findByStatus(Project.ProjectStatus projectStatus);

    @Query("SELECT DISTINCT p FROM Project p LEFT JOIN FETCH p.tasks")
    Page<Project> findAllWithTasks(Pageable pageable);

    void deleteById(UUID id);

    boolean existsById(UUID projectId);

    @Query("SELECT p FROM Project p WHERE " +
            "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:status IS NULL OR p.status = :status) AND " +
            "(:startDateFrom IS NULL OR p.startDate >= :startDateFrom) AND " +
            "(:startDateTo IS NULL OR p.startDate <= :startDateTo) AND " +
            "(:deadlineFrom IS NULL OR p.deadline >= :deadlineFrom) AND " +
            "(:deadlineTo IS NULL OR p.deadline <= :deadlineTo)")
    Page<Project> searchProjects(
            @Param("name") String name,
            @Param("status") Project.ProjectStatus status,
            @Param("startDateFrom") LocalDate startDateFrom,
            @Param("startDateTo") LocalDate startDateTo,
            @Param("deadlineFrom") LocalDate deadlineFrom,
            @Param("deadlineTo") LocalDate deadlineTo,
            Pageable pageable);
}
