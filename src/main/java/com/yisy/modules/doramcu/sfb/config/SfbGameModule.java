package com.yisy.modules.doramcu.sfb.config;

import com.yisy.modules.doramcu.gamecore.AbstractGameModule;
import com.yisy.modules.doramcu.gamecore.CombatService;
import com.yisy.modules.doramcu.gamecore.DiceService;
import com.yisy.modules.doramcu.gamecore.DiceService.DiceRollResult;
import com.yisy.modules.doramcu.gamecore.EncounterService;
import com.yisy.modules.doramcu.gamecore.GameModuleRegistry;
import com.yisy.modules.doramcu.gamecore.GameType;
import com.yisy.modules.doramcu.gamecore.MechanicType;
import com.yisy.modules.doramcu.gamecore.RulesEngine;
import com.yisy.modules.doramcu.gamecore.RulesService;
import com.yisy.modules.doramcu.shared.model.RulesEntry;
import com.yisy.modules.doramcu.shared.service.RulesAwareCombatAdapter;
import com.yisy.modules.doramcu.shared.service.UnifiedRulesQueryService;
import com.yisy.modules.doramcu.sfb.model.SfbRulesDatabase;
import com.yisy.modules.doramcu.sfb.service.SfbCombatService;
import com.yisy.modules.doramcu.sfb.service.SfbRulesEngine;
import com.yisy.modules.doramcu.util.DiceUtil;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * SFB (Star Frontiers Battle) Game Module adapter.
 * <p>
 * Implements the {@link GameModule} interface to register SFB features
 * with the {@link GameModuleRegistry}. This allows other modules to access
 * SFB mechanics through the shared game-core interfaces.
 * </p>
 * <p>
 * SFB provides:
 * <ul>
 *   <li>Dice: 2D6 roll-under based resolution</li>
 *   <li>Combat: Ship-based fleet combat with energy allocation</li>
 *   <li>Encounters: Space and ground encounters</li>
 *   <li>Rules: SFB rules database</li>
 * </ul>
 * </p>
 */
@Component
public class SfbGameModule extends AbstractGameModule {

    private final SfbCombatService sfbCombatService;
    private final SfbRulesDatabase sfbRulesDatabase;
    private final UnifiedRulesQueryService unifiedRulesQueryService;
    private final SfbRulesEngine rulesEngine;

    public SfbGameModule(
            SfbCombatService sfbCombatService,
            SfbRulesDatabase sfbRulesDatabase,
            GameModuleRegistry gameModuleRegistry,
            UnifiedRulesQueryService unifiedRulesQueryService,
            SfbRulesEngine rulesEngine) {
        super(gameModuleRegistry);
        this.sfbCombatService = sfbCombatService;
        this.sfbRulesDatabase = sfbRulesDatabase;
        this.unifiedRulesQueryService = unifiedRulesQueryService;
        this.rulesEngine = rulesEngine;
    }

    @Override
    public GameType getGameType() {
        return GameType.SFB;
    }

    @Override
    public String getModuleName() {
        return "Star Frontiers Battle";
    }

    @Override
    public DiceService getDiceService() {
        return new SfbDiceAdapter();
    }

    @Override
    public CombatService getCombatService() {
        // Wrap the SFB combat adapter with rules-aware combat
        CombatService delegate = new SfbCombatAdapter(sfbCombatService);
        RulesAwareCombatAdapter rulesAware = new RulesAwareCombatAdapter(delegate, unifiedRulesQueryService);
        return rulesAware;
    }

    @Override
    public EncounterService getEncounterService() {
        return new SfbEncounterAdapter();
    }

    @Override
    public RulesService getRulesService() {
        return new SfbRulesAdapter(sfbRulesDatabase);
    }

    @Override
    public Set<MechanicType> getMechanics() {
        return Set.of(
                MechanicType.COMBAT,
                MechanicType.TACTICAL,
                MechanicType.EXPLORATION
        );
    }

    @Override
    public RulesEngine getRulesEngine() {
        return rulesEngine;
    }

    // ==================== Inner Adapter Implementations ====================

    /**
     * Adapts SFB 2D6-based dice rolling to the unified DiceService.
     */
    private static class SfbDiceAdapter implements DiceService {

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
            // SFB uses 2D6 as primary mechanic
            int result = DiceUtil.roll2D6();
            return new DiceRollResult(formula, List.of(result), result, "Rolled " + formula);
        }

