package com.yisy.modules.doradl.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GameStateFeatures Tests")
class GameStateFeaturesTest {

    @Nested
    @DisplayName("Constants")
    class Constants {

        @Test
        @DisplayName("FEATURE_DIMENSION is 64")
        void featureDimension() {
            assertEquals(64, GameStateFeatures.FEATURE_DIMENSION);
        }

        @Test
        @DisplayName("ORIGINAL_DIMENSION is 32")
        void originalDimension() {
            assertEquals(32, GameStateFeatures.ORIGINAL_DIMENSION);
        }

        @Test
        @DisplayName("EMPIRE_FEATURE_START is 32")
        void empireFeatureStart() {
            assertEquals(32, GameStateFeatures.EMPIRE_FEATURE_START);
        }
    }

    @Nested
    @DisplayName("Default constructor")
    class DefaultConstructor {

        @Test
        @DisplayName("Creates 64 zero-filled features")
        void createsZeroFilled() {
            GameStateFeatures features = new GameStateFeatures();
            double[] arr = features.getFeatures();
            assertEquals(64, arr.length);
            for (double v : arr) {
                assertEquals(0.0, v);
            }
        }
    }

    @Nested
    @DisplayName("Parameterized constructor")
    class ParameterizedConstructor {

        @Test
        @DisplayName("Clones valid 64-dim input")
        void clonesInput() {
            double[] input = new double[64];
            input[0] = 0.5;
            GameStateFeatures features = new GameStateFeatures(input);
            assertNotSame(input, features.getFeatures());
            assertEquals(0.5, features.getFeature(0));
        }

        @Test
        @DisplayName("Throws IllegalArgumentException for wrong dimension")
        void wrongDimension() {
            assertThrows(IllegalArgumentException.class, () -> new GameStateFeatures(new double[32]));
            assertThrows(IllegalArgumentException.class, () -> new GameStateFeatures(new double[65]));
        }
    }

    @Nested
    @DisplayName("getFeatures()")
    class GetFeatures {

        @Test
        @DisplayName("Returns a clone — modifying returned array does not affect internal state")
        void returnsClone() {
            GameStateFeatures features = new GameStateFeatures();
            features.setFeature(0, 0.5);
            double[] retrieved = features.getFeatures();
            retrieved[0] = 0.0;
            assertEquals(0.5, features.getFeature(0));
        }
    }

    @Nested
    @DisplayName("setFeatures()")
    class SetFeatures {

        @Test
        @DisplayName("Copies values from valid 64-dim array")
        void copiesValues() {
            GameStateFeatures features = new GameStateFeatures();
            double[] input = new double[64];
            input[0] = 0.5;
            input[63] = 1.0;
            features.setFeatures(input);
            assertEquals(0.5, features.getFeature(0));
            assertEquals(1.0, features.getFeature(63));
        }

        @Test
        @DisplayName("Throws IllegalArgumentException for wrong dimension")
        void wrongDimension() {
            GameStateFeatures features = new GameStateFeatures();
            assertThrows(IllegalArgumentException.class, () -> features.setFeatures(new double[32]));
        }
    }

    @Nested
    @DisplayName("getFeature/setFeature")
    class GetSetFeature {

        @Test
        @DisplayName("Set and get at index 0")
        void index0() {
            GameStateFeatures features = new GameStateFeatures();
            features.setFeature(0, 0.5);
            assertEquals(0.5, features.getFeature(0));
        }

        @Test
        @DisplayName("Set and get at index 31")
        void index31() {
            GameStateFeatures features = new GameStateFeatures();
            features.setFeature(31, 0.75);
            assertEquals(0.75, features.getFeature(31));
        }

        @Test
        @DisplayName("Set and get at index 32")
        void index32() {
            GameStateFeatures features = new GameStateFeatures();
            features.setFeature(32, 0.25);
            assertEquals(0.25, features.getFeature(32));
        }

        @Test
        @DisplayName("Set and get at index 63")
        void index63() {
            GameStateFeatures features = new GameStateFeatures();
            features.setFeature(63, 1.0);
            assertEquals(1.0, features.getFeature(63));
        }

        @Test
        @DisplayName("Throws IndexOutOfBoundsException for index -1")
        void negativeIndex() {
            GameStateFeatures features = new GameStateFeatures();
            assertThrows(IndexOutOfBoundsException.class, () -> features.getFeature(-1));
            assertThrows(IndexOutOfBoundsException.class, () -> features.setFeature(-1, 0.0));
        }

        @Test
        @DisplayName("Throws IndexOutOfBoundsException for index 64")
        void outOfBoundsIndex() {
            GameStateFeatures features = new GameStateFeatures();
            assertThrows(IndexOutOfBoundsException.class, () -> features.getFeature(64));
            assertThrows(IndexOutOfBoundsException.class, () -> features.setFeature(64, 0.0));
        }
    }

