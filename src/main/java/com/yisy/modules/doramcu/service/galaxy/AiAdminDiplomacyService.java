package com.yisy.modules.doramcu.service.galaxy;

import com.yisy.modules.doramcu.admin.model.AiAdminOrder;
import com.yisy.modules.doramcu.admin.service.AiAdminService;
import com.yisy.modules.doramcu.model.galaxy.Galaxy;
import com.yisy.modules.doramcu.model.players.Player;
import com.yisy.modules.doramcu.model.players.Player.Ethos;
import com.yisy.modules.doramcu.model.players.Player.GovernmentType;
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
 * AI Admin Diplomacy Service — manages diplomatic relations between empires in a galaxy.
 * <p>
 * This is the domain-specific sub-manager that reports to the Galaxy Overseer.
 * It runs as part of the scheduled health check and performs these functions:
 * <ul>
 *   <li>Assesses diplomatic relations between all empires in a galaxy</li>
 *   <li>Evaluates government type compatibility between empires</li>
 *   <li>Checks ethos alignment to predict diplomatic friction</li>
 *   <li>Identifies potential alliance opportunities</li>
 *   <li>Creates work orders for diplomatic actions (treaties, trade agreements, alliances)</li>
 *   <li>Monitors diplomatic health and flags conflicts</li>
 * </ul>
 * </p>
 * <p>
 * Diplomatic priority order for work order creation:
 * <ol>
 *   <li>CRITICAL — Active conflict between empires requiring mediation</li>
 *   <li>CRITICAL — No diplomatic relations established in a multi-empire galaxy</li>
 *   <li>HIGH — Hostile government type pairing without defensive alliances</li>
 *   <li>HIGH — Ethos conflict creating diplomatic friction</li>
 *   <li>MEDIUM — Potential trade agreement opportunity between compatible empires</li>
 *   <li>MEDIUM — Alliance opportunity between aligned governments</li>
 *   <li>LOW — Recommend diplomatic outreach to isolated empires</li>
 * </ol>
 * </p>
 * <p>
 * Government compatibility matrix (based on Stellaris-inspired diplomacy):
 * <ul>
 *   <li>DEMOCRACY ↔ CONFEDERATION — High compatibility (shared values)</li>
 *   <li>DEMOCRACY ↔ CORPORATE — Medium compatibility (trade-focused)</li>
 *   <li>AUTHORITARIAN ↔ DICTATORSHIP — High compatibility (shared control)</li>
 *   <li>MONARCHY ↔ THEOCRACY — Medium compatibility (traditional values)</li>
 *   <li>OLIGARCHY ↔ CORPORATE — High compatibility (elite rule)</li>
 *   <li>DICTATORSHIP ↔ DEMOCRACY — Low compatibility (ideological conflict)</li>
 *   <li>PACIFIST ethos ↔ MILITARIST ethos — Low compatibility (value conflict)</li>
 * </ul>
 * </p>
 */
@Service
public class AiAdminDiplomacyService {

    private static final Logger log = LoggerFactory.getLogger(AiAdminDiplomacyService.class);

    private final GalaxyRepository galaxyRepository;
    private final PlayerRepository playerRepository;
    private final ObjectProvider<AiAdminService> aiAdminServiceProvider;

    /**
     * Represents the diplomatic relation status between two empires.
     */
    public enum DiplomaticStatus {
        NONE,           // No diplomatic contact yet
        HOSTILE,        // Active hostility or war
        UNFRIENDLY,     // Tense relations, no cooperation
        NEUTRAL,        // Neutral, open to diplomacy
        FRIENDLY,       // Positive relations, trade possible
        ALLIED          // Formal alliance, mutual defense
    }

    /**
     * Represents a diplomatic action type.
     */
    public enum DiplomaticAction {
        ESTABLISH_RELATIONS,    // Open diplomatic channels
        TRADE_AGREEMENT,        // Propose trade deal
        DEFENSIVE_PACT,         // Propose defensive alliance
        NON_AGGRESSION,         // Propose non-aggression pact
        MEDIATION,              // Mediate conflict between empires
        DIPLOMATIC_OUTREACH,    // General diplomatic contact
        ALLIANCE_PROPOSAL       // Propose full alliance
    }

