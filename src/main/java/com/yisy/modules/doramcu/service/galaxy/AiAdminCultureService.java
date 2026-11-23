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

/**
 * AI Admin Culture Service — manages cultural identity, propaganda, morale,
 * and cultural influence across a galaxy.
 * <p>
 * This is the domain-specific sub-manager that reports to the Galaxy Overseer.
 * It runs as part of the scheduled health check and performs these functions:
 * <ul>
 *   <li>Assesses cultural identity and cohesion per empire</li>
 *   <li>Evaluates propaganda and information infrastructure</li>
 *   <li>Monitors morale levels across planets and systems</li>
 *   <li>Tracks cultural influence between empires</li>
 *   <li>Creates work orders for culture operations (build cultural facilities, launch propaganda)</li>
 *   <li>Assesses cultural diversity and assimilation opportunities</li>
 *   <li>Evaluates cultural stability and detects unrest risks</li>
 * </ul>
 * </p>
 * <p>
 * Culture priority order for work order creation:
 * <ol>
 *   <li>CRITICAL — Empire with zero morale (imminent revolt risk)</li>
 *   <li>CRITICAL — No cultural infrastructure in the galaxy</li>
 *   <li>HIGH — Morale crisis affecting multiple planets</li>
 *   <li>HIGH — Cultural unrest detected in strategic systems</li>
 *   <li>MEDIUM — Low cultural cohesion between empire planets</li>
 *   <li>MEDIUM — No propaganda infrastructure for empire</li>
 *   <li>LOW — Recommend expanding cultural influence</li>
 * </ol>
 * </p>
 * <p>
 * Cultural categories:
 * <ul>
 *   <li>IDENTITY — Core cultural identity and heritage</li>
 *   <li>PROPAGANDA — Information control and messaging</li>
 *   <li>MORALE — Population happiness and satisfaction</li>
 *   <li>INFLUENCE — Cultural soft power and reach</li>
 *   <li>COHESION — Internal unity and social stability</li>
 *   <li>DIVERSITY — Cultural variety and tolerance</li>
 * </ul>
 * </p>
 * <p>
 * Cultural facility types:
 * <ul>
 *   <li>TRADE_PORT (planet facility) — Cultural exchange hub</li>
 *   <li>COLONY_DOME (planet facility) — Population center, cultural anchor</li>
 *   <li>COLONY (space station) — Cultural outpost</li>
 *   <li>TRADE (space station) — Cultural trade hub</li>
 * </ul>
 * </p>
 */
@Service
public class AiAdminCultureService {

    private static final Logger log = LoggerFactory.getLogger(AiAdminCultureService.class);

    private final GalaxyRepository galaxyRepository;
    private final PlayerRepository playerRepository;
    private final AiCharacterRepository aiCharacterRepository;
    private final ObjectProvider<AiAdminService> aiAdminServiceProvider;

    /** Minimum cultural facilities per empire for adequate cultural presence. */
    public static final int MIN_CULTURAL_FACILITIES_PER_EMPIRE = 1;

    /** Maximum acceptable morale gap between planets in the same empire. */
    public static final int MAX_MORALE_GAP = 30;

    /** Threshold for low morale (0-100 scale). */
    public static final int LOW_MORALE_THRESHOLD = 40;

    /** Threshold for critical morale (0-100 scale). */
    public static final int CRITICAL_MORALE_THRESHOLD = 20;

    /** Threshold for cultural unrest risk. */
    public static final int UNREST_RISK_THRESHOLD = 25;

    /**
     * Represents a cultural category for assessment.
     */
    public enum CultureCategory {
        IDENTITY,     // Core cultural identity and heritage
        PROPAGANDA,   // Information control and messaging
        MORALE,       // Population happiness and satisfaction
        INFLUENCE,    // Cultural soft power and reach
        COHESION,     // Internal unity and social stability
        DIVERSITY     // Cultural variety and tolerance
    }

    /**
     * Represents the cultural stability of a planet or system.
     */
    public enum CulturalStability {
        STABLE,           // High morale, strong cohesion
        MODERATE,         // Acceptable morale, some concerns
        UNREST_RISK,      // Low morale, unrest possible
        CRISIS            // Critical morale, revolt imminent
    }

    /**
     * Represents the cultural influence level of an empire.
     */
    public enum CulturalInfluenceLevel {
        ISOLATED,     // No cultural reach beyond own systems
        REGIONAL,     // Influence within local sector
        GALACTIC,     // Galaxy-wide cultural presence
        DOMINANT      // Cultural hegemony across galaxy
    }

    /**
     * Represents a culture work order priority.
     */
    public enum CulturePriority {
        CRITICAL,     // Must address immediately
        HIGH,         // Important for stability
        MEDIUM,       // Beneficial but not urgent
        LOW           // Nice to have
    }

    public AiAdminCultureService(GalaxyRepository galaxyRepository,
                                  PlayerRepository playerRepository,
                                  AiCharacterRepository aiCharacterRepository,
                                  ObjectProvider<AiAdminService> aiAdminServiceProvider) {
        this.galaxyRepository = galaxyRepository;
        this.playerRepository = playerRepository;
        this.aiCharacterRepository = aiCharacterRepository;
        this.aiAdminServiceProvider = aiAdminServiceProvider;
    }

    // ------------------------------------------------------------------
    // Cultural Identity Assessment
    // ------------------------------------------------------------------

    /**
     * Assesses cultural identity and cohesion per empire in the galaxy.
     * <p>
     * For each empire, evaluates:
     * <ul>
     *   <li>Cultural identity strength based on government type and ethos alignment</li>
     *   <li>Internal cohesion based on planet count and distribution</li>
     *   <li>Cultural infrastructure presence (TRADE_PORT, COLONY_DOME facilities)</li>
     *   <li>Government-ethos harmony score</li>
     * </ul>
     * </p>
     *
     * @param galaxy the galaxy to assess
     * @return a cultural identity report
     */
    @Transactional(readOnly = true)
    public CulturalIdentityReport assessCulturalIdentity(Galaxy galaxy) {
        CulturalIdentityReport report = new CulturalIdentityReport();
        report.setGalaxyId(galaxy.getId());
        report.setGalaxyName(galaxy.getName());

        List<Player> players = playerRepository.findAll();
        List<EmpireCulturalProfile> empireProfiles = new ArrayList<>();

        for (Player player : players) {
            EmpireCulturalProfile profile = new EmpireCulturalProfile();
            profile.setEmpireId(player.getId());
            profile.setEmpireName(player.getEmpireName() != null && !player.getEmpireName().isEmpty()
                    ? player.getEmpireName() : player.getFirstName());

            // Calculate government-ethos harmony
            int harmonyScore = calculateGovernmentEthosHarmony(player);
            profile.setGovernmentEthosHarmony(harmonyScore);

            // Estimate cultural identity strength
            int identityStrength = estimateCulturalIdentityStrength(player, harmonyScore);
            profile.setCulturalIdentityStrength(identityStrength);

            // Estimate cultural cohesion
            int cohesionScore = estimateCulturalCohesion(player, galaxy);
            profile.setCulturalCohesion(cohesionScore);

            // Determine cultural stability
            CulturalStability stability = determineCulturalStability(identityStrength, cohesionScore, harmonyScore);
            profile.setCulturalStability(stability);

            // Estimate cultural influence level
            CulturalInfluenceLevel influence = estimateCulturalInfluence(player, galaxy, identityStrength);
            profile.setCulturalInfluenceLevel(influence);

            empireProfiles.add(profile);
        }

        report.setEmpireProfiles(empireProfiles);
        report.setAverageIdentityStrength(calculateAverageIdentityStrength(empireProfiles));
        report.setAverageCohesion(calculateAverageCohesion(empireProfiles));
        report.setCulturalDiversityIndex(calculateCulturalDiversityIndex(empireProfiles));

        log.info("Cultural identity for '{}': {} empires, avg identity={}, avg cohesion={}, diversity={}",
                galaxy.getName(), empireProfiles.size(), report.getAverageIdentityStrength(),
                report.getAverageCohesion(), report.getCulturalDiversityIndex());

        return report;
    }

