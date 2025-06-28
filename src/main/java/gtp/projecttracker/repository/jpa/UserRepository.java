package gtp.projecttracker.repository.jpa;

import gtp.projecttracker.model.jpa.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @NonNull
    Page<User> findAll(@NonNull Pageable pageable);

    @NonNull
    boolean existsById(UUID userId);
}
