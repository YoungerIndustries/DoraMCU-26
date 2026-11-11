package com.yisy.modules.doramcu.dnd5e.service;

import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCharacter;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCharacterMagicItem;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eMagicItem;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5eCharacterMagicItemRepository;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5eCharacterRepository;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5eMagicItemRepository;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eMagicItemService.UseItemResult;
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
@DisplayName("Dnd5eMagicItemService Tests")
class Dnd5eMagicItemServiceTest {

    @Mock
    private Dnd5eMagicItemRepository magicItemRepository;

    @Mock
    private Dnd5eCharacterMagicItemRepository charItemRepository;

    @Mock
    private Dnd5eCharacterRepository characterRepository;

    @InjectMocks
    private Dnd5eMagicItemService service;

    // ==================== Helper Methods ====================

    private Dnd5eMagicItem createItem(String name, String category, String rarity) {
        Dnd5eMagicItem item = new Dnd5eMagicItem();
        item.setId(1L);
        item.setName(name);
        item.setCategory(category);
        item.setRarity(rarity);
        item.setDescription("Test item");
        item.setRequiresAttunement(false);
        item.setHasCharges(false);
        item.setCursed(false);
        item.setActive(true);
        return item;
    }

    private Dnd5eMagicItem createChargedItem(String name, int maxCharges, String rechargeRule) {
        Dnd5eMagicItem item = createItem(name, "Wondrous Item", "Rare");
        item.setHasCharges(true);
        item.setMaxCharges(maxCharges);
        item.setRechargeRule(rechargeRule);
        if ("Dawn".equals(rechargeRule)) {
            item.setRechargeDc(5);
        }
        return item;
    }

    private Dnd5eMagicItem createAttunementItem(String name) {
        Dnd5eMagicItem item = createItem(name, "Wondrous Item", "Rare");
        item.setRequiresAttunement(true);
        return item;
    }

    private Dnd5eMagicItem createCursedItem(String name) {
        Dnd5eMagicItem item = createItem(name, "Ring", "Uncommon");
        item.setCursed(true);
        item.setCurseDescription("You cannot cast spells.");
        item.setRequiresAttunement(true);
        return item;
    }

    private Dnd5eCharacterMagicItem createBinding(Long characterId, Long magicItemId, boolean attuned, Integer charges) {
        Dnd5eCharacterMagicItem binding = new Dnd5eCharacterMagicItem();
        binding.setId(1L);
        binding.setCharacterId(characterId);
        binding.setMagicItemId(magicItemId);
        binding.setCharacterName("TestChar");
        binding.setItemName("TestItem");
        binding.setAttuned(attuned);
        binding.setChargesRemaining(charges);
        binding.setActive(true);
        binding.setUseCount(0);
        return binding;
    }

    private Dnd5eCharacter createCharacter(Long id, String name) {
        Dnd5eCharacter character = new Dnd5eCharacter();
        character.setId(id);
        character.setName(name);
        character.setLevel(5);
        return character;
    }

    // ==================== Magic Item CRUD ====================

    @Nested
    @DisplayName("createMagicItem Tests")
    class CreateMagicItemTests {

        @Test
        @DisplayName("should create and save a magic item")
        void shouldCreateAndSaveMagicItem() {
            Dnd5eMagicItem item = createItem("Bag of Holding", "Wondrous Item", "Common");
            when(magicItemRepository.save(any())).thenReturn(item);

            Dnd5eMagicItem result = service.createMagicItem(item);

            assertThat(result.getName()).isEqualTo("Bag of Holding");
            verify(magicItemRepository).save(item);
        }
    }

    @Nested
    @DisplayName("getMagicItem Tests")
    class GetMagicItemTests {

        @Test
        @DisplayName("should return item by ID")
        void shouldReturnItemById() {
            Dnd5eMagicItem item = createItem("Bag of Holding", "Wondrous Item", "Common");
            when(magicItemRepository.findById(1L)).thenReturn(Optional.of(item));

            Optional<Dnd5eMagicItem> result = service.getMagicItem(1L);

            assertThat(result).isPresent();
            assertThat(result.get().getName()).isEqualTo("Bag of Holding");
        }

