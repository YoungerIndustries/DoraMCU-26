package com.yisy.modules.doramcu.babylon5.config;

import com.yisy.modules.doramcu.babylon5.service.*;
import com.yisy.modules.doramcu.gamecore.*;
import com.yisy.modules.doramcu.shared.model.RulesEntry;
import com.yisy.modules.doramcu.shared.service.RulesAwareCombatAdapter;
import com.yisy.modules.doramcu.shared.service.UnifiedRulesQueryService;
import com.yisy.modules.doramcu.techlevel.TechLevelAdapter;
import com.yisy.modules.doramcu.gamecore.GameType;
import com.yisy.modules.doramcu.gamecore.RulesEngine;
import com.yisy.modules.doramcu.util.DiceUtil;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Babylon 5 Game Module adapter.
 * <p>
 * Implements the {@link GameModule} interface to register Babylon 5 features
 * with the {@link GameModuleRegistry}. This allows Earth2026 and other modules
 * to access Babylon 5 features through the shared game-core interfaces.
 * </p>
 * <p>
 * Babylon 5 provides:
 * <ul>
 *   <li>Ship-to-Ship Combat: Fleet warfare with B5-specific ship types</li>
 *   <li>Psionics: Psi corps powers and disciplines</li>
 *   <li>Diplomacy: Inter-species relations and politics</li>
 *   <li>Exploration: Unknown regions and first contact</li>
 *   <li>Character Creation: B5-specific characteristics and careers</li>
 * </ul>
 * </p>
 */
@Component
public class Babylon5GameModule extends AbstractGameModule {

    private final B5CharacterService characterService;
    private final B5CombatService combatService;
    private final B5DiplomacyService diplomacyService;
    private final B5EconomyService economyService;
    private final B5ExplorationService explorationService;
    private final B5ShipDesignService shipDesignService;
    private final Babylon5DatabaseService databaseService;
    private final B5RulesDatabase rulesDatabase;
    private final B5TechLevelAdapter techLevelAdapter;
    private final UnifiedRulesQueryService unifiedRulesQueryService;
    private final B5RulesEngine rulesEngine;

    public Babylon5GameModule(
            B5CharacterService characterService,
            B5CombatService combatService,
            B5DiplomacyService diplomacyService,
            B5EconomyService economyService,
            B5ExplorationService explorationService,
            B5ShipDesignService shipDesignService,
            Babylon5DatabaseService databaseService,
            B5RulesDatabase rulesDatabase,
            GameModuleRegistry gameModuleRegistry,
            B5TechLevelAdapter techLevelAdapter,
            UnifiedRulesQueryService unifiedRulesQueryService,
            B5RulesEngine rulesEngine) {
        super(gameModuleRegistry);
        this.characterService = characterService;
        this.combatService = combatService;
        this.diplomacyService = diplomacyService;
        this.economyService = economyService;
        this.explorationService = explorationService;
        this.shipDesignService = shipDesignService;
        this.databaseService = databaseService;
        this.rulesDatabase = rulesDatabase;
        this.techLevelAdapter = techLevelAdapter;
        this.unifiedRulesQueryService = unifiedRulesQueryService;
        this.rulesEngine = rulesEngine;
    }

    @Override
    public GameType getGameType() {
        return GameType.BABYLON_5;
    }

    @Override
    public String getModuleName() {
        return "Babylon 5 RPG";
    }

    @Override
    public DiceService getDiceService() {
        return new B5DiceAdapter();
    }

    @Override
    public CombatService getCombatService() {
        // Wrap the B5 combat adapter with rules-aware combat
        CombatService delegate = new B5CombatAdapter(combatService);
        RulesAwareCombatAdapter rulesAware = new RulesAwareCombatAdapter(delegate, unifiedRulesQueryService);
        return rulesAware;
    }

    @Override
    public InventoryService getInventoryService() {
        return new B5InventoryAdapter(economyService);
    }

    @Override
    public CraftingService getCraftingService() {
        return new B5CraftingAdapter(shipDesignService);
    }

    @Override
    public DungeonRoomService getDungeonRoomService() {
        return new B5DungeonRoomAdapter();
    }

