package gtp.projecttracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableCaching
@EnableMongoAuditing
@EnableAsync
public class ProjectTracker {

    public static void main(String[] args) {
        SpringApplication.run(ProjectTracker.class, args);
    }

}
