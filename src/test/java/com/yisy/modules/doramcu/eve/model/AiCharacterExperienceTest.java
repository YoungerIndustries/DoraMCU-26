package com.yisy.modules.doramcu.eve.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AiCharacter Experience and Hiring Tests")
class AiCharacterExperienceTest {

    @Nested
    @DisplayName("Constructors")
    class ConstructorTests {

        @Test
        @DisplayName("no-arg constructor should create empty character with defaults")
        void noArgConstructor() {
            AiCharacter character = new AiCharacter();
            assertNull(character.getId());
            assertNull(character.getName());
            assertEquals(0, character.getTotalExperience());
            assertEquals(5, character.getLeadershipRating());
            assertEquals(0, character.getTotalBattles());
            assertEquals(0, character.getTotalVictories());
            assertTrue(character.isHirable());
            assertEquals(100L, character.getDailySalary());
            assertEquals(AiCharacter.HiringStatus.AVAILABLE, character.getHiringStatus());
            assertTrue(character.isActive());
            assertFalse(character.isCanPromoteToCoServer());
            assertNull(character.getLinkedPlayerId());
            assertNull(character.getDescription());
            assertNull(character.getPrimarySkill());
        }

        @Test
        @DisplayName("full constructor should set name, role, and empireName")
        void fullConstructor() {
            AiCharacter character = new AiCharacter("Sif Younger", "Leader", "Interstellar Alliance");
            assertEquals("Sif Younger", character.getName());
            assertEquals("Leader", character.getRole());
            assertEquals("Interstellar Alliance", character.getEmpireName());
        }
    }

    @Nested
    @DisplayName("General Fields")
    class GeneralFieldTests {

        @Test
        @DisplayName("should set and get linkedPlayerId")
        void setLinkedPlayerId() {
            AiCharacter character = new AiCharacter();
            character.setLinkedPlayerId("player-123");
            assertEquals("player-123", character.getLinkedPlayerId());
        }

        @Test
        @DisplayName("should set and get description")
        void setDescription() {
            AiCharacter character = new AiCharacter();
            character.setDescription("A brave AI commander");
            assertEquals("A brave AI commander", character.getDescription());
        }

        @Test
        @DisplayName("should set and get active")
        void setActive() {
            AiCharacter character = new AiCharacter();
            character.setActive(false);
            assertFalse(character.isActive());
        }

        @Test
        @DisplayName("should set and get canPromoteToCoServer")
        void setCanPromoteToCoServer() {
            AiCharacter character = new AiCharacter();
            character.setCanPromoteToCoServer(true);
            assertTrue(character.isCanPromoteToCoServer());
        }

        @Test
        @DisplayName("should set and get id")
        void setId() {
            AiCharacter character = new AiCharacter();
            character.setId(42L);
            assertEquals(42L, character.getId());
        }

        @Test
        @DisplayName("should set and get name")
        void setName() {
            AiCharacter character = new AiCharacter();
            character.setName("K'tharr");
            assertEquals("K'tharr", character.getName());
        }

        @Test
        @DisplayName("should set and get role")
        void setRole() {
            AiCharacter character = new AiCharacter();
            character.setRole("General");
            assertEquals("General", character.getRole());
        }

        @Test
        @DisplayName("should set and get empireName")
        void setEmpireName() {
            AiCharacter character = new AiCharacter();
            character.setEmpireName("Stellar Hegemony");
            assertEquals("Stellar Hegemony", character.getEmpireName());
        }
    }

    @Nested
    @DisplayName("HiringStatus Enum")
    class HiringStatusEnumTests {

        @Test
        @DisplayName("should have all 3 hiring status values")
        void allHiringStatusValues() {
            AiCharacter.HiringStatus[] statuses = AiCharacter.HiringStatus.values();
            assertEquals(3, statuses.length);
        }

        @Test
        @DisplayName("AVAILABLE status should be accessible")
        void availableStatus() {
            assertEquals("AVAILABLE", AiCharacter.HiringStatus.AVAILABLE.name());
        }

        @Test
        @DisplayName("HIRED status should be accessible")
        void hiredStatus() {
            assertEquals("HIRED", AiCharacter.HiringStatus.HIRED.name());
        }

        @Test
        @DisplayName("UNAVAILABLE status should be accessible")
        void unavailableStatus() {
            assertEquals("UNAVAILABLE", AiCharacter.HiringStatus.UNAVAILABLE.name());
        }
    }

    @Nested
    @DisplayName("Experience Fields")
    class ExperienceFieldTests {