    /**
     * Calculates the harmony score between government type and ethos (0-100).
     * <p>
     * Some government-ethos combinations are naturally more harmonious:
     * <ul>
     *   <li>DEMOCRACY + LIBERTARIAN = 90 (natural alignment)</li>
     *   <li>DEMOCRACY + EQUALIST = 85 (shared values)</li>
     *   <li>AUTHORITARIAN + MILITARIST = 85 (order and discipline)</li>
     *   <li>DICTATORSHIP + MATERIALIST = 80 (pragmatic control)</li>
     *   <li>THEOCRACY + SPIRITUALIST = 95 (perfect alignment)</li>
     *   <li>CORPORATE + MATERIALIST = 90 (profit-driven)</li>
     *   <li>MILITARIST + PACIFIST = 20 (fundamental conflict)</li>
     * </ul>
     * </p>
     */
    private int calculateGovernmentEthosHarmony(Player player) {
        if (player.getGovernmentType() == null || player.getEthos() == null) {
            return 50; // Default neutral harmony
        }

        int harmony = 50; // Base harmony

        // Government-ethos compatibility matrix
        switch (player.getGovernmentType()) {
            case DEMOCRACY:
                switch (player.getEthos()) {
                    case LIBERTARIAN: harmony = 90; break;
                    case EQUALIST: harmony = 85; break;
                    case PACIFIST: harmony = 75; break;
                    case MATERIALIST: harmony = 65; break;
                    case SPIRITUALIST: harmony = 60; break;
                    case MILITARIST: harmony = 40; break;
                    default: harmony = 55; break;
                }
                break;
            case AUTHORITARIAN:
                switch (player.getEthos()) {
                    case MILITARIST: harmony = 85; break;
                    case MATERIALIST: harmony = 70; break;
                    case EQUALIST: harmony = 45; break;
                    case LIBERTARIAN: harmony = 25; break;
                    case PACIFIST: harmony = 30; break;
                    case SPIRITUALIST: harmony = 55; break;
                    default: harmony = 50; break;
                }
                break;
            case MONARCHY:
                switch (player.getEthos()) {
                    case SPIRITUALIST: harmony = 80; break;
                    case MATERIALIST: harmony = 70; break;
                    case MILITARIST: harmony = 65; break;
                    case EQUALIST: harmony = 35; break;
                    case LIBERTARIAN: harmony = 30; break;
                    case PACIFIST: harmony = 50; break;
                    default: harmony = 55; break;
                }
                break;
            case OLIGARCHY:
                switch (player.getEthos()) {
                    case MATERIALIST: harmony = 85; break;
                    case MILITARIST: harmony = 70; break;
                    case SPIRITUALIST: harmony = 60; break;
                    case EQUALIST: harmony = 30; break;
                    case LIBERTARIAN: harmony = 45; break;
                    case PACIFIST: harmony = 40; break;
                    default: harmony = 50; break;
                }
                break;
            case THEOCRACY:
                switch (player.getEthos()) {
                    case SPIRITUALIST: harmony = 95; break;
                    case EQUALIST: harmony = 60; break;
                    case PACIFIST: harmony = 70; break;
                    case MATERIALIST: harmony = 30; break;
                    case MILITARIST: harmony = 50; break;
                    case LIBERTARIAN: harmony = 40; break;
                    default: harmony = 55; break;
                }
                break;
            case DICTATORSHIP:
                switch (player.getEthos()) {
                    case MATERIALIST: harmony = 80; break;
                    case MILITARIST: harmony = 75; break;
                    case SPIRITUALIST: harmony = 50; break;
                    case EQUALIST: harmony = 20; break;
                    case LIBERTARIAN: harmony = 15; break;
                    case PACIFIST: harmony = 25; break;
                    default: harmony = 45; break;
                }
                break;
            case CONFEDERATION:
                switch (player.getEthos()) {
                    case LIBERTARIAN: harmony = 85; break;
                    case EQUALIST: harmony = 80; break;
                    case PACIFIST: harmony = 75; break;
                    case MATERIALIST: harmony = 60; break;
                    case SPIRITUALIST: harmony = 55; break;
                    case MILITARIST: harmony = 45; break;
                    default: harmony = 60; break;
                }
                break;
            case CORPORATE:
                switch (player.getEthos()) {
                    case MATERIALIST: harmony = 90; break;
                    case LIBERTARIAN: harmony = 75; break;
                    case MILITARIST: harmony = 60; break;
                    case SPIRITUALIST: harmony = 35; break;
                    case EQUALIST: harmony = 40; break;
                    case PACIFIST: harmony = 50; break;
                    default: harmony = 55; break;
                }
                break;
            default:
                harmony = 50;
                break;
        }

        return harmony;
    }

    /**
     * Estimates the cultural identity strength for an empire (0-100).
     */
    private int estimateCulturalIdentityStrength(Player player, int harmonyScore) {
        int strength = 30; // Base strength

        // Government-ethos harmony contributes significantly
        strength += harmonyScore / 3;

        // Empire name presence indicates established identity
        if (player.getEmpireName() != null && !player.getEmpireName().isEmpty()) {
            strength += 15;
        }

        // Government type contributes to identity distinctiveness
        if (player.getGovernmentType() != null) {
            strength += 10;
        }

        // Ethos contributes to cultural distinctiveness
        if (player.getEthos() != null) {
            strength += 10;
        }

        return Math.min(100, strength);
    }

    /**
     * Estimates the cultural cohesion score for an empire (0-100).
     */
    private int estimateCulturalCohesion(Player player, Galaxy galaxy) {
        int cohesion = 50; // Base cohesion

        // Count planets in this galaxy that might belong to this empire
        int planetCount = 0;
        if (galaxy.getSolarSystems() != null) {
            for (SolarSystem system : galaxy.getSolarSystems()) {
                if (system.getPlanets() != null) {
                    for (Planet planet : system.getPlanets()) {
                        // Planets with ownerEmpireId matching this player's ID
                        if (planet.getOwnerEmpireId() != null && planet.getOwnerEmpireId().equals(player.getId().intValue())) {
                            planetCount++;
                        }
                    }
                }
            }
        }

        // More planets mean more cohesion challenges but also more cultural presence
        if (planetCount > 0) {
            cohesion += Math.min(30, planetCount * 5);
        }

        // Government-ethos harmony affects cohesion
        int harmony = calculateGovernmentEthosHarmony(player);
        cohesion += harmony / 10;

        return Math.min(100, cohesion);
    }

    /**
     * Determines the cultural stability based on identity, cohesion, and harmony.
     */
    private CulturalStability determineCulturalStability(int identityStrength, int cohesion, int harmony) {
        int combinedScore = (identityStrength + cohesion + harmony) / 3;

        if (combinedScore >= 70) {
            return CulturalStability.STABLE;
        } else if (combinedScore >= 50) {
            return CulturalStability.MODERATE;
        } else if (combinedScore >= 30) {
            return CulturalStability.UNREST_RISK;
        } else {
            return CulturalStability.CRISIS;
        }
    }

    /**
     * Estimates the cultural influence level of an empire.
     */
    private CulturalInfluenceLevel estimateCulturalInfluence(Player player, Galaxy galaxy, int identityStrength) {
        if (identityStrength < 30) {
            return CulturalInfluenceLevel.ISOLATED;
        }

        // Count planets owned by this empire
        int planetCount = 0;
        if (galaxy.getSolarSystems() != null) {
            for (SolarSystem system : galaxy.getSolarSystems()) {
                if (system.getPlanets() != null) {
                    for (Planet planet : system.getPlanets()) {
                        if (planet.getOwnerEmpireId() != null && planet.getOwnerEmpireId().equals(player.getId().intValue())) {
                            planetCount++;
                        }
                    }
                }
            }
        }

        int totalPlanets = 0;
        if (galaxy.getSolarSystems() != null) {
            for (SolarSystem system : galaxy.getSolarSystems()) {
                if (system.getPlanets() != null) {
                    totalPlanets += system.getPlanets().size();
                }
            }
        }

        double marketShare = totalPlanets > 0 ? (double) planetCount / totalPlanets : 0;

        if (marketShare > 0.4 && identityStrength > 70) {
            return CulturalInfluenceLevel.DOMINANT;
        } else if (marketShare > 0.2 && identityStrength > 50) {
            return CulturalInfluenceLevel.GALACTIC;
        } else if (planetCount > 0) {
            return CulturalInfluenceLevel.REGIONAL;
        }

        return CulturalInfluenceLevel.ISOLATED;
    }

