package com.yisy.modules.doramcu.hex.planet.config;

import com.yisy.modules.doramcu.gamecore.*;
import com.yisy.modules.doramcu.gamecore.CampaignService.Campaign;
import com.yisy.modules.doramcu.gamecore.CraftingService.CraftingRecipe;
import com.yisy.modules.doramcu.gamecore.CraftingService.CraftingProject;
import com.yisy.modules.doramcu.gamecore.DungeonRoomService.DungeonRoom;
import com.yisy.modules.doramcu.gamecore.DungeonRoomService.TrapCheckResult;
import com.yisy.modules.doramcu.gamecore.DungeonRoomService.TreasureResult;
import com.yisy.modules.doramcu.gamecore.EncounterService.Encounter;
import com.yisy.modules.doramcu.gamecore.EncounterService.EncounterResult;
import com.yisy.modules.doramcu.gamecore.InventoryService.InventoryItem;
import com.yisy.modules.doramcu.shared.model.RulesEntry;
import com.yisy.modules.doramcu.shared.service.RulesAwareCombatAdapter;
import com.yisy.modules.doramcu.shared.service.UnifiedRulesQueryService;
import com.yisy.modules.doramcu.gamecore.RulesEngine;
import com.yisy.modules.doramcu.hex.planet.*;
import com.yisy.modules.doramcu.gamecore.GameType;
import com.yisy.modules.doramcu.hex.planet.service.TransportTycoonRulesEngine;
import com.yisy.modules.doramcu.util.DiceUtil;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Transport Tycoon Game Module adapter.
 * <p>
 * Implements the {@link GameModule} interface to register Transport Tycoon features
 * with the {@link GameModuleRegistry}. This allows Earth2026 and other modules
 * to access Transport Tycoon features through the shared game-core interfaces.
 * </p>
 * <p>
 * Transport Tycoon provides:
 * <ul>
 *   <li>Dice: d20-based skill checks for business operations</li>
 *   <li>Combat: Economic warfare — market share battles, route sabotage, AI rival competition</li>
 *   <li>Inventory: Cargo inventory system with 22 cargo types across 4 categories</li>
 *   <li>Crafting: Building industries, infrastructure, stations, and settlements</li>
 *   <li>Dungeon Rooms: Station interiors, corporate offices, settlement buildings</li>
 *   <li>Encounters: Market events, contract opportunities, AI rival actions, RPG quests</li>
 *   <li>Characters: Corporation profiles with rank progression (Freelancer → Galactic Monopoly)</li>
 *   <li>Campaigns: Turn-based campaigns with objectives, market share goals, and win conditions</li>
 *   <li>Rules: Trade mechanics, cargo flow, market share calculation, contract execution</li>
 * </ul>
 * </p>
 */
@Component
public class TransportTycoonGameModule extends AbstractGameModule {

    private final UnifiedRulesQueryService unifiedRulesQueryService;
    private final TransportTycoonRulesEngine rulesEngine;

    /** Cached cargo inventory items. */
    private final Map<Long, InventoryItem> cargoItems = new HashMap<>();

    /** Cached crafting recipes for building operations. */
    private final List<CraftingRecipe> buildingRecipes = new ArrayList<>();

    /** Cached station/office dungeon rooms. */
    private final Map<Long, DungeonRoom> rooms = new HashMap<>();
    private long nextRoomId = 1L;

    /** Cached encounter types. */
    private final List<Encounter> encounters = new ArrayList<>();

    /** Cached campaign data. */
    private final Map<Long, Campaign> campaigns = new HashMap<>();
    private long nextCampaignId = 1L;

    public TransportTycoonGameModule(GameModuleRegistry gameModuleRegistry,
                                      UnifiedRulesQueryService unifiedRulesQueryService,
                                      TransportTycoonRulesEngine rulesEngine) {
        super(gameModuleRegistry);
        this.unifiedRulesQueryService = unifiedRulesQueryService;
        this.rulesEngine = rulesEngine;
        initializeCargoItems();
        initializeBuildingRecipes();
        initializeEncounters();
    }

    /**
     * Initializes all 22 cargo types as inventory items.
     */
    private void initializeCargoItems() {
        long id = 1L;
        for (CargoType cargoType : CargoType.values()) {
            InventoryItem item = new InventoryItem();
            item.setId(id++);
            item.setName(cargoType.getDisplayName());
            item.setDescription(cargoType.getCategory().name() + " cargo: " + cargoType.getDisplayName());
            item.setItemType(cargoType.getCategory().name().toLowerCase());
            item.setQuantity(0);
            item.setWeight(cargoType.getBaseValue());
            cargoItems.put(item.getId(), item);
        }
    }

    /**
     * Initializes building recipes for industry and infrastructure construction.
     */
    private void initializeBuildingRecipes() {
        long id = 1L;

        for (IndustryType industryType : IndustryType.values()) {
            CraftingRecipe recipe = new CraftingRecipe();
            recipe.setId(id++);
            recipe.setName("Build " + industryType.getDisplayName());
            recipe.setDescription("Construct a " + industryType.getDisplayName() + " on the planet surface.");
            recipe.setCategory("Industry");
            recipe.setDifficulty(industryType.getLevel().ordinal() + 1);
            recipe.setTimeRequired(industryType.getConstructionCost() / 10);
            recipe.setMaterialsRequired("Construction materials, workers");
            recipe.setResultItem(industryType.getDisplayName() + " facility");
            buildingRecipes.add(recipe);
        }

        // Infrastructure recipes
        CraftingRecipe road = new CraftingRecipe();
        road.setId(id++);
        road.setName("Build Road");
        road.setDescription("Construct a road connecting two hex cells.");
        road.setCategory("Infrastructure");
        road.setDifficulty(3);
        road.setTimeRequired(60);
        road.setMaterialsRequired("Paving materials, construction equipment");
        road.setResultItem("Road segment");
        buildingRecipes.add(road);

        CraftingRecipe rail = new CraftingRecipe();
        rail.setId(id++);
        rail.setName("Build Railway");
        rail.setDescription("Construct a railway line connecting two hex cells.");
        rail.setCategory("Infrastructure");
        rail.setDifficulty(5);
        rail.setTimeRequired(120);
        rail.setMaterialsRequired("Rails, ties, construction equipment");
        rail.setResultItem("Railway segment");
        buildingRecipes.add(rail);

        CraftingRecipe station = new CraftingRecipe();
        station.setId(id++);
        station.setName("Build Station");
        station.setDescription("Construct a cargo loading station on a hex cell.");
        station.setCategory("Infrastructure");
        station.setDifficulty(7);
        station.setTimeRequired(180);
        station.setMaterialsRequired("Steel, concrete, loading equipment");
        station.setResultItem("Cargo station");
        buildingRecipes.add(station);

        CraftingRecipe settlement = new CraftingRecipe();
        settlement.setId(id++);
        settlement.setName("Found Settlement");
        settlement.setDescription("Establish a new settlement on the planet surface.");
        settlement.setCategory("Settlement");
        settlement.setDifficulty(8);
        settlement.setTimeRequired(240);
        settlement.setMaterialsRequired("Housing materials, infrastructure, population");
        settlement.setResultItem("New settlement");
        buildingRecipes.add(settlement);
    }

