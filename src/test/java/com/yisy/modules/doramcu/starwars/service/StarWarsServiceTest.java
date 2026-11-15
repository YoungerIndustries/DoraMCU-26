package com.yisy.modules.doramcu.starwars.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yisy.modules.doramcu.starwars.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for StarWarsService.
 * Tests all data loading and query methods.
 */
class StarWarsServiceTest {

    private StarWarsService service;

    @BeforeEach
    void setUp() {
        service = new StarWarsService(new ObjectMapper());
    }

    // ==========================================
    // Ground Units
    // ==========================================

    @Nested
    @DisplayName("Ground Units")
    class GroundUnits {

        @Test
        @DisplayName("Loads ground units from JSON")
        void loadsGroundUnits() {
            List<Unit> units = service.getGroundUnits();
            assertFalse(units.isEmpty(), "Should load ground units");
            assertTrue(units.size() >= 10, "Should have at least 10 ground units");
        }

        @Test
        @DisplayName("Clone trooper has correct stats")
        void cloneTrooperStats() {
            Optional<Unit> unit = service.getUnitById("clone_trooper");
            assertTrue(unit.isPresent(), "Clone trooper should exist");
            Unit clone = unit.get();
            assertEquals("clone_trooper", clone.id());
            assertEquals("Clone Trooper", clone.name());
            assertEquals("galactic_republic", clone.faction());
            assertEquals("infantry", clone.type());
            assertNotNull(clone.stats());
            assertEquals(2, clone.stats().attack());
            assertEquals(2, clone.stats().defense());
            assertEquals(3, clone.stats().hull());
            assertEquals(3, clone.stats().wounds());
        }

        @Test
        @DisplayName("AT-AT walker has high stats")
        void atAtWalkerStats() {
            Optional<Unit> unit = service.getUnitById("at_at_walker");
            assertTrue(unit.isPresent(), "AT-AT should exist");
            Unit atAt = unit.get();
            assertEquals("galactic_empire", atAt.faction());
            assertEquals("walker", atAt.type());
            assertTrue(atAt.stats().attack() >= 5, "AT-AT should have high attack");
            assertTrue(atAt.stats().hull() >= 10, "AT-AT should have high hull");
        }

        @Test
        @DisplayName("B1 battle droid has low stats")
        void b1BattleDroidStats() {
            Optional<Unit> unit = service.getUnitById("b1 Battle Droid");
            assertTrue(unit.isPresent(), "B1 Battle Droid should exist");
            Unit b1 = unit.get();
            assertEquals("separatist_alliance", b1.faction());
            assertEquals("droid_infantry", b1.type());
            assertEquals(1, b1.stats().attack());
            assertEquals(1, b1.stats().defense());
        }

        @Test
        @DisplayName("Rebel commando is combat unit")
        void rebelCommandoIsCombatUnit() {
            Optional<Unit> unit = service.getUnitById("rebel_commando");
            assertTrue(unit.isPresent());
            assertTrue(unit.get().isCombatUnit());
        }

        @Test
        @DisplayName("Units have weapons list")
        void unitsHaveWeapons() {
            Optional<Unit> unit = service.getUnitById("clone_commando");
            assertTrue(unit.isPresent());
            Unit commando = unit.get();
            assertFalse(commando.weapons().isEmpty(), "Commando should have weapons");
            assertTrue(commando.weapons().contains("DC-15A Blaster Rifle"));
        }

        @Test
        @DisplayName("Units have abilities list")
        void unitsHaveAbilities() {
            Optional<Unit> unit = service.getUnitById("stormtrooper");
            assertTrue(unit.isPresent());
            Unit storm = unit.get();
            assertFalse(storm.abilities().isEmpty(), "Stormtrooper should have abilities");
        }

        @Test
        @DisplayName("Ground vehicles loaded separately")
        void groundVehiclesLoaded() {
            List<Unit> vehicles = service.getGroundVehicles();
            assertFalse(vehicles.isEmpty(), "Should load ground vehicles");
            assertTrue(vehicles.stream().anyMatch(u -> u.id().equals("speeder_bike")));
            assertTrue(vehicles.stream().anyMatch(u -> u.id().equals("at_at_rebel")));
        }
    }

    // ==========================================
    // Starships
    // ==========================================

    @Nested
    @DisplayName("Starships")
    class Starships {

        @Test
        @DisplayName("Loads starships from JSON")
        void loadsStarships() {
            List<Unit> ships = service.getStarships();
            assertFalse(ships.isEmpty(), "Should load starships");
            assertTrue(ships.size() >= 10, "Should have at least 10 starships");
        }

