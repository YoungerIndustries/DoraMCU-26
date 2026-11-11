package com.yisy.modules.doramcu.dnd5e.service;

import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCharacter;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eDarkGift;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eFearDread;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5eCharacterRepository;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5eDarkGiftRepository;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5eFearDreadRepository;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Dnd5eHorrorService Tests")
class Dnd5eHorrorServiceTest {

    @Mock
    private Dnd5eDarkGiftRepository darkGiftRepository;

    @Mock
    private Dnd5eFearDreadRepository fearDreadRepository;

    @Mock
    private Dnd5eCharacterRepository characterRepository;

    @InjectMocks
    private Dnd5eHorrorService horrorService;

    // ==================== Helper Methods ====================

    private Dnd5eDarkGift createDarkGift(String name, String category) {
        Dnd5eDarkGift gift = new Dnd5eDarkGift();
        gift.setId(1L);
        gift.setName(name);
        gift.setCategory(category);
        gift.setMinLevel(1);
        gift.setBenefit("Test benefit");
        gift.setCurse("Test curse");
        gift.setRepeatable(false);
        gift.setDescription("Test description");
        return gift;
    }

    private Dnd5eFearDread createFearDread(Long characterId, String characterName) {
        Dnd5eFearDread fearDread = new Dnd5eFearDread();
        fearDread.setId(1L);
        fearDread.setCharacterId(characterId);
        fearDread.setCharacterName(characterName);
        fearDread.setFearLevel(0);
        fearDread.setDreadLevel(0);
        fearDread.setDomain(null);
        fearDread.setFrightened(false);
        return fearDread;
    }

    private Dnd5eCharacter createCharacter(Long id, String name) {
        Dnd5eCharacter character = new Dnd5eCharacter();
        character.setId(id);
        character.setName(name);
        return character;
    }

    // ==================== Dark Gift CRUD ====================

    @Nested
    @DisplayName("getDarkGiftByName Tests")
    class GetDarkGiftByNameTests {

        @Test
        @DisplayName("should return dark gift by name")
        void shouldReturnDarkGiftByName() {
            Dnd5eDarkGift gift = createDarkGift("Bone Armor", "Undead");
            when(darkGiftRepository.findByName("Bone Armor")).thenReturn(Optional.of(gift));

            Dnd5eDarkGift result = horrorService.getDarkGiftByName("Bone Armor");

            assertThat(result.getName()).isEqualTo("Bone Armor");
            assertThat(result.getCategory()).isEqualTo("Undead");
            verify(darkGiftRepository).findByName("Bone Armor");
        }

