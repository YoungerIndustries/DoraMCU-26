package com.yisy.modules.doramcu.service;

import com.yisy.modules.doramcu.event.GalaxyEventType;
import com.yisy.modules.doramcu.admin.repository.AiAdminOrderRepository;
import com.yisy.modules.doramcu.repository.AiCharacterRepository;
import com.yisy.modules.doramcu.repository.GalaxyRepository;
import com.yisy.modules.doramcu.repository.PlayerRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Centralized Micrometer metrics service for all galaxy monitoring data.
 * <p>
 * Exposes galaxy health scores, build progress, workforce counts, work order
 * status, event counts, and player population as Prometheus-compatible metrics
 * via the Micrometer registry. These metrics are scraped by Prometheus and
 * visualized in Grafana dashboards.
 * </p>
 * <p>
 * All metrics are prefixed with {@code dora.galaxy.} to namespace them from
 * other DoraMCU metrics (neuron, device, auth, etc.).
 * </p>
 */
@Service
public class GalaxyMetricsService {

    private static final Logger log = LoggerFactory.getLogger(GalaxyMetricsService.class);

    private final MeterRegistry meterRegistry;
    private final GalaxyRepository galaxyRepository;
    private final AiCharacterRepository aiCharacterRepository;
    private final AiAdminOrderRepository aiAdminOrderRepository;
    private final PlayerRepository playerRepository;

    // ---- Counters ----

    /** Total galaxies created. */
    private Counter galaxiesCreatedCounter;

    /** Total galaxy assessments completed. */
    private Counter assessmentsCompletedCounter;

    /** Total work orders created by galaxy. */
    private Counter workOrdersCreatedCounter;

    /** Total characters transitioned from Earth 2026 to galaxy. */
    private Counter charactersTransitionedCounter;

    /** Total AI agents deployed. */
    private Counter aiAgentsDeployedCounter;

    /** Total sectors generated during background builds. */
    private Counter sectorsGeneratedCounter;

    /** Total build failures. */
    private Counter buildFailuresCounter;

    /** Total builds completed successfully. */
    private Counter buildsCompletedCounter;

    /** Event counts by type. */
    private final Map<GalaxyEventType, Counter> eventCounters = new java.util.EnumMap<>(GalaxyEventType.class);

    /** Total backups created. */
    private Counter backupsCreatedCounter;

    /** Total backups restored. */
    private Counter backupsRestoredCounter;

    /** Tracks the last backup time per galaxy for the gauge. */
    private final Map<String, LocalDateTime> lastBackupTimeByGalaxy = new ConcurrentHashMap<>();

    /** Total wormhole pairs created. */
    private Counter wormholesCreatedCounter;

    /** Total wormhole travels. */
    private Counter wormholeTravelsCounter;

    // ---- Timers ----

    /** Timer for galaxy build duration. */
    private Timer buildDurationTimer;

    /** Timer for assessment duration. */
    private Timer assessmentDurationTimer;

    // ---- Gauges (registered via PostConstruct with suppliers) ----