    /**
     * Calculates the average cultural identity strength across all empires.
     */
    private int calculateAverageIdentityStrength(List<EmpireCulturalProfile> profiles) {
        if (profiles.isEmpty()) return 0;
        return (int) profiles.stream().mapToInt(EmpireCulturalProfile::getCulturalIdentityStrength).average().orElse(0);
    }

    /**
     * Calculates the average cultural cohesion across all empires.
     */
    private int calculateAverageCohesion(List<EmpireCulturalProfile> profiles) {
        if (profiles.isEmpty()) return 0;
        return (int) profiles.stream().mapToInt(EmpireCulturalProfile::getCulturalCohesion).average().orElse(0);
    }

    /**
     * Calculates the cultural diversity index (0-100) based on government and ethos variety.
     */
    private int calculateCulturalDiversityIndex(List<EmpireCulturalProfile> profiles) {
        if (profiles.size() <= 1) return 0;

        // Count unique government types and ethos values
        long uniqueGovernments = profiles.stream()
                .map(EmpireCulturalProfile::getGovernmentType)
                .filter(gt -> gt != null)
                .distinct()
                .count();

        long uniqueEthos = profiles.stream()
                .map(EmpireCulturalProfile::getEthos)
                .filter(e -> e != null)
                .distinct()
                .count();

        // Diversity index based on variety relative to total empires
        double govDiversity = (double) uniqueGovernments / Math.min(profiles.size(), 8);
        double ethosDiversity = (double) uniqueEthos / Math.min(profiles.size(), 6);

        return (int) ((govDiversity + ethosDiversity) / 2 * 100);
    }

    // ------------------------------------------------------------------
    // Propaganda Infrastructure Assessment
    // ------------------------------------------------------------------

    /**
     * Assesses propaganda and information infrastructure across the galaxy.
     * <p>
     * Propaganda infrastructure includes:
     * <ul>
     *   <li>TRADE_PORT facilities (cultural exchange and messaging hubs)</li>
     *   <li>COLONY_DOME facilities (population centers for information dissemination)</li>
     *   <li>COLONY stations (cultural outposts)</li>
     *   <li>TRADE stations (information trade hubs)</li>
     * </ul>
     * </p>
     *
     * @param galaxy the galaxy to assess
     * @return a propaganda infrastructure report
     */
    @Transactional(readOnly = true)
    public PropagandaInfrastructureReport assessPropagandaInfrastructure(Galaxy galaxy) {
        PropagandaInfrastructureReport report = new PropagandaInfrastructureReport();
        report.setGalaxyId(galaxy.getId());
        report.setGalaxyName(galaxy.getName());

        List<SolarSystem> systems = galaxy.getSolarSystems();
        if (systems == null || systems.isEmpty()) {
            report.setTotalSystems(0);
            report.setSystemsPropagandaInfrastructure(0);
            report.setInfrastructurePercentage(0.0);
            log.info("No solar systems in galaxy '{}' — no propaganda infrastructure to assess", galaxy.getName());
            return report;
        }

        int tradePortCount = 0;
        int colonyDomeCount = 0;
        int colonyStationCount = 0;
        int tradeStationCount = 0;
        List<SystemPropagandaInfo> systemPropagandaInfos = new ArrayList<>();

        for (SolarSystem system : systems) {
            SystemPropagandaInfo systemInfo = new SystemPropagandaInfo();
            systemInfo.setSystemName(system.getName());
            systemInfo.setSectorRow(system.getSectorRow());
            systemInfo.setSectorCol(system.getSectorCol());

            boolean hasTradePort = false;
            boolean hasColonyDome = false;

            // Check planets for cultural facilities
            if (system.getPlanets() != null) {
                for (Planet planet : system.getPlanets()) {
                    // TRADE_PORT provides influence bonus — cultural exchange hub
                    if (planet.getInfluenceProduction() > 0) {
                        hasTradePort = true;
                        tradePortCount++;
                    }
                    // COLONY_DOME provides food bonus — population center
                    if (planet.getFoodProduction() > 0) {
                        hasColonyDome = true;
                        colonyDomeCount++;
                    }
                }
            }

            // Determine propaganda infrastructure status
            if (hasTradePort && hasColonyDome) {
                systemInfo.setPropagandaStatus(PropagandaStatus.FULLY_EQUIPPED);
            } else if (hasTradePort || hasColonyDome) {
                systemInfo.setPropagandaStatus(PropagandaStatus.PARTIALLY_EQUIPPED);
            } else {
                systemInfo.setPropagandaStatus(PropagandaStatus.NO_INFRASTRUCTURE);
            }

            systemInfo.setHasTradePort(hasTradePort);
            systemInfo.setHasColonyDome(hasColonyDome);
            systemPropagandaInfos.add(systemInfo);
        }

        report.setSystemPropagandaInfos(systemPropagandaInfos);
        report.setTotalSystems(systems.size());
        report.setTradePortCount(tradePortCount);
        report.setColonyDomeCount(colonyDomeCount);
        report.setColonyStationCount(colonyStationCount);
        report.setTradeStationCount(tradeStationCount);
        report.setSystemsPropagandaInfrastructure(
                (int) systemPropagandaInfos.stream()
                        .filter(s -> s.getPropagandaStatus() != PropagandaStatus.NO_INFRASTRUCTURE)
                        .count()
        );
        report.setInfrastructurePercentage(
                systems.size() > 0
                        ? (double) report.getSystemsPropagandaInfrastructure() / systems.size() * 100
                        : 0.0
        );

        log.info("Propaganda infrastructure for '{}': {} trade ports, {} colony domes across {} systems ({:.1f}% coverage)",
                galaxy.getName(), tradePortCount, colonyDomeCount,
                systems.size(), report.getInfrastructurePercentage());

        return report;
    }

    // ------------------------------------------------------------------
    // Morale Assessment
    // ------------------------------------------------------------------

    /**
     * Monitors morale levels across all planets in the galaxy.
     * <p>
     * Morale is estimated based on:
     * <ul>
     *   <li>Planet resource production (food, minerals, energy)</li>
     *   <li>Planet defense capabilities</li>
     *   <li>Empire government-ethos harmony</li>
     *   <li>AI character presence and activity</li>
     * </ul>
     * Planets with low morale are flagged for intervention.
     * </p>
     *
     * @param galaxy the galaxy to assess
     * @return a morale report
     */
    @Transactional(readOnly = true)
    public MoraleReport assessMoraleLevels(Galaxy galaxy) {
        MoraleReport report = new MoraleReport();
        report.setGalaxyId(galaxy.getId());
        report.setGalaxyName(galaxy.getName());

        List<SolarSystem> systems = galaxy.getSolarSystems();
        if (systems == null || systems.isEmpty()) {
            report.setTotalPlanets(0);
            report.setAverageMorale(50.0);
            report.setLowMoralePlanets(0);
            report.setCriticalMoralePlanets(0);
            log.info("No solar systems in galaxy '{}' — no morale to assess", galaxy.getName());
            return report;
        }

        List<Player> players = playerRepository.findAll();
        List<PlanetMoraleInfo> planetMoraleInfos = new ArrayList<>();
        int totalMorale = 0;
        int lowMoraleCount = 0;
        int criticalMoraleCount = 0;

        for (SolarSystem system : systems) {
            if (system.getPlanets() != null) {
                for (Planet planet : system.getPlanets()) {
                    PlanetMoraleInfo moraleInfo = new PlanetMoraleInfo();
                    moraleInfo.setPlanetName(planet.getName());
                    moraleInfo.setSystemName(system.getName());
                    moraleInfo.setSectorRow(system.getSectorRow());
                    moraleInfo.setSectorCol(system.getSectorCol());

                    // Estimate morale based on available data
                    int morale = estimatePlanetMorale(planet, system, players);
                    moraleInfo.setMorale(morale);
                    moraleInfo.setMoraleCategory(getMoraleCategory(morale));

                    // Determine unrest risk
                    boolean unrestRisk = morale < UNREST_RISK_THRESHOLD;
                    moraleInfo.setUnrestRisk(unrestRisk);

                    totalMorale += morale;
                    if (morale < LOW_MORALE_THRESHOLD) {
                        lowMoraleCount++;
                    }
                    if (morale < CRITICAL_MORALE_THRESHOLD) {
                        criticalMoraleCount++;
                    }

                    planetMoraleInfos.add(moraleInfo);
                }
            }
        }

        report.setPlanetMoraleInfos(planetMoraleInfos);
        report.setTotalPlanets(planetMoraleInfos.size());
        report.setAverageMorale(
                planetMoraleInfos.isEmpty() ? 50.0 : (double) totalMorale / planetMoraleInfos.size()
        );
        report.setLowMoralePlanets(lowMoraleCount);
        report.setCriticalMoralePlanets(criticalMoraleCount);

        log.info("Morale for '{}': {} planets, avg morale={:.1f}, low={}, critical={}",
                galaxy.getName(), planetMoraleInfos.size(), report.getAverageMorale(),
                lowMoraleCount, criticalMoraleCount);

        return report;
    }

