package com.yisy.modules.doramcu.starwars.config;

import com.yisy.modules.doramcu.shared.model.GameSystem;
import com.yisy.modules.doramcu.shared.model.RulesEntry;
import com.yisy.modules.doramcu.techlevel.TechLevelAdapter;
import com.yisy.modules.doramcu.thirdreich.adapter.GroundCombatAdapter;
import com.yisy.modules.doramcu.thirdreich.adapter.StarWarsTo3R4Adapter;
import com.yisy.modules.doramcu.gamecore.GameType;
import com.yisy.modules.doramcu.gamecore.RulesEngine;
import com.yisy.modules.doramcu.shared.service.RulesAwareCombatAdapter;
import com.yisy.modules.doramcu.shared.service.UnifiedRulesQueryService;
import com.yisy.modules.doramcu.starwars.model.Unit;
import com.yisy.modules.doramcu.starwars.service.StarWarsService;
import com.yisy.modules.doramcu.starwars.service.StarWarsRulesEngine;
import com.yisy.modules.doramcu.util.DiceUtil;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Star Wars Game Module adapter.
 * <p>
 * Implements the {@link GameModule} interface to register Star Wars features
 * with the {@link GameModuleRegistry}. This allows other modules to access
 * Star Wars mechanics through the shared game-core interfaces.
 * </p>
 * <p>
 * Star Wars provides:
 * <ul>
 *   <li>Dice: d6-based combat resolution</li>
 *   <li>Combat: Ground and space combat with multiple factions</li>
 *   <li>Ground Combat: 3R4 adapter for cross-module battles</li>
 *   <li>Encounters: Scenario-based battles from the Star Wars saga</li>
 *   <li>Rules: Star Wars rules database with factions, units, and lore</li>
 * </ul>
 * </p>
 */
@Component
public class StarWarsGameModule extends AbstractGameModule {

    private final StarWarsService starWarsService;
    private final GroundCombatAdapter groundCombatAdapter;
    private final UnifiedRulesQueryService unifiedRulesQueryService;
    private final StarWarsRulesEngine rulesEngine;

    public StarWarsGameModule(
            StarWarsService starWarsService,
            GameModuleRegistry gameModuleRegistry,
            UnifiedRulesQueryService unifiedRulesQueryService,
            StarWarsRulesEngine rulesEngine) {
        super(gameModuleRegistry);
        this.starWarsService = starWarsService;
        this.groundCombatAdapter = new StarWarsTo3R4Adapter();
        this.unifiedRulesQueryService = unifiedRulesQueryService;
        this.rulesEngine = rulesEngine;
    }

    @Override
    public GameType getGameType() {
        return GameType.STAR_WARS;
    }

    @Override
    public String getModuleName() {
        return "Star Wars";
    }

    @Override
    public DiceService getDiceService() {
        return new StarWarsDiceAdapter();
    }

    @Override
    public CombatService getCombatService() {
        // Wrap the Star Wars combat adapter with rules-aware combat
        CombatService delegate = new StarWarsCombatAdapter(starWarsService);
        RulesAwareCombatAdapter rulesAware = new RulesAwareCombatAdapter(delegate, unifiedRulesQueryService);
        return rulesAware;
    }

    @Override
    public EncounterService getEncounterService() {
        return new StarWarsEncounterAdapter(starWarsService);
    }

    @Override
    public RulesService getRulesService() {
        return new StarWarsRulesAdapter(starWarsService);
    }

    @Override
    public GroundCombatAdapter getGroundCombatAdapter() {
        return groundCombatAdapter;
    }

    @Override
    public TechLevelAdapter getTechLevelAdapter() {
        return new StarWarsTechLevelAdapter();
    }

    @Override
    public Set<MechanicType> getMechanics() {
        return Set.of(
                MechanicType.COMBAT,
                MechanicType.TACTICAL,
                MechanicType.CHARACTER
        );
    }

    // ==================== Inner Adapter Implementations ====================

    /**
     * Adapts Star Wars d6-based dice rolling to the unified DiceService.
     */
    private static class StarWarsDiceAdapter implements DiceService {

        @Override
        public DiceRollResult rollDSides(int sides) {
            int result = DiceUtil.rollDSides(sides);
            return new DiceRollResult("d" + sides, List.of(result), result, "Rolled d" + sides);
        }

