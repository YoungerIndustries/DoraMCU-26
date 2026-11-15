package com.yisy.modules.doramcu.earth2026.survival;

import com.yisy.modules.doramcu.earth2026.model.Earth2026Character;
import com.yisy.modules.doramcu.earth2026.repository.Earth2026CharacterRepository;
import com.yisy.modules.doramcu.earth2026.survival.dto.ScavengeResultDto;
import com.yisy.modules.doramcu.earth2026.survival.dto.SurvivalStatusDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("SurvivalService Tests")
class SurvivalServiceTest {

    private SurvivalStateRepository stateRepository;
    private Earth2026CharacterRepository characterRepository;
    private ResourceService resourceService;
    private SurvivalService survivalService;

    @BeforeEach
    void setUp() {
        stateRepository = mock(SurvivalStateRepository.class);
        characterRepository = mock(Earth2026CharacterRepository.class);
        resourceService = mock(ResourceService.class);
        survivalService = new SurvivalService(stateRepository, characterRepository, resourceService);
    }

    private Earth2026Character createCharacter(Long id, int hitPoints) {
        Earth2026Character character = new Earth2026Character();
        character.setId(id);
        character.setName("Test Character");
        character.setHitPoints(hitPoints);
        character.setMaxHitPoints(100);
        character.setStrScore(10);
        character.setDexScore(10);
        character.setConScore(10);
        character.setIntScore(10);
        character.setWisScore(10);
        character.setChaScore(10);
        character.setLevel(1);
        character.setEarthLocation("Test Location");
        return character;
    }

    private SurvivalState createState(Long characterId) {
        SurvivalState state = new SurvivalState(characterId);
        state.setId(1L);
        state.setHunger(100);
        state.setThirst(100);
        state.setFatigue(0);
        state.setBodyTemperature(37.0);
        state.setExposure(0);
        state.setActive(true);
        state.setLastTick(LocalDateTime.now());
        return state;
    }

    // ---- getOrCreateState ----

    @Nested
    @DisplayName("getOrCreateState Tests")
    class GetOrCreateStateTests {

        @Test
        @DisplayName("should return existing state when found")
        void getOrCreateState_shouldReturnExisting() {
            SurvivalState existing = createState(1L);
            when(stateRepository.findByCharacterId(1L)).thenReturn(Optional.of(existing));

            SurvivalState result = survivalService.getOrCreateState(1L);

            assertEquals(existing, result);
            verify(stateRepository, never()).save(any());
        }

        @Test
        @DisplayName("should create default state when not found")
        void getOrCreateState_shouldCreateDefault() {
            when(stateRepository.findByCharacterId(1L)).thenReturn(Optional.empty());
            when(stateRepository.save(any(SurvivalState.class))).thenAnswer(inv -> inv.getArgument(0));

            SurvivalState result = survivalService.getOrCreateState(1L);

            assertNotNull(result);
            assertEquals(1L, result.getCharacterId());
            assertEquals(100, result.getHunger());
            assertEquals(100, result.getThirst());
            assertEquals(0, result.getFatigue());
            assertEquals(37.0, result.getBodyTemperature(), 0.01);
            assertEquals(0, result.getExposure());
            assertTrue(result.isActive());
            verify(stateRepository).save(result);
        }
    }

    // ---- getState ----

    @Nested
    @DisplayName("getState Tests")
    class GetStateTests {

        @Test
        @DisplayName("should return state when found")
        void getState_shouldReturnState() {
            SurvivalState state = createState(1L);
            when(stateRepository.findByCharacterId(1L)).thenReturn(Optional.of(state));

            SurvivalState result = survivalService.getState(1L);

            assertEquals(state, result);
        }

        @Test
        @DisplayName("should return null when not found")
        void getState_shouldReturnNull() {
            when(stateRepository.findByCharacterId(1L)).thenReturn(Optional.empty());

            SurvivalState result = survivalService.getState(1L);

            assertNull(result);
        }
    }

    // ---- eat ----

    @Nested
    @DisplayName("eat Tests")
    class EatTests {

