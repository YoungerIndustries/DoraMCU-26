package com.yisy.modules.doramcu.earth2026.service;

import com.yisy.modules.doramcu.earth2026.model.Earth2026Companion;
import com.yisy.modules.doramcu.earth2026.repository.Earth2026CompanionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class Earth2026CompanionServiceTest {

    @Mock
    private Earth2026CompanionRepository repository;

    @InjectMocks
    private Earth2026CompanionService service;

    private Earth2026Companion createCompanion() {
        Earth2026Companion companion = new Earth2026Companion();
        companion.setId(1L);
        companion.setName("Buddy");
        companion.setSpeciesId("DOG");
        companion.setBreed("Golden Retriever");
        companion.setOwnerCharacterId(100L);
        companion.setAiCharacterId(200L);
        companion.setCompanionType(Earth2026Companion.CompanionType.PET);
        companion.setStr(12);
        companion.setDex(14);
        companion.setCon(10);
        companion.setIntel(8);
        companion.setWis(10);
        companion.setCha(12);
        companion.setHitPoints(20);
        companion.setMaxHitPoints(20);
        companion.setBondScore(50);
        companion.setTrainingLevel(Earth2026Companion.TrainingLevel.BASIC);
        companion.setTricks("[\"Sit\", \"Stay\"]");
        companion.setAbilities("[\"Scent Tracking\"]");
        companion.setEquipment("[\"Collar\"]");
        companion.setHealth(Earth2026Companion.HealthStatus.HEALTHY);
        companion.setLocation(Earth2026Companion.LocationType.FOLLOWS_OWNER);
        companion.setAcquiredDate(LocalDateTime.now());
        companion.setActive(true);
        return companion;
    }

    @Nested
    @DisplayName("CRUD Operations")
    class CrudOperations {

        @Test
        @DisplayName("findById returns companion when found")
        void findById_returnsCompanion_whenFound() {
            Earth2026Companion companion = createCompanion();
            when(repository.findById(1L)).thenReturn(Optional.of(companion));

            Optional<Earth2026Companion> result = service.findById(1L);

            assertTrue(result.isPresent());
            assertEquals(1L, result.get().getId());
            assertEquals("Buddy", result.get().getName());
            verify(repository).findById(1L);
        }

        @Test
        @DisplayName("findById returns empty when not found")
        void findById_returnsEmpty_whenNotFound() {
            when(repository.findById(999L)).thenReturn(Optional.empty());

            Optional<Earth2026Companion> result = service.findById(999L);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("findAll returns all companions")
        void findAll_returnsAllCompanions() {
            List<Earth2026Companion> companions = Arrays.asList(createCompanion(), createCompanion());
            companions.get(1).setId(2L);
            when(repository.findAll()).thenReturn(companions);

            List<Earth2026Companion> result = service.findAll();

            assertEquals(2, result.size());
            verify(repository).findAll();
        }

        @Test
        @DisplayName("save persists companion")
        void save_persistsCompanion() {
            Earth2026Companion companion = createCompanion();
            when(repository.save(companion)).thenReturn(companion);

            Earth2026Companion result = service.save(companion);

            assertEquals(companion, result);
            verify(repository).save(companion);
        }

        @Test
        @DisplayName("deleteById deletes companion")
        void deleteById_deletesCompanion() {
            service.deleteById(1L);
            verify(repository).deleteById(1L);
        }
    }

    @Nested
    @DisplayName("Query Operations")
    class QueryOperations {

        @Test
        @DisplayName("findByOwnerCharacterId returns companions for owner")
        void findByOwnerCharacterId_returnsCompanions() {
            Earth2026Companion companion = createCompanion();
            when(repository.findByOwnerCharacterId(100L)).thenReturn(List.of(companion));

            List<Earth2026Companion> result = service.findByOwnerCharacterId(100L);

            assertEquals(1, result.size());
            assertEquals(100L, result.get(0).getOwnerCharacterId());
        }

        @Test
        @DisplayName("findActiveByOwnerCharacterId returns active companions")
        void findActiveByOwnerCharacterId_returnsActiveCompanions() {
            Earth2026Companion companion = createCompanion();
            when(repository.findByOwnerCharacterIdAndActive(100L, true)).thenReturn(List.of(companion));

            List<Earth2026Companion> result = service.findActiveByOwnerCharacterId(100L);

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("findBySpeciesId returns companions of species")
        void findBySpeciesId_returnsCompanions() {
            Earth2026Companion companion = createCompanion();
            when(repository.findBySpeciesId("DOG")).thenReturn(List.of(companion));

            List<Earth2026Companion> result = service.findBySpeciesId("DOG");

            assertEquals(1, result.size());
            assertEquals("DOG", result.get(0).getSpeciesId());
        }

        @Test
        @DisplayName("findByCompanionType returns companions of type")
        void findByCompanionType_returnsCompanions() {
            Earth2026Companion companion = createCompanion();
            when(repository.findByCompanionType(Earth2026Companion.CompanionType.PET))
                    .thenReturn(List.of(companion));

            List<Earth2026Companion> result = service.findByCompanionType(Earth2026Companion.CompanionType.PET);

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("findByAiCharacterId returns companions with AI")
        void findByAiCharacterId_returnsCompanions() {
            Earth2026Companion companion = createCompanion();
            when(repository.findByAiCharacterId(200L)).thenReturn(List.of(companion));

            List<Earth2026Companion> result = service.findByAiCharacterId(200L);

            assertEquals(1, result.size());
            assertEquals(200L, result.get(0).getAiCharacterId());
        }

        @Test
        @DisplayName("findByHealth returns companions by health status")
        void findByHealth_returnsCompanions() {
            Earth2026Companion companion = createCompanion();
            when(repository.findByHealth(Earth2026Companion.HealthStatus.HEALTHY))
                    .thenReturn(List.of(companion));

            List<Earth2026Companion> result = service.findByHealth(Earth2026Companion.HealthStatus.HEALTHY);

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("findByOwnerAndType returns companions by owner and type")
        void findByOwnerAndType_returnsCompanions() {
            Earth2026Companion companion = createCompanion();
            when(repository.findByOwnerCharacterIdAndCompanionType(100L, Earth2026Companion.CompanionType.PET))
                    .thenReturn(List.of(companion));

            List<Earth2026Companion> result = service.findByOwnerAndType(100L, Earth2026Companion.CompanionType.PET);

            assertEquals(1, result.size());
        }
    }

    @Nested
    @DisplayName("Bond Management")
    class BondManagement {

        @Test
        @DisplayName("increaseBond increases bond score")
        void increaseBond_increasesBondScore() {
            Earth2026Companion companion = createCompanion();
            when(repository.findById(1L)).thenReturn(Optional.of(companion));
            when(repository.save(companion)).thenReturn(companion);

            Optional<Earth2026Companion> result = service.increaseBond(1L, 20);

            assertTrue(result.isPresent());
            assertEquals(70, result.get().getBondScore());
            verify(repository).save(companion);
        }

        @Test
        @DisplayName("increaseBond clamps to 100")
        void increaseBond_clampsTo100() {
            Earth2026Companion companion = createCompanion();
            companion.setBondScore(90);
            when(repository.findById(1L)).thenReturn(Optional.of(companion));
            when(repository.save(companion)).thenReturn(companion);

            Optional<Earth2026Companion> result = service.increaseBond(1L, 20);

            assertTrue(result.isPresent());
            assertEquals(100, result.get().getBondScore());
        }

        @Test
        @DisplayName("decreaseBond decreases bond score")
        void decreaseBond_decreasesBondScore() {
            Earth2026Companion companion = createCompanion();
            when(repository.findById(1L)).thenReturn(Optional.of(companion));
            when(repository.save(companion)).thenReturn(companion);

            Optional<Earth2026Companion> result = service.decreaseBond(1L, 20);

            assertTrue(result.isPresent());
            assertEquals(30, result.get().getBondScore());
        }

        @Test
        @DisplayName("decreaseBond clamps to 0")
        void decreaseBond_clampsTo0() {
            Earth2026Companion companion = createCompanion();
            companion.setBondScore(10);
            when(repository.findById(1L)).thenReturn(Optional.of(companion));
            when(repository.save(companion)).thenReturn(companion);

            Optional<Earth2026Companion> result = service.decreaseBond(1L, 20);

            assertTrue(result.isPresent());
            assertEquals(0, result.get().getBondScore());
        }

        @Test
        @DisplayName("increaseBond returns empty when not found")
        void increaseBond_returnsEmpty_whenNotFound() {
            when(repository.findById(999L)).thenReturn(Optional.empty());

            Optional<Earth2026Companion> result = service.increaseBond(999L, 10);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("decreaseBond returns empty when not found")
        void decreaseBond_returnsEmpty_whenNotFound() {
            when(repository.findById(999L)).thenReturn(Optional.empty());

            Optional<Earth2026Companion> result = service.decreaseBond(999L, 10);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Training Progression")
    class TrainingProgression {

        @Test
        @DisplayName("advanceTraining progresses from UNTRAINED to BASIC")
        void advanceTraining_UNTRAINED_to_BASIC() {
            Earth2026Companion companion = createCompanion();
            companion.setTrainingLevel(Earth2026Companion.TrainingLevel.UNTRAINED);
            when(repository.findById(1L)).thenReturn(Optional.of(companion));
            when(repository.save(companion)).thenReturn(companion);

            Optional<Earth2026Companion> result = service.advanceTraining(1L);

            assertTrue(result.isPresent());
            assertEquals(Earth2026Companion.TrainingLevel.BASIC, result.get().getTrainingLevel());
        }

        @Test
        @DisplayName("advanceTraining progresses from BASIC to ADVANCED")
        void advanceTraining_BASIC_to_ADVANCED() {
            Earth2026Companion companion = createCompanion();
            companion.setTrainingLevel(Earth2026Companion.TrainingLevel.BASIC);
            when(repository.findById(1L)).thenReturn(Optional.of(companion));
            when(repository.save(companion)).thenReturn(companion);

            Optional<Earth2026Companion> result = service.advanceTraining(1L);

            assertTrue(result.isPresent());
            assertEquals(Earth2026Companion.TrainingLevel.ADVANCED, result.get().getTrainingLevel());
        }

        @Test
        @DisplayName("advanceTraining progresses from ADVANCED to MASTER")
        void advanceTraining_ADVANCED_to_MASTER() {
            Earth2026Companion companion = createCompanion();
            companion.setTrainingLevel(Earth2026Companion.TrainingLevel.ADVANCED);
            when(repository.findById(1L)).thenReturn(Optional.of(companion));
            when(repository.save(companion)).thenReturn(companion);

            Optional<Earth2026Companion> result = service.advanceTraining(1L);

            assertTrue(result.isPresent());
            assertEquals(Earth2026Companion.TrainingLevel.MASTER, result.get().getTrainingLevel());
        }

        @Test
        @DisplayName("advanceTraining stays at MASTER")
        void advanceTraining_staysAt_MASTER() {
            Earth2026Companion companion = createCompanion();
            companion.setTrainingLevel(Earth2026Companion.TrainingLevel.MASTER);
            when(repository.findById(1L)).thenReturn(Optional.of(companion));
            when(repository.save(companion)).thenReturn(companion);

            Optional<Earth2026Companion> result = service.advanceTraining(1L);

            assertTrue(result.isPresent());
            assertEquals(Earth2026Companion.TrainingLevel.MASTER, result.get().getTrainingLevel());
        }

        @Test
        @DisplayName("advanceTraining returns empty when not found")
        void advanceTraining_returnsEmpty_whenNotFound() {
            when(repository.findById(999L)).thenReturn(Optional.empty());

            Optional<Earth2026Companion> result = service.advanceTraining(999L);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Health Management")
    class HealthManagement {

        @Test
        @DisplayName("heal increases hit points")
        void heal_increasesHitPoints() {
            Earth2026Companion companion = createCompanion();
            companion.setHitPoints(10);
            when(repository.findById(1L)).thenReturn(Optional.of(companion));
            when(repository.save(companion)).thenReturn(companion);

            Optional<Earth2026Companion> result = service.heal(1L, 10);

            assertTrue(result.isPresent());
            assertEquals(20, result.get().getHitPoints());
        }

        @Test
        @DisplayName("heal clamps to max hit points")
        void heal_clampsToMaxHitPoints() {
            Earth2026Companion companion = createCompanion();
            companion.setHitPoints(18);
            when(repository.findById(1L)).thenReturn(Optional.of(companion));
            when(repository.save(companion)).thenReturn(companion);

            Optional<Earth2026Companion> result = service.heal(1L, 10);

            assertTrue(result.isPresent());
            assertEquals(20, result.get().getHitPoints());
        }

        @Test
        @DisplayName("heal revives deceased companion")
        void heal_revivesDeceasedCompanion() {
            Earth2026Companion companion = createCompanion();
            companion.setHitPoints(0);
            companion.setHealth(Earth2026Companion.HealthStatus.DECEASED);
            when(repository.findById(1L)).thenReturn(Optional.of(companion));
            when(repository.save(companion)).thenReturn(companion);

            Optional<Earth2026Companion> result = service.heal(1L, 5);

            assertTrue(result.isPresent());
            assertEquals(5, result.get().getHitPoints());
            assertEquals(Earth2026Companion.HealthStatus.HEALTHY, result.get().getHealth());
        }

        @Test
        @DisplayName("damage decreases hit points")
        void damage_decreasesHitPoints() {
            Earth2026Companion companion = createCompanion();
            companion.setHitPoints(20);
            when(repository.findById(1L)).thenReturn(Optional.of(companion));
            when(repository.save(companion)).thenReturn(companion);

            Optional<Earth2026Companion> result = service.damage(1L, 10);

            assertTrue(result.isPresent());
            assertEquals(10, result.get().getHitPoints());
        }

        @Test
        @DisplayName("damage clamps to 0")
        void damage_clampsTo0() {
            Earth2026Companion companion = createCompanion();
            companion.setHitPoints(5);
            when(repository.findById(1L)).thenReturn(Optional.of(companion));
            when(repository.save(companion)).thenReturn(companion);

            Optional<Earth2026Companion> result = service.damage(1L, 10);

            assertTrue(result.isPresent());
            assertEquals(0, result.get().getHitPoints());
        }

        @Test
        @DisplayName("damage sets health to INJURED when below half")
        void damage_setsHealthToINJURED() {
            Earth2026Companion companion = createCompanion();
            companion.setHitPoints(15);
            companion.setMaxHitPoints(20);
            when(repository.findById(1L)).thenReturn(Optional.of(companion));
            when(repository.save(companion)).thenReturn(companion);

            Optional<Earth2026Companion> result = service.damage(1L, 10);

            assertTrue(result.isPresent());
            assertEquals(5, result.get().getHitPoints());
            assertEquals(Earth2026Companion.HealthStatus.INJURED, result.get().getHealth());
        }

        @Test
        @DisplayName("damage sets health to DECEASED when HP reaches 0")
        void damage_setsHealthToDECEASED() {
            Earth2026Companion companion = createCompanion();
            companion.setHitPoints(5);
            when(repository.findById(1L)).thenReturn(Optional.of(companion));
            when(repository.save(companion)).thenReturn(companion);

            Optional<Earth2026Companion> result = service.damage(1L, 5);

            assertTrue(result.isPresent());
            assertEquals(0, result.get().getHitPoints());
            assertEquals(Earth2026Companion.HealthStatus.DECEASED, result.get().getHealth());
        }

        @Test
        @DisplayName("heal returns empty when not found")
        void heal_returnsEmpty_whenNotFound() {
            when(repository.findById(999L)).thenReturn(Optional.empty());

            Optional<Earth2026Companion> result = service.heal(999L, 10);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("damage returns empty when not found")
        void damage_returnsEmpty_whenNotFound() {
            when(repository.findById(999L)).thenReturn(Optional.empty());

            Optional<Earth2026Companion> result = service.damage(999L, 10);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Entity Methods")
    class EntityMethods {

        @Test
        @DisplayName("Companion defaults are correct")
        void companionDefaults_areCorrect() {
            Earth2026Companion companion = new Earth2026Companion();
            assertEquals(10, companion.getStr());
            assertEquals(10, companion.getDex());
            assertEquals(10, companion.getCon());
            assertEquals(10, companion.getIntel());
            assertEquals(10, companion.getWis());
            assertEquals(10, companion.getCha());
            assertEquals(10, companion.getHitPoints());
            assertEquals(10, companion.getMaxHitPoints());
            assertEquals(0, companion.getBondScore());
            assertEquals(Earth2026Companion.TrainingLevel.UNTRAINED, companion.getTrainingLevel());
            assertEquals(Earth2026Companion.HealthStatus.HEALTHY, companion.getHealth());
            assertEquals(Earth2026Companion.LocationType.FOLLOWS_OWNER, companion.getLocation());
            assertTrue(companion.isActive());
        }

        @Test
        @DisplayName("Companion constructor sets fields")
        void companionConstructor_setsFields() {
            Earth2026Companion companion = new Earth2026Companion(
                    "Buddy", "DOG", 100L, Earth2026Companion.CompanionType.PET);

            assertEquals("Buddy", companion.getName());
            assertEquals("DOG", companion.getSpeciesId());
            assertEquals(100L, companion.getOwnerCharacterId());
            assertEquals(Earth2026Companion.CompanionType.PET, companion.getCompanionType());
        }

        @Test
        @DisplayName("setBondScore clamps to 0-100")
        void setBondScore_clampsToRange() {
            Earth2026Companion companion = new Earth2026Companion();
            companion.setBondScore(-10);
            assertEquals(0, companion.getBondScore());
            companion.setBondScore(150);
            assertEquals(100, companion.getBondScore());
        }

        @Test
        @DisplayName("setHitPoints clamps to 0-max")
        void setHitPoints_clampsToRange() {
            Earth2026Companion companion = new Earth2026Companion();
            companion.setMaxHitPoints(20);
            companion.setHitPoints(-5);
            assertEquals(0, companion.getHitPoints());
            companion.setHitPoints(30);
            assertEquals(20, companion.getHitPoints());
        }

        @Test
        @DisplayName("increaseBond clamps to 100")
        void increaseBond_clampsTo100() {
            Earth2026Companion companion = new Earth2026Companion();
            companion.setBondScore(90);
            companion.increaseBond(20);
            assertEquals(100, companion.getBondScore());
        }

        @Test
        @DisplayName("decreaseBond clamps to 0")
        void decreaseBond_clampsTo0() {
            Earth2026Companion companion = new Earth2026Companion();
            companion.setBondScore(10);
            companion.decreaseBond(20);
            assertEquals(0, companion.getBondScore());
        }

        @Test
        @DisplayName("heal increases HP up to max")
        void heal_increasesHP() {
            Earth2026Companion companion = new Earth2026Companion();
            companion.setHitPoints(5);
            companion.setMaxHitPoints(20);
            companion.heal(10);
            assertEquals(15, companion.getHitPoints());
        }

        @Test
        @DisplayName("takeDamage decreases HP to 0 minimum")
        void takeDamage_decreasesHP() {
            Earth2026Companion companion = new Earth2026Companion();
            companion.setHitPoints(5);
            companion.takeDamage(10);
            assertEquals(0, companion.getHitPoints());
            assertEquals(Earth2026Companion.HealthStatus.DECEASED, companion.getHealth());
        }

        @Test
        @DisplayName("toString returns meaningful string")
        void toString_returnsMeaningfulString() {
            Earth2026Companion companion = createCompanion();
            String str = companion.toString();
            assertTrue(str.contains("Earth2026Companion"));
            assertTrue(str.contains("Buddy"));
            assertTrue(str.contains("DOG"));
        }

        @Test
        @DisplayName("CompanionType enum has all expected values")
        void companionTypeEnum_hasAllValues() {
            Earth2026Companion.CompanionType[] types = Earth2026Companion.CompanionType.values();
            assertArrayEquals(
                    new Earth2026Companion.CompanionType[]{
                            Earth2026Companion.CompanionType.PET,
                            Earth2026Companion.CompanionType.SERVICE_ANIMAL,
                            Earth2026Companion.CompanionType.FAMILIAR,
                            Earth2026Companion.CompanionType.MOUNT,
                            Earth2026Companion.CompanionType.WORKING_ANIMAL
                    }, types);
        }

        @Test
        @DisplayName("TrainingLevel enum has all expected values")
        void trainingLevelEnum_hasAllValues() {
            Earth2026Companion.TrainingLevel[] levels = Earth2026Companion.TrainingLevel.values();
            assertArrayEquals(
                    new Earth2026Companion.TrainingLevel[]{
                            Earth2026Companion.TrainingLevel.UNTRAINED,
                            Earth2026Companion.TrainingLevel.BASIC,
                            Earth2026Companion.TrainingLevel.ADVANCED,
                            Earth2026Companion.TrainingLevel.MASTER
                    }, levels);
        }

        @Test
        @DisplayName("HealthStatus enum has all expected values")
        void healthStatusEnum_hasAllValues() {
            Earth2026Companion.HealthStatus[] statuses = Earth2026Companion.HealthStatus.values();
            assertArrayEquals(
                    new Earth2026Companion.HealthStatus[]{
                            Earth2026Companion.HealthStatus.HEALTHY,
                            Earth2026Companion.HealthStatus.INJURED,
                            Earth2026Companion.HealthStatus.SICK,
                            Earth2026Companion.HealthStatus.DECEASED
                    }, statuses);
        }
    }
}
