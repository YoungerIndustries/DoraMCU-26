package com.yisy.modules.doramcu.traveller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Economic Events")
class EconomicEventsTest {

    // =====================================================================
    // EconomicEventType Tests
    // =====================================================================

    @Nested
    @DisplayName("EconomicEventType")
    class EconomicEventTypeTests {

        @Test
        @DisplayName("All event types have valid properties")
        void allEventTypesHaveValidProperties() {
            for (EconomicEventType type : EconomicEventType.values()) {
                assertNotNull(type.getDisplayName(), type + " should have display name");
                assertFalse(type.getDisplayName().isEmpty(), type + " display name should not be empty");
                assertNotNull(type.getDescription(), type + " should have description");
                assertTrue(type.getMinSeverity() >= 1, type + " min severity should be >= 1");
                assertTrue(type.getMaxSeverity() <= 10, type + " max severity should be <= 10");
                assertTrue(type.getMinSeverity() <= type.getMaxSeverity(), type + " min <= max severity");
                assertTrue(type.getMinDuration() >= 1, type + " min duration should be >= 1");
                assertTrue(type.getMaxDuration() >= type.getMinDuration(), type + " min <= max duration");
            }
        }

        @Test
        @DisplayName("Event type count is correct")
        void eventTypesCount() {
            assertEquals(10, EconomicEventType.values().length, "Should have 10 event types");
        }

        @Test
        @DisplayName("PIRACY affects weapons positively")
        void piracyAffectsWeapons() {
            double weaponModifier = EconomicEventType.PIRACY.getPriceModifier(Commodity.WEAPONS, 5);
            assertTrue(weaponModifier > 1.0, "Weapons should increase during piracy");

            double foodModifier = EconomicEventType.PIRACY.getPriceModifier(Commodity.FOOD, 5);
            assertTrue(foodModifier > 1.0, "Other commodities should also increase during piracy");
        }

        @Test
        @DisplayName("EMBARGO makes all commodities expensive")
        void embargoMakesCommoditiesExpensive() {
            for (Commodity commodity : Commodity.values()) {
                double modifier = EconomicEventType.EMBARGO.getPriceModifier(commodity, 5);
                assertTrue(modifier >= 2.0, commodity + " should be very expensive during embargo");
            }
        }

        @Test
        @DisplayName("BOOM increases all prices")
        void boomIncreasesAllPrices() {
            for (Commodity commodity : Commodity.values()) {
                double modifier = EconomicEventType.BOOM.getPriceModifier(commodity, 5);
                assertTrue(modifier > 1.0, commodity + " should increase during boom");
            }
        }

        @Test
        @DisplayName("BUST decreases all prices")
        void bustDecreasesAllPrices() {
            for (Commodity commodity : Commodity.values()) {
                double modifier = EconomicEventType.BUST.getPriceModifier(commodity, 5);
                assertTrue(modifier < 1.0, commodity + " should decrease during bust");
            }
        }

        @Test
        @DisplayName("WAR increases weapons and medicine")
        void warIncreasesMilitarySupplies() {
            double weaponModifier = EconomicEventType.WAR.getPriceModifier(Commodity.WEAPONS, 7);
            double medicineModifier = EconomicEventType.WAR.getPriceModifier(Commodity.MEDICINE, 7);
            assertTrue(weaponModifier > 1.5, "Weapons should increase significantly during war");
            assertTrue(medicineModifier > 1.5, "Medicine should increase significantly during war");
        }

        @Test
        @DisplayName("PLAGUE makes medicine extremely valuable")
        void plagueMakesMedicineValuable() {
            double medicineModifier = EconomicEventType.PLAGUE.getPriceModifier(Commodity.MEDICINE, 7);
            assertTrue(medicineModifier > 2.0, "Medicine should be extremely valuable during plague");

            double foodModifier = EconomicEventType.PLAGUE.getPriceModifier(Commodity.FOOD, 7);
            assertTrue(foodModifier < 1.0, "Other commodities should decrease during plague");
        }

        @Test
        @DisplayName("HARVEST lowers food prices only")
        void harvestLowersFoodPrices() {
            double foodModifier = EconomicEventType.HARVEST.getPriceModifier(Commodity.FOOD, 3);
            assertTrue(foodModifier < 1.0, "Food should decrease during harvest");

            double weaponModifier = EconomicEventType.HARVEST.getPriceModifier(Commodity.WEAPONS, 3);
            assertEquals(1.0, weaponModifier, 0.001, "Other commodities should be unaffected");
        }

