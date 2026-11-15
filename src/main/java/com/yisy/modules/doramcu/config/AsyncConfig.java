package com.yisy.modules.doramcu.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configuration for async execution of galaxy-related tasks.
 * <p>
 * Provides separate thread pools for different types of async work:
 * <ul>
 *   <li>Default executor: Corporation AI management cycles</li>
 *   <li>AI executor: Galaxy Overlord AI processing (per-galaxy isolation)</li>
 *   <li>Health check executor: Galaxy health assessments (async scheduled tasks)</li>
 *   <li>Event executor: Galaxy event publishing and SSE broadcasting</li>
 * </ul>
 * This prevents one galaxy's heavy AI computation from blocking another's operations.
 * </p>
 */
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(AsyncConfig.class);

    @Value("${doramcu.async.core-pool-size:5}")
    private int corePoolSize;

    @Value("${doramcu.async.max-pool-size:20}")
    private int maxPoolSize;

    @Value("${doramcu.async.queue-capacity:100}")
    private int queueCapacity;

    @Value("${doramcu.async.ai-core-pool-size:3}")
    private int aiCorePoolSize;

    @Value("${doramcu.async.ai-max-pool-size:15}")
    private int aiMaxPoolSize;

    @Value("${doramcu.async.health-check-core-pool-size:2}")
    private int healthCheckCorePoolSize;

    @Value("${doramcu.async.health-check-max-pool-size:10}")
    private int healthCheckMaxPoolSize;

    @Value("${doramcu.async.event-core-pool-size:2}")
    private int eventCorePoolSize;

    @Value("${doramcu.async.event-max-pool-size:10}")
    private int eventMaxPoolSize;

    @Override
    public Executor getAsyncExecutor() {
        return corporationAiExecutor();
    }

    /**
     * Default executor for corporation AI management cycles.
     * Runs asynchronously to avoid blocking the main thread during turn scheduling.
     */
    @Bean("corporationAiExecutor")
    public Executor corporationAiExecutor() {
        ThreadPoolTaskExecutor executor = createExecutor("corporation-ai-", corePoolSize, maxPoolSize, queueCapacity);
        executor.initialize();
        return executor;
    }

    /**
     * Dedicated executor for Galaxy Overlord AI processing.
     * Provides per-galaxy isolation so one galaxy's heavy AI computation
     * doesn't block another galaxy's AI operations.
     */
    @Bean("galaxyAiExecutor")
    public Executor galaxyAiExecutor() {
        ThreadPoolTaskExecutor executor = createExecutor("galaxy-ai-", aiCorePoolSize, aiMaxPoolSize, queueCapacity);
        executor.initialize();
        return executor;
    }

    /**
     * Dedicated executor for galaxy health check assessments.
     * Allows scheduled health checks to run asynchronously without blocking
     * the main scheduler thread.
     */
    @Bean("galaxyHealthCheckExecutor")
    public Executor galaxyHealthCheckExecutor() {
        ThreadPoolTaskExecutor executor = createExecutor("galaxy-health-", healthCheckCorePoolSize, healthCheckMaxPoolSize, 50);
        executor.initialize();
        return executor;
    }

    /**
     * Dedicated executor for galaxy event publishing and SSE broadcasting.
     * Ensures event publishing doesn't block turn processing or AI operations.
     */
    @Bean("galaxyEventExecutor")
    public Executor galaxyEventExecutor() {
        ThreadPoolTaskExecutor executor = createExecutor("galaxy-event-", eventCorePoolSize, eventMaxPoolSize, 200);
        executor.initialize();
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) ->
                logger.error("Async error in {}: {}", method.getName(), ex.getMessage(), ex);
    }

    private ThreadPoolTaskExecutor createExecutor(String namePrefix, int coreSize, int maxSize, int queueSize) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(coreSize);
        executor.setMaxPoolSize(maxSize);
        executor.setQueueCapacity(queueSize);
        executor.setThreadNamePrefix(namePrefix);
        executor.setRejectedExecutionHandler((r, e) ->
                logger.warn("Async task rejected for {}: thread pool exhausted (core={}, max={}, queue={})",
                        namePrefix, coreSize, maxSize, queueSize));
        logger.info("Created async executor: {} (core={}, max={}, queue={})",
                namePrefix, coreSize, maxSize, queueSize);
        return executor;
    }
}
