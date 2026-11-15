package com.yisy.modules.doramcu.eve.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("VoiceProfile Tests")
class VoiceProfileTest {

    @Nested
    @DisplayName("Default Values")
    class DefaultValuesTests {

        @Test
        @DisplayName("should have default pitch of 1.0")
        void shouldHaveDefaultPitch() {
            VoiceProfile profile = new VoiceProfile();
            assertEquals(1.0, profile.getPitch(), 0.01);
        }

        @Test
        @DisplayName("should have default speed of 1.0")
        void shouldHaveDefaultSpeed() {
            VoiceProfile profile = new VoiceProfile();
            assertEquals(1.0, profile.getSpeed(), 0.01);
        }

        @Test
        @DisplayName("should have null name initially")
        void shouldHaveNullName() {
            VoiceProfile profile = new VoiceProfile();
            assertNull(profile.getName());
        }

        @Test
        @DisplayName("should have null description initially")
        void shouldHaveNullDescription() {
            VoiceProfile profile = new VoiceProfile();
            assertNull(profile.getDescription());
        }

        @Test
        @DisplayName("should have null ttsModel initially")
        void shouldHaveNullTtsModel() {
            VoiceProfile profile = new VoiceProfile();
            assertNull(profile.getTtsModel());
        }

        @Test
        @DisplayName("should have null ttsVoice initially")
        void shouldHaveNullTtsVoice() {
            VoiceProfile profile = new VoiceProfile();
            assertNull(profile.getTtsVoice());
        }

        @Test
        @DisplayName("should have null tone initially")
        void shouldHaveNullTone() {
            VoiceProfile profile = new VoiceProfile();
            assertNull(profile.getTone());
        }

        @Test
        @DisplayName("should have null language initially")
        void shouldHaveNullLanguage() {
            VoiceProfile profile = new VoiceProfile();
            assertNull(profile.getLanguage());
        }
    }

    @Nested
    @DisplayName("Setters")
    class SetterTests {

        @Test
        @DisplayName("should set name")
        void shouldSetName() {
            VoiceProfile profile = new VoiceProfile();
            profile.setName("Robot");
            assertEquals("Robot", profile.getName());
        }

        @Test
        @DisplayName("should set description")
        void shouldSetDescription() {
            VoiceProfile profile = new VoiceProfile();
            profile.setDescription("Mechanical voice");
            assertEquals("Mechanical voice", profile.getDescription());
        }

        @Test
        @DisplayName("should set ttsModel")
        void shouldSetTtsModel() {
            VoiceProfile profile = new VoiceProfile();
            profile.setTtsModel("custom/tts");
            assertEquals("custom/tts", profile.getTtsModel());
        }

        @Test
        @DisplayName("should set ttsVoice")
        void shouldSetTtsVoice() {
            VoiceProfile profile = new VoiceProfile();
            profile.setTtsVoice("alloy");
            assertEquals("alloy", profile.getTtsVoice());
        }

        @Test
        @DisplayName("should set tone")
        void shouldSetTone() {
            VoiceProfile profile = new VoiceProfile();
            profile.setTone("deep");
            assertEquals("deep", profile.getTone());
        }

        @Test
        @DisplayName("should set language")
        void shouldSetLanguage() {
            VoiceProfile profile = new VoiceProfile();
            profile.setLanguage("en-US");
            assertEquals("en-US", profile.getLanguage());
        }
    }

    @Nested
    @DisplayName("Pitch Clamping")
    class PitchClampingTests {

        @Test
        @DisplayName("should clamp pitch to minimum 0.5")
        void shouldClampPitchMin() {
            VoiceProfile profile = new VoiceProfile();
            profile.setPitch(0.1);
            assertEquals(0.5, profile.getPitch(), 0.01);
        }

        @Test
        @DisplayName("should clamp pitch to maximum 2.0")
        void shouldClampPitchMax() {
            VoiceProfile profile = new VoiceProfile();
            profile.setPitch(3.0);
            assertEquals(2.0, profile.getPitch(), 0.01);
        }

        @Test
        @DisplayName("should accept pitch within range")
        void shouldAcceptPitchInRange() {
            VoiceProfile profile = new VoiceProfile();
            profile.setPitch(1.5);
            assertEquals(1.5, profile.getPitch(), 0.01);
        }

        @Test
        @DisplayName("should accept pitch at boundary 0.5")
        void shouldAcceptPitchAtMinBoundary() {
            VoiceProfile profile = new VoiceProfile();
            profile.setPitch(0.5);
            assertEquals(0.5, profile.getPitch(), 0.01);
        }

        @Test
        @DisplayName("should accept pitch at boundary 2.0")
        void shouldAcceptPitchAtMaxBoundary() {
            VoiceProfile profile = new VoiceProfile();
            profile.setPitch(2.0);
            assertEquals(2.0, profile.getPitch(), 0.01);
        }
    }

