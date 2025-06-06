package gtp.projecttracker.repository.jpa;

import gtp.projecttracker.model.jpa.Developer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeveloperRepository extends JpaRepository<Developer, Long> {
    Developer findByName(String name);

    Developer findByEmail(String email);

    Developer findById(UUID id);

    @Query("SELECT d FROM Developer d WHERE :skill MEMBER OF d.skills")
    List<Developer> findBySkillsContaining(String skill);

    @Query("SELECT t.assignee, COUNT(t) as taskCount FROM Task t " +
            "WHERE t.assignee IS NOT NULL " +
            "GROUP BY t.assignee " +
            "ORDER BY taskCount DESC")
    List<Object[]> findTopDevelopersByTaskCount(Pageable pageable);

    @Query("SELECT t.assignee FROM Task t WHERE t.id = :taskId")
    Optional<Developer> findDeveloperByTaskId(Long taskId);

    Page<Developer> findByEmailContaining(String email, Pageable pageable);

    void deleteById(UUID id);
}
