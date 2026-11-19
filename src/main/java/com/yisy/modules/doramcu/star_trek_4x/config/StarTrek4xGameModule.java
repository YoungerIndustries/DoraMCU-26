package com.yisy.modules.doramcu.star_trek_4x.config;

import com.yisy.modules.doramcu.shared.model.RulesEntry;
import com.yisy.modules.doramcu.shared.service.RulesAwareCombatAdapter;
import com.yisy.modules.doramcu.shared.service.UnifiedRulesQueryService;
import com.yisy.modules.doramcu.techlevel.TechLevelAdapter;
import com.yisy.modules.doramcu.gamecore.RulesEngine;
import com.yisy.modules.doramcu.star_trek_4x.combat.GroundCombatService;
import com.yisy.modules.doramcu.star_trek_4x.diplomacy.DiplomacyService;
import com.yisy.modules.doramcu.star_trek_4x.exploitation.EconomyService;
import com.yisy.modules.doramcu.star_trek_4x.exploitation.TechnologyService;
import com.yisy.modules.doramcu.star_trek_4x.exploration.ExplorationService;
import com.yisy.modules.doramcu.star_trek_4x.exploration.FirstContactService;
import com.yisy.modules.doramcu.star_trek_4x.model.StarTrekFaction;
import com.yisy.modules.doramcu.star_trek_4x.repository.StarTrekFactionRepository;
import com.yisy.modules.doramcu.star_trek_4x.service.RulesDatabaseService;
import com.yisy.modules.doramcu.star_trek_4x.service.StarTrek4xRulesEngine;
import com.yisy.modules.doramcu.gamecore.GameType;
import com.yisy.modules.doramcu.util.DiceUtil;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Star Trek 4X Game Module adapter.
 * <p>
 * Implements the {@link GameModule} interface to register Star Trek 4X features
 * with the {@link GameModuleRegistry}. This allows Earth2026 and other modules
 * to access Star Trek 4X features through the shared game-core interfaces.
 * </p>
 * <p>
 * Star Trek 4X provides:
 * <ul>
 *   <li>4X Strategy: Explore, Expand, Exploit, Exterminate</li>
 *   <li>Diplomacy: Treaties, alliances, and diplomatic relations</li>
 *   <li>Exploration: First contact, scientific missions, sector scanning</li>
 *   <li>Combat: Fleet warfare and ground combat</li>
 *   <li>Economy: Trade routes, resources, technology research</li>
 *   <li>Encounters: Random events and first contact scenarios</li>
 * </ul>
 * </p>
 */
@Component
public class StarTrek4xGameModule extends AbstractGameModule {

    private final GroundCombatService groundCombatService;
    private final DiplomacyService diplomacyService;
    private final EconomyService economyService;
    private final TechnologyService technologyService;
    private final ExplorationService explorationService;
    private final FirstContactService firstContactService;
    private final StarTrekFactionRepository factionRepository;
    private final RulesDatabaseService rulesDatabaseService;
    private final StarTrekTechLevelAdapter techLevelAdapter;
    private final UnifiedRulesQueryService unifiedRulesQueryService;
    private final StarTrek4xRulesEngine rulesEngine;

    public StarTrek4xGameModule(
            GroundCombatService groundCombatService,
            DiplomacyService diplomacyService,
            EconomyService economyService,
            TechnologyService technologyService,
            ExplorationService explorationService,
            FirstContactService firstContactService,
            StarTrekFactionRepository factionRepository,
            RulesDatabaseService rulesDatabaseService,
            GameModuleRegistry gameModuleRegistry,
            StarTrekTechLevelAdapter techLevelAdapter,
            UnifiedRulesQueryService unifiedRulesQueryService,
            StarTrek4xRulesEngine rulesEngine) {
        super(gameModuleRegistry);
        this.groundCombatService = groundCombatService;
        this.diplomacyService = diplomacyService;
        this.economyService = economyService;
        this.technologyService = technologyService;
        this.explorationService = explorationService;
        this.firstContactService = firstContactService;
        this.factionRepository = factionRepository;
        this.rulesDatabaseService = rulesDatabaseService;
        this.techLevelAdapter = techLevelAdapter;
        this.unifiedRulesQueryService = unifiedRulesQueryService;
        this.rulesEngine = rulesEngine;
    }

    @Override
    public GameType getGameType() {
        return GameType.STAR_TREK_4X;
    }

