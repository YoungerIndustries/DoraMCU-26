package com.yisy.modules.doramcu.service.galaxy;
import com.yisy.modules.doramcu.model.misc.Planet;
import com.yisy.modules.doramcu.admin.model.AiAdminIncident;
import com.yisy.modules.doramcu.admin.service.AiAdminIncidentService;
import com.yisy.modules.doramcu.admin.service.AiAdminService;
import com.yisy.modules.doramcu.earth2026.model.E26GalaxyRules;
import com.yisy.modules.doramcu.earth2026.model.E26PlanetRules;
import com.yisy.modules.doramcu.earth2026.model.E26SystemRules;
import com.yisy.modules.doramcu.earth2026.service.Earth2026RulesDatabaseService;
import com.yisy.modules.doramcu.event.GalaxyEventService;
import com.yisy.modules.doramcu.event.GalaxyEventType;
import com.yisy.modules.doramcu.eve.model.AiCharacter;
import com.yisy.modules.doramcu.repository.ai.AiCharacterRepository;
import com.yisy.modules.doramcu.repository.galaxy.GalaxyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service that manages the Galaxy-level AI Overseer â€” the top AI Admin who oversees
 * the entire galaxy workforce of AI agents and monitors resources to detect overstaffing.
 * <p>
 * The Galaxy Overseer:
 * <ul>
 *   <li>Is created when a galaxy is initialized (seed data or dynamic creation)</li>
 *   <li>Oversees all planet-level AI Admins (overseers, leaders, governors) in the galaxy</li>
 *   <li>Monitors the total AI agent workforce count</li>
 *   <li>Reports when the workforce exceeds the recommended threshold</li>
 * </ul>
 * </p>
 */
@SuppressWarnings("ClassEscapesItsScope")
@Service
public class GalaxyOverseerService {

    private static final Logger log = LoggerFactory.getLogger(GalaxyOverseerService.class);

    /**
     * Recommended maximum AI agents per planet.
     * If a galaxy's total AI agent count exceeds this value multiplied by the number of planets,
     * the overseer flags the workforce as overstaffed.
     */
    public static final int RECOMMENDED_AI_AGENTS_PER_PLANET = 15;

    /**
     * Role name used for the galaxy-level overseer AI character.
     */
    public static final String ROLE_GALAXY_OVERSEER = "Galaxy Overseer";

    private final GalaxyRepository galaxyRepository;
    private final AiCharacterRepository aiCharacterRepository;
    private final Earth2026RulesDatabaseService rulesDatabase;
    private final AiAdminService aiAdminService;
    private final AiAdminIncidentService incidentService;
    private final GalaxyEventService galaxyEventService;
    private final GalaxyMetricsService galaxyMetricsService;
    private final java.util.concurrent.Executor galaxyHealthCheckExecutor;

    // Tracks previous health scores for threshold-crossing detection
    private final Map<Long, Integer> previousHealthScores = new ConcurrentHashMap<>();

    public GalaxyOverseerService(GalaxyRepository galaxyRepository,
                                   AiCharacterRepository aiCharacterRepository,
                                   Earth2026RulesDatabaseService rulesDatabase,
                                   @Lazy AiAdminService aiAdminService,
                                   @Lazy AiAdminIncidentService incidentService,
                                   GalaxyEventService galaxyEventService,
                                   GalaxyMetricsService galaxyMetricsService,
                                   @org.springframework.beans.factory.annotation.Qualifier("galaxyHealthCheckExecutor")
                                   java.util.concurrent.Executor galaxyHealthCheckExecutor) {
        this.galaxyRepository = galaxyRepository;
        this.aiCharacterRepository = aiCharacterRepository;
        this.rulesDatabase = rulesDatabase;
        this.aiAdminService = aiAdminService;
        this.incidentService = incidentService;
        this.galaxyEventService = galaxyEventService;
        this.galaxyMetricsService = galaxyMetricsService;
        this.galaxyHealthCheckExecutor = galaxyHealthCheckExecutor;
    }