    @PostConstruct
    public void init() {
        log.info("Initializing GalaxyMetricsService — registering Prometheus metrics");

        // ---- Counters ----
        galaxiesCreatedCounter = Counter.builder("dora.galaxy.created")
                .description("Total number of galaxies created")
                .register(meterRegistry);

        assessmentsCompletedCounter = Counter.builder("dora.galaxy.assessments.completed")
                .description("Total number of galaxy health assessments completed")
                .register(meterRegistry);

        workOrdersCreatedCounter = Counter.builder("dora.galaxy.workorders.created")
                .description("Total number of work orders created by Galaxy Overseer")
                .register(meterRegistry);

        charactersTransitionedCounter = Counter.builder("dora.galaxy.characters.transitioned")
                .description("Total number of characters transitioned from Earth 2026 to galaxy")
                .register(meterRegistry);

        aiAgentsDeployedCounter = Counter.builder("dora.galaxy.ai.agents.deployed")
                .description("Total number of AI agents deployed to galaxies")
                .register(meterRegistry);

        sectorsGeneratedCounter = Counter.builder("dora.galaxy.sectors.generated")
                .description("Total number of sectors generated during background builds")
                .register(meterRegistry);

        buildFailuresCounter = Counter.builder("dora.galaxy.build.failures")
                .description("Total number of galaxy build failures")
                .register(meterRegistry);

        buildsCompletedCounter = Counter.builder("dora.galaxy.build.completed")
                .description("Total number of galaxy builds completed successfully")
                .register(meterRegistry);

        backupsCreatedCounter = Counter.builder("dora.galaxy.backups.created")
                .description("Total number of galaxy backups created")
                .register(meterRegistry);

        backupsRestoredCounter = Counter.builder("dora.galaxy.backups.restored")
                .description("Total number of galaxy backups restored")
                .register(meterRegistry);

        wormholesCreatedCounter = Counter.builder("dora.galaxy.wormholes.created")
                .description("Total number of wormhole pairs created")
                .register(meterRegistry);

        wormholeTravelsCounter = Counter.builder("dora.galaxy.wormhole.travels")
                .description("Total number of wormhole travels")
                .register(meterRegistry);

        // Event counters by type
        for (GalaxyEventType eventType : GalaxyEventType.values()) {
            Counter eventCounter = Counter.builder("dora.galaxy.events.total")
                    .description("Total galaxy events by type")
                    .tag("type", eventType.name())
                    .register(meterRegistry);
            eventCounters.put(eventType, eventCounter);
        }

        // ---- Timers ----
        buildDurationTimer = Timer.builder("dora.galaxy.build.duration")
                .description("Duration of galaxy background builds")
                .register(meterRegistry);

        assessmentDurationTimer = Timer.builder("dora.galaxy.assessment.duration")
                .description("Duration of galaxy health assessments")
                .register(meterRegistry);

        // ---- Gauges (dynamic, updated on each scrape) ----

        // Total galaxy count
        Gauge.builder("dora.galaxy.count", galaxyRepository, r -> (double) r.count())
                .description("Total number of galaxies in the database")
                .register(meterRegistry);

        // Total AI characters count
        Gauge.builder("dora.galaxy.ai.characters.total", aiCharacterRepository, r -> (double) r.count())
                .description("Total number of AI characters (agents) across all galaxies")
                .register(meterRegistry);

        // Total players count
        Gauge.builder("dora.galaxy.players.total", playerRepository, r -> (double) r.count())
                .description("Total number of players across all galaxies")
                .register(meterRegistry);

        // Total work orders by status
        for (String status : List.of("PENDING", "RUNNING", "COMPLETED", "FAILED")) {
            final String finalStatus = status;
            Gauge.builder("dora.galaxy.workorders.count", aiAdminOrderRepository, (AiAdminOrderRepository repo) -> {
                long count = repo.findBySubmittedByContaining("GalaxyOverseer:").stream()
                        .filter(o -> finalStatus.equals(o.getStatus()))
                        .count();
                return (double) count;
            })
                    .tag("status", status)
                    .description("Number of work orders by status")
                    .register(meterRegistry);
        }

        // Total work orders overall
        Gauge.builder("dora.galaxy.workorders.total", aiAdminOrderRepository, (AiAdminOrderRepository repo) -> 
                (double) repo.findBySubmittedByContaining("GalaxyOverseer:").size())
                .description("Total number of work orders across all galaxies")
                .register(meterRegistry);

        log.info("GalaxyMetricsService initialized — all Prometheus metrics registered");
    }

    // ---- Counter increment methods ----

    /** Record a new galaxy creation. */
    public void incrementGalaxyCreated() {
        galaxiesCreatedCounter.increment();
    }

    /** Record a completed assessment. */
    public void incrementAssessmentCompleted() {
        assessmentsCompletedCounter.increment();
    }

    /** Record a work order creation. */
    public void incrementWorkOrderCreated() {
        workOrdersCreatedCounter.increment();
    }

    /** Record a character transition. */
    public void incrementCharacterTransitioned() {
        charactersTransitionedCounter.increment();
    }

    /** Record an AI agent deployment. */
    public void incrementAiAgentDeployed() {
        aiAgentsDeployedCounter.increment();
    }

    /** Record a sector generated during background build. */
    public void incrementSectorGenerated() {
        sectorsGeneratedCounter.increment();
    }

    /** Record a build failure. */
    public void incrementBuildFailure() {
        buildFailuresCounter.increment();
    }

    /** Record a build completion. */
    public void incrementBuildCompleted() {
        buildsCompletedCounter.increment();
    }

    /** Record an event by type. */
    public void incrementEvent(GalaxyEventType eventType) {
        Counter counter = eventCounters.get(eventType);
        if (counter != null) {
            counter.increment();
        }
    }

    /** Record a backup creation. */
    public void incrementBackupCreated() {
        backupsCreatedCounter.increment();
    }

    /** Record a backup restore. */
    public void incrementBackupRestored() {
        backupsRestoredCounter.increment();
    }

    /** Record a wormhole pair creation. */
    public void incrementWormholeCreated() {
        wormholesCreatedCounter.increment();
    }

    /** Record a wormhole travel. */
    public void incrementWormholeTravel() {
        wormholeTravelsCounter.increment();
    }

    /** Set the last backup time for a galaxy (used by the gauge). */
    public void setLastBackupTime(String galaxyName, LocalDateTime time) {
        lastBackupTimeByGalaxy.put(galaxyName, time);
        final LocalDateTime finalTime = time;
        Gauge.builder("dora.galaxy.last.backup.time", finalTime, t -> {
            // Returns epoch seconds since 2020-01-01T00:00:00 for Prometheus compatibility
            return (double) java.time.Duration.between(java.time.LocalDateTime.of(2020, 1, 1, 0, 0), t).getSeconds();
        })
                .tag("galaxy", galaxyName)
                .description("Last backup time for a galaxy (epoch seconds since 2020-01-01)")
                .register(meterRegistry);
    }

    // ---- Timer methods ----