    @Override
    public SpellService getSpellService() {
        return new B5SpellAdapter();
    }

    @Override
    public PsionicsService getPsionicsService() {
        return new B5PsionicsAdapter();
    }

    @Override
    public EncounterService getEncounterService() {
        return new B5EncounterAdapter(explorationService);
    }

    @Override
    public CharacterService getCharacterService() {
        return new B5CharacterAdapter(characterService);
    }

    @Override
    public CampaignService getCampaignService() {
        return new B5CampaignAdapter(diplomacyService, explorationService);
    }

    @Override
    public RulesService getRulesService() {
        return new B5RulesAdapter(rulesDatabase);
    }

    @Override
    public TechLevelAdapter getTechLevelAdapter() {
        return techLevelAdapter;
    }

    @Override
    public Set<MechanicType> getMechanics() {
        return Set.of(
                MechanicType.DIPLOMACY,
                MechanicType.COMBAT,
                MechanicType.EXPLORATION,
                MechanicType.ENCOUNTER,
                MechanicType.CAMPAIGN,
                MechanicType.CHARACTER,
                MechanicType.ECONOMY,
                MechanicType.SUPERNATURAL
        );
    }

    // ==================== Adapter Implementations ====================

    /**
     * Adapts Babylon 5 dice system to DiceService interface.
     * B5 uses 2d6 with skill levels (E:2, T:4, A:6, S:8, E+:10, T+:12, A+:14, S+:16).
     */
    private static class B5DiceAdapter implements DiceService {

        @Override
        public DiceRollResult rollDSides(int sides) {
            int result = DiceUtil.rollDSides(sides);
            return new DiceRollResult("d" + sides, List.of(result), result,
                    "Rolled d" + sides + ": " + result);
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
            return new DiceRollResult(count + "d" + sides, rolls, total,
                    "Rolled " + count + "d" + sides + ": " + total);
        }

        @Override
        public DiceRollResult rollFormula(String formula) {
            // B5 uses 2d6 + skill modifier
            int roll = DiceUtil.roll2D6();
            return new DiceRollResult("2d6", List.of(roll), roll,
                    "Skill check: " + roll);
        }

        @Override
        public DiceRollResult rollWithAdvantage(String formula) {
            int roll1 = DiceUtil.roll2D6();
            int roll2 = DiceUtil.roll2D6();
            int total = Math.max(roll1, roll2);
            return new DiceRollResult(formula + " (advantage)", List.of(roll1, roll2, total), total,
                    "Advantage roll: " + roll1 + ", " + roll2 + " → " + total);
        }

        @Override
        public DiceRollResult rollWithDisadvantage(String formula) {
            int roll1 = DiceUtil.roll2D6();
            int roll2 = DiceUtil.roll2D6();
            int total = Math.min(roll1, roll2);
            return new DiceRollResult(formula + " (disadvantage)", List.of(roll1, roll2, total), total,
                    "Disadvantage roll: " + roll1 + ", " + roll2 + " → " + total);
        }

        @Override
        public DiceRollResult roll2D6WithModifier(int modifier) {
            int roll = DiceUtil.roll2D6();
            int total = roll + modifier;
            String modStr = modifier >= 0 ? "+" + modifier : String.valueOf(modifier);
            return new DiceRollResult("2D6" + modStr, List.of(roll, total), total,
                    "Rolled 2D6" + modStr + ": " + roll + " → " + total);
        }
    }

    /**
     * Adapts Babylon 5 ship-to-ship combat to CombatService interface.
     */
    private static class B5CombatAdapter implements CombatService {
        private final B5CombatService combatService;
        private final Map<Long, CombatState> combats = new HashMap<>();
        private final AtomicLong combatIdCounter = new AtomicLong(1);

        B5CombatAdapter(B5CombatService combatService) {
            this.combatService = combatService;
        }

        @Override
        public Long createCombat(String encounterName) {
            Long combatId = combatIdCounter.getAndIncrement();
            CombatState state = new CombatState();
            state.id = combatId;
            state.encounterName = encounterName;
            state.status = "ACTIVE";
            state.currentRound = 1;
            state.combatants = new ArrayList<>();
            combats.put(combatId, state);
            return combatId;
        }

