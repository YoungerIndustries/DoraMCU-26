package com.yisy.modules.doramcu.config;
import com.yisy.modules.doramcu.model.players.Player;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.concurrent.TimeUnit;

/**
 * Configuration for Caffeine-based caching of corporation data.
 * <p>
 * Caches frequently accessed corporation state (office state, financial reports, available contracts)
 * to reduce database queries for multiple concurrent players. Cache entries expire after 30 seconds
 * to balance freshness with performance.
 * </p>
 */
@Configuration
@EnableCaching
public class CacheConfig {

    private static final Logger log = LoggerFactory.getLogger(CacheConfig.class);

    @Bean
    @Primary
    public CacheManager corporationCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
                "corporationOfficeState",
                "corporationFinancialReport",
                "corporationContracts",
                "corporationCrew"
        );

        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(500)
                .expireAfterWrite(30, TimeUnit.SECONDS)
                .recordStats()
                .evictionListener((key, value, cause) -> {
                    if (cause != RemovalCause.EXPLICIT) {
                        log.debug("Cache eviction: key={}, cause={}", key, cause);
                    }
                }));

        return cacheManager;
    }
}
