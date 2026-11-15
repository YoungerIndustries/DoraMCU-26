package com.yisy.modules.doramcu.earth2026.survival.base;

import com.yisy.modules.doramcu.earth2026.model.Earth2026Character;
import com.yisy.modules.doramcu.earth2026.repository.Earth2026CharacterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("BaseBuildingService Tests")
class BaseBuildingServiceTest {

    private BaseStructureRepository structureRepository;
    private Earth2026CharacterRepository characterRepository;
    private BaseBuildingService baseBuildingService;

    @BeforeEach
    void setUp() {
        structureRepository = mock(BaseStructureRepository.class);
        characterRepository = mock(Earth2026CharacterRepository.class);
        baseBuildingService = new BaseBuildingService(structureRepository, characterRepository);
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

    // ---- getStructures ----

    @Nested
    @DisplayName("getStructures Tests")
    class GetStructuresTests {

        @Test
        @DisplayName("should return all structures for a character")
        void getStructures_shouldReturnAll() {
            Earth2026Character character = createCharacter(1L);
            when(characterRepository.findById(1L)).thenReturn(java.util.Optional.of(character));

            BaseStructure shelter = new BaseStructure(1L, "SHELTER", "Wooden Cabin");
            shelter.setId(1L);
            when(structureRepository.findByCharacterId(1L)).thenReturn(List.of(shelter));

            List<BaseStructure> result = baseBuildingService.getStructures(1L);

            assertEquals(1, result.size());
            assertEquals("SHELTER", result.get(0).getStructureType());
        }

        @Test
        @DisplayName("should throw when character not found")
        void getStructures_shouldThrowWhenNotFound() {
            when(characterRepository.findById(999L)).thenReturn(java.util.Optional.empty());

            assertThrows(IllegalArgumentException.class, () -> baseBuildingService.getStructures(999L));
        }

        @Test
        @DisplayName("should return structures by type")
        void getStructuresByType_shouldReturnFiltered() {
            Earth2026Character character = createCharacter(1L);
            when(characterRepository.findById(1L)).thenReturn(java.util.Optional.of(character));

            BaseStructure shelter = new BaseStructure(1L, "SHELTER", "Wooden Cabin");
            shelter.setId(1L);
            when(structureRepository.findByCharacterIdAndStructureType(1L, "SHELTER"))
                    .thenReturn(List.of(shelter));

            List<BaseStructure> result = baseBuildingService.getStructuresByType(1L, "SHELTER");

            assertEquals(1, result.size());
            assertEquals("SHELTER", result.get(0).getStructureType());
        }
    }

    // ---- buildStructure ----

    @Nested
    @DisplayName("buildStructure Tests")
    class BuildStructureTests {

        @Test
        @DisplayName("should build a shelter")
        void buildStructure_shouldBuildShelter() {
            Earth2026Character character = createCharacter(1L);
            when(characterRepository.findById(1L)).thenReturn(java.util.Optional.of(character));
            when(structureRepository.findByCharacterIdAndStructureType(1L, "SHELTER"))
                    .thenReturn(new ArrayList<>());
            when(structureRepository.save(any(BaseStructure.class))).thenAnswer(inv -> {
                BaseStructure s = inv.getArgument(0);
                s.setId(1L);
                return s;
            });

            BaseStructure result = baseBuildingService.buildStructure(1L, "SHELTER");

            assertNotNull(result);
            assertEquals("SHELTER", result.getStructureType());
            assertEquals("Wooden Cabin", result.getName());
            assertEquals(1, result.getLevel());
            assertEquals(3, result.getMaxLevel());
            assertEquals(100, result.getHealth());
            assertEquals(100, result.getMaxHealth());
            assertEquals(15, result.getExposureReduction());
            verify(structureRepository).save(result);
        }

        @Test
        @DisplayName("should build a storage")
        void buildStructure_shouldBuildStorage() {
            Earth2026Character character = createCharacter(1L);
            when(characterRepository.findById(1L)).thenReturn(java.util.Optional.of(character));
            when(structureRepository.findByCharacterIdAndStructureType(1L, "STORAGE"))
                    .thenReturn(new ArrayList<>());
            when(structureRepository.save(any(BaseStructure.class))).thenAnswer(inv -> {
                BaseStructure s = inv.getArgument(0);
                s.setId(1L);
                return s;
            });

            BaseStructure result = baseBuildingService.buildStructure(1L, "STORAGE");

            assertEquals("STORAGE", result.getStructureType());
            assertEquals("Storage Shed", result.getName());
            assertEquals(50, result.getStorageBonus());
        }

        @Test
        @DisplayName("should build a workshop")
        void buildStructure_shouldBuildWorkshop() {
            Earth2026Character character = createCharacter(1L);
            when(characterRepository.findById(1L)).thenReturn(java.util.Optional.of(character));
            when(structureRepository.findByCharacterIdAndStructureType(1L, "WORKSHOP"))
                    .thenReturn(new ArrayList<>());
            when(structureRepository.save(any(BaseStructure.class))).thenAnswer(inv -> {
                BaseStructure s = inv.getArgument(0);
                s.setId(1L);
                return s;
            });

            BaseStructure result = baseBuildingService.buildStructure(1L, "WORKSHOP");

            assertEquals("WORKSHOP", result.getStructureType());
            assertTrue(result.isWorkshop());
        }

        @Test
        @DisplayName("should build a defense")
        void buildStructure_shouldBuildDefense() {
            Earth2026Character character = createCharacter(1L);
            when(characterRepository.findById(1L)).thenReturn(java.util.Optional.of(character));
            when(structureRepository.findByCharacterIdAndStructureType(1L, "DEFENSE"))
                    .thenReturn(new ArrayList<>());
            when(structureRepository.save(any(BaseStructure.class))).thenAnswer(inv -> {
                BaseStructure s = inv.getArgument(0);
                s.setId(1L);
                return s;
            });

            BaseStructure result = baseBuildingService.buildStructure(1L, "DEFENSE");

            assertEquals("DEFENSE", result.getStructureType());
            assertTrue(result.isDefense());
            assertEquals(20, result.getDefenseValue());
        }

        @Test
        @DisplayName("should build a garden")
        void buildStructure_shouldBuildGarden() {
            Earth2026Character character = createCharacter(1L);
            when(characterRepository.findById(1L)).thenReturn(java.util.Optional.of(character));
            when(structureRepository.findByCharacterIdAndStructureType(1L, "GARDEN"))
                    .thenReturn(new ArrayList<>());
            when(structureRepository.save(any(BaseStructure.class))).thenAnswer(inv -> {
                BaseStructure s = inv.getArgument(0);
                s.setId(1L);
                return s;
            });

            BaseStructure result = baseBuildingService.buildStructure(1L, "GARDEN");

            assertEquals("GARDEN", result.getStructureType());
            assertEquals(5, result.getHungerBonus());
        }

        @Test
        @DisplayName("should throw for unknown structure type")
        void buildStructure_shouldThrowForUnknownType() {
            Earth2026Character character = createCharacter(1L);
            when(characterRepository.findById(1L)).thenReturn(java.util.Optional.of(character));

            assertThrows(IllegalArgumentException.class, () -> baseBuildingService.buildStructure(1L, "UNKNOWN"));
        }

        @Test
        @DisplayName("should throw when max shelter count reached")
        void buildStructure_shouldThrowWhenMaxShelterReached() {
            Earth2026Character character = createCharacter(1L);
            when(characterRepository.findById(1L)).thenReturn(java.util.Optional.of(character));

            BaseStructure existing = new BaseStructure(1L, "SHELTER", "Wooden Cabin");
            when(structureRepository.findByCharacterIdAndStructureType(1L, "SHELTER"))
                    .thenReturn(List.of(existing));

            assertThrows(IllegalStateException.class, () -> baseBuildingService.buildStructure(1L, "SHELTER"));
        }

        @Test
        @DisplayName("should allow up to 2 storage structures")
        void buildStructure_shouldAllowTwoStorage() {
            Earth2026Character character = createCharacter(1L);
            when(characterRepository.findById(1L)).thenReturn(java.util.Optional.of(character));

            BaseStructure existing = new BaseStructure(1L, "STORAGE", "Storage Shed");
            when(structureRepository.findByCharacterIdAndStructureType(1L, "STORAGE"))
                    .thenReturn(List.of(existing));
            when(structureRepository.save(any(BaseStructure.class))).thenAnswer(inv -> {
                BaseStructure s = inv.getArgument(0);
                s.setId(2L);
                return s;
            });

            BaseStructure result = baseBuildingService.buildStructure(1L, "STORAGE");

            assertNotNull(result);
        }

        @Test
        @DisplayName("should throw when max storage count reached")
        void buildStructure_shouldThrowWhenMaxStorageReached() {
            Earth2026Character character = createCharacter(1L);
            when(characterRepository.findById(1L)).thenReturn(java.util.Optional.of(character));

            BaseStructure s1 = new BaseStructure(1L, "STORAGE", "Storage Shed");
            BaseStructure s2 = new BaseStructure(1L, "STORAGE", "Storage Shed 2");
            when(structureRepository.findByCharacterIdAndStructureType(1L, "STORAGE"))
                    .thenReturn(List.of(s1, s2));

            assertThrows(IllegalStateException.class, () -> baseBuildingService.buildStructure(1L, "STORAGE"));
        }
    }

    // ---- upgradeStructure ----

    @Nested
    @DisplayName("upgradeStructure Tests")
    class UpgradeStructureTests {

        @Test
        @DisplayName("should upgrade shelter level")
        void upgradeStructure_shouldUpgradeShelter() {
            BaseStructure shelter = new BaseStructure(1L, "SHELTER", "Wooden Cabin");
            shelter.setId(1L);
            shelter.setLevel(1);
            shelter.setMaxLevel(3);
            shelter.setHealth(100);
            shelter.setMaxHealth(100);

            when(structureRepository.findById(1L)).thenReturn(java.util.Optional.of(shelter));
            when(structureRepository.save(any(BaseStructure.class))).thenAnswer(inv -> inv.getArgument(0));

            BaseStructure result = baseBuildingService.upgradeStructure(1L);

            assertEquals(2, result.getLevel());
            assertEquals(30, result.getExposureReduction()); // 15 * 2
            assertEquals(150, result.getMaxHealth()); // 100 + 50 * (2-1)
            assertEquals(150, result.getHealth());
            assertNotNull(result.getUpgradedAt());
        }

        @Test
        @DisplayName("should upgrade defense value")
        void upgradeStructure_shouldUpgradeDefense() {
            BaseStructure defense = new BaseStructure(1L, "DEFENSE", "Wooden Wall");
            defense.setId(1L);
            defense.setLevel(1);
            defense.setMaxLevel(2);
            defense.setHealth(120);
            defense.setMaxHealth(120);
            defense.setDefenseValue(20);

            when(structureRepository.findById(1L)).thenReturn(java.util.Optional.of(defense));
            when(structureRepository.save(any(BaseStructure.class))).thenAnswer(inv -> inv.getArgument(0));

            BaseStructure result = baseBuildingService.upgradeStructure(1L);

            assertEquals(2, result.getLevel());
            assertEquals(40, result.getDefenseValue()); // 20 * 2
            assertEquals(180, result.getMaxHealth()); // 120 + 60 * (2-1)
        }

        @Test
        @DisplayName("should upgrade garden hunger bonus")
        void upgradeStructure_shouldUpgradeGarden() {
            BaseStructure garden = new BaseStructure(1L, "GARDEN", "Garden Plot");
            garden.setId(1L);
            garden.setLevel(1);
            garden.setMaxLevel(2);
            garden.setHealth(60);
            garden.setMaxHealth(60);
            garden.setHungerBonus(5);

            when(structureRepository.findById(1L)).thenReturn(java.util.Optional.of(garden));
            when(structureRepository.save(any(BaseStructure.class))).thenAnswer(inv -> inv.getArgument(0));

            BaseStructure result = baseBuildingService.upgradeStructure(1L);

            assertEquals(2, result.getLevel());
            assertEquals(10, result.getHungerBonus()); // 5 * 2
        }

        @Test
        @DisplayName("should throw when structure not found")
        void upgradeStructure_shouldThrowWhenNotFound() {
            when(structureRepository.findById(999L)).thenReturn(java.util.Optional.empty());

            assertThrows(IllegalArgumentException.class, () -> baseBuildingService.upgradeStructure(999L));
        }

        @Test
        @DisplayName("should throw when at max level")
        void upgradeStructure_shouldThrowWhenMaxLevel() {
            BaseStructure shelter = new BaseStructure(1L, "SHELTER", "Wooden Cabin");
            shelter.setId(1L);
            shelter.setMaxLevel(3);
            shelter.setLevel(3);

            when(structureRepository.findById(1L)).thenReturn(java.util.Optional.of(shelter));

            assertThrows(IllegalStateException.class, () -> baseBuildingService.upgradeStructure(1L));
        }
    }

    // ---- repairStructure ----

    @Nested
    @DisplayName("repairStructure Tests")
    class RepairStructureTests {

        @Test
        @DisplayName("should repair structure health")
        void repairStructure_shouldRepair() {
            BaseStructure shelter = new BaseStructure(1L, "SHELTER", "Wooden Cabin");
            shelter.setId(1L);
            shelter.setHealth(50);
            shelter.setMaxHealth(100);

            when(structureRepository.findById(1L)).thenReturn(java.util.Optional.of(shelter));
            when(structureRepository.save(any(BaseStructure.class))).thenAnswer(inv -> inv.getArgument(0));

            BaseStructure result = baseBuildingService.repairStructure(1L, 30);

            assertEquals(80, result.getHealth());
        }

        @Test
        @DisplayName("should throw when structure destroyed")
        void repairStructure_shouldThrowWhenDestroyed() {
            BaseStructure shelter = new BaseStructure(1L, "SHELTER", "Wooden Cabin");
            shelter.setId(1L);
            shelter.setHealth(0);

            when(structureRepository.findById(1L)).thenReturn(java.util.Optional.of(shelter));

            assertThrows(IllegalStateException.class, () -> baseBuildingService.repairStructure(1L, 30));
        }

        @Test
        @DisplayName("should throw when structure not found")
        void repairStructure_shouldThrowWhenNotFound() {
            when(structureRepository.findById(999L)).thenReturn(java.util.Optional.empty());

            assertThrows(IllegalArgumentException.class, () -> baseBuildingService.repairStructure(999L, 30));
        }
    }

    // ---- demolishStructure ----

    @Nested
    @DisplayName("demolishStructure Tests")
    class DemolishStructureTests {

        @Test
        @DisplayName("should demolish structure")
        void demolishStructure_shouldDemolish() {
            BaseStructure shelter = new BaseStructure(1L, "SHELTER", "Wooden Cabin");
            shelter.setId(1L);
            when(structureRepository.findById(1L)).thenReturn(java.util.Optional.of(shelter));

            baseBuildingService.demolishStructure(1L);

            verify(structureRepository).delete(shelter);
        }

        @Test
        @DisplayName("should throw when structure not found")
        void demolishStructure_shouldThrowWhenNotFound() {
            when(structureRepository.findById(999L)).thenReturn(java.util.Optional.empty());

            assertThrows(IllegalArgumentException.class, () -> baseBuildingService.demolishStructure(999L));
        }
    }

    // ---- BaseStructure model ----

    @Nested
    @DisplayName("BaseStructure Model Tests")
    class BaseStructureModelTests {

        @Test
        @DisplayName("should detect max level")
        void isMaxLevel_shouldBeTrue() {
            BaseStructure structure = new BaseStructure(1L, "SHELTER", "Wooden Cabin");
            structure.setMaxLevel(3);
            structure.setLevel(3);

            assertTrue(structure.isMaxLevel());
        }

        @Test
        @DisplayName("should detect not max level")
        void isMaxLevel_shouldBeFalse() {
            BaseStructure structure = new BaseStructure(1L, "SHELTER", "Wooden Cabin");
            structure.setMaxLevel(3);
            structure.setLevel(1);

            assertFalse(structure.isMaxLevel());
        }

        @Test
        @DisplayName("should detect destroyed structure")
        void isDestroyed_shouldBeTrue() {
            BaseStructure structure = new BaseStructure(1L, "SHELTER", "Wooden Cabin");
            structure.setHealth(0);

            assertTrue(structure.isDestroyed());
        }

        @Test
        @DisplayName("should detect non-destroyed structure")
        void isDestroyed_shouldBeFalse() {
            BaseStructure structure = new BaseStructure(1L, "SHELTER", "Wooden Cabin");
            structure.setHealth(50);

            assertFalse(structure.isDestroyed());
        }
    }
}
