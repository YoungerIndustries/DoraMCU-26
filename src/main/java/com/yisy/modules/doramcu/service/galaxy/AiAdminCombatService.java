package com.yisy.modules.doramcu.service.galaxy;

import com.yisy.modules.doramcu.admin.model.AiAdminOrder;
import com.yisy.modules.doramcu.admin.service.AiAdminService;
import com.yisy.modules.doramcu.hex.Battle;
import com.yisy.modules.doramcu.hex.Battle.BattleOutcome;
import com.yisy.modules.doramcu.hex.Battle.BattleResult;
import com.yisy.modules.doramcu.hex.Fleet;
import com.yisy.modules.doramcu.hex.Hex3D;
import com.yisy.modules.doramcu.hex.Ship;
import com.yisy.modules.doramcu.hex.ShipTactic;
import com.yisy.modules.doramcu.hex.SpaceStation;
import com.yisy.modules.doramcu.hex.SpaceStationType;
import com.yisy.modules.doramcu.hex.model.InfrastructureItem;
import com.yisy.modules.doramcu.hex.repository.InfrastructureItemRepository;
import com.yisy.modules.doramcu.model.galaxy.Galaxy;
import com.yisy.modules.doramcu.hex.Planet;
import com.yisy.modules.doramcu.repository.galaxy.GalaxyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI Admin Combat Service — the domain-specific combat manager that reports to the Galaxy Overseer.
 * <p>
 * This service is responsible for all combat-related operations across a galaxy:
 * </p>
 * <ul>
 *   <li>Assesses fleet combat readiness (hull/shield levels, crew status, weapon loadout)</li>
 *   <li>Evaluates threats from hostile fleets and identifies defense gaps</li>
 *   <li>Simulates battles to predict outcomes before committing forces</li>
 *   <li>Recommends combat strategies (attack, defend, retreat, reinforce)</li>
 *   <li>Creates work orders to address combat readiness issues</li>
 *   <li>Monitors military infrastructure (defense turrets, military bases, military stations)</li>
 * </ul>
 * <p>
 * Combat priority order for work order creation:
 * </p>
 * <ol>
 *   <li>CRITICAL — Fleet destroyed or critically damaged without backup</li>
 *   <li>CRITICAL — Uncontested hostile fleet in galaxy territory</li>
 *   <li>HIGH — Fleet below combat readiness threshold</li>
 *   <li>HIGH — No defense infrastructure at strategic planet</li>
 *   <li>MEDIUM — Insufficient military stations for territory coverage</li>
 *   <li>MEDIUM — Fleet shield levels below optimal threshold</li>
 *   <li>LOW — Recommend fleet repositioning or reinforcement</li>
 * </ol>
 * <p>
 * This service uses the existing Battle simulation framework to predict outcomes
 * and make strategic decisions. It integrates with CombatManagerService for
 * defense infrastructure assessment and with AiAdminProductionService for
 * ship construction orders.
 * </p>
 */
@Service
public class AiAdminCombatService {

    private static final Logger log = LoggerFactory.getLogger(AiAdminCombatService.class);

    private final AiAdminService aiAdminService;
    private final InfrastructureItemRepository infrastructureItemRepository;
    private final GalaxyRepository galaxyRepository;

    /** Minimum fleet health ratio below which a fleet is considered combat ineffective. */
    public static final double FLEET_COMBAT_INEFFECTIVE_THRESHOLD = 0.3;

    /** Fleet health ratio below which a HIGH priority work order is created. */
    public static final double FLEET_LOW_READINESS_THRESHOLD = 0.6;

    /** Minimum shield ratio below which shield recharging is recommended. */
    public static final double FLEET_SHIELD_LOW_THRESHOLD = 0.4;

    /** Minimum number of ships for a fleet to be considered a viable combat force. */
    public static final int MIN_COMBAT_FLEET_SIZE = 2;

    /** Maximum acceptable distance (in hex units) between military stations. */
    public static final int MAX_MILITARY_STATION_GAP = 10;

    /** Battle simulation confidence threshold — below this, recommend caution. */
    public static final double BATTLE_SIMULATION_CONFIDENCE_THRESHOLD = 0.6;

    public AiAdminCombatService(AiAdminService aiAdminService,
                                 InfrastructureItemRepository infrastructureItemRepository,
                                 GalaxyRepository galaxyRepository) {
        this.aiAdminService = aiAdminService;
        this.infrastructureItemRepository = infrastructureItemRepository;
        this.galaxyRepository = galaxyRepository;
    }

    // ------------------------------------------------------------------
    // Fleet Readiness Assessment
    // ------------------------------------------------------------------