    /**
     * Estimates the morale level for a planet (0-100).
     */
    private int estimatePlanetMorale(Planet planet, SolarSystem system, List<Player> players) {
        int morale = 50; // Base morale

        // Resource production contributes to morale
        if (planet.getFoodProduction() > 0) {
            morale += 10; // Food security
        }
        if (planet.getMineralProduction() > 0) {
            morale += 5; // Economic activity
        }
        if (planet.getEnergyProduction() > 0) {
            morale += 5; // Energy availability
        }

        // Research production contributes to progress morale
        if (planet.getResearchProduction() > 0) {
            morale += 5; // Scientific progress
        }

        // Influence production contributes to cultural morale
        if (planet.getInfluenceProduction() > 0) {
            morale += 5; // Cultural engagement
        }

        // Empire ownership provides stability
        if (planet.getOwnerEmpireId() != null) {
            morale += 10; // Political stability
            // Find the owning empire and check harmony
            for (Player player : players) {
                if (player.getId().equals(planet.getOwnerEmpireId())) {
                    int harmony = calculateGovernmentEthosHarmony(player);
                    morale += harmony / 20; // Harmony bonus
                    break;
                }
            }
        }

        // Mineral quality affects living conditions
        if (planet.getMineralQuality() != null) {
            switch (planet.getMineralQuality()) {
                case ULTRA_RICH:
                case RICH:
                    morale += 5;
                    break;
                case POOR:
                case ULTRA_POOR:
                    morale -= 5;
                    break;
                default:
                    break;
            }
        }

        return Math.max(0, Math.min(100, morale));
    }

    /**
     * Categorizes morale level.
     */
    private String getMoraleCategory(int morale) {
        if (morale >= 80) return "Excellent";
        if (morale >= 60) return "Good";
        if (morale >= 40) return "Moderate";
        if (morale >= 20) return "Low";
        return "Critical";
    }

    // ------------------------------------------------------------------
    // Cultural Influence Assessment
    // ------------------------------------------------------------------

    /**
     * Tracks cultural influence between empires in the galaxy.
     * <p>
     * Cultural influence is measured by:
     * <ul>
     *   <li>Planet ownership ratio</li>
     *   <li>Cultural identity strength</li>
     *   <li>Government-ethos appeal to other empires</li>
     *   <li>Propaganda infrastructure coverage</li>
     * </ul>
     * </p>
     *
     * @param galaxy the galaxy to assess
     * @return a cultural influence report
     */
    @Transactional(readOnly = true)
    public CulturalInfluenceReport assessCulturalInfluence(Galaxy galaxy) {
        CulturalInfluenceReport report = new CulturalInfluenceReport();
        report.setGalaxyId(galaxy.getId());
        report.setGalaxyName(galaxy.getName());

        List<Player> players = playerRepository.findAll();
        List<EmpireInfluenceProfile> influenceProfiles = new ArrayList<>();

        // Count total planets per empire
        Map<Integer, Integer> empirePlanetCounts = new HashMap<>();
        int totalPlanets = 0;
        if (galaxy.getSolarSystems() != null) {
            for (SolarSystem system : galaxy.getSolarSystems()) {
                if (system.getPlanets() != null) {
                    for (Planet planet : system.getPlanets()) {
                        totalPlanets++;
                        if (planet.getOwnerEmpireId() != null) {
                            empirePlanetCounts.merge(planet.getOwnerEmpireId(), 1, Integer::sum);
                        }
                    }
                }
            }
        }

        for (Player player : players) {
            EmpireInfluenceProfile profile = new EmpireInfluenceProfile();
            profile.setEmpireId(player.getId());
            profile.setEmpireName(player.getEmpireName() != null && !player.getEmpireName().isEmpty()
                    ? player.getEmpireName() : player.getFirstName());

            int planetCount = empirePlanetCounts.getOrDefault(player.getId().intValue(), 0);
            profile.setPlanetCount(planetCount);
            profile.setPlanetMarketShare(totalPlanets > 0 ? (double) planetCount / totalPlanets * 100 : 0.0);

            int identityStrength = estimateCulturalIdentityStrength(player, calculateGovernmentEthosHarmony(player));
            profile.setCulturalIdentityStrength(identityStrength);

            // Cultural appeal to other empires
            int appealScore = calculateCulturalAppeal(player);
            profile.setCulturalAppeal(appealScore);

            // Influence score combines identity, market share, and appeal
            int influenceScore = (int) (identityStrength * 0.4 + profile.getPlanetMarketShare() * 0.3 + appealScore * 0.3);
            profile.setInfluenceScore(influenceScore);

            // Determine influence level
            CulturalInfluenceLevel level;
            if (influenceScore >= 70 && profile.getPlanetMarketShare() > 40) {
                level = CulturalInfluenceLevel.DOMINANT;
            } else if (influenceScore >= 50 && profile.getPlanetMarketShare() > 20) {
                level = CulturalInfluenceLevel.GALACTIC;
            } else if (planetCount > 0) {
                level = CulturalInfluenceLevel.REGIONAL;
            } else {
                level = CulturalInfluenceLevel.ISOLATED;
            }
            profile.setInfluenceLevel(level);

            influenceProfiles.add(profile);
        }

        report.setInfluenceProfiles(influenceProfiles);
        report.setDominantCulture(getDominantCulture(influenceProfiles));

        log.info("Cultural influence for '{}': {} empires, dominant={}",
                galaxy.getName(), influenceProfiles.size(), report.getDominantCulture());

        return report;
    }

    /**
     * Calculates the cultural appeal score for an empire (0-100).
     * <p>
     * Cultural appeal measures how attractive an empire's culture is to others.
     * Democracies and confederations tend to have higher appeal, while
     * dictatorships have lower appeal but stronger internal cohesion.
     * </p>
     */
    private int calculateCulturalAppeal(Player player) {
        int appeal = 50; // Base appeal

        if (player.getGovernmentType() == null) return appeal;

        switch (player.getGovernmentType()) {
            case DEMOCRACY:
            case CONFEDERATION:
                appeal += 20; // Democratic appeal
                break;
            case MONARCHY:
            case THEOCRACY:
                appeal += 10; // Traditional appeal
                break;
            case OLIGARCHY:
            case CORPORATE:
                appeal += 5; // Pragmatic appeal
                break;
            case AUTHORITARIAN:
                appeal -= 10; // Authoritarian unappeal
                break;
            case DICTATORSHIP:
                appeal -= 20; // Dictatorship unappeal
                break;
            default:
                break;
        }

        // Ethos influence on appeal
        if (player.getEthos() != null) {
            switch (player.getEthos()) {
                case PACIFIST:
                case EQUALIST:
                    appeal += 10; // Peaceful and equal appeal
                    break;
                case LIBERTARIAN:
                    appeal += 5; // Freedom appeal
                    break;
                case MATERIALIST:
                    appeal += 5; // Prosperity appeal
                    break;
                case SPIRITUALIST:
                    appeal += 5; // Spiritual appeal
                    break;
                case MILITARIST:
                    appeal -= 10; // Militarist unappeal
                    break;
                default:
                    break;
            }
        }

        return Math.max(0, Math.min(100, appeal));
    }

    /**
     * Finds the dominant culture in the galaxy.
     */
    private String getDominantCulture(List<EmpireInfluenceProfile> profiles) {
        if (profiles.isEmpty()) return "None";

        return profiles.stream()
                .max((a, b) -> Integer.compare(a.getInfluenceScore(), b.getInfluenceScore()))
                .map(EmpireInfluenceProfile::getEmpireName)
                .orElse("None");
    }

    // ------------------------------------------------------------------
    // Cultural Stability Assessment
    // ------------------------------------------------------------------

