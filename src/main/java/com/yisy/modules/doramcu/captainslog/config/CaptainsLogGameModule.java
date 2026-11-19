package com.yisy.modules.doramcu.captainslog.config;

import com.yisy.modules.doramcu.captainslog.service.CaptainLogCharacterService;
import com.yisy.modules.doramcu.captainslog.service.CaptainLogCombatService;
import com.yisy.modules.doramcu.captainslog.service.CaptainLogCrewService;
import com.yisy.modules.doramcu.captainslog.service.CaptainLogMissionService;
import com.yisy.modules.doramcu.captainslog.service.CaptainLogShipTalentService;
import com.yisy.modules.doramcu.captainslog.service.CaptainLogTalentService;
import com.yisy.modules.doramcu.captainslog.service.CareerProgressionService;
import com.yisy.modules.doramcu.captainslog.service.DiceService;
import com.yisy.modules.doramcu.captainslog.service.DowntimeService;
import org.springframework.beans.factory.annotation.Qualifier;
import com.yisy.modules.doramcu.captainslog.service.EquipmentService;
import com.yisy.modules.doramcu.captainslog.service.InjuryService;
import com.yisy.modules.doramcu.captainslog.service.LifepathService;
import com.yisy.modules.doramcu.captainslog.service.RulesService;
import com.yisy.modules.doramcu.captainslog.service.StarshipCombatService;
import com.yisy.modules.doramcu.captainslog.service.StarshipCreationService;
import com.yisy.modules.doramcu.captainslog.service.CaptainsLogRulesEngine;
import com.yisy.modules.doramcu.shared.model.RulesEntry;
import com.yisy.modules.doramcu.shared.service.RulesAwareCombatAdapter;
import com.yisy.modules.doramcu.shared.service.UnifiedRulesQueryService;
import com.yisy.modules.doramcu.gamecore.GameType;
import com.yisy.modules.doramcu.gamecore.RulesEngine;
import com.yisy.modules.doramcu.util.DiceUtil;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Captain's Log Game Module adapter.
 * <p>
 * Implements the {@link GameModule} interface to register Captain's Log (Star Trek Adventures)
 * features with the {@link GameModuleRegistry}. This allows Earth2026 and other modules
 * to access Captain's Log features through the shared game-core interfaces.
 * </p>
 * <p>
 * Captain's Log provides:
 * <ul>
 *   <li>2d20 Dice System: Modiphius "count successes" mechanic with boons/threats</li>
 *   <li>Ground Combat: Simplified task-attempt combat with hits and complications</li>
 *   <li>Starship Combat: Range bands, system attacks, evasion, critical hits</li>
 *   <li>Character Creation: Attributes, disciplines, foci, values, lifepath</li>
 *   <li>Missions: Momentum/threat economy, narrative beats, complications</li>
 *   <li>Crew Management: Crew members with roles and specialties</li>
 *   <li>Equipment: Starfleet and third-party gear</li>
 *   <li>Starship Design: Ship talents, systems, and customization</li>
 *   <li>Injuries: Wounded, Down, and recovery tracking</li>
 *   <li>Downtime: Activities between missions</li>
 *   <li>Career Progression: Promotions, new foci, and talents</li>
 * </ul>
 * </p>
 */
@Component
public class CaptainsLogGameModule extends AbstractGameModule {

    private final CaptainLogCharacterService characterService;
    private final CaptainLogCombatService combatService;
    private final StarshipCombatService starshipCombatService;
    private final CaptainLogMissionService missionService;
    private final com.yisy.modules.doramcu.captainslog.service.CampaignService captainsLogCampaignService;
    private final EquipmentService equipmentService;
    private final CaptainLogCrewService crewService;
    private final CaptainLogTalentService talentService;
    private final CaptainLogShipTalentService shipTalentService;
    private final StarshipCreationService starshipCreationService;
    private final InjuryService injuryService;
    private final DowntimeService downtimeService;
    private final CareerProgressionService careerProgressionService;
    private final LifepathService lifepathService;
    private final RulesService captainsLogRulesService;
    private final DiceService captainsLogDiceService;
    private final UnifiedRulesQueryService unifiedRulesQueryService;
    private final CaptainsLogRulesEngine rulesEngine;

