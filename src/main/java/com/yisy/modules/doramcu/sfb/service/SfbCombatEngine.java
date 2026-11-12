package com.yisy.modules.doramcu.sfb.service;

import com.yisy.modules.doramcu.sfb.model.*;
import com.yisy.modules.doramcu.util.DiceUtil;

import java.util.*;

/**
 * Orchestrates the 32-impulse turn loop in SFB.
 * <p>
 * Coordinates energy allocation phase, impulse procedure (movement, weapons,
 * seeking weapons, damage), and turn completion.
 * </p>
 */
public class SfbCombatEngine {

    private final ImpulseManager impulseManager;
    private final EnergyAllocationService energyAllocationService;
    private final MovementService movementService;
    private final WeaponsFireService weaponsFireService;
    private final DamageAllocationService damageAllocationService;
    private final SpecialSystemsService specialSystemsService;
    private final SfbAIService aiService;
    private final SfbRoleSystem roleSystem;
    private final SfbCharacterBridge characterBridge;
    private final com.yisy.modules.doramcu.service.AiCommanderExperienceService experienceService;

    public SfbCombatEngine() {
        this.impulseManager = new ImpulseManager();
        this.energyAllocationService = new EnergyAllocationService();
        this.movementService = new MovementService();
        this.weaponsFireService = new WeaponsFireService();
        this.damageAllocationService = new DamageAllocationService();
        this.specialSystemsService = new SpecialSystemsService();
        this.aiService = new SfbAIService();
        this.characterBridge = new SfbCharacterBridge();
        this.roleSystem = new SfbRoleSystem(characterBridge);
        this.experienceService = null;
    }

    /**
     * Constructor with injected dependencies (for Spring/SFB config).
     */
    public SfbCombatEngine(ImpulseManager impulseManager, EnergyAllocationService energyAllocationService,
                           MovementService movementService, WeaponsFireService weaponsFireService,
                           DamageAllocationService damageAllocationService,
                           SpecialSystemsService specialSystemsService,
                           SfbAIService aiService, SfbRoleSystem roleSystem,
                           SfbCharacterBridge characterBridge) {
        this(impulseManager, energyAllocationService, movementService, weaponsFireService,
                damageAllocationService, specialSystemsService, aiService, roleSystem,
                characterBridge, null);
    }

    /**
     * Constructor with injected dependencies including experience service.
     */
    public SfbCombatEngine(ImpulseManager impulseManager, EnergyAllocationService energyAllocationService,
                           MovementService movementService, WeaponsFireService weaponsFireService,
                           DamageAllocationService damageAllocationService,
                           SpecialSystemsService specialSystemsService,
                           SfbAIService aiService, SfbRoleSystem roleSystem,
                           SfbCharacterBridge characterBridge,
                           com.yisy.modules.doramcu.service.AiCommanderExperienceService experienceService) {
        this.impulseManager = impulseManager;
        this.energyAllocationService = energyAllocationService;
        this.movementService = movementService;
        this.weaponsFireService = weaponsFireService;
        this.damageAllocationService = damageAllocationService;
        this.specialSystemsService = specialSystemsService;
        this.aiService = aiService;
        this.roleSystem = roleSystem;
        this.characterBridge = characterBridge;
        this.experienceService = experienceService;
    }

    public ImpulseManager getImpulseManager() { return impulseManager; }
    public EnergyAllocationService getEnergyAllocationService() { return energyAllocationService; }
    public MovementService getMovementService() { return movementService; }
    public WeaponsFireService getWeaponsFireService() { return weaponsFireService; }
    public DamageAllocationService getDamageAllocationService() { return damageAllocationService; }
    public SpecialSystemsService getSpecialSystemsService() { return specialSystemsService; }
    public SfbAIService getAiService() { return aiService; }
    public SfbRoleSystem getRoleSystem() { return roleSystem; }
    public SfbCharacterBridge getCharacterBridge() { return characterBridge; }

