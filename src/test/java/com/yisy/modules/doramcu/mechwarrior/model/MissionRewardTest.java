package com.yisy.modules.doramcu.mechwarrior.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MissionReward Model Tests")
class MissionRewardTest {

    private MissionReward reward;

    @BeforeEach
    void setUp() {
        reward = new MissionReward(MissionReward.RewardType.CREDITS, 10000);
    }

    // ---- Constructor ----

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("should initialize with amount constructor")
        void constructor_shouldInitializeWithAmount() {
            assertNotNull(reward.getId());
            assertEquals(MissionReward.RewardType.CREDITS, reward.getType());
            assertEquals(10000, reward.getAmount());
            assertFalse(reward.isClaimed());
            assertFalse(reward.isBonus());
        }

        @Test
        @DisplayName("should initialize with item name constructor")
        void constructor_shouldInitializeWithItemName() {
            MissionReward unlock = new MissionReward(
                    MissionReward.RewardType.MECH_UNLOCK,
                    "Atlas AS7-D",
                    "Unlock the Atlas Assault Mech"
            );
            assertEquals(MissionReward.RewardType.MECH_UNLOCK, unlock.getType());
            assertEquals("Atlas AS7-D", unlock.getItemName());
            assertEquals("Unlock the Atlas Assault Mech", unlock.getDescription());
            assertEquals(0, unlock.getAmount());
            assertFalse(unlock.isClaimed());
        }

        @Test
        @DisplayName("should create with no-arg constructor")
        void noArgConstructor_shouldCreateEmptyReward() {
            MissionReward empty = new MissionReward();
            assertNull(empty.getId());
            assertNull(empty.getType());
        }

