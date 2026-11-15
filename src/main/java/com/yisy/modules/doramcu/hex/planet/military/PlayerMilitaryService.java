package com.yisy.modules.doramcu.hex.planet.military;

import com.yisy.modules.doramcu.earth2026.model.Earth2026Country;
import com.yisy.modules.doramcu.earth2026.service.PlanetService;
import com.yisy.modules.doramcu.thirdreich.domain.ThirdReichGroundUnit;
import com.yisy.modules.doramcu.thirdreich.domain.ThirdReichUnitType;
import com.yisy.modules.doramcu.thirdreich.service.CountryCampaignService;
import com.yisy.modules.doramcu.thirdreich.service.CountryCampaignService.CountryCampaign;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service for player military enlistment.
 * <p>
 * Players can enlist in a country's military branch (ARMY, NAVY, AIR, SPACE_NAVY).
 * Upon enlistment, the player is assigned to a military unit produced by the
 * country's military infrastructure and participates in battles as part of
 * that country's faction.
 * </p>
 */
@Service
public class PlayerMilitaryService {

    private static final Logger log = LoggerFactory.getLogger(PlayerMilitaryService.class);

    private final PlanetService planetService;
    private final CountryCampaignService countryCampaignService;

    /** Maps player ID to their military record */
    private final Map<String, PlayerMilitaryRecord> records = new ConcurrentHashMap<>();

    private final AtomicInteger recordIdCounter = new AtomicInteger(1);

    public PlayerMilitaryService(
            PlanetService planetService,
            CountryCampaignService countryCampaignService) {
        this.planetService = planetService;
        this.countryCampaignService = countryCampaignService;
    }

    /**
     * Enlist a player in a country's military branch.
     *
     * @param playerId   the player's unique identifier
     * @param planetId   the planet ID
     * @param countryId  the country ID
     * @param branch     the military branch
     * @return the player's military record
     */
    public PlayerMilitaryRecord enlist(String playerId, Long planetId, Long countryId, MilitaryBranch branch) {
        // Check if already enlisted
        if (records.containsKey(playerId)) {
            log.debug("Player {} is already enlisted", playerId);
            return records.get(playerId);
        }

        // Get the country
        Optional<Earth2026Country> countryOpt = planetService.getCountry(countryId);
        if (countryOpt.isEmpty()) {
            throw new IllegalArgumentException("Country not found: " + countryId);
        }
        Earth2026Country country = countryOpt.get();

        // Ensure the country has a campaign
        CountryCampaign campaign = countryCampaignService.getCampaign(countryId);
        if (campaign == null) {
            campaign = countryCampaignService.initializeCampaign(country);
        }

        // Create the military record
        PlayerMilitaryRecord record = new PlayerMilitaryRecord(
                playerId,
                country.getName(),
                country.getIsoCode(),
                branch,
                campaign
        );

        // Assign to a unit based on branch
        ThirdReichGroundUnit unit = assignToUnit(record, campaign);
        record.setAssignedUnit(unit);

        records.put(playerId, record);
        log.info("Player {} enlisted in {} {} as {}",
                playerId, country.getName(), branch, record.getRank());

        return record;
    }

    /**
     * Get a player's military record.
     *
     * @param playerId the player ID
     * @return the military record, or null if not enlisted
     */
    public PlayerMilitaryRecord getRecord(String playerId) {
        return records.get(playerId);
    }

    /**
     * Get all enlisted players for a country.
     *
     * @param countryId the country ID
     * @return list of military records
     */
    public List<PlayerMilitaryRecord> getPlayersByCountry(Long countryId) {
        return records.values().stream()
                .filter(r -> r.getCountryId().equals(countryId))
                .toList();
    }

    /**
     * Discharge a player from military service.
     *
     * @param playerId the player ID
     * @return the discharged record, or null if not enlisted
     */
    public PlayerMilitaryRecord discharge(String playerId) {
        PlayerMilitaryRecord record = records.remove(playerId);
        if (record != null) {
            log.info("Player {} discharged from {} {}",
                    playerId, record.getCountryName(), record.getBranch());
        }
        return record;
    }

    /**
     * Promote a player to the next rank.
     *
     * @param playerId the player ID
     * @return the updated record, or null if not enlisted
     */
    public PlayerMilitaryRecord promote(String playerId) {
        PlayerMilitaryRecord record = records.get(playerId);
        if (record == null) {
            return null;
        }
        record.promote();
        log.info("Player {} promoted to {}", playerId, record.getRank());
        return record;
    }

