package com.yisy.modules.doramcu.captainslog.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CaptainLogMission Tests")
class CaptainLogMissionTest {

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("Default constructor initializes defaults")
        void defaults() {
            CaptainLogMission mission = new CaptainLogMission();
            assertNull(mission.getId());
            assertNull(mission.getCharacter());
            assertEquals(0, mission.getMissionNumber());
            assertNull(mission.getName());
            assertNull(mission.getType());
            assertNull(mission.getStatus());
            assertEquals(1, mission.getCurrentAct());
            assertEquals(0, mission.getMomentum());
            assertEquals(0, mission.getThreat());
            assertEquals(1, mission.getCurrentBeat());
            assertFalse(mission.isCareerEvent());
            assertNull(mission.getCreatedAt());
            assertNull(mission.getCompletedAt());
            assertTrue(mission.getLogEntries().isEmpty());
        }

        @Test
        @DisplayName("Parameterized constructor sets character, number, name, status, timestamp")
        void parameterized() {
            CaptainLogCharacter charact = new CaptainLogCharacter();
            CaptainLogMission mission = new CaptainLogMission(charact, 1, "First Contact");
            assertSame(charact, mission.getCharacter());
            assertEquals(1, mission.getMissionNumber());
            assertEquals("First Contact", mission.getName());
            assertEquals(CaptainLogMission.MissionStatus.PLANNED, mission.getStatus());
            assertNotNull(mission.getCreatedAt());
        }
    }

    @Nested
    @DisplayName("Enums")
    class Enums {

        @Test
        @DisplayName("MissionType values")
        void missionType() {
            assertSame(CaptainLogMission.MissionType.EXPLORATION, CaptainLogMission.MissionType.valueOf("EXPLORATION"));
            assertSame(CaptainLogMission.MissionType.DIPLOMACY, CaptainLogMission.MissionType.valueOf("DIPLOMACY"));
            assertSame(CaptainLogMission.MissionType.COMBAT, CaptainLogMission.MissionType.valueOf("COMBAT"));
            assertSame(CaptainLogMission.MissionType.SCIENCE, CaptainLogMission.MissionType.valueOf("SCIENCE"));
            assertSame(CaptainLogMission.MissionType.RESCUE, CaptainLogMission.MissionType.valueOf("RESCUE"));
            assertSame(CaptainLogMission.MissionType.INVESTIGATION, CaptainLogMission.MissionType.valueOf("INVESTIGATION"));
            assertSame(CaptainLogMission.MissionType.TRADE, CaptainLogMission.MissionType.valueOf("TRADE"));
            assertSame(CaptainLogMission.MissionType.FIRST_CONTACT, CaptainLogMission.MissionType.valueOf("FIRST_CONTACT"));
            assertSame(CaptainLogMission.MissionType.CRISIS, CaptainLogMission.MissionType.valueOf("CRISIS"));
            assertSame(CaptainLogMission.MissionType.PERSONAL, CaptainLogMission.MissionType.valueOf("PERSONAL"));
        }

        @Test
        @DisplayName("MissionStatus values")
        void missionStatus() {
            assertSame(CaptainLogMission.MissionStatus.PLANNED, CaptainLogMission.MissionStatus.valueOf("PLANNED"));
            assertSame(CaptainLogMission.MissionStatus.IN_PROGRESS, CaptainLogMission.MissionStatus.valueOf("IN_PROGRESS"));
            assertSame(CaptainLogMission.MissionStatus.COMPLETED, CaptainLogMission.MissionStatus.valueOf("COMPLETED"));
            assertSame(CaptainLogMission.MissionStatus.ABANDONED, CaptainLogMission.MissionStatus.valueOf("ABANDONED"));
        }
    }

    @Nested
    @DisplayName("Momentum clamping")
    class MomentumClamping {

        @Test
        @DisplayName("Momentum clamps at 0")
        void clampMin() {
            CaptainLogMission mission = new CaptainLogMission();
            mission.setMomentum(-5);
            assertEquals(0, mission.getMomentum());
        }

        @Test
        @DisplayName("Momentum clamps at 1")
        void clampMax() {
            CaptainLogMission mission = new CaptainLogMission();
            mission.setMomentum(5);
            assertEquals(1, mission.getMomentum());
        }

        @Test
        @DisplayName("Momentum within range is unchanged")
        void withinRange() {
            CaptainLogMission mission = new CaptainLogMission();
            mission.setMomentum(1);
            assertEquals(1, mission.getMomentum());
        }
    }

    @Nested
    @DisplayName("Threat clamping")
    class ThreatClamping {

        @Test
        @DisplayName("Threat clamps at 0")
        void clampMin() {
            CaptainLogMission mission = new CaptainLogMission();
            mission.setThreat(-10);
            assertEquals(0, mission.getThreat());
        }

        @Test
        @DisplayName("Threat can grow unbounded")
        void unbounded() {
            CaptainLogMission mission = new CaptainLogMission();
            mission.setThreat(100);
            assertEquals(100, mission.getThreat());
        }
    }

    @Nested
    @DisplayName("Beat clamping")
    class BeatClamping {

        @Test
        @DisplayName("Beat clamps at 1")
        void clampMin() {
            CaptainLogMission mission = new CaptainLogMission();
            mission.setCurrentBeat(0);
            assertEquals(1, mission.getCurrentBeat());
        }

        @Test
        @DisplayName("Beat clamps at 5")
        void clampMax() {
            CaptainLogMission mission = new CaptainLogMission();
            mission.setCurrentBeat(10);
            assertEquals(5, mission.getCurrentBeat());
        }
    }

    @Nested
    @DisplayName("Beat advancement")
    class BeatAdvancement {

        @Test
        @DisplayName("Advance beat increments beat")
        void advanceBeat() {
            CaptainLogMission mission = new CaptainLogMission();
            mission.setCurrentBeat(1);
            boolean actAdvanced = mission.advanceBeat();
            assertEquals(2, mission.getCurrentBeat());
            assertFalse(actAdvanced);
        }

        @Test
        @DisplayName("Advance from beat 5 advances act")
        void advanceAct() {
            CaptainLogMission mission = new CaptainLogMission();
            mission.setCurrentAct(1);
            mission.setCurrentBeat(5);
            boolean actAdvanced = mission.advanceBeat();
            assertEquals(1, mission.getCurrentBeat());
            assertEquals(2, mission.getCurrentAct());
            assertTrue(actAdvanced);
        }

        @Test
        @DisplayName("Advance from act 3 stays at act 3")
        void maxAct() {
            CaptainLogMission mission = new CaptainLogMission();
            mission.setCurrentAct(3);
            mission.setCurrentBeat(5);
            mission.advanceBeat();
            assertEquals(3, mission.getCurrentAct());
            assertEquals(1, mission.getCurrentBeat());
        }

        @Test
        @DisplayName("Multiple advances progress through acts")
        void multipleAdvances() {
            CaptainLogMission mission = new CaptainLogMission();
            mission.setCurrentAct(1);
            mission.setCurrentBeat(4);
            mission.advanceBeat(); // beat 5
            assertEquals(1, mission.getCurrentAct());
            mission.advanceBeat(); // act 2, beat 1
            assertEquals(2, mission.getCurrentAct());
            assertEquals(1, mission.getCurrentBeat());
        }
    }

    @Nested
    @DisplayName("Career event")
    class CareerEvent {

        @Test
        @DisplayName("Set career event")
        void setCareerEvent() {
            CaptainLogMission mission = new CaptainLogMission();
            mission.setCareerEvent(true);
            mission.setCareerEventDescription("Promotion");
            assertTrue(mission.isCareerEvent());
            assertEquals("Promotion", mission.getCareerEventDescription());
        }
    }

    @Nested
    @DisplayName("Act progression")
    class ActProgression {

        @Test
        @DisplayName("Default act is 1")
        void defaultAct() {
            CaptainLogMission mission = new CaptainLogMission();
            assertEquals(1, mission.getCurrentAct());
        }

        @Test
        @DisplayName("Set act")
        void setAct() {
            CaptainLogMission mission = new CaptainLogMission();
            mission.setCurrentAct(2);
            assertEquals(2, mission.getCurrentAct());
        }
    }
}
