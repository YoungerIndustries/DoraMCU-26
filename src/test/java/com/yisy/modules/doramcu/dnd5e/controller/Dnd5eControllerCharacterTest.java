package com.yisy.modules.doramcu.dnd5e.controller;

import com.yisy.modules.doramcu.dnd5e.dto.CreateDnd5eCharacterRequest;
import com.yisy.modules.doramcu.dnd5e.dto.UpdateDnd5eCharacterRequest;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCharacter;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eCampaignService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eCharacterService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eCombatService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eDiceService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eGameplayService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eCraftingService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eExhaustionService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eFeatService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eHeroicInspirationService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eToolProficiencyService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5ePsionService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eHorrorService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eGrowingMagicItemService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eInventoryService;
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
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eBackgroundService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eClassService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eMonsterService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eRulesService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eSpellService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Dnd5eController Character Tests")
class Dnd5eControllerCharacterTest {

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

    // ---- POST /characters ----

    @Nested
    @DisplayName("POST /characters Tests")
    class CreateCharacterTests {

        @Test
        @DisplayName("should create a character with standard array scores")
        void createCharacter_standardArray() throws Exception {
            // Arrange
            Dnd5eCharacter character = createCharacter(1L, "Frodo");
            character.setRace("Halfling");
            character.setCharacterClass("Rogue");
            when(characterService.createCharacter(any())).thenReturn(character);

            String json = """
                    {
                        "name": "Frodo",
                        "race": "Halfling",
                        "characterClass": "Rogue",
                        "background": "Outlander"
                    }
                    """;

            // Act & Assert
            mockMvc.perform(post("/api/v1/dnd5e/characters")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Frodo"))
                    .andExpect(jsonPath("$.race").value("Halfling"))
                    .andExpect(jsonPath("$.characterClass").value("Rogue"));

            verify(characterService).createCharacter(any());
        }

        @Test
        @DisplayName("should create a character with provided ability scores")
        void createCharacter_providedScores() throws Exception {
            // Arrange
            Dnd5eCharacter character = createCharacter(1L, "Gimli");
            character.setStrScore(18);
            character.setDexScore(12);
            character.setConScore(16);
            when(characterService.createCharacter(any())).thenReturn(character);

            String json = """
                    {
                        "name": "Gimli",
                        "race": "Dwarf",
                        "characterClass": "Fighter",
                        "strScore": 18,
                        "dexScore": 12,
                        "conScore": 16,
                        "intScore": 10,
                        "wisScore": 12,
                        "chaScore": 8
                    }
                    """;

            // Act & Assert
            mockMvc.perform(post("/api/v1/dnd5e/characters")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.strScore").value(18))
                    .andExpect(jsonPath("$.dexScore").value(12));
        }

        @Test
        @DisplayName("should create a character with roll method")
        void createCharacter_rollMethod() throws Exception {
            // Arrange
            Dnd5eCharacter character = createCharacter(1L, "Rolled");
            when(characterService.createCharacter(any())).thenReturn(character);

            String json = """
                    {
                        "name": "Rolled",
                        "race": "Human",
                        "characterClass": "Wizard",
                        "scoreMethod": "roll"
                    }
                    """;

            // Act & Assert
            mockMvc.perform(post("/api/v1/dnd5e/characters")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Rolled"));
        }
    }

    // ---- GET /characters ----

    @Nested
    @DisplayName("GET /characters Tests")
    class ListCharactersTests {

        @Test
        @DisplayName("should return all characters")
        void listCharacters_success() throws Exception {
            // Arrange
            when(characterService.listCharacters()).thenReturn(List.of(
                    createCharacter(1L, "Frodo"),
                    createCharacter(2L, "Aragorn")
            ));

            // Act & Assert
            mockMvc.perform(get("/api/v1/dnd5e/characters"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].name").value("Frodo"))
                    .andExpect(jsonPath("$[1].name").value("Aragorn"));
        }

        @Test
        @DisplayName("should return empty list when no characters")
        void listCharacters_empty() throws Exception {
            // Arrange
            when(characterService.listCharacters()).thenReturn(List.of());

            // Act & Assert
            mockMvc.perform(get("/api/v1/dnd5e/characters"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    // ---- GET /characters/{id} ----

    @Nested
    @DisplayName("GET /characters/{id} Tests")
    class GetCharacterTests {

        @Test
        @DisplayName("should return character by id")
        void getCharacter_success() throws Exception {
            // Arrange
            Dnd5eCharacter character = createCharacter(1L, "Frodo");
            character.setStrScore(15);
            character.setDexScore(14);
            character.setHitPoints(18);
            character.setMaxHitPoints(18);
            when(characterService.getCharacter(1L)).thenReturn(character);

            // Act & Assert
            mockMvc.perform(get("/api/v1/dnd5e/characters/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Frodo"))
                    .andExpect(jsonPath("$.strScore").value(15))
                    .andExpect(jsonPath("$.hitPoints").value(18));
        }
    }

    // ---- PATCH /characters/{id} ----

    @Nested
    @DisplayName("PATCH /characters/{id} Tests")
    class UpdateCharacterTests {

        @Test
        @DisplayName("should update character HP")
        void updateCharacter_hp() throws Exception {
            // Arrange
            Dnd5eCharacter updated = createCharacter(1L, "Frodo");
            updated.setHitPoints(15);
            updated.setMaxHitPoints(25);
            when(characterService.updateCharacter(eq(1L), any())).thenReturn(updated);

            String json = """
                    {
                        "hitPoints": 15
                    }
                    """;

            // Act & Assert
            mockMvc.perform(patch("/api/v1/dnd5e/characters/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.hitPoints").value(15));
        }

        @Test
        @DisplayName("should update character AC and speed")
        void updateCharacter_acAndSpeed() throws Exception {
            // Arrange
            Dnd5eCharacter updated = createCharacter(1L, "Frodo");
            updated.setArmorClass(18);
            updated.setSpeed(25);
            when(characterService.updateCharacter(eq(1L), any())).thenReturn(updated);

            String json = """
                    {
                        "armorClass": 18,
                        "speed": 25
                    }
                    """;

            // Act & Assert
            mockMvc.perform(patch("/api/v1/dnd5e/characters/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.armorClass").value(18))
                    .andExpect(jsonPath("$.speed").value(25));
        }

        @Test
        @DisplayName("should update character XP")
        void updateCharacter_xp() throws Exception {
            // Arrange
            Dnd5eCharacter updated = createCharacter(1L, "Frodo");
            updated.setExperiencePoints(500);
            when(characterService.updateCharacter(eq(1L), any())).thenReturn(updated);

            String json = """
                    {
                        "experiencePoints": 500
                    }
                    """;

            // Act & Assert
            mockMvc.perform(patch("/api/v1/dnd5e/characters/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.experiencePoints").value(500));
        }

        @Test
        @DisplayName("should update character ability scores")
        void updateCharacter_abilityScores() throws Exception {
            // Arrange
            Dnd5eCharacter updated = createCharacter(1L, "Frodo");
            updated.setStrScore(20);
            updated.setDexScore(18);
            updated.setConScore(16);
            when(characterService.updateCharacter(eq(1L), any())).thenReturn(updated);

            String json = """
                    {
                        "strScore": 20,
                        "dexScore": 18,
                        "conScore": 16
                    }
                    """;

            // Act & Assert
            mockMvc.perform(patch("/api/v1/dnd5e/characters/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.strScore").value(20))
                    .andExpect(jsonPath("$.dexScore").value(18));
        }

        @Test
        @DisplayName("should update character equipment")
        void updateCharacter_equipment() throws Exception {
            // Arrange
            Dnd5eCharacter updated = createCharacter(1L, "Frodo");
            updated.setEquipment("Ring, Sword, Shield");
            when(characterService.updateCharacter(eq(1L), any())).thenReturn(updated);

            String json = """
                    {
                        "equipment": "Ring, Sword, Shield"
                    }
                    """;

            // Act & Assert
            mockMvc.perform(patch("/api/v1/dnd5e/characters/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.equipment").value("Ring, Sword, Shield"));
        }

        @Test
        @DisplayName("should update character conditions")
        void updateCharacter_conditions() throws Exception {
            // Arrange
            Dnd5eCharacter updated = createCharacter(1L, "Frodo");
            updated.setConditions("poisoned");
            when(characterService.updateCharacter(eq(1L), any())).thenReturn(updated);

            String json = """
                    {
                        "conditions": "poisoned"
                    }
                    """;

            // Act & Assert
            mockMvc.perform(patch("/api/v1/dnd5e/characters/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.conditions").value("poisoned"));
        }

        @Test
        @DisplayName("should update character temp HP and exhaustion")
        void updateCharacter_tempHpAndExhaustion() throws Exception {
            // Arrange
            Dnd5eCharacter updated = createCharacter(1L, "Frodo");
            updated.setTempHitPoints(10);
            updated.setExhaustionLevel(2);
            when(characterService.updateCharacter(eq(1L), any())).thenReturn(updated);

            String json = """
                    {
                        "tempHitPoints": 10,
                        "exhaustionLevel": 2
                    }
                    """;

            // Act & Assert
            mockMvc.perform(patch("/api/v1/dnd5e/characters/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.tempHitPoints").value(10))
                    .andExpect(jsonPath("$.exhaustionLevel").value(2));
        }

        @Test
        @DisplayName("should update character death saves")
        void updateCharacter_deathSaves() throws Exception {
            // Arrange
            Dnd5eCharacter updated = createCharacter(1L, "Frodo");
            updated.setDeathSaveSuccesses(1);
            updated.setDeathSaveFailures(2);
            when(characterService.updateCharacter(eq(1L), any())).thenReturn(updated);

            String json = """
                    {
                        "deathSaveSuccesses": 1,
                        "deathSaveFailures": 2
                    }
                    """;

            // Act & Assert
            mockMvc.perform(patch("/api/v1/dnd5e/characters/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.deathSaveSuccesses").value(1))
                    .andExpect(jsonPath("$.deathSaveFailures").value(2));
        }

        @Test
        @DisplayName("should update character inspiration")
        void updateCharacter_inspiration() throws Exception {
            // Arrange
            Dnd5eCharacter updated = createCharacter(1L, "Frodo");
            updated.setInspiration(true);
            when(characterService.updateCharacter(eq(1L), any())).thenReturn(updated);

            String json = """
                    {
                        "inspiration": true
                    }
                    """;

            // Act & Assert
            mockMvc.perform(patch("/api/v1/dnd5e/characters/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.inspiration").value(true));
        }
    }

    // ---- DELETE /characters/{id} ----

    @Nested
    @DisplayName("DELETE /characters/{id} Tests")
    class DeleteCharacterTests {

        @Test
        @DisplayName("should delete character")
        void deleteCharacter_success() throws Exception {
            // Arrange
            doNothing().when(characterService).deleteCharacter(1L);

            // Act & Assert
            mockMvc.perform(delete("/api/v1/dnd5e/characters/1"))
                    .andExpect(status().isOk());

            verify(characterService).deleteCharacter(1L);
        }
    }

    // ---- Helper methods ----

    private Dnd5eCharacter createCharacter(Long id, String name) {
        Dnd5eCharacter character = new Dnd5eCharacter();
        character.setId(id);
        character.setName(name);
        character.setRace("Human");
        character.setCharacterClass("Fighter");
        character.setLevel(1);
        character.setStrScore(15);
        character.setDexScore(14);
        character.setConScore(13);
        character.setIntScore(12);
        character.setWisScore(10);
        character.setChaScore(8);
        character.setHitPoints(18);
        character.setMaxHitPoints(18);
        character.setArmorClass(12);
        character.setSpeed(30);
        character.setHitDice("d10");
        character.setProficiencyBonus(2);
        return character;
    }
}
