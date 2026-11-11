package com.yisy.modules.doramcu.traveller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Aggregates naval battle experiences into a campaign.
 * <p>
 * A naval campaign represents a series of related naval battles fought over a period of time,
 * such as the "Battle of the Atlantic" or "Pacific Island Campaign". The campaign log tracks:
 * </p>
 * <ul>
 *   <li><b>Campaign name</b> — descriptive name (e.g., "Battle of the Atlantic")</li>
 *   <li><b>Dates</b> — start and end dates of the campaign</li>
 *   <li><b>Battles</b> — list of {@link NavalBattleExperience} entries</li>
 *   <li><b>Turns</b> — campaign turns with strategic decisions</li>
 *   <li><b>Nationality</b> — the primary nation conducting the campaign</li>
 * </ul>
 * <p>
 * Used by {@link NavalCampaignBookGenerator} to generate narrative campaign books.
 * </p>
 */
public class NavalCampaignLog {

    private final String id;
    private String campaignName;
    private String nationality;
    private LocalDate startDate;
    private LocalDate endDate;
    private final List<NavalBattleExperience> battles;
    private final List<CampaignTurn> turns;
    private String description;
    private CampaignStatus status;

    /**
     * Status of the campaign.
     */
    public enum CampaignStatus {
        /** Campaign is ongoing */
        ACTIVE("Active"),
        /** Campaign has concluded */
        COMPLETED("Completed"),
        /** Campaign was abandoned */
        ABANDONED("Abandoned");

        private final String displayName;

        CampaignStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() { return displayName; }
    }

    /**
     * A single turn in a naval campaign.
     */
    public static class CampaignTurn {
        private final int turnNumber;
        private final String description;
        private final LocalDate date;
        private final List<String> decisions;

        CampaignTurn(int turnNumber, String description, LocalDate date) {
            this.turnNumber = turnNumber;
            this.description = Objects.requireNonNull(description, "Description must not be null");
            this.date = date;
            this.decisions = new ArrayList<>();
        }

        public int getTurnNumber() { return turnNumber; }
        public String getDescription() { return description; }
        public LocalDate getDate() { return date; }

        public List<String> getDecisions() {
            return Collections.unmodifiableList(decisions);
        }

        public void addDecision(String decision) {
            Objects.requireNonNull(decision, "Decision must not be null");
            decisions.add(decision);
        }
    }

    /**
     * Creates a new naval campaign log.
     *
     * @param id the unique campaign ID
     * @param campaignName the name of the campaign
     */
    public NavalCampaignLog(String id, String campaignName) {
        this.id = Objects.requireNonNull(id, "ID must not be null");
        this.campaignName = Objects.requireNonNull(campaignName, "Campaign name must not be null");
        this.nationality = "";
        this.battles = new ArrayList<>();
        this.turns = new ArrayList<>();
        this.description = "";
        this.status = CampaignStatus.ACTIVE;
    }

    // ---- Accessors & Mutators ----

    /** Returns the campaign ID. */
    public String getId() { return id; }

    /** Returns the campaign name. */
    public String getCampaignName() { return campaignName; }

    /** Sets the campaign name. */
    public void setCampaignName(String campaignName) {
        this.campaignName = Objects.requireNonNull(campaignName, "Campaign name must not be null");
    }

    /** Returns the primary nationality. */
    public String getNationality() { return nationality; }

    /** Sets the primary nationality. */
    public void setNationality(String nationality) {
        this.nationality = Objects.requireNonNull(nationality, "Nationality must not be null");
    }

    /** Returns the campaign start date. */
    public LocalDate getStartDate() { return startDate; }

    /** Sets the campaign start date. */
    public void setStartDate(LocalDate startDate) {
        this.startDate = Objects.requireNonNull(startDate, "Start date must not be null");
    }

    /** Returns the campaign end date. */
    public LocalDate getEndDate() { return endDate; }

    /** Sets the campaign end date. */
    public void setEndDate(LocalDate endDate) {
        this.endDate = Objects.requireNonNull(endDate, "End date must not be null");
        this.status = CampaignStatus.COMPLETED;
    }

    /** Returns the campaign description. */
    public String getDescription() { return description; }