        @Test
        @DisplayName("X-Wing is a starship")
        void xWingIsStarship() {
            Optional<Unit> unit = service.getUnitById("x_wing");
            assertTrue(unit.isPresent(), "X-Wing should exist");
            Unit xwing = unit.get();
            assertTrue(xwing.isStarship());
            assertEquals("rebel_alliance", xwing.faction());
            assertEquals("starfighter", xwing.type());
        }

        @Test
        @DisplayName("TIE fighter has no shields")
        void tieFighterNoShields() {
            Optional<Unit> unit = service.getUnitById("tie_fighter");
            assertTrue(unit.isPresent());
            Unit tie = unit.get();
            assertEquals(0, tie.stats().shields());
            assertEquals("galactic_empire", tie.faction());
        }

        @Test
        @DisplayName("Millennium Falcon has high speed")
        void millenniumFalconSpeed() {
            Optional<Unit> unit = service.getUnitById("millennium_falcon");
            assertTrue(unit.isPresent());
            Unit falcon = unit.get();
            assertTrue(falcon.stats().speed() >= 10, "Falcon should have high speed");
            assertEquals("freighter", falcon.type());
        }

        @Test
        @DisplayName("Death Star is a space station")
        void deathStarIsSpaceStation() {
            Optional<Unit> unit = service.getUnitById("death_star");
            assertTrue(unit.isPresent());
            Unit ds = unit.get();
            assertEquals("space_station", ds.type());
            assertTrue(ds.stats().hull() >= 100, "Death Star should have massive hull");
        }

        @Test
        @DisplayName("Star Destroyer is a capital ship")
        void starDestroyerIsCapitalShip() {
            Optional<Unit> unit = service.getUnitById("star_destroyer");
            assertTrue(unit.isPresent());
            Unit sd = unit.get();
            assertEquals("capital_ship", sd.type());
            assertTrue(sd.isStarship());
        }

        @Test
        @DisplayName("Vulture droid fighter loaded")
        void vultureDroidLoaded() {
            Optional<Unit> unit = service.getUnitById("vulture_droid");
            assertTrue(unit.isPresent());
            assertEquals("separatist_alliance", unit.get().faction());
        }

        @Test
        @DisplayName("TIE Silencer has shields")
        void tieSilencerHasShields() {
            Optional<Unit> unit = service.getUnitById("tie_silencer");
            assertTrue(unit.isPresent());
            Unit silencer = unit.get();
            assertTrue(silencer.stats().shields() > 0, "TIE Silencer should have shields");
            assertEquals("first_order", silencer.faction());
        }
    }

    // ==========================================
    // Characters
    // ==========================================

    @Nested
    @DisplayName("Characters")
    class Characters {

        @Test
        @DisplayName("Loads characters from JSON")
        void loadsCharacters() {
            List<Unit> characters = service.getCharacters();
            assertFalse(characters.isEmpty(), "Should load characters");
            assertTrue(characters.size() >= 10, "Should have at least 10 characters");
        }

        @Test
        @DisplayName("Darth Vader is a commander")
        void darthVaderIsCommander() {
            Optional<Unit> unit = service.getUnitById("darth_vader");
            assertTrue(unit.isPresent(), "Darth Vader should exist");
            Unit vader = unit.get();
            assertEquals("commander", vader.type());
            assertTrue(vader.isCharacter());
            assertEquals("galactic_empire", vader.faction());
        }

        @Test
        @DisplayName("Darth Vader has high Force power")
        void darthVaderForcePower() {
            Optional<Unit> unit = service.getUnitById("darth_vader");
            assertTrue(unit.isPresent());
            Unit vader = unit.get();
            assertEquals(10, vader.stats().forcePower());
        }

        @Test
        @DisplayName("Luke Skywalker is Force-sensitive")
        void lukeSkywalkerForceSensitive() {
            Optional<Unit> unit = service.getUnitById("luke_skywalker");
            assertTrue(unit.isPresent());
            Unit luke = unit.get();
            assertTrue(luke.stats().forcePower() > 0);
            assertEquals("rebel_alliance", luke.faction());
        }

        @Test
        @DisplayName("Yoda has highest Force power")
        void yodaForcePower() {
            Optional<Unit> unit = service.getUnitById("yoda");
            assertTrue(unit.isPresent());
            Unit yoda = unit.get();
            assertEquals(10, yoda.stats().forcePower());
            assertEquals("jedi_order", yoda.faction());
        }

        @Test
        @DisplayName("Han Solo has no Force power")
        void hanSoloNoForce() {
            Optional<Unit> unit = service.getUnitById("han_solo");
            assertTrue(unit.isPresent());
            Unit han = unit.get();
            assertEquals(0, han.stats().forcePower());
        }

