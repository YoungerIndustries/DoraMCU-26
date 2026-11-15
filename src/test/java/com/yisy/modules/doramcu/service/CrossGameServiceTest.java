package com.yisy.modules.doramcu.service;

import com.yisy.modules.doramcu.service.CrossGameService.Ability;
import com.yisy.modules.doramcu.service.CrossGameService.CharacterSummary;
import com.yisy.modules.doramcu.service.CrossGameService.CrossGameCharacter;
import com.yisy.modules.doramcu.service.CrossGameService.PortRecord;
import com.yisy.modules.doramcu.service.CrossGameService.Universe;
import org.junit.jupiter.api.*;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CrossGameServiceTest {

    private CrossGameService service;

    @BeforeEach
    void setUp() {
        service = new CrossGameService();
    }

    @Nested
    @Order(1)
    class CreateCharacter {

        @Test
        @Order(1)
        void creates_simple_character() {
            Optional<CrossGameCharacter> result = service.createCharacter(
                    "char1", "Alan", Universe.TRAVELLER, null);
            assertTrue(result.isPresent());
            assertEquals("char1", result.get().getId());
            assertEquals("Alan", result.get().getName());
            assertEquals(Universe.TRAVELLER, result.get().getOriginalUniverse());
            assertEquals(Universe.TRAVELLER, result.get().getCurrentUniverse());
        }

        @Test
        @Order(2)
        void creates_character_with_scores() {
            Map<Ability, Integer> scores = new EnumMap<>(Ability.class);
            scores.put(Ability.STRENGTH, 10);
            scores.put(Ability.DEXTERITY, 8);
            Optional<CrossGameCharacter> result = service.createCharacter(
                    "char2", "Boris", Universe.DND5E, scores);
            assertTrue(result.isPresent());
            assertEquals(10, result.get().getScore(Ability.STRENGTH));
            assertEquals(8, result.get().getScore(Ability.DEXTERITY));
            assertEquals(0, result.get().getScore(Ability.WISDOM));
        }

        @Test
        @Order(3)
        void returns_existing_on_duplicate_id() {
            service.createCharacter("char3", "Carol", Universe.STAR_WARS, null);
            Optional<CrossGameCharacter> result = service.createCharacter(
                    "char3", "Dave", Universe.TRAVELLER, null);
            assertTrue(result.isPresent());
            assertEquals("Carol", result.get().getName());
            assertEquals(Universe.STAR_WARS, result.get().getOriginalUniverse());
        }

        @Test
        @Order(4)
        void creates_from_builder() {
            CrossGameCharacter character = CrossGameCharacter.builder("char4", "Eve")
                    .originalUniverse(Universe.MECHWARRIOR)
                    .score(Ability.STRENGTH, 12)
                    .career("MechWarrior Academy")
                    .build();
            Optional<CrossGameCharacter> result = service.createCharacter(character);
            assertTrue(result.isPresent());
            assertEquals("Eve", result.get().getName());
            assertEquals(Universe.MECHWARRIOR, result.get().getOriginalUniverse());
            assertEquals(1, result.get().getCareerHistory().size());
        }

        @Test
        @Order(5)
        void defaults_to_generic_universe() {
            Optional<CrossGameCharacter> result = service.createCharacter(
                    "char5", "Frank", Universe.GENERIC, null);
            assertTrue(result.isPresent());
            assertEquals(Universe.GENERIC, result.get().getCurrentUniverse());
        }
    }

    @Nested
    @Order(2)
    class GetAndListCharacters {

        @Test
        @Order(1)
        void gets_character_by_id() {
            service.createCharacter("char1", "Alan", Universe.TRAVELLER, null);
            Optional<CrossGameCharacter> result = service.getCharacter("char1");
            assertTrue(result.isPresent());
            assertEquals("Alan", result.get().getName());
        }

        @Test
        @Order(2)
        void returns_empty_for_missing_id() {
            Optional<CrossGameCharacter> result = service.getCharacter("missing");
            assertTrue(result.isEmpty());
        }

        @Test
        @Order(3)
        void lists_all_characters() {
            service.createCharacter("char1", "Alan", Universe.TRAVELLER, null);
            service.createCharacter("char2", "Boris", Universe.DND5E, null);
            List<CharacterSummary> all = service.getAllCharacters();
            assertEquals(2, all.size());
        }

        @Test
        @Order(4)
        void filters_by_current_universe() {
            service.createCharacter("char1", "Alan", Universe.TRAVELLER, null);
            service.createCharacter("char2", "Boris", Universe.DND5E, null);
            List<CharacterSummary> traveller = service.getCharactersByUniverse(Universe.TRAVELLER);
            assertEquals(1, traveller.size());
            assertEquals("Alan", traveller.get(0).getName());
        }

        @Test
        @Order(5)
        void filters_by_original_universe() {
            service.createCharacter("char1", "Alan", Universe.TRAVELLER, null);
            service.createCharacter("char2", "Boris", Universe.DND5E, null);
            List<CharacterSummary> dnd = service.getCharactersByOriginalUniverse(Universe.DND5E);
            assertEquals(1, dnd.size());
            assertEquals("Boris", dnd.get(0).getName());
        }

        @Test
        @Order(6)
        void searches_by_name_case_insensitive() {
            service.createCharacter("char1", "Alan Turing", Universe.TRAVELLER, null);
            service.createCharacter("char2", "Boris", Universe.DND5E, null);
            List<CharacterSummary> results = service.searchCharacters("alan");
            assertEquals(1, results.size());
            assertEquals("Alan Turing", results.get(0).getName());
        }

        @Test
        @Order(7)
        void search_partial_match() {
            service.createCharacter("char1", "Alan Turing", Universe.TRAVELLER, null);
            List<CharacterSummary> results = service.searchCharacters("Turing");
            assertEquals(1, results.size());
        }
    }

    @Nested
    @Order(3)
    class PortCharacter {

        @Test
        @Order(1)
        void ports_character_to_different_universe() {
            service.createCharacter("char1", "Alan", Universe.TRAVELLER, null);
            Optional<PortRecord> result = service.portCharacter("char1", Universe.STAR_WARS, "First port");
            assertTrue(result.isPresent());
            assertEquals(Universe.TRAVELLER, result.get().getFromUniverse());
            assertEquals(Universe.STAR_WARS, result.get().getToUniverse());
            assertEquals("First port", result.get().getNotes());
        }

        @Test
        @Order(2)
        void updates_current_universe_after_port() {
            service.createCharacter("char1", "Alan", Universe.TRAVELLER, null);
            service.portCharacter("char1", Universe.STAR_WARS, null);
            Optional<CrossGameCharacter> charResult = service.getCharacter("char1");
            assertTrue(charResult.isPresent());
            assertEquals(Universe.STAR_WARS, charResult.get().getCurrentUniverse());
            assertNotNull(charResult.get().getLastPortedAt());
        }

        @Test
        @Order(3)
        void returns_empty_for_missing_character() {
            Optional<PortRecord> result = service.portCharacter("missing", Universe.STAR_WARS, null);
            assertTrue(result.isEmpty());
        }

        @Test
        @Order(4)
        void returns_empty_when_already_in_universe() {
            service.createCharacter("char1", "Alan", Universe.TRAVELLER, null);
            Optional<PortRecord> result = service.portCharacter("char1", Universe.TRAVELLER, null);
            assertTrue(result.isEmpty());
        }

        @Test
        @Order(5)
        void records_port_history() {
            service.createCharacter("char1", "Alan", Universe.TRAVELLER, null);
            service.portCharacter("char1", Universe.STAR_WARS, "To SW");
            service.portCharacter("char1", Universe.DND5E, "To D&D");
            List<PortRecord> history = service.getPortHistory("char1");
            assertEquals(2, history.size());
            assertEquals(Universe.STAR_WARS, history.get(0).getToUniverse());
            assertEquals(Universe.DND5E, history.get(1).getToUniverse());
        }

        @Test
        @Order(6)
        void maps_scores_on_port() {
            Map<Ability, Integer> scores = new EnumMap<>(Ability.class);
            scores.put(Ability.STRENGTH, 10);
            service.createCharacter("char1", "Alan", Universe.TRAVELLER, scores);
            Optional<PortRecord> result = service.portCharacter("char1", Universe.STAR_TREK, null);
            assertTrue(result.isPresent());
            assertTrue(result.get().getToScores().containsKey(Ability.STRENGTH));
        }

        @Test
        @Order(7)
        void port_record_has_from_scores() {
            Map<Ability, Integer> scores = new EnumMap<>(Ability.class);
            scores.put(Ability.STRENGTH, 10);
            scores.put(Ability.DEXTERITY, 8);
            service.createCharacter("char1", "Alan", Universe.TRAVELLER, scores);
            Optional<PortRecord> result = service.portCharacter("char1", Universe.STAR_WARS, null);
            assertTrue(result.isPresent());
            assertEquals(10, result.get().getFromScores().get(Ability.STRENGTH));
            assertEquals(8, result.get().getFromScores().get(Ability.DEXTERITY));
        }
    }

    @Nested
    @Order(4)
    class ScoreMapping {

        @Test
        @Order(1)
        void maps_to_traveller_range() {
            Map<Ability, Integer> scores = new EnumMap<>(Ability.class);
            scores.put(Ability.STRENGTH, 10);
            Map<Ability, Integer> mapped = service.mapScoresToUniverse(scores, Universe.TRAVELLER);
            assertTrue(mapped.get(Ability.STRENGTH) >= 2);
            assertTrue(mapped.get(Ability.STRENGTH) <= 12);
        }

        @Test
        @Order(2)
        void maps_to_dnd5e_range() {
            Map<Ability, Integer> scores = new EnumMap<>(Ability.class);
            scores.put(Ability.STRENGTH, 15);
            Map<Ability, Integer> mapped = service.mapScoresToUniverse(scores, Universe.DND5E);
            assertTrue(mapped.get(Ability.STRENGTH) >= 1);
            assertTrue(mapped.get(Ability.STRENGTH) <= 20);
        }

        @Test
        @Order(3)
        void maps_to_star_trek_range() {
            Map<Ability, Integer> scores = new EnumMap<>(Ability.class);
            scores.put(Ability.STRENGTH, 10);
            Map<Ability, Integer> mapped = service.mapScoresToUniverse(scores, Universe.STAR_TREK);
            assertTrue(mapped.get(Ability.STRENGTH) >= 0);
            assertTrue(mapped.get(Ability.STRENGTH) <= 5);
        }

        @Test
        @Order(4)
        void maps_to_star_wars_range() {
            Map<Ability, Integer> scores = new EnumMap<>(Ability.class);
            scores.put(Ability.STRENGTH, 10);
            Map<Ability, Integer> mapped = service.mapScoresToUniverse(scores, Universe.STAR_WARS);
            assertTrue(mapped.get(Ability.STRENGTH) >= 0);
            assertTrue(mapped.get(Ability.STRENGTH) <= 10);
        }

        @Test
        @Order(5)
        void maps_to_mechwarrior_range() {
            Map<Ability, Integer> scores = new EnumMap<>(Ability.class);
            scores.put(Ability.STRENGTH, 10);
            Map<Ability, Integer> mapped = service.mapScoresToUniverse(scores, Universe.MECHWARRIOR);
            assertTrue(mapped.get(Ability.STRENGTH) >= 0);
            assertTrue(mapped.get(Ability.STRENGTH) <= 10);
        }

        @Test
        @Order(6)
        void generic_keeps_score_as_is() {
            Map<Ability, Integer> scores = new EnumMap<>(Ability.class);
            scores.put(Ability.STRENGTH, 10);
            Map<Ability, Integer> mapped = service.mapScoresToUniverse(scores, Universe.GENERIC);
            assertEquals(10, mapped.get(Ability.STRENGTH));
        }

        @Test
        @Order(7)
        void maps_all_abilities() {
            Map<Ability, Integer> scores = new EnumMap<>(Ability.class);
            for (Ability ability : Ability.values()) {
                scores.put(ability, 10);
            }
            Map<Ability, Integer> mapped = service.mapScoresToUniverse(scores, Universe.TRAVELLER);
            assertEquals(6, mapped.size());
            for (Ability ability : Ability.values()) {
                assertTrue(mapped.containsKey(ability));
            }
        }
    }

    @Nested
    @Order(5)
    class CareerManagement {

        @Test
        @Order(1)
        void adds_career_entry() {
            service.createCharacter("char1", "Alan", Universe.TRAVELLER, null);
            assertTrue(service.addCareer("char1", "Soldier"));
            Optional<CrossGameCharacter> result = service.getCharacter("char1");
            assertTrue(result.isPresent());
            assertEquals(1, result.get().getCareerHistory().size());
            assertEquals("Soldier", result.get().getCareerHistory().get(0));
        }

        @Test
        @Order(2)
        void adds_multiple_careers() {
            service.createCharacter("char1", "Alan", Universe.TRAVELLER, null);
            service.addCareer("char1", "Soldier");
            service.addCareer("char1", "Explorer");
            service.addCareer("char1", "Merchant");
            Optional<CrossGameCharacter> result = service.getCharacter("char1");
            assertTrue(result.isPresent());
            assertEquals(3, result.get().getCareerHistory().size());
        }

        @Test
        @Order(3)
        void returns_false_for_missing_character() {
            assertFalse(service.addCareer("missing", "Soldier"));
        }
    }

    @Nested
    @Order(6)
    class UpdateScores {

        @Test
        @Order(1)
        void updates_scores() {
            service.createCharacter("char1", "Alan", Universe.TRAVELLER, null);
            Map<Ability, Integer> scores = new EnumMap<>(Ability.class);
            scores.put(Ability.STRENGTH, 12);
            assertTrue(service.updateScores("char1", scores));
            Optional<CrossGameCharacter> result = service.getCharacter("char1");
            assertTrue(result.isPresent());
            assertEquals(12, result.get().getScore(Ability.STRENGTH));
        }

        @Test
        @Order(2)
        void returns_false_for_missing_character() {
            Map<Ability, Integer> scores = new EnumMap<>(Ability.class);
            scores.put(Ability.STRENGTH, 12);
            assertFalse(service.updateScores("missing", scores));
        }
    }

    @Nested
    @Order(7)
    class UniverseData {

        @Test
        @Order(1)
        void sets_universe_specific_data() {
            service.createCharacter("char1", "Alan", Universe.TRAVELLER, null);
            Map<String, Object> data = new HashMap<>();
            data.put("shipClass", "Mongoose");
            data.put("credits", 1000);
            assertTrue(service.setUniverseData("char1", Universe.TRAVELLER, data));
            Optional<CrossGameCharacter> result = service.getCharacter("char1");
            assertTrue(result.isPresent());
            Map<String, Object> retrieved = result.get().getUniverseSpecificData().get(Universe.TRAVELLER);
            assertEquals("Mongoose", retrieved.get("shipClass"));
        }

        @Test
        @Order(2)
        void returns_false_for_missing_character() {
            Map<String, Object> data = new HashMap<>();
            assertFalse(service.setUniverseData("missing", Universe.TRAVELLER, data));
        }
    }

    @Nested
    @Order(8)
    class RemoveCharacter {

        @Test
        @Order(1)
        void removes_character() {
            service.createCharacter("char1", "Alan", Universe.TRAVELLER, null);
            assertTrue(service.removeCharacter("char1"));
            Optional<CrossGameCharacter> result = service.getCharacter("char1");
            assertTrue(result.isEmpty());
        }

        @Test
        @Order(2)
        void returns_false_for_missing() {
            assertFalse(service.removeCharacter("missing"));
        }

        @Test
        @Order(3)
        void removes_port_history() {
            service.createCharacter("char1", "Alan", Universe.TRAVELLER, null);
            service.portCharacter("char1", Universe.STAR_WARS, null);
            service.removeCharacter("char1");
            List<PortRecord> history = service.getPortHistory("char1");
            assertTrue(history.isEmpty());
        }
    }

    @Nested
    @Order(9)
    class Statistics {

        @Test
        @Order(1)
        void returns_total_characters() {
            service.createCharacter("char1", "Alan", Universe.TRAVELLER, null);
            service.createCharacter("char2", "Boris", Universe.DND5E, null);
            Map<String, Object> stats = service.getStatistics();
            assertEquals(2, stats.get("totalCharacters"));
        }

        @Test
        @Order(2)
        void returns_by_current_universe() {
            service.createCharacter("char1", "Alan", Universe.TRAVELLER, null);
            service.createCharacter("char2", "Boris", Universe.TRAVELLER, null);
            service.createCharacter("char3", "Carol", Universe.DND5E, null);
            Map<String, Object> stats = service.getStatistics();
            @SuppressWarnings("unchecked")
            Map<Universe, Long> byCurrent = (Map<Universe, Long>) stats.get("byCurrentUniverse");
            assertEquals(2L, byCurrent.get(Universe.TRAVELLER));
            assertEquals(1L, byCurrent.get(Universe.DND5E));
        }

        @Test
        @Order(3)
        void returns_total_ports() {
            service.createCharacter("char1", "Alan", Universe.TRAVELLER, null);
            service.portCharacter("char1", Universe.STAR_WARS, null);
            service.portCharacter("char1", Universe.DND5E, null);
            Map<String, Object> stats = service.getStatistics();
            assertEquals(2L, stats.get("totalPorts"));
        }

        @Test
        @Order(4)
        void returns_characters_with_ports() {
            service.createCharacter("char1", "Alan", Universe.TRAVELLER, null);
            service.createCharacter("char2", "Boris", Universe.DND5E, null);
            service.portCharacter("char1", Universe.STAR_WARS, null);
            Map<String, Object> stats = service.getStatistics();
            assertEquals(1L, stats.get("charactersWithPorts"));
        }
    }

    @Nested
    @Order(10)
    class EnumTests {

        @Test
        @Order(1)
        void lists_all_universes() {
            List<Universe> universes = service.getSupportedUniverses();
            assertEquals(Universe.values().length, universes.size());
        }

        @Test
        @Order(2)
        void lists_all_abilities() {
            List<Ability> abilities = service.getAbilities();
            assertEquals(Ability.values().length, abilities.size());
        }

        @Test
        @Order(3)
        void universe_has_name_and_description() {
            assertNotNull(Universe.TRAVELLER.getName());
            assertNotNull(Universe.TRAVELLER.getDescription());
            assertFalse(Universe.TRAVELLER.getName().isBlank());
        }

        @Test
        @Order(4)
        void ability_has_abbreviation() {
            assertEquals("STR", Ability.STRENGTH.getAbbreviation());
            assertEquals("DEX", Ability.DEXTERITY.getAbbreviation());
            assertEquals("INT", Ability.INTELLIGENCE.getAbbreviation());
        }
    }

    @Nested
    @Order(11)
    class BuilderTests {

        @Test
        @Order(1)
        void port_record_builder() {
            PortRecord record = PortRecord.builder()
                    .fromUniverse(Universe.TRAVELLER)
                    .toUniverse(Universe.STAR_WARS)
                    .notes("Test port")
                    .fromScore(Ability.STRENGTH, 10)
                    .toScore(Ability.STRENGTH, 5)
                    .build();
            assertEquals(Universe.TRAVELLER, record.getFromUniverse());
            assertEquals(Universe.STAR_WARS, record.getToUniverse());
            assertEquals("Test port", record.getNotes());
            assertEquals(10, record.getFromScores().get(Ability.STRENGTH));
            assertEquals(5, record.getToScores().get(Ability.STRENGTH));
        }

        @Test
        @Order(2)
        void character_builder_with_all_fields() {
            Instant now = Instant.now();
            CrossGameCharacter character = CrossGameCharacter.builder("char1", "Alan")
                    .originalUniverse(Universe.TRAVELLER)
                    .originalCharacterId("orig-123")
                    .score(Ability.STRENGTH, 10)
                    .score(Ability.DEXTERITY, 8)
                    .career("Soldier")
                    .career("Explorer")
                    .createdAt(now)
                    .creationOrder(1)
                    .currentUniverse(Universe.STAR_WARS)
                    .build();
            assertEquals("char1", character.getId());
            assertEquals("Alan", character.getName());
            assertEquals(Universe.TRAVELLER, character.getOriginalUniverse());
            assertEquals("orig-123", character.getOriginalCharacterId());
            assertEquals(Universe.STAR_WARS, character.getCurrentUniverse());
            assertEquals(2, character.getCareerHistory().size());
            assertEquals(1, character.getCreationOrder());
        }

        @Test
        @Order(3)
        void character_summary() {
            CrossGameCharacter character = CrossGameCharacter.builder("char1", "Alan")
                    .originalUniverse(Universe.TRAVELLER)
                    .currentUniverse(Universe.STAR_WARS)
                    .career("Soldier")
                    .build();
            CharacterSummary summary = new CharacterSummary(character, 2);
            assertEquals("char1", summary.getId());
            assertEquals("Alan", summary.getName());
            assertEquals(Universe.TRAVELLER, summary.getOriginalUniverse());
            assertEquals(Universe.STAR_WARS, summary.getCurrentUniverse());
            assertEquals(1, summary.getCareerCount());
            assertEquals(2, summary.getPortCount());
        }
    }

    @Nested
    @Order(12)
    class IntegrationScenarios {

        @Test
        @Order(1)
        void full_lifecycle_create_port_career() {
            service.createCharacter("char1", "Alan", Universe.TRAVELLER, null);
            service.addCareer("char1", "Soldier");
            service.portCharacter("char1", Universe.STAR_WARS, "Jedi training");
            service.addCareer("char1", "Jedi Knight");
            service.portCharacter("char1", Universe.DND5E, "Planar travel");
            service.addCareer("char1", "Adventurer");
            Optional<CrossGameCharacter> result = service.getCharacter("char1");
            assertTrue(result.isPresent());
            assertEquals(Universe.DND5E, result.get().getCurrentUniverse());
            assertEquals(3, result.get().getCareerHistory().size());
            List<PortRecord> history = service.getPortHistory("char1");
            assertEquals(2, history.size());
        }

        @Test
        @Order(2)
        void multi_universe_character_fleet() {
            service.createCharacter("char1", "Alan", Universe.TRAVELLER, null);
            service.createCharacter("char2", "Boris", Universe.STAR_TREK, null);
            service.createCharacter("char3", "Carol", Universe.STAR_WARS, null);
            service.portCharacter("char2", Universe.TRAVELLER, "Cross-universe mission");
            List<CharacterSummary> traveller = service.getCharactersByUniverse(Universe.TRAVELLER);
            assertEquals(2, traveller.size());
        }

        @Test
        @Order(3)
        void clear_all_resets_state() {
            service.createCharacter("char1", "Alan", Universe.TRAVELLER, null);
            service.createCharacter("char2", "Boris", Universe.DND5E, null);
            service.portCharacter("char1", Universe.STAR_WARS, null);
            service.clearAll();
            assertEquals(0, service.getAllCharacters().size());
            assertEquals(0, service.getPortHistory("char1").size());
            Map<String, Object> stats = service.getStatistics();
            assertEquals(0, stats.get("totalCharacters"));
        }

        @Test
        @Order(4)
        void character_with_all_abilities_mapped() {
            Map<Ability, Integer> scores = new EnumMap<>(Ability.class);
            for (Ability ability : Ability.values()) {
                scores.put(ability, 10);
            }
            service.createCharacter("char1", "Alan", Universe.TRAVELLER, scores);
            service.portCharacter("char1", Universe.STAR_TREK, null);
            Optional<PortRecord> record = service.getPortHistory("char1").stream().findFirst();
            assertTrue(record.isPresent());
            assertEquals(6, record.get().getFromScores().size());
            assertEquals(6, record.get().getToScores().size());
        }
    }
}
