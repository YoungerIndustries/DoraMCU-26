package com.yisy.modules.doramcu.traveller.config;
import com.yisy.modules.doramcu.sfb.service.SfbDiplomacyService.FactionProfile;
import com.yisy.modules.doramcu.sfb.service.SfbDiplomacyService.RelationType;
import com.yisy.modules.doramcu.sfb.service.SfbEconomyService.FactionEconomy;
import com.yisy.modules.doramcu.sfb.service.SfbEconomyService.ResourceType;

import com.yisy.modules.doramcu.gamecore.AbstractGameModule;
import com.yisy.modules.doramcu.gamecore.BuildingInstanceService;
import com.yisy.modules.doramcu.gamecore.CampaignService;
import com.yisy.modules.doramcu.gamecore.CampaignService.Campaign;
import com.yisy.modules.doramcu.gamecore.CharacterService;
import com.yisy.modules.doramcu.gamecore.CraftingService;
import com.yisy.modules.doramcu.gamecore.CombatService;
import com.yisy.modules.doramcu.gamecore.DiceService;
import com.yisy.modules.doramcu.gamecore.DiceService.DiceRollResult;
import com.yisy.modules.doramcu.gamecore.DungeonRoomService;
import com.yisy.modules.doramcu.gamecore.EncounterService;
import com.yisy.modules.doramcu.gamecore.EncounterService.Encounter;
import com.yisy.modules.doramcu.gamecore.GameModuleRegistry;
import com.yisy.modules.doramcu.gamecore.GameType;
import com.yisy.modules.doramcu.gamecore.InventoryService;
import com.yisy.modules.doramcu.gamecore.InventoryService.InventoryItem;
import com.yisy.modules.doramcu.gamecore.MechanicType;
import com.yisy.modules.doramcu.gamecore.PsionicsService;
import com.yisy.modules.doramcu.gamecore.PsionicsService.PsionicPower;
import com.yisy.modules.doramcu.gamecore.RealEstateService;
import com.yisy.modules.doramcu.gamecore.RulesEngine;
import com.yisy.modules.doramcu.gamecore.RulesService;
import com.yisy.modules.doramcu.gamecore.SpellService;
import com.yisy.modules.doramcu.gamecore.SpellService.Spell;
import com.yisy.modules.doramcu.shared.model.RulesEntry;
import com.yisy.modules.doramcu.shared.service.RulesAwareCombatAdapter;
import com.yisy.modules.doramcu.shared.service.UnifiedRulesQueryService;
import com.yisy.modules.doramcu.service.battle.FirearmsCombatService;
import com.yisy.modules.doramcu.service.battle.MeleeCombatService;
import com.yisy.modules.doramcu.service.character.EncumbranceService;
import com.yisy.modules.doramcu.techlevel.TechLevelAdapter;
import com.yisy.modules.doramcu.traveller.AbilityScore;
import com.yisy.modules.doramcu.traveller.TravellerAdventureService;
import com.yisy.modules.doramcu.traveller.TravellerCharacter;
import com.yisy.modules.doramcu.traveller.TravellerRulesService;
import com.yisy.modules.doramcu.traveller.service.TravellerBuildingInstanceService;
import com.yisy.modules.doramcu.traveller.service.TravellerRealEstateService;
import com.yisy.modules.doramcu.traveller.service.TravellerRulesEngine;
import com.yisy.modules.doramcu.util.DiceUtil;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Traveller Game Module adapter.
 * <p>
 * Implements the {@link GameModule} interface to register Traveller features
 * with the {@link GameModuleRegistry}. This allows Earth2026 and other modules
 * to access Traveller features through the shared game-core interfaces.
 * </p>
 * <p>
 * Traveller provides:
 * <ul>
 *   <li>Dice: 2D6 bell-curve system</li>
 *   <li>Combat: Firearms, Melee, Air, Naval, Ship-to-Ship</li>
 *   <li>Inventory: Encumbrance-based system</li>
 *   <li>Psionics: Psionic disciplines and powers</li>
 *   <li>Characters: Classic Traveller character generation</li>
 *   <li>Adventures: Structured missions and campaigns</li>
 *   <li>Rules: Full Classic Traveller rules database</li>
 *   <li>Worlds: Planet and sector generation</li>
 *   <li>Trade: Commodity trading and smuggling</li>
 * </ul>
 * </p>
 */