    /**
     * Assesses cultural stability and detects unrest risks across the galaxy.
     * <p>
     * Cultural stability is evaluated by:
     * <ul>
     *   <li>Planet-level morale variance within empires</li>
     *   <li>Government-ethos harmony across empires</li>
     *   <li>Cultural infrastructure coverage</li>
     *   <li>AI character activity and presence</li>
     * </ul>
     * Systems with high unrest risk are flagged for immediate attention.
     * </p>
     *
     * @param galaxy the galaxy to assess
     * @return a cultural stability report
     */
    @Transactional(readOnly = true)
    public CulturalStabilityReport assessCulturalStability(Galaxy galaxy) {
        CulturalStabilityReport report = new CulturalStabilityReport();
        report.setGalaxyId(galaxy.getId());
        report.setGalaxyName(galaxy.getName());

        List<Player> players = playerRepository.findAll();
        List<EmpireStabilityProfile> stabilityProfiles = new ArrayList<>();
        int totalUnrestRiskSystems = 0;
        int totalCrisisSystems = 0;

        for (Player player : players) {
            EmpireStabilityProfile profile = new EmpireStabilityProfile();
            profile.setEmpireId(player.getId());
            profile.setEmpireName(player.getEmpireName() != null && !player.getEmpireName().isEmpty()
                    ? player.getEmpireName() : player.getFirstName());

            // Calculate stability metrics
            int harmony = calculateGovernmentEthosHarmony(player);
            profile.setGovernmentEthosHarmony(harmony);

            int identityStrength = estimateCulturalIdentityStrength(player, harmony);
            profile.setCulturalIdentityStrength(identityStrength);

            // Estimate stability score
            int stabilityScore = calculateStabilityScore(player, galaxy, harmony, identityStrength);
            profile.setStabilityScore(stabilityScore);

            // Determine stability category
            CulturalStability stability;
            if (stabilityScore >= 70) {
                stability = CulturalStability.STABLE;
            } else if (stabilityScore >= 50) {
                stability = CulturalStability.MODERATE;
            } else if (stabilityScore >= 30) {
                stability = CulturalStability.UNREST_RISK;
                totalUnrestRiskSystems++;
            } else {
                stability = CulturalStability.CRISIS;
                totalCrisisSystems++;
            }
            profile.setStabilityCategory(stability);

            // Unrest risk percentage
            double unrestRisk = Math.max(0, 100 - stabilityScore * 1.5);
            profile.setUnrestRiskPercentage(unrestRisk);

            stabilityProfiles.add(profile);
        }

        report.setStabilityProfiles(stabilityProfiles);
        report.setTotalUnrestRiskSystems(totalUnrestRiskSystems);
        report.setTotalCrisisSystems(totalCrisisSystems);
        report.setAverageStabilityScore(
                stabilityProfiles.isEmpty() ? 50.0 :
                        (double) stabilityProfiles.stream().mapToInt(EmpireStabilityProfile::getStabilityScore).sum()
                                / stabilityProfiles.size()
        );

        log.info("Cultural stability for '{}': {} empires, unrest risk={}, crisis={}, avg stability={:.1f}",
                galaxy.getName(), stabilityProfiles.size(), totalUnrestRiskSystems,
                totalCrisisSystems, report.getAverageStabilityScore());

        return report;
    }

    /**
     * Calculates the stability score for an empire (0-100).
     */
    private int calculateStabilityScore(Player player, Galaxy galaxy, int harmony, int identityStrength) {
        int score = 40; // Base stability

        // Government-ethos harmony is the biggest factor
        score += harmony / 2;

        // Cultural identity strength
        score += identityStrength / 5;

        // Planet ownership provides stability
        int planetCount = 0;
        if (galaxy.getSolarSystems() != null) {
            for (SolarSystem system : galaxy.getSolarSystems()) {
                if (system.getPlanets() != null) {
                    for (Planet planet : system.getPlanets()) {
                        if (planet.getOwnerEmpireId() != null && planet.getOwnerEmpireId().equals(player.getId().intValue())) {
                            planetCount++;
                        }
                    }
                }
            }
        }

        // Having planets stabilizes the empire
        if (planetCount > 0) {
            score += Math.min(20, planetCount * 3);
        }

        // AI character presence adds administrative stability
        List<AiCharacter> aiAdmins = aiCharacterRepository
                .findByAiAdminTrueAndActiveTrueAndEmpireName(galaxy.getName());
        if (!aiAdmins.isEmpty()) {
            score += Math.min(10, aiAdmins.size());
        }

        return Math.max(0, Math.min(100, score));
    }

    // ------------------------------------------------------------------
    // Work Order Creation
    // ------------------------------------------------------------------

    /**
     * Creates prioritized work orders based on cultural assessment findings.
     * <p>
     * Work order priority order:
     * <ol>
     *   <li>CRITICAL — Empire with critical morale (imminent revolt risk)</li>
     *   <li>CRITICAL — No cultural infrastructure in the galaxy</li>
     *   <li>HIGH — Morale crisis affecting multiple planets</li>
     *   <li>HIGH — Cultural unrest detected in strategic systems</li>
     *   <li>MEDIUM — Low cultural cohesion between empire planets</li>
     *   <li>MEDIUM — No propaganda infrastructure for empire</li>
     *   <li>LOW — Recommend expanding cultural influence</li>
     * </ol>
     * </p>
     *
     * @param galaxy the galaxy to create work orders for
     * @return list of created work order titles
     */
    @Transactional
    public List<String> createCultureWorkOrders(Galaxy galaxy) {
        List<String> ordersCreated = new ArrayList<>();

        // Assess all cultural aspects
        CulturalIdentityReport identityReport = assessCulturalIdentity(galaxy);
        PropagandaInfrastructureReport propagandaReport = assessPropagandaInfrastructure(galaxy);
        MoraleReport moraleReport = assessMoraleLevels(galaxy);
        CulturalInfluenceReport influenceReport = assessCulturalInfluence(galaxy);
        CulturalStabilityReport stabilityReport = assessCulturalStability(galaxy);

        String submittedBy = "GalaxyOverseer:" + galaxy.getName();

        AiAdminService aiAdminService = aiAdminServiceProvider.getIfAvailable();
        if (aiAdminService == null) {
            log.warn("AiAdminService not available — skipping culture work order creation for '{}'", galaxy.getName());
            return ordersCreated;
        }

        // CRITICAL: Critical morale planets
        for (PlanetMoraleInfo planetInfo : moraleReport.getPlanetMoraleInfos()) {
            if (planetInfo.getMorale() < CRITICAL_MORALE_THRESHOLD) {
                String orderText = String.format(
                        "[CRITICAL] Planet '%s' in system '%s' has morale at %d (critical threshold: %d). " +
                        "Immediate intervention required to prevent revolt. " +
                        "Action: Deploy cultural agents and improve living conditions.",
                        planetInfo.getPlanetName(), planetInfo.getSystemName(),
                        planetInfo.getMorale(), CRITICAL_MORALE_THRESHOLD);
                aiAdminService.submitOrder(submittedBy, orderText);
                ordersCreated.add(orderText);
                log.info("Created CRITICAL culture work order: {}", orderText);
            }
        }

        // CRITICAL: No cultural infrastructure
        if (propagandaReport.getInfrastructurePercentage() == 0.0 && propagandaReport.getTotalSystems() > 0) {
            String orderText = String.format(
                    "[CRITICAL] No cultural infrastructure in galaxy '%s' — %d systems with no cultural facilities. " +
                    "Action: Build TRADE_PORT and COLONY_DOME facilities to establish cultural presence.",
                    galaxy.getName(), propagandaReport.getTotalSystems());
            aiAdminService.submitOrder(submittedBy, orderText);
            ordersCreated.add(orderText);
            log.info("Created CRITICAL culture work order: {}", orderText);
        }

        // HIGH: Low morale planets
        if (moraleReport.getLowMoralePlanets() > 2) {
            String orderText = String.format(
                    "[HIGH] %d planets have low morale (below %d) in '%s'. Average galaxy morale: %.1f. " +
                    "Action: Launch propaganda campaigns and improve conditions.",
                    moraleReport.getLowMoralePlanets(), LOW_MORALE_THRESHOLD,
                    galaxy.getName(), moraleReport.getAverageMorale());
            aiAdminService.submitOrder(submittedBy, orderText);
            ordersCreated.add(orderText);
            log.info("Created HIGH culture work order: {}", orderText);
        }

        // HIGH: Cultural unrest risk
        for (EmpireStabilityProfile stabilityProfile : stabilityReport.getStabilityProfiles()) {
            if (stabilityProfile.getStabilityCategory() == CulturalStability.UNREST_RISK ||
                stabilityProfile.getStabilityCategory() == CulturalStability.CRISIS) {
                String orderText = String.format(
                        "[HIGH] Empire '%s' has stability score %d (category: %s). " +
                        "Unrest risk: %.1f%%. " +
                        "Action: Deploy cultural agents and improve government-ethos harmony.",
                        stabilityProfile.getEmpireName(), stabilityProfile.getStabilityScore(),
                        stabilityProfile.getStabilityCategory(), stabilityProfile.getUnrestRiskPercentage());
                aiAdminService.submitOrder(submittedBy, orderText);
                ordersCreated.add(orderText);
                log.info("Created HIGH culture work order: {}", orderText);
            }
        }

        // MEDIUM: Low cultural cohesion
        for (EmpireCulturalProfile profile : identityReport.getEmpireProfiles()) {
            if (profile.getCulturalCohesion() < 40) {
                String orderText = String.format(
                        "[MEDIUM] Empire '%s' has cultural cohesion of only %d. " +
                        "Government-ethos harmony: %d. " +
                        "Action: Launch unity campaigns and cultural exchange programs.",
                        profile.getEmpireName(), profile.getCulturalCohesion(),
                        profile.getGovernmentEthosHarmony());
                aiAdminService.submitOrder(submittedBy, orderText);
                ordersCreated.add(orderText);
                log.info("Created MEDIUM culture work order: {}", orderText);
            }
        }

        // MEDIUM: Low propaganda infrastructure
        if (propagandaReport.getInfrastructurePercentage() < 30.0 && propagandaReport.getTotalSystems() > 0) {
            String orderText = String.format(
                    "[MEDIUM] Only %.1f%% of systems have propaganda infrastructure in '%s'. " +
                    "Action: Build more TRADE_PORT and COLONY_DOME facilities.",
                    propagandaReport.getInfrastructurePercentage(), galaxy.getName());
            aiAdminService.submitOrder(submittedBy, orderText);
            ordersCreated.add(orderText);
            log.info("Created MEDIUM culture work order: {}", orderText);
        }

        // LOW: Expand cultural influence for isolated empires
        for (EmpireInfluenceProfile profile : influenceReport.getInfluenceProfiles()) {
            if (profile.getInfluenceLevel() == CulturalInfluenceLevel.ISOLATED && profile.getPlanetCount() > 0) {
                String orderText = String.format(
                        "[LOW] Empire '%s' has %d planets but isolated cultural influence. " +
                        "Cultural appeal: %d. " +
                        "Action: Consider propaganda campaigns and cultural exchange.",
                        profile.getEmpireName(), profile.getPlanetCount(), profile.getCulturalAppeal());
                aiAdminService.submitOrder(submittedBy, orderText);
                ordersCreated.add(orderText);
                log.info("Created LOW culture work order: {}", orderText);
            }
        }

        log.info("Created {} culture work orders for galaxy '{}'", ordersCreated.size(), galaxy.getName());
        return ordersCreated;
    }