        @Override
        public CombatInfo getCombat(Long combatId) {
            CombatState state = combats.get(combatId);
            if (state == null) return null;
            CombatInfo info = new CombatInfo();
            info.setId(state.id);
            info.setEncounterName(state.encounterName);
            info.setStatus(state.status);
            info.setCurrentRound(state.currentRound);
            return info;
        }

        @Override
        public CombatantInfo addCombatant(Long combatId, String name, int armorClass, int maxHp, boolean isCharacter) {
            CombatState state = combats.get(combatId);
            if (state == null) return null;
            Combatant combatant = new Combatant();
            combatant.name = name;
            combatant.armorClass = armorClass;
            combatant.maxHp = maxHp;
            combatant.currentHp = maxHp;
            combatant.isCharacter = isCharacter;
            combatant.alive = true;
            state.combatants.add(combatant);

            CombatantInfo info = new CombatantInfo();
            info.setName(name);
            info.setArmorClass(armorClass);
            info.setCurrentHp(maxHp);
            info.setMaxHp(maxHp);
            info.setCharacter(isCharacter);
            info.setAlive(true);
            return info;
        }

        @Override
        public CombatInfo rollInitiative(Long combatId, Map<String, Integer> dexModifiers) {
            CombatState state = combats.get(combatId);
            if (state == null) return null;
            state.combatants.sort((a, b) -> {
                int initA = DiceUtil.roll2D6() + (dexModifiers.getOrDefault(a.name, 0));
                int initB = DiceUtil.roll2D6() + (dexModifiers.getOrDefault(b.name, 0));
                return Integer.compare(initB, initA);
            });
            return getCombat(combatId);
        }

        @Override
        public AttackResult makeAttack(Long combatId, AttackRequest request) {
            CombatState state = combats.get(combatId);
            if (state == null) return null;

            // B5 combat: 2d6 + attack bonus vs defense
            int attackRoll = DiceUtil.roll2D6();
            int attackTotal = attackRoll + request.getAttackBonus();
            int targetAc = 10;
            for (Combatant c : state.combatants) {
                if (c.name.equals(request.getTargetName())) {
                    targetAc = c.armorClass;
                    break;
                }
            }

            boolean hit = attackTotal >= targetAc;
            boolean critical = attackRoll == 12;
            boolean fumble = attackRoll == 2;

            AttackResult result = new AttackResult();
            result.setAttackerName(request.getAttackerName());
            result.setTargetName(request.getTargetName());
            result.setAttackRoll(attackRoll);
            result.setAttackTotal(attackTotal);
            result.setHit(hit);
            result.setCriticalHit(critical);
            result.setCriticalFail(fumble);

            if (hit) {
                int damage = DiceUtil.rollDSides(6) + DiceUtil.rollDSides(6);
                if (critical) damage *= 2;
                result.setDamageDealt(damage);
                result.setDamageType(request.getDamageType() != null ? request.getDamageType() : "energy");
                result.setMessage(request.getAttackerName() + " hits " + request.getTargetName() + " for " + damage + " damage!");

                for (Combatant c : state.combatants) {
                    if (c.name.equals(request.getTargetName())) {
                        c.currentHp = Math.max(0, c.currentHp - damage);
                        c.alive = c.currentHp > 0;
                        break;
                    }
                }
            } else {
                result.setDamageDealt(0);
                result.setMessage(request.getAttackerName() + " misses " + request.getTargetName() + ".");
            }
            return result;
        }

        @Override
        public CombatantInfo healCombatant(Long combatId, String name, int amount) {
            CombatState state = combats.get(combatId);
            if (state == null) return null;
            for (Combatant c : state.combatants) {
                if (c.name.equals(name)) {
                    c.currentHp = Math.min(c.maxHp, c.currentHp + amount);
                    c.alive = c.currentHp > 0;
                    CombatantInfo info = new CombatantInfo();
                    info.setName(c.name);
                    info.setCurrentHp(c.currentHp);
                    info.setMaxHp(c.maxHp);
                    info.setAlive(c.alive);
                    return info;
                }
            }
            return null;
        }

