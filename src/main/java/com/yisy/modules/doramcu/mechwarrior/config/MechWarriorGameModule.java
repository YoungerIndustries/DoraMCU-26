package com.yisy.modules.doramcu.mechwarrior.config;

import com.yisy.modules.doramcu.mechwarrior.engine.MechWarriorGameEngine;
import com.yisy.modules.doramcu.mechwarrior.service.MechWarriorRulesEngine;
import com.yisy.modules.doramcu.shared.model.RulesEntry;
import com.yisy.modules.doramcu.shared.service.RulesAwareCombatAdapter;
import com.yisy.modules.doramcu.shared.service.UnifiedRulesQueryService;
import com.yisy.modules.doramcu.gamecore.RulesEngine;
import com.yisy.modules.doramcu.mechwarrior.model.MechChassis;
import com.yisy.modules.doramcu.mechwarrior.model.MechPilot;
import com.yisy.modules.doramcu.mechwarrior.model.MechWeapon;
import com.yisy.modules.doramcu.service.character.EncumbranceService;
import com.yisy.modules.doramcu.service.battle.FirearmsCombatService;
import com.yisy.modules.doramcu.service.battle.MeleeCombatService;
import com.yisy.modules.doramcu.techlevel.TechLevelAdapter;
import com.yisy.modules.doramcu.thirdreich.adapter.GroundCombatAdapter;
import com.yisy.modules.doramcu.thirdreich.adapter.MechWarriorTo3R4Adapter;
import com.yisy.modules.doramcu.gamecore.GameType;
import com.yisy.modules.doramcu.util.DiceUtil;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * MechWarrior Game Module adapter.
 * <p>
 * Implements the {@link GameModule} interface to register MechWarrior features
 * with the {@link GameModuleRegistry}. This allows other modules to access
 * MechWarrior mechanics through the shared game-core interfaces.
 * </p>
 * <p>
 * MechWarrior provides:
 * <ul>
 *   <li>Dice: d6-based system for damage and heat</li>
 *   <li>Combat: Component-based mech combat with heat management</li>
 *   <li>Inventory: Tonnage-based encumbrance system</li>
 *   <li>Ground Combat: 3R4 adapter for cross-module battles</li>
 *   <li>Tech Levels: TL7-TL9 mech era technology</li>
 *   <li>Characters: MechWarrior pilots with skills and traits</li>
 *   <li>Vehicles: BattleMechs, vehicles, and aerospace fighters</li>
 * </ul>
 * </p>
 */
@Component
public class MechWarriorGameModule extends AbstractGameModule {

    private final MechWarriorGameEngine gameEngine;
    private final FirearmsCombatService firearmsCombatService;
    private final MeleeCombatService meleeCombatService;
    private final EncumbranceService encumbranceService;
    private final MechWarriorTechLevelAdapter techLevelAdapter;
    private final GroundCombatAdapter groundCombatAdapter;
    private final UnifiedRulesQueryService unifiedRulesQueryService;
    private final MechWarriorRulesEngine rulesEngine;

    public MechWarriorGameModule(
            MechWarriorGameEngine gameEngine,
            FirearmsCombatService firearmsCombatService,
            MeleeCombatService meleeCombatService,
            EncumbranceService encumbranceService,
            GameModuleRegistry gameModuleRegistry,
            MechWarriorTechLevelAdapter techLevelAdapter,
            UnifiedRulesQueryService unifiedRulesQueryService,
            MechWarriorRulesEngine rulesEngine) {
        super(gameModuleRegistry);
        this.gameEngine = gameEngine;
        this.firearmsCombatService = firearmsCombatService;
        this.meleeCombatService = meleeCombatService;
        this.encumbranceService = encumbranceService;
        this.techLevelAdapter = techLevelAdapter;
        this.groundCombatAdapter = new MechWarriorTo3R4Adapter();
        this.unifiedRulesQueryService = unifiedRulesQueryService;
        this.rulesEngine = rulesEngine;
    }

    @Override
    public GameType getGameType() {
        return GameType.MECHWARRIOR;
    }

    @Override
    public String getModuleName() {
        return "MechWarrior";
    }

    @Override
    public DiceService getDiceService() {
        return new MechWarriorDiceAdapter();
    }