    /**
     * Assesses the combat readiness of all fleets in a galaxy.
     * <p>
     * Evaluates each fleet's health ratio, shield status, crew readiness,
     * and overall combat effectiveness. Identifies fleets that need repair,
     * reinforcement, or repositioning.
     * </p>
     *
     * @param galaxy the galaxy to assess
     * @param fleets list of fleets in the galaxy
     * @return a fleet readiness report
     */
    public FleetReadinessReport assessFleetReadiness(Galaxy galaxy, List<Fleet> fleets) {
        FleetReadinessReport report = new FleetReadinessReport();
        report.setGalaxyName(galaxy.getName());
        report.setTotalFleets(fleets != null ? fleets.size() : 0);

        if (fleets == null || fleets.isEmpty()) {
            report.setCombatReadyFleets(0);
            report.setCombatIneffectiveFleets(0);
            report.setNeedsRepairFleets(0);
            log.info("No fleets found in galaxy '{}' — no combat capability", galaxy.getName());
            return report;
        }

        int combatReady = 0;
        int combatIneffective = 0;
        int needsRepair = 0;
        int totalShips = 0;
        int activeShips = 0;
        int totalFleetDamage = 0;

        List<FleetStatus> fleetStatuses = new ArrayList<>();

        for (Fleet fleet : fleets) {
            FleetStatus status = new FleetStatus();
            status.setFleetId(fleet.getId());
            status.setFleetName(fleet.getName());
            status.setShipCount(fleet.getShipCount());
            status.setActiveShipCount(fleet.getActiveShipCount());
            status.setHealthRatio(fleet.getFleetHealthRatio());
            status.setTotalDamage(fleet.getTotalDamage());
            status.setTotalShieldPoints(fleet.getTotalShieldPoints());
            status.setCurrentShieldPoints(fleet.getCurrentShieldPoints());

            totalShips += fleet.getShipCount();
            activeShips += fleet.getActiveShipCount();
            totalFleetDamage += fleet.getTotalDamage();

            // Calculate shield ratio
            int maxShields = fleet.getTotalShieldPoints();
            int currentShields = fleet.getCurrentShieldPoints();
            double shieldRatio = maxShields > 0 ? (double) currentShields / maxShields : 1.0;
            status.setShieldRatio(shieldRatio);

            // Determine combat status
            if (fleet.isDestroyed() || fleet.getActiveShipCount() == 0) {
                status.setCombatStatus(CombatStatus.DESTROYED);
                combatIneffective++;
            } else if (fleet.getFleetHealthRatio() < FLEET_COMBAT_INEFFECTIVE_THRESHOLD) {
                status.setCombatStatus(CombatStatus.COMBAT_INEFFECTIVE);
                combatIneffective++;
            } else if (fleet.getFleetHealthRatio() < FLEET_LOW_READINESS_THRESHOLD ||
                       shieldRatio < FLEET_SHIELD_LOW_THRESHOLD) {
                status.setCombatStatus(CombatStatus.NEEDS_REPAIR);
                needsRepair++;
            } else if (fleet.getActiveShipCount() < MIN_COMBAT_FLEET_SIZE) {
                status.setCombatStatus(CombatStatus.UNDERSTRENGTH);
                needsRepair++;
            } else {
                status.setCombatStatus(CombatStatus.COMBAT_READY);
                combatReady++;
            }

            fleetStatuses.add(status);
        }

        report.setFleetStatuses(fleetStatuses);
        report.setCombatReadyFleets(combatReady);
        report.setCombatIneffectiveFleets(combatIneffective);
        report.setNeedsRepairFleets(needsRepair);
        report.setTotalShips(totalShips);
        report.setActiveShips(activeShips);
        report.setTotalFleetDamage(totalFleetDamage);
        report.setOverallReadinessPercent(report.getTotalFleets() > 0
                ? (int) ((double) combatReady / report.getTotalFleets() * 100) : 0);

        log.info("Fleet readiness for galaxy '{}': {}/{} combat ready, {} need repair, {} ineffective",
                galaxy.getName(), combatReady, fleets.size(), needsRepair, combatIneffective);

        return report;
    }

    // ------------------------------------------------------------------
    // Threat Assessment
    // ------------------------------------------------------------------

    /**
     * Assesses threats within a galaxy by analyzing fleet positions,
     * defense infrastructure, and potential vulnerabilities.
     * <p>
     * Identifies hostile fleets, unprotected planets, and gaps in defense coverage.
     * </p>
     *
     * @param galaxy the galaxy to assess
     * @param friendlyFleets list of friendly fleets
     * @param hostileFleets list of hostile fleets (if known)
     * @param planets list of planets in the galaxy
     * @return a threat assessment report
     */
    public ThreatAssessmentReport assessThreats(Galaxy galaxy,
                                                  List<Fleet> friendlyFleets,
                                                  List<Fleet> hostileFleets,
                                                  List<Planet> planets) {
        ThreatAssessmentReport report = new ThreatAssessmentReport();
        report.setGalaxyName(galaxy.getName());

        // Assess hostile fleet threats
        List<ThreatInfo> threats = new ArrayList<>();
        int totalHostileDamage = 0;
        int totalHostileShips = 0;

        if (hostileFleets != null) {
            for (Fleet hostile : hostileFleets) {
                if (hostile.isDestroyed()) continue;

                ThreatInfo threat = new ThreatInfo();
                threat.setFleetId(hostile.getId());
                threat.setFleetName(hostile.getName());
                threat.setShipCount(hostile.getActiveShipCount());
                threat.setDamageCapability(hostile.getTotalDamage());
                threat.setPosition(hostile.getPosition());

                // Assess threat level based on fleet size and damage
                if (hostile.getActiveShipCount() >= 5 && hostile.getTotalDamage() > 500) {
                    threat.setThreatLevel(ThreatLevel.CRITICAL);
                } else if (hostile.getActiveShipCount() >= 3 && hostile.getTotalDamage() > 200) {
                    threat.setThreatLevel(ThreatLevel.HIGH);
                } else if (hostile.getActiveShipCount() >= 2) {
                    threat.setThreatLevel(ThreatLevel.MEDIUM);
                } else {
                    threat.setThreatLevel(ThreatLevel.LOW);
                }

                threats.add(threat);
                totalHostileDamage += hostile.getTotalDamage();
                totalHostileShips += hostile.getActiveShipCount();
            }
        }

        report.setThreats(threats);
        report.setTotalHostileDamage(totalHostileDamage);
        report.setTotalHostileShips(totalHostileShips);

        // Assess friendly fleet capability
        int totalFriendlyDamage = 0;
        int totalFriendlyShips = 0;
        if (friendlyFleets != null) {
            for (Fleet friendly : friendlyFleets) {
                if (friendly.isDestroyed()) continue;
                totalFriendlyDamage += friendly.getTotalDamage();
                totalFriendlyShips += friendly.getActiveShipCount();
            }
        }
        report.setTotalFriendlyDamage(totalFriendlyDamage);
        report.setTotalFriendlyShips(totalFriendlyShips);

        // Calculate power balance
        if (totalHostileDamage > 0) {
            report.setPowerBalance((double) totalFriendlyDamage / totalHostileDamage);
        } else if (totalFriendlyDamage > 0) {
            report.setPowerBalance(Double.POSITIVE_INFINITY);
        } else {
            report.setPowerBalance(1.0);
        }

        // Identify unprotected planets
        List<String> unprotectedPlanets = new ArrayList<>();
        if (planets != null) {
            for (Planet planet : planets) {
                boolean protectedByFleet = false;
                if (friendlyFleets != null) {
                    for (Fleet fleet : friendlyFleets) {
                        if (fleet.getHomeSystem() != null &&
                            fleet.getHomeSystem().getId() == planet.getId()) {
                            protectedByFleet = true;
                            break;
                        }
                    }
                }
                if (!protectedByFleet) {
                    unprotectedPlanets.add(planet.getName());
                }
            }
        }
        report.setUnprotectedPlanetCount(unprotectedPlanets.size());
        report.setUnprotectedPlanets(unprotectedPlanets);

        // Determine overall threat level
        if (totalHostileDamage > totalFriendlyDamage * 2 || threats.stream()
                .anyMatch(t -> t.getThreatLevel() == ThreatLevel.CRITICAL)) {
            report.setOverallThreatLevel(ThreatLevel.CRITICAL);
        } else if (totalHostileDamage > totalFriendlyDamage) {
            report.setOverallThreatLevel(ThreatLevel.HIGH);
        } else if (totalHostileDamage > 0) {
            report.setOverallThreatLevel(ThreatLevel.MEDIUM);
        } else {
            report.setOverallThreatLevel(ThreatLevel.LOW);
        }

        log.info("Threat assessment for galaxy '{}': {} hostile fleets, {} threats, overall level={}",
                galaxy.getName(), hostileFleets != null ? hostileFleets.size() : 0,
                threats.size(), report.getOverallThreatLevel());

        return report;
    }

