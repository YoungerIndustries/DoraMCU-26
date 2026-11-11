package com.yisy.modules.doramcu.zero_zero_seven.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BondAgent Tests")
class BondAgentTest {

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("Default constructor initializes defaults")
        void defaults() {
            BondAgent agent = new BondAgent();
            assertNull(agent.getId());
            assertNull(agent.getName());
            assertNull(agent.getCodeName());
            assertEquals(BondAgent.AgentMode.COVERT, agent.getMode());
            assertEquals(5, agent.getStealth());
            assertEquals(5, agent.getCharisma());
            assertEquals(5, agent.getCombat());
            assertEquals(5, agent.getTechnical());
            assertEquals(BondAgent.AgentStatus.ACTIVE, agent.getStatus());
            assertEquals(0, agent.getMissionsCompleted());
            assertEquals(1, agent.getExperienceLevel());
        }
    }

    @Nested
    @DisplayName("Skill clamping")
    class SkillClamping {

        @Test
        @DisplayName("Stealth clamps to 1-10")
        void stealthClamp() {
            BondAgent agent = new BondAgent();
            agent.setStealth(0);
            assertEquals(1, agent.getStealth());
            agent.setStealth(15);
            assertEquals(10, agent.getStealth());
            agent.setStealth(7);
            assertEquals(7, agent.getStealth());
        }

        @Test
        @DisplayName("Charisma clamps to 1-10")
        void charismaClamp() {
            BondAgent agent = new BondAgent();
            agent.setCharisma(-5);
            assertEquals(1, agent.getCharisma());
            agent.setCharisma(20);
            assertEquals(10, agent.getCharisma());
        }

        @Test
        @DisplayName("Combat clamps to 1-10")
        void combatClamp() {
            BondAgent agent = new BondAgent();
            agent.setCombat(0);
            assertEquals(1, agent.getCombat());
            agent.setCombat(100);
            assertEquals(10, agent.getCombat());
        }

        @Test
        @DisplayName("Technical clamps to 1-10")
        void technicalClamp() {
            BondAgent agent = new BondAgent();
            agent.setTechnical(-10);
            assertEquals(1, agent.getTechnical());
            agent.setTechnical(50);
            assertEquals(10, agent.getTechnical());
        }
    }

    @Nested
    @DisplayName("Total skills")
    class TotalSkills {

        @Test
        @DisplayName("Sums all four skills")
        void sumSkills() {
            BondAgent agent = new BondAgent();
            agent.setStealth(8);
            agent.setCharisma(6);
            agent.setCombat(7);
            agent.setTechnical(9);
            assertEquals(30, agent.getTotalSkills());
        }

        @Test
        @DisplayName("Default skills total is 20")
        void defaultTotal() {
            BondAgent agent = new BondAgent();
            assertEquals(20, agent.getTotalSkills());
        }
    }

    @Nested
    @DisplayName("Availability")
    class Availability {

        @Test
        @DisplayName("Active agent with no operation is available")
        void available() {
            BondAgent agent = new BondAgent();
            assertTrue(agent.isAvailable());
        }

        @Test
        @DisplayName("Agent on mission is not available")
        void onMission() {
            BondAgent agent = new BondAgent();
            agent.setCurrentOperationId(1L);
            assertFalse(agent.isAvailable());
        }

        @Test
        @DisplayName("Inactive agent is not available")
        void inactive() {
            BondAgent agent = new BondAgent();
            agent.setStatus(BondAgent.AgentStatus.CAPTURED);
            assertFalse(agent.isAvailable());
        }

        @Test
        @DisplayName("Retired agent is not available")
        void retired() {
            BondAgent agent = new BondAgent();
            agent.setStatus(BondAgent.AgentStatus.RETIRED);
            assertFalse(agent.isAvailable());
        }
    }

    @Nested
    @DisplayName("Enums")
    class Enums {

        @Test
        @DisplayName("AgentMode values")
        void agentMode() {
            assertSame(BondAgent.AgentMode.COVERT, BondAgent.AgentMode.valueOf("COVERT"));
            assertSame(BondAgent.AgentMode.OVERT, BondAgent.AgentMode.valueOf("OVERT"));
        }

        @Test
        @DisplayName("AgentStatus values")
        void agentStatus() {
            assertSame(BondAgent.AgentStatus.ACTIVE, BondAgent.AgentStatus.valueOf("ACTIVE"));
            assertSame(BondAgent.AgentStatus.ON_MISSION, BondAgent.AgentStatus.valueOf("ON_MISSION"));
            assertSame(BondAgent.AgentStatus.CAPTURED, BondAgent.AgentStatus.valueOf("CAPTURED"));
            assertSame(BondAgent.AgentStatus.RETIRED, BondAgent.AgentStatus.valueOf("RETIRED"));
            assertSame(BondAgent.AgentStatus.DECEASED, BondAgent.AgentStatus.valueOf("DECEASED"));
        }
    }
}
