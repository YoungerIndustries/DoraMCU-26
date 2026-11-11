package com.yisy.modules.doramcu.dnd5e.service;

import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCharacter;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCharacterFeat;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eFeat;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5eCharacterFeatRepository;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5eCharacterRepository;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5eFeatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.argThat;

@DisplayName("Dnd5eFeatService Tests")
class Dnd5eFeatServiceTest {

    private Dnd5eFeatRepository featRepository;
    private Dnd5eCharacterFeatRepository characterFeatRepository;
    private Dnd5eCharacterRepository characterRepository;
    private Dnd5eFeatService service;

    private Dnd5eCharacter fighter;
    private Dnd5eCharacter wizard;
    private Dnd5eFeat tough;
    private Dnd5eFeat lucky;
    private Dnd5eFeat greatWeaponMaster;

    @BeforeEach
    void setUp() {
        featRepository = mock(Dnd5eFeatRepository.class);
        characterFeatRepository = mock(Dnd5eCharacterFeatRepository.class);
        characterRepository = mock(Dnd5eCharacterRepository.class);
        service = new Dnd5eFeatService(featRepository, characterFeatRepository, characterRepository);

        // Fighter with high STR
        fighter = new Dnd5eCharacter();
        fighter.setId(1L);
        fighter.setName("Thorin");
        fighter.setRace("Human");
        fighter.setCharacterClass("Fighter");
        fighter.setLevel(4);
        fighter.setStrScore(16);
        fighter.setDexScore(12);
        fighter.setConScore(14);
        fighter.setIntScore(10);
        fighter.setWisScore(10);
        fighter.setChaScore(12);
        fighter.setProficiencyBonus(2);
        fighter.setHitPoints(20);
        fighter.setMaxHitPoints(20);

        // Wizard with high INT
        wizard = new Dnd5eCharacter();
        wizard.setId(2L);
        wizard.setName("Gandalf");
        wizard.setRace("Elf");
        wizard.setCharacterClass("Wizard");
        wizard.setLevel(6);
        wizard.setStrScore(8);
        wizard.setDexScore(14);
        wizard.setConScore(12);
        wizard.setIntScore(16);
        wizard.setWisScore(14);
        wizard.setChaScore(10);
        wizard.setProficiencyBonus(3);
        wizard.setHitPoints(24);
        wizard.setMaxHitPoints(24);

        // Tough feat (no requirement)
        tough = new Dnd5eFeat();
        tough.setId(1L);
        tough.setName("Tough");
        tough.setDescription("Your hit point maximum increases by 2.");
        tough.setCategory("General");
        tough.setAbilityRequirement(null);
        tough.setAbilityScoreRequired(0);
        tough.setPrerequisite("None");
        tough.setRepeatable(false);
        tough.setAbilityIncrease(0);

        // Lucky feat (no requirement)
        lucky = new Dnd5eFeat();
        lucky.setId(2L);
        lucky.setName("Lucky");
        lucky.setDescription("You have 3 charges of luck.");
        lucky.setCategory("General");
        lucky.setAbilityRequirement(null);
        lucky.setAbilityScoreRequired(0);
        lucky.setPrerequisite("None");
        lucky.setRepeatable(false);
        lucky.setAbilityIncrease(0);

        // Great Weapon Master (STR 13)
        greatWeaponMaster = new Dnd5eFeat();
        greatWeaponMaster.setId(3L);
        greatWeaponMaster.setName("Great Weapon Master");
        greatWeaponMaster.setDescription("Extra damage on critical hits.");
        greatWeaponMaster.setCategory("Combat");
        greatWeaponMaster.setAbilityRequirement("STR");
        greatWeaponMaster.setAbilityScoreRequired(13);
        greatWeaponMaster.setPrerequisite("None");
        greatWeaponMaster.setRepeatable(false);
        greatWeaponMaster.setAbilityIncrease(0);
    }

    // ---- getFeatByName ----

    @Nested
    @DisplayName("getFeatByName Tests")
    class GetFeatByNameTests {

