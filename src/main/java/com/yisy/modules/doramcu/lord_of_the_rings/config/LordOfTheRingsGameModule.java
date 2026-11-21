package com.yisy.modules.doramcu.lord_of_the_rings.config;

import com.yisy.modules.doramcu.gamecore.AbstractGameModule;
import com.yisy.modules.doramcu.gamecore.CombatService;
import com.yisy.modules.doramcu.gamecore.DiceService;
import com.yisy.modules.doramcu.gamecore.DiceService.DiceRollResult;
import com.yisy.modules.doramcu.gamecore.EncounterService;
import com.yisy.modules.doramcu.gamecore.GameModuleRegistry;
import com.yisy.modules.doramcu.gamecore.GameType;
import com.yisy.modules.doramcu.gamecore.MechanicType;
import com.yisy.modules.doramcu.gamecore.RulesService;
import com.yisy.modules.doramcu.shared.model.RulesEntry;
import com.yisy.modules.doramcu.shared.service.RulesAwareCombatAdapter;
import com.yisy.modules.doramcu.shared.service.UnifiedRulesQueryService;
import com.yisy.modules.doramcu.techlevel.ModuleCategory;
import com.yisy.modules.doramcu.techlevel.TechLevel;
import com.yisy.modules.doramcu.techlevel.TechLevelAdapter;
import com.yisy.modules.doramcu.thirdreich.adapter.GroundCombatAdapter;
import com.yisy.modules.doramcu.lord_of_the_rings.model.Unit;
import com.yisy.modules.doramcu.lord_of_the_rings.service.LordOfTheRingsService;
import com.yisy.modules.doramcu.util.DiceUtil;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Lord of the Rings Game Module adapter.
 * <p>
 * Implements the {@link GameModule} interface to register Lord of the Rings features
 * with the {@link GameModuleRegistry}. This allows other modules to access
 * Lord of the Rings mechanics through the shared game-core interfaces.
 * </p>
 * <p>
 * Lord of the Rings provides:
 * <ul>
 *   <li>Dice: d6-based combat resolution</li>
 *   <li>Combat: Ground and space combat with multiple factions</li>
 *   <li>Encounters: Scenario-based battles from the Lord of the Rings saga</li>
 *   <li>Rules: Lord of the Rings rules database with factions, units, and lore</li>
 * </ul>
 * </p>
 */
@Component
public class LordOfTheRingsGameModule extends AbstractGameModule {

    private final LordOfTheRingsService lotrService;
    private final GroundCombatAdapter groundCombatAdapter;
    private final UnifiedRulesQueryService unifiedRulesQueryService;

    public LordOfTheRingsGameModule(
            LordOfTheRingsService lotrService,
            GameModuleRegistry gameModuleRegistry,
            UnifiedRulesQueryService unifiedRulesQueryService) {
        super(gameModuleRegistry);
        this.lotrService = lotrService;
        this.groundCombatAdapter = new LotRTo3R4Adapter();
        this.unifiedRulesQueryService = unifiedRulesQueryService;
    }

    @Override
    public GameType getGameType() {
        return GameType.LORD_OF_THE_RINGS;
    }

    @Override
    public String getModuleName() {
        return "Lord of the Rings";
    }

    @Override
    public DiceService getDiceService() {
        return new LotRDiceAdapter();
    }

    @Override
    public CombatService getCombatService() {
        CombatService delegate = new LotRCombatAdapter(lotrService);
        RulesAwareCombatAdapter rulesAware = new RulesAwareCombatAdapter(delegate, unifiedRulesQueryService);
        return rulesAware;
    }

    @Override
    public EncounterService getEncounterService() {
        return new LotREncounterAdapter(lotrService);
    }

    @Override
    public RulesService getRulesService() {
        return new LotRRulesAdapter(lotrService);
    }

    @Override
    public GroundCombatAdapter getGroundCombatAdapter() {
        return groundCombatAdapter;
    }

    @Override
    public TechLevelAdapter getTechLevelAdapter() {
        return new LotRTechLevelAdapter();
    }

    @Override
    public Set<MechanicType> getMechanics() {
        return Set.of(
                MechanicType.COMBAT,
                MechanicType.TACTICAL,
                MechanicType.CHARACTER
        );
    }

    /**
     * Adapts Lord of the Rings dice to the unified DiceService.
     */
    private static class LotRDiceAdapter implements DiceService {

        @Override
        public DiceRollResult rollDSides(int sides) {
            int roll = (int) (Math.random() * sides) + 1;
            return new DiceRollResult("d" + sides, List.of(roll), roll, "LOTR d" + sides);
        }

        @Override
        public DiceRollResult rollNdX(int count, int sides) {
            int total = 0;
            List<Integer> rolls = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                int r = (int) (Math.random() * sides) + 1;
                total += r;
                rolls.add(r);
            }
            return new DiceRollResult(count + "d" + sides, rolls, total, "LOTR " + count + "d" + sides);
        }

        @Override
        public DiceRollResult rollFormula(String formula) {
            // Simple formula parser: supports "NdS+M" format
            int count = 1, sides = 6, modifier = 0;
            String f = formula.trim().toLowerCase();
            if (f.startsWith("d")) {
                sides = Integer.parseInt(f.substring(1));
            } else {
                int dIdx = f.indexOf('d');
                if (dIdx > 0) {
                    count = Integer.parseInt(f.substring(0, dIdx));
                    int plusIdx = f.indexOf('+', dIdx);
                    int minusIdx = f.indexOf('-', dIdx);
                    if (plusIdx > dIdx) {
                        sides = Integer.parseInt(f.substring(dIdx + 1, plusIdx));
                        modifier = Integer.parseInt(f.substring(plusIdx + 1));
                    } else if (minusIdx > dIdx) {
                        sides = Integer.parseInt(f.substring(dIdx + 1, minusIdx));
                        modifier = -Integer.parseInt(f.substring(minusIdx + 1));
                    } else {
                        sides = Integer.parseInt(f.substring(dIdx + 1));
                    }
                }
            }
            int total = DiceUtil.rollNdX(count, sides) + modifier;
            List<Integer> rolls = DiceUtil.rollMultiple(count, sides);
            return new DiceRollResult(formula, rolls, total, "LOTR " + formula);
        }