    @Override
    public String getModuleName() {
        return "Star Trek 4X Strategy";
    }

    @Override
    public DiceService getDiceService() {
        return new StarTrekDiceAdapter();
    }

    @Override
    public CombatService getCombatService() {
        // Wrap the Star Trek combat adapter with rules-aware combat
        CombatService delegate = new StarTrekCombatAdapter(groundCombatService);
        RulesAwareCombatAdapter rulesAware = new RulesAwareCombatAdapter(delegate, unifiedRulesQueryService);
        return rulesAware;
    }

    @Override
    public InventoryService getInventoryService() {
        return new StarTrekInventoryAdapter(economyService);
    }

    @Override
    public CraftingService getCraftingService() {
        return new StarTrekCraftingAdapter(technologyService);
    }

    @Override
    public DungeonRoomService getDungeonRoomService() {
        return new StarTrekDungeonRoomAdapter();
    }

    @Override
    public SpellService getSpellService() {
        return new StarTrekSpellAdapter();
    }

    @Override
    public PsionicsService getPsionicsService() {
        return new StarTrekPsionicsAdapter();
    }

    @Override
    public EncounterService getEncounterService() {
        return new StarTrekEncounterAdapter(firstContactService, explorationService);
    }

    @Override
    public CharacterService getCharacterService() {
        return new StarTrekCharacterAdapter(factionRepository);
    }

    @Override
    public CampaignService getCampaignService() {
        return new StarTrekCampaignAdapter(diplomacyService, explorationService);
    }

    @Override
    public RulesService getRulesService() {
        return new StarTrekRulesAdapter(rulesDatabaseService);
    }

    @Override
    public TechLevelAdapter getTechLevelAdapter() {
        return techLevelAdapter;
    }

    @Override
    public Set<MechanicType> getMechanics() {
        return Set.of(
                MechanicType.FOUR_X,
                MechanicType.DIPLOMACY,
                MechanicType.COMBAT,
                MechanicType.EXPLORATION,
                MechanicType.ECONOMY
        );
    }

    // ==================== Adapter Implementations ====================

    /**
     * Adapts Star Trek dice system to DiceService interface.
     * Star Trek Adventures uses 2d10 (percentile) with difficulty thresholds.
     */
    private static class StarTrekDiceAdapter implements DiceService {

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
            // Star Trek Adventures uses percentile dice (2d10)
            int tens = DiceUtil.rollDSides(10) * 10;
            int ones = DiceUtil.rollDSides(10);
            int total = (tens == 100 && ones == 10) ? 100 : tens + ones;
            return new DiceRollResult("2d10", List.of(tens / 10, ones), total,
                    "Percentile roll: " + total);
        }

        @Override
        public DiceRollResult rollWithAdvantage(String formula) {
            int roll1 = DiceUtil.rollDSides(10) * 10 + DiceUtil.rollDSides(10);
            int roll2 = DiceUtil.rollDSides(10) * 10 + DiceUtil.rollDSides(10);
            // In Star Trek, lower is better, so advantage = take lower
            int total = Math.min(roll1, roll2);
            return new DiceRollResult(formula + " (advantage)", List.of(roll1, roll2, total), total,
                    "Advantage roll: " + roll1 + ", " + roll2 + " → " + total);
        }