        @Test
        @DisplayName("totalExperience should default to 0")
        void defaultTotalExperience() {
            AiCharacter character = new AiCharacter();
            assertEquals(0, character.getTotalExperience());
        }

        @Test
        @DisplayName("leadershipRating should default to 5")
        void defaultLeadershipRating() {
            AiCharacter character = new AiCharacter();
            assertEquals(5, character.getLeadershipRating());
        }

        @Test
        @DisplayName("totalBattles should default to 0")
        void defaultTotalBattles() {
            AiCharacter character = new AiCharacter();
            assertEquals(0, character.getTotalBattles());
        }

        @Test
        @DisplayName("totalVictories should default to 0")
        void defaultTotalVictories() {
            AiCharacter character = new AiCharacter();
            assertEquals(0, character.getTotalVictories());
        }

        @Test
        @DisplayName("should set and get totalExperience")
        void setTotalExperience() {
            AiCharacter character = new AiCharacter();
            character.setTotalExperience(250);
            assertEquals(250, character.getTotalExperience());
        }

        @Test
        @DisplayName("should set and get totalBattles")
        void setTotalBattles() {
            AiCharacter character = new AiCharacter();
            character.setTotalBattles(50);
            assertEquals(50, character.getTotalBattles());
        }

        @Test
        @DisplayName("should set and get totalVictories")
        void setTotalVictories() {
            AiCharacter character = new AiCharacter();
            character.setTotalVictories(40);
            assertEquals(40, character.getTotalVictories());
        }
    }

    @Nested
    @DisplayName("Leadership Rating Clamping")
    class LeadershipRatingTests {

        @Test
        @DisplayName("leadership rating should be clamped to minimum 1")
        void clampMinimumLeadership() {
            AiCharacter character = new AiCharacter();
            character.setLeadershipRating(0);
            assertEquals(1, character.getLeadershipRating());
        }

        @Test
        @DisplayName("leadership rating should be clamped to maximum 10")
        void clampMaximumLeadership() {
            AiCharacter character = new AiCharacter();
            character.setLeadershipRating(15);
            assertEquals(10, character.getLeadershipRating());
        }

        @Test
        @DisplayName("leadership rating should accept values within range")
        void leadershipWithinRange() {
            AiCharacter character = new AiCharacter();
            character.setLeadershipRating(7);
            assertEquals(7, character.getLeadershipRating());
        }

        @Test
        @DisplayName("leadership rating should clamp negative values to 1")
        void clampNegativeLeadership() {
            AiCharacter character = new AiCharacter();
            character.setLeadershipRating(-5);
            assertEquals(1, character.getLeadershipRating());
        }
    }

    @Nested
    @DisplayName("addExperience")
    class AddExperienceTests {

        @Test
        @DisplayName("should add XP to total experience")
        void addXp() {
            AiCharacter character = new AiCharacter();
            character.addExperience(50);
            assertEquals(50, character.getTotalExperience());
        }

        @Test
        @DisplayName("should accumulate XP across multiple calls")
        void accumulateXp() {
            AiCharacter character = new AiCharacter();
            character.addExperience(30);
            character.addExperience(20);
            character.addExperience(50);
            assertEquals(100, character.getTotalExperience());
        }

        @Test
        @DisplayName("should increase leadership every 100 XP")
        void leadershipIncreasesEvery100Xp() {
            AiCharacter character = new AiCharacter();
            character.addExperience(99);
            assertEquals(5, character.getLeadershipRating());
            character.addExperience(1);
            assertEquals(6, character.getLeadershipRating());
        }

        @Test
        @DisplayName("should cap leadership at 10")
        void leadershipCappedAt10() {
            AiCharacter character = new AiCharacter();
            character.addExperience(500);
            assertEquals(10, character.getLeadershipRating());
        }

        @Test
        @DisplayName("should increase leadership by 2 for 200 XP")
        void leadershipIncreasesBy2For200Xp() {
            AiCharacter character = new AiCharacter();
            character.addExperience(200);
            assertEquals(7, character.getLeadershipRating());
        }

        @Test
        @DisplayName("should not increase leadership for less than 100 XP")
        void noLeadershipIncreaseBelow100Xp() {
            AiCharacter character = new AiCharacter();
            character.addExperience(99);
            assertEquals(5, character.getLeadershipRating());
        }
    }

    @Nested
    @DisplayName("recordVictory")
    class RecordVictoryTests {