    /**
     * Creates or reuses a Galaxy Overseer AI Admin for the given galaxy.
     * <p>
     * If the galaxy already has an overseer assigned, returns the existing one.
     * Otherwise, attempts to reuse an existing AI character with the "Galaxy Overseer" role.
     * If none exists, creates a new Galaxy Overseer AI Admin.
     * </p>
     *
     * @param galaxy the galaxy to assign an overseer to
     * @return the assigned Galaxy Overseer AI character
     */
    @Transactional
    public AiCharacter createOrGetGalaxyOverseer(Galaxy galaxy) {
        // If already assigned, return existing
        if (galaxy.getGalaxyOverseerId() != null) {
            Optional<AiCharacter> existing = aiCharacterRepository.findById(galaxy.getGalaxyOverseerId());
            if (existing.isPresent()) {
                log.info("Galaxy '{}' already has Galaxy Overseer: {}", galaxy.getName(), existing.get().getName());
                return existing.get();
            }
        }

        // Try to reuse an existing Galaxy Overseer from the pool
        List<AiCharacter> pool = aiCharacterRepository.findByRoleAndActiveTrueOrderByTotalExperienceDesc(ROLE_GALAXY_OVERSEER);
        if (!pool.isEmpty()) {
            AiCharacter overseer = pool.get(0);
            overseer.setEmpireName(galaxy.getName());
            overseer.setDescription("Galaxy Overseer for " + galaxy.getName() +
                    " â€” oversees the entire AI workforce across all planets in the galaxy.");
            overseer.setAiAdmin(true);
            overseer = aiCharacterRepository.save(overseer);
            galaxy.setGalaxyOverseerId(overseer.getId());
            galaxyRepository.save(galaxy);
            log.info("Galaxy '{}' assigned Galaxy Overseer (reused with experience): {} (xp={})",
                    galaxy.getName(), overseer.getName(), overseer.getTotalExperience());
            return overseer;
        }

        // Create a new Galaxy Overseer
        AiCharacter overseer = new AiCharacter("Galaxy Overseer " + galaxy.getName(), ROLE_GALAXY_OVERSEER, galaxy.getName());
        overseer.setActive(true);
        overseer.setAiAdmin(true);
        overseer.setCanPromoteToCoServer(true);
        overseer.setDescription("Galaxy Overseer for " + galaxy.getName() +
                " â€” oversees the entire AI workforce across all planets in the galaxy.");
        overseer.setHirable(false);
        overseer.setDailySalary(1000);
        overseer = aiCharacterRepository.save(overseer);

        galaxy.setGalaxyOverseerId(overseer.getId());
        galaxyRepository.save(galaxy);

        log.info("Galaxy '{}' assigned new Galaxy Overseer: {} (id={})", galaxy.getName(), overseer.getName(), overseer.getId());
        galaxyMetricsService.incrementAiAgentDeployed();
        return overseer;
    }

    /**
     * Gets the Galaxy Overseer for a galaxy by galaxy ID.
     *
     * @param galaxyId the galaxy ID
     * @return the Galaxy Overseer if one is assigned
     */
    @Transactional(readOnly = true)
    public Optional<AiCharacter> getGalaxyOverseer(Long galaxyId) {
        Optional<Galaxy> galaxy = galaxyRepository.findById(galaxyId);
        if (galaxy.isPresent() && galaxy.get().getGalaxyOverseerId() != null) {
            return aiCharacterRepository.findById(galaxy.get().getGalaxyOverseerId());
        }
        return Optional.empty();
    }

    /**
     * Gets the Galaxy Overseer for a galaxy by galaxy name.
     *
     * @param galaxyName the galaxy name
     * @return the Galaxy Overseer if one is assigned
     */
    @Transactional(readOnly = true)
    public Optional<AiCharacter> getGalaxyOverseer(String galaxyName) {
        Optional<Galaxy> galaxy = galaxyRepository.findByName(galaxyName);
        if (galaxy.isPresent() && galaxy.get().getGalaxyOverseerId() != null) {
            return aiCharacterRepository.findById(galaxy.get().getGalaxyOverseerId());
        }
        return Optional.empty();
    }

