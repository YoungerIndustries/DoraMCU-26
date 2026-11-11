package com.yisy.modules.doramcu.dnd5e.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Dnd5eInventoryItem Tests")
class Dnd5eInventoryItemTest {

    private Dnd5eInventoryItem item;

    @BeforeEach
    void setUp() {
        item = new Dnd5eInventoryItem();
    }

    // ---- Basic Properties ----

    @Nested
    @DisplayName("Basic Property Tests")
    class BasicPropertyTests {

        @Test
        @DisplayName("should set and get id")
        void testId() {
            item.setId(1L);
            assertEquals(1L, item.getId());
        }

        @Test
        @DisplayName("should set and get characterId")
        void testCharacterId() {
            item.setCharacterId(42L);
            assertEquals(42L, item.getCharacterId());
        }

        @Test
        @DisplayName("should set and get name")
        void testName() {
            item.setName("Longsword");
            assertEquals("Longsword", item.getName());
        }

        @Test
        @DisplayName("should set and get description")
        void testDescription() {
            item.setDescription("A standard steel longsword");
            assertEquals("A standard steel longsword", item.getDescription());
        }

        @Test
        @DisplayName("should set and get itemType")
        void testItemType() {
            item.setItemType("Weapon");
            assertEquals("Weapon", item.getItemType());
        }

        @Test
        @DisplayName("should default rarity to Common")
        void testRarityDefault() {
            assertEquals("Common", item.getRarity());
        }

        @Test
        @DisplayName("should set and get rarity")
        void testRarity() {
            item.setRarity("Rare");
            assertEquals("Rare", item.getRarity());
        }

        @Test
        @DisplayName("should default quantity to 1")
        void testQuantityDefault() {
            assertEquals(1, item.getQuantity());
        }

        @Test
        @DisplayName("should set and get quantity")
        void testQuantity() {
            item.setQuantity(20);
            assertEquals(20, item.getQuantity());
        }

        @Test
        @DisplayName("should default weight to 0")
        void testWeightDefault() {
            assertEquals(BigDecimal.ZERO, item.getWeight());
        }

        @Test
        @DisplayName("should set and get weight")
        void testWeight() {
            item.setWeight(BigDecimal.valueOf(3.5));
            assertEquals(BigDecimal.valueOf(3.5), item.getWeight());
        }

        @Test
        @DisplayName("should default value to 0")
        void testValueDefault() {
            assertEquals(0, item.getValue());
        }

        @Test
        @DisplayName("should set and get value")
        void testValue() {
            item.setValue(15);
            assertEquals(15, item.getValue());
        }
    }

    // ---- Equipment Status ----

    @Nested
    @DisplayName("Equipment Status Tests")
    class EquipmentStatusTests {

        @Test
        @DisplayName("should default equipped to false")
        void testEquippedDefault() {
            assertFalse(item.isEquipped());
        }

        @Test
        @DisplayName("should set and get equipped")
        void testEquipped() {
            item.setEquipped(true);
            assertTrue(item.isEquipped());
        }

        @Test
        @DisplayName("should default attuned to false")
        void testAttunedDefault() {
            assertFalse(item.isAttuned());
        }

        @Test
        @DisplayName("should set and get attuned")
        void testAttuned() {
            item.setAttuned(true);
            assertTrue(item.isAttuned());
        }

        @Test
        @DisplayName("should default identified to true")
        void testIdentifiedDefault() {
            assertTrue(item.isIdentified());
        }

        @Test
        @DisplayName("should set and get identified")
        void testIdentified() {
            item.setIdentified(false);
            assertFalse(item.isIdentified());
        }
    }

    // ---- Magic Item Properties ----

    @Nested
    @DisplayName("Magic Item Property Tests")
    class MagicItemPropertyTests {

        @Test
        @DisplayName("should default charges to null")
        void testChargesDefault() {
            assertNull(item.getCharges());
        }

        @Test
        @DisplayName("should set and get charges")
        void testCharges() {
            item.setCharges(7);
            assertEquals(7, item.getCharges());
        }

        @Test
        @DisplayName("should default maxCharges to null")
        void testMaxChargesDefault() {
            assertNull(item.getMaxCharges());
        }

        @Test
        @DisplayName("should set and get maxCharges")
        void testMaxCharges() {
            item.setMaxCharges(7);
            assertEquals(7, item.getMaxCharges());
        }

        @Test
        @DisplayName("should default propertyAc to null")
        void testPropertyAcDefault() {
            assertNull(item.getPropertyAc());
        }

        @Test
        @DisplayName("should set and get propertyAc")
        void testPropertyAc() {
            item.setPropertyAc(16);
            assertEquals(16, item.getPropertyAc());
        }

        @Test
        @DisplayName("should default propertyDamage to null")
        void testPropertyDamageDefault() {
            assertNull(item.getPropertyDamage());
        }

        @Test
        @DisplayName("should set and get propertyDamage")
        void testPropertyDamage() {
            item.setPropertyDamage("1d8+2 slashing");
            assertEquals("1d8+2 slashing", item.getPropertyDamage());
        }

