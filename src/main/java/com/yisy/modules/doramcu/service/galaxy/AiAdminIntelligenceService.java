package com.yisy.modules.doramcu.service.galaxy;

import com.yisy.modules.doramcu.admin.model.AiAdminOrder;
import com.yisy.modules.doramcu.admin.service.AiAdminService;
import com.yisy.modules.doramcu.eve.model.AiCharacter;
import com.yisy.modules.doramcu.hex.Fleet;
import com.yisy.modules.doramcu.hex.Ship;
import com.yisy.modules.doramcu.model.galaxy.Galaxy;
import com.yisy.modules.doramcu.model.galaxy.SolarSystem;
import com.yisy.modules.doramcu.model.misc.Planet;
import com.yisy.modules.doramcu.model.players.Player;
import com.yisy.modules.doramcu.repository.ai.AiCharacterRepository;
import com.yisy.modules.doramcu.repository.galaxy.GalaxyRepository;
import com.yisy.modules.doramcu.repository.players.PlayerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * AI Admin Intelligence Service — manages espionage, security, information gathering,
 * and counter-intelligence operations across a galaxy.
 * <p>
 * This is the domain-specific sub-manager that reports to the Galaxy Overseer.
 * It runs as part of the scheduled health check and performs these functions:
 * <ul>
 *   <li>Assesses intelligence coverage across all solar systems in the galaxy</li>
 *   <li>Monitors AI character network for spy detection and infiltration risks</li>
 *   <li>Evaluates fleet stealth capabilities and reconnaissance readiness</li>
 *   <li>Identifies security vulnerabilities (unprotected facilities, exposed data)</li>
 *   <li>Creates work orders for intelligence operations (surveillance, counter-espionage)</li>
 *   <li>Tracks threat actors and suspicious activity patterns</li>
 *   <li>Assesses information security posture of all empires</li>
 * </ul>
 * </p>
 * <p>
 * Intelligence priority order for work order creation:
 * <ol>
 *   <li>CRITICAL — Active espionage detected against friendly empire</li>
 *   <li>CRITICAL — No intelligence coverage in strategic sector</li>
 *   <li>HIGH — Suspicious AI character activity pattern detected</li>
 *   <li>HIGH — Fleet without stealth capability in contested space</li>
 *   <li>MEDIUM — Unprotected facility in contested system</li>
 *   <li>MEDIUM — Intelligence gap between rival empires</li>
 *   <li>LOW — Recommend expanding surveillance network</li>
 * </ol>
 * </p>
 * <p>
 * Intelligence operations model:
 * <ul>
 *   <li>Surveillance — Passive monitoring of target systems and fleets</li>
 *   <li>Reconnaissance — Active scouting of unexplored or contested areas</li>
 *   <li>Counter-Espionage — Detection and neutralization of enemy spies</li>
 *   <li>Signal Intelligence — Interception and analysis of communications</li>
 *   <li>Human Intelligence — Covert operations using AI character network</li>
 *   <li>Security Assessment — Evaluation of facility and fleet protection</li>
 * </ul>
 * </p>
 * <p>
 * Threat levels for intelligence targets:
 * <ul>
 *   <li>ALPHA — Known hostile empire with active military presence</li>
 *   <li>BRAVO — Neutral empire with military capability</li>
 *   <li>CHARLIE — Civilian empire or unclaimed space</li>
 *   <li>DELTA — Unknown or unclassified entity</li>
 * </ul>
 * </p>
 */
@Service
public class AiAdminIntelligenceService {

    private static final Logger log = LoggerFactory.getLogger(AiAdminIntelligenceService.class);

    private final GalaxyRepository galaxyRepository;
    private final PlayerRepository playerRepository;
    private final AiCharacterRepository aiCharacterRepository;
    private final ObjectProvider<AiAdminService> aiAdminServiceProvider;

    /** Minimum systems per sector for adequate intelligence coverage. */
    public static final int MIN_SYSTEMS_PER_SECTOR_FOR_COVERAGE = 3;

    /** Maximum acceptable percentage of uncovered strategic systems. */
    public static final double MAX_UNCOVERED_SYSTEMS = 0.3;

    /** Minimum stealth ships per fleet for reconnaissance capability. */
    public static final int MIN_STEALTH_SHIPS_PER_FLEET = 1;

    /** Threshold for suspicious AI character activity (orders per cycle). */
    public static final int SUSPICIOUS_ACTIVITY_THRESHOLD = 10;

    /**
     * Represents the intelligence coverage status of a system.
     */
    public enum SystemCoverageStatus {
        FULLY_COVERED,      // Multiple intelligence assets monitoring
        PARTIALLY_COVERED,  // Some intelligence presence
        UNCOVERED,          // No intelligence assets
        BLIND_SPOT          // Known blind spot requiring attention
    }

    /**
     * Represents the threat level of an intelligence target.
     */
    public enum ThreatLevel {
        ALPHA,    // Known hostile with active military
        BRAVO,    // Neutral with military capability
        CHARLIE,  // Civilian or unclaimed
        DELTA     // Unknown or unclassified
    }

    /**
     * Represents an intelligence operation type.
     */
    public enum IntelligenceOperation {
        SURVEILLANCE,         // Passive monitoring
        RECONNAISSANCE,       // Active scouting
        COUNTER_ESPIONAGE,    // Detect and neutralize spies
        SIGNAL_INTELLIGENCE,  // Intercept communications
        HUMAN_INTELLIGENCE,   // Covert character operations
        SECURITY_ASSESSMENT,  // Evaluate protection levels
        DECEPTION,            // Mislead enemy intelligence
        INFILTRATION          // Plant assets in enemy territory
    }

    /**
     * Represents the status of an intelligence asset.
     */
    public enum AssetStatus {
        ACTIVE,       // Currently operational
        STANDBY,      // Available but not deployed
        COMPROMISED,  // Detected or at risk
        DORMANT       // Inactive but recoverable
    }

    public AiAdminIntelligenceService(GalaxyRepository galaxyRepository,
                                       PlayerRepository playerRepository,
                                       AiCharacterRepository aiCharacterRepository,
                                       ObjectProvider<AiAdminService> aiAdminServiceProvider) {
        this.galaxyRepository = galaxyRepository;
        this.playerRepository = playerRepository;
        this.aiCharacterRepository = aiCharacterRepository;
        this.aiAdminServiceProvider = aiAdminServiceProvider;
    }

