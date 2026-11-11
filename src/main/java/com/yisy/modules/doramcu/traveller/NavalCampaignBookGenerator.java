package com.yisy.modules.doramcu.traveller;

import java.util.Objects;

/**
 * Generates a narrative campaign book from a {@link NavalCampaignLog}.
 * <p>
 * The book follows the same structure as {@link BookGenerator} but is specialized
 * for naval campaigns. It generates:
 * </p>
 * <ul>
 *   <li><b>Title page</b> — campaign overview with key statistics</li>
 *   <li><b>Table of contents</b> — chapters for each turn and battle</li>
 *   <li><b>Turn chapters</b> — strategic decisions and events for each campaign turn</li>
 *   <li><b>Battle chapters</b> — narrative descriptions of each engagement</li>
 *   <li><b>Epilogue</b> — campaign summary and final assessment</li>
 * </ul>
 * <p>
 * Usage:
 * </p>
 * <pre>
 * NavalCampaignLog log = ...;
 * NavalCampaignBookGenerator generator = new NavalCampaignBookGenerator();
 * String book = generator.generateBook(log);
 * </pre>
 */
public class NavalCampaignBookGenerator {

    /**
     * Generates a complete campaign book from a naval campaign log.
     *
     * @param log the campaign log
     * @return the full book as a formatted string
     */
    public String generateBook(NavalCampaignLog log) {
        Objects.requireNonNull(log, "Campaign log must not be null");

        StringBuilder book = new StringBuilder();

        // Title page
        appendTitlePage(book, log);

        // Table of contents
        appendTableOfContents(book, log);

        // Turn chapters
        appendTurnChapters(book, log);

        // Battle chapters
        appendBattleChapters(book, log);

        // Epilogue
        appendEpilogue(book, log);

        return book.toString();
    }

    /**
     * Generates a summary book — shorter version with key highlights.
     *
     * @param log the campaign log
     * @return a condensed book
     */
    public String generateSummaryBook(NavalCampaignLog log) {
        Objects.requireNonNull(log, "Campaign log must not be null");

        StringBuilder book = new StringBuilder();

        book.append("========================================\n");
        book.append("  ").append(log.getCampaignName()).append("\n");
        book.append("  Campaign Summary\n");
        book.append("========================================\n\n");

        if (!log.getNationality().isEmpty()) {
            book.append("NATION: ").append(log.getNationality()).append("\n");
        }
        book.append("STATUS: ").append(log.getStatus().getDisplayName()).append("\n");
        book.append("BATTLES: ").append(log.getBattleCount()).append("\n");
        book.append("RECORD: ").append(log.getVictories()).append("W-")
                .append(log.getDefeats()).append("L-")
                .append(log.getDraws()).append("D\n");
        book.append("VICTORY RATE: ").append(String.format("%.1f", log.getVictoryRate())).append("%\n");
        book.append("CASUALTIES: ").append(log.getTotalCasualties()).append(" lost, ")
                .append(log.getTotalEnemyCasualties()).append(" enemy destroyed\n\n");

        book.append("--- BATTLES ---\n");
        for (NavalBattleExperience battle : log.getBattles()) {
            book.append("  ").append(battle.getSummary()).append("\n");
        }

        book.append("\n========================================\n");
        book.append("  End of Summary\n");
        book.append("========================================\n");

        return book.toString();
    }

    // ============================================================
    // Book Sections
    // ============================================================

