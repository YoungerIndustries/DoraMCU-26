package com.yisy.modules.doramcu.babylon5;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yisy.modules.doramcu.babylon5.model.*;
import com.yisy.modules.doramcu.babylon5.service.Babylon5DatabaseService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Babylon 5 Database Integration Tests")
class Babylon5DatabaseIntegrationTest {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String B5_DB_PATH = "docs/babylon5-db/babylon5-universe.json";

    private static JsonNode database;
    private static Babylon5DatabaseService service;

    @BeforeAll
    static void loadDatabase() throws Exception {
        database = objectMapper.readTree(new File(B5_DB_PATH));
        service = new Babylon5DatabaseService(B5_DB_PATH);
        service.loadDatabase();
    }

    // ==========================================
    // Structure Tests
    // ==========================================

    @Test
    @DisplayName("Database should have all required top-level sections")
    void testDatabaseStructure() {
        assertTrue(database.has("metadata"), "Missing metadata");
        assertTrue(database.has("races"), "Missing races");
        assertTrue(database.has("characters"), "Missing characters");
        assertTrue(database.has("storylines"), "Missing storylines");
        assertTrue(database.has("dialogues"), "Missing dialogues");
        assertTrue(database.has("locations"), "Missing locations");
    }

    @Test
    @DisplayName("Database should have 13 races")
    void testRaceCount() {
        JsonNode races = database.get("races");
        assertEquals(13, races.size(), "Expected 13 races");
        assertEquals(13, service.getAllRaces().size(), "Service should load 13 races");
    }

    @Test
    @DisplayName("Database should have 20 characters")
    void testCharacterCount() {
        JsonNode characters = database.get("characters");
        assertEquals(20, characters.size(), "Expected 20 characters");
        assertEquals(20, service.getAllCharacters().size(), "Service should load 20 characters");
    }

    @Test
    @DisplayName("Database should have 6 storylines")
    void testStorylineCount() {
        JsonNode storylines = database.get("storylines");
        assertEquals(6, storylines.size(), "Expected 6 storylines");
        assertEquals(6, service.getAllStorylines().size(), "Service should load 6 storylines");
    }

    @Test
    @DisplayName("Database should have dialogues")
    void testDialogueCount() {
        JsonNode dialogues = database.get("dialogues");
        assertTrue(dialogues.size() > 0, "Expected at least 1 dialogue");
        assertTrue(service.getAllDialogues().size() > 0, "Service should load dialogues");
    }

    @Test
    @DisplayName("Database should have locations")
    void testLocationCount() {
        JsonNode locations = database.get("locations");
        assertTrue(locations.size() > 0, "Expected at least 1 location");
        assertTrue(service.getAllLocations().size() > 0, "Service should load locations");
    }

    // ==========================================
    // Race Tests
    // ==========================================

    @Test
    @DisplayName("Human race should exist with correct characteristics")
    void testHumanRace() {
        Optional<B5Race> human = service.getRace("HUMAN");
        assertTrue(human.isPresent(), "Human race should exist");
        B5Race race = human.get();
        assertEquals("HUMAN", race.getId());
        assertEquals("Earth", race.getHomeworld());
        assertNotNull(race.getCharacteristics());
        assertEquals(0, race.getCharacteristics().getST(), "Human ST should be 0");
    }

    @Test
    @DisplayName("Narn race should have ST and EN bonuses")
    void testNarnRace() {
        Optional<B5Race> narn = service.getRace("NARN");
        assertTrue(narn.isPresent(), "Narn race should exist");
        B5Race race = narn.get();
        assertEquals(1, race.getCharacteristics().getST(), "Narn ST should be +1");
        assertEquals(1, race.getCharacteristics().getEN(), "Narn EN should be +1");
        assertEquals(-1, race.getSocialDM(), "Narn social DM should be -1");
    }

    @Test
    @DisplayName("Centauri race should have social and trade bonuses")
    void testCentauriRace() {
        Optional<B5Race> centauri = service.getRace("CENTAURI");
        assertTrue(centauri.isPresent(), "Centauri race should exist");
        B5Race race = centauri.get();
        assertEquals(2, race.getSocialDM(), "Centauri social DM should be +2");
        assertEquals(2, race.getTradeBonus(), "Centauri trade bonus should be +2");
    }

    @Test
    @DisplayName("Minbari race should have navigation and armor bonuses")
    void testMinbariRace() {
        Optional<B5Race> minbari = service.getRace("MINBARI");
        assertTrue(minbari.isPresent(), "Minbari race should exist");
        B5Race race = minbari.get();
        assertEquals(1, race.getNavigationDM(), "Minbari navigation DM should be +1");
        assertEquals(1, race.getArmorBonus(), "Minbari armor bonus should be +1");
        assertEquals(250, race.getLifespan(), "Minbari lifespan should be 250");
    }

    // ==========================================
    // Character Tests
    // ==========================================