    /**
     * Time a galaxy build operation.
     * @param runnable the build operation to time
     */
    public void timeBuild(Runnable runnable) {
        buildDurationTimer.record(runnable);
    }

    /**
     * Time an assessment operation.
     * @param runnable the assessment operation to time
     */
    public void timeAssessment(Runnable runnable) {
        assessmentDurationTimer.record(runnable);
    }

    // ---- Gauge update methods (for per-galaxy metrics) ----

    /**
     * Set the health score gauge for a specific galaxy.
     * Call this after each assessment to update the gauge value.
     */
    public void setHealthScore(String galaxyName, int score) {
        final double value = (double) score;
        Gauge.builder("dora.galaxy.health.score", value, v -> v)
                .tag("galaxy", galaxyName)
                .description("Health score of a galaxy (0-100)")
                .register(meterRegistry);
    }

    /**
     * Set the completion percentage gauge for a specific galaxy.
     */
    public void setCompletionPercent(String galaxyName, int percent) {
        final double value = (double) percent;
        Gauge.builder("dora.galaxy.completion.percent", value, v -> v)
                .tag("galaxy", galaxyName)
                .description("Completion percentage of a galaxy (0-100)")
                .register(meterRegistry);
    }

    /**
     * Set the system count gauge for a specific galaxy.
     */
    public void setSystemCount(String galaxyName, int count) {
        final double value = (double) count;
        Gauge.builder("dora.galaxy.systems.count", value, v -> v)
                .tag("galaxy", galaxyName)
                .description("Number of solar systems in a galaxy")
                .register(meterRegistry);
    }

    /**
     * Set the AI agent count gauge for a specific galaxy.
     */
    public void setAiAgentCount(String galaxyName, int count) {
        final double value = (double) count;
        Gauge.builder("dora.galaxy.ai.agents.count", value, v -> v)
                .tag("galaxy", galaxyName)
                .description("Number of AI agents in a galaxy")
                .register(meterRegistry);
    }

    /**
     * Set the AI agent count by role gauge for a specific galaxy.
     */
    public void setAiAgentCountByRole(String galaxyName, String role, int count) {
        final double value = (double) count;
        Gauge.builder("dora.galaxy.ai.agents.by.role", value, v -> v)
                .tag("galaxy", galaxyName)
                .tag("role", role)
                .description("Number of AI agents by role in a galaxy")
                .register(meterRegistry);
    }

    /**
     * Set the health status gauge for a specific galaxy (HEALTHY=1, WARNING=2, CRITICAL=3).
     */
    public void setHealthStatus(String galaxyName, String status) {
        int statusValue = switch (status.toUpperCase()) {
            case "HEALTHY" -> 1;
            case "WARNING" -> 2;
            case "CRITICAL" -> 3;
            default -> 0;
        };
        final double value = (double) statusValue;
        Gauge.builder("dora.galaxy.health.status", value, v -> v)
                .tag("galaxy", galaxyName)
                .tag("status", status)
                .description("Health status of a galaxy (1=HEALTHY, 2=WARNING, 3=CRITICAL)")
                .register(meterRegistry);
    }

    /**
     * Set the finding count gauge for a specific galaxy by severity.
     */
    public void setFindingCount(String galaxyName, String severity, int count) {
        final double value = (double) count;
        Gauge.builder("dora.galaxy.findings.count", value, v -> v)
                .tag("galaxy", galaxyName)
                .tag("severity", severity)
                .description("Number of assessment findings by severity in a galaxy")
                .register(meterRegistry);
    }

    /**
     * Set the player count gauge for a specific galaxy.
     */
    public void setPlayerCount(String galaxyName, int count) {
        final double value = (double) count;
        Gauge.builder("dora.galaxy.players.count", value, v -> v)
                .tag("galaxy", galaxyName)
                .description("Number of players in a galaxy")
                .register(meterRegistry);
    }

    /**
     * Set the work order count gauge for a specific galaxy by status.
     */
    public void setWorkOrderCount(String galaxyName, String status, int count) {
        final double value = (double) count;
        Gauge.builder("dora.galaxy.workorders.by.galaxy", value, v -> v)
                .tag("galaxy", galaxyName)
                .tag("status", status)
                .description("Number of work orders by galaxy and status")
                .register(meterRegistry);
    }

    /**
     * Get the last backup time for a galaxy.
     */
    public LocalDateTime getLastBackupTime(String galaxyName) {
        return lastBackupTimeByGalaxy.get(galaxyName);
    }

    // ---- Dependency getters for constructor injection ----

    public GalaxyMetricsService(MeterRegistry meterRegistry,
                                 GalaxyRepository galaxyRepository,
                                 AiCharacterRepository aiCharacterRepository,
                                 AiAdminOrderRepository aiAdminOrderRepository,
                                 PlayerRepository playerRepository) {
        this.meterRegistry = meterRegistry;
        this.galaxyRepository = galaxyRepository;
        this.aiCharacterRepository = aiCharacterRepository;
        this.aiAdminOrderRepository = aiAdminOrderRepository;
        this.playerRepository = playerRepository;
    }
}
