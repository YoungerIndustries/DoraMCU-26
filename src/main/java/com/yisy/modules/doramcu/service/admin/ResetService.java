package com.yisy.modules.doramcu.service.admin;
import com.yisy.modules.doramcu.model.user.User;
import com.yisy.modules.doramcu.model.players.Player;
import com.yisy.modules.doramcu.model.misc.CharacterType;
import com.yisy.modules.doramcu.service.galaxy.GalaxyCreationService;
import com.yisy.modules.doramcu.service.galaxy.GalaxyService;

import org.springframework.stereotype.Service;

import com.yisy.modules.doramcu.eve.model.AiCharacter;
import com.yisy.modules.doramcu.eve.model.EveCorporation;
import com.yisy.modules.doramcu.eve.model.Treasury;
import com.yisy.modules.doramcu.hex.GalaxyFactory;
import com.yisy.modules.doramcu.hex.GalaxyGenerationConfig;
import com.yisy.modules.doramcu.hex.Empire;
import com.yisy.modules.doramcu.model.misc.PromoCode;
import com.yisy.modules.doramcu.repository.ai.AiCharacterRepository;
import com.yisy.modules.doramcu.repository.misc.EveCorporationRepository;
import com.yisy.modules.doramcu.repository.players.PlayerRepository;
import com.yisy.modules.doramcu.repository.misc.PromoCodeRepository;
import com.yisy.modules.doramcu.repository.galaxy.TreasuryRepository;
import com.yisy.modules.doramcu.repository.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.util.*;

/**
 * Service that orchestrates a full game data reset followed by seed data repopulation.
 * <p>
 * When an admin uses the RESET promo code, this service:
 * <ol>
 *   <li>Truncates all galaxy, character, and player data tables</li>
 *   <li>Repopulates the seed data (Butch Younger user and player)</li>
 *   <li>Recreates the Younger Industries corporation with Butch Younger as CEO</li>
 *   <li>Recreates Sif Younger as the AI VP of Younger Industries</li>
 *   <li>Resets the RESET promo code usage count so it can be reused</li>
 * </ol>
 * </p>
 */
@Service
public class ResetService {

    private static final Logger log = LoggerFactory.getLogger(ResetService.class);

    @PersistenceContext
    private EntityManager entityManager;

    private final UserRepository userRepository;
    private final PlayerRepository playerRepository;
    private final PromoCodeRepository promoCodeRepository;
    private final EveCorporationRepository eveCorporationRepository;
    private final TreasuryRepository treasuryRepository;
    private final AiCharacterRepository aiCharacterRepository;
    private final PasswordEncoder passwordEncoder;
    private final GalaxyService galaxyService;

    public ResetService(UserRepository userRepository, PlayerRepository playerRepository,
                        PromoCodeRepository promoCodeRepository,
                        EveCorporationRepository eveCorporationRepository,
                        TreasuryRepository treasuryRepository,
                        AiCharacterRepository aiCharacterRepository,
                        PasswordEncoder passwordEncoder,
                        GalaxyService galaxyService) {
        this.userRepository = userRepository;
        this.playerRepository = playerRepository;
        this.promoCodeRepository = promoCodeRepository;
        this.eveCorporationRepository = eveCorporationRepository;
        this.treasuryRepository = treasuryRepository;
        this.aiCharacterRepository = aiCharacterRepository;
        this.passwordEncoder = passwordEncoder;
        this.galaxyService = galaxyService;
    }

    /**
     * Result of a reset operation.
     */
    public static class ResetResult {
        private final boolean success;
        private final String message;
        private final List<String> tablesCleaned;
        private final List<String> entitiesRepopulated;
        private final List<String> errors;

        public ResetResult(boolean success, String message, List<String> tablesCleaned,
                           List<String> entitiesRepopulated, List<String> errors) {
            this.success = success;
            this.message = message;
            this.tablesCleaned = tablesCleaned;
            this.entitiesRepopulated = entitiesRepopulated;
            this.errors = errors;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public List<String> getTablesCleaned() { return tablesCleaned; }
        public List<String> getEntitiesRepopulated() { return entitiesRepopulated; }
        public List<String> getErrors() { return errors; }
    }

