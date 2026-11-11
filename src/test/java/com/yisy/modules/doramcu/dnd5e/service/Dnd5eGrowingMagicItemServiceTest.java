package com.yisy.modules.doramcu.dnd5e.service;

import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCharacter;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCharacterGrowingItem;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eGrowingMagicItem;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5eCharacterGrowingItemRepository;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5eCharacterRepository;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5eGrowingMagicItemRepository;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eGrowingMagicItemService.ItemAbilities;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eGrowingMagicItemService.ItemLevelUpResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Dnd5eGrowingMagicItemService Tests")
class Dnd5eGrowingMagicItemServiceTest {

    @Mock
    private Dnd5eGrowingMagicItemRepository growingItemRepository;

    @Mock
    private Dnd5eCharacterGrowingItemRepository charItemRepository;

    @Mock
    private Dnd5eCharacterRepository characterRepository;

    @InjectMocks
    private Dnd5eGrowingMagicItemService service;

    // ==================== Helper Methods ====================

    private Dnd5eGrowingMagicItem createItem(String name, String category, String rarity) {
        Dnd5eGrowingMagicItem item = new Dnd5eGrowingMagicItem();
        item.setId(1L);
        item.setName(name);
        item.setCategory(category);
        item.setRarity(rarity);
        item.setMaxLevel(20);
        item.setDescription("Test item");
        item.setAbility1("Base ability");
        item.setAbility5("Level 5 ability");
        item.setAbility10("Level 10 ability");
        item.setAbility15("Level 15 ability");
        item.setAbility20("Level 20 ability");
        item.setStatBonus1("+1 to attack");
        item.setStatBonus5("+1 to attack, extra");
        item.setStatBonus10("+2 to attack");
        item.setStatBonus15("+3 to attack");
        item.setStatBonus20("+3 legendary");
        return item;
    }

    private Dnd5eCharacterGrowingItem createCharItem(Long characterId, Long itemId, int level) {
        Dnd5eCharacterGrowingItem charItem = new Dnd5eCharacterGrowingItem();
        charItem.setId(1L);
        charItem.setCharacterId(characterId);
        charItem.setCharacterName("TestChar");
        charItem.setGrowingItemId(itemId);
        charItem.setItemName("TestItem");
        charItem.setItemLevel(level);
        charItem.setAttuned(true);
        charItem.setActive(true);
        charItem.setAbility1UnlockedAt(1);
        return charItem;
    }

    private Dnd5eCharacter createCharacter(Long id, String name, int level) {
        Dnd5eCharacter character = new Dnd5eCharacter();
        character.setId(id);
        character.setName(name);
        character.setLevel(level);
        return character;
    }

    // ==================== Growing Item CRUD ====================

    @Nested
    @DisplayName("getGrowingItemByName Tests")
    class GetGrowingItemByNameTests {

        @Test
        @DisplayName("should return item by name")
        void shouldReturnItemByName() {
            Dnd5eGrowingMagicItem item = createItem("Blade of Growth", "Weapon", "Rare");
            when(growingItemRepository.findByName("Blade of Growth")).thenReturn(Optional.of(item));

            Dnd5eGrowingMagicItem result = service.getGrowingItemByName("Blade of Growth");

            assertThat(result.getName()).isEqualTo("Blade of Growth");
            assertThat(result.getCategory()).isEqualTo("Weapon");
            verify(growingItemRepository).findByName("Blade of Growth");
        }

