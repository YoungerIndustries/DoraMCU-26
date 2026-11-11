package com.yisy.modules.doramcu.dnd5e.service;

import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCharacter;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5ePsion;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5ePsionicPower;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5eCharacterRepository;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5ePsionRepository;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5ePsionicPowerRepository;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5ePsionService.PsionicPowerResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Dnd5ePsionService Tests")
class Dnd5ePsionServiceTest {

    private Dnd5ePsionRepository psionRepository;
    private Dnd5ePsionicPowerRepository powerRepository;
    private Dnd5eCharacterRepository characterRepository;
    private Dnd5ePsionService service;

    private Dnd5eCharacter character;
    private Dnd5ePsionicPower power;

    @BeforeEach
    void setUp() {
        psionRepository = mock(Dnd5ePsionRepository.class);
        powerRepository = mock(Dnd5ePsionicPowerRepository.class);
        characterRepository = mock(Dnd5eCharacterRepository.class);
        service = new Dnd5ePsionService(psionRepository, powerRepository, characterRepository);

        character = new Dnd5eCharacter();
        character.setId(1L);
        character.setName("Mind Bender");
        character.setRace("Human");
        character.setCharacterClass("Psion");
        character.setLevel(3);
        character.setStrScore(10);
        character.setDexScore(12);
        character.setConScore(14);
        character.setIntScore(16);
        character.setWisScore(12);
        character.setChaScore(10);
        character.setProficiencyBonus(2);
        character.setHitPoints(18);
        character.setMaxHitPoints(18);

        power = new Dnd5ePsionicPower();
        power.setId(1L);
        power.setName("Psionic Blast");
        power.setPowerLevel(0);
        power.setPowerPointCost(0);
        power.setCastingTime("1 action");
        power.setDuration("Instantaneous");
        power.setRange("60 feet");
        power.setConcentration(false);
        power.setRitual(false);
        power.setSubclass("All");
        power.setSchool("Evocation");
        power.setDescription("A burst of psionic energy.");
    }

    private Dnd5ePsion createFreshPsion() {
        Dnd5ePsion p = new Dnd5ePsion();
        p.setId(1L);
        p.setCharacterId(1L);
        p.setCharacterName("Mind Bender");
        p.setSubclass("Psi Warper");
        p.setPsionLevel(3);
        p.setMaxPowerPoints(4);
        p.setPowerPoints(4);
        p.setSurgePoints(0);
        p.setMaxSurgePoints(0);
        p.setPrimaryAbility("INT");
        p.setKnownPowers(6);
        p.setSurgeActive(false);
        return p;
    }

    // ---- getOrCreatePsion ----

    @Nested
    @DisplayName("getOrCreatePsion Tests")
    class GetOrCreatePsionTests {

        @Test
        @DisplayName("should return existing psion record")
        void getOrCreate_shouldReturnExisting() {
            when(characterRepository.findById(1L)).thenReturn(Optional.of(character));
            when(psionRepository.findByCharacterId(1L)).thenReturn(Optional.of(createFreshPsion()));

            Dnd5ePsion result = service.getOrCreatePsion(1L, "Psi Warper");

            assertNotNull(result);
            assertEquals(1L, result.getCharacterId());
            assertEquals("Psi Warper", result.getSubclass());
            assertEquals(3, result.getPsionLevel());
        }

        @Test
        @DisplayName("should create new psion record if not exists")
        void getOrCreate_shouldCreateNew() {
            when(characterRepository.findById(1L)).thenReturn(Optional.of(character));
            when(psionRepository.findByCharacterId(1L)).thenReturn(Optional.empty());
            when(psionRepository.save(any(Dnd5ePsion.class))).thenAnswer(inv -> inv.getArgument(0));

            Dnd5ePsion result = service.getOrCreatePsion(1L, "Telepath");

            assertNotNull(result);
            assertEquals("Telepath", result.getSubclass());
            assertEquals(1, result.getPsionLevel());
            assertEquals("INT", result.getPrimaryAbility());
            assertEquals(4, result.getKnownPowers());
            verify(psionRepository).save(any(Dnd5ePsion.class));
        }

