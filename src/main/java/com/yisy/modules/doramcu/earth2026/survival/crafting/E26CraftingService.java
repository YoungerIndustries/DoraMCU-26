package com.yisy.modules.doramcu.earth2026.survival.crafting;
import org.springframework.stereotype.Service;

import com.yisy.modules.doramcu.earth2026.model.Earth2026Character;
import com.yisy.modules.doramcu.earth2026.repository.Earth2026CharacterRepository;
import com.yisy.modules.doramcu.earth2026.survival.SurvivalResource;
import com.yisy.modules.doramcu.earth2026.survival.SurvivalResourceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Service for managing Earth2026 crafting system.
 * <p>
 * Provides recipe management, crafting project tracking, and skill checks
 * based on character abilities. Follows the D&D 5E crafting pattern.
 * </p>
 */
@Service
public class E26CraftingService {

    private static final Logger log = LoggerFactory.getLogger(E26CraftingService.class);
    private static final Random random = new Random();

    private static final int EXPECTED_RECIPE_COUNT_MIN = 20;

    private final E26CraftingRecipeRepository recipeRepository;
    private final E26CraftingProjectRepository projectRepository;
    private final Earth2026CharacterRepository characterRepository;
    private final SurvivalResourceRepository resourceRepository;

    private boolean initialized = false;

    public E26CraftingService(E26CraftingRecipeRepository recipeRepository,
                               E26CraftingProjectRepository projectRepository,
                               Earth2026CharacterRepository characterRepository,
                               SurvivalResourceRepository resourceRepository) {
        this.recipeRepository = recipeRepository;
        this.projectRepository = projectRepository;
        this.characterRepository = characterRepository;
        this.resourceRepository = resourceRepository;
    }

    // ==================== Initialization ====================

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initializeCraftingDatabase() {
        if (initialized) {
            return;
        }
        long count = recipeRepository.count();
        if (count >= EXPECTED_RECIPE_COUNT_MIN) {
            log.info("Earth2026 crafting database already populated: {} recipes", count);
            initialized = true;
            return;
        }

        log.info("Initializing Earth2026 crafting database...");
        populateRecipes();
        initialized = true;
        log.info("Earth2026 crafting database initialized: {} recipes", recipeRepository.count());
    }

    // ==================== Recipe Queries ====================

    @Transactional(readOnly = true)
    public List<E26CraftingRecipe> listRecipes() {
        return recipeRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<E26CraftingRecipe> listRecipesByCategory(String category) {
        return recipeRepository.findByCategory(category);
    }

    @Transactional(readOnly = true)
    public E26CraftingRecipe getRecipe(Long recipeId) {
        return recipeRepository.findById(recipeId)
                .orElseThrow(() -> new IllegalArgumentException("Recipe not found: " + recipeId));
    }

    // ==================== Crafting Projects ====================

    /**
     * Start a new crafting project.
     *
     * @param characterId the character ID
     * @param recipeId the recipe ID
     * @return the created project
     */
    @Transactional
    public E26CraftingProject startProject(Long characterId, Long recipeId) {
        Earth2026Character character = characterRepository.findById(characterId)
                .orElseThrow(() -> new IllegalArgumentException("Character not found: " + characterId));
        E26CraftingRecipe recipe = getRecipe(recipeId);

        // Check if character already has an in-progress project
        List<E26CraftingProject> inProgress = projectRepository.findByCharacterIdAndStatus(characterId, "IN_PROGRESS");
        if (!inProgress.isEmpty()) {
            throw new IllegalStateException("Character already has an in-progress crafting project");
        }

        // Perform skill check
        boolean skillCheckPassed = performSkillCheck(character, recipe);

        // Create project
        E26CraftingProject project = new E26CraftingProject();
        project.setCharacterId(characterId);
        project.setRecipeId(recipeId);
        project.setStatus("IN_PROGRESS");
        project.setProgress(0);
        project.setTotalProgress(recipe.getTimeRequired());
        project.setSkillCheckPassed(skillCheckPassed);
        project.setCreatedAt(LocalDateTime.now());

        E26CraftingProject saved = projectRepository.save(project);
        log.info("Character {} started crafting '{}' (skill check: {})",
                character.getName(), recipe.getName(), skillCheckPassed);
        return saved;
    }

    /**
     * Progress a crafting project by one tick.
     *
     * @param projectId the project ID
     * @return the updated project
     */
    @Transactional
    public E26CraftingProject progressProject(Long projectId) {
        E26CraftingProject project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));