    /**
     * Records experience for AI admirals after a battle.
     * If the experience service is configured, it will record experience
     * for each admiral based on battle performance.
     *
     * @param scenario the completed battle scenario
     */
    public void recordAdmiralExperience(SfbScenario scenario) {
        if (experienceService == null) {
            return;
        }
        if (scenario == null) {
            return;
        }

        // Determine surviving ships per side
        int side0Surviving = 0;
        int side1Surviving = 0;
        for (SfbShip ship : scenario.getShips()) {
            if (!ship.isDestroyed()) {
                if (ship.getSide() == 0) {
                    side0Surviving++;
                } else {
                    side1Surviving++;
                }
            }
        }

        // Determine outcome for each side
        com.yisy.modules.doramcu.eve.model.AiCommanderExperience.Outcome outcome0;
        com.yisy.modules.doramcu.eve.model.AiCommanderExperience.Outcome outcome1;

        if (side0Surviving > 0 && side1Surviving > 0) {
            // Both sides have survivors — stalemate
            outcome0 = com.yisy.modules.doramcu.eve.model.AiCommanderExperience.Outcome.STALEMATE;
            outcome1 = com.yisy.modules.doramcu.eve.model.AiCommanderExperience.Outcome.STALEMATE;
        } else if (side0Surviving > 0 && side1Surviving == 0) {
            // Side 0 wins
            outcome0 = com.yisy.modules.doramcu.eve.model.AiCommanderExperience.Outcome.VICTORY;
            outcome1 = com.yisy.modules.doramcu.eve.model.AiCommanderExperience.Outcome.DEFEAT;
        } else if (side0Surviving == 0 && side1Surviving > 0) {
            // Side 1 wins
            outcome0 = com.yisy.modules.doramcu.eve.model.AiCommanderExperience.Outcome.DEFEAT;
            outcome1 = com.yisy.modules.doramcu.eve.model.AiCommanderExperience.Outcome.VICTORY;
        } else {
            // No survivors on either side — stalemate (mutual destruction)
            outcome0 = com.yisy.modules.doramcu.eve.model.AiCommanderExperience.Outcome.STALEMATE;
            outcome1 = com.yisy.modules.doramcu.eve.model.AiCommanderExperience.Outcome.STALEMATE;
        }

        com.yisy.modules.doramcu.eve.model.AiCommanderExperience.Outcome[] outcomes =
                new com.yisy.modules.doramcu.eve.model.AiCommanderExperience.Outcome[]{outcome0, outcome1};

        String description = "SFB Battle: " + scenario.getName() + " (turn " + scenario.getCurrentTurn() + ")";

        for (int side = 0; side < 2; side++) {
            SfbCharacterAssignment admiral = scenario.getAdmiralAssignment(side);
            if (admiral != null && admiral.getAiCharacterId() != null) {
                try {
                    experienceService.recordBattleExperience(
                            admiral.getAiCharacterId(),
                            "SFB",
                            "Space Battle",
                            outcomes[side],
                            description,
                            scenario.getName());
                } catch (Exception e) {
                    // Silently ignore experience recording failures
                }
            }
        }
    }

    /**
     * Run a complete turn for the scenario.
     * Returns true if the battle is over after this turn.
     */
    public boolean runTurn(SfbScenario scenario) {
        // Reset movement tracking for all ships at turn start
        for (SfbShip ship : scenario.getShips()) {
            if (ship.isInCombat()) {
                ship.resetMovementTracking();
            }
        }

        scenario.startTurn();

        // Energy allocation phase
        processEnergyAllocation(scenario);

        // Impulse procedure
        for (int impulse = 1; impulse <= 32; impulse++) {
            scenario.nextImpulse();
            processImpulse(scenario, impulse);

            if (scenario.isBattleOver()) {
                return true;
            }
        }

        // Turn complete
        scenario.setCurrentPhase(SfbScenario.ScenarioPhase.TURN_COMPLETE);
        scenario.logEvent(new SfbEvent(scenario.getCurrentTurn(), 0,
                SfbEvent.EventType.TURN_COMPLETED, "Turn " + scenario.getCurrentTurn() + " completed", null));

        return scenario.isBattleOver();
    }