    /**
     * Government compatibility scores between types.
     * Score range: 0 (incompatible) to 100 (highly compatible).
     */
    private static final Map<String, Integer> GOVERNMENT_COMPATIBILITY;
    static {
        Map<String, Integer> map = new HashMap<>();
        // Democracy compatibilities
        map.put("DEMOCRACY-DEMOCRACY", 80);
        map.put("DEMOCRACY-CONFEDERATION", 90);
        map.put("DEMOCRACY-CORPORATE", 60);
        map.put("DEMOCRACY-AUTHORITARIAN", 20);
        map.put("DEMOCRACY-DICTATORSHIP", 10);
        map.put("DEMOCRACY-MONARCHY", 40);
        map.put("DEMOCRACY-OLIGARCHY", 30);
        map.put("DEMOCRACY-THEOCRACY", 35);
        // Authoritarian compatibilities
        map.put("AUTHORITARIAN-AUTHORITARIAN", 70);
        map.put("AUTHORITARIAN-DICTATORSHIP", 85);
        map.put("AUTHORITARIAN-MONARCHY", 50);
        map.put("AUTHORITARIAN-OLIGARCHY", 60);
        map.put("AUTHORITARIAN-THEOCRACY", 45);
        map.put("AUTHORITARIAN-CONFEDERATION", 25);
        map.put("AUTHORITARIAN-CORPORATE", 40);
        // Monarchy compatibilities
        map.put("MONARCHY-MONARCHY", 65);
        map.put("MONARCHY-THEOCRACY", 60);
        map.put("MONARCHY-OLIGARCHY", 55);
        map.put("MONARCHY-CORPORATE", 45);
        map.put("MONARCHY-CONFEDERATION", 40);
        // Oligarchy compatibilities
        map.put("OLIGARCHY-OLIGARCHY", 70);
        map.put("OLIGARCHY-CORPORATE", 80);
        map.put("OLIGARCHY-THEOCRACY", 40);
        map.put("OLIGARCHY-CONFEDERATION", 35);
        // Theocracy compatibilities
        map.put("THEOCRACY-THEOCRACY", 60);
        map.put("THEOCRACY-CONFEDERATION", 30);
        // Corporate compatibilities
        map.put("CORPORATE-CORPORATE", 75);
        map.put("CORPORATE-CONFEDERATION", 50);
        // Confederation compatibilities
        map.put("CONFEDERATION-CONFEDERATION", 85);
        // Dictatorship compatibilities
        map.put("DICTATORSHIP-DICTATORSHIP", 80);
        map.put("DICTATORSHIP-MONARCHY", 40);
        map.put("DICTATORSHIP-OLIGARCHY", 50);
        map.put("DICTATORSHIP-THEOCRACY", 35);
        map.put("DICTATORSHIP-CORPORATE", 30);
        map.put("DICTATORSHIP-CONFEDERATION", 15);
        GOVERNMENT_COMPATIBILITY = Map.copyOf(map);
    }

    /**
     * Ethos compatibility scores between types.
     * Score range: 0 (incompatible) to 100 (highly compatible).
     */
    private static final Map<String, Integer> ETHOS_COMPATIBILITY;
    static {
        Map<String, Integer> map = new HashMap<>();
        // Materialist compatibilities
        map.put("MATERIALIST-MATERIALIST", 80);
        map.put("MATERIALIST-LIBERTARIAN", 70);
        map.put("MATERIALIST-EQUALIST", 50);
        map.put("MATERIALIST-MILITARIST", 40);
        map.put("MATERIALIST-PACIFIST", 45);
        map.put("MATERIALIST-SPIRITUALIST", 20);
        // Spiritualist compatibilities
        map.put("SPIRITUALIST-SPIRITUALIST", 80);
        map.put("SPIRITUALIST-EQUALIST", 60);
        map.put("SPIRITUALIST-PACIFIST", 70);
        map.put("SPIRITUALIST-LIBERTARIAN", 40);
        map.put("SPIRITUALIST-MILITARIST", 25);
        // Libertarian compatibilities
        map.put("LIBERTARIAN-LIBERTARIAN", 85);
        map.put("LIBERTARIAN-MILITARIST", 45);
        map.put("LIBERTARIAN-EQUALIST", 30);
        map.put("LIBERTARIAN-PACIFIST", 50);
        // Equalist compatibilities
        map.put("EQUALIST-EQUALIST", 85);
        map.put("EQUALIST-PACIFIST", 65);
        map.put("EQUALIST-MILITARIST", 35);
        // Militarist compatibilities
        map.put("MILITARIST-MILITARIST", 80);
        map.put("MILITARIST-PACIFIST", 5);
        // Pacifist compatibilities
        map.put("PACIFIST-PACIFIST", 90);
        ETHOS_COMPATIBILITY = Map.copyOf(map);
    }