    @Nested
    @DisplayName("Speed Clamping")
    class SpeedClampingTests {

        @Test
        @DisplayName("should clamp speed to minimum 0.5")
        void shouldClampSpeedMin() {
            VoiceProfile profile = new VoiceProfile();
            profile.setSpeed(0.1);
            assertEquals(0.5, profile.getSpeed(), 0.01);
        }

        @Test
        @DisplayName("should clamp speed to maximum 2.0")
        void shouldClampSpeedMax() {
            VoiceProfile profile = new VoiceProfile();
            profile.setSpeed(3.0);
            assertEquals(2.0, profile.getSpeed(), 0.01);
        }

        @Test
        @DisplayName("should accept speed within range")
        void shouldAcceptSpeedInRange() {
            VoiceProfile profile = new VoiceProfile();
            profile.setSpeed(0.7);
            assertEquals(0.7, profile.getSpeed(), 0.01);
        }
    }

    @Nested
    @DisplayName("Timestamps")
    class TimestampTests {

        @Test
        @DisplayName("should have null createdAt initially")
        void shouldHaveNullCreatedAt() {
            VoiceProfile profile = new VoiceProfile();
            assertNull(profile.getCreatedAt());
        }

        @Test
        @DisplayName("should have null updatedAt initially")
        void shouldHaveNullUpdatedAt() {
            VoiceProfile profile = new VoiceProfile();
            assertNull(profile.getUpdatedAt());
        }
    }

    @Nested
    @DisplayName("ID")
    class IdTests {

        @Test
        @DisplayName("should set and get ID")
        void shouldSetAndGetId() {
            VoiceProfile profile = new VoiceProfile();
            profile.setId(42L);
            assertEquals(42L, profile.getId());
        }

        @Test
        @DisplayName("should have null ID initially")
        void shouldHaveNullId() {
            VoiceProfile profile = new VoiceProfile();
            assertNull(profile.getId());
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringTests {

        @Test
        @DisplayName("should include name and pitch in toString")
        void shouldIncludeFieldsInToString() {
            VoiceProfile profile = new VoiceProfile();
            profile.setId(1L);
            profile.setName("Robot");
            profile.setPitch(0.7);
            profile.setSpeed(0.9);
            profile.setTone("mechanical");
            profile.setTtsVoice("robot-voice");

            String str = profile.toString();
            assertTrue(str.contains("Robot"));
            assertTrue(str.contains("0.7"));
            assertTrue(str.contains("mechanical"));
        }
    }

    @Nested
    @DisplayName("Default Profile Scenarios")
    class DefaultProfileScenarios {

        @Test
        @DisplayName("should create a default male profile")
        void shouldCreateDefaultMaleProfile() {
            VoiceProfile profile = new VoiceProfile();
            profile.setName("Default Male");
            profile.setDescription("Male voice with neutral tone");
            profile.setTone("warm");
            profile.setLanguage("en-US");

            assertEquals("Default Male", profile.getName());
            assertEquals("warm", profile.getTone());
            assertEquals(1.0, profile.getPitch(), 0.01);
            assertEquals(1.0, profile.getSpeed(), 0.01);
        }

        @Test
        @DisplayName("should create a robot profile")
        void shouldCreateRobotProfile() {
            VoiceProfile profile = new VoiceProfile();
            profile.setName("Robot");
            profile.setDescription("Mechanical voice with deep tone");
            profile.setPitch(0.7);
            profile.setSpeed(0.9);
            profile.setTone("mechanical");
            profile.setLanguage("en-US");

            assertEquals("Robot", profile.getName());
            assertEquals(0.7, profile.getPitch(), 0.01);
            assertEquals(0.9, profile.getSpeed(), 0.01);
            assertEquals("mechanical", profile.getTone());
        }

        @Test
        @DisplayName("should create an elder profile")
        void shouldCreateElderProfile() {
            VoiceProfile profile = new VoiceProfile();
            profile.setName("Elder");
            profile.setDescription("Wise voice with slow speed");
            profile.setSpeed(0.7);
            profile.setTone("wise");

            assertEquals("Elder", profile.getName());
            assertEquals(0.7, profile.getSpeed(), 0.01);
            assertEquals("wise", profile.getTone());
        }

        @Test
        @DisplayName("should create a monster profile")
        void shouldCreateMonsterProfile() {
            VoiceProfile profile = new VoiceProfile();
            profile.setName("Monster");
            profile.setDescription("Growling voice with low pitch");
            profile.setPitch(0.5);
            profile.setSpeed(0.8);
            profile.setTone("deep");

            assertEquals("Monster", profile.getName());
            assertEquals(0.5, profile.getPitch(), 0.01);
            assertEquals(0.8, profile.getSpeed(), 0.01);
            assertEquals("deep", profile.getTone());
        }
    }
}