        @Test
        @DisplayName("should return empty when not found")
        void shouldReturnEmptyWhenNotFound() {
            when(magicItemRepository.findById(999L)).thenReturn(Optional.empty());

            Optional<Dnd5eMagicItem> result = service.getMagicItem(999L);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getMagicItemByName Tests")
    class GetMagicItemByNameTests {

        @Test
        @DisplayName("should return item by name")
        void shouldReturnItemByName() {
            Dnd5eMagicItem item = createItem("Bag of Holding", "Wondrous Item", "Common");
            when(magicItemRepository.findByName("Bag of Holding")).thenReturn(Optional.of(item));

            Optional<Dnd5eMagicItem> result = service.getMagicItemByName("Bag of Holding");

            assertThat(result).isPresent();
            assertThat(result.get().getRarity()).isEqualTo("Common");
        }

        @Test
        @DisplayName("should return empty when not found")
        void shouldReturnEmptyWhenNotFound() {
            when(magicItemRepository.findByName("Nonexistent")).thenReturn(Optional.empty());

            Optional<Dnd5eMagicItem> result = service.getMagicItemByName("Nonexistent");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("listMagicItems Tests")
    class ListMagicItemsTests {

        @Test
        @DisplayName("should return all items")
        void shouldReturnAllItems() {
            List<Dnd5eMagicItem> items = Arrays.asList(createItem("Item1", "Weapon", "Rare"), createItem("Item2", "Armor", "Uncommon"));
            when(magicItemRepository.findAll()).thenReturn(items);

            List<Dnd5eMagicItem> result = service.listMagicItems();

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("should return empty list when no items")
        void shouldReturnEmptyListWhenNoItems() {
            when(magicItemRepository.findAll()).thenReturn(Collections.emptyList());

            List<Dnd5eMagicItem> result = service.listMagicItems();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("listMagicItemsByRarity Tests")
    class ListMagicItemsByRarityTests {

        @Test
        @DisplayName("should return items by rarity")
        void shouldReturnItemsByRarity() {
            List<Dnd5eMagicItem> items = Arrays.asList(createItem("Item1", "Weapon", "Rare"));
            when(magicItemRepository.findByRarityAndActiveTrue("Rare")).thenReturn(items);

            List<Dnd5eMagicItem> result = service.listMagicItemsByRarity("Rare");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getRarity()).isEqualTo("Rare");
        }
    }

    @Nested
    @DisplayName("listMagicItemsByCategory Tests")
    class ListMagicItemsByCategoryTests {

        @Test
        @DisplayName("should return items by category")
        void shouldReturnItemsByCategory() {
            List<Dnd5eMagicItem> items = Arrays.asList(createItem("Item1", "Weapon", "Rare"));
            when(magicItemRepository.findByCategoryAndActiveTrue("Weapon")).thenReturn(items);

            List<Dnd5eMagicItem> result = service.listMagicItemsByCategory("Weapon");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getCategory()).isEqualTo("Weapon");
        }
    }

    @Nested
    @DisplayName("listAttunementItems Tests")
    class ListAttunementItemsTests {

        @Test
        @DisplayName("should return items requiring attunement")
        void shouldReturnAttunementItems() {
            Dnd5eMagicItem item = createAttunementItem("Ring of Protection");
            when(magicItemRepository.findByRequiresAttunementTrue()).thenReturn(List.of(item));

            List<Dnd5eMagicItem> result = service.listAttunementItems();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).isRequiresAttunement()).isTrue();
        }
    }

    @Nested
    @DisplayName("listChargedItems Tests")
    class ListChargedItemsTests {

        @Test
        @DisplayName("should return items with charges")
        void shouldReturnChargedItems() {
            Dnd5eMagicItem item = createChargedItem("Staff of Power", 10, "Dawn");
            when(magicItemRepository.findByHasChargesTrue()).thenReturn(List.of(item));

            List<Dnd5eMagicItem> result = service.listChargedItems();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).isHasCharges()).isTrue();
        }
    }

    @Nested
    @DisplayName("listCursedItems Tests")
    class ListCursedItemsTests {

