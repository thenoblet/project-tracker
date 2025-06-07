package gtp.projecttracker;

import gtp.projecttracker.config.EmailProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCaching
@EnableMongoAuditing
@EnableAsync
@EnableMongoRepositories(basePackages = "gtp.projecttracker.repository.mongodb")
@EnableConfigurationProperties(EmailProperties.class)
@EnableScheduling
public class ProjectTracker {

    public static void main(String[] args) {
        SpringApplication.run(ProjectTracker.class, args);
    }

}
