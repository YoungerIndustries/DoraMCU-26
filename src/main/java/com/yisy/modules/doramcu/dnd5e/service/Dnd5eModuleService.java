package com.yisy.modules.doramcu.dnd5e.service;

import com.yisy.modules.doramcu.dnd5e.model.*;
import com.yisy.modules.doramcu.dnd5e.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing D&D 5e Adventure Modules.
 * <p>
 * A module is a complete adventure that ties together all building blocks:
 * adventure hooks, dungeon rooms, NPCs, encounters, villains, bastions,
 * tension pools, growing magic items, dark gifts, feats, crafting recipes,
 * and psionic powers.
 * </p>
 * <p>
 * This service provides:
 * - Full CRUD for modules
 * - Component linking/unlinking for all subsystems
 * - Auto-generation of module summaries
 * - Theme-based component suggestions
 * - Module statistics and validation
 * - Example module population
 * </p>
 */
@Service
public class Dnd5eModuleService {

    private static final Logger log = LoggerFactory.getLogger(Dnd5eModuleService.class);

    private final Dnd5eModuleRepository moduleRepository;
    private final Dnd5eAdventureHookService adventureHookService;
    private final Dnd5eDungeonRoomService dungeonRoomService;
    private final Dnd5eNpcService npcService;
    private final Dnd5eEncounterService encounterService;
    private final Dnd5eVillainService villainService;
    private final Dnd5eBastionService bastionService;
    private final Dnd5eTensionPoolService tensionPoolService;
    private final Dnd5eGrowingMagicItemService growingItemService;
    private final Dnd5eHorrorService horrorService;
    private final Dnd5eFeatService featService;
    private final Dnd5eCraftingService craftingService;
    private final Dnd5ePsionService psionService;

    public Dnd5eModuleService(Dnd5eModuleRepository moduleRepository,
                              Dnd5eAdventureHookService adventureHookService,
                              Dnd5eDungeonRoomService dungeonRoomService,
                              Dnd5eNpcService npcService,
                              Dnd5eEncounterService encounterService,
                              Dnd5eVillainService villainService,
                              Dnd5eBastionService bastionService,
                              Dnd5eTensionPoolService tensionPoolService,
                              Dnd5eGrowingMagicItemService growingItemService,
                              Dnd5eHorrorService horrorService,
                              Dnd5eFeatService featService,
                              Dnd5eCraftingService craftingService,
                              Dnd5ePsionService psionService) {
        this.moduleRepository = moduleRepository;
        this.adventureHookService = adventureHookService;
        this.dungeonRoomService = dungeonRoomService;
        this.npcService = npcService;
        this.encounterService = encounterService;
        this.villainService = villainService;
        this.bastionService = bastionService;
        this.tensionPoolService = tensionPoolService;
        this.growingItemService = growingItemService;
        this.horrorService = horrorService;
        this.featService = featService;
        this.craftingService = craftingService;
        this.psionService = psionService;
    }

    // ==================== CRUD ====================

    public Dnd5eModule createModule(String name, String tagline, String description, String theme,
                                    int minLevel, int maxLevel, int estimatedSessions) {
        Dnd5eModule module = new Dnd5eModule();
        module.setName(name);
        module.setTagline(tagline);
        module.setDescription(description);
        module.setTheme(theme);
        module.setMinLevel(minLevel);
        module.setMaxLevel(maxLevel);
        module.setEstimatedSessions(estimatedSessions);
        module.setStatus("Draft");
        return moduleRepository.save(module);
    }

    public Dnd5eModule getModule(Long id) {
        return moduleRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Module not found: " + id));
    }

    public List<Dnd5eModule> listModules() {
        return moduleRepository.findByActiveTrue();
    }

    public List<Dnd5eModule> listModulesByTheme(String theme) {
        return moduleRepository.findByThemeAndActiveTrue(theme);
    }

    public List<Dnd5eModule> listModulesByStatus(String status) {
        return moduleRepository.findByStatusAndActiveTrue(status);
    }

    public List<Dnd5eModule> listModulesByPartyLevel(int partyLevel) {
        return moduleRepository.findByPartyLevel(partyLevel);
    }

    public List<Dnd5eModule> searchModules(String keyword) {
        return moduleRepository.searchByKeyword(keyword);
    }