    // ------------------------------------------------------------------
    // Battle Simulation
    // ------------------------------------------------------------------

    /**
     * Simulates a battle between two fleets to predict the outcome.
     * <p>
     * Uses the Battle class to run a full simulation without affecting actual fleet state.
     * Returns a report with predicted outcome, casualty estimates, and confidence level.
     * </p>
     *
     * @param attacker the attacking fleet
     * @param defender the defending fleet
     * @param defenderPlanet the planet where the defender is positioned (for defender bonus)
     * @return a battle simulation report
     */
    public BattleSimulationReport simulateBattle(Fleet attacker, Fleet defender, Planet defenderPlanet) {
        BattleSimulationReport report = new BattleSimulationReport();
        report.setAttackerName(attacker.getName());
        report.setDefenderName(defender.getName());

        // Create deep copies for simulation
        Fleet attackerCopy = attacker.copyFleet();
        Fleet defenderCopy = defender.copyFleet();

        report.setInitialAttackerShips(attackerCopy.getShipCount());
        report.setInitialDefenderShips(defenderCopy.getShipCount());
        report.setInitialAttackerDamage(attackerCopy.getTotalDamage());
        report.setInitialDefenderDamage(defenderCopy.getTotalDamage());

        // Create and run battle simulation
        int battleId = System.identityHashCode(attacker) ^ System.identityHashCode(defender);
        Hex3D location = defender.getPosition() != null ? defender.getPosition() :
                         (attacker.getPosition() != null ? attacker.getPosition() : new Hex3D(0, 0, 0));

        Battle battle = new Battle(battleId, attackerCopy, defenderCopy, location);
        if (defenderPlanet != null) {
            battle.setDefenderPlanet(defenderPlanet);
        }

        try {
            BattleResult result = battle.fightToCompletion();
            report.setOutcome(result.getOutcome());
            report.setTotalRounds(result.getTotalRounds());
            report.setAttackerShipsLost(result.getTotalAttackerShipsLost());
            report.setDefenderShipsLost(result.getTotalDefenderShipsLost());
            report.setAttackerDamageDealt(result.getTotalAttackerDamage());
            report.setDefenderDamageDealt(result.getTotalDefenderDamage());
            report.setCasualtyRatio(result.getCasualtyRatio());

            // Calculate confidence based on fleet size and round count
            int totalShips = report.getInitialAttackerShips() + report.getInitialDefenderShips();
            double sizeConfidence = Math.min(1.0, totalShips / 10.0);
            double roundConfidence = result.getTotalRounds() > 0 ?
                    Math.min(1.0, result.getTotalRounds() / 5.0) : 0.5;
            report.setConfidence((sizeConfidence + roundConfidence) / 2.0);

            // Generate recommendation
            if (result.getOutcome() == BattleOutcome.ATTACKER_VICTORY &&
                result.getCasualtyRatio() < 1.0) {
                report.setRecommendation(CombatRecommendation.ATTACK);
            } else if (result.getOutcome() == BattleOutcome.ATTACKER_VICTORY &&
                       result.getCasualtyRatio() >= 1.0) {
                report.setRecommendation(CombatRecommendation.PYRRHIC_VICTORY);
            } else if (result.getOutcome() == BattleOutcome.DEFENDER_VICTORY) {
                report.setRecommendation(CombatRecommendation.DEFEND);
            } else if (result.getOutcome() == BattleOutcome.MUTUAL_DESTRUCTION) {
                report.setRecommendation(CombatRecommendation.AVOID);
            } else if (result.getOutcome() == BattleOutcome.STALEMATE) {
                report.setRecommendation(CombatRecommendation.REINFORCE);
            } else {
                report.setRecommendation(CombatRecommendation.ASSESS);
            }

            // Adjust recommendation based on confidence
            if (report.getConfidence() < BATTLE_SIMULATION_CONFIDENCE_THRESHOLD) {
                report.setRecommendation(CombatRecommendation.CAUTION);
            }

        } catch (Exception e) {
            log.error("Battle simulation failed: {}", e.getMessage());
            report.setOutcome(null);
            report.setRecommendation(CombatRecommendation.ASSESS);
        }

        log.info("Battle simulation: {} vs {} -> {} (confidence: {:.1f}%)",
                attacker.getName(), defender.getName(),
                report.getRecommendation(), report.getConfidence() * 100);

        return report;
    }

