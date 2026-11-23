package com.yisy.modules.doramcu.service.galaxy;

import com.yisy.modules.doramcu.admin.service.AiAdminService;
import com.yisy.modules.doramcu.eve.model.AiCharacter;
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
 * AI Admin Science Service — manages research and development, technology progression,
 * and scientific infrastructure across a galaxy.
 * <p>
 * This is the domain-specific sub-manager that reports to the Galaxy Overseer.
 * It runs as part of the scheduled health check and performs these functions:
 * <ul>
 *   <li>Assesses research infrastructure (RESEARCH_LAB facilities, RESEARCH stations)</li>
 *   <li>Evaluates technology levels per empire across all technology categories</li>
 *   <li>Monitors R&D progress and identifies technology gaps</li>
 *   <li>Tracks scientific output and research efficiency</li>
 *   <li>Creates work orders for science operations (build labs, fund research)</li>
 *   <li>Assesses technology tree coverage and recommends research priorities</li>
 *   <li>Evaluates scientific collaboration opportunities between empires</li>
 * </ul>
 * </p>
 * <p>
 * Science priority order for work order creation:
 * <ol>
 *   <li>CRITICAL — No research infrastructure in the galaxy</li>
 *   <li>CRITICAL — Empire with zero technology level (cannot build anything)</li>
 *   <li>HIGH — Research lab shortage relative to empire count</li>
 *   <li>HIGH — Technology gap between rival empires exceeds threshold</li>
 *   <li>MEDIUM — Low research output in strategic technology category</li>
 *   <li>MEDIUM — No research stations in key sectors</li>
 *   <li>LOW — Recommend expanding research collaboration</li>
 * </ol>
 * </p>
 * <p>
 * Technology categories:
 * <ul>
 *   <li>PROPULSION — Engine and drive technology for ships</li>
 *   <li>WEAPONS — Weapon systems and armament technology</li>
 *   <li>SHIELDS — Defensive shield and armor technology</li>
 *   <li>SENSORS — Detection and scanning technology</li>
 *   <li>ENGINEERING — Hull construction and repair technology</li>
 *   <li>SCIENCE — Research and analysis technology</li>
 *   <li>MEDICAL — Healing and biological technology</li>
 *   <li>COMPUTER — AI and computing technology</li>
 * </ul>
 * </p>
 * <p>
 * Research facility types:
 * <ul>
 *   <li>RESEARCH_LAB (planet facility) — Primary research output source</li>
 *   <li>RESEARCH (space station) — Deep space research outpost</li>
 *   <li>SENSOR_ARRAY (planet facility) — Sensor technology research</li>
 * </ul>
 * </p>
 */
@Service
public class AiAdminScienceService {

    private static final Logger log = LoggerFactory.getLogger(AiAdminScienceService.class);

    private final GalaxyRepository galaxyRepository;
    private final PlayerRepository playerRepository;
    private final AiCharacterRepository aiCharacterRepository;
    private final ObjectProvider<AiAdminService> aiAdminServiceProvider;

    /** Minimum research labs per empire for adequate research output. */
    public static final int MIN_RESEARCH_LABS_PER_EMPIRE = 1;

    /** Maximum acceptable technology gap between rival empires. */
    public static final int MAX_TECH_GAP = 3;

    /** Minimum research stations per 10 solar systems. */
    public static final int MIN_RESEARCH_STATIONS_PER_SYSTEMS = 10;

    /** Threshold for low research output (research points per cycle). */
    public static final int LOW_RESEARCH_OUTPUT_THRESHOLD = 5;

    /**
     * Represents a technology category for research.
     */
    public enum TechnologyCategory {
        PROPULSION,     // Engine and drive technology
        WEAPONS,        // Weapon systems and armament
        SHIELDS,        // Defensive shield and armor
        SENSORS,        // Detection and scanning
        ENGINEERING,    // Hull construction and repair
        SCIENCE,        // Research and analysis
        MEDICAL,        // Healing and biological
        COMPUTER        // AI and computing
    }

    /**
     * Represents the research infrastructure status of a system.
     */
    public enum ResearchInfrastructureStatus {
        FULLY_EQUIPPED,     // Research lab + research station present
        PARTIALLY_EQUIPPED, // Only research lab or only research station
        NO_INFRASTRUCTURE   // No research facilities
    }

    /**
     * Represents the technology level of an empire.
     */
    public enum TechnologyLevel {
        PRIMITIVE,    // No research infrastructure
        BASIC,        // Minimal research capability
        INTERMEDIATE, // Moderate research output
        ADVANCED,     // Strong research infrastructure
        ELITE         // Maximum research capability
    }

    /**
     * Represents a research priority.
     */
    public enum ResearchPriority {
        CRITICAL,     // Must research immediately
        HIGH,         // Important for competitive advantage
        MEDIUM,       // Beneficial but not urgent
        LOW           // Nice to have
    }

    public AiAdminScienceService(GalaxyRepository galaxyRepository,
                                  PlayerRepository playerRepository,
                                  AiCharacterRepository aiCharacterRepository,
                                  ObjectProvider<AiAdminService> aiAdminServiceProvider) {
        this.galaxyRepository = galaxyRepository;
        this.playerRepository = playerRepository;
        this.aiCharacterRepository = aiCharacterRepository;
        this.aiAdminServiceProvider = aiAdminServiceProvider;
    }

    // ------------------------------------------------------------------
    // Research Infrastructure Assessment
    // ------------------------------------------------------------------