    private void appendTitlePage(StringBuilder book, NavalCampaignLog log) {
        book.append("========================================\n");
        book.append("  NAVAL CAMPAIGN REPORT\n");
        book.append("  ").append(log.getCampaignName()).append("\n");
        book.append("========================================\n\n");

        if (!log.getNationality().isEmpty()) {
            book.append("Conducted by: ").append(log.getNationality()).append("\n");
        }
        book.append("Status: ").append(log.getStatus().getDisplayName()).append("\n");

        if (log.getStartDate() != null) {
            book.append("Started: ").append(log.getStartDate()).append("\n");
        }
        if (log.getEndDate() != null) {
            book.append("Ended: ").append(log.getEndDate()).append("\n");
        }

        book.append("\n--- CAMPAIGN OVERVIEW ---\n");
        if (!log.getDescription().isEmpty()) {
            book.append(log.getDescription()).append("\n\n");
        }

        book.append("Total Battles: ").append(log.getBattleCount()).append("\n");
        book.append("Record: ").append(log.getVictories()).append(" Victories, ")
                .append(log.getDefeats()).append(" Defeats, ")
                .append(log.getDraws()).append(" Draws\n");
        book.append("Victory Rate: ").append(String.format("%.1f", log.getVictoryRate())).append("%\n");
        book.append("Friendly Losses: ").append(log.getTotalCasualties()).append(" vessels\n");
        book.append("Enemy Destroyed: ").append(log.getTotalEnemyCasualties()).append(" vessels\n\n");

        book.append("\n========================================\n\n\n");
    }

    private void appendTableOfContents(StringBuilder book, NavalCampaignLog log) {
        book.append("========================================\n");
        book.append("  TABLE OF CONTENTS\n");
        book.append("========================================\n\n");

        int chapterNumber = 1;

        // Turn chapters
        for (NavalCampaignLog.CampaignTurn turn : log.getTurns()) {
            book.append("  Chapter ").append(chapterNumber).append(": ")
                    .append("Turn ").append(turn.getTurnNumber())
                    .append(" — ").append(turn.getDescription()).append("\n");
            chapterNumber++;
        }

        // Battle chapters
        for (NavalBattleExperience battle : log.getBattles()) {
            book.append("  Chapter ").append(chapterNumber).append(": ")
                    .append(battle.getTitle()).append("\n");
            chapterNumber++;
        }

        book.append("  Epilogue — Campaign Assessment\n\n");
        book.append("========================================\n\n\n");
    }

    private void appendTurnChapters(StringBuilder book, NavalCampaignLog log) {
        int chapterNumber = 1;

        for (NavalCampaignLog.CampaignTurn turn : log.getTurns()) {
            book.append("========================================\n");
            book.append("  CHAPTER ").append(chapterNumber).append(": ")
                    .append("TURN ").append(turn.getTurnNumber()).append("\n");
            book.append("  ").append(turn.getDescription()).append("\n");
            book.append("========================================\n\n");

            if (turn.getDate() != null) {
                book.append("Date: ").append(turn.getDate()).append("\n\n");
            }

            if (!turn.getDecisions().isEmpty()) {
                book.append("--- Strategic Decisions ---\n\n");
                for (String decision : turn.getDecisions()) {
                    book.append("  ").append(decision).append("\n");
                }
                book.append("\n");
            }

            book.append("\n\n");
            chapterNumber++;
        }
    }

    private void appendBattleChapters(StringBuilder book, NavalCampaignLog log) {
        int chapterNumber = 1 + log.getTurnCount();

        for (NavalBattleExperience battle : log.getBattles()) {
            book.append("========================================\n");
            book.append("  CHAPTER ").append(chapterNumber).append(": ")
                    .append(battle.getTitle().toUpperCase()).append("\n");
            if (!battle.getSeaZone().isEmpty()) {
                book.append("  ").append(battle.getSeaZone()).append("\n");
            }
            book.append("========================================\n\n");

            // Battle metadata
            if (!battle.getSeaZone().isEmpty()) {
                book.append("Location: ").append(battle.getSeaZone()).append("\n");
            }
            if (battle.getDate() != null) {
                book.append("Date: ").append(battle.getFormattedDate()).append("\n");
            }
            if (!battle.getFleetName().isEmpty()) {
                book.append("Fleet: ").append(battle.getFleetName()).append("\n");
            }
            book.append("\n");

            // Battle details
            if (!battle.getOddsRatio().isEmpty()) {
                book.append("Combat Odds: ").append(battle.getOddsRatio()).append("\n");
            }
            if (battle.getRoundsFought() > 0) {
                book.append("Rounds Fought: ").append(battle.getRoundsFought()).append("\n");
            }
            if (!battle.getBattleMode().isEmpty()) {
                book.append("Mode: ").append(battle.getBattleMode()).append("\n");
            }
            book.append("\n");

            // Narrative
            if (!battle.getNarrative().isEmpty()) {
                book.append("--- Battle Narrative ---\n\n");
                book.append(battle.getNarrative()).append("\n\n");
            } else if (!battle.getDescription().isEmpty()) {
                book.append(battle.getDescription()).append("\n\n");
            }

            // Casualties
            book.append("--- Casualties ---\n\n");
            book.append("Friendly Losses: ").append(battle.getCasualties()).append(" vessel(s)\n");
            book.append("Enemy Destroyed: ").append(battle.getEnemyCasualties()).append(" vessel(s)\n\n");

            // Vessels engaged
            if (!battle.getVesselNames().isEmpty()) {
                book.append("--- Vessels Engaged ---\n\n");
                for (String vesselName : battle.getVesselNames()) {
                    book.append("  - ").append(vesselName).append("\n");
                }
                book.append("\n");
            }

            // Outcome
            book.append("[Outcome: ").append(battle.getOutcome().getDisplayName()).append("]\n\n");
            book.append("\n\n");

            chapterNumber++;
        }
    }