    // ------------------------------------------------------------------
    // Combat Strategy Recommendation
    // ------------------------------------------------------------------

    /**
     * Provides a strategic combat recommendation based on current fleet status
     * and threat assessment.
     * <p>
     * Considers fleet readiness, threat levels, and battle simulation results
     * to recommend the optimal course of action.
     * </p>
     *
     * @param galaxyName the galaxy name for context
     * @param fleetReadiness the fleet readiness report
     * @param threatAssessment the threat assessment report
     * @return a combat strategy recommendation
     */
    public CombatStrategyRecommendation recommendStrategy(String galaxyName,
                                                            FleetReadinessReport fleetReadiness,
                                                            ThreatAssessmentReport threatAssessment) {
        CombatStrategyRecommendation strategy = new CombatStrategyRecommendation();
        strategy.setGalaxyName(galaxyName);

        // Determine primary strategy based on threat level and fleet readiness
        if (threatAssessment.getOverallThreatLevel() == ThreatLevel.CRITICAL) {
            if (fleetReadiness.getOverallReadinessPercent() >= 80) {
                strategy.setPrimaryStrategy(CombatStrategy.CONFRONT);
                strategy.setReasoning("Critical threat detected, but fleet is combat-ready. Engage hostile forces.");
            } else if (fleetReadiness.getOverallReadinessPercent() >= 50) {
                strategy.setPrimaryStrategy(CombatStrategy.DEFEND_AND_REPAIR);
                strategy.setReasoning("Critical threat detected, but fleet needs repair. Defend key positions while repairing.");
            } else {
                strategy.setPrimaryStrategy(CombatStrategy.RETREAT_AND_REINFORCE);
                strategy.setReasoning("Critical threat with insufficient forces. Retreat to safe positions and request reinforcements.");
            }
        } else if (threatAssessment.getOverallThreatLevel() == ThreatLevel.HIGH) {
            if (fleetReadiness.getOverallReadinessPercent() >= 70) {
                strategy.setPrimaryStrategy(CombatStrategy.PREEMPTIVE_STRIKE);
                strategy.setReasoning("High threat level with adequate forces. Consider preemptive engagement.");
            } else {
                strategy.setPrimaryStrategy(CombatStrategy.FORTIFY);
                strategy.setReasoning("High threat level but fleet needs preparation. Fortify positions and repair fleets.");
            }
        } else if (threatAssessment.getOverallThreatLevel() == ThreatLevel.MEDIUM) {
            strategy.setPrimaryStrategy(CombatStrategy.PATROL);
            strategy.setReasoning("Medium threat level. Maintain patrol and monitor hostile movements.");
        } else {
            strategy.setPrimaryStrategy(CombatStrategy.STAND_DOWN);
            strategy.setReasoning("Low threat level. Maintain readiness but no immediate action required.");
        }

        // Add specific recommendations
        List<String> recommendations = new ArrayList<>();

        if (fleetReadiness.getCombatIneffectiveFleets() > 0) {
            recommendations.add(String.format("Repair or replace %d combat-ineffective fleets.",
                    fleetReadiness.getCombatIneffectiveFleets()));
        }

        if (fleetReadiness.getNeedsRepairFleets() > 0) {
            recommendations.add(String.format("Schedule repairs for %d fleets below optimal readiness.",
                    fleetReadiness.getNeedsRepairFleets()));
        }

        if (threatAssessment.getUnprotectedPlanetCount() > 0) {
            recommendations.add(String.format("Assign defense fleets to %d unprotected planets.",
                    threatAssessment.getUnprotectedPlanetCount()));
        }

        if (threatAssessment.getPowerBalance() < 1.0) {
            recommendations.add("Friendly forces are outnumbered. Request reinforcements or seek diplomatic solutions.");
        }

        if (fleetReadiness.getTotalFleets() == 0) {
            recommendations.add("No fleets detected. Priority: construct combat vessels immediately.");
        }

        strategy.setRecommendations(recommendations);

        log.info("Combat strategy for galaxy '{}': {} — {}",
                galaxyName, strategy.getPrimaryStrategy(), strategy.getReasoning());

        return strategy;
    }

    // ------------------------------------------------------------------
    // Work Order Creation
    // ------------------------------------------------------------------