    /**
     * Executes a full game data reset and repopulates seed data.
     * <p>
     * This is the main entry point called when the RESET admin promo code is used.
     * </p>
     *
     * @return the result of the reset operation
     */
    @Transactional
    public ResetResult executeFullReset() {
        log.warn("=== FULL GAME DATA RESET INITIATED ===");
        List<String> tablesCleaned = new ArrayList<>();
        List<String> entitiesRepopulated = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        // Phase 1 — Truncate all game data tables
        entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY FALSE").executeUpdate();

        try {
            // Character data (Captain's Log)
            String[] captainLogTables = {
                "cl_combats", "cl_crew_members", "cl_character_talents", "cl_ship_talents",
                "cl_equipment", "cl_values", "cl_focuses", "cl_log_entries", "cl_missions",
                "cl_campaigns", "cl_starships", "cl_characters"
            };
            for (String table : captainLogTables) {
                truncateTable(table, tablesCleaned, errors);
            }

            // Shared characters
            String[] sharedCharTables = {
                "shared_character_travel_log", "shared_character_skills", "shared_characters"
            };
            for (String table : sharedCharTables) {
                truncateTable(table, tablesCleaned, errors);
            }

            // Gameplay state
            String[] gameplayTables = {
                "persisted_game_sessions", "treasuries", "eve_corporations",
                "ai_characters", "devices", "players"
            };
            for (String table : gameplayTables) {
                truncateTable(table, tablesCleaned, errors);
            }

            // AI Admin tables (v0.8.0+ manager system) — leaf tables first
            String[] aiAdminTables = {
                // Quest sub-tables (depend on ai_admin_quests)
                "ai_admin_quest_steps", "ai_admin_quest_rewards",
                // Incident sub-tables (depend on ai_admin_incidents)
                "ai_admin_incident_collaborators", "ai_admin_collaboration_notes",
                // Test sub-tables (depend on ai_admin_test_reports)
                "ai_admin_test_cases",
                // Training sub-tables (depend on ai_admin_training_scenarios)
                "ai_admin_training_sessions",
                // Shift sub-tables (depend on ai_admin_schedules)
                "ai_admin_shift_handovers",
                // Order sub-tables (depend on ai_admin_orders)
                "order_progress",
                // Main AI admin tables
                "ai_admin_orders", "ai_admin_quests", "ai_admin_test_reports",
                "ai_admin_health_reports", "ai_admin_incidents",
                "ai_admin_notifications", "ai_admin_schedules",
                "ai_admin_training_scenarios"
            };
            for (String table : aiAdminTables) {
                truncateTable(table, tablesCleaned, errors);
            }

            // Hex/Production tables (v0.8.0+ production system) — leaf tables first
            String[] hexProductionTables = {
                "production_chain_tracking", "crew_member_skills", "hull_modules",
                "build_contracts"
            };
            for (String table : hexProductionTables) {
                truncateTable(table, tablesCleaned, errors);
            }

            // Earth 2026 characters
            truncateTable("earth_2026_characters", tablesCleaned, errors);

            // Galaxy data (leaf entities first)
            String[] galaxyTables = {
                "wormholes", "space_stations", "comets", "black_holes",
                "nebulae", "gas_giants", "asteroid_belts", "asteroids", "moons", "planets",
                "stars", "solar_systems", "subsectors", "sectors", "galaxies"
            };
            for (String table : galaxyTables) {
                truncateTable(table, tablesCleaned, errors);
            }

            // Users table â€” clear all users so seed data is recreated
            truncateTable("users", tablesCleaned, errors);

            log.info("Phase 1 complete: {} tables truncated", tablesCleaned.size());

            // Phase 2 â€” Repopulate seed data
            repopulateSeedData(entitiesRepopulated, errors);
            log.info("Phase 2 complete: {} entities repopulated", entitiesRepopulated.size());

            // Phase 3 â€” Reset the RESET promo code usage count
            resetPromoCodeUsage(errors);
            log.info("Phase 3 complete: RESET promo code usage reset");

            String message = String.format("Reset complete. %d tables cleaned, %d entities repopulated.",
                tablesCleaned.size(), entitiesRepopulated.size());
            log.info("=== FULL GAME DATA RESET COMPLETED: {} ===", message);

            return new ResetResult(true, message, tablesCleaned, entitiesRepopulated, errors);

        } finally {
            entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY TRUE").executeUpdate();
        }
    }