    /**
     * Assign a player to a military unit based on their branch.
     */
    private ThirdReichGroundUnit assignToUnit(PlayerMilitaryRecord record, CountryCampaign campaign) {
        MilitaryBranch branch = record.getBranch();

        // Find an appropriate unit type for the branch
        ThirdReichUnitType unitType;
        switch (branch) {
            case ARMY:
                unitType = ThirdReichUnitType.INFANTRY;
                break;
            case NAVY:
                unitType = ThirdReichUnitType.MECHANIZED; // Proxy for naval
                break;
            case AIR:
                unitType = ThirdReichUnitType.AIR_SUPPORT;
                break;
            case SPACE_NAVY:
                unitType = ThirdReichUnitType.MECH; // Proxy for space
                break;
            default:
                unitType = ThirdReichUnitType.INFANTRY;
        }

        // Find an existing unit of this type
        Optional<ThirdReichGroundUnit> existingUnit = campaign.getArmyUnits().stream()
                .filter(u -> u.getUnitType() == unitType)
                .filter(ThirdReichGroundUnit::isCombatEffective)
                .findFirst();

        if (existingUnit.isPresent()) {
            return existingUnit.get();
        }

        // Create a new player unit
        String unitId = record.getPlayerId() + "-" + unitType.name() + "-" +
                UUID.randomUUID().toString().substring(0, 8);
        ThirdReichGroundUnit unit = new ThirdReichGroundUnit(
                unitId,
                record.getPlayerId() + "'s Unit",
                unitType
        );
        unit.setStrength(100);
        unit.setMaxStrength(100);
        unit.setMorale(100);
        unit.setSourceGame("Earth2026-Player");

        // Add to campaign
        campaign.getArmyUnits().add(unit);

        return unit;
    }

    /**
     * Military branch enumeration.
     */
    public enum MilitaryBranch {
        /** Ground forces - infantry, tanks, artillery */
        ARMY("Army"),
        /** Naval forces - ships, submarines */
        NAVY("Navy"),
        /** Air forces - fighters, bombers */
        AIR("Air Force"),
        /** Space forces - starships, space stations */
        SPACE_NAVY("Space Navy");

        private final String displayName;

        MilitaryBranch(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Military rank progression.
     */
    public enum MilitaryRank {
        PRIVATE(1, "Private"),
        CORPORAL(2, "Corporal"),
        SERGEANT(3, "Sergeant"),
        LIEUTENANT(4, "Lieutenant"),
        CAPTAIN(5, "Captain"),
        MAJOR(6, "Major"),
        COLONEL(7, "Colonel"),
        GENERAL(8, "General");

        private final int level;
        private final String displayName;

        MilitaryRank(int level, String displayName) {
            this.level = level;
            this.displayName = displayName;
        }

        public int getLevel() {
            return level;
        }

        public String getDisplayName() {
            return displayName;
        }

        public MilitaryRank nextRank() {
            MilitaryRank[] values = values();
            int next = level + 1;
            for (MilitaryRank rank : values) {
                if (rank.level == next) {
                    return rank;
                }
            }
            return this; // Already at highest rank
        }
    }

    /**
     * Player's military service record.
     */
    public static class PlayerMilitaryRecord {
        private final String playerId;
        private final String countryName;
        private final Long countryId;
        private final String isoCode;
        private final MilitaryBranch branch;
        private final long enlistmentDate;
        private final CountryCampaign campaign;

        private MilitaryRank rank;
        private ThirdReichGroundUnit assignedUnit;
        private int battlesParticipated;
        private int kills;
        private int medals;
        private boolean active;

        PlayerMilitaryRecord(String playerId, String countryName, String isoCode,
                             MilitaryBranch branch, CountryCampaign campaign) {
            this.playerId = playerId;
            this.countryName = countryName;
            this.isoCode = isoCode;
            this.branch = branch;
            this.campaign = campaign;
            this.countryId = campaign.getCountryId();
            this.enlistmentDate = System.currentTimeMillis();
            this.rank = MilitaryRank.PRIVATE;
            this.active = true;
            this.battlesParticipated = 0;
            this.kills = 0;
            this.medals = 0;
        }

        public String getPlayerId() { return playerId; }
        public String getCountryName() { return countryName; }
        public Long getCountryId() { return countryId; }
        public String getIsoCode() { return isoCode; }
        public MilitaryBranch getBranch() { return branch; }
        public long getEnlistmentDate() { return enlistmentDate; }
        public CountryCampaign getCampaign() { return campaign; }
        public MilitaryRank getRank() { return rank; }
        public ThirdReichGroundUnit getAssignedUnit() { return assignedUnit; }
        public void setAssignedUnit(ThirdReichGroundUnit assignedUnit) { this.assignedUnit = assignedUnit; }
        public int getBattlesParticipated() { return battlesParticipated; }
        public int getKills() { return kills; }
        public int getMedals() { return medals; }
        public boolean isActive() { return active; }

        public void promote() {
            this.rank = rank.nextRank();
        }

        public void recordBattleParticipation() {
            this.battlesParticipated++;
        }

        public void addKill() {
            this.kills++;
        }

        public void awardMedal() {
            this.medals++;
        }

        public void deactivate() {
            this.active = false;
        }

        @Override
        public String toString() {
            return String.format("PlayerMilitary{%s, %s, %s, %s, battles=%d, kills=%d}",
                    playerId, rank.getDisplayName(), countryName, branch,
                    battlesParticipated, kills);
        }
    }
}