    /**
     * Monitors the AI workforce in a galaxy and returns a workforce report.
     * <p>
     * The report includes:
     * <ul>
     *   <li>Total AI admin count in the galaxy</li>
     *   <li>Breakdown by role (Galaxy Overseer, Overseer, Leader, Governor)</li>
     *   <li>Whether the workforce is overstaffed</li>
     *   <li>Recommended actions if overstaffed</li>
     * </ul>
     * </p>
     *
     * @param galaxyId the galaxy ID to monitor
     * @return a workforce report for the galaxy
     */
    @Transactional(readOnly = true)
    public WorkforceReport monitorWorkforce(Long galaxyId) {
        Optional<Galaxy> galaxyOpt = galaxyRepository.findById(galaxyId);
        if (galaxyOpt.isEmpty()) {
            throw new IllegalArgumentException("Galaxy not found: " + galaxyId);
        }
        Galaxy galaxy = galaxyOpt.get();

        // Count all AI admins associated with this galaxy by empire name
        List<AiCharacter> allAiAdmins = aiCharacterRepository.findByAiAdminTrueAndActiveTrueAndEmpireName(galaxy.getName());
        int totalAiAgents = allAiAdmins.size();

        // Break down by role
        int galaxyOverseerCount = 0;
        int planetOverseerCount = 0;
        int leaderCount = 0;
        int governorCount = 0;
        int otherCount = 0;

        for (AiCharacter ai : allAiAdmins) {
            String role = ai.getRole();
            switch (role) {
                case ROLE_GALAXY_OVERSEER:
                    galaxyOverseerCount++;
                    break;
                case "Overseer":
                    planetOverseerCount++;
                    break;
                case "Leader":
                    leaderCount++;
                    break;
                case "Governor":
                    governorCount++;
                    break;
                default:
                    otherCount++;
                    break;
            }
        }

        // Calculate total experience of workforce
        long totalExperience = allAiAdmins.stream().mapToLong(AiCharacter::getTotalExperience).sum();
        double averageExperience = totalAiAgents > 0 ? (double) totalExperience / totalAiAgents : 0;

        // Determine if overstaffed
        // Estimate number of planets from solar systems
        int estimatedPlanets = galaxy.getSolarSystems() != null ? galaxy.getSolarSystems().size() : 1;
        int recommendedMax = estimatedPlanets * RECOMMENDED_AI_AGENTS_PER_PLANET;
        boolean isOverstaffed = totalAiAgents > recommendedMax;

        WorkforceReport report = new WorkforceReport();
        report.setGalaxyId(galaxyId);
        report.setGalaxyName(galaxy.getName());
        report.setTotalAiAgents(totalAiAgents);
        report.setGalaxyOverseerCount(galaxyOverseerCount);
        report.setPlanetOverseerCount(planetOverseerCount);
        report.setLeaderCount(leaderCount);
        report.setGovernorCount(governorCount);
        report.setOtherCount(otherCount);
        report.setEstimatedPlanets(estimatedPlanets);
        report.setRecommendedMaxAgents(recommendedMax);
        report.setIsOverstaffed(isOverstaffed);
        report.setTotalWorkforceExperience((int) totalExperience);
        report.setAverageExperience(averageExperience);

        if (isOverstaffed) {
            int excess = totalAiAgents - recommendedMax;
            report.setRecommendation("Workforce exceeds recommended maximum by " + excess +
                    " AI agents. Consider consolidating roles or deactivating redundant AI admins.");
        } else {
            int remaining = recommendedMax - totalAiAgents;
            report.setRecommendation("Workforce is within recommended limits. " + remaining +
                    " additional AI agent slots available.");
        }

        log.info("Workforce report for galaxy '{}': {} AI agents (overstaffed={})",
                galaxy.getName(), totalAiAgents, isOverstaffed);
        return report;
    }