    /**
     * Assesses research infrastructure across all solar systems in the galaxy.
     * <p>
     * Maps which systems have research facilities (RESEARCH_LAB planet facilities
     * and RESEARCH space stations) and identifies gaps in coverage.
     * Strategic systems (those with planets or high mineral quality) are prioritized.
     * </p>
     *
     * @param galaxy the galaxy to assess
     * @return a research infrastructure report
     */
    @Transactional(readOnly = true)
    public ResearchInfrastructureReport assessResearchInfrastructure(Galaxy galaxy) {
        ResearchInfrastructureReport report = new ResearchInfrastructureReport();
        report.setGalaxyId(galaxy.getId());
        report.setGalaxyName(galaxy.getName());

        List<SolarSystem> systems = galaxy.getSolarSystems();
        if (systems == null || systems.isEmpty()) {
            report.setTotalSystems(0);
            report.setSystemsResearchInfrastructure(0);
            report.setInfrastructurePercentage(0.0);
            log.info("No solar systems in galaxy '{}' — no research infrastructure to assess", galaxy.getName());
            return report;
        }

        // Count research facilities across all systems
        int researchLabCount = 0;
        int researchStationCount = 0;
        int sensorArrayCount = 0;
        List<SystemResearchInfo> systemResearchInfos = new ArrayList<>();

        for (SolarSystem system : systems) {
            SystemResearchInfo systemInfo = new SystemResearchInfo();
            systemInfo.setSystemName(system.getName());
            systemInfo.setSectorRow(system.getSectorRow());
            systemInfo.setSectorCol(system.getSectorCol());

            boolean hasResearchLab = false;
            boolean hasResearchStation = false;
            boolean hasSensorArray = false;

            // Check planets for research facilities
            if (system.getPlanets() != null) {
                for (Planet planet : system.getPlanets()) {
                    // Check for RESEARCH_LAB facility
                    // Note: Planet model doesn't directly track facilities; we infer from production stats
                    // A planet with researchProduction > 0 likely has a research lab
                    if (planet.getResearchProduction() > 0) {
                        hasResearchLab = true;
                        researchLabCount++;
                    }
                }
            }

            // Check for research stations
            // Note: Space stations are tracked separately; actual station data would come from the hex layer

            // Determine infrastructure status
            if (hasResearchLab && hasResearchStation) {
                systemInfo.setInfrastructureStatus(ResearchInfrastructureStatus.FULLY_EQUIPPED);
            } else if (hasResearchLab || hasResearchStation || hasSensorArray) {
                systemInfo.setInfrastructureStatus(ResearchInfrastructureStatus.PARTIALLY_EQUIPPED);
            } else {
                systemInfo.setInfrastructureStatus(ResearchInfrastructureStatus.NO_INFRASTRUCTURE);
            }

            systemInfo.setHasResearchLab(hasResearchLab);
            systemInfo.setHasResearchStation(hasResearchStation);
            systemInfo.setHasSensorArray(hasSensorArray);
            systemResearchInfos.add(systemInfo);
        }

        report.setSystemResearchInfos(systemResearchInfos);
        report.setTotalSystems(systems.size());
        report.setResearchLabCount(researchLabCount);
        report.setResearchStationCount(researchStationCount);
        report.setSensorArrayCount(sensorArrayCount);
        report.setSystemsResearchInfrastructure(
                (int) systemResearchInfos.stream()
                        .filter(s -> s.getInfrastructureStatus() != ResearchInfrastructureStatus.NO_INFRASTRUCTURE)
                        .count()
        );
        report.setInfrastructurePercentage(
                systems.size() > 0
                        ? (double) report.getSystemsResearchInfrastructure() / systems.size() * 100
                        : 0.0
        );

        log.info("Research infrastructure for '{}': {} labs, {} stations, {} sensor arrays across {} systems ({:.1f}% coverage)",
                galaxy.getName(), researchLabCount, researchStationCount, sensorArrayCount,
                systems.size(), report.getInfrastructurePercentage());

        return report;
    }

    // ------------------------------------------------------------------
    // Technology Level Assessment
    // ------------------------------------------------------------------

    /**
     * Evaluates technology levels per empire across all technology categories.
     * <p>
     * For each empire in the galaxy, estimates the technology level based on:
     * <ul>
     *   <li>Number of research facilities (RESEARCH_LAB, RESEARCH stations)</li>
     *   <li>Empire's government type (some governments prioritize science)</li>
     *   <li>Empire's ethos (some ethos values research)</li>
     *   <li>AI character experience levels (proxy for technological knowledge)</li>
     * </ul>
     * </p>
     *
     * @param galaxy the galaxy to assess
     * @return a technology level report
     */
    @Transactional(readOnly = true)
    public TechnologyLevelReport assessTechnologyLevels(Galaxy galaxy) {
        TechnologyLevelReport report = new TechnologyLevelReport();
        report.setGalaxyId(galaxy.getId());
        report.setGalaxyName(galaxy.getName());

        // Get all empires in the galaxy
        List<Player> players = playerRepository.findAll();
        List<EmpireTechnologyProfile> empireProfiles = new ArrayList<>();

        // Get all AI characters in this galaxy to estimate research capacity
        List<AiCharacter> allAiInGalaxy = aiCharacterRepository
                .findByAiAdminTrueAndActiveTrueAndEmpireName(galaxy.getName());

        // Calculate total research capacity from AI characters
        long totalAiExperience = allAiInGalaxy.stream()
                .mapToLong(AiCharacter::getTotalExperience)
                .sum();
        double averageAiExperience = allAiInGalaxy.isEmpty() ? 0 : (double) totalAiExperience / allAiInGalaxy.size();

        for (Player player : players) {
            EmpireTechnologyProfile profile = new EmpireTechnologyProfile();
            profile.setEmpireId(player.getId());
            profile.setEmpireName(player.getEmpireName() != null && !player.getEmpireName().isEmpty()
                    ? player.getEmpireName() : player.getFirstName());

            // Estimate technology level based on available data
            int techLevel = estimateTechnologyLevel(player, averageAiExperience);
            profile.setOverallTechLevel(techLevel);
            profile.setTechLevelCategory(getTechLevelCategory(techLevel));

            // Estimate technology levels per category
            Map<String, Integer> categoryLevels = new HashMap<>();
            for (TechnologyCategory category : TechnologyCategory.values()) {
                int categoryLevel = estimateCategoryLevel(player, category, techLevel);
                categoryLevels.put(category.name(), categoryLevel);
            }
            profile.setCategoryLevels(categoryLevels);

            // Determine research priority based on tech gaps
            ResearchPriority priority = determineResearchPriority(techLevel, categoryLevels);
            profile.setResearchPriority(priority);

            empireProfiles.add(profile);
        }

        report.setEmpireProfiles(empireProfiles);
        report.setAverageTechLevel(calculateAverageTechLevel(empireProfiles));
        report.setTechDisparity(calculateTechDisparity(empireProfiles));

        log.info("Technology levels for '{}': {} empires, avg level={}, disparity={}",
                galaxy.getName(), empireProfiles.size(), report.getAverageTechLevel(), report.getTechDisparity());

        return report;
    }