    // ------------------------------------------------------------------
    // Integration Method for GalaxyOverseerService
    // ------------------------------------------------------------------

    /**
     * Integration method called by GalaxyOverseerService during the health check cycle.
     * <p>
     * Assesses all cultural aspects and adds findings to the galaxy assessment.
     * </p>
     *
     * @param galaxy the galaxy to assess
     * @param assessment the galaxy assessment to add findings to
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void assessCultureNeeds(Galaxy galaxy, GalaxyOverseerService.GalaxyAssessment assessment) {
        try {
            if (galaxy.getSolarSystems() == null || galaxy.getSolarSystems().isEmpty()) {
                log.info("No solar systems in galaxy '{}' — skipping culture assessment", galaxy.getName());
                return;
            }

            // Assess cultural identity
            CulturalIdentityReport identityReport = assessCulturalIdentity(galaxy);

            // Assess propaganda infrastructure
            PropagandaInfrastructureReport propagandaReport = assessPropagandaInfrastructure(galaxy);

            // Assess morale
            MoraleReport moraleReport = assessMoraleLevels(galaxy);

            // Assess cultural influence
            CulturalInfluenceReport influenceReport = assessCulturalInfluence(galaxy);

            // Assess cultural stability
            CulturalStabilityReport stabilityReport = assessCulturalStability(galaxy);

            // Add findings based on assessment results

            // Critical morale findings
            if (moraleReport.getCriticalMoralePlanets() > 0) {
                assessment.addFinding(GalaxyOverseerService.GalaxyAssessment.Severity.CRITICAL,
                        moraleReport.getCriticalMoralePlanets() + " planets have critical morale (below " +
                                CRITICAL_MORALE_THRESHOLD + "). Revolt risk is imminent.");
                assessment.addRecommendation("Deploy cultural agents immediately to stabilize critical planets.");
            }

            // Low morale findings
            if (moraleReport.getLowMoralePlanets() > 2) {
                assessment.addFinding(GalaxyOverseerService.GalaxyAssessment.Severity.HIGH,
                        moraleReport.getLowMoralePlanets() + " planets have low morale (below " +
                                LOW_MORALE_THRESHOLD + "). Average galaxy morale: " +
                                String.format("%.1f", moraleReport.getAverageMorale()));
                assessment.addRecommendation("Launch galaxy-wide morale improvement campaigns.");
            }

            // No cultural infrastructure
            if (propagandaReport.getInfrastructurePercentage() == 0.0 && propagandaReport.getTotalSystems() > 0) {
                assessment.addFinding(GalaxyOverseerService.GalaxyAssessment.Severity.CRITICAL,
                        "No cultural infrastructure detected across " + propagandaReport.getTotalSystems() +
                                " systems. Empire has no cultural presence.");
                assessment.addRecommendation("Build TRADE_PORT and COLONY_DOME facilities immediately.");
            }

            // Low propaganda infrastructure
            if (propagandaReport.getInfrastructurePercentage() < 30.0 && propagandaReport.getTotalSystems() > 0) {
                assessment.addFinding(GalaxyOverseerService.GalaxyAssessment.Severity.MEDIUM,
                        "Only " + String.format("%.1f", propagandaReport.getInfrastructurePercentage()) +
                                "% of systems have propaganda infrastructure.");
                assessment.addRecommendation("Expand cultural infrastructure to improve influence and morale.");
            }

            // Cultural unrest
            if (stabilityReport.getTotalCrisisSystems() > 0) {
                assessment.addFinding(GalaxyOverseerService.GalaxyAssessment.Severity.CRITICAL,
                        stabilityReport.getTotalCrisisSystems() + " empires in cultural crisis. " +
                                "Immediate intervention required.");
                assessment.addRecommendation("Deploy cultural agents to crisis empires.");
            }

            if (stabilityReport.getTotalUnrestRiskSystems() > 0) {
                assessment.addFinding(GalaxyOverseerService.GalaxyAssessment.Severity.HIGH,
                        stabilityReport.getTotalUnrestRiskSystems() + " empires at unrest risk.");
                assessment.addRecommendation("Monitor unrest risk empires and prepare stabilization measures.");
            }

            // Low cultural cohesion
            for (EmpireCulturalProfile profile : identityReport.getEmpireProfiles()) {
                if (profile.getCulturalCohesion() < 30) {
                    assessment.addFinding(GalaxyOverseerService.GalaxyAssessment.Severity.MEDIUM,
                            profile.getEmpireName() + " has very low cultural cohesion (" +
                                    profile.getCulturalCohesion() + ").");
                    assessment.addRecommendation("Launch unity campaigns for " + profile.getEmpireName() + ".");
                }
            }

            // Low government-ethos harmony
            for (EmpireCulturalProfile profile : identityReport.getEmpireProfiles()) {
                if (profile.getGovernmentEthosHarmony() < 30) {
                    assessment.addFinding(GalaxyOverseerService.GalaxyAssessment.Severity.MEDIUM,
                            profile.getEmpireName() + " has poor government-ethos harmony (" +
                                    profile.getGovernmentEthosHarmony() + ").");
                    assessment.addRecommendation("Consider government reform or cultural adaptation for " +
                            profile.getEmpireName() + ".");
                }
            }

            // Cultural diversity note
            if (identityReport.getCulturalDiversityIndex() < 20 && identityReport.getEmpireProfiles().size() > 1) {
                assessment.addFinding(GalaxyOverseerService.GalaxyAssessment.Severity.LOW,
                        "Galaxy cultural diversity is very low (" +
                                identityReport.getCulturalDiversityIndex() + "%). " +
                                "Limited variety in government types and ethos values.");
                assessment.addRecommendation("Encourage cultural diversity through diplomatic exchanges.");
            }

            log.info("Culture assessment for '{}': {} findings added", galaxy.getName(),
                    assessment.getFindings().size());

        } catch (Exception e) {
            log.error("Culture assessment failed for galaxy '{}': {}", galaxy.getName(), e.getMessage(), e);
        }
    }

    // ------------------------------------------------------------------
    // DTOs
    // ------------------------------------------------------------------

    /**
     * Report on cultural identity across a galaxy.
     */
    public static class CulturalIdentityReport {
        private Long galaxyId;
        private String galaxyName;
        private List<EmpireCulturalProfile> empireProfiles;
        private int averageIdentityStrength;
        private int averageCohesion;
        private int culturalDiversityIndex;

