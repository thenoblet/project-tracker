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

@Configuration
public class MongoConfig {
    @Bean
    public IndexOperations auditLogIndexOps(MongoMappingContext context, MongoTemplate mongoTemplate) {
        IndexOperations indexOps = mongoTemplate.indexOps(AuditLog.class);
        indexOps.ensureIndex(new Index().on("timestamp", org.springframework.data.domain.Sort.Direction.DESC)
                .expire(30L * 24 * 60 * 60)); // 30 days in seconds
        return indexOps;
    }
}