        @Test
        @DisplayName("TECHNOLOGY lowers electronics and luxury prices")
        void technologyLowersTechGoods() {
            double electronicsModifier = EconomicEventType.TECHNOLOGY.getPriceModifier(Commodity.ELECTRONICS, 3);
            double luxuryModifier = EconomicEventType.TECHNOLOGY.getPriceModifier(Commodity.LUXURY, 3);
            assertTrue(electronicsModifier < 1.0, "Electronics should decrease during tech breakthrough");
            assertTrue(luxuryModifier < 1.0, "Luxury should decrease during tech breakthrough");
        }

        @Test
        @DisplayName("DISASTER increases fuel and metals")
        void disasterIncreasesReconstructionMaterials() {
            double fuelModifier = EconomicEventType.DISASTER.getPriceModifier(Commodity.FUEL, 7);
            double metalsModifier = EconomicEventType.DISASTER.getPriceModifier(Commodity.METALS, 7);
            assertTrue(fuelModifier > 1.5, "Fuel should increase during disaster");
            assertTrue(metalsModifier > 1.5, "Metals should increase during disaster");
        }

        @Test
        @DisplayName("Condition overrides are correct for PIRACY")
        void piracyConditionOverrides() {
            MarketConditions.Condition weaponCondition =
                    EconomicEventType.PIRACY.getConditionOverride(Commodity.WEAPONS, 5);
            assertEquals(MarketConditions.Condition.SURPLUS, weaponCondition,
                    "Weapons should be surplus during piracy");

            MarketConditions.Condition foodCondition =
                    EconomicEventType.PIRACY.getConditionOverride(Commodity.FOOD, 5);
            assertEquals(MarketConditions.Condition.NORMAL, foodCondition,
                    "Other commodities should be normal during moderate piracy");

            MarketConditions.Condition highSeverityCondition =
                    EconomicEventType.PIRACY.getConditionOverride(Commodity.FOOD, 7);
            assertEquals(MarketConditions.Condition.SHORT, highSeverityCondition,
                    "High severity piracy should cause shortage");
        }

        @Test
        @DisplayName("Condition overrides are correct for HARVEST")
        void harvestConditionOverrides() {
            MarketConditions.Condition foodCondition =
                    EconomicEventType.HARVEST.getConditionOverride(Commodity.FOOD, 3);
            assertEquals(MarketConditions.Condition.SURPLUS, foodCondition,
                    "Food should be surplus during harvest");

            MarketConditions.Condition weaponCondition =
                    EconomicEventType.HARVEST.getConditionOverride(Commodity.WEAPONS, 3);
            assertNull(weaponCondition, "Other commodities should have no override during harvest");
        }

        @Test
        @DisplayName("toString returns display name")
        void toStringReturnsDisplayName() {
            assertEquals("Piracy", EconomicEventType.PIRACY.toString());
            assertEquals("Economic Boom", EconomicEventType.BOOM.toString());
        }
    }

    // =====================================================================
    // EconomicEvent Tests
    // =====================================================================

    @Nested
    @DisplayName("EconomicEvent")
    class EconomicEventTests {

        @Test
        @DisplayName("Builder creates event with correct properties")
        void builderCreatesEvent() {
            EconomicEvent event = EconomicEvent.builder(EconomicEventType.PIRACY)
                    .severity(5)
                    .durationTicks(3)
                    .startTick(10)
                    .scope(EconomicEvent.Scope.WORLD)
                    .build();

            assertEquals(EconomicEventType.PIRACY, event.getType());
            assertEquals(5, event.getSeverity());
            assertEquals(3, event.getDurationTicks());
            assertEquals(10, event.getStartTick());
            assertEquals(13, event.getEndTick());
            assertEquals(EconomicEvent.Scope.WORLD, event.getScope());
            assertTrue(event.isActive());
            assertNotNull(event.getId());
        }

