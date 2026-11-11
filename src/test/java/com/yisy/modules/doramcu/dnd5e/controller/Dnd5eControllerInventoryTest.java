package com.yisy.modules.doramcu.dnd5e.controller;

import com.yisy.modules.doramcu.dnd5e.dto.CreateDnd5eInventoryItemRequest;
import com.yisy.modules.doramcu.dnd5e.dto.InventorySummaryResponse;
import com.yisy.modules.doramcu.dnd5e.dto.UpdateDnd5eInventoryItemRequest;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eInventoryItem;
import com.yisy.modules.doramcu.dnd5e.service.*;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eBackgroundService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eClassService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eMonsterService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eExhaustionService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eFeatService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eToolProficiencyService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5ePsionService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eHorrorService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eGrowingMagicItemService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eVillainService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eBastionService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eAdventureHookService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eDungeonRoomService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eTensionPoolService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eEncounterService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eNpcService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eModuleService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eSpeciesService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eCampaignTemplateService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eMagicItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Dnd5eController Inventory Tests")
class Dnd5eControllerInventoryTest {

    private MockMvc mockMvc;

    private Dnd5eCharacterService characterService;
    private Dnd5eCombatService combatService;
    private Dnd5eSpellService spellService;
    private Dnd5eRulesService rulesService;
    private Dnd5eDiceService diceService;
    private Dnd5eGameplayService gameplayService;
    private Dnd5eCampaignService campaignService;
    private Dnd5eInventoryService inventoryService;
    private Dnd5eCraftingService craftingService;
    private Dnd5eHeroicInspirationService heroicInspirationService;
    private Dnd5eExhaustionService exhaustionService;
    private Dnd5eFeatService featService;
    private Dnd5eToolProficiencyService toolProficiencyService;
    private Dnd5ePsionService psionService;
    private Dnd5eHorrorService horrorService;
    private Dnd5eGrowingMagicItemService growingItemService;
    private Dnd5eVillainService villainService;
    private Dnd5eBastionService bastionService;
    private Dnd5eAdventureHookService adventureHookService;
    private Dnd5eDungeonRoomService dungeonRoomService;
    private Dnd5eTensionPoolService tensionPoolService;
    private Dnd5eNpcService npcService;
    private Dnd5eEncounterService encounterService;
    private Dnd5eModuleService moduleService;
    private Dnd5eSpeciesService speciesService;
    private Dnd5eCampaignTemplateService campaignTemplateService;
    private Dnd5eMagicItemService magicItemService;
    private Dnd5eBackgroundService backgroundService;
    private Dnd5eClassService classService;
    private Dnd5eMonsterService monsterService;

