package com.yisy.modules.doramcu.config;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Drives the early startup progress steps (1-3) that occur before
 * CommandLineRunners execute: process cleanup, Spring context init,
 * and database connection verification.
 */
@Component
public class StartupRunner {

    private static final Logger log = LoggerFactory.getLogger(StartupRunner.class);

    private final StartupProgressService startupProgress;
    private final EntityManager entityManager;

    public StartupRunner(StartupProgressService startupProgress, EntityManager entityManager) {
        this.startupProgress = startupProgress;
        this.entityManager = entityManager;
    }

    @PostConstruct
    public void init() {
        // Step 1: Process cleanup (handled by StartupProcessCleaner, mark as done)
        startupProgress.completeStep("process_cleanup", "Previous processes cleaned up");
        log.info("Startup step 1: Process cleanup complete");

        // Step 2: Spring context initialized
        startupProgress.completeStep("spring_context", "Spring context initialized");
        log.info("Startup step 2: Spring context ready");

        // Step 3: Database connection
        try {
            entityManager.createNativeQuery("SELECT 1").getSingleResult();
            startupProgress.completeStep("database_connection", "Database connection established");
            log.info("Startup step 3: Database connection verified");
        } catch (Exception e) {
            startupProgress.errorStep("database_connection", "Database connection failed: " + e.getMessage());
            log.error("Startup step 3: Database connection failed", e);
        }
    }
}