    /**
     * Creates work orders to address combat readiness issues.
     * <p>
     * Orders are created in priority order: CRITICAL first, then HIGH, MEDIUM, LOW.
     * </p>
     *
     * @param galaxyName the galaxy name for context
     * @param fleetReadiness the fleet readiness report
     * @param threatAssessment the threat assessment report
     * @return list of created order texts
     */
    public List<String> createCombatWorkOrders(String galaxyName,
                                                 FleetReadinessReport fleetReadiness,
                                                 ThreatAssessmentReport threatAssessment) {
        List<String> ordersCreated = new ArrayList<>();
        String submittedBy = "GalaxyOverseer:" + galaxyName;

        // CRITICAL: Combat ineffective fleets with no backup
        if (fleetReadiness.getCombatIneffectiveFleets() > 0 &&
            fleetReadiness.getCombatReadyFleets() <= 1) {
            String orderText = String.format(
                    "[CRITICAL] %d combat-ineffective fleets in galaxy '%s' with only %d combat-ready backup. " +
                    "Galaxy defense is critically compromised. Action: Emergency repair or construct replacement vessels.",
                    fleetReadiness.getCombatIneffectiveFleets(), galaxyName,
                    fleetReadiness.getCombatReadyFleets());
            submitOrder(submittedBy, orderText);
            ordersCreated.add(orderText);
        }

        // CRITICAL: Uncontested hostile fleet
        if (threatAssessment.getOverallThreatLevel() == ThreatLevel.CRITICAL &&
            fleetReadiness.getCombatReadyFleets() == 0) {
            String orderText = String.format(
                    "[CRITICAL] Critical hostile threat in galaxy '%s' with no combat-ready fleets to respond. " +
                    "Action: Emergency fleet construction or request reinforcement from allied galaxies.",
                    galaxyName);
            submitOrder(submittedBy, orderText);
            ordersCreated.add(orderText);
        }

        // HIGH: Fleets below combat readiness
        if (fleetReadiness.getNeedsRepairFleets() > 0) {
            String orderText = String.format(
                    "[HIGH] %d fleets in galaxy '%s' below combat readiness threshold. " +
                    "Action: Schedule repairs and shield recharging for affected fleets.",
                    fleetReadiness.getNeedsRepairFleets(), galaxyName);
            submitOrder(submittedBy, orderText);
            ordersCreated.add(orderText);
        }

        // HIGH: Unprotected planets with threats
        if (threatAssessment.getUnprotectedPlanetCount() > 0 &&
            threatAssessment.getOverallThreatLevel().ordinal() >= ThreatLevel.MEDIUM.ordinal()) {
            String orderText = String.format(
                    "[HIGH] %d unprotected planets in galaxy '%s' with %s threat level. " +
                    "Action: Assign defense fleets to vulnerable planets.",
                    threatAssessment.getUnprotectedPlanetCount(), galaxyName,
                    threatAssessment.getOverallThreatLevel());
            submitOrder(submittedBy, orderText);
            ordersCreated.add(orderText);
        }

        // HIGH: No fleets at all
        if (fleetReadiness.getTotalFleets() == 0) {
            String orderText = String.format(
                    "[HIGH] No fleets found in galaxy '%s'. Galaxy has no combat capability. " +
                    "Action: Construct initial defense fleet immediately.",
                    galaxyName);
            submitOrder(submittedBy, orderText);
            ordersCreated.add(orderText);
        }

        // MEDIUM: Power imbalance
        if (threatAssessment.getPowerBalance() < 1.0 &&
            threatAssessment.getPowerBalance() > 0) {
            String orderText = String.format(
                    "[MEDIUM] Power imbalance in galaxy '%s': friendly/hostile damage ratio = %.2f. " +
                    "Action: Build additional combat vessels or seek diplomatic resolution.",
                    galaxyName, threatAssessment.getPowerBalance());
            submitOrder(submittedBy, orderText);
            ordersCreated.add(orderText);
        }

        // MEDIUM: Shield levels below optimal
        int lowShieldFleets = (int) fleetReadiness.getFleetStatuses().stream()
                .filter(s -> s.getShieldRatio() < FLEET_SHIELD_LOW_THRESHOLD)
                .count();
        if (lowShieldFleets > 0) {
            String orderText = String.format(
                    "[MEDIUM] %d fleets in galaxy '%s' have shield levels below %.0f%%. " +
                    "Action: Dock fleets for shield recharging.",
                    lowShieldFleets, galaxyName, FLEET_SHIELD_LOW_THRESHOLD * 100);
            submitOrder(submittedBy, orderText);
            ordersCreated.add(orderText);
        }

        // LOW: Understrength fleets
        long understrengthCount = fleetReadiness.getFleetStatuses().stream()
                .filter(s -> s.getCombatStatus() == CombatStatus.UNDERSTRENGTH)
                .count();
        if (understrengthCount > 0) {
            String orderText = String.format(
                    "[LOW] %d fleets in galaxy '%s' are understrength (below %d ships). " +
                    "Action: Reinforce fleets to minimum combat size.",
                    understrengthCount, galaxyName, MIN_COMBAT_FLEET_SIZE);
            submitOrder(submittedBy, orderText);
            ordersCreated.add(orderText);
        }

        log.info("Created {} combat work orders for galaxy '{}'", ordersCreated.size(), galaxyName);
        return ordersCreated;
    }

    // ------------------------------------------------------------------
    // Assessment Integration
    // ------------------------------------------------------------------