@Component
public class TravellerGameModule extends AbstractGameModule {

    private final com.yisy.modules.doramcu.service.character.CharacterService characterService;
    private final FirearmsCombatService firearmsCombatService;
    private final MeleeCombatService meleeCombatService;
    private final EncumbranceService encumbranceService;
    private final TravellerRulesService travellerRulesService;
    private final TravellerAdventureService travellerAdventureService;
    private final TravellerTechLevelAdapter techLevelAdapter;
    private final TravellerRealEstateService realEstateService;
    private final TravellerBuildingInstanceService buildingInstanceService;
    private final UnifiedRulesQueryService unifiedRulesQueryService;
    private final TravellerRulesEngine rulesEngine;

    /** Cached map of all known psionic powers from Traveller rules. */
    private final Map<Long, PsionicsService.PsionicPower> psionicPowers = new HashMap<>();

    public TravellerGameModule(
            com.yisy.modules.doramcu.service.character.CharacterService characterService,
            FirearmsCombatService firearmsCombatService,
            MeleeCombatService meleeCombatService,
            EncumbranceService encumbranceService,
            TravellerRulesService travellerRulesService,
            TravellerAdventureService travellerAdventureService,
            GameModuleRegistry gameModuleRegistry,
            TravellerTechLevelAdapter techLevelAdapter,
            TravellerRealEstateService realEstateService,
            TravellerBuildingInstanceService buildingInstanceService,
            UnifiedRulesQueryService unifiedRulesQueryService,
            TravellerRulesEngine rulesEngine) {
        super(gameModuleRegistry);
        this.characterService = characterService;
        this.firearmsCombatService = firearmsCombatService;
        this.meleeCombatService = meleeCombatService;
        this.encumbranceService = encumbranceService;
        this.travellerRulesService = travellerRulesService;
        this.travellerAdventureService = travellerAdventureService;
        this.techLevelAdapter = techLevelAdapter;
        this.realEstateService = realEstateService;
        this.buildingInstanceService = buildingInstanceService;
        this.unifiedRulesQueryService = unifiedRulesQueryService;
        this.rulesEngine = rulesEngine;
        initializePsionicPowers();
    }

    /**
     * Initializes the list of known Traveller psionic powers.
     */
    private void initializePsionicPowers() {
        // Traveller psionic disciplines
        addPsionicPower(1L, "Telepathy", "Psychic", 1, 1, "Touch", "Concentration",
                "Read surface thoughts of a target creature.");
        addPsionicPower(2L, "Telekinesis", "Psychic", 2, 2, "30 feet", "Concentration",
                "Move objects with your mind.");
        addPsionicPower(3L, "Precognition", "Psychic", 3, 3, "Self", "Instant",
                "Gain insight into possible future events.");
        addPsionicPower(4L, "Psychokinesis", "Psychic", 4, 4, "60 feet", "Concentration",
                "Exert physical force through mental power.");
        addPsionicPower(5L, "Metabolism Control", "Psychic", 2, 1, "Self", "1 hour",
                "Control bodily functions like heart rate and breathing.");
        addPsionicPower(6L, "Probability Control", "Psychic", 5, 5, "Self", "Instant",
                "Influence the outcome of random events.");
        addPsionicPower(7L, "Clairsentience", "Psychic", 3, 2, "Unlimited", "Concentration",
                "Perceive events at a distance.");
        addPsionicPower(8L, "Psychometabolism", "Psychic", 4, 3, "Touch", "Concentration",
                "Alter the physical properties of matter.");
    }