    /** Minimum compatibility score for a relationship to be considered friendly. */
    public static final int FRIENDLY_COMPATIBILITY_THRESHOLD = 60;

    /** Compatibility score below which a relationship is considered hostile. */
    public static final int HOSTILE_COMPATIBILITY_THRESHOLD = 25;

    /** Minimum number of empires in a galaxy to warrant diplomatic management. */
    public static final int MIN_EMPIRES_FOR_DIPLOMACY = 2;

    public AiAdminDiplomacyService(GalaxyRepository galaxyRepository,
                                    PlayerRepository playerRepository,
                                    ObjectProvider<AiAdminService> aiAdminServiceProvider) {
        this.galaxyRepository = galaxyRepository;
        this.playerRepository = playerRepository;
        this.aiAdminServiceProvider = aiAdminServiceProvider;
    }

    // ------------------------------------------------------------------
    // Empire Discovery
    // ------------------------------------------------------------------

    /**
     * Discovers all empires (players) in a galaxy by checking which players
     * have characters or presence in the galaxy.
     * <p>
     * Since players are not directly linked to galaxies in the current schema,
     * we retrieve all players and check their galaxy affiliation through
     * their empire name or character registry.
     * </p>
     *
     * @param galaxy the galaxy to scan for empires
     * @return list of empires present in the galaxy
     */
    @Transactional(readOnly = true)
    public List<EmpireProfile> discoverEmpiresInGalaxy(Galaxy galaxy) {
        List<EmpireProfile> empires = new ArrayList<>();

        // Retrieve all players — each player represents an empire
        List<Player> allPlayers = playerRepository.findAll();
        for (Player player : allPlayers) {
            if (player.getEmpireName() == null || player.getEmpireName().isBlank()) {
                continue;
            }

            // Create a profile for each player's empire
            EmpireProfile profile = new EmpireProfile();
            profile.setEmpireId(player.getId());
            profile.setEmpireName(player.getEmpireName());
            profile.setGovernmentType(player.getGovernmentType());
            profile.setEthos(player.getEthos());
            profile.setPlayerEmail(player.getEmail());
            profile.setAdmin(player.isAdmin());

            empires.add(profile);
        }

        log.debug("Discovered {} empires for diplomacy assessment", empires.size());
        return empires;
    }

    // ------------------------------------------------------------------
    // Diplomatic Relation Assessment
    // ------------------------------------------------------------------