        @Override
        public CombatInfo endCombat(Long combatId, String status) {
            CombatState state = combats.get(combatId);
            if (state == null) return null;
            state.status = status;
            return getCombat(combatId);
        }

        private static class Combatant {
            String name;
            int armorClass;
            int maxHp;
            int currentHp;
            boolean isCharacter;
            boolean alive;
        }

        private static class CombatState {
            Long id;
            String encounterName;
            String status;
            int currentRound;
            List<Combatant> combatants;
        }
    }

    /**
     * Adapts Babylon 5 economy to InventoryService interface.
     */
    private static class B5InventoryAdapter implements InventoryService {
        private final B5EconomyService economyService;

        B5InventoryAdapter(B5EconomyService economyService) {
            this.economyService = economyService;
        }

        @Override
        public List<InventoryItem> getInventory(Long characterId) {
            // B5 uses wealth-based inventory
            List<InventoryItem> items = new ArrayList<>();
            InventoryItem credits = new InventoryItem();
            credits.setName("Credits");
            credits.setDescription("Standard interstellar currency");
            credits.setItemType("currency");
            credits.setQuantity(1000);
            credits.setWeight(0);
            items.add(credits);
            return items;
        }

        @Override
        public InventoryItem addItem(Long characterId, InventoryItem item) {
            return item;
        }

        @Override
        public boolean removeItem(Long itemId) {
            return false;
        }

        @Override
        public String useItem(Long characterId, Long itemId) {
            return "Item used by B5 character";
        }
    }

    /**
     * Adapts Babylon 5 ship design to CraftingService interface.
     */
    private static class B5CraftingAdapter implements CraftingService {
        private final B5ShipDesignService shipDesignService;
        private final Map<Long, CraftingProject> projects = new HashMap<>();
        private final AtomicLong projectIdCounter = new AtomicLong(1);

        B5CraftingAdapter(B5ShipDesignService shipDesignService) {
            this.shipDesignService = shipDesignService;
        }

        @Override
        public List<CraftingRecipe> listRecipes() {
            List<CraftingRecipe> recipes = new ArrayList<>();

            CraftingRecipe nightstar = new CraftingRecipe();
            nightstar.setId(1L);
            nightstar.setName("Nightstar Construction");
            nightstar.setDescription("Build a Centauri Nightstar fighter");
            nightstar.setCategory("Ship Design");
            nightstar.setDifficulty(14);
            nightstar.setTimeRequired(720);
            nightstar.setMaterialsRequired("Hull plating, weapons systems, engines");
            nightstar.setResultItem("Nightstar fighter");
            recipes.add(nightstar);

            CraftingRecipe weapons = new CraftingRecipe();
            weapons.setId(2L);
            weapons.setName("Weapon Calibration");
            weapons.setDescription("Calibrate ship weapons for optimal output");
            weapons.setCategory("Engineering");
            weapons.setDifficulty(10);
            weapons.setTimeRequired(120);
            weapons.setMaterialsRequired("Weapon components, energy cells");
            weapons.setResultItem("Calibrated weapons");
            recipes.add(weapons);

            CraftingRecipe medical = new CraftingRecipe();
            medical.setId(3L);
            medical.setName("Medical Supplies");
            medical.setDescription("Create medical supplies for ship sickbay");
            medical.setCategory("Medical");
            medical.setDifficulty(8);
            medical.setTimeRequired(60);
            medical.setMaterialsRequired("Medical chemicals, bandages");
            medical.setResultItem("Medical supply kit");
            recipes.add(medical);

            return recipes;
        }

        @Override
        public CraftingRecipe getRecipe(Long recipeId) {
            return listRecipes().stream()
                    .filter(r -> r.getId().equals(recipeId))
                    .findFirst().orElse(null);
        }