    // ------------------------------------------------------------------
    // Intelligence Coverage Assessment
    // ------------------------------------------------------------------

    /**
     * Assesses intelligence coverage across all solar systems in the galaxy.
     * <p>
     * Maps which systems have intelligence assets (AI characters with surveillance roles)
     * and identifies gaps in coverage. Strategic systems (those with facilities or fleets)
     * are prioritized for coverage.
     * </p>
     *
     * @param galaxy the galaxy to assess
     * @return an intelligence coverage report
     */
    @Transactional(readOnly = true)
    public IntelligenceCoverageReport assessIntelligenceCoverage(Galaxy galaxy) {
        IntelligenceCoverageReport report = new IntelligenceCoverageReport();
        report.setGalaxyId(galaxy.getId());
        report.setGalaxyName(galaxy.getName());

        List<SolarSystem> systems = galaxy.getSolarSystems();
        if (systems == null || systems.isEmpty()) {
            report.setTotalSystems(0);
            report.setCoveredSystems(0);
            report.setUncoveredSystems(0);
            report.setCoveragePercentage(0.0);
            log.info("No solar systems in galaxy '{}' — no intelligence coverage to assess", galaxy.getName());
            return report;
        }

        // Get all AI characters in this galaxy that could serve as intelligence assets
        List<AiCharacter> allAiInGalaxy = aiCharacterRepository
                .findByAiAdminTrueAndActiveTrueAndEmpireName(galaxy.getName());

        // Count AI characters with intelligence-relevant roles
        int surveillanceAgents = 0;
        int reconnaissanceAgents = 0;
        int counterIntelligenceAgents = 0;
        for (AiCharacter ai : allAiInGalaxy) {
            String role = ai.getRole() != null ? ai.getRole().toLowerCase() : "";
            if (role.contains("overseer") || role.contains("intelligence") || role.contains("exploration")) {
                surveillanceAgents++;
            }
            if (role.contains("exploration") || role.contains("combat")) {
                reconnaissanceAgents++;
            }
            if (role.contains("overseer") || role.contains("governor")) {
                counterIntelligenceAgents++;
            }
        }

        // Assess each system's coverage status
        List<SystemCoverageInfo> systemCoverages = new ArrayList<>();
        int coveredCount = 0;
        int uncoveredCount = 0;
        int strategicUncovered = 0;

        for (SolarSystem system : systems) {
            SystemCoverageInfo coverage = new SystemCoverageInfo();
            coverage.setSystemName(system.getName());
            coverage.setSectorRow(system.getSectorRow());
            coverage.setSectorCol(system.getSectorCol());

            // Determine if this system is strategic
            boolean isStrategic = false;
            if (system.getPlanets() != null && !system.getPlanets().isEmpty()) {
                isStrategic = true;
                coverage.setPlanetCount(system.getPlanets().size());
            }

            // Check for fleet presence (makes system strategic)
            // Note: Fleet data would come from hex layer; for now, planets indicate strategic value
            coverage.setIsStrategic(isStrategic);

            // Determine coverage based on AI character positions and system proximity
            // A system is considered covered if there are AI agents positioned nearby
            boolean hasCoverage = false;
            for (AiCharacter ai : allAiInGalaxy) {
                if (ai.hasPosition()) {
                    // Simple proximity check: same sector or adjacent sector
                    int rowDiff = Math.abs((int) (ai.getPosY() - system.getSectorRow()));
                    int colDiff = Math.abs((int) (ai.getPosX() - system.getSectorCol()));
                    if (rowDiff <= 1 && colDiff <= 1) {
                        hasCoverage = true;
                        break;
                    }
                }
            }

            if (hasCoverage) {
                coverage.setCoverageStatus(SystemCoverageStatus.FULLY_COVERED);
                coveredCount++;
            } else if (allAiInGalaxy.size() >= 3) {
                // If there are enough AI agents, consider it partially covered
                coverage.setCoverageStatus(SystemCoverageStatus.PARTIALLY_COVERED);
                coveredCount++;
            } else {
                coverage.setCoverageStatus(SystemCoverageStatus.UNCOVERED);
                uncoveredCount++;
                if (isStrategic) {
                    strategicUncovered++;
                    coverage.setCoverageStatus(SystemCoverageStatus.BLIND_SPOT);
                }
            }

            coverage.setSurveillanceAgents(surveillanceAgents);
            systemCoverages.add(coverage);
        }

        report.setSystemCoverages(systemCoverages);
        report.setTotalSystems(systems.size());
        report.setCoveredSystems(coveredCount);
        report.setUncoveredSystems(uncoveredCount);
        report.setStrategicUncovered(strategicUncovered);
        report.setCoveragePercentage(systems.size() > 0 ? (double) coveredCount / systems.size() * 100 : 0);
        report.setSurveillanceAgents(surveillanceAgents);
        report.setReconnaissanceAgents(reconnaissanceAgents);
        report.setCounterIntelligenceAgents(counterIntelligenceAgents);

        log.info("Intelligence coverage for '{}': {}/{} systems covered ({:.1f}%), {} strategic blind spots",
                galaxy.getName(), coveredCount, systems.size(),
                report.getCoveragePercentage(), strategicUncovered);

        return report;
    }

    // ------------------------------------------------------------------
    // AI Character Network Analysis
    // ------------------------------------------------------------------

