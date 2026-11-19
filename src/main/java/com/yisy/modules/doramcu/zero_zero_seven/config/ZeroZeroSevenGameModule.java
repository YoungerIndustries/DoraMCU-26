package com.yisy.modules.doramcu.zero_zero_seven.config;

import com.yisy.modules.doramcu.shared.model.RulesEntry;
import com.yisy.modules.doramcu.shared.service.RulesAwareCombatAdapter;
import com.yisy.modules.doramcu.shared.service.UnifiedRulesQueryService;
import com.yisy.modules.doramcu.gamecore.RulesEngine;
import com.yisy.modules.doramcu.zero_zero_seven.intelligence.IntelligenceService;
import com.yisy.modules.doramcu.zero_zero_seven.military.GroundCombatService;
import com.yisy.modules.doramcu.zero_zero_seven.networks.SpyNetworkService;
import com.yisy.modules.doramcu.zero_zero_seven.networks.SafehouseService;
import com.yisy.modules.doramcu.zero_zero_seven.operations.CovertOperationService;
import com.yisy.modules.doramcu.zero_zero_seven.service.RulesDatabaseService;
import com.yisy.modules.doramcu.zero_zero_seven.service.ZeroZeroSevenRulesEngine;
import com.yisy.modules.doramcu.zero_zero_seven.model.BondAgent;
import com.yisy.modules.doramcu.zero_zero_seven.model.CovertOperation;
import com.yisy.modules.doramcu.zero_zero_seven.model.SpyNetwork;
import com.yisy.modules.doramcu.zero_zero_seven.repository.BondAgentRepository;
import com.yisy.modules.doramcu.zero_zero_seven.repository.CovertOperationRepository;
import com.yisy.modules.doramcu.zero_zero_seven.repository.SpyNetworkRepository;
import com.yisy.modules.doramcu.gamecore.GameType;
import com.yisy.modules.doramcu.util.DiceUtil;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 007 (James Bond) Game Module adapter.
 * <p>
 * Implements the {@link GameModule} interface to register 007 espionage features
 * with the {@link GameModuleRegistry}. This allows Earth2026 and other modules
 * to access spy/espionage features through the shared game-core interfaces.
 * </p>
 * <p>
 * 007 provides:
 * <ul>
 *   <li>Intelligence: Spy networks, surveillance, counter-intelligence</li>
 *   <li>Covert Operations: Assassination, infiltration, sabotage</li>
 *   <li>Safehouses: Hidden bases and recruitment</li>
 *   <li>Combat: Modern firearms and tactical combat</li>
 *   <li>Encounters: Espionage scenarios and missions</li>
 * </ul>
 * </p>
 */
@Component
public class ZeroZeroSevenGameModule extends AbstractGameModule {

    private final IntelligenceService intelligenceService;
    private final GroundCombatService groundCombatService;
    private final SpyNetworkService spyNetworkService;
    private final SafehouseService safehouseService;
    private final CovertOperationService covertOperationService;
    private final BondAgentRepository agentRepository;
    private final CovertOperationRepository operationRepository;
    private final SpyNetworkRepository spyNetworkRepository;
    private final RulesDatabaseService rulesDatabaseService;
    private final UnifiedRulesQueryService unifiedRulesQueryService;
    private final ZeroZeroSevenRulesEngine rulesEngine;

    public ZeroZeroSevenGameModule(
            IntelligenceService intelligenceService,
            GroundCombatService groundCombatService,
            SpyNetworkService spyNetworkService,
            SafehouseService safehouseService,
            CovertOperationService covertOperationService,
            BondAgentRepository agentRepository,
            CovertOperationRepository operationRepository,
            SpyNetworkRepository spyNetworkRepository,
            RulesDatabaseService rulesDatabaseService,
            GameModuleRegistry gameModuleRegistry,
            UnifiedRulesQueryService unifiedRulesQueryService,
            ZeroZeroSevenRulesEngine rulesEngine) {
        super(gameModuleRegistry);
        this.intelligenceService = intelligenceService;
        this.groundCombatService = groundCombatService;
        this.spyNetworkService = spyNetworkService;
        this.safehouseService = safehouseService;
        this.covertOperationService = covertOperationService;
        this.agentRepository = agentRepository;
        this.operationRepository = operationRepository;
        this.spyNetworkRepository = spyNetworkRepository;
        this.rulesDatabaseService = rulesDatabaseService;
        this.unifiedRulesQueryService = unifiedRulesQueryService;
        this.rulesEngine = rulesEngine;
    }