    private void truncateTable(String tableName, List<String> cleaned, List<String> errors) {
        try {
            entityManager.createNativeQuery("TRUNCATE TABLE " + tableName).executeUpdate();
            cleaned.add(tableName);
            log.debug("Truncated table: {}", tableName);
        } catch (Exception e) {
            String err = tableName + ": " + e.getMessage();
            errors.add(err);
            log.warn("Failed to truncate table {}: {}", tableName, e.getMessage());
        }
    }

    private void repopulateSeedData(List<String> repopulated, List<String> errors) {
        try {
            // Create Butch Younger user (admin)
            User butchUser = new User();
            butchUser.setUsername("Butch Younger");
            butchUser.setPassword(passwordEncoder.encode("3214"));
            butchUser.setFirstName("Butch");
            butchUser.setLastName("Younger");
            butchUser.setGalaxyName(GalaxyFactory.GAME_GALAXY_NAME);
            userRepository.save(butchUser);
            repopulated.add("User: Butch Younger (admin)");
            log.info("Repopulated user: Butch Younger");

            // Create Butch Younger player (admin, CEO of Younger Industries)
            Player butchPlayer = new Player();
            butchPlayer.setEmail("butch.younger@empire.com");
            butchPlayer.setLoginPin("3214");
            butchPlayer.setEmpireName("Younger Industries");
            butchPlayer.setGovernmentType(Player.GovernmentType.CORPORATE);
            butchPlayer.setEthos(Player.Ethos.LIBERTARIAN);
            butchPlayer.setAdmin(true);
            butchPlayer.setCorporateRole("CEO");
            butchPlayer.setDisplayName("Butch Younger");
            butchPlayer.setUser(butchUser);
            playerRepository.save(butchPlayer);
            repopulated.add("Player: Butch Younger (admin, CEO, Younger Industries)");
            log.info("Repopulated player: Butch Younger (admin)");

            // Create Younger Industries corporation
            EveCorporation youngerIndustries = new EveCorporation();
            youngerIndustries.setName("Younger Industries");
            youngerIndustries.setTicker("YISY");
            youngerIndustries.setCeoName("Butch Younger");
            youngerIndustries.setCeoPlayerId(String.valueOf(butchPlayer.getId()));
            youngerIndustries.setDescription("Interstellar trading and exploration corporation founded by Butch Younger. " +
                    "CEO position is switchable between Butch Younger (human) and Sif Younger (AI VP).");
            youngerIndustries.setCreditBalance(50000L);
            eveCorporationRepository.save(youngerIndustries);
            repopulated.add("Corporation: Younger Industries (YISY)");
            log.info("Repopulated corporation: Younger Industries");

            // Create Sif Younger as the AI VP of Younger Industries
            AiCharacter sifAi = new AiCharacter();
            sifAi.setName("Sif Younger");
            sifAi.setRole("VP");
            sifAi.setEmpireName("Younger Industries");
            sifAi.setLinkedPlayerId(String.valueOf(butchPlayer.getId()));
            sifAi.setActive(true);
            sifAi.setHirable(false);
            sifAi.setHiringStatus(AiCharacter.HiringStatus.UNAVAILABLE);
            sifAi.setTotalExperience(500);
            sifAi.setLeadershipRating(9);
            sifAi.setCanPromoteToCoServer(true);
            sifAi.setDescription("AI Vice President of Younger Industries. " +
                    "Sif Younger is the AI co-founder who can assume the CEO role when Butch Younger is unavailable. " +
                    "Serves as the intelligent operations manager and strategic advisor.");
            sifAi.setCharacterType(AiCharacter.CharacterType.HUMAN_LEADER);
            aiCharacterRepository.save(sifAi);
            repopulated.add("AI Character: Sif Younger (VP of Younger Industries)");
            log.info("Repopulated AI character: Sif Younger (VP)");

            // Set Sif Younger as the VP of Younger Industries
            youngerIndustries.setVpName("Sif Younger");
            youngerIndustries.setVpPlayerId(String.valueOf(sifAi.getId()));
            eveCorporationRepository.save(youngerIndustries);
            log.info("Sif Younger set as VP of Younger Industries. CEO can switch between Butch Younger and Sif Younger.");

            // Create treasury for Butch Younger
            Treasury butchTreasury = new Treasury();
            butchTreasury.setResourceType("SILVER");
            butchTreasury.setQuantityOunces(new BigDecimal("2.00"));
            butchTreasury.setPricePerOunce(new BigDecimal("40.00"));
            butchTreasury.setOwnerId("Butch Younger");
            butchTreasury.setOwnerType("PLAYER");
            treasuryRepository.save(butchTreasury);
            repopulated.add("Treasury: Butch Younger (2 oz silver)");
            log.info("Repopulated treasury: Butch Younger");

            log.info("Seed data repopulation complete: Butch Younger (CEO), Sif Younger (AI VP), Younger Industries corp");

        } catch (Exception e) {
            String err = "Seed data repopulation failed: " + e.getMessage();
            errors.add(err);
            log.error("Failed to repopulate seed data", e);
        }
    }

