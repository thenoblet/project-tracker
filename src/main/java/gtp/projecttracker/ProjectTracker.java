package gtp.projecttracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableCaching
@EnableMongoAuditing
@EnableAsync
@EnableMongoRepositories(basePackages = "gtp.projecttracker.repository.mongodb")
public class ProjectTracker {

    public static void main(String[] args) {
        SpringApplication.run(ProjectTracker.class, args);
    }

}