        @Override
        public CraftingProject startProject(Long characterId, Long recipeId) {
            Long projectId = projectIdCounter.getAndIncrement();
            CraftingProject project = new CraftingProject();
            project.setId(projectId);
            project.setCharacterId(characterId);
            project.setRecipeId(recipeId);
            project.setStatus("STARTED");
            project.setProgress(0);
            project.setTotalProgress(100);
            projects.put(projectId, project);
            return project;
        }

        @Override
        public CraftingProject progressProject(Long projectId) {
            CraftingProject project = projects.get(projectId);
            if (project == null) return null;
            int progress = DiceUtil.roll2D6() * 5;
            project.setProgress(Math.min(100, project.getProgress() + progress));
            if (project.getProgress() >= 100) {
                project.setStatus("COMPLETED");
            } else {
                project.setStatus("IN_PROGRESS");
            }
            return project;
        }

        @Override
        public CraftingProject completeProject(Long projectId) {
            CraftingProject project = projects.get(projectId);
            if (project == null) return null;
            project.setStatus("COMPLETED");
            project.setProgress(100);
            return project;
        }
    }

    /**
     * Adapts Babylon 5 station rooms to DungeonRoomService interface.
     * Babylon 5 station has decks and rooms that can be explored.
     */
    private static class B5DungeonRoomAdapter implements DungeonRoomService {
        private final Map<Long, DungeonRoom> rooms = new HashMap<>();
        private final AtomicLong roomIdCounter = new AtomicLong(1);

        @Override
        public DungeonRoom createRoom(DungeonRoom room) {
            Long id = roomIdCounter.getAndIncrement();
            room.setId(id);
            rooms.put(id, room);
            return room;
        }

        @Override
        public DungeonRoom getRoom(Long id) {
            return rooms.get(id);
        }

        @Override
        public List<DungeonRoom> listRoomsByDungeon(String dungeonId) {
            return rooms.values().stream()
                    .filter(r -> dungeonId.equals(r.getDungeonId()))
                    .collect(Collectors.toList());
        }

        @Override
        public List<DungeonRoom> listRoomsByTheme(String theme) {
            return rooms.values().stream()
                    .filter(r -> theme.equals(r.getTheme()))
                    .collect(Collectors.toList());
        }

        @Override
        public DungeonRoom discoverRoom(Long id) {
            DungeonRoom room = rooms.get(id);
            if (room != null) room.setDiscovered(true);
            return room;
        }

        @Override
        public void addConnection(Long fromId, Long toId) {
            // Connections tracked in room data
        }

        @Override
        public List<DungeonRoom> getConnectedRooms(Long roomId) {
            return List.of();
        }

        @Override
        public void createCircularConnections(String dungeonId) {
            List<DungeonRoom> dungeonRooms = listRoomsByDungeon(dungeonId);
            for (int i = 0; i < dungeonRooms.size(); i++) {
                DungeonRoom current = dungeonRooms.get(i);
                DungeonRoom next = dungeonRooms.get((i + 1) % dungeonRooms.size());
                addConnection(current.getId(), next.getId());
            }
        }

        @Override
        public TrapCheckResult makeTrapCheck(Long roomId, int dexModifier, int proficiencyBonus, int roll) {
            int dc = 15;
            int total = roll + dexModifier + proficiencyBonus;
            boolean success = total >= dc;
            boolean criticalFail = roll == 1;
            return new TrapCheckResult(roomId, "Station Room", dc, success, criticalFail,
                    criticalFail ? 2 : 1, "energy", "1d6");
        }

        @Override
        public TreasureResult discoverTreasure(Long roomId) {
            return new TreasureResult(roomId, "Station Room", "Alien artifact with unknown technology",
                    0, "Rare");
        }
    }

    /**
     * Babylon 5 has no traditional spells — psionics replace magic.
     */
    private static class B5SpellAdapter implements SpellService {

        @Override
        public List<Spell> listSpells() {
            // B5 uses psionics, not spells
            return List.of();
        }

        @Override
        public Spell getSpell(Long spellId) {
            return null;
        }

        @Override
        public List<Spell> listSpellsByLevel(int level) {
            return List.of();
        }