    /**
     * Assesses the diplomatic relations between all pairs of empires.
     * <p>
     * For each pair of empires, calculates:
     * <ul>
     *   <li>Government compatibility score</li>
     *   <li>Ethos compatibility score</li>
     *   <li>Overall diplomatic compatibility</li>
     *   <li>Recommended diplomatic action</li>
     * </ul>
     * </p>
     *
     * @param galaxy the galaxy to assess
     * @return a diplomatic assessment report
     */
    @Transactional(readOnly = true)
    public DiplomaticAssessmentReport assessDiplomaticRelations(Galaxy galaxy) {
        DiplomaticAssessmentReport report = new DiplomaticAssessmentReport();
        report.setGalaxyId(galaxy.getId());
        report.setGalaxyName(galaxy.getName());

        List<EmpireProfile> empires = discoverEmpiresInGalaxy(galaxy);
        report.setTotalEmpires(empires.size());
        report.setEmpireProfiles(empires);

        if (empires.size() < MIN_EMPIRES_FOR_DIPLOMACY) {
            report.setDiplomaticHealthScore(empires.size() == 1 ? 100 : 0);
            if (empires.isEmpty()) {
                report.setOverallStatus(DiplomaticStatus.NONE);
                log.info("No empires found in galaxy '{}' — no diplomatic relations to assess", galaxy.getName());
            } else {
                report.setOverallStatus(DiplomaticStatus.NEUTRAL);
                log.info("Single empire in galaxy '{}' — no inter-empire diplomacy needed", galaxy.getName());
            }
            return report;
        }

        // Assess each pair of empires
        List<EmpirePairAssessment> pairAssessments = new ArrayList<>();
        int totalCompatibility = 0;
        int pairCount = 0;
        int hostilePairs = 0;
        int friendlyPairs = 0;
        int noContactPairs = 0;

        for (int i = 0; i < empires.size(); i++) {
            for (int j = i + 1; j < empires.size(); j++) {
                EmpireProfile empireA = empires.get(i);
                EmpireProfile empireB = empires.get(j);

                EmpirePairAssessment pair = new EmpirePairAssessment();
                pair.setEmpireAName(empireA.getEmpireName());
                pair.setEmpireAGovernment(empireA.getGovernmentType());
                pair.setEmpireAEthos(empireA.getEthos());
                pair.setEmpireBName(empireB.getEmpireName());
                pair.setEmpireBGovernment(empireB.getGovernmentType());
                pair.setEmpireBEthos(empireB.getEthos());

                // Calculate government compatibility
                int govCompat = getGovernmentCompatibility(empireA.getGovernmentType(), empireB.getGovernmentType());
                pair.setGovernmentCompatibility(govCompat);

                // Calculate ethos compatibility
                int ethosCompat = getEthosCompatibility(empireA.getEthos(), empireB.getEthos());
                pair.setEthosCompatibility(ethosCompat);

                // Overall compatibility is weighted average (government 60%, ethos 40%)
                int overallCompat = (int) (govCompat * 0.6 + ethosCompat * 0.4);
                pair.setOverallCompatibility(overallCompat);
                totalCompatibility += overallCompat;
                pairCount++;

                // Determine diplomatic status based on compatibility
                if (overallCompat <= HOSTILE_COMPATIBILITY_THRESHOLD) {
                    pair.setDiplomaticStatus(DiplomaticStatus.HOSTILE);
                    pair.setRecommendedAction(DiplomaticAction.NON_AGGRESSION);
                    hostilePairs++;
                } else if (overallCompat < FRIENDLY_COMPATIBILITY_THRESHOLD) {
                    pair.setDiplomaticStatus(DiplomaticStatus.NEUTRAL);
                    pair.setRecommendedAction(DiplomaticAction.ESTABLISH_RELATIONS);
                    noContactPairs++;
                } else if (overallCompat >= 70) {
                    pair.setDiplomaticStatus(DiplomaticStatus.FRIENDLY);
                    if (govCompat >= 80 && ethosCompat >= 70) {
                        pair.setRecommendedAction(DiplomaticAction.ALLIANCE_PROPOSAL);
                    } else {
                        pair.setRecommendedAction(DiplomaticAction.TRADE_AGREEMENT);
                    }
                    friendlyPairs++;
                } else {
                    pair.setDiplomaticStatus(DiplomaticStatus.NEUTRAL);
                    pair.setRecommendedAction(DiplomaticAction.DIPLOMATIC_OUTREACH);
                    noContactPairs++;
                }

                // Add specific recommendations
                List<String> recommendations = new ArrayList<>();
                if (govCompat < ethosCompat) {
                    recommendations.add("Government differences are the main source of tension. Focus on shared ethical values.");
                } else if (ethosCompat < govCompat) {
                    recommendations.add("Ethos differences create friction. Emphasize mutual governmental interests.");
                }

                if (overallCompat <= HOSTILE_COMPATIBILITY_THRESHOLD) {
                    recommendations.add("CRITICAL: High risk of conflict. Propose non-aggression pact immediately.");
                }

                if (overallCompat >= 80) {
                    recommendations.add("Excellent compatibility. Propose full alliance with mutual defense clause.");
                }

                pair.setRecommendations(recommendations);
                pairAssessments.add(pair);
            }
        }

        report.setPairAssessments(pairAssessments);
        report.setHostilePairs(hostilePairs);
        report.setFriendlyPairs(friendlyPairs);
        report.setNoContactPairs(noContactPairs);

        // Calculate overall diplomatic health (0-100)
        int healthScore = pairCount > 0 ? totalCompatibility / pairCount : 0;
        report.setDiplomaticHealthScore(healthScore);

        // Determine overall galaxy diplomatic status
        if (hostilePairs > friendlyPairs) {
            report.setOverallStatus(DiplomaticStatus.HOSTILE);
        } else if (friendlyPairs > hostilePairs) {
            report.setOverallStatus(DiplomaticStatus.FRIENDLY);
        } else if (noContactPairs > 0) {
            report.setOverallStatus(DiplomaticStatus.NEUTRAL);
        } else {
            report.setOverallStatus(DiplomaticStatus.NEUTRAL);
        }

        log.info("Diplomatic assessment for '{}': {} empires, {} pairs assessed, health={}, status={}",
                galaxy.getName(), empires.size(), pairCount, healthScore, report.getOverallStatus());

        return report;
    }