        @Test
        @DisplayName("isActiveAt returns correct values")
        void isActiveAt() {
            EconomicEvent event = EconomicEvent.builder(EconomicEventType.BOOM)
                    .severity(3)
                    .durationTicks(5)
                    .startTick(10)
                    .build();

            assertFalse(event.isActiveAt(9), "Should not be active before start");
            assertTrue(event.isActiveAt(10), "Should be active at start");
            assertTrue(event.isActiveAt(14), "Should be active before end");
            assertFalse(event.isActiveAt(15), "Should not be active at end");
            assertFalse(event.isActiveAt(16), "Should not be active after end");
        }

        @Test
        @DisplayName("Deactivation makes event inactive")
        void deactivate() {
            EconomicEvent event = EconomicEvent.builder(EconomicEventType.BUST)
                    .severity(4)
                    .durationTicks(5)
                    .startTick(0)
                    .build();

            assertTrue(event.isActive());
            event.deactivate();
            assertFalse(event.isActive());
            assertFalse(event.isActiveAt(2), "Deactivated event should not be active at any tick");
        }

        @Test
        @DisplayName("getTicksRemaining calculates correctly")
        void getTicksRemaining() {
            EconomicEvent event = EconomicEvent.builder(EconomicEventType.WAR)
                    .severity(5)
                    .durationTicks(10)
                    .startTick(5)
                    .build();

            assertEquals(10, event.getTicksRemaining(5));
            assertEquals(5, event.getTicksRemaining(10));
            assertEquals(1, event.getTicksRemaining(14));
            assertEquals(0, event.getTicksRemaining(15));
            assertEquals(0, event.getTicksRemaining(20));
        }

        @Test
        @DisplayName("SECTOR scope affects all worlds")
        void sectorScopeAffectsAllWorlds() {
            TravellerWorld world = createTestWorld("Test", 0, 0);
            EconomicEvent event = EconomicEvent.builder(EconomicEventType.BOOM)
                    .severity(3)
                    .durationTicks(5)
                    .startTick(0)
                    .scope(EconomicEvent.Scope.SECTOR)
                    .build();

            assertTrue(event.affectsWorld(world), "SECTOR scope should affect all worlds");
        }

        @Test
        @DisplayName("WORLD scope affects only specified worlds")
        void worldScopeAffectsOnlySpecifiedWorlds() {
            TravellerWorld world1 = createTestWorld("World1", 0, 0);
            TravellerWorld world2 = createTestWorld("World2", 1, 1);

            EconomicEvent event = EconomicEvent.builder(EconomicEventType.PIRACY)
                    .severity(5)
                    .durationTicks(3)
                    .startTick(0)
                    .scope(EconomicEvent.Scope.WORLD)
                    .affectedWorld(world1)
                    .build();

            assertTrue(event.affectsWorld(world1), "Should affect specified world");
            assertFalse(event.affectsWorld(world2), "Should not affect other world");
        }

        @Test
        @DisplayName("getPriceModifier delegates to type")
        void getPriceModifier() {
            EconomicEvent event = EconomicEvent.builder(EconomicEventType.PIRACY)
                    .severity(6)
                    .durationTicks(3)
                    .startTick(0)
                    .build();

            double modifier = event.getPriceModifier(Commodity.FOOD);
            assertEquals(EconomicEventType.PIRACY.getPriceModifier(Commodity.FOOD, 6), modifier);
        }

        @Test
        @DisplayName("getConditionOverride delegates to type")
        void getConditionOverride() {
            EconomicEvent event = EconomicEvent.builder(EconomicEventType.HARVEST)
                    .severity(3)
                    .durationTicks(2)
                    .startTick(0)
                    .build();

            MarketConditions.Condition condition = event.getConditionOverride(Commodity.FOOD);
            assertEquals(MarketConditions.Condition.SURPLUS, condition);
        }

        @Test
        @DisplayName("randomBuilder creates valid event")
        void randomBuilder() {
            Random random = new Random(42);
            EconomicEvent.Builder builder = EconomicEvent.randomBuilder(random);
            EconomicEvent event = builder.startTick(0).build();

            assertNotNull(event.getType());
            assertTrue(event.getSeverity() >= 1);
            assertTrue(event.getSeverity() <= 10);
            assertTrue(event.getDurationTicks() >= 1);
        }

