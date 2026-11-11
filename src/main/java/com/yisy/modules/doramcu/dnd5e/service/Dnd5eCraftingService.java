package com.yisy.modules.doramcu.dnd5e.service;

import com.yisy.modules.doramcu.dnd5e.dto.CraftingCheckResponse;
import com.yisy.modules.doramcu.dnd5e.dto.CreateCraftingRecipeRequest;
import com.yisy.modules.doramcu.dnd5e.dto.StartCraftingProjectRequest;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCharacter;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCraftingProject;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCraftingRecipe;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCraftingTool;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5eCharacterRepository;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5eCraftingProjectRepository;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5eCraftingRecipeRepository;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5eCraftingToolRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Service for managing D&D 5e crafting system.
 * <p>
 * Provides recipe management, crafting project tracking, tool proficiency lookup,
 * and crafting check resolution based on the 2024 PHB crafting system (p.233).
 * </p>
 */
@Service
public class Dnd5eCraftingService {

    private static final Logger log = LoggerFactory.getLogger(Dnd5eCraftingService.class);
    private static final Random random = new Random();

    private static final int EXPECTED_RECIPE_COUNT_MIN = 10;
    private static final int EXPECTED_TOOL_COUNT_MIN = 10;

    private final Dnd5eCraftingRecipeRepository recipeRepository;
    private final Dnd5eCraftingProjectRepository projectRepository;
    private final Dnd5eCraftingToolRepository toolRepository;
    private final Dnd5eCharacterRepository characterRepository;

    private boolean initialized = false;

    public Dnd5eCraftingService(Dnd5eCraftingRecipeRepository recipeRepository,
                                 Dnd5eCraftingProjectRepository projectRepository,
                                 Dnd5eCraftingToolRepository toolRepository,
                                 Dnd5eCharacterRepository characterRepository) {
        this.recipeRepository = recipeRepository;
        this.projectRepository = projectRepository;
        this.toolRepository = toolRepository;
        this.characterRepository = characterRepository;
    }

    // ==================== Initialization ====================

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initializeCraftingDatabase() {
        if (initialized) {
            return;
        }
        long recipeCount = recipeRepository.count();
        long toolCount = toolRepository.count();
        if (recipeCount >= EXPECTED_RECIPE_COUNT_MIN && toolCount >= EXPECTED_TOOL_COUNT_MIN) {
            log.info("D&D 5e crafting database already populated: {} recipes, {} tools", recipeCount, toolCount);
            initialized = true;
            return;
        }

        log.info("Initializing D&D 5e crafting database...");
        populateCraftingTools();
        populateCraftingRecipes();
        initialized = true;
        log.info("D&D 5e crafting database initialized: {} recipes, {} tools",
                recipeRepository.count(), toolRepository.count());
    }

    // ==================== Recipe CRUD ====================

    @Transactional
    public Dnd5eCraftingRecipe createRecipe(CreateCraftingRecipeRequest request) {
        Dnd5eCraftingRecipe recipe = new Dnd5eCraftingRecipe();
        recipe.setName(request.getName());
        recipe.setDescription(request.getDescription());
        recipe.setCategory(request.getCategory());
        recipe.setRarity(request.getRarity() != null ? request.getRarity() : "Common");
        recipe.setRequiredTool(request.getRequiredTool());
        recipe.setRequiredAbility(request.getRequiredAbility());
        recipe.setDc(request.getDc());
        recipe.setGoldCost(request.getGoldCost());
        recipe.setHoursRequired(request.getHoursRequired());
        recipe.setMaterialComponents(request.getMaterialComponents());
        recipe.setResultItemName(request.getResultItemName());
        recipe.setResultItemType(request.getResultItemType());
        recipe.setResultQuantity(Math.max(1, request.getResultQuantity()));
        recipe.setMagicItem(request.isMagicItem());
        recipe.setPrerequisites(request.getPrerequisites());

        Dnd5eCraftingRecipe saved = recipeRepository.save(recipe);
        log.info("Created crafting recipe '{}'", saved.getName());
        return saved;
    }

    @Transactional(readOnly = true)
    public Dnd5eCraftingRecipe getRecipe(Long recipeId) {
        return recipeRepository.findById(recipeId)
                .orElseThrow(() -> new IllegalArgumentException("Crafting recipe not found: " + recipeId));
    }