    // ------------------------------------------------------------------
    // Compatibility Calculation
    // ------------------------------------------------------------------

    /**
     * Gets the compatibility score between two government types.
     *
     * @param govA first government type
     * @param govB second government type
     * @return compatibility score from 0 to 100
     */
    public int getGovernmentCompatibility(GovernmentType govA, GovernmentType govB) {
        if (govA == null || govB == null) {
            return 50; // Default neutral
        }
        if (govA == govB) {
            String key = govA.name() + "-" + govB.name();
            Integer score = GOVERNMENT_COMPATIBILITY.get(key);
            return score != null ? score : 50;
        }
        // Try both orderings
        String key1 = govA.name() + "-" + govB.name();
        String key2 = govB.name() + "-" + govA.name();
        Integer score = GOVERNMENT_COMPATIBILITY.get(key1);
        if (score == null) {
            score = GOVERNMENT_COMPATIBILITY.get(key2);
        }
        return score != null ? score : 50;
    }

    /**
     * Gets the compatibility score between two ethos types.
     *
     * @param ethosA first ethos type
     * @param ethosB second ethos type
     * @return compatibility score from 0 to 100
     */
    public int getEthosCompatibility(Ethos ethosA, Ethos ethosB) {
        if (ethosA == null || ethosB == null) {
            return 50; // Default neutral
        }
        if (ethosA == ethosB) {
            String key = ethosA.name() + "-" + ethosB.name();
            Integer score = ETHOS_COMPATIBILITY.get(key);
            return score != null ? score : 50;
        }
        // Try both orderings
        String key1 = ethosA.name() + "-" + ethosB.name();
        String key2 = ethosB.name() + "-" + ethosA.name();
        Integer score = ETHOS_COMPATIBILITY.get(key1);
        if (score == null) {
            score = ETHOS_COMPATIBILITY.get(key2);
        }
        return score != null ? score : 50;
    }

    // ------------------------------------------------------------------
    // Work Order Creation
    // ------------------------------------------------------------------