        @Test
        @DisplayName("should return feat by name")
        void getFeatByName_shouldReturnFeat() {
            when(featRepository.findByName("Tough")).thenReturn(Optional.of(tough));

            Dnd5eFeat result = service.getFeatByName("Tough");

            assertNotNull(result);
            assertEquals("Tough", result.getName());
            assertEquals("General", result.getCategory());
        }

        @Test
        @DisplayName("should throw when feat not found")
        void getFeatByName_shouldThrowWhenNotFound() {
            when(featRepository.findByName("Nonexistent")).thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class, () -> service.getFeatByName("Nonexistent"));
        }
    }

    // ---- getFeat ----

    @Nested
    @DisplayName("getFeat Tests")
    class GetFeatTests {

        @Test
        @DisplayName("should return feat by ID")
        void getFeat_shouldReturnFeat() {
            when(featRepository.findById(1L)).thenReturn(Optional.of(tough));

            Dnd5eFeat result = service.getFeat(1L);

            assertNotNull(result);
            assertEquals("Tough", result.getName());
        }

        @Test
        @DisplayName("should throw when feat not found")
        void getFeat_shouldThrowWhenNotFound() {
            when(featRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class, () -> service.getFeat(99L));
        }
    }

    // ---- listFeats ----

    @Nested
    @DisplayName("listFeats Tests")
    class ListFeatsTests {

        @Test
        @DisplayName("should return all feats")
        void listFeats_shouldReturnAll() {
            when(featRepository.findAll()).thenReturn(List.of(tough, lucky, greatWeaponMaster));

            List<Dnd5eFeat> result = service.listFeats();

            assertEquals(3, result.size());
        }

        @Test
        @DisplayName("should return empty list when no feats")
        void listFeats_shouldReturnEmpty() {
            when(featRepository.findAll()).thenReturn(List.of());

            List<Dnd5eFeat> result = service.listFeats();

            assertTrue(result.isEmpty());
        }
    }

    // ---- listFeatsByCategory ----

    @Nested
    @DisplayName("listFeatsByCategory Tests")
    class ListFeatsByCategoryTests {

        @Test
        @DisplayName("should return feats by category")
        void listFeatsByCategory_shouldReturnFeats() {
            when(featRepository.findByCategory("Combat")).thenReturn(List.of(greatWeaponMaster));

            List<Dnd5eFeat> result = service.listFeatsByCategory("Combat");

            assertEquals(1, result.size());
            assertEquals("Great Weapon Master", result.get(0).getName());
        }
    }

    // ---- listFeatsByAbility ----

    @Nested
    @DisplayName("listFeatsByAbility Tests")
    class ListFeatsByAbilityTests {

        @Test
        @DisplayName("should return feats by ability requirement")
        void listFeatsByAbility_shouldReturnFeats() {
            when(featRepository.findByAbilityRequirement("STR")).thenReturn(List.of(greatWeaponMaster));

            List<Dnd5eFeat> result = service.listFeatsByAbility("STR");

            assertEquals(1, result.size());
            assertEquals("Great Weapon Master", result.get(0).getName());
        }
    }

    // ---- searchFeats ----

    @Nested
    @DisplayName("searchFeats Tests")
    class SearchFeatsTests {

        @Test
        @DisplayName("should search by keyword")
        void searchFeats_shouldFindMatch() {
            when(featRepository.searchByNameOrDescription("luck")).thenReturn(List.of(lucky));

            List<Dnd5eFeat> result = service.searchFeats("luck");

            assertEquals(1, result.size());
            assertEquals("Lucky", result.get(0).getName());
        }

        @Test
        @DisplayName("should return empty when no match")
        void searchFeats_shouldReturnEmpty() {
            when(featRepository.searchByNameOrDescription("nonexistent")).thenReturn(List.of());

            List<Dnd5eFeat> result = service.searchFeats("nonexistent");

            assertTrue(result.isEmpty());
        }
    }

    // ---- listAvailableFeats ----

    @Nested
    @DisplayName("listAvailableFeats Tests")
    class ListAvailableFeatsTests {

        @Test
        @DisplayName("should return feats character can take")
        void listAvailableFeats_shouldReturnAvailable() {
            when(characterRepository.findById(1L)).thenReturn(Optional.of(fighter));
            when(featRepository.findByAbilityScoreLessThanEqual(10)).thenReturn(List.of(tough, lucky));
            when(characterFeatRepository.findByCharacterIdAndActiveTrue(1L)).thenReturn(List.of());

            List<Dnd5eFeat> result = service.listAvailableFeats(1L);

            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("should exclude already-owned non-repeatable feats")
        void listAvailableFeats_shouldExcludeOwned() {
            when(characterRepository.findById(1L)).thenReturn(Optional.of(fighter));
            when(featRepository.findByAbilityScoreLessThanEqual(10)).thenReturn(List.of(tough, lucky));

            Dnd5eCharacterFeat ownedFeat = new Dnd5eCharacterFeat();
            ownedFeat.setFeatName("Tough");
            when(characterFeatRepository.findByCharacterIdAndActiveTrue(1L)).thenReturn(List.of(ownedFeat));

            List<Dnd5eFeat> result = service.listAvailableFeats(1L);

            assertEquals(1, result.size());
            assertEquals("Lucky", result.get(0).getName());
        }

        @Test
        @DisplayName("should throw when character not found")
        void listAvailableFeats_shouldThrowWhenNotFound() {
            when(characterRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class, () -> service.listAvailableFeats(99L));
        }
    }

    // ---- createFeat ----

    @Nested
    @DisplayName("createFeat Tests")
    class CreateFeatTests {

        @Test
        @DisplayName("should create a new feat")
        void createFeat_shouldCreate() {
            when(featRepository.findByName("New Feat")).thenReturn(Optional.empty());
            when(featRepository.save(any(Dnd5eFeat.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Dnd5eFeat result = service.createFeat("New Feat", "Description", "General",
                    null, 0, "None", false, 0, null);

            assertNotNull(result);
            assertEquals("New Feat", result.getName());
            assertEquals("General", result.getCategory());
            assertEquals(0, result.getAbilityIncrease());
        }

        @Test
        @DisplayName("should throw when feat already exists")
        void createFeat_shouldThrowWhenDuplicate() {
            when(featRepository.findByName("Tough")).thenReturn(Optional.of(tough));

            assertThrows(IllegalArgumentException.class,
                    () -> service.createFeat("Tough", "Desc", "General", null, 0, "None", false, 0, null));
        }

        @Test
        @DisplayName("should clamp ability increase to 0-2")
        void createFeat_shouldClampAbilityIncrease() {
            when(featRepository.findByName("Clamped Feat")).thenReturn(Optional.empty());
            when(featRepository.save(any(Dnd5eFeat.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Dnd5eFeat result = service.createFeat("Clamped Feat", "Desc", "General",
                    null, 0, "None", false, 10, "STR");

            assertEquals(2, result.getAbilityIncrease());
        }
    }

    // ---- deleteFeat ----

    @Nested
    @DisplayName("deleteFeat Tests")
    class DeleteFeatTests {

        @Test
        @DisplayName("should delete a feat")
        void deleteFeat_shouldDelete() {
            when(featRepository.findByName("Tough")).thenReturn(Optional.of(tough));
            doNothing().when(featRepository).delete(tough);

            assertDoesNotThrow(() -> service.deleteFeat("Tough"));
            verify(featRepository).delete(tough);
        }
    }

    // ---- assignFeatToCharacter ----

    @Nested
    @DisplayName("assignFeatToCharacter Tests")
    class AssignFeatToCharacterTests {

        @Test
        @DisplayName("should assign a feat to a character")
        void assignFeat_shouldAssign() {
            when(characterRepository.findById(1L)).thenReturn(Optional.of(fighter));
            when(featRepository.findByName("Tough")).thenReturn(Optional.of(tough));
            when(characterFeatRepository.existsByCharacterIdAndFeatName(1L, "Tough")).thenReturn(false);
            when(characterFeatRepository.save(any(Dnd5eCharacterFeat.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Dnd5eCharacterFeat result = service.assignFeatToCharacter(1L, "Tough");

            assertNotNull(result);
            assertEquals(1L, result.getCharacterId());
            assertEquals("Tough", result.getFeatName());
            assertEquals(4, result.getLevelAcquired());
            assertTrue(result.isActive());
        }

        @Test
        @DisplayName("should throw when character already has non-repeatable feat")
        void assignFeat_shouldThrowWhenAlreadyOwned() {
            when(characterRepository.findById(1L)).thenReturn(Optional.of(fighter));
            when(featRepository.findByName("Tough")).thenReturn(Optional.of(tough));
            when(characterFeatRepository.existsByCharacterIdAndFeatName(1L, "Tough")).thenReturn(true);

            assertThrows(IllegalArgumentException.class,
                    () -> service.assignFeatToCharacter(1L, "Tough"));
        }

        @Test
        @DisplayName("should allow assigning repeatable feat")
        void assignFeat_shouldAllowRepeatable() {
            when(characterRepository.findById(1L)).thenReturn(Optional.of(fighter));

            Dnd5eFeat magicInitiate = new Dnd5eFeat();
            magicInitiate.setName("Magic Initiate");
            magicInitiate.setRepeatable(true);
            magicInitiate.setAbilityScoreRequired(0);
            magicInitiate.setPrerequisite("None");

            when(featRepository.findByName("Magic Initiate")).thenReturn(Optional.of(magicInitiate));
            when(characterFeatRepository.existsByCharacterIdAndFeatName(1L, "Magic Initiate")).thenReturn(true);
            when(characterFeatRepository.save(any(Dnd5eCharacterFeat.class))).thenAnswer(invocation -> invocation.getArgument(0));

            assertDoesNotThrow(() -> service.assignFeatToCharacter(1L, "Magic Initiate"));
        }

        @Test
        @DisplayName("should throw when ability requirement not met")
        void assignFeat_shouldThrowWhenAbilityNotMet() {
            when(characterRepository.findById(2L)).thenReturn(Optional.of(wizard));
            when(featRepository.findByName("Great Weapon Master")).thenReturn(Optional.of(greatWeaponMaster));
            when(characterFeatRepository.existsByCharacterIdAndFeatName(2L, "Great Weapon Master")).thenReturn(false);

            // Wizard has STR 8, GWM requires STR 13
            assertThrows(IllegalArgumentException.class,
                    () -> service.assignFeatToCharacter(2L, "Great Weapon Master"));
        }

        @Test
        @DisplayName("should apply ability score increase")
        void assignFeat_shouldApplyAbilityIncrease() {
            when(characterRepository.findById(1L)).thenReturn(Optional.of(fighter));

            Dnd5eFeat observant = new Dnd5eFeat();
            observant.setName("Observant");
            observant.setAbilityScoreRequired(0);
            observant.setPrerequisite("None");
            observant.setRepeatable(false);
            observant.setAbilityIncrease(1);
            observant.setAbilityIncreased("WIS");

            when(featRepository.findByName("Observant")).thenReturn(Optional.of(observant));
            when(characterFeatRepository.existsByCharacterIdAndFeatName(1L, "Observant")).thenReturn(false);
            when(characterRepository.save(any(Dnd5eCharacter.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(characterFeatRepository.save(any(Dnd5eCharacterFeat.class))).thenAnswer(invocation -> invocation.getArgument(0));

            service.assignFeatToCharacter(1L, "Observant");

            // WIS should increase from 10 to 11
            verify(characterRepository).save(argThat(c -> c.getWisScore() == 11));
        }

        @Test
        @DisplayName("should cap ability score at 20")
        void assignFeat_shouldCapAbilityAt20() {
            Dnd5eCharacter maxChar = new Dnd5eCharacter();
            maxChar.setId(3L);
            maxChar.setName("Maxed");
            maxChar.setStrScore(20);
            maxChar.setDexScore(20);
            maxChar.setConScore(20);
            maxChar.setIntScore(20);
            maxChar.setWisScore(20);
            maxChar.setChaScore(20);
            maxChar.setLevel(10);

            when(characterRepository.findById(3L)).thenReturn(Optional.of(maxChar));

            Dnd5eFeat observant = new Dnd5eFeat();
            observant.setName("Observant");
            observant.setAbilityScoreRequired(0);
            observant.setPrerequisite("None");
            observant.setRepeatable(false);
            observant.setAbilityIncrease(1);
            observant.setAbilityIncreased("WIS");

            when(featRepository.findByName("Observant")).thenReturn(Optional.of(observant));
            when(characterFeatRepository.existsByCharacterIdAndFeatName(3L, "Observant")).thenReturn(false);
            when(characterRepository.save(any(Dnd5eCharacter.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(characterFeatRepository.save(any(Dnd5eCharacterFeat.class))).thenAnswer(invocation -> invocation.getArgument(0));

            service.assignFeatToCharacter(3L, "Observant");

            // WIS should stay at 20, not go to 21
            verify(characterRepository).save(argThat(c -> c.getWisScore() == 20));
        }

        @Test
        @DisplayName("should throw when character not found")
        void assignFeat_shouldThrowWhenCharacterNotFound() {
            when(characterRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class,
                    () -> service.assignFeatToCharacter(99L, "Tough"));
        }

        @Test
        @DisplayName("should throw when feat not found")
        void assignFeat_shouldThrowWhenFeatNotFound() {
            when(characterRepository.findById(1L)).thenReturn(Optional.of(fighter));
            when(featRepository.findByName("Nonexistent")).thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class,
                    () -> service.assignFeatToCharacter(1L, "Nonexistent"));
        }
    }

    // ---- removeFeatFromCharacter ----

    @Nested
    @DisplayName("removeFeatFromCharacter Tests")
    class RemoveFeatFromCharacterTests {

        @Test
        @DisplayName("should remove a feat from a character")
        void removeFeat_shouldRemove() {
            when(characterRepository.findById(1L)).thenReturn(Optional.of(fighter));
            Dnd5eCharacterFeat charFeat = new Dnd5eCharacterFeat();
            charFeat.setFeatName("Tough");
            when(characterFeatRepository.findByCharacterIdAndFeatName(1L, "Tough")).thenReturn(List.of(charFeat));
            doNothing().when(characterFeatRepository).deleteByCharacterIdAndFeatName(1L, "Tough");

            assertDoesNotThrow(() -> service.removeFeatFromCharacter(1L, "Tough"));
            verify(characterFeatRepository).deleteByCharacterIdAndFeatName(1L, "Tough");
        }

        @Test
        @DisplayName("should throw when character does not have the feat")
        void removeFeat_shouldThrowWhenNotOwned() {
            when(characterRepository.findById(1L)).thenReturn(Optional.of(fighter));
            when(characterFeatRepository.findByCharacterIdAndFeatName(1L, "Tough")).thenReturn(List.of());

            assertThrows(IllegalArgumentException.class,
                    () -> service.removeFeatFromCharacter(1L, "Tough"));
        }
    }

    // ---- toggleFeatActive ----

    @Nested
    @DisplayName("toggleFeatActive Tests")
    class ToggleFeatActiveTests {

        @Test
        @DisplayName("should toggle feat active state")
        void toggleFeat_shouldToggle() {
            Dnd5eCharacterFeat charFeat = new Dnd5eCharacterFeat();
            charFeat.setId(1L);
            charFeat.setFeatName("Tough");
            charFeat.setActive(true);

            when(characterFeatRepository.findByCharacterIdAndFeatName(1L, "Tough")).thenReturn(List.of(charFeat));
            when(characterFeatRepository.save(any(Dnd5eCharacterFeat.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Dnd5eCharacterFeat result = service.toggleFeatActive(1L, "Tough", false);

            assertFalse(result.isActive());
        }

        @Test
        @DisplayName("should throw when character does not have the feat")
        void toggleFeat_shouldThrowWhenNotOwned() {
            when(characterFeatRepository.findByCharacterIdAndFeatName(1L, "Tough")).thenReturn(List.of());

            assertThrows(IllegalArgumentException.class,
                    () -> service.toggleFeatActive(1L, "Tough", false));
        }
    }

    // ---- listCharacterFeats ----

    @Nested
    @DisplayName("listCharacterFeats Tests")
    class ListCharacterFeatsTests {

        @Test
        @DisplayName("should return all character feats")
        void listCharacterFeats_shouldReturnAll() {
            Dnd5eCharacterFeat feat1 = new Dnd5eCharacterFeat();
            feat1.setFeatName("Tough");
            Dnd5eCharacterFeat feat2 = new Dnd5eCharacterFeat();
            feat2.setFeatName("Lucky");
            when(characterFeatRepository.findByCharacterIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(feat1, feat2));

            List<Dnd5eCharacterFeat> result = service.listCharacterFeats(1L);

            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("should return empty list when no feats")
        void listCharacterFeats_shouldReturnEmpty() {
            when(characterFeatRepository.findByCharacterIdOrderByCreatedAtDesc(1L)).thenReturn(List.of());

            List<Dnd5eCharacterFeat> result = service.listCharacterFeats(1L);

            assertTrue(result.isEmpty());
        }
    }

    // ---- listActiveCharacterFeats ----

    @Nested
    @DisplayName("listActiveCharacterFeats Tests")
    class ListActiveCharacterFeatsTests {

        @Test
        @DisplayName("should return only active feats")
        void listActiveCharacterFeats_shouldReturnActive() {
            Dnd5eCharacterFeat feat = new Dnd5eCharacterFeat();
            feat.setFeatName("Tough");
            when(characterFeatRepository.findByCharacterIdAndActiveTrue(1L)).thenReturn(List.of(feat));

            List<Dnd5eCharacterFeat> result = service.listActiveCharacterFeats(1L);

            assertEquals(1, result.size());
        }
    }

    // ---- hasFeat ----

    @Nested
    @DisplayName("hasFeat Tests")
    class HasFeatTests {

        @Test
        @DisplayName("should return true when character has feat")
        void hasFeat_shouldReturnTrue() {
            when(characterFeatRepository.existsByCharacterIdAndFeatName(1L, "Tough")).thenReturn(true);

            assertTrue(service.hasFeat(1L, "Tough"));
        }

        @Test
        @DisplayName("should return false when character does not have feat")
        void hasFeat_shouldReturnFalse() {
            when(characterFeatRepository.existsByCharacterIdAndFeatName(1L, "Tough")).thenReturn(false);

            assertFalse(service.hasFeat(1L, "Tough"));
        }
    }

    // ---- populateFeats ----

    @Nested
    @DisplayName("populateFeats Tests")
    class PopulateFeatsTests {

        @Test
        @DisplayName("should populate feat database")
        void populateFeats_shouldPopulate() {
            when(featRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

            service.populateFeats();

            verify(featRepository).saveAll(argThat(feats -> {
                List<Dnd5eFeat> list = new java.util.ArrayList<>();
                feats.forEach(list::add);
                return list.size() >= 20;
            }));
        }
    }

    // ---- initializeFeatDatabase ----

    @Nested
    @DisplayName("initializeFeatDatabase Tests")
    class InitializeFeatDatabaseTests {

        @Test
        @DisplayName("should skip if already initialized")
        void initialize_shouldSkipIfInitialized() {
            when(featRepository.count()).thenReturn(25L);
            service.initializeFeatDatabase();
            // Should not throw; skips because count >= EXPECTED_FEAT_COUNT_MIN
            verify(featRepository, never()).saveAll(anyList());
        }

        @Test
        @DisplayName("should skip if enough feats exist")
        void initialize_shouldSkipIfEnoughFeats() {
            when(featRepository.count()).thenReturn(25L);

            service.initializeFeatDatabase();

            verify(featRepository, never()).saveAll(anyList());
        }

        @Test
        @DisplayName("should populate if not enough feats")
        void initialize_shouldPopulateIfMissing() {
            when(featRepository.count()).thenReturn(0L);
            when(featRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

            service.initializeFeatDatabase();

            verify(featRepository).saveAll(argThat(feats -> {
                List<Dnd5eFeat> list = new java.util.ArrayList<>();
                feats.forEach(list::add);
                return list.size() >= 20;
            }));
        }
    }

    // ---- Constants ----

    @Nested
    @DisplayName("Constants Tests")
    class ConstantsTests {

        @Test
        @DisplayName("EXPECTED_FEAT_COUNT_MIN should be 20")
        void expectedFeatCountMin_shouldBe20() throws Exception {
            java.lang.reflect.Field field = Dnd5eFeatService.class.getDeclaredField("EXPECTED_FEAT_COUNT_MIN");
            field.setAccessible(true);
            assertEquals(20, field.getInt(null));
        }
    }
}