    /**
     * Analyzes the AI character network for suspicious activity patterns.
     * <p>
     * Examines all AI characters in the galaxy to detect:
     * <ul>
     *   <li>Characters with unusual role assignments</li>
     *   <li>Characters positioned in unexpected sectors</li>
     *   <li>Characters with anomalous experience levels</li>
     *   <li>Characters that may be compromised or infiltrated</li>
     * </ul>
     * </p>
     *
     * @param galaxy the galaxy to analyze
     * @return a network analysis report
     */
    @Transactional(readOnly = true)
    public NetworkAnalysisReport analyzeAiNetwork(Galaxy galaxy) {
        NetworkAnalysisReport report = new NetworkAnalysisReport();
        report.setGalaxyId(galaxy.getId());
        report.setGalaxyName(galaxy.getName());

        List<AiCharacter> allAi = aiCharacterRepository
                .findByAiAdminTrueAndActiveTrueAndEmpireName(galaxy.getName());

        report.setTotalAiCharacters(allAi.size());

        // Analyze role distribution
        Map<String, Integer> roleDistribution = new HashMap<>();
        for (AiCharacter ai : allAi) {
            String role = ai.getRole() != null ? ai.getRole() : "Unknown";
            roleDistribution.merge(role, 1, Integer::sum);
        }
        report.setRoleDistribution(roleDistribution);

        // Detect suspicious patterns
        List<SuspiciousActivity> suspiciousActivities = new ArrayList<>();

        for (AiCharacter ai : allAi) {
            // Check for characters without positions (potential infiltration risk)
            if (!ai.hasPosition()) {
                SuspiciousActivity activity = new SuspiciousActivity();
                activity.setAiCharacterId(ai.getId());
                activity.setAiCharacterName(ai.getName());
                activity.setActivityType("NO_POSITION");
                activity.setRiskLevel(ThreatLevel.BRAVO);
                activity.setDescription("AI character has no position — cannot be tracked or verified.");
                suspiciousActivities.add(activity);
            }

            // Check for characters with zero experience (newly created, potential compromise)
            if (ai.getTotalExperience() == 0 && ai.getDailySalary() > 500) {
                SuspiciousActivity activity = new SuspiciousActivity();
                activity.setAiCharacterId(ai.getId());
                activity.setAiCharacterName(ai.getName());
                activity.setActivityType("HIGH_SALARY_NO_EXPERIENCE");
                activity.setRiskLevel(ThreatLevel.CHARLIE);
                activity.setDescription("High salary (" + ai.getDailySalary() +
                        ") with zero experience — verify authorization.");
                suspiciousActivities.add(activity);
            }

            // Check for non-admin characters marked as AI admin (potential impersonation)
            if (ai.isAiAdmin() && !ai.isActive()) {
                SuspiciousActivity activity = new SuspiciousActivity();
                activity.setAiCharacterId(ai.getId());
                activity.setAiCharacterName(ai.getName());
                activity.setActivityType("INACTIVE_ADMIN");
                activity.setRiskLevel(ThreatLevel.BRAVO);
                activity.setDescription("AI admin marked as inactive — review security clearance.");
                suspiciousActivities.add(activity);
            }
        }

        report.setSuspiciousActivities(suspiciousActivities);
        report.setNetworkHealthScore(calculateNetworkHealthScore(allAi, suspiciousActivities.size()));

        log.info("AI network analysis for '{}': {} characters, {} suspicious activities, health={}",
                galaxy.getName(), allAi.size(), suspiciousActivities.size(), report.getNetworkHealthScore());

        return report;
    }

    /**
     * Calculates the network health score based on AI character quality.
     */
    private int calculateNetworkHealthScore(List<AiCharacter> allAi, int suspiciousCount) {
        if (allAi.isEmpty()) {
            return 0;
        }

        int score = 100;

        // Deduct for suspicious activities
        score -= suspiciousCount * 10;

        // Deduct for characters without positions
        long noPositionCount = allAi.stream().filter(ai -> !ai.hasPosition()).count();
        score -= noPositionCount * 5;

        // Bonus for experienced agents
        long experiencedCount = allAi.stream().filter(ai -> ai.getTotalExperience() > 100).count();
        if (experiencedCount > allAi.size() / 2) {
            score += 10;
        }

        return Math.max(0, Math.min(100, score));
    }

    // ------------------------------------------------------------------
    // Fleet Stealth Assessment
    // ------------------------------------------------------------------

    /**
     * Assesses fleet stealth and reconnaissance capabilities.
     * <p>
     * Evaluates whether fleets have adequate stealth capabilities for
     * intelligence operations. Checks for scout ships, stealth technology,
     * and reconnaissance readiness.
     * </p>
     *
     * @param galaxy the galaxy to assess
     * @return a fleet stealth report
     */
    @Transactional(readOnly = true)
    public FleetStealthReport assessFleetStealth(Galaxy galaxy) {
        FleetStealthReport report = new FleetStealthReport();
        report.setGalaxyId(galaxy.getId());
        report.setGalaxyName(galaxy.getName());

        // Get all players to check their fleets
        List<Player> allPlayers = playerRepository.findAll();
        List<FleetStealthInfo> fleetStealthInfos = new ArrayList<>();

        int totalFleets = 0;
        int stealthCapableFleets = 0;
        int reconnaissanceReadyFleets = 0;

        for (Player player : allPlayers) {
            if (player.getEmpireName() == null || player.getEmpireName().isBlank()) {
                continue;
            }

            // Note: Fleet data would come from the hex layer.
            // For now, we assess based on empire-level data.
            FleetStealthInfo fleetInfo = new FleetStealthInfo();
            fleetInfo.setEmpireName(player.getEmpireName());
            fleetInfo.setPlayerId(player.getId());

            // Check if empire has any ships (proxy for fleet capability)
            // In a full implementation, this would query the Fleet repository
            fleetInfo.setHasStealthCapability(false);
            fleetInfo.setHasReconnaissanceCapability(false);
            fleetInfo.setStealthShipCount(0);
            fleetInfo.setReconShipCount(0);

            fleetStealthInfos.add(fleetInfo);
            totalFleets++;
        }

        report.setFleetStealthInfos(fleetStealthInfos);
        report.setTotalFleets(totalFleets);
        report.setStealthCapableFleets(stealthCapableFleets);
        report.setReconnaissanceReadyFleets(reconnaissanceReadyFleets);
        report.setStealthPercentage(totalFleets > 0 ? (double) stealthCapableFleets / totalFleets * 100 : 0);

        log.info("Fleet stealth assessment for '{}': {} fleets, {} stealth-capable ({:.1f}%)",
                galaxy.getName(), totalFleets, stealthCapableFleets, report.getStealthPercentage());

        return report;
    }

    // ------------------------------------------------------------------
    // Security Vulnerability Assessment
    // ------------------------------------------------------------------