    /**
     * Estimates the overall technology level for an empire.
     */
    private int estimateTechnologyLevel(Player player, double averageAiExperience) {
        int level = 1; // Start at minimum level

        // Government type influence
        if (player.getGovernmentType() != null) {
            switch (player.getGovernmentType()) {
                case DEMOCRACY:
                case CONFEDERATION:
                    level += 2; // Democratic governments tend to invest in science
                    break;
                case DICTATORSHIP:
                case AUTHORITARIAN:
                    level += 1; // Authoritarian governments may focus on military tech
                    break;
                case THEOCRACY:
                    level += 1; // Theocracies may have unique research paths
                    break;
                default:
                    level += 1;
                    break;
            }
        }

        // Ethos influence
        if (player.getEthos() != null) {
            switch (player.getEthos()) {
                case LIBERTARIAN:
                case PACIFIST:
                    level += 1; // Altruistic/pacifist empires focus on non-military tech
                    break;
                case MILITARIST:
                    level += 1; // Militarist empires focus on military tech
                    break;
                default:
                    break;
            }
        }
        // AI experience as proxy for technological knowledge
        if (averageAiExperience > 1000) {
            level += 2;
        } else if (averageAiExperience > 500) {
            level += 1;
        }

        // Cap at maximum level
        return Math.min(level, 10);
    }

    /**
     * Estimates the technology level for a specific category.
     */
    private int estimateCategoryLevel(Player player, TechnologyCategory category, int overallLevel) {
        // Base level is the overall level
        int level = overallLevel;

        // Adjust based on government/ethos alignment with category
        if (player.getEthos() != null) {
            if (player.getEthos().name().equals("MILITARIST")) {
                if (category == TechnologyCategory.WEAPONS || category == TechnologyCategory.SHIELDS) {
                    level += 1;
                } else if (category == TechnologyCategory.MEDICAL || category == TechnologyCategory.SCIENCE) {
                    level -= 1;
                }
            } else if (player.getEthos().name().equals("PACIFIST")) {
                if (category == TechnologyCategory.MEDICAL || category == TechnologyCategory.SCIENCE) {
                    level += 1;
                } else if (category == TechnologyCategory.WEAPONS) {
                    level -= 1;
                }
            }
        }

        return Math.max(1, Math.min(level, 10));
    }

    /**
     * Determines the research priority based on tech gaps.
     */
    private ResearchPriority determineResearchPriority(int overallLevel, Map<String, Integer> categoryLevels) {
        if (overallLevel <= 2) {
            return ResearchPriority.CRITICAL;
        }

        // Check for significant gaps between categories
        int minLevel = categoryLevels.values().stream().mapToInt(Integer::intValue).min().orElse(overallLevel);
        int maxLevel = categoryLevels.values().stream().mapToInt(Integer::intValue).max().orElse(overallLevel);

        if (maxLevel - minLevel >= MAX_TECH_GAP) {
            return ResearchPriority.HIGH;
        }

        if (overallLevel <= 5) {
            return ResearchPriority.MEDIUM;
        }

        return ResearchPriority.LOW;
    }

    /**
     * Gets the technology level category from a numeric level.
     */
    private TechnologyLevel getTechLevelCategory(int level) {
        if (level <= 2) return TechnologyLevel.PRIMITIVE;
        if (level <= 4) return TechnologyLevel.BASIC;
        if (level <= 6) return TechnologyLevel.INTERMEDIATE;
        if (level <= 8) return TechnologyLevel.ADVANCED;
        return TechnologyLevel.ELITE;
    }

    /**
     * Calculates the average technology level across all empires.
     */
    private int calculateAverageTechLevel(List<EmpireTechnologyProfile> profiles) {
        if (profiles.isEmpty()) return 0;
        return (int) profiles.stream().mapToInt(EmpireTechnologyProfile::getOverallTechLevel).average().orElse(0);
    }

    /**
     * Calculates the technology disparity (max - min) across all empires.
     */
    private int calculateTechDisparity(List<EmpireTechnologyProfile> profiles) {
        if (profiles.size() < 2) return 0;
        int max = profiles.stream().mapToInt(EmpireTechnologyProfile::getOverallTechLevel).max().orElse(0);
        int min = profiles.stream().mapToInt(EmpireTechnologyProfile::getOverallTechLevel).min().orElse(0);
        return max - min;
    }

    // ------------------------------------------------------------------
    // R&D Progress Assessment
    // ------------------------------------------------------------------