    public Dnd5eModule updateModule(Long id, String name, String tagline, String description,
                                    String theme, int minLevel, int maxLevel, int estimatedSessions,
                                    String status, String summary, String dmNotes) {
        Dnd5eModule module = getModule(id);
        if (name != null) module.setName(name);
        if (tagline != null) module.setTagline(tagline);
        if (description != null) module.setDescription(description);
        if (theme != null) module.setTheme(theme);
        module.setMinLevel(minLevel);
        module.setMaxLevel(maxLevel);
        module.setEstimatedSessions(estimatedSessions);
        if (status != null) module.setStatus(status);
        if (summary != null) module.setSummary(summary);
        if (dmNotes != null) module.setDmNotes(dmNotes);
        return moduleRepository.save(module);
    }

    public void deleteModule(Long id) {
        Dnd5eModule module = getModule(id);
        module.setActive(false);
        moduleRepository.save(module);
    }

    // ==================== Component Linking ====================

    /**
     * Add a component ID to the specified field.
     */
    private void addComponentId(Dnd5eModule module, String field, String value, String componentId) {
        List<String> ids = parseIds(value);
        if (!ids.contains(componentId)) {
            ids.add(componentId);
            setField(module, field, ids);
        }
    }

    /**
     * Remove a component ID from the specified field.
     */
    private void removeComponentId(Dnd5eModule module, String field, String value, String componentId) {
        List<String> ids = parseIds(value);
        ids.removeIf(id -> id.equals(componentId));
        setField(module, field, ids);
    }

    private void setField(Dnd5eModule module, String field, List<String> ids) {
        String joined = ids.isEmpty() ? "" : String.join(",", ids);
        switch (field) {
            case "hookIds" -> module.setHookIds(joined);
            case "roomIds" -> module.setRoomIds(joined);
            case "npcIds" -> module.setNpcIds(joined);
            case "encounterIds" -> module.setEncounterIds(joined);
            case "villainIds" -> module.setVillainIds(joined);
            case "bastionIds" -> module.setBastionIds(joined);
            case "tensionPoolIds" -> module.setTensionPoolIds(joined);
            case "growingItemIds" -> module.setGrowingItemIds(joined);
            case "darkGiftIds" -> module.setDarkGiftIds(joined);
            case "featIds" -> module.setFeatIds(joined);
            case "craftingRecipeIds" -> module.setCraftingRecipeIds(joined);
            case "psionicPowerIds" -> module.setPsionicPowerIds(joined);
        }
    }