    private void appendEpilogue(StringBuilder book, NavalCampaignLog log) {
        book.append("========================================\n");
        book.append("  EPILOGUE\n");
        book.append("  Campaign Assessment\n");
        book.append("========================================\n\n");

        book.append("The ").append(log.getCampaignName()).append(" has ");
        if (log.getStatus() == NavalCampaignLog.CampaignStatus.COMPLETED) {
            book.append("concluded");
        } else if (log.getStatus() == NavalCampaignLog.CampaignStatus.ABANDONED) {
            book.append("been abandoned");
        } else {
            book.append("ongoing");
        }
        book.append(".\n\n");

        book.append("Over the course of ").append(log.getBattleCount()).append(" battle(s),");
        if (!log.getNationality().isEmpty()) {
            book.append(" the ").append(log.getNationality()).append(" fleet");
        } else {
            book.append(" our fleet");
        }
        book.append(" achieved a record of ");
        book.append(log.getVictories()).append(" victories, ");
        book.append(log.getDefeats()).append(" defeats, and ");
        book.append(log.getDraws()).append(" draws.\n\n");

        // Assessment
        double victoryRate = log.getVictoryRate();
        if (victoryRate >= 75) {
            book.append("This was a highly successful campaign. The fleet demonstrated");
            book.append(" superior tactics and resolve in the face of the enemy.\n\n");
        } else if (victoryRate >= 50) {
            book.append("The campaign yielded mixed results. While victories were achieved,");
            book.append(" the heavy losses suggest room for improvement in strategy.\n\n");
        } else if (victoryRate > 0) {
            book.append("The campaign was costly. Despite some successes, the fleet suffered");
            book.append(" significant losses that will take time to recover from.\n\n");
        } else {
            book.append("The campaign ended in defeat. The lessons learned will be vital");
            book.append(" for future operations.\n\n");
        }

        // Final statistics
        book.append("--- Final Statistics ---\n\n");
        book.append("Total Battles: ").append(log.getBattleCount()).append("\n");
        book.append("Victory Rate: ").append(String.format("%.1f", victoryRate)).append("%\n");
        book.append("Friendly Vessels Lost: ").append(log.getTotalCasualties()).append("\n");
        book.append("Enemy Vessels Destroyed: ").append(log.getTotalEnemyCasualties()).append("\n");

        if (log.getTotalCasualties() > 0 && log.getTotalEnemyCasualties() > 0) {
            double exchangeRatio = (double) log.getTotalEnemyCasualties() / log.getTotalCasualties();
            book.append("Exchange Ratio: ").append(String.format("%.1f", exchangeRatio))
                    .append(":1 (enemy/friendly)\n");
        }

        book.append("\n========================================\n");
        book.append("  END OF CAMPAIGN REPORT\n");
        book.append("========================================\n");
    }
}
