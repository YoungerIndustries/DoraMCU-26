package com.yisy.modules.doramcu.earth2026.survival.crafting;

import com.yisy.modules.doramcu.earth2026.model.Earth2026Character;
import com.yisy.modules.doramcu.earth2026.repository.Earth2026CharacterRepository;
import com.yisy.modules.doramcu.earth2026.survival.SurvivalResourceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("E26CraftingService Tests")
class E26CraftingServiceTest {

    private E26CraftingRecipeRepository recipeRepository;
    private E26CraftingProjectRepository projectRepository;
    private Earth2026CharacterRepository characterRepository;
    private SurvivalResourceRepository resourceRepository;
    private E26CraftingService craftingService;

    @BeforeEach
    void setUp() {
        recipeRepository = mock(E26CraftingRecipeRepository.class);
        projectRepository = mock(E26CraftingProjectRepository.class);
        characterRepository = mock(Earth2026CharacterRepository.class);
        resourceRepository = mock(SurvivalResourceRepository.class);
        craftingService = new E26CraftingService(
                recipeRepository, projectRepository, characterRepository, resourceRepository);
    }

    private Earth2026Character createCharacter(Long id) {
        Earth2026Character character = new Earth2026Character();
        character.setId(id);
        character.setName("Test Character");
        character.setHitPoints(100);
        character.setMaxHitPoints(100);
        character.setStrScore(10);
        character.setDexScore(10);
        character.setConScore(10);
        character.setIntScore(10);
        character.setWisScore(10);
        character.setChaScore(10);
        character.setLevel(1);
        character.setEarthLocation("Test Location");
        character.setExperiencePoints(0);
        return character;
    }

    private E26CraftingRecipe createRecipe(Long id, String name) {
        E26CraftingRecipe recipe = new E26CraftingRecipe();
        recipe.setId(id);
        recipe.setName(name);
        recipe.setCategory("TOOL");
        recipe.setDifficulty(5);
        recipe.setTimeRequired(10);
        recipe.setRequiredSkill("INT");
        recipe.setResultResource("Crafted Item");
        recipe.setResultQuantity(1);
        return recipe;
    }

    // ---- listRecipes ----

    @Nested
    @DisplayName("listRecipes Tests")
    class ListRecipesTests {

        @Test
        @DisplayName("should return all recipes")
        void listRecipes_shouldReturnAll() {
            List<E26CraftingRecipe> recipes = List.of(createRecipe(1L, "Knife"), createRecipe(2L, "Axe"));
            when(recipeRepository.findAll()).thenReturn(recipes);

            List<E26CraftingRecipe> result = craftingService.listRecipes();

            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("should return recipes by category")
        void listRecipesByCategory_shouldReturnFiltered() {
            List<E26CraftingRecipe> tools = List.of(createRecipe(1L, "Knife"));
            when(recipeRepository.findByCategory("TOOL")).thenReturn(tools);

            List<E26CraftingRecipe> result = craftingService.listRecipesByCategory("TOOL");

            assertEquals(1, result.size());
            assertEquals("TOOL", result.get(0).getCategory());
        }

        @Test
        @DisplayName("should return recipe by ID")
        void getRecipe_shouldReturnRecipe() {
            E26CraftingRecipe recipe = createRecipe(1L, "Knife");
            when(recipeRepository.findById(1L)).thenReturn(java.util.Optional.of(recipe));

            E26CraftingRecipe result = craftingService.getRecipe(1L);

            assertEquals("Knife", result.getName());
        }

        @Test
        @DisplayName("should throw when recipe not found")
        void getRecipe_shouldThrow() {
            when(recipeRepository.findById(999L)).thenReturn(java.util.Optional.empty());

            assertThrows(IllegalArgumentException.class, () -> craftingService.getRecipe(999L));
        }
    }

    // ---- startProject ----

    @Nested
    @DisplayName("startProject Tests")
    class StartProjectTests {