    /** Sets the campaign description. */
    public void setDescription(String description) {
        this.description = Objects.requireNonNull(description, "Description must not be null");
    }

    /** Returns the campaign status. */
    public CampaignStatus getStatus() { return status; }

    /** Sets the campaign status. */
    public void setStatus(CampaignStatus status) {
        this.status = Objects.requireNonNull(status, "Status must not be null");
    }

    // ---- Battle Management ----

    /**
     * Returns the list of battle experiences (immutable).
     *
     * @return the battles list
     */
    public List<NavalBattleExperience> getBattles() {
        return Collections.unmodifiableList(battles);
    }

    /**
     * Adds a battle experience to the campaign.
     *
     * @param battle the battle experience to add
     * @return this log for chaining
     */
    public NavalCampaignLog addBattle(NavalBattleExperience battle) {
        Objects.requireNonNull(battle, "Battle must not be null");
        battles.add(battle);
        return this;
    }

    /**
     * Returns the number of battles in the campaign.
     *
     * @return battle count
     */
    public int getBattleCount() {
        return battles.size();
    }

    // ---- Turn Management ----

    /**
     * Returns the list of campaign turns (immutable).
     *
     * @return the turns list
     */
    public List<CampaignTurn> getTurns() {
        return Collections.unmodifiableList(turns);
    }

    /**
     * Adds a new campaign turn.
     *
     * @param description the turn description
     * @param date the turn date
     * @return the new turn
     */
    public CampaignTurn addTurn(String description, LocalDate date) {
        int turnNumber = turns.size() + 1;
        CampaignTurn turn = new CampaignTurn(turnNumber, description, date);
        turns.add(turn);
        return turn;
    }

    /**
     * Returns the number of turns.
     *
     * @return turn count
     */
    public int getTurnCount() {
        return turns.size();
    }

    // ---- Statistics ----

    /**
     * Returns the total number of friendly vessels lost across all battles.
     *
     * @return total casualties
     */
    public int getTotalCasualties() {
        return battles.stream().mapToInt(NavalBattleExperience::getCasualties).sum();
    }

    /**
     * Returns the total number of enemy vessels destroyed across all battles.
     *
     * @return total enemy casualties
     */
    public int getTotalEnemyCasualties() {
        return battles.stream().mapToInt(NavalBattleExperience::getEnemyCasualties).sum();
    }

    /**
     * Returns the number of battles won.
     *
     * @return victories count
     */
    public int getVictories() {
        return (int) battles.stream()
                .filter(b -> b.getOutcome() == CharacterExperience.Outcome.SUCCESS)
                .count();
    }

    /**
     * Returns the number of battles lost.
     *
     * @return defeats count
     */
    public int getDefeats() {
        return (int) battles.stream()
                .filter(b -> b.getOutcome() == CharacterExperience.Outcome.FAILURE)
                .count();
    }

    /**
     * Returns the number of draws.
     *
     * @return draws count
     */
    public int getDraws() {
        return (int) battles.stream()
                .filter(b -> b.getOutcome() == CharacterExperience.Outcome.MIXED)
                .count();
    }

    /**
     * Returns the victory rate as a percentage.
     *
     * @return victory rate (0-100)
     */
    public double getVictoryRate() {
        if (battles.isEmpty()) {
            return 0.0;
        }
        return (getVictories() * 100.0) / battles.size();
    }

    // ---- Utility Methods ----

    /**
     * Returns a summary of the campaign.
     *
     * @return a formatted summary
     */
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append(campaignName).append("\n");
        sb.append("Status: ").append(status.getDisplayName()).append("\n");
        if (startDate != null) {
            sb.append("Started: ").append(startDate).append("\n");
        }
        if (endDate != null) {
            sb.append("Ended: ").append(endDate).append("\n");
        }
        sb.append("Battles: ").append(battles.size()).append("\n");
        sb.append("Record: ").append(getVictories()).append("W-")
                .append(getDefeats()).append("L-").append(getDraws()).append("D\n");
        sb.append("Casualties: ").append(getTotalCasualties()).append(" lost, ")
                .append(getTotalEnemyCasualties()).append(" enemy destroyed");
        return sb.toString();
    }

    @Override
    public String toString() {
        return getSummary();
    }
}