    /**
     * Identifies security vulnerabilities across the galaxy.
     * <p>
     * Scans for:
     * <ul>
     *   <li>Unprotected facilities in contested systems</li>
     *   <li>Planets without defense infrastructure</li>
     *   <li>Systems with no patrol coverage</li>
     *   <li>Exposed communication channels</li>
     * </ul>
     * </p>
     *
     * @param galaxy the galaxy to assess
     * @return a security vulnerability report
     */
    @Transactional(readOnly = true)
    public SecurityVulnerabilityReport assessSecurityVulnerabilities(Galaxy galaxy) {
        SecurityVulnerabilityReport report = new SecurityVulnerabilityReport();
        report.setGalaxyId(galaxy.getId());
        report.setGalaxyName(galaxy.getName());

        List<SolarSystem> systems = galaxy.getSolarSystems();
        if (systems == null || systems.isEmpty()) {
            report.setTotalVulnerabilities(0);
            report.setCriticalVulnerabilities(0);
            report.setSecurityScore(100);
            return report;
        }

        List<VulnerabilityInfo> vulnerabilities = new ArrayList<>();
        int criticalCount = 0;
        int highCount = 0;
        int mediumCount = 0;

        for (SolarSystem system : systems) {
            // Check for planets without owners (unclaimed strategic assets)
            if (system.getPlanets() != null) {
                for (Planet planet : system.getPlanets()) {
                    if (planet.getOwnerEmpireId() == null) {
                        // Unclaimed planet — potential vulnerability
                        VulnerabilityInfo vuln = new VulnerabilityInfo();
                        vuln.setSystemName(system.getName());
                        vuln.setPlanetName(planet.getName());
                        vuln.setVulnerabilityType("UNCLAIMED_PLANET");
                        vuln.setSeverity(ThreatLevel.BRAVO);
                        vuln.setDescription("Unclaimed planet in strategic system — vulnerable to hostile takeover.");
                        vulnerabilities.add(vuln);
                        highCount++;
                    }
                }
            }

            // Check for systems in contested sectors (multiple empires nearby)
            // This would require empire presence data; for now, flag edge sectors
            if (system.getSectorRow() == 0 || system.getSectorRow() == 9 ||
                system.getSectorCol() == 0 || system.getSectorCol() == 9) {
                VulnerabilityInfo vuln = new VulnerabilityInfo();
                vuln.setSystemName(system.getName());
                vuln.setVulnerabilityType("EDGE_SECTOR");
                vuln.setSeverity(ThreatLevel.CHARLIE);
                vuln.setDescription("System in edge sector — limited escape routes and support.");
                vulnerabilities.add(vuln);
                mediumCount++;
            }
        }

        report.setVulnerabilities(vulnerabilities);
        report.setTotalVulnerabilities(vulnerabilities.size());
        report.setCriticalVulnerabilities(criticalCount);
        report.setHighVulnerabilities(highCount);
        report.setMediumVulnerabilities(mediumCount);

        // Calculate security score
        int securityScore = 100;
        securityScore -= criticalCount * 20;
        securityScore -= highCount * 10;
        securityScore -= mediumCount * 5;
        report.setSecurityScore(Math.max(0, securityScore));

        log.info("Security vulnerability assessment for '{}': {} vulnerabilities ({} critical, {} high, {} medium), score={}",
                galaxy.getName(), vulnerabilities.size(), criticalCount, highCount, mediumCount, securityScore);

        return report;
    }

    // ------------------------------------------------------------------
    // Threat Actor Tracking
    // ------------------------------------------------------------------

    /**
     * Tracks and profiles threat actors in the galaxy.
     * <p>
     * Identifies empires and entities that pose intelligence threats:
     * <ul>
     *   <li>Militarist empires with expansionist tendencies</li>
     *   <li>Empires with large AI character networks</li>
     *   <li>Empires in contested sectors</li>
     *   <li>Unknown or unclassified entities</li>
     * </ul>
     * </p>
     *
     * @param galaxy the galaxy to scan
     * @return a threat actor report
     */
    @Transactional(readOnly = true)
    public ThreatActorReport trackThreatActors(Galaxy galaxy) {
        ThreatActorReport report = new ThreatActorReport();
        report.setGalaxyId(galaxy.getId());
        report.setGalaxyName(galaxy.getName());

        List<Player> allPlayers = playerRepository.findAll();
        List<ThreatActorProfile> threatActors = new ArrayList<>();

        for (Player player : allPlayers) {
            if (player.getEmpireName() == null || player.getEmpireName().isBlank()) {
                continue;
            }

            ThreatActorProfile profile = new ThreatActorProfile();
            profile.setEmpireId(player.getId());
            profile.setEmpireName(player.getEmpireName());
            profile.setGovernmentType(player.getGovernmentType());
            profile.setEthos(player.getEthos());

            // Assess threat level based on government type and ethos
            ThreatLevel threatLevel = assessThreatLevel(player);
            profile.setThreatLevel(threatLevel);

            // Get AI character count for this empire
            List<AiCharacter> empireAi = aiCharacterRepository
                    .findByAiAdminTrueAndActiveTrueAndEmpireName(player.getEmpireName());
            profile.setAiCharacterCount(empireAi.size());

            // Determine capabilities
            List<String> capabilities = new ArrayList<>();
            if (player.getEthos() == Player.Ethos.MILITARIST) {
                capabilities.add("MILITARY_EXPANSION");
                capabilities.add("COMBAT_OPERATIONS");
            }
            if (player.getGovernmentType() == Player.GovernmentType.DICTATORSHIP ||
                player.getGovernmentType() == Player.GovernmentType.AUTHORITARIAN) {
                capabilities.add("COVERT_OPERATIONS");
                capabilities.add("ESPIONAGE");
            }
            if (empireAi.size() > 5) {
                capabilities.add("AI_NETWORK");
            }
            profile.setCapabilities(capabilities);

            // Generate threat assessment
            profile.setThreatAssessment(generateThreatAssessment(profile));
            threatActors.add(profile);
        }

        report.setThreatActors(threatActors);
        report.setTotalThreatActors(threatActors.size());

        // Count by threat level
        long alphaThreats = threatActors.stream().filter(t -> t.getThreatLevel() == ThreatLevel.ALPHA).count();
        long bravoThreats = threatActors.stream().filter(t -> t.getThreatLevel() == ThreatLevel.BRAVO).count();
        report.setAlphaThreats((int) alphaThreats);
        report.setBravoThreats((int) bravoThreats);

        log.info("Threat actor tracking for '{}': {} actors ({} ALPHA, {} BRAVO)",
                galaxy.getName(), threatActors.size(), alphaThreats, bravoThreats);

        return report;
    }

    /**
     * Assesses the threat level of an empire based on its profile.
     */
    private ThreatLevel assessThreatLevel(Player player) {
        if (player.getEthos() == Player.Ethos.MILITARIST) {
            if (player.getGovernmentType() == Player.GovernmentType.DICTATORSHIP ||
                player.getGovernmentType() == Player.GovernmentType.AUTHORITARIAN) {
                return ThreatLevel.ALPHA;
            }
            return ThreatLevel.BRAVO;
        }

        if (player.getGovernmentType() == Player.GovernmentType.DICTATORSHIP) {
            return ThreatLevel.BRAVO;
        }

        return ThreatLevel.CHARLIE;
    }