        @Test
        @DisplayName("should start a new crafting project")
        void startProject_shouldStartProject() {
            Earth2026Character character = createCharacter(1L);
            E26CraftingRecipe recipe = createRecipe(1L, "Knife");
            when(characterRepository.findById(1L)).thenReturn(java.util.Optional.of(character));
            when(recipeRepository.findById(1L)).thenReturn(java.util.Optional.of(recipe));
            when(projectRepository.findByCharacterIdAndStatus(1L, "IN_PROGRESS"))
                    .thenReturn(new ArrayList<>());
            when(projectRepository.save(any(E26CraftingProject.class))).thenAnswer(inv -> {
                E26CraftingProject p = inv.getArgument(0);
                p.setId(1L);
                return p;
            });

            E26CraftingProject result = craftingService.startProject(1L, 1L);

            assertNotNull(result);
            assertEquals(1L, result.getCharacterId());
            assertEquals(1L, result.getRecipeId());
            assertEquals("IN_PROGRESS", result.getStatus());
            assertEquals(0, result.getProgress());
            assertEquals(10, result.getTotalProgress());
            verify(projectRepository).save(result);
        }

        @Test
        @DisplayName("should throw when character not found")
        void startProject_shouldThrowWhenNotFound() {
            when(characterRepository.findById(999L)).thenReturn(java.util.Optional.empty());

            assertThrows(IllegalArgumentException.class, () -> craftingService.startProject(999L, 1L));
        }

        @Test
        @DisplayName("should throw when recipe not found")
        void startProject_shouldThrowWhenRecipeNotFound() {
            Earth2026Character character = createCharacter(1L);
            when(characterRepository.findById(1L)).thenReturn(java.util.Optional.of(character));
            when(recipeRepository.findById(999L)).thenReturn(java.util.Optional.empty());

            assertThrows(IllegalArgumentException.class, () -> craftingService.startProject(1L, 999L));
        }

        @Test
        @DisplayName("should throw when already has in-progress project")
        void startProject_shouldThrowWhenInProgress() {
            Earth2026Character character = createCharacter(1L);
            E26CraftingRecipe recipe = createRecipe(1L, "Knife");
            when(characterRepository.findById(1L)).thenReturn(java.util.Optional.of(character));
            when(recipeRepository.findById(1L)).thenReturn(java.util.Optional.of(recipe));

            E26CraftingProject existing = new E26CraftingProject();
            existing.setStatus("IN_PROGRESS");
            when(projectRepository.findByCharacterIdAndStatus(1L, "IN_PROGRESS"))
                    .thenReturn(List.of(existing));

            assertThrows(IllegalStateException.class, () -> craftingService.startProject(1L, 1L));
        }
    }

    // ---- progressProject ----

    @Nested
    @DisplayName("progressProject Tests")
    class ProgressProjectTests {

        @Test
        @DisplayName("should progress by 2 when skill check passed")
        void progressProject_shouldProgressWithSkillCheck() {
            E26CraftingProject project = new E26CraftingProject();
            project.setId(1L);
            project.setStatus("IN_PROGRESS");
            project.setProgress(0);
            project.setTotalProgress(10);
            project.setSkillCheckPassed(true);

            when(projectRepository.findById(1L)).thenReturn(java.util.Optional.of(project));
            when(projectRepository.save(any(E26CraftingProject.class))).thenAnswer(inv -> inv.getArgument(0));

            E26CraftingProject result = craftingService.progressProject(1L);

            assertEquals(2, result.getProgress());
        }

        @Test
        @DisplayName("should progress by 1 when skill check failed")
        void progressProject_shouldProgressWithoutSkillCheck() {
            E26CraftingProject project = new E26CraftingProject();
            project.setId(1L);
            project.setStatus("IN_PROGRESS");
            project.setProgress(0);
            project.setTotalProgress(10);
            project.setSkillCheckPassed(false);

            when(projectRepository.findById(1L)).thenReturn(java.util.Optional.of(project));
            when(projectRepository.save(any(E26CraftingProject.class))).thenAnswer(inv -> inv.getArgument(0));

            E26CraftingProject result = craftingService.progressProject(1L);

            assertEquals(1, result.getProgress());
        }