        @Override
        public DiceRollResult rollWithAdvantage(String formula) {
            int r1 = DiceUtil.rollD20();
            int r2 = DiceUtil.rollD20();
            int result = Math.max(r1, r2);
            return new DiceRollResult(formula + " (advantage)", List.of(r1, r2), result, "LOTR Advantage roll");
        }

        @Override
        public DiceRollResult rollWithDisadvantage(String formula) {
            int r1 = DiceUtil.rollD20();
            int r2 = DiceUtil.rollD20();
            int result = Math.min(r1, r2);
            return new DiceRollResult(formula + " (disadvantage)", List.of(r1, r2), result, "LOTR Disadvantage roll");
        }

        @Override
        public DiceRollResult roll2D6WithModifier(int modifier) {
            int roll = DiceUtil.roll2D6() + modifier;
            return new DiceRollResult("2d6+" + modifier, List.of(roll), roll, "2D6 with modifier");
        }
    }

    /**
     * Adapts Lord of the Rings combat to the unified CombatService.
     */
    private static class LotRCombatAdapter implements CombatService {
        private final LordOfTheRingsService service;
        private final java.util.concurrent.atomic.AtomicLong combatIdCounter = new java.util.concurrent.atomic.AtomicLong(1);
        private final Map<Long, CombatInfo> combats = new java.util.concurrent.ConcurrentHashMap<>();

        LotRCombatAdapter(LordOfTheRingsService service) {
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
            // LOTR uses CRT-based resolution
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
     * Adapts Lord of the Rings encounters to the unified EncounterService.
     */
    private static class LotREncounterAdapter implements EncounterService {
        private final LordOfTheRingsService service;

        LotREncounterAdapter(LordOfTheRingsService service) {
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
            return new EncounterResult(encounterId, "Lord of the Rings Scenario", true, "Resolved via LOTR engine", 100);
        }
    }

    /**
     * Adapts Lord of the Rings rules to the unified RulesService.
     */
    private static class LotRRulesAdapter implements RulesService {
        private final LordOfTheRingsService service;

        LotRRulesAdapter(LordOfTheRingsService service) {
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
                entry.setSource("LordOfTheRings");
                rules.add(entry);
            }

            // Units
            for (Unit unit : service.getAllUnits()) {
                RulesEntry entry = new RulesEntry();
                entry.setId(id++);
                entry.setTitle(unit.name());
                entry.setCategory(unit.type());
                entry.setText(unit.description());
                entry.setSource("LordOfTheRings");
                rules.add(entry);
            }

            // Special Abilities
            for (var ability : service.getSpecialAbilities()) {
                RulesEntry entry = new RulesEntry();
                entry.setId(id++);
                entry.setTitle(ability.name());
                entry.setCategory(ability.category());
                entry.setText(ability.description());
                entry.setSource("LordOfTheRings");
                rules.add(entry);
            }

            // Lore
            for (var lore : service.getLore()) {
                RulesEntry entry = new RulesEntry();
                entry.setId(id++);
                entry.setTitle(lore.name());
                entry.setCategory(lore.category());
                entry.setText(lore.description());
                entry.setSource("LordOfTheRings");
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

    /**
     * Simple adapter for Lord of the Rings ground combat to 3R4 format.
     */
    private static class LotRTo3R4Adapter implements GroundCombatAdapter {
        @Override
        public java.util.List<com.yisy.modules.doramcu.thirdreich.domain.ThirdReichGroundUnit> to3R4Units(java.util.List<?> gameUnits) {
            return java.util.Collections.emptyList();
        }

        @Override
        public com.yisy.modules.doramcu.thirdreich.domain.ThirdReichGroundUnit to3R4Unit(Object gameUnit) {
            return null;
        }

        @Override
        public java.util.Map<String, Object> mapResult(com.yisy.modules.doramcu.thirdreich.domain.ThirdReichBattleResult result) {
            return java.util.Collections.emptyMap();
        }

        @Override
        public com.yisy.modules.doramcu.thirdreich.commander.AICommander toCommander(Object gameCharacter) {
            return null;
        }

        @Override
        public String getSourceGame() {
            return "Lord of the Rings";
        }
    }

    /**
     * Tech level adapter for Lord of the Rings.
     */
    private static class LotRTechLevelAdapter implements TechLevelAdapter {
        @Override
        public String getLoreName(TechLevel techLevel, ModuleCategory category, String genericType) {
            return genericType;
        }

        @Override
        public java.util.Map<String, Object> getLoreStats(TechLevel techLevel, ModuleCategory category, String genericType) {
            return java.util.Collections.emptyMap();
        }

        @Override
        public TechLevel getMaxTechLevel() {
            return TechLevel.TL5;
        }

        @Override
        public TechLevel getMinTechLevel() {
            return TechLevel.TL0;
        }

        @Override
        public java.util.List<String> getAvailableModules(TechLevel techLevel) {
            return java.util.Collections.emptyList();
        }
    }
}