        @Test
        @DisplayName("should restore hunger when eating food")
        void eat_shouldRestoreHunger() {
            SurvivalState state = createState(1L);
            state.setHunger(50);
            when(stateRepository.findByCharacterId(1L)).thenReturn(Optional.of(state));
            when(stateRepository.save(any(SurvivalState.class))).thenAnswer(inv -> inv.getArgument(0));

            SurvivalResource food = new SurvivalResource("Canned Beans", "FOOD");
            food.setHungerValue(30);
            food.setConsumable(true);
            when(resourceService.getResourceByName("Canned Beans")).thenReturn(food);

            SurvivalStatusDto result = survivalService.eat(1L, "Canned Beans");

            assertNotNull(result);
            assertEquals(80, result.getHunger());
            verify(stateRepository).save(state);
        }

        @Test
        @DisplayName("should cap hunger at 100")
        void eat_shouldCapHunger() {
            SurvivalState state = createState(1L);
            state.setHunger(90);
            when(stateRepository.findByCharacterId(1L)).thenReturn(Optional.of(state));
            when(stateRepository.save(any(SurvivalState.class))).thenAnswer(inv -> inv.getArgument(0));

            SurvivalResource food = new SurvivalResource("Large Meal", "FOOD");
            food.setHungerValue(30);
            food.setConsumable(true);
            when(resourceService.getResourceByName("Large Meal")).thenReturn(food);

            SurvivalStatusDto result = survivalService.eat(1L, "Large Meal");

            assertEquals(100, result.getHunger());
        }

        @Test
        @DisplayName("should throw when trying to eat non-food")
        void eat_shouldThrowForNonFood() {
            when(stateRepository.findByCharacterId(1L)).thenReturn(Optional.of(createState(1L)));

            SurvivalResource tool = new SurvivalResource("Hammer", "TOOL");
            tool.setConsumable(false);
            when(resourceService.getResourceByName("Hammer")).thenReturn(tool);

            assertThrows(IllegalArgumentException.class, () -> survivalService.eat(1L, "Hammer"));
        }

        @Test
        @DisplayName("should reduce fatigue if food has fatigue reduction")
        void eat_shouldReduceFatigue() {
            SurvivalState state = createState(1L);
            state.setFatigue(50);
            when(stateRepository.findByCharacterId(1L)).thenReturn(Optional.of(state));
            when(stateRepository.save(any(SurvivalState.class))).thenAnswer(inv -> inv.getArgument(0));

            SurvivalResource food = new SurvivalResource("Energy Bar", "FOOD");
            food.setHungerValue(20);
            food.setFatigueReduction(15);
            food.setConsumable(true);
            when(resourceService.getResourceByName("Energy Bar")).thenReturn(food);

            SurvivalStatusDto result = survivalService.eat(1L, "Energy Bar");

            assertEquals(35, result.getFatigue());
        }

        @Test
        @DisplayName("should restore health if food has health value")
        void eat_shouldRestoreHealth() {
            SurvivalState state = createState(1L);
            when(stateRepository.findByCharacterId(1L)).thenReturn(Optional.of(state));
            when(stateRepository.save(any(SurvivalState.class))).thenAnswer(inv -> inv.getArgument(0));

            Earth2026Character character = createCharacter(1L, 50);
            when(characterRepository.findById(1L)).thenReturn(Optional.of(character));

            SurvivalResource food = new SurvivalResource("Health Soup", "FOOD");
            food.setHungerValue(20);
            food.setHealthValue(15);
            food.setConsumable(true);
            when(resourceService.getResourceByName("Health Soup")).thenReturn(food);

            survivalService.eat(1L, "Health Soup");

            assertEquals(65, character.getHitPoints());
            verify(characterRepository).save(character);
        }
    }

    // ---- drink ----

    @Nested
    @DisplayName("drink Tests")
    class DrinkTests {

        @Test
        @DisplayName("should restore thirst when drinking water")
        void drink_shouldRestoreThirst() {
            SurvivalState state = createState(1L);
            state.setThirst(40);
            when(stateRepository.findByCharacterId(1L)).thenReturn(Optional.of(state));
            when(stateRepository.save(any(SurvivalState.class))).thenAnswer(inv -> inv.getArgument(0));

            SurvivalResource water = new SurvivalResource("Water Bottle", "WATER");
            water.setThirstValue(40);
            water.setConsumable(true);
            when(resourceService.getResourceByName("Water Bottle")).thenReturn(water);

            SurvivalStatusDto result = survivalService.drink(1L, "Water Bottle");

            assertNotNull(result);
            assertEquals(80, result.getThirst());
        }