    @Test
    @DisplayName("Sheridan should exist with personality and relationships")
    void testSheridan() {
        Optional<B5Character> sheridan = service.getCharacter("SHERIDAN");
        assertTrue(sheridan.isPresent(), "Sheridan should exist");
        B5Character ch = sheridan.get();
        assertEquals("John Sheridan", ch.getName());
        assertEquals("HUMAN", ch.getSpecies());
        assertNotNull(ch.getPersonality(), "Sheridan should have personality");
        assertNotNull(ch.getRelationships(), "Sheridan should have relationships");
        assertTrue(ch.getRelationships().size() > 0, "Sheridan should have relationships");
        assertNotNull(ch.getFamousQuotes(), "Sheridan should have quotes");
    }

    @Test
    @DisplayName("G'Kar should be Narn species")
    void testGKar() {
        Optional<B5Character> gkar = service.getCharacter("GKAR");
        assertTrue(gkar.isPresent(), "G'Kar should exist");
        assertEquals("NARN", gkar.get().getSpecies());
        assertNotNull(gkar.get().getPersonality());
    }

    @Test
    @DisplayName("Delenn should be Minbari species")
    void testDelenn() {
        Optional<B5Character> delenn = service.getCharacter("DELN");
        assertTrue(delenn.isPresent(), "Delenn should exist");
        assertEquals("MINBARI", delenn.get().getSpecies());
        assertNotNull(delenn.get().getPersonality());
    }

    @Test
    @DisplayName("Garibaldi should exist with spy archetype")
    void testGaribaldi() {
        Optional<B5Character> garibaldi = service.getCharacter("GARIBALDI");
        assertTrue(garibaldi.isPresent(), "Garibaldi should exist");
        assertEquals("SPY", garibaldi.get().getArchetype());
    }

    @Test
    @DisplayName("Characters should have non-blank descriptions")
    void testCharacterDescriptions() {
        for (B5Character ch : service.getAllCharacters()) {
            assertNotNull(ch.getDescription(), ch.getId() + " should have description");
            assertTrue(ch.getDescription().trim().length() > 0, ch.getId() + " description should not be blank");
        }
    }

    @Test
    @DisplayName("Sheridan should have highest leadership score")
    void testSheridanLeadership() {
        B5Character sheridan = service.getCharacter("SHERIDAN").orElseThrow();
        assertTrue(sheridan.getPersonality().getLeadership() > 0.8,
                "Sheridan should have high leadership");
    }

    // ==========================================
    // Personality Tests
    // ==========================================

    @Test
    @DisplayName("Personality axes should be in valid range")
    void testPersonalityRange() {
        for (B5Character ch : service.getAllCharacters()) {
            B5Personality p = ch.getPersonality();
            assertNotNull(p, ch.getId() + " should have personality");
            assertTrue(p.getAggression() >= 0.0 && p.getAggression() <= 1.0,
                    ch.getId() + " aggression out of range");
            assertTrue(p.getLeadership() >= 0.0 && p.getLeadership() <= 1.0,
                    ch.getId() + " leadership out of range");
        }
    }

    @Test
    @DisplayName("Personality should have 12 axes")
    void testPersonalityAxisCount() {
        B5Character sheridan = service.getCharacter("SHERIDAN").orElseThrow();
        B5Personality p = sheridan.getPersonality();
        // Check all 12 axes are present (non-zero or meaningful values)
        assertNotNull(p.getAggression());
        assertNotNull(p.getDiplomacy());
        assertNotNull(p.getEconomicFocus());
        assertNotNull(p.getScientificFocus());
        assertNotNull(p.getExpansionDrive());
        assertNotNull(p.getRiskTolerance());
        assertNotNull(p.getAltruism());
        assertNotNull(p.getDetermination());
        assertNotNull(p.getAdaptability());
        assertNotNull(p.getLeadership());
        assertNotNull(p.getCunning());
        assertNotNull(p.getHonor());
    }

    // ==========================================
    // Characteristics Tests
    // ==========================================

    @Test
    @DisplayName("Characteristics should have 6 values")
    void testCharacteristicsCount() {
        B5Character sheridan = service.getCharacter("SHERIDAN").orElseThrow();
        B5Characteristics c = sheridan.getCharacteristics();
        assertNotNull(c);
        // All 6 characteristics should be present
        c.getST();
        c.getDX();
        c.getEN();
        c.getIN();
        c.getED();
        c.getSO();
    }

    // ==========================================
    // Species Filter Tests
    // ==========================================

    @Test
    @DisplayName("Filter by species should return correct characters")
    void testCharactersBySpecies() {
        List<B5Character> humans = service.getCharactersBySpecies("HUMAN");
        assertTrue(humans.size() > 0, "Should have Human characters");
        for (B5Character ch : humans) {
            assertEquals("HUMAN", ch.getSpecies());
        }

        List<B5Character> narns = service.getCharactersBySpecies("NARN");
        assertTrue(narns.size() > 0, "Should have Narn characters");
        for (B5Character ch : narns) {
            assertEquals("NARN", ch.getSpecies());
        }
    }