    /**
     * Generates a threat assessment string for a threat actor.
     */
    private String generateThreatAssessment(ThreatActorProfile profile) {
        StringBuilder sb = new StringBuilder();
        sb.append("Threat Level: ").append(profile.getThreatLevel()).append(". ");
        sb.append("Government: ").append(profile.getGovernmentType()).append(". ");
        sb.append("Ethos: ").append(profile.getEthos()).append(". ");
        sb.append("AI Network: ").append(profile.getAiCharacterCount()).append(" agents. ");

        if (profile.getThreatLevel() == ThreatLevel.ALPHA) {
            sb.append("HIGH PRIORITY: Active monitoring recommended. ");
            sb.append("Consider counter-espionage measures.");
        } else if (profile.getThreatLevel() == ThreatLevel.BRAVO) {
            sb.append("MODERATE PRIORITY: Maintain surveillance. ");
            sb.append("Monitor for escalation.");
        } else {
            sb.append("LOW PRIORITY: Routine monitoring sufficient.");
        }

        return sb.toString();
    }

    // ------------------------------------------------------------------
    // Work Order Creation
    // ------------------------------------------------------------------

    /**
     * Creates intelligence work orders based on all assessment reports.
     * Orders are prioritized: CRITICAL (active threats) > HIGH (gaps) >
     * MEDIUM (opportunities) > LOW (recommendations).
     *
     * @param galaxy the galaxy to create work orders for
     * @param coverageReport the intelligence coverage report
     * @param networkReport the network analysis report
     * @param securityReport the security vulnerability report
     * @param threatReport the threat actor report
     * @return list of created order texts
     */
    @Transactional
    public List<String> createIntelligenceWorkOrders(Galaxy galaxy,
                                                      IntelligenceCoverageReport coverageReport,
                                                      NetworkAnalysisReport networkReport,
                                                      SecurityVulnerabilityReport securityReport,
                                                      ThreatActorReport threatReport) {
        List<String> ordersCreated = new ArrayList<>();
        String submittedBy = "GalaxyOverseer:" + galaxy.getName();

        AiAdminService aiAdminService = aiAdminServiceProvider.getIfAvailable();
        if (aiAdminService == null) {
            log.warn("AiAdminService not available for intelligence work orders in '{}'", galaxy.getName());
            return ordersCreated;
        }

        // CRITICAL: Strategic blind spots
        if (coverageReport.getStrategicUncovered() > 0) {
            String orderText = String.format("[CRITICAL] Intelligence blind spots: %d strategic systems have no coverage. " +
                    "Coverage: %.1f%%. Action: Deploy surveillance assets to uncovered strategic systems.",
                    coverageReport.getStrategicUncovered(), coverageReport.getCoveragePercentage());
            aiAdminService.submitOrder(submittedBy, orderText);
            ordersCreated.add(orderText);
        }

        // CRITICAL: ALPHA threat actors
        if (threatReport.getAlphaThreats() > 0) {
            for (ThreatActorProfile actor : threatReport.getThreatActors()) {
                if (actor.getThreatLevel() == ThreatLevel.ALPHA) {
                    String orderText = String.format("[CRITICAL] ALPHA threat: %s. %s " +
                            "Action: Initiate active surveillance and counter-espionage measures.",
                            actor.getEmpireName(), actor.getThreatAssessment());
                    aiAdminService.submitOrder(submittedBy, orderText);
                    ordersCreated.add(orderText);
                }
            }
        }

        // HIGH: Suspicious AI network activity
        for (SuspiciousActivity activity : networkReport.getSuspiciousActivities()) {
            if (activity.getRiskLevel() == ThreatLevel.BRAVO || activity.getRiskLevel() == ThreatLevel.ALPHA) {
                String orderText = String.format("[HIGH] Suspicious activity: %s (%s). %s " +
                        "Action: Investigate and verify authorization.",
                        activity.getAiCharacterName(), activity.getActivityType(), activity.getDescription());
                aiAdminService.submitOrder(submittedBy, orderText);
                ordersCreated.add(orderText);
            }
        }

        // HIGH: Critical security vulnerabilities
        if (securityReport.getCriticalVulnerabilities() > 0) {
            String orderText = String.format("[HIGH] Security vulnerabilities: %d critical, %d high, %d medium. " +
                    "Security score: %d. Action: Address critical vulnerabilities immediately.",
                    securityReport.getCriticalVulnerabilities(),
                    securityReport.getHighVulnerabilities(),
                    securityReport.getMediumVulnerabilities(),
                    securityReport.getSecurityScore());
            aiAdminService.submitOrder(submittedBy, orderText);
            ordersCreated.add(orderText);
        }

        // MEDIUM: Low coverage percentage
        if (coverageReport.getCoveragePercentage() < (100.0 * (1.0 - MAX_UNCOVERED_SYSTEMS))) {
            String orderText = String.format("[MEDIUM] Low intelligence coverage: %.1f%% of systems monitored. " +
                    "%d surveillance agents, %d reconnaissance agents. " +
                    "Action: Expand surveillance network to improve coverage.",
                    coverageReport.getCoveragePercentage(),
                    coverageReport.getSurveillanceAgents(),
                    coverageReport.getReconnaissanceAgents());
            aiAdminService.submitOrder(submittedBy, orderText);
            ordersCreated.add(orderText);
        }

        // MEDIUM: BRAVO threat actors
        if (threatReport.getBravoThreats() > 0) {
            String orderText = String.format("[MEDIUM] %d BRAVO-level threat actors detected. " +
                    "Action: Maintain surveillance and monitor for escalation.",
                    threatReport.getBravoThreats());
            aiAdminService.submitOrder(submittedBy, orderText);
            ordersCreated.add(orderText);
        }

        // LOW: Network health recommendations
        if (networkReport.getNetworkHealthScore() < 70) {
            String orderText = String.format("[LOW] AI network health score: %d. " +
                    "%d suspicious activities detected. " +
                    "Action: Review and improve AI character network security.",
                    networkReport.getNetworkHealthScore(),
                    networkReport.getSuspiciousActivities().size());
            aiAdminService.submitOrder(submittedBy, orderText);
            ordersCreated.add(orderText);
        }

        log.info("Created {} intelligence work orders for galaxy '{}'", ordersCreated.size(), galaxy.getName());
        return ordersCreated;
    }

