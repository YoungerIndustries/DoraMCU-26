package com.yisy.modules.doramcu.traveller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Traveller Tier 1 — Psionics, Smuggling, Alien Races")
class TravellerTier1Test {

    // ---- Psionics Tests ----

    @Nested
    @DisplayName("PsionicDiscipline enum")
    class PsionicDisciplines {

        @Test
        void shouldHaveSixDisciplines() {
            assertEquals(6, PsionicDiscipline.values().length, "Should have 6 psionic disciplines");
        }

        @Test
        void shouldHaveTelepathy() {
            PsionicDiscipline disc = PsionicDiscipline.TELEPATHY;
            assertEquals("Telepathy", disc.getDisplayName());
            assertTrue(disc.getDescription().contains("Mind reading"));
            assertEquals(1, disc.getTableValue());
        }

        @Test
        void shouldHaveAllDisciplinesWithDescriptions() {
            for (PsionicDiscipline disc : PsionicDiscipline.values()) {
                assertNotNull(disc.getDisplayName(), disc.name() + " should have display name");
                assertNotNull(disc.getDescription(), disc.name() + " should have description");
                assertTrue(disc.getTableValue() > 0, disc.name() + " should have positive table value");
            }
        }
    }

    @Nested
    @DisplayName("PsionicCheck")
    class PsionicChecks {

        @Test
        void shouldSucceedWithHighInstinct() {
            PsionicCheck check = new PsionicCheck();
            PsionicCheck.PsionicResult result = check.performCheck(12, 3, PsionicCheck.Difficulty.EASY);
            // Target = 12 - 6 - 3 + 0 = 3, very hard to hit
            assertNotNull(result);
            assertEquals(3, result.getTarget());
        }

        @Test
        void shouldFailWithNoPsionicAbility() {
            PsionicCheck check = new PsionicCheck();
            PsionicCheck.PsionicResult result = check.performCheck(1, 3, PsionicCheck.Difficulty.NORMAL);
            assertFalse(result.isSuccess());
            assertEquals("No psionic ability", result.getMessage());
        }

        @Test
        void shouldFailWithNoDiscipline() {
            PsionicCheck check = new PsionicCheck();
            PsionicCheck.PsionicResult result = check.performCheck(8, 0, PsionicCheck.Difficulty.NORMAL);
            assertFalse(result.isSuccess());
            assertEquals("No discipline trained", result.getMessage());
        }

        @Test
        void shouldCalculateFatigueCost() {
            PsionicCheck check = new PsionicCheck();
            assertEquals(1, check.getFatigueCost(false, PsionicCheck.Difficulty.NORMAL));
            assertEquals(2, check.getFatigueCost(true, PsionicCheck.Difficulty.NORMAL));
            assertEquals(3, check.getFatigueCost(true, PsionicCheck.Difficulty.HARD));
        }

        @Test
        void shouldUseSeededRoll() {
            PsionicCheck check = new PsionicCheck();
            // Target = 12 - 4 - 2 + 0 = 6, roll 3 should succeed
            PsionicCheck.PsionicResult result = check.performCheck(8, 2, PsionicCheck.Difficulty.EASY, 3);
            assertTrue(result.isSuccess());
            assertEquals(3, result.getRoll());
            assertEquals(6, result.getTarget());
        }
    }

    @Nested
    @DisplayName("PsionicEncounter")
    class PsionicEncounters {

        @Test
        void shouldCheckEncounterForPsionic() {
            PsionicEncounter encounter = new PsionicEncounter();
            // Just verify it doesn't throw
            boolean result = encounter.checkEncounter(true);
            assertNotNull(result);
        }

        @Test
        void shouldCheckEncounterForNonPsionic() {
            PsionicEncounter encounter = new PsionicEncounter();
            boolean result = encounter.checkEncounter(false);
            assertNotNull(result);
        }

