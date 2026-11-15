package com.yisy.modules.doramcu.shared.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Caffeine cache configuration for rules database queries.
 * <p>
 * Caches rules entries and tables by (GameSystem, category) key with a TTL of 1 hour.
 * Cache is invalidated when rules are updated via save/delete operations.
 * </p>
 * <p>
 * This addresses Issue #10: No Rules Caching Layer. Previously, every rules query
 * hit the database directly. Now, frequently accessed rules (e.g., combat rules for
 * a specific game system) are cached in memory, reducing database load.
 * </p>
 */
@Configuration
@EnableCaching
public class RulesCacheConfig {

    /**
     * Cache manager for rules entries and tables.
     * <p>
     * Two caches are configured:
     * <ul>
     *   <li>{@code rulesEntries} — caches RulesEntry query results (1 hour TTL)</li>
     *   <li>{@code rulesTables} — caches RulesTable query results (1 hour TTL)</li>
     * </ul>
     * </p>
     */
    @Bean("rulesCacheManager")
    public CacheManager rulesCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
                "rulesEntries",
                "rulesTables"
        );

        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(1, TimeUnit.HOURS)
                .recordStats()
        );

        return cacheManager;
    }
}