        @Test
        @DisplayName("General Grievous loaded")
        void generalGrievousLoaded() {
            Optional<Unit> unit = service.getUnitById("general_grievous");
            assertTrue(unit.isPresent());
            assertEquals("separatist_alliance", unit.get().faction());
        }

        @Test
        @DisplayName("Boba Fett loaded")
        void bobafettLoaded() {
            Optional<Unit> unit = service.getUnitById("bobafett");
            assertTrue(unit.isPresent());
            assertEquals("scum_villainy", unit.get().faction());
        }

        @Test
        @DisplayName("Emperor Palpatine has max Force power")
        void emperorPalpatineForcePower() {
            Optional<Unit> unit = service.getUnitById("emperor_palpatine");
            assertTrue(unit.isPresent());
            Unit palpatine = unit.get();
            assertEquals(10, palpatine.stats().forcePower());
            assertEquals("sith", palpatine.faction());
        }
    }

    // ==========================================
    // Force Users
    // ==========================================

    @Nested
    @DisplayName("Force Users")
    class ForceUsers {

        @Test
        @DisplayName("Returns only Force-sensitive characters")
        void returnsForceUsers() {
            List<Unit> forceUsers = service.getForceUsers();
            assertFalse(forceUsers.isEmpty(), "Should have Force users");
            forceUsers.forEach(u -> {
                assertNotNull(u.stats());
                assertTrue(u.stats().forcePower() > 0, u.name() + " should have Force power > 0");
            });
        }

        @Test
        @DisplayName("Han Solo is not a Force user")
        void hanSoloNotForceUser() {
            List<Unit> forceUsers = service.getForceUsers();
            assertFalse(forceUsers.stream().anyMatch(u -> u.id().equals("han_solo")));
        }

        @Test
        @DisplayName("Darth Vader is a Force user")
        void darthVaderIsForceUser() {
            List<Unit> forceUsers = service.getForceUsers();
            assertTrue(forceUsers.stream().anyMatch(u -> u.id().equals("darth_vader")));
        }

        @Test
        @DisplayName("Multiple Force users exist")
        void multipleForceUsers() {
            List<Unit> forceUsers = service.getForceUsers();
            assertTrue(forceUsers.size() >= 5, "Should have at least 5 Force users");
        }
    }

    // ==========================================
    // All Units
    // ==========================================

    @Nested
    @DisplayName("All Units")
    class AllUnits {

        @Test
        @DisplayName("Returns all units across categories")
        void getAllUnits() {
            List<Unit> all = service.getAllUnits();
            List<Unit> ground = service.getGroundUnits();
            List<Unit> ships = service.getStarships();
            List<Unit> vehicles = service.getGroundVehicles();
            List<Unit> chars = service.getCharacters();

            int expected = ground.size() + ships.size() + vehicles.size() + chars.size();
            assertEquals(expected, all.size(), "All units should be sum of all categories");
        }

        @Test
        @DisplayName("Get unit by ID works across all categories")
        void getUnitByIdAcrossCategories() {
            assertTrue(service.getUnitById("clone_trooper").isPresent());
            assertTrue(service.getUnitById("x_wing").isPresent());
            assertTrue(service.getUnitById("speeder_bike").isPresent());
            assertTrue(service.getUnitById("darth_vader").isPresent());
        }

        @Test
        @DisplayName("Non-existent unit returns empty")
        void nonExistentUnitReturnsEmpty() {
            assertFalse(service.getUnitById("nonexistent").isPresent());
        }

        @Test
        @DisplayName("Combat units have attack > 0")
        void combatUnitsHaveAttack() {
            List<Unit> combat = service.getCombatUnits();
            combat.forEach(u -> assertTrue(u.isCombatUnit(), u.name() + " should be combat unit"));
        }

        @Test
        @DisplayName("Units by faction filters correctly")
        void unitsByFaction() {
            List<Unit> empire = service.getUnitsByFaction("galactic_empire");
            assertFalse(empire.isEmpty(), "Empire should have units");
            empire.forEach(u -> assertEquals("galactic_empire", u.faction()));
        }

        @Test
        @DisplayName("Rebel units exist")
        void rebelUnitsExist() {
            List<Unit> rebels = service.getUnitsByFaction("rebel_alliance");
            assertFalse(rebels.isEmpty(), "Rebels should have units");
        }

        @Test
        @DisplayName("Separatist units exist")
        void separatistUnitsExist() {
            List<Unit> cis = service.getUnitsByFaction("separatist_alliance");
            assertFalse(cis.isEmpty(), "Separatists should have units");
        }
    }

    // ==========================================
    // Factions
    // ==========================================

    @Nested
    @DisplayName("Factions")
    class Factions {

