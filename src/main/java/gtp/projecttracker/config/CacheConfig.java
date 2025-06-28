package gtp.projecttracker.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {
    public static final String CACHE_USERS = "users";
    public static final String CACHE_PROJECTS = "projects";
    public static final String CACHE_TASKS = "tasks";
    public static final String CACHE_AUTH = "authCache";

    /**
     * Primary cache manager for business entities
     */
    @Bean
    @Primary
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
                CACHE_USERS,
                CACHE_PROJECTS,
                CACHE_TASKS
        );

        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(5000)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .recordStats()
                .weakKeys()
        );

        return cacheManager;
    }

    /**
     * Specialized cache manager for authentication-related data
     * with shorter TTL and higher throughput
     */
    @Bean
    public CacheManager authCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(CACHE_AUTH);
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(5000)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .recordStats()
        );
        return cacheManager;
    }
}