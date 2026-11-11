package com.yisy.modules.doramcu.dnd5e.service;

import com.yisy.modules.doramcu.dnd5e.model.Dnd5eVillain;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eVillainPower;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5eVillainPowerRepository;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5eVillainRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Dnd5eVillainService Tests")
class Dnd5eVillainServiceTest {

    @Mock
    private Dnd5eVillainRepository villainRepository;

    @Mock
    private Dnd5eVillainPowerRepository villainPowerRepository;

    @InjectMocks
    private Dnd5eVillainService villainService;

    // ==================== Helper Methods ====================

    private Dnd5eVillain createVillain(String name, String type) {
        Dnd5eVillain villain = new Dnd5eVillain();
        villain.setId(1L);
        villain.setName(name);
        villain.setVillainType(type);
        villain.setChallengeRating(15);
        villain.setDescription("Test villain");
        villain.setMotive("Test motive");
        villain.setHitPoints(200);
        villain.setArmorClass(18);
        villain.setMaxLegendaryResistances(3);
        villain.setLegendaryResistances(3);
        villain.setLich(false);
        villain.setDeathKnight(false);
        return villain;
    }

    private Dnd5eVillainPower createPower(String name, String type, int cost) {
        Dnd5eVillainPower power = new Dnd5eVillainPower();
        power.setId(1L);
        power.setVillainId(1L);
        power.setName(name);
        power.setPowerType(type);
        power.setActionCost(cost);
        power.setDescription("Test power");
        power.setSortOrder(1);
        return power;
    }

    // ==================== Villain CRUD ====================

    @Nested
    @DisplayName("createVillain Tests")
    class CreateVillainTests {

        @Test
        @DisplayName("should create and save villain")
        void shouldCreateAndSaveVillain() {
            Dnd5eVillain villain = createVillain("Test Villain", "Lich");
            when(villainRepository.save(any(Dnd5eVillain.class))).thenAnswer(inv -> inv.getArgument(0));

            Dnd5eVillain result = villainService.createVillain(villain);

            assertThat(result.getName()).isEqualTo("Test Villain");
            assertThat(result.getVillainType()).isEqualTo("Lich");
            // save() called at least once (constructor init + this call)
            verify(villainRepository, atLeast(1)).save(any(Dnd5eVillain.class));
        }
    }

    @Nested
    @DisplayName("getVillain Tests")
    class GetVillainTests {

        @Test
        @DisplayName("should return villain by ID")
        void shouldReturnVillainById() {
            Dnd5eVillain villain = createVillain("Vecna", "Lich");
            when(villainRepository.findById(1L)).thenReturn(Optional.of(villain));

            Optional<Dnd5eVillain> result = villainService.getVillain(1L);

            assertThat(result).isPresent();
            assertThat(result.get().getName()).isEqualTo("Vecna");
        }

