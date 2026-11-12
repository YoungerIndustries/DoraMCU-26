package com.yisy.modules.doramcu.star_trek_4x.combat;

import com.yisy.modules.doramcu.earth2026.model.Earth2026BattleEnvironment;
import com.yisy.modules.doramcu.earth2026.service.PlanetService;
import com.yisy.modules.doramcu.star_trek_4x.model.PlanetaryBattle;
import com.yisy.modules.doramcu.star_trek_4x.model.StarTrekGroundUnit;
import com.yisy.modules.doramcu.star_trek_4x.repository.PlanetaryBattleRepository;
import com.yisy.modules.doramcu.star_trek_4x.repository.StarTrekGroundUnitRepository;
import com.yisy.modules.doramcu.thirdreich.commander.AICommander;
import com.yisy.modules.doramcu.thirdreich.domain.TerrainType;
import com.yisy.modules.doramcu.thirdreich.service.ThirdReichGroundBattleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Ground combat service that wraps ThirdReichGroundBattleService for Star Trek 4x.
 * <p>
 * Provides Star Trek-specific unit creation and result mapping, integrating with
 * PlanetService for Earth 2026 battle environments.
 * </p>
 */
@Service("starTrek4xGroundCombatService")
public class GroundCombatService {

    private static final Logger log = LoggerFactory.getLogger(GroundCombatService.class);

    private final ThirdReichGroundBattleService battleService;
    private final PlanetService planetService;
    private final PlanetaryBattleRepository battleRepository;
    private final StarTrekGroundUnitRepository unitRepository;

    public GroundCombatService(ThirdReichGroundBattleService battleService,
                                PlanetService planetService,
                                PlanetaryBattleRepository battleRepository,
                                StarTrekGroundUnitRepository unitRepository) {
        this.battleService = battleService;
        this.planetService = planetService;
        this.battleRepository = battleRepository;
        this.unitRepository = unitRepository;
    }

    /**
     * Initiate a planetary invasion.
     *
     * @param systemId the star system ID
     * @param attackerFactionId the attacker faction ID
     * @param defenderFactionId the defender faction ID
     * @return the created PlanetaryBattle
     */
    public PlanetaryBattle initiatePlanetaryInvasion(Long systemId, Long attackerFactionId, Long defenderFactionId) {
        log.info("Initiating planetary invasion: system={}, attacker={}, defender={}", systemId, attackerFactionId, defenderFactionId);

        PlanetaryBattle battle = new PlanetaryBattle();
        battle.setName("Invasion of System " + systemId);
        battle.setStarSystemId(systemId);
        battle.setAttackerFactionId(attackerFactionId);
        battle.setDefenderFactionId(defenderFactionId);

        return battleRepository.save(battle);
    }

    /**
     * Create a Star Trek ground unit from a character.
     *
     * @param name the unit name
     * @param unitType the unit type
     * @param characterId the character ID
     * @param ownerFactionId the owner faction ID
     * @return the created unit
     */
    public StarTrekGroundUnit createStarTrekGroundUnit(String name, StarTrekGroundUnit.UnitType unitType,
                                                        Long characterId, Long ownerFactionId) {
        log.info("Creating Star Trek ground unit: {}, type={}, character={}", name, unitType, characterId);

        StarTrekGroundUnit unit = new StarTrekGroundUnit();
        unit.setName(name);
        unit.setUnitType(unitType);
        unit.setCharacterId(characterId);
        unit.setOwnerFactionId(ownerFactionId);

        // Set default stats based on unit type
        switch (unitType) {
            case SECURITY:
                unit.setStrength(100);
                unit.setAttackValue(4);
                unit.setDefenseValue(3);
                break;
            case MARINE:
                unit.setStrength(120);
                unit.setAttackValue(5);
                unit.setDefenseValue(4);
                break;
            case SCIENCE:
                unit.setStrength(60);
                unit.setAttackValue(1);
                unit.setDefenseValue(2);
                break;
            case ENGINEERING:
                unit.setStrength(80);
                unit.setAttackValue(2);
                unit.setDefenseValue(3);
                break;
            case MEDICAL:
                unit.setStrength(50);
                unit.setAttackValue(1);
                unit.setDefenseValue(2);
                break;
            case DIPLOMAT:
                unit.setStrength(40);
                unit.setAttackValue(1);
                unit.setDefenseValue(1);
                break;
        }
        unit.setMaxStrength(unit.getStrength());

        return unitRepository.save(unit);
    }