    @BeforeEach
    void setUp() {
        characterService = mock(Dnd5eCharacterService.class);
        combatService = mock(Dnd5eCombatService.class);
        spellService = mock(Dnd5eSpellService.class);
        rulesService = mock(Dnd5eRulesService.class);
        diceService = mock(Dnd5eDiceService.class);
        gameplayService = mock(Dnd5eGameplayService.class);
        campaignService = mock(Dnd5eCampaignService.class);
        inventoryService = mock(Dnd5eInventoryService.class);
        craftingService = mock(Dnd5eCraftingService.class);
        heroicInspirationService = mock(Dnd5eHeroicInspirationService.class);
        exhaustionService = mock(Dnd5eExhaustionService.class);
        featService = mock(Dnd5eFeatService.class);
        toolProficiencyService = mock(Dnd5eToolProficiencyService.class);
        psionService = mock(Dnd5ePsionService.class);
        horrorService = mock(Dnd5eHorrorService.class);
        growingItemService = mock(Dnd5eGrowingMagicItemService.class);
        villainService = mock(Dnd5eVillainService.class);
        bastionService = mock(Dnd5eBastionService.class);
        adventureHookService = mock(Dnd5eAdventureHookService.class);
        dungeonRoomService = mock(Dnd5eDungeonRoomService.class);
        tensionPoolService = mock(Dnd5eTensionPoolService.class);
        npcService = mock(Dnd5eNpcService.class);
        encounterService = mock(Dnd5eEncounterService.class);
        moduleService = mock(Dnd5eModuleService.class);
        speciesService = mock(Dnd5eSpeciesService.class);
        campaignTemplateService = mock(Dnd5eCampaignTemplateService.class);
        magicItemService = mock(Dnd5eMagicItemService.class);
        backgroundService = mock(Dnd5eBackgroundService.class);
        classService = mock(Dnd5eClassService.class);
        monsterService = mock(Dnd5eMonsterService.class);

        Dnd5eController controller = new Dnd5eController(
                characterService, combatService, spellService, rulesService,
                diceService, gameplayService, campaignService, inventoryService, craftingService,
                heroicInspirationService, exhaustionService, featService, toolProficiencyService,
                psionService, horrorService, growingItemService, villainService, bastionService, adventureHookService,
                dungeonRoomService, tensionPoolService, npcService, encounterService, moduleService,
                speciesService, campaignTemplateService, magicItemService, backgroundService, classService,
                monsterService
        );

        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    // ---- POST /inventory/items ----

    @Nested
    @DisplayName("POST /inventory/items Tests")
    class CreateItemTests {

        @Test
        @DisplayName("should create an inventory item")
        void createItem_success() throws Exception {
            // Arrange
            Dnd5eInventoryItem item = createItem(1L, "Longsword");
            when(inventoryService.createItem(eq(1L), any())).thenReturn(item);

            String json = """
                    {
                        "characterId": 1,
                        "name": "Longsword",
                        "itemType": "Weapon",
                        "rarity": "Common",
                        "quantity": 1,
                        "weight": 3.0,
                        "value": 15
                    }
                    """;

            // Act & Assert
            mockMvc.perform(post("/api/v1/dnd5e/inventory/items")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Longsword"));

            verify(inventoryService).createItem(eq(1L), any());
        }
    }

    // ---- GET /inventory/items/{id} ----

    @Nested
    @DisplayName("GET /inventory/items/{id} Tests")
    class GetItemTests {

        @Test
        @DisplayName("should return an inventory item by id")
        void getItem_success() throws Exception {
            // Arrange
            Dnd5eInventoryItem item = createItem(1L, "Longsword");
            when(inventoryService.getItem(1L)).thenReturn(item);

            // Act & Assert
            mockMvc.perform(get("/api/v1/dnd5e/inventory/items/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Longsword"));

            verify(inventoryService).getItem(1L);
        }
    }

    // ---- GET /inventory/character/{characterId} ----

    @Nested
    @DisplayName("GET /inventory/character/{characterId} Tests")
    class ListItemsTests {

        @Test
        @DisplayName("should return all items for a character")
        void listItems_success() throws Exception {
            // Arrange
            List<Dnd5eInventoryItem> items = List.of(
                    createItem(1L, "Longsword"),
                    createItem(2L, "Shield")
            );
            when(inventoryService.listItems(1L)).thenReturn(items);

            // Act & Assert
            mockMvc.perform(get("/api/v1/dnd5e/inventory/character/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2));

            verify(inventoryService).listItems(1L);
        }

        @Test
        @DisplayName("should return empty list when no items")
        void listItems_empty() throws Exception {
            // Arrange
            when(inventoryService.listItems(1L)).thenReturn(List.of());

            // Act & Assert
            mockMvc.perform(get("/api/v1/dnd5e/inventory/character/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    // ---- PATCH /inventory/items/{id} ----

    @Nested
    @DisplayName("PATCH /inventory/items/{id} Tests")
    class UpdateItemTests {

        @Test
        @DisplayName("should update an inventory item")
        void updateItem_success() throws Exception {
            // Arrange
            Dnd5eInventoryItem updated = createItem(1L, "Magic Longsword");
            updated.setRarity("Rare");
            when(inventoryService.updateItem(eq(1L), any())).thenReturn(updated);

            String json = """
                    {
                        "name": "Magic Longsword",
                        "rarity": "Rare"
                    }
                    """;

            // Act & Assert
            mockMvc.perform(patch("/api/v1/dnd5e/inventory/items/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Magic Longsword"))
                    .andExpect(jsonPath("$.rarity").value("Rare"));

            verify(inventoryService).updateItem(eq(1L), any());
        }
    }

    // ---- DELETE /inventory/items/{id} ----

    @Nested
    @DisplayName("DELETE /inventory/items/{id} Tests")
    class DeleteItemTests {

        @Test
        @DisplayName("should delete an inventory item")
        void deleteItem_success() throws Exception {
            // Arrange
            doNothing().when(inventoryService).deleteItem(1L);

            // Act & Assert
            mockMvc.perform(delete("/api/v1/dnd5e/inventory/items/1"))
                    .andExpect(status().isOk());

            verify(inventoryService).deleteItem(1L);
        }
    }

    // ---- PATCH /inventory/items/{id}/equip ----

    @Nested
    @DisplayName("PATCH /inventory/items/{id}/equip Tests")
    class EquipItemTests {

        @Test
        @DisplayName("should equip an item")
        void equipItem_success() throws Exception {
            // Arrange
            Dnd5eInventoryItem item = createItem(1L, "Longsword");
            item.setEquipped(true);
            when(inventoryService.equipItem(1L)).thenReturn(item);

            // Act & Assert
            mockMvc.perform(patch("/api/v1/dnd5e/inventory/items/1/equip"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.equipped").value(true));

            verify(inventoryService).equipItem(1L);
        }
    }

    // ---- PATCH /inventory/items/{id}/unequip ----

    @Nested
    @DisplayName("PATCH /inventory/items/{id}/unequip Tests")
    class UnequipItemTests {

        @Test
        @DisplayName("should unequip an item")
        void unequipItem_success() throws Exception {
            // Arrange
            Dnd5eInventoryItem item = createItem(1L, "Longsword");
            item.setEquipped(false);
            when(inventoryService.unequipItem(1L)).thenReturn(item);

            // Act & Assert
            mockMvc.perform(patch("/api/v1/dnd5e/inventory/items/1/unequip"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.equipped").value(false));

            verify(inventoryService).unequipItem(1L);
        }
    }

    // ---- GET /inventory/character/{characterId}/equipped ----

    @Nested
    @DisplayName("GET /inventory/character/{characterId}/equipped Tests")
    class GetEquippedItemsTests {

        @Test
        @DisplayName("should return equipped items")
        void getEquippedItems_success() throws Exception {
            // Arrange
            Dnd5eInventoryItem equipped = createItem(1L, "Longsword");
            equipped.setEquipped(true);
            when(inventoryService.getEquippedItems(1L)).thenReturn(List.of(equipped));

            // Act & Assert
            mockMvc.perform(get("/api/v1/dnd5e/inventory/character/1/equipped"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1));

            verify(inventoryService).getEquippedItems(1L);
        }
    }

    // ---- PATCH /inventory/items/{id}/attune ----

    @Nested
    @DisplayName("PATCH /inventory/items/{id}/attune Tests")
    class AttuneItemTests {

        @Test
        @DisplayName("should attune an item")
        void attuneItem_success() throws Exception {
            // Arrange
            Dnd5eInventoryItem item = createItem(1L, "Amulet");
            item.setAttuned(true);
            when(inventoryService.attuneItem(1L)).thenReturn(item);

            // Act & Assert
            mockMvc.perform(patch("/api/v1/dnd5e/inventory/items/1/attune"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.attuned").value(true));

            verify(inventoryService).attuneItem(1L);
        }
    }

    // ---- PATCH /inventory/items/{id}/unattune ----

    @Nested
    @DisplayName("PATCH /inventory/items/{id}/unattune Tests")
    class UnattuneItemTests {

        @Test
        @DisplayName("should unattune an item")
        void unattuneItem_success() throws Exception {
            // Arrange
            Dnd5eInventoryItem item = createItem(1L, "Amulet");
            item.setAttuned(false);
            when(inventoryService.unattuneItem(1L)).thenReturn(item);

            // Act & Assert
            mockMvc.perform(patch("/api/v1/dnd5e/inventory/items/1/unattune"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.attuned").value(false));

            verify(inventoryService).unattuneItem(1L);
        }
    }

    // ---- PATCH /inventory/items/{id}/add-quantity ----

    @Nested
    @DisplayName("PATCH /inventory/items/{id}/add-quantity Tests")
    class AddQuantityTests {

        @Test
        @DisplayName("should add quantity to an item")
        void addQuantity_success() throws Exception {
            // Arrange
            Dnd5eInventoryItem item = createItem(1L, "Arrow");
            item.setQuantity(30);
            when(inventoryService.addItemQuantity(eq(1L), anyInt())).thenReturn(item);

            String json = """
                    { "amount": 10 }
                    """;

            // Act & Assert
            mockMvc.perform(patch("/api/v1/dnd5e/inventory/items/1/add-quantity")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.quantity").value(30));

            verify(inventoryService).addItemQuantity(eq(1L), anyInt());
        }
    }

    // ---- PATCH /inventory/items/{id}/consume ----

    @Nested
    @DisplayName("PATCH /inventory/items/{id}/consume Tests")
    class ConsumeItemTests {

        @Test
        @DisplayName("should consume item quantity")
        void consumeItem_success() throws Exception {
            // Arrange
            Dnd5eInventoryItem item = createItem(1L, "Arrow");
            item.setQuantity(15);
            when(inventoryService.consumeItem(eq(1L), anyInt())).thenReturn(item);

            String json = """
                    { "amount": 5 }
                    """;

            // Act & Assert
            mockMvc.perform(patch("/api/v1/dnd5e/inventory/items/1/consume")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.quantity").value(15));

            verify(inventoryService).consumeItem(eq(1L), anyInt());
        }
    }

    // ---- PATCH /inventory/items/{id}/use-charge ----

    @Nested
    @DisplayName("PATCH /inventory/items/{id}/use-charge Tests")
    class UseChargeTests {

        @Test
        @DisplayName("should use a charge")
        void useCharge_success() throws Exception {
            // Arrange
            Dnd5eInventoryItem item = createItem(1L, "Wand");
            item.setCharges(4);
            when(inventoryService.useCharge(1L)).thenReturn(item);

            // Act & Assert
            mockMvc.perform(patch("/api/v1/dnd5e/inventory/items/1/use-charge"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.charges").value(4));

            verify(inventoryService).useCharge(1L);
        }
    }

    // ---- PATCH /inventory/items/{id}/recharge ----

    @Nested
    @DisplayName("PATCH /inventory/items/{id}/recharge Tests")
    class RechargeItemTests {

        @Test
        @DisplayName("should recharge an item")
        void rechargeItem_success() throws Exception {
            // Arrange
            Dnd5eInventoryItem item = createItem(1L, "Wand");
            item.setCharges(7);
            when(inventoryService.rechargeItem(eq(1L), anyInt())).thenReturn(item);

            String json = """
                    { "charges": 3 }
                    """;

            // Act & Assert
            mockMvc.perform(patch("/api/v1/dnd5e/inventory/items/1/recharge")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.charges").value(7));

            verify(inventoryService).rechargeItem(eq(1L), anyInt());
        }
    }

    // ---- GET /inventory/character/{characterId}/search ----

    @Nested
    @DisplayName("GET /inventory/character/{characterId}/search Tests")
    class SearchItemsTests {

        @Test
        @DisplayName("should search items by keyword")
        void searchItems_success() throws Exception {
            // Arrange
            when(inventoryService.searchItems(eq(1L), eq("Long"))).thenReturn(
                    List.of(createItem(1L, "Longsword"))
            );

            // Act & Assert
            mockMvc.perform(get("/api/v1/dnd5e/inventory/character/1/search")
                            .param("keyword", "Long"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1));

            verify(inventoryService).searchItems(eq(1L), eq("Long"));
        }
    }

    // ---- GET /inventory/character/{characterId}/summary ----

    @Nested
    @DisplayName("GET /inventory/character/{characterId}/summary Tests")
    class InventorySummaryTests {

        @Test
        @DisplayName("should return inventory summary")
        void getInventorySummary_success() throws Exception {
            // Arrange
            InventorySummaryResponse summary = new InventorySummaryResponse();
            summary.setCharacterId(1L);
            summary.setTotalItems(5);
            summary.setTotalWeight(BigDecimal.valueOf(25.5));
            summary.setTotalValue(150);
            summary.setEquippedCount(2);
            summary.setAttunedCount(1);
            when(inventoryService.getInventorySummary(1L)).thenReturn(summary);

            // Act & Assert
            mockMvc.perform(get("/api/v1/dnd5e/inventory/character/1/summary"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalItems").value(5))
                    .andExpect(jsonPath("$.totalValue").value(150))
                    .andExpect(jsonPath("$.equippedCount").value(2))
                    .andExpect(jsonPath("$.attunedCount").value(1));

            verify(inventoryService).getInventorySummary(1L);
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