    /**
     * Assesses a galaxy against the Earth 2026 rules database to identify gaps and needs.
     * <p>
     * The assessment checks:
     * <ul>
     *   <li>Solar system count vs rules-based expected count</li>
     *   <li>AI staffing levels vs recommended thresholds</li>
     *   <li>Galaxy metadata completeness (game type, difficulty, theme)</li>
     *   <li>Hyperlane density vs rules-based expectations</li>
     * </ul>
     * </p>
     *
     * @param galaxy the galaxy to assess
     * @return an assessment report detailing findings and recommendations
     */
    @Transactional(readOnly = true)
    public GalaxyAssessment assessGalaxyNeeds(Galaxy galaxy) {
        // Re-fetch from repository to attach to current session (avoids LazyInitializationException)
        Galaxy managed = galaxyRepository.findById(galaxy.getId())
                .orElseThrow(() -> new IllegalArgumentException("Galaxy not found: " + galaxy.getId()));
        GalaxyAssessment assessment = new GalaxyAssessment();
        assessment.setGalaxyId(managed.getId());
        assessment.setGalaxyName(managed.getName());

        E26GalaxyRules galaxyRules = rulesDatabase.getGalaxyRules();
        E26SystemRules systemRules = rulesDatabase.getSystemRules();
        E26PlanetRules planetRules = rulesDatabase.getPlanetRules();

        // Check solar system count against rules
        int systemCount = managed.getSolarSystems() != null ? managed.getSolarSystems().size() : 0;
        int maxSystemsPerSector = galaxyRules.getMaxSystemsPerSector();
        int maxSectors = galaxyRules.getMaxSectorsPerGalaxy();
        int expectedMaxSystems = maxSectors * maxSystemsPerSector;

        assessment.setSolarSystemCount(systemCount);
        assessment.setExpectedMaxSystems(expectedMaxSystems);

        if (systemCount == 0) {
            assessment.addFinding(GalaxyAssessment.Severity.CRITICAL,
                    "No solar systems generated â€” galaxy is empty. Players cannot join.");
            assessment.addRecommendation("Generate Earth hub sector immediately â€” this is the character creation hub.");
        } else if (systemCount < expectedMaxSystems / 4) {
            assessment.addFinding(GalaxyAssessment.Severity.HIGH,
                    "Only " + systemCount + " of " + expectedMaxSystems + " expected solar systems generated (" +
                    String.format("%.1f", (double) systemCount / expectedMaxSystems * 100) + "% complete).");
            assessment.addRecommendation("Continue generating sectors â€” galaxy is less than 25% complete.");
        } else if (systemCount < expectedMaxSystems) {
            assessment.addFinding(GalaxyAssessment.Severity.MEDIUM,
                    "Galaxy partially generated: " + systemCount + " of " + expectedMaxSystems + " systems (" +
                    String.format("%.1f", (double) systemCount / expectedMaxSystems * 100) + "% complete).");
            assessment.addRecommendation("Continue background generation of remaining sectors.");
        } else {
            assessment.addFinding(GalaxyAssessment.Severity.INFO,
                    "Galaxy fully generated with " + systemCount + " solar systems.");
        }

        // Check AI staffing
        WorkforceReport workforce = monitorWorkforce(managed.getId());
        assessment.setAiAgentCount(workforce.getTotalAiAgents());
        assessment.setRecommendedAiAgents(workforce.getRecommendedMaxAgents());

        if (workforce.getTotalAiAgents() == 0) {
            assessment.addFinding(GalaxyAssessment.Severity.HIGH,
                    "No AI agents deployed â€” galaxy has no workforce.");
            assessment.addRecommendation("Deploy Galaxy Overseer and initial AI agent team.");
        } else if (workforce.isOverstaffed()) {
            assessment.addFinding(GalaxyAssessment.Severity.MEDIUM,
                    "Workforce overstaffed: " + workforce.getTotalAiAgents() + " agents vs " +
                    workforce.getRecommendedMaxAgents() + " recommended.");
            assessment.addRecommendation("Consolidate or deactivate redundant AI agents.");
        }

        // Check galaxy metadata completeness
        boolean hasGameType = managed.getGameType() != null && !managed.getGameType().isBlank();
        boolean hasDifficulty = managed.getDifficultyLevel() != null && !managed.getDifficultyLevel().isBlank();
        boolean hasTheme = managed.getTheme() != null && !managed.getTheme().isBlank();
        boolean hasDescription = managed.getDescription() != null && !managed.getDescription().isBlank();

        int missingMetadata = 0;
        if (!hasGameType) missingMetadata++;
        if (!hasDifficulty) missingMetadata++;
        if (!hasTheme) missingMetadata++;
        if (!hasDescription) missingMetadata++;

        assessment.setMetadataComplete(missingMetadata == 0);
        if (missingMetadata > 0) {
            assessment.addFinding(GalaxyAssessment.Severity.LOW,
                    "Galaxy metadata incomplete: " + missingMetadata + " of 4 fields missing.");
            assessment.addRecommendation("Complete galaxy metadata (game type, difficulty, theme, description).");
        }

        // Check hyperlane density
        double hyperlaneDensity = managed.getHyperlaneDensity();
        assessment.setHyperlaneDensity(hyperlaneDensity);
        if (hyperlaneDensity <= 0) {
            assessment.addFinding(GalaxyAssessment.Severity.MEDIUM,
                    "Hyperlane density is 0 â€” no travel routes between systems.");
            assessment.addRecommendation("Generate hyperlanes to connect solar systems.");
        }

        // Check rules database availability
        if (systemRules.getStarTypes() == null || systemRules.getStarTypes().isEmpty()) {
            assessment.addFinding(GalaxyAssessment.Severity.LOW,
                    "Star type rules not loaded â€” celestial body generation may use defaults.");
        }
        if (planetRules.getGovernmentTypes() == null || planetRules.getGovernmentTypes().isEmpty()) {
            assessment.addFinding(GalaxyAssessment.Severity.LOW,
                    "Government type rules not loaded â€” planet politics may use defaults.");
        }

        // Calculate overall health score (0-100)
        int healthScore = 100;
        for (GalaxyAssessment.Finding finding : assessment.getFindings()) {
            switch (finding.severity) {
                case CRITICAL: healthScore -= 40; break;
                case HIGH: healthScore -= 20; break;
                case MEDIUM: healthScore -= 10; break;
                case LOW: healthScore -= 5; break;
                case INFO: break;
            }
        }
        assessment.setHealthScore(Math.max(0, healthScore));

        log.info("Galaxy assessment for '{}': health={}, findings={}",
                managed.getName(), assessment.getHealthScore(), assessment.getFindings().size());

        // Check for health threshold crossing
        Integer previousScore = previousHealthScores.get(managed.getId());
        if (previousScore != null) {
            String previousStatus = getHealthStatus(previousScore);
            String currentStatus = getHealthStatus(assessment.getHealthScore());
            if (!previousStatus.equals(currentStatus)) {
                galaxyEventService.publish(GalaxyEventType.HEALTH_THRESHOLD_CROSSED, managed.getName(), managed.getId(),
                        "healthScore", String.valueOf(assessment.getHealthScore()),
                        "previousScore", String.valueOf(previousScore),
                        "thresholdCrossed", currentStatus);
                log.info("Galaxy '{}' health threshold crossed: {} ({}) -> {} ({})",
                        managed.getName(), previousScore, previousStatus, assessment.getHealthScore(), currentStatus);
            }
        }
        previousHealthScores.put(managed.getId(), assessment.getHealthScore());

        // Publish ASSESSMENT_COMPLETED event
        galaxyEventService.publish(GalaxyEventType.ASSESSMENT_COMPLETED, managed.getName(), managed.getId(),
                "healthScore", String.valueOf(assessment.getHealthScore()),
                "findingsCount", String.valueOf(assessment.getFindings().size()),
                "recommendationsCount", String.valueOf(assessment.getRecommendations().size()));

        // Publish Prometheus metrics
        galaxyMetricsService.incrementAssessmentCompleted();
        galaxyMetricsService.setHealthScore(galaxy.getName(), assessment.getHealthScore());
        galaxyMetricsService.setHealthStatus(galaxy.getName(), getHealthStatus(assessment.getHealthScore()));
        galaxyMetricsService.setSystemCount(galaxy.getName(), assessment.getSolarSystemCount());
        galaxyMetricsService.setAiAgentCount(galaxy.getName(), assessment.getAiAgentCount());

        // Count findings by severity
        long criticalCount = assessment.getFindings().stream().filter(f -> f.severity == GalaxyAssessment.Severity.CRITICAL).count();
        long highCount = assessment.getFindings().stream().filter(f -> f.severity == GalaxyAssessment.Severity.HIGH).count();
        long mediumCount = assessment.getFindings().stream().filter(f -> f.severity == GalaxyAssessment.Severity.MEDIUM).count();
        long lowCount = assessment.getFindings().stream().filter(f -> f.severity == GalaxyAssessment.Severity.LOW).count();
        galaxyMetricsService.setFindingCount(galaxy.getName(), "CRITICAL", (int) criticalCount);
        galaxyMetricsService.setFindingCount(galaxy.getName(), "HIGH", (int) highCount);
        galaxyMetricsService.setFindingCount(galaxy.getName(), "MEDIUM", (int) mediumCount);
        galaxyMetricsService.setFindingCount(galaxy.getName(), "LOW", (int) lowCount);

        return assessment;
    }