    /**
     * Initializes encounter types for Transport Tycoon.
     */
    private void initializeEncounters() {
        long id = 1L;

        Encounter marketCrash = new Encounter();
        marketCrash.setId(id++);
        marketCrash.setName("Market Crash");
        marketCrash.setDescription("A sudden market crash reduces cargo values across all routes.");
        marketCrash.setDifficulty("Hard");
        marketCrash.setEncounterType("Economic");
        encounters.add(marketCrash);

        Encounter boom = new Encounter();
        boom.setId(id++);
        boom.setName("Economic Boom");
        boom.setDescription("An economic boom increases cargo demand and prices.");
        boom.setDifficulty("Easy");
        boom.setEncounterType("Economic");
        encounters.add(boom);

        Encounter contract = new Encounter();
        contract.setId(id++);
        contract.setName("Lucrative Contract");
        contract.setDescription("An AI empire offers a high-value contract for cargo delivery.");
        contract.setDifficulty("Medium");
        contract.setEncounterType("Contract");
        encounters.add(contract);

        Encounter sabotage = new Encounter();
        sabotage.setId(id++);
        sabotage.setName("Route Sabotage");
        sabotage.setDescription("An AI rival sabotages one of your trade routes.");
        sabotage.setDifficulty("Hard");
        sabotage.setEncounterType("Conflict");
        encounters.add(sabotage);

        Encounter aiRival = new Encounter();
        aiRival.setId(id++);
        aiRival.setName("AI Rival Expansion");
        aiRival.setDescription("An AI rival corporation expands into your territory.");
        aiRival.setDifficulty("Medium");
        aiRival.setEncounterType("Competition");
        encounters.add(aiRival);

        Encounter quest = new Encounter();
        quest.setId(id++);
        quest.setName("RPG Quest");
        quest.setDescription("An AI empire issues a narrative quest with rewards.");
        quest.setDifficulty("Medium");
        quest.setEncounterType("Quest");
        encounters.add(quest);

        Encounter naturalDisaster = new Encounter();
        naturalDisaster.setId(id++);
        naturalDisaster.setName("Natural Disaster");
        naturalDisaster.setDescription("A natural disaster damages infrastructure on a planet.");
        naturalDisaster.setDifficulty("Hard");
        naturalDisaster.setEncounterType("Disaster");
        encounters.add(naturalDisaster);

        Encounter technology = new Encounter();
        technology.setId(id++);
        technology.setName("Technology Breakthrough");
        technology.setDescription("A new technology improves vehicle efficiency or cargo capacity.");
        technology.setDifficulty("Easy");
        technology.setEncounterType("Technology");
        encounters.add(technology);

        Encounter war = new Encounter();
        war.setId(id++);
        war.setName("War Outbreak");
        war.setDescription("War breaks out between AI empires, disrupting trade routes.");
        war.setDifficulty("Hard");
        war.setEncounterType("Conflict");
        encounters.add(war);

        Encounter alliance = new Encounter();
        alliance.setId(id++);
        alliance.setName("Diplomatic Alliance");
        alliance.setDescription("An AI empire offers an alliance, granting access to new systems.");
        alliance.setDifficulty("Medium");
        alliance.setEncounterType("Diplomacy");
        encounters.add(alliance);
    }

    @Override
    public GameType getGameType() {
        return GameType.TRANSPORT_TYCOON;
    }

    @Override
    public String getModuleName() {
        return "Transport Tycoon";
    }

    @Override
    public DiceService getDiceService() {
        return new TycoonDiceAdapter();
    }

    @Override
    public CombatService getCombatService() {
        // Wrap the Transport Tycoon combat adapter with rules-aware combat
        CombatService delegate = new TycoonCombatAdapter();
        RulesAwareCombatAdapter rulesAware = new RulesAwareCombatAdapter(delegate, unifiedRulesQueryService);
        return rulesAware;
    }

    @Override
    public InventoryService getInventoryService() {
        return new TycoonInventoryAdapter(cargoItems);
    }

    @Override
    public CraftingService getCraftingService() {
        return new TycoonCraftingAdapter(buildingRecipes);
    }

    @Override
    public DungeonRoomService getDungeonRoomService() {
        return new TycoonDungeonRoomAdapter(rooms);
    }

    @Override
    public SpellService getSpellService() {
        return new TycoonSpellAdapter();
    }

    @Override
    public PsionicsService getPsionicsService() {
        return new TycoonPsionicsAdapter();
    }

    @Override
    public EncounterService getEncounterService() {
        return new TycoonEncounterAdapter(encounters);
    }

    @Override
    public CharacterService getCharacterService() {
        return new TycoonCharacterAdapter();
    }

    @Override
    public CampaignService getCampaignService() {
        return new TycoonCampaignAdapter(campaigns);
    }

    @Override
    public RulesService getRulesService() {
        return new TycoonRulesAdapter();
    }

    @Override
    public Set<MechanicType> getMechanics() {
        return Set.of(
                MechanicType.TYCOON,
                MechanicType.ECONOMY,
                MechanicType.REAL_ESTATE
        );
    }

    // ==================== Adapter Implementations ====================

    /**
     * Adapts Transport Tycoon dice system to DiceService interface.
     * Uses d20 for skill checks and business operations.
     */
    private static class TycoonDiceAdapter implements DiceService {

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
            // Default to d20 for business skill checks
            int result = DiceUtil.rollDSides(20);
            return new DiceRollResult(formula, List.of(result), result,
                    "Rolled " + formula + ": " + result);
        }

        @Override
        public DiceRollResult rollWithAdvantage(String formula) {
            int roll1 = DiceUtil.rollDSides(20);
            int roll2 = DiceUtil.rollDSides(20);
            int total = Math.max(roll1, roll2);
            return new DiceRollResult(formula + " (advantage)", List.of(roll1, roll2, total), total,
                    "Rolled " + formula + " with advantage: " + roll1 + ", " + roll2 + " → " + total);
        }