    @Override
    public CombatService getCombatService() {
        // Wrap the MechWarrior combat adapter with rules-aware combat
        CombatService delegate = new MechWarriorCombatAdapter(gameEngine);
        RulesAwareCombatAdapter rulesAware = new RulesAwareCombatAdapter(delegate, unifiedRulesQueryService);
        return rulesAware;
    }

    @Override
    public InventoryService getInventoryService() {
        return new MechWarriorInventoryAdapter(encumbranceService);
    }

    @Override
    public CraftingService getCraftingService() {
        return new MechWarriorCraftingAdapter();
    }

    @Override
    public DungeonRoomService getDungeonRoomService() {
        return new MechWarriorDungeonRoomAdapter();
    }

    @Override
    public SpellService getSpellService() {
        return new MechWarriorSpellAdapter();
    }

    @Override
    public PsionicsService getPsionicsService() {
        return new MechWarriorPsionicsAdapter();
    }

    @Override
    public EncounterService getEncounterService() {
        return new MechWarriorEncounterAdapter();
    }

    @Override
    public com.yisy.modules.doramcu.gamecore.CharacterService getCharacterService() {
        return new MechWarriorCharacterAdapter();
    }

    @Override
    public CampaignService getCampaignService() {
        return new MechWarriorCampaignAdapter();
    }

    @Override
    public RulesService getRulesService() {
        return new MechWarriorRulesAdapter();
    }

    @Override
    public TechLevelAdapter getTechLevelAdapter() {
        return techLevelAdapter;
    }

    /**
     * Returns the 3R4 ground combat adapter for MechWarrior units.
     * Other modules can use this to convert MechWarrior mechs to 3R4 ground units.
     */
    public GroundCombatAdapter getGroundCombatAdapter() {
        return groundCombatAdapter;
    }

    @Override
    public Set<MechanicType> getMechanics() {
        return Set.of(
                MechanicType.COMBAT,
                MechanicType.TACTICAL,
                MechanicType.CRAFTING,
                MechanicType.CHARACTER
        );
    }

    // ==================== Adapter Implementations ====================

    /**
     * Adapts MechWarrior d6 dice system to DiceService interface.
     */
    private static class MechWarriorDiceAdapter implements DiceService {

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
            // MechWarrior primarily uses d6 for damage
            int total = DiceUtil.rollDSides(6);
            return new DiceRollResult(formula, List.of(total), total,
                    "Rolled " + formula + ": " + total);
        }

        @Override
        public DiceRollResult rollWithAdvantage(String formula) {
            int roll1 = DiceUtil.rollDSides(6);
            int roll2 = DiceUtil.rollDSides(6);
            int total = Math.max(roll1, roll2);
            return new DiceRollResult(formula + " (advantage)", List.of(roll1, roll2, total), total,
                    "Rolled " + formula + " with advantage: " + roll1 + ", " + roll2 + " → " + total);
        }