    private List<String> parseIds(String ids) {
        if (ids == null || ids.trim().isEmpty()) return new ArrayList<>();
        return Arrays.stream(ids.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    // --- Adventure Hooks ---

    public Dnd5eModule addHook(Long moduleId, String hookId) {
        Dnd5eModule module = getModule(moduleId);
        addComponentId(module, "hookIds", module.getHookIds(), hookId);
        return moduleRepository.save(module);
    }

    public Dnd5eModule removeHook(Long moduleId, String hookId) {
        Dnd5eModule module = getModule(moduleId);
        removeComponentId(module, "hookIds", module.getHookIds(), hookId);
        return moduleRepository.save(module);
    }

    public List<String> getHookIds(Long moduleId) {
        return parseIds(getModule(moduleId).getHookIds());
    }

    // --- Dungeon Rooms ---

    public Dnd5eModule addRoom(Long moduleId, String roomId) {
        Dnd5eModule module = getModule(moduleId);
        addComponentId(module, "roomIds", module.getRoomIds(), roomId);
        return moduleRepository.save(module);
    }

    public Dnd5eModule removeRoom(Long moduleId, String roomId) {
        Dnd5eModule module = getModule(moduleId);
        removeComponentId(module, "roomIds", module.getRoomIds(), roomId);
        return moduleRepository.save(module);
    }

    public List<String> getRoomIds(Long moduleId) {
        return parseIds(getModule(moduleId).getRoomIds());
    }

    // --- NPCs ---

    public Dnd5eModule addNpc(Long moduleId, String npcId) {
        Dnd5eModule module = getModule(moduleId);
        addComponentId(module, "npcIds", module.getNpcIds(), npcId);
        return moduleRepository.save(module);
    }

    public Dnd5eModule removeNpc(Long moduleId, String npcId) {
        Dnd5eModule module = getModule(moduleId);
        removeComponentId(module, "npcIds", module.getNpcIds(), npcId);
        return moduleRepository.save(module);
    }

    public List<String> getNpcIds(Long moduleId) {
        return parseIds(getModule(moduleId).getNpcIds());
    }

    // --- Encounters ---

    public Dnd5eModule addEncounter(Long moduleId, String encounterId) {
        Dnd5eModule module = getModule(moduleId);
        addComponentId(module, "encounterIds", module.getEncounterIds(), encounterId);
        return moduleRepository.save(module);
    }

    public Dnd5eModule removeEncounter(Long moduleId, String encounterId) {
        Dnd5eModule module = getModule(moduleId);
        removeComponentId(module, "encounterIds", module.getEncounterIds(), encounterId);
        return moduleRepository.save(module);
    }

    public List<String> getEncounterIds(Long moduleId) {
        return parseIds(getModule(moduleId).getEncounterIds());
    }

    // --- Villains ---

    public Dnd5eModule addVillain(Long moduleId, String villainId) {
        Dnd5eModule module = getModule(moduleId);
        addComponentId(module, "villainIds", module.getVillainIds(), villainId);
        return moduleRepository.save(module);
    }

    public Dnd5eModule removeVillain(Long moduleId, String villainId) {
        Dnd5eModule module = getModule(moduleId);
        removeComponentId(module, "villainIds", module.getVillainIds(), villainId);
        return moduleRepository.save(module);
    }

    public List<String> getVillainIds(Long moduleId) {
        return parseIds(getModule(moduleId).getVillainIds());
    }

    // --- Bastions ---

    public Dnd5eModule addBastion(Long moduleId, String bastionId) {
        Dnd5eModule module = getModule(moduleId);
        addComponentId(module, "bastionIds", module.getBastionIds(), bastionId);
        return moduleRepository.save(module);
    }

    public Dnd5eModule removeBastion(Long moduleId, String bastionId) {
        Dnd5eModule module = getModule(moduleId);
        removeComponentId(module, "bastionIds", module.getBastionIds(), bastionId);
        return moduleRepository.save(module);
    }

    public List<String> getBastionIds(Long moduleId) {
        return parseIds(getModule(moduleId).getBastionIds());
    }

    // --- Tension Pools ---

    public Dnd5eModule addTensionPool(Long moduleId, String tensionPoolId) {
        Dnd5eModule module = getModule(moduleId);
        addComponentId(module, "tensionPoolIds", module.getTensionPoolIds(), tensionPoolId);
        return moduleRepository.save(module);
    }

    public Dnd5eModule removeTensionPool(Long moduleId, String tensionPoolId) {
        Dnd5eModule module = getModule(moduleId);
        removeComponentId(module, "tensionPoolIds", module.getTensionPoolIds(), tensionPoolId);
        return moduleRepository.save(module);
    }

    public List<String> getTensionPoolIds(Long moduleId) {
        return parseIds(getModule(moduleId).getTensionPoolIds());
    }

    // --- Growing Magic Items ---

    public Dnd5eModule addGrowingItem(Long moduleId, String itemId) {
        Dnd5eModule module = getModule(moduleId);
        addComponentId(module, "growingItemIds", module.getGrowingItemIds(), itemId);
        return moduleRepository.save(module);
    }

    public Dnd5eModule removeGrowingItem(Long moduleId, String itemId) {
        Dnd5eModule module = getModule(moduleId);
        removeComponentId(module, "growingItemIds", module.getGrowingItemIds(), itemId);
        return moduleRepository.save(module);
    }

    public List<String> getGrowingItemIds(Long moduleId) {
        return parseIds(getModule(moduleId).getGrowingItemIds());
    }

    // --- Dark Gifts ---

    public Dnd5eModule addDarkGift(Long moduleId, String darkGiftId) {
        Dnd5eModule module = getModule(moduleId);
        addComponentId(module, "darkGiftIds", module.getDarkGiftIds(), darkGiftId);
        return moduleRepository.save(module);
    }

    public Dnd5eModule removeDarkGift(Long moduleId, String darkGiftId) {
        Dnd5eModule module = getModule(moduleId);
        removeComponentId(module, "darkGiftIds", module.getDarkGiftIds(), darkGiftId);
        return moduleRepository.save(module);
    }

    public List<String> getDarkGiftIds(Long moduleId) {
        return parseIds(getModule(moduleId).getDarkGiftIds());
    }

    // --- Feats ---

    public Dnd5eModule addFeat(Long moduleId, String featId) {
        Dnd5eModule module = getModule(moduleId);
        addComponentId(module, "featIds", module.getFeatIds(), featId);
        return moduleRepository.save(module);
    }

    public Dnd5eModule removeFeat(Long moduleId, String featId) {
        Dnd5eModule module = getModule(moduleId);
        removeComponentId(module, "featIds", module.getFeatIds(), featId);
        return moduleRepository.save(module);
    }

    public List<String> getFeatIds(Long moduleId) {
        return parseIds(getModule(moduleId).getFeatIds());
    }

    // --- Crafting Recipes ---

    public Dnd5eModule addCraftingRecipe(Long moduleId, String recipeId) {
        Dnd5eModule module = getModule(moduleId);
        addComponentId(module, "craftingRecipeIds", module.getCraftingRecipeIds(), recipeId);
        return moduleRepository.save(module);
    }

    public Dnd5eModule removeCraftingRecipe(Long moduleId, String recipeId) {
        Dnd5eModule module = getModule(moduleId);
        removeComponentId(module, "craftingRecipeIds", module.getCraftingRecipeIds(), recipeId);
        return moduleRepository.save(module);
    }

    public List<String> getCraftingRecipeIds(Long moduleId) {
        return parseIds(getModule(moduleId).getCraftingRecipeIds());
    }

    // --- Psionic Powers ---

    public Dnd5eModule addPsionicPower(Long moduleId, String powerId) {
        Dnd5eModule module = getModule(moduleId);
        addComponentId(module, "psionicPowerIds", module.getPsionicPowerIds(), powerId);
        return moduleRepository.save(module);
    }

    public Dnd5eModule removePsionicPower(Long moduleId, String powerId) {
        Dnd5eModule module = getModule(moduleId);
        removeComponentId(module, "psionicPowerIds", module.getPsionicPowerIds(), powerId);
        return moduleRepository.save(module);
    }

    public List<String> getPsionicPowerIds(Long moduleId) {
        return parseIds(getModule(moduleId).getPsionicPowerIds());
    }

    // ==================== Module Summary ====================

    /**
     * Generate a summary for the module based on its components.
     */
    public String generateSummary(Long moduleId) {
        Dnd5eModule module = getModule(moduleId);
        StringBuilder sb = new StringBuilder();
        sb.append("Module: ").append(module.getName());
        if (module.getTagline() != null && !module.getTagline().isEmpty()) {
            sb.append(" - ").append(module.getTagline());
        }
        sb.append("\n");
        sb.append("Theme: ").append(module.getTheme()).append("\n");
        sb.append("Levels: ").append(module.getMinLevel()).append("-").append(module.getMaxLevel()).append("\n");
        sb.append("Estimated Sessions: ").append(module.getEstimatedSessions()).append("\n");
        sb.append("\nComponents:\n");

        int hooks = parseIds(module.getHookIds()).size();
        int rooms = parseIds(module.getRoomIds()).size();
        int npcs = parseIds(module.getNpcIds()).size();
        int encounters = parseIds(module.getEncounterIds()).size();
        int villains = parseIds(module.getVillainIds()).size();
        int bastions = parseIds(module.getBastionIds()).size();
        int tensionPools = parseIds(module.getTensionPoolIds()).size();
        int growingItems = parseIds(module.getGrowingItemIds()).size();
        int darkGifts = parseIds(module.getDarkGiftIds()).size();
        int feats = parseIds(module.getFeatIds()).size();
        int craftingRecipes = parseIds(module.getCraftingRecipeIds()).size();
        int psionicPowers = parseIds(module.getPsionicPowerIds()).size();

        sb.append("  Adventure Hooks: ").append(hooks).append("\n");
        sb.append("  Dungeon Rooms: ").append(rooms).append("\n");
        sb.append("  NPCs: ").append(npcs).append("\n");
        sb.append("  Encounters: ").append(encounters).append("\n");
        sb.append("  Villains: ").append(villains).append("\n");
        sb.append("  Bastions: ").append(bastions).append("\n");
        sb.append("  Tension Pools: ").append(tensionPools).append("\n");
        sb.append("  Growing Magic Items: ").append(growingItems).append("\n");
        sb.append("  Dark Gifts: ").append(darkGifts).append("\n");
        sb.append("  Feats: ").append(feats).append("\n");
        sb.append("  Crafting Recipes: ").append(craftingRecipes).append("\n");
        sb.append("  Psionic Powers: ").append(psionicPowers).append("\n");

        int total = hooks + rooms + npcs + encounters + villains + bastions + tensionPools +
                    growingItems + darkGifts + feats + craftingRecipes + psionicPowers;
        sb.append("\nTotal Components: ").append(total).append("\n");

        // Completion percentage
        int expectedComponents = 12; // one of each type
        int completedTypes = 0;
        if (hooks > 0) completedTypes++;
        if (rooms > 0) completedTypes++;
        if (npcs > 0) completedTypes++;
        if (encounters > 0) completedTypes++;
        if (villains > 0) completedTypes++;
        if (bastions > 0) completedTypes++;
        if (tensionPools > 0) completedTypes++;
        if (growingItems > 0) completedTypes++;
        if (darkGifts > 0) completedTypes++;
        if (feats > 0) completedTypes++;
        if (craftingRecipes > 0) completedTypes++;
        if (psionicPowers > 0) completedTypes++;

        int completionPercent = (int) ((completedTypes / (double) expectedComponents) * 100);
        sb.append("Completion: ").append(completionPercent).append("% (").append(completedTypes).append("/").append(expectedComponents).append(" types)\n");

        String summary = sb.toString();
        module.setSummary(summary);
        moduleRepository.save(module);
        return summary;
    }

    // ==================== Module Statistics ====================

    /**
     * Get statistics for a module.
     */
    public Map<String, Object> getModuleStatistics(Long moduleId) {
        Dnd5eModule module = getModule(moduleId);
        Map<String, Object> stats = new HashMap<>();

        stats.put("id", module.getId());
        stats.put("name", module.getName());
        stats.put("theme", module.getTheme());
        stats.put("status", module.getStatus());
        stats.put("playCount", module.getPlayCount());
        stats.put("rating", module.getRating());

        Map<String, Integer> componentCounts = new HashMap<>();
        componentCounts.put("hooks", parseIds(module.getHookIds()).size());
        componentCounts.put("rooms", parseIds(module.getRoomIds()).size());
        componentCounts.put("npcs", parseIds(module.getNpcIds()).size());
        componentCounts.put("encounters", parseIds(module.getEncounterIds()).size());
        componentCounts.put("villains", parseIds(module.getVillainIds()).size());
        componentCounts.put("bastions", parseIds(module.getBastionIds()).size());
        componentCounts.put("tensionPools", parseIds(module.getTensionPoolIds()).size());
        componentCounts.put("growingItems", parseIds(module.getGrowingItemIds()).size());
        componentCounts.put("darkGifts", parseIds(module.getDarkGiftIds()).size());
        componentCounts.put("feats", parseIds(module.getFeatIds()).size());
        componentCounts.put("craftingRecipes", parseIds(module.getCraftingRecipeIds()).size());
        componentCounts.put("psionicPowers", parseIds(module.getPsionicPowerIds()).size());
        stats.put("components", componentCounts);

        int total = componentCounts.values().stream().mapToInt(Integer::intValue).sum();
        stats.put("totalComponents", total);

        stats.put("createdAt", module.getCreatedAt());
        stats.put("updatedAt", module.getUpdatedAt());

        return stats;
    }

    // ==================== Play Count & Rating ====================

    public Dnd5eModule incrementPlayCount(Long moduleId) {
        Dnd5eModule module = getModule(moduleId);
        module.setPlayCount(module.getPlayCount() + 1);
        return moduleRepository.save(module);
    }

    public Dnd5eModule setRating(Long moduleId, double rating) {
        Dnd5eModule module = getModule(moduleId);
        module.setRating(rating);
        return moduleRepository.save(module);
    }

    // ==================== Theme Suggestions ====================

    /**
     * Get theme-appropriate component suggestions for a module.
     */
    public Map<String, Object> getThemeSuggestions(String theme) {
        Map<String, Object> suggestions = new HashMap<>();
        suggestions.put("theme", theme);

        Map<String, List<String>> recommendedComponents = new HashMap<>();

        switch (theme.toLowerCase()) {
            case "horror" -> {
                recommendedComponents.put("darkGifts", List.of("Undead", "Fiend", "Aberration"));
                recommendedComponents.put("tensionPools", List.of("Castle Ward", "Dark Forest"));
                recommendedComponents.put("villains", List.of("Lich", "Archfey"));
                recommendedComponents.put("encounters", List.of("Fear checks", "Dread thresholds"));
            }
            case "high magic" -> {
                recommendedComponents.put("growingItems", List.of("Staff of the Arcane", "Amulet of Arcana"));
                recommendedComponents.put("psionicPowers", List.of("Telepath", "Psi Warper"));
                recommendedComponents.put("feats", List.of("Magic Initiate", "Spell Sniper"));
                recommendedComponents.put("craftingRecipes", List.of("Magic items", "Spell scrolls"));
            }
            case "sandbox" -> {
                recommendedComponents.put("tensionPools", List.of("Open world areas"));
                recommendedComponents.put("bastions", List.of("Party base", "Trading post"));
                recommendedComponents.put("npcs", List.of("Faction leaders", "Merchants"));
                recommendedComponents.put("encounters", List.of("Wandering encounters"));
            }
            case "dungeon crawl" -> {
                recommendedComponents.put("rooms", List.of("Traps", "Treasure rooms", "Boss rooms"));
                recommendedComponents.put("encounters", List.of("Combat encounters", "Puzzles"));
                recommendedComponents.put("npcs", List.of("Guardians", "Trapped souls"));
                recommendedComponents.put("growingItems", List.of("Dungeon loot"));
            }
            case "feywild" -> {
                recommendedComponents.put("npcs", List.of("Archfey", "Fey creatures"));
                recommendedComponents.put("rooms", List.of("Fey groves", "Enchanted areas"));
                recommendedComponents.put("darkGifts", List.of("Fey gifts"));
                recommendedComponents.put("villains", List.of("Archfey antagonists"));
            }
            case "steampunk" -> {
                recommendedComponents.put("craftingRecipes", List.of("Magitech devices", "Artificer items"));
                recommendedComponents.put("bastions", List.of("Workshop", "Factory"));
                recommendedComponents.put("feats", List.of("Artificer feats"));
                recommendedComponents.put("growingItems", List.of("Magitech weapons"));
            }
            default -> {
                recommendedComponents.put("general", List.of("Mix of all component types"));
            }
        }

        suggestions.put("recommendedComponents", recommendedComponents);

        // Suggested hooks by theme
        List<Dnd5eAdventureHook> hooks = adventureHookService.listHooksByTheme(theme);
        suggestions.put("availableHooks", hooks.size());

        return suggestions;
    }

    // ==================== Module Validation ====================

    /**
     * Validate a module and return a list of issues.
     */
    public List<String> validateModule(Long moduleId) {
        Dnd5eModule module = getModule(moduleId);
        List<String> issues = new ArrayList<>();

        if (module.getName() == null || module.getName().trim().isEmpty()) {
            issues.add("Module name is required");
        }
        if (module.getDescription() == null || module.getDescription().trim().isEmpty()) {
            issues.add("Module description is required");
        }
        if (module.getTheme() == null || module.getTheme().trim().isEmpty()) {
            issues.add("Module theme is required");
        }
        if (module.getMinLevel() > module.getMaxLevel()) {
            issues.add("Min level cannot be greater than max level");
        }
        if (module.getMinLevel() < 1 || module.getMinLevel() > 20) {
            issues.add("Min level must be between 1 and 20");
        }
        if (module.getMaxLevel() < 1 || module.getMaxLevel() > 20) {
            issues.add("Max level must be between 1 and 20");
        }

        // Check for empty components
        if (parseIds(module.getHookIds()).isEmpty()) {
            issues.add("No adventure hooks linked — consider adding hooks for player engagement");
        }
        if (parseIds(module.getNpcIds()).isEmpty()) {
            issues.add("No NPCs linked — consider adding NPCs for world-building");
        }
        if (parseIds(module.getEncounterIds()).isEmpty()) {
            issues.add("No encounters linked — consider adding encounters for challenge");
        }

        return issues;
    }

    // ==================== Reference Data ====================

    public List<String> listThemes() {
        return List.of("Horror", "High Magic", "Sandbox", "Dungeon Crawl", "Feywild", "Steampunk");
    }

    public List<String> listStatuses() {
        return List.of("Draft", "In Progress", "Complete", "Archived");
    }

    public List<Dnd5eModule> findMostPlayed() {
        return moduleRepository.findMostPlayed();
    }

    public List<Dnd5eModule> findTopRated(double minRating) {
        return moduleRepository.findByRating(minRating);
    }

    // ==================== Database Initialization ====================

    /**
     * Populate example modules if the database is empty.
     */
    public void populateExampleModules() {
        if (moduleRepository.count() > 0) {
            log.info("Modules already populated, skipping initialization");
            return;
        }

        log.info("Populating example modules");

        // Horror module
        Dnd5eModule horrorModule = createModule(
                "Curse of the Shadowcrypt",
                "A Ravenloft-style horror adventure",
                "The party is drawn into a Domain of Dread where an ancient lich's phylactery has corrupted the land. " +
                "Dark Gifts await those brave enough to face the horrors, but each gift comes with a curse. " +
                "The tension rises as the Darklord's power grows, and the party must find a way to break the curse " +
                "before they become permanent residents of the Domain.",
                "Horror",
                3, 7, 6
        );

        // High Magic module
        Dnd5eModule highMagicModule = createModule(
                "Arcana Unbound",
                "A high-magic adventure with growing items",
                "In a world where magic is abundant, the party discovers ancient growing magic items that scale " +
                "with their power. Psionic powers emerge as the veil between minds thins, and crafting legendary " +
                "artifacts becomes the key to stopping an arcane catastrophe. The party's bastion serves as a " +
                "research hub for their magical discoveries.",
                "High Magic",
                5, 12, 8
        );

        // Sandbox module
        Dnd5eModule sandboxModule = createModule(
                "Kingdoms at War",
                "An open-world sandbox adventure",
                "The kingdom is torn apart by warring factions, and the party must navigate political intrigue, " +
                "wandering encounters, and shifting alliances. Tension pools track the escalating conflict, " +
                "while the party's bastion serves as their base of operations. NPCs from all factions offer " +
                "quests, and the party's choices determine the outcome of the war.",
                "Sandbox",
                1, 10, 12
        );

        // Dungeon Crawl module
        Dnd5eModule dungeonModule = createModule(
                "Tomb of the Forgotten King",
                "A classic dungeon crawl adventure",
                "Deep beneath the ruins of an ancient kingdom lies a tomb filled with traps, treasures, and " +
                "the undead guardians of a forgotten king. Each room presents new challenges, from deadly traps " +
                "to powerful encounters. The party must navigate the dungeon's circular connections, solve puzzles, " +
                "and face the king's phylactery to claim the legendary treasure within.",
                "Dungeon Crawl",
                1, 5, 4
        );

        // Feywild module
        Dnd5eModule feywildModule = createModule(
                "Whispers of the Feywild",
                "A whimsical Feywild adventure",
                "The party is pulled into the Feywild, where time flows differently and every encounter is " +
                "a test of wit and charm. Archfey vie for their allegiance, and Dark Gifts from the Feywild " +
                "offer power at a price. The party must find their way home before they become lost in the " +
                "Feywild forever.",
                "Feywild",
                3, 8, 5
        );

        // Steampunk module
        Dnd5eModule steampunkModule = createModule(
                "Gears of Destiny",
                "A steampunk adventure with magitech crafting",
                "In a world where magic and technology merge, the party are inventors and adventurers who craft " +
                "magitech devices to solve problems. Their bastion is a workshop where they create powerful " +
                "artifacts, and growing magic items evolve with their ingenuity. A villainous inventor threatens " +
                "to unleash a machine that could destroy the balance between magic and technology.",
                "Steampunk",
                5, 10, 6
        );

        log.info("Populated {} example modules", 6);
    }
}