        @Test
        @DisplayName("should throw when item not found")
        void shouldThrowWhenItemNotFound() {
            when(growingItemRepository.findByName("Nonexistent")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getGrowingItemByName("Nonexistent"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Growing magic item not found");
        }
    }

    @Nested
    @DisplayName("getGrowingItem Tests")
    class GetGrowingItemTests {

        @Test
        @DisplayName("should return item by ID")
        void shouldReturnItemById() {
            Dnd5eGrowingMagicItem item = createItem("Blade of Growth", "Weapon", "Rare");
            when(growingItemRepository.findById(1L)).thenReturn(Optional.of(item));

            Dnd5eGrowingMagicItem result = service.getGrowingItem(1L);

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getName()).isEqualTo("Blade of Growth");
        }

        @Test
        @DisplayName("should throw when item not found")
        void shouldThrowWhenItemNotFound() {
            when(growingItemRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getGrowingItem(999L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Growing magic item not found");
        }
    }

    @Nested
    @DisplayName("listGrowingItems Tests")
    class ListGrowingItemsTests {

        @Test
        @DisplayName("should return all items")
        void shouldReturnAllItems() {
            List<Dnd5eGrowingMagicItem> items = Arrays.asList(
                    createItem("Item1", "Weapon", "Rare"),
                    createItem("Item2", "Armor", "Rare")
            );
            items.get(0).setId(1L);
            items.get(1).setId(2L);
            when(growingItemRepository.findAll()).thenReturn(items);

            List<Dnd5eGrowingMagicItem> result = service.listGrowingItems();

            assertThat(result).hasSize(2);
            assertThat(result).extracting(Dnd5eGrowingMagicItem::getName)
                    .containsExactly("Item1", "Item2");
        }

        @Test
        @DisplayName("should return empty list when no items")
        void shouldReturnEmptyListWhenNoItems() {
            when(growingItemRepository.findAll()).thenReturn(Collections.emptyList());

            List<Dnd5eGrowingMagicItem> result = service.listGrowingItems();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("listGrowingItemsByCategory Tests")
    class ListGrowingItemsByCategoryTests {

        @Test
        @DisplayName("should return items by category")
        void shouldReturnItemsByCategory() {
            Dnd5eGrowingMagicItem item = createItem("Blade", "Weapon", "Rare");
            when(growingItemRepository.findByCategory("Weapon")).thenReturn(List.of(item));

            List<Dnd5eGrowingMagicItem> result = service.listGrowingItemsByCategory("Weapon");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getCategory()).isEqualTo("Weapon");
        }
    }

    @Nested
    @DisplayName("listGrowingItemsByRarity Tests")
    class ListGrowingItemsByRarityTests {

        @Test
        @DisplayName("should return items by rarity")
        void shouldReturnItemsByRarity() {
            Dnd5eGrowingMagicItem item = createItem("Blade", "Weapon", "Legendary");
            when(growingItemRepository.findByRarity("Legendary")).thenReturn(List.of(item));

            List<Dnd5eGrowingMagicItem> result = service.listGrowingItemsByRarity("Legendary");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getRarity()).isEqualTo("Legendary");
        }
    }

    @Nested
    @DisplayName("searchGrowingItems Tests")
    class SearchGrowingItemsTests {

        @Test
        @DisplayName("should return matching items")
        void shouldReturnMatchingItems() {
            Dnd5eGrowingMagicItem item = createItem("Blade of Growth", "Weapon", "Rare");
            when(growingItemRepository.findByNameContainingIgnoreCase("blade")).thenReturn(List.of(item));

            List<Dnd5eGrowingMagicItem> result = service.searchGrowingItems("blade");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("Blade of Growth");
        }

        @Test
        @DisplayName("should return empty when no match")
        void shouldReturnEmptyWhenNoMatch() {
            when(growingItemRepository.findByNameContainingIgnoreCase("nonexistent"))
                    .thenReturn(Collections.emptyList());

            List<Dnd5eGrowingMagicItem> result = service.searchGrowingItems("nonexistent");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("createGrowingItem Tests")
    class CreateGrowingItemTests {

        @Test
        @DisplayName("should create and save item")
        void shouldCreateAndSaveItem() {
            Dnd5eGrowingMagicItem saved = new Dnd5eGrowingMagicItem();
            saved.setId(10L);
            saved.setName("New Item");
            saved.setCategory("Wondrous Item");
            saved.setRarity("Very Rare");
            saved.setMaxLevel(20);
            saved.setDescription("Description");
            saved.setAbility1("Ability 1");
            saved.setAbility5("Ability 5");
            saved.setAbility10("Ability 10");
            saved.setAbility15("Ability 15");
            saved.setAbility20("Ability 20");
            saved.setStatBonus1("Bonus 1");
            saved.setStatBonus5("Bonus 5");
            saved.setStatBonus10("Bonus 10");
            saved.setStatBonus15("Bonus 15");
            saved.setStatBonus20("Bonus 20");
            when(growingItemRepository.save(any(Dnd5eGrowingMagicItem.class))).thenReturn(saved);

            Dnd5eGrowingMagicItem result = service.createGrowingItem(
                    "New Item", "Wondrous Item", "Very Rare", 20, "Description",
                    "Ability 1", "Ability 5", "Ability 10", "Ability 15", "Ability 20",
                    "Bonus 1", "Bonus 5", "Bonus 10", "Bonus 15", "Bonus 20"
            );

            assertThat(result.getName()).isEqualTo("New Item");
            assertThat(result.getCategory()).isEqualTo("Wondrous Item");
            assertThat(result.getAbility1()).isEqualTo("Ability 1");
            assertThat(result.getStatBonus5()).isEqualTo("Bonus 5");
            verify(growingItemRepository).save(any(Dnd5eGrowingMagicItem.class));
        }
    }

    @Nested
    @DisplayName("deleteGrowingItem Tests")
    class DeleteGrowingItemTests {

        @Test
        @DisplayName("should delete item by ID")
        void shouldDeleteItemById() {
            service.deleteGrowingItem(5L);

            verify(growingItemRepository).deleteById(5L);
        }
    }

    // ==================== Character Item Binding ====================

    @Nested
    @DisplayName("bindItemToCharacter Tests")
    class BindItemToCharacterTests {

        @Test
        @DisplayName("should bind item to character")
        void shouldBindItemToCharacter() {
            Dnd5eCharacter character = createCharacter(1L, "Hero", 3);
            Dnd5eGrowingMagicItem item = createItem("Blade", "Weapon", "Rare");
            Dnd5eCharacterGrowingItem saved = createCharItem(1L, 1L, 3);
            saved.setId(1L);

            when(characterRepository.findById(1L)).thenReturn(Optional.of(character));
            when(growingItemRepository.findById(1L)).thenReturn(Optional.of(item));
            when(charItemRepository.existsByCharacterIdAndGrowingItemIdAndActiveTrue(1L, 1L))
                    .thenReturn(false);
            when(charItemRepository.save(any(Dnd5eCharacterGrowingItem.class))).thenReturn(saved);

            Dnd5eCharacterGrowingItem result = service.bindItemToCharacter(1L, 1L);

            assertThat(result.getItemName()).isEqualTo("TestItem");
            assertThat(result.getItemLevel()).isEqualTo(3);
            assertThat(result.isAttuned()).isTrue();
            assertThat(result.isActive()).isTrue();
            verify(charItemRepository).save(any(Dnd5eCharacterGrowingItem.class));
        }

        @Test
        @DisplayName("should throw when character not found")
        void shouldThrowWhenCharacterNotFound() {
            when(characterRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.bindItemToCharacter(999L, 1L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Character not found");
        }

        @Test
        @DisplayName("should throw when item not found")
        void shouldThrowWhenItemNotFound() {
            Dnd5eCharacter character = createCharacter(1L, "Hero", 3);
            when(characterRepository.findById(1L)).thenReturn(Optional.of(character));
            when(growingItemRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.bindItemToCharacter(1L, 999L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Growing magic item not found");
        }

        @Test
        @DisplayName("should throw when already bound")
        void shouldThrowWhenAlreadyBound() {
            Dnd5eCharacter character = createCharacter(1L, "Hero", 3);
            Dnd5eGrowingMagicItem item = createItem("Blade", "Weapon", "Rare");
            when(characterRepository.findById(1L)).thenReturn(Optional.of(character));
            when(growingItemRepository.findById(1L)).thenReturn(Optional.of(item));
            when(charItemRepository.existsByCharacterIdAndGrowingItemIdAndActiveTrue(1L, 1L))
                    .thenReturn(true);

            assertThatThrownBy(() -> service.bindItemToCharacter(1L, 1L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("already has this growing item");
        }

        @Test
        @DisplayName("should set high level unlocks for high level character")
        void shouldSetHighLevelUnlocksForHighLevelCharacter() {
            Dnd5eCharacter character = createCharacter(1L, "Hero", 15);
            Dnd5eGrowingMagicItem item = createItem("Blade", "Weapon", "Rare");
            Dnd5eCharacterGrowingItem saved = createCharItem(1L, 1L, 15);
            saved.setAbility5UnlockedAt(5);
            saved.setAbility10UnlockedAt(10);
            saved.setAbility15UnlockedAt(15);

            when(characterRepository.findById(1L)).thenReturn(Optional.of(character));
            when(growingItemRepository.findById(1L)).thenReturn(Optional.of(item));
            when(charItemRepository.existsByCharacterIdAndGrowingItemIdAndActiveTrue(1L, 1L))
                    .thenReturn(false);
            when(charItemRepository.save(any(Dnd5eCharacterGrowingItem.class))).thenReturn(saved);

            Dnd5eCharacterGrowingItem result = service.bindItemToCharacter(1L, 1L);

            assertThat(result.getItemLevel()).isEqualTo(15);
            assertThat(result.getAbility5UnlockedAt()).isEqualTo(5);
            assertThat(result.getAbility10UnlockedAt()).isEqualTo(10);
            assertThat(result.getAbility15UnlockedAt()).isEqualTo(15);
        }
    }

    @Nested
    @DisplayName("listCharacterGrowingItems Tests")
    class ListCharacterGrowingItemsTests {

        @Test
        @DisplayName("should return active items for character")
        void shouldReturnActiveItemsForCharacter() {
            when(characterRepository.existsById(1L)).thenReturn(true);
            List<Dnd5eCharacterGrowingItem> items = Arrays.asList(
                    createCharItem(1L, 1L, 5),
                    createCharItem(1L, 2L, 3)
            );
            items.get(0).setId(1L);
            items.get(1).setId(2L);
            when(charItemRepository.findByCharacterIdAndActiveTrue(1L)).thenReturn(items);

            List<Dnd5eCharacterGrowingItem> result = service.listCharacterGrowingItems(1L);

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("should throw when character not found")
        void shouldThrowWhenCharacterNotFound() {
            when(characterRepository.existsById(999L)).thenReturn(false);

            assertThatThrownBy(() -> service.listCharacterGrowingItems(999L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Character not found");
        }
    }

    @Nested
    @DisplayName("getCharacterGrowingItem Tests")
    class GetCharacterGrowingItemTests {

        @Test
        @DisplayName("should return specific item for character")
        void shouldReturnSpecificItemForCharacter() {
            when(characterRepository.existsById(1L)).thenReturn(true);
            Dnd5eCharacterGrowingItem charItem = createCharItem(1L, 1L, 5);
            when(charItemRepository.findByCharacterIdAndGrowingItemId(1L, 1L))
                    .thenReturn(Optional.of(charItem));

            Dnd5eCharacterGrowingItem result = service.getCharacterGrowingItem(1L, 1L);

            assertThat(result.getItemLevel()).isEqualTo(5);
            assertThat(result.getItemName()).isEqualTo("TestItem");
        }

        @Test
        @DisplayName("should throw when item not found")
        void shouldThrowWhenItemNotFound() {
            when(characterRepository.existsById(1L)).thenReturn(true);
            when(charItemRepository.findByCharacterIdAndGrowingItemId(1L, 999L))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getCharacterGrowingItem(1L, 999L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Growing item not found");
        }
    }

    @Nested
    @DisplayName("unbindItemFromCharacter Tests")
    class UnbindItemFromCharacterTests {

        @Test
        @DisplayName("should unbind item from character")
        void shouldUnbindItemFromCharacter() {
            when(characterRepository.existsById(1L)).thenReturn(true);
            Dnd5eCharacterGrowingItem charItem = createCharItem(1L, 1L, 5);
            when(charItemRepository.findByCharacterIdAndGrowingItemId(1L, 1L))
                    .thenReturn(Optional.of(charItem));
            when(charItemRepository.save(any(Dnd5eCharacterGrowingItem.class))).thenReturn(charItem);

            service.unbindItemFromCharacter(1L, 1L);

            assertThat(charItem.isActive()).isFalse();
            assertThat(charItem.isAttuned()).isFalse();
            verify(charItemRepository).save(charItem);
        }
    }

    @Nested
    @DisplayName("toggleAttunement Tests")
    class ToggleAttunementTests {

        @Test
        @DisplayName("should toggle attunement from true to false")
        void shouldToggleAttunementFromTrueToFalse() {
            when(characterRepository.existsById(1L)).thenReturn(true);
            Dnd5eCharacterGrowingItem charItem = createCharItem(1L, 1L, 5);
            charItem.setAttuned(true);
            when(charItemRepository.findByCharacterIdAndGrowingItemId(1L, 1L))
                    .thenReturn(Optional.of(charItem));
            when(charItemRepository.save(any(Dnd5eCharacterGrowingItem.class))).thenReturn(charItem);

            Dnd5eCharacterGrowingItem result = service.toggleAttunement(1L, 1L);

            assertThat(result.isAttuned()).isFalse();
        }

        @Test
        @DisplayName("should toggle attunement from false to true")
        void shouldToggleAttunementFromFalseToTrue() {
            when(characterRepository.existsById(1L)).thenReturn(true);
            Dnd5eCharacterGrowingItem charItem = createCharItem(1L, 1L, 5);
            charItem.setAttuned(false);
            when(charItemRepository.findByCharacterIdAndGrowingItemId(1L, 1L))
                    .thenReturn(Optional.of(charItem));
            when(charItemRepository.save(any(Dnd5eCharacterGrowingItem.class))).thenReturn(charItem);

            Dnd5eCharacterGrowingItem result = service.toggleAttunement(1L, 1L);

            assertThat(result.isAttuned()).isTrue();
        }
    }

    // ==================== Level Up Mechanics ====================

    @Nested
    @DisplayName("levelUpItems Tests")
    class LevelUpItemsTests {

        @Test
        @DisplayName("should level up items and unlock abilities")
        void shouldLevelUpItemsAndUnlockAbilities() {
            when(characterRepository.existsById(1L)).thenReturn(true);
            Dnd5eCharacterGrowingItem charItem = createCharItem(1L, 1L, 3);
            charItem.setAbility5UnlockedAt(null);
            Dnd5eGrowingMagicItem item = createItem("Blade", "Weapon", "Rare");

            when(charItemRepository.findByCharacterIdAndActiveTrue(1L)).thenReturn(List.of(charItem));
            when(growingItemRepository.findById(1L)).thenReturn(Optional.of(item));
            when(charItemRepository.save(any(Dnd5eCharacterGrowingItem.class))).thenReturn(charItem);

            List<ItemLevelUpResult> results = service.levelUpItems(1L, 5);

            assertThat(results).hasSize(1);
            assertThat(results.get(0).getOldLevel()).isEqualTo(3);
            assertThat(results.get(0).getNewLevel()).isEqualTo(5);
            assertThat(results.get(0).getNewAbilities()).contains("Level 5 ability");
            assertThat(charItem.getItemLevel()).isEqualTo(5);
            assertThat(charItem.getAbility5UnlockedAt()).isEqualTo(5);
        }

        @Test
        @DisplayName("should unlock multiple abilities at once")
        void shouldUnlockMultipleAbilitiesAtOnce() {
            when(characterRepository.existsById(1L)).thenReturn(true);
            Dnd5eCharacterGrowingItem charItem = createCharItem(1L, 1L, 3);
            charItem.setAbility5UnlockedAt(null);
            charItem.setAbility10UnlockedAt(null);
            Dnd5eGrowingMagicItem item = createItem("Blade", "Weapon", "Rare");

            when(charItemRepository.findByCharacterIdAndActiveTrue(1L)).thenReturn(List.of(charItem));
            when(growingItemRepository.findById(1L)).thenReturn(Optional.of(item));
            when(charItemRepository.save(any(Dnd5eCharacterGrowingItem.class))).thenReturn(charItem);

            List<ItemLevelUpResult> results = service.levelUpItems(1L, 10);

            assertThat(results).hasSize(1);
            assertThat(results.get(0).getNewAbilities()).hasSize(2);
            assertThat(results.get(0).getNewAbilities()).contains("Level 5 ability", "Level 10 ability");
        }

        @Test
        @DisplayName("should not level up if already at new level")
        void shouldNotLevelUpIfAlreadyAtNewLevel() {
            when(characterRepository.existsById(1L)).thenReturn(true);
            Dnd5eCharacterGrowingItem charItem = createCharItem(1L, 1L, 10);
            Dnd5eGrowingMagicItem item = createItem("Blade", "Weapon", "Rare");

            when(charItemRepository.findByCharacterIdAndActiveTrue(1L)).thenReturn(List.of(charItem));
            when(growingItemRepository.findById(1L)).thenReturn(Optional.of(item));

            List<ItemLevelUpResult> results = service.levelUpItems(1L, 7);

            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("should cap at max level")
        void shouldCapAtMaxLevel() {
            when(characterRepository.existsById(1L)).thenReturn(true);
            Dnd5eCharacterGrowingItem charItem = createCharItem(1L, 1L, 18);
            Dnd5eGrowingMagicItem item = createItem("Blade", "Weapon", "Rare");
            item.setMaxLevel(20);

            when(charItemRepository.findByCharacterIdAndActiveTrue(1L)).thenReturn(List.of(charItem));
            when(growingItemRepository.findById(1L)).thenReturn(Optional.of(item));
            when(charItemRepository.save(any(Dnd5eCharacterGrowingItem.class))).thenReturn(charItem);

            List<ItemLevelUpResult> results = service.levelUpItems(1L, 25);

            assertThat(results).hasSize(1);
            assertThat(results.get(0).getNewLevel()).isEqualTo(20);
        }

        @Test
        @DisplayName("should return empty when no active items")
        void shouldReturnEmptyWhenNoActiveItems() {
            when(characterRepository.existsById(1L)).thenReturn(true);
            when(charItemRepository.findByCharacterIdAndActiveTrue(1L))
                    .thenReturn(Collections.emptyList());

            List<ItemLevelUpResult> results = service.levelUpItems(1L, 10);

            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("should unlock level 20 ability")
        void shouldUnlockLevel20Ability() {
            when(characterRepository.existsById(1L)).thenReturn(true);
            Dnd5eCharacterGrowingItem charItem = createCharItem(1L, 1L, 19);
            charItem.setAbility20UnlockedAt(null);
            Dnd5eGrowingMagicItem item = createItem("Blade", "Weapon", "Rare");

            when(charItemRepository.findByCharacterIdAndActiveTrue(1L)).thenReturn(List.of(charItem));
            when(growingItemRepository.findById(1L)).thenReturn(Optional.of(item));
            when(charItemRepository.save(any(Dnd5eCharacterGrowingItem.class))).thenReturn(charItem);

            List<ItemLevelUpResult> results = service.levelUpItems(1L, 20);

            assertThat(results).hasSize(1);
            assertThat(results.get(0).getNewAbilities()).contains("Level 20 ability");
            assertThat(charItem.getAbility20UnlockedAt()).isEqualTo(20);
        }
    }

    @Nested
    @DisplayName("getAbilities Tests")
    class GetAbilitiesTests {

        @Test
        @DisplayName("should return abilities for level 1 item")
        void shouldReturnAbilitiesForLevel1Item() {
            when(characterRepository.existsById(1L)).thenReturn(true);
            Dnd5eCharacterGrowingItem charItem = createCharItem(1L, 1L, 1);
            Dnd5eGrowingMagicItem item = createItem("Blade", "Weapon", "Rare");

            when(charItemRepository.findByCharacterIdAndGrowingItemId(1L, 1L))
                    .thenReturn(Optional.of(charItem));
            when(growingItemRepository.findById(1L)).thenReturn(Optional.of(item));

            ItemAbilities abilities = service.getAbilities(1L, 1L);

            assertThat(abilities.getItemName()).isEqualTo("Blade");
            assertThat(abilities.getItemLevel()).isEqualTo(1);
            assertThat(abilities.isAttuned()).isTrue();
            assertThat(abilities.getAbility1()).isEqualTo("Base ability");
            assertThat(abilities.getStatBonus1()).isEqualTo("+1 to attack");
            assertThat(abilities.getAbility5()).isNull();
        }

        @Test
        @DisplayName("should return abilities for level 10 item")
        void shouldReturnAbilitiesForLevel10Item() {
            when(characterRepository.existsById(1L)).thenReturn(true);
            Dnd5eCharacterGrowingItem charItem = createCharItem(1L, 1L, 10);
            Dnd5eGrowingMagicItem item = createItem("Blade", "Weapon", "Rare");

            when(charItemRepository.findByCharacterIdAndGrowingItemId(1L, 1L))
                    .thenReturn(Optional.of(charItem));
            when(growingItemRepository.findById(1L)).thenReturn(Optional.of(item));

            ItemAbilities abilities = service.getAbilities(1L, 1L);

            assertThat(abilities.getAbility1()).isEqualTo("Base ability");
            assertThat(abilities.getAbility5()).isEqualTo("Level 5 ability");
            assertThat(abilities.getAbility10()).isEqualTo("Level 10 ability");
            assertThat(abilities.getStatBonus10()).isEqualTo("+2 to attack");
            assertThat(abilities.getAbility15()).isNull();
        }

        @Test
        @DisplayName("should return all abilities for level 20 item")
        void shouldReturnAllAbilitiesForLevel20Item() {
            when(characterRepository.existsById(1L)).thenReturn(true);
            Dnd5eCharacterGrowingItem charItem = createCharItem(1L, 1L, 20);
            Dnd5eGrowingMagicItem item = createItem("Blade", "Weapon", "Rare");

            when(charItemRepository.findByCharacterIdAndGrowingItemId(1L, 1L))
                    .thenReturn(Optional.of(charItem));
            when(growingItemRepository.findById(1L)).thenReturn(Optional.of(item));

            ItemAbilities abilities = service.getAbilities(1L, 1L);

            assertThat(abilities.getAbility1()).isNotNull();
            assertThat(abilities.getAbility5()).isNotNull();
            assertThat(abilities.getAbility10()).isNotNull();
            assertThat(abilities.getAbility15()).isNotNull();
            assertThat(abilities.getAbility20()).isEqualTo("Level 20 ability");
            assertThat(abilities.getStatBonus20()).isEqualTo("+3 legendary");
        }
    }

    // ==================== Reference Data ====================

    @Nested
    @DisplayName("getCategories Tests")
    class GetCategoriesTests {

        @Test
        @DisplayName("should return all categories")
        void shouldReturnAllCategories() {
            List<String> categories = service.getCategories();

            assertThat(categories).contains("Weapon", "Armor", "Wondrous Item", "Ring",
                    "Amulet", "Rod", "Staff", "Wand");
        }
    }

    @Nested
    @DisplayName("getRarities Tests")
    class GetRaritiesTests {

        @Test
        @DisplayName("should return all rarities")
        void shouldReturnAllRarities() {
            List<String> rarities = service.getRarities();

            assertThat(rarities).contains("Uncommon", "Rare", "Very Rare", "Legendary", "Artifact");
        }
    }

    // ==================== Population ====================

    @Nested
    @DisplayName("populateGrowingItems Tests")
    class PopulateGrowingItemsTests {

        @Test
        @DisplayName("should populate growing items")
        void shouldPopulateGrowingItems() {
            when(growingItemRepository.save(any(Dnd5eGrowingMagicItem.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            service.populateGrowingItems();

            verify(growingItemRepository, times(8)).save(any(Dnd5eGrowingMagicItem.class));
        }
    }

    // ==================== Initialization ====================

    @Nested
    @DisplayName("initializeGrowingItemsDatabase Tests")
    class InitializeGrowingItemsDatabaseTests {

        @Test
        @DisplayName("should skip if already initialized")
        void shouldSkipIfAlreadyInitialized() {
            when(growingItemRepository.count()).thenReturn(0L);
            when(growingItemRepository.save(any(Dnd5eGrowingMagicItem.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // First call initializes
            service.initializeGrowingItemsDatabase();
            // Second call should skip due to initialized flag
            service.initializeGrowingItemsDatabase();

            verify(growingItemRepository, times(8)).save(any(Dnd5eGrowingMagicItem.class));
        }

        @Test
        @DisplayName("should skip if already populated")
        void shouldSkipIfAlreadyPopulated() {
            when(growingItemRepository.count()).thenReturn(10L);

            service.initializeGrowingItemsDatabase();

            verify(growingItemRepository, times(0)).save(any(Dnd5eGrowingMagicItem.class));
        }

        @Test
        @DisplayName("should populate if missing")
        void shouldPopulateIfMissing() {
            when(growingItemRepository.count()).thenReturn(0L);
            when(growingItemRepository.save(any(Dnd5eGrowingMagicItem.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            service.initializeGrowingItemsDatabase();

            verify(growingItemRepository, times(8)).save(any(Dnd5eGrowingMagicItem.class));
        }
    }
}