    /**
     * Derives health status from a score.
     */
    private String getHealthStatus(int healthScore) {
        if (healthScore >= 80) {
            return "HEALTHY";
        } else if (healthScore >= 50) {
            return "WARNING";
        } else {
            return "CRITICAL";
        }
    }

    /**
     * Creates work orders for AI Admins based on the galaxy assessment.
     * <p>
     * Prioritizes orders: CRITICAL > HIGH > MEDIUM > LOW.
     * Each finding generates a focused work order that the AI Admin can process.
     * </p>
     *
     * @param galaxy the galaxy to create work orders for
     * @return list of created order texts
     */
    @Transactional
    public List<String> createWorkOrdersForGalaxy(Galaxy galaxy) {
        GalaxyAssessment assessment = assessGalaxyNeeds(galaxy);
        List<String> ordersCreated = new ArrayList<>();

        if (assessment.getFindings().isEmpty()) {
            log.info("Galaxy '{}' is healthy â€” no work orders needed.", galaxy.getName());
            return ordersCreated;
        }

        for (GalaxyAssessment.Finding finding : assessment.getFindings()) {
            if (finding.severity == GalaxyAssessment.Severity.INFO) {
                continue; // Skip informational findings
            }

            String orderText = buildOrderText(galaxy.getName(), finding);
            aiAdminService.submitOrder("GalaxyOverseer:" + galaxy.getName(), orderText);
            ordersCreated.add(orderText);
            galaxyMetricsService.incrementWorkOrderCreated();

            // Publish WORK_ORDER_CREATED event
            galaxyEventService.publish(GalaxyEventType.WORK_ORDER_CREATED, galaxy.getName(), galaxy.getId(),
                    "orderText", orderText,
                    "category", finding.severity.name(),
                    "severity", finding.severity.name());

            // Create incident for CRITICAL and HIGH severity findings
            if (finding.severity == GalaxyAssessment.Severity.CRITICAL ||
                    finding.severity == GalaxyAssessment.Severity.HIGH) {
                try {
                    AiAdminIncident.Severity incidentSeverity =
                            finding.severity == GalaxyAssessment.Severity.CRITICAL
                                    ? AiAdminIncident.Severity.CRITICAL
                                    : AiAdminIncident.Severity.HIGH;
                    incidentService.createIncident(
                            AiAdminIncident.IncidentType.HEALTH,
                            incidentSeverity,
                            orderText,
                            finding.description,
                            galaxy.getName()
                    );
                } catch (Exception e) {
                    log.warn("Failed to create incident for finding in galaxy '{}': {}",
                            galaxy.getName(), e.getMessage());
                }
            }
        }

        log.info("Created {} work orders for galaxy '{}'", ordersCreated.size(), galaxy.getName());
        return ordersCreated;
    }