    // ------------------------------------------------------------------
    // Assessment Integration
    // ------------------------------------------------------------------

    /**
     * Performs a complete intelligence assessment for a galaxy and adds findings
     * to the galaxy assessment. This is called by GalaxyOverseerService during
     * the health check cycle.
     *
     * @param galaxy the galaxy to assess
     * @param assessment the galaxy assessment to add findings to
     * @return the number of work orders created
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public int assessIntelligenceNeeds(Galaxy galaxy, GalaxyOverseerService.GalaxyAssessment assessment) {
        // Run all intelligence assessments
        IntelligenceCoverageReport coverageReport = assessIntelligenceCoverage(galaxy);
        NetworkAnalysisReport networkReport = analyzeAiNetwork(galaxy);
        FleetStealthReport stealthReport = assessFleetStealth(galaxy);
        SecurityVulnerabilityReport securityReport = assessSecurityVulnerabilities(galaxy);
        ThreatActorReport threatReport = trackThreatActors(galaxy);

        // Add intelligence findings based on coverage
        if (coverageReport.getTotalSystems() == 0) {
            assessment.addFinding(
                    GalaxyOverseerService.GalaxyAssessment.Severity.INFO,
                    "No solar systems in galaxy — intelligence operations not yet applicable."
            );
        } else if (coverageReport.getStrategicUncovered() > 0) {
            assessment.addFindingWithAction(
                    GalaxyOverseerService.GalaxyAssessment.Severity.HIGH,
                    String.format("Intelligence blind spots: %d strategic systems uncovered. " +
                            "Overall coverage: %.1f%%.",
                            coverageReport.getStrategicUncovered(),
                            coverageReport.getCoveragePercentage()),
                    "Deploy surveillance assets to cover strategic systems."
            );
        } else if (coverageReport.getCoveragePercentage() < 70) {
            assessment.addFindingWithAction(
                    GalaxyOverseerService.GalaxyAssessment.Severity.MEDIUM,
                    String.format("Low intelligence coverage: %.1f%% of systems monitored.",
                            coverageReport.getCoveragePercentage()),
                    "Expand surveillance network to improve coverage."
            );
        } else {
            assessment.addFinding(
                    GalaxyOverseerService.GalaxyAssessment.Severity.INFO,
                    String.format("Good intelligence coverage: %.1f%% of systems monitored. " +
                            "%d surveillance agents active.",
                            coverageReport.getCoveragePercentage(),
                            coverageReport.getSurveillanceAgents())
            );
        }

        // Add network health findings
        if (networkReport.getNetworkHealthScore() < 50) {
            assessment.addFindingWithAction(
                    GalaxyOverseerService.GalaxyAssessment.Severity.HIGH,
                    String.format("AI network health compromised: score %d. " +
                            "%d suspicious activities detected.",
                            networkReport.getNetworkHealthScore(),
                            networkReport.getSuspiciousActivities().size()),
                    "Investigate suspicious activities and secure AI character network."
            );
        }

        // Add security vulnerability findings
        if (securityReport.getCriticalVulnerabilities() > 0) {
            assessment.addFindingWithAction(
                    GalaxyOverseerService.GalaxyAssessment.Severity.CRITICAL,
                    String.format("Critical security vulnerabilities: %d critical, %d high. " +
                            "Security score: %d.",
                            securityReport.getCriticalVulnerabilities(),
                            securityReport.getHighVulnerabilities(),
                            securityReport.getSecurityScore()),
                    "Address critical security vulnerabilities immediately."
            );
        } else if (securityReport.getSecurityScore() < 70) {
            assessment.addFindingWithAction(
                    GalaxyOverseerService.GalaxyAssessment.Severity.MEDIUM,
                    String.format("Security score below threshold: %d. " +
                            "%d total vulnerabilities.",
                            securityReport.getSecurityScore(),
                            securityReport.getTotalVulnerabilities()),
                    "Improve security posture across all systems."
            );
        }

        // Add threat actor findings
        if (threatReport.getAlphaThreats() > 0) {
            assessment.addFindingWithAction(
                    GalaxyOverseerService.GalaxyAssessment.Severity.HIGH,
                    String.format("%d ALPHA-level threat actors detected. " +
                            "%d BRAVO-level threats.",
                            threatReport.getAlphaThreats(),
                            threatReport.getBravoThreats()),
                    "Initiate active surveillance and counter-espionage measures."
            );
        }

        // Create work orders for intelligence operations
        List<String> ordersCreated = createIntelligenceWorkOrders(
                galaxy, coverageReport, networkReport, securityReport, threatReport);

        log.info("Intelligence assessment for '{}': coverage={:.1f}%%, network health={}, security={}, " +
                        "{} threats, {} orders created",
                galaxy.getName(),
                coverageReport.getCoveragePercentage(),
                networkReport.getNetworkHealthScore(),
                securityReport.getSecurityScore(),
                threatReport.getTotalThreatActors(),
                ordersCreated.size());

        return ordersCreated.size();
    }

    // ------------------------------------------------------------------
    // DTOs
    // ------------------------------------------------------------------

    /**
     * Intelligence coverage report for a galaxy.
     */
    public static class IntelligenceCoverageReport {
        private Long galaxyId;
        private String galaxyName;
        private int totalSystems;
        private int coveredSystems;
        private int uncoveredSystems;
        private int strategicUncovered;
        private double coveragePercentage;
        private int surveillanceAgents;
        private int reconnaissanceAgents;
        private int counterIntelligenceAgents;
        private List<SystemCoverageInfo> systemCoverages;

