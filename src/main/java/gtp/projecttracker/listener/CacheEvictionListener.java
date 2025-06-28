package gtp.projecttracker.listener;

import gtp.projecttracker.config.CacheConfig;

import gtp.projecttracker.event.ProjectUpdatedEvent;
import gtp.projecttracker.event.UserUpdatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class CacheEvictionListener {
    private final CacheManager cacheManager;

    private static final Logger log = LoggerFactory.getLogger(CacheEvictionListener.class);

    public CacheEvictionListener(@Qualifier("cacheManager") CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @TransactionalEventListener
    public void handleProjectChange(ProjectUpdatedEvent event) {
        cacheManager.getCache(CacheConfig.CACHE_PROJECTS).evict(event.projectId());
        log.debug("Evicted project {} from cache", event.projectId());
    }

    @TransactionalEventListener
    public void handleUserChange(UserUpdatedEvent event) {
        cacheManager.getCache(CacheConfig.CACHE_USERS).evict(event.userId());
        cacheManager.getCache(CacheConfig.CACHE_AUTH).evict(event.email());
        log.debug("Evicted user {} and auth data from cache", event.userId());
    }
}
