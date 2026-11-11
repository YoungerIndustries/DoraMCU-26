package com.yisy.modules.doramcu.captainslog.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CaptainLogStarship Tests")
class CaptainLogStarshipTest {

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("Default constructor initializes defaults")
        void defaults() {
            CaptainLogStarship ship = new CaptainLogStarship();
            assertNull(ship.getId());
            assertNull(ship.getName());
            assertNull(ship.getRegistry());
            assertNull(ship.getShipClass());
            assertNull(ship.getScale());
            assertFalse(ship.isStation());
            assertEquals(0, ship.getComms());
            assertEquals(0, ship.getComputers());
            assertEquals(0, ship.getEngines());
            assertEquals(0, ship.getSensors());
            assertEquals(0, ship.getStructure());
            assertEquals(0, ship.getWeapons());
            assertEquals(0, ship.getCommsStress());
            assertEquals(0, ship.getCommand());
            assertEquals(0, ship.getScience());
            assertEquals(0, ship.getSecurity());
            assertEquals(0, ship.getEngineering());
            assertEquals(0, ship.getMedical());
            assertEquals(0, ship.getOperations());
            assertTrue(ship.getTalents().isEmpty());
            assertTrue(ship.getCrew().isEmpty());
            assertEquals(0, ship.getFitness());
            assertEquals(0, ship.getTechnical());
            assertEquals(0, ship.getStrength());
            assertEquals(0, ship.getReason());
            assertEquals(0, ship.getInsight());
            assertEquals(0, ship.getPresence());
        }