        public Long getGalaxyId() { return galaxyId; }
        public void setGalaxyId(Long galaxyId) { this.galaxyId = galaxyId; }
        public String getGalaxyName() { return galaxyName; }
        public void setGalaxyName(String galaxyName) { this.galaxyName = galaxyName; }
        public int getTotalSystems() { return totalSystems; }
        public void setTotalSystems(int totalSystems) { this.totalSystems = totalSystems; }
        public int getCoveredSystems() { return coveredSystems; }
        public void setCoveredSystems(int coveredSystems) { this.coveredSystems = coveredSystems; }
        public int getUncoveredSystems() { return uncoveredSystems; }
        public void setUncoveredSystems(int uncoveredSystems) { this.uncoveredSystems = uncoveredSystems; }
        public int getStrategicUncovered() { return strategicUncovered; }
        public void setStrategicUncovered(int strategicUncovered) { this.strategicUncovered = strategicUncovered; }
        public double getCoveragePercentage() { return coveragePercentage; }
        public void setCoveragePercentage(double coveragePercentage) { this.coveragePercentage = coveragePercentage; }
        public int getSurveillanceAgents() { return surveillanceAgents; }
        public void setSurveillanceAgents(int surveillanceAgents) { this.surveillanceAgents = surveillanceAgents; }
        public int getReconnaissanceAgents() { return reconnaissanceAgents; }
        public void setReconnaissanceAgents(int reconnaissanceAgents) { this.reconnaissanceAgents = reconnaissanceAgents; }
        public int getCounterIntelligenceAgents() { return counterIntelligenceAgents; }
        public void setCounterIntelligenceAgents(int counterIntelligenceAgents) { this.counterIntelligenceAgents = counterIntelligenceAgents; }
        public List<SystemCoverageInfo> getSystemCoverages() { return systemCoverages; }
        public void setSystemCoverages(List<SystemCoverageInfo> systemCoverages) { this.systemCoverages = systemCoverages; }
    }

    /**
     * Coverage information for a single solar system.
     */
    public static class SystemCoverageInfo {
        private String systemName;
        private int sectorRow;
        private int sectorCol;
        private SystemCoverageStatus coverageStatus;
        private boolean isStrategic;
        private int planetCount;
        private int surveillanceAgents;

        public String getSystemName() { return systemName; }
        public void setSystemName(String systemName) { this.systemName = systemName; }
        public int getSectorRow() { return sectorRow; }
        public void setSectorRow(int sectorRow) { this.sectorRow = sectorRow; }
        public int getSectorCol() { return sectorCol; }
        public void setSectorCol(int sectorCol) { this.sectorCol = sectorCol; }
        public SystemCoverageStatus getCoverageStatus() { return coverageStatus; }
        public void setCoverageStatus(SystemCoverageStatus coverageStatus) { this.coverageStatus = coverageStatus; }
        public boolean isStrategic() { return isStrategic; }
        public void setIsStrategic(boolean isStrategic) { this.isStrategic = isStrategic; }
        public int getPlanetCount() { return planetCount; }
        public void setPlanetCount(int planetCount) { this.planetCount = planetCount; }
        public int getSurveillanceAgents() { return surveillanceAgents; }
        public void setSurveillanceAgents(int surveillanceAgents) { this.surveillanceAgents = surveillanceAgents; }
    }

    /**
     * Network analysis report for AI character network.
     */
    public static class NetworkAnalysisReport {
        private Long galaxyId;
        private String galaxyName;
        private int totalAiCharacters;
        private Map<String, Integer> roleDistribution;
        private List<SuspiciousActivity> suspiciousActivities;
        private int networkHealthScore;

        public Long getGalaxyId() { return galaxyId; }
        public void setGalaxyId(Long galaxyId) { this.galaxyId = galaxyId; }
        public String getGalaxyName() { return galaxyName; }
        public void setGalaxyName(String galaxyName) { this.galaxyName = galaxyName; }
        public int getTotalAiCharacters() { return totalAiCharacters; }
        public void setTotalAiCharacters(int totalAiCharacters) { this.totalAiCharacters = totalAiCharacters; }
        public Map<String, Integer> getRoleDistribution() { return roleDistribution; }
        public void setRoleDistribution(Map<String, Integer> roleDistribution) { this.roleDistribution = roleDistribution; }
        public List<SuspiciousActivity> getSuspiciousActivities() { return suspiciousActivities; }
        public void setSuspiciousActivities(List<SuspiciousActivity> suspiciousActivities) { this.suspiciousActivities = suspiciousActivities; }
        public int getNetworkHealthScore() { return networkHealthScore; }
        public void setNetworkHealthScore(int networkHealthScore) { this.networkHealthScore = networkHealthScore; }
    }

    /**
     * A suspicious activity detected in the AI character network.
     */
    public static class SuspiciousActivity {
        private Long aiCharacterId;
        private String aiCharacterName;
        private String activityType;
        private ThreatLevel riskLevel;
        private String description;