        @Override
        public DiceRollResult rollWithAdvantage(String formula) {
            int r1 = DiceUtil.roll2D6();
            int r2 = DiceUtil.roll2D6();
            int result = Math.min(r1, r2); // Lower is better in SFB
            return new DiceRollResult(formula + " (adv)", List.of(r1, r2), result, "Advantage roll (lower is better)");
        }

        @Override
        public DiceRollResult rollWithDisadvantage(String formula) {
            int r1 = DiceUtil.roll2D6();
            int r2 = DiceUtil.roll2D6();
            int result = Math.max(r1, r2); // Higher is worse in SFB
            return new DiceRollResult(formula + " (dis)", List.of(r1, r2), result, "Disadvantage roll (higher is worse)");
        }

        @Override
        public DiceRollResult roll2D6WithModifier(int modifier) {
            int roll = DiceUtil.roll2D6() + modifier;
            return new DiceRollResult("2d6+" + modifier, List.of(roll), roll, "2D6 with modifier");
        }
    }

    /**
     * Adapts SFB ship combat to the unified CombatService.
     */
    private static class SfbCombatAdapter implements CombatService {
        private final SfbCombatService service;
        private final java.util.concurrent.atomic.AtomicLong combatIdCounter = new java.util.concurrent.atomic.AtomicLong(1);
        private final Map<Long, CombatInfo> combats = new java.util.concurrent.ConcurrentHashMap<>();

        SfbCombatAdapter(SfbCombatService service) {
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
            AttackResult result = new AttackResult();
            result.setAttackerName(request.getAttackerName());
            result.setTargetName(request.getTargetName());

            // SFB combat: 2D6 + attack vs 2D6 + defense
            int attackRoll = DiceUtil.roll2D6() + request.getAttackBonus();
            int defenseRoll = DiceUtil.roll2D6();
            boolean hit = attackRoll >= defenseRoll;

            result.setHit(hit);
            result.setAttackRoll(attackRoll);
            if (hit) {
                int damage = Math.max(1, DiceUtil.roll2D6());
                result.setDamageDealt(damage);
                result.setMessage("Hit! Attack " + attackRoll + " >= defense " + defenseRoll + ". Damage: " + damage + ".");
            } else {
                result.setMessage("Miss. Attack " + attackRoll + " < defense " + defenseRoll + ".");
            }
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
     * Adapts SFB encounters to the unified EncounterService.
     */
    private static class SfbEncounterAdapter implements EncounterService {

        @Override
        public List<Encounter> listEncounters() {
            List<Encounter> encounters = new ArrayList<>();
            Encounter e1 = new Encounter();
            e1.setId(1L);
            e1.setName("Space Encounter");
            e1.setDescription("Random space encounter with ships or debris");
            e1.setDifficulty("Medium");
            e1.setXpReward(100);
            e1.setEncounterType("Space");
            encounters.add(e1);

            Encounter e2 = new Encounter();
            e2.setId(2L);
            e2.setName("Station Encounter");
            e2.setDescription("Encounter at a space station");
            e2.setDifficulty("Easy");
            e2.setXpReward(50);
            e2.setEncounterType("Station");
            encounters.add(e2);

            Encounter e3 = new Encounter();
            e3.setId(3L);
            e3.setName("Ground Encounter");
            e3.setDescription("Planetary surface encounter");
            e3.setDifficulty("Hard");
            e3.setXpReward(150);
            e3.setEncounterType("Ground");
            encounters.add(e3);
            return encounters;
        }

        @Override
        public Encounter getEncounter(Long encounterId) {
            return listEncounters().stream()
                    .filter(e -> e.getId().equals(encounterId))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public Encounter generateRandomEncounter(String areaId, int level) {
            List<Encounter> encounters = listEncounters();
            return encounters.get(new Random().nextInt(encounters.size()));
        }

        @Override
        public EncounterResult resolveEncounter(Long encounterId, List<Long> characterIds) {
            return new EncounterResult(encounterId, "SFB Encounter", true, "Resolved via SFB combat system", 100);
        }
    }

    /**
     * Adapts SFB rules to the unified RulesService.
     */
    private static class SfbRulesAdapter implements RulesService {
        private final SfbRulesDatabase database;

        SfbRulesAdapter(SfbRulesDatabase database) {
            this.database = database;
        }

        @Override
        public List<RulesEntry> listRules() {
            return database.getAllEntries();
        }

        @Override
        public List<RulesEntry> searchRules(String keyword) {
            return database.search(keyword);
        }

        @Override
        public RulesEntry getRule(Long id) {
            return database.getEntryById(id.toString());
        }
    }
}