    /**
     * Builds a natural language work order text from a galaxy assessment finding.
     * Prioritizes the order text based on severity level.
     */
    private String buildOrderText(String galaxyName, GalaxyAssessment.Finding finding) {
        String priorityPrefix;
        switch (finding.severity) {
            case CRITICAL:
                priorityPrefix = "[CRITICAL]";
                break;
            case HIGH:
                priorityPrefix = "[HIGH]";
                break;
            case MEDIUM:
                priorityPrefix = "[MEDIUM]";
                break;
            case LOW:
                priorityPrefix = "[LOW]";
                break;
            default:
                priorityPrefix = "[INFO]";
                break;
        }

        return priorityPrefix + " Galaxy '" + galaxyName + "': " + finding.description +
                ". Action: " + finding.recommendedAction;
    }

    /**
     * Scheduled health check that runs periodically to assess all galaxies
     * and create work orders for any that need attention.
     * <p>
     * Runs every 5 minutes. Each galaxy is assessed asynchronously using the
     * dedicated health check executor, so one galaxy's assessment doesn't block
     * others. Skips galaxies that are fully healthy (health score >= 90).
     * </p>
     */
    @Scheduled(fixedRate = 300000)
    public void scheduledGalaxyHealthCheck() {
        List<Galaxy> galaxies = galaxyRepository.findAll();
        if (galaxies.isEmpty()) {
            log.debug("No galaxies to assess");
            return;
        }

        log.info("Starting async health check for {} galaxies", galaxies.size());

        // Process each galaxy asynchronously using the dedicated health check executor
        List<CompletableFuture<HealthCheckResult>> futures = galaxies.stream()
                .map(galaxy -> CompletableFuture.supplyAsync(
                        () -> assessSingleGalaxy(galaxy),
                        galaxyHealthCheckExecutor
                ))
                .toList();

        // Wait for all assessments to complete and aggregate results
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
        );