    /**
     * Resolve a ground battle using the 3R4 engine with Earth 2026 terrain.
     *
     * @param battleId the PlanetaryBattle ID
     * @return the updated PlanetaryBattle with results
     */
    public PlanetaryBattle resolveGroundBattle(Long battleId) {
        log.info("Resolving ground battle: {}", battleId);

        Optional<PlanetaryBattle> battleOpt = battleRepository.findById(battleId);
        if (battleOpt.isEmpty()) {
            throw new IllegalArgumentException("Battle not found: " + battleId);
        }

        PlanetaryBattle battle = battleOpt.get();

        // Get units for both sides
        List<StarTrekGroundUnit> attackerUnits = unitRepository.findByOwnerFactionId(battle.getAttackerFactionId());
        List<StarTrekGroundUnit> defenderUnits = unitRepository.findByOwnerFactionId(battle.getDefenderFactionId());

        if (attackerUnits.isEmpty() || defenderUnits.isEmpty()) {
            log.warn("Cannot resolve battle: missing units for attacker or defender");
            return battle;
        }

        // Convert to maps for the adapter
        List<Map<String, Object>> attackerMaps = new ArrayList<>();
        for (StarTrekGroundUnit unit : attackerUnits) {
            Map<String, Object> map = new HashMap<>();
            map.put("name", unit.getName());
            map.put("type", unit.getUnitType().name());
            map.put("strength", unit.getStrength());
            map.put("morale", unit.getMorale());
            map.put("attack", unit.getAttackValue());
            map.put("defense", unit.getDefenseValue());
            attackerMaps.add(map);
        }

        List<Map<String, Object>> defenderMaps = new ArrayList<>();
        for (StarTrekGroundUnit unit : defenderUnits) {
            Map<String, Object> map = new HashMap<>();
            map.put("name", unit.getName());
            map.put("type", unit.getUnitType().name());
            map.put("strength", unit.getStrength());
            map.put("morale", unit.getMorale());
            map.put("attack", unit.getAttackValue());
            map.put("defense", unit.getDefenseValue());
            defenderMaps.add(map);
        }

        // Resolve battle using 3R4 engine
        Map<String, Object> result = battleService.resolveBattle(
                "Captains_Log",
                attackerMaps,
                defenderMaps,
                TerrainType.PLAINS,
                "Unknown",
                null,
                null
        );

        // Map results to PlanetaryBattle
        battle.setAttackerCasualties((int) result.getOrDefault("attackerCasualties", 0));
        battle.setDefenderCasualties((int) result.getOrDefault("defenderCasualties", 0));
        battle.setRoundsFought((int) result.getOrDefault("roundsFought", 0));
        battle.setNarrativeSummary((String) result.getOrDefault("narrative", ""));

        String outcome = (String) result.getOrDefault("outcome", "DRAW");
        switch (outcome) {
            case "ATTACKER_VICTORY":
                battle.setOutcome(PlanetaryBattle.BattleOutcome.ATTACKER_VICTORY);
                break;
            case "DEFENDER_VICTORY":
                battle.setOutcome(PlanetaryBattle.BattleOutcome.DEFENDER_VICTORY);
                break;
            default:
                battle.setOutcome(PlanetaryBattle.BattleOutcome.DRAW);
        }

        battle.endBattle();
        return battleRepository.save(battle);
    }

    /**
     * Get battle result.
     *
     * @param battleId the PlanetaryBattle ID
     * @return the PlanetaryBattle
     */
    public Optional<PlanetaryBattle> getBattleResult(Long battleId) {
        return battleRepository.findById(battleId);
    }

    /**
     * Get battle environment for a planet location.
     *
     * @param locationId the Earth 2026 location ID
     * @param timePeriod the time period (e.g., "MODERN", "FUTURE")
     * @return the battle environment
     */
    public Earth2026BattleEnvironment getBattleEnvironment(Long locationId, String timePeriod) {
        if (planetService != null) {
            return planetService.buildBattleEnvironment(locationId, timePeriod);
        }
        return null;
    }
}
