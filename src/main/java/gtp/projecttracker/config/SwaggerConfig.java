package gtp.projecttracker.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for Swagger/OpenAPI documentation.
 * Configures the API documentation that will be automatically generated
 * and available at /swagger-ui.html when the application is running.
 *
 * @see "OpenAPI Specification at https://swagger.io/specification/"
 */
@Configuration
public class SwaggerConfig {

    /**
     * Configures the OpenAPI documentation for BuildMaster Project Tracker.
     * Includes server information, API metadata, and license details.
     *
     * @return Configured OpenAPI object with API documentation settings
     */
    @Bean
    public OpenAPI projectTrackerOpenAPI() {
        return new OpenAPI()
                .addServersItem(new Server()
                        .url("http://localhost:8080")
                        .description("Local development server"))
                .info(new Info()
                        .title("BuildMaster Project Tracker API")
                        .description("API for managing projects, tasks and developers")
                        .version("v1.0")
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}