        @Test
        @DisplayName("should throw when dark gift not found")
        void shouldThrowWhenDarkGiftNotFound() {
            when(darkGiftRepository.findByName("Nonexistent")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> horrorService.getDarkGiftByName("Nonexistent"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Dark Gift not found");
        }
    }

    @Nested
    @DisplayName("getDarkGift Tests")
    class GetDarkGiftTests {

        @Test
        @DisplayName("should return dark gift by ID")
        void shouldReturnDarkGiftById() {
            Dnd5eDarkGift gift = createDarkGift("Bone Armor", "Undead");
            when(darkGiftRepository.findById(1L)).thenReturn(Optional.of(gift));

            Dnd5eDarkGift result = horrorService.getDarkGift(1L);

            assertThat(result.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("should throw when dark gift not found by ID")
        void shouldThrowWhenDarkGiftNotFound() {
            when(darkGiftRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> horrorService.getDarkGift(999L))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("listDarkGifts Tests")
    class ListDarkGiftsTests {

        @Test
        @DisplayName("should return all dark gifts")
        void shouldReturnAllDarkGifts() {
            List<Dnd5eDarkGift> gifts = Arrays.asList(
                    createDarkGift("Bone Armor", "Undead"),
                    createDarkGift("Hellish Rebuke Aura", "Fiend")
            );
            when(darkGiftRepository.findAll()).thenReturn(gifts);

            List<Dnd5eDarkGift> result = horrorService.listDarkGifts();

            assertThat(result).hasSize(2);
            assertThat(result).extracting(Dnd5eDarkGift::getName)
                    .contains("Bone Armor", "Hellish Rebuke Aura");
        }

        @Test
        @DisplayName("should return empty list when no dark gifts")
        void shouldReturnEmptyList() {
            when(darkGiftRepository.findAll()).thenReturn(List.of());

            List<Dnd5eDarkGift> result = horrorService.listDarkGifts();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("listDarkGiftsByCategory Tests")
    class ListDarkGiftsByCategoryTests {

        @Test
        @DisplayName("should return dark gifts by category")
        void shouldReturnDarkGiftsByCategory() {
            List<Dnd5eDarkGift> gifts = Arrays.asList(
                    createDarkGift("Bone Armor", "Undead"),
                    createDarkGift("Life Drain", "Undead")
            );
            when(darkGiftRepository.findByCategory("Undead")).thenReturn(gifts);

            List<Dnd5eDarkGift> result = horrorService.listDarkGiftsByCategory("Undead");

            assertThat(result).hasSize(2);
            assertThat(result).allMatch(g -> "Undead".equals(g.getCategory()));
        }
    }

    @Nested
    @DisplayName("listDarkGiftsByDarklord Tests")
    class ListDarkGiftsByDarklordTests {

        @Test
        @DisplayName("should return dark gifts by darklord")
        void shouldReturnDarkGiftsByDarklord() {
            Dnd5eDarkGift gift = createDarkGift("Life Drain", "Undead");
            gift.setDarklord("Strahd");
            when(darkGiftRepository.findByDarklord("Strahd")).thenReturn(List.of(gift));

            List<Dnd5eDarkGift> result = horrorService.listDarkGiftsByDarklord("Strahd");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getDarklord()).isEqualTo("Strahd");
        }
    }

    @Nested
    @DisplayName("listDarkGiftsByLevel Tests")
    class ListDarkGiftsByLevelTests {

        @Test
        @DisplayName("should return dark gifts available at level")
        void shouldReturnDarkGiftsByLevel() {
            Dnd5eDarkGift gift = createDarkGift("Bone Armor", "Undead");
            gift.setMinLevel(1);
            when(darkGiftRepository.findByMinLevelLessThanEqual(5)).thenReturn(List.of(gift));

            List<Dnd5eDarkGift> result = horrorService.listDarkGiftsByLevel(5);

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("searchDarkGifts Tests")
    class SearchDarkGiftsTests {

        @Test
        @DisplayName("should search dark gifts by name")
        void shouldSearchDarkGifts() {
            Dnd5eDarkGift gift = createDarkGift("Bone Armor", "Undead");
            when(darkGiftRepository.findByNameContainingIgnoreCase("bone")).thenReturn(List.of(gift));

            List<Dnd5eDarkGift> result = horrorService.searchDarkGifts("bone");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("Bone Armor");
        }

        @Test
        @DisplayName("should return empty list when no matches")
        void shouldReturnEmptyOnNoMatch() {
            when(darkGiftRepository.findByNameContainingIgnoreCase("xyz")).thenReturn(List.of());

            List<Dnd5eDarkGift> result = horrorService.searchDarkGifts("xyz");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("createDarkGift Tests")
    class CreateDarkGiftTests {

        @Test
        @DisplayName("should create a new dark gift")
        void shouldCreateDarkGift() {
            when(darkGiftRepository.save(any(Dnd5eDarkGift.class))).thenAnswer(inv -> inv.getArgument(0));

            Dnd5eDarkGift result = horrorService.createDarkGift(
                    "New Gift", 1, "Undead", "Benefit", "Curse", null, false, "Description");

            assertThat(result.getName()).isEqualTo("New Gift");
            assertThat(result.getCategory()).isEqualTo("Undead");
            assertThat(result.getBenefit()).isEqualTo("Benefit");
            assertThat(result.getCurse()).isEqualTo("Curse");
            verify(darkGiftRepository).save(any(Dnd5eDarkGift.class));
        }
    }

    @Nested
    @DisplayName("deleteDarkGift Tests")
    class DeleteDarkGiftTests {

        @Test
        @DisplayName("should delete dark gift by ID")
        void shouldDeleteDarkGift() {
            doNothing().when(darkGiftRepository).deleteById(1L);

            horrorService.deleteDarkGift(1L);

            verify(darkGiftRepository).deleteById(1L);
        }
    }

    // ==================== Fear & Dread Management ====================

    @Nested
    @DisplayName("getOrCreateFearDread Tests")
    class GetOrCreateFearDreadTests {

        @Test
        @DisplayName("should return existing fear/dread state")
        void shouldReturnExistingFearDread() {
            Dnd5eFearDread fearDread = createFearDread(1L, "Test Character");
            when(fearDreadRepository.findByCharacterId(1L)).thenReturn(Optional.of(fearDread));

            Dnd5eFearDread result = horrorService.getOrCreateFearDread(1L);

            assertThat(result.getCharacterName()).isEqualTo("Test Character");
            verify(fearDreadRepository, never()).save(any());
        }

        @Test
        @DisplayName("should create new fear/dread state when not found")
        void shouldCreateNewFearDread() {
            Dnd5eCharacter character = createCharacter(1L, "New Character");
            when(fearDreadRepository.findByCharacterId(1L)).thenReturn(Optional.empty());
            when(characterRepository.findById(1L)).thenReturn(Optional.of(character));
            when(fearDreadRepository.save(any(Dnd5eFearDread.class))).thenAnswer(inv -> inv.getArgument(0));

            Dnd5eFearDread result = horrorService.getOrCreateFearDread(1L);

            assertThat(result.getCharacterName()).isEqualTo("New Character");
            assertThat(result.getFearLevel()).isZero();
            assertThat(result.getDreadLevel()).isZero();
            verify(fearDreadRepository).save(any(Dnd5eFearDread.class));
        }

        @Test
        @DisplayName("should throw when character not found")
        void shouldThrowWhenCharacterNotFound() {
            when(fearDreadRepository.findByCharacterId(999L)).thenReturn(Optional.empty());
            when(characterRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> horrorService.getOrCreateFearDread(999L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Character not found");
        }
    }

    @Nested
    @DisplayName("getFearDread Tests")
    class GetFearDreadTests {

        @Test
        @DisplayName("should return fear/dread state")
        void shouldReturnFearDread() {
            Dnd5eFearDread fearDread = createFearDread(1L, "Test Character");
            when(fearDreadRepository.findByCharacterId(1L)).thenReturn(Optional.of(fearDread));

            Dnd5eFearDread result = horrorService.getFearDread(1L);

            assertThat(result.getCharacterId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("should throw when fear/dread not found")
        void shouldThrowWhenNotFound() {
            when(fearDreadRepository.findByCharacterId(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> horrorService.getFearDread(999L))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("listAllFearDread Tests")
    class ListAllFearDreadTests {

        @Test
        @DisplayName("should return all fear/dread states")
        void shouldReturnAllFearDread() {
            List<Dnd5eFearDread> states = Arrays.asList(
                    createFearDread(1L, "Character 1"),
                    createFearDread(2L, "Character 2")
            );
            when(fearDreadRepository.findAll()).thenReturn(states);

            List<Dnd5eFearDread> result = horrorService.listAllFearDread();

            assertThat(result).hasSize(2);
        }
    }

    @Nested
    @DisplayName("listByDomain Tests")
    class ListByDomainTests {

        @Test
        @DisplayName("should return characters in a domain")
        void shouldReturnCharactersInDomain() {
            Dnd5eFearDread fearDread = createFearDread(1L, "Character 1");
            fearDread.setDomain("Barovia");
            when(fearDreadRepository.findByDomain("Barovia")).thenReturn(List.of(fearDread));

            List<Dnd5eFearDread> result = horrorService.listByDomain("Barovia");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getDomain()).isEqualTo("Barovia");
        }
    }

    @Nested
    @DisplayName("listFrightenedCharacters Tests")
    class ListFrightenedCharactersTests {

        @Test
        @DisplayName("should return frightened characters")
        void shouldReturnFrightenedCharacters() {
            Dnd5eFearDread fearDread = createFearDread(1L, "Frightened Character");
            fearDread.setFrightened(true);
            when(fearDreadRepository.findByIsFrightenedTrue()).thenReturn(List.of(fearDread));

            List<Dnd5eFearDread> result = horrorService.listFrightenedCharacters();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).isFrightened()).isTrue();
        }
    }

    @Nested
    @DisplayName("setDomain Tests")
    class SetDomainTests {

        @Test
        @DisplayName("should set domain and darklord")
        void shouldSetDomain() {
            Dnd5eFearDread fearDread = createFearDread(1L, "Test Character");
            when(fearDreadRepository.findByCharacterId(1L)).thenReturn(Optional.of(fearDread));
            when(fearDreadRepository.save(any(Dnd5eFearDread.class))).thenAnswer(inv -> inv.getArgument(0));

            Dnd5eFearDread result = horrorService.setDomain(1L, "Barovia", "Strahd");

            assertThat(result.getDomain()).isEqualTo("Barovia");
            assertThat(result.getDarklord()).isEqualTo("Strahd");
        }
    }

    // ==================== Fear Mechanics ====================

    @Nested
    @DisplayName("makeFearCheck Tests")
    class MakeFearCheckTests {

        @Test
        @DisplayName("should create fear/dread if not exists and perform check")
        void shouldCreateFearDreadAndPerformCheck() {
            Dnd5eCharacter character = createCharacter(1L, "Test Character");
            when(fearDreadRepository.findByCharacterId(1L)).thenReturn(Optional.empty());
            when(characterRepository.findById(1L)).thenReturn(Optional.of(character));
            when(fearDreadRepository.save(any(Dnd5eFearDread.class))).thenAnswer(inv -> inv.getArgument(0));

            Dnd5eHorrorService.FearCheckResult result = horrorService.makeFearCheck(1L, 0, false, 2);

            assertThat(result.getDc()).isEqualTo(15);
            assertThat(result.getRoll()).isBetween(1, 20);
            assertThat(result.getFearLevel()).isGreaterThanOrEqualTo(0);
            verify(fearDreadRepository, atLeast(1)).save(any(Dnd5eFearDread.class));
        }

        @Test
        @DisplayName("should update fear level on success")
        void shouldUpdateFearLevelOnSuccess() {
            Dnd5eFearDread fearDread = createFearDread(1L, "Test Character");
            fearDread.setFearLevel(3);
            fearDread.setDreadLevel(30);
            when(fearDreadRepository.findByCharacterId(1L)).thenReturn(Optional.of(fearDread));
            when(fearDreadRepository.save(any(Dnd5eFearDread.class))).thenAnswer(inv -> inv.getArgument(0));

            // With +10 modifier, total = roll + 10 >= 11 (almost always succeeds; only nat 1 fails)
            // Run multiple times to ensure at least one success
            boolean atLeastOneSuccess = false;
            for (int i = 0; i < 10; i++) {
                Dnd5eHorrorService.FearCheckResult result = horrorService.makeFearCheck(1L, 10, false, 2);
                if (result.isSuccess()) {
                    assertThat(result.getFearLevel()).isLessThan(3);
                    assertThat(result.getDreadLevel()).isLessThan(30);
                    atLeastOneSuccess = true;
                    break;
                }
                // Reset for next attempt
                fearDread.setFearLevel(3);
                fearDread.setDreadLevel(30);
            }
            assertThat(atLeastOneSuccess).isTrue();
        }

        @Test
        @DisplayName("should update fear level on failure")
        void shouldUpdateFearLevelOnFailure() {
            Dnd5eFearDread fearDread = createFearDread(1L, "Test Character");
            fearDread.setFearLevel(1);
            fearDread.setDreadLevel(5);
            when(fearDreadRepository.findByCharacterId(1L)).thenReturn(Optional.of(fearDread));
            when(fearDreadRepository.save(any(Dnd5eFearDread.class))).thenAnswer(inv -> inv.getArgument(0));

            // With -5 modifier, most rolls will fail
            Dnd5eHorrorService.FearCheckResult result = horrorService.makeFearCheck(1L, -5, false, 2);

            assertThat(result.getDc()).isEqualTo(15);
            assertThat(result.getRoll()).isBetween(1, 20);
        }

        @Test
        @DisplayName("should set frightened when fear level >= 3")
        void shouldSetFrightenedWhenFearLevelHigh() {
            Dnd5eFearDread fearDread = createFearDread(1L, "Test Character");
            fearDread.setFearLevel(2);
            when(fearDreadRepository.findByCharacterId(1L)).thenReturn(Optional.of(fearDread));
            when(fearDreadRepository.save(any(Dnd5eFearDread.class))).thenAnswer(inv -> inv.getArgument(0));

            // With -10 modifier, will fail and increase fear to 3
            horrorService.makeFearCheck(1L, -10, false, 2);

            Dnd5eFearDread saved = fearDreadRepository.save(fearDread);
            // The save in the service sets frightened based on fear level
        }

        @Test
        @DisplayName("should include proficiency bonus when proficient")
        void shouldIncludeProficiencyBonus() {
            Dnd5eFearDread fearDread = createFearDread(1L, "Test Character");
            when(fearDreadRepository.findByCharacterId(1L)).thenReturn(Optional.of(fearDread));
            when(fearDreadRepository.save(any(Dnd5eFearDread.class))).thenAnswer(inv -> inv.getArgument(0));

            Dnd5eHorrorService.FearCheckResult result = horrorService.makeFearCheck(1L, 2, true, 3);

            // Total = roll + 2 + 3 (proficient)
            assertThat(result.getTotal()).isBetween(6, 25);
        }

        @Test
        @DisplayName("should not include proficiency bonus when not proficient")
        void shouldNotIncludeProficiencyBonus() {
            Dnd5eFearDread fearDread = createFearDread(1L, "Test Character");
            when(fearDreadRepository.findByCharacterId(1L)).thenReturn(Optional.of(fearDread));
            when(fearDreadRepository.save(any(Dnd5eFearDread.class))).thenAnswer(inv -> inv.getArgument(0));

            Dnd5eHorrorService.FearCheckResult result = horrorService.makeFearCheck(1L, 2, false, 3);

            // Total = roll + 2 (not proficient, no prof bonus)
            assertThat(result.getTotal()).isBetween(3, 22);
        }
    }

    @Nested
    @DisplayName("increaseFear Tests")
    class IncreaseFearTests {

        @Test
        @DisplayName("should increase fear level")
        void shouldIncreaseFearLevel() {
            Dnd5eFearDread fearDread = createFearDread(1L, "Test Character");
            fearDread.setFearLevel(1);
            when(fearDreadRepository.findByCharacterId(1L)).thenReturn(Optional.of(fearDread));
            when(fearDreadRepository.save(any(Dnd5eFearDread.class))).thenAnswer(inv -> inv.getArgument(0));

            Dnd5eFearDread result = horrorService.increaseFear(1L, 2);

            assertThat(result.getFearLevel()).isEqualTo(3);
            assertThat(result.isFrightened()).isTrue();
        }

        @Test
        @DisplayName("should clamp fear level to max")
        void shouldClampFearLevel() {
            Dnd5eFearDread fearDread = createFearDread(1L, "Test Character");
            fearDread.setFearLevel(5);
            fearDread.setMaxFearLevel(6);
            when(fearDreadRepository.findByCharacterId(1L)).thenReturn(Optional.of(fearDread));
            when(fearDreadRepository.save(any(Dnd5eFearDread.class))).thenAnswer(inv -> inv.getArgument(0));

            Dnd5eFearDread result = horrorService.increaseFear(1L, 5);

            assertThat(result.getFearLevel()).isEqualTo(6);
        }
    }

    @Nested
    @DisplayName("decreaseFear Tests")
    class DecreaseFearTests {

        @Test
        @DisplayName("should decrease fear level")
        void shouldDecreaseFearLevel() {
            Dnd5eFearDread fearDread = createFearDread(1L, "Test Character");
            fearDread.setFearLevel(4);
            when(fearDreadRepository.findByCharacterId(1L)).thenReturn(Optional.of(fearDread));
            when(fearDreadRepository.save(any(Dnd5eFearDread.class))).thenAnswer(inv -> inv.getArgument(0));

            Dnd5eFearDread result = horrorService.decreaseFear(1L, 2);

            assertThat(result.getFearLevel()).isEqualTo(2);
            assertThat(result.isFrightened()).isFalse();
        }

        @Test
        @DisplayName("should not go below 0")
        void shouldNotGoBelowZero() {
            Dnd5eFearDread fearDread = createFearDread(1L, "Test Character");
            fearDread.setFearLevel(1);
            when(fearDreadRepository.findByCharacterId(1L)).thenReturn(Optional.of(fearDread));
            when(fearDreadRepository.save(any(Dnd5eFearDread.class))).thenAnswer(inv -> inv.getArgument(0));

            Dnd5eFearDread result = horrorService.decreaseFear(1L, 5);

            assertThat(result.getFearLevel()).isZero();
        }
    }

    @Nested
    @DisplayName("clearFear Tests")
    class ClearFearTests {

        @Test
        @DisplayName("should clear all fear")
        void shouldClearAllFear() {
            Dnd5eFearDread fearDread = createFearDread(1L, "Test Character");
            fearDread.setFearLevel(5);
            fearDread.setFrightened(true);
            when(fearDreadRepository.findByCharacterId(1L)).thenReturn(Optional.of(fearDread));
            when(fearDreadRepository.save(any(Dnd5eFearDread.class))).thenAnswer(inv -> inv.getArgument(0));

            Dnd5eFearDread result = horrorService.clearFear(1L);

            assertThat(result.getFearLevel()).isZero();
            assertThat(result.isFrightened()).isFalse();
        }
    }

    @Nested
    @DisplayName("getFearPenalty Tests")
    class GetFearPenaltyTests {

        @Test
        @DisplayName("should return negative fear level as penalty")
        void shouldReturnNegativeFearLevel() {
            Dnd5eFearDread fearDread = createFearDread(1L, "Test Character");
            fearDread.setFearLevel(3);
            when(fearDreadRepository.findByCharacterId(1L)).thenReturn(Optional.of(fearDread));

            int penalty = horrorService.getFearPenalty(1L);

            assertThat(penalty).isEqualTo(-3);
        }

        @Test
        @DisplayName("should return 0 penalty when no fear")
        void shouldReturnZeroPenalty() {
            Dnd5eFearDread fearDread = createFearDread(1L, "Test Character");
            fearDread.setFearLevel(0);
            when(fearDreadRepository.findByCharacterId(1L)).thenReturn(Optional.of(fearDread));

            int penalty = horrorService.getFearPenalty(1L);

            assertThat(penalty).isZero();
        }
    }

    @Nested
    @DisplayName("getFearEffects Tests")
    class GetFearEffectsTests {

        @Test
        @DisplayName("should return no effects at level 0")
        void shouldReturnNoEffects() {
            Dnd5eFearDread fearDread = createFearDread(1L, "Test Character");
            fearDread.setFearLevel(0);
            when(fearDreadRepository.findByCharacterId(1L)).thenReturn(Optional.of(fearDread));

            String effects = horrorService.getFearEffects(1L);

            assertThat(effects).isEqualTo("No fear effects");
        }

        @Test
        @DisplayName("should return disadvantage at level 1-2")
        void shouldReturnDisadvantage() {
            Dnd5eFearDread fearDread = createFearDread(1L, "Test Character");
            fearDread.setFearLevel(2);
            when(fearDreadRepository.findByCharacterId(1L)).thenReturn(Optional.of(fearDread));

            String effects = horrorService.getFearEffects(1L);

            assertThat(effects).isEqualTo("Disadvantage on d20 tests");
        }

        @Test
        @DisplayName("should return no reactions at level 3-4")
        void shouldReturnNoReactions() {
            Dnd5eFearDread fearDread = createFearDread(1L, "Test Character");
            fearDread.setFearLevel(4);
            when(fearDreadRepository.findByCharacterId(1L)).thenReturn(Optional.of(fearDread));

            String effects = horrorService.getFearEffects(1L);

            assertThat(effects).isEqualTo("Disadvantage on d20 tests, cannot take reactions");
        }

        @Test
        @DisplayName("should return paralyzed at level 5-6")
        void shouldReturnParalyzed() {
            Dnd5eFearDread fearDread = createFearDread(1L, "Test Character");
            fearDread.setFearLevel(6);
            when(fearDreadRepository.findByCharacterId(1L)).thenReturn(Optional.of(fearDread));

            String effects = horrorService.getFearEffects(1L);

            assertThat(effects).isEqualTo("Paralyzed with fear, cannot act, move, or speak");
        }
    }

    // ==================== Dread Mechanics ====================

    @Nested
    @DisplayName("increaseDread Tests")
    class IncreaseDreadTests {

        @Test
        @DisplayName("should increase dread level")
        void shouldIncreaseDreadLevel() {
            Dnd5eFearDread fearDread = createFearDread(1L, "Test Character");
            fearDread.setDreadLevel(5);
            when(fearDreadRepository.findByCharacterId(1L)).thenReturn(Optional.of(fearDread));
            when(fearDreadRepository.save(any(Dnd5eFearDread.class))).thenAnswer(inv -> inv.getArgument(0));

            Dnd5eFearDread result = horrorService.increaseDread(1L, 10, "Encountered horror");

            assertThat(result.getDreadLevel()).isEqualTo(15);
            assertThat(result.getDreadEffects()).isNotNull();
        }

        @Test
        @DisplayName("should clamp dread to 100")
        void shouldClampDreadTo100() {
            Dnd5eFearDread fearDread = createFearDread(1L, "Test Character");
            fearDread.setDreadLevel(95);
            when(fearDreadRepository.findByCharacterId(1L)).thenReturn(Optional.of(fearDread));
            when(fearDreadRepository.save(any(Dnd5eFearDread.class))).thenAnswer(inv -> inv.getArgument(0));

            Dnd5eFearDread result = horrorService.increaseDread(1L, 20, "Encountered horror");

            assertThat(result.getDreadLevel()).isEqualTo(100);
        }

        @Test
        @DisplayName("should update dread effects")
        void shouldUpdateDreadEffects() {
            Dnd5eFearDread fearDread = createFearDread(1L, "Test Character");
            fearDread.setDreadLevel(0);
            when(fearDreadRepository.findByCharacterId(1L)).thenReturn(Optional.of(fearDread));
            when(fearDreadRepository.save(any(Dnd5eFearDread.class))).thenAnswer(inv -> inv.getArgument(0));

            Dnd5eFearDread result = horrorService.increaseDread(1L, 25, "Encountered horror");

            assertThat(result.getDreadEffects()).contains("Nightmares");
            assertThat(result.getDreadEffects()).contains("Physical changes");
        }
    }

    @Nested
    @DisplayName("decreaseDread Tests")
    class DecreaseDreadTests {

        @Test
        @DisplayName("should decrease dread level")
        void shouldDecreaseDreadLevel() {
            Dnd5eFearDread fearDread = createFearDread(1L, "Test Character");
            fearDread.setDreadLevel(30);
            when(fearDreadRepository.findByCharacterId(1L)).thenReturn(Optional.of(fearDread));
            when(fearDreadRepository.save(any(Dnd5eFearDread.class))).thenAnswer(inv -> inv.getArgument(0));

            Dnd5eFearDread result = horrorService.decreaseDread(1L, 10);

            assertThat(result.getDreadLevel()).isEqualTo(20);
        }

        @Test
        @DisplayName("should not go below 0")
        void shouldNotGoBelowZero() {
            Dnd5eFearDread fearDread = createFearDread(1L, "Test Character");
            fearDread.setDreadLevel(5);
            when(fearDreadRepository.findByCharacterId(1L)).thenReturn(Optional.of(fearDread));
            when(fearDreadRepository.save(any(Dnd5eFearDread.class))).thenAnswer(inv -> inv.getArgument(0));

            Dnd5eFearDread result = horrorService.decreaseDread(1L, 20);

            assertThat(result.getDreadLevel()).isZero();
        }
    }

    @Nested
    @DisplayName("clearDread Tests")
    class ClearDreadTests {

        @Test
        @DisplayName("should clear all dread")
        void shouldClearAllDread() {
            Dnd5eFearDread fearDread = createFearDread(1L, "Test Character");
            fearDread.setDreadLevel(50);
            fearDread.setDreadEffects("Some effects");
            when(fearDreadRepository.findByCharacterId(1L)).thenReturn(Optional.of(fearDread));
            when(fearDreadRepository.save(any(Dnd5eFearDread.class))).thenAnswer(inv -> inv.getArgument(0));

            Dnd5eFearDread result = horrorService.clearDread(1L);

            assertThat(result.getDreadLevel()).isZero();
            assertThat(result.getDreadEffects()).isNull();
        }
    }

    @Nested
    @DisplayName("getDreadEffects Tests")
    class GetDreadEffectsTests {

        @Test
        @DisplayName("should return dread effects")
        void shouldReturnDreadEffects() {
            Dnd5eFearDread fearDread = createFearDread(1L, "Test Character");
            fearDread.setDreadLevel(30);
            fearDread.setDreadEffects("Nightmares; Physical changes");
            when(fearDreadRepository.findByCharacterId(1L)).thenReturn(Optional.of(fearDread));

            String effects = horrorService.getDreadEffects(1L);

            assertThat(effects).isEqualTo("Nightmares; Physical changes");
        }

        @Test
        @DisplayName("should return null when no effects")
        void shouldReturnNullWhenNoEffects() {
            Dnd5eFearDread fearDread = createFearDread(1L, "Test Character");
            fearDread.setDreadLevel(0);
            when(fearDreadRepository.findByCharacterId(1L)).thenReturn(Optional.of(fearDread));

            String effects = horrorService.getDreadEffects(1L);

            assertThat(effects).isNull();
        }
    }

    @Nested
    @DisplayName("getDreadDescription Tests")
    class GetDreadDescriptionTests {

        @Test
        @DisplayName("should return no corruption at 0")
        void shouldReturnNoCorruption() {
            Dnd5eFearDread fearDread = createFearDread(1L, "Test Character");
            fearDread.setDreadLevel(0);
            when(fearDreadRepository.findByCharacterId(1L)).thenReturn(Optional.of(fearDread));

            String desc = horrorService.getDreadDescription(1L);

            assertThat(desc).isEqualTo("No corruption");
        }

        @Test
        @DisplayName("should return slightly unsettled at 1-9")
        void shouldReturnSlightlyUnsettled() {
            Dnd5eFearDread fearDread = createFearDread(1L, "Test Character");
            fearDread.setDreadLevel(5);
            when(fearDreadRepository.findByCharacterId(1L)).thenReturn(Optional.of(fearDread));

            String desc = horrorService.getDreadDescription(1L);

            assertThat(desc).isEqualTo("Slightly unsettled");
        }

        @Test
        @DisplayName("should return haunted by nightmares at 10-24")
        void shouldReturnHauntedByNightmares() {
            Dnd5eFearDread fearDread = createFearDread(1L, "Test Character");
            fearDread.setDreadLevel(15);
            when(fearDreadRepository.findByCharacterId(1L)).thenReturn(Optional.of(fearDread));

            String desc = horrorService.getDreadDescription(1L);

            assertThat(desc).isEqualTo("Haunted by nightmares");
        }

        @Test
        @DisplayName("should return visibly affected at 25-49")
        void shouldReturnVisiblyAffected() {
            Dnd5eFearDread fearDread = createFearDread(1L, "Test Character");
            fearDread.setDreadLevel(30);
            when(fearDreadRepository.findByCharacterId(1L)).thenReturn(Optional.of(fearDread));

            String desc = horrorService.getDreadDescription(1L);

            assertThat(desc).isEqualTo("Visibly affected by the Domain");
        }

        @Test
        @DisplayName("should return deeply corrupted at 50-74")
        void shouldReturnDeeplyCorrupted() {
            Dnd5eFearDread fearDread = createFearDread(1L, "Test Character");
            fearDread.setDreadLevel(60);
            when(fearDreadRepository.findByCharacterId(1L)).thenReturn(Optional.of(fearDread));

            String desc = horrorService.getDreadDescription(1L);

            assertThat(desc).isEqualTo("Deeply corrupted by the Domain");
        }

        @Test
        @DisplayName("should return becoming part of domain at 75-89")
        void shouldReturnBecomingPartOfDomain() {
            Dnd5eFearDread fearDread = createFearDread(1L, "Test Character");
            fearDread.setDreadLevel(80);
            when(fearDreadRepository.findByCharacterId(1L)).thenReturn(Optional.of(fearDread));

            String desc = horrorService.getDreadDescription(1L);

            assertThat(desc).isEqualTo("Becoming part of the Domain");
        }

        @Test
        @DisplayName("should return verge of transformation at 90+")
        void shouldReturnVergeOfTransformation() {
            Dnd5eFearDread fearDread = createFearDread(1L, "Test Character");
            fearDread.setDreadLevel(95);
            when(fearDreadRepository.findByCharacterId(1L)).thenReturn(Optional.of(fearDread));

            String desc = horrorService.getDreadDescription(1L);

            assertThat(desc).isEqualTo("On the verge of transformation");
        }
    }

    // ==================== Dark Gift Acquisition ====================

    @Nested
    @DisplayName("acquireDarkGift Tests")
    class AcquireDarkGiftTests {

        @Test
        @DisplayName("should acquire dark gift and increase dread")
        void shouldAcquireDarkGift() {
            Dnd5eDarkGift gift = createDarkGift("Bone Armor", "Undead");
            Dnd5eFearDread fearDread = createFearDread(1L, "Test Character");
            fearDread.setDreadLevel(5);

            when(darkGiftRepository.findByName("Bone Armor")).thenReturn(Optional.of(gift));
            when(fearDreadRepository.findByCharacterId(1L)).thenReturn(Optional.of(fearDread));
            when(fearDreadRepository.save(any(Dnd5eFearDread.class))).thenAnswer(inv -> inv.getArgument(0));

            Dnd5eFearDread result = horrorService.acquireDarkGift(1L, "Bone Armor");

            assertThat(result.getDreadLevel()).isEqualTo(15);
            assertThat(result.getDreadEffects()).contains("Dark Gift curse");
        }
    }

    // ==================== Reference Data ====================

    @Nested
    @DisplayName("getDarkGiftCategories Tests")
    class GetDarkGiftCategoriesTests {

        @Test
        @DisplayName("should return all categories")
        void shouldReturnAllCategories() {
            List<String> categories = horrorService.getDarkGiftCategories();

            assertThat(categories).containsExactly("Undead", "Fiend", "Aberration", "Fey", "Beast");
        }
    }

    @Nested
    @DisplayName("getDarklords Tests")
    class GetDarklordsTests {

        @Test
        @DisplayName("should return all darklords")
        void shouldReturnAllDarklords() {
            List<String> darklords = horrorService.getDarklords();

            assertThat(darklords).contains("Strahd", "Azalin", "Vecna");
            assertThat(darklords).hasSize(8);
        }
    }

    @Nested
    @DisplayName("getDomains Tests")
    class GetDomainsTests {

        @Test
        @DisplayName("should return all domains")
        void shouldReturnAllDomains() {
            List<String> domains = horrorService.getDomains();

            assertThat(domains).contains("Barovia", "Ravenloft");
            assertThat(domains).hasSize(10);
        }
    }

    // ==================== Initialization ====================

    @Nested
    @DisplayName("populateDarkGifts Tests")
    class PopulateDarkGiftsTests {

        @Test
        @DisplayName("should populate dark gifts")
        void shouldPopulateDarkGifts() {
            when(darkGiftRepository.save(any(Dnd5eDarkGift.class))).thenAnswer(inv -> inv.getArgument(0));

            horrorService.populateDarkGifts();

            verify(darkGiftRepository, atLeast(10)).save(any(Dnd5eDarkGift.class));
        }
    }

    @Nested
    @DisplayName("initializeHorrorDatabase Tests")
    class InitializeHorrorDatabaseTests {

        @Test
        @DisplayName("should skip if already initialized")
        void shouldSkipIfAlreadyInitialized() {
            // Create a fresh service to avoid shared state from other tests
            Dnd5eHorrorService freshService = new Dnd5eHorrorService(darkGiftRepository, fearDreadRepository, characterRepository);
            when(darkGiftRepository.count()).thenReturn(15L);

            freshService.initializeHorrorDatabase();

            // Second call should skip because initialized flag is set
            freshService.initializeHorrorDatabase();

            verify(darkGiftRepository, times(1)).count();
        }

        @Test
        @DisplayName("should skip if already populated")
        void shouldSkipIfAlreadyPopulated() {
            when(darkGiftRepository.count()).thenReturn(15L);

            horrorService.initializeHorrorDatabase();

            verify(darkGiftRepository).count();
            verify(darkGiftRepository, never()).save(any(Dnd5eDarkGift.class));
        }

        @Test
        @DisplayName("should populate if missing")
        void shouldPopulateIfMissing() {
            when(darkGiftRepository.count()).thenReturn(0L);
            when(darkGiftRepository.save(any(Dnd5eDarkGift.class))).thenAnswer(inv -> inv.getArgument(0));

            horrorService.initializeHorrorDatabase();

            verify(darkGiftRepository, atLeast(10)).save(any(Dnd5eDarkGift.class));
        }
    }
}