        @Override
        public DiceRollResult rollWithDisadvantage(String formula) {
            int roll1 = DiceUtil.rollDSides(20);
            int roll2 = DiceUtil.rollDSides(20);
            int total = Math.min(roll1, roll2);
            return new DiceRollResult(formula + " (disadvantage)", List.of(roll1, roll2, total), total,
                    "Rolled " + formula + " with disadvantage: " + roll1 + ", " + roll2 + " → " + total);
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
     * Adapts Transport Tycoon economic warfare to CombatService interface.
     * Combat in Transport Tycoon is economic: market share battles, route sabotage,
     * and AI rival competition rather than direct physical combat.
     */
    private static class TycoonCombatAdapter implements CombatService {

        @Override
        public Long createCombat(String encounterName) {
            return System.currentTimeMillis();
        }

        @Override
        public CombatInfo getCombat(Long combatId) {
            CombatInfo info = new CombatInfo();
            info.setId(combatId);
            info.setEncounterName("Market Share Battle");
            info.setStatus("ACTIVE");
            info.setCurrentRound(1);
            return info;
        }

        @Override
        public CombatantInfo addCombatant(Long combatId, String name, int armorClass, int maxHp, boolean isCharacter) {
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
            CombatInfo info = new CombatInfo();
            info.setId(combatId);
            info.setStatus("ACTIVE");
            info.setCurrentRound(1);
            return info;
        }

        @Override
        public AttackResult makeAttack(Long combatId, AttackRequest request) {
            // Economic warfare: d20 skill check against target's market defense
            int attackRoll = DiceUtil.rollDSides(20);
            int attackTotal = attackRoll + request.getAttackBonus();
            boolean hit = attackTotal >= 10; // DC 10 for economic attacks

            AttackResult result = new AttackResult();
            result.setAttackerName(request.getAttackerName());
            result.setTargetName(request.getTargetName());
            result.setAttackRoll(attackRoll);
            result.setAttackTotal(attackTotal);
            result.setHit(hit);
            result.setCriticalHit(attackRoll == 20);
            result.setCriticalFail(attackRoll == 1);

            if (hit) {
                // Damage represents market share loss (1d6 percentage points)
                int damage = DiceUtil.rollDSides(6);
                result.setDamageDealt(damage);
                result.setDamageType("economic");
                result.setMessage(request.getAttackerName() + " undermines " + request.getTargetName() + "'s market share by " + damage + "%!");
            } else {
                result.setDamageDealt(0);
                result.setMessage(request.getAttackerName() + "'s economic attack on " + request.getTargetName() + " fails.");
            }
            return result;
        }

        @Override
        public CombatantInfo healCombatant(Long combatId, String name, int amount) {
            CombatantInfo info = new CombatantInfo();
            info.setName(name);
            info.setCurrentHp(amount);
            info.setMaxHp(amount);
            info.setAlive(true);
            return info;
        }

        @Override
        public CombatInfo endCombat(Long combatId, String status) {
            CombatInfo info = new CombatInfo();
            info.setId(combatId);
            info.setStatus(status);
            return info;
        }
    }

    /**
     * Adapts Transport Tycoon cargo inventory to InventoryService interface.
     * Cargo inventory tracks raw materials, processed goods, advanced goods, and mail.
     */
    private static class TycoonInventoryAdapter implements InventoryService {
        private final Map<Long, InventoryItem> cargoItems;
        private final Map<Long, List<InventoryItem>> corporationInventories = new HashMap<>();

        TycoonInventoryAdapter(Map<Long, InventoryItem> cargoItems) {
            this.cargoItems = cargoItems;
        }

        @Override
        public List<InventoryItem> getInventory(Long characterId) {
            // Return corporation's cargo inventory
            return corporationInventories.getOrDefault(characterId, new ArrayList<>());
        }

        @Override
        public InventoryItem addItem(Long characterId, InventoryItem item) {
            // Find matching cargo type
            InventoryItem cargoTemplate = cargoItems.values().stream()
                    .filter(c -> c.getName().equals(item.getName()))
                    .findFirst()
                    .orElse(null);

            if (cargoTemplate != null) {
                item.setId(cargoTemplate.getId());
                item.setItemType(cargoTemplate.getItemType());
                item.setWeight(cargoTemplate.getWeight());
            }

            corporationInventories.computeIfAbsent(characterId, k -> new ArrayList<>()).add(item);
            return item;
        }

        @Override
        public boolean removeItem(Long itemId) {
            for (List<InventoryItem> inventory : corporationInventories.values()) {
                boolean removed = inventory.removeIf(i -> i.getId().equals(itemId));
                if (removed) return true;
            }
            return false;
        }

        @Override
        public String useItem(Long characterId, Long itemId) {
            List<InventoryItem> inventory = corporationInventories.get(characterId);
            if (inventory == null) return "No inventory found";

            Optional<InventoryItem> itemOpt = inventory.stream()
                    .filter(i -> i.getId().equals(itemId))
                    .findFirst();

            if (itemOpt.isPresent()) {
                InventoryItem item = itemOpt.get();
                return "Used " + item.getName() + " for cargo delivery.";
            }
            return "Item not found in inventory";
        }
    }

    /**
     * Adapts Transport Tycoon building operations to CraftingService interface.
     * Crafting in Transport Tycoon means building industries, infrastructure, and settlements.
     */
    private static class TycoonCraftingAdapter implements CraftingService {
        private final List<CraftingRecipe> recipes;
        private final Map<Long, CraftingProject> projects = new HashMap<>();
        private long nextProjectId = 1L;

        TycoonCraftingAdapter(List<CraftingRecipe> recipes) {
            this.recipes = recipes;
        }

        @Override
        public List<CraftingRecipe> listRecipes() {
            return recipes;
        }

        @Override
        public CraftingRecipe getRecipe(Long recipeId) {
            return recipes.stream()
                    .filter(r -> r.getId().equals(recipeId))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public CraftingProject startProject(Long characterId, Long recipeId) {
            long id = nextProjectId++;
            CraftingProject project = new CraftingProject();
            project.setId(id);
            project.setCharacterId(characterId);
            project.setRecipeId(recipeId);
            project.setStatus("STARTED");
            project.setProgress(0);
            project.setTotalProgress(100);
            projects.put(id, project);
            return project;
        }

        @Override
        public CraftingProject progressProject(Long projectId) {
            CraftingProject project = projects.get(projectId);
            if (project == null) {
                project = new CraftingProject();
                project.setId(projectId);
                project.setStatus("IN_PROGRESS");
            }
            int progress = DiceUtil.rollDSides(20) * 5; // 5-100 progress per turn
            project.setProgress(Math.min(progress, 100));
            project.setTotalProgress(100);
            if (project.getProgress() >= 100) {
                project.setStatus("COMPLETED");
            } else {
                project.setStatus("IN_PROGRESS");
            }
            projects.put(projectId, project);
            return project;
        }

        @Override
        public CraftingProject completeProject(Long projectId) {
            CraftingProject project = projects.get(projectId);
            if (project == null) {
                project = new CraftingProject();
                project.setId(projectId);
            }
            project.setStatus("COMPLETED");
            project.setProgress(100);
            project.setTotalProgress(100);
            projects.put(projectId, project);
            return project;
        }
    }

    /**
     * Adapts Transport Tycoon station and office interiors to DungeonRoomService interface.
     * Stations, corporate offices, and settlements serve as interior locations.
     */
    private static class TycoonDungeonRoomAdapter implements DungeonRoomService {
        private final Map<Long, DungeonRoom> rooms;
        private long nextRoomId;

        TycoonDungeonRoomAdapter(Map<Long, DungeonRoom> rooms) {
            this.rooms = rooms;
            this.nextRoomId = rooms.isEmpty() ? 1L : rooms.keySet().stream().mapToLong(Long::longValue).max().orElse(0) + 1;
        }

        @Override
        public DungeonRoom createRoom(DungeonRoom room) {
            long id = nextRoomId++;
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
            if (room != null) {
                room.setDiscovered(true);
            }
            return room;
        }

        @Override
        public void addConnection(Long fromId, Long toId) {
            DungeonRoom from = rooms.get(fromId);
            if (from != null) {
                String connections = from.getConnections() != null ? from.getConnections() + "," + toId : String.valueOf(toId);
                from.setConnections(connections);
            }
        }

        @Override
        public List<DungeonRoom> getConnectedRooms(Long roomId) {
            DungeonRoom room = rooms.get(roomId);
            if (room == null || room.getConnections() == null) return List.of();
            return Arrays.stream(room.getConnections().split(","))
                    .map(String::trim)
                    .map(Long::parseLong)
                    .map(rooms::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
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
            DungeonRoom room = rooms.get(roomId);
            if (room == null || room.getTrapDc() == null) {
                return new TrapCheckResult(roomId, "Unknown", null, true, false, 1, null, null);
            }
            int total = roll + dexModifier + proficiencyBonus;
            boolean success = total >= room.getTrapDc();
            boolean critFail = roll == 1;
            return new TrapCheckResult(roomId, room.getName(), room.getTrapDc(),
                    success, critFail, critFail ? 2 : 1,
                    room.getTrapDamage(), room.getTrapDamageExpr());
        }

        @Override
        public TreasureResult discoverTreasure(Long roomId) {
            DungeonRoom room = rooms.get(roomId);
            if (room == null) {
                return new TreasureResult(roomId, "Unknown", null, 0, null);
            }
            return new TreasureResult(roomId, room.getName(),
                    room.getTreasure() != null ? room.getTreasure() : "None",
                    room.getGoldPieces() != null ? room.getGoldPieces() : 0,
                    room.getMagicRarity());
        }
    }

    /**
     * Transport Tycoon has no spells — this adapter returns empty results.
     * Technology and economic advantages serve the same role.
     */
    private static class TycoonSpellAdapter implements SpellService {

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
                    "Transport Tycoon does not have spells. Use technology and economic advantages instead.", 0);
        }
    }

    /**
     * Transport Tycoon has no psionics — this adapter returns empty results.
     * Corporate intelligence and market analysis serve the same role.
     */
    private static class TycoonPsionicsAdapter implements PsionicsService {

        @Override
        public List<PsionicPower> listPowers() {
            return List.of();
        }

        @Override
        public PsionicPower getPower(Long powerId) {
            return null;
        }

        @Override
        public PsionicPowerResult usePower(Long characterId, Long powerId, Long targetId) {
            return new PsionicPowerResult(powerId, "Unknown", false,
                    "Transport Tycoon does not have psionics. Use corporate intelligence instead.", 0);
        }
    }

    /**
     * Adapts Transport Tycoon market events and RPG quests to EncounterService interface.
     */
    private static class TycoonEncounterAdapter implements EncounterService {
        private final List<Encounter> encounters;

        TycoonEncounterAdapter(List<Encounter> encounters) {
            this.encounters = encounters;
        }

        @Override
        public List<Encounter> listEncounters() {
            return encounters;
        }

        @Override
        public Encounter getEncounter(Long encounterId) {
            return encounters.stream()
                    .filter(e -> e.getId().equals(encounterId))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public Encounter generateRandomEncounter(String areaId, int level) {
            int index = DiceUtil.rollIndex(encounters.size());
            Encounter encounter = encounters.get(index);
            Encounter randomEncounter = new Encounter();
            randomEncounter.setId(encounter.getId());
            randomEncounter.setName(encounter.getName());
            randomEncounter.setDescription(encounter.getDescription());
            randomEncounter.setDifficulty(encounter.getDifficulty());
            randomEncounter.setEncounterType(encounter.getEncounterType());
            randomEncounter.setAreaId(areaId);
            return randomEncounter;
        }

        @Override
        public EncounterResult resolveEncounter(Long encounterId, List<Long> characterIds) {
            Encounter encounter = getEncounter(encounterId);
            if (encounter == null) {
                return new EncounterResult(encounterId, "Unknown", false, "Encounter not found", 0);
            }
            // Resolve with d20 check per corporation
            int totalRoll = 0;
            for (Long charId : characterIds) {
                totalRoll += DiceUtil.rollDSides(20);
            }
            boolean success = totalRoll >= characterIds.size() * 10;
            int xpReward = success ? 100 : 25;
            return new EncounterResult(encounterId, encounter.getName(), success,
                    success ? "Encounter resolved successfully!" : "Encounter failed.",
                    xpReward);
        }
    }

    /**
     * Adapts Transport Tycoon corporations to CharacterService interface.
     * Corporations serve as the "characters" in Transport Tycoon, with rank progression
     * from Freelancer to Galactic Monopoly.
     */
    private static class TycoonCharacterAdapter implements CharacterService {
        private final Map<Long, CharacterInfo> corporations = new HashMap<>();
        private long nextCorpId = 1L;

        @Override
        public CharacterInfo getCharacter(Long characterId) {
            return corporations.get(characterId);
        }

        @Override
        public List<CharacterInfo> listCharacters() {
            return new ArrayList<>(corporations.values());
        }

        @Override
        public void updateAbilityScores(Long characterId, int str, int dex, int con, int intelligence, int wis, int cha) {
            CharacterInfo corp = corporations.get(characterId);
            if (corp != null) {
                corp.setStrScore(str);
                corp.setDexScore(dex);
                corp.setConScore(con);
                corp.setIntScore(intelligence);
                corp.setWisScore(wis);
                corp.setChaScore(cha);
            }
        }

        @Override
        public CharacterInfo awardExperience(Long characterId, int xp) {
            CharacterInfo corp = corporations.get(characterId);
            if (corp != null) {
                corp.setExperiencePoints(corp.getExperiencePoints() + xp);
                // Check rank progression
                if (corp.getExperiencePoints() >= 100) {
                    corp.setLevel(Math.min(corp.getLevel() + 1, 5));
                }
            }
            return corp;
        }

        /**
         * Creates a new corporation character.
         */
        public CharacterInfo createCorporation(String name, String ownerId) {
            long id = nextCorpId++;
            CharacterInfo corp = new CharacterInfo();
            corp.setId(id);
            corp.setName(name);
            corp.setLevel(1);
            corp.setExperiencePoints(0);
            corp.setHitPoints(5000);
            corp.setMaxHitPoints(5000);
            corp.setStrScore(10);
            corp.setDexScore(10);
            corp.setConScore(10);
            corp.setIntScore(10);
            corp.setWisScore(10);
            corp.setChaScore(10);
            corp.setLocation("Freelancer");
            corporations.put(id, corp);
            return corp;
        }
    }

    /**
     * Adapts Transport Tycoon campaigns to CampaignService interface.
     * Campaigns are turn-based with objectives, market share goals, and win conditions.
     */
    private static class TycoonCampaignAdapter implements CampaignService {
        private final Map<Long, Campaign> campaigns;

        TycoonCampaignAdapter(Map<Long, Campaign> campaigns) {
            this.campaigns = campaigns;
        }

        @Override
        public List<Campaign> listCampaigns() {
            return new ArrayList<>(campaigns.values());
        }

        @Override
        public Campaign getCampaign(Long campaignId) {
            return campaigns.get(campaignId);
        }

        @Override
        public Campaign createCampaign(String name, String description) {
            long id = System.currentTimeMillis();
            Campaign campaign = new Campaign();
            campaign.setId(id);
            campaign.setName(name);
            campaign.setDescription(description);
            campaign.setStatus("ACTIVE");
            campaign.setCurrentSession(0);
            campaigns.put(id, campaign);
            return campaign;
        }

        @Override
        public void addCharacterToCampaign(Long campaignId, Long characterId) {
            Campaign campaign = campaigns.get(campaignId);
            if (campaign != null) {
                campaign.setCurrentSession(campaign.getCurrentSession() + 1);
            }
        }
    }

    /**
     * Adapts Transport Tycoon trade rules to RulesService interface.
     */
    private static class TycoonRulesAdapter implements RulesService {

        @Override
        public List<RulesEntry> listRules() {
            List<RulesEntry> entries = new ArrayList<>();

            RulesEntry trade = new RulesEntry();
            trade.setId(1L);
            trade.setTitle("Trade Route Revenue");
            trade.setCategory("Trade");
            trade.setText("Revenue per turn is calculated as: cargo value × cargo per turn × distance modifier. Operating costs reduce net profit. Route profitability depends on cargo demand at origin and destination.");
            trade.setSource("Transport Tycoon Rules");
            entries.add(trade);

            RulesEntry marketShare = new RulesEntry();
            marketShare.setId(2L);
            marketShare.setTitle("Market Share Calculation");
            marketShare.setCategory("Market");
            marketShare.setText("Market share is calculated as: (corporation's cargo value delivered) / (total cargo value delivered by all corporations). Achieving 60% market share for 3 consecutive turns wins the campaign.");
            marketShare.setSource("Transport Tycoon Rules");
            entries.add(marketShare);

            RulesEntry cargo = new RulesEntry();
            cargo.setId(3L);
            cargo.setTitle("Cargo Flow System");
            cargo.setCategory("Cargo");
            cargo.setText("Cargo flows through the economy: primary industries produce raw materials, secondary industries process them, tertiary industries create advanced goods. Settlements consume consumer goods and produce mail.");
            cargo.setSource("Transport Tycoon Rules");
            entries.add(cargo);

            RulesEntry contracts = new RulesEntry();
            contracts.setId(4L);
            contracts.setTitle("Contract Execution");
            contracts.setCategory("Contracts");
            contracts.setText("Contracts are issued by AI empires for building settlements, industries, infrastructure, and deploying vehicles. Each contract has a payment, difficulty, and duration. Completing contracts earns credits and XP.");
            contracts.setSource("Transport Tycoon Rules");
            entries.add(contracts);

            RulesEntry rank = new RulesEntry();
            rank.setId(5L);
            rank.setTitle("Corporation Rank Progression");
            rank.setCategory("Progression");
            rank.setText("Corporations progress through ranks: Freelancer → Startup → Regional Carrier → Interplanetary Logistics → Galactic Monopoly. Each rank unlocks new capabilities like larger vehicles and more planets.");
            rank.setSource("Transport Tycoon Rules");
            entries.add(rank);

            RulesEntry ai = new RulesEntry();
            ai.setId(6L);
            ai.setTitle("AI Rival Strategies");
            ai.setCategory("AI");
            ai.setText("AI rivals have personalities: Aggressive (high spending, high risk), Balanced (moderate approach), Cautious (high savings, low risk), Route Optimizer (focus on efficiency). They compete for contracts and market share.");
            ai.setSource("Transport Tycoon Rules");
            entries.add(ai);

            RulesEntry quests = new RulesEntry();
            quests.setId(7L);
            quests.setTitle("RPG Quest System");
            quests.setCategory("Quests");
            quests.setText("AI empires issue quests based on their needs: delivery, construction, military logistics, diplomacy, exploration, sabotage, trade embargo, and information gathering. Completing quests earns credits, XP, and reputation.");
            quests.setSource("Transport Tycoon Rules");
            entries.add(quests);

            RulesEntry vehicles = new RulesEntry();
            vehicles.setId(8L);
            vehicles.setTitle("Vehicle Types");
            vehicles.setCategory("Vehicles");
            vehicles.setText("Vehicles range from trucks and trains to ships and aircraft. Each has cargo capacity, speed, and operating cost. Higher tech levels unlock more efficient vehicles with greater capacity.");
            vehicles.setSource("Transport Tycoon Rules");
            entries.add(vehicles);

            // ---- OpenTTD Extended Vehicle Entries ----

            RulesEntry steamTrain = new RulesEntry();
            steamTrain.setId(9L);
            steamTrain.setTitle("Steam Locomotive (NUTS)");
            steamTrain.setCategory("Vehicles");
            steamTrain.setText("Classic steam-powered locomotive for early industrial planets (tech level 1-4). Speed: 4 hexes/turn, cargo: 50, passengers: 40, cost: 300. Variants: NUTS Super-Strong Steam (heavy freight), Strong Steam (balanced), Medium Steam (faster passenger).");
            steamTrain.setSource("OpenTTD - NUTS");
            entries.add(steamTrain);

            RulesEntry dieselTrain = new RulesEntry();
            dieselTrain.setId(10L);
            dieselTrain.setTitle("Diesel Locomotive (NUTS)");
            dieselTrain.setCategory("Vehicles");
            dieselTrain.setText("Diesel-powered locomotive with improved speed and reliability (tech level 2-5). Speed: 6 hexes/turn, cargo: 80, passengers: 60, cost: 450. Variants: Super-Strong Diesel (maximum freight), Fast Diesel (high-speed passenger).");
            dieselTrain.setSource("OpenTTD - NUTS");
            entries.add(dieselTrain);

            RulesEntry electricTrain = new RulesEntry();
            electricTrain.setId(11L);
            electricTrain.setTitle("Electric Locomotive (NUTS)");
            electricTrain.setCategory("Vehicles");
            electricTrain.setText("Electric train requiring electrified railway (tech level 3-6, available 1965). Speed: 8 hexes/turn, cargo: 100, passengers: 80, cost: 600. Clean, efficient transport for industrial planets.");
            electricTrain.setSource("OpenTTD - NUTS");
            entries.add(electricTrain);

            RulesEntry monorailTrain = new RulesEntry();
            monorailTrain.setId(12L);
            monorailTrain.setTitle("Monorail Train (NUTS)");
            monorailTrain.setCategory("Vehicles");
            monorailTrain.setText("Advanced monorail system for high-tech planets (tech level 4-7, available 1999). Speed: 10 hexes/turn, cargo: 120, passengers: 100, cost: 800. Elevated tracks reduce land use. Requires MONORAIL infrastructure.");
            monorailTrain.setSource("OpenTTD - NUTS");
            entries.add(monorailTrain);

            RulesEntry maglevTrain = new RulesEntry();
            maglevTrain.setId(13L);
            maglevTrain.setTitle("Maglev Train (Modern Maglev)");
            maglevTrain.setCategory("Vehicles");
            maglevTrain.setText("Magnetic levitation train — pinnacle of conventional rail (tech level 5-8, available 2022). Speed: 12 hexes/turn, cargo: 150, passengers: 120, cost: 1000. Variants: Chinese Maglev (high-speed passenger), Japanese SCMaglev (ultra-high speed), German Transrapid (balanced), Korean Maglev (cost-effective). Requires MAGLEV infrastructure.");
            maglevTrain.setSource("OpenTTD - Modern Maglev");
            entries.add(maglevTrain);

            RulesEntry frimsMaglev = new RulesEntry();
            frimsMaglev.setId(14L);
            frimsMaglev.setTitle("FRIMS Futuristic Maglev");
            frimsMaglev.setCategory("Vehicles");
            frimsMaglev.setText("Futuristic maglev trains inspired by 'Future Reality' (tech level 6-9). Speed: 15 hexes/turn, cargo: 200, passengers: 150, cost: 1500. Sleek designs for space-faring civilizations. Variants: FRIMS Express (ultra-fast intercity), FRIMS Freight (heavy cargo), FRIMS Luxury (premium passenger).");
            frimsMaglev.setSource("OpenTTD - FRIMS");
            entries.add(frimsMaglev);

            RulesEntry helicopter = new RulesEntry();
            helicopter.setId(15L);
            helicopter.setTitle("Helicopter");
            helicopter.setCategory("Vehicles");
            helicopter.setText("Short-range helicopter transport (tech level 4-7). Speed: 6 hexes/turn, cargo: 5, passengers: 5, cost: 400. Can operate with minimal infrastructure. Flexible for emergency and short-distance routes.");
            helicopter.setSource("OpenTTD Core");
            entries.add(helicopter);

            RulesEntry fictionalAircraft = new RulesEntry();
            fictionalAircraft.setId(16L);
            fictionalAircraft.setTitle("Fictional Aircraft (Kaaskrokets)");
            fictionalAircraft.setCategory("Vehicles");
            fictionalAircraft.setText("Fictional aircraft and spacecraft designs for advanced transport (tech level 7-9). Speed: 15 hexes/turn, cargo: 20, passengers: 20, cost: 1500. Includes actual spacecraft for gameplay. Variants: Spacecraft Transport (atmospheric and space-capable), Orbital Shuttle (low orbit), Planetary Express (high-speed atmospheric).");
            fictionalAircraft.setSource("OpenTTD - Kaaskrokets Fictional Aircraft");
            entries.add(fictionalAircraft);

            RulesEntry rocket = new RulesEntry();
            rocket.setId(17L);
            rocket.setTitle("Rocket (SpaceY)");
            rocket.setCategory("Vehicles");
            rocket.setText("Space-themed rocket for orbital and interplanetary travel (tech level 8-10). Speed: 20 hexes/turn, cargo: 30, passengers: 10, cost: 3000. In OpenTTD, rockets are coded as helicopters. Variants: Falcon 9 Style (reusable orbital), Heavy Lifter (deep space cargo), Crew Dragon Style (passenger space transport). Requires SPACEPORT infrastructure.");
            rocket.setSource("OpenTTD - SpaceY");
            entries.add(rocket);

            RulesEntry apolloRocket = new RulesEntry();
            apolloRocket.setId(18L);
            apolloRocket.setTitle("Apollo Rocket (Apollo Rocket Set)");
            apolloRocket.setCategory("Vehicles");
            apolloRocket.setText("Full space logistics rocket for interplanetary travel (tech level 9-10). Speed: 25 hexes/turn, cargo: 50, passengers: 15, cost: 5000. Production chain: Rocket Parts Factory → Spacecraft Assembly Plant → Lander Factory → Rocket Launch Pad. Released for 50th anniversary of Apollo 11.");
            apolloRocket.setSource("OpenTTD - Apollo Rocket Industry Set");
            entries.add(apolloRocket);

            RulesEntry spaceStation = new RulesEntry();
            spaceStation.setId(19L);
            spaceStation.setTitle("Orbital Space Station");
            spaceStation.setCategory("Vehicles");
            spaceStation.setText("Orbital station serving as a hub for interplanetary transport (tech level 10). Station-type vehicle: speed 0, cargo capacity 500, passenger capacity 100, cost: 10000. Acts as a station in space for refueling and transfers. Requires ORBITAL_STATION infrastructure.");
            spaceStation.setSource("OpenTTD - Community / DoraMCU");
            entries.add(spaceStation);

            RulesEntry advancedLiner = new RulesEntry();
            advancedLiner.setId(20L);
            advancedLiner.setTitle("Advanced Cargo Liner");
            advancedLiner.setCategory("Vehicles");
            advancedLiner.setText("High-capacity container ship for interplanetary trade routes on ocean worlds (tech level 4-8). Speed: 3 hexes/turn, cargo: 200, cost: 800. Essential for bulk cargo transport across water hexes on ocean planets.");
            advancedLiner.setSource("OpenTTD - NUTS / Community");
            entries.add(advancedLiner);

            // ---- OpenTTD Station Entries ----

            RulesEntry busStop = new RulesEntry();
            busStop.setId(21L);
            busStop.setTitle("Bus Stop Station");
            busStop.setCategory("Stations");
            busStop.setText("Simple shelter with bench for road vehicle stops. Interior rooms: Waiting Area, Ticket Booth. Catchment area: 3 hexes. Lowest cost station for connecting settlements to road networks.");
            busStop.setSource("OpenTTD - NewStations");
            entries.add(busStop);

            RulesEntry railStation = new RulesEntry();
            railStation.setId(22L);
            railStation.setTitle("Rail Station (NewStations)");
            railStation.setCategory("Stations");
            railStation.setText("Platform with tracks and animated platforms — passengers gather as cargo waits. Interior rooms: Waiting Hall, Ticket Office, Platform, Control Room, Storage. Most popular station replacement set by Michael Blunck. Catchment area: 5 hexes.");
            railStation.setSource("OpenTTD - NewStations");
            entries.add(railStation);

            RulesEntry maglevStation = new RulesEntry();
            maglevStation.setId(23L);
            maglevStation.setTitle("Maglev Station Set");
            maglevStation.setCategory("Stations");
            maglevStation.setText("Modern, sleek station designs from Chinese maglev lines and Braunschweig test track. For advanced planets with maglev infrastructure. Interior rooms: Waiting Hall, Platform, Control Room. Catchment area: 7 hexes.");
            maglevStation.setSource("OpenTTD - Maglev Station Set");
            entries.add(maglevStation);

            RulesEntry airport = new RulesEntry();
            airport.setId(24L);
            airport.setTitle("Airport Station");
            airport.setCategory("Stations");
            airport.setText("Runway with terminal building for aircraft operations. Interior rooms: Check-in Hall, Security, Gate Area, Control Tower, Hangar, Maintenance Bay. Required for all air vehicles. Catchment area: 10 hexes.");
            airport.setSource("OpenTTD Core");
            entries.add(airport);

            RulesEntry spaceport = new RulesEntry();
            spaceport.setId(25L);
            spaceport.setTitle("Space Port Station");
            spaceport.setCategory("Stations");
            spaceport.setText("Advanced space docking facility for rocket and spacecraft operations. Interior rooms: Check-in, Customs, Docking Bay, Hangar, Command Center, Fuel Station. Required for space vehicles. Catchment area: 15 hexes. Tech level 8+.");
            spaceport.setSource("OpenTTD - SpaceY / DoraMCU");
            entries.add(spaceport);

            RulesEntry orbitalStation = new RulesEntry();
            orbitalStation.setId(26L);
            orbitalStation.setTitle("Orbital Station");
            orbitalStation.setCategory("Stations");
            orbitalStation.setText("Space-based station for interplanetary transport hub. Interior rooms: Command Center, Docking Bays, Life Support, Cargo Hold, Crew Quarters, Observation Deck. Serves as transfer point between planets. Tech level 10.");
            orbitalStation.setSource("OpenTTD - Community / DoraMCU");
            entries.add(orbitalStation);

            // ---- OpenTTD Infrastructure Entries ----

            RulesEntry roadInfra = new RulesEntry();
            roadInfra.setId(27L);
            roadInfra.setTitle("Road Infrastructure");
            roadInfra.setCategory("Infrastructure");
            roadInfra.setText("Basic road network allowing truck and bus movement. Available from tech level 1. Movement cost: 1.0 per hex. Foundation for all land transport. Can be upgraded to higher-tech infrastructure as planet develops.");
            roadInfra.setSource("OpenTTD Core");
            entries.add(roadInfra);

            RulesEntry railInfra = new RulesEntry();
            railInfra.setId(28L);
            railInfra.setTitle("Rail Infrastructure");
            railInfra.setCategory("Infrastructure");
            railInfra.setText("Normal railway for steam, diesel, and electric trains. Available from tech level 1. Can be electrified for electric trains (tech level 3+). Four rail types: Normal, Electrified, Monorail, Maglev — each requiring specific vehicles.");
            railInfra.setSource("OpenTTD Core");
            entries.add(railInfra);

            RulesEntry monorailInfra = new RulesEntry();
            monorailInfra.setId(29L);
            monorailInfra.setTitle("Monorail Infrastructure");
            monorailInfra.setCategory("Infrastructure");
            monorailInfra.setText("Elevated monorail track for high-tech planets (tech level 4+). Reduces land use compared to conventional rail. Only monorail vehicles can use this infrastructure. Available from 1999 in OpenTTD.");
            monorailInfra.setSource("OpenTTD Core");
            entries.add(monorailInfra);

            RulesEntry maglevInfra = new RulesEntry();
            maglevInfra.setId(30L);
            maglevInfra.setTitle("Maglev Infrastructure");
            maglevInfra.setCategory("Infrastructure");
            maglevInfra.setText("Magnetic levitation track for advanced planets (tech level 5+). Fastest ground transport infrastructure. Only maglev vehicles can use this track. Available from 2022 in OpenTTD. Custom maglev track graphics available.");
            maglevInfra.setSource("OpenTTD Core / Maglev Track Set");
            entries.add(maglevInfra);

            RulesEntry spaceportInfra = new RulesEntry();
            spaceportInfra.setId(31L);
            spaceportInfra.setTitle("Spaceport Infrastructure");
            spaceportInfra.setCategory("Infrastructure");
            spaceportInfra.setText("Rocket launch and landing facility for space vehicles (tech level 8+). Required for rockets and spacecraft. Includes launch pads, fueling stations, and assembly buildings. Apollo Rocket Set includes full production chain industries.");
            spaceportInfra.setSource("OpenTTD - SpaceY / Apollo Rocket Set");
            entries.add(spaceportInfra);

            // ---- OpenTTD Tech Level Entries ----

            RulesEntry techLevel1 = new RulesEntry();
            techLevel1.setId(32L);
            techLevel1.setTitle("Tech Level 1-2: Primitive Era");
            techLevel1.setCategory("Tech Levels");
            techLevel1.setText("Early industrial planets with steam locomotives, trucks, and buses. Stations: bus stops, basic rail stations. Industries: mines, farms, logging. Recommended NewGRF: NUTS (steam/diesel). Landscape: default Earth-like terrain.");
            techLevel1.setSource("OpenTTD - Tech Progression");
            entries.add(techLevel1);

            RulesEntry techLevel3 = new RulesEntry();
            techLevel3.setId(33L);
            techLevel3.setTitle("Tech Level 3-4: Industrial Era");
            techLevel3.setCategory("Tech Levels");
            techLevel3.setText("Mid-industrial planets with diesel/electric trains, ships, and helicopters. Stations: rail stations, ports. Industries: steel mills, refineries, chemical plants. Recommended NewGRF: NUTS (electric), NewStations. Infrastructure: electrified rail.");
            techLevel3.setSource("OpenTTD - Tech Progression");
            entries.add(techLevel3);

            RulesEntry techLevel5 = new RulesEntry();
            techLevel5.setId(34L);
            techLevel5.setTitle("Tech Level 5-6: Advanced Era");
            techLevel5.setCategory("Tech Levels");
            techLevel5.setText("Advanced planets with maglev trains and aircraft. Stations: maglev stations, airports. Industries: electronics factories, machine shops. Recommended NewGRF: FRIMS Maglevs, Modern Maglev. Infrastructure: monorail, maglev tracks.");
            techLevel5.setSource("OpenTTD - Tech Progression");
            entries.add(techLevel5);

            RulesEntry techLevel7 = new RulesEntry();
            techLevel7.setId(35L);
            techLevel7.setTitle("Tech Level 7-8: Space-faring Era");
            techLevel7.setCategory("Tech Levels");
            techLevel7.setText("Space-faring planets with advanced aircraft, fictional spacecraft, and rockets. Stations: airports, rocket pads. Industries: rocket parts factories, spacecraft assembly. Recommended NewGRF: Kaaskrokets Fictional Aircraft, SpaceY. Infrastructure: spaceports.");
            techLevel7.setSource("OpenTTD - Tech Progression");
            entries.add(techLevel7);

            RulesEntry techLevel9 = new RulesEntry();
            techLevel9.setId(36L);
            techLevel9.setTitle("Tech Level 9-10: Interplanetary Era");
            techLevel9.setCategory("Tech Levels");
            techLevel9.setText("Interplanetary civilizations with spacecraft, space stations, and full Apollo production chains. Stations: spaceports, orbital stations. Industries: lander factories, rocket launch pads. Recommended NewGRF: Apollo Rocket Set, FRIMS. Landscape: OpenGFX Mars.");
            techLevel9.setSource("OpenTTD - Tech Progression");
            entries.add(techLevel9);

            // ---- OpenTTD Landscape Entries ----

            RulesEntry marsLandscape = new RulesEntry();
            marsLandscape.setId(37L);
            marsLandscape.setTitle("Mars Landscape (OpenGFX Mars)");
            marsLandscape.setCategory("Landscapes");
            marsLandscape.setText("Mars-themed landscape with Martian counterparts for all climates: Temperate = Equatorial Mars, Arctic = Northern/Southern Mars, Tropic = Equatorial with Desert, Toyland = Arctic (permanently frozen). Martian town names and industry modifications available.");
            marsLandscape.setSource("OpenTTD - OpenGFX Mars");
            entries.add(marsLandscape);

            RulesEntry earthLandscape = new RulesEntry();
            earthLandscape.setId(38L);
            earthLandscape.setTitle("Earth Landscape (Default)");
            earthLandscape.setCategory("Landscapes");
            earthLandscape.setText("Default Earth-like landscape with temperate, arctic, tropical, and toyland climates. Supports all terrain types: grassland, desert, tundra, jungle, savanna. Standard town names and industries.");
            earthLandscape.setSource("OpenTTD Core");
            entries.add(earthLandscape);

            // ---- OpenTTD Cargo Chain Entries ----

            RulesEntry cargoChain = new RulesEntry();
            cargoChain.setId(39L);
            cargoChain.setTitle("Cargo Production Chains");
            cargoChain.setCategory("Cargo");
            cargoChain.setText("Primary → Secondary → Tertiary chain: Mine→Ore→Steel Mill→Steel→Electronics Factory→Electronics. Coal Mine→Coal→Cement Plant→Concrete. Oil Well→Oil→Refinery→Refined Oil→Chemical Plant→Plastic. Space chain: Steel+Electronics→Rocket Parts→Spacecraft→Lander→Launch.");
            cargoChain.setSource("OpenTTD - Cargo System");
            entries.add(cargoChain);

            RulesEntry spaceCargo = new RulesEntry();
            spaceCargo.setId(40L);
            spaceCargo.setTitle("Space Rocket Production Chain");
            spaceCargo.setCategory("Cargo");
            spaceCargo.setText("Full space logistics chain (Apollo Rocket Set): Steel + Electronics → Rocket Parts Factory → Rocket Parts → Spacecraft Assembly Plant → Spacecraft → Lander Factory → Lander → Rocket Launch Pad → Launch. Each stage requires specific cargo inputs and produces intermediate goods.");
            spaceCargo.setSource("OpenTTD - Apollo Rocket Industry Set");
            entries.add(spaceCargo);

            return entries;
        }

        @Override
        public List<RulesEntry> searchRules(String keyword) {
            return listRules().stream()
                    .filter(r -> r.getTitle().toLowerCase().contains(keyword.toLowerCase()) ||
                            r.getText().toLowerCase().contains(keyword.toLowerCase()) ||
                            r.getCategory().toLowerCase().contains(keyword.toLowerCase()))
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