        @Test
        @DisplayName("Parameterized constructor sets name, class, scale")
        void parameterized() {
            CaptainLogStarship ship = new CaptainLogStarship("Enterprise", "CONSTITUTION", CaptainLogStarship.Scale.LARGE);
            assertEquals("Enterprise", ship.getName());
            assertEquals("CONSTITUTION", ship.getShipClass());
            assertEquals(CaptainLogStarship.Scale.LARGE, ship.getScale());
        }
    }

    @Nested
    @DisplayName("Enums")
    class Enums {

        @Test
        @DisplayName("Scale values")
        void scale() {
            assertSame(CaptainLogStarship.Scale.SMALL, CaptainLogStarship.Scale.valueOf("SMALL"));
            assertSame(CaptainLogStarship.Scale.MEDIUM, CaptainLogStarship.Scale.valueOf("MEDIUM"));
            assertSame(CaptainLogStarship.Scale.LARGE, CaptainLogStarship.Scale.valueOf("LARGE"));
            assertSame(CaptainLogStarship.Scale.CAPITAL, CaptainLogStarship.Scale.valueOf("CAPITAL"));
            assertSame(CaptainLogStarship.Scale.STATION, CaptainLogStarship.Scale.valueOf("STATION"));
        }

        @Test
        @DisplayName("StarshipSystem values")
        void starshipSystem() {
            assertSame(CaptainLogStarship.StarshipSystem.COMMS, CaptainLogStarship.StarshipSystem.valueOf("COMMS"));
            assertSame(CaptainLogStarship.StarshipSystem.COMPUTERS, CaptainLogStarship.StarshipSystem.valueOf("COMPUTERS"));
            assertSame(CaptainLogStarship.StarshipSystem.ENGINES, CaptainLogStarship.StarshipSystem.valueOf("ENGINES"));
            assertSame(CaptainLogStarship.StarshipSystem.SENSORS, CaptainLogStarship.StarshipSystem.valueOf("SENSORS"));
            assertSame(CaptainLogStarship.StarshipSystem.STRUCTURE, CaptainLogStarship.StarshipSystem.valueOf("STRUCTURE"));
            assertSame(CaptainLogStarship.StarshipSystem.WEAPONS, CaptainLogStarship.StarshipSystem.valueOf("WEAPONS"));
        }

        @Test
        @DisplayName("StarshipDepartment values")
        void starshipDepartment() {
            assertSame(CaptainLogStarship.StarshipDepartment.COMMAND, CaptainLogStarship.StarshipDepartment.valueOf("COMMAND"));
            assertSame(CaptainLogStarship.StarshipDepartment.SCIENCE, CaptainLogStarship.StarshipDepartment.valueOf("SCIENCE"));
            assertSame(CaptainLogStarship.StarshipDepartment.SECURITY, CaptainLogStarship.StarshipDepartment.valueOf("SECURITY"));
            assertSame(CaptainLogStarship.StarshipDepartment.ENGINEERING, CaptainLogStarship.StarshipDepartment.valueOf("ENGINEERING"));
            assertSame(CaptainLogStarship.StarshipDepartment.MEDICAL, CaptainLogStarship.StarshipDepartment.valueOf("MEDICAL"));
            assertSame(CaptainLogStarship.StarshipDepartment.OPERATIONS, CaptainLogStarship.StarshipDepartment.valueOf("OPERATIONS"));
        }
    }

    @Nested
    @DisplayName("Max hits")
    class MaxHits {

        @Test
        @DisplayName("Small ship has 2 max hits")
        void small() {
            CaptainLogStarship ship = new CaptainLogStarship();
            ship.setScale(CaptainLogStarship.Scale.SMALL);
            assertEquals(2, ship.getMaxHits());
        }

        @Test
        @DisplayName("Medium ship has 3 max hits")
        void medium() {
            CaptainLogStarship ship = new CaptainLogStarship();
            ship.setScale(CaptainLogStarship.Scale.MEDIUM);
            assertEquals(3, ship.getMaxHits());
        }

        @Test
        @DisplayName("Large ship has 4 max hits")
        void large() {
            CaptainLogStarship ship = new CaptainLogStarship();
            ship.setScale(CaptainLogStarship.Scale.LARGE);
            assertEquals(4, ship.getMaxHits());
        }

        @Test
        @DisplayName("Capital ship has 5 max hits")
        void capital() {
            CaptainLogStarship ship = new CaptainLogStarship();
            ship.setScale(CaptainLogStarship.Scale.CAPITAL);
            assertEquals(5, ship.getMaxHits());
        }

        @Test
        @DisplayName("Station has 6 max hits")
        void station() {
            CaptainLogStarship ship = new CaptainLogStarship();
            ship.setScale(CaptainLogStarship.Scale.STATION);
            assertEquals(6, ship.getMaxHits());
        }
    }

    @Nested
    @DisplayName("System rating lookup")
    class SystemRatingLookup {

        @Test
        @DisplayName("Get system rating by enum")
        void getSystemRating() {
            CaptainLogStarship ship = new CaptainLogStarship();
            ship.setComms(3);
            ship.setComputers(2);
            ship.setEngines(4);
            ship.setSensors(3);
            ship.setStructure(2);
            ship.setWeapons(3);
            assertEquals(3, ship.getSystemRating(CaptainLogStarship.StarshipSystem.COMMS));
            assertEquals(2, ship.getSystemRating(CaptainLogStarship.StarshipSystem.COMPUTERS));
            assertEquals(4, ship.getSystemRating(CaptainLogStarship.StarshipSystem.ENGINES));
            assertEquals(3, ship.getSystemRating(CaptainLogStarship.StarshipSystem.SENSORS));
            assertEquals(2, ship.getSystemRating(CaptainLogStarship.StarshipSystem.STRUCTURE));
            assertEquals(3, ship.getSystemRating(CaptainLogStarship.StarshipSystem.WEAPONS));
        }
    }

    @Nested
    @DisplayName("Department rating lookup")
    class DepartmentRatingLookup {

        @Test
        @DisplayName("Get department rating by enum")
        void getDepartmentRating() {
            CaptainLogStarship ship = new CaptainLogStarship();
            ship.setCommand(2);
            ship.setScience(3);
            ship.setSecurity(2);
            ship.setEngineering(3);
            ship.setMedical(1);
            ship.setOperations(2);
            assertEquals(2, ship.getDepartmentRating(CaptainLogStarship.StarshipDepartment.COMMAND));
            assertEquals(3, ship.getDepartmentRating(CaptainLogStarship.StarshipDepartment.SCIENCE));
            assertEquals(2, ship.getDepartmentRating(CaptainLogStarship.StarshipDepartment.SECURITY));
            assertEquals(3, ship.getDepartmentRating(CaptainLogStarship.StarshipDepartment.ENGINEERING));
            assertEquals(1, ship.getDepartmentRating(CaptainLogStarship.StarshipDepartment.MEDICAL));
            assertEquals(2, ship.getDepartmentRating(CaptainLogStarship.StarshipDepartment.OPERATIONS));
        }
    }

    @Nested
    @DisplayName("System stress")
    class SystemStress {

        @Test
        @DisplayName("Add stress increases stress")
        void addStress() {
            CaptainLogStarship ship = new CaptainLogStarship();
            ship.setEngines(4);
            boolean added = ship.addSystemStress(CaptainLogStarship.StarshipSystem.ENGINES, 2);
            assertTrue(added);
            assertEquals(2, ship.getSystemStress(CaptainLogStarship.StarshipSystem.ENGINES));
        }

        @Test
        @DisplayName("Stress caps at system rating")
        void stressCap() {
            CaptainLogStarship ship = new CaptainLogStarship();
            ship.setEngines(3);
            ship.addSystemStress(CaptainLogStarship.StarshipSystem.ENGINES, 5);
            assertEquals(3, ship.getSystemStress(CaptainLogStarship.StarshipSystem.ENGINES));
        }

        @Test
        @DisplayName("Reduce stress decreases stress")
        void reduceStress() {
            CaptainLogStarship ship = new CaptainLogStarship();
            ship.setEnginesStress(3);
            boolean reduced = ship.reduceSystemStress(CaptainLogStarship.StarshipSystem.ENGINES, 1);
            assertTrue(reduced);
            assertEquals(2, ship.getEnginesStress());
        }

        @Test
        @DisplayName("Reduce stress doesn't go below 0")
        void reduceStressFloor() {
            CaptainLogStarship ship = new CaptainLogStarship();
            ship.setEnginesStress(1);
            ship.reduceSystemStress(CaptainLogStarship.StarshipSystem.ENGINES, 5);
            assertEquals(0, ship.getEnginesStress());
        }

        @Test
        @DisplayName("Add stress returns false when no rating")
        void noRating() {
            CaptainLogStarship ship = new CaptainLogStarship();
            ship.setEngines(0);
            assertFalse(ship.addSystemStress(CaptainLogStarship.StarshipSystem.ENGINES, 1));
        }
    }

    @Nested
    @DisplayName("Station flag")
    class StationFlag {

        @Test
        @DisplayName("Default is not a station")
        void defaultNotStation() {
            CaptainLogStarship ship = new CaptainLogStarship();
            assertFalse(ship.isStation());
        }

        @Test
        @DisplayName("Set as station")
        void setStation() {
            CaptainLogStarship ship = new CaptainLogStarship();
            ship.setStation(true);
            assertTrue(ship.isStation());
        }
    }
}