    /**
     * Performs a complete combat assessment for a galaxy and adds findings
     * to the galaxy assessment. This is called by GalaxyOverseerService during
     * the health check cycle.
     *
     * @param galaxy the galaxy to assess
     * @param fleets list of fleets in the galaxy (friendly + hostile)
     * @param planets list of planets in the galaxy
     * @param assessment the galaxy assessment to add findings to
     * @return the number of work orders created
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public int assessCombatNeeds(Galaxy galaxy, List<Fleet> fleets, List<Planet> planets,
                                    GalaxyOverseerService.GalaxyAssessment assessment) {
        // Split fleets into friendly and hostile (simplified: odd IDs are hostile)
        List<Fleet> friendlyFleets = new ArrayList<>();
        List<Fleet> hostileFleets = new ArrayList<>();

        if (fleets != null) {
            for (Fleet fleet : fleets) {
                // In a real implementation, this would check fleet ownership
                // For now, treat all fleets as friendly for assessment
                friendlyFleets.add(fleet);
            }
        }

        // Assess fleet readiness
        FleetReadinessReport fleetReadiness = assessFleetReadiness(galaxy, friendlyFleets);

        // Assess threats
        ThreatAssessmentReport threatAssessment = assessThreats(galaxy, friendlyFleets, hostileFleets, planets);

        // Add fleet readiness findings
        if (fleetReadiness.getTotalFleets() == 0) {
            assessment.addFindingWithAction(
                    GalaxyOverseerService.GalaxyAssessment.Severity.HIGH,
                    String.format("No fleets in galaxy '%s'. No combat capability.", galaxy.getName()),
                    "Construct initial defense fleet."
            );
        } else {
            assessment.addFinding(
                    GalaxyOverseerService.GalaxyAssessment.Severity.INFO,
                    String.format("Fleet readiness: %d/%d combat ready (%d%%). " +
                            "Total damage: %d, Active ships: %d/%d.",
                            fleetReadiness.getCombatReadyFleets(),
                            fleetReadiness.getTotalFleets(),
                            fleetReadiness.getOverallReadinessPercent(),
                            fleetReadiness.getTotalFleetDamage(),
                            fleetReadiness.getActiveShips(),
                            fleetReadiness.getTotalShips())
            );

            if (fleetReadiness.getCombatIneffectiveFleets() > 0) {
                assessment.addFindingWithAction(
                        GalaxyOverseerService.GalaxyAssessment.Severity.HIGH,
                        String.format("%d fleets are combat-ineffective in galaxy '%s'.",
                                fleetReadiness.getCombatIneffectiveFleets(), galaxy.getName()),
                        "Repair or replace ineffective fleets."
                );
            }
        }

        // Add threat findings
        if (threatAssessment.getOverallThreatLevel() != ThreatLevel.LOW) {
            assessment.addFindingWithAction(
                    GalaxyOverseerService.GalaxyAssessment.Severity.valueOf(
                            threatAssessment.getOverallThreatLevel() == ThreatLevel.CRITICAL ?
                                    "CRITICAL" : "HIGH"),
                    String.format("%s threat level in galaxy '%s': %d hostile fleets, " +
                            "%d hostile ships, %d damage capability.",
                            threatAssessment.getOverallThreatLevel(),
                            galaxy.getName(),
                            threatAssessment.getThreats().size(),
                            threatAssessment.getTotalHostileShips(),
                            threatAssessment.getTotalHostileDamage()),
                    "Deploy defense fleets and reinforce positions."
            );
        }

        if (threatAssessment.getUnprotectedPlanetCount() > 0) {
            assessment.addFindingWithAction(
                    GalaxyOverseerService.GalaxyAssessment.Severity.MEDIUM,
                    String.format("%d unprotected planets in galaxy '%s'.",
                            threatAssessment.getUnprotectedPlanetCount(), galaxy.getName()),
                    "Assign defense fleets to vulnerable planets."
            );
        }

        // Check defense infrastructure
        List<InfrastructureItem> defenseItems = infrastructureItemRepository
                .findByItemType(InfrastructureItem.InfrastructureItemType.PLANET_FACILITY)
                .stream()
                .filter(item -> item.getItemName() != null &&
                        (item.getItemName().contains("DEFENSE") || item.getItemName().contains("MILITARY")))
                .toList();

        if (defenseItems.isEmpty()) {
            assessment.addFindingWithAction(
                    GalaxyOverseerService.GalaxyAssessment.Severity.HIGH,
                    "No defense infrastructure items defined — galaxy has no defensive capability.",
                    "Create Defense Turret and Military Base infrastructure items."
            );
        } else {
            assessment.addFinding(
                    GalaxyOverseerService.GalaxyAssessment.Severity.INFO,
                    String.format("Defense infrastructure: %d defense facility types available.",
                            defenseItems.size())
            );
        }

        // Check military station coverage
        List<InfrastructureItem> militaryStations = infrastructureItemRepository
                .findByItemType(InfrastructureItem.InfrastructureItemType.SPACE_STATION)
                .stream()
                .filter(item -> item.getItemName() != null &&
                        item.getItemName().contains("MILITARY"))
                .toList();

        if (militaryStations.isEmpty()) {
            assessment.addFindingWithAction(
                    GalaxyOverseerService.GalaxyAssessment.Severity.MEDIUM,
                    "No military space stations defined — limited strategic defense positions.",
                    "Create Military space station infrastructure items."
            );
        }

        // Create work orders
        int ordersCreated = createCombatWorkOrders(galaxy.getName(), fleetReadiness, threatAssessment).size();

        log.info("Combat assessment for galaxy '{}': {} orders created", galaxy.getName(), ordersCreated);
        return ordersCreated;
    }

    // ------------------------------------------------------------------
    // Helper Methods
    // ------------------------------------------------------------------

    private void submitOrder(String submittedBy, String orderText) {
        try {
            aiAdminService.submitOrder(submittedBy, orderText);
        } catch (Exception e) {
            log.warn("Failed to submit combat order: {}", e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // Enums
    // ------------------------------------------------------------------

    /**
     * Combat status of a fleet.
     */
    public enum CombatStatus {
        /** Fleet is at full combat readiness. */
        COMBAT_READY,
        /** Fleet needs repair or shield recharging. */
        NEEDS_REPAIR,
        /** Fleet is below minimum combat size. */
        UNDERSTRENGTH,
        /** Fleet health is critically low. */
        COMBAT_INEFFECTIVE,
        /** Fleet is destroyed. */
        DESTROYED
    }

    /**
     * Threat level classification.
     */
    public enum ThreatLevel {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    /**
     * Combat strategy options.
     */
    public enum CombatStrategy {
        /** Engage hostile forces directly. */
        CONFRONT,
        /** Launch preemptive strike before enemy is ready. */
        PREEMPTIVE_STRIKE,
        /** Defend key positions while repairing. */
        DEFEND_AND_REPAIR,
        /** Retreat and request reinforcements. */
        RETREAT_AND_REINFORCE,
        /** Fortify positions and prepare for engagement. */
        FORTIFY,
        /** Maintain patrol and monitor. */
        PATROL,
        /** Stand down — no immediate action. */
        STAND_DOWN
    }