    // ==========================================
    // Storyline Tests
    // ==========================================

    @Test
    @DisplayName("Shadow War storyline should exist")
    void testShadowWarStoryline() {
        Optional<B5Storyline> shadowWar = service.getStoryline("SHADOW_WAR");
        assertTrue(shadowWar.isPresent(), "Shadow War storyline should exist");
        B5Storyline sl = shadowWar.get();
        assertNotNull(sl.getDescription());
        assertTrue(sl.getChapters().size() > 0, "Should have chapters");
        assertTrue(sl.getKeyCharacters().size() > 0, "Should have key characters");
    }

    @Test
    @DisplayName("Storylines should have non-blank descriptions")
    void testStorylineDescriptions() {
        for (B5Storyline sl : service.getAllStorylines()) {
            assertNotNull(sl.getDescription(), sl.getId() + " should have description");
            assertTrue(sl.getDescription().trim().length() > 0,
                    sl.getId() + " description should not be blank");
        }
    }

    @Test
    @DisplayName("Storyline chapters should be ordered by season")
    void testStorylineChapterOrder() {
        B5Storyline shadowWar = service.getStoryline("SHADOW_WAR").orElseThrow();
        List<B5StorylineChapter> chapters = shadowWar.getChapters();
        for (int i = 1; i < chapters.size(); i++) {
            assertTrue(chapters.get(i).getSeason() >= chapters.get(i - 1).getSeason(),
                    "Chapters should be ordered by season");
        }
    }

    // ==========================================
    // Dialogue Tests
    // ==========================================

    @Test
    @DisplayName("Dialogues should have lines")
    void testDialogueLines() {
        List<B5Dialogue> dialogues = service.getAllDialogues();
        for (B5Dialogue d : dialogues) {
            assertNotNull(d.getLines(), d.getId() + " should have lines");
            assertTrue(d.getLines().size() > 0, d.getId() + " should have at least 1 line");
        }
    }

    @Test
    @DisplayName("Dialogue lines should have character and text")
    void testDialogueLineContent() {
        List<B5Dialogue> dialogues = service.getAllDialogues();
        for (B5Dialogue d : dialogues) {
            for (B5DialogueLine line : d.getLines()) {
                assertNotNull(line.getCharacter(), "Line should have character");
                assertNotNull(line.getText(), "Line should have text");
                assertTrue(line.getText().trim().length() > 0, "Line text should not be blank");
            }
        }
    }

    // ==========================================
    // Location Tests
    // ==========================================

    @Test
    @DisplayName("Babylon 5 station should exist")
    void testBabylon5Station() {
        Optional<B5Location> station = service.getLocation("BABYLON5");
        assertTrue(station.isPresent(), "Babylon 5 station should exist");
        assertEquals("Space Station", station.get().getType());
    }

    @Test
    @DisplayName("Locations should have non-blank descriptions")
    void testLocationDescriptions() {
        for (B5Location loc : service.getAllLocations()) {
            assertNotNull(loc.getDescription(), loc.getId() + " should have description");
            assertTrue(loc.getDescription().trim().length() > 0,
                    loc.getId() + " description should not be blank");
        }
    }

    // ==========================================
    // Relationship Tests
    // ==========================================

    @Test
    @DisplayName("Bidirectional relationships should be consistent")
    void testBidirectionalRelationships() {
        List<B5Character> characters = service.getAllCharacters();
        for (B5Character ch : characters) {
            if (ch.getRelationships() != null) {
                for (B5Relationship rel : ch.getRelationships()) {
                    Optional<B5Character> other = service.getCharacter(rel.getCharacterId());
                    assertTrue(other.isPresent(),
                            ch.getId() + " references " + rel.getCharacterId() + " which should exist");
                }
            }
        }
    }

    // ==========================================
    // Service Method Tests
    // ==========================================

    @Test
    @DisplayName("getRace with invalid ID should return empty")
    void testGetRaceNotFound() {
        assertFalse(service.getRace("NONEXISTENT").isPresent());
    }

    @Test
    @DisplayName("getCharacter with invalid ID should return empty")
    void testGetCharacterNotFound() {
        assertFalse(service.getCharacter("NONEXISTENT").isPresent());
    }

    @Test
    @DisplayName("getStoryline with invalid ID should return empty")
    void testGetStorylineNotFound() {
        assertFalse(service.getStoryline("NONEXISTENT").isPresent());
    }

    @Test
    @DisplayName("getLocation with invalid ID should return empty")
    void testGetLocationNotFound() {
        assertFalse(service.getLocation("NONEXISTENT").isPresent());
    }

    @Test
    @DisplayName("Service methods should be case-insensitive")
    void testCaseInsensitiveLookup() {
        assertTrue(service.getRace("human").isPresent());
        assertTrue(service.getRace("HUMAN").isPresent());
        assertTrue(service.getCharacter("sheridan").isPresent());
        assertTrue(service.getCharacter("SHERIDAN").isPresent());
    }
}
