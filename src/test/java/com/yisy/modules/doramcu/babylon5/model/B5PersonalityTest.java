package com.yisy.modules.doramcu.babylon5.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("B5Personality Tests")
class B5PersonalityTest {

    @Nested
    @DisplayName("Default constructor")
    class DefaultConstructor {

        @Test
        @DisplayName("All axes default to 0.0")
        void defaults() {
            B5Personality personality = new B5Personality();
            assertEquals(0.0, personality.getAggression());
            assertEquals(0.0, personality.getDiplomacy());
            assertEquals(0.0, personality.getEconomicFocus());
            assertEquals(0.0, personality.getScientificFocus());
            assertEquals(0.0, personality.getExpansionDrive());
            assertEquals(0.0, personality.getRiskTolerance());
            assertEquals(0.0, personality.getAltruism());
            assertEquals(0.0, personality.getDetermination());
            assertEquals(0.0, personality.getAdaptability());
            assertEquals(0.0, personality.getLeadership());
            assertEquals(0.0, personality.getCunning());
            assertEquals(0.0, personality.getHonor());
        }
    }

    @Nested
    @DisplayName("Setters")
    class Setters {

        @Test
        @DisplayName("Set aggression")
        void setAggression() {
            B5Personality personality = new B5Personality();
            personality.setAggression(0.8);
            assertEquals(0.8, personality.getAggression());
        }

        @Test
        @DisplayName("Set diplomacy")
        void setDiplomacy() {
            B5Personality personality = new B5Personality();
            personality.setDiplomacy(0.5);
            assertEquals(0.5, personality.getDiplomacy());
        }

        @Test
        @DisplayName("Set economic focus")
        void setEconomicFocus() {
            B5Personality personality = new B5Personality();
            personality.setEconomicFocus(0.3);
            assertEquals(0.3, personality.getEconomicFocus());
        }

        @Test
        @DisplayName("Set scientific focus")
        void setScientificFocus() {
            B5Personality personality = new B5Personality();
            personality.setScientificFocus(0.9);
            assertEquals(0.9, personality.getScientificFocus());
        }

        @Test
        @DisplayName("Set expansion drive")
        void setExpansionDrive() {
            B5Personality personality = new B5Personality();
            personality.setExpansionDrive(0.7);
            assertEquals(0.7, personality.getExpansionDrive());
        }

        @Test
        @DisplayName("Set risk tolerance")
        void setRiskTolerance() {
            B5Personality personality = new B5Personality();
            personality.setRiskTolerance(0.6);
            assertEquals(0.6, personality.getRiskTolerance());
        }

        @Test
        @DisplayName("Set altruism")
        void setAltruism() {
            B5Personality personality = new B5Personality();
            personality.setAltruism(0.4);
            assertEquals(0.4, personality.getAltruism());
        }

        @Test
        @DisplayName("Set determination")
        void setDetermination() {
            B5Personality personality = new B5Personality();
            personality.setDetermination(0.95);
            assertEquals(0.95, personality.getDetermination());
        }

        @Test
        @DisplayName("Set adaptability")
        void setAdaptability() {
            B5Personality personality = new B5Personality();
            personality.setAdaptability(0.75);
            assertEquals(0.75, personality.getAdaptability());
        }

        @Test
        @DisplayName("Set leadership")
        void setLeadership() {
            B5Personality personality = new B5Personality();
            personality.setLeadership(0.85);
            assertEquals(0.85, personality.getLeadership());
        }

        @Test
        @DisplayName("Set cunning")
        void setCunning() {
            B5Personality personality = new B5Personality();
            personality.setCunning(0.65);
            assertEquals(0.65, personality.getCunning());
        }

        @Test
        @DisplayName("Set honor")
        void setHonor() {
            B5Personality personality = new B5Personality();
            personality.setHonor(1.0);
            assertEquals(1.0, personality.getHonor());
        }
    }

    @Nested
    @DisplayName("toMap")
    class ToMap {

        @Test
        @DisplayName("toMap returns all 12 axes")
        void allAxes() {
            B5Personality personality = new B5Personality();
            personality.setAggression(0.1);
            personality.setHonor(1.0);
            Map<String, Double> map = personality.toMap();
            assertEquals(12, map.size());
            assertEquals(0.1, map.get("aggression"));
            assertEquals(1.0, map.get("honor"));
        }

        @Test
        @DisplayName("toMap keys match axis names")
        void keys() {
            B5Personality personality = new B5Personality();
            Map<String, Double> map = personality.toMap();
            assertTrue(map.containsKey("aggression"));
            assertTrue(map.containsKey("diplomacy"));
            assertTrue(map.containsKey("economicFocus"));
            assertTrue(map.containsKey("scientificFocus"));
            assertTrue(map.containsKey("expansionDrive"));
            assertTrue(map.containsKey("riskTolerance"));
            assertTrue(map.containsKey("altruism"));
            assertTrue(map.containsKey("determination"));
            assertTrue(map.containsKey("adaptability"));
            assertTrue(map.containsKey("leadership"));
            assertTrue(map.containsKey("cunning"));
            assertTrue(map.containsKey("honor"));
        }
    }

    @Nested
    @DisplayName("toString")
    class ToString {

        @Test
        @DisplayName("toString includes formatted values")
        void toStringContent() {
            B5Personality personality = new B5Personality();
            personality.setAggression(0.5);
            personality.setHonor(1.0);
            String result = personality.toString();
            assertTrue(result.contains("aggression=0.50"));
            assertTrue(result.contains("honor=1.00"));
        }
    }
}