    /**
     * Combat recommendation based on battle simulation.
     */
    public enum CombatRecommendation {
        ATTACK, DEFEND, RETREAT, REINFORCE, AVOID, PYRRHIC_VICTORY, CAUTION, ASSESS
    }

    // ------------------------------------------------------------------
    // DTOs
    // ------------------------------------------------------------------

    /**
     * Status of an individual fleet.
     */
    public static class FleetStatus {
        private int fleetId;
        private String fleetName;
        private int shipCount;
        private int activeShipCount;
        private double healthRatio;
        private int totalDamage;
        private int totalShieldPoints;
        private int currentShieldPoints;
        private double shieldRatio;
        private CombatStatus combatStatus;

        public int getFleetId() { return fleetId; }
        public void setFleetId(int fleetId) { this.fleetId = fleetId; }
        public String getFleetName() { return fleetName; }
        public void setFleetName(String fleetName) { this.fleetName = fleetName; }
        public int getShipCount() { return shipCount; }
        public void setShipCount(int shipCount) { this.shipCount = shipCount; }
        public int getActiveShipCount() { return activeShipCount; }
        public void setActiveShipCount(int activeShipCount) { this.activeShipCount = activeShipCount; }
        public double getHealthRatio() { return healthRatio; }
        public void setHealthRatio(double healthRatio) { this.healthRatio = healthRatio; }
        public int getTotalDamage() { return totalDamage; }
        public void setTotalDamage(int totalDamage) { this.totalDamage = totalDamage; }
        public int getTotalShieldPoints() { return totalShieldPoints; }
        public void setTotalShieldPoints(int totalShieldPoints) { this.totalShieldPoints = totalShieldPoints; }
        public int getCurrentShieldPoints() { return currentShieldPoints; }
        public void setCurrentShieldPoints(int currentShieldPoints) { this.currentShieldPoints = currentShieldPoints; }
        public double getShieldRatio() { return shieldRatio; }
        public void setShieldRatio(double shieldRatio) { this.shieldRatio = shieldRatio; }
        public CombatStatus getCombatStatus() { return combatStatus; }
        public void setCombatStatus(CombatStatus combatStatus) { this.combatStatus = combatStatus; }
    }

    /**
     * Report on fleet readiness across a galaxy.
     */
    public static class FleetReadinessReport {
        private String galaxyName;
        private int totalFleets;
        private int combatReadyFleets;
        private int combatIneffectiveFleets;
        private int needsRepairFleets;
        private int totalShips;
        private int activeShips;
        private int totalFleetDamage;
        private int overallReadinessPercent;
        private List<FleetStatus> fleetStatuses = new ArrayList<>();

        public String getGalaxyName() { return galaxyName; }
        public void setGalaxyName(String galaxyName) { this.galaxyName = galaxyName; }
        public int getTotalFleets() { return totalFleets; }
        public void setTotalFleets(int totalFleets) { this.totalFleets = totalFleets; }
        public int getCombatReadyFleets() { return combatReadyFleets; }
        public void setCombatReadyFleets(int combatReadyFleets) { this.combatReadyFleets = combatReadyFleets; }
        public int getCombatIneffectiveFleets() { return combatIneffectiveFleets; }
        public void setCombatIneffectiveFleets(int combatIneffectiveFleets) { this.combatIneffectiveFleets = combatIneffectiveFleets; }
        public int getNeedsRepairFleets() { return needsRepairFleets; }
        public void setNeedsRepairFleets(int needsRepairFleets) { this.needsRepairFleets = needsRepairFleets; }
        public int getTotalShips() { return totalShips; }
        public void setTotalShips(int totalShips) { this.totalShips = totalShips; }
        public int getActiveShips() { return activeShips; }
        public void setActiveShips(int activeShips) { this.activeShips = activeShips; }
        public int getTotalFleetDamage() { return totalFleetDamage; }
        public void setTotalFleetDamage(int totalFleetDamage) { this.totalFleetDamage = totalFleetDamage; }
        public int getOverallReadinessPercent() { return overallReadinessPercent; }
        public void setOverallReadinessPercent(int overallReadinessPercent) { this.overallReadinessPercent = overallReadinessPercent; }
        public List<FleetStatus> getFleetStatuses() { return fleetStatuses; }
        public void setFleetStatuses(List<FleetStatus> fleetStatuses) { this.fleetStatuses = fleetStatuses; }
    }

    /**
     * Information about a single threat.
     */
    public static class ThreatInfo {
        private int fleetId;
        private String fleetName;
        private int shipCount;
        private int damageCapability;
        private Hex3D position;
        private ThreatLevel threatLevel;

        public int getFleetId() { return fleetId; }
        public void setFleetId(int fleetId) { this.fleetId = fleetId; }
        public String getFleetName() { return fleetName; }
        public void setFleetName(String fleetName) { this.fleetName = fleetName; }
        public int getShipCount() { return shipCount; }
        public void setShipCount(int shipCount) { this.shipCount = shipCount; }
        public int getDamageCapability() { return damageCapability; }
        public void setDamageCapability(int damageCapability) { this.damageCapability = damageCapability; }
        public Hex3D getPosition() { return position; }
        public void setPosition(Hex3D position) { this.position = position; }
        public ThreatLevel getThreatLevel() { return threatLevel; }
        public void setThreatLevel(ThreatLevel threatLevel) { this.threatLevel = threatLevel; }
    }

    /**
     * Report on threats within a galaxy.
     */
    public static class ThreatAssessmentReport {
        private String galaxyName;
        private ThreatLevel overallThreatLevel;
        private List<ThreatInfo> threats = new ArrayList<>();
        private int totalHostileDamage;
        private int totalHostileShips;
        private int totalFriendlyDamage;
        private int totalFriendlyShips;
        private double powerBalance;
        private int unprotectedPlanetCount;
        private List<String> unprotectedPlanets = new ArrayList<>();