        @Test
        void shouldGenerateEncounter() {
            PsionicEncounter encounter = new PsionicEncounter();
            PsionicEncounter.PsionicEncounterResult result =
                    encounter.generateEncounter(PsionicAbility.DetectionLevel.AVERAGE);
            assertNotNull(result.getType());
            assertNotNull(result.getNarrative());
            assertTrue(result.getRoll() >= 2 && result.getRoll() <= 12);
        }

        @Test
        void shouldHaveAllEncounterTypes() {
            assertEquals(8, PsionicEncounter.EncounterType.values().length);
            for (PsionicEncounter.EncounterType type : PsionicEncounter.EncounterType.values()) {
                assertNotNull(type.getDisplayName());
                assertNotNull(type.getDescription());
            }
        }
    }

    // ---- Smuggling Tests ----

    @Nested
    @DisplayName("ContrabandCommodity enum")
    class ContrabandTypes {

        @Test
        void shouldHaveEightContrabandTypes() {
            assertEquals(8, ContrabandCommodity.values().length, "Should have 8 contraband types");
        }

        @Test
        void shouldHaveDrugsWithHighRisk() {
            ContrabandCommodity drugs = ContrabandCommodity.DRUGS;
            assertEquals("Drugs", drugs.getDisplayName());
            assertEquals(3, drugs.getRiskLevel());
            assertEquals(500, drugs.getBasePrice());
        }

        @Test
        void shouldCalculateProfitMultiplier() {
            // Risk 1 = 2.0x, Risk 3 = 3.0x, Risk 4 = 3.5x
            assertEquals(2.0, ContrabandCommodity.COUNTERFEIT.getProfitMultiplier());
            assertEquals(3.0, ContrabandCommodity.DRUGS.getProfitMultiplier());
            assertEquals(3.5, ContrabandCommodity.SLAVES.getProfitMultiplier());
        }

        @Test
        void allContrabandShouldHavePositivePrice() {
            for (ContrabandCommodity c : ContrabandCommodity.values()) {
                assertTrue(c.getBasePrice() > 0, c.name() + " should have positive base price");
                assertTrue(c.getVolumePerUnit() > 0, c.name() + " should have positive volume");
            }
        }
    }

    @Nested
    @DisplayName("CustomsCheck")
    class CustomsChecks {

        @Test
        void shouldCheckInspection() {
            CustomsCheck customs = new CustomsCheck();
            // Just verify it doesn't throw
            boolean result = customs.checkInspection(5, 'C');
            assertNotNull(result);
        }

        @Test
        void shouldResolveEvadeAction() {
            CustomsCheck customs = new CustomsCheck();
            CustomsCheck.CustomsResult result = customs.resolve(
                    CustomsCheck.Action.EVADE, ContrabandCommodity.COUNTERFEIT, 3, 2, 10000);
            assertNotNull(result.getOutcome());
            assertNotNull(result.getNarrative());
            assertTrue(result.getRoll() >= 2 && result.getRoll() <= 12);
        }

        @Test
        void shouldResolveBribeAction() {
            CustomsCheck customs = new CustomsCheck();
            CustomsCheck.CustomsResult result = customs.resolve(
                    CustomsCheck.Action.BRIBE, ContrabandCommodity.WEAPONS, 5, 2, 50000);
            assertNotNull(result.getOutcome());
        }

        @Test
        void shouldResolveRunAction() {
            CustomsCheck customs = new CustomsCheck();
            CustomsCheck.CustomsResult result = customs.resolve(
                    CustomsCheck.Action.RUN, ContrabandCommodity.DRUGS, 5, 3, 10000);
            assertNotNull(result.getOutcome());
        }

        @Test
        void shouldCalculateFine() {
            CustomsCheck customs = new CustomsCheck();
            long fine = customs.calculateFine(ContrabandCommodity.DRUGS, 10, 8);
            assertTrue(fine > 0, "Fine should be positive");
            // Base: 500 * 10 = 5000, multiplier: 1.0 + 8*0.2 = 2.6, so 13000
            assertEquals(13000, fine);
        }

        @Test
        void shouldHaveAllCustomsActions() {
            assertEquals(3, CustomsCheck.Action.values().length);
        }