        @Override
        public DiceRollResult rollWithDisadvantage(String formula) {
            int roll1 = DiceUtil.rollDSides(6);
            int roll2 = DiceUtil.rollDSides(6);
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
     * Adapts MechWarrior component-based combat to CombatService interface.
     */
    private static class MechWarriorCombatAdapter implements CombatService {
        private final MechWarriorGameEngine gameEngine;

        MechWarriorCombatAdapter(MechWarriorGameEngine gameEngine) {
            this.gameEngine = gameEngine;
        }

        @Override
        public Long createCombat(String encounterName) {
            String sessionId = "mw-combat-" + System.currentTimeMillis();
            gameEngine.createSession(sessionId, encounterName, 10);
            return System.currentTimeMillis();
        }

        @Override
        public CombatInfo getCombat(Long combatId) {
            CombatInfo info = new CombatInfo();
            info.setId(combatId);
            info.setEncounterName("MechWarrior Battle");
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
            // MechWarrior uses hit probability based on range, movement, and skill
            double hitChance = 0.7; // Base 70% hit chance

            boolean hit = Math.random() < hitChance;

            AttackResult result = new AttackResult();
            result.setAttackerName(request.getAttackerName());
            result.setTargetName(request.getTargetName());
            result.setAttackRoll((int) (Math.random() * 100));
            result.setAttackTotal((int) (hitChance * 100));
            result.setHit(hit);
            result.setCriticalHit(Math.random() < 0.05); // 5% crit chance
            result.setCriticalFail(Math.random() < 0.02); // 2% fumble chance

            if (hit) {
                int damage = DiceUtil.rollDSides(6) + DiceUtil.rollDSides(6);
                result.setDamageDealt(damage);
                result.setDamageType(request.getDamageType() != null ? request.getDamageType() : "heat");
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
     * Adapts MechWarrior tonnage-based inventory to InventoryService interface.
     */
    private static class MechWarriorInventoryAdapter implements InventoryService {
        private final EncumbranceService encumbranceService;

        MechWarriorInventoryAdapter(EncumbranceService encumbranceService) {
            this.encumbranceService = encumbranceService;
        }

        @Override
        public List<InventoryItem> getInventory(Long characterId) {
            // MechWarrior inventory is tonnage-based
            List<InventoryItem> items = new ArrayList<>();

            InventoryItem ammo = new InventoryItem();
            ammo.setName("Ammo Canister");
            ammo.setDescription("Standard ammunition for mech weapons");
            ammo.setItemType("ammo");
            ammo.setQuantity(10);
            ammo.setWeight(2);
            items.add(ammo);

            InventoryItem repair = new InventoryItem();
            repair.setName("Repair Kit");
            repair.setDescription("Field repair kit for mech maintenance");
            repair.setItemType("tool");
            repair.setQuantity(1);
            repair.setWeight(5);
            items.add(repair);

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
            return "Item used by MechWarrior pilot";
        }
    }

    /**
     * Adapts MechWarrior mech customization to CraftingService interface.
     */
    private static class MechWarriorCraftingAdapter implements CraftingService {

        @Override
        public List<CraftingRecipe> listRecipes() {
            List<CraftingRecipe> recipes = new ArrayList<>();

            CraftingRecipe mechRepair = new CraftingRecipe();
            mechRepair.setId(1L);
            mechRepair.setName("Mech Repair");
            mechRepair.setDescription("Repair damage to a BattleMech's armor and structure");
            mechRepair.setCategory("Engineering");
            mechRepair.setDifficulty(8);
            mechRepair.setTimeRequired(240);
            mechRepair.setMaterialsRequired("Repair kit, armor plates");
            mechRepair.setResultItem("Repaired BattleMech");
            recipes.add(mechRepair);

            CraftingRecipe weaponMount = new CraftingRecipe();
            weaponMount.setId(2L);
            weaponMount.setName("Weapon Mounting");
            weaponMount.setDescription("Install weapons on a BattleMech chassis");
            weaponMount.setCategory("Weaponsmithing");
            weaponMount.setDifficulty(10);
            weaponMount.setTimeRequired(120);
            weaponMount.setMaterialsRequired("Weapon, tools, hardpoint");
            weaponMount.setResultItem("Mounted weapon system");
            recipes.add(weaponMount);

            CraftingRecipe armorUpgrade = new CraftingRecipe();
            armorUpgrade.setId(3L);
            armorUpgrade.setName("Armor Upgrade");
            armorUpgrade.setDescription("Upgrade BattleMech armor plating");
            armorUpgrade.setCategory("Engineering");
            armorUpgrade.setDifficulty(9);
            armorUpgrade.setTimeRequired(180);
            armorUpgrade.setMaterialsRequired("Ferro-Fibrous armor, tools");
            armorUpgrade.setResultItem("Upgraded armor");
            recipes.add(armorUpgrade);

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
            project.setProgress(DiceUtil.rollDSides(6) * 10);
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
     * Adapts MechWarrior mech bays and facilities to DungeonRoomService interface.
     */
    private static class MechWarriorDungeonRoomAdapter implements DungeonRoomService {
        private final Map<Long, DungeonRoom> rooms = new HashMap<>();
        private long nextRoomId = 1L;

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
            return new TrapCheckResult(roomId, "Mech Bay", null, true, false, 0, null, null);
        }

        @Override
        public TreasureResult discoverTreasure(Long roomId) {
            return new TreasureResult(roomId, "Mech Bay", "Spare Parts", 0, null);
        }
    }

    /**
     * MechWarrior has no spells — this adapter returns empty results.
     */
    private static class MechWarriorSpellAdapter implements SpellService {

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
                    "MechWarrior does not have spells.", 0);
        }
    }

    /**
     * MechWarrior has no psionics — this adapter returns empty results.
     */
    private static class MechWarriorPsionicsAdapter implements PsionicsService {

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
                    "MechWarrior does not have psionics.", 0);
        }
    }

    /**
     * Adapts MechWarrior missions to EncounterService interface.
     */
    private static class MechWarriorEncounterAdapter implements EncounterService {

        @Override
        public List<Encounter> listEncounters() {
            List<Encounter> encounters = new ArrayList<>();

            Encounter ambush = new Encounter();
            ambush.setId(1L);
            ambush.setName("Ambush");
            ambush.setDescription("Your lance is ambushed by enemy mechs in the treeline.");
            ambush.setDifficulty("Medium");
            ambush.setEncounterType("Combat");
            encounters.add(ambush);

            Encounter salvage = new Encounter();
            salvage.setId(2L);
            salvage.setName("Salvage Operation");
            salvage.setDescription("Recover parts from a wrecked mech on the battlefield.");
            salvage.setDifficulty("Easy");
            salvage.setEncounterType("Exploration");
            encounters.add(salvage);

            Encounter escort = new Encounter();
            escort.setId(3L);
            escort.setName("Convoy Escort");
            escort.setDescription("Protect a supply convoy from raiders.");
            escort.setDifficulty("Hard");
            escort.setEncounterType("Combat");
            encounters.add(escort);

            Encounter patrol = new Encounter();
            patrol.setId(4L);
            patrol.setName("Border Patrol");
            patrol.setDescription("Patrol the border for signs of enemy activity.");
            patrol.setDifficulty("Easy");
            patrol.setEncounterType("Exploration");
            encounters.add(patrol);

            Encounter assault = new Encounter();
            assault.setId(5L);
            assault.setName("Fortress Assault");
            assault.setDescription("Assault a fortified enemy position.");
            assault.setDifficulty("Very Hard");
            assault.setEncounterType("Combat");
            encounters.add(assault);

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
            // MechWarrior resolution: roll d6 for each pilot
            int totalRoll = 0;
            for (Long charId : characterIds) {
                totalRoll += DiceUtil.rollDSides(6);
            }
            boolean success = totalRoll >= characterIds.size() * 4;
            return new EncounterResult(encounterId, encounter.getName(), success,
                    success ? "Mission accomplished!" : "Mission failed.",
                    success ? 100 : 25);
        }
    }

    /**
     * Adapts MechWarrior pilots to gamecore.CharacterService interface.
     */
    private static class MechWarriorCharacterAdapter implements com.yisy.modules.doramcu.gamecore.CharacterService {

        // TODO: Wire up to MechPilotRepository to persist and load pilots
        private final Map<Long, MechPilot> pilots = new HashMap<>();
        private long nextPilotId = 1L;

        @Override
        public CharacterInfo getCharacter(Long characterId) {
            MechPilot pilot = pilots.get(characterId);
            if (pilot == null) return null;
            return toCharacterInfo(pilot);
        }

        @Override
        public List<CharacterInfo> listCharacters() {
            return pilots.values().stream()
                    .map(this::toCharacterInfo)
                    .collect(Collectors.toList());
        }

        @Override
        public void updateAbilityScores(Long characterId, int str, int dex, int con, int intelligence, int wis, int cha) {
            MechPilot pilot = pilots.get(characterId);
            if (pilot == null) return;
            // Map D&D-style abilities to MechWarrior skills
            pilot.setBallisticSkill(Math.min(6, Math.max(1, str / 3)));
            pilot.setEvasionSkill(Math.min(6, Math.max(1, dex / 3)));
            pilot.setHeatManagementSkill(Math.min(6, Math.max(1, con / 3)));
            pilot.setEnergySkill(Math.min(6, Math.max(1, intelligence / 3)));
            pilot.setShieldingSkill(Math.min(6, Math.max(1, wis / 3)));
            pilot.setMissileSkill(Math.min(6, Math.max(1, cha / 3)));
        }

        @Override
        public CharacterInfo awardExperience(Long characterId, int xp) {
            MechPilot pilot = pilots.get(characterId);
            if (pilot == null) return null;
            pilot.gainExperience(xp);
            return toCharacterInfo(pilot);
        }

        private CharacterInfo toCharacterInfo(MechPilot pilot) {
            CharacterInfo info = new CharacterInfo();
            info.setId(nextPilotId);
            info.setName(pilot.getName());
            info.setLevel(pilot.getExperienceLevel().ordinal() + 1);
            info.setExperiencePoints(pilot.getExperiencePoints());
            info.setHitPoints(100 - pilot.getStressLevel());
            info.setMaxHitPoints(100);
            info.setStrScore(pilot.getBallisticSkill());
            info.setDexScore(pilot.getEvasionSkill());
            info.setConScore(pilot.getHeatManagementSkill());
            info.setIntScore(pilot.getEnergySkill());
            info.setWisScore(pilot.getShieldingSkill());
            info.setChaScore(pilot.getMissileSkill());
            info.setLocation(pilot.getExperienceLevel().name());
            return info;
        }
    }

    /**
     * Adapts MechWarrior campaigns to CampaignService interface.
     */
    private static class MechWarriorCampaignAdapter implements CampaignService {
        private final Map<Long, Campaign> campaigns = new HashMap<>();
        private long nextCampaignId = 1L;

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
     * Adapts MechWarrior rules to RulesService interface.
     */
    private static class MechWarriorRulesAdapter implements RulesService {

        @Override
        public List<RulesEntry> listRules() {
            List<RulesEntry> entries = new ArrayList<>();

            RulesEntry heat = new RulesEntry();
            heat.setId(1L);
            heat.setTitle("Heat Management");
            heat.setCategory("Core Rules");
            heat.setText("Each weapon generates heat when fired. If heat reaches 100, the mech shuts down. Heat dissipates by 2 points per turn. Heat Override adds 1.5 heat per turn but prevents shutdown.");
            heat.setSource("BattleTech Total Warfare");
            entries.add(heat);

            RulesEntry damage = new RulesEntry();
            damage.setId(2L);
            damage.setTitle("Component Damage");
            damage.setCategory("Combat");
            damage.setText("Damage is applied to specific locations: Head, Center Torso, Left/Right Torso, Left/Right Arm, Left/Right Leg. Each location has armor and structure points.");
            damage.setSource("BattleTech Total Warfare");
            entries.add(damage);

            RulesEntry stability = new RulesEntry();
            stability.setId(3L);
            stability.setTitle("Stability and Knockdown");
            stability.setCategory("Combat");
            stability.setText("When stability damage reaches 50, the mech is knocked down. A knocked-down mech cannot move or fire until it stands up (requires a full turn).");
            stability.setSource("BattleTech Total Warfare");
            entries.add(stability);

            RulesEntry movement = new RulesEntry();
            movement.setId(4L);
            movement.setTitle("Movement and Jump Jets");
            movement.setCategory("Movement");
            movement.setText("Mechs move at speeds determined by their engine rating. Jump jets allow vertical movement at a cost of 10 heat per 100 meters.");
            movement.setSource("BattleTech Total Warfare");
            entries.add(movement);

            RulesEntry weapons = new RulesEntry();
            weapons.setId(5L);
            weapons.setTitle("Weapon Systems");
            weapons.setCategory("Weapons");
            weapons.setText("Weapons are categorized as Energy (lasers, PPCs), Ballistic (autocannons, Gauss), or Missile (SRMs, LRMs). Each has different heat, cooldown, and range characteristics.");
            weapons.setSource("BattleTech Total Warfare");
            entries.add(weapons);

            RulesEntry pilot = new RulesEntry();
            pilot.setId(6L);
            pilot.setTitle("Pilot Skills");
            pilot.setCategory("Pilots");
            pilot.setText("Pilots have six skills: Ballistic, Energy, Missile (weapon skills) and Shielding, Heat Management, Evasion (piloting skills). Each skill ranges from 1-6.");
            pilot.setSource("BattleTech Total Warfare");
            entries.add(pilot);

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
