package gtp.projecttracker.repository.jpa;

import gtp.projecttracker.model.jpa.Developer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeveloperRepository extends JpaRepository<Developer, UUID> {

    Developer findByName(String name);

    Developer findByEmail(String email);

    Developer findDeveloperById(UUID id);

    void deleteById(UUID id);

    @Query("SELECT d FROM Developer d WHERE :skill MEMBER OF d.skills")
    Page<Developer> findBySkill(@Param("skill") String skill, Pageable pageable);

    @Query("SELECT t.assignee, COUNT(t) as taskCount FROM Task t " +
            "WHERE t.assignee IS NOT NULL " +
            "GROUP BY t.assignee " +
            "ORDER BY taskCount DESC LIMIT 5")
    List<Object[]> findTopDevelopersByTaskCount(Pageable pageable);

    @Query("SELECT t.assignee FROM Task t WHERE t.id = :taskId")
    Optional<Developer> findDeveloperByTaskId(@Param("taskId") UUID taskId);

    Page<Developer> findByEmailContaining(String email, Pageable pageable);

    @Query("SELECT d FROM Developer d WHERE " +
            "(:name IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:email IS NULL OR LOWER(d.email) LIKE LOWER(CONCAT('%', :email, '%')))")
    Page<Developer> searchDevelopers(
            @Param("name") String name,
            @Param("email") String email,
            Pageable pageable);

    boolean existsByEmail(String email);

    List<Developer> findBySkillsContaining(String skill);

    boolean existsById(UUID id);
}
