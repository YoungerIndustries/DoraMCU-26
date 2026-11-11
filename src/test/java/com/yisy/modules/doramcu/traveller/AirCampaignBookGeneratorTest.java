package com.yisy.modules.doramcu.traveller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AirCampaignBookGenerator")
class AirCampaignBookGeneratorTest {

    @Nested
    @DisplayName("Generate Book")
    class GenerateBook {

        @Test
        @DisplayName("generates book with campaign name")
        void generatesBookWithCampaignName() {
            AirCampaignBookGenerator generator = new AirCampaignBookGenerator();
            AirCampaignLog campaign = new AirCampaignLog("camp-1", "Battle of Britain");

            String book = generator.generateBook(campaign);

            assertTrue(book.contains("# Battle of Britain"));
        }

        @Test
        @DisplayName("generates book with nationality")
        void generatesBookWithNationality() {
            AirCampaignBookGenerator generator = new AirCampaignBookGenerator();
            AirCampaignLog campaign = new AirCampaignLog("camp-1", "Battle of Britain");
            campaign.setNationality("United Kingdom");

            String book = generator.generateBook(campaign);

            assertTrue(book.contains("**Nationality:** United Kingdom"));
        }

        @Test
        @DisplayName("generates book with dates")
        void generatesBookWithDates() {
            AirCampaignBookGenerator generator = new AirCampaignBookGenerator();
            AirCampaignLog campaign = new AirCampaignLog("camp-1", "Battle of Britain");
            campaign.setStartDate("1940-07-10");
            campaign.setEndDate("1940-10-31");

            String book = generator.generateBook(campaign);

            assertTrue(book.contains("1940-07-10"));
            assertTrue(book.contains("1940-10-31"));
        }

        @Test
        @DisplayName("generates book with table of contents")
        void generatesTableOfContents() {
            AirCampaignBookGenerator generator = new AirCampaignBookGenerator();
            AirCampaignLog campaign = new AirCampaignLog("camp-1", "Battle of Britain");
            AirBattleExperience exp1 = new AirBattleExperience("battle-1", "Luftwaffe", "Germany");
            AirBattleExperience exp2 = new AirBattleExperience("battle-2", "RAF", "UK");
            campaign.addBattle(exp1);
            campaign.addBattle(exp2);

            String book = generator.generateBook(campaign);

            assertTrue(book.contains("## Table of Contents"));
            assertTrue(book.contains("Battle 1"));
            assertTrue(book.contains("Battle 2"));
        }

        @Test
        @DisplayName("generates book with battle details")
        void generatesBattleDetails() {
            AirCampaignBookGenerator generator = new AirCampaignBookGenerator();
            AirCampaignLog campaign = new AirCampaignLog("camp-1", "Battle of Britain");
            AirBattleExperience exp = new AirBattleExperience("battle-1", "Luftwaffe", "Germany");
            exp.setAltitude("HIGH");
            exp.setCrtResult("Good - Enemy eliminated");
            exp.setLosses(3);
            exp.setNarrative("The fighters engaged at high altitude.");
            campaign.addBattle(exp);

            String book = generator.generateBook(campaign);

            assertTrue(book.contains("**Air Force:** Luftwaffe"));
            assertTrue(book.contains("**Altitude:** HIGH"));
            assertTrue(book.contains("**CRT Result:** Good - Enemy eliminated"));
            assertTrue(book.contains("**Losses:** 3"));
            assertTrue(book.contains("The fighters engaged at high altitude."));
        }

        @Test
        @DisplayName("generates book with epilogue")
        void generatesEpilogue() {
            AirCampaignBookGenerator generator = new AirCampaignBookGenerator();
            AirCampaignLog campaign = new AirCampaignLog("camp-1", "Battle of Britain");
            campaign.setTotalAttackerLosses(10);
            campaign.setTotalDefenderLosses(5);

            String book = generator.generateBook(campaign);

            assertTrue(book.contains("## Epilogue"));
            assertTrue(book.contains("10 attacker losses"));
            assertTrue(book.contains("5 defender losses"));
        }

        @Test
        @DisplayName("generates book for empty campaign")
        void generatesEmptyBook() {
            AirCampaignBookGenerator generator = new AirCampaignBookGenerator();
            AirCampaignLog campaign = new AirCampaignLog("camp-1", "Empty Campaign");

            String book = generator.generateBook(campaign);

            assertTrue(book.contains("# Empty Campaign"));
            assertTrue(book.contains("## Epilogue"));
        }
    }
}
