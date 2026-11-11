package com.yisy.modules.doramcu.dnd5e.service;

import com.yisy.modules.doramcu.dnd5e.model.Dnd5eBastion;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5eBastionRepository;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eBastionService.BastionDamageResult;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eBastionService.BastionFacilityResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Dnd5eBastionService Tests")
class Dnd5eBastionServiceTest {

    private Dnd5eBastionRepository bastionRepository;
    private Dnd5eBastionService bastionService;

    private Dnd5eBastion testBastion;

    @BeforeEach
    void setUp() {
        bastionRepository = mock(Dnd5eBastionRepository.class);
        // Skip initialization by pretending bastions exist
        when(bastionRepository.count()).thenReturn(1L);
        bastionService = new Dnd5eBastionService(bastionRepository);

        testBastion = new Dnd5eBastion();
        testBastion.setId(1L);
        testBastion.setName("Test Fortress");
        testBastion.setTheme("Fortress");
        testBastion.setLevel(1);
        testBastion.setDescription("A test fortress");
        testBastion.setLocation("Test Location");
        testBastion.setMaxHitPoints(200);
        testBastion.setHitPoints(200);
        testBastion.setArmorClass(15);
        testBastion.setGoldInvested(1000);
        testBastion.setPartySize(4);
        testBastion.setRoomCount(5);
        testBastion.setHasCrafting(false);
        testBastion.setHasTraining(false);
        testBastion.setHasLibrary(false);
        testBastion.setHasHealing(false);
        testBastion.setActive(true);
    }

    @Nested
    @DisplayName("createBastion Tests")
    class CreateBastionTests {

        @Test
        @DisplayName("should create a bastion")
        void createBastion_success() {
            when(bastionRepository.save(any(Dnd5eBastion.class))).thenReturn(testBastion);

            Dnd5eBastion created = bastionService.createBastion(testBastion);

            assertThat(created).isNotNull();
            assertThat(created.getName()).isEqualTo("Test Fortress");
            verify(bastionRepository).save(testBastion);
        }
    }

    @Nested
    @DisplayName("getBastion Tests")
    class GetBastionTests {

        @Test
        @DisplayName("should return bastion when found")
        void getBastion_found() {
            when(bastionRepository.findById(1L)).thenReturn(Optional.of(testBastion));

            Optional<Dnd5eBastion> result = bastionService.getBastion(1L);

            assertThat(result).isPresent();
            assertThat(result.get().getName()).isEqualTo("Test Fortress");
        }

