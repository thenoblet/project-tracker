package gtp.projecttracker.repository.mongodb;

import gtp.projecttracker.model.mongodb.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface AuditLogRepository extends MongoRepository<AuditLog, String> {
    List<AuditLog> findByEntityType(String entityType);
    List<AuditLog> findByActorName(String actorName);

    @Query("{'timestamp': {$gte: ?0, $lte: ?1}}")
    List<AuditLog> findByTimestampBetween(Instant startDate, Instant endDate);

    Page<AuditLog> findByActionType(AuditLog.ActionType actionType, Pageable pageable);
}