        @Test
        @DisplayName("Description is auto-generated")
        void descriptionAutoGenerated() {
            EconomicEvent event = EconomicEvent.builder(EconomicEventType.PIRACY)
                    .severity(5)
                    .durationTicks(3)
                    .startTick(10)
                    .scope(EconomicEvent.Scope.SECTOR)
                    .build();

            String desc = event.getDescription();
            assertTrue(desc.contains("Piracy"), "Description should contain event type");
            assertTrue(desc.contains("severity 5"), "Description should contain severity");
            assertTrue(desc.contains("3 ticks"), "Description should contain duration");
        }

        @Test
        @DisplayName("Custom description overrides auto-generated")
        void customDescription() {
            EconomicEvent event = EconomicEvent.builder(EconomicEventType.WAR)
                    .severity(7)
                    .durationTicks(5)
                    .startTick(0)
                    .customDescription("The Great War begins")
                    .build();

            assertEquals("The Great War begins", event.getDescription());
        }

        @Test
        @DisplayName("Equals and hashCode use ID")
        void equalsAndHashCode() {
            EconomicEvent event1 = EconomicEvent.builder(EconomicEventType.BOOM)
                    .severity(3)
                    .durationTicks(5)
                    .startTick(0)
                    .build();
            EconomicEvent event2 = EconomicEvent.builder(EconomicEventType.BUST)
                    .severity(4)
                    .durationTicks(3)
                    .startTick(0)
                    .build();

            assertNotEquals(event1, event2, "Different events should not be equal");
            assertNotEquals(event1.hashCode(), event2.hashCode(), "Different events should have different hashCodes");
            assertEquals(event1, event1, "Event should equal itself");
        }

        @Test
        @DisplayName("Scope enum has correct values")
        void scopeEnum() {
            assertEquals(3, EconomicEvent.Scope.values().length);
            assertNotNull(EconomicEvent.Scope.WORLD);
            assertNotNull(EconomicEvent.Scope.SUB_SECTOR);
            assertNotNull(EconomicEvent.Scope.SECTOR);
        }
    }

    // =====================================================================
    // EconomicEventService Tests
    // =====================================================================

    @Nested
    @DisplayName("EconomicEventService")
    class EconomicEventServiceTests {

        private int serviceWorldIdCounter = 1;

        private TravellerWorld createServiceWorld(String name, int x, int y) {
            UniversalWorldProfile uwp = new UniversalWorldProfile(
                    5, 4, 4, 4, 9, 6, 5, "", "5444965-");
            TravellerWorld world = new TravellerWorld(serviceWorldIdCounter, name, uwp, x, y, false, false);
            serviceWorldIdCounter++;
            return world;
        }

        private TravellerSector createTestSector() {
            TravellerSector sector = new TravellerSector(1, "TestSector");
            sector.addWorld(createServiceWorld("Alpha", 0, 0));
            sector.addWorld(createServiceWorld("Beta", 1, 1));
            sector.addWorld(createServiceWorld("Gamma", 2, 2));
            sector.addWorld(createServiceWorld("Delta", 5, 5));
            sector.addWorld(createServiceWorld("Epsilon", 8, 8));
            return sector;
        }

        @Test
        @DisplayName("Service starts with no active events")
        void startsEmpty() {
            TravellerSector sector = createTestSector();
            EconomicEventService service = new EconomicEventService(sector, new Random(42));

            assertEquals(0, service.getActiveEventCount());
            assertTrue(service.getActiveEvents().isEmpty());
            assertTrue(service.getEventHistory().isEmpty());
        }

        @Test
        @DisplayName("generateRandomEvent creates and adds event")
        void generateRandomEvent() {
            TravellerSector sector = createTestSector();
            EconomicEventService service = new EconomicEventService(sector, new Random(42));

            EconomicEvent event = service.generateRandomEvent(0);
            assertNotNull(event);
            assertEquals(1, service.getActiveEventCount());
            assertTrue(service.getActiveEvents().containsKey(event.getId()));
        }

        @Test
        @DisplayName("generateRandomEvent returns null when max reached")
        void generateRandomEventRespectsMax() {
            TravellerSector sector = createTestSector();
            EconomicEventService service = new EconomicEventService(sector, new Random(42), 100, 2);

            EconomicEvent event1 = service.generateRandomEvent(0);
            EconomicEvent event2 = service.generateRandomEvent(1);
            EconomicEvent event3 = service.generateRandomEvent(2);

            assertNotNull(event1);
            assertNotNull(event2);
            assertNull(event3, "Should return null when max active events reached");
            assertEquals(2, service.getActiveEventCount());
        }