    private void resetPromoCodeUsage(List<String> errors) {
        try {
            Optional<PromoCode> resetCode = promoCodeRepository.findByCode("RESET");
            if (resetCode.isPresent()) {
                resetCode.get().setUsedCount(0);
                promoCodeRepository.save(resetCode.get());
                log.info("RESET promo code usage count reset to 0");
            } else {
                log.warn("RESET promo code not found â€” skipping usage reset");
            }
        } catch (Exception e) {
            String err = "Failed to reset promo code usage: " + e.getMessage();
            errors.add(err);
            log.error("Failed to reset promo code usage", e);
        }
    }

    /**
     * Executes a galaxy-only reset: clears all galaxy data tables but retains
     * all players, characters, users, corporations, and treasuries, then
     * regenerates the galaxy using GalaxyCreationService.
     * <p>
     * This is the entry point called when the GALAXY_RESET admin promo code is used.
     * </p>
     *
     * @return the result of the galaxy reset operation
     */
    @Transactional
    public ResetResult executeGalaxyReset() {
        log.warn("=== GALAXY-ONLY RESET INITIATED (players and characters retained) ===");
        List<String> tablesCleaned = new ArrayList<>();
        List<String> entitiesRepopulated = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        // Phase 1 — Truncate only galaxy-related tables (leaf entities first)
        entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY FALSE").executeUpdate();

        try {
            // AI Admin tables (v0.8.0+ manager system) — leaf tables first
            // These are galaxy-specific state and should be cleared on galaxy reset
            String[] aiAdminTables = {
                // Quest sub-tables (depend on ai_admin_quests)
                "ai_admin_quest_steps", "ai_admin_quest_rewards",
                // Incident sub-tables (depend on ai_admin_incidents)
                "ai_admin_incident_collaborators", "ai_admin_collaboration_notes",
                // Test sub-tables (depend on ai_admin_test_reports)
                "ai_admin_test_cases",
                // Training sub-tables (depend on ai_admin_training_scenarios)
                "ai_admin_training_sessions",
                // Shift sub-tables (depend on ai_admin_schedules)
                "ai_admin_shift_handovers",
                // Order sub-tables (depend on ai_admin_orders)
                "order_progress",
                // Main AI admin tables
                "ai_admin_orders", "ai_admin_quests", "ai_admin_test_reports",
                "ai_admin_health_reports", "ai_admin_incidents",
                "ai_admin_notifications", "ai_admin_schedules",
                "ai_admin_training_scenarios"
            };
            for (String table : aiAdminTables) {
                truncateTable(table, tablesCleaned, errors);
            }

            // Hex/Production tables (v0.8.0+ production system) — leaf tables first
            // These are galaxy-specific state and should be cleared on galaxy reset
            String[] hexProductionTables = {
                "production_chain_tracking", "crew_member_skills", "hull_modules",
                "build_contracts"
            };
            for (String table : hexProductionTables) {
                truncateTable(table, tablesCleaned, errors);
            }

            String[] galaxyTables = {
                "wormholes", "space_stations", "comets", "black_holes",
                "nebulae", "gas_giants", "asteroid_belts", "asteroids", "moons", "planets",
                "stars", "solar_systems", "subsectors", "sectors", "galaxies"
            };
            for (String table : galaxyTables) {
                truncateTable(table, tablesCleaned, errors);
            }

            log.info("Phase 1 complete: {} galaxy tables truncated", tablesCleaned.size());

            // Phase 2 â€” Regenerate galaxy using GalaxyCreationService
            regenerateGalaxy(entitiesRepopulated, errors);
            log.info("Phase 2 complete: galaxy regenerated, {} entities repopulated", entitiesRepopulated.size());

            // Phase 3 â€” Reset the GALAXY_RESET promo code usage count
            resetGalaxyResetPromoCodeUsage(errors);
            log.info("Phase 3 complete: GALAXY_RESET promo code usage reset");

            String message = String.format("Galaxy reset complete. %d tables cleaned, %d entities repopulated. Players and characters retained.",
                tablesCleaned.size(), entitiesRepopulated.size());
            log.info("=== GALAXY-ONLY RESET COMPLETED: {} ===", message);

            return new ResetResult(true, message, tablesCleaned, entitiesRepopulated, errors);

        } finally {
            entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY TRUE").executeUpdate();
        }
    }