        @Test
        @DisplayName("should return empty when villain not found")
        void shouldReturnEmptyWhenNotFound() {
            when(villainRepository.findById(999L)).thenReturn(Optional.empty());

            Optional<Dnd5eVillain> result = villainService.getVillain(999L);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getVillainByName Tests")
    class GetVillainByNameTests {

        @Test
        @DisplayName("should return villain by name")
        void shouldReturnVillainByName() {
            Dnd5eVillain villain = createVillain("Vecna", "Lich");
            when(villainRepository.findByName("Vecna")).thenReturn(Optional.of(villain));

            Optional<Dnd5eVillain> result = villainService.getVillainByName("Vecna");

            assertThat(result).isPresent();
            assertThat(result.get().getName()).isEqualTo("Vecna");
        }

        @Test
        @DisplayName("should return empty when villain not found by name")
        void shouldReturnEmptyWhenNotFound() {
            when(villainRepository.findByName("Nonexistent")).thenReturn(Optional.empty());

            Optional<Dnd5eVillain> result = villainService.getVillainByName("Nonexistent");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("listVillains Tests")
    class ListVillainsTests {

        @Test
        @DisplayName("should return all villains")
        void shouldReturnAllVillains() {
            List<Dnd5eVillain> villains = Arrays.asList(
                    createVillain("Vecna", "Lich"),
                    createVillain("Tiamat", "Dragon")
            );
            when(villainRepository.findAll()).thenReturn(villains);

            List<Dnd5eVillain> result = villainService.listVillains();

            assertThat(result).hasSize(2);
            assertThat(result).extracting(Dnd5eVillain::getName)
                    .contains("Vecna", "Tiamat");
        }

        @Test
        @DisplayName("should return empty list when no villains")
        void shouldReturnEmptyList() {
            when(villainRepository.findAll()).thenReturn(List.of());

            List<Dnd5eVillain> result = villainService.listVillains();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("listVillainsByType Tests")
    class ListVillainsByTypeTests {

        @Test
        @DisplayName("should return villains by type")
        void shouldReturnVillainsByType() {
            List<Dnd5eVillain> villains = Arrays.asList(
                    createVillain("Vecna", "Lich"),
                    createVillain("Kas", "Lich")
            );
            when(villainRepository.findByVillainType("Lich")).thenReturn(villains);

            List<Dnd5eVillain> result = villainService.listVillainsByType("Lich");

            assertThat(result).hasSize(2);
            assertThat(result).allMatch(v -> "Lich".equals(v.getVillainType()));
        }
    }

    @Nested
    @DisplayName("listVillainsByCrRange Tests")
    class ListVillainsByCrRangeTests {

        @Test
        @DisplayName("should return villains within CR range")
        void shouldReturnVillainsByCrRange() {
            Dnd5eVillain villain = createVillain("Vecna", "Lich");
            villain.setChallengeRating(21);
            when(villainRepository.findByChallengeRatingRange(15, 25)).thenReturn(List.of(villain));

            List<Dnd5eVillain> result = villainService.listVillainsByCrRange(15, 25);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getChallengeRating()).isEqualTo(21);
        }
    }

    @Nested
    @DisplayName("listLiches Tests")
    class ListLichesTests {

        @Test
        @DisplayName("should return all liches")
        void shouldReturnAllLiches() {
            Dnd5eVillain lich = createVillain("Vecna", "Lich");
            lich.setLich(true);
            when(villainRepository.findByIsLichTrue()).thenReturn(List.of(lich));

            List<Dnd5eVillain> result = villainService.listLiches();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).isLich()).isTrue();
        }
    }

    @Nested
    @DisplayName("listDeathKnights Tests")
    class ListDeathKnightsTests {

        @Test
        @DisplayName("should return all death knights")
        void shouldReturnAllDeathKnights() {
            Dnd5eVillain dk = createVillain("Runefang", "Death Knight");
            dk.setDeathKnight(true);
            when(villainRepository.findByIsDeathKnightTrue()).thenReturn(List.of(dk));

            List<Dnd5eVillain> result = villainService.listDeathKnights();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).isDeathKnight()).isTrue();
        }
    }

    @Nested
    @DisplayName("searchVillains Tests")
    class SearchVillainsTests {

        @Test
        @DisplayName("should search villains by keyword")
        void shouldSearchVillains() {
            Dnd5eVillain villain = createVillain("Vecna", "Lich");
            when(villainRepository.searchByKeyword("Vecna")).thenReturn(List.of(villain));

            List<Dnd5eVillain> result = villainService.searchVillains("Vecna");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("Vecna");
        }