        @Test
        @DisplayName("should throw when project not found")
        void progressProject_shouldThrowWhenNotFound() {
            when(projectRepository.findById(999L)).thenReturn(java.util.Optional.empty());

            assertThrows(IllegalArgumentException.class, () -> craftingService.progressProject(999L));
        }

        @Test
        @DisplayName("should throw when project not in progress")
        void progressProject_shouldThrowWhenNotInProgress() {
            E26CraftingProject project = new E26CraftingProject();
            project.setId(1L);
            project.setStatus("COMPLETED");
            when(projectRepository.findById(1L)).thenReturn(java.util.Optional.of(project));

            assertThrows(IllegalStateException.class, () -> craftingService.progressProject(1L));
        }
    }

    // ---- completeProject ----

    @Nested
    @DisplayName("completeProject Tests")
    class CompleteProjectTests {

        @Test
        @DisplayName("should complete a project")
        void completeProject_shouldComplete() {
            E26CraftingProject project = new E26CraftingProject();
            project.setId(1L);
            project.setStatus("IN_PROGRESS");
            project.setProgress(5);
            project.setTotalProgress(10);
            project.setRecipeId(1L);

            E26CraftingRecipe recipe = createRecipe(1L, "Knife");
            recipe.setResultResource("Knife");
            recipe.setResultQuantity(1);

            when(projectRepository.findById(1L)).thenReturn(java.util.Optional.of(project));
            when(recipeRepository.findById(1L)).thenReturn(java.util.Optional.of(recipe));
            when(projectRepository.save(any(E26CraftingProject.class))).thenAnswer(inv -> inv.getArgument(0));

            E26CraftingProject result = craftingService.completeProject(1L);

            assertEquals("COMPLETED", result.getStatus());
            assertEquals("Knife", result.getResultResource());
            assertEquals(1, result.getResultQuantity());
            assertNotNull(result.getCompletedAt());
        }

        @Test
        @DisplayName("should throw when project not in progress")
        void completeProject_shouldThrowWhenNotInProgress() {
            E26CraftingProject project = new E26CraftingProject();
            project.setId(1L);
            project.setStatus("CANCELLED");
            when(projectRepository.findById(1L)).thenReturn(java.util.Optional.of(project));

            assertThrows(IllegalStateException.class, () -> craftingService.completeProject(1L));
        }
    }

    // ---- cancelProject ----

    @Nested
    @DisplayName("cancelProject Tests")
    class CancelProjectTests {

        @Test
        @DisplayName("should cancel a project")
        void cancelProject_shouldCancel() {
            E26CraftingProject project = new E26CraftingProject();
            project.setId(1L);
            project.setStatus("IN_PROGRESS");
            when(projectRepository.findById(1L)).thenReturn(java.util.Optional.of(project));
            when(projectRepository.save(any(E26CraftingProject.class))).thenAnswer(inv -> inv.getArgument(0));

            craftingService.cancelProject(1L);

            assertEquals("CANCELLED", project.getStatus());
            verify(projectRepository).save(project);
        }

        @Test
        @DisplayName("should throw when project not in progress")
        void cancelProject_shouldThrowWhenNotInProgress() {
            E26CraftingProject project = new E26CraftingProject();
            project.setId(1L);
            project.setStatus("COMPLETED");
            when(projectRepository.findById(1L)).thenReturn(java.util.Optional.of(project));

            assertThrows(IllegalStateException.class, () -> craftingService.cancelProject(1L));
        }
    }

    // ---- isComplete check ----

    @Nested
    @DisplayName("E26CraftingProject Tests")
    class ProjectModelTests {

        @Test
        @DisplayName("should be complete when progress >= total")
        void isComplete_shouldBeTrue() {
            E26CraftingProject project = new E26CraftingProject();
            project.setTotalProgress(10);
            project.setProgress(10);

            assertTrue(project.isComplete());
        }

        @Test
        @DisplayName("should not be complete when progress < total")
        void isComplete_shouldBeFalse() {
            E26CraftingProject project = new E26CraftingProject();
            project.setTotalProgress(10);
            project.setProgress(5);

            assertFalse(project.isComplete());
        }
    }
}