    public CaptainsLogGameModule(
            CaptainLogCharacterService characterService,
            CaptainLogCombatService combatService,
            StarshipCombatService starshipCombatService,
            CaptainLogMissionService missionService,
            com.yisy.modules.doramcu.captainslog.service.CampaignService captainsLogCampaignService,
            EquipmentService equipmentService,
            CaptainLogCrewService crewService,
            CaptainLogTalentService talentService,
            CaptainLogShipTalentService shipTalentService,
            StarshipCreationService starshipCreationService,
            InjuryService injuryService,
            DowntimeService downtimeService,
            @Qualifier("captainsLogCareerProgressionService_0") CareerProgressionService careerProgressionService,
            LifepathService lifepathService,
            RulesService captainsLogRulesService,
            DiceService captainsLogDiceService,
            GameModuleRegistry gameModuleRegistry,
            UnifiedRulesQueryService unifiedRulesQueryService,
            CaptainsLogRulesEngine rulesEngine) {
        super(gameModuleRegistry);
        this.characterService = characterService;
        this.combatService = combatService;
        this.starshipCombatService = starshipCombatService;
        this.missionService = missionService;
        this.captainsLogCampaignService = captainsLogCampaignService;
        this.equipmentService = equipmentService;
        this.crewService = crewService;
        this.talentService = talentService;
        this.shipTalentService = shipTalentService;
        this.starshipCreationService = starshipCreationService;
        this.injuryService = injuryService;
        this.downtimeService = downtimeService;
        this.careerProgressionService = careerProgressionService;
        this.lifepathService = lifepathService;
        this.captainsLogRulesService = captainsLogRulesService;
        this.captainsLogDiceService = captainsLogDiceService;
        this.unifiedRulesQueryService = unifiedRulesQueryService;
        this.rulesEngine = rulesEngine;
    }

    @Override
    public GameType getGameType() {
        return GameType.CAPTAINS_LOG;
    }

    @Override
    public String getModuleName() {
        return "Captain's Log - Star Trek Adventures";
    }

    @Override
    public com.yisy.modules.doramcu.gamecore.DiceService getDiceService() {
        return new CLDiceAdapter(captainsLogDiceService);
    }

    @Override
    public CombatService getCombatService() {
        // Wrap the Captain's Log combat adapter with rules-aware combat
        CombatService delegate = new CLCombatAdapter(combatService, starshipCombatService);
        RulesAwareCombatAdapter rulesAware = new RulesAwareCombatAdapter(delegate, unifiedRulesQueryService);
        return rulesAware;
    }

    @Override
    public InventoryService getInventoryService() {
        return new CLInventoryAdapter(equipmentService);
    }

    @Override
    public CraftingService getCraftingService() {
        return new CLCraftingAdapter(starshipCreationService, downtimeService);
    }

    @Override
    public DungeonRoomService getDungeonRoomService() {
        return new CLDungeonRoomAdapter();
    }

    @Override
    public SpellService getSpellService() {
        return new CLSpellAdapter();
    }

    @Override
    public PsionicsService getPsionicsService() {
        return new CLPsionicsAdapter();
    }

    @Override
    public EncounterService getEncounterService() {
        return new CLEncounterAdapter(missionService);
    }

    @Override
    public CharacterService getCharacterService() {
        return new CLCharacterAdapter(characterService);
    }

    @Override
    public CampaignService getCampaignService() {
        return new CLCampaignAdapter(captainsLogCampaignService, missionService);
    }

    @Override
    public com.yisy.modules.doramcu.gamecore.RulesService getRulesService() {
        return new CLRulesAdapter(captainsLogRulesService);
    }

    @Override
    public Set<MechanicType> getMechanics() {
        return Set.of(
                MechanicType.EXPLORATION,
                MechanicType.DIPLOMACY,
                MechanicType.COMBAT,
                MechanicType.ENCOUNTER,
                MechanicType.CAMPAIGN,
                MechanicType.CHARACTER
        );
    }

    // ==================== Adapter Implementations ====================

    /**
     * Adapts Captain's Log 2d20 "count successes" system to DiceService interface.
     * CL uses 2d20 with target numbers, boons/threats, and complication on 20.
     */
    private static class CLDiceAdapter implements com.yisy.modules.doramcu.gamecore.DiceService {
        private final DiceService clDiceService;

        CLDiceAdapter(DiceService clDiceService) {
            this.clDiceService = clDiceService;
        }

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
            // CL uses 2d20 count successes
            int d1 = DiceUtil.rollDSides(20);
            int d2 = DiceUtil.rollDSides(20);
            return new DiceRollResult("2d20", List.of(d1, d2), d1 + d2,
                    "2d20 task attempt: " + d1 + ", " + d2);
        }