    @Override
    public GameType getGameType() {
        return GameType.ZERO_ZERO_SEVEN;
    }

    @Override
    public String getModuleName() {
        return "007 Espionage";
    }

    @Override
    public DiceService getDiceService() {
        return new BondDiceAdapter();
    }

    @Override
    public CombatService getCombatService() {
        // Wrap the 007 combat adapter with rules-aware combat
        CombatService delegate = new BondCombatAdapter(groundCombatService);
        RulesAwareCombatAdapter rulesAware = new RulesAwareCombatAdapter(delegate, unifiedRulesQueryService);
        return rulesAware;
    }

    @Override
    public InventoryService getInventoryService() {
        return new BondInventoryAdapter(agentRepository);
    }

    @Override
    public CraftingService getCraftingService() {
        return new BondCraftingAdapter();
    }

    @Override
    public DungeonRoomService getDungeonRoomService() {
        return new BondDungeonRoomAdapter(safehouseService);
    }

    @Override
    public SpellService getSpellService() {
        return new BondSpellAdapter();
    }

    @Override
    public PsionicsService getPsionicsService() {
        return new BondPsionicsAdapter();
    }

    @Override
    public EncounterService getEncounterService() {
        return new BondEncounterAgentAdapter(covertOperationService, intelligenceService);
    }

    @Override
    public CharacterService getCharacterService() {
        return new BondCharacterAdapter(agentRepository);
    }

    @Override
    public CampaignService getCampaignService() {
        return new BondCampaignAdapter(covertOperationService, spyNetworkService);
    }

    @Override
    public RulesService getRulesService() {
        return new BondRulesAdapter(rulesDatabaseService);
    }

    @Override
    public Set<MechanicType> getMechanics() {
        return Set.of(
                MechanicType.RPG,
                MechanicType.COMBAT,
                MechanicType.ENCOUNTER,
                MechanicType.CHARACTER,
                MechanicType.SUPERNATURAL
        );
    }

    // ==================== Adapter Implementations ====================

    /**
     * Adapts 007 dice system to DiceService interface.
     * Uses d20-based skill checks with espionage modifiers.
     */
    private static class BondDiceAdapter implements DiceService {

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
            // 007 uses d20 for skill checks
            int roll = DiceUtil.rollDSides(20);
            return new DiceRollResult("d20", List.of(roll), roll,
                    "Skill check: " + roll);
        }

        @Override
        public DiceRollResult rollWithAdvantage(String formula) {
            int roll1 = DiceUtil.rollDSides(20);
            int roll2 = DiceUtil.rollDSides(20);
            int total = Math.max(roll1, roll2);
            return new DiceRollResult(formula + " (advantage)", List.of(roll1, roll2, total), total,
                    "Advantage roll: " + roll1 + ", " + roll2 + " → " + total);
        }