    /**
     * Process the energy allocation phase.
     * Applies character modifiers (Admiral fleet bonuses, Captain/Crew bonuses) before allocation.
     */
    private void processEnergyAllocation(SfbScenario scenario) {
        // Apply Admiral modifiers to each fleet
        for (int side = 0; side < 2; side++) {
            SfbCharacterAssignment admiral = scenario.getAdmiralAssignment(side);
            if (admiral != null) {
                List<SfbShip> fleet = scenario.getShipsBySide(side);
                roleSystem.applyAdmiralModifiers(admiral, fleet);
                scenario.logEvent(new SfbEvent(scenario.getCurrentTurn(), 0,
                        SfbEvent.EventType.SPECIAL_SYSTEM,
                        "Admiral " + admiral.getCharacterName() + " provides fleet bonuses to side " + side,
                        null));
            }
        }

        for (SfbShip ship : scenario.getShips()) {
            if (ship.isInCombat()) {
                // Apply Captain modifiers
                SfbCharacterAssignment captain = scenario.getCaptainAssignment(ship.getId());
                if (captain != null) {
                    roleSystem.applyCaptainModifiers(captain, ship);
                }

                // Apply Crew modifiers
                List<SfbCharacterAssignment> crew = scenario.getCrewAssignments(ship.getId());
                if (!crew.isEmpty()) {
                    roleSystem.applyCrewModifiers(crew, ship);
                }

                // Calculate energy allocation
                SfbEnergyAllocation ea = energyAllocationService.calculateAvailablePower(ship);
                
                // Apply Admiral energy bonus if available
                for (int side = 0; side < 2; side++) {
                    SfbCharacterAssignment admiral = scenario.getAdmiralAssignment(side);
                    if (admiral != null && ship.getSide() == side) {
                        int energyBonus = admiral.getModifiers() != null ? 
                                admiral.getModifiers().getOrDefault("FLEET_ENERGY_BONUS", 0) : 0;
                        if (energyBonus > 0) {
                            ea.setAuxiliaryPower(ea.getAuxiliaryPower() + energyBonus);
                        }
                    }
                }

                ship.allocateEnergy(ea);
            }
        }
        scenario.logEvent(new SfbEvent(scenario.getCurrentTurn(), 0,
                SfbEvent.EventType.ENERGY_ALLOCATED, "Energy allocation phase", null));
    }

    /**
     * Process a single impulse.
     * Includes special systems (sensors, EW, cloaking) before movement and weapons fire.
     * AI-controlled ships get strategic decisions from SfbAIService.
     */
    private void processImpulse(SfbScenario scenario, int impulse) {
        // Process special systems for all ships before movement/weapons
        processSpecialSystems(scenario, impulse);

        List<SfbShip> movingShips = impulseManager.getShipsMovingOnImpulse(scenario.getShips(), impulse);

        for (SfbShip ship : movingShips) {
            // AI ships get strategic decisions
            if (scenario.isAiShip(ship.getId())) {
                processAiShipImpulse(scenario, ship, impulse);
            } else {
                // Player-controlled ships
                processMovement(scenario, ship, impulse);
                processWeaponsFire(scenario, ship, impulse);
            }
        }

        // Update seeking weapons
        processSeekingWeapons(scenario, impulse);
    }

    /**
     * Process special systems (sensors, EW, cloaking) for all ships.
     */
    private void processSpecialSystems(SfbScenario scenario, int impulse) {
        for (SfbShip ship : scenario.getShips()) {
            if (!ship.isInCombat()) continue;

            List<SfbShip> targets = findTargets(scenario, ship);
            if (targets.isEmpty()) continue;

            // Process sensor detection for cloaked ships
            for (SfbShip target : targets) {
                if (target.isCloaked()) {
                    SpecialSystemsService.SensorResult result = 
                            specialSystemsService.detectCloakedShip(ship, target);
                    if (result.isDetected()) {
                        scenario.logEvent(new SfbEvent(scenario.getCurrentTurn(), impulse,
                                SfbEvent.EventType.SPECIAL_SYSTEM,
                                result.getMessage() + " by " + ship.getName(),
                                ship.getId()));
                    }
                }
            }

            // Process EW (Electronic Warfare)
            for (SfbShip target : targets) {
                SpecialSystemsService.EWResult ewResult = specialSystemsService.resolveEW(ship, target);
                if (ewResult.isEcmSuccess() || ewResult.isEccmSuccess()) {
                    scenario.logEvent(new SfbEvent(scenario.getCurrentTurn(), impulse,
                            SfbEvent.EventType.SPECIAL_SYSTEM,
                            ewResult.getMessage() + " (" + ship.getName() + " vs " + target.getName() + ")",
                            ship.getId()));
                }
            }
        }
    }