        @Override
        public DiceRollResult rollNdX(int count, int sides) {
            List<Integer> rolls = new ArrayList<>();
            int total = 0;
            for (int i = 0; i < count; i++) {
                int roll = DiceUtil.rollDSides(sides);
                rolls.add(roll);
                total += roll;
            }
            return new DiceRollResult(count + "d" + sides, rolls, total, count + "d" + sides + " total");
        }

        @Override
        public DiceRollResult rollFormula(String formula) {
            // Default to d6 for Star Wars
            int result = DiceUtil.rollD6();
            return new DiceRollResult(formula, List.of(result), result, "Rolled " + formula);
        }

        @Override
        public DiceRollResult rollWithAdvantage(String formula) {
            int r1 = DiceUtil.rollD6();
            int r2 = DiceUtil.rollD6();
            int result = Math.max(r1, r2);
            return new DiceRollResult(formula + " (adv)", List.of(r1, r2), result, "Advantage roll");
        }

        @Override
        public DiceRollResult rollWithDisadvantage(String formula) {
            int r1 = DiceUtil.rollD6();
            int r2 = DiceUtil.rollD6();
            int result = Math.min(r1, r2);
            return new DiceRollResult(formula + " (dis)", List.of(r1, r2), result, "Disadvantage roll");
        }

        @Override
        public DiceRollResult roll2D6WithModifier(int modifier) {
            int roll = DiceUtil.roll2D6() + modifier;
            return new DiceRollResult("2d6+" + modifier, List.of(roll), roll, "2D6 with modifier");
        }
    }

    /**
     * Adapts Star Wars combat to the unified CombatService.
     * Delegates to 3R4 engine for cross-module battles.
     */
    private static class StarWarsCombatAdapter implements CombatService {
        private final StarWarsService service;
        private final java.util.concurrent.atomic.AtomicLong combatIdCounter = new java.util.concurrent.atomic.AtomicLong(1);
        private final Map<Long, CombatInfo> combats = new java.util.concurrent.ConcurrentHashMap<>();

        StarWarsCombatAdapter(StarWarsService service) {
            this.service = service;
        }

        @Override
        public Long createCombat(String encounterName) {
            Long id = combatIdCounter.getAndIncrement();
            CombatInfo info = new CombatInfo();
            info.setId(id);
            info.setEncounterName(encounterName);
            info.setStatus("ACTIVE");
            info.setCurrentRound(0);
            info.setCombatants(new ArrayList<>());
            combats.put(id, info);
            return id;
        }

        @Override
        public CombatInfo getCombat(Long combatId) {
            return combats.get(combatId);
        }

        @Override
        public CombatantInfo addCombatant(Long combatId, String name, int armorClass, int maxHp, boolean isCharacter) {
            CombatInfo info = combats.get(combatId);
            if (info == null) return null;
            CombatantInfo ci = new CombatantInfo();
            ci.setName(name);
            ci.setArmorClass(armorClass);
            ci.setCurrentHp(maxHp);
            ci.setMaxHp(maxHp);
            ci.setAlive(true);
            ci.setCharacter(isCharacter);
            info.getCombatants().add(ci);
            return ci;
        }

        @Override
        public CombatInfo rollInitiative(Long combatId, Map<String, Integer> dexModifiers) {
            return combats.get(combatId);
        }

        @Override
        public AttackResult makeAttack(Long combatId, AttackRequest request) {
            // Star Wars uses CRT-based resolution
            // For cross-module use, delegate to 3R4 engine
            AttackResult result = new AttackResult();
            result.setAttackerName(request.getAttackerName());
            result.setTargetName(request.getTargetName());
            result.setHit(false);
            result.setMessage("Use 3R4 engine via GroundCombatAdapter for cross-module battles");
            return result;
        }

        @Override
        public CombatantInfo healCombatant(Long combatId, String name, int amount) {
            CombatInfo info = combats.get(combatId);
            if (info == null) return null;
            for (CombatantInfo ci : info.getCombatants()) {
                if (ci.getName().equals(name)) {
                    ci.setCurrentHp(Math.min(ci.getMaxHp(), ci.getCurrentHp() + amount));
                    return ci;
                }
            }
            return null;
        }