        if (!"IN_PROGRESS".equals(project.getStatus())) {
            throw new IllegalStateException("Project is not in progress: " + project.getStatus());
        }

        // Progress amount depends on skill check
        int progressAmount = project.getSkillCheckPassed() ? 2 : 1;
        project.setProgress(project.getProgress() + progressAmount);

        // Check if complete
        if (project.isComplete()) {
            completeProject(project);
        }

        projectRepository.save(project);
        return project;
    }

    /**
     * Complete a crafting project.
     *
     * @param projectId the project ID
     * @return the completed project
     */
    @Transactional
    public E26CraftingProject completeProject(Long projectId) {
        E26CraftingProject project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));

        if (!"IN_PROGRESS".equals(project.getStatus())) {
            throw new IllegalStateException("Project is not in progress: " + project.getStatus());
        }

        // Force completion
        project.setProgress(project.getTotalProgress());
        completeProject(project);

        projectRepository.save(project);
        return project;
    }

    private void completeProject(E26CraftingProject project) {
        E26CraftingRecipe recipe = getRecipe(project.getRecipeId());

        project.setStatus("COMPLETED");
        project.setResultResource(recipe.getResultResource());
        project.setResultQuantity(recipe.getResultQuantity());
        project.setCompletedAt(LocalDateTime.now());

        log.info("Project {} completed: crafted {} x {}",
                project.getId(), recipe.getResultQuantity(), recipe.getResultResource());
    }

    /**
     * Cancel a crafting project.
     *
     * @param projectId the project ID
     */
    @Transactional
    public void cancelProject(Long projectId) {
        E26CraftingProject project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));

        if (!"IN_PROGRESS".equals(project.getStatus())) {
            throw new IllegalStateException("Project is not in progress: " + project.getStatus());
        }

        project.setStatus("CANCELLED");
        projectRepository.save(project);
        log.info("Project {} cancelled", projectId);
    }

    // ==================== Skill Check ====================

    private boolean performSkillCheck(Earth2026Character character, E26CraftingRecipe recipe) {
        int skillScore;

        // Determine the relevant skill score
        if (recipe.getRequiredSkill() != null) {
            switch (recipe.getRequiredSkill()) {
                case "STR" -> skillScore = character.getStrScore();
                case "DEX" -> skillScore = character.getDexScore();
                case "INT" -> skillScore = character.getIntScore();
                case "WIS" -> skillScore = character.getWisScore();
                case "CHA" -> skillScore = character.getChaScore();
                default -> skillScore = (character.getStrScore() + character.getDexScore()
                        + character.getIntScore() + character.getWisScore()) / 4;
            }
        } else {
            // Average of all skills
            skillScore = (character.getStrScore() + character.getDexScore()
                    + character.getIntScore() + character.getWisScore()) / 4;
        }

        // DC based on difficulty
        int dc = recipe.getDifficulty() * 2;

        // Roll d20 + skill modifier
        int roll = 1 + random.nextInt(20);
        int modifier = (skillScore - 10) / 2;
        int total = roll + modifier;

        boolean passed = total >= dc;
        log.debug("Skill check: roll={}, modifier={}, total={}, dc={}, passed={}",
                roll, modifier, total, dc, passed);
        return passed;
    }

    // ==================== Recipe Population ====================

    private void populateRecipes() {
        addRecipeIfNotExists("Wooden Shelter", "SHELTER", 8, 60,
                "{\"Wood Plank\": 10, \"Rope\": 2}", "STR", "Tarp", 1, false);
        addRecipeIfNotExists("Reinforced Shelter", "SHELTER", 12, 120,
                "{\"Wood Plank\": 20, \"Metal Sheet\": 5, \"Rope\": 4}", "STR", "Sleeping Bag", 1, true);
        addRecipeIfNotExists("Water Filter", "UTILITY", 10, 45,
                "{\"Fabric Scrap\": 3, \"Wire\": 2, \"Duct Tape\": 1}", "INT", "Water Filter", 1, false);
        addRecipeIfNotExists("Bandage Kit", "MEDICINE", 6, 30,
                "{\"Fabric Scrap\": 5, \"Bandages\": 2}", "WIS", "First Aid Kit", 1, false);
        addRecipeIfNotExists("Improvised Trap", "UTILITY", 7, 20,
                "{\"Wire\": 3, \"Rope\": 1}", "DEX", "Improvised Club", 1, false);
        addRecipeIfNotExists("Campfire Kit", "SHELTER", 4, 15,
                "{\"Firewood\": 3}", null, "Firewood", 2, false);
        addRecipeIfNotExists("Repair Kit", "UTILITY", 9, 40,
                "{\"Duct Tape\": 2, \"Wire\": 2, \"Wrench\": 1}", "INT", "Duct Tape", 3, false);
        addRecipeIfNotExists("Hunting Arrow Set", "WEAPON", 11, 50,
                "{\"Wood Plank\": 5, \"Wire\": 3}", "DEX", "Hunting Bow", 1, false);
        addRecipeIfNotExists("Medicinal Salve", "MEDICINE", 8, 35,
                "{\"Wild Berries\": 3, \"Fabric Scrap\": 1}", "WIS", "Bandages", 2, false);
        addRecipeIfNotExists("Rope Bridge", "SHELTER", 14, 180,
                "{\"Rope\": 10, \"Wood Plank\": 15}", "STR", "Rope", 5, true);
        addRecipeIfNotExists("Signal Fire", "UTILITY", 5, 10,
                "{\"Firewood\": 5, \"Fabric Scrap\": 2}", null, "Firewood", 1, false);
        addRecipeIfNotExists("Improvised Armor", "SHELTER", 10, 60,
                "{\"Metal Sheet\": 5, \"Duct Tape\": 3}", "DEX", "Metal Sheet", 2, false);
        addRecipeIfNotExists("Fishing Rod", "TOOL", 7, 25,
                "{\"Wire\": 2, \"Rope\": 1, \"Wood Plank\": 2}", "DEX", "Rope", 1, false);
        addRecipeIfNotExists("Preserved Food", "FOOD", 6, 20,
                "{\"Canned Beans\": 2, \"Salt\": 1}", "WIS", "Dried Meat", 1, false);
        addRecipeIfNotExists("Tool Sharpening", "TOOL", 5, 15,
                "{\"Concrete Block\": 1}", "STR", "Knife", 1, false);
        addRecipeIfNotExists("Emergency Radio", "UTILITY", 15, 90,
                "{\"Wire\": 5, \"Metal Sheet\": 3, \"Duct Tape\": 2}", "INT", "Flashlight", 1, true);
        addRecipeIfNotExists("Wooden Spear", "WEAPON", 6, 20,
                "{\"Wood Plank\": 3, \"Wire\": 1}", "STR", "Improvised Club", 1, false);
        addRecipeIfNotExists("Tent", "SHELTER", 9, 45,
                "{\"Tarp\": 1, \"Rope\": 4, \"Wood Plank\": 4}", "DEX", "Tarp", 1, false);
        addRecipeIfNotExists("Water Purification", "UTILITY", 8, 30,
                "{\"Fabric Scrap\": 2, \"Firewood\": 2}", "INT", "Purified Water", 2, false);
        addRecipeIfNotExists("Defensive Wall", "SHELTER", 13, 150,
                "{\"Concrete Block\": 10, \"Metal Sheet\": 5}", "STR", "Concrete Block", 5, true);
    }

    private void addRecipeIfNotExists(String name, String category, int difficulty, int timeRequired,
                                       String materialsJson, String requiredSkill,
                                       String resultResource, int resultQuantity, boolean requiresWorkshop) {
        if (recipeRepository.existsByName(name)) {
            return;
        }
        E26CraftingRecipe recipe = new E26CraftingRecipe(name, category);
        recipe.setDescription("Craft " + name + " using available materials.");
        recipe.setDifficulty(difficulty);
        recipe.setTimeRequired(timeRequired);
        recipe.setMaterialsJson(materialsJson);
        recipe.setRequiredSkill(requiredSkill);
        recipe.setResultResource(resultResource);
        recipe.setResultQuantity(resultQuantity);
        recipe.setRequiresWorkshop(requiresWorkshop);
        recipeRepository.save(recipe);
    }
}