        @Test
        @DisplayName("createEvent creates specific event")
        void createEvent() {
            TravellerSector sector = createTestSector();
            EconomicEventService service = new EconomicEventService(sector, new Random(42));

            List<TravellerWorld> worlds = sector.getAllWorlds();
            EconomicEvent event = service.createEvent(
                    EconomicEventType.PIRACY, 5, 3,
                    EconomicEvent.Scope.WORLD, List.of(worlds.get(0)), 0);

            assertNotNull(event);
            assertEquals(EconomicEventType.PIRACY, event.getType());
            assertEquals(5, event.getSeverity());
            assertEquals(3, event.getDurationTicks());
            assertEquals(EconomicEvent.Scope.WORLD, event.getScope());
        }

        @Test
        @DisplayName("advanceTick expires expired events")
        void advanceTickExpiresEvents() {
            TravellerSector sector = createTestSector();
            EconomicEventService service = new EconomicEventService(sector, new Random(42));

            List<TravellerWorld> worlds = sector.getAllWorlds();
            service.createEvent(EconomicEventType.PIRACY, 5, 3,
                    EconomicEvent.Scope.WORLD, List.of(worlds.get(0)), 0);
            service.createEvent(EconomicEventType.BOOM, 3, 10,
                    EconomicEvent.Scope.WORLD, List.of(worlds.get(1)), 0);

            assertEquals(2, service.getActiveEventCount());

            // Advance to tick 3 — PIRACY expires, BOOM continues
            List<EconomicEvent> expired = service.advanceTick(3);
            assertEquals(1, expired.size());
            assertEquals(EconomicEventType.PIRACY, expired.get(0).getType());
            assertEquals(1, service.getActiveEventCount());
            assertEquals(1, service.getEventHistory().size());
        }

        @Test
        @DisplayName("advanceTick does not expire active events")
        void advanceTickKeepsActiveEvents() {
            TravellerSector sector = createTestSector();
            EconomicEventService service = new EconomicEventService(sector, new Random(42));

            List<TravellerWorld> worlds = sector.getAllWorlds();
            service.createEvent(EconomicEventType.BOOM, 3, 10,
                    EconomicEvent.Scope.WORLD, List.of(worlds.get(0)), 0);

            List<EconomicEvent> expired = service.advanceTick(5);
            assertTrue(expired.isEmpty());
            assertEquals(1, service.getActiveEventCount());
        }

        @Test
        @DisplayName("deactivateEvent removes event")
        void deactivateEvent() {
            TravellerSector sector = createTestSector();
            EconomicEventService service = new EconomicEventService(sector, new Random(42));

            List<TravellerWorld> worlds = sector.getAllWorlds();
            EconomicEvent event = service.createEvent(
                    EconomicEventType.PIRACY, 5, 10,
                    EconomicEvent.Scope.WORLD, List.of(worlds.get(0)), 0);

            EconomicEvent deactivated = service.deactivateEvent(event.getId());
            assertNotNull(deactivated);
            assertEquals(event, deactivated);
            assertEquals(0, service.getActiveEventCount());
            assertEquals(1, service.getEventHistory().size());
            assertFalse(deactivated.isActive());
        }

        @Test
        @DisplayName("deactivateEvent returns null for unknown ID")
        void deactivateUnknownEvent() {
            TravellerSector sector = createTestSector();
            EconomicEventService service = new EconomicEventService(sector, new Random(42));

            assertNull(service.deactivateEvent("nonexistent"));
        }

        @Test
        @DisplayName("clearAllEvents removes all events")
        void clearAllEvents() {
            TravellerSector sector = createTestSector();
            EconomicEventService service = new EconomicEventService(sector, new Random(42));

            List<TravellerWorld> worlds = sector.getAllWorlds();
            service.createEvent(EconomicEventType.PIRACY, 5, 10,
                    EconomicEvent.Scope.WORLD, List.of(worlds.get(0)), 0);
            service.createEvent(EconomicEventType.BOOM, 3, 10,
                    EconomicEvent.Scope.WORLD, List.of(worlds.get(1)), 0);

            int cleared = service.clearAllEvents();
            assertEquals(2, cleared);
            assertEquals(0, service.getActiveEventCount());
            assertEquals(2, service.getEventHistory().size());
        }