        @Test
        @DisplayName("Loads factions from JSON")
        void loadsFactions() {
            List<Faction> factions = service.getFactions();
            assertFalse(factions.isEmpty(), "Should load factions");
            assertTrue(factions.size() >= 8, "Should have at least 8 factions");
        }

        @Test
        @DisplayName("Galactic Empire faction details")
        void galacticEmpireDetails() {
            Optional<Faction> faction = service.getFactionById("galactic_empire");
            assertTrue(faction.isPresent());
            Faction empire = faction.get();
            assertEquals("Galactic Empire", empire.name());
            assertEquals("galactic_empire", empire.id());
            assertNotNull(empire.ideology());
            assertFalse(empire.strengths().isEmpty());
            assertFalse(empire.keyLeaders().isEmpty());
        }

        @Test
        @DisplayName("Rebel Alliance faction details")
        void rebelAllianceDetails() {
            Optional<Faction> faction = service.getFactionById("rebel_alliance");
            assertTrue(faction.isPresent());
            Faction rebel = faction.get();
            assertEquals("Rebel Alliance (Alliance to Restore the Republic)", rebel.name());
            assertNotNull(rebel.homeWorld());
        }

        @Test
        @DisplayName("Sith faction has Force abilities")
        void sithFaction() {
            Optional<Faction> faction = service.getFactionById("sith");
            assertTrue(faction.isPresent());
        }

        @Test
        @DisplayName("Jedi Order faction loaded")
        void jediOrderLoaded() {
            Optional<Faction> faction = service.getFactionById("jedi_order");
            assertTrue(faction.isPresent());
            Faction jedi = faction.get();
            assertEquals("Jedi Order", jedi.name());
        }

        @Test
        @DisplayName("First Order faction loaded")
        void firstOrderLoaded() {
            Optional<Faction> faction = service.getFactionById("first_order");
            assertTrue(faction.isPresent());
        }

        @Test
        @DisplayName("Resistance faction loaded")
        void resistanceLoaded() {
            Optional<Faction> faction = service.getFactionById("resistance");
            assertTrue(faction.isPresent());
        }

        @Test
        @DisplayName("Non-existent faction returns empty")
        void nonExistentFactionReturnsEmpty() {
            assertFalse(service.getFactionById("nonexistent").isPresent());
        }

        @Test
        @DisplayName("Allies query works")
        void alliesQuery() {
            List<Faction> allies = service.getAlliesOf("rebel_alliance");
            // Rebels have allies like Jedi Order, Smugglers
            assertNotNull(allies);
        }

        @Test
        @DisplayName("Faction has military branches")
        void factionHasMilitaryBranches() {
            Optional<Faction> faction = service.getFactionById("galactic_empire");
            assertTrue(faction.isPresent());
            assertFalse(faction.get().militaryBranches().isEmpty());
        }

        @Test
        @DisplayName("Faction has key leaders")
        void factionHasKeyLeaders() {
            Optional<Faction> faction = service.getFactionById("galactic_empire");
            assertTrue(faction.isPresent());
            Faction empire = faction.get();
            assertTrue(empire.keyLeaders().stream()
                    .anyMatch(l -> l.name().contains("Palpatine")));
            assertTrue(empire.keyLeaders().stream()
                    .anyMatch(l -> l.name().contains("Vader")));
        }
    }

    // ==========================================
    // Scenarios
    // ==========================================

    @Nested
    @DisplayName("Scenarios")
    class Scenarios {

        @Test
        @DisplayName("Loads 10 scenarios from JSON")
        void loadsScenarios() {
            List<Scenario> scenarios = service.getScenarios();
            assertFalse(scenarios.isEmpty(), "Should load scenarios");
            assertTrue(scenarios.size() >= 10, "Should have 10 scenarios (got " + scenarios.size() + ")");
        }

        @Test
        @DisplayName("Get scenario by ID")
        void getScenarioById() {
            Optional<Scenario> yavin = service.getScenarioById("battle_of_yavin");
            assertTrue(yavin.isPresent(), "Battle of Yavin should exist");
            assertEquals("battle_of_yavin", yavin.get().id());
        }

        @Test
        @DisplayName("Scenarios have rich setup data")
        void scenarioSetupData() {
            Optional<Scenario> yavin = service.getScenarioById("battle_of_yavin");
            if (yavin.isPresent()) {
                Scenario s = yavin.get();
                assertNotNull(s.map());
                assertEquals(2, s.players());
                assertNotNull(s.sourceGame());
                assertFalse(s.rebelForces().isEmpty(), "Should have rebel forces");
                assertFalse(s.imperialForces().isEmpty(), "Should have imperial forces");
            }
        }