        @Override
        public DiceRollResult rollWithDisadvantage(String formula) {
            int roll1 = DiceUtil.rollDSides(10) * 10 + DiceUtil.rollDSides(10);
            int roll2 = DiceUtil.rollDSides(10) * 10 + DiceUtil.rollDSides(10);
            // Disadvantage = take higher (worse)
            int total = Math.max(roll1, roll2);
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
     * Adapts Star Trek ground combat to CombatService interface.
     */
    private static class StarTrekCombatAdapter implements CombatService {
        private final GroundCombatService groundCombatService;
        private final Map<Long, CombatState> combats = new HashMap<>();
        private final AtomicLong combatIdCounter = new AtomicLong(1);

        StarTrekCombatAdapter(GroundCombatService groundCombatService) {
            this.groundCombatService = groundCombatService;
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
            // Star Trek uses agility-based initiative
            state.combatants.sort((a, b) -> {
                int initA = DiceUtil.rollDSides(10) + (dexModifiers.getOrDefault(a.name, 0));
                int initB = DiceUtil.rollDSides(10) + (dexModifiers.getOrDefault(b.name, 0));
                return Integer.compare(initB, initA); // Higher goes first
            });
            return getCombat(combatId);
        }

        @Override
        public AttackResult makeAttack(Long combatId, AttackRequest request) {
            CombatState state = combats.get(combatId);
            if (state == null) return null;

            // Star Trek attack: roll 2d10 vs difficulty
            int attackRoll = DiceUtil.rollDSides(10) * 10 + DiceUtil.rollDSides(10);
            int difficulty = 50; // Base difficulty
            int targetAc = 0;
            for (Combatant c : state.combatants) {
                if (c.name.equals(request.getTargetName())) {
                    targetAc = c.armorClass;
                    break;
                }
            }
            difficulty = Math.max(10, difficulty - targetAc);

            boolean hit = attackRoll <= difficulty;
            boolean critical = attackRoll <= 5;
            boolean fumble = attackRoll >= 96;

            AttackResult result = new AttackResult();
            result.setAttackerName(request.getAttackerName());
            result.setTargetName(request.getTargetName());
            result.setAttackRoll(attackRoll);
            result.setAttackTotal(attackRoll);
            result.setHit(hit);
            result.setCriticalHit(critical);
            result.setCriticalFail(fumble);

            if (hit) {
                int damage = DiceUtil.rollDSides(6) + DiceUtil.rollDSides(6);
                result.setDamageDealt(damage);
                result.setDamageType(request.getDamageType() != null ? request.getDamageType() : "energy");
                result.setMessage(request.getAttackerName() + " hits " + request.getTargetName() + " for " + damage + " damage!");

                // Apply damage
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
     * Adapts Star Trek resources to InventoryService interface.
     */
    private static class StarTrekInventoryAdapter implements InventoryService {
        private final EconomyService economyService;

        StarTrekInventoryAdapter(EconomyService economyService) {
            this.economyService = economyService;
        }

        @Override
        public List<InventoryItem> getInventory(Long characterId) {
            // Star Trek uses resource-based inventory
            List<InventoryItem> items = new ArrayList<>();
            InventoryItem dilithium = new InventoryItem();
            dilithium.setName("Dilithium Crystals");
            dilithium.setDescription("Warp core fuel crystals");
            dilithium.setItemType("resource");
            dilithium.setQuantity(10);
            dilithium.setWeight(5);
            items.add(dilithium);
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
            return "Item used by Star Trek character";
        }
    }

    /**
     * Adapts Star Trek technology research to CraftingService interface.
     */
    private static class StarTrekCraftingAdapter implements CraftingService {
        private final TechnologyService technologyService;
        private final Map<Long, CraftingProject> projects = new HashMap<>();
        private final AtomicLong projectIdCounter = new AtomicLong(1);

        StarTrekCraftingAdapter(TechnologyService technologyService) {
            this.technologyService = technologyService;
        }

        @Override
        public List<CraftingRecipe> listRecipes() {
            List<CraftingRecipe> recipes = new ArrayList<>();

            CraftingRecipe warpCore = new CraftingRecipe();
            warpCore.setId(1L);
            warpCore.setName("Warp Core Assembly");
            warpCore.setDescription("Assemble a warp drive core from dilithium crystals");
            warpCore.setCategory("Engineering");
            warpCore.setDifficulty(12);
            warpCore.setTimeRequired(480);
            warpCore.setMaterialsRequired("Dilithium crystals, tritanium hull plating");
            warpCore.setResultItem("Functional warp core");
            recipes.add(warpCore);

            CraftingRecipe phaser = new CraftingRecipe();
            phaser.setId(2L);
            phaser.setName("Phaser Calibration");
            phaser.setDescription("Calibrate phaser array for optimal output");
            phaser.setCategory("Weapons");
            phaser.setDifficulty(8);
            phaser.setTimeRequired(120);
            phaser.setMaterialsRequired("Phaser components, energy cells");
            phaser.setResultItem("Calibrated phaser array");
            recipes.add(phaser);

            CraftingRecipe medigel = new CraftingRecipe();
            medigel.setId(3L);
            medigel.setName("Medigel Synthesis");
            medigel.setDescription("Synthesize healing medigel for sickbay");
            medigel.setCategory("Medical");
            medigel.setDifficulty(10);
            medigel.setTimeRequired(60);
            medigel.setMaterialsRequired("Biochemical compounds, hypospray cartridges");
            medigel.setResultItem("Medigel supply");
            recipes.add(medigel);

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
     * Adapts Star Trek ship interiors to DungeonRoomService interface.
     * Star Trek ships have decks and rooms that can be explored.
     */
    private static class StarTrekDungeonRoomAdapter implements DungeonRoomService {
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
            return new TrapCheckResult(roomId, "Ship Room", dc, success, criticalFail,
                    criticalFail ? 2 : 1, "energy", "1d6");
        }

        @Override
        public TreasureResult discoverTreasure(Long roomId) {
            return new TreasureResult(roomId, "Ship Room", "Data logs with classified information",
                    0, "Common");
        }
    }

    /**
     * Star Trek has no traditional spells, but has technobabble abilities.
     */
    private static class StarTrekSpellAdapter implements SpellService {

        @Override
        public List<Spell> listSpells() {
            // Star Trek has no spells; technobabble abilities are used instead
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
                    "Star Trek has no spells — use technobabble abilities", 0);
        }
    }

    /**
     * Star Trek has limited psionics (Vulcan mind meld, etc.).
     */
    private static class StarTrekPsionicsAdapter implements PsionicsService {
        private final Map<Long, PsionicPower> psionicPowers = new HashMap<>();

        StarTrekPsionicsAdapter() {
            initializePsionicPowers();
        }

        private void initializePsionicPowers() {
            addPsionicPower(1L, "Mind Meld", "Vulcan", 3, 3, "Touch", "Concentration",
                    "Read the memories of a target creature.");
            addPsionicPower(2L, "Mental Discipline", "Vulcan", 1, 1, "Self", "1 hour",
                    "Suppress emotions and resist mental influence.");
            addPsionicPower(3L, "Telepathy", "Betazoid", 2, 2, "60 feet", "Concentration",
                    "Communicate telepathically with a target.");
            addPsionicPower(4L, "Precognition", "Betazoid", 4, 4, "Self", "Instant",
                    "Sense impending danger or future events.");
            addPsionicPower(5L, "Empathy", "Betazoid", 1, 1, "30 feet", "Concentration",
                    "Sense the emotional state of nearby creatures.");
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
            int roll = DiceUtil.rollDSides(20);
            boolean success = roll >= power.getPowerLevel() * 5;
            return new PsionicPowerResult(powerId, power.getName(), success,
                    success ? power.getName() + " used successfully!" : "Power failed.", power.getPowerPointsCost());
        }
    }

    /**
     * Adapts Star Trek first contact and exploration to EncounterService interface.
     */
    private static class StarTrekEncounterAdapter implements EncounterService {
        private final FirstContactService firstContactService;
        private final ExplorationService explorationService;

        StarTrekEncounterAdapter(FirstContactService firstContactService, ExplorationService explorationService) {
            this.firstContactService = firstContactService;
            this.explorationService = explorationService;
        }

        @Override
        public List<Encounter> listEncounters() {
            List<Encounter> encounters = new ArrayList<>();

            Encounter firstContact = new Encounter();
            firstContact.setId(1L);
            firstContact.setName("First Contact");
            firstContact.setDescription("Encounter with a previously unknown species");
            firstContact.setDifficulty("Medium");
            encounters.add(firstContact);

            Encounter anomaly = new Encounter();
            anomaly.setId(2L);
            anomaly.setName("Spatial Anomaly");
            anomaly.setDescription("Navigate through a dangerous spatial phenomenon");
            anomaly.setDifficulty("Hard");
            encounters.add(anomaly);

            Encounter distress = new Encounter();
            distress.setId(3L);
            distress.setName("Distress Signal");
            distress.setDescription("Respond to a distress call from an unknown source");
            distress.setDifficulty("Easy");
            encounters.add(distress);

            Encounter pirate = new Encounter();
            pirate.setId(4L);
            pirate.setName("Pirate Attack");
            pirate.setDescription("Defend against rogue space pirates");
            pirate.setDifficulty("Medium");
            encounters.add(pirate);

            Encounter derelict = new Encounter();
            derelict.setId(5L);
            derelict.setName("Derelict Ship");
            derelict.setDescription("Investigate an abandoned vessel drifting in space");
            derelict.setDifficulty("Medium");
            encounters.add(derelict);

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
            int roll = DiceUtil.rollDSides(20);
            boolean success = roll + characterIds.size() * 2 >= 10;
            return new EncounterResult(encounterId, enc.getName(), success,
                    success ? "Encounter resolved successfully" : "Encounter failed", 0);
        }
    }

    /**
     * Adapts Star Trek factions to CharacterService interface.
     */
    private static class StarTrekCharacterAdapter implements CharacterService {
        private final StarTrekFactionRepository factionRepository;

        StarTrekCharacterAdapter(StarTrekFactionRepository factionRepository) {
            this.factionRepository = factionRepository;
        }

        @Override
        public CharacterInfo getCharacter(Long characterId) {
            // Star Trek uses factions, not individual characters in this context
            return null;
        }

        @Override
        public List<CharacterInfo> listCharacters() {
            List<StarTrekFaction> factions = factionRepository.findAll();
            return factions.stream().map(faction -> {
                CharacterInfo info = new CharacterInfo();
                info.setId(faction.getId());
                info.setName(faction.getName());
                info.setLevel(faction.getFleetStrength() > 50 ? 5 : 1);
                return info;
            }).collect(Collectors.toList());
        }

        @Override
        public void updateAbilityScores(Long characterId, int str, int dex, int con, int intelligence, int wis, int cha) {
            // Star Trek factions don't use ability scores
        }

        @Override
        public CharacterInfo awardExperience(Long characterId, int xp) {
            return null;
        }
    }

    /**
     * Adapts Star Trek diplomacy and exploration to CampaignService interface.
     */
    private static class StarTrekCampaignAdapter implements CampaignService {
        private final DiplomacyService diplomacyService;
        private final ExplorationService explorationService;
        private final Map<Long, Campaign> campaigns = new HashMap<>();
        private final AtomicLong campaignIdCounter = new AtomicLong(1);

        StarTrekCampaignAdapter(DiplomacyService diplomacyService, ExplorationService explorationService) {
            this.diplomacyService = diplomacyService;
            this.explorationService = explorationService;
        }

        @Override
        public List<Campaign> listCampaigns() {
            if (campaigns.isEmpty()) {
                // Seed default campaigns
                createCampaign("Galactic Exploration", "Explore the unknown regions of space");
                createCampaign("Diplomatic Relations", "Establish alliances with alien species");
                createCampaign("Threat Response", "Defend against emerging threats");
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
     * Adapts Star Trek rules database to RulesService interface.
     */
    private static class StarTrekRulesAdapter implements RulesService {
        private final RulesDatabaseService rulesDatabaseService;

        StarTrekRulesAdapter(RulesDatabaseService rulesDatabaseService) {
            this.rulesDatabaseService = rulesDatabaseService;
        }

        @Override
        public List<RulesEntry> listRules() {
            List<RulesEntry> rules = new ArrayList<>();

            RulesEntry exploration = new RulesEntry();
            exploration.setId(1L);
            exploration.setTitle("Exploration Rules");
            exploration.setCategory("Exploration");
            exploration.setText("Sectors are explored by sending starships. Each exploration mission reveals star systems, planets, and potential first contact opportunities.");
            exploration.setSource("Star Trek 4X Core Rules");
            rules.add(exploration);

            RulesEntry diplomacy = new RulesEntry();
            diplomacy.setId(2L);
            diplomacy.setTitle("Diplomacy Rules");
            diplomacy.setCategory("Diplomacy");
            diplomacy.setText("Factions can establish diplomatic relations, sign treaties, and form alliances. Relations range from Hostile to Allied.");
            diplomacy.setSource("Star Trek 4X Core Rules");
            rules.add(diplomacy);

            RulesEntry combat = new RulesEntry();
            combat.setId(3L);
            combat.setTitle("Combat Rules");
            combat.setCategory("Combat");
            combat.setText("Combat uses percentile dice (2d10). Lower rolls are better. Critical successes occur on rolls of 5 or lower, fumbles on 96 or higher.");
            combat.setSource("Star Trek 4X Core Rules");
            rules.add(combat);

            RulesEntry technology = new RulesEntry();
            technology.setId(4L);
            technology.setTitle("Technology Research");
            technology.setCategory("Technology");
            technology.setText("Research new technologies to improve ship capabilities, weapons, shields, and warp drive efficiency.");
            technology.setSource("Star Trek 4X Core Rules");
            rules.add(technology);

            RulesEntry economy = new RulesEntry();
            economy.setId(5L);
            economy.setTitle("Economy Rules");
            economy.setCategory("Economy");
            economy.setText("Manage resources like dilithium, tritanium, and deuterium. Trade routes generate income and support expansion.");
            economy.setSource("Star Trek 4X Core Rules");
            rules.add(economy);

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