        @Test
        @DisplayName("should cap thirst at 100")
        void drink_shouldCapThirst() {
            SurvivalState state = createState(1L);
            state.setThirst(90);
            when(stateRepository.findByCharacterId(1L)).thenReturn(Optional.of(state));
            when(stateRepository.save(any(SurvivalState.class))).thenAnswer(inv -> inv.getArgument(0));

            SurvivalResource water = new SurvivalResource("Large Water Jug", "WATER");
            water.setThirstValue(30);
            water.setConsumable(true);
            when(resourceService.getResourceByName("Large Water Jug")).thenReturn(water);

            SurvivalStatusDto result = survivalService.drink(1L, "Large Water Jug");

            assertEquals(100, result.getThirst());
        }

        @Test
        @DisplayName("should throw when trying to drink non-water")
        void drink_shouldThrowForNonWater() {
            when(stateRepository.findByCharacterId(1L)).thenReturn(Optional.of(createState(1L)));

            SurvivalResource food = new SurvivalResource("Canned Beans", "FOOD");
            food.setConsumable(false);
            when(resourceService.getResourceByName("Canned Beans")).thenReturn(food);

            assertThrows(IllegalArgumentException.class, () -> survivalService.drink(1L, "Canned Beans"));
        }

        @Test
        @DisplayName("should apply negative health for contaminated water")
        void drink_shouldApplyNegativeHealth() {
            SurvivalState state = createState(1L);
            when(stateRepository.findByCharacterId(1L)).thenReturn(Optional.of(state));
            when(stateRepository.save(any(SurvivalState.class))).thenAnswer(inv -> inv.getArgument(0));

            Earth2026Character character = createCharacter(1L, 50);
            when(characterRepository.findById(1L)).thenReturn(Optional.of(character));

            SurvivalResource water = new SurvivalResource("Contaminated Water", "WATER");
            water.setThirstValue(20);
            water.setHealthValue(-10);
            water.setConsumable(true);
            when(resourceService.getResourceByName("Contaminated Water")).thenReturn(water);

            survivalService.drink(1L, "Contaminated Water");

            assertEquals(40, character.getHitPoints());
        }
    }

    // ---- rest ----

    @Nested
    @DisplayName("rest Tests")
    class RestTests {

        @Test
        @DisplayName("should reduce fatigue by 10 per hour")
        void rest_shouldReduceFatigue() {
            SurvivalState state = createState(1L);
            state.setFatigue(50);
            when(stateRepository.findByCharacterId(1L)).thenReturn(Optional.of(state));
            when(stateRepository.save(any(SurvivalState.class))).thenAnswer(inv -> inv.getArgument(0));

            SurvivalStatusDto result = survivalService.rest(1L, 3);

            assertEquals(20, result.getFatigue());
        }

        @Test
        @DisplayName("should not reduce fatigue below 0")
        void rest_shouldNotReduceFatigueBelowZero() {
            SurvivalState state = createState(1L);
            state.setFatigue(15);
            when(stateRepository.findByCharacterId(1L)).thenReturn(Optional.of(state));
            when(stateRepository.save(any(SurvivalState.class))).thenAnswer(inv -> inv.getArgument(0));

            SurvivalStatusDto result = survivalService.rest(1L, 3);

            assertEquals(0, result.getFatigue());
        }

        @Test
        @DisplayName("should decay hunger and thirst during rest")
        void rest_shouldDecayHungerAndThirst() {
            SurvivalState state = createState(1L);
            state.setHunger(100);
            state.setThirst(100);
            when(stateRepository.findByCharacterId(1L)).thenReturn(Optional.of(state));
            when(stateRepository.save(any(SurvivalState.class))).thenAnswer(inv -> inv.getArgument(0));

            SurvivalStatusDto result = survivalService.rest(1L, 4);

            // Hunger decay: max(1, (int)(2 * 4 * 0.5)) = 4
            // Thirst decay: max(1, (int)(3 * 4 * 0.5)) = 6
            assertEquals(96, result.getHunger());
            assertEquals(94, result.getThirst());
        }