        public Long getGalaxyId() { return galaxyId; }
        public void setGalaxyId(Long galaxyId) { this.galaxyId = galaxyId; }
        public String getGalaxyName() { return galaxyName; }
        public void setGalaxyName(String galaxyName) { this.galaxyName = galaxyName; }
        public List<EmpireCulturalProfile> getEmpireProfiles() { return empireProfiles; }
        public void setEmpireProfiles(List<EmpireCulturalProfile> empireProfiles) { this.empireProfiles = empireProfiles; }
        public int getAverageIdentityStrength() { return averageIdentityStrength; }
        public void setAverageIdentityStrength(int averageIdentityStrength) { this.averageIdentityStrength = averageIdentityStrength; }
        public int getAverageCohesion() { return averageCohesion; }
        public void setAverageCohesion(int averageCohesion) { this.averageCohesion = averageCohesion; }
        public int getCulturalDiversityIndex() { return culturalDiversityIndex; }
        public void setCulturalDiversityIndex(int culturalDiversityIndex) { this.culturalDiversityIndex = culturalDiversityIndex; }
    }

    /**
     * Cultural profile for a single empire.
     */
    public static class EmpireCulturalProfile {
        private Long empireId;
        private String empireName;
        private int governmentEthosHarmony;
        private int culturalIdentityStrength;
        private int culturalCohesion;
        private CulturalStability culturalStability;
        private CulturalInfluenceLevel culturalInfluenceLevel;
        private Player.GovernmentType governmentType;
        private Player.Ethos ethos;

        public Long getEmpireId() { return empireId; }
        public void setEmpireId(Long empireId) { this.empireId = empireId; }
        public String getEmpireName() { return empireName; }
        public void setEmpireName(String empireName) { this.empireName = empireName; }
        public int getGovernmentEthosHarmony() { return governmentEthosHarmony; }
        public void setGovernmentEthosHarmony(int governmentEthosHarmony) { this.governmentEthosHarmony = governmentEthosHarmony; }
        public int getCulturalIdentityStrength() { return culturalIdentityStrength; }
        public void setCulturalIdentityStrength(int culturalIdentityStrength) { this.culturalIdentityStrength = culturalIdentityStrength; }
        public int getCulturalCohesion() { return culturalCohesion; }
        public void setCulturalCohesion(int culturalCohesion) { this.culturalCohesion = culturalCohesion; }
        public CulturalStability getCulturalStability() { return culturalStability; }
        public void setCulturalStability(CulturalStability culturalStability) { this.culturalStability = culturalStability; }
        public CulturalInfluenceLevel getCulturalInfluenceLevel() { return culturalInfluenceLevel; }
        public void setCulturalInfluenceLevel(CulturalInfluenceLevel culturalInfluenceLevel) { this.culturalInfluenceLevel = culturalInfluenceLevel; }
        public Player.GovernmentType getGovernmentType() { return governmentType; }
        public void setGovernmentType(Player.GovernmentType governmentType) { this.governmentType = governmentType; }
        public Player.Ethos getEthos() { return ethos; }
        public void setEthos(Player.Ethos ethos) { this.ethos = ethos; }
    }

    /**
     * Report on propaganda infrastructure across a galaxy.
     */
    public static class PropagandaInfrastructureReport {
        private Long galaxyId;
        private String galaxyName;
        private int totalSystems;
        private int systemsPropagandaInfrastructure;
        private double infrastructurePercentage;
        private int tradePortCount;
        private int colonyDomeCount;
        private int colonyStationCount;
        private int tradeStationCount;
        private List<SystemPropagandaInfo> systemPropagandaInfos;

        public Long getGalaxyId() { return galaxyId; }
        public void setGalaxyId(Long galaxyId) { this.galaxyId = galaxyId; }
        public String getGalaxyName() { return galaxyName; }
        public void setGalaxyName(String galaxyName) { this.galaxyName = galaxyName; }
        public int getTotalSystems() { return totalSystems; }
        public void setTotalSystems(int totalSystems) { this.totalSystems = totalSystems; }
        public int getSystemsPropagandaInfrastructure() { return systemsPropagandaInfrastructure; }
        public void setSystemsPropagandaInfrastructure(int systemsPropagandaInfrastructure) { this.systemsPropagandaInfrastructure = systemsPropagandaInfrastructure; }
        public double getInfrastructurePercentage() { return infrastructurePercentage; }
        public void setInfrastructurePercentage(double infrastructurePercentage) { this.infrastructurePercentage = infrastructurePercentage; }
        public int getTradePortCount() { return tradePortCount; }
        public void setTradePortCount(int tradePortCount) { this.tradePortCount = tradePortCount; }
        public int getColonyDomeCount() { return colonyDomeCount; }
        public void setColonyDomeCount(int colonyDomeCount) { this.colonyDomeCount = colonyDomeCount; }
        public int getColonyStationCount() { return colonyStationCount; }
        public void setColonyStationCount(int colonyStationCount) { this.colonyStationCount = colonyStationCount; }
        public int getTradeStationCount() { return tradeStationCount; }
        public void setTradeStationCount(int tradeStationCount) { this.tradeStationCount = tradeStationCount; }
        public List<SystemPropagandaInfo> getSystemPropagandaInfos() { return systemPropagandaInfos; }
        public void setSystemPropagandaInfos(List<SystemPropagandaInfo> systemPropagandaInfos) { this.systemPropagandaInfos = systemPropagandaInfos; }
    }

    /**
     * Propaganda infrastructure info for a single system.
     */
    public static class SystemPropagandaInfo {
        private String systemName;
        private int sectorRow;
        private int sectorCol;
        private boolean hasTradePort;
        private boolean hasColonyDome;
        private PropagandaStatus propagandaStatus;

        public String getSystemName() { return systemName; }
        public void setSystemName(String systemName) { this.systemName = systemName; }
        public int getSectorRow() { return sectorRow; }
        public void setSectorRow(int sectorRow) { this.sectorRow = sectorRow; }
        public int getSectorCol() { return sectorCol; }
        public void setSectorCol(int sectorCol) { this.sectorCol = sectorCol; }
        public boolean isHasTradePort() { return hasTradePort; }
        public void setHasTradePort(boolean hasTradePort) { this.hasTradePort = hasTradePort; }
        public boolean isHasColonyDome() { return hasColonyDome; }
        public void setHasColonyDome(boolean hasColonyDome) { this.hasColonyDome = hasColonyDome; }
        public PropagandaStatus getPropagandaStatus() { return propagandaStatus; }
        public void setPropagandaStatus(PropagandaStatus propagandaStatus) { this.propagandaStatus = propagandaStatus; }
    }

    /**
     * Represents the propaganda infrastructure status of a system.
     */
    public enum PropagandaStatus {
        FULLY_EQUIPPED,       // Trade port + colony dome present
        PARTIALLY_EQUIPPED,   // Only trade port or only colony dome
        NO_INFRASTRUCTURE     // No propaganda facilities
    }