        allFutures.thenRun(() -> {
            int assessed = 0;
            int ordersCreated = 0;
            int failed = 0;

            for (CompletableFuture<HealthCheckResult> future : futures) {
                try {
                    HealthCheckResult result = future.get();
                    assessed += result.assessed;
                    ordersCreated += result.ordersCreated;
                } catch (Exception e) {
                    log.warn("Health check assessment failed: {}", e.getMessage());
                    failed++;
                }
            }

            log.info("Async galaxy health check complete: assessed {} galaxies, created {} orders, {} failed",
                    assessed, ordersCreated, failed);
        }).exceptionally(ex -> {
            log.error("Health check aggregation failed: {}", ex.getMessage(), ex);
            return null;
        });
    }

    /**
     * Assesses a single galaxy and creates work orders if needed.
     * Runs asynchronously via the galaxyHealthCheckExecutor.
     */
    @Async("galaxyHealthCheckExecutor")
    public HealthCheckResult assessSingleGalaxy(Galaxy galaxy) {
        try {
            GalaxyAssessment assessment = assessGalaxyNeeds(galaxy);

            if (assessment.getHealthScore() < 90) {
                List<String> newOrders = createWorkOrdersForGalaxy(galaxy);
                log.debug("Galaxy '{}' health score: {}, created {} work orders",
                        galaxy.getName(), assessment.getHealthScore(), newOrders.size());
                return new HealthCheckResult(1, newOrders.size());
            } else {
                log.debug("Galaxy '{}' is healthy (score: {})", galaxy.getName(), assessment.getHealthScore());
                return new HealthCheckResult(1, 0);
            }
        } catch (Exception e) {
            log.warn("Failed to assess galaxy '{}': {}", galaxy.getName(), e.getMessage());
            throw e;
        }
    }

    /**
     * Simple result holder for async health check operations.
     */
    static class HealthCheckResult {
        final int assessed;
        final int ordersCreated;

        HealthCheckResult(int assessed, int ordersCreated) {
            this.assessed = assessed;
            this.ordersCreated = ordersCreated;
        }
    }

    /**
     * Gets the rules database service for access to galaxy rules.
     */
    public Earth2026RulesDatabaseService getRulesDatabase() {
        return rulesDatabase;
    }

    /**
     * DTO holding the workforce monitoring report for a galaxy.
     */
    public static class WorkforceReport {
        private Long galaxyId;
        private String galaxyName;
        private int totalAiAgents;
        private int galaxyOverseerCount;
        private int planetOverseerCount;
        private int leaderCount;
        private int governorCount;
        private int otherCount;
        private int estimatedPlanets;
        private int recommendedMaxAgents;
        private boolean isOverstaffed;
        private int totalWorkforceExperience;
        private double averageExperience;
        private String recommendation;

        public Long getGalaxyId() {
            return galaxyId;
        }

        public void setGalaxyId(Long galaxyId) {
            this.galaxyId = galaxyId;
        }

        public String getGalaxyName() {
            return galaxyName;
        }

        public void setGalaxyName(String galaxyName) {
            this.galaxyName = galaxyName;
        }

        public int getTotalAiAgents() {
            return totalAiAgents;
        }

        public void setTotalAiAgents(int totalAiAgents) {
            this.totalAiAgents = totalAiAgents;
        }

        public int getGalaxyOverseerCount() {
            return galaxyOverseerCount;
        }

        public void setGalaxyOverseerCount(int galaxyOverseerCount) {
            this.galaxyOverseerCount = galaxyOverseerCount;
        }

        public int getPlanetOverseerCount() {
            return planetOverseerCount;
        }

        public void setPlanetOverseerCount(int planetOverseerCount) {
            this.planetOverseerCount = planetOverseerCount;
        }

        public int getLeaderCount() {
            return leaderCount;
        }

        public void setLeaderCount(int leaderCount) {
            this.leaderCount = leaderCount;
        }

        public int getGovernorCount() {
            return governorCount;
        }

        public void setGovernorCount(int governorCount) {
            this.governorCount = governorCount;
        }

        public int getOtherCount() {
            return otherCount;
        }

        public void setOtherCount(int otherCount) {
            this.otherCount = otherCount;
        }

        public int getEstimatedPlanets() {
            return estimatedPlanets;
        }

        public void setEstimatedPlanets(int estimatedPlanets) {
            this.estimatedPlanets = estimatedPlanets;
        }

        public int getRecommendedMaxAgents() {
            return recommendedMaxAgents;
        }

        public void setRecommendedMaxAgents(int recommendedMaxAgents) {
            this.recommendedMaxAgents = recommendedMaxAgents;
        }

        public boolean isOverstaffed() {
            return isOverstaffed;
        }

        public void setIsOverstaffed(boolean isOverstaffed) {
            this.isOverstaffed = isOverstaffed;
        }

        public int getTotalWorkforceExperience() {
            return totalWorkforceExperience;
        }

        public void setTotalWorkforceExperience(int totalWorkforceExperience) {
            this.totalWorkforceExperience = totalWorkforceExperience;
        }

        public double getAverageExperience() {
            return averageExperience;
        }

        public void setAverageExperience(double averageExperience) {
            this.averageExperience = averageExperience;
        }

        public String getRecommendation() {
            return recommendation;
        }

        public void setRecommendation(String recommendation) {
            this.recommendation = recommendation;
        }

        @Override
        public String toString() {
            return "WorkforceReport{galaxy='" + galaxyName + "', totalAgents=" + totalAiAgents +
                    ", overstaffed=" + isOverstaffed + ", recommendation='" + recommendation + "'}";
        }
    }

    /**
     * DTO holding the galaxy assessment result â€” the AI Overseer's expert evaluation
     * of a galaxy's state against the Earth 2026 rules database.
     */
    public static class GalaxyAssessment {
        private Long galaxyId;
        private String galaxyName;
        private int healthScore;
        private int solarSystemCount;
        private int expectedMaxSystems;
        private int aiAgentCount;
        private int recommendedAiAgents;
        private boolean metadataComplete;
        private double hyperlaneDensity;
        private List<Finding> findings = new ArrayList<>();
        private List<String> recommendations = new ArrayList<>();

        public Long getGalaxyId() { return galaxyId; }
        public void setGalaxyId(Long galaxyId) { this.galaxyId = galaxyId; }

        public String getGalaxyName() { return galaxyName; }
        public void setGalaxyName(String galaxyName) { this.galaxyName = galaxyName; }

        public int getHealthScore() { return healthScore; }
        public void setHealthScore(int healthScore) { this.healthScore = healthScore; }

        public int getSolarSystemCount() { return solarSystemCount; }
        public void setSolarSystemCount(int solarSystemCount) { this.solarSystemCount = solarSystemCount; }

        public int getExpectedMaxSystems() { return expectedMaxSystems; }
        public void setExpectedMaxSystems(int expectedMaxSystems) { this.expectedMaxSystems = expectedMaxSystems; }

        public int getAiAgentCount() { return aiAgentCount; }
        public void setAiAgentCount(int aiAgentCount) { this.aiAgentCount = aiAgentCount; }

        public int getRecommendedAiAgents() { return recommendedAiAgents; }
        public void setRecommendedAiAgents(int recommendedAiAgents) { this.recommendedAiAgents = recommendedAiAgents; }

        public boolean isMetadataComplete() { return metadataComplete; }
        public void setMetadataComplete(boolean metadataComplete) { this.metadataComplete = metadataComplete; }

        public double getHyperlaneDensity() { return hyperlaneDensity; }
        public void setHyperlaneDensity(double hyperlaneDensity) { this.hyperlaneDensity = hyperlaneDensity; }

        public List<Finding> getFindings() { return findings; }
        public void setFindings(List<Finding> findings) { this.findings = findings; }

        public List<String> getRecommendations() { return recommendations; }
        public void setRecommendations(List<String> recommendations) { this.recommendations = recommendations; }

        /**
         * Add a finding with a recommended action.
         */
        public void addFinding(Severity severity, String description) {
            findings.add(new Finding(severity, description, ""));
        }

        /**
         * Add a finding with a recommended action.
         */
        public void addFindingWithAction(Severity severity, String description, String action) {
            findings.add(new Finding(severity, description, action));
        }

        /**
         * Add a standalone recommendation.
         */
        public void addRecommendation(String recommendation) {
            recommendations.add(recommendation);
            // Also attach the recommendation to the last finding if any
            if (!findings.isEmpty()) {
                findings.get(findings.size() - 1).recommendedAction = recommendation;
            }
        }

        @Override
        public String toString() {
            return "GalaxyAssessment{galaxy='" + galaxyName + "', health=" + healthScore +
                    ", systems=" + solarSystemCount + "/" + expectedMaxSystems +
                    ", findings=" + findings.size() + "}";
        }

        /**
         * Severity levels for assessment findings.
         */
        public enum Severity {
            CRITICAL, HIGH, MEDIUM, LOW, INFO
        }

        /**
         * A single finding from the galaxy assessment.
         */
        public static class Finding {
            final Severity severity;
            final String description;
            String recommendedAction;

            Finding(Severity severity, String description, String recommendedAction) {
                this.severity = severity;
                this.description = description;
                this.recommendedAction = recommendedAction;
            }

            public Severity getSeverity() { return severity; }
            public String getDescription() { return description; }
            public String getRecommendedAction() { return recommendedAction; }

            @Override
            public String toString() {
                return String.format("[%s] %s -> %s", severity, description, recommendedAction);
            }
        }
    }

    /**
     * Returns the campaign theme for the given galaxy.
     */
    public String campaignTheme(String galaxyName) {
        return galaxyRepository.findByName(galaxyName)
                .map(Galaxy::getTheme)
                .orElse("default");
    }

    /**
     * Loads themes for the given galaxy.
     */
    public List<String> loadThemes(String galaxyName) {
        return galaxyRepository.findByName(galaxyName)
                .map(g -> List.of(g.getTheme() != null ? g.getTheme() : "default"))
                .orElse(List.of("default"));
    }

    /**
     * Checks if the 4X layer is enabled.
     */
    public boolean isEnable4xLayer() {
        return true;
    }

    /**
     * Returns the 4X unlock state.
     */
    public Map<String, Boolean> getFourxUnlockState() {
        return Map.of("exploration", true, "expansion", true, "exploitation", true, "extermination", true);
    }

    /**
     * Declares war between empires.
     */
    public void declareWar(Integer empireId, String targetEmpireName, double aggressionLevel) {
        log.info("Empire {} declares war on {} with aggression level {}", empireId, targetEmpireName, aggressionLevel);
    }

    /**
     * Forms an alliance between empires.
     */
    public void formAlliance(Integer empireId, String partnerEmpireName, double trustLevel) {
        log.info("Empire {} forms alliance with {} with trust level {}", empireId, partnerEmpireName, trustLevel);
    }

    /**
     * Expands a colony.
     */
    public void expandColony(String colonyName, double investment) {
        log.info("Expanding colony {} with investment {}", colonyName, investment);
    }

    /**
     * Deploys an AI agent to a galaxy.
     * <p>
     * Creates a new AI character assigned to the specified galaxy and publishes
     * an AI_AGENT_DEPLOYED event so the admin dashboard can track the deployment.
     * </p>
     *
     * @param galaxyName the name of the galaxy to deploy to
     * @param agentName  the name of the AI agent
     * @param role       the role of the agent (e.g., "Galaxy Overseer", "Planet Overseer")
     * @return the created AI character
     */
    public AiCharacter deployAiAgent(String galaxyName, String agentName, String role) {
        log.info("Deploying AI agent '{}' (role: {}) to galaxy '{}'", agentName, role, galaxyName);

        Galaxy galaxy = galaxyRepository.findByName(galaxyName)
                .orElseThrow(() -> new IllegalArgumentException("Galaxy not found: " + galaxyName));

        AiCharacter agent = new AiCharacter();
        agent.setName(agentName);
        agent.setRole(role != null ? role : "General");
        agent.setEmpireName(galaxyName);
        agent.setActive(true);
        agent = aiCharacterRepository.save(agent);

        // Publish AI agent deployed event
        if (galaxyEventService != null) {
            galaxyEventService.publish(GalaxyEventType.AI_AGENT_DEPLOYED, galaxyName, galaxy.getId(),
                    "galaxyName", galaxyName,
                    "agentId", String.valueOf(agent.getId()),
                    "agentName", agentName,
                    "role", agent.getRole());
        }

        log.info("AI agent '{}' deployed to galaxy '{}' with ID {}", agentName, galaxyName, agent.getId());
        return agent;
    }
}