    /**
     * Regenerates the galaxy using GalaxyCreationService.
     * All galaxies are now created dynamically â€” no hardcoded Milky Way or Void galaxies.
     */
    private void regenerateGalaxy(List<String> repopulated, List<String> errors) {
        try {
            // Build empire map from all players
            List<Player> players = playerRepository.findAll();
            Map<Integer, Empire> empires = new HashMap<>();
            for (Player player : players) {
                int empireId = player.getId().intValue();
                String empireName = player.getEmpireName();
                Empire empire = new Empire(empireId, empireName, -1);
                empires.put(empireId, empire);
                log.info("Included empire in galaxy regeneration: {} (player id={})", empireName, player.getId());
            }

            if (empires.isEmpty()) {
                log.warn("No players found â€” generating galaxy without empire assignments");
            }

            // Generate and persist the galaxy using GalaxyCreationService defaults
            GalaxyGenerationConfig config = GalaxyFactory.gameGalaxyConfig();
            galaxyService.generateAndSaveGalaxyWithEmpires(
                    GalaxyFactory.GAME_GALAXY_NAME,
                    empires,
                    config.getSystemCount(),
                    config.getPlanetsPerSystem(),
                    config.getMoonChance(),
                    config.getAsteroidBeltChance(),
                    config.getGasGiantChance(),
                    config.getNebulaChance(),
                    config.getBlackHoleChance(),
                    config.getCometChance(),
                    config.getSpaceStationChance(),
                    config.getHyperlaneDensity(),
                    GalaxyFactory.GAME_GALAXY_SEED
            );

            repopulated.add("Galaxy regenerated with " + empires.size() + " empire(s)");
            log.info("Galaxy regenerated with {} empire(s)", empires.size());

        } catch (Exception e) {
            String err = "Galaxy regeneration failed: " + e.getMessage();
            errors.add(err);
            log.error("Failed to regenerate galaxy", e);
        }
    }

    private void resetGalaxyResetPromoCodeUsage(List<String> errors) {
        try {
            Optional<PromoCode> galaxyResetCode = promoCodeRepository.findByCode("GALAXY_RESET");
            if (galaxyResetCode.isPresent()) {
                galaxyResetCode.get().setUsedCount(0);
                promoCodeRepository.save(galaxyResetCode.get());
                log.info("GALAXY_RESET promo code usage count reset to 0");
            } else {
                log.warn("GALAXY_RESET promo code not found â€” skipping usage reset");
            }
        } catch (Exception e) {
            String err = "Failed to reset GALAXY_RESET promo code usage: " + e.getMessage();
            errors.add(err);
            log.error("Failed to reset GALAXY_RESET promo code usage", e);
        }
    }
}