        @Test
        @DisplayName("Scenarios have objectives")
        void scenarioObjectives() {
            Optional<Scenario> yavin = service.getScenarioById("battle_of_yavin");
            if (yavin.isPresent()) {
                Scenario s = yavin.get();
                assertNotNull(s.rebelObjective());
                assertNotNull(s.imperialObjective());
            }
        }

        @Test
        @DisplayName("Scenarios have victory conditions")
        void scenarioVictoryConditions() {
            Optional<Scenario> yavin = service.getScenarioById("battle_of_yavin");
            if (yavin.isPresent()) {
                Scenario s = yavin.get();
                assertFalse(s.victoryConditions().isEmpty(), "Should have victory conditions");
            }
        }

        @Test
        @DisplayName("Scenarios have playtime")
        void scenarioPlaytime() {
            List<Scenario> scenarios = service.getScenarios();
            boolean hasPlaytime = scenarios.stream().anyMatch(s -> !s.playtime().isEmpty());
            assertTrue(hasPlaytime, "Some scenarios should have playtime");
        }

        @Test
        @DisplayName("Scenarios sorted by difficulty")
        void scenariosByDifficulty() {
            List<Scenario> sorted = service.getScenariosByDifficulty();
            assertFalse(sorted.isEmpty());
        }

        @Test
        @DisplayName("Scenarios by era filter")
        void scenariosByEra() {
            List<Scenario> prequel = service.getScenariosByEra("Prequel");
            assertNotNull(prequel);
            prequel.forEach(s -> assertTrue(s.era().toLowerCase().contains("prequel")));
        }

        @Test
        @DisplayName("Scenarios by faction filter")
        void scenariosByFaction() {
            List<Scenario> rebel = service.getScenariosByFaction("Rebel");
            assertNotNull(rebel);
        }

        @Test
        @DisplayName("Non-existent scenario returns empty")
        void nonExistentScenarioReturnsEmpty() {
            assertFalse(service.getScenarioById("nonexistent").isPresent());
        }
    }

    // ==========================================
    // Special Abilities
    // ==========================================

    @Nested
    @DisplayName("Special Abilities")
    class SpecialAbilities {

        @Test
        @DisplayName("Loads special abilities from nested JSON structure")
        void abilitiesLoading() {
            List<SpecialAbility> abilities = service.getSpecialAbilities();
            assertNotNull(abilities);
            assertTrue(abilities.size() >= 40, "Should load 40+ abilities (got " + abilities.size() + ")");
        }

        @Test
        @DisplayName("Light side powers loaded correctly")
        void lightSidePowers() {
            List<SpecialAbility> light = service.getLightSidePowers();
            assertNotNull(light);
            assertTrue(light.size() >= 10, "Should have at least 10 light side powers (got " + light.size() + ")");
            light.forEach(a -> {
                assertTrue(a.isLightSide());
                assertEquals("Force Power", a.category());
                assertNotNull(a.effect());
            });
        }

        @Test
        @DisplayName("Dark side powers loaded correctly")
        void darkSidePowers() {
            List<SpecialAbility> dark = service.getDarkSidePowers();
            assertNotNull(dark);
            assertTrue(dark.size() >= 9, "Should have at least 9 dark side powers (got " + dark.size() + ")");
            dark.forEach(a -> {
                assertTrue(a.isDarkSide());
                assertEquals("Force Power", a.category());
            });
        }

        @Test
        @DisplayName("Leader abilities loaded correctly")
        void leaderAbilities() {
            List<SpecialAbility> leaders = service.getLeaderAbilities();
            assertNotNull(leaders);
            assertTrue(!leaders.isEmpty(), "Should have leader abilities");
            leaders.forEach(a -> assertEquals("Leader Ability", a.category()));
        }

        @Test
        @DisplayName("Leader abilities by faction")
        void leaderAbilitiesByFaction() {
            List<SpecialAbility> empire = service.getLeaderAbilitiesByFaction("galactic_empire");
            assertNotNull(empire);
            empire.forEach(a -> assertEquals("galactic_empire", a.side()));
        }

        @Test
        @DisplayName("Unit keywords loaded correctly")
        void unitKeywords() {
            List<SpecialAbility> keywords = service.getUnitKeywords();
            assertNotNull(keywords);
            assertTrue(keywords.size() >= 10, "Should have at least 10 keywords (got " + keywords.size() + ")");
            keywords.forEach(a -> assertEquals("Keyword", a.category()));
        }

        @Test
        @DisplayName("Force powers have cost and effect fields")
        void forcePowerFields() {
            List<SpecialAbility> light = service.getLightSidePowers();
            if (!light.isEmpty()) {
                SpecialAbility first = light.get(0);
                assertNotNull(first.cost());
                assertNotNull(first.effect());
                assertNotNull(first.users());
            }
        }

