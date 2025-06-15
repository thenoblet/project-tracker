package gtp.projecttracker.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for application caching.
 * Enables Spring's caching support and configures a cache manager
 * for storing frequently accessed data to improve performance.
 */
@Configuration
@EnableCaching
public class CacheConfig {
    /**
     * Creates and configures a CacheManager for the application.
     * Sets up in-memory caches for projects, developers, and tasks data.
     *
     * @return A configured ConcurrentMapCacheManager instance
     */
    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("projects", "developers", "tasks");
    }
}
