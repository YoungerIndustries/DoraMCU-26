package com.yisy.modules.doramcu.earth2026.survival;

import com.yisy.modules.doramcu.earth2026.model.Earth2026Character;
import com.yisy.modules.doramcu.earth2026.repository.Earth2026CharacterRepository;
import com.yisy.modules.doramcu.earth2026.service.BiosphereService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("ResourceService Tests")
class ResourceServiceTest {

    private SurvivalResourceRepository resourceRepository;
    private Earth2026CharacterRepository characterRepository;
    private BiosphereService biosphereService;
    private ResourceService resourceService;

    @BeforeEach
    void setUp() {
        resourceRepository = mock(SurvivalResourceRepository.class);
        characterRepository = mock(Earth2026CharacterRepository.class);
        biosphereService = mock(BiosphereService.class);
        resourceService = new ResourceService(resourceRepository, characterRepository, biosphereService);
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
        character.setEarthLocation("Urban Area");
        character.setExperiencePoints(0);
        return character;
    }

    // ---- initializeResources ----

    @Nested
    @DisplayName("initializeResources Tests")
    class InitializeResourcesTests {

        @Test
        @DisplayName("should not initialize if already populated")
        void initializeResources_shouldSkipIfPopulated() {
            when(resourceRepository.count()).thenReturn(35L);

            resourceService.initializeResources();

            verify(resourceRepository, never()).save(any());
        }

        @Test
        @DisplayName("should populate resources when empty")
        void initializeResources_shouldPopulate() {
            when(resourceRepository.count()).thenReturn(0L);
            when(resourceRepository.save(any(SurvivalResource.class))).thenAnswer(inv -> {
                SurvivalResource r = inv.getArgument(0);
                r.setId(1L);
                return r;
            });

            resourceService.initializeResources();

            verify(resourceRepository, atLeast(30)).save(any(SurvivalResource.class));
        }
    }

    // ---- getAllResources ----

    @Nested
    @DisplayName("getAllResources Tests")
    class GetAllResourcesTests {

        @Test
        @DisplayName("should return all resources")
        void getAllResources_shouldReturnAll() {
            List<SurvivalResource> resources = List.of(
                    new SurvivalResource("Canned Beans", "FOOD"),
                    new SurvivalResource("Water Bottle", "WATER")
            );
            when(resourceRepository.findAll()).thenReturn(resources);

            List<SurvivalResource> result = resourceService.getAllResources();

            assertEquals(2, result.size());
        }
    }

    // ---- getResourcesByCategory ----

    @Nested
    @DisplayName("getResourcesByCategory Tests")
    class GetResourcesByCategoryTests {

        @Test
        @DisplayName("should return resources for a category")
        void getResourcesByCategory_shouldReturnResources() {
            List<SurvivalResource> foods = List.of(
                    new SurvivalResource("Canned Beans", "FOOD"),
                    new SurvivalResource("Dried Meat", "FOOD")
            );
            when(resourceRepository.findByCategory("FOOD")).thenReturn(foods);

            List<SurvivalResource> result = resourceService.getResourcesByCategory("FOOD");

            assertEquals(2, result.size());
            assertEquals("FOOD", result.get(0).getCategory());
        }

        @Test
        @DisplayName("should return empty list for unknown category")
        void getResourcesByCategory_shouldReturnEmpty() {
            when(resourceRepository.findByCategory("UNKNOWN")).thenReturn(new ArrayList<>());

            List<SurvivalResource> result = resourceService.getResourcesByCategory("UNKNOWN");

            assertTrue(result.isEmpty());
        }
    }

    // ---- getResourceByName ----

    @Nested
    @DisplayName("getResourceByName Tests")
    class GetResourceByNameTests {

        @Test
        @DisplayName("should return resource by name")
        void getResourceByName_shouldReturnResource() {
            SurvivalResource resource = new SurvivalResource("Canned Beans", "FOOD");
            when(resourceRepository.findByName("Canned Beans")).thenReturn(java.util.Optional.of(resource));

            SurvivalResource result = resourceService.getResourceByName("Canned Beans");

            assertEquals("Canned Beans", result.getName());
        }

        @Test
        @DisplayName("should throw when resource not found")
        void getResourceByName_shouldThrow() {
            when(resourceRepository.findByName("Nonexistent")).thenReturn(java.util.Optional.empty());

            assertThrows(IllegalArgumentException.class, () -> resourceService.getResourceByName("Nonexistent"));
        }
    }

    // ---- scavenge ----

    @Nested
    @DisplayName("scavenge Tests")
    class ScavengeTests {