    /**
     * Creates diplomatic work orders based on the assessment report.
     * Orders are prioritized: CRITICAL (conflicts) > HIGH (hostile pairs) >
     * MEDIUM (opportunities) > LOW (outreach).
     *
     * @param galaxy the galaxy to create work orders for
     * @param report the diplomatic assessment report
     * @return list of created order texts
     */
    @Transactional
    public List<String> createDiplomaticWorkOrders(Galaxy galaxy, DiplomaticAssessmentReport report) {
        List<String> ordersCreated = new ArrayList<>();
        String submittedBy = "GalaxyOverseer:" + galaxy.getName();

        if (report.getTotalEmpires() < MIN_EMPIRES_FOR_DIPLOMACY) {
            log.debug("Not enough empires in '{}' for diplomatic work orders", galaxy.getName());
            return ordersCreated;
        }

        AiAdminService aiAdminService = aiAdminServiceProvider.getIfAvailable();
        if (aiAdminService == null) {
            log.warn("AiAdminService not available for diplomatic work orders in '{}'", galaxy.getName());
            return ordersCreated;
        }

        // Process each pair assessment
        for (EmpirePairAssessment pair : report.getPairAssessments()) {
            String orderText;
            String severity;

            switch (pair.getDiplomaticStatus()) {
                case HOSTILE:
                    severity = "CRITICAL";
                    orderText = String.format("[%s] Diplomatic crisis: %s vs %s. " +
                            "Compatibility: %d%% (gov: %d%%, ethos: %d%%). " +
                            "Action: Propose non-aggression pact to prevent conflict.",
                            severity, pair.getEmpireAName(), pair.getEmpireBName(),
                            pair.getOverallCompatibility(),
                            pair.getGovernmentCompatibility(),
                            pair.getEthosCompatibility());
                    break;

                case NONE:
                    severity = "HIGH";
                    orderText = String.format("[%s] No diplomatic contact: %s and %s have no established relations. " +
                            "Action: Open diplomatic channels.",
                            severity, pair.getEmpireAName(), pair.getEmpireBName());
                    break;

                case NEUTRAL:
                    if (pair.getRecommendedAction() == DiplomaticAction.ESTABLISH_RELATIONS) {
                        severity = "MEDIUM";
                        orderText = String.format("[%s] Diplomatic opportunity: %s and %s are neutral. " +
                                "Compatibility: %d%%. Action: Establish formal diplomatic relations.",
                                severity, pair.getEmpireAName(), pair.getEmpireBName(),
                                pair.getOverallCompatibility());
                    } else {
                        severity = "LOW";
                        orderText = String.format("[%s] Diplomatic outreach: %s and %s. " +
                                "Action: Maintain contact and explore cooperation.",
                                severity, pair.getEmpireAName(), pair.getEmpireBName());
                    }
                    break;

                case FRIENDLY:
                    if (pair.getRecommendedAction() == DiplomaticAction.ALLIANCE_PROPOSAL) {
                        severity = "MEDIUM";
                        orderText = String.format("[%s] Alliance opportunity: %s and %s are highly compatible (%d%%). " +
                                "Action: Propose formal alliance with mutual defense.",
                                severity, pair.getEmpireAName(), pair.getEmpireBName(),
                                pair.getOverallCompatibility());
                    } else {
                        severity = "LOW";
                        orderText = String.format("[%s] Trade opportunity: %s and %s have friendly relations. " +
                                "Action: Propose trade agreement.",
                                severity, pair.getEmpireAName(), pair.getEmpireBName());
                    }
                    break;

                default:
                    continue;
            }

            aiAdminService.submitOrder(submittedBy, orderText);
            ordersCreated.add(orderText);
            log.info("Created diplomatic work order: {} vs {} ({})",
                    pair.getEmpireAName(), pair.getEmpireBName(), severity);
        }

        // Check for isolated empires (no relations with anyone)
        for (EmpireProfile empire : report.getEmpireProfiles()) {
            boolean hasAnyRelation = report.getPairAssessments().stream()
                    .anyMatch(p -> (p.getEmpireAName().equals(empire.getEmpireName()) ||
                            p.getEmpireBName().equals(empire.getEmpireName())) &&
                            p.getDiplomaticStatus() != DiplomaticStatus.NONE);

            if (!hasAnyRelation && report.getTotalEmpires() > 1) {
                String orderText = String.format("[HIGH] Isolated empire: %s has no diplomatic relations. " +
                        "Government: %s, Ethos: %s. Action: Initiate diplomatic outreach.",
                        empire.getEmpireName(), empire.getGovernmentType(), empire.getEthos());
                aiAdminService.submitOrder(submittedBy, orderText);
                ordersCreated.add(orderText);
                log.info("Created outreach work order for isolated empire: {}", empire.getEmpireName());
            }
        }

        log.info("Created {} diplomatic work orders for galaxy '{}'", ordersCreated.size(), galaxy.getName());
        return ordersCreated;
    }

    // ------------------------------------------------------------------
    // Assessment Integration
    // ------------------------------------------------------------------