        @Override
        public SpellCastResult castSpell(Long characterId, Long spellId, Long targetId) {
            return new SpellCastResult(spellId, "Unknown", false,
                    "Babylon 5 has no spells — use psionic powers", 0);
        }
    }

    /**
     * Babylon 5 has extensive psionics (Psi Corps).
     */
    private static class B5PsionicsAdapter implements PsionicsService {
        private final Map<Long, PsionicPower> psionicPowers = new HashMap<>();

        B5PsionicsAdapter() {
            initializePsionicPowers();
        }

        private void initializePsionicPowers() {
            // Telepathy powers
            addPsionicPower(1L, "Mind Touch", "Telepathy", 1, 1, "Touch", "Concentration",
                    "Read surface thoughts of a target.");
            addPsionicPower(2L, "Mind Probe", "Telepathy", 2, 2, "30 feet", "Concentration",
                    "Search the mind of a target for specific information.");
            addPsionicPower(3L, "Mind Wipe", "Telepathy", 4, 4, "Touch", "Instant",
                    "Erase specific memories from a target's mind.");
            addPsionicPower(4L, "Telepathic Shield", "Telepathy", 2, 2, "Self", "1 hour",
                    "Protect your mind from telepathic intrusion.");

            // Telekinesis powers
            addPsionicPower(5L, "Telekinetic Push", "Telekinesis", 1, 1, "30 feet", "Instant",
                    "Push an object or creature with your mind.");
            addPsionicPower(6L, "Telekinetic Barrier", "Telekinesis", 3, 3, "60 feet", "Concentration",
                    "Create a barrier of telekinetic force.");
            addPsionicPower(7L, "Telekinetic Crush", "Telekinesis", 4, 4, "60 feet", "Concentration",
                    "Crush an object or creature with telekinetic force.");

            // Precognition powers
            addPsionicPower(8L, "Precognition", "Precognition", 2, 2, "Self", "Instant",
                    "Gain insight into possible future events.");
            addPsionicPower(9L, "Danger Sense", "Precognition", 1, 1, "Self", "1 hour",
                    "Sense impending danger.");
            addPsionicPower(10L, "Dream Walking", "Precognition", 3, 3, "Unlimited", "Concentration",
                    "Enter and navigate the dreams of others.");

            // Empathy powers
            addPsionicPower(11L, "Empathy", "Empathy", 1, 1, "30 feet", "Concentration",
                    "Sense the emotional state of nearby creatures.");
            addPsionicPower(12L, "Emotional Influence", "Empathy", 3, 3, "60 feet", "Concentration",
                    "Influence the emotions of a target creature.");
        }

        private void addPsionicPower(Long id, String name, String discipline, int level, int cost,
                                      String range, String duration, String description) {
            PsionicPower power = new PsionicPower();
            power.setId(id);
            power.setName(name);
            power.setDiscipline(discipline);
            power.setPowerLevel(level);
            power.setPowerPointsCost(cost);
            power.setRange(range);
            power.setDuration(duration);
            power.setDescription(description);
            psionicPowers.put(id, power);
        }

        @Override
        public List<PsionicPower> listPowers() {
            return new ArrayList<>(psionicPowers.values());
        }

        @Override
        public PsionicPower getPower(Long powerId) {
            return psionicPowers.get(powerId);
        }

        @Override
        public PsionicPowerResult usePower(Long characterId, Long powerId, Long targetId) {
            PsionicPower power = psionicPowers.get(powerId);
            if (power == null) {
                return new PsionicPowerResult(powerId, "Unknown", false, "Power not found", 0);
            }
            int roll = DiceUtil.roll2D6();
            boolean success = roll >= power.getPowerLevel() * 2;
            return new PsionicPowerResult(powerId, power.getName(), success,
                    success ? power.getName() + " used successfully!" : "Power failed.",
                    power.getPowerPointsCost());
        }
    }

    /**
     * Adapts Babylon 5 exploration to EncounterService interface.
     */
    private static class B5EncounterAdapter implements EncounterService {
        private final B5ExplorationService explorationService;

        B5EncounterAdapter(B5ExplorationService explorationService) {
            this.explorationService = explorationService;
        }