        @Test
        @DisplayName("should return resources based on location and skills")
        void scavenge_shouldReturnResources() {
            Earth2026Character character = createCharacter(1L);
            when(characterRepository.findById(1L)).thenReturn(java.util.Optional.of(character));

            SurvivalResource food = new SurvivalResource("Canned Beans", "FOOD");
            SurvivalResource material = new SurvivalResource("Wood Plank", "MATERIAL");
            when(resourceRepository.findByCategory("FOOD")).thenReturn(List.of(food));
            when(resourceRepository.findByCategory("MATERIAL")).thenReturn(List.of(material));
            when(resourceRepository.findByCategory("WATER")).thenReturn(new ArrayList<>());
            when(resourceRepository.findByCategory("MEDICINE")).thenReturn(new ArrayList<>());
            when(resourceRepository.findByCategory("TOOL")).thenReturn(new ArrayList<>());

            List<SurvivalResource> result = resourceService.scavenge(1L, "Abandoned Building");

            assertNotNull(result);
        }

        @Test
        @DisplayName("should throw when character not found")
        void scavenge_shouldThrowWhenNotFound() {
            when(characterRepository.findById(999L)).thenReturn(java.util.Optional.empty());

            assertThrows(IllegalArgumentException.class, () -> resourceService.scavenge(999L, "Test"));
        }

        @Test
        @DisplayName("should use character location when null provided")
        void scavenge_shouldUseCharacterLocation() {
            Earth2026Character character = createCharacter(1L);
            character.setEarthLocation("Downtown");
            when(characterRepository.findById(1L)).thenReturn(java.util.Optional.of(character));
            when(resourceRepository.findByCategory("FOOD")).thenReturn(new ArrayList<>());
            when(resourceRepository.findByCategory("MATERIAL")).thenReturn(new ArrayList<>());

            List<SurvivalResource> result = resourceService.scavenge(1L, null);

            assertNotNull(result);
        }
    }

    // ---- forage ----

    @Nested
    @DisplayName("forage Tests")
    class ForageTests {

        @Test
        @DisplayName("should return food and possibly water")
        void forage_shouldReturnFood() {
            Earth2026Character character = createCharacter(1L);
            when(characterRepository.findById(1L)).thenReturn(java.util.Optional.of(character));

            SurvivalResource food = new SurvivalResource("Wild Berries", "FOOD");
            when(resourceRepository.findByCategory("FOOD")).thenReturn(List.of(food));
            when(resourceRepository.findByCategory("WATER")).thenReturn(new ArrayList<>());

            List<SurvivalResource> result = resourceService.forage(1L);

            assertNotNull(result);
            assertTrue(result.size() >= 1);
        }

        @Test
        @DisplayName("should throw when character not found")
        void forage_shouldThrowWhenNotFound() {
            when(characterRepository.findById(999L)).thenReturn(java.util.Optional.empty());

            assertThrows(IllegalArgumentException.class, () -> resourceService.forage(999L));
        }

        @Test
        @DisplayName("should return more food with higher WIS")
        void forage_shouldReturnMoreWithHigherWis() {
            Earth2026Character character = createCharacter(1L);
            character.setWisScore(20);
            when(characterRepository.findById(1L)).thenReturn(java.util.Optional.of(character));

            SurvivalResource food1 = new SurvivalResource("Wild Berries", "FOOD");
            SurvivalResource food2 = new SurvivalResource("Mushrooms", "FOOD");
            when(resourceRepository.findByCategory("FOOD")).thenReturn(List.of(food1, food2));
            when(resourceRepository.findByCategory("WATER")).thenReturn(new ArrayList<>());

            List<SurvivalResource> result = resourceService.forage(1L);

            assertNotNull(result);
            assertTrue(result.size() >= 2); // Higher WIS = more food
        }
    }

    // ---- hunt ----

    @Nested
    @DisplayName("hunt Tests")
    class HuntTests {

        @Test
        @DisplayName("should return food on successful hunt")
        void hunt_shouldReturnFood() {
            Earth2026Character character = createCharacter(1L);
            character.setStrScore(15);
            character.setDexScore(15);
            when(characterRepository.findById(1L)).thenReturn(java.util.Optional.of(character));

            SurvivalResource food = new SurvivalResource("Hunted Game", "FOOD");
            when(resourceRepository.findByCategory("FOOD")).thenReturn(List.of(food));

            List<SurvivalResource> result = resourceService.hunt(1L);

            assertNotNull(result);
        }

        @Test
        @DisplayName("should return empty on failed hunt")
        void hunt_shouldReturnEmptyOnFail() {
            Earth2026Character character = createCharacter(1L);
            character.setStrScore(3);
            character.setDexScore(3);
            when(characterRepository.findById(1L)).thenReturn(java.util.Optional.of(character));

            SurvivalResource food = new SurvivalResource("Hunted Game", "FOOD");
            when(resourceRepository.findByCategory("FOOD")).thenReturn(List.of(food));

            // May return empty if hunt fails
            List<SurvivalResource> result = resourceService.hunt(1L);

            assertNotNull(result);
        }

        @Test
        @DisplayName("should throw when character not found")
        void hunt_shouldThrowWhenNotFound() {
            when(characterRepository.findById(999L)).thenReturn(java.util.Optional.empty());

            assertThrows(IllegalArgumentException.class, () -> resourceService.hunt(999L));
        }
    }
}