    @Nested
    @DisplayName("Domain setters")
    class DomainSetters {

        @Test
        @DisplayName("setResources sets indices 0-4")
        void setResources() {
            GameStateFeatures features = new GameStateFeatures();
            features.setResources(0.5, 0.3, 0.8, 0.2, 0.1);
            assertEquals(0.5, features.getFeature(0));
            assertEquals(0.3, features.getFeature(1));
            assertEquals(0.8, features.getFeature(2));
            assertEquals(0.2, features.getFeature(3));
            assertEquals(0.1, features.getFeature(4));
        }

        @Test
        @DisplayName("setAssets sets indices 5-7")
        void setAssets() {
            GameStateFeatures features = new GameStateFeatures();
            features.setAssets(0.4, 0.6, 0.7);
            assertEquals(0.4, features.getFeature(5));
            assertEquals(0.6, features.getFeature(6));
            assertEquals(0.7, features.getFeature(7));
        }

        @Test
        @DisplayName("setFleetPosition sets indices 8-10")
        void setFleetPosition() {
            GameStateFeatures features = new GameStateFeatures();
            features.setFleetPosition(0.1, 0.2, 0.3);
            assertEquals(0.1, features.getFeature(8));
            assertEquals(0.2, features.getFeature(9));
            assertEquals(0.3, features.getFeature(10));
        }

        @Test
        @DisplayName("setEnemyInfo sets indices 11-13")
        void setEnemyInfo() {
            GameStateFeatures features = new GameStateFeatures();
            features.setEnemyInfo(0.5, 0.3, 0.8);
            assertEquals(0.5, features.getFeature(11));
            assertEquals(0.3, features.getFeature(12));
            assertEquals(0.8, features.getFeature(13));
        }

        @Test
        @DisplayName("setGameState sets indices 14-16")
        void setGameState() {
            GameStateFeatures features = new GameStateFeatures();
            features.setGameState(0.1, 0.25, 0.5);
            assertEquals(0.1, features.getFeature(14));
            assertEquals(0.25, features.getFeature(15));
            assertEquals(0.5, features.getFeature(16));
        }

        @Test
        @DisplayName("setRelativeStrength sets indices 17-18")
        void setRelativeStrength() {
            GameStateFeatures features = new GameStateFeatures();
            features.setRelativeStrength(1.5, 2.0);
            assertEquals(1.5, features.getFeature(17));
            assertEquals(2.0, features.getFeature(18));
        }

        @Test
        @DisplayName("setResourceTrends sets indices 19-20")
        void setResourceTrends() {
            GameStateFeatures features = new GameStateFeatures();
            features.setResourceTrends(0.1, -0.2);
            assertEquals(0.1, features.getFeature(19));
            assertEquals(-0.2, features.getFeature(20));
        }

        @Test
        @DisplayName("setStrategicContext sets indices 21-23")
        void setStrategicContext() {
            GameStateFeatures features = new GameStateFeatures();
            features.setStrategicContext(1.0, 0.0, 1.0);
            assertEquals(1.0, features.getFeature(21));
            assertEquals(0.0, features.getFeature(22));
            assertEquals(1.0, features.getFeature(23));
        }

        @Test
        @DisplayName("setDifficultyBias sets index 24")
        void setDifficultyBias() {
            GameStateFeatures features = new GameStateFeatures();
            features.setDifficultyBias(0.5);
            assertEquals(0.5, features.getFeature(24));
        }

        @Test
        @DisplayName("setTurnPhase early=true, mid=false")
        void turnPhaseEarly() {
            GameStateFeatures features = new GameStateFeatures();
            features.setTurnPhase(true, false);
            assertEquals(1.0, features.getFeature(25));
            assertEquals(0.0, features.getFeature(26));
        }

        @Test
        @DisplayName("setTurnPhase early=false, mid=true")
        void turnPhaseMid() {
            GameStateFeatures features = new GameStateFeatures();
            features.setTurnPhase(false, true);
            assertEquals(0.0, features.getFeature(25));
            assertEquals(1.0, features.getFeature(26));
        }

        @Test
        @DisplayName("setTurnPhase early=false, mid=false (late)")
        void turnPhaseLate() {
            GameStateFeatures features = new GameStateFeatures();
            features.setTurnPhase(false, false);
            assertEquals(0.0, features.getFeature(25));
            assertEquals(0.0, features.getFeature(26));
        }

        @Test
        @DisplayName("setPersonality sets indices 32-33")
        void setPersonality() {
            GameStateFeatures features = new GameStateFeatures();
            features.setPersonality(0.8, 0.3);
            assertEquals(0.8, features.getFeature(32));
            assertEquals(0.3, features.getFeature(33));
        }