        @Test
        @DisplayName("should return empty when not found")
        void getBastion_notFound() {
            when(bastionRepository.findById(99L)).thenReturn(Optional.empty());

            Optional<Dnd5eBastion> result = bastionService.getBastion(99L);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getBastionByName Tests")
    class GetBastionByNameTests {

        @Test
        @DisplayName("should return bastion by name")
        void getBastionByName_found() {
            when(bastionRepository.findByName("Test Fortress")).thenReturn(Optional.of(testBastion));

            Optional<Dnd5eBastion> result = bastionService.getBastionByName("Test Fortress");

            assertThat(result).isPresent();
            assertThat(result.get().getTheme()).isEqualTo("Fortress");
        }

        @Test
        @DisplayName("should return empty when not found")
        void getBastionByName_notFound() {
            when(bastionRepository.findByName("Nonexistent")).thenReturn(Optional.empty());

            Optional<Dnd5eBastion> result = bastionService.getBastionByName("Nonexistent");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("listBastions Tests")
    class ListBastionsTests {

        @Test
        @DisplayName("should return all bastions")
        void listBastions_success() {
            when(bastionRepository.findAll()).thenReturn(List.of(testBastion));

            List<Dnd5eBastion> result = bastionService.listBastions();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("Test Fortress");
        }

        @Test
        @DisplayName("should return empty list when no bastions")
        void listBastions_empty() {
            when(bastionRepository.findAll()).thenReturn(List.of());

            List<Dnd5eBastion> result = bastionService.listBastions();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("listActiveBastions Tests")
    class ListActiveBastionsTests {

        @Test
        @DisplayName("should return only active bastions")
        void listActiveBastions_success() {
            when(bastionRepository.findByActiveTrue()).thenReturn(List.of(testBastion));

            List<Dnd5eBastion> result = bastionService.listActiveBastions();

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("listBastionsByTheme Tests")
    class ListBastionsByThemeTests {

        @Test
        @DisplayName("should return bastions by theme")
        void listBastionsByTheme_success() {
            when(bastionRepository.findByTheme("Fortress")).thenReturn(List.of(testBastion));

            List<Dnd5eBastion> result = bastionService.listBastionsByTheme("Fortress");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTheme()).isEqualTo("Fortress");
        }
    }

    @Nested
    @DisplayName("listBastionsByLevelRange Tests")
    class ListBastionsByLevelRangeTests {

        @Test
        @DisplayName("should return bastions in level range")
        void listBastionsByLevelRange_success() {
            when(bastionRepository.findByLevelRange(1, 5)).thenReturn(List.of(testBastion));

            List<Dnd5eBastion> result = bastionService.listBastionsByLevelRange(1, 5);

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("searchBastions Tests")
    class SearchBastionsTests {

        @Test
        @DisplayName("should return matching bastions")
        void searchBastions_match() {
            when(bastionRepository.searchByKeyword("Fortress")).thenReturn(List.of(testBastion));

            List<Dnd5eBastion> result = bastionService.searchBastions("Fortress");

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("should return empty when no match")
        void searchBastions_noMatch() {
            when(bastionRepository.searchByKeyword("Nonexistent")).thenReturn(List.of());

            List<Dnd5eBastion> result = bastionService.searchBastions("Nonexistent");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("updateBastion Tests")
    class UpdateBastionTests {

        @Test
        @DisplayName("should update bastion fields")
        void updateBastion_success() {
            when(bastionRepository.findById(1L)).thenReturn(Optional.of(testBastion));
            when(bastionRepository.save(any(Dnd5eBastion.class))).thenReturn(testBastion);

            Dnd5eBastion updates = new Dnd5eBastion();
            updates.setName("Updated Name");
            updates.setTheme("Tower");

            Dnd5eBastion updated = bastionService.updateBastion(1L, updates);

            assertThat(updated.getName()).isEqualTo("Updated Name");
            assertThat(updated.getTheme()).isEqualTo("Tower");
            verify(bastionRepository).save(testBastion);
        }

        @Test
        @DisplayName("should throw when bastion not found")
        void updateBastion_notFound() {
            when(bastionRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bastionService.updateBastion(99L, new Dnd5eBastion()))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Bastion not found");
        }
    }

    @Nested
    @DisplayName("deleteBastion Tests")
    class DeleteBastionTests {

        @Test
        @DisplayName("should delete bastion when exists")
        void deleteBastion_success() {
            when(bastionRepository.existsById(1L)).thenReturn(true);

            boolean result = bastionService.deleteBastion(1L);

            assertThat(result).isTrue();
            verify(bastionRepository).deleteById(1L);
        }

        @Test
        @DisplayName("should return false when not found")
        void deleteBastion_notFound() {
            when(bastionRepository.existsById(99L)).thenReturn(false);

            boolean result = bastionService.deleteBastion(99L);

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("levelUpBastion Tests")
    class LevelUpBastionTests {

        @Test
        @DisplayName("should level up and return perks")
        void levelUpBastion_success() {
            when(bastionRepository.findById(1L)).thenReturn(Optional.of(testBastion));
            when(bastionRepository.save(any(Dnd5eBastion.class))).thenReturn(testBastion);

            List<String> perks = bastionService.levelUpBastion(1L);

            assertThat(perks).isNotEmpty();
            assertThat(testBastion.getLevel()).isEqualTo(2);
            assertThat(testBastion.getMaxHitPoints()).isEqualTo(220);
        }

        @Test
        @DisplayName("should return empty at max level")
        void levelUpBastion_maxLevel() {
            testBastion.setLevel(20);
            when(bastionRepository.findById(1L)).thenReturn(Optional.of(testBastion));

            List<String> perks = bastionService.levelUpBastion(1L);

            assertThat(perks).isEmpty();
            assertThat(testBastion.getLevel()).isEqualTo(20);
        }

        @Test
        @DisplayName("should throw when bastion not found")
        void levelUpBastion_notFound() {
            when(bastionRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bastionService.levelUpBastion(99L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Bastion not found");
        }

        @Test
        @DisplayName("should increase HP on level up")
        void levelUpBastion_hpIncrease() {
            when(bastionRepository.findById(1L)).thenReturn(Optional.of(testBastion));
            when(bastionRepository.save(any(Dnd5eBastion.class))).thenReturn(testBastion);

            bastionService.levelUpBastion(1L);

            assertThat(testBastion.getMaxHitPoints()).isEqualTo(220);
            assertThat(testBastion.getHitPoints()).isEqualTo(220);
        }

        @Test
        @DisplayName("should increase AC at level 5")
        void levelUpBastion_acAtFive() {
            testBastion.setLevel(4);
            when(bastionRepository.findById(1L)).thenReturn(Optional.of(testBastion));
            when(bastionRepository.save(any(Dnd5eBastion.class))).thenReturn(testBastion);

            List<String> perks = bastionService.levelUpBastion(1L);

            assertThat(testBastion.getArmorClass()).isEqualTo(16);
            assertThat(perks).contains("Reinforced walls (+1 AC)");
        }
    }

    @Nested
    @DisplayName("addFacility Tests")
    class AddFacilityTests {

        @Test
        @DisplayName("should add crafting facility")
        void addFacility_crafting() {
            when(bastionRepository.findById(1L)).thenReturn(Optional.of(testBastion));
            when(bastionRepository.save(any(Dnd5eBastion.class))).thenReturn(testBastion);

            BastionFacilityResult result = bastionService.addFacility(1L, "crafting");

            assertThat(result.success).isTrue();
            assertThat(testBastion.isHasCrafting()).isTrue();
            assertThat(testBastion.getGoldInvested()).isEqualTo(500);
        }

        @Test
        @DisplayName("should add training facility")
        void addFacility_training() {
            when(bastionRepository.findById(1L)).thenReturn(Optional.of(testBastion));
            when(bastionRepository.save(any(Dnd5eBastion.class))).thenReturn(testBastion);

            BastionFacilityResult result = bastionService.addFacility(1L, "training");

            assertThat(result.success).isTrue();
            assertThat(testBastion.isHasTraining()).isTrue();
        }

        @Test
        @DisplayName("should add library facility")
        void addFacility_library() {
            when(bastionRepository.findById(1L)).thenReturn(Optional.of(testBastion));
            when(bastionRepository.save(any(Dnd5eBastion.class))).thenReturn(testBastion);

            BastionFacilityResult result = bastionService.addFacility(1L, "library");

            assertThat(result.success).isTrue();
            assertThat(testBastion.isHasLibrary()).isTrue();
        }

        @Test
        @DisplayName("should add healing facility")
        void addFacility_healing() {
            when(bastionRepository.findById(1L)).thenReturn(Optional.of(testBastion));
            when(bastionRepository.save(any(Dnd5eBastion.class))).thenReturn(testBastion);

            BastionFacilityResult result = bastionService.addFacility(1L, "healing");

            assertThat(result.success).isTrue();
            assertThat(testBastion.isHasHealing()).isTrue();
        }

        @Test
        @DisplayName("should fail if already has facility")
        void addFacility_alreadyHas() {
            testBastion.setHasCrafting(true);
            when(bastionRepository.findById(1L)).thenReturn(Optional.of(testBastion));

            BastionFacilityResult result = bastionService.addFacility(1L, "crafting");

            assertThat(result.success).isFalse();
            assertThat(result.message).contains("already has");
        }

        @Test
        @DisplayName("should fail if insufficient gold")
        void addFacility_insufficientGold() {
            testBastion.setGoldInvested(100);
            when(bastionRepository.findById(1L)).thenReturn(Optional.of(testBastion));

            BastionFacilityResult result = bastionService.addFacility(1L, "crafting");

            assertThat(result.success).isFalse();
            assertThat(result.message).contains("Insufficient gold");
        }

        @Test
        @DisplayName("should fail for unknown facility type")
        void addFacility_unknownType() {
            when(bastionRepository.findById(1L)).thenReturn(Optional.of(testBastion));

            BastionFacilityResult result = bastionService.addFacility(1L, "unknown");

            assertThat(result.success).isFalse();
            assertThat(result.message).contains("Unknown facility type");
        }

        @Test
        @DisplayName("should throw when bastion not found")
        void addFacility_notFound() {
            when(bastionRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bastionService.addFacility(99L, "crafting"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Bastion not found");
        }
    }

    @Nested
    @DisplayName("investGold Tests")
    class InvestGoldTests {

        @Test
        @DisplayName("should invest gold into bastion")
        void investGold_success() {
            when(bastionRepository.findById(1L)).thenReturn(Optional.of(testBastion));
            when(bastionRepository.save(any(Dnd5eBastion.class))).thenReturn(testBastion);

            bastionService.investGold(1L, 500);

            assertThat(testBastion.getGoldInvested()).isEqualTo(1500);
            verify(bastionRepository).save(testBastion);
        }
    }

    @Nested
    @DisplayName("withdrawGold Tests")
    class WithdrawGoldTests {

        @Test
        @DisplayName("should withdraw gold when sufficient")
        void withdrawGold_success() {
            when(bastionRepository.findById(1L)).thenReturn(Optional.of(testBastion));
            when(bastionRepository.save(any(Dnd5eBastion.class))).thenReturn(testBastion);

            boolean result = bastionService.withdrawGold(1L, 300);

            assertThat(result).isTrue();
            assertThat(testBastion.getGoldInvested()).isEqualTo(700);
        }

        @Test
        @DisplayName("should fail when insufficient gold")
        void withdrawGold_insufficient() {
            when(bastionRepository.findById(1L)).thenReturn(Optional.of(testBastion));

            boolean result = bastionService.withdrawGold(1L, 2000);

            assertThat(result).isFalse();
            assertThat(testBastion.getGoldInvested()).isEqualTo(1000);
        }

        @Test
        @DisplayName("should return false when bastion not found")
        void withdrawGold_notFound() {
            when(bastionRepository.findById(99L)).thenReturn(Optional.empty());

            boolean result = bastionService.withdrawGold(99L, 100);

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("dealSiegeDamage Tests")
    class DealSiegeDamageTests {

        @Test
        @DisplayName("should deal damage and return remaining HP")
        void dealSiegeDamage_partial() {
            when(bastionRepository.findById(1L)).thenReturn(Optional.of(testBastion));
            when(bastionRepository.save(any(Dnd5eBastion.class))).thenReturn(testBastion);

            BastionDamageResult result = bastionService.dealSiegeDamage(1L, 50);

            assertThat(result.remainingHp).isEqualTo(150);
            assertThat(result.isDestroyed).isFalse();
        }

        @Test
        @DisplayName("should destroy bastion at 0 HP")
        void dealSiegeDamage_destroyed() {
            when(bastionRepository.findById(1L)).thenReturn(Optional.of(testBastion));
            when(bastionRepository.save(any(Dnd5eBastion.class))).thenReturn(testBastion);

            BastionDamageResult result = bastionService.dealSiegeDamage(1L, 200);

            assertThat(result.remainingHp).isEqualTo(0);
            assertThat(result.isDestroyed).isTrue();
            assertThat(testBastion.isActive()).isFalse();
        }

        @Test
        @DisplayName("should throw when bastion not found")
        void dealSiegeDamage_notFound() {
            when(bastionRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bastionService.dealSiegeDamage(99L, 50))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Bastion not found");
        }
    }

    @Nested
    @DisplayName("repairBastion Tests")
    class RepairBastionTests {

        @Test
        @DisplayName("should repair bastion HP")
        void repairBastion_success() {
            testBastion.setHitPoints(100);
            when(bastionRepository.findById(1L)).thenReturn(Optional.of(testBastion));
            when(bastionRepository.save(any(Dnd5eBastion.class))).thenReturn(testBastion);

            bastionService.repairBastion(1L, 50);

            assertThat(testBastion.getHitPoints()).isEqualTo(150);
        }

        @Test
        @DisplayName("should cap HP at max")
        void repairBastion_capped() {
            testBastion.setHitPoints(180);
            when(bastionRepository.findById(1L)).thenReturn(Optional.of(testBastion));
            when(bastionRepository.save(any(Dnd5eBastion.class))).thenReturn(testBastion);

            bastionService.repairBastion(1L, 50);

            assertThat(testBastion.getHitPoints()).isEqualTo(200);
        }

        @Test
        @DisplayName("should reactivate destroyed bastion")
        void repairBastion_reactivate() {
            testBastion.setHitPoints(0);
            testBastion.setActive(false);
            when(bastionRepository.findById(1L)).thenReturn(Optional.of(testBastion));
            when(bastionRepository.save(any(Dnd5eBastion.class))).thenReturn(testBastion);

            bastionService.repairBastion(1L, 50);

            assertThat(testBastion.isActive()).isTrue();
            assertThat(testBastion.getHitPoints()).isEqualTo(50);
        }
    }

    @Nested
    @DisplayName("getAvailablePerks Tests")
    class GetAvailablePerksTests {

        @Test
        @DisplayName("should return perks for level 1")
        void getAvailablePerks_level1() {
            when(bastionRepository.findById(1L)).thenReturn(Optional.of(testBastion));

            List<String> perks = bastionService.getAvailablePerks(1L);

            assertThat(perks).contains("Basic shelter and safe resting");
            assertThat(perks).contains("Party can long rest without interruption");
        }

        @Test
        @DisplayName("should return all perks for level 20")
        void getAvailablePerks_level20() {
            testBastion.setLevel(20);
            when(bastionRepository.findById(1L)).thenReturn(Optional.of(testBastion));

            List<String> perks = bastionService.getAvailablePerks(1L);

            assertThat(perks).contains("Legendary status — known across the realm");
            assertThat(perks).contains("Basic shelter and safe resting");
            assertThat(perks).hasSizeGreaterThanOrEqualTo(14);
        }

        @Test
        @DisplayName("should include facility perks")
        void getAvailablePerks_facilities() {
            testBastion.setHasCrafting(true);
            testBastion.setHasHealing(true);
            when(bastionRepository.findById(1L)).thenReturn(Optional.of(testBastion));

            List<String> perks = bastionService.getAvailablePerks(1L);

            assertThat(perks).contains("Crafting facility — reduced crafting time by 25%");
            assertThat(perks).contains("Healing sanctuary — +1d4 HP per long rest");
        }

        @Test
        @DisplayName("should throw when bastion not found")
        void getAvailablePerks_notFound() {
            when(bastionRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bastionService.getAvailablePerks(99L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Bastion not found");
        }
    }

    @Nested
    @DisplayName("getBastionThemes Tests")
    class GetBastionThemesTests {

        @Test
        @DisplayName("should return all bastion themes")
        void getBastionThemes_success() {
            List<String> themes = bastionService.getBastionThemes();

            assertThat(themes).contains("Fortress", "Tower", "Ship", "Camp", "Temple");
            assertThat(themes).hasSize(10);
        }
    }

    @Nested
    @DisplayName("initializeBastionDatabase Tests")
    class InitializeBastionDatabaseTests {

        @Test
        @DisplayName("should skip initialization if populated")
        void initialize_skipIfPopulated() {
            when(bastionRepository.count()).thenReturn(5L);

            // Service constructor calls initializeBastionDatabase
            // No save calls should be made
            verify(bastionRepository, never()).save(any(Dnd5eBastion.class));
        }

        @Test
        @DisplayName("should populate if empty")
        void initialize_populateIfEmpty() {
            Dnd5eBastionRepository repo = mock(Dnd5eBastionRepository.class);
            when(repo.count()).thenReturn(0L);
            when(repo.save(any(Dnd5eBastion.class))).thenAnswer(invocation -> {
                Dnd5eBastion b = invocation.getArgument(0);
                b.setId(1L);
                return b;
            });

            new Dnd5eBastionService(repo);

            verify(repo, atLeast(6)).save(any(Dnd5eBastion.class));
        }
    }

    @Nested
    @DisplayName("Facility query Tests")
    class FacilityQueryTests {

        @Test
        @DisplayName("should list bastions with crafting")
        void listBastionsWithCrafting() {
            when(bastionRepository.findByHasCrafting()).thenReturn(List.of(testBastion));

            List<Dnd5eBastion> result = bastionService.listBastionsWithCrafting();

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("should list bastions with training")
        void listBastionsWithTraining() {
            when(bastionRepository.findByHasTraining()).thenReturn(List.of(testBastion));

            List<Dnd5eBastion> result = bastionService.listBastionsWithTraining();

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("should list bastions with library")
        void listBastionsWithLibrary() {
            when(bastionRepository.findByHasLibrary()).thenReturn(List.of(testBastion));

            List<Dnd5eBastion> result = bastionService.listBastionsWithLibrary();

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("should list bastions with healing")
        void listBastionsWithHealing() {
            when(bastionRepository.findByHasHealing()).thenReturn(List.of(testBastion));

            List<Dnd5eBastion> result = bastionService.listBastionsWithHealing();

            assertThat(result).hasSize(1);
        }
    }
}