        @Override
        public List<Encounter> listEncounters() {
            List<Encounter> encounters = new ArrayList<>();

            Encounter shadow = new Encounter();
            shadow.setId(1L);
            shadow.setName("Shadow Encounter");
            shadow.setDescription("The ancient Shadow race reveals themselves");
            shadow.setDifficulty("Deadly");
            encounters.add(shadow);

            Encounter minbari = new Encounter();
            minbari.setId(2L);
            minbari.setName("Minbari Patrol");
            minbari.setDescription("Encounter with Minbari warships");
            minbari.setDifficulty("Hard");
            encounters.add(minbari);

            Encounter centauri = new Encounter();
            centauri.setId(3L);
            centauri.setName("Centauri Diplomacy");
            centauri.setDescription("Navigate the complex politics of the Centauri Republic");
            centauri.setDifficulty("Medium");
            encounters.add(centauri);

            Encounter vorlon = new Encounter();
            vorlon.setId(4L);
            vorlon.setName("Vorlon Mystery");
            vorlon.setDescription("The enigmatic Vorlon race makes contact");
            vorlon.setDifficulty("Hard");
            encounters.add(vorlon);

            Encounter marauder = new Encounter();
            marauder.setId(5L);
            marauder.setName("Marauder Attack");
            marauder.setDescription("Defend against space marauders");
            marauder.setDifficulty("Medium");
            encounters.add(marauder);

            return encounters;
        }

        @Override
        public Encounter getEncounter(Long encounterId) {
            return listEncounters().stream()
                    .filter(e -> e.getId().equals(encounterId))
                    .findFirst().orElse(null);
        }

        @Override
        public Encounter generateRandomEncounter(String areaId, int level) {
            List<Encounter> encounters = listEncounters();
            int index = DiceUtil.rollIndex(encounters.size());
            Encounter enc = encounters.get(index);
            enc.setAreaId(areaId);
            return enc;
        }

        @Override
        public EncounterResult resolveEncounter(Long encounterId, List<Long> characterIds) {
            Encounter enc = getEncounter(encounterId);
            if (enc == null) {
                return new EncounterResult(encounterId, "Unknown", false, "Encounter not found", 0);
            }
            int roll = DiceUtil.roll2D6();
            boolean success = roll + characterIds.size() >= 8;
            return new EncounterResult(encounterId, enc.getName(), success,
                    success ? "Encounter resolved successfully" : "Encounter failed", 0);
        }
    }

    /**
     * Adapts Babylon 5 characters to CharacterService interface.
     */
    private static class B5CharacterAdapter implements CharacterService {
        private final B5CharacterService characterService;

        B5CharacterAdapter(B5CharacterService characterService) {
            this.characterService = characterService;
        }

        @Override
        public CharacterInfo getCharacter(Long characterId) {
            // B5 character service may use different ID types
            return null;
        }

        @Override
        public List<CharacterInfo> listCharacters() {
            // B5 characters are managed separately
            return List.of();
        }

        @Override
        public void updateAbilityScores(Long characterId, int str, int dex, int con, int intelligence, int wis, int cha) {
            // B5 uses different ability system
        }

        @Override
        public CharacterInfo awardExperience(Long characterId, int xp) {
            return null;
        }
    }

    /**
     * Adapts Babylon 5 diplomacy to CampaignService interface.
     */
    private static class B5CampaignAdapter implements CampaignService {
        private final B5DiplomacyService diplomacyService;
        private final B5ExplorationService explorationService;
        private final Map<Long, Campaign> campaigns = new HashMap<>();
        private final AtomicLong campaignIdCounter = new AtomicLong(1);

        B5CampaignAdapter(B5DiplomacyService diplomacyService, B5ExplorationService explorationService) {
            this.diplomacyService = diplomacyService;
            this.explorationService = explorationService;
        }

        @Override
        public List<Campaign> listCampaigns() {
            if (campaigns.isEmpty()) {
                createCampaign("The Shadow War", "The ancient Shadow war threatens the galaxy");
                createCampaign("Earth-Minbari War", "Aftermath of the Earth-Minbari conflict");
                createCampaign("Vorlon Mystery", "Uncover the secrets of the Vorlon race");
            }
            return new ArrayList<>(campaigns.values());
        }