        @Test
        @DisplayName("should throw for invalid subclass")
        void getOrCreate_shouldThrowForInvalidSubclass() {
            when(characterRepository.findById(1L)).thenReturn(Optional.of(character));

            assertThrows(IllegalArgumentException.class, () ->
                    service.getOrCreatePsion(1L, "Invalid Subclass"));
        }

        @Test
        @DisplayName("should throw for character not found")
        void getOrCreate_shouldThrowForCharacterNotFound() {
            when(characterRepository.findById(1L)).thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class, () ->
                    service.getOrCreatePsion(1L, "Psi Warper"));
        }

        @Test
        @DisplayName("should accept all valid subclasses")
        void getOrCreate_shouldAcceptAllValidSubclasses() {
            when(characterRepository.findById(1L)).thenReturn(Optional.of(character));
            when(psionRepository.findByCharacterId(1L)).thenReturn(Optional.empty());
            when(psionRepository.save(any(Dnd5ePsion.class))).thenAnswer(inv -> inv.getArgument(0));

            assertDoesNotThrow(() -> service.getOrCreatePsion(1L, "Psi Warper"));
            assertDoesNotThrow(() -> service.getOrCreatePsion(1L, "Metamorph"));
            assertDoesNotThrow(() -> service.getOrCreatePsion(1L, "Psykinetic"));
            assertDoesNotThrow(() -> service.getOrCreatePsion(1L, "Telepath"));
        }
    }

    // ---- getPsion ----

    @Nested
    @DisplayName("getPsion Tests")
    class GetPsionTests {

        @Test
        @DisplayName("should return psion by character ID")
        void getPsion_shouldReturnPsion() {
            when(psionRepository.findByCharacterId(1L)).thenReturn(Optional.of(createFreshPsion()));

            Dnd5ePsion result = service.getPsion(1L);

            assertNotNull(result);
            assertEquals("Mind Bender", result.getCharacterName());
        }

        @Test
        @DisplayName("should throw if psion not found")
        void getPsion_shouldThrowIfNotFound() {
            when(psionRepository.findByCharacterId(1L)).thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class, () -> service.getPsion(1L));
        }
    }

    // ---- listAllPsions ----

    @Nested
    @DisplayName("listAllPsions Tests")
    class ListAllPsionsTests {

        @Test
        @DisplayName("should return all psions")
        void listAll_shouldReturnAll() {
            when(psionRepository.findAll()).thenReturn(List.of(createFreshPsion()));

            List<Dnd5ePsion> result = service.listAllPsions();

            assertEquals(1, result.size());
            assertEquals("Mind Bender", result.get(0).getCharacterName());
        }

        @Test
        @DisplayName("should return empty list if no psions")
        void listAll_shouldReturnEmpty() {
            when(psionRepository.findAll()).thenReturn(List.of());

            List<Dnd5ePsion> result = service.listAllPsions();

            assertTrue(result.isEmpty());
        }
    }

    // ---- listPsionsBySubclass ----

    @Nested
    @DisplayName("listPsionsBySubclass Tests")
    class ListPsionsBySubclassTests {

        @Test
        @DisplayName("should return psions by subclass")
        void listBySubclass_shouldReturnPsions() {
            when(psionRepository.findBySubclass("Psi Warper")).thenReturn(List.of(createFreshPsion()));

            List<Dnd5ePsion> result = service.listPsionsBySubclass("Psi Warper");

            assertEquals(1, result.size());
            assertEquals("Psi Warper", result.get(0).getSubclass());
        }
    }

    // ---- updatePsionLevel ----

    @Nested
    @DisplayName("updatePsionLevel Tests")
    class UpdatePsionLevelTests {

        @Test
        @DisplayName("should update level and recalculate power points")
        void updateLevel_shouldRecalculate() {
            when(psionRepository.findByCharacterId(1L)).thenReturn(Optional.of(createFreshPsion()));
            when(psionRepository.save(any(Dnd5ePsion.class))).thenAnswer(inv -> inv.getArgument(0));

            Dnd5ePsion result = service.updatePsionLevel(1L, 10);

            assertEquals(10, result.getPsionLevel());
            assertEquals(11, result.getMaxPowerPoints());
            assertEquals(9, result.getKnownPowers());
        }

        @Test
        @DisplayName("should throw for level < 1")
        void updateLevel_shouldThrowForLowLevel() {
            when(psionRepository.findByCharacterId(1L)).thenReturn(Optional.of(createFreshPsion()));

            assertThrows(IllegalArgumentException.class, () -> service.updatePsionLevel(1L, 0));
        }

        @Test
        @DisplayName("should throw for level > 20")
        void updateLevel_shouldThrowForHighLevel() {
            when(psionRepository.findByCharacterId(1L)).thenReturn(Optional.of(createFreshPsion()));

            assertThrows(IllegalArgumentException.class, () -> service.updatePsionLevel(1L, 21));
        }

        @Test
        @DisplayName("should update surge points at level 4")
        void updateLevel_shouldUpdateSurgeAtLevel4() {
            when(psionRepository.findByCharacterId(1L)).thenReturn(Optional.of(createFreshPsion()));
            when(psionRepository.save(any(Dnd5ePsion.class))).thenAnswer(inv -> inv.getArgument(0));

            Dnd5ePsion result = service.updatePsionLevel(1L, 4);

            assertEquals(1, result.getMaxSurgePoints());
        }

        @Test
        @DisplayName("should update surge points at level 20")
        void updateLevel_shouldUpdateSurgeAtLevel20() {
            when(psionRepository.findByCharacterId(1L)).thenReturn(Optional.of(createFreshPsion()));
            when(psionRepository.save(any(Dnd5ePsion.class))).thenAnswer(inv -> inv.getArgument(0));

            Dnd5ePsion result = service.updatePsionLevel(1L, 20);

            assertEquals(5, result.getMaxSurgePoints());
            assertEquals(21, result.getMaxPowerPoints());
        }
    }

    // ---- setPrimaryAbility ----

    @Nested
    @DisplayName("setPrimaryAbility Tests")
    class SetPrimaryAbilityTests {

        @Test
        @DisplayName("should set primary ability to INT")
        void setAbility_shouldSetToInt() {
            when(psionRepository.findByCharacterId(1L)).thenReturn(Optional.of(createFreshPsion()));
            when(psionRepository.save(any(Dnd5ePsion.class))).thenAnswer(inv -> inv.getArgument(0));

            Dnd5ePsion result = service.setPrimaryAbility(1L, "INT");

            assertEquals("INT", result.getPrimaryAbility());
        }

        @Test
        @DisplayName("should set primary ability to WIS")
        void setAbility_shouldSetToWis() {
            when(psionRepository.findByCharacterId(1L)).thenReturn(Optional.of(createFreshPsion()));
            when(psionRepository.save(any(Dnd5ePsion.class))).thenAnswer(inv -> inv.getArgument(0));

            Dnd5ePsion result = service.setPrimaryAbility(1L, "wis");

            assertEquals("WIS", result.getPrimaryAbility());
        }

        @Test
        @DisplayName("should throw for invalid ability")
        void setAbility_shouldThrowForInvalid() {
            when(psionRepository.findByCharacterId(1L)).thenReturn(Optional.of(createFreshPsion()));

            assertThrows(IllegalArgumentException.class, () -> service.setPrimaryAbility(1L, "STR"));
        }
    }

    // ---- spendPowerPoints ----

    @Nested
    @DisplayName("spendPowerPoints Tests")
    class SpendPowerPointsTests {

        @Test
        @DisplayName("should spend power points successfully")
        void spend_shouldSucceed() {
            Dnd5ePsion freshPsion = createFreshPsion();
            when(psionRepository.findByCharacterId(1L)).thenReturn(Optional.of(freshPsion));
            when(psionRepository.save(any(Dnd5ePsion.class))).thenAnswer(inv -> inv.getArgument(0));

            PsionicPowerResult result = service.spendPowerPoints(1L, "Test Power", 2);

            assertTrue(result.isSuccess());
            assertEquals(2, result.getRemainingPoints());
            assertEquals(2, result.getCost());
        }

        @Test
        @DisplayName("should fail if not enough power points")
        void spend_shouldFailIfNotEnough() {
            when(psionRepository.findByCharacterId(1L)).thenReturn(Optional.of(createFreshPsion()));

            PsionicPowerResult result = service.spendPowerPoints(1L, "Test Power", 10);

            assertFalse(result.isSuccess());
            assertEquals(4, result.getRemainingPoints());
            assertTrue(result.getMessage().contains("Not enough"));
        }

        @Test
        @DisplayName("should not spend below 0")
        void spend_shouldNotGoBelowZero() {
            when(psionRepository.findByCharacterId(1L)).thenReturn(Optional.of(createFreshPsion()));
            when(psionRepository.save(any(Dnd5ePsion.class))).thenAnswer(inv -> inv.getArgument(0));

            PsionicPowerResult result = service.spendPowerPoints(1L, "Test Power", 4);

            assertTrue(result.isSuccess());
            assertEquals(0, result.getRemainingPoints());
        }
    }

    // ---- restorePowerPoints ----

    @Nested
    @DisplayName("restorePowerPoints Tests")
    class RestorePowerPointsTests {

        @Test
        @DisplayName("should restore all power points")
        void restore_shouldRestoreAll() {
            Dnd5ePsion freshPsion = createFreshPsion();
            freshPsion.setPowerPoints(1);
            when(psionRepository.findByCharacterId(1L)).thenReturn(Optional.of(freshPsion));
            when(psionRepository.save(any(Dnd5ePsion.class))).thenAnswer(inv -> inv.getArgument(0));

            Dnd5ePsion result = service.restorePowerPoints(1L);

            assertEquals(4, result.getPowerPoints());
        }
    }

    // ---- addPowerPoints ----

    @Nested
    @DisplayName("addPowerPoints Tests")
    class AddPowerPointsTests {

        @Test
        @DisplayName("should add power points")
        void add_shouldAddPoints() {
            when(psionRepository.findByCharacterId(1L)).thenReturn(Optional.of(createFreshPsion()));
            when(psionRepository.save(any(Dnd5ePsion.class))).thenAnswer(inv -> inv.getArgument(0));

            Dnd5ePsion result = service.addPowerPoints(1L, 2);

            // Clamped to maxPowerPoints (4)
            assertEquals(4, result.getPowerPoints());
        }

        @Test
        @DisplayName("should cap at max power points")
        void add_shouldCapAtMax() {
            when(psionRepository.findByCharacterId(1L)).thenReturn(Optional.of(createFreshPsion()));
            when(psionRepository.save(any(Dnd5ePsion.class))).thenAnswer(inv -> inv.getArgument(0));

            Dnd5ePsion result = service.addPowerPoints(1L, 10);

            assertEquals(4, result.getPowerPoints());
        }
    }

    // ---- activateSurge ----

    @Nested
    @DisplayName("activateSurge Tests")
    class ActivateSurgeTests {

        @Test
        @DisplayName("should activate surge and consume a point")
        void activate_shouldConsumePoint() {
            Dnd5ePsion freshPsion = createFreshPsion();
            freshPsion.setMaxSurgePoints(2);
            freshPsion.setSurgePoints(2);
            when(psionRepository.findByCharacterId(1L)).thenReturn(Optional.of(freshPsion));
            when(psionRepository.save(any(Dnd5ePsion.class))).thenAnswer(inv -> inv.getArgument(0));

            Dnd5ePsion result = service.activateSurge(1L);

            assertTrue(result.isSurgeActive());
            assertEquals(1, result.getSurgePoints());
        }

        @Test
        @DisplayName("should throw if no surge points available")
        void activate_shouldThrowIfNoPoints() {
            Dnd5ePsion freshPsion = createFreshPsion();
            freshPsion.setSurgePoints(0);
            when(psionRepository.findByCharacterId(1L)).thenReturn(Optional.of(freshPsion));

            assertThrows(IllegalStateException.class, () -> service.activateSurge(1L));
        }
    }

    // ---- deactivateSurge ----

    @Nested
    @DisplayName("deactivateSurge Tests")
    class DeactivateSurgeTests {

        @Test
        @DisplayName("should deactivate surge")
        void deactivate_shouldDeactivate() {
            Dnd5ePsion freshPsion = createFreshPsion();
            freshPsion.setSurgeActive(true);
            when(psionRepository.findByCharacterId(1L)).thenReturn(Optional.of(freshPsion));
            when(psionRepository.save(any(Dnd5ePsion.class))).thenAnswer(inv -> inv.getArgument(0));

            Dnd5ePsion result = service.deactivateSurge(1L);

            assertFalse(result.isSurgeActive());
        }
    }

    // ---- restoreSurgePoints ----

    @Nested
    @DisplayName("restoreSurgePoints Tests")
    class RestoreSurgePointsTests {

        @Test
        @DisplayName("should restore all surge points")
        void restore_shouldRestoreAll() {
            Dnd5ePsion freshPsion = createFreshPsion();
            freshPsion.setMaxSurgePoints(2);
            freshPsion.setSurgePoints(0);
            when(psionRepository.findByCharacterId(1L)).thenReturn(Optional.of(freshPsion));
            when(psionRepository.save(any(Dnd5ePsion.class))).thenAnswer(inv -> inv.getArgument(0));

            Dnd5ePsion result = service.restoreSurgePoints(1L);

            assertEquals(2, result.getSurgePoints());
            assertFalse(result.isSurgeActive());
        }
    }

    // ---- listAllPowers ----

    @Nested
    @DisplayName("listAllPowers Tests")
    class ListAllPowersTests {

        @Test
        @DisplayName("should return all powers")
        void listAll_shouldReturnAll() {
            when(powerRepository.findAll()).thenReturn(List.of(power));

            List<Dnd5ePsionicPower> result = service.listAllPowers();

            assertEquals(1, result.size());
            assertEquals("Psionic Blast", result.get(0).getName());
        }

        @Test
        @DisplayName("should return empty list if no powers")
        void listAll_shouldReturnEmpty() {
            when(powerRepository.findAll()).thenReturn(List.of());

            List<Dnd5ePsionicPower> result = service.listAllPowers();

            assertTrue(result.isEmpty());
        }
    }

    // ---- getPowerByName ----

    @Nested
    @DisplayName("getPowerByName Tests")
    class GetPowerByNameTests {

        @Test
        @DisplayName("should return power by name")
        void getByName_shouldReturnPower() {
            when(powerRepository.findByName("Psionic Blast")).thenReturn(Optional.of(power));

            Dnd5ePsionicPower result = service.getPowerByName("Psionic Blast");

            assertNotNull(result);
            assertEquals("Psionic Blast", result.getName());
        }

        @Test
        @DisplayName("should throw if power not found")
        void getByName_shouldThrowIfNotFound() {
            when(powerRepository.findByName("Nonexistent")).thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class, () -> service.getPowerByName("Nonexistent"));
        }
    }

    // ---- listPowersByLevel ----

    @Nested
    @DisplayName("listPowersByLevel Tests")
    class ListPowersByLevelTests {

        @Test
        @DisplayName("should return powers by level")
        void listByLevel_shouldReturnPowers() {
            when(powerRepository.findByPowerLevel(0)).thenReturn(List.of(power));

            List<Dnd5ePsionicPower> result = service.listPowersByLevel(0);

            assertEquals(1, result.size());
        }
    }

    // ---- listPowersBySubclass ----

    @Nested
    @DisplayName("listPowersBySubclass Tests")
    class ListPowersBySubclassTests {

        @Test
        @DisplayName("should return powers by subclass")
        void listBySubclass_shouldReturnPowers() {
            when(powerRepository.findBySubclass("Psi Warper")).thenReturn(List.of(power));

            List<Dnd5ePsionicPower> result = service.listPowersBySubclass("Psi Warper");

            assertEquals(1, result.size());
        }
    }

    // ---- listCantrips ----

    @Nested
    @DisplayName("listCantrips Tests")
    class ListCantripsTests {

        @Test
        @DisplayName("should return cantrips (level 0)")
        void listCantrips_shouldReturnCantrips() {
            when(powerRepository.findByPowerLevelZero()).thenReturn(List.of(power));

            List<Dnd5ePsionicPower> result = service.listCantrips();

            assertEquals(1, result.size());
            assertEquals(0, result.get(0).getPowerLevel());
        }
    }

    // ---- listPowersBySchool ----

    @Nested
    @DisplayName("listPowersBySchool Tests")
    class ListPowersBySchoolTests {

        @Test
        @DisplayName("should return powers by school")
        void listBySchool_shouldReturnPowers() {
            when(powerRepository.findBySchool("Evocation")).thenReturn(List.of(power));

            List<Dnd5ePsionicPower> result = service.listPowersBySchool("Evocation");

            assertEquals(1, result.size());
        }
    }

    // ---- searchPowers ----

    @Nested
    @DisplayName("searchPowers Tests")
    class SearchPowersTests {

        @Test
        @DisplayName("should search powers by name")
        void search_shouldReturnMatches() {
            when(powerRepository.findByNameContainingIgnoreCase("psionic")).thenReturn(List.of(power));

            List<Dnd5ePsionicPower> result = service.searchPowers("psionic");

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("should return empty if no matches")
        void search_shouldReturnEmpty() {
            when(powerRepository.findByNameContainingIgnoreCase("nonexistent")).thenReturn(List.of());

            List<Dnd5ePsionicPower> result = service.searchPowers("nonexistent");

            assertTrue(result.isEmpty());
        }
    }

    // ---- createPower ----

    @Nested
    @DisplayName("createPower Tests")
    class CreatePowerTests {

        @Test
        @DisplayName("should create a new power")
        void create_shouldCreatePower() {
            when(powerRepository.existsByName("New Power")).thenReturn(false);
            when(powerRepository.save(any(Dnd5ePsionicPower.class))).thenAnswer(inv -> inv.getArgument(0));

            Dnd5ePsionicPower result = service.createPower(
                    "New Power", 1, 2, "1 action", "Instantaneous", "60 feet",
                    false, false, "All", "Evocation", "A new power.");

            assertNotNull(result);
            assertEquals("New Power", result.getName());
            assertEquals(1, result.getPowerLevel());
            assertEquals(2, result.getPowerPointCost());
        }

        @Test
        @DisplayName("should throw if power already exists")
        void create_shouldThrowIfExists() {
            when(powerRepository.existsByName("Existing Power")).thenReturn(true);

            assertThrows(IllegalStateException.class, () ->
                    service.createPower("Existing Power", 1, 2, "1 action",
                            "Instantaneous", "60 feet", false, false, "All", "Evocation", "Desc"));
        }
    }

    // ---- deletePower ----

    @Nested
    @DisplayName("deletePower Tests")
    class DeletePowerTests {

        @Test
        @DisplayName("should delete a power")
        void delete_shouldDeletePower() {
            when(powerRepository.findByName("Test Power")).thenReturn(Optional.of(power));
            doNothing().when(powerRepository).delete(power);

            assertDoesNotThrow(() -> service.deletePower("Test Power"));
            verify(powerRepository).delete(power);
        }
    }

    // ---- usePower ----

    @Nested
    @DisplayName("usePower Tests")
    class UsePowerTests {

        @Test
        @DisplayName("should use a cantrip without spending points")
        void useCantrip_shouldNotSpendPoints() {
            when(psionRepository.findByCharacterId(1L)).thenReturn(Optional.of(createFreshPsion()));
            when(powerRepository.findByName("Psionic Blast")).thenReturn(Optional.of(power));
            when(psionRepository.save(any(Dnd5ePsion.class))).thenAnswer(inv -> inv.getArgument(0));

            PsionicPowerResult result = service.usePower(1L, "Psionic Blast", false);

            assertTrue(result.isSuccess());
            assertEquals(0, result.getCost());
            assertEquals("Psionic Blast", result.getPowerName());
        }

        @Test
        @DisplayName("should use a level 1 power and spend points")
        void useLevel1Power_shouldSpendPoints() {
            Dnd5ePsionicPower level1Power = new Dnd5ePsionicPower();
            level1Power.setName("Dissonant Whispers");
            level1Power.setPowerLevel(1);
            level1Power.setPowerPointCost(2);
            level1Power.setSubclass("All");

            when(psionRepository.findByCharacterId(1L)).thenReturn(Optional.of(createFreshPsion()));
            when(powerRepository.findByName("Dissonant Whispers")).thenReturn(Optional.of(level1Power));
            when(psionRepository.save(any(Dnd5ePsion.class))).thenAnswer(inv -> inv.getArgument(0));

            PsionicPowerResult result = service.usePower(1L, "Dissonant Whispers", false);

            assertTrue(result.isSuccess());
            assertEquals(2, result.getCost());
        }

        @Test
        @DisplayName("should fail if not enough power points")
        void usePower_shouldFailIfNotEnoughPoints() {
            Dnd5ePsionicPower expensivePower = new Dnd5ePsionicPower();
            expensivePower.setName("Expensive Power");
            expensivePower.setPowerLevel(5);
            expensivePower.setPowerPointCost(10);
            expensivePower.setSubclass("All");

            when(psionRepository.findByCharacterId(1L)).thenReturn(Optional.of(createFreshPsion()));
            when(powerRepository.findByName("Expensive Power")).thenReturn(Optional.of(expensivePower));

            PsionicPowerResult result = service.usePower(1L, "Expensive Power", false);

            assertFalse(result.isSuccess());
            assertTrue(result.getMessage().contains("Not enough"));
        }

        @Test
        @DisplayName("should fail if power not available to subclass")
        void usePower_shouldFailIfSubclassMismatch() {
            Dnd5ePsionicPower subclassPower = new Dnd5ePsionicPower();
            subclassPower.setName("Telepath Power");
            subclassPower.setPowerLevel(1);
            subclassPower.setPowerPointCost(2);
            subclassPower.setSubclass("Telepath");

            when(psionRepository.findByCharacterId(1L)).thenReturn(Optional.of(createFreshPsion()));
            when(powerRepository.findByName("Telepath Power")).thenReturn(Optional.of(subclassPower));

            PsionicPowerResult result = service.usePower(1L, "Telepath Power", false);

            assertFalse(result.isSuccess());
            assertTrue(result.getMessage().contains("not available"));
        }

        @Test
        @DisplayName("should consume surge when active")
        void usePower_shouldConsumeSurge() {
            Dnd5ePsion freshPsion = createFreshPsion();
            freshPsion.setSurgeActive(true);
            when(psionRepository.findByCharacterId(1L)).thenReturn(Optional.of(freshPsion));
            when(powerRepository.findByName("Psionic Blast")).thenReturn(Optional.of(power));
            when(psionRepository.save(any(Dnd5ePsion.class))).thenAnswer(inv -> inv.getArgument(0));

            PsionicPowerResult result = service.usePower(1L, "Psionic Blast", true);

            assertTrue(result.isSuccess());
        }
    }

    // ---- longRest ----

    @Nested
    @DisplayName("longRest Tests")
    class LongRestTests {

        @Test
        @DisplayName("should restore all resources")
        void longRest_shouldRestoreAll() {
            Dnd5ePsion freshPsion = createFreshPsion();
            freshPsion.setPowerPoints(1);
            freshPsion.setMaxPowerPoints(4);
            freshPsion.setSurgePoints(0);
            freshPsion.setMaxSurgePoints(2);
            freshPsion.setSurgeActive(true);
            when(psionRepository.findByCharacterId(1L)).thenReturn(Optional.of(freshPsion));
            when(psionRepository.save(any(Dnd5ePsion.class))).thenAnswer(inv -> inv.getArgument(0));

            Dnd5ePsion result = service.longRest(1L);

            assertEquals(4, result.getPowerPoints());
            assertEquals(2, result.getSurgePoints());
            assertFalse(result.isSurgeActive());
        }
    }

    // ---- initializePsionDatabase ----

    @Nested
    @DisplayName("initializePsionDatabase Tests")
    class InitializePsionDatabaseTests {

        @Test
        @DisplayName("should skip if powers already populated")
        void initialize_shouldSkipIfPopulated() {
            when(powerRepository.count()).thenReturn(10L);

            service.initializePsionDatabase();

            verify(powerRepository, never()).save(any(Dnd5ePsionicPower.class));
        }

        @Test
        @DisplayName("should populate if no powers exist")
        void initialize_shouldPopulateIfEmpty() {
            when(powerRepository.count()).thenReturn(0L);
            when(powerRepository.existsByName(anyString())).thenReturn(false);
            when(powerRepository.save(any(Dnd5ePsionicPower.class))).thenAnswer(inv -> inv.getArgument(0));

            service.initializePsionDatabase();

            verify(powerRepository, atLeastOnce()).save(any(Dnd5ePsionicPower.class));
        }
    }
}