    private void addPsionicPower(Long id, String name, String discipline, int level, int cost,
                                  String range, String duration, String description) {
        PsionicsService.PsionicPower power = new PsionicsService.PsionicPower();
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
    public GameType getGameType() {
        return GameType.TRAVELLER;
    }

    @Override
    public String getModuleName() {
        return "Classic Traveller";
    }

    @Override
    public DiceService getDiceService() {
        return new TravellerDiceAdapter();
    }

    @Override
    public CombatService getCombatService() {
        // Wrap the Traveller combat adapter with rules-aware combat
        CombatService delegate = new TravellerCombatAdapter(firearmsCombatService, meleeCombatService);
        RulesAwareCombatAdapter rulesAware = new RulesAwareCombatAdapter(delegate, unifiedRulesQueryService);
        return rulesAware;
    }

    @Override
    public InventoryService getInventoryService() {
        return new TravellerInventoryAdapter(encumbranceService, characterService);
    }

    @Override
    public CraftingService getCraftingService() {
        return new TravellerCraftingAdapter();
    }

    @Override
    public DungeonRoomService getDungeonRoomService() {
        return new TravellerDungeonRoomAdapter();
    }

    @Override
    public SpellService getSpellService() {
        return new TravellerSpellAdapter();
    }

    @Override
    public PsionicsService getPsionicsService() {
        return new TravellerPsionicsAdapter(psionicPowers);
    }

    @Override
    public EncounterService getEncounterService() {
        return new TravellerEncounterAdapter(travellerAdventureService);
    }

    @Override
    public com.yisy.modules.doramcu.gamecore.CharacterService getCharacterService() {
        return new TravellerCharacterAdapter(characterService);
    }

    @Override
    public CampaignService getCampaignService() {
        return new TravellerCampaignAdapter(travellerAdventureService);
    }

    @Override
    public RulesService getRulesService() {
        return new TravellerRulesAdapter(travellerRulesService);
    }

    @Override
    public TechLevelAdapter getTechLevelAdapter() {
        return techLevelAdapter;
    }

    @Override
    public RealEstateService getRealEstateService() {
        return realEstateService;
    }

    @Override
    public BuildingInstanceService getBuildingInstanceService() {
        return buildingInstanceService;
    }

    @Override
    public Set<MechanicType> getMechanics() {
        return Set.of(
                MechanicType.EXPLORATION,
                MechanicType.ECONOMY,
                MechanicType.COMBAT,
                MechanicType.CRAFTING,
                MechanicType.CHARACTER,
                MechanicType.ENCOUNTER
        );
    }

    // ==================== Adapter Implementations ====================

    /**
     * Adapts Traveller 2D6 dice system to DiceService interface.
     */
    private static class TravellerDiceAdapter implements DiceService {

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
            // Traveller primarily uses 2D6
            int total = DiceUtil.roll2D6();
            return new DiceRollResult(formula, List.of(total), total,
                    "Rolled " + formula + ": " + total);
        }

        @Override
        public DiceRollResult rollWithAdvantage(String formula) {
            int roll1 = DiceUtil.roll2D6();
            int roll2 = DiceUtil.roll2D6();
            int total = Math.max(roll1, roll2);
            return new DiceRollResult(formula + " (advantage)", List.of(roll1, roll2, total), total,
                    "Rolled " + formula + " with advantage: " + roll1 + ", " + roll2 + " → " + total);
        }

        @Override
        public DiceRollResult rollWithDisadvantage(String formula) {
            int roll1 = DiceUtil.roll2D6();
            int roll2 = DiceUtil.roll2D6();
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
     * Adapts Traveller Firearms and Melee combat to CombatService interface.
     */
    private static class TravellerCombatAdapter implements CombatService {
        private final FirearmsCombatService firearmsService;
        private final MeleeCombatService meleeService;

        TravellerCombatAdapter(FirearmsCombatService firearmsService, MeleeCombatService meleeService) {
            this.firearmsService = firearmsService;
            this.meleeService = meleeService;
        }

        @Override
        public Long createCombat(String encounterName) {
            // Traveller combat is started with character IDs, not by name
            // Return a generated ID for the combat session
            return System.currentTimeMillis();
        }

        @Override
        public CombatInfo getCombat(Long combatId) {
            CombatInfo info = new CombatInfo();
            info.setId(combatId);
            info.setEncounterName("Traveller Combat");
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
            int attackRoll = DiceUtil.roll2D6();
            int attackTotal = attackRoll + request.getAttackBonus();
            boolean hit = attackTotal >= 8; // Traveller standard: 8+ to hit

            AttackResult result = new AttackResult();
            result.setAttackerName(request.getAttackerName());
            result.setTargetName(request.getTargetName());
            result.setAttackRoll(attackRoll);
            result.setAttackTotal(attackTotal);
            result.setHit(hit);
            result.setCriticalHit(attackRoll == 12);
            result.setCriticalFail(attackRoll == 2);

            if (hit) {
                int damage = DiceUtil.rollDSides(6);
                result.setDamageDealt(damage);
                result.setDamageType(request.getDamageType() != null ? request.getDamageType() : "piercing");
                result.setMessage(request.getAttackerName() + " hits " + request.getTargetName() + " for " + damage + " damage!");
            } else {
                result.setDamageDealt(0);
                result.setMessage(request.getAttackerName() + " misses " + request.getTargetName() + ".");
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
     * Adapts Traveller Encumbrance system to InventoryService interface.
     */
    private static class TravellerInventoryAdapter implements InventoryService {
        private final EncumbranceService encumbranceService;
        private final com.yisy.modules.doramcu.service.character.CharacterService characterService;

        TravellerInventoryAdapter(EncumbranceService encumbranceService, com.yisy.modules.doramcu.service.character.CharacterService characterService) {
            this.encumbranceService = encumbranceService;
            this.characterService = characterService;
        }

        @Override
        public List<InventoryItem> getInventory(Long characterId) {
            TravellerCharacter character = characterService.getCharacter(characterId.intValue());
            if (character == null) return List.of();

            // Traveller uses encumbrance-based inventory
            List<InventoryItem> items = new ArrayList<>();
            // Return basic equipment based on character's career
            for (var tour : character.getCareerTours()) {
                InventoryItem item = new InventoryItem();
                item.setName(tour.getCareer().getDisplayName() + " Equipment");
                item.setDescription("Standard equipment from " + tour.getCareer().getDisplayName() + " service");
                item.setItemType("equipment");
                item.setQuantity(1);
                item.setWeight(10);
                items.add(item);
            }
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
            TravellerCharacter character = characterService.getCharacter(characterId.intValue());
            if (character == null) return "Character not found";
            return "Item used by " + character.getName();
        }
    }

    /**
     * Adapts Traveller ship repair and equipment creation to CraftingService interface.
     */
    private static class TravellerCraftingAdapter implements CraftingService {

        @Override
        public List<CraftingRecipe> listRecipes() {
            List<CraftingRecipe> recipes = new ArrayList<>();

            CraftingRecipe shipRepair = new CraftingRecipe();
            shipRepair.setId(1L);
            shipRepair.setName("Ship Repair");
            shipRepair.setDescription("Repair damage to a spacecraft hull or systems");
            shipRepair.setCategory("Engineering");
            shipRepair.setDifficulty(8);
            shipRepair.setTimeRequired(240);
            shipRepair.setMaterialsRequired("Repair kit, tools");
            shipRepair.setResultItem("Repaired ship systems");
            recipes.add(shipRepair);

            CraftingRecipe weaponCraft = new CraftingRecipe();
            weaponCraft.setId(2L);
            weaponCraft.setName("Weapon Modification");
            weaponCraft.setDescription("Modify a firearm for improved performance");
            weaponCraft.setCategory("Weaponsmithing");
            weaponCraft.setDifficulty(10);
            weaponCraft.setTimeRequired(120);
            weaponCraft.setMaterialsRequired("Weapon parts, tools");
            weaponCraft.setResultItem("Modified weapon");
            recipes.add(weaponCraft);

            CraftingRecipe medBay = new CraftingRecipe();
            medBay.setId(3L);
            medBay.setName("Medical Supplies");
            medBay.setDescription("Create medical supplies and treatments");
            medBay.setCategory("Medical");
            medBay.setDifficulty(9);
            medBay.setTimeRequired(60);
            medBay.setMaterialsRequired("Medical kit, chemicals");
            medBay.setResultItem("Medical supplies");
            recipes.add(medBay);

            return recipes;
        }

        @Override
        public CraftingRecipe getRecipe(Long recipeId) {
            return listRecipes().stream()
                    .filter(r -> r.getId().equals(recipeId))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public CraftingProject startProject(Long characterId, Long recipeId) {
            CraftingProject project = new CraftingProject();
            project.setId(System.currentTimeMillis());
            project.setCharacterId(characterId);
            project.setRecipeId(recipeId);
            project.setStatus("STARTED");
            project.setProgress(0);
            project.setTotalProgress(100);
            return project;
        }

        @Override
        public CraftingProject progressProject(Long projectId) {
            CraftingProject project = new CraftingProject();
            project.setId(projectId);
            project.setStatus("IN_PROGRESS");
            project.setProgress(DiceUtil.roll2D6() * 5);
            project.setTotalProgress(100);
            return project;
        }

        @Override
        public CraftingProject completeProject(Long projectId) {
            CraftingProject project = new CraftingProject();
            project.setId(projectId);
            project.setStatus("COMPLETED");
            project.setProgress(100);
            project.setTotalProgress(100);
            return project;
        }
    }

    /**
     * Adapts Traveller Space Stations to DungeonRoomService interface.
     * Stations serve as interior locations for exploration.
     */
    private static class TravellerDungeonRoomAdapter implements DungeonRoomService {
        private final Map<Long, DungeonRoom> rooms = new HashMap<>();
        private long nextRoomId = 1L;

        TravellerDungeonRoomAdapter() {
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
     * Traveller has no spells — this adapter returns empty results.
     * Psionics are handled by the PsionicsService adapter.
     */
    private static class TravellerSpellAdapter implements SpellService {

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
                    "Traveller does not have spells. Use psionics instead.", 0);
        }
    }

    /**
     * Adapts Traveller psionic powers to PsionicsService interface.
     */
    private static class TravellerPsionicsAdapter implements PsionicsService {
        private final Map<Long, PsionicPower> powerMap;

        TravellerPsionicsAdapter(Map<Long, PsionicPower> powerMap) {
            this.powerMap = powerMap;
        }

        @Override
        public List<PsionicPower> listPowers() {
            return new ArrayList<>(powerMap.values());
        }

        @Override
        public PsionicPower getPower(Long powerId) {
            return powerMap.get(powerId);
        }

        @Override
        public PsionicPowerResult usePower(Long characterId, Long powerId, Long targetId) {
            PsionicPower power = powerMap.get(powerId);
            if (power == null) {
                return new PsionicPowerResult(powerId, "Unknown", false,
                        "Psionic power not found", 0);
            }
            // Traveller psionic check: roll 2D6 against ability score
            int roll = DiceUtil.roll2D6();
            boolean success = roll <= 7; // Simplified: success on 7 or less
            String message = success
                    ? power.getName() + " used successfully! (rolled " + roll + ")"
                    : power.getName() + " failed (rolled " + roll + ")";
            return new PsionicPowerResult(powerId, power.getName(), success, message,
                    success ? power.getPowerPointsCost() : 0);
        }
    }

    /**
     * Adapts Traveller adventures to EncounterService interface.
     */
    private static class TravellerEncounterAdapter implements EncounterService {
        private final TravellerAdventureService adventureService;

        TravellerEncounterAdapter(TravellerAdventureService adventureService) {
            this.adventureService = adventureService;
        }

        @Override
        public List<Encounter> listEncounters() {
            // Traveller encounters are generated from adventure events
            List<Encounter> encounters = new ArrayList<>();

            Encounter ambush = new Encounter();
            ambush.setId(1L);
            ambush.setName("Ambush");
            ambush.setDescription("Your party is ambushed by hostile forces.");
            ambush.setDifficulty("Medium");
            ambush.setEncounterType("Combat");
            encounters.add(ambush);

            Encounter customs = new Encounter();
            customs.setId(2L);
            customs.setName("Customs Check");
            customs.setDescription("Authorities inspect your cargo and documents.");
            customs.setDifficulty("Easy");
            customs.setEncounterType("Social");
            encounters.add(customs);

            Encounter pirate = new Encounter();
            pirate.setId(3L);
            pirate.setName("Pirate Attack");
            pirate.setDescription("Pirates emerge from jump to attack your ship.");
            pirate.setDifficulty("Hard");
            pirate.setEncounterType("Combat");
            encounters.add(pirate);

            Encounter market = new Encounter();
            market.setId(4L);
            market.setName("Black Market Deal");
            market.setDescription("A shady dealer offers rare contraband.");
            market.setDifficulty("Medium");
            market.setEncounterType("Social");
            encounters.add(market);

            Encounter psionic = new Encounter();
            psionic.setId(5L);
            psionic.setName("Psionic Encounter");
            psionic.setDescription("A psionic entity makes contact.");
            psionic.setDifficulty("Hard");
            psionic.setEncounterType("Supernatural");
            encounters.add(psionic);

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
            int index = DiceUtil.rollIndex(encounters.size());
            Encounter encounter = encounters.get(index);
            encounter.setAreaId(areaId);
            return encounter;
        }

        @Override
        public EncounterResult resolveEncounter(Long encounterId, List<Long> characterIds) {
            Encounter encounter = getEncounter(encounterId);
            if (encounter == null) {
                return new EncounterResult(encounterId, "Unknown", false, "Encounter not found", 0);
            }
            // Traveller resolution: roll 2D6 for each character
            int totalRoll = 0;
            for (Long charId : characterIds) {
                totalRoll += DiceUtil.roll2D6();
            }
            boolean success = totalRoll >= characterIds.size() * 5;
            return new EncounterResult(encounterId, encounter.getName(), success,
                    success ? "Encounter resolved successfully!" : "Encounter failed.",
                    success ? 100 : 25);
        }
    }

    /**
     * Adapts Traveller CharacterService to gamecore.CharacterService interface.
     */
    private static class TravellerCharacterAdapter implements com.yisy.modules.doramcu.gamecore.CharacterService {
        private final com.yisy.modules.doramcu.service.character.CharacterService delegate;

        TravellerCharacterAdapter(com.yisy.modules.doramcu.service.character.CharacterService delegate) {
            this.delegate = delegate;
        }

        public com.yisy.modules.doramcu.gamecore.CharacterService.CharacterInfo getCharacter(Long characterId) {
            TravellerCharacter character = delegate.getCharacter(characterId.intValue());
            if (character == null) return null;
            return toCharacterInfo(character);
        }

        public List<com.yisy.modules.doramcu.gamecore.CharacterService.CharacterInfo> listCharacters() {
            return delegate.listCharacters().stream()
                    .map(this::toCharacterInfo)
                    .collect(Collectors.toList());
        }

        public void updateAbilityScores(Long characterId, int str, int dex, int con, int intelligence, int wis, int cha) {
            TravellerCharacter character = delegate.getCharacter(characterId.intValue());
            if (character == null) return;
            character.setAbility(AbilityScore.END, str);
            character.setAbility(AbilityScore.DEX, dex);
            character.setAbility(AbilityScore.ST, con);
            character.setAbility(AbilityScore.INT, intelligence);
            character.setAbility(AbilityScore.EDU, wis);
            character.setAbility(AbilityScore.APP, cha);
        }

        public com.yisy.modules.doramcu.gamecore.CharacterService.CharacterInfo awardExperience(Long characterId, int xp) {
            TravellerCharacter character = delegate.getCharacter(characterId.intValue());
            if (character == null) return null;
            // Traveller uses career-based progression, not XP
            return toCharacterInfo(character);
        }

        private com.yisy.modules.doramcu.gamecore.CharacterService.CharacterInfo toCharacterInfo(TravellerCharacter character) {
            com.yisy.modules.doramcu.gamecore.CharacterService.CharacterInfo info = new com.yisy.modules.doramcu.gamecore.CharacterService.CharacterInfo();
            info.setId((long) character.getId());
            info.setName(character.getName());
            info.setLevel(character.getCareerTours().size());
            info.setExperiencePoints((int) character.getWealth());
            info.setHitPoints(character.getAbility(AbilityScore.END));
            info.setMaxHitPoints(character.getAbility(AbilityScore.END));
            info.setStrScore(character.getAbility(AbilityScore.END));
            info.setDexScore(character.getAbility(AbilityScore.DEX));
            info.setConScore(character.getAbility(AbilityScore.ST));
            info.setIntScore(character.getAbility(AbilityScore.INT));
            info.setWisScore(character.getAbility(AbilityScore.EDU));
            info.setChaScore(character.getAbility(AbilityScore.APP));
            info.setLocation(character.getStatus().name());
            return info;
        }
    }

    /**
     * Adapts Traveller adventures to CampaignService interface.
     */
    private static class TravellerCampaignAdapter implements CampaignService {
        private final TravellerAdventureService adventureService;
        private final Map<Long, Campaign> campaigns = new HashMap<>();
        private long nextCampaignId = 1L;

        TravellerCampaignAdapter(TravellerAdventureService adventureService) {
            this.adventureService = adventureService;
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
            long id = nextCampaignId++;
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
     * Adapts TravellerRulesService to RulesService interface.
     */
    private static class TravellerRulesAdapter implements RulesService {
        private final TravellerRulesService delegate;

        TravellerRulesAdapter(TravellerRulesService delegate) {
            this.delegate = delegate;
        }

        @Override
        public List<RulesEntry> listRules() {
            // TravellerRulesService stores rules in the database
            // Return a summary of available rule categories
            List<RulesEntry> entries = new ArrayList<>();

            RulesEntry dice = new RulesEntry();
            dice.setId(1L);
            dice.setTitle("2D6 Basic Roll");
            dice.setCategory("Core Rules");
            dice.setText("All dice rolls in Traveller use two six-sided dice (2D6). The result is the sum of both dice, ranging from 2 to 12. Results of 8+ are generally considered successes.");
            dice.setSource("The Traveller Book");
            entries.add(dice);

            RulesEntry combat = new RulesEntry();
            combat.setId(2L);
            combat.setTitle("Combat Resolution");
            combat.setCategory("Combat");
            combat.setText("Roll 2D6 and add modifiers. 8+ is a hit. 12 is a critical hit. 2 is a fumble. Damage is rolled and subtracted from hit points after armor reduction.");
            combat.setSource("Characters & Combat");
            entries.add(combat);

            RulesEntry psionics = new RulesEntry();
            psionics.setId(3L);
            psionics.setTitle("Psionic Powers");
            psionics.setCategory("Psionics");
            psionics.setText("Psionic characters can use mental powers. Each power has a level and requires a check against the character's psionic ability score.");
            psionics.setSource("High Guard");
            entries.add(psionics);

            RulesEntry careers = new RulesEntry();
            careers.setId(4L);
            careers.setTitle("Career Service");
            careers.setCategory("Character Creation");
            careers.setText("Characters serve in careers for 4-year tours. Each tour may grant skills, promotions, and wealth. Retirement checks are made at the end of each tour.");
            careers.setSource("Characters & Combat");
            entries.add(careers);

            RulesEntry ships = new RulesEntry();
            ships.setId(5L);
            ships.setTitle("Ship Design");
            ships.setCategory("Starships");
            ships.setText("Ships are designed using a point-buy system. Hull size determines tonnage, which affects cargo capacity, passenger capacity, and weapon mounts.");
            ships.setSource("Starships");
            entries.add(ships);

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