    /**
     * Process an impulse for an AI-controlled ship.
     */
    private void processAiShipImpulse(SfbScenario scenario, SfbShip ship, int impulse) {
        if (!ship.isInCombat()) return;

        List<SfbShip> targets = findTargets(scenario, ship);
        if (targets.isEmpty()) return;

        // AI decides movement
        int movement = aiService.decideMovement(ship, targets);
        if (movement > 0) {
            movementService.moveShip(ship, impulse);
        } else if (movement < 0) {
            ship.turn(3);
            movementService.moveShip(ship, impulse);
        }

        // AI decides special systems
        if (aiService.decideCloak(ship, targets)) {
            if (specialSystemsService.cloakShip(ship)) {
                scenario.logEvent(new SfbEvent(scenario.getCurrentTurn(), impulse,
                        SfbEvent.EventType.SPECIAL_SYSTEM,
                        ship.getName() + " cloaks", ship.getId()));
            }
        }

        // AI decides targeting and fires
        SfbShip target = aiService.decideTarget(ship, targets);
        if (target != null) {
            // Set overload decisions for weapons
            for (com.yisy.modules.doramcu.sfb.model.SfbWeaponBank weapon : ship.getWeaponBanks()) {
                if (weapon.isOperational()) {
                    boolean overload = aiService.decideOverload(ship, weapon);
                    weapon.setOverloaded(overload);
                }
            }

            // Fire weapons
            List<WeaponsFireService.FireResult> results = weaponsFireService.resolveAllWeapons(ship, targets);
            for (WeaponsFireService.FireResult result : results) {
                scenario.logEvent(new SfbEvent(scenario.getCurrentTurn(), impulse,
                        result.isHit() ? SfbEvent.EventType.WEAPON_HIT : SfbEvent.EventType.WEAPON_MISS,
                        result.getMessage() + " (AI: " + ship.getName() + ")", ship.getId()));
            }
        }
    }

    /**
     * Process movement for a ship.
     * Uses proportional movement based on ship speed — faster ships move on more impulses.
     */
    private void processMovement(SfbScenario scenario, SfbShip ship, int impulse) {
        if (!ship.isInCombat()) return;

        // Proportional movement: ship moves only on impulses determined by its speed
        boolean moved = movementService.moveShip(ship, impulse);
        if (moved) {
            scenario.logEvent(new SfbEvent(scenario.getCurrentTurn(), impulse,
                    SfbEvent.EventType.SHIP_MOVED, 
                    ship.getName() + " moved (speed " + ship.getSpeed() + 
                    ", hex " + ship.getHexesMovedThisTurn() + "/" + 
                    movementService.getHexesPerTurn(ship) + ")", ship.getId()));
        }
    }

    /**
     * Process weapons fire for a ship.
     * Applies character modifiers (weapon damage bonus, hit probability bonus) and EW accuracy penalties.
     */
    private void processWeaponsFire(SfbScenario scenario, SfbShip ship, int impulse) {
        if (!ship.isInCombat()) return;

        // Find targets (ships on the opposite side)
        List<SfbShip> targets = findTargets(scenario, ship);
        
        // Apply character weapon modifiers
        int weaponDamageBonus = 0;
        int hitProbabilityBonus = 0;
        
        // Captain weapon damage bonus
        SfbCharacterAssignment captain = scenario.getCaptainAssignment(ship.getId());
        if (captain != null && captain.getModifiers() != null) {
            weaponDamageBonus += captain.getModifiers().getOrDefault("WEAPON_DAMAGE", 0);
        }
        
        // Crew tactical station hit probability bonus
        List<SfbCharacterAssignment> crew = scenario.getCrewAssignments(ship.getId());
        for (SfbCharacterAssignment assignment : crew) {
            if (assignment.getStation() == SfbCrewStationType.TACTICAL && assignment.getModifiers() != null) {
                hitProbabilityBonus += assignment.getModifiers().getOrDefault("HIT_PROBABILITY", 0);
            }
        }

        // Use WeaponsFireService for resolution
        List<WeaponsFireService.FireResult> results = weaponsFireService.resolveAllWeapons(ship, targets);
        
        for (WeaponsFireService.FireResult result : results) {
            // Apply weapon damage bonus to hit results
            if (result.isHit() && weaponDamageBonus > 0) {
                int adjustedDamage = result.getDamageDealt() + weaponDamageBonus;
                int adjustedPenetrated = result.getDamagePenetrated() + weaponDamageBonus;
                result = new WeaponsFireService.FireResult(true, adjustedDamage, adjustedPenetrated,
                        result.getMessage() + " (+" + weaponDamageBonus + " captain bonus)");
            }
            
            scenario.logEvent(new SfbEvent(scenario.getCurrentTurn(), impulse,
                    result.isHit() ? SfbEvent.EventType.WEAPON_HIT : SfbEvent.EventType.WEAPON_MISS,
                    result.getMessage(), ship.getId()));
        }
    }