        @Test
        @DisplayName("should normalize body temperature during rest")
        void rest_shouldNormalizeTemperature() {
            SurvivalState state = createState(1L);
            state.setBodyTemperature(35.0); // hypothermia
            when(stateRepository.findByCharacterId(1L)).thenReturn(Optional.of(state));
            when(stateRepository.save(any(SurvivalState.class))).thenAnswer(inv -> inv.getArgument(0));

            SurvivalStatusDto result = survivalService.rest(1L, 5);

            // 35.0 + 0.1 * 5 = 35.5
            assertEquals(35.5, result.getBodyTemperature(), 0.01);
        }

        @Test
        @DisplayName("should reduce exposure during rest")
        void rest_shouldReduceExposure() {
            SurvivalState state = createState(1L);
            state.setExposure(60);
            when(stateRepository.findByCharacterId(1L)).thenReturn(Optional.of(state));
            when(stateRepository.save(any(SurvivalState.class))).thenAnswer(inv -> inv.getArgument(0));

            SurvivalStatusDto result = survivalService.rest(1L, 3);

            // 60 - 3 * 5 = 45
            assertEquals(45, result.getExposure());
        }

        @Test
        @DisplayName("should not reduce exposure below 0")
        void rest_shouldNotReduceExposureBelowZero() {
            SurvivalState state = createState(1L);
            state.setExposure(10);
            when(stateRepository.findByCharacterId(1L)).thenReturn(Optional.of(state));
            when(stateRepository.save(any(SurvivalState.class))).thenAnswer(inv -> inv.getArgument(0));

            SurvivalStatusDto result = survivalService.rest(1L, 3);

            assertEquals(0, result.getExposure());
        }
    }

    // ---- scavenge ----

    @Nested
    @DisplayName("scavenge Tests")
    class ScavengeTests {

        @Test
        @DisplayName("should return resources and increase fatigue")
        void scavenge_shouldReturnResources() {
            SurvivalState state = createState(1L);
            state.setFatigue(20);
            when(stateRepository.findByCharacterId(1L)).thenReturn(Optional.of(state));
            when(stateRepository.save(any(SurvivalState.class))).thenAnswer(inv -> inv.getArgument(0));

            SurvivalResource resource1 = new SurvivalResource("Canned Beans", "FOOD");
            SurvivalResource resource2 = new SurvivalResource("Wood Plank", "MATERIAL");
            when(resourceService.scavenge(1L, "Abandoned Building"))
                    .thenReturn(List.of(resource1, resource2));

            ScavengeResultDto result = survivalService.scavenge(1L, "Abandoned Building");

            assertTrue(result.isSuccess());
            assertEquals(2, result.getTotalItems());
            assertEquals("Abandoned Building", result.getLocation());
            assertEquals(25, state.getFatigue()); // 20 + 5
        }

        @Test
        @DisplayName("should fail if character is exhausted")
        void scavenge_shouldFailWhenExhausted() {
            SurvivalState state = createState(1L);
            state.setFatigue(100);
            when(stateRepository.findByCharacterId(1L)).thenReturn(Optional.of(state));

            ScavengeResultDto result = survivalService.scavenge(1L, "Abandoned Building");

            assertFalse(result.isSuccess());
            assertTrue(result.getMessage().contains("exhausted"));
            verify(stateRepository, never()).save(any());
        }

        @Test
        @DisplayName("should use current location when location is null")
        void scavenge_shouldUseCurrentLocation() {
            SurvivalState state = createState(1L);
            when(stateRepository.findByCharacterId(1L)).thenReturn(Optional.of(state));
            when(stateRepository.save(any(SurvivalState.class))).thenAnswer(inv -> inv.getArgument(0));
            when(resourceService.scavenge(1L, null)).thenReturn(List.of());

            ScavengeResultDto result = survivalService.scavenge(1L, null);

            assertTrue(result.isSuccess());
            assertEquals("Current location", result.getLocation());
        }
    }

    // ---- processTick ----

    @Nested
    @DisplayName("processTick Tests")
    class ProcessTickTests {