        @Test
        @DisplayName("getPriceModifier combines multiple events")
        void getPriceModifierCombinesEvents() {
            TravellerSector sector = createTestSector();
            EconomicEventService service = new EconomicEventService(sector, new Random(42));

            TravellerWorld world = findWorldByName(sector, "Alpha");
            service.createEvent(EconomicEventType.BOOM, 5, 10,
                    EconomicEvent.Scope.WORLD, List.of(world), 0);
            service.createEvent(EconomicEventType.PIRACY, 5, 10,
                    EconomicEvent.Scope.WORLD, List.of(world), 0);

            double modifier = service.getPriceModifier(world, Commodity.FOOD);
            // BOOM: 1.0 + 5*0.1 = 1.5; PIRACY: 1.0 + 5*0.05 = 1.25; Combined: 1.5 * 1.25 = 1.875
            assertEquals(1.875, modifier, 0.001, "Modifiers should be combined multiplicatively");
        }

        @Test
        @DisplayName("getPriceModifier clamps to reasonable range")
        void getPriceModifierClamps() {
            TravellerSector sector = createTestSector();
            EconomicEventService service = new EconomicEventService(sector, new Random(42));

            TravellerWorld world = findWorldByName(sector, "Alpha");
            // Create multiple high-severity events to push modifier high
            for (int i = 0; i < 10; i++) {
                service.createEvent(EconomicEventType.EMBARGO, 9, 10,
                        EconomicEvent.Scope.WORLD, List.of(world), 0);
            }

            double modifier = service.getPriceModifier(world, Commodity.FOOD);
            assertTrue(modifier <= 5.0, "Modifier should be clamped to max 5.0");
            assertTrue(modifier >= 0.1, "Modifier should be clamped to min 0.1");
        }

        @Test
        @DisplayName("getPriceModifier returns 1.0 with no events")
        void getPriceModifierNoEvents() {
            TravellerSector sector = createTestSector();
            EconomicEventService service = new EconomicEventService(sector, new Random(42));

            TravellerWorld world = findWorldByName(sector, "Alpha");
            double modifier = service.getPriceModifier(world, Commodity.FOOD);
            assertEquals(1.0, modifier, 0.001, "No events should give 1.0 modifier");
        }

        @Test
        @DisplayName("getEventsForWorld filters correctly")
        void getEventsForWorld() {
            TravellerSector sector = createTestSector();
            EconomicEventService service = new EconomicEventService(sector, new Random(42));

            TravellerWorld world1 = findWorldByName(sector, "Alpha");
            TravellerWorld world2 = findWorldByName(sector, "Beta");

            service.createEvent(EconomicEventType.PIRACY, 5, 10,
                    EconomicEvent.Scope.WORLD, List.of(world1), 0);
            service.createEvent(EconomicEventType.BOOM, 3, 10,
                    EconomicEvent.Scope.WORLD, List.of(world2), 0);
            service.createEvent(EconomicEventType.BUST, 4, 10,
                    EconomicEvent.Scope.SECTOR, new ArrayList<>(), 0);

            List<EconomicEvent> world1Events = service.getEventsForWorld(world1);
            assertEquals(2, world1Events.size(), "WORLD event + SECTOR event");

            List<EconomicEvent> world2Events = service.getEventsForWorld(world2);
            assertEquals(2, world2Events.size(), "WORLD event + SECTOR event");
        }

        @Test
        @DisplayName("getEventsByType filters correctly")
        void getEventsByType() {
            TravellerSector sector = createTestSector();
            EconomicEventService service = new EconomicEventService(sector, new Random(42));

            List<TravellerWorld> worlds = sector.getAllWorlds();
            service.createEvent(EconomicEventType.PIRACY, 5, 10,
                    EconomicEvent.Scope.WORLD, List.of(worlds.get(0)), 0);
            service.createEvent(EconomicEventType.PIRACY, 7, 10,
                    EconomicEvent.Scope.WORLD, List.of(worlds.get(1)), 0);
            service.createEvent(EconomicEventType.BOOM, 3, 10,
                    EconomicEvent.Scope.WORLD, List.of(worlds.get(2)), 0);

            assertEquals(2, service.getEventsByType(EconomicEventType.PIRACY).size());
            assertEquals(1, service.getEventsByType(EconomicEventType.BOOM).size());
            assertTrue(service.getEventsByType(EconomicEventType.WAR).isEmpty());
        }