        @Test
        @DisplayName("should default propertyRange to null")
        void testPropertyRangeDefault() {
            assertNull(item.getPropertyRange());
        }

        @Test
        @DisplayName("should set and get propertyRange")
        void testPropertyRange() {
            item.setPropertyRange("80/320");
            assertEquals("80/320", item.getPropertyRange());
        }

        @Test
        @DisplayName("should default propertyHealing to null")
        void testPropertyHealingDefault() {
            assertNull(item.getPropertyHealing());
        }

        @Test
        @DisplayName("should set and get propertyHealing")
        void testPropertyHealing() {
            item.setPropertyHealing("2d4+2");
            assertEquals("2d4+2", item.getPropertyHealing());
        }
    }

    // ---- Notes ----

    @Nested
    @DisplayName("Notes Tests")
    class NotesTests {

        @Test
        @DisplayName("should default notes to null")
        void testNotesDefault() {
            assertNull(item.getNotes());
        }

        @Test
        @DisplayName("should set and get notes")
        void testNotes() {
            item.setNotes("Found in the dungeon");
            assertEquals("Found in the dungeon", item.getNotes());
        }
    }

    // ---- Timestamps ----

    @Nested
    @DisplayName("Timestamp Tests")
    class TimestampTests {

        @Test
        @DisplayName("should default createdAt to null")
        void testCreatedAtDefault() {
            assertNull(item.getCreatedAt());
        }

        @Test
        @DisplayName("should set createdAt")
        void testCreatedAt() {
            LocalDateTime now = LocalDateTime.now();
            item.setCreatedAt(now);
            assertEquals(now, item.getCreatedAt());
        }

        @Test
        @DisplayName("should default updatedAt to null")
        void testUpdatedAtDefault() {
            assertNull(item.getUpdatedAt());
        }

        @Test
        @DisplayName("should set updatedAt")
        void testUpdatedAt() {
            LocalDateTime now = LocalDateTime.now();
            item.setUpdatedAt(now);
            assertEquals(now, item.getUpdatedAt());
        }
    }

    // ---- Lifecycle Callbacks ----

    @Nested
    @DisplayName("Lifecycle Callback Tests")
    class LifecycleCallbackTests {

        @Test
        @DisplayName("onCreate should set timestamps")
        void testOnCreate() {
            // Act
            item.onCreate();

            // Assert
            assertNotNull(item.getCreatedAt());
            assertNotNull(item.getUpdatedAt());
        }

        @Test
        @DisplayName("onUpdate should set updatedAt")
        void testOnUpdate() {
            // Arrange
            item.setCreatedAt(LocalDateTime.of(2024, 1, 1, 0, 0));

            // Act
            item.onUpdate();

            // Assert
            assertNotNull(item.getUpdatedAt());
            assertEquals(LocalDateTime.of(2024, 1, 1, 0, 0), item.getCreatedAt());
        }
    }

    // ---- Item Type Scenarios ----

    @Nested
    @DisplayName("Item Type Scenario Tests")
    class ItemTypeScenarioTests {

        @Test
        @DisplayName("should create a weapon item")
        void testWeaponItem() {
            item.setName("Longsword");
            item.setItemType("Weapon");
            item.setRarity("Common");
            item.setWeight(BigDecimal.valueOf(3.0));
            item.setValue(15);
            item.setPropertyDamage("1d8 slashing");

            assertEquals("Weapon", item.getItemType());
            assertEquals("1d8 slashing", item.getPropertyDamage());
        }

        @Test
        @DisplayName("should create an armor item")
        void testArmorItem() {
            item.setName("Chain Mail");
            item.setItemType("Armor");
            item.setRarity("Common");
            item.setWeight(BigDecimal.valueOf(55.0));
            item.setValue(75);
            item.setPropertyAc(16);

            assertEquals("Armor", item.getItemType());
            assertEquals(16, item.getPropertyAc());
        }

        @Test
        @DisplayName("should create a magic item")
        void testMagicItem() {
            item.setName("Wand of Fireballs");
            item.setItemType("Wand");
            item.setRarity("Uncommon");
            item.setCharges(7);
            item.setMaxCharges(7);
            item.setAttuned(false);

            assertEquals("Wand", item.getItemType());
            assertEquals(7, item.getCharges());
            assertEquals(7, item.getMaxCharges());
        }

        @Test
        @DisplayName("should create a potion item")
        void testPotionItem() {
            item.setName("Potion of Healing");
            item.setItemType("Potion");
            item.setRarity("Common");
            item.setWeight(BigDecimal.valueOf(0.5));
            item.setValue(50);
            item.setPropertyHealing("2d4+2");

            assertEquals("Potion", item.getItemType());
            assertEquals("2d4+2", item.getPropertyHealing());
        }

        @Test
        @DisplayName("should create an ammunition item")
        void testAmmunitionItem() {
            item.setName("Arrow");
            item.setItemType("Ammunition");
            item.setRarity("Common");
            item.setQuantity(20);
            item.setWeight(BigDecimal.valueOf(0.5));
            item.setValue(1);

            assertEquals("Ammunition", item.getItemType());
            assertEquals(20, item.getQuantity());
        }
    }
}