        @Test
        @DisplayName("Rank supports string values like Master and Unique")
        void rankAsString() {
            List<SpecialAbility> abilities = service.getSpecialAbilities();
            boolean hasMasterRank = abilities.stream()
                    .anyMatch(a -> "Master".equals(a.rank()));
            boolean hasUniqueRank = abilities.stream()
                    .anyMatch(a -> "Unique".equals(a.rank()));
            assertTrue(hasMasterRank || hasUniqueRank, "Should have string rank values");
        }

        @Test
        @DisplayName("Non-existent ability returns empty")
        void nonExistentAbilityReturnsEmpty() {
            assertFalse(service.getAbilityById("nonexistent").isPresent());
        }
    }

    // ==========================================
    // Lore
    // ==========================================

    @Nested
    @DisplayName("Lore")
    class LoreTest {

        @Test
        @DisplayName("Loads lore from nested JSON structure")
        void loadsLore() {
            List<Lore> lore = service.getLore();
            assertNotNull(lore);
            assertTrue(lore.size() >= 40, "Should load 40+ lore entries (got " + lore.size() + ")");
        }

        @Test
        @DisplayName("Loads 13 planets")
        void planetsLoaded() {
            List<Lore> planets = service.getPlanets();
            assertNotNull(planets);
            assertTrue(planets.size() >= 13, "Should have 13 planets (got " + planets.size() + ")");
            planets.forEach(p -> {
                assertEquals("planet", p.category());
                assertNotNull(p.name());
            });
        }

        @Test
        @DisplayName("Planets have regions and notable locations")
        void planetDetails() {
            List<Lore> planets = service.getPlanets();
            boolean hasRegion = planets.stream().anyMatch(p -> !p.region().isEmpty());
            boolean hasLocations = planets.stream().anyMatch(p -> !p.notableLocations().isEmpty());
            assertTrue(hasRegion, "Some planets should have regions");
            assertTrue(hasLocations, "Some planets should have notable locations");
        }

        @Test
        @DisplayName("Loads 6 eras")
        void erasLoaded() {
            List<Lore> eras = service.getEras();
            assertNotNull(eras);
            assertTrue(eras.size() >= 6, "Should have 6 eras (got " + eras.size() + ")");
            eras.forEach(e -> {
                assertEquals("era", e.category());
                assertNotNull(e.period());
            });
        }

        @Test
        @DisplayName("Eras have key events and jedi/sith status")
        void eraDetails() {
            List<Lore> eras = service.getEras();
            boolean hasKeyEvents = eras.stream().anyMatch(e -> !e.keyEvents().isEmpty());
            boolean hasJediStatus = eras.stream().anyMatch(e -> !e.jediStatus().isEmpty());
            assertTrue(hasKeyEvents, "Some eras should have key events");
            assertTrue(hasJediStatus, "Some eras should have jedi status");
        }

        @Test
        @DisplayName("Loads 11 species")
        void speciesLoaded() {
            List<Lore> species = service.getSpecies();
            assertNotNull(species);
            assertTrue(species.size() >= 11, "Should have 11 species (got " + species.size() + ")");
            species.forEach(s -> {
                assertEquals("species", s.category());
                assertNotNull(s.homeworld());
            });
        }

        @Test
        @DisplayName("Species have notable members")
        void speciesDetails() {
            List<Lore> species = service.getSpecies();
            boolean hasMembers = species.stream().anyMatch(s -> !s.notableMembers().isEmpty());
            assertTrue(hasMembers, "Some species should have notable members");
        }

        @Test
        @DisplayName("Loads 10 key events")
        void eventsLoaded() {
            List<Lore> events = service.getEvents();
            assertNotNull(events);
            assertTrue(events.size() >= 10, "Should have 10 key events (got " + events.size() + ")");
            events.forEach(e -> {
                assertEquals("event", e.category());
                assertNotNull(e.date());
            });
        }

        @Test
        @DisplayName("Force info loaded")
        void forceInfoLoaded() {
            List<Lore> force = service.getForceInfo();
            assertNotNull(force);
            assertTrue(!force.isEmpty(), "Should have Force info entries");
        }

        @Test
        @DisplayName("Lightsaber info loaded")
        void lightsaberInfoLoaded() {
            List<Lore> lightsabers = service.getLightsaberInfo();
            assertNotNull(lightsabers);
            assertTrue(!lightsabers.isEmpty(), "Should have lightsaber info");
        }

