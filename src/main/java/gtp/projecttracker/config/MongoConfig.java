package gtp.projecttracker.config;

import gtp.projecttracker.model.mongodb.AuditLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

/**
 * Configuration class for MongoDB-specific settings.
 * Sets up indexes and other MongoDB configurations required by the application.
 * Currently configures a TTL (time-to-live) index on audit logs to automatically
 * delete old entries after a specified period.
 */
@Configuration
public class MongoConfig {
    /**
     * Configures MongoDB indexes for the AuditLog collection.
     * Creates a TTL (time-to-live) index on the timestamp field that automatically
     * deletes audit log entries after 30 days to prevent the collection from growing indefinitely.
     *
     * @param context MongoDB mapping context
     * @param mongoTemplate Template for MongoDB operations
     * @return The IndexOperations object for the AuditLog collection
     */
    @Bean
    public IndexOperations auditLogIndexOps(MongoMappingContext context, MongoTemplate mongoTemplate) {
        IndexOperations indexOps = mongoTemplate.indexOps(AuditLog.class);
        indexOps.ensureIndex(new Index().on("timestamp", org.springframework.data.domain.Sort.Direction.DESC)
                .expire(30L * 24 * 60 * 60)); // 30 days in seconds
        return indexOps;
    }
}