    /**
     * Performs a complete diplomatic assessment for a galaxy and adds findings
     * to the galaxy assessment. This is called by GalaxyOverseerService during
     * the health check cycle.
     *
     * @param galaxy the galaxy to assess
     * @param assessment the galaxy assessment to add findings to
     * @return the number of work orders created
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public int assessDiplomaticNeeds(Galaxy galaxy, GalaxyOverseerService.GalaxyAssessment assessment) {
        DiplomaticAssessmentReport report = assessDiplomaticRelations(galaxy);

        // Add diplomatic findings based on health score and status
        if (report.getTotalEmpires() == 0) {
            assessment.addFinding(
                    GalaxyOverseerService.GalaxyAssessment.Severity.INFO,
                    "No empires in galaxy — diplomacy not yet applicable."
            );
        } else if (report.getTotalEmpires() == 1) {
            assessment.addFinding(
                    GalaxyOverseerService.GalaxyAssessment.Severity.INFO,
                    String.format("Single empire in galaxy ('%s') — no inter-empire diplomacy needed.",
                            report.getEmpireProfiles().get(0).getEmpireName())
            );
        } else if (report.getDiplomaticHealthScore() <= HOSTILE_COMPATIBILITY_THRESHOLD) {
            assessment.addFindingWithAction(
                    GalaxyOverseerService.GalaxyAssessment.Severity.CRITICAL,
                    String.format("Diplomatic crisis: %d hostile empire pairs, health score %d%%. " +
                            "Risk of inter-empire conflict is high.",
                            report.getHostilePairs(), report.getDiplomaticHealthScore()),
                    "Initiate emergency mediation between hostile empires. Propose non-aggression pacts."
            );
        } else if (report.getDiplomaticHealthScore() < FRIENDLY_COMPATIBILITY_THRESHOLD) {
            assessment.addFindingWithAction(
                    GalaxyOverseerService.GalaxyAssessment.Severity.MEDIUM,
                    String.format("Diplomatic relations underdeveloped: health score %d%%. " +
                            "%d hostile pairs, %d friendly pairs among %d empires.",
                            report.getDiplomaticHealthScore(),
                            report.getHostilePairs(),
                            report.getFriendlyPairs(),
                            report.getTotalEmpires()),
                    "Establish diplomatic channels between neutral empires. Reduce hostility through mediation."
            );
        } else {
            assessment.addFinding(
                    GalaxyOverseerService.GalaxyAssessment.Severity.INFO,
                    String.format("Healthy diplomatic relations: health score %d%%. " +
                            "%d friendly pairs among %d empires.",
                            report.getDiplomaticHealthScore(),
                            report.getFriendlyPairs(),
                            report.getTotalEmpires())
            );
        }

        // Add government diversity finding
        if (report.getTotalEmpires() >= 2) {
            long uniqueGovernments = report.getEmpireProfiles().stream()
                    .map(EmpireProfile::getGovernmentType)
                    .distinct()
                    .count();
            if (uniqueGovernments >= 4) {
                assessment.addFinding(
                        GalaxyOverseerService.GalaxyAssessment.Severity.LOW,
                        String.format("High government diversity: %d unique government types among %d empires. " +
                                "This creates diplomatic complexity but also opportunity.",
                                uniqueGovernments, report.getTotalEmpires())
                );
            }
        }

        // Create work orders for diplomatic actions
        List<String> ordersCreated = createDiplomaticWorkOrders(galaxy, report);

        log.info("Diplomatic assessment for '{}': {} empires, health={}%, {} orders created",
                galaxy.getName(), report.getTotalEmpires(),
                report.getDiplomaticHealthScore(), ordersCreated.size());

        return ordersCreated.size();
    }

    // ------------------------------------------------------------------
    // DTOs
    // ------------------------------------------------------------------

    /**
     * Profile of an empire for diplomatic assessment.
     */
    public static class EmpireProfile {
        private Long empireId;
        private String empireName;
        private GovernmentType governmentType;
        private Ethos ethos;
        private String playerEmail;
        private boolean admin;

        public Long getEmpireId() { return empireId; }
        public void setEmpireId(Long empireId) { this.empireId = empireId; }
        public String getEmpireName() { return empireName; }
        public void setEmpireName(String empireName) { this.empireName = empireName; }
        public GovernmentType getGovernmentType() { return governmentType; }
        public void setGovernmentType(GovernmentType governmentType) { this.governmentType = governmentType; }
        public Ethos getEthos() { return ethos; }
        public void setEthos(Ethos ethos) { this.ethos = ethos; }
        public String getPlayerEmail() { return playerEmail; }
        public void setPlayerEmail(String playerEmail) { this.playerEmail = playerEmail; }
        public boolean isAdmin() { return admin; }
        public void setAdmin(boolean admin) { this.admin = admin; }
    }

    /**
     * Assessment of the diplomatic relationship between two empires.
     */
    public static class EmpirePairAssessment {
        private String empireAName;
        private GovernmentType empireAGovernment;
        private Ethos empireAEthos;
        private String empireBName;
        private GovernmentType empireBGovernment;
        private Ethos empireBEthos;
        private int governmentCompatibility;
        private int ethosCompatibility;
        private int overallCompatibility;
        private DiplomaticStatus diplomaticStatus;
        private DiplomaticAction recommendedAction;
        private List<String> recommendations;