        @Test
        @DisplayName("should skip inactive characters")
        void processTick_shouldSkipInactive() {
            SurvivalState state = createState(1L);
            state.setActive(false);
            when(stateRepository.findByCharacterId(1L)).thenReturn(Optional.of(state));

            survivalService.processTick(state, 20.0, false, 30);

            // Hunger should not change
            assertEquals(100, state.getHunger());
            verify(stateRepository, never()).save(any());
        }

        @Test
        @DisplayName("should decay hunger and thirst")
        void processTick_shouldDecayStats() {
            SurvivalState state = createState(1L);
            when(stateRepository.save(any(SurvivalState.class))).thenAnswer(inv -> inv.getArgument(0));

            survivalService.processTick(state, 20.0, false, 30);

            assertEquals(98, state.getHunger()); // 100 - 2
            assertEquals(97, state.getThirst()); // 100 - 3
        }

        @Test
        @DisplayName("should increase fatigue")
        void processTick_shouldIncreaseFatigue() {
            SurvivalState state = createState(1L);
            state.setFatigue(50);
            when(stateRepository.save(any(SurvivalState.class))).thenAnswer(inv -> inv.getArgument(0));

            survivalService.processTick(state, 20.0, false, 30);

            assertEquals(51, state.getFatigue()); // 50 + 1
        }

        @Test
        @DisplayName("should increase fatigue more at night")
        void processTick_shouldIncreaseFatigueAtNight() {
            SurvivalState state = createState(1L);
            state.setFatigue(50);
            when(stateRepository.save(any(SurvivalState.class))).thenAnswer(inv -> inv.getArgument(0));

            survivalService.processTick(state, 20.0, true, 30);

            assertEquals(52, state.getFatigue()); // 50 + 1 + 1 (night bonus)
        }

        @Test
        @DisplayName("should drift temperature toward ambient")
        void processTick_shouldDriftTemperature() {
            SurvivalState state = createState(1L);
            state.setBodyTemperature(37.0);
            when(stateRepository.save(any(SurvivalState.class))).thenAnswer(inv -> inv.getArgument(0));

            survivalService.processTick(state, 10.0, false, 30);

            // 37.0 + (10.0 - 37.0) * 0.01 = 37.0 - 0.27 = 36.73
            assertEquals(36.73, state.getBodyTemperature(), 0.01);
        }

        @Test
        @DisplayName("should increase exposure in severe weather")
        void processTick_shouldIncreaseExposure() {
            SurvivalState state = createState(1L);
            state.setExposure(10);
            when(stateRepository.save(any(SurvivalState.class))).thenAnswer(inv -> inv.getArgument(0));

            survivalService.processTick(state, 20.0, false, 70);

            // (70 - 50) / 10 = 2
            assertEquals(12, state.getExposure());
        }

        @Test
        @DisplayName("should decrease exposure in mild weather")
        void processTick_shouldDecreaseExposure() {
            SurvivalState state = createState(1L);
            state.setExposure(10);
            when(stateRepository.save(any(SurvivalState.class))).thenAnswer(inv -> inv.getArgument(0));

            survivalService.processTick(state, 20.0, false, 30);

            assertEquals(9, state.getExposure()); // 10 - 1
        }

        @Test
        @DisplayName("should not decrease exposure below 0")
        void processTick_shouldNotDecreaseExposureBelowZero() {
            SurvivalState state = createState(1L);
            state.setExposure(0);
            when(stateRepository.save(any(SurvivalState.class))).thenAnswer(inv -> inv.getArgument(0));

            survivalService.processTick(state, 20.0, false, 30);

            assertEquals(0, state.getExposure());
        }

        @Test
        @DisplayName("should apply starvation damage")
        void processTick_shouldApplyStarvationDamage() {
            SurvivalState state = createState(1L);
            state.setHunger(0);
            Earth2026Character character = createCharacter(1L, 50);
            when(characterRepository.findById(1L)).thenReturn(Optional.of(character));
            when(stateRepository.save(any(SurvivalState.class))).thenAnswer(inv -> inv.getArgument(0));

            survivalService.processTick(state, 20.0, false, 30);

            assertEquals(45, character.getHitPoints()); // 50 - 5 starvation
            verify(characterRepository).save(character);
        }