        @Test
        @DisplayName("Lore by category returns list")
        void loreByCategory() {
            List<Lore> planets = service.getLoreByCategory("planet");
            assertNotNull(planets);
            planets.forEach(p -> assertEquals("planet", p.category()));
        }

        @Test
        @DisplayName("Non-existent category returns empty")
        void nonExistentCategoryReturnsEmpty() {
            List<Lore> result = service.getLoreByCategory("nonexistent");
            assertTrue(result.isEmpty());
        }
    }

    // ==========================================
    // Scenarios
    // ==========================================

    @Nested
    @DisplayName("Scenarios")
    class ScenariosTest {

        @Test
        @DisplayName("Loads scenarios from nested JSON structure")
        void loadsScenarios() {
            List<Scenario> scenarios = service.getScenarios();
            assertNotNull(scenarios);
            assertTrue(scenarios.size() >= 10, "Should load 10+ scenarios (got " + scenarios.size() + ")");
        }

        @Test
        @DisplayName("Scenario has rich setup data")
        void scenarioSetupData() {
            Optional<Scenario> yavin = service.getScenarioById("battle_of_yavin");
            if (yavin.isPresent()) {
                Scenario s = yavin.get();
                assertNotNull(s.map());
                assertTrue(s.players() > 0, "Should have players count");
                assertNotNull(s.rebelForces());
                assertNotNull(s.imperialForces());
                assertNotNull(s.victoryConditions());
            }
        }

        @Test
        @DisplayName("Scenarios filtered by era")
        void scenariosByEra() {
            List<Scenario> original = service.getScenariosByEra("original");
            assertNotNull(original);
            original.forEach(s -> assertNotNull(s.era()));
        }

        @Test
        @DisplayName("Scenarios filtered by faction")
        void scenariosByFaction() {
            List<Scenario> rebels = service.getScenariosByFaction("rebel_alliance");
            assertNotNull(rebels);
        }

        @Test
        @DisplayName("Scenario has playtime and source game")
        void scenarioMetadata() {
            List<Scenario> scenarios = service.getScenarios();
            boolean hasPlaytime = scenarios.stream().anyMatch(s -> !s.playtime().isEmpty());
            boolean hasSource = scenarios.stream().anyMatch(s -> !s.sourceGame().isEmpty());
            assertTrue(hasPlaytime, "Some scenarios should have playtime");
            assertTrue(hasSource, "Some scenarios should have source game");
        }
    }

    // ==========================================
    // Combat Tables
    // ==========================================

    @Nested
    @DisplayName("Combat Tables")
    class CombatTables {

        @Test
        @DisplayName("Ground combat table loaded")
        void groundCombatTableLoaded() {
            Map<String, Map<String, String>> table = service.getGroundCombatTable();
            assertNotNull(table);
        }

        @Test
        @DisplayName("Space combat table loaded")
        void spaceCombatTableLoaded() {
            Map<String, Map<String, String>> table = service.getSpaceCombatTable();
            assertNotNull(table);
        }

        @Test
        @DisplayName("Combat outcomes loaded")
        void combatOutcomesLoaded() {
            Map<String, String> outcomes = service.getCombatOutcomes();
            assertNotNull(outcomes);
        }

        @Test
        @DisplayName("Odds ratio calculation")
        void calculateOddsRatio() {
            // ratio 1.0 falls between 0.5 (1:2) and 2.0 (2:1), so it returns 1:2
            assertEquals("1:2", service.calculateOddsRatio(5, 5));
            // ratio 2.0 >= 2.0 threshold → 2:1
            assertEquals("2:1", service.calculateOddsRatio(10, 5));
            // ratio 0.5 >= 0.5 threshold → 1:2
            assertEquals("1:2", service.calculateOddsRatio(5, 10));
            // ratio 3.33 >= 3.0 threshold → 3:1
            assertEquals("3:1", service.calculateOddsRatio(10, 3));
            // ratio 0.3 >= 0.25 threshold → 1:4
            assertEquals("1:4", service.calculateOddsRatio(3, 10));
        }

        @Test
        @DisplayName("Extreme odds ratio clamping")
        void extremeOddsRatio() {
            // ratio 100.0 >= 6.0 threshold → 6:1 (max label)
            assertEquals("6:1", service.calculateOddsRatio(100, 1));
            // ratio 0.01 < 0.167 → falls through, returns default
            String lowOdds = service.calculateOddsRatio(1, 100);
            assertNotNull(lowOdds);
        }

        @Test
        @DisplayName("Zero defense gives max odds")
        void zeroDefense() {
            String odds = service.calculateOddsRatio(5, 0);
            assertEquals("7:1+", odds);
        }
    }

    // ==========================================
    // Integration Scenarios
    // ==========================================