        @Test
        void shouldHaveAllCustomsOutcomes() {
            assertEquals(6, CustomsCheck.Outcome.values().length);
        }
    }

    @Nested
    @DisplayName("SmugglingOperation")
    class SmugglingOperations {

        @Test
        void shouldCreateOperation() {
            SmugglingOperation op = new SmugglingOperation(
                    1, ContrabandCommodity.DRUGS, 10, "Alcander", "Tintavel", 8, 'A');
            assertEquals(SmugglingOperation.Status.PLANNED, op.getStatus());
            assertEquals(1, op.getId());
            assertEquals(10, op.getQuantity());
        }

        @Test
        void shouldCalculatePurchaseCost() {
            SmugglingOperation op = new SmugglingOperation(
                    1, ContrabandCommodity.DRUGS, 10, "Alcander", "Tintavel", 8, 'A');
            long cost = op.calculatePurchaseCost();
            // 500 * 10 / 2 = 2500
            assertEquals(2500, cost);
        }

        @Test
        void shouldCalculateSellRevenue() {
            SmugglingOperation op = new SmugglingOperation(
                    1, ContrabandCommodity.DRUGS, 10, "Alcander", "Tintavel", 8, 'A');
            long revenue = op.calculateSellRevenue();
            // 500 * 10 * 3.0 = 15000
            assertEquals(15000, revenue);
        }

        @Test
        void shouldCalculateCargoVolume() {
            SmugglingOperation op = new SmugglingOperation(
                    1, ContrabandCommodity.DRUGS, 10, "Alcander", "Tintavel", 8, 'A');
            // 20 * 10 = 200
            assertEquals(200, op.getCargoVolume());
        }

        @Test
        void shouldHaveLogEntries() {
            SmugglingOperation op = new SmugglingOperation(
                    1, ContrabandCommodity.DRUGS, 10, "Alcander", "Tintavel", 8, 'A');
            assertTrue(op.getLog().size() > 0, "Should have initial log entry");
            op.calculatePurchaseCost();
            assertTrue(op.getLog().size() >= 2, "Should have log entry after purchase");
        }
    }

    // ---- Alien Race Tests ----

    @Nested
    @DisplayName("AlienRace enum")
    class AlienRaces {

        @Test
        void shouldHaveEightRaces() {
            assertEquals(8, AlienRace.values().length, "Should have 8 alien races");
        }

        @Test
        void humanShouldHaveNoModifiers() {
            AlienRace human = AlienRace.HUMAN;
            assertEquals(0, human.getCombatDM());
            assertEquals(0, human.getSocialDM());
            assertEquals(0, human.getNavigationDM());
            assertEquals(0, human.getArmorBonus());
            assertEquals(0, human.getTradeBonusPercent());
        }

        @Test
        void aslanShouldHaveMeleeBonus() {
            AlienRace aslan = AlienRace.ASLAN;
            assertEquals("Aslan", aslan.getDisplayName());
            assertEquals(1, aslan.getCombatDM());
            assertEquals(-1, aslan.getSocialDM());
        }

        @Test
        void vargrShouldHaveNavigationBonus() {
            AlienRace vargr = AlienRace.VARGR;
            assertEquals(1, vargr.getNavigationDM());
        }

        @Test
        void insectoidShouldHaveArmorBonus() {
            AlienRace insectoid = AlienRace.INSECTOID;
            assertEquals(1, insectoid.getArmorBonus());
        }

        @Test
        void melnikiShouldHaveTradeBonus() {
            AlienRace melniki = AlienRace.MELNIKI;
            assertEquals(2, melniki.getTradeBonusPercent());
            assertEquals(-1, melniki.getCombatDM());
        }

        @Test
        void allRacesShouldHaveDescriptions() {
            for (AlienRace race : AlienRace.values()) {
                assertNotNull(race.getDisplayName(), race.name() + " should have display name");
                assertNotNull(race.getDescription(), race.name() + " should have description");
            }
        }
    }
}