        @Test
        @DisplayName("should apply dehydration damage")
        void processTick_shouldApplyDehydrationDamage() {
            SurvivalState state = createState(1L);
            state.setThirst(0);
            Earth2026Character character = createCharacter(1L, 50);
            when(characterRepository.findById(1L)).thenReturn(Optional.of(character));
            when(stateRepository.save(any(SurvivalState.class))).thenAnswer(inv -> inv.getArgument(0));

            survivalService.processTick(state, 20.0, false, 30);

            assertEquals(45, character.getHitPoints()); // 50 - 5 dehydration
        }

        @Test
        @DisplayName("should apply exhaustion damage")
        void processTick_shouldApplyExhaustionDamage() {
            SurvivalState state = createState(1L);
            state.setFatigue(100);
            Earth2026Character character = createCharacter(1L, 50);
            when(characterRepository.findById(1L)).thenReturn(Optional.of(character));
            when(stateRepository.save(any(SurvivalState.class))).thenAnswer(inv -> inv.getArgument(0));

            survivalService.processTick(state, 20.0, false, 30);

            assertEquals(47, character.getHitPoints()); // 50 - 3 exhaustion
        }

        @Test
        @DisplayName("should apply exposure damage at critical level")
        void processTick_shouldApplyExposureDamage() {
            SurvivalState state = createState(1L);
            state.setExposure(100);
            Earth2026Character character = createCharacter(1L, 50);
            when(characterRepository.findById(1L)).thenReturn(Optional.of(character));
            when(stateRepository.save(any(SurvivalState.class))).thenAnswer(inv -> inv.getArgument(0));

            // Use weather severity > 50 so exposure stays at 100 (capped)
            survivalService.processTick(state, 20.0, false, 70);

            assertEquals(45, character.getHitPoints()); // 50 - 5 exposure
        }

        @Test
        @DisplayName("should apply hypothermia damage")
        void processTick_shouldApplyHypothermiaDamage() {
            SurvivalState state = createState(1L);
            state.setBodyTemperature(34.0);
            Earth2026Character character = createCharacter(1L, 50);
            when(characterRepository.findById(1L)).thenReturn(Optional.of(character));
            when(stateRepository.save(any(SurvivalState.class))).thenAnswer(inv -> inv.getArgument(0));

            survivalService.processTick(state, 20.0, false, 30);

            assertEquals(45, character.getHitPoints()); // 50 - 5 hypothermia
        }

        @Test
        @DisplayName("should apply heat stroke damage")
        void processTick_shouldApplyHeatStrokeDamage() {
            SurvivalState state = createState(1L);
            state.setBodyTemperature(41.0);
            Earth2026Character character = createCharacter(1L, 50);
            when(characterRepository.findById(1L)).thenReturn(Optional.of(character));
            when(stateRepository.save(any(SurvivalState.class))).thenAnswer(inv -> inv.getArgument(0));

            survivalService.processTick(state, 20.0, false, 30);

            assertEquals(45, character.getHitPoints()); // 50 - 5 heat stroke
        }

        @Test
        @DisplayName("should combine multiple damage sources")
        void processTick_shouldCombineDamage() {
            SurvivalState state = createState(1L);
            state.setHunger(0);
            state.setThirst(0);
            state.setFatigue(100);
            Earth2026Character character = createCharacter(1L, 50);
            when(characterRepository.findById(1L)).thenReturn(Optional.of(character));
            when(stateRepository.save(any(SurvivalState.class))).thenAnswer(inv -> inv.getArgument(0));

            survivalService.processTick(state, 20.0, false, 30);

            // 5 starvation + 5 dehydration + 3 exhaustion = 13
            assertEquals(37, character.getHitPoints());
        }

        @Test
        @DisplayName("should not reduce HP below 0")
        void processTick_shouldNotReduceHPBelowZero() {
            SurvivalState state = createState(1L);
            state.setHunger(0);
            state.setThirst(0);
            state.setFatigue(100);
            state.setExposure(100);
            Earth2026Character character = createCharacter(1L, 10);
            when(characterRepository.findById(1L)).thenReturn(Optional.of(character));
            when(stateRepository.save(any(SurvivalState.class))).thenAnswer(inv -> inv.getArgument(0));

            survivalService.processTick(state, 20.0, false, 30);

            assertEquals(0, character.getHitPoints());
        }

