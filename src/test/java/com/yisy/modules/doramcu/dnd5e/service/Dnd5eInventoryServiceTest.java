package com.yisy.modules.doramcu.dnd5e.service;

import com.yisy.modules.doramcu.dnd5e.dto.CreateDnd5eInventoryItemRequest;
import com.yisy.modules.doramcu.dnd5e.dto.InventorySummaryResponse;
import com.yisy.modules.doramcu.dnd5e.dto.UpdateDnd5eInventoryItemRequest;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCharacter;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eInventoryItem;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5eCharacterRepository;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5eInventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Dnd5eInventoryService Tests")
class Dnd5eInventoryServiceTest {

    private Dnd5eInventoryRepository inventoryRepository;
    private Dnd5eCharacterRepository characterRepository;
    private Dnd5eInventoryService inventoryService;

    @BeforeEach
    void setUp() {
        inventoryRepository = mock(Dnd5eInventoryRepository.class);
        characterRepository = mock(Dnd5eCharacterRepository.class);
        inventoryService = new Dnd5eInventoryService(inventoryRepository, characterRepository);
    }

    // ---- createItem ----

    @Nested
    @DisplayName("createItem Tests")
    class CreateItemTests {

        @Test
        @DisplayName("should create an inventory item with all fields")
        void createItem_shouldCreateWithAllFields() {
            // Arrange
            when(characterRepository.findById(1L)).thenReturn(Optional.of(new Dnd5eCharacter()));
            CreateDnd5eInventoryItemRequest request = new CreateDnd5eInventoryItemRequest();
            request.setName("Longsword");
            request.setDescription("A standard steel longsword");
            request.setItemType("Weapon");
            request.setRarity("Common");
            request.setQuantity(1);
            request.setWeight(3.0);
            request.setValue(15);
            request.setEquipped(false);
            request.setAttuned(false);
            request.setIdentified(true);

            when(inventoryRepository.save(any(Dnd5eInventoryItem.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Dnd5eInventoryItem item = inventoryService.createItem(1L, request);

            // Assert
            assertNotNull(item);
            assertEquals("Longsword", item.getName());
            assertEquals("A standard steel longsword", item.getDescription());
            assertEquals("Weapon", item.getItemType());
            assertEquals("Common", item.getRarity());
            assertEquals(1, item.getQuantity());
            assertEquals(BigDecimal.valueOf(3.0), item.getWeight());
            assertEquals(15, item.getValue());
            assertFalse(item.isEquipped());
            assertFalse(item.isAttuned());
            assertTrue(item.isIdentified());
            assertEquals(1L, item.getCharacterId());
            verify(inventoryRepository).save(item);
        }

        @Test
        @DisplayName("should default rarity to Common when null")
        void createItem_shouldDefaultRarity() {
            // Arrange
            when(characterRepository.findById(1L)).thenReturn(Optional.of(new Dnd5eCharacter()));
            CreateDnd5eInventoryItemRequest request = new CreateDnd5eInventoryItemRequest();
            request.setName("Arrow");
            request.setItemType("Ammunition");
            request.setRarity(null);

            when(inventoryRepository.save(any(Dnd5eInventoryItem.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Dnd5eInventoryItem item = inventoryService.createItem(1L, request);

            // Assert
            assertEquals("Common", item.getRarity());
        }

        @Test
        @DisplayName("should clamp quantity to minimum 1")
        void createItem_shouldClampQuantity() {
            // Arrange
            when(characterRepository.findById(1L)).thenReturn(Optional.of(new Dnd5eCharacter()));
            CreateDnd5eInventoryItemRequest request = new CreateDnd5eInventoryItemRequest();
            request.setName("Arrow");
            request.setItemType("Ammunition");
            request.setQuantity(0);

            when(inventoryRepository.save(any(Dnd5eInventoryItem.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Dnd5eInventoryItem item = inventoryService.createItem(1L, request);

            // Assert
            assertEquals(1, item.getQuantity());
        }

        @Test
        @DisplayName("should clamp negative weight to 0")
        void createItem_shouldClampNegativeWeight() {
            // Arrange
            when(characterRepository.findById(1L)).thenReturn(Optional.of(new Dnd5eCharacter()));
            CreateDnd5eInventoryItemRequest request = new CreateDnd5eInventoryItemRequest();
            request.setName("Potion");
            request.setItemType("Potion");
            request.setWeight(-5.0);

            when(inventoryRepository.save(any(Dnd5eInventoryItem.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Dnd5eInventoryItem item = inventoryService.createItem(1L, request);

            // Assert
            assertEquals(0, item.getWeight().compareTo(BigDecimal.ZERO));
        }

        @Test
        @DisplayName("should clamp negative value to 0")
        void createItem_shouldClampNegativeValue() {
            // Arrange
            when(characterRepository.findById(1L)).thenReturn(Optional.of(new Dnd5eCharacter()));
            CreateDnd5eInventoryItemRequest request = new CreateDnd5eInventoryItemRequest();
            request.setName("Potion");
            request.setItemType("Potion");
            request.setValue(-10);

            when(inventoryRepository.save(any(Dnd5eInventoryItem.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Dnd5eInventoryItem item = inventoryService.createItem(1L, request);

            // Assert
            assertEquals(0, item.getValue());
        }

        @Test
        @DisplayName("should throw when character not found")
        void createItem_shouldThrowWhenCharacterNotFound() {
            // Arrange
            when(characterRepository.findById(999L)).thenReturn(Optional.empty());
            CreateDnd5eInventoryItemRequest request = new CreateDnd5eInventoryItemRequest();
            request.setName("Sword");
            request.setItemType("Weapon");

            // Act & Assert
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> inventoryService.createItem(999L, request));
            assertTrue(ex.getMessage().contains("Character not found"));
            assertTrue(ex.getMessage().contains("999"));
        }

        @Test
        @DisplayName("should create item with charges")
        void createItem_shouldCreateWithCharges() {
            // Arrange
            when(characterRepository.findById(1L)).thenReturn(Optional.of(new Dnd5eCharacter()));
            CreateDnd5eInventoryItemRequest request = new CreateDnd5eInventoryItemRequest();
            request.setName("Wand of Fireballs");
            request.setItemType("Wand");
            request.setRarity("Uncommon");
            request.setCharges(7);
            request.setMaxCharges(7);

            when(inventoryRepository.save(any(Dnd5eInventoryItem.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Dnd5eInventoryItem item = inventoryService.createItem(1L, request);

            // Assert
            assertEquals(7, item.getCharges());
            assertEquals(7, item.getMaxCharges());
        }

        @Test
        @DisplayName("should create armor item with AC property")
        void createItem_shouldCreateArmorWithAC() {
            // Arrange
            when(characterRepository.findById(1L)).thenReturn(Optional.of(new Dnd5eCharacter()));
            CreateDnd5eInventoryItemRequest request = new CreateDnd5eInventoryItemRequest();
            request.setName("Chain Mail");
            request.setItemType("Armor");
            request.setRarity("Common");
            request.setWeight(55.0);
            request.setValue(75);
            request.setPropertyAc(16);

            when(inventoryRepository.save(any(Dnd5eInventoryItem.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Dnd5eInventoryItem item = inventoryService.createItem(1L, request);

            // Assert
            assertEquals(16, item.getPropertyAc());
            assertEquals("Armor", item.getItemType());
        }
    }

    // ---- getItem ----

    @Nested
    @DisplayName("getItem Tests")
    class GetItemTests {

        @Test
        @DisplayName("should return item by id")
        void getItem_shouldReturnById() {
            // Arrange
            Dnd5eInventoryItem item = createItem(1L, "Longsword");
            when(inventoryRepository.findById(1L)).thenReturn(Optional.of(item));

            // Act
            Dnd5eInventoryItem result = inventoryService.getItem(1L);

            // Assert
            assertEquals("Longsword", result.getName());
        }

        @Test
        @DisplayName("should throw when item not found")
        void getItem_shouldThrowWhenNotFound() {
            // Arrange
            when(inventoryRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> inventoryService.getItem(999L));
            assertTrue(ex.getMessage().contains("Inventory item not found"));
            assertTrue(ex.getMessage().contains("999"));
        }
    }

    // ---- listItems ----

    @Nested
    @DisplayName("listItems Tests")
    class ListItemsTests {

        @Test
        @DisplayName("should return all items for a character")
        void listItems_shouldReturnAll() {
            // Arrange
            when(characterRepository.existsById(1L)).thenReturn(true);
            List<Dnd5eInventoryItem> items = List.of(
                    createItem(1L, "Longsword"),
                    createItem(2L, "Shield")
            );
            when(inventoryRepository.findByCharacterId(1L)).thenReturn(items);

            // Act
            List<Dnd5eInventoryItem> result = inventoryService.listItems(1L);

            // Assert
            assertEquals(2, result.size());
            verify(inventoryRepository).findByCharacterId(1L);
        }

        @Test
        @DisplayName("should return empty list when no items")
        void listItems_shouldReturnEmpty() {
            // Arrange
            when(characterRepository.existsById(1L)).thenReturn(true);
            when(inventoryRepository.findByCharacterId(1L)).thenReturn(List.of());

            // Act
            List<Dnd5eInventoryItem> result = inventoryService.listItems(1L);

            // Assert
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("should throw when character not found")
        void listItems_shouldThrowWhenCharacterNotFound() {
            // Arrange
            when(characterRepository.existsById(999L)).thenReturn(false);

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> inventoryService.listItems(999L));
        }
    }

    // ---- updateItem ----

    @Nested
    @DisplayName("updateItem Tests")
    class UpdateItemTests {

        @Test
        @DisplayName("should update item name")
        void updateItem_shouldUpdateName() {
            // Arrange
            Dnd5eInventoryItem existing = createItem(1L, "Old Sword");
            when(inventoryRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(inventoryRepository.save(any(Dnd5eInventoryItem.class))).thenAnswer(inv -> inv.getArgument(0));

            UpdateDnd5eInventoryItemRequest request = new UpdateDnd5eInventoryItemRequest();
            request.setName("New Sword");

            // Act
            Dnd5eInventoryItem result = inventoryService.updateItem(1L, request);

            // Assert
            assertEquals("New Sword", result.getName());
            verify(inventoryRepository).save(existing);
        }

        @Test
        @DisplayName("should update quantity with clamping to 0 minimum")
        void updateItem_shouldUpdateQuantity() {
            // Arrange
            Dnd5eInventoryItem existing = createItem(1L, "Arrow");
            existing.setQuantity(20);
            when(inventoryRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(inventoryRepository.save(any(Dnd5eInventoryItem.class))).thenAnswer(inv -> inv.getArgument(0));

            UpdateDnd5eInventoryItemRequest request = new UpdateDnd5eInventoryItemRequest();
            request.setQuantity(10);

            // Act
            Dnd5eInventoryItem result = inventoryService.updateItem(1L, request);

            // Assert
            assertEquals(10, result.getQuantity());
        }

        @Test
        @DisplayName("should clamp negative quantity to 0")
        void updateItem_shouldClampNegativeQuantity() {
            // Arrange
            Dnd5eInventoryItem existing = createItem(1L, "Arrow");
            existing.setQuantity(20);
            when(inventoryRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(inventoryRepository.save(any(Dnd5eInventoryItem.class))).thenAnswer(inv -> inv.getArgument(0));

            UpdateDnd5eInventoryItemRequest request = new UpdateDnd5eInventoryItemRequest();
            request.setQuantity(-5);

            // Act
            Dnd5eInventoryItem result = inventoryService.updateItem(1L, request);

            // Assert
            assertEquals(0, result.getQuantity());
        }

        @Test
        @DisplayName("should update rarity")
        void updateItem_shouldUpdateRarity() {
            // Arrange
            Dnd5eInventoryItem existing = createItem(1L, "Magic Sword");
            existing.setRarity("Common");
            when(inventoryRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(inventoryRepository.save(any(Dnd5eInventoryItem.class))).thenAnswer(inv -> inv.getArgument(0));

            UpdateDnd5eInventoryItemRequest request = new UpdateDnd5eInventoryItemRequest();
            request.setRarity("Rare");

            // Act
            Dnd5eInventoryItem result = inventoryService.updateItem(1L, request);

            // Assert
            assertEquals("Rare", result.getRarity());
        }

        @Test
        @DisplayName("should update charges")
        void updateItem_shouldUpdateCharges() {
            // Arrange
            Dnd5eInventoryItem existing = createItem(1L, "Wand");
            existing.setCharges(5);
            when(inventoryRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(inventoryRepository.save(any(Dnd5eInventoryItem.class))).thenAnswer(inv -> inv.getArgument(0));

            UpdateDnd5eInventoryItemRequest request = new UpdateDnd5eInventoryItemRequest();
            request.setCharges(3);

            // Act
            Dnd5eInventoryItem result = inventoryService.updateItem(1L, request);

            // Assert
            assertEquals(3, result.getCharges());
        }

        @Test
        @DisplayName("should skip null fields")
        void updateItem_shouldSkipNullFields() {
            // Arrange
            Dnd5eInventoryItem existing = createItem(1L, "Sword");
            existing.setRarity("Uncommon");
            existing.setQuantity(5);
            when(inventoryRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(inventoryRepository.save(any(Dnd5eInventoryItem.class))).thenAnswer(inv -> inv.getArgument(0));

            UpdateDnd5eInventoryItemRequest request = new UpdateDnd5eInventoryItemRequest();
            // All fields null

            // Act
            Dnd5eInventoryItem result = inventoryService.updateItem(1L, request);

            // Assert
            assertEquals("Uncommon", result.getRarity()); // unchanged
            assertEquals(5, result.getQuantity()); // unchanged
        }
    }

    // ---- deleteItem ----

    @Nested
    @DisplayName("deleteItem Tests")
    class DeleteItemTests {

        @Test
        @DisplayName("should delete item by id")
        void deleteItem_shouldDelete() {
            // Arrange
            when(inventoryRepository.existsById(1L)).thenReturn(true);

            // Act
            inventoryService.deleteItem(1L);

            // Assert
            verify(inventoryRepository).deleteById(1L);
        }

        @Test
        @DisplayName("should throw when item not found")
        void deleteItem_shouldThrowWhenNotFound() {
            // Arrange
            when(inventoryRepository.existsById(999L)).thenReturn(false);

            // Act & Assert
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> inventoryService.deleteItem(999L));
            assertTrue(ex.getMessage().contains("Inventory item not found"));
        }
    }

    // ---- equipItem ----

    @Nested
    @DisplayName("equipItem Tests")
    class EquipItemTests {

        @Test
        @DisplayName("should equip an item")
        void equipItem_shouldEquip() {
            // Arrange
            Dnd5eInventoryItem item = createItem(1L, "Longsword");
            item.setCharacterId(1L);
            item.setItemType("Weapon");
            when(inventoryRepository.findById(1L)).thenReturn(Optional.of(item));
            when(inventoryRepository.findByCharacterIdAndItemTypeAndEquippedTrue(1L, "Weapon"))
                    .thenReturn(List.of());
            when(inventoryRepository.save(any(Dnd5eInventoryItem.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Dnd5eInventoryItem result = inventoryService.equipItem(1L);

            // Assert
            assertTrue(result.isEquipped());
            verify(inventoryRepository).save(item);
        }

        @Test
        @DisplayName("should unequip other items of the same type")
        void equipItem_shouldUnequipSameType() {
            // Arrange
            Dnd5eInventoryItem newItem = createItem(2L, "Greatsword");
            newItem.setCharacterId(1L);
            newItem.setItemType("Weapon");
            Dnd5eInventoryItem oldItem = createItem(1L, "Longsword");
            oldItem.setCharacterId(1L);
            oldItem.setItemType("Weapon");
            oldItem.setEquipped(true);

            when(inventoryRepository.findById(2L)).thenReturn(Optional.of(newItem));
            when(inventoryRepository.findByCharacterIdAndItemTypeAndEquippedTrue(1L, "Weapon"))
                    .thenReturn(List.of(oldItem));
            when(inventoryRepository.save(any(Dnd5eInventoryItem.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Dnd5eInventoryItem result = inventoryService.equipItem(2L);

            // Assert
            assertTrue(result.isEquipped());
            assertFalse(oldItem.isEquipped());
        }
    }

    // ---- unequipItem ----

    @Nested
    @DisplayName("unequipItem Tests")
    class UnequipItemTests {

        @Test
        @DisplayName("should unequip an item")
        void unequipItem_shouldUnequip() {
            // Arrange
            Dnd5eInventoryItem item = createItem(1L, "Longsword");
            item.setEquipped(true);
            item.setCharacterId(1L);
            when(inventoryRepository.findById(1L)).thenReturn(Optional.of(item));
            when(inventoryRepository.save(any(Dnd5eInventoryItem.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Dnd5eInventoryItem result = inventoryService.unequipItem(1L);

            // Assert
            assertFalse(result.isEquipped());
        }
    }

    // ---- getEquippedItems ----

    @Nested
    @DisplayName("getEquippedItems Tests")
    class GetEquippedItemsTests {

        @Test
        @DisplayName("should return only equipped items")
        void getEquippedItems_shouldReturnEquipped() {
            // Arrange
            when(characterRepository.existsById(1L)).thenReturn(true);
            Dnd5eInventoryItem equipped = createItem(1L, "Longsword");
            equipped.setEquipped(true);
            when(inventoryRepository.findByCharacterIdAndEquippedTrue(1L)).thenReturn(List.of(equipped));

            // Act
            List<Dnd5eInventoryItem> result = inventoryService.getEquippedItems(1L);

            // Assert
            assertEquals(1, result.size());
            assertTrue(result.get(0).isEquipped());
        }
    }

    // ---- getItemsByType ----

    @Nested
    @DisplayName("getItemsByType Tests")
    class GetItemsByTypeTests {

        @Test
        @DisplayName("should return items filtered by type")
        void getItemsByType_shouldFilterByType() {
            // Arrange
            when(characterRepository.existsById(1L)).thenReturn(true);
            Dnd5eInventoryItem weapon = createItem(1L, "Longsword");
            weapon.setItemType("Weapon");
            when(inventoryRepository.findByCharacterIdAndItemType(1L, "Weapon"))
                    .thenReturn(List.of(weapon));

            // Act
            List<Dnd5eInventoryItem> result = inventoryService.getItemsByType(1L, "Weapon");

            // Assert
            assertEquals(1, result.size());
            assertEquals("Weapon", result.get(0).getItemType());
        }
    }

    // ---- attuneItem ----

    @Nested
    @DisplayName("attuneItem Tests")
    class AttuneItemTests {

        @Test
        @DisplayName("should attune an item")
        void attuneItem_shouldAttune() {
            // Arrange
            Dnd5eInventoryItem item = createItem(1L, "Amulet");
            item.setCharacterId(1L);
            when(inventoryRepository.findById(1L)).thenReturn(Optional.of(item));
            when(inventoryRepository.findByCharacterIdAndAttunedTrue(1L)).thenReturn(List.of());
            when(inventoryRepository.save(any(Dnd5eInventoryItem.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Dnd5eInventoryItem result = inventoryService.attuneItem(1L);

            // Assert
            assertTrue(result.isAttuned());
        }

        @Test
        @DisplayName("should throw when attunement limit reached")
        void attuneItem_shouldThrowWhenLimitReached() {
            // Arrange
            Dnd5eInventoryItem item = createItem(4L, "New Amulet");
            item.setCharacterId(1L);
            item.setAttuned(false);
            when(inventoryRepository.findById(4L)).thenReturn(Optional.of(item));

            List<Dnd5eInventoryItem> attuned = new ArrayList<>();
            attuned.add(createItem(1L, "Amulet 1"));
            attuned.add(createItem(2L, "Amulet 2"));
            attuned.add(createItem(3L, "Amulet 3"));
            when(inventoryRepository.findByCharacterIdAndAttunedTrue(1L)).thenReturn(attuned);

            // Act & Assert
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> inventoryService.attuneItem(4L));
            assertTrue(ex.getMessage().contains("3 attuned items"));
        }

        @Test
        @DisplayName("should allow re-attuning already attuned item")
        void attuneItem_shouldAllowReAttune() {
            // Arrange
            Dnd5eInventoryItem item = createItem(1L, "Amulet");
            item.setCharacterId(1L);
            item.setAttuned(true);
            when(inventoryRepository.findById(1L)).thenReturn(Optional.of(item));

            List<Dnd5eInventoryItem> attuned = new ArrayList<>();
            attuned.add(item);
            attuned.add(createItem(2L, "Amulet 2"));
            attuned.add(createItem(3L, "Amulet 3"));
            when(inventoryRepository.findByCharacterIdAndAttunedTrue(1L)).thenReturn(attuned);
            when(inventoryRepository.save(any(Dnd5eInventoryItem.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act - should not throw since item is already attuned
            Dnd5eInventoryItem result = inventoryService.attuneItem(1L);

            // Assert
            assertTrue(result.isAttuned());
        }
    }

    // ---- unattuneItem ----

    @Nested
    @DisplayName("unattuneItem Tests")
    class UnattuneItemTests {

        @Test
        @DisplayName("should unattune an item")
        void unattuneItem_shouldUnattune() {
            // Arrange
            Dnd5eInventoryItem item = createItem(1L, "Amulet");
            item.setAttuned(true);
            item.setCharacterId(1L);
            when(inventoryRepository.findById(1L)).thenReturn(Optional.of(item));
            when(inventoryRepository.save(any(Dnd5eInventoryItem.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Dnd5eInventoryItem result = inventoryService.unattuneItem(1L);

            // Assert
            assertFalse(result.isAttuned());
        }
    }

    // ---- getAttunedItems ----

    @Nested
    @DisplayName("getAttunedItems Tests")
    class GetAttunedItemsTests {

        @Test
        @DisplayName("should return only attuned items")
        void getAttunedItems_shouldReturnAttuned() {
            // Arrange
            when(characterRepository.existsById(1L)).thenReturn(true);
            Dnd5eInventoryItem attuned = createItem(1L, "Amulet");
            attuned.setAttuned(true);
            when(inventoryRepository.findByCharacterIdAndAttunedTrue(1L)).thenReturn(List.of(attuned));

            // Act
            List<Dnd5eInventoryItem> result = inventoryService.getAttunedItems(1L);

            // Assert
            assertEquals(1, result.size());
            assertTrue(result.get(0).isAttuned());
        }
    }

    // ---- addItemQuantity ----

    @Nested
    @DisplayName("addItemQuantity Tests")
    class AddItemQuantityTests {

        @Test
        @DisplayName("should add to item quantity")
        void addItemQuantity_shouldAdd() {
            // Arrange
            Dnd5eInventoryItem item = createItem(1L, "Arrow");
            item.setQuantity(20);
            when(inventoryRepository.findById(1L)).thenReturn(Optional.of(item));
            when(inventoryRepository.save(any(Dnd5eInventoryItem.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Dnd5eInventoryItem result = inventoryService.addItemQuantity(1L, 10);

            // Assert
            assertEquals(30, result.getQuantity());
        }

        @Test
        @DisplayName("should clamp quantity to 0 minimum")
        void addItemQuantity_shouldClampToZero() {
            // Arrange
            Dnd5eInventoryItem item = createItem(1L, "Arrow");
            item.setQuantity(5);
            when(inventoryRepository.findById(1L)).thenReturn(Optional.of(item));
            when(inventoryRepository.save(any(Dnd5eInventoryItem.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Dnd5eInventoryItem result = inventoryService.addItemQuantity(1L, -10);

            // Assert
            assertEquals(0, result.getQuantity());
        }
    }

    // ---- consumeItem ----

    @Nested
    @DisplayName("consumeItem Tests")
    class ConsumeItemTests {

        @Test
        @DisplayName("should consume item quantity")
        void consumeItem_shouldConsume() {
            // Arrange
            Dnd5eInventoryItem item = createItem(1L, "Arrow");
            item.setQuantity(20);
            when(inventoryRepository.findById(1L)).thenReturn(Optional.of(item));
            when(inventoryRepository.save(any(Dnd5eInventoryItem.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Dnd5eInventoryItem result = inventoryService.consumeItem(1L, 5);

            // Assert
            assertEquals(15, result.getQuantity());
        }

        @Test
        @DisplayName("should delete item when quantity reaches 0")
        void consumeItem_shouldDeleteWhenZero() {
            // Arrange
            Dnd5eInventoryItem item = createItem(1L, "Arrow");
            item.setQuantity(5);
            when(inventoryRepository.findById(1L)).thenReturn(Optional.of(item));
            when(inventoryRepository.save(any(Dnd5eInventoryItem.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            inventoryService.consumeItem(1L, 5);

            // Assert
            verify(inventoryRepository).delete(item);
        }

        @Test
        @DisplayName("should clamp quantity to 0 when consuming more than available")
        void consumeItem_shouldClampToZero() {
            // Arrange
            Dnd5eInventoryItem item = createItem(1L, "Arrow");
            item.setQuantity(3);
            when(inventoryRepository.findById(1L)).thenReturn(Optional.of(item));
            when(inventoryRepository.save(any(Dnd5eInventoryItem.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            inventoryService.consumeItem(1L, 10);

            // Assert
            verify(inventoryRepository).delete(item);
        }
    }

    // ---- useCharge ----

    @Nested
    @DisplayName("useCharge Tests")
    class UseChargeTests {

        @Test
        @DisplayName("should use one charge")
        void useCharge_shouldUseOne() {
            // Arrange
            Dnd5eInventoryItem item = createItem(1L, "Wand");
            item.setCharges(5);
            when(inventoryRepository.findById(1L)).thenReturn(Optional.of(item));
            when(inventoryRepository.save(any(Dnd5eInventoryItem.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Dnd5eInventoryItem result = inventoryService.useCharge(1L);

            // Assert
            assertEquals(4, result.getCharges());
        }

        @Test
        @DisplayName("should throw when no charges")
        void useCharge_shouldThrowWhenNoCharges() {
            // Arrange
            Dnd5eInventoryItem item = createItem(1L, "Wand");
            item.setCharges(0);
            when(inventoryRepository.findById(1L)).thenReturn(Optional.of(item));

            // Act & Assert
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> inventoryService.useCharge(1L));
            assertTrue(ex.getMessage().contains("no charges"));
        }

        @Test
        @DisplayName("should throw when charges is null")
        void useCharge_shouldThrowWhenChargesNull() {
            // Arrange
            Dnd5eInventoryItem item = createItem(1L, "Wand");
            item.setCharges(null);
            when(inventoryRepository.findById(1L)).thenReturn(Optional.of(item));

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> inventoryService.useCharge(1L));
        }
    }

    // ---- rechargeItem ----

    @Nested
    @DisplayName("rechargeItem Tests")
    class RechargeItemTests {

        @Test
        @DisplayName("should recharge item")
        void rechargeItem_shouldRecharge() {
            // Arrange
            Dnd5eInventoryItem item = createItem(1L, "Wand");
            item.setCharges(3);
            item.setMaxCharges(7);
            when(inventoryRepository.findById(1L)).thenReturn(Optional.of(item));
            when(inventoryRepository.save(any(Dnd5eInventoryItem.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Dnd5eInventoryItem result = inventoryService.rechargeItem(1L, 2);

            // Assert
            assertEquals(5, result.getCharges());
        }

        @Test
        @DisplayName("should cap charges at max")
        void rechargeItem_shouldCapAtMax() {
            // Arrange
            Dnd5eInventoryItem item = createItem(1L, "Wand");
            item.setCharges(6);
            item.setMaxCharges(7);
            when(inventoryRepository.findById(1L)).thenReturn(Optional.of(item));
            when(inventoryRepository.save(any(Dnd5eInventoryItem.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Dnd5eInventoryItem result = inventoryService.rechargeItem(1L, 5);

            // Assert
            assertEquals(7, result.getCharges());
        }

        @Test
        @DisplayName("should allow unlimited recharge when no max")
        void rechargeItem_shouldAllowUnlimited() {
            // Arrange
            Dnd5eInventoryItem item = createItem(1L, "Wand");
            item.setCharges(3);
            item.setMaxCharges(null);
            when(inventoryRepository.findById(1L)).thenReturn(Optional.of(item));
            when(inventoryRepository.save(any(Dnd5eInventoryItem.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Dnd5eInventoryItem result = inventoryService.rechargeItem(1L, 10);

            // Assert
            assertEquals(13, result.getCharges());
        }
    }

    // ---- searchItems ----

    @Nested
    @DisplayName("searchItems Tests")
    class SearchItemsTests {

        @Test
        @DisplayName("should search items by name keyword")
        void searchItems_shouldSearchByName() {
            // Arrange
            when(characterRepository.existsById(1L)).thenReturn(true);
            Dnd5eInventoryItem item = createItem(1L, "Longsword");
            when(inventoryRepository.searchByCharacterIdAndName(1L, "Long"))
                    .thenReturn(List.of(item));

            // Act
            List<Dnd5eInventoryItem> result = inventoryService.searchItems(1L, "Long");

            // Assert
            assertEquals(1, result.size());
            assertEquals("Longsword", result.get(0).getName());
        }
    }

    // ---- getItemsByRarity ----

    @Nested
    @DisplayName("getItemsByRarity Tests")
    class GetItemsByRarityTests {

        @Test
        @DisplayName("should return items filtered by rarity")
        void getItemsByRarity_shouldFilterByRarity() {
            // Arrange
            when(characterRepository.existsById(1L)).thenReturn(true);
            Dnd5eInventoryItem rare = createItem(1L, "Magic Sword");
            rare.setRarity("Rare");
            when(inventoryRepository.findByCharacterIdAndRarity(1L, "Rare"))
                    .thenReturn(List.of(rare));

            // Act
            List<Dnd5eInventoryItem> result = inventoryService.getItemsByRarity(1L, "Rare");

            // Assert
            assertEquals(1, result.size());
            assertEquals("Rare", result.get(0).getRarity());
        }
    }

    // ---- getInventorySummary ----

    @Nested
    @DisplayName("getInventorySummary Tests")
    class GetInventorySummaryTests {

        @Test
        @DisplayName("should calculate inventory summary")
        void getInventorySummary_shouldCalculate() {
            // Arrange
            when(characterRepository.existsById(1L)).thenReturn(true);
            Dnd5eInventoryItem sword = createItem(1L, "Longsword");
            sword.setQuantity(1);
            sword.setWeight(BigDecimal.valueOf(3.0));
            sword.setValue(15);
            sword.setEquipped(true);
            sword.setAttuned(false);

            Dnd5eInventoryItem arrows = createItem(2L, "Arrows");
            arrows.setQuantity(20);
            arrows.setWeight(BigDecimal.valueOf(0.5));
            arrows.setValue(1);
            arrows.setEquipped(false);
            arrows.setAttuned(false);

            when(inventoryRepository.findByCharacterId(1L)).thenReturn(List.of(sword, arrows));

            // Act
            InventorySummaryResponse summary = inventoryService.getInventorySummary(1L);

            // Assert
            assertEquals(1L, summary.getCharacterId());
            assertEquals(2, summary.getTotalItems());
            assertEquals(BigDecimal.valueOf(13.0), summary.getTotalWeight()); // 3*1 + 0.5*20
            assertEquals(35, summary.getTotalValue()); // 15*1 + 1*20
            assertEquals(1, summary.getEquippedCount());
            assertEquals(0, summary.getAttunedCount());
        }

        @Test
        @DisplayName("should return zero summary for empty inventory")
        void getInventorySummary_shouldReturnZeroForEmpty() {
            // Arrange
            when(characterRepository.existsById(1L)).thenReturn(true);
            when(inventoryRepository.findByCharacterId(1L)).thenReturn(List.of());

            // Act
            InventorySummaryResponse summary = inventoryService.getInventorySummary(1L);

            // Assert
            assertEquals(0, summary.getTotalItems());
            assertEquals(BigDecimal.ZERO, summary.getTotalWeight());
            assertEquals(0, summary.getTotalValue());
            assertEquals(0, summary.getEquippedCount());
            assertEquals(0, summary.getAttunedCount());
        }
    }

    // ---- Helper ----

    private Dnd5eInventoryItem createItem(Long id, String name) {
        Dnd5eInventoryItem item = new Dnd5eInventoryItem();
        item.setId(id);
        item.setName(name);
        item.setCharacterId(1L);
        item.setItemType("Weapon");
        item.setRarity("Common");
        item.setQuantity(1);
        item.setWeight(BigDecimal.ZERO);
        item.setValue(0);
        return item;
    }
}