        @Test
        @DisplayName("should support all reward types")
        void constructor_shouldSupportAllRewardTypes() {
            for (MissionReward.RewardType type : MissionReward.RewardType.values()) {
                MissionReward r = new MissionReward(type, 100);
                assertEquals(type, r.getType());
            }
        }
    }

    // ---- Claim ----

    @Nested
    @DisplayName("Claim Tests")
    class ClaimTests {

        @Test
        @DisplayName("should mark as claimed")
        void claim_shouldMarkClaimed() {
            reward.claim();
            assertTrue(reward.isClaimed());
        }

        @Test
        @DisplayName("should be unclaimed by default")
        void claim_shouldBeUnclaimedByDefault() {
            assertFalse(reward.isClaimed());
        }

        @Test
        @DisplayName("should allow setting claimed directly")
        void claim_shouldAllowDirectSet() {
            reward.setClaimed(true);
            assertTrue(reward.isClaimed());
        }
    }

    // ---- Bonus Rewards ----

    @Nested
    @DisplayName("Bonus Reward Tests")
    class BonusTests {

        @Test
        @DisplayName("should set bonus flag")
        void setBonus_shouldSetFlag() {
            reward.setBonus(true);
            assertTrue(reward.isBonus());
        }

        @Test
        @DisplayName("should be non-bonus by default")
        void setBonus_shouldBeNonBonusByDefault() {
            assertFalse(reward.isBonus());
        }

        @Test
        @DisplayName("should set requirement text")
        void setRequirement_shouldSetRequirement() {
            reward.setRequirement("Complete mission without taking damage");
            assertEquals("Complete mission without taking damage", reward.getRequirement());
        }

        @Test
        @DisplayName("should have null requirement by default")
        void setRequirement_shouldBeNullByDefault() {
            assertNull(reward.getRequirement());
        }
    }

    // ---- Faction ----

    @Nested
    @DisplayName("Faction Tests")
    class FactionTests {

        @Test
        @DisplayName("should set faction for reputation rewards")
        void setFaction_shouldSetFaction() {
            reward.setFaction("Federated Suns");
            assertEquals("Federated Suns", reward.getFaction());
        }

        @Test
        @DisplayName("should have null faction by default")
        void setFaction_shouldBeNullByDefault() {
            assertNull(reward.getFaction());
        }
    }

    // ---- Reward Type Specific Tests ----

    @Nested
    @DisplayName("Reward Type Specific Tests")
    class RewardTypeTests {

        @Test
        @DisplayName("should create credits reward")
        void creditsReward_shouldHaveAmount() {
            MissionReward credits = new MissionReward(MissionReward.RewardType.CREDITS, 25000);
            assertEquals(MissionReward.RewardType.CREDITS, credits.getType());
            assertEquals(25000, credits.getAmount());
        }

        @Test
        @DisplayName("should create experience reward")
        void experienceReward_shouldHaveAmount() {
            MissionReward xp = new MissionReward(MissionReward.RewardType.EXPERIENCE, 200);
            assertEquals(MissionReward.RewardType.EXPERIENCE, xp.getType());
            assertEquals(200, xp.getAmount());
        }

        @Test
        @DisplayName("should create reputation reward with faction")
        void reputationReward_shouldHaveFaction() {
            MissionReward rep = new MissionReward(MissionReward.RewardType.REPUTATION, 15);
            rep.setFaction("Clan Wolf");
            assertEquals(MissionReward.RewardType.REPUTATION, rep.getType());
            assertEquals(15, rep.getAmount());
            assertEquals("Clan Wolf", rep.getFaction());
        }

        @Test
        @DisplayName("should create mech unlock reward")
        void mechUnlockReward_shouldHaveItemName() {
            MissionReward mech = new MissionReward(
                    MissionReward.RewardType.MECH_UNLOCK,
                    "Griffin GF-6D",
                    "Unlock the Griffin Medium Mech"
            );
            assertEquals(MissionReward.RewardType.MECH_UNLOCK, mech.getType());
            assertEquals("Griffin GF-6D", mech.getItemName());
        }

        @Test
        @DisplayName("should create weapon unlock reward")
        void weaponUnlockReward_shouldHaveItemName() {
            MissionReward weapon = new MissionReward(
                    MissionReward.RewardType.WEAPON_UNLOCK,
                    "Large Laser",
                    "Unlock the Large Laser weapon"
            );
            assertEquals(MissionReward.RewardType.WEAPON_UNLOCK, weapon.getType());
            assertEquals("Large Laser", weapon.getItemName());
        }

        @Test
        @DisplayName("should create trait unlock reward")
        void traitUnlockReward_shouldHaveItemName() {
            MissionReward trait = new MissionReward(
                    MissionReward.RewardType.TRAIT_UNLOCK,
                    "Iron Will",
                    "Unlock the Iron Will pilot trait"
            );
            assertEquals(MissionReward.RewardType.TRAIT_UNLOCK, trait.getType());
            assertEquals("Iron Will", trait.getItemName());
        }

        @Test
        @DisplayName("should create skill bonus reward")
        void skillBonusReward_shouldHaveAmount() {
            MissionReward skill = new MissionReward(MissionReward.RewardType.SKILL_BONUS, 5);
            assertEquals(MissionReward.RewardType.SKILL_BONUS, skill.getType());
            assertEquals(5, skill.getAmount());
        }

        @Test
        @DisplayName("should create repair credit reward")
        void repairCreditReward_shouldHaveAmount() {
            MissionReward repair = new MissionReward(MissionReward.RewardType.REPAIR_CREDIT, 5000);
            assertEquals(MissionReward.RewardType.REPAIR_CREDIT, repair.getType());
            assertEquals(5000, repair.getAmount());
        }

        @Test
        @DisplayName("should create contract bonus reward")
        void contractBonusReward_shouldHaveAmount() {
            MissionReward contract = new MissionReward(MissionReward.RewardType.CONTRACT_BONUS, 10000);
            assertEquals(MissionReward.RewardType.CONTRACT_BONUS, contract.getType());
            assertEquals(10000, contract.getAmount());
        }

        @Test
        @DisplayName("should create salvage reward")
        void salvageReward_shouldHaveAmount() {
            MissionReward salvage = new MissionReward(MissionReward.RewardType.SALVAGE, 15000);
            assertEquals(MissionReward.RewardType.SALVAGE, salvage.getType());
            assertEquals(15000, salvage.getAmount());
        }
    }

    // ---- Getters and Setters ----

    @Nested
    @DisplayName("Getters and Setters Tests")
    class GetterSetterTests {

        @Test
        @DisplayName("should set and get ID")
        void setId_shouldSetId() {
            reward.setId("test-id");
            assertEquals("test-id", reward.getId());
        }

        @Test
        @DisplayName("should set and get type")
        void setType_shouldSetType() {
            reward.setType(MissionReward.RewardType.EXPERIENCE);
            assertEquals(MissionReward.RewardType.EXPERIENCE, reward.getType());
        }

        @Test
        @DisplayName("should set and get amount")
        void setAmount_shouldSetAmount() {
            reward.setAmount(50000);
            assertEquals(50000, reward.getAmount());
        }

        @Test
        @DisplayName("should set and get item name")
        void setItemName_shouldSetItemName() {
            reward.setItemName("Test Item");
            assertEquals("Test Item", reward.getItemName());
        }

        @Test
        @DisplayName("should set and get description")
        void setDescription_shouldSetDescription() {
            reward.setDescription("Test description");
            assertEquals("Test description", reward.getDescription());
        }
    }
}