        @Test
        @DisplayName("setGovernmentEthos sets indices 34-35")
        void setGovernmentEthos() {
            GameStateFeatures features = new GameStateFeatures();
            features.setGovernmentEthos(0.5, 0.4);
            assertEquals(0.5, features.getFeature(34));
            assertEquals(0.4, features.getFeature(35));
        }

        @Test
        @DisplayName("setResearchLevel sets indices 36-37")
        void setResearchLevel() {
            GameStateFeatures features = new GameStateFeatures();
            features.setResearchLevel(0.7, 0.5);
            assertEquals(0.7, features.getFeature(36));
            assertEquals(0.5, features.getFeature(37));
        }

        @Test
        @DisplayName("setFleetComposition sets indices 38-39")
        void setFleetComposition() {
            GameStateFeatures features = new GameStateFeatures();
            features.setFleetComposition(0.6, 0.2);
            assertEquals(0.6, features.getFeature(38));
            assertEquals(0.2, features.getFeature(39));
        }

        @Test
        @DisplayName("setStationCoverage sets indices 40-41")
        void setStationCoverage() {
            GameStateFeatures features = new GameStateFeatures();
            features.setStationCoverage(0.3, 0.5);
            assertEquals(0.3, features.getFeature(40));
            assertEquals(0.5, features.getFeature(41));
        }

        @Test
        @DisplayName("setEconomicHealth sets indices 42-43")
        void setEconomicHealth() {
            GameStateFeatures features = new GameStateFeatures();
            features.setEconomicHealth(0.8, 0.4);
            assertEquals(0.8, features.getFeature(42));
            assertEquals(0.4, features.getFeature(43));
        }

        @Test
        @DisplayName("setDiplomaticContext sets indices 44-45")
        void setDiplomaticContext() {
            GameStateFeatures features = new GameStateFeatures();
            features.setDiplomaticContext(0.5, 0.25);
            assertEquals(0.5, features.getFeature(44));
            assertEquals(0.25, features.getFeature(45));
        }

        @Test
        @DisplayName("setTacticalSituation sets indices 46-47")
        void setTacticalSituation() {
            GameStateFeatures features = new GameStateFeatures();
            features.setTacticalSituation(0.2, 0.1);
            assertEquals(0.2, features.getFeature(46));
            assertEquals(0.1, features.getFeature(47));
        }

        @Test
        @DisplayName("setTechnologyEdge sets indices 48-49")
        void setTechnologyEdge() {
            GameStateFeatures features = new GameStateFeatures();
            features.setTechnologyEdge(0.9, 0.7);
            assertEquals(0.9, features.getFeature(48));
            assertEquals(0.7, features.getFeature(49));
        }

        @Test
        @DisplayName("setResourcePipeline sets indices 50-51")
        void setResourcePipeline() {
            GameStateFeatures features = new GameStateFeatures();
            features.setResourcePipeline(0.6, 0.4);
            assertEquals(0.6, features.getFeature(50));
            assertEquals(0.4, features.getFeature(51));
        }

        @Test
        @DisplayName("setFleetReadiness sets indices 52-53")
        void setFleetReadiness() {
            GameStateFeatures features = new GameStateFeatures();
            features.setFleetReadiness(0.85, 0.9);
            assertEquals(0.85, features.getFeature(52));
            assertEquals(0.9, features.getFeature(53));
        }

        @Test
        @DisplayName("setStrategicDepth sets indices 54-55")
        void setStrategicDepth() {
            GameStateFeatures features = new GameStateFeatures();
            features.setStrategicDepth(0.3, 0.7);
            assertEquals(0.3, features.getFeature(54));
            assertEquals(0.7, features.getFeature(55));
        }
    }

    @Nested
    @DisplayName("clear()")
    class Clear {

        @Test
        @DisplayName("Resets all features to zero")
        void resetsAll() {
            GameStateFeatures features = new GameStateFeatures();
            features.setFeature(0, 0.5);
            features.setFeature(32, 1.0);
            features.setFeature(63, 0.25);
            features.clear();
            for (int i = 0; i < 64; i++) {
                assertEquals(0.0, features.getFeature(i));
            }
        }
    }

    @Nested
    @DisplayName("equals/hashCode")
    class EqualsAndHashCode {

        @Test
        @DisplayName("Equal features are equal")
        void equalFeatures() {
            double[] data = new double[64];
            data[0] = 0.5;
            GameStateFeatures a = new GameStateFeatures(data);
            GameStateFeatures b = new GameStateFeatures(data);
            assertEquals(a, b);
            assertEquals(a.hashCode(), b.hashCode());
        }

        @Test
        @DisplayName("Different features are not equal")
        void differentFeatures() {
            GameStateFeatures a = new GameStateFeatures();
            GameStateFeatures b = new GameStateFeatures();
            b.setFeature(0, 1.0);
            assertNotEquals(a, b);
        }
    }
}