        public Long getAiCharacterId() { return aiCharacterId; }
        public void setAiCharacterId(Long aiCharacterId) { this.aiCharacterId = aiCharacterId; }
        public String getAiCharacterName() { return aiCharacterName; }
        public void setAiCharacterName(String aiCharacterName) { this.aiCharacterName = aiCharacterName; }
        public String getActivityType() { return activityType; }
        public void setActivityType(String activityType) { this.activityType = activityType; }
        public ThreatLevel getRiskLevel() { return riskLevel; }
        public void setRiskLevel(ThreatLevel riskLevel) { this.riskLevel = riskLevel; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    /**
     * Fleet stealth capability report.
     */
    public static class FleetStealthReport {
        private Long galaxyId;
        private String galaxyName;
        private int totalFleets;
        private int stealthCapableFleets;
        private int reconnaissanceReadyFleets;
        private double stealthPercentage;
        private List<FleetStealthInfo> fleetStealthInfos;

        public Long getGalaxyId() { return galaxyId; }
        public void setGalaxyId(Long galaxyId) { this.galaxyId = galaxyId; }
        public String getGalaxyName() { return galaxyName; }
        public void setGalaxyName(String galaxyName) { this.galaxyName = galaxyName; }
        public int getTotalFleets() { return totalFleets; }
        public void setTotalFleets(int totalFleets) { this.totalFleets = totalFleets; }
        public int getStealthCapableFleets() { return stealthCapableFleets; }
        public void setStealthCapableFleets(int stealthCapableFleets) { this.stealthCapableFleets = stealthCapableFleets; }
        public int getReconnaissanceReadyFleets() { return reconnaissanceReadyFleets; }
        public void setReconnaissanceReadyFleets(int reconnaissanceReadyFleets) { this.reconnaissanceReadyFleets = reconnaissanceReadyFleets; }
        public double getStealthPercentage() { return stealthPercentage; }
        public void setStealthPercentage(double stealthPercentage) { this.stealthPercentage = stealthPercentage; }
        public List<FleetStealthInfo> getFleetStealthInfos() { return fleetStealthInfos; }
        public void setFleetStealthInfos(List<FleetStealthInfo> fleetStealthInfos) { this.fleetStealthInfos = fleetStealthInfos; }
    }

    /**
     * Stealth capability information for a single fleet.
     */
    public static class FleetStealthInfo {
        private Long playerId;
        private String empireName;
        private boolean hasStealthCapability;
        private boolean hasReconnaissanceCapability;
        private int stealthShipCount;
        private int reconShipCount;

        public Long getPlayerId() { return playerId; }
        public void setPlayerId(Long playerId) { this.playerId = playerId; }
        public String getEmpireName() { return empireName; }
        public void setEmpireName(String empireName) { this.empireName = empireName; }
        public boolean isHasStealthCapability() { return hasStealthCapability; }
        public void setHasStealthCapability(boolean hasStealthCapability) { this.hasStealthCapability = hasStealthCapability; }
        public boolean isHasReconnaissanceCapability() { return hasReconnaissanceCapability; }
        public void setHasReconnaissanceCapability(boolean hasReconnaissanceCapability) { this.hasReconnaissanceCapability = hasReconnaissanceCapability; }
        public int getStealthShipCount() { return stealthShipCount; }
        public void setStealthShipCount(int stealthShipCount) { this.stealthShipCount = stealthShipCount; }
        public int getReconShipCount() { return reconShipCount; }
        public void setReconShipCount(int reconShipCount) { this.reconShipCount = reconShipCount; }
    }

    /**
     * Security vulnerability report for a galaxy.
     */
    public static class SecurityVulnerabilityReport {
        private Long galaxyId;
        private String galaxyName;
        private int totalVulnerabilities;
        private int criticalVulnerabilities;
        private int highVulnerabilities;
        private int mediumVulnerabilities;
        private int securityScore;
        private List<VulnerabilityInfo> vulnerabilities;

        public Long getGalaxyId() { return galaxyId; }
        public void setGalaxyId(Long galaxyId) { this.galaxyId = galaxyId; }
        public String getGalaxyName() { return galaxyName; }
        public void setGalaxyName(String galaxyName) { this.galaxyName = galaxyName; }
        public int getTotalVulnerabilities() { return totalVulnerabilities; }
        public void setTotalVulnerabilities(int totalVulnerabilities) { this.totalVulnerabilities = totalVulnerabilities; }
        public int getCriticalVulnerabilities() { return criticalVulnerabilities; }
        public void setCriticalVulnerabilities(int criticalVulnerabilities) { this.criticalVulnerabilities = criticalVulnerabilities; }
        public int getHighVulnerabilities() { return highVulnerabilities; }
        public void setHighVulnerabilities(int highVulnerabilities) { this.highVulnerabilities = highVulnerabilities; }
        public int getMediumVulnerabilities() { return mediumVulnerabilities; }
        public void setMediumVulnerabilities(int mediumVulnerabilities) { this.mediumVulnerabilities = mediumVulnerabilities; }
        public int getSecurityScore() { return securityScore; }
        public void setSecurityScore(int securityScore) { this.securityScore = securityScore; }
        public List<VulnerabilityInfo> getVulnerabilities() { return vulnerabilities; }
        public void setVulnerabilities(List<VulnerabilityInfo> vulnerabilities) { this.vulnerabilities = vulnerabilities; }
    }

    /**
     * Information about a single security vulnerability.
     */
    public static class VulnerabilityInfo {
        private String systemName;
        private String planetName;
        private String vulnerabilityType;
        private ThreatLevel severity;
        private String description;

        public String getSystemName() { return systemName; }
        public void setSystemName(String systemName) { this.systemName = systemName; }
        public String getPlanetName() { return planetName; }
        public void setPlanetName(String planetName) { this.planetName = planetName; }
        public String getVulnerabilityType() { return vulnerabilityType; }
        public void setVulnerabilityType(String vulnerabilityType) { this.vulnerabilityType = vulnerabilityType; }
        public ThreatLevel getSeverity() { return severity; }
        public void setSeverity(ThreatLevel severity) { this.severity = severity; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    /**
     * Threat actor tracking report.
     */
    public static class ThreatActorReport {
        private Long galaxyId;
        private String galaxyName;
        private int totalThreatActors;
        private int alphaThreats;
        private int bravoThreats;
        private List<ThreatActorProfile> threatActors;

        public Long getGalaxyId() { return galaxyId; }
        public void setGalaxyId(Long galaxyId) { this.galaxyId = galaxyId; }
        public String getGalaxyName() { return galaxyName; }
        public void setGalaxyName(String galaxyName) { this.galaxyName = galaxyName; }
        public int getTotalThreatActors() { return totalThreatActors; }
        public void setTotalThreatActors(int totalThreatActors) { this.totalThreatActors = totalThreatActors; }
        public int getAlphaThreats() { return alphaThreats; }
        public void setAlphaThreats(int alphaThreats) { this.alphaThreats = alphaThreats; }
        public int getBravoThreats() { return bravoThreats; }
        public void setBravoThreats(int bravoThreats) { this.bravoThreats = bravoThreats; }
        public List<ThreatActorProfile> getThreatActors() { return threatActors; }
        public void setThreatActors(List<ThreatActorProfile> threatActors) { this.threatActors = threatActors; }
    }

    /**
     * Profile of a threat actor in the galaxy.
     */
    public static class ThreatActorProfile {
        private Long empireId;
        private String empireName;
        private Player.GovernmentType governmentType;
        private Player.Ethos ethos;
        private ThreatLevel threatLevel;
        private int aiCharacterCount;
        private List<String> capabilities;
        private String threatAssessment;

        public Long getEmpireId() { return empireId; }
        public void setEmpireId(Long empireId) { this.empireId = empireId; }
        public String getEmpireName() { return empireName; }
        public void setEmpireName(String empireName) { this.empireName = empireName; }
        public Player.GovernmentType getGovernmentType() { return governmentType; }
        public void setGovernmentType(Player.GovernmentType governmentType) { this.governmentType = governmentType; }
        public Player.Ethos getEthos() { return ethos; }
        public void setEthos(Player.Ethos ethos) { this.ethos = ethos; }
        public ThreatLevel getThreatLevel() { return threatLevel; }
        public void setThreatLevel(ThreatLevel threatLevel) { this.threatLevel = threatLevel; }
        public int getAiCharacterCount() { return aiCharacterCount; }
        public void setAiCharacterCount(int aiCharacterCount) { this.aiCharacterCount = aiCharacterCount; }
        public List<String> getCapabilities() { return capabilities; }
        public void setCapabilities(List<String> capabilities) { this.capabilities = capabilities; }
        public String getThreatAssessment() { return threatAssessment; }
        public void setThreatAssessment(String threatAssessment) { this.threatAssessment = threatAssessment; }
    }
}