        @Test
        @DisplayName("should not apply damage when character not found")
        void processTick_shouldNotApplyDamageWhenNotFound() {
            SurvivalState state = createState(1L);
            state.setHunger(0);
            when(characterRepository.findById(1L)).thenReturn(Optional.empty());
            when(stateRepository.save(any(SurvivalState.class))).thenAnswer(inv -> inv.getArgument(0));

            survivalService.processTick(state, 20.0, false, 30);

            verify(characterRepository, never()).save(any());
        }

        @Test
        @DisplayName("should update lastTick and updatedAt")
        void processTick_shouldUpdateTimestamps() {
            SurvivalState state = createState(1L);
            state.setLastTick(null);
            when(stateRepository.save(any(SurvivalState.class))).thenAnswer(inv -> inv.getArgument(0));

            survivalService.processTick(state, 20.0, false, 30);

            assertNotNull(state.getLastTick());
            assertNotNull(state.getUpdatedAt());
        }
    }

    // ---- getStatus ----

    @Nested
    @DisplayName("getStatus Tests")
    class GetStatusTests {

        @Test
        @DisplayName("should return DTO with all fields")
        void getStatus_shouldReturnDto() {
            SurvivalState state = createState(1L);
            state.setHunger(50);
            state.setThirst(60);
            state.setFatigue(30);
            when(stateRepository.findByCharacterId(1L)).thenReturn(Optional.of(state));

            SurvivalStatusDto result = survivalService.getStatus(1L);

            assertEquals(1L, result.getCharacterId());
            assertEquals(50, result.getHunger());
            assertEquals(60, result.getThirst());
            assertEquals(30, result.getFatigue());
            assertFalse(result.isStarving());
            assertFalse(result.isDehydrated());
            assertFalse(result.isExhausted());
            assertFalse(result.isCritical());
        }

        @Test
        @DisplayName("should mark starving when hunger is 0")
        void getStatus_shouldMarkStarving() {
            SurvivalState state = createState(1L);
            state.setHunger(0);
            when(stateRepository.findByCharacterId(1L)).thenReturn(Optional.of(state));

            SurvivalStatusDto result = survivalService.getStatus(1L);

            assertTrue(result.isStarving());
            assertTrue(result.isCritical());
        }

        @Test
        @DisplayName("should mark dehydrated when thirst is 0")
        void getStatus_shouldMarkDehydrated() {
            SurvivalState state = createState(1L);
            state.setThirst(0);
            when(stateRepository.findByCharacterId(1L)).thenReturn(Optional.of(state));

            SurvivalStatusDto result = survivalService.getStatus(1L);

            assertTrue(result.isDehydrated());
        }

        @Test
        @DisplayName("should mark exhausted when fatigue is 100")
        void getStatus_shouldMarkExhausted() {
            SurvivalState state = createState(1L);
            state.setFatigue(100);
            when(stateRepository.findByCharacterId(1L)).thenReturn(Optional.of(state));

            SurvivalStatusDto result = survivalService.getStatus(1L);

            assertTrue(result.isExhausted());
        }

        @Test
        @DisplayName("should mark hypothermia when temp below 35")
        void getStatus_shouldMarkHypothermia() {
            SurvivalState state = createState(1L);
            state.setBodyTemperature(34.0);
            when(stateRepository.findByCharacterId(1L)).thenReturn(Optional.of(state));

            SurvivalStatusDto result = survivalService.getStatus(1L);

            assertTrue(result.hasHypothermia());
        }

        @Test
        @DisplayName("should mark heat stroke when temp above 40")
        void getStatus_shouldMarkHeatStroke() {
            SurvivalState state = createState(1L);
            state.setBodyTemperature(41.0);
            when(stateRepository.findByCharacterId(1L)).thenReturn(Optional.of(state));

            SurvivalStatusDto result = survivalService.getStatus(1L);

            assertTrue(result.hasHeatStroke());
        }

        @Test
        @DisplayName("should mark critical exposure when exposure is 100")
        void getStatus_shouldMarkCriticalExposure() {
            SurvivalState state = createState(1L);
            state.setExposure(100);
            when(stateRepository.findByCharacterId(1L)).thenReturn(Optional.of(state));

            SurvivalStatusDto result = survivalService.getStatus(1L);

            assertTrue(result.hasCriticalExposure());
        }
    }
}