    /**
     * Monitors R&D progress across the galaxy.
     * <p>
     * Evaluates the research output of all empires and identifies:
     * <ul>
     *   <li>Empires with no research output (stalled R&D)</li>
     *   <li>Technology categories with low overall progress</li>
     *   <li>Research efficiency metrics</li>
     *   <li>Recommended research priorities</li>
     * </ul>
     * </p>
     *
     * @param galaxy the galaxy to assess
     * @return an R&D progress report
     */
    @Transactional(readOnly = true)
    public RdProgressReport assessRnDProgress(Galaxy galaxy) {
        RdProgressReport report = new RdProgressReport();
        report.setGalaxyId(galaxy.getId());
        report.setGalaxyName(galaxy.getName());

        // Get all AI characters with research-related roles
        List<AiCharacter> allAiInGalaxy = aiCharacterRepository
                .findByAiAdminTrueAndActiveTrueAndEmpireName(galaxy.getName());

        int researchFocusedAgents = 0;
        for (AiCharacter ai : allAiInGalaxy) {
            String role = ai.getRole() != null ? ai.getRole().toLowerCase() : "";
            if (role.contains("science") || role.contains("research") || role.contains("overseer")) {
                researchFocusedAgents++;
            }
        }

        report.setResearchFocusedAgents(researchFocusedAgents);

        // Estimate research output based on AI experience and facility count
        long totalExperience = allAiInGalaxy.stream()
                .mapToLong(AiCharacter::getTotalExperience)
                .sum();

        // Research output is proportional to experience and research-focused agents
        int estimatedResearchOutput = (int) (totalExperience / 100) + (researchFocusedAgents * 5);
        report.setEstimatedResearchOutput(estimatedResearchOutput);

        // Assess each technology category
        List<CategoryProgressInfo> categoryProgresses = new ArrayList<>();
        for (TechnologyCategory category : TechnologyCategory.values()) {
            CategoryProgressInfo categoryInfo = new CategoryProgressInfo();
            categoryInfo.setCategory(category.name());

            // Estimate progress based on available data
            int progress = estimateCategoryProgress(galaxy, category, researchFocusedAgents, totalExperience);
            categoryInfo.setProgressPercentage(Math.min(100, progress));
            categoryInfo.setStalled(progress < LOW_RESEARCH_OUTPUT_THRESHOLD);

            categoryProgresses.add(categoryInfo);
        }
        report.setCategoryProgresses(categoryProgresses);

        // Calculate overall research efficiency
        double efficiency = calculateResearchEfficiency(researchFocusedAgents, estimatedResearchOutput, allAiInGalaxy.size());
        report.setResearchEfficiency(efficiency);

        // Determine stalled categories
        List<String> stalledCategories = categoryProgresses.stream()
                .filter(CategoryProgressInfo::isStalled)
                .map(CategoryProgressInfo::getCategory)
                .toList();
        report.setStalledCategories(stalledCategories);

        log.info("R&D progress for '{}': {} research agents, output={}, efficiency={:.1f}%, stalled categories={}",
                galaxy.getName(), researchFocusedAgents, estimatedResearchOutput,
                efficiency, stalledCategories.size());

        return report;
    }

    /**
     * Estimates the progress for a specific technology category.
     */
    private int estimateCategoryProgress(Galaxy galaxy, TechnologyCategory category,
                                          int researchAgents, long totalExperience) {
        // Base progress from experience
        int progress = (int) (totalExperience / 500);

        // Bonus for research-focused agents
        progress += researchAgents * 10;

        // Category-specific adjustments
        switch (category) {
            case PROPULSION:
            case ENGINEERING:
                progress += 5; // Core technologies tend to advance faster
                break;
            case WEAPONS:
            case SHIELDS:
                progress += 3; // Military tech advances steadily
                break;
            case SCIENCE:
            case COMPUTER:
                progress += researchAgents * 5; // Science benefits most from research agents
                break;
            default:
                break;
        }

        return Math.min(100, progress);
    }

    /**
     * Calculates research efficiency as a percentage.
     */
    private double calculateResearchEfficiency(int researchAgents, int output, int totalAgents) {
        if (totalAgents == 0) return 0;
        double agentRatio = (double) researchAgents / totalAgents;
        double outputPerAgent = totalAgents > 0 ? (double) output / totalAgents : 0;
        // Efficiency is a combination of agent ratio and output per agent
        return Math.min(100, (agentRatio * 50) + (outputPerAgent * 0.5));
    }

    // ------------------------------------------------------------------
    // Scientific Collaboration Assessment
    // ------------------------------------------------------------------

    /**
     * Assesses scientific collaboration opportunities between empires.
     * <p>
     * Evaluates which empires could benefit from research partnerships based on:
     * <ul>
     *   <li>Complementary technology strengths</li>
     *   <li>Diplomatic relations (friendly empires are more likely to collaborate)</li>
     *   <li>Research infrastructure proximity</li>
     * </ul>
     * </p>
     *
     * @param galaxy the galaxy to assess
     * @return a collaboration assessment report
     */
    @Transactional(readOnly = true)
    public CollaborationReport assessScientificCollaboration(Galaxy galaxy) {
        CollaborationReport report = new CollaborationReport();
        report.setGalaxyId(galaxy.getId());
        report.setGalaxyName(galaxy.getName());

        List<Player> players = playerRepository.findAll();
        List<CollaborationOpportunity> opportunities = new ArrayList<>();

        // Evaluate every pair of empires for collaboration potential
        for (int i = 0; i < players.size(); i++) {
            for (int j = i + 1; j < players.size(); j++) {
                Player empireA = players.get(i);
                Player empireB = players.get(j);

                // Check if empires have complementary strengths
                boolean hasComplementaryStrengths = false;
                if (empireA.getEthos() != null && empireB.getEthos() != null) {
                    // Different ethos often means different research priorities = complementary
                    if (!empireA.getEthos().equals(empireB.getEthos())) {
                        hasComplementaryStrengths = true;
                    }
                }

                // Check diplomatic compatibility
                boolean diplomaticallyCompatible = areDiplomaticallyCompatible(empireA, empireB);

                if (hasComplementaryStrengths || diplomaticallyCompatible) {
                    CollaborationOpportunity opportunity = new CollaborationOpportunity();
                    opportunity.setEmpireA(empireA.getEmpireName() != null && !empireA.getEmpireName().isEmpty()
                            ? empireA.getEmpireName() : empireA.getFirstName());
                    opportunity.setEmpireB(empireB.getEmpireName() != null && !empireB.getEmpireName().isEmpty()
                            ? empireB.getEmpireName() : empireB.getFirstName());
                    opportunity.setHasComplementaryStrengths(hasComplementaryStrengths);
                    opportunity.setDiplomaticallyCompatible(diplomaticallyCompatible);
                    opportunity.setPotentialBenefit(calculateCollaborationBenefit(hasComplementaryStrengths, diplomaticallyCompatible));
                    opportunities.add(opportunity);
                }
            }
        }

        report.setCollaborationOpportunities(opportunities);
        report.setTotalOpportunities(opportunities.size());

        log.info("Scientific collaboration for '{}': {} potential opportunities",
                galaxy.getName(), opportunities.size());

        return report;
    }