        @Override
        public DiceRollResult rollWithDisadvantage(String formula) {
            int roll1 = DiceUtil.rollDSides(20);
            int roll2 = DiceUtil.rollDSides(20);
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
     * Adapts 007 ground combat to CombatService interface.
     */
    private static class BondCombatAdapter implements CombatService {
        private final GroundCombatService groundCombatService;
        private final Map<Long, CombatState> combats = new HashMap<>();
        private final AtomicLong combatIdCounter = new AtomicLong(1);

        BondCombatAdapter(GroundCombatService groundCombatService) {
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
            state.combatants.sort((a, b) -> {
                int initA = DiceUtil.rollDSides(20) + (dexModifiers.getOrDefault(a.name, 0));
                int initB = DiceUtil.rollDSides(20) + (dexModifiers.getOrDefault(b.name, 0));
                return Integer.compare(initB, initA);
            });
            return getCombat(combatId);
        }

        @Override
        public AttackResult makeAttack(Long combatId, AttackRequest request) {
            CombatState state = combats.get(combatId);
            if (state == null) return null;

            // 007 combat: d20 + bonus vs AC
            int attackRoll = DiceUtil.rollDSides(20);
            int attackTotal = attackRoll + request.getAttackBonus();
            int targetAc = 10;
            for (Combatant c : state.combatants) {
                if (c.name.equals(request.getTargetName())) {
                    targetAc = c.armorClass;
                    break;
                }
            }

            boolean hit = attackTotal >= targetAc;
            boolean critical = attackRoll == 20;
            boolean fumble = attackRoll == 1;

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
                result.setDamageType(request.getDamageType() != null ? request.getDamageType() : "piercing");
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
     * Adapts 007 agent equipment to InventoryService interface.
     */
    private static class BondInventoryAdapter implements InventoryService {
        private final BondAgentRepository agentRepository;

        BondInventoryAdapter(BondAgentRepository agentRepository) {
            this.agentRepository = agentRepository;
        }

        @Override
        public List<InventoryItem> getInventory(Long characterId) {
            Optional<BondAgent> agentOpt = agentRepository.findById(characterId);
            if (agentOpt.isEmpty()) return List.of();

            BondAgent agent = agentOpt.get();
            List<InventoryItem> items = new ArrayList<>();

            InventoryItem weapon = new InventoryItem();
            weapon.setName("Walther PPK");
            weapon.setDescription("Standard issue sidearm");
            weapon.setItemType("weapon");
            weapon.setQuantity(1);
            weapon.setWeight(1);
            items.add(weapon);

            InventoryItem gadgets = new InventoryItem();
            gadgets.setName("Gadget Kit");
            gadgets.setDescription("Q Branch supplied gadgets");
            gadgets.setItemType("equipment");
            gadgets.setQuantity(1);
            gadgets.setWeight(2);
            items.add(gadgets);

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
            return "Agent uses equipment";
        }
    }

    /**
     * Adapts 007 gadget creation to CraftingService interface.
     */
    private static class BondCraftingAdapter implements CraftingService {
        private final Map<Long, CraftingProject> projects = new HashMap<>();
        private final AtomicLong projectIdCounter = new AtomicLong(1);

        @Override
        public List<CraftingRecipe> listRecipes() {
            List<CraftingRecipe> recipes = new ArrayList<>();

            CraftingRecipe watch = new CraftingRecipe();
            watch.setId(1L);
            watch.setName("Explosive Wristwatch");
            watch.setDescription("Classic Q Branch explosive watch");
            watch.setCategory("Gadgets");
            watch.setDifficulty(12);
            watch.setTimeRequired(180);
            watch.setMaterialsRequired("Explosives, watch components, timer");
            watch.setResultItem("Explosive wristwatch");
            recipes.add(watch);

            CraftingRecipe pen = new CraftingRecipe();
            pen.setId(2L);
            pen.setName("Stun Gun Pen");
            pen.setDescription("Discreet stun weapon disguised as a pen");
            pen.setCategory("Gadgets");
            pen.setDifficulty(10);
            pen.setTimeRequired(120);
            pen.setMaterialsRequired("Electronics, pen casing, capacitor");
            pen.setResultItem("Stun gun pen");
            recipes.add(pen);

            CraftingRecipe car = new CraftingRecipe();
            car.setId(3L);
            car.setName("Vehicle Modifications");
            car.setDescription("Add defensive/offensive systems to a vehicle");
            car.setCategory("Engineering");
            car.setDifficulty(15);
            car.setTimeRequired(480);
            car.setMaterialsRequired("Armor plating, weapons systems, electronics");
            car.setResultItem("Modified vehicle");
            recipes.add(car);

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
     * Adapts 007 safehouses to DungeonRoomService interface.
     * Safehouses serve as hidden bases for espionage operations.
     */
    private static class BondDungeonRoomAdapter implements DungeonRoomService {
        private final SafehouseService safehouseService;
        private final Map<Long, DungeonRoom> rooms = new HashMap<>();
        private final AtomicLong roomIdCounter = new AtomicLong(1);

        BondDungeonRoomAdapter(SafehouseService safehouseService) {
            this.safehouseService = safehouseService;
        }

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
            return new TrapCheckResult(roomId, "Safehouse Room", dc, success, criticalFail,
                    criticalFail ? 2 : 1, "blunt", "1d6");
        }

        @Override
        public TreasureResult discoverTreasure(Long roomId) {
            return new TreasureResult(roomId, "Safehouse", "Hidden cache with classified documents",
                    0, "Uncommon");
        }
    }

    /**
     * 007 has no spells — gadgets and technology replace magic.
     */
    private static class BondSpellAdapter implements SpellService {

        @Override
        public List<Spell> listSpells() {
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
                    "007 has no spells — use gadgets and technology", 0);
        }
    }

    /**
     * 007 has no psionics — espionage skills replace psychic powers.
     */
    private static class BondPsionicsAdapter implements PsionicsService {

        @Override
        public List<PsionicPower> listPowers() {
            // 007 uses espionage skills, not psionics
            return List.of();
        }

        @Override
        public PsionicPower getPower(Long powerId) {
            return null;
        }

        @Override
        public PsionicPowerResult usePower(Long characterId, Long powerId, Long targetId) {
            return new PsionicPowerResult(powerId, "Unknown", false,
                    "007 has no psionics — use espionage skills", 0);
        }
    }

    /**
     * Adapts 007 covert operations to EncounterService interface.
     */
    private static class BondEncounterAgentAdapter implements EncounterService {
        private final CovertOperationService covertOperationService;
        private final IntelligenceService intelligenceService;

        BondEncounterAgentAdapter(CovertOperationService covertOperationService, IntelligenceService intelligenceService) {
            this.covertOperationService = covertOperationService;
            this.intelligenceService = intelligenceService;
        }

        @Override
        public List<Encounter> listEncounters() {
            List<Encounter> encounters = new ArrayList<>();

            Encounter ambush = new Encounter();
            ambush.setId(1L);
            ambush.setName("Ambush");
            ambush.setDescription("Your convoy is ambushed by hostile agents");
            ambush.setDifficulty("Hard");
            encounters.add(ambush);

            Encounter mole = new Encounter();
            mole.setId(2L);
            mole.setName("Mole Discovery");
            mole.setDescription("Discover a double agent in your organization");
            mole.setDifficulty("Medium");
            encounters.add(mole);

            Encounter chase = new Encounter();
            chase.setId(3L);
            chase.setName("Car Chase");
            chase.setDescription("High-speed pursuit through city streets");
            chase.setDifficulty("Medium");
            encounters.add(chase);

            Encounter extraction = new Encounter();
            extraction.setId(4L);
            extraction.setName("Extraction Mission");
            extraction.setDescription("Extract a valuable asset from hostile territory");
            extraction.setDifficulty("Hard");
            encounters.add(extraction);

            Encounter blackmail = new Encounter();
            blackmail.setId(5L);
            blackmail.setName("Blackmail Attempt");
            blackmail.setDescription("Someone is trying to blackmail you with compromising information");
            blackmail.setDifficulty("Easy");
            encounters.add(blackmail);

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
     * Adapts 007 agents to CharacterService interface.
     */
    private static class BondCharacterAdapter implements CharacterService {
        private final BondAgentRepository agentRepository;

        BondCharacterAdapter(BondAgentRepository agentRepository) {
            this.agentRepository = agentRepository;
        }

        @Override
        public CharacterInfo getCharacter(Long characterId) {
            Optional<BondAgent> agentOpt = agentRepository.findById(characterId);
            if (agentOpt.isEmpty()) return null;

            BondAgent agent = agentOpt.get();
            CharacterInfo info = new CharacterInfo();
            info.setId(agent.getId());
            info.setName(agent.getName());
            info.setLevel(agent.getMissionsCompleted() > 10 ? 5 : 1);
            info.setStrScore(agent.getCombat());
            info.setDexScore(agent.getStealth());
            info.setConScore(5);
            info.setIntScore(agent.getTechnical());
            info.setWisScore(5);
            info.setChaScore(agent.getCharisma());
            return info;
        }

        @Override
        public List<CharacterInfo> listCharacters() {
            List<BondAgent> agents = agentRepository.findAll();
            return agents.stream().map(agent -> {
                CharacterInfo info = new CharacterInfo();
                info.setId(agent.getId());
                info.setName(agent.getName());
                info.setLevel(agent.getMissionsCompleted() > 10 ? 5 : 1);
                return info;
            }).collect(Collectors.toList());
        }

        @Override
        public void updateAbilityScores(Long characterId, int str, int dex, int con, int intelligence, int wis, int cha) {
            // Bond agents use specialized stats, not standard ability scores
        }

        @Override
        public CharacterInfo awardExperience(Long characterId, int xp) {
            Optional<BondAgent> agentOpt = agentRepository.findById(characterId);
            if (agentOpt.isEmpty()) return null;

            BondAgent agent = agentOpt.get();
            CharacterInfo info = new CharacterInfo();
            info.setId(agent.getId());
            info.setName(agent.getName());
            info.setExperiencePoints(xp);
            return info;
        }
    }

    /**
     * Adapts 007 covert operations to CampaignService interface.
     */
    private static class BondCampaignAdapter implements CampaignService {
        private final CovertOperationService covertOperationService;
        private final SpyNetworkService spyNetworkService;
        private final Map<Long, Campaign> campaigns = new HashMap<>();
        private final AtomicLong campaignIdCounter = new AtomicLong(1);

        BondCampaignAdapter(CovertOperationService covertOperationService, SpyNetworkService spyNetworkService) {
            this.covertOperationService = covertOperationService;
            this.spyNetworkService = spyNetworkService;
        }

        @Override
        public List<Campaign> listCampaigns() {
            if (campaigns.isEmpty()) {
                createCampaign("Cold War Operations", "Espionage during the Cold War era");
                createCampaign("Terrorist Threat", "Counter-terrorism operations worldwide");
                createCampaign("Corporate Espionage", "Industrial espionage and corporate sabotage");
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
     * Adapts 007 rules database to RulesService interface.
     */
    private static class BondRulesAdapter implements RulesService {
        private final RulesDatabaseService rulesDatabaseService;

        BondRulesAdapter(RulesDatabaseService rulesDatabaseService) {
            this.rulesDatabaseService = rulesDatabaseService;
        }

        @Override
        public List<RulesEntry> listRules() {
            List<RulesEntry> rules = new ArrayList<>();

            RulesEntry espionage = new RulesEntry();
            espionage.setId(1L);
            espionage.setTitle("Espionage Rules");
            espionage.setCategory("Espionage");
            espionage.setText("Agents use stealth, technical skills, and charm to complete covert operations. Skill checks use d20 + modifier vs difficulty.");
            espionage.setSource("007 Core Rules");
            rules.add(espionage);

            RulesEntry combat = new RulesEntry();
            combat.setId(2L);
            combat.setTitle("Combat Rules");
            combat.setCategory("Combat");
            combat.setText("Modern firearms combat uses d20 + attack bonus vs AC. Critical hits on natural 20, fumbles on natural 1.");
            combat.setSource("007 Core Rules");
            rules.add(combat);

            RulesEntry gadgets = new RulesEntry();
            gadgets.setId(3L);
            gadgets.setTitle("Gadget Rules");
            gadgets.setCategory("Gadgets");
            gadgets.setText("Q Branch gadgets provide special abilities. Crafting gadgets requires engineering skills and rare materials.");
            gadgets.setSource("007 Core Rules");
            rules.add(gadgets);

            RulesEntry networks = new RulesEntry();
            networks.setId(4L);
            networks.setTitle("Spy Network Rules");
            networks.setCategory("Networks");
            networks.setText("Build and maintain spy networks to gather intelligence. Networks can be compromised by counter-intelligence.");
            networks.setSource("007 Core Rules");
            rules.add(networks);

            RulesEntry operations = new RulesEntry();
            operations.setId(5L);
            operations.setTitle("Covert Operations");
            operations.setCategory("Operations");
            operations.setText("Covert operations include assassination, infiltration, and sabotage. Each has unique success conditions and risks.");
            operations.setSource("007 Core Rules");
            rules.add(operations);

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