    /**
     * Find target ships for a given ship.
     */
    private List<SfbShip> findTargets(SfbScenario scenario, SfbShip ship) {
        List<SfbShip> targets = new ArrayList<>();
        int targetSide = 1 - ship.getSide();
        for (SfbShip other : scenario.getShips()) {
            if (other.getSide() == targetSide && other.isInCombat()) {
                targets.add(other);
            }
        }
        return targets;
    }

    /**
     * Resolve a weapon hit against a target.
     */
    public void resolveHit(SfbScenario scenario, SfbShip attacker, SfbShip target,
                           SfbWeaponBank weapon, int rangeBand, int impulse) {
        // Roll to hit
        int hitProb = weapon.getHitProbability(rangeBand);
        int roll = DiceUtil.rollD6();

        if (roll <= hitProb) {
            // Hit!
            int damage = weapon.getBaseDamage(rangeBand);
            if (weapon.isOverloaded()) {
                damage *= 2;
            }

            int direction = movementService.getDirectionTo(attacker, target);
            int penetrated = target.takeDamage(damage, direction);

            scenario.logEvent(new SfbEvent(scenario.getCurrentTurn(), impulse,
                    SfbEvent.EventType.WEAPON_HIT,
                    attacker.getName() + " hits " + target.getName() + " for " + damage + " damage",
                    attacker.getId()));

            if (penetrated > 0) {
                scenario.logEvent(new SfbEvent(scenario.getCurrentTurn(), impulse,
                        SfbEvent.EventType.DAMAGE_PENETRATED,
                        penetrated + " damage penetrates " + target.getName() + "'s shields",
                        attacker.getId()));
            }

            // Check for crippled/destroyed
            if (target.getSsd().isCrippled() && !target.isCrippled()) {
                target.setCrippled(true);
                scenario.logEvent(new SfbEvent(scenario.getCurrentTurn(), impulse,
                        SfbEvent.EventType.SHIP_CRIPPLED, target.getName() + " is crippled",
                        target.getId()));
            }

            if (target.isDestroyed()) {
                scenario.logEvent(new SfbEvent(scenario.getCurrentTurn(), impulse,
                        SfbEvent.EventType.SHIP_DESTROYED, target.getName() + " is destroyed",
                        target.getId()));
            }
        } else {
            // Miss
            scenario.logEvent(new SfbEvent(scenario.getCurrentTurn(), impulse,
                    SfbEvent.EventType.WEAPON_MISS,
                    attacker.getName() + " misses " + target.getName(),
                    attacker.getId()));
        }
    }

    /**
     * Process seeking weapons (plasma torpedoes, drones).
     */
    private void processSeekingWeapons(SfbScenario scenario, int impulse) {
        for (SfbShip ship : scenario.getShips()) {
            if (!ship.isInCombat()) continue;
            
            List<SfbShip> targets = findTargets(scenario, ship);
            List<WeaponsFireService.FireResult> results = weaponsFireService.updateSeekingWeapons(ship, targets);
            
            for (WeaponsFireService.FireResult result : results) {
                scenario.logEvent(new SfbEvent(scenario.getCurrentTurn(), impulse,
                        SfbEvent.EventType.WEAPON_HIT,
                        result.getMessage(), ship.getId()));
            }
        }
    }

    /**
     * Run the entire battle until one side is eliminated.
     * Returns the winning side (0 or 1), or -1 for stalemate.
     */
    public int runBattle(SfbScenario scenario, int maxTurns) {
        for (int turn = 1; turn <= maxTurns; turn++) {
            boolean over = runTurn(scenario);
            if (over) {
                return determineWinner(scenario);
            }
        }
        return -1; // Stalemate after max turns
    }

    /**
     * Determine the winner of the battle.
     */
    private int determineWinner(SfbScenario scenario) {
        List<SfbShip> side0 = scenario.getActiveShipsBySide(0);
        List<SfbShip> side1 = scenario.getActiveShipsBySide(1);

        if (side0.isEmpty() && side1.isEmpty()) return -1; // Mutual destruction
        if (side0.isEmpty()) return 1;
        if (side1.isEmpty()) return 0;
        return -1; // Should not reach here
    }
}