        @Override
        public Campaign getCampaign(Long campaignId) {
            return campaigns.get(campaignId);
        }

        @Override
        public Campaign createCampaign(String name, String description) {
            Long id = campaignIdCounter.getAndIncrement();
            Campaign campaign = new Campaign();
            campaign.setId(id);
            campaign.setName(name);
            campaign.setDescription(description);
            campaigns.put(id, campaign);
            return campaign;
        }

        @Override
        public void addCharacterToCampaign(Long campaignId, Long characterId) {
            // Campaign character tracking is handled by the campaign service
        }
    }

    /**
     * Adapts Babylon 5 rules database to RulesService interface.
     */
    private static class B5RulesAdapter implements RulesService {
        private final B5RulesDatabase rulesDatabase;

        B5RulesAdapter(B5RulesDatabase rulesDatabase) {
            this.rulesDatabase = rulesDatabase;
        }

        @Override
        public List<RulesEntry> listRules() {
            List<RulesEntry> rules = new ArrayList<>();

            RulesEntry combat = new RulesEntry();
            combat.setId(1L);
            combat.setTitle("Combat Rules");
            combat.setCategory("Combat");
            combat.setText("B5 combat uses 2d6 + skill modifier. Skill levels: E(2), T(4), A(6), S(8), E+(10), T+(12), A+(14), S+(16).");
            combat.setSource("Babylon 5 Core Rules");
            rules.add(combat);

            RulesEntry psionics = new RulesEntry();
            psionics.setId(2L);
            psionics.setTitle("Psionics Rules");
            psionics.setCategory("Psionics");
            psionics.setText("Psionic powers use power points. Disciplines include Telepathy, Telekinesis, Precognition, and Empathy.");
            psionics.setSource("Babylon 5 Core Rules");
            rules.add(psionics);

            RulesEntry shipCombat = new RulesEntry();
            shipCombat.setId(3L);
            shipCombat.setTitle("Ship Combat Rules");
            shipCombat.setCategory("Ship Combat");
            shipCombat.setText("Ship-to-ship combat uses hex-based movement and weapon arcs. Ship types include Nightstar, Crusader, and Battle Cruiser.");
            shipCombat.setSource("Babylon 5 Core Rules");
            rules.add(shipCombat);

            RulesEntry diplomacy = new RulesEntry();
            diplomacy.setId(4L);
            diplomacy.setTitle("Diplomacy Rules");
            diplomacy.setCategory("Diplomacy");
            diplomacy.setText("Inter-species diplomacy involves the Earth Alliance, Minbari, Centauri, Narn, and Vorlon races.");
            diplomacy.setSource("Babylon 5 Core Rules");
            rules.add(diplomacy);

            RulesEntry characters = new RulesEntry();
            characters.setId(5L);
            characters.setTitle("Character Creation");
            characters.setCategory("Characters");
            characters.setText("Characters have B5-specific characteristics: Strength, Dexterity, Endurance, Intelligence, Education, Social Standing, Appearance.");
            characters.setSource("Babylon 5 Core Rules");
            rules.add(characters);

            return rules;
        }

        @Override
        public List<RulesEntry> searchRules(String keyword) {
            return listRules().stream()
                    .filter(r -> r.getTitle().toLowerCase().contains(keyword.toLowerCase()) ||
                            r.getText().toLowerCase().contains(keyword.toLowerCase()))
                    .collect(Collectors.toList());
        }

        @Override
        public RulesEntry getRule(Long id) {
            return listRules().stream()
                    .filter(r -> r.getId().equals(id))
                    .findFirst().orElse(null);
        }
    }

    // Utility class for atomic counters
    private static class AtomicLong {
        private volatile long value;

        AtomicLong(long initialValue) {
            this.value = initialValue;
        }

        synchronized long getAndIncrement() {
            return value++;
        }
    }

    @Override
    public RulesEngine getRulesEngine() {
        return rulesEngine;
    }
}