        @Override
        public DiceRollResult rollWithAdvantage(String formula) {
            // Boon: roll 3d20, keep best 2
            int d1 = DiceUtil.rollDSides(20);
            int d2 = DiceUtil.rollDSides(20);
            int d3 = DiceUtil.rollDSides(20);
            int[] dice = {d1, d2, d3};
            Arrays.sort(dice);
            int total = dice[1] + dice[2]; // best 2
            return new DiceRollResult("2d20+1 boon", List.of(d1, d2, d3, total), total,
                    "Boon roll (3d20 keep best 2): " + d1 + ", " + d2 + ", " + d3 + " → " + total);
        }

        @Override
        public DiceRollResult rollWithDisadvantage(String formula) {
            // Threat: roll 3d20, keep best 2 (threat die is usually worst)
            int d1 = DiceUtil.rollDSides(20);
            int d2 = DiceUtil.rollDSides(20);
            int d3 = DiceUtil.rollDSides(20);
            int[] dice = {d1, d2, d3};
            Arrays.sort(dice);
            int total = dice[0] + dice[1]; // worst 2
            return new DiceRollResult("2d20+1 threat", List.of(d1, d2, d3, total), total,
                    "Threat roll (3d20 keep best 2): " + d1 + ", " + d2 + ", " + d3 + " → " + total);
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
     * Adapts Captain's Log combat to CombatService interface.
     * Supports both ground combat (simplified task attempts) and starship combat.
     */
    private static class CLCombatAdapter implements CombatService {
        private final CaptainLogCombatService combatService;
        private final StarshipCombatService starshipCombatService;
        private final Map<Long, CombatState> combats = new HashMap<>();
        private final AtomicLong combatIdCounter = new AtomicLong(1);

        CLCombatAdapter(CaptainLogCombatService combatService, StarshipCombatService starshipCombatService) {
            this.combatService = combatService;
            this.starshipCombatService = starshipCombatService;
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
            info.setCombatants(state.combatants.stream().map(c -> {
                CombatantInfo ci = new CombatantInfo();
                ci.setName(c.name);
                ci.setArmorClass(c.targetNumber);
                ci.setCurrentHp(c.currentHits);
                ci.setMaxHp(c.maxHits);
                ci.setCharacter(c.isCharacter);
                ci.setAlive(c.currentHits > 0);
                return ci;
            }).collect(Collectors.toList()));
            return info;
        }

        @Override
        public CombatantInfo addCombatant(Long combatId, String name, int armorClass, int maxHp, boolean isCharacter) {
            CombatState state = combats.get(combatId);
            if (state == null) return null;
            Combatant combatant = new Combatant();
            combatant.name = name;
            combatant.targetNumber = armorClass;
            combatant.maxHits = maxHp;
            combatant.currentHits = maxHp;
            combatant.isCharacter = isCharacter;
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
            // CL uses Fitness attribute for initiative
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

            // CL combat: 2d20 task attempt
            int d1 = DiceUtil.rollDSides(20);
            int d2 = DiceUtil.rollDSides(20);
            boolean complication = (d1 == 20 || d2 == 20);
            boolean automaticSuccess = (d1 == 1 || d2 == 1);

            // Target number from attack bonus (CL-style)
            int targetNumber = request.getAttackBonus();
            int successes = 0;
            if (d1 <= targetNumber && d1 != 20) successes++;
            if (d2 <= targetNumber && d2 != 20) successes++;
            if (d1 == 1) successes++; // automatic extra success
            if (d2 == 1) successes++; // automatic extra success

            boolean hit = successes > 0;
            boolean critical = (d1 == 1 && d2 == 1);

            AttackResult result = new AttackResult();
            result.setAttackerName(request.getAttackerName());
            result.setTargetName(request.getTargetName());
            result.setAttackRoll(Math.max(d1, d2));
            result.setAttackTotal(successes);
            result.setHit(hit);
            result.setCriticalHit(critical);
            result.setCriticalFail(complication);

            if (hit) {
                // CL: 1 hit per success
                int damage = successes;
                if (critical) damage *= 2;
                result.setDamageDealt(damage);
                result.setDamageType(request.getDamageType() != null ? request.getDamageType() : "kinetic");
                result.setMessage(request.getAttackerName() + " hits " + request.getTargetName() + " for " + damage + " hit" + (damage > 1 ? "s" : "") + "!");

                for (Combatant c : state.combatants) {
                    if (c.name.equals(request.getTargetName())) {
                        c.currentHits = Math.max(0, c.currentHits - damage);
                        break;
                    }
                }
            } else {
                result.setDamageDealt(0);
                if (complication) {
                    result.setMessage(request.getAttackerName() + " misses and a complication arises!");
                } else {
                    result.setMessage(request.getAttackerName() + " misses " + request.getTargetName() + ".");
                }
            }
            return result;
        }

        @Override
        public CombatantInfo healCombatant(Long combatId, String name, int amount) {
            CombatState state = combats.get(combatId);
            if (state == null) return null;
            for (Combatant c : state.combatants) {
                if (c.name.equals(name)) {
                    c.currentHits = Math.min(c.maxHits, c.currentHits + amount);
                    CombatantInfo info = new CombatantInfo();
                    info.setName(c.name);
                    info.setCurrentHp(c.currentHits);
                    info.setMaxHp(c.maxHits);
                    info.setAlive(c.currentHits > 0);
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
            int targetNumber;
            int maxHits;
            int currentHits;
            boolean isCharacter;
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
     * Adapts Captain's Log equipment to InventoryService interface.
     */
    private static class CLInventoryAdapter implements InventoryService {
        private final EquipmentService equipmentService;

        CLInventoryAdapter(EquipmentService equipmentService) {
            this.equipmentService = equipmentService;
        }

        @Override
        public List<InventoryItem> getInventory(Long characterId) {
            List<InventoryItem> items = new ArrayList<>();
            // CL characters have standard Starfleet issue
            InventoryItem phaser = new InventoryItem();
            phaser.setName("Type-2 Phaser");
            phaser.setDescription("Standard Starfleet sidearm");
            phaser.setItemType("weapon");
            phaser.setQuantity(1);
            phaser.setWeight(1);
            phaser.setEquipped(true);
            items.add(phaser);

            InventoryItem tricorder = new InventoryItem();
            tricorder.setName("Tricorder");
            tricorder.setDescription("Standard scanning device");
            tricorder.setItemType("tool");
            tricorder.setQuantity(1);
            tricorder.setWeight(1);
            items.add(tricorder);

            InventoryItem medkit = new InventoryItem();
            medkit.setName("Medkit");
            medkit.setDescription("Emergency medical supplies");
            medkit.setItemType("medical");
            medkit.setQuantity(1);
            medkit.setWeight(2);
            items.add(medkit);

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
            return "Item used by Captain's Log character";
        }
    }

    /**
     * Adapts Captain's Log starship creation and downtime to CraftingService interface.
     */
    private static class CLCraftingAdapter implements CraftingService {
        private final StarshipCreationService starshipCreationService;
        private final DowntimeService downtimeService;
        private final Map<Long, CraftingProject> projects = new HashMap<>();
        private final AtomicLong projectIdCounter = new AtomicLong(1);

        CLCraftingAdapter(StarshipCreationService starshipCreationService, DowntimeService downtimeService) {
            this.starshipCreationService = starshipCreationService;
            this.downtimeService = downtimeService;
        }

        @Override
        public List<CraftingRecipe> listRecipes() {
            List<CraftingRecipe> recipes = new ArrayList<>();

            CraftingRecipe ship = new CraftingRecipe();
            ship.setId(1L);
            ship.setName("Starship Design");
            ship.setDescription("Design and build a new starship");
            ship.setCategory("Starship");
            ship.setDifficulty(14);
            ship.setTimeRequired(1440);
            ship.setMaterialsRequired("Hull plating, warp core, impulse engines");
            ship.setResultItem("Custom starship");
            recipes.add(ship);

            CraftingRecipe phaser = new CraftingRecipe();
            phaser.setId(2L);
            phaser.setName("Phaser Calibration");
            phaser.setDescription("Calibrate phaser weapons for optimal output");
            phaser.setCategory("Engineering");
            phaser.setDifficulty(10);
            phaser.setTimeRequired(120);
            phaser.setMaterialsRequired("Phaser components, energy cells");
            phaser.setResultItem("Calibrated phaser");
            recipes.add(phaser);

            CraftingRecipe medical = new CraftingRecipe();
            medical.setId(3L);
            medical.setName("Medical Supplies");
            medical.setDescription("Create medical supplies for ship sickbay");
            medical.setCategory("Medical");
            medical.setDifficulty(8);
            medical.setTimeRequired(60);
            medical.setMaterialsRequired("Hyposprays, bandages, antitoxins");
            medical.setResultItem("Medical supply kit");
            recipes.add(medical);

            CraftingRecipe repair = new CraftingRecipe();
            repair.setId(4L);
            repair.setName("Ship Repairs");
            repair.setDescription("Repair damaged ship systems during downtime");
            repair.setCategory("Maintenance");
            repair.setDifficulty(10);
            repair.setTimeRequired(240);
            repair.setMaterialsRequired("Spare parts, welding equipment");
            repair.setResultItem("Repaired systems");
            recipes.add(repair);

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
            int progress = DiceUtil.rollDSides(20) * 3;
            project.setProgress(Math.min(100, project.getProgress() + progress));
            if (project.getProgress() >= 100) {
                project.setStatus("COMPLETED");
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
     * Adapts Captain's Log ship interiors to DungeonRoomService interface.
     * Starship decks and rooms serve as dungeon rooms.
     */
    private static class CLDungeonRoomAdapter implements DungeonRoomService {
        private final Map<Long, DungeonRoom> rooms = new HashMap<>();
        private final AtomicLong roomIdCounter = new AtomicLong(1);

        @Override
        public DungeonRoom createRoom(DungeonRoom room) {
            Long id = roomIdCounter.getAndIncrement();
            room.setId(id);
            room.setActive(true);
            room.setDiscovered(false);
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
            DungeonRoom from = rooms.get(fromId);
            if (from != null) {
                String connections = from.getConnections() != null ? from.getConnections() + "," + toId : String.valueOf(toId);
                from.setConnections(connections);
            }
        }

        @Override
        public List<DungeonRoom> getConnectedRooms(Long roomId) {
            DungeonRoom room = rooms.get(roomId);
            if (room == null || room.getConnections() == null) return Collections.emptyList();
            List<DungeonRoom> connected = new ArrayList<>();
            for (String connId : room.getConnections().split(",")) {
                Long id = Long.parseLong(connId.trim());
                DungeonRoom conn = rooms.get(id);
                if (conn != null) connected.add(conn);
            }
            return connected;
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
            if (room == null) return null;
            int dc = room.getTrapDc() != null ? room.getTrapDc() : 15;
            int total = roll + dexModifier + proficiencyBonus;
            boolean success = total >= dc;
            boolean criticalFailure = roll == 1;
            return new TrapCheckResult(roomId, room.getName(), dc, success, criticalFailure,
                    criticalFailure ? 2 : 1, "energy", room.getTrapDamageExpr());
        }

        @Override
        public TreasureResult discoverTreasure(Long roomId) {
            DungeonRoom room = rooms.get(roomId);
            if (room == null) return null;
            return new TreasureResult(roomId, room.getName(),
                    room.getTreasure() != null ? room.getTreasure() : "No treasure found",
                    room.getGoldPieces() != null ? room.getGoldPieces() : 0,
                    room.getMagicRarity() != null ? room.getMagicRarity() : "none");
        }
    }

    /**
     * Adapts Captain's Log to SpellService interface.
     * Star Trek has no magic; returns empty results directing to technobabble or psionics.
     */
    private static class CLSpellAdapter implements SpellService {

        @Override
        public List<Spell> listSpells() {
            // Star Trek has no spells; technobabble abilities are modeled as psionics
            return Collections.emptyList();
        }

        @Override
        public Spell getSpell(Long spellId) {
            return null;
        }

        @Override
        public List<Spell> listSpellsByLevel(int level) {
            return Collections.emptyList();
        }

        @Override
        public SpellCastResult castSpell(Long characterId, Long spellId, Long targetId) {
            return new SpellCastResult(spellId, "N/A", false,
                    "Star Trek has no spells. Use psionics or technobabble abilities instead.", 0);
        }
    }

    /**
     * Adapts Captain's Log psionics to PsionicsService interface.
     * Star Trek features Vulcan mind melds, Betazoid empathy, and El-Aurian powers.
     */
    private static class CLPsionicsAdapter implements PsionicsService {
        private final List<PsionicPower> powers;

        CLPsionicsAdapter() {
            this.powers = new ArrayList<>();
            addPower(1L, "Mind Meld", "Vulcan", 3, 5, "Touch", "Concentration",
                    "Establish telepathic link with target to read memories or implant suggestions");
            addPower(2L, "Empathy", "Betazoid", 1, 2, "Short Range", "Concentration",
                    "Sense the emotional state of nearby creatures");
            addPower(3L, "Telepathy", "Betazoid", 2, 3, "Long Range", "Concentration",
                    "Communicate telepathically with another being");
            addPower(4L, "Precognition", "Betazoid/El-Aurian", 4, 8, "Self", "Instantaneous",
                    "Receive visions of possible future events");
            addPower(5L, "Telekinesis", "El-Aurian", 3, 5, "Medium Range", "Concentration",
                    "Move objects with the power of the mind");
            addPower(6L, "Mind Blank", "Vulcan", 4, 6, "Self", "1 hour",
                    "Shield mind from telepathic intrusion");
            addPower(7L, "Psychic Scream", "El-Aurian", 3, 4, "Short Range", "Instantaneous",
                    "Project psychic energy to stun or damage a target");
            addPower(8L, "Dream Walking", "Betazoid", 5, 10, "Any Range", "Sleep",
                    "Enter the dreams of another being to communicate or observe");
        }

        private void addPower(Long id, String name, String discipline, int level, int cost,
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
            powers.add(power);
        }

        @Override
        public List<PsionicPower> listPowers() {
            return powers;
        }

        @Override
        public PsionicPower getPower(Long powerId) {
            return powers.stream().filter(p -> p.getId().equals(powerId)).findFirst().orElse(null);
        }

        @Override
        public PsionicPowerResult usePower(Long characterId, Long powerId, Long targetId) {
            PsionicPower power = powers.stream().filter(p -> p.getId().equals(powerId)).findFirst().orElse(null);
            if (power == null) {
                return new PsionicPowerResult(powerId, "Unknown", false, "Psionic power not found.", 0);
            }
            // CL-style 2d20 check
            int d1 = DiceUtil.rollDSides(20);
            int d2 = DiceUtil.rollDSides(20);
            boolean success = (d1 != 20 && d2 != 20); // no complication
            return new PsionicPowerResult(powerId, power.getName(), success,
                    "Psionic power used: " + power.getName() + " (rolled " + d1 + ", " + d2 + ")",
                    success ? power.getPowerPointsCost() : 0);
        }
    }

    /**
     * Adapts Captain's Log missions to EncounterService interface.
     * Missions provide structured encounters with complications and narrative beats.
     */
    private static class CLEncounterAdapter implements EncounterService {
        private final CaptainLogMissionService missionService;
        private final List<Encounter> encounters;

        CLEncounterAdapter(CaptainLogMissionService missionService) {
            this.missionService = missionService;
            this.encounters = new ArrayList<>();
            addEncounter(1L, "First Contact", "Encounter with an unknown alien species",
                    "Medium", 500, 1, 20, "Diplomacy", "Space");
            addEncounter(2L, "Pirate Ambush", "Romulan or Cardassian raiders attack",
                    "Hard", 750, 1, 20, "Combat", "Space");
            addEncounter(3L, "Spatial Anomaly", "Wormhole, nebula, or temporal distortion",
                    "Medium", 400, 1, 20, "Exploration", "Space");
            addEncounter(4L, "Distress Signal", "Unknown vessel requests assistance",
                    "Easy", 300, 1, 20, "Rescue", "Space");
            addEncounter(5L, "Political Intrigue", "Diplomatic negotiations with hidden agendas",
                    "Hard", 600, 1, 20, "Diplomacy", "Station");
            addEncounter(6L, "Boarding Action", "Hostiles attempt to board the ship",
                    "Hard", 700, 1, 20, "Combat", "Ship");
            addEncounter(7L, "Black Market", "Opportunity for contraband trade",
                    "Medium", 350, 1, 20, "Social", "Station");
            addEncounter(8L, "System Failure", "Critical ship systems malfunction",
                    "Medium", 450, 1, 20, "Engineering", "Ship");
        }

        private void addEncounter(Long id, String name, String description, String difficulty,
                                  int xp, int minLevel, int maxLevel, String type, String area) {
            Encounter e = new Encounter();
            e.setId(id);
            e.setName(name);
            e.setDescription(description);
            e.setDifficulty(difficulty);
            e.setXpReward(xp);
            e.setMinLevel(minLevel);
            e.setMaxLevel(maxLevel);
            e.setEncounterType(type);
            e.setAreaId(area);
            encounters.add(e);
        }

        @Override
        public List<Encounter> listEncounters() {
            return encounters;
        }

        @Override
        public Encounter getEncounter(Long encounterId) {
            return encounters.stream().filter(e -> e.getId().equals(encounterId)).findFirst().orElse(null);
        }

        @Override
        public Encounter generateRandomEncounter(String areaId, int level) {
            List<Encounter> filtered = encounters.stream()
                    .filter(e -> areaId == null || areaId.equals(e.getAreaId()))
                    .filter(e -> level >= e.getMinLevel() && level <= e.getMaxLevel())
                    .collect(Collectors.toList());
            if (filtered.isEmpty()) return encounters.get(0);
            return filtered.get(new Random().nextInt(filtered.size()));
        }

        @Override
        public EncounterResult resolveEncounter(Long encounterId, List<Long> characterIds) {
            Encounter encounter = encounters.stream()
                    .filter(e -> e.getId().equals(encounterId)).findFirst().orElse(null);
            if (encounter == null) {
                return new EncounterResult(encounterId, "Unknown", false, "Encounter not found.", 0);
            }
            // CL-style resolution: 2d20 task attempt
            int d1 = DiceUtil.rollDSides(20);
            int d2 = DiceUtil.rollDSides(20);
            boolean complication = (d1 == 20 || d2 == 20);
            boolean success = (d1 + d2) <= 12; // simplified target
            if (complication) success = !success; // complication flips outcome
            return new EncounterResult(encounterId, encounter.getName(), success,
                    success ? "Encounter resolved successfully!" : "Encounter resolved with complications.",
                    success ? encounter.getXpReward() : encounter.getXpReward() / 2);
        }
    }

    /**
     * Adapts Captain's Log characters to CharacterService interface.
     * Maps CL attributes (Fitness, Technical, Strength, Reason, Insight, Presence) to shared 6-ability system.
     */
    private static class CLCharacterAdapter implements CharacterService {
        private final CaptainLogCharacterService characterService;

        CLCharacterAdapter(CaptainLogCharacterService characterService) {
            this.characterService = characterService;
        }

        @Override
        public CharacterInfo getCharacter(Long characterId) {
            Optional<CaptainLogCharacter> opt = characterService.getCharacter(characterId);
            if (!opt.isPresent()) return null;
            CaptainLogCharacter cl = opt.get();
            CharacterInfo info = new CharacterInfo();
            info.setId(cl.getId());
            info.setName(cl.getName());
            info.setLevel(cl.getRank().ordinal() + 1);
            info.setExperiencePoints(cl.getExperiencePoints());
            // CL uses Constitution for hit points
            int con = cl.getConstitution();
            info.setHitPoints(con * 5);
            info.setMaxHitPoints(con * 5);
            // Map CL attributes to shared 6-ability system
            info.setStrScore(cl.getStrength());
            info.setDexScore(cl.getFitness());
            info.setConScore(con);
            info.setIntScore(cl.getReason());
            info.setWisScore(cl.getInsight());
            info.setChaScore(cl.getPresence());
            info.setLocation(cl.getAssignment() != null ? cl.getAssignment().name() : "Unknown");
            return info;
        }

        @Override
        public List<CharacterInfo> listCharacters() {
            return characterService.listCharacters().stream()
                    .map(cl -> {
                        CharacterInfo info = new CharacterInfo();
                        info.setId(cl.getId());
                        info.setName(cl.getName());
                        info.setLevel(cl.getRank().ordinal() + 1);
                        info.setExperiencePoints(cl.getExperiencePoints());
                        int con = cl.getConstitution();
                        info.setHitPoints(con * 5);
                        info.setMaxHitPoints(con * 5);
                        info.setStrScore(cl.getStrength());
                        info.setDexScore(cl.getFitness());
                        info.setConScore(con);
                        info.setIntScore(cl.getReason());
                        info.setWisScore(cl.getInsight());
                        info.setChaScore(cl.getPresence());
                        info.setLocation(cl.getAssignment() != null ? cl.getAssignment().name() : "Unknown");
                        return info;
                    })
                    .collect(Collectors.toList());
        }

        @Override
        public void updateAbilityScores(Long characterId, int str, int dex, int con, int intelligence, int wis, int cha) {
            // CL characters use 0-5 scale; clamp values
            Optional<CaptainLogCharacter> opt = characterService.getCharacter(characterId);
            opt.ifPresent(cl -> {
                cl.setStrength(Math.min(5, Math.max(0, str)));
                cl.setFitness(Math.min(5, Math.max(0, dex)));
                cl.setConstitution(Math.min(5, Math.max(0, con)));
                cl.setReason(Math.min(5, Math.max(0, intelligence)));
                cl.setInsight(Math.min(5, Math.max(0, wis)));
                cl.setPresence(Math.min(5, Math.max(0, cha)));
            });
        }

        @Override
        public CharacterInfo awardExperience(Long characterId, int xp) {
            Optional<CaptainLogCharacter> opt = characterService.getCharacter(characterId);
            if (!opt.isPresent()) return null;
            CaptainLogCharacter cl = opt.get();
            int currentXp = cl.getExperiencePoints();
            cl.setExperiencePoints(currentXp + xp);
            return getCharacter(characterId);
        }
    }

    /**
     * Adapts Captain's Log campaigns and missions to CampaignService interface.
     */
    private static class CLCampaignAdapter implements com.yisy.modules.doramcu.gamecore.CampaignService {
        private final com.yisy.modules.doramcu.captainslog.service.CampaignService clCampaignService;
        private final CaptainLogMissionService missionService;

        CLCampaignAdapter(com.yisy.modules.doramcu.captainslog.service.CampaignService clCampaignService, CaptainLogMissionService missionService) {
            this.clCampaignService = clCampaignService;
            this.missionService = missionService;
        }

        @Override
        public List<com.yisy.modules.doramcu.gamecore.CampaignService.Campaign> listCampaigns() {
            // CL CampaignService requires characterId; return empty list for shared interface
            return Collections.emptyList();
        }

        @Override
        public com.yisy.modules.doramcu.gamecore.CampaignService.Campaign getCampaign(Long campaignId) {
            Optional<CaptainLogCampaign> opt = clCampaignService.getCampaign(campaignId);
            if (!opt.isPresent()) return null;
            CaptainLogCampaign cl = opt.get();
            com.yisy.modules.doramcu.gamecore.CampaignService.Campaign c = new com.yisy.modules.doramcu.gamecore.CampaignService.Campaign();
            c.setId(cl.getId());
            c.setName(cl.getTitle());
            c.setDescription(cl.getDescription());
            c.setStatus(cl.getStatus() != null ? cl.getStatus().name() : "ACTIVE");
            c.setCurrentSession(0);
            return c;
        }

        @Override
        public com.yisy.modules.doramcu.gamecore.CampaignService.Campaign createCampaign(String name, String description) {
            // CL CampaignService requires characterId; create stub for shared interface
            com.yisy.modules.doramcu.gamecore.CampaignService.Campaign c = new com.yisy.modules.doramcu.gamecore.CampaignService.Campaign();
            c.setName(name);
            c.setDescription(description);
            c.setStatus("ACTIVE");
            c.setCurrentSession(0);
            return c;
        }

        @Override
        public void addCharacterToCampaign(Long campaignId, Long characterId) {
            // CL CampaignService doesn't have this method; no-op
        }
    }

    /**
     * Adapts Captain's Log rules to RulesService interface.
     */
    private static class CLRulesAdapter implements com.yisy.modules.doramcu.gamecore.RulesService {
        private final com.yisy.modules.doramcu.captainslog.service.RulesService clRulesService;

        CLRulesAdapter(com.yisy.modules.doramcu.captainslog.service.RulesService clRulesService) {
            this.clRulesService = clRulesService;
        }

        @Override
        public List<RulesEntry> listRules() {
            // CL RulesService uses getCategories() + getRulesByCategory()
            List<RulesEntry> entries = new ArrayList<>();
            for (String category : clRulesService.getCategories()) {
                List<com.yisy.modules.doramcu.shared.model.RulesEntry> clRules = clRulesService.getRulesByCategory(category);
                for (com.yisy.modules.doramcu.shared.model.RulesEntry cl : clRules) {
                    RulesEntry entry = new RulesEntry();
                    entry.setId(cl.getId());
                    entry.setTitle(cl.getTitle());
                    entry.setCategory(cl.getCategory());
                    entry.setText(cl.getText());
                    entry.setSource(cl.getSource());
                    entry.setGameSystem(cl.getGameSystem());
                    entry.setChapter(cl.getChapter());
                    entries.add(entry);
                }
            }
            return entries;
        }

        @Override
        public List<RulesEntry> searchRules(String keyword) {
            List<com.yisy.modules.doramcu.shared.model.RulesEntry> clRules = clRulesService.searchRules(keyword);
            return clRules.stream().map(cl -> {
                RulesEntry entry = new RulesEntry();
                entry.setId(cl.getId());
                entry.setTitle(cl.getTitle());
                entry.setCategory(cl.getCategory());
                entry.setText(cl.getText());
                entry.setSource(cl.getSource());
                entry.setGameSystem(cl.getGameSystem());
                entry.setChapter(cl.getChapter());
                return entry;
            }).collect(Collectors.toList());
        }

        @Override
        public RulesEntry getRule(Long id) {
            // CL RulesService doesn't have getRule by ID; search all categories
            for (String category : clRulesService.getCategories()) {
                List<com.yisy.modules.doramcu.shared.model.RulesEntry> clRules = clRulesService.getRulesByCategory(category);
                for (com.yisy.modules.doramcu.shared.model.RulesEntry cl : clRules) {
                    if (cl.getId().equals(id)) {
                        RulesEntry entry = new RulesEntry();
                        entry.setId(cl.getId());
                        entry.setTitle(cl.getTitle());
                        entry.setCategory(cl.getCategory());
                        entry.setText(cl.getText());
                        entry.setSource(cl.getSource());
                        entry.setGameSystem(cl.getGameSystem());
                        entry.setChapter(cl.getChapter());
                        return entry;
                    }
                }
            }
            return null;
        }
    }

    @Override
    public RulesEngine getRulesEngine() {
        return rulesEngine;
    }
}