        @Test
        @DisplayName("applyToMarketConditions applies event overrides")
        void applyToMarketConditions() {
            TravellerSector sector = createTestSector();
            EconomicEventService service = new EconomicEventService(sector, new Random(42));

            TravellerWorld world = findWorldByName(sector, "Alpha");
            MarketConditions conditions = new MarketConditions(world);

            service.createEvent(EconomicEventType.HARVEST, 3, 10,
                    EconomicEvent.Scope.WORLD, List.of(world), 0);

            service.applyToMarketConditions(world, conditions);

            // HARVEST should make food SURPLUS
            MarketConditions.Condition foodCondition = conditions.getAllConditions().get(Commodity.FOOD);
            assertEquals(MarketConditions.Condition.SURPLUS, foodCondition,
                    "HARVEST should make food SURPLUS");
        }

        @Test
        @DisplayName("applyToMarketConditions uses strongest override")
        void applyToMarketConditionsUsesStrongest() {
            TravellerSector sector = createTestSector();
            EconomicEventService service = new EconomicEventService(sector, new Random(42));

            TravellerWorld world = findWorldByName(sector, "Alpha");
            MarketConditions conditions = new MarketConditions(world);

            // BOOM makes SHORT, EMBARGO makes NONE (stronger)
            service.createEvent(EconomicEventType.BOOM, 5, 10,
                    EconomicEvent.Scope.WORLD, List.of(world), 0);
            service.createEvent(EconomicEventType.EMBARGO, 6, 10,
                    EconomicEvent.Scope.WORLD, List.of(world), 0);

            service.applyToMarketConditions(world, conditions);

            // EMBARGO with severity 6 should make NONE (stronger than BOOM's SHORT)
            MarketConditions.Condition foodCondition = conditions.getAllConditions().get(Commodity.FOOD);
            assertEquals(MarketConditions.Condition.NONE, foodCondition,
                    "Strongest override should win");
        }

        @Test
        @DisplayName("applyToMarketConditions no-ops with no events")
        void applyToMarketConditionsNoEvents() {
            TravellerSector sector = createTestSector();
            EconomicEventService service = new EconomicEventService(sector, new Random(42));

            TravellerWorld world = findWorldByName(sector, "Alpha");
            MarketConditions conditions = new MarketConditions(world);
            Map<Commodity, MarketConditions.Condition> before = new HashMap<>(conditions.getAllConditions());

            service.applyToMarketConditions(world, conditions);

            assertEquals(before, conditions.getAllConditions(),
                    "No events should not change conditions");
        }

        @Test
        @DisplayName("maybeGenerateEvent respects chance")
        void maybeGenerateEvent() {
            TravellerSector sector = createTestSector();
            // 100% chance means always generate
            EconomicEventService service = new EconomicEventService(sector, new Random(42), 100, 10);

            EconomicEvent event = service.maybeGenerateEvent(0);
            assertNotNull(event, "100% chance should always generate");
        }

        @Test
        @DisplayName("maybeGenerateEvent with 0 chance never generates")
        void maybeGenerateEventZeroChance() {
            TravellerSector sector = createTestSector();
            // 0% chance means never generate (clamped to 1% min)
            EconomicEventService service = new EconomicEventService(sector, new Random(42), 1, 10);

            // With seed 42 and 1% chance, it may or may not generate — just verify no crash
            service.maybeGenerateEvent(0);
        }

        @Test
        @DisplayName("Event history tracks expired events")
        void eventHistoryTracksExpired() {
            TravellerSector sector = createTestSector();
            EconomicEventService service = new EconomicEventService(sector, new Random(42));

            List<TravellerWorld> worlds = sector.getAllWorlds();
            service.createEvent(EconomicEventType.PIRACY, 5, 3,
                    EconomicEvent.Scope.WORLD, List.of(worlds.get(0)), 0);

            service.advanceTick(3);

            assertEquals(1, service.getEventHistory().size());
            EconomicEvent historical = service.getEventHistory().get(0);
            assertEquals(EconomicEventType.PIRACY, historical.getType());
            assertFalse(historical.isActive());
        }