    /**
     * Report on morale levels across a galaxy.
     */
    public static class MoraleReport {
        private Long galaxyId;
        private String galaxyName;
        private int totalPlanets;
        private double averageMorale;
        private int lowMoralePlanets;
        private int criticalMoralePlanets;
        private List<PlanetMoraleInfo> planetMoraleInfos;

        public Long getGalaxyId() { return galaxyId; }
        public void setGalaxyId(Long galaxyId) { this.galaxyId = galaxyId; }
        public String getGalaxyName() { return galaxyName; }
        public void setGalaxyName(String galaxyName) { this.galaxyName = galaxyName; }
        public int getTotalPlanets() { return totalPlanets; }
        public void setTotalPlanets(int totalPlanets) { this.totalPlanets = totalPlanets; }
        public double getAverageMorale() { return averageMorale; }
        public void setAverageMorale(double averageMorale) { this.averageMorale = averageMorale; }
        public int getLowMoralePlanets() { return lowMoralePlanets; }
        public void setLowMoralePlanets(int lowMoralePlanets) { this.lowMoralePlanets = lowMoralePlanets; }
        public int getCriticalMoralePlanets() { return criticalMoralePlanets; }
        public void setCriticalMoralePlanets(int criticalMoralePlanets) { this.criticalMoralePlanets = criticalMoralePlanets; }
        public List<PlanetMoraleInfo> getPlanetMoraleInfos() { return planetMoraleInfos; }
        public void setPlanetMoraleInfos(List<PlanetMoraleInfo> planetMoraleInfos) { this.planetMoraleInfos = planetMoraleInfos; }
    }

    /**
     * Morale info for a single planet.
     */
    public static class PlanetMoraleInfo {
        private String planetName;
        private String systemName;
        private int sectorRow;
        private int sectorCol;
        private int morale;
        private String moraleCategory;
        private boolean unrestRisk;

        public String getPlanetName() { return planetName; }
        public void setPlanetName(String planetName) { this.planetName = planetName; }
        public String getSystemName() { return systemName; }
        public void setSystemName(String systemName) { this.systemName = systemName; }
        public int getSectorRow() { return sectorRow; }
        public void setSectorRow(int sectorRow) { this.sectorRow = sectorRow; }
        public int getSectorCol() { return sectorCol; }
        public void setSectorCol(int sectorCol) { this.sectorCol = sectorCol; }
        public int getMorale() { return morale; }
        public void setMorale(int morale) { this.morale = morale; }
        public String getMoraleCategory() { return moraleCategory; }
        public void setMoraleCategory(String moraleCategory) { this.moraleCategory = moraleCategory; }
        public boolean isUnrestRisk() { return unrestRisk; }
        public void setUnrestRisk(boolean unrestRisk) { this.unrestRisk = unrestRisk; }
    }

    /**
     * Report on cultural influence across a galaxy.
     */
    public static class CulturalInfluenceReport {
        private Long galaxyId;
        private String galaxyName;
        private List<EmpireInfluenceProfile> influenceProfiles;
        private String dominantCulture;

        public Long getGalaxyId() { return galaxyId; }
        public void setGalaxyId(Long galaxyId) { this.galaxyId = galaxyId; }
        public String getGalaxyName() { return galaxyName; }
        public void setGalaxyName(String galaxyName) { this.galaxyName = galaxyName; }
        public List<EmpireInfluenceProfile> getInfluenceProfiles() { return influenceProfiles; }
        public void setInfluenceProfiles(List<EmpireInfluenceProfile> influenceProfiles) { this.influenceProfiles = influenceProfiles; }
        public String getDominantCulture() { return dominantCulture; }
        public void setDominantCulture(String dominantCulture) { this.dominantCulture = dominantCulture; }
    }

    /**
     * Influence profile for a single empire.
     */
    public static class EmpireInfluenceProfile {
        private Long empireId;
        private String empireName;
        private int planetCount;
        private double planetMarketShare;
        private int culturalIdentityStrength;
        private int culturalAppeal;
        private int influenceScore;
        private CulturalInfluenceLevel influenceLevel;

        public Long getEmpireId() { return empireId; }
        public void setEmpireId(Long empireId) { this.empireId = empireId; }
        public String getEmpireName() { return empireName; }
        public void setEmpireName(String empireName) { this.empireName = empireName; }
        public int getPlanetCount() { return planetCount; }
        public void setPlanetCount(int planetCount) { this.planetCount = planetCount; }
        public double getPlanetMarketShare() { return planetMarketShare; }
        public void setPlanetMarketShare(double planetMarketShare) { this.planetMarketShare = planetMarketShare; }
        public int getCulturalIdentityStrength() { return culturalIdentityStrength; }
        public void setCulturalIdentityStrength(int culturalIdentityStrength) { this.culturalIdentityStrength = culturalIdentityStrength; }
        public int getCulturalAppeal() { return culturalAppeal; }
        public void setCulturalAppeal(int culturalAppeal) { this.culturalAppeal = culturalAppeal; }
        public int getInfluenceScore() { return influenceScore; }
        public void setInfluenceScore(int influenceScore) { this.influenceScore = influenceScore; }
        public CulturalInfluenceLevel getInfluenceLevel() { return influenceLevel; }
        public void setInfluenceLevel(CulturalInfluenceLevel influenceLevel) { this.influenceLevel = influenceLevel; }
    }

    /**
     * Report on cultural stability across a galaxy.
     */
    public static class CulturalStabilityReport {
        private Long galaxyId;
        private String galaxyName;
        private List<EmpireStabilityProfile> stabilityProfiles;
        private int totalUnrestRiskSystems;
        private int totalCrisisSystems;
        private double averageStabilityScore;

        public Long getGalaxyId() { return galaxyId; }
        public void setGalaxyId(Long galaxyId) { this.galaxyId = galaxyId; }
        public String getGalaxyName() { return galaxyName; }
        public void setGalaxyName(String galaxyName) { this.galaxyName = galaxyName; }
        public List<EmpireStabilityProfile> getStabilityProfiles() { return stabilityProfiles; }
        public void setStabilityProfiles(List<EmpireStabilityProfile> stabilityProfiles) { this.stabilityProfiles = stabilityProfiles; }
        public int getTotalUnrestRiskSystems() { return totalUnrestRiskSystems; }
        public void setTotalUnrestRiskSystems(int totalUnrestRiskSystems) { this.totalUnrestRiskSystems = totalUnrestRiskSystems; }
        public int getTotalCrisisSystems() { return totalCrisisSystems; }
        public void setTotalCrisisSystems(int totalCrisisSystems) { this.totalCrisisSystems = totalCrisisSystems; }
        public double getAverageStabilityScore() { return averageStabilityScore; }
        public void setAverageStabilityScore(double averageStabilityScore) { this.averageStabilityScore = averageStabilityScore; }
    }

    /**
     * Stability profile for a single empire.
     */
    public static class EmpireStabilityProfile {
        private Long empireId;
        private String empireName;
        private int governmentEthosHarmony;
        private int culturalIdentityStrength;
        private int stabilityScore;
        private CulturalStability stabilityCategory;
        private double unrestRiskPercentage;

        public Long getEmpireId() { return empireId; }
        public void setEmpireId(Long empireId) { this.empireId = empireId; }
        public String getEmpireName() { return empireName; }
        public void setEmpireName(String empireName) { this.empireName = empireName; }
        public int getGovernmentEthosHarmony() { return governmentEthosHarmony; }
        public void setGovernmentEthosHarmony(int governmentEthosHarmony) { this.governmentEthosHarmony = governmentEthosHarmony; }
        public int getCulturalIdentityStrength() { return culturalIdentityStrength; }
        public void setCulturalIdentityStrength(int culturalIdentityStrength) { this.culturalIdentityStrength = culturalIdentityStrength; }
        public int getStabilityScore() { return stabilityScore; }
        public void setStabilityScore(int stabilityScore) { this.stabilityScore = stabilityScore; }
        public CulturalStability getStabilityCategory() { return stabilityCategory; }
        public void setStabilityCategory(CulturalStability stabilityCategory) { this.stabilityCategory = stabilityCategory; }
        public double getUnrestRiskPercentage() { return unrestRiskPercentage; }
        public void setUnrestRiskPercentage(double unrestRiskPercentage) { this.unrestRiskPercentage = unrestRiskPercentage; }
    }
}