        public String getGalaxyName() { return galaxyName; }
        public void setGalaxyName(String galaxyName) { this.galaxyName = galaxyName; }
        public ThreatLevel getOverallThreatLevel() { return overallThreatLevel; }
        public void setOverallThreatLevel(ThreatLevel overallThreatLevel) { this.overallThreatLevel = overallThreatLevel; }
        public List<ThreatInfo> getThreats() { return threats; }
        public void setThreats(List<ThreatInfo> threats) { this.threats = threats; }
        public int getTotalHostileDamage() { return totalHostileDamage; }
        public void setTotalHostileDamage(int totalHostileDamage) { this.totalHostileDamage = totalHostileDamage; }
        public int getTotalHostileShips() { return totalHostileShips; }
        public void setTotalHostileShips(int totalHostileShips) { this.totalHostileShips = totalHostileShips; }
        public int getTotalFriendlyDamage() { return totalFriendlyDamage; }
        public void setTotalFriendlyDamage(int totalFriendlyDamage) { this.totalFriendlyDamage = totalFriendlyDamage; }
        public int getTotalFriendlyShips() { return totalFriendlyShips; }
        public void setTotalFriendlyShips(int totalFriendlyShips) { this.totalFriendlyShips = totalFriendlyShips; }
        public double getPowerBalance() { return powerBalance; }
        public void setPowerBalance(double powerBalance) { this.powerBalance = powerBalance; }
        public int getUnprotectedPlanetCount() { return unprotectedPlanetCount; }
        public void setUnprotectedPlanetCount(int unprotectedPlanetCount) { this.unprotectedPlanetCount = unprotectedPlanetCount; }
        public List<String> getUnprotectedPlanets() { return unprotectedPlanets; }
        public void setUnprotectedPlanets(List<String> unprotectedPlanets) { this.unprotectedPlanets = unprotectedPlanets; }
    }

    /**
     * Report from a battle simulation.
     */
    public static class BattleSimulationReport {
        private String attackerName;
        private String defenderName;
        private int initialAttackerShips;
        private int initialDefenderShips;
        private int initialAttackerDamage;
        private int initialDefenderDamage;
        private BattleOutcome outcome;
        private int totalRounds;
        private int attackerShipsLost;
        private int defenderShipsLost;
        private int attackerDamageDealt;
        private int defenderDamageDealt;
        private double casualtyRatio;
        private double confidence;
        private CombatRecommendation recommendation;

        public String getAttackerName() { return attackerName; }
        public void setAttackerName(String attackerName) { this.attackerName = attackerName; }
        public String getDefenderName() { return defenderName; }
        public void setDefenderName(String defenderName) { this.defenderName = defenderName; }
        public int getInitialAttackerShips() { return initialAttackerShips; }
        public void setInitialAttackerShips(int initialAttackerShips) { this.initialAttackerShips = initialAttackerShips; }
        public int getInitialDefenderShips() { return initialDefenderShips; }
        public void setInitialDefenderShips(int initialDefenderShips) { this.initialDefenderShips = initialDefenderShips; }
        public int getInitialAttackerDamage() { return initialAttackerDamage; }
        public void setInitialAttackerDamage(int initialAttackerDamage) { this.initialAttackerDamage = initialAttackerDamage; }
        public int getInitialDefenderDamage() { return initialDefenderDamage; }
        public void setInitialDefenderDamage(int initialDefenderDamage) { this.initialDefenderDamage = initialDefenderDamage; }
        public BattleOutcome getOutcome() { return outcome; }
        public void setOutcome(BattleOutcome outcome) { this.outcome = outcome; }
        public int getTotalRounds() { return totalRounds; }
        public void setTotalRounds(int totalRounds) { this.totalRounds = totalRounds; }
        public int getAttackerShipsLost() { return attackerShipsLost; }
        public void setAttackerShipsLost(int attackerShipsLost) { this.attackerShipsLost = attackerShipsLost; }
        public int getDefenderShipsLost() { return defenderShipsLost; }
        public void setDefenderShipsLost(int defenderShipsLost) { this.defenderShipsLost = defenderShipsLost; }
        public int getAttackerDamageDealt() { return attackerDamageDealt; }
        public void setAttackerDamageDealt(int attackerDamageDealt) { this.attackerDamageDealt = attackerDamageDealt; }
        public int getDefenderDamageDealt() { return defenderDamageDealt; }
        public void setDefenderDamageDealt(int defenderDamageDealt) { this.defenderDamageDealt = defenderDamageDealt; }
        public double getCasualtyRatio() { return casualtyRatio; }
        public void setCasualtyRatio(double casualtyRatio) { this.casualtyRatio = casualtyRatio; }
        public double getConfidence() { return confidence; }
        public void setConfidence(double confidence) { this.confidence = confidence; }
        public CombatRecommendation getRecommendation() { return recommendation; }
        public void setRecommendation(CombatRecommendation recommendation) { this.recommendation = recommendation; }
    }

    /**
     * Strategic combat recommendation.
     */
    public static class CombatStrategyRecommendation {
        private String galaxyName;
        private CombatStrategy primaryStrategy;
        private String reasoning;
        private List<String> recommendations = new ArrayList<>();

        public String getGalaxyName() { return galaxyName; }
        public void setGalaxyName(String galaxyName) { this.galaxyName = galaxyName; }
        public CombatStrategy getPrimaryStrategy() { return primaryStrategy; }
        public void setPrimaryStrategy(CombatStrategy primaryStrategy) { this.primaryStrategy = primaryStrategy; }
        public String getReasoning() { return reasoning; }
        public void setReasoning(String reasoning) { this.reasoning = reasoning; }
        public List<String> getRecommendations() { return recommendations; }
        public void setRecommendations(List<String> recommendations) { this.recommendations = recommendations; }
    }
}