    /**
     * Checks if two empires are diplomatically compatible for collaboration.
     */
    private boolean areDiplomaticallyCompatible(Player empireA, Player empireB) {
        if (empireA.getGovernmentType() == null || empireB.getGovernmentType() == null) {
            return true; // Unknown government types are assumed compatible
        }

        // Hostile government combinations
        if (isHostileGovernmentPair(empireA.getGovernmentType(), empireB.getGovernmentType())) {
            return false;
        }

        // Hostile ethos combinations
        if (empireA.getEthos() != null && empireB.getEthos() != null) {
            if (isHostileEthosPair(empireA.getEthos(), empireB.getEthos())) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks if two government types are hostile to each other.
     */
    private boolean isHostileGovernmentPair(Player.GovernmentType govA,
                                             Player.GovernmentType govB) {
        // Democracies and dictatorships are naturally hostile
        boolean isDemocracyA = govA == Player.GovernmentType.DEMOCRACY ||
                               govA == Player.GovernmentType.CONFEDERATION;
        boolean isDictatorshipA = govA == Player.GovernmentType.DICTATORSHIP ||
                                  govA == Player.GovernmentType.AUTHORITARIAN;

        boolean isDemocracyB = govB == Player.GovernmentType.DEMOCRACY ||
                               govB == Player.GovernmentType.CONFEDERATION;
        boolean isDictatorshipB = govB == Player.GovernmentType.DICTATORSHIP ||
                                  govB == Player.GovernmentType.AUTHORITARIAN;

        return (isDemocracyA && isDictatorshipB) || (isDictatorshipA && isDemocracyB);
    }

    /**
     * Checks if two ethos values are hostile to each other.
     */
    private boolean isHostileEthosPair(Player.Ethos ethosA,
                                        Player.Ethos ethosB) {
        // Militarist and Pacifist are naturally hostile
        return (ethosA == Player.Ethos.MILITARIST &&
                ethosB == Player.Ethos.PACIFIST) ||
               (ethosA == Player.Ethos.PACIFIST &&
                ethosB == Player.Ethos.MILITARIST);
    }

    /**
     * Calculates the potential benefit of a collaboration.
     */
    private int calculateCollaborationBenefit(boolean complementaryStrengths, boolean diplomaticallyCompatible) {
        int benefit = 0;
        if (complementaryStrengths) benefit += 50;
        if (diplomaticallyCompatible) benefit += 50;
        return benefit;
    }

    // ------------------------------------------------------------------
    // Science Work Order Creation
    // ------------------------------------------------------------------

    /**
     * Creates prioritized work orders for science operations based on assessment findings.
     * <p>
     * Work order priority:
     * <ol>
     *   <li>CRITICAL — Build research lab when none exist</li>
     *   <li>CRITICAL — Fund emergency research for empire with zero tech level</li>
     *   <li>HIGH — Build research station in strategic sector</li>
     *   <li>HIGH — Address technology gap between rival empires</li>
     *   <li>MEDIUM — Improve research output in stalled category</li>
     *   <li>MEDIUM — Establish research collaboration</li>
     *   <li>LOW — Expand research infrastructure</li>
     * </ol>
     * </p>
     *
     * @param galaxy the galaxy to create work orders for
     * @return list of created work order descriptions
     */
    @Transactional
    public List<String> createScienceWorkOrders(Galaxy galaxy) {
        List<String> ordersCreated = new ArrayList<>();

        // Assess current state
        ResearchInfrastructureReport infraReport = assessResearchInfrastructure(galaxy);
        TechnologyLevelReport techReport = assessTechnologyLevels(galaxy);
        RdProgressReport rdReport = assessRnDProgress(galaxy);
        CollaborationReport collabReport = assessScientificCollaboration(galaxy);

        String submittedBy = "GalaxyOverseer:" + galaxy.getName();

        AiAdminService aiAdminService = aiAdminServiceProvider.getIfAvailable();
        if (aiAdminService == null) {
            log.warn("AiAdminService not available for science work orders in '{}'", galaxy.getName());
            return ordersCreated;
        }

        // CRITICAL: No research infrastructure at all
        if (infraReport.getResearchLabCount() == 0 && infraReport.getTotalSystems() > 0) {
            String orderText = String.format(
                    "[CRITICAL] No research infrastructure in galaxy '%s' — %d systems with no research labs. " +
                    "Action: Build first RESEARCH_LAB facility to enable technology research.",
                    galaxy.getName(), infraReport.getTotalSystems());
            aiAdminService.submitOrder(submittedBy, orderText);
            ordersCreated.add(orderText);
        }

        // CRITICAL: Empire with zero technology level
        for (EmpireTechnologyProfile profile : techReport.getEmpireProfiles()) {
            if (profile.getOverallTechLevel() <= 1) {
                String orderText = String.format(
                        "[CRITICAL] Empire '%s' has technology level %d (category: %s) — cannot build advanced items. " +
                        "Action: Allocate research resources to boost technology level.",
                        profile.getEmpireName(), profile.getOverallTechLevel(), profile.getTechLevelCategory());
                aiAdminService.submitOrder(submittedBy, orderText);
                ordersCreated.add(orderText);
            }
        }

        // HIGH: Research lab shortage
        int empireCount = techReport.getEmpireProfiles().size();
        if (empireCount > 0 && infraReport.getResearchLabCount() < empireCount * MIN_RESEARCH_LABS_PER_EMPIRE) {
            int needed = (empireCount * MIN_RESEARCH_LABS_PER_EMPIRE) - infraReport.getResearchLabCount();
            String orderText = String.format(
                    "[HIGH] Research lab shortage in '%s': %d labs for %d empires — need %d more. " +
                    "Action: Build additional RESEARCH_LAB facilities.",
                    galaxy.getName(), infraReport.getResearchLabCount(), empireCount, needed);
            aiAdminService.submitOrder(submittedBy, orderText);
            ordersCreated.add(orderText);
        }

        // HIGH: Technology gap between rival empires
        if (techReport.getTechDisparity() >= MAX_TECH_GAP) {
            String orderText = String.format(
                    "[HIGH] Technology disparity of %d levels between empires in '%s' — risk of technological dominance. " +
                    "Action: Balance technology levels through targeted research investment.",
                    techReport.getTechDisparity(), galaxy.getName());
            aiAdminService.submitOrder(submittedBy, orderText);
            ordersCreated.add(orderText);
        }

        // MEDIUM: Stalled research categories
        for (String stalledCategory : rdReport.getStalledCategories()) {
            String orderText = String.format(
                    "[MEDIUM] Research category '%s' has stalled progress in '%s'. " +
                    "Action: Invest resources in %s research to restore progress.",
                    stalledCategory, galaxy.getName(), stalledCategory);
            aiAdminService.submitOrder(submittedBy, orderText);
            ordersCreated.add(orderText);
        }

        // MEDIUM: Research collaboration opportunities
        for (CollaborationOpportunity opportunity : collabReport.getCollaborationOpportunities()) {
            if (opportunity.getPotentialBenefit() >= 80) {
                String orderText = String.format(
                        "[MEDIUM] High-value collaboration opportunity in '%s': %s + %s (benefit: %d%%). " +
                        "Action: Facilitate research collaboration.",
                        galaxy.getName(), opportunity.getEmpireA(), opportunity.getEmpireB(),
                        opportunity.getPotentialBenefit());
                aiAdminService.submitOrder(submittedBy, orderText);
                ordersCreated.add(orderText);
            }
        }

        // LOW: Expand research infrastructure
        if (infraReport.getInfrastructurePercentage() < 50 && infraReport.getTotalSystems() > 0) {
            String orderText = String.format(
                    "[LOW] Research infrastructure coverage in '%s' is only %.1f%% — consider expanding. " +
                    "Action: Build additional research facilities.",
                    galaxy.getName(), infraReport.getInfrastructurePercentage());
            aiAdminService.submitOrder(submittedBy, orderText);
            ordersCreated.add(orderText);
        }

        log.info("Created {} science work orders for galaxy '{}'", ordersCreated.size(), galaxy.getName());
        return ordersCreated;
    }

    // ------------------------------------------------------------------
    // Integration Method
    // ------------------------------------------------------------------

    /**
     * Integration method called by GalaxyOverseerService during the 5-minute health check cycle.
     * <p>
     * Performs a comprehensive science assessment and adds findings to the galaxy assessment.
     * </p>
     *
     * @param galaxy the galaxy to assess
     * @param assessment the galaxy assessment to add findings to
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void assessScienceNeeds(Galaxy galaxy, GalaxyOverseerService.GalaxyAssessment assessment) {
        try {
            ResearchInfrastructureReport infraReport = assessResearchInfrastructure(galaxy);

            // Check for critical lack of research infrastructure
            if (infraReport.getResearchLabCount() == 0 && infraReport.getTotalSystems() > 0) {
                assessment.addFinding(GalaxyOverseerService.GalaxyAssessment.Severity.CRITICAL,
                        "No research labs in galaxy — no technology research possible.");
                assessment.addRecommendation("Build RESEARCH_LAB facility immediately to enable technology progression.");
            } else if (infraReport.getInfrastructurePercentage() < 20) {
                assessment.addFinding(GalaxyOverseerService.GalaxyAssessment.Severity.HIGH,
                        "Research infrastructure coverage is only " +
                                String.format("%.1f", infraReport.getInfrastructurePercentage()) +
                                "% — technology progression will be slow.");
                assessment.addRecommendation("Expand research infrastructure to at least 20% of systems.");
            }

            // Check technology levels
            TechnologyLevelReport techReport = assessTechnologyLevels(galaxy);
            if (techReport.getAverageTechLevel() <= 1 && techReport.getEmpireProfiles().size() > 0) {
                assessment.addFinding(GalaxyOverseerService.GalaxyAssessment.Severity.HIGH,
                        "Average technology level is " + techReport.getAverageTechLevel() +
                                " — empires are technologically primitive.");
                assessment.addRecommendation("Fund research programs to boost technology levels across all empires.");
            }

            if (techReport.getTechDisparity() >= MAX_TECH_GAP) {
                assessment.addFinding(GalaxyOverseerService.GalaxyAssessment.Severity.MEDIUM,
                        "Technology disparity of " + techReport.getTechDisparity() +
                                " levels between empires — potential for technological dominance.");
                assessment.addRecommendation("Balance technology levels through targeted research investment.");
            }

            // Check R&D progress
            RdProgressReport rdReport = assessRnDProgress(galaxy);
            if (!rdReport.getStalledCategories().isEmpty()) {
                assessment.addFinding(GalaxyOverseerService.GalaxyAssessment.Severity.MEDIUM,
                        rdReport.getStalledCategories().size() +
                                " research categories have stalled progress: " +
                                String.join(", ", rdReport.getStalledCategories()));
                assessment.addRecommendation("Invest resources in stalled research categories to restore progress.");
            }

            if (rdReport.getResearchEfficiency() < 20 && rdReport.getResearchFocusedAgents() > 0) {
                assessment.addFinding(GalaxyOverseerService.GalaxyAssessment.Severity.LOW,
                        "Research efficiency is only " + String.format("%.1f", rdReport.getResearchEfficiency()) +
                                "% — research resources may be misallocated.");
                assessment.addRecommendation("Review research resource allocation and optimize research agent assignments.");
            }

            log.info("Science assessment for '{}': {} findings added", galaxy.getName(),
                    assessment.getFindings().size());

        } catch (Exception e) {
            log.error("Science assessment failed for galaxy '{}': {}", galaxy.getName(), e.getMessage(), e);
        }
    }

    // ------------------------------------------------------------------
    // DTOs
    // ------------------------------------------------------------------

    /**
     * Report on research infrastructure across a galaxy.
     */
    public static class ResearchInfrastructureReport {
        private Long galaxyId;
        private String galaxyName;
        private int totalSystems;
        private int systemsResearchInfrastructure;
        private double infrastructurePercentage;
        private int researchLabCount;
        private int researchStationCount;
        private int sensorArrayCount;
        private List<SystemResearchInfo> systemResearchInfos;

        public Long getGalaxyId() { return galaxyId; }
        public void setGalaxyId(Long galaxyId) { this.galaxyId = galaxyId; }
        public String getGalaxyName() { return galaxyName; }
        public void setGalaxyName(String galaxyName) { this.galaxyName = galaxyName; }
        public int getTotalSystems() { return totalSystems; }
        public void setTotalSystems(int totalSystems) { this.totalSystems = totalSystems; }
        public int getSystemsResearchInfrastructure() { return systemsResearchInfrastructure; }
        public void setSystemsResearchInfrastructure(int systemsResearchInfrastructure) { this.systemsResearchInfrastructure = systemsResearchInfrastructure; }
        public double getInfrastructurePercentage() { return infrastructurePercentage; }
        public void setInfrastructurePercentage(double infrastructurePercentage) { this.infrastructurePercentage = infrastructurePercentage; }
        public int getResearchLabCount() { return researchLabCount; }
        public void setResearchLabCount(int researchLabCount) { this.researchLabCount = researchLabCount; }
        public int getResearchStationCount() { return researchStationCount; }
        public void setResearchStationCount(int researchStationCount) { this.researchStationCount = researchStationCount; }
        public int getSensorArrayCount() { return sensorArrayCount; }
        public void setSensorArrayCount(int sensorArrayCount) { this.sensorArrayCount = sensorArrayCount; }
        public List<SystemResearchInfo> getSystemResearchInfos() { return systemResearchInfos; }
        public void setSystemResearchInfos(List<SystemResearchInfo> systemResearchInfos) { this.systemResearchInfos = systemResearchInfos; }
    }

    /**
     * Research infrastructure info for a single system.
     */
    public static class SystemResearchInfo {
        private String systemName;
        private int sectorRow;
        private int sectorCol;
        private boolean hasResearchLab;
        private boolean hasResearchStation;
        private boolean hasSensorArray;
        private ResearchInfrastructureStatus infrastructureStatus;

        public String getSystemName() { return systemName; }
        public void setSystemName(String systemName) { this.systemName = systemName; }
        public int getSectorRow() { return sectorRow; }
        public void setSectorRow(int sectorRow) { this.sectorRow = sectorRow; }
        public int getSectorCol() { return sectorCol; }
        public void setSectorCol(int sectorCol) { this.sectorCol = sectorCol; }
        public boolean isHasResearchLab() { return hasResearchLab; }
        public void setHasResearchLab(boolean hasResearchLab) { this.hasResearchLab = hasResearchLab; }
        public boolean isHasResearchStation() { return hasResearchStation; }
        public void setHasResearchStation(boolean hasResearchStation) { this.hasResearchStation = hasResearchStation; }
        public boolean isHasSensorArray() { return hasSensorArray; }
        public void setHasSensorArray(boolean hasSensorArray) { this.hasSensorArray = hasSensorArray; }
        public ResearchInfrastructureStatus getInfrastructureStatus() { return infrastructureStatus; }
        public void setInfrastructureStatus(ResearchInfrastructureStatus infrastructureStatus) { this.infrastructureStatus = infrastructureStatus; }
    }

    /**
     * Report on technology levels across all empires in a galaxy.
     */
    public static class TechnologyLevelReport {
        private Long galaxyId;
        private String galaxyName;
        private List<EmpireTechnologyProfile> empireProfiles;
        private int averageTechLevel;
        private int techDisparity;

        public Long getGalaxyId() { return galaxyId; }
        public void setGalaxyId(Long galaxyId) { this.galaxyId = galaxyId; }
        public String getGalaxyName() { return galaxyName; }
        public void setGalaxyName(String galaxyName) { this.galaxyName = galaxyName; }
        public List<EmpireTechnologyProfile> getEmpireProfiles() { return empireProfiles; }
        public void setEmpireProfiles(List<EmpireTechnologyProfile> empireProfiles) { this.empireProfiles = empireProfiles; }
        public int getAverageTechLevel() { return averageTechLevel; }
        public void setAverageTechLevel(int averageTechLevel) { this.averageTechLevel = averageTechLevel; }
        public int getTechDisparity() { return techDisparity; }
        public void setTechDisparity(int techDisparity) { this.techDisparity = techDisparity; }
    }

    /**
     * Technology profile for a single empire.
     */
    public static class EmpireTechnologyProfile {
        private Long empireId;
        private String empireName;
        private int overallTechLevel;
        private TechnologyLevel techLevelCategory;
        private Map<String, Integer> categoryLevels;
        private ResearchPriority researchPriority;

        public Long getEmpireId() { return empireId; }
        public void setEmpireId(Long empireId) { this.empireId = empireId; }
        public String getEmpireName() { return empireName; }
        public void setEmpireName(String empireName) { this.empireName = empireName; }
        public int getOverallTechLevel() { return overallTechLevel; }
        public void setOverallTechLevel(int overallTechLevel) { this.overallTechLevel = overallTechLevel; }
        public TechnologyLevel getTechLevelCategory() { return techLevelCategory; }
        public void setTechLevelCategory(TechnologyLevel techLevelCategory) { this.techLevelCategory = techLevelCategory; }
        public Map<String, Integer> getCategoryLevels() { return categoryLevels; }
        public void setCategoryLevels(Map<String, Integer> categoryLevels) { this.categoryLevels = categoryLevels; }
        public ResearchPriority getResearchPriority() { return researchPriority; }
        public void setResearchPriority(ResearchPriority researchPriority) { this.researchPriority = researchPriority; }
    }

    /**
     * Report on R&D progress across the galaxy.
     */
    public static class RdProgressReport {
        private Long galaxyId;
        private String galaxyName;
        private int researchFocusedAgents;
        private int estimatedResearchOutput;
        private double researchEfficiency;
        private List<CategoryProgressInfo> categoryProgresses;
        private List<String> stalledCategories;

        public Long getGalaxyId() { return galaxyId; }
        public void setGalaxyId(Long galaxyId) { this.galaxyId = galaxyId; }
        public String getGalaxyName() { return galaxyName; }
        public void setGalaxyName(String galaxyName) { this.galaxyName = galaxyName; }
        public int getResearchFocusedAgents() { return researchFocusedAgents; }
        public void setResearchFocusedAgents(int researchFocusedAgents) { this.researchFocusedAgents = researchFocusedAgents; }
        public int getEstimatedResearchOutput() { return estimatedResearchOutput; }
        public void setEstimatedResearchOutput(int estimatedResearchOutput) { this.estimatedResearchOutput = estimatedResearchOutput; }
        public double getResearchEfficiency() { return researchEfficiency; }
        public void setResearchEfficiency(double researchEfficiency) { this.researchEfficiency = researchEfficiency; }
        public List<CategoryProgressInfo> getCategoryProgresses() { return categoryProgresses; }
        public void setCategoryProgresses(List<CategoryProgressInfo> categoryProgresses) { this.categoryProgresses = categoryProgresses; }
        public List<String> getStalledCategories() { return stalledCategories; }
        public void setStalledCategories(List<String> stalledCategories) { this.stalledCategories = stalledCategories; }
    }

    /**
     * Progress info for a single technology category.
     */
    public static class CategoryProgressInfo {
        private String category;
        private int progressPercentage;
        private boolean isStalled;

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public int getProgressPercentage() { return progressPercentage; }
        public void setProgressPercentage(int progressPercentage) { this.progressPercentage = progressPercentage; }
        public boolean isStalled() { return isStalled; }
        public void setStalled(boolean stalled) { isStalled = stalled; }
    }

    /**
     * Report on scientific collaboration opportunities.
     */
    public static class CollaborationReport {
        private Long galaxyId;
        private String galaxyName;
        private List<CollaborationOpportunity> collaborationOpportunities;
        private int totalOpportunities;

        public Long getGalaxyId() { return galaxyId; }
        public void setGalaxyId(Long galaxyId) { this.galaxyId = galaxyId; }
        public String getGalaxyName() { return galaxyName; }
        public void setGalaxyName(String galaxyName) { this.galaxyName = galaxyName; }
        public List<CollaborationOpportunity> getCollaborationOpportunities() { return collaborationOpportunities; }
        public void setCollaborationOpportunities(List<CollaborationOpportunity> collaborationOpportunities) { this.collaborationOpportunities = collaborationOpportunities; }
        public int getTotalOpportunities() { return totalOpportunities; }
        public void setTotalOpportunities(int totalOpportunities) { this.totalOpportunities = totalOpportunities; }
    }

    /**
     * A collaboration opportunity between two empires.
     */
    public static class CollaborationOpportunity {
        private String empireA;
        private String empireB;
        private boolean hasComplementaryStrengths;
        private boolean diplomaticallyCompatible;
        private int potentialBenefit;

        public String getEmpireA() { return empireA; }
        public void setEmpireA(String empireA) { this.empireA = empireA; }
        public String getEmpireB() { return empireB; }
        public void setEmpireB(String empireB) { this.empireB = empireB; }
        public boolean isHasComplementaryStrengths() { return hasComplementaryStrengths; }
        public void setHasComplementaryStrengths(boolean hasComplementaryStrengths) { this.hasComplementaryStrengths = hasComplementaryStrengths; }
        public boolean isDiplomaticallyCompatible() { return diplomaticallyCompatible; }
        public void setDiplomaticallyCompatible(boolean diplomaticallyCompatible) { this.diplomaticallyCompatible = diplomaticallyCompatible; }
        public int getPotentialBenefit() { return potentialBenefit; }
        public void setPotentialBenefit(int potentialBenefit) { this.potentialBenefit = potentialBenefit; }
    }
}