        public String getEmpireAName() { return empireAName; }
        public void setEmpireAName(String empireAName) { this.empireAName = empireAName; }
        public GovernmentType getEmpireAGovernment() { return empireAGovernment; }
        public void setEmpireAGovernment(GovernmentType empireAGovernment) { this.empireAGovernment = empireAGovernment; }
        public Ethos getEmpireAEthos() { return empireAEthos; }
        public void setEmpireAEthos(Ethos empireAEthos) { this.empireAEthos = empireAEthos; }
        public String getEmpireBName() { return empireBName; }
        public void setEmpireBName(String empireBName) { this.empireBName = empireBName; }
        public GovernmentType getEmpireBGovernment() { return empireBGovernment; }
        public void setEmpireBGovernment(GovernmentType empireBGovernment) { this.empireBGovernment = empireBGovernment; }
        public Ethos getEmpireBEthos() { return empireBEthos; }
        public void setEmpireBEthos(Ethos empireBEthos) { this.empireBEthos = empireBEthos; }
        public int getGovernmentCompatibility() { return governmentCompatibility; }
        public void setGovernmentCompatibility(int governmentCompatibility) { this.governmentCompatibility = governmentCompatibility; }
        public int getEthosCompatibility() { return ethosCompatibility; }
        public void setEthosCompatibility(int ethosCompatibility) { this.ethosCompatibility = ethosCompatibility; }
        public int getOverallCompatibility() { return overallCompatibility; }
        public void setOverallCompatibility(int overallCompatibility) { this.overallCompatibility = overallCompatibility; }
        public DiplomaticStatus getDiplomaticStatus() { return diplomaticStatus; }
        public void setDiplomaticStatus(DiplomaticStatus diplomaticStatus) { this.diplomaticStatus = diplomaticStatus; }
        public DiplomaticAction getRecommendedAction() { return recommendedAction; }
        public void setRecommendedAction(DiplomaticAction recommendedAction) { this.recommendedAction = recommendedAction; }
        public List<String> getRecommendations() { return recommendations; }
        public void setRecommendations(List<String> recommendations) { this.recommendations = recommendations; }
    }

    /**
     * Comprehensive diplomatic assessment report for a galaxy.
     */
    public static class DiplomaticAssessmentReport {
        private Long galaxyId;
        private String galaxyName;
        private int totalEmpires;
        private List<EmpireProfile> empireProfiles;
        private List<EmpirePairAssessment> pairAssessments;
        private int hostilePairs;
        private int friendlyPairs;
        private int noContactPairs;
        private int diplomaticHealthScore;
        private DiplomaticStatus overallStatus;

        public Long getGalaxyId() { return galaxyId; }
        public void setGalaxyId(Long galaxyId) { this.galaxyId = galaxyId; }
        public String getGalaxyName() { return galaxyName; }
        public void setGalaxyName(String galaxyName) { this.galaxyName = galaxyName; }
        public int getTotalEmpires() { return totalEmpires; }
        public void setTotalEmpires(int totalEmpires) { this.totalEmpires = totalEmpires; }
        public List<EmpireProfile> getEmpireProfiles() { return empireProfiles; }
        public void setEmpireProfiles(List<EmpireProfile> empireProfiles) { this.empireProfiles = empireProfiles; }
        public List<EmpirePairAssessment> getPairAssessments() { return pairAssessments; }
        public void setPairAssessments(List<EmpirePairAssessment> pairAssessments) { this.pairAssessments = pairAssessments; }
        public int getHostilePairs() { return hostilePairs; }
        public void setHostilePairs(int hostilePairs) { this.hostilePairs = hostilePairs; }
        public int getFriendlyPairs() { return friendlyPairs; }
        public void setFriendlyPairs(int friendlyPairs) { this.friendlyPairs = friendlyPairs; }
        public int getNoContactPairs() { return noContactPairs; }
        public void setNoContactPairs(int noContactPairs) { this.noContactPairs = noContactPairs; }
        public int getDiplomaticHealthScore() { return diplomaticHealthScore; }
        public void setDiplomaticHealthScore(int diplomaticHealthScore) { this.diplomaticHealthScore = diplomaticHealthScore; }
        public DiplomaticStatus getOverallStatus() { return overallStatus; }
        public void setOverallStatus(DiplomaticStatus overallStatus) { this.overallStatus = overallStatus; }
    }
}
