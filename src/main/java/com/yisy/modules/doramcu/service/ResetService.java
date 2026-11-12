package com.yisy.modules.doramcu.service;

import com.yisy.modules.doramcu.eve.model.AiCharacter;
import com.yisy.modules.doramcu.eve.model.EveCorporation;
import com.yisy.modules.doramcu.eve.model.Treasury;
import com.yisy.modules.doramcu.hex.GalaxyFactory;
import com.yisy.modules.doramcu.hex.GalaxyGenerationConfig;
import com.yisy.modules.doramcu.hex.Empire;
import com.yisy.modules.doramcu.model.Player;
import com.yisy.modules.doramcu.model.PromoCode;
import com.yisy.modules.doramcu.model.User;
import com.yisy.modules.doramcu.repository.AiCharacterRepository;
import com.yisy.modules.doramcu.repository.EveCorporationRepository;
import com.yisy.modules.doramcu.repository.PlayerRepository;
import com.yisy.modules.doramcu.repository.PromoCodeRepository;
import com.yisy.modules.doramcu.repository.TreasuryRepository;
import com.yisy.modules.doramcu.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
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
 *   <li>Repopulates the seed data (Sif Younger and Butch Younger users and players)</li>
 *   <li>Recreates the Younger Industries corporation and treasury entries</li>
 *   <li>Recreates the Sif Younger AI character</li>
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
        entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();

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
                "game_sessions_persisted", "treasuries", "eve_corporations",
                "ai_characters", "devices", "players"
            };
            for (String table : gameplayTables) {
                truncateTable(table, tablesCleaned, errors);
            }

            // Galaxy data (leaf entities first)
            String[] galaxyTables = {
                "wormholes", "space_stations", "comets", "black_holes",
                "nebulas", "gas_giants", "asteroid_belts", "asteroids", "moons", "planets",
                "stars", "solar_systems", "sub_sectors", "sectors", "galaxies"
            };
            for (String table : galaxyTables) {
                truncateTable(table, tablesCleaned, errors);
            }

            // Users table — clear all users so seed data is recreated
            truncateTable("users", tablesCleaned, errors);

            log.info("Phase 1 complete: {} tables truncated", tablesCleaned.size());

            // Phase 2 — Repopulate seed data
            repopulateSeedData(entitiesRepopulated, errors);
            log.info("Phase 2 complete: {} entities repopulated", entitiesRepopulated.size());

            // Phase 3 — Reset the RESET promo code usage count
            resetPromoCodeUsage(errors);
            log.info("Phase 3 complete: RESET promo code usage reset");

            String message = String.format("Reset complete. %d tables cleaned, %d entities repopulated.",
                tablesCleaned.size(), entitiesRepopulated.size());
            log.info("=== FULL GAME DATA RESET COMPLETED: {} ===", message);

            return new ResetResult(true, message, tablesCleaned, entitiesRepopulated, errors);

        } finally {
            entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();
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
            // Create Sif Younger user
            User sifUser = new User();
            sifUser.setUsername("Sif Younger");
            sifUser.setPassword(passwordEncoder.encode("Lazarus2026"));
            sifUser.setFirstName("Sif");
            sifUser.setLastName("Younger");
            sifUser.setPhoneNumber("6196596073");
            sifUser.setGalaxyName(GalaxyFactory.GAME_GALAXY_NAME);
            userRepository.save(sifUser);
            repopulated.add("User: Sif Younger (admin)");
            log.info("Repopulated user: Sif Younger");

            // Create Butch Younger user
            User butchUser = new User();
            butchUser.setUsername("Butch Younger");
            butchUser.setPassword(passwordEncoder.encode("3214"));
            butchUser.setFirstName("Butch");
            butchUser.setLastName("Younger");
            butchUser.setGalaxyName(GalaxyFactory.GAME_GALAXY_NAME);
            userRepository.save(butchUser);
            repopulated.add("User: Butch Younger (player)");
            log.info("Repopulated user: Butch Younger");

            // Create game users
            userRepository.save(new User(null, "player1", passwordEncoder.encode("123456")));
            userRepository.save(new User(null, "player2", passwordEncoder.encode("234567")));
            userRepository.save(new User(null, "player3", passwordEncoder.encode("345678")));
            repopulated.add("Users: player1, player2, player3");
            log.info("Repopulated game users: player1, player2, player3");

            // Create Sif Younger player (admin)
            Player sifPlayer = new Player();
            sifPlayer.setEmail("YoungerIndustries@yahoo.com");
            sifPlayer.setLoginPin("L4z4r");
            sifPlayer.setEmpireName("Interstellar Alliance");
            sifPlayer.setGovernmentType(Player.GovernmentType.CORPORATE);
            sifPlayer.setEthos(Player.Ethos.LIBERTARIAN);
            sifPlayer.setAdmin(true);
            sifPlayer.setCorporateRole("CEO");
            sifPlayer.setDisplayName("Sif Younger");
            sifPlayer.setUser(sifUser);
            playerRepository.save(sifPlayer);
            repopulated.add("Player: Sif Younger (admin, CEO, Interstellar Alliance)");
            log.info("Repopulated player: Sif Younger (admin)");

            // Create Butch Younger player
            Player butchPlayer = new Player();
            butchPlayer.setEmail("butch.younger@empire.com");
            butchPlayer.setLoginPin("3214");
            butchPlayer.setEmpireName("Interstellar Alliance");
            butchPlayer.setGovernmentType(Player.GovernmentType.CORPORATE);
            butchPlayer.setEthos(Player.Ethos.LIBERTARIAN);
            butchPlayer.setAdmin(false);
            butchPlayer.setCorporateRole("Vice President");
            butchPlayer.setDisplayName("Butch Younger");
            butchPlayer.setUser(butchUser);
            playerRepository.save(butchPlayer);
            repopulated.add("Player: Butch Younger (VP, Interstellar Alliance)");
            log.info("Repopulated player: Butch Younger");

            // Create additional players for multiplayer
            Player player1 = new Player();
            player1.setEmail("player1@empire.com");
            player1.setLoginPin("123456");
            player1.setEmpireName("Terran Republic");
            player1.setGovernmentType(Player.GovernmentType.DEMOCRACY);
            player1.setEthos(Player.Ethos.MATERIALIST);
            playerRepository.save(player1);
            repopulated.add("Player: Terran Republic");

            Player player2 = new Player();
            player2.setEmail("player2@empire.com");
            player2.setLoginPin("234567");
            player2.setEmpireName("Solar Imperium");
            player2.setGovernmentType(Player.GovernmentType.MONARCHY);
            player2.setEthos(Player.Ethos.MILITARIST);
            playerRepository.save(player2);
            repopulated.add("Player: Solar Imperium");

            Player player3 = new Player();
            player3.setEmail("player3@empire.com");
            player3.setLoginPin("345678");
            player3.setEmpireName("United Colonies");
            player3.setGovernmentType(Player.GovernmentType.CONFEDERATION);
            player3.setEthos(Player.Ethos.LIBERTARIAN);
            playerRepository.save(player3);
            repopulated.add("Player: United Colonies");

            Player player4 = new Player();
            player4.setEmail("player4@empire.com");
            player4.setLoginPin("456789");
            player4.setEmpireName("Stellar Hegemony");
            player4.setGovernmentType(Player.GovernmentType.OLIGARCHY);
            player4.setEthos(Player.Ethos.EQUALIST);
            playerRepository.save(player4);
            repopulated.add("Player: Stellar Hegemony");

            Player player5 = new Player();
            player5.setEmail("player5@empire.com");
            player5.setLoginPin("567890");
            player5.setEmpireName("Divine Theocracy");
            player5.setGovernmentType(Player.GovernmentType.THEOCRACY);
            player5.setEthos(Player.Ethos.SPIRITUALIST);
            playerRepository.save(player5);
            repopulated.add("Player: Divine Theocracy");

            Player player6 = new Player();
            player6.setEmail("player6@empire.com");
            player6.setLoginPin("678901");
            player6.setEmpireName("Corporate Syndicate");
            player6.setGovernmentType(Player.GovernmentType.CORPORATE);
            player6.setEthos(Player.Ethos.PACIFIST);
            playerRepository.save(player6);
            repopulated.add("Player: Corporate Syndicate");

            log.info("Repopulated 8 players total");

            // Create Younger Industries corporation
            EveCorporation youngerIndustries = new EveCorporation();
            youngerIndustries.setName("Younger Industries");
            youngerIndustries.setTicker("YISY");
            youngerIndustries.setCeoName("Sif Younger");
            youngerIndustries.setDescription("Interstellar trading and manufacturing corporation. CEO: Sif Younger, VP: Butch Younger.");
            eveCorporationRepository.save(youngerIndustries);
            repopulated.add("Corporation: Younger Industries (YISY)");
            log.info("Repopulated corporation: Younger Industries");

            // Create treasury for Sif Younger
            Treasury sifTreasury = new Treasury();
            sifTreasury.setResourceType("SILVER");
            sifTreasury.setQuantityOunces(new BigDecimal("13.00"));
            sifTreasury.setPricePerOunce(new BigDecimal("40.00"));
            sifTreasury.setOwnerId("Sif Younger");
            sifTreasury.setOwnerType("PLAYER");
            treasuryRepository.save(sifTreasury);
            repopulated.add("Treasury: Sif Younger (13 oz silver)");
            log.info("Repopulated treasury: Sif Younger");

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

            // Create AI character for Sif Younger
            AiCharacter sifAi = new AiCharacter();
            sifAi.setName("Sif Younger");
            sifAi.setRole("Leader");
            sifAi.setEmpireName("Interstellar Alliance");
            sifAi.setLinkedPlayerId("Sif Younger");
            sifAi.setActive(true);
            sifAi.setCanPromoteToCoServer(true);
            sifAi.setDescription("AI character representing Sif Younger, CEO of Younger Industries and Leader of the Interstellar Alliance empire.");
            aiCharacterRepository.save(sifAi);
            repopulated.add("AI Character: Sif Younger (Leader)");
            log.info("Repopulated AI character: Sif Younger");

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
                log.warn("RESET promo code not found — skipping usage reset");
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
        entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();

        try {
            String[] galaxyTables = {
                "wormholes", "space_stations", "comets", "black_holes",
                "nebulas", "gas_giants", "asteroid_belts", "asteroids", "moons", "planets",
                "stars", "solar_systems", "sub_sectors", "sectors", "galaxies"
            };
            for (String table : galaxyTables) {
                truncateTable(table, tablesCleaned, errors);
            }

            log.info("Phase 1 complete: {} galaxy tables truncated", tablesCleaned.size());

            // Phase 2 — Regenerate galaxy using GalaxyCreationService
            regenerateGalaxy(entitiesRepopulated, errors);
            log.info("Phase 2 complete: galaxy regenerated, {} entities repopulated", entitiesRepopulated.size());

            // Phase 3 — Reset the GALAXY_RESET promo code usage count
            resetGalaxyResetPromoCodeUsage(errors);
            log.info("Phase 3 complete: GALAXY_RESET promo code usage reset");

            String message = String.format("Galaxy reset complete. %d tables cleaned, %d entities repopulated. Players and characters retained.",
                tablesCleaned.size(), entitiesRepopulated.size());
            log.info("=== GALAXY-ONLY RESET COMPLETED: {} ===", message);

            return new ResetResult(true, message, tablesCleaned, entitiesRepopulated, errors);

        } finally {
            entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();
        }
    }

    /**
     * Regenerates the galaxy using GalaxyCreationService.
     * All galaxies are now created dynamically — no hardcoded Milky Way or Void galaxies.
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
                log.warn("No players found — generating galaxy without empire assignments");
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
                log.warn("GALAXY_RESET promo code not found — skipping usage reset");
            }
        } catch (Exception e) {
            String err = "Failed to reset GALAXY_RESET promo code usage: " + e.getMessage();
            errors.add(err);
            log.error("Failed to reset GALAXY_RESET promo code usage", e);
        }
    }
}