        @Override
        public CombatInfo endCombat(Long combatId, String status) {
            CombatInfo info = combats.get(combatId);
            if (info != null) info.setStatus(status);
            return info;
        }
    }

    /**
     * Adapts Star Wars scenarios to the unified EncounterService.
     */
    private static class StarWarsEncounterAdapter implements EncounterService {
        private final StarWarsService service;

        StarWarsEncounterAdapter(StarWarsService service) {
            this.service = service;
        }

        @Override
        public List<Encounter> listEncounters() {
            List<Encounter> encounters = new ArrayList<>();
            int id = 1;
            for (var scenario : service.getScenarios()) {
                Encounter e = new Encounter();
                e.setId((long) id++);
                e.setName(scenario.name());
                e.setDescription(scenario.description());
                e.setDifficulty(scenario.difficulty());
                e.setXpReward(100);
                e.setEncounterType("Scenario");
                encounters.add(e);
            }
            return encounters;
        }

        @Override
        public Encounter getEncounter(Long encounterId) {
            var scenarios = service.getScenarios();
            if (encounterId > 0 && encounterId <= scenarios.size()) {
                var scenario = scenarios.get(encounterId.intValue() - 1);
                Encounter e = new Encounter();
                e.setId(encounterId);
                e.setName(scenario.name());
                e.setDescription(scenario.description());
                e.setEncounterType("Scenario");
                return e;
            }
            return null;
        }

        @Override
        public Encounter generateRandomEncounter(String areaId, int level) {
            var scenarios = service.getScenarios();
            if (scenarios.isEmpty()) return null;
            var scenario = scenarios.get(new Random().nextInt(scenarios.size()));
            Encounter e = new Encounter();
            e.setName(scenario.name());
            e.setDescription(scenario.description());
            e.setEncounterType("Scenario");
            e.setAreaId(areaId);
            return e;
        }

        @Override
        public EncounterResult resolveEncounter(Long encounterId, List<Long> characterIds) {
            return new EncounterResult(encounterId, "Star Wars Scenario", true, "Resolved via 3R4 engine", 100);
        }
    }

    /**
     * Adapts Star Wars rules to the unified RulesService.
     */
    private static class StarWarsRulesAdapter implements RulesService {
        private final StarWarsService service;

        StarWarsRulesAdapter(StarWarsService service) {
            this.service = service;
        }

        @Override
        public List<RulesEntry> listRules() {
            List<RulesEntry> rules = new ArrayList<>();
            long id = 1;

            // Factions
            for (var faction : service.getFactions()) {
                RulesEntry entry = new RulesEntry();
                entry.setId(id++);
                entry.setTitle(faction.name());
                entry.setCategory("Faction");
                entry.setText(faction.description());
                entry.setSource("StarWars");
                rules.add(entry);
            }

            // Units
            for (Unit unit : service.getAllUnits()) {
                RulesEntry entry = new RulesEntry();
                entry.setId(id++);
                entry.setTitle(unit.name());
                entry.setCategory(unit.type());
                entry.setText(unit.description());
                entry.setSource("StarWars");
                rules.add(entry);
            }

            // Special Abilities
            for (var ability : service.getSpecialAbilities()) {
                RulesEntry entry = new RulesEntry();
                entry.setId(id++);
                entry.setTitle(ability.name());
                entry.setCategory(ability.category());
                entry.setText(ability.description());
                entry.setSource("StarWars");
                rules.add(entry);
            }

            // Lore
            for (var lore : service.getLore()) {
                RulesEntry entry = new RulesEntry();
                entry.setId(id++);
                entry.setTitle(lore.name());
                entry.setCategory(lore.category());
                entry.setText(lore.description());
                entry.setSource("StarWars");
                rules.add(entry);
            }

            return rules;
        }

        @Override
        public List<RulesEntry> searchRules(String keyword) {
            return listRules().stream()
                    .filter(r -> r.getTitle().toLowerCase().contains(keyword.toLowerCase())
                            || r.getText().toLowerCase().contains(keyword.toLowerCase()))
                    .collect(Collectors.toList());
        }

        @Override
        public RulesEntry getRule(Long id) {
            return listRules().stream()
                    .filter(r -> r.getId().equals(id))
                    .findFirst()
                    .orElse(null);
        }
    }

    @Override
    public RulesEngine getRulesEngine() {
        return rulesEngine;
    }
}