    @Transactional(readOnly = true)
    public List<Dnd5eCraftingRecipe> listRecipes() {
        return recipeRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Dnd5eCraftingRecipe> listRecipesByCategory(String category) {
        return recipeRepository.findByCategory(category);
    }

    @Transactional(readOnly = true)
    public List<Dnd5eCraftingRecipe> listRecipesByTool(String tool) {
        return recipeRepository.findByRequiredTool(tool);
    }

    @Transactional(readOnly = true)
    public List<Dnd5eCraftingRecipe> searchRecipes(String keyword) {
        return recipeRepository.searchByNameOrDescription(keyword);
    }

    @Transactional
    public void deleteRecipe(Long recipeId) {
        recipeRepository.deleteById(recipeId);
        log.info("Deleted crafting recipe: {}", recipeId);
    }

    // ==================== Tool Management ====================

    @Transactional(readOnly = true)
    public List<Dnd5eCraftingTool> listTools() {
        return toolRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Dnd5eCraftingTool getToolByName(String name) {
        return toolRepository.findByName(name)
                .orElseThrow(() -> new IllegalArgumentException("Crafting tool not found: " + name));
    }

    @Transactional(readOnly = true)
    public List<Dnd5eCraftingTool> listToolsByAbility(String ability) {
        return toolRepository.findByAssociatedAbility(ability);
    }

    // ==================== Crafting Projects ====================

    @Transactional
    public Dnd5eCraftingProject startProject(StartCraftingProjectRequest request) {
        // Verify character exists
        Dnd5eCharacter character = characterRepository.findById(request.getCharacterId())
                .orElseThrow(() -> new IllegalArgumentException("Character not found: " + request.getCharacterId()));

        // Verify recipe exists
        Dnd5eCraftingRecipe recipe = getRecipe(request.getRecipeId());

        // Check tool proficiency
        int abilityModifier = getAbilityModifier(character, recipe.getRequiredAbility());
        boolean hasProficiency = checkToolProficiency(character, recipe.getRequiredTool());

        // Calculate effective hours (group crafting divides time)
        int effectiveHours = recipe.getHoursRequired();
        if (request.isGroupCraft() && request.getGroupMemberIds() != null && !request.getGroupMemberIds().isEmpty()) {
            int groupSize = request.getGroupMemberIds().size() + 1; // +1 for the main character
            effectiveHours = Math.max(1, recipe.getHoursRequired() / groupSize);
        }

        Dnd5eCraftingProject project = new Dnd5eCraftingProject();
        project.setCharacterId(request.getCharacterId());
        project.setRecipeId(request.getRecipeId());
        project.setName(recipe.getName());
        project.setStatus("IN_PROGRESS");
        project.setHoursWorked(0);
        project.setHoursRequired(effectiveHours);
        project.setGoldSpent(0);
        project.setGoldRequired(recipe.getGoldCost());
        project.setGroupCraft(request.isGroupCraft());

        if (request.isGroupCraft() && request.getGroupMemberIds() != null) {
            project.setGroupMembers(request.getGroupMemberIds().stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(",")));
        }

        project.setNotes(request.getNotes());

        Dnd5eCraftingProject saved = projectRepository.save(project);
        log.info("Started crafting project '{}' for character {}", saved.getName(), request.getCharacterId());
        return saved;
    }

    @Transactional(readOnly = true)
    public Dnd5eCraftingProject getProject(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Crafting project not found: " + projectId));
    }

    @Transactional(readOnly = true)
    public List<Dnd5eCraftingProject> listProjects(Long characterId) {
        verifyCharacterExists(characterId);
        return projectRepository.findByCharacterId(characterId);
    }

    @Transactional(readOnly = true)
    public List<Dnd5eCraftingProject> listActiveProjects(Long characterId) {
        verifyCharacterExists(characterId);
        return projectRepository.findByCharacterIdAndStatus(characterId, "IN_PROGRESS");
    }

    @Transactional
    public void cancelProject(Long projectId) {
        Dnd5eCraftingProject project = getProject(projectId);
        project.setStatus("CANCELLED");
        projectRepository.save(project);
        log.info("Cancelled crafting project: {}", projectId);
    }

    // ==================== Crafting Checks ====================

    @Transactional
    public CraftingCheckResponse makeCraftingCheck(Long projectId) {
        Dnd5eCraftingProject project = getProject(projectId);
        if (!"IN_PROGRESS".equals(project.getStatus())) {
            throw new IllegalStateException("Project is not in progress: " + project.getStatus());
        }

        Dnd5eCraftingRecipe recipe = getRecipe(project.getRecipeId());
        Dnd5eCharacter character = characterRepository.findById(project.getCharacterId())
                .orElseThrow(() -> new IllegalArgumentException("Character not found: " + project.getCharacterId()));

        // Calculate modifier
        int abilityModifier = getAbilityModifier(character, recipe.getRequiredAbility());
        boolean hasProficiency = checkToolProficiency(character, recipe.getRequiredTool());
        int proficiencyBonus = hasProficiency ? character.getProficiencyBonus() : 0;
        int totalModifier = abilityModifier + proficiencyBonus;

        // Roll d20
        int roll = random.nextInt(20) + 1;
        int total = roll + totalModifier;
        boolean success = total >= recipe.getDc();

        // Build result description
        String result;
        int hoursToAdd = 0;
        if (roll == 20) {
            result = "Critical Success! Exceptional craftsmanship.";
            hoursToAdd = 2; // Critical success counts as 2 hours
        } else if (roll == 1) {
            result = "Critical Failure! Materials damaged.";
            hoursToAdd = 0; // No progress on critical failure
        } else if (success) {
            result = "Success! Progress made on the item.";
            hoursToAdd = 1;
        } else {
            result = "Failure. No progress this hour.";
            hoursToAdd = 0;
        }

        // Update project
        int newHoursWorked = Math.min(project.getHoursWorked() + hoursToAdd, project.getHoursRequired());
        project.setHoursWorked(newHoursWorked);

        // Append check roll history
        String existingRolls = project.getCheckRolls();
        String newRoll = String.format("d20=%d + %d = %d (DC %d) %s",
                roll, totalModifier, total, recipe.getDc(), success ? "✓" : "✗");
        project.setCheckRolls(existingRolls == null ? newRoll : existingRolls + "; " + newRoll);
        project.setLastCheckResult(result);

        // Check for completion
        boolean completed = newHoursWorked >= project.getHoursRequired();
        if (completed) {
            project.setStatus("COMPLETED");
            project.setCompletedAt(LocalDateTime.now());
            log.info("Crafting project '{}' completed by character {}", project.getName(), project.getCharacterId());
        }

        projectRepository.save(project);

        // Build response
        CraftingCheckResponse response = new CraftingCheckResponse();
        response.setProjectId(projectId);
        response.setRoll(roll);
        response.setModifier(totalModifier);
        response.setTotal(total);
        response.setDc(recipe.getDc());
        response.setSuccess(success);
        response.setResult(result);
        response.setHoursWorked(project.getHoursWorked());
        response.setHoursRequired(project.getHoursRequired());
        response.setGoldSpent(project.getGoldSpent());
        response.setGoldRequired(project.getGoldRequired());
        response.setStatus(project.getStatus());
        response.setCompleted(completed);

        return response;
    }

    // ==================== Helper Methods ====================

    private void verifyCharacterExists(Long characterId) {
        if (!characterRepository.existsById(characterId)) {
            throw new IllegalArgumentException("Character not found: " + characterId);
        }
    }

    private int getAbilityModifier(Dnd5eCharacter character, String ability) {
        int score = switch (ability.toUpperCase()) {
            case "STR" -> character.getStrScore();
            case "DEX" -> character.getDexScore();
            case "CON" -> character.getConScore();
            case "INT" -> character.getIntScore();
            case "WIS" -> character.getWisScore();
            case "CHA" -> character.getChaScore();
            default -> 10;
        };
        return (score - 10) / 2;
    }

    private boolean checkToolProficiency(Dnd5eCharacter character, String toolName) {
        // Check if character has the tool in their equipment/proficiencies
        String equipment = character.getEquipment();
        if (equipment != null && equipment.contains(toolName)) {
            return true;
        }
        return false;
    }

    // ==================== Recipe Population ====================

    @Transactional
    public void populateCraftingRecipes() {
        List<Dnd5eCraftingRecipe> recipes = new ArrayList<>();

        // --- Non-Magical Items ---
        recipes.add(createRecipe("Potion of Healing", "Potions", "Common",
                "Herbalism Kit", "WIS", 15, 50, 8,
                "25 gp worth of herbal reagents and a blank potion vial",
                "Potion of Healing", "Potion", 1, false, null));

        recipes.add(createRecipe("Antitoxin", "Potions", "Common",
                "Herbalism Kit", "WIS", 15, 50, 8,
                "25 gp worth of herbal reagents",
                "Antitoxin", "Potion", 1, false, null));

        recipes.add(createRecipe("Healer's Kit", "Tools", "Common",
                "Herbalism Kit", "WIS", 12, 5, 4,
                "5 gp worth of bandages, salves, and sutures",
                "Healer's Kit", "Tool", 1, false, null));

        recipes.add(createRecipe("Arrow (20)", "Ammunition", "Common",
                "Woodcarver's Tools", "DEX", 10, 1, 2,
                "1 gp worth of wood and fletching",
                "Arrow", "Ammunition", 20, false, null));

        recipes.add(createRecipe("Ball Bearings", "Tools", "Common",
                "Smith's Hammer", "STR", 12, 1, 2,
                "1 gp worth of metal",
                "Ball Bearings", "Tool", 1, false, null));

        recipes.add(createRecipe("Caltrops", "Tools", "Common",
                "Smith's Hammer", "STR", 12, 1, 4,
                "1 gp worth of metal",
                "Caltrops", "Tool", 1, false, null));

        recipes.add(createRecipe("Cloak, Camouflage", "Clothing", "Common",
                "Tailor's Tools", "DEX", 12, 5, 8,
                "5 gp worth of dyes and fabric",
                "Camouflage Cloak", "Wondrous Item", 1, false, null));

        recipes.add(createRecipe("Parchment Sheets", "Supplies", "Common",
                "Calligraphy Set", "DEX", 10, 1, 2,
                "1 gp worth of paper and ink",
                "Parchment", "Supply", 10, false, null));

        recipes.add(createRecipe("Spell Scroll (1st Level)", "Scrolls", "Uncommon",
                "Calligraphy Set", "INT", 15, 50, 8,
                "50 gp worth of calligraphy supplies and a blank scroll",
                "Spell Scroll", "Scroll", 1, true, "Must be able to cast the spell"));

        recipes.add(createRecipe("Spell Scroll (2nd Level)", "Scrolls", "Uncommon",
                "Calligraphy Set", "INT", 17, 100, 8,
                "100 gp worth of calligraphy supplies and a blank scroll",
                "Spell Scroll", "Scroll", 1, true, "Must be able to cast the spell"));

        // --- Weapons and Armor ---
        recipes.add(createRecipe("Longsword", "Weapons", "Common",
                "Smith's Hammer", "STR", 15, 15, 8,
                "15 gp worth of steel",
                "Longsword", "Weapon", 1, false, null));

        recipes.add(createRecipe("Leather Armor", "Armor", "Common",
                "Leatherworker's Tools", "DEX", 15, 10, 16,
                "10 gp worth of leather and thread",
                "Leather Armor", "Armor", 1, false, null));

        recipes.add(createRecipe("Shield", "Armor", "Common",
                "Carpenter's Tools", "STR", 15, 10, 8,
                "10 gp worth of wood and metal fittings",
                "Shield", "Armor", 1, false, null));

        // --- Alchemical Items ---
        recipes.add(createRecipe("Alchemist's Fire", "Alchemical", "Common",
                "Herbalism Kit", "INT", 12, 50, 4,
                "50 gp worth of oil and sulfur",
                "Alchemist's Fire", "Alchemical", 1, false, null));

        recipes.add(createRecipe("Acid Flask", "Alchemical", "Common",
                "Herbalism Kit", "INT", 12, 25, 4,
                "25 gp worth of acid reagents",
                "Acid", "Alchemical", 1, false, null));

        recipeRepository.saveAll(recipes);
        log.info("Populated {} D&D 5e crafting recipes", recipes.size());
    }

    @Transactional
    public void populateCraftingTools() {
        List<Dnd5eCraftingTool> tools = new ArrayList<>();

        tools.add(createTool("Alchemist's Supplies", "Chemical analysis and potion brewing",
                "INT", "Artisan", "Potions, alchemical items, antitoxins",
                "Can create alchemist's fire as a bonus action",
                "Identify properties of unknown substances",
                "Mix chemicals to create temporary effects"));

        tools.add(createTool("Calligraphy Set", "Writing and scroll creation",
                "DEX", "Artisan", "Spell scrolls, maps, documents",
                "Can forge documents as part of deception",
                "Create detailed maps during travel",
                "Write or copy text quickly"));

        tools.add(createTool("Carpenter's Tools", "Woodworking and construction",
                "STR", "Artisan", "Furniture, doors, shields, structures",
                "Jam doors or break down barriers",
                "Build shelters or bridges",
                "Repair wooden structures"));

        tools.add(createTool("Cobbler's Tools", "Shoe and boot making",
                "DEX", "Artisan", "Boots, shoes, sandals",
                "Silent movement with custom footwear",
                "Create climbing aids from leather",
                "Repair damaged footwear"));

        tools.add(createTool("Cook's Utensils", "Food preparation and preservation",
                "CHA", "Artisan", "Rations, preserved foods, feast items",
                "Prepare healing rations",
                "Preserve food for long journeys",
                "Prepare a meal that boosts morale"));

        tools.add(createTool("Glassblower's Tools", "Glass and crystal crafting",
                "DEX", "Artisan", "Lenses, vials, crystal items",
                "Create signal flares or magnifying glasses",
                "Craft potion vials or lenses",
                "Shape glass for specialized uses"));

        tools.add(createTool("Herbalism Kit", "Plant identification and preparation",
                "WIS", "Artisan", "Potions of healing, antitoxins, poisons",
                "Identify and use medicinal plants",
                "Forage for food and medicine",
                "Prepare healing salves"));

        tools.add(createTool("Jeweler's Tools", "Gem and metal working",
                "DEX", "Artisan", "Jewelry, gem settings, small metal items",
                "Appraise gem quality",
                "Craft small metal components",
                "Set gems in settings"));

        tools.add(createTool("Leatherworker's Tools", "Leather crafting and treatment",
                "DEX", "Artisan", "Leather armor, belts, bags, quivers",
                "Create armor patches for temporary AC bonus",
                "Craft bags and containers",
                "Treat leather for weather resistance"));

        tools.add(createTool("Smith's Hammer", "Metal forging and shaping",
                "STR", "Artisan", "Weapons, armor, tools, metal fittings",
                "Repair weapons or armor in the field",
                "Forge simple tools or weapons",
                "Shape metal for repairs"));

        tools.add(createTool("Tailor's Tools", "Textile and clothing creation",
                "DEX", "Artisan", "Clothing, banners, camouflage gear",
                "Create disguises or camouflage",
                "Sew banners or signals",
                "Repair torn clothing quickly"));

        tools.add(createTool("Woodcarver's Tools", "Wood shaping and carving",
                "DEX", "Artisan", "Arrows, bows, wooden items, sculptures",
                "Craft arrows or simple weapons",
                "Carve messages or symbols",
                "Create wooden traps or devices"));

        toolRepository.saveAll(tools);
        log.info("Populated {} D&D 5e crafting tools", tools.size());
    }

    // ==================== Private Helpers ====================

    private Dnd5eCraftingRecipe createRecipe(String name, String category, String rarity,
                                              String requiredTool, String requiredAbility,
                                              int dc, int goldCost, int hoursRequired,
                                              String materialComponents, String resultItemName,
                                              String resultItemType, int resultQuantity,
                                              boolean isMagicItem, String prerequisites) {
        Dnd5eCraftingRecipe recipe = new Dnd5eCraftingRecipe();
        recipe.setName(name);
        recipe.setDescription("Craft " + name + " using " + requiredTool + ".");
        recipe.setCategory(category);
        recipe.setRarity(rarity);
        recipe.setRequiredTool(requiredTool);
        recipe.setRequiredAbility(requiredAbility);
        recipe.setDc(dc);
        recipe.setGoldCost(goldCost);
        recipe.setHoursRequired(hoursRequired);
        recipe.setMaterialComponents(materialComponents);
        recipe.setResultItemName(resultItemName);
        recipe.setResultItemType(resultItemType);
        recipe.setResultQuantity(resultQuantity);
        recipe.setMagicItem(isMagicItem);
        recipe.setPrerequisites(prerequisites);
        return recipe;
    }

    private Dnd5eCraftingTool createTool(String name, String description,
                                          String associatedAbility, String toolType,
                                          String craftableItems, String combatUse,
                                          String explorationUse, String useAction) {
        Dnd5eCraftingTool tool = new Dnd5eCraftingTool();
        tool.setName(name);
        tool.setDescription(description);
        tool.setAssociatedAbility(associatedAbility);
        tool.setToolType(toolType);
        tool.setCraftableItems(craftableItems);
        tool.setCombatUse(combatUse);
        tool.setExplorationUse(explorationUse);
        tool.setUseAction(useAction);
        return tool;
    }
}