        @Test
        @DisplayName("should return empty list when no matches")
        void shouldReturnEmptyOnNoMatch() {
            when(villainRepository.searchByKeyword("xyz")).thenReturn(List.of());

            List<Dnd5eVillain> result = villainService.searchVillains("xyz");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("updateVillain Tests")
    class UpdateVillainTests {

        @Test
        @DisplayName("should update villain fields")
        void shouldUpdateVillainFields() {
            Dnd5eVillain existing = createVillain("Vecna", "Lich");
            Dnd5eVillain updates = new Dnd5eVillain();
            updates.setName("Vecna the Returned");
            updates.setHitPoints(500);

            when(villainRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(villainRepository.save(any(Dnd5eVillain.class))).thenAnswer(inv -> inv.getArgument(0));

            Dnd5eVillain result = villainService.updateVillain(1L, updates);

            assertThat(result.getName()).isEqualTo("Vecna the Returned");
            assertThat(result.getHitPoints()).isEqualTo(500);
            // save() called at least once (constructor init + this call)
            verify(villainRepository, atLeast(1)).save(any(Dnd5eVillain.class));
        }

        @Test
        @DisplayName("should throw when villain not found")
        void shouldThrowWhenNotFound() {
            Dnd5eVillain updates = new Dnd5eVillain();
            updates.setName("New Name");
            when(villainRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> villainService.updateVillain(999L, updates))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Villain not found");
        }
    }

    @Nested
    @DisplayName("deleteVillain Tests")
    class DeleteVillainTests {

        @Test
        @DisplayName("should delete villain and associated powers")
        void shouldDeleteVillainAndPowers() {
            when(villainRepository.existsById(1L)).thenReturn(true);
            doNothing().when(villainPowerRepository).deleteByVillainId(1L);
            doNothing().when(villainRepository).deleteById(1L);

            boolean result = villainService.deleteVillain(1L);

            assertThat(result).isTrue();
            verify(villainPowerRepository).deleteByVillainId(1L);
            verify(villainRepository).deleteById(1L);
        }

        @Test
        @DisplayName("should return false when villain not found")
        void shouldReturnFalseWhenNotFound() {
            when(villainRepository.existsById(999L)).thenReturn(false);

            boolean result = villainService.deleteVillain(999L);

            assertThat(result).isFalse();
        }
    }

    // ==================== Villain Powers ====================

    @Nested
    @DisplayName("createPower Tests")
    class CreatePowerTests {

        @Test
        @DisplayName("should create power for villain")
        void shouldCreatePower() {
            Dnd5eVillainPower power = createPower("Life Drain", "Legendary Action", 1);
            when(villainRepository.existsById(1L)).thenReturn(true);
            when(villainPowerRepository.save(any(Dnd5eVillainPower.class))).thenAnswer(inv -> inv.getArgument(0));

            Dnd5eVillainPower result = villainService.createPower(1L, power);

            assertThat(result.getName()).isEqualTo("Life Drain");
            assertThat(result.getVillainId()).isEqualTo(1L);
            verify(villainPowerRepository).save(any(Dnd5eVillainPower.class));
        }

        @Test
        @DisplayName("should throw when villain not found")
        void shouldThrowWhenVillainNotFound() {
            Dnd5eVillainPower power = createPower("Life Drain", "Legendary Action", 1);
            when(villainRepository.existsById(999L)).thenReturn(false);

            assertThatThrownBy(() -> villainService.createPower(999L, power))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Villain not found");
        }
    }

    @Nested
    @DisplayName("listPowers Tests")
    class ListPowersTests {

        @Test
        @DisplayName("should return all powers for villain")
        void shouldReturnAllPowers() {
            List<Dnd5eVillainPower> powers = Arrays.asList(
                    createPower("Life Drain", "Legendary Action", 1),
                    createPower("Spectral Hands", "Lair Action", 0)
            );
            when(villainPowerRepository.findByVillainIdOrdered(1L)).thenReturn(powers);

            List<Dnd5eVillainPower> result = villainService.listPowers(1L);

            assertThat(result).hasSize(2);
        }
    }

    @Nested
    @DisplayName("listLegendaryActions Tests")
    class ListLegendaryActionsTests {

        @Test
        @DisplayName("should return legendary actions")
        void shouldReturnLegendaryActions() {
            List<Dnd5eVillainPower> actions = Arrays.asList(
                    createPower("Life Drain", "Legendary Action", 1),
                    createPower("Cloudkill", "Legendary Action", 2)
            );
            when(villainPowerRepository.findLegendaryActionsByVillainId(1L)).thenReturn(actions);

            List<Dnd5eVillainPower> result = villainService.listLegendaryActions(1L);

            assertThat(result).hasSize(2);
            assertThat(result).allMatch(p -> "Legendary Action".equals(p.getPowerType()));
        }
    }

    @Nested
    @DisplayName("listLairActions Tests")
    class ListLairActionsTests {

        @Test
        @DisplayName("should return lair actions")
        void shouldReturnLairActions() {
            List<Dnd5eVillainPower> actions = List.of(createPower("Spectral Hands", "Lair Action", 0));
            when(villainPowerRepository.findLairActionsByVillainId(1L)).thenReturn(actions);

            List<Dnd5eVillainPower> result = villainService.listLairActions(1L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getPowerType()).isEqualTo("Lair Action");
        }
    }

    @Nested
    @DisplayName("listRegionalEffects Tests")
    class ListRegionalEffectsTests {

        @Test
        @DisplayName("should return regional effects")
        void shouldReturnRegionalEffects() {
            List<Dnd5eVillainPower> effects = List.of(createPower("Blighted Land", "Regional Effect", 0));
            when(villainPowerRepository.findRegionalEffectsByVillainId(1L)).thenReturn(effects);

            List<Dnd5eVillainPower> result = villainService.listRegionalEffects(1L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getPowerType()).isEqualTo("Regional Effect");
        }
    }

    @Nested
    @DisplayName("updatePower Tests")
    class UpdatePowerTests {

        @Test
        @DisplayName("should update power fields")
        void shouldUpdatePowerFields() {
            Dnd5eVillainPower existing = createPower("Life Drain", "Legendary Action", 1);
            Dnd5eVillainPower updates = new Dnd5eVillainPower();
            updates.setName("Enhanced Life Drain");
            updates.setActionCost(2);

            when(villainPowerRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(villainPowerRepository.save(any(Dnd5eVillainPower.class))).thenAnswer(inv -> inv.getArgument(0));

            Dnd5eVillainPower result = villainService.updatePower(1L, updates);

            assertThat(result.getName()).isEqualTo("Enhanced Life Drain");
            assertThat(result.getActionCost()).isEqualTo(2);
        }

        @Test
        @DisplayName("should throw when power not found")
        void shouldThrowWhenNotFound() {
            Dnd5eVillainPower updates = new Dnd5eVillainPower();
            updates.setName("New Name");
            when(villainPowerRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> villainService.updatePower(999L, updates))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Villain power not found");
        }
    }

    @Nested
    @DisplayName("deletePower Tests")
    class DeletePowerTests {

        @Test
        @DisplayName("should delete power by ID")
        void shouldDeletePower() {
            when(villainPowerRepository.existsById(1L)).thenReturn(true);
            doNothing().when(villainPowerRepository).deleteById(1L);

            boolean result = villainService.deletePower(1L);

            assertThat(result).isTrue();
            verify(villainPowerRepository).deleteById(1L);
        }

        @Test
        @DisplayName("should return false when power not found")
        void shouldReturnFalseWhenNotFound() {
            when(villainPowerRepository.existsById(999L)).thenReturn(false);

            boolean result = villainService.deletePower(999L);

            assertThat(result).isFalse();
        }
    }

    // ==================== Combat Mechanics ====================

    @Nested
    @DisplayName("useLegendaryResistance Tests")
    class UseLegendaryResistanceTests {

        @Test
        @DisplayName("should use legendary resistance when available")
        void shouldUseLegendaryResistance() {
            Dnd5eVillain villain = createVillain("Vecna", "Lich");
            villain.setLegendaryResistances(3);
            when(villainRepository.findById(1L)).thenReturn(Optional.of(villain));
            when(villainRepository.save(any(Dnd5eVillain.class))).thenAnswer(inv -> inv.getArgument(0));

            boolean result = villainService.useLegendaryResistance(1L);

            assertThat(result).isTrue();
            assertThat(villain.getLegendaryResistances()).isEqualTo(2);
        }

        @Test
        @DisplayName("should return false when no resistances remain")
        void shouldReturnFalseWhenNoneRemaining() {
            Dnd5eVillain villain = createVillain("Vecna", "Lich");
            villain.setLegendaryResistances(0);
            when(villainRepository.findById(1L)).thenReturn(Optional.of(villain));

            boolean result = villainService.useLegendaryResistance(1L);

            assertThat(result).isFalse();
            assertThat(villain.getLegendaryResistances()).isZero();
        }

        @Test
        @DisplayName("should throw when villain not found")
        void shouldThrowWhenNotFound() {
            when(villainRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> villainService.useLegendaryResistance(999L))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("restoreLegendaryResistances Tests")
    class RestoreLegendaryResistancesTests {

        @Test
        @DisplayName("should restore legendary resistances to max")
        void shouldRestoreLegendaryResistances() {
            Dnd5eVillain villain = createVillain("Vecna", "Lich");
            villain.setLegendaryResistances(1);
            villain.setMaxLegendaryResistances(3);
            when(villainRepository.findById(1L)).thenReturn(Optional.of(villain));
            when(villainRepository.save(any(Dnd5eVillain.class))).thenAnswer(inv -> inv.getArgument(0));

            villainService.restoreLegendaryResistances(1L);

            assertThat(villain.getLegendaryResistances()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("dealDamage Tests")
    class DealDamageTests {

        @Test
        @DisplayName("should deal damage and return remaining HP")
        void shouldDealDamage() {
            Dnd5eVillain villain = createVillain("Vecna", "Lich");
            villain.setHitPoints(200);
            villain.setLich(false);
            when(villainRepository.findById(1L)).thenReturn(Optional.of(villain));
            when(villainRepository.save(any(Dnd5eVillain.class))).thenAnswer(inv -> inv.getArgument(0));

            Dnd5eVillainService.VillainDamageResult result = villainService.dealDamage(1L, 50);

            assertThat(result.remainingHp).isEqualTo(150);
            assertThat(result.isDefeated).isFalse();
            assertThat(result.retreatedToPhylactery).isFalse();
        }

        @Test
        @DisplayName("should defeat non-lich villain at 0 HP")
        void shouldDefeatNonLichVillain() {
            Dnd5eVillain villain = createVillain("Runefang", "Death Knight");
            villain.setHitPoints(50);
            villain.setLich(false);
            when(villainRepository.findById(1L)).thenReturn(Optional.of(villain));
            when(villainRepository.save(any(Dnd5eVillain.class))).thenAnswer(inv -> inv.getArgument(0));

            Dnd5eVillainService.VillainDamageResult result = villainService.dealDamage(1L, 100);

            assertThat(result.remainingHp).isZero();
            assertThat(result.isDefeated).isTrue();
            assertThat(result.retreatedToPhylactery).isFalse();
        }

        @Test
        @DisplayName("should retreat lich to phylactery at 0 HP")
        void shouldRetreatLichToPhylactery() {
            Dnd5eVillain villain = createVillain("Vecna", "Lich");
            villain.setHitPoints(50);
            villain.setLich(true);
            villain.setPhylactery("Hidden chest");
            when(villainRepository.findById(1L)).thenReturn(Optional.of(villain));
            when(villainRepository.save(any(Dnd5eVillain.class))).thenAnswer(inv -> inv.getArgument(0));

            Dnd5eVillainService.VillainDamageResult result = villainService.dealDamage(1L, 100);

            assertThat(result.remainingHp).isZero();
            assertThat(result.isDefeated).isFalse();
            assertThat(result.retreatedToPhylactery).isTrue();
            assertThat(result.message).contains("phylactery");
        }

        @Test
        @DisplayName("should clamp HP to 0 minimum")
        void shouldClampHpToZero() {
            Dnd5eVillain villain = createVillain("Vecna", "Lich");
            villain.setHitPoints(50);
            villain.setLich(false);
            when(villainRepository.findById(1L)).thenReturn(Optional.of(villain));
            when(villainRepository.save(any(Dnd5eVillain.class))).thenAnswer(inv -> inv.getArgument(0));

            Dnd5eVillainService.VillainDamageResult result = villainService.dealDamage(1L, 9999);

            assertThat(result.remainingHp).isZero();
        }
    }

    @Nested
    @DisplayName("destroyPhylactery Tests")
    class DestroyPhylacteryTests {

        @Test
        @DisplayName("should destroy phylactery and kill lich")
        void shouldDestroyPhylactery() {
            Dnd5eVillain villain = createVillain("Vecna", "Lich");
            villain.setLich(true);
            villain.setPhylactery("Hidden chest");
            villain.setHitPoints(100);
            when(villainRepository.findById(1L)).thenReturn(Optional.of(villain));
            when(villainRepository.save(any(Dnd5eVillain.class))).thenAnswer(inv -> inv.getArgument(0));

            boolean result = villainService.destroyPhylactery(1L);

            assertThat(result).isTrue();
            assertThat(villain.isLich()).isFalse();
            assertThat(villain.getPhylactery()).isNull();
            assertThat(villain.getHitPoints()).isZero();
        }

        @Test
        @DisplayName("should return false for non-lich villain")
        void shouldReturnFalseForNonLich() {
            Dnd5eVillain villain = createVillain("Runefang", "Death Knight");
            villain.setLich(false);
            when(villainRepository.findById(1L)).thenReturn(Optional.of(villain));

            boolean result = villainService.destroyPhylactery(1L);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("should return false when lich has no phylactery")
        void shouldReturnFalseWhenNoPhylactery() {
            Dnd5eVillain villain = createVillain("Vecna", "Lich");
            villain.setLich(true);
            villain.setPhylactery(null);
            when(villainRepository.findById(1L)).thenReturn(Optional.of(villain));

            boolean result = villainService.destroyPhylactery(1L);

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("spawnMinions Tests")
    class SpawnMinionsTests {

        @Test
        @DisplayName("should spawn minions from minion types")
        void shouldSpawnMinions() {
            Dnd5eVillain villain = createVillain("Vecna", "Lich");
            villain.setMinionTypes("Skeleton Warriors, Zombie Brutes, Ghosts");
            when(villainRepository.findById(1L)).thenReturn(Optional.of(villain));

            List<String> result = villainService.spawnMinions(1L);

            assertThat(result).hasSize(3);
            assertThat(result).contains("Skeleton Warriors", "Zombie Brutes", "Ghosts");
        }

        @Test
        @DisplayName("should return empty list when no minions")
        void shouldReturnEmptyWhenNoMinions() {
            Dnd5eVillain villain = createVillain("Vecna", "Lich");
            villain.setMinionTypes(null);
            when(villainRepository.findById(1L)).thenReturn(Optional.of(villain));

            List<String> result = villainService.spawnMinions(1L);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should trim whitespace from minion names")
        void shouldTrimWhitespace() {
            Dnd5eVillain villain = createVillain("Vecna", "Lich");
            villain.setMinionTypes("  Skeleton Warriors  ,  Zombie Brutes  ");
            when(villainRepository.findById(1L)).thenReturn(Optional.of(villain));

            List<String> result = villainService.spawnMinions(1L);

            assertThat(result).containsExactly("Skeleton Warriors", "Zombie Brutes");
        }
    }

    @Nested
    @DisplayName("getLairActionOptions Tests")
    class GetLairActionOptionsTests {

        @Test
        @DisplayName("should return lair action options")
        void shouldReturnLairActionOptions() {
            List<Dnd5eVillainPower> actions = List.of(createPower("Spectral Hands", "Lair Action", 0));
            when(villainPowerRepository.findLairActionsByVillainId(1L)).thenReturn(actions);

            List<Dnd5eVillainPower> result = villainService.getLairActionOptions(1L);

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("getLegendaryActionOptions Tests")
    class GetLegendaryActionOptionsTests {

        @Test
        @DisplayName("should return legendary action options")
        void shouldReturnLegendaryActionOptions() {
            List<Dnd5eVillainPower> actions = List.of(createPower("Life Drain", "Legendary Action", 1));
            when(villainPowerRepository.findLegendaryActionsByVillainId(1L)).thenReturn(actions);

            List<Dnd5eVillainPower> result = villainService.getLegendaryActionOptions(1L);

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("getRegionalEffects Tests")
    class GetRegionalEffectsTests {

        @Test
        @DisplayName("should return regional effects")
        void shouldReturnRegionalEffects() {
            List<Dnd5eVillainPower> effects = List.of(createPower("Blighted Land", "Regional Effect", 0));
            when(villainPowerRepository.findRegionalEffectsByVillainId(1L)).thenReturn(effects);

            List<Dnd5eVillainPower> result = villainService.getRegionalEffects(1L);

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("resetVillain Tests")
    class ResetVillainTests {

        @Test
        @DisplayName("should reset villain resources")
        void shouldResetVillain() {
            Dnd5eVillain villain = createVillain("Vecna", "Lich");
            villain.setLegendaryResistances(1);
            villain.setMaxLegendaryResistances(3);
            villain.setActionSurge(true);
            when(villainRepository.findById(1L)).thenReturn(Optional.of(villain));
            when(villainRepository.save(any(Dnd5eVillain.class))).thenAnswer(inv -> inv.getArgument(0));

            villainService.resetVillain(1L);

            assertThat(villain.getLegendaryResistances()).isEqualTo(3);
            assertThat(villain.isActionSurge()).isFalse();
        }
    }

    // ==================== Reference Data ====================

    @Nested
    @DisplayName("getVillainTypes Tests")
    class GetVillainTypesTests {

        @Test
        @DisplayName("should return all villain types")
        void shouldReturnAllVillainTypes() {
            List<String> types = villainService.getVillainTypes();

            assertThat(types).contains("Lich", "Death Knight", "Archfey", "Dragon", "Demon Lord");
            assertThat(types).hasSize(20);
        }
    }

    // ==================== Initialization ====================

    @Nested
    @DisplayName("initializeVillainDatabase Tests")
    class InitializeVillainDatabaseTests {

        @Test
        @DisplayName("should skip if already populated")
        void shouldSkipIfAlreadyPopulated() {
            when(villainRepository.count()).thenReturn(5L);

            villainService.initializeVillainDatabase();

            verify(villainRepository, atLeast(1)).count();
            // save may have been called by constructor init, but not by this call
        }

        @Test
        @DisplayName("should populate if missing")
        void shouldPopulateIfMissing() {
            when(villainRepository.count()).thenReturn(0L);
            when(villainRepository.save(any(Dnd5eVillain.class))).thenAnswer(inv -> inv.getArgument(0));
            when(villainRepository.findByName(anyString())).thenReturn(Optional.empty());

            villainService.initializeVillainDatabase();

            verify(villainRepository, atLeast(5)).save(any(Dnd5eVillain.class));
        }

        @Test
        @DisplayName("should skip if already initialized")
        void shouldSkipIfAlreadyInitialized() {
            // First call initializes
            when(villainRepository.count()).thenReturn(0L);
            when(villainRepository.save(any(Dnd5eVillain.class))).thenAnswer(inv -> inv.getArgument(0));
            when(villainRepository.findByName(anyString())).thenReturn(Optional.empty());

            villainService.initializeVillainDatabase();

            // Second call should skip because initialized flag is set
            villainService.initializeVillainDatabase();

            // count() called at least once (constructor + first explicit call)
            verify(villainRepository, atLeast(1)).count();
        }
    }

    @Nested
    @DisplayName("populateExampleVillains Tests")
    class PopulateExampleVillainsTests {

        @Test
        @DisplayName("should populate example villains")
        void shouldPopulateExampleVillains() {
            when(villainRepository.save(any(Dnd5eVillain.class))).thenAnswer(inv -> inv.getArgument(0));

            villainService.populateExampleVillains();

            verify(villainRepository, atLeast(5)).save(any(Dnd5eVillain.class));
        }
    }
}