        @Test
        @DisplayName("should return cursed items")
        void shouldReturnCursedItems() {
            Dnd5eMagicItem item = createCursedItem("Cursed Ring");
            when(magicItemRepository.findByCursedTrue()).thenReturn(List.of(item));

            List<Dnd5eMagicItem> result = service.listCursedItems();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).isCursed()).isTrue();
        }
    }

    @Nested
    @DisplayName("searchMagicItems Tests")
    class SearchMagicItemsTests {

        @Test
        @DisplayName("should return items matching keyword")
        void shouldReturnItemsMatchingKeyword() {
            Dnd5eMagicItem item = createItem("Bag of Holding", "Wondrous Item", "Common");
            when(magicItemRepository.searchByKeyword("Bag")).thenReturn(List.of(item));

            List<Dnd5eMagicItem> result = service.searchMagicItems("Bag");

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("should return empty when no match")
        void shouldReturnEmptyWhenNoMatch() {
            when(magicItemRepository.searchByKeyword("Nonexistent")).thenReturn(Collections.emptyList());

            List<Dnd5eMagicItem> result = service.searchMagicItems("Nonexistent");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("updateMagicItem Tests")
    class UpdateMagicItemTests {

        @Test
        @DisplayName("should update an item")
        void shouldUpdateItem() {
            Dnd5eMagicItem existing = createItem("Old Name", "Weapon", "Common");
            Dnd5eMagicItem updated = createItem("New Name", "Armor", "Rare");
            when(magicItemRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(magicItemRepository.save(any())).thenReturn(existing);

            Dnd5eMagicItem result = service.updateMagicItem(1L, updated);

            assertThat(result.getName()).isEqualTo("New Name");
            verify(magicItemRepository).save(existing);
        }

        @Test
        @DisplayName("should throw when item not found")
        void shouldThrowWhenItemNotFound() {
            when(magicItemRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.updateMagicItem(999L, createItem("X", "Y", "Z")))
                    .isInstanceOf(Exception.class);
        }
    }

    @Nested
    @DisplayName("deleteMagicItem Tests")
    class DeleteMagicItemTests {

        @Test
        @DisplayName("should delete an item")
        void shouldDeleteItem() {
            service.deleteMagicItem(1L);
            verify(magicItemRepository).deleteById(1L);
        }
    }

    // ==================== Character-Item Binding ====================

    @Nested
    @DisplayName("giveItemToCharacter Tests")
    class GiveItemToCharacterTests {

        @Test
        @DisplayName("should give item to character")
        void shouldGiveItemToCharacter() {
            Dnd5eCharacter character = createCharacter(1L, "TestChar");
            Dnd5eMagicItem item = createItem("Bag of Holding", "Wondrous Item", "Common");
            when(characterRepository.findById(1L)).thenReturn(Optional.of(character));
            when(magicItemRepository.findById(1L)).thenReturn(Optional.of(item));
            when(charItemRepository.existsByCharacterIdAndMagicItemId(1L, 1L)).thenReturn(false);
            when(charItemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Dnd5eCharacterMagicItem result = service.giveItemToCharacter(1L, 1L);

            assertThat(result.getCharacterName()).isEqualTo("TestChar");
            assertThat(result.getItemName()).isEqualTo("Bag of Holding");
            assertThat(result.isActive()).isTrue();
        }

        @Test
        @DisplayName("should auto-attune if item requires attunement")
        void shouldAutoAttuneIfRequiresAttunement() {
            Dnd5eCharacter character = createCharacter(1L, "TestChar");
            Dnd5eMagicItem item = createAttunementItem("Ring of Protection");
            when(characterRepository.findById(1L)).thenReturn(Optional.of(character));
            when(magicItemRepository.findById(1L)).thenReturn(Optional.of(item));
            when(charItemRepository.existsByCharacterIdAndMagicItemId(1L, 1L)).thenReturn(false);
            when(charItemRepository.countActiveAttunedByCharacter(1L)).thenReturn(0);
            when(charItemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Dnd5eCharacterMagicItem result = service.giveItemToCharacter(1L, 1L);

            assertThat(result.isAttuned()).isTrue();
        }

        @Test
        @DisplayName("should set charges for charged items")
        void shouldSetChargesForChargedItems() {
            Dnd5eCharacter character = createCharacter(1L, "TestChar");
            Dnd5eMagicItem item = createChargedItem("Staff of Power", 10, "Dawn");
            when(characterRepository.findById(1L)).thenReturn(Optional.of(character));
            when(magicItemRepository.findById(1L)).thenReturn(Optional.of(item));
            when(charItemRepository.existsByCharacterIdAndMagicItemId(1L, 1L)).thenReturn(false);
            when(charItemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Dnd5eCharacterMagicItem result = service.giveItemToCharacter(1L, 1L);

            assertThat(result.getChargesRemaining()).isEqualTo(10);
        }

        @Test
        @DisplayName("should throw if character already has item")
        void shouldThrowIfCharacterAlreadyHasItem() {
            when(characterRepository.findById(1L)).thenReturn(Optional.of(createCharacter(1L, "TestChar")));
            when(magicItemRepository.findById(1L)).thenReturn(Optional.of(createItem("Item", "Wondrous Item", "Common")));
            when(charItemRepository.existsByCharacterIdAndMagicItemId(1L, 1L)).thenReturn(true);

            assertThatThrownBy(() -> service.giveItemToCharacter(1L, 1L))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("should throw if character not found")
        void shouldThrowIfCharacterNotFound() {
            when(characterRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.giveItemToCharacter(999L, 1L))
                    .isInstanceOf(Exception.class);
        }
    }

    @Nested
    @DisplayName("listCharacterItems Tests")
    class ListCharacterItemsTests {

        @Test
        @DisplayName("should return character's items")
        void shouldReturnCharactersItems() {
            List<Dnd5eCharacterMagicItem> items = Arrays.asList(
                    createBinding(1L, 1L, true, null),
                    createBinding(1L, 2L, false, 5)
            );
            when(charItemRepository.findByCharacterIdAndActiveTrue(1L)).thenReturn(items);

            List<Dnd5eCharacterMagicItem> result = service.listCharacterItems(1L);

            assertThat(result).hasSize(2);
        }
    }

    @Nested
    @DisplayName("removeItemFromCharacter Tests")
    class RemoveItemFromCharacterTests {

        @Test
        @DisplayName("should remove item from character")
        void shouldRemoveItemFromCharacter() {
            Dnd5eCharacterMagicItem binding = createBinding(1L, 1L, true, null);
            when(charItemRepository.findByCharacterIdAndMagicItemId(1L, 1L)).thenReturn(Optional.of(binding));
            when(charItemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            service.removeItemFromCharacter(1L, 1L);

            assertThat(binding.isActive()).isFalse();
            assertThat(binding.isAttuned()).isFalse();
        }
    }

    // ==================== Attunement Management ====================

    @Nested
    @DisplayName("attuneToItem Tests")
    class AttuneToItemTests {

        @Test
        @DisplayName("should attune to item")
        void shouldAttuneToItem() {
            Dnd5eCharacterMagicItem binding = createBinding(1L, 1L, false, null);
            Dnd5eMagicItem item = createAttunementItem("Ring of Protection");
            when(charItemRepository.findByCharacterIdAndMagicItemId(1L, 1L)).thenReturn(Optional.of(binding));
            when(magicItemRepository.findById(1L)).thenReturn(Optional.of(item));
            when(charItemRepository.countActiveAttunedByCharacter(1L)).thenReturn(1);
            when(charItemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Dnd5eCharacterMagicItem result = service.attuneToItem(1L, 1L);

            assertThat(result.isAttuned()).isTrue();
        }

        @Test
        @DisplayName("should throw if already attuned")
        void shouldThrowIfAlreadyAttuned() {
            Dnd5eCharacterMagicItem binding = createBinding(1L, 1L, true, null);
            Dnd5eMagicItem item = createAttunementItem("Ring of Protection");
            when(charItemRepository.findByCharacterIdAndMagicItemId(1L, 1L)).thenReturn(Optional.of(binding));
            when(magicItemRepository.findById(1L)).thenReturn(Optional.of(item));

            assertThatThrownBy(() -> service.attuneToItem(1L, 1L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("already attuned");
        }

        @Test
        @DisplayName("should throw if attunement limit reached")
        void shouldThrowIfAttunementLimitReached() {
            Dnd5eCharacterMagicItem binding = createBinding(1L, 1L, false, null);
            Dnd5eMagicItem item = createAttunementItem("Ring of Protection");
            when(charItemRepository.findByCharacterIdAndMagicItemId(1L, 1L)).thenReturn(Optional.of(binding));
            when(magicItemRepository.findById(1L)).thenReturn(Optional.of(item));
            when(charItemRepository.countActiveAttunedByCharacter(1L)).thenReturn(3);

            assertThatThrownBy(() -> service.attuneToItem(1L, 1L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("maximum");
        }

        @Test
        @DisplayName("should throw if item does not require attunement")
        void shouldThrowIfItemDoesNotRequireAttunement() {
            Dnd5eCharacterMagicItem binding = createBinding(1L, 1L, false, null);
            Dnd5eMagicItem item = createItem("Bag of Holding", "Wondrous Item", "Common");
            when(charItemRepository.findByCharacterIdAndMagicItemId(1L, 1L)).thenReturn(Optional.of(binding));
            when(magicItemRepository.findById(1L)).thenReturn(Optional.of(item));

            assertThatThrownBy(() -> service.attuneToItem(1L, 1L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("does not require attunement");
        }
    }

    @Nested
    @DisplayName("deattuneFromItem Tests")
    class DeattuneFromItemTests {

        @Test
        @DisplayName("should de-attune from item")
        void shouldDeattuneFromItem() {
            Dnd5eCharacterMagicItem binding = createBinding(1L, 1L, true, null);
            when(charItemRepository.findByCharacterIdAndMagicItemId(1L, 1L)).thenReturn(Optional.of(binding));
            when(charItemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Dnd5eCharacterMagicItem result = service.deattuneFromItem(1L, 1L);

            assertThat(result.isAttuned()).isFalse();
        }

        @Test
        @DisplayName("should throw if not attuned")
        void shouldThrowIfNotAttuned() {
            Dnd5eCharacterMagicItem binding = createBinding(1L, 1L, false, null);
            when(charItemRepository.findByCharacterIdAndMagicItemId(1L, 1L)).thenReturn(Optional.of(binding));

            assertThatThrownBy(() -> service.deattuneFromItem(1L, 1L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not attuned");
        }
    }

    @Nested
    @DisplayName("canAttuneMore Tests")
    class CanAttuneMoreTests {

        @Test
        @DisplayName("should return true when under limit")
        void shouldReturnTrueWhenUnderLimit() {
            when(charItemRepository.countActiveAttunedByCharacter(1L)).thenReturn(2);

            boolean result = service.canAttuneMore(1L);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("should return false when at limit")
        void shouldReturnFalseWhenAtLimit() {
            when(charItemRepository.countActiveAttunedByCharacter(1L)).thenReturn(3);

            boolean result = service.canAttuneMore(1L);

            assertThat(result).isFalse();
        }
    }

    // ==================== Charge Management ====================

    @Nested
    @DisplayName("useItem Tests")
    class UseItemTests {

        @Test
        @DisplayName("should use item without charges")
        void shouldUseItemWithoutCharges() {
            Dnd5eCharacterMagicItem binding = createBinding(1L, 1L, false, null);
            Dnd5eMagicItem item = createItem("Bag of Tricks", "Wondrous Item", "Uncommon");
            when(charItemRepository.findByCharacterIdAndMagicItemId(1L, 1L)).thenReturn(Optional.of(binding));
            when(magicItemRepository.findById(1L)).thenReturn(Optional.of(item));
            when(charItemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            UseItemResult result = service.useItem(1L, 1L, 0);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getMessage()).contains("no charges expended");
        }

        @Test
        @DisplayName("should use item with charges")
        void shouldUseItemWithCharges() {
            Dnd5eCharacterMagicItem binding = createBinding(1L, 1L, true, 10);
            Dnd5eMagicItem item = createChargedItem("Staff of Power", 10, "Dawn");
            when(charItemRepository.findByCharacterIdAndMagicItemId(1L, 1L)).thenReturn(Optional.of(binding));
            when(magicItemRepository.findById(1L)).thenReturn(Optional.of(item));
            when(charItemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            UseItemResult result = service.useItem(1L, 1L, 2);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getChargesRemaining()).isEqualTo(8);
            assertThat(result.getChargesExpended()).isEqualTo(2);
        }

        @Test
        @DisplayName("should fail if not enough charges")
        void shouldFailIfNotEnoughCharges() {
            Dnd5eCharacterMagicItem binding = createBinding(1L, 1L, true, 1);
            Dnd5eMagicItem item = createChargedItem("Staff of Power", 10, "Dawn");
            when(charItemRepository.findByCharacterIdAndMagicItemId(1L, 1L)).thenReturn(Optional.of(binding));
            when(magicItemRepository.findById(1L)).thenReturn(Optional.of(item));

            UseItemResult result = service.useItem(1L, 1L, 5);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getMessage()).contains("Not enough charges");
        }

        @Test
        @DisplayName("should throw if not attuned to attunement item")
        void shouldThrowIfNotAttunedToAttunementItem() {
            Dnd5eCharacterMagicItem binding = createBinding(1L, 1L, false, null);
            Dnd5eMagicItem item = createAttunementItem("Ring of Protection");
            when(charItemRepository.findByCharacterIdAndMagicItemId(1L, 1L)).thenReturn(Optional.of(binding));
            when(magicItemRepository.findById(1L)).thenReturn(Optional.of(item));

            assertThatThrownBy(() -> service.useItem(1L, 1L, 0))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("must be attuned");
        }

        @Test
        @DisplayName("should increment use count")
        void shouldIncrementUseCount() {
            Dnd5eCharacterMagicItem binding = createBinding(1L, 1L, false, null);
            binding.setUseCount(5);
            Dnd5eMagicItem item = createItem("Bag of Tricks", "Wondrous Item", "Uncommon");
            when(charItemRepository.findByCharacterIdAndMagicItemId(1L, 1L)).thenReturn(Optional.of(binding));
            when(magicItemRepository.findById(1L)).thenReturn(Optional.of(item));
            when(charItemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            service.useItem(1L, 1L, 0);

            assertThat(binding.getUseCount()).isEqualTo(6);
        }
    }

    @Nested
    @DisplayName("rechargeAfterShortRest Tests")
    class RechargeAfterShortRestTests {

        @Test
        @DisplayName("should recharge short rest items")
        void shouldRechargeShortRestItems() {
            Dnd5eCharacterMagicItem binding = createBinding(1L, 1L, true, 3);
            Dnd5eMagicItem item = createChargedItem("Chime of Opening", 6, "Short Rest");
            when(charItemRepository.findByCharacterIdAndActiveTrue(1L)).thenReturn(List.of(binding));
            when(magicItemRepository.findById(1L)).thenReturn(Optional.of(item));
            when(charItemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            int recharged = service.rechargeAfterShortRest(1L);

            assertThat(recharged).isEqualTo(1);
            assertThat(binding.getChargesRemaining()).isEqualTo(6);
        }

        @Test
        @DisplayName("should not recharge non-short-rest items")
        void shouldNotRechargeNonShortRestItems() {
            Dnd5eCharacterMagicItem binding = createBinding(1L, 1L, true, 3);
            Dnd5eMagicItem item = createChargedItem("Staff of Power", 10, "Dawn");
            when(charItemRepository.findByCharacterIdAndActiveTrue(1L)).thenReturn(List.of(binding));
            when(magicItemRepository.findById(1L)).thenReturn(Optional.of(item));

            int recharged = service.rechargeAfterShortRest(1L);

            assertThat(recharged).isEqualTo(0);
            assertThat(binding.getChargesRemaining()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("rechargeAfterLongRest Tests")
    class RechargeAfterLongRestTests {

        @Test
        @DisplayName("should recharge long rest items")
        void shouldRechargeLongRestItems() {
            Dnd5eCharacterMagicItem binding = createBinding(1L, 1L, true, 3);
            Dnd5eMagicItem item = createChargedItem("Chime of Opening", 6, "Long Rest");
            when(charItemRepository.findByCharacterIdAndActiveTrue(1L)).thenReturn(List.of(binding));
            when(magicItemRepository.findById(1L)).thenReturn(Optional.of(item));
            when(charItemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            int recharged = service.rechargeAfterLongRest(1L);

            assertThat(recharged).isEqualTo(1);
            assertThat(binding.getChargesRemaining()).isEqualTo(6);
        }
    }

    @Nested
    @DisplayName("rechargeAtDawn Tests")
    class RechargeAtDawnTests {

        @Test
        @DisplayName("should recharge dawn items")
        void shouldRechargeDawnItems() {
            Dnd5eCharacterMagicItem binding = createBinding(1L, 1L, true, 3);
            Dnd5eMagicItem item = createChargedItem("Staff of Power", 10, "Dawn");
            item.setRechargeDc(null); // No DC check needed
            when(charItemRepository.findByCharacterIdAndActiveTrue(1L)).thenReturn(List.of(binding));
            when(magicItemRepository.findById(1L)).thenReturn(Optional.of(item));
            when(charItemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            int recharged = service.rechargeAtDawn(1L);

            assertThat(recharged).isEqualTo(1);
        }
    }

    // ==================== Curse Management ====================

    @Nested
    @DisplayName("revealCurse Tests")
    class RevealCurseTests {

        @Test
        @DisplayName("should reveal curse")
        void shouldRevealCurse() {
            Dnd5eCharacterMagicItem binding = createBinding(1L, 1L, true, null);
            Dnd5eMagicItem item = createCursedItem("Cursed Ring");
            when(charItemRepository.findByCharacterIdAndMagicItemId(1L, 1L)).thenReturn(Optional.of(binding));
            when(magicItemRepository.findById(1L)).thenReturn(Optional.of(item));
            when(charItemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Dnd5eCharacterMagicItem result = service.revealCurse(1L, 1L);

            assertThat(result.isCurseRevealed()).isTrue();
        }

        @Test
        @DisplayName("should throw if item is not cursed")
        void shouldThrowIfItemIsNotCursed() {
            Dnd5eCharacterMagicItem binding = createBinding(1L, 1L, true, null);
            Dnd5eMagicItem item = createItem("Bag of Holding", "Wondrous Item", "Common");
            when(charItemRepository.findByCharacterIdAndMagicItemId(1L, 1L)).thenReturn(Optional.of(binding));
            when(magicItemRepository.findById(1L)).thenReturn(Optional.of(item));

            assertThatThrownBy(() -> service.revealCurse(1L, 1L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not cursed");
        }
    }

    @Nested
    @DisplayName("removeCurse Tests")
    class RemoveCurseTests {

        @Test
        @DisplayName("should remove curse if revealed")
        void shouldRemoveCurseIfRevealed() {
            Dnd5eCharacterMagicItem binding = createBinding(1L, 1L, true, null);
            binding.setCurseRevealed(true);
            when(charItemRepository.findByCharacterIdAndMagicItemId(1L, 1L)).thenReturn(Optional.of(binding));
            when(charItemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            boolean result = service.removeCurse(1L, 1L);

            assertThat(result).isTrue();
            assertThat(binding.isCurseRevealed()).isFalse();
        }

        @Test
        @DisplayName("should return false if curse not revealed")
        void shouldReturnFalseIfCurseNotRevealed() {
            Dnd5eCharacterMagicItem binding = createBinding(1L, 1L, true, null);
            binding.setCurseRevealed(false);
            when(charItemRepository.findByCharacterIdAndMagicItemId(1L, 1L)).thenReturn(Optional.of(binding));

            boolean result = service.removeCurse(1L, 1L);

            assertThat(result).isFalse();
        }
    }

    // ==================== Reference Data ====================

    @Nested
    @DisplayName("listAllRarities Tests")
    class ListAllRaritiesTests {

        @Test
        @DisplayName("should return all rarities")
        void shouldReturnAllRarities() {
            when(magicItemRepository.findAllRarities()).thenReturn(Arrays.asList("Common", "Uncommon", "Rare", "Very Rare", "Legendary"));

            List<String> result = service.listAllRarities();

            assertThat(result).containsExactlyInAnyOrder("Common", "Uncommon", "Rare", "Very Rare", "Legendary");
        }
    }

    @Nested
    @DisplayName("listAllCategories Tests")
    class ListAllCategoriesTests {

        @Test
        @DisplayName("should return all categories")
        void shouldReturnAllCategories() {
            when(magicItemRepository.findAllCategories()).thenReturn(Arrays.asList("Weapon", "Armor", "Wondrous Item", "Ring"));

            List<String> result = service.listAllCategories();

            assertThat(result).containsExactlyInAnyOrder("Weapon", "Armor", "Wondrous Item", "Ring");
        }
    }

    // ==================== Initialization ====================

    @Nested
    @DisplayName("initializeMagicItemsDatabase Tests")
    class InitializeMagicItemsDatabaseTests {

        @Test
        @DisplayName("should skip if already populated")
        void shouldSkipIfAlreadyPopulated() {
            when(magicItemRepository.count()).thenReturn(30L);

            service.initializeMagicItemsDatabase();

            verify(magicItemRepository, never()).save(any());
        }

        @Test
        @DisplayName("should populate if missing")
        void shouldPopulateIfMissing() {
            when(magicItemRepository.count()).thenReturn(0L);
            when(magicItemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            service.initializeMagicItemsDatabase();

            verify(magicItemRepository, atLeastOnce()).save(any());
        }

        @Test
        @DisplayName("should skip if already initialized")
        void shouldSkipIfAlreadyInitialized() {
            when(magicItemRepository.count()).thenReturn(30L);
            service.initializeMagicItemsDatabase();
            service.initializeMagicItemsDatabase();

            verify(magicItemRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("populateMagicItems Tests")
    class PopulateMagicItemsTests {

        @Test
        @DisplayName("should populate items")
        void shouldPopulateItems() {
            when(magicItemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(magicItemRepository.count()).thenReturn(0L);

            service.populateMagicItems();

            verify(magicItemRepository, atLeast(20)).save(any());
        }
    }

    @Nested
    @DisplayName("countAttunedItems Tests")
    class CountAttunedItemsTests {

        @Test
        @DisplayName("should return attuned count")
        void shouldReturnAttunedCount() {
            when(charItemRepository.countActiveAttunedByCharacter(1L)).thenReturn(2);

            int count = service.countAttunedItems(1L);

            assertThat(count).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("listActiveMagicItems Tests")
    class ListActiveMagicItemsTests {

        @Test
        @DisplayName("should return active items")
        void shouldReturnActiveItems() {
            List<Dnd5eMagicItem> items = List.of(createItem("Item1", "Weapon", "Rare"));
            when(magicItemRepository.findByActiveTrue()).thenReturn(items);

            List<Dnd5eMagicItem> result = service.listActiveMagicItems();

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("getCharacterItem Tests")
    class GetCharacterItemTests {

        @Test
        @DisplayName("should return binding")
        void shouldReturnBinding() {
            Dnd5eCharacterMagicItem binding = createBinding(1L, 1L, true, null);
            when(charItemRepository.findByCharacterIdAndMagicItemId(1L, 1L)).thenReturn(Optional.of(binding));

            Optional<Dnd5eCharacterMagicItem> result = service.getCharacterItem(1L, 1L);

            assertThat(result).isPresent();
            assertThat(result.get().isAttuned()).isTrue();
        }

        @Test
        @DisplayName("should return empty if not found")
        void shouldReturnEmptyIfNotFound() {
            when(charItemRepository.findByCharacterIdAndMagicItemId(1L, 999L)).thenReturn(Optional.empty());

            Optional<Dnd5eCharacterMagicItem> result = service.getCharacterItem(1L, 999L);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("listAttunedItems Tests")
    class ListAttunedItemsTests {

        @Test
        @DisplayName("should return attuned items")
        void shouldReturnAttunedItems() {
            List<Dnd5eCharacterMagicItem> items = List.of(createBinding(1L, 1L, true, null));
            when(charItemRepository.findActiveAttunedByCharacter(1L)).thenReturn(items);

            List<Dnd5eCharacterMagicItem> result = service.listAttunedItems(1L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).isAttuned()).isTrue();
        }
    }
}