        @Test
        @DisplayName("should increment totalBattles and totalVictories")
        void recordVictory() {
            AiCharacter character = new AiCharacter();
            character.recordVictory();
            assertEquals(1, character.getTotalBattles());
            assertEquals(1, character.getTotalVictories());
        }

        @Test
        @DisplayName("should accumulate victories across multiple calls")
        void multipleVictories() {
            AiCharacter character = new AiCharacter();
            character.recordVictory();
            character.recordVictory();
            character.recordVictory();
            assertEquals(3, character.getTotalBattles());
            assertEquals(3, character.getTotalVictories());
        }
    }

    @Nested
    @DisplayName("recordDefeat")
    class RecordDefeatTests {

        @Test
        @DisplayName("should increment totalBattles but not totalVictories")
        void recordDefeat() {
            AiCharacter character = new AiCharacter();
            character.recordDefeat();
            assertEquals(1, character.getTotalBattles());
            assertEquals(0, character.getTotalVictories());
        }

        @Test
        @DisplayName("should accumulate battles across multiple defeats")
        void multipleDefeats() {
            AiCharacter character = new AiCharacter();
            character.recordDefeat();
            character.recordDefeat();
            assertEquals(2, character.getTotalBattles());
            assertEquals(0, character.getTotalVictories());
        }

        @Test
        @DisplayName("mixed victories and defeats should track correctly")
        void mixedVictoriesAndDefeats() {
            AiCharacter character = new AiCharacter();
            character.recordVictory();
            character.recordDefeat();
            character.recordVictory();
            assertEquals(3, character.getTotalBattles());
            assertEquals(2, character.getTotalVictories());
        }
    }

    @Nested
    @DisplayName("Hiring Fields")
    class HiringFieldTests {

        @Test
        @DisplayName("hirable should default to true")
        void defaultHirable() {
            AiCharacter character = new AiCharacter();
            assertTrue(character.isHirable());
        }

        @Test
        @DisplayName("dailySalary should default to 100")
        void defaultDailySalary() {
            AiCharacter character = new AiCharacter();
            assertEquals(100L, character.getDailySalary());
        }

        @Test
        @DisplayName("hiringStatus should default to AVAILABLE")
        void defaultHiringStatus() {
            AiCharacter character = new AiCharacter();
            assertEquals(AiCharacter.HiringStatus.AVAILABLE, character.getHiringStatus());
        }

        @Test
        @DisplayName("should set and get hirable")
        void setHirable() {
            AiCharacter character = new AiCharacter();
            character.setHirable(false);
            assertFalse(character.isHirable());
        }

        @Test
        @DisplayName("should set and get dailySalary")
        void setDailySalary() {
            AiCharacter character = new AiCharacter();
            character.setDailySalary(500L);
            assertEquals(500L, character.getDailySalary());
        }

        @Test
        @DisplayName("should set and get hiringStatus")
        void setHiringStatus() {
            AiCharacter character = new AiCharacter();
            character.setHiringStatus(AiCharacter.HiringStatus.HIRED);
            assertEquals(AiCharacter.HiringStatus.HIRED, character.getHiringStatus());
        }

        @Test
        @DisplayName("should set and get primarySkill")
        void setPrimarySkill() {
            AiCharacter character = new AiCharacter();
            character.setPrimarySkill("Armor Tactics");
            assertEquals("Armor Tactics", character.getPrimarySkill());
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringTests {

        @Test
        @DisplayName("toString should include key fields")
        void toStringContent() {
            AiCharacter character = new AiCharacter("Sif Younger", "Leader", "Interstellar Alliance");
            character.setId(1L);
            character.setTotalExperience(250);
            character.setLeadershipRating(7);
            character.setTotalBattles(10);
            character.setTotalVictories(8);
            character.setHiringStatus(AiCharacter.HiringStatus.HIRED);

            String str = character.toString();
            assertTrue(str.contains("AiCharacter{"));
            assertTrue(str.contains("id=1"));
            assertTrue(str.contains("name='Sif Younger'"));
            assertTrue(str.contains("role='Leader'"));
            assertTrue(str.contains("empire='Interstellar Alliance'"));
            assertTrue(str.contains("xp=250"));
            assertTrue(str.contains("leadership=7"));
            assertTrue(str.contains("battles=10"));
            assertTrue(str.contains("victories=8"));
            assertTrue(str.contains("hiring=HIRED"));
        }

        @Test
        @DisplayName("toString should handle null fields gracefully")
        void toStringNullFields() {
            AiCharacter character = new AiCharacter();
            String str = character.toString();
            assertNotNull(str);
            assertTrue(str.contains("AiCharacter{"));
        }
    }
}