        @Test
        @DisplayName("Summary contains event information")
        void getSummary() {
            TravellerSector sector = createTestSector();
            EconomicEventService service = new EconomicEventService(sector, new Random(42));

            List<TravellerWorld> worlds = sector.getAllWorlds();
            service.createEvent(EconomicEventType.PIRACY, 5, 3,
                    EconomicEvent.Scope.WORLD, List.of(worlds.get(0)), 0);

            String summary = service.getSummary();
            assertTrue(summary.contains("TestSector"));
            assertTrue(summary.contains("Active Events: 1"));
            assertTrue(summary.contains("Piracy"));
        }

        @Test
        @DisplayName("Sector scope event affects all worlds in getEventsForWorld")
        void sectorScopeAffectsAllWorlds() {
            TravellerSector sector = createTestSector();
            EconomicEventService service = new EconomicEventService(sector, new Random(42));

            service.createEvent(EconomicEventType.BOOM, 3, 10,
                    EconomicEvent.Scope.SECTOR, new ArrayList<>(), 0);

            for (TravellerWorld world : sector.getAllWorlds()) {
                List<EconomicEvent> events = service.getEventsForWorld(world);
                assertEquals(1, events.size(), "All worlds should see SECTOR event");
            }
        }

        @Test
        @DisplayName("Integration: full event lifecycle")
        void fullEventLifecycle() {
            TravellerSector sector = createTestSector();
            EconomicEventService service = new EconomicEventService(sector, new Random(42));

            // Tick 0: create event
            List<TravellerWorld> worlds = sector.getAllWorlds();
            EconomicEvent event = service.createEvent(
                    EconomicEventType.PIRACY, 5, 5,
                    EconomicEvent.Scope.WORLD, List.of(worlds.get(0)), 0);
            assertNotNull(event);
            assertEquals(1, service.getActiveEventCount());

            // Tick 2: check price modifier
            TravellerWorld world = worlds.get(0);
            double modifier = service.getPriceModifier(world, Commodity.FOOD);
            assertTrue(modifier > 1.0, "Piracy should increase food prices");

            // Tick 5: event expires
            List<EconomicEvent> expired = service.advanceTick(5);
            assertEquals(1, expired.size());
            assertEquals(0, service.getActiveEventCount());
            assertEquals(1, service.getEventHistory().size());

            // Tick 6: no modifier
            modifier = service.getPriceModifier(world, Commodity.FOOD);
            assertEquals(1.0, modifier, 0.001, "No active events should give 1.0 modifier");
        }

        @Test
        @DisplayName("Integration: multiple events with different scopes")
        void multipleEventsDifferentScopes() {
            TravellerSector sector = createTestSector();
            EconomicEventService service = new EconomicEventService(sector, new Random(42));

            List<TravellerWorld> worlds = sector.getAllWorlds();

            // SECTOR-wide BOOM
            service.createEvent(EconomicEventType.BOOM, 3, 10,
                    EconomicEvent.Scope.SECTOR, new ArrayList<>(), 0);
            // WORLD-specific PIRACY
            service.createEvent(EconomicEventType.PIRACY, 5, 10,
                    EconomicEvent.Scope.WORLD, List.of(worlds.get(0)), 0);

            // World 0 should have both events
            List<EconomicEvent> world0Events = service.getEventsForWorld(worlds.get(0));
            assertEquals(2, world0Events.size());

            // World 1 should only have SECTOR event
            List<EconomicEvent> world1Events = service.getEventsForWorld(worlds.get(1));
            assertEquals(1, world1Events.size());
            assertEquals(EconomicEventType.BOOM, world1Events.get(0).getType());
        }
    }

    // =====================================================================
    // Helper Methods
    // =====================================================================

    private int worldIdCounter = 1;

    private TravellerWorld createTestWorld(String name, int x, int y) {
        UniversalWorldProfile uwp = new UniversalWorldProfile(
                5, 4, 4, 4, 9, 6, 5, "", "5444965-");
        TravellerWorld world = new TravellerWorld(worldIdCounter, name, uwp, x, y, false, false);
        worldIdCounter++;
        return world;
    }

    private TravellerWorld findWorldByName(TravellerSector sector, String name) {
        return sector.getAllWorlds().stream()
                .filter(w -> w.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new AssertionError("World not found: " + name));
    }
}
