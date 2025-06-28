package gtp.projecttracker;

import gtp.projecttracker.config.EmailProperties;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;


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


    @Bean
    public CommandLineRunner checkCache(Environment env) {
        return args -> {
            System.out.println("Active cache type: " +
                    env.getProperty("spring.cache.type"));
        };
    }
}
