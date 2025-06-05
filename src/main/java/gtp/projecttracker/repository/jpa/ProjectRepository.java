package gtp.projecttracker.repository.jpa;

import gtp.projecttracker.model.jpa.Project;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    @Query("SELECT p FROM Project p WHERE SIZE(p.tasks) = 0")
    List<Project> findProjectsWithoutTasks();

    Project findById(UUID id);
}