    @Nested
    @DisplayName("Integration Scenarios")
    class IntegrationScenarios {

        @Test
        @DisplayName("Battle of Hoth: Rebels vs Empire")
        void battleOfHoth() {
            // Get Rebel units
            List<Unit> rebels = service.getUnitsByFaction("rebel_alliance");
            List<Unit> empire = service.getUnitsByFaction("galactic_empire");

            assertFalse(rebels.isEmpty(), "Rebels should have units");
            assertFalse(empire.isEmpty(), "Empire should have units");

            // Find AT-AT (Empire)
            Optional<Unit> atAt = service.getUnitById("at_at_walker");
            assertTrue(atAt.isPresent(), "AT-AT should exist");

            // Find Snowspeeder (Rebel)
            Optional<Unit> snowspeeder = service.getUnitById("at_at_rebel");
            assertTrue(snowspeeder.isPresent(), "Snowspeeder should exist");

            // Calculate odds
            int atAtAttack = atAt.get().attackStrength();
            int snowDefense = snowspeeder.get().defenseStrength();
            String odds = service.calculateOddsRatio(atAtAttack, snowDefense);
            assertNotNull(odds);
        }

        @Test
        @DisplayName("Death Star battle: X-Wings vs TIE Fighters")
        void deathStarBattle() {
            Optional<Unit> xwing = service.getUnitById("x_wing");
            Optional<Unit> tie = service.getUnitById("tie_fighter");

            assertTrue(xwing.isPresent());
            assertTrue(tie.isPresent());

            // X-Wing should have shields, TIE should not
            assertTrue(xwing.get().stats().shields() > 0);
            assertEquals(0, tie.get().stats().shields());

            // Both should be starships
            assertTrue(xwing.get().isStarship());
            assertTrue(tie.get().isStarship());
        }

        @Test
        @DisplayName("Force duel: Vader vs Luke")
        void forceDuel() {
            Optional<Unit> vader = service.getUnitById("darth_vader");
            Optional<Unit> luke = service.getUnitById("luke_skywalker");

            assertTrue(vader.isPresent());
            assertTrue(luke.isPresent());

            // Both should be Force users
            List<Unit> forceUsers = service.getForceUsers();
            assertTrue(forceUsers.stream().anyMatch(u -> u.id().equals("darth_vader")));
            assertTrue(forceUsers.stream().anyMatch(u -> u.id().equals("luke_skywalker")));

            // Vader should have equal or higher Force power
            assertTrue(vader.get().stats().forcePower() >= luke.get().stats().forcePower());
        }

        @Test
        @DisplayName("Clone Wars: Republic vs Separatists")
        void cloneWars() {
            List<Unit> republic = service.getUnitsByFaction("galactic_republic");
            List<Unit> separatists = service.getUnitsByFaction("separatist_alliance");

            assertFalse(republic.isEmpty());
            assertFalse(separatists.isEmpty());

            // ARC-170 should be Republic
            Optional<Unit> arc170 = service.getUnitById("arc170");
            assertTrue(arc170.isPresent());
            assertEquals("galactic_republic", arc170.get().faction());

            // Vulture droid should be Separatist
            Optional<Unit> vulture = service.getUnitById("vulture_droid");
            assertTrue(vulture.isPresent());
            assertEquals("separatist_alliance", vulture.get().faction());
        }

        @Test
        @DisplayName("Faction alliance network")
        void factionAllianceNetwork() {
            // Jedi Order should ally with Republic
            List<Faction> jediAllies = service.getAlliesOf("jedi_order");
            assertNotNull(jediAllies);

            // Empire should have enemies
            Optional<Faction> empire = service.getFactionById("galactic_empire");
            assertTrue(empire.isPresent());
            assertFalse(empire.get().enemies().isEmpty());
        }

        @Test
        @DisplayName("Total unit count verification")
        void totalUnitCount() {
            List<Unit> all = service.getAllUnits();
            assertTrue(all.size() >= 30, "Should have at least 30 total units");

            // Verify categories
            long groundCount = all.stream().filter(u ->
                    u.type().equals("infantry") || u.type().equals("elite_infantry") ||
                    u.type().equals("droid_infantry") || u.type().equals("heavy_droid") ||
                    u.type().equals("walker") || u.type().equals("heavy_weapons") ||
                    u.type().equals("elite_droid") || u.type().equals("creature")).count();

            long shipCount = all.stream().filter(Unit::isStarship).count();
            long charCount = all.stream().filter(Unit::isCharacter).count();

            assertTrue(groundCount > 0, "Should have ground units");
            assertTrue(shipCount > 0, "Should have starships");
            assertTrue(charCount > 0, "Should have characters");
        }
    }
}
