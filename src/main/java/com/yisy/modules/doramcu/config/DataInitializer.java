package com.yisy.modules.doramcu.config;

import com.yisy.modules.doramcu.corporation.model.CorporateContract;
import com.yisy.modules.doramcu.corporation.model.CorporateHiring;
import com.yisy.modules.doramcu.corporation.model.CorporatePosition;
import com.yisy.modules.doramcu.corporation.repository.CorporateContractRepository;
import com.yisy.modules.doramcu.corporation.repository.CorporateHiringRepository;
import com.yisy.modules.doramcu.corporation.repository.CorporationRelationshipRepository;
import com.yisy.modules.doramcu.earth2026.model.Earth2026Character;
import com.yisy.modules.doramcu.earth2026.repository.Earth2026CharacterRepository;
import com.yisy.modules.doramcu.eve.model.AiCharacter;
import com.yisy.modules.doramcu.eve.model.EveCorporation;
import com.yisy.modules.doramcu.hex.Empire;
import com.yisy.modules.doramcu.hex.GalaxyFactory;
import com.yisy.modules.doramcu.hex.GalaxyGenerationProgress;
import com.yisy.modules.doramcu.service.galaxy.GalaxyCreationService;
import com.yisy.modules.doramcu.service.galaxy.GalaxyProgressService;
import com.yisy.modules.doramcu.service.galaxy.GalaxyService;
import com.yisy.modules.doramcu.service.session.GameSessionService;
import com.yisy.modules.doramcu.model.players.Player;
import com.yisy.modules.doramcu.model.user.User;
import com.yisy.modules.doramcu.repository.ai.AiCharacterRepository;
import com.yisy.modules.doramcu.model.misc.Device;
import com.yisy.modules.doramcu.model.misc.CareerLevel;
import com.yisy.modules.doramcu.repository.misc.DeviceRepository;
import com.yisy.modules.doramcu.repository.misc.EveCorporationRepository;
import com.yisy.modules.doramcu.model.misc.PromoCode;
import com.yisy.modules.doramcu.repository.misc.PromoCodeRepository;
import com.yisy.modules.doramcu.repository.players.PlayerRepository;
import com.yisy.modules.doramcu.repository.user.UserRepository;
import com.yisy.modules.doramcu.gamecore.CharacterService;
import com.yisy.modules.doramcu.traveller.AbilityScore;
import com.yisy.modules.doramcu.traveller.TravellerCharacter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.*;

@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    @ConditionalOnProperty(name = "app.seed-data", havingValue = "true", matchIfMissing = false)
    CommandLineRunner initBasicData(DeviceRepository deviceRepository, UserRepository userRepository,
                                    PlayerRepository playerRepository, PasswordEncoder passwordEncoder,
                                    PromoCodeRepository promoCodeRepository,
                                    Earth2026CharacterRepository earth2026CharacterRepository,
                                    EveCorporationRepository eveCorporationRepository,
                                    AiCharacterRepository aiCharacterRepository,
                                    CorporateContractRepository corporateContractRepository,
                                    CorporateHiringRepository corporateHiringRepository,
                                    CorporationRelationshipRepository corporationRelationshipRepository,
                                    CharacterService travellerCharacterService) {
        return args -> {
            log.warn("SEED DATA IS ENABLED - This should only be used in development environments!");
            log.info("Initializing database with seed data...");
            // Only seed data if the database is empty
            if (userRepository.count() == 0) {
                // Admin user: Butch Younger with admin rights
                userRepository.save(new User(null, "Butch Younger", passwordEncoder.encode("3214")));
                log.info("1 default user created (Butch Younger)");
            }
            if (deviceRepository.count() == 0) {
                deviceRepository.save(new Device(null, "Sensor-001", "active", "Building A"));
                deviceRepository.save(new Device(null, "Sensor-002", "inactive", "Building B"));
                deviceRepository.save(new Device(null, "Controller-001", "active", "Server Room"));
                deviceRepository.save(new Device(null, "Controller-002", "active", "Command Center"));
                deviceRepository.save(new Device(null, "Scanner-001", "active", "Orbital Station"));
                log.info("5 default devices created");
            }
            if (playerRepository.count() == 0) {
                // Admin player: Butch Younger with admin rights, linked to "Butch Younger" user
                Player butchPlayer = new Player();
                butchPlayer.setEmail("butch.younger@empire.com");
                butchPlayer.setLoginPin("3214");
                butchPlayer.setEmpireName("Younger Industries");
                butchPlayer.setGovernmentType(Player.GovernmentType.CORPORATE);
                butchPlayer.setEthos(Player.Ethos.LIBERTARIAN);
                butchPlayer.setAdmin(true);
                Optional<User> butchUserOpt = userRepository.findByUsername("Butch Younger");
                butchUserOpt.ifPresent(butchPlayer::setUser);
                playerRepository.save(butchPlayer);
                log.info("Admin player 'Butch Younger' created with PIN 3214, empire 'Younger Industries', corporate, libertarian");

                // Create Butch Younger as the first Earth 2026 character (linked to the Butch Younger player)
                // Butch is an Earth2026 character, NOT a Traveller character
                if (earth2026CharacterRepository.count() == 0) {
                    Earth2026Character butchCharacter = new Earth2026Character("Butch Younger", 35, "CEO");
                    butchCharacter.setPlayerId(butchPlayer.getId());
                    butchCharacter.setStrScore(12);
                    butchCharacter.setDexScore(10);
                    butchCharacter.setConScore(11);
                    butchCharacter.setIntScore(10);
                    butchCharacter.setWisScore(9);
                    butchCharacter.setChaScore(12);
                    butchCharacter.setHitPoints(35);
                    butchCharacter.setMaxHitPoints(35);
                    butchCharacter.setExperiencePoints(0);
                    butchCharacter.setEarthLocation("San Diego, CA");
                    butchCharacter.setCreatedAt(LocalDateTime.now());
                    butchCharacter.setUpdatedAt(LocalDateTime.now());
                    butchCharacter.setHasTransitioned(false);
                    earth2026CharacterRepository.save(butchCharacter);
                    log.info("Earth2026 character 'Butch Younger' created (age=35, occupation=CEO, playerId={})", butchPlayer.getId());
                }

                log.info("1 default player created (Butch Younger admin)");
            }
            // Seed promo codes for onboarding flow testing
            if (promoCodeRepository.count() == 0) {
                PromoCode immortal = new PromoCode();
                immortal.setCode("IMMORTAL");
                immortal.setDescription("Welcome to DoraMCU — Free access code");
                immortal.setActive(true);
                immortal.setMaxUses(1000);
                immortal.setUsedCount(0);
                immortal.setCodeType(PromoCode.CodeType.PLAYER);
                promoCodeRepository.save(immortal);

                PromoCode dragons = new PromoCode();
                dragons.setCode("DRAGONS");
                dragons.setDescription("Dragon slayer bonus — Early access code");
                dragons.setActive(true);
                dragons.setMaxUses(500);
                dragons.setUsedCount(0);
                dragons.setCodeType(PromoCode.CodeType.PLAYER);
                promoCodeRepository.save(dragons);

                log.info("2 default promo codes created (IMMORTAL, DRAGONS)");
            }
            // Seed corporation data for the corporation dashboard
            if (eveCorporationRepository.count() == 0) {
                // Create Butch Younger's corporation - Younger Industries
                EveCorporation corporation = new EveCorporation("Younger Industries", "YISY", "Butch Younger");
                corporation.setCeoPlayerId("1");
                corporation.setDescription("Interstellar trading and exploration corporation founded by Butch Younger. " +
                        "CEO position is switchable between Butch Younger (human) and Sif Younger (AI VP).");
                corporation.setCreditBalance(50000L);
                eveCorporationRepository.save(corporation);
                log.info("Corporation 'Younger Industries' created with 50,000 credits");

                // Create Sif Younger as the AI VP of Younger Industries
                // Sif is an AiCharacter (HUMAN_LEADER type) who serves as VP and can switch to CEO
                AiCharacter sifYounger = new AiCharacter();
                sifYounger.setName("Sif Younger");
                sifYounger.setRole("VP");
                sifYounger.setEmpireName("Younger Industries");
                sifYounger.setActive(true);
                sifYounger.setHirable(false);
                sifYounger.setHiringStatus(AiCharacter.HiringStatus.UNAVAILABLE);
                sifYounger.setTotalExperience(500);
                sifYounger.setLeadershipRating(9);
                sifYounger.setTotalBattles(0);
                sifYounger.setTotalVictories(0);
                sifYounger.setDescription("AI Vice President of Younger Industries. " +
                        "Sif Younger is the AI co-founder who can assume the CEO role when Butch Younger is unavailable. " +
                        "Serves as the intelligent operations manager and strategic advisor.");
                sifYounger.setCharacterType(AiCharacter.CharacterType.HUMAN_LEADER);
                sifYounger.setCanPromoteToCoServer(true);
                AiCharacter savedSif = aiCharacterRepository.save(sifYounger);
                log.info("AI character 'Sif Younger' created as VP of Younger Industries (id={})", savedSif.getId());

                // Set Sif Younger as the VP of Younger Industries
                corporation.setVpName("Sif Younger");
                corporation.setVpPlayerId(String.valueOf(savedSif.getId()));
                eveCorporationRepository.save(corporation);
                log.info("Sif Younger set as VP of Younger Industries. CEO can switch between Butch Younger and Sif Younger.");

                // Create AI characters for hiring
                String[] aiNames = {"Zara-7", "Kael-3", "Nova-9", "Orion-5", "Lyra-2"};
                String[] aiRoles = {"Navigator", "Tactician", "Engineer", "Diplomat", "Scientist"};

                List<AiCharacter> aiCharacters = new ArrayList<>();
                for (int i = 0; i < 5; i++) {
                    AiCharacter ai = new AiCharacter();
                    ai.setName(aiNames[i]);
                    ai.setRole(aiRoles[i]);
                    ai.setEmpireName("Younger Industries");
                    ai.setActive(true);
                    ai.setHirable(true);
                    ai.setHiringStatus(AiCharacter.HiringStatus.AVAILABLE);
                    ai.setTotalExperience(100 + i * 50);
                    ai.setLeadershipRating(5 + i);
                    ai.setTotalBattles(i * 3);
                    ai.setTotalVictories(i * 2);
                    aiCharacters.add(aiCharacterRepository.save(ai));
                }
                log.info("5 AI characters created for hiring");

                // Hire 2 AI characters for the corporation
                AiCharacter hired1 = aiCharacters.get(0); // Zara-7 Navigator
                hired1.setHiringStatus(AiCharacter.HiringStatus.HIRED);
                aiCharacterRepository.save(hired1);

                AiCharacter hired2 = aiCharacters.get(1); // Kael-3 Tactician
                hired2.setHiringStatus(AiCharacter.HiringStatus.HIRED);
                aiCharacterRepository.save(hired2);

                CorporateHiring hiring1 = new CorporateHiring(
                        corporation.getId(), hired1.getId(), CorporatePosition.CFO, 500L);
                hiring1.setPerformanceRating(4.5);
                hiring1.setCareerLevel(CareerLevel.JUNIOR);
                hiring1.setXpEarned(150);
                hiring1.setExpiryDate(LocalDateTime.now().plusDays(90));
                corporateHiringRepository.save(hiring1);

                CorporateHiring hiring2 = new CorporateHiring(
                        corporation.getId(), hired2.getId(), CorporatePosition.SECURITY_CHIEF, 450L);
                hiring2.setPerformanceRating(4.2);
                hiring2.setCareerLevel(CareerLevel.JUNIOR);
                hiring2.setXpEarned(100);
                hiring2.setExpiryDate(LocalDateTime.now().plusDays(90));
                corporateHiringRepository.save(hiring2);

                log.info("2 AI characters hired: Zara-7 (CFO), Kael-3 (Security Chief)");

                // Create available contracts
                String[] contractTitles = {
                        "Spice Trade Route Alpha", "Convoy Escort Mission",
                        "Supply Chain Optimization", "Corporate Espionage", "Market Analysis"
                };
                CorporateContract.ContractType[] contractTypes = {
                        CorporateContract.ContractType.TRADE, CorporateContract.ContractType.SECURITY,
                        CorporateContract.ContractType.LOGISTICS, CorporateContract.ContractType.MISSION,
                        CorporateContract.ContractType.TRADE
                };

                for (int i = 0; i < 5; i++) {
                    CorporateContract contract = new CorporateContract();
                    contract.setTitle(contractTitles[i]);
                    contract.setType(contractTypes[i]);
                    contract.setDescription("A " + contractTypes[i].name().toLowerCase() + " contract for the corporation.");
                    contract.setPaymentCredits(2000 + i * 1000);
                    contract.setXpReward(50 + i * 25);
                    contract.setDifficulty(1 + i);
                    contract.setDurationTurns(1 + i / 2);
                    contract.setStatus(CorporateContract.Status.AVAILABLE);
                    contract.setCorporationId(corporation.getId());
                    contract.setCreatedAt(LocalDateTime.now());
                    contract.setExpiryDate(LocalDateTime.now().plusDays(30));
                    corporateContractRepository.save(contract);
                }
                log.info("5 available contracts created for Younger Industries");

                log.info("Younger Industries corporation setup complete with CEO Butch Younger and AI VP Sif Younger");
            }
            // Seed Traveller character for Butch Younger (in-memory, for unified login)
            TravellerCharacter butchTraveller = new TravellerCharacter(1, "Butch Younger");
            butchTraveller.setAbility(AbilityScore.DEX, 10);
            butchTraveller.setAbility(AbilityScore.ST, 12);
            butchTraveller.setAbility(AbilityScore.END, 11);
            butchTraveller.setAbility(AbilityScore.INT, 10);
            butchTraveller.setAbility(AbilityScore.EDU, 9);
            butchTraveller.setAbility(AbilityScore.SOC, 12);
            butchTraveller.setAbility(AbilityScore.APP, 8);
            log.info("Traveller character 'Butch Younger' created (id=1, DEX:10, ST:12, END:11, INT:10, EDU:9, SOC:12, APP:8)");
            log.info("Basic database initialization completed");
        };
    }

    /**
     * First CommandLineRunner: generates the galaxy synchronously if it doesn't exist.
     * Creates AI-controlled Empire instances BEFORE any players join, so planets
     * can be assigned to empires with shipyard and industrial facilities.
     * When players later join the game session, their empires are converted from
     * AI-controlled to player-controlled.
     * This runs BEFORE any other initialization, ensuring the galaxy is fully built
     * and persisted before game sessions are created.
     */
    @Bean
    @ConditionalOnProperty(name = "doramcu.galaxy.auto-generate", havingValue = "true", matchIfMissing = false)
    CommandLineRunner initGalaxyFirst(GalaxyService galaxyService,
                                       GalaxyProgressService galaxyProgressService,
                                       PlayerRepository playerRepository) {
        return args -> {
            // Set progress listener to broadcast SSE events to connected clients
            galaxyService.setProgressListener(galaxyProgressService);

            java.util.List<Player> players = playerRepository.findAll();
            log.info("Checking galaxy persistence for {} player slots...", players.size());

            // Generate Game Galaxy ("Milky Way") only if it doesn't already exist
            if (!galaxyService.hasGalaxy(GalaxyFactory.GAME_GALAXY_NAME)) {
                log.info("Galaxy '{}' not found. Starting synchronous generation...", GalaxyFactory.GAME_GALAXY_NAME);

                Map<Integer, Empire> empires = buildAiEmpires(players);
                for (Empire e : empires.values()) {
                    log.info("Created AI-controlled empire: {} (id={}, ruler=AI)", e.getName(), e.getId());
                }

                galaxyService.generateAndSaveGalaxyWithEmpires(
                        GalaxyFactory.GAME_GALAXY_NAME,
                        empires,
                        GalaxyFactory.gameGalaxyConfig().getSystemCount(),
                        GalaxyFactory.gameGalaxyConfig().getPlanetsPerSystem(),
                        GalaxyFactory.gameGalaxyConfig().getMoonChance(),
                        GalaxyFactory.gameGalaxyConfig().getAsteroidBeltChance(),
                        GalaxyFactory.gameGalaxyConfig().getGasGiantChance(),
                        GalaxyFactory.gameGalaxyConfig().getNebulaChance(),
                        GalaxyFactory.gameGalaxyConfig().getBlackHoleChance(),
                        GalaxyFactory.gameGalaxyConfig().getCometChance(),
                        GalaxyFactory.gameGalaxyConfig().getSpaceStationChance(),
                        GalaxyFactory.gameGalaxyConfig().getHyperlaneDensity(),
                        GalaxyFactory.GAME_GALAXY_SEED
                );
                log.info("Galaxy '{}' generated with {} AI-controlled empires and persisted at startup",
                        GalaxyFactory.GAME_GALAXY_NAME, players.size());
            } else {
                log.info("Galaxy '{}' already exists in database. Skipping generation.", GalaxyFactory.GAME_GALAXY_NAME);
            }

            // Signal galaxy ready to all connected SSE clients so the frontend modal closes
            galaxyProgressService.finishAll();
            log.info("Galaxy ready signal broadcast to connected clients");

        };
    }

    /**
     * Builds a map of AI-controlled Empire instances from the given players.
     *
     * @param players the list of players
     * @return map of empire ID to Empire instance
     */
    private static Map<Integer, Empire> buildAiEmpires(java.util.List<Player> players) {
        Map<Integer, Empire> empires = new HashMap<>();
        for (Player player : players) {
            int empireId = player.getId().intValue();
            Empire empire = new Empire(empireId, player.getEmpireName(), -1);
            empires.put(empireId, empire);
        }
        return empires;
    }

    /**
     * Second CommandLineRunner: creates the default game session after galaxy is ready.
     * Uses GalaxyCreationService to orchestrate the complete sequence:
     * galaxy creation → session creation → 3 AI players → ready for human players to join.
     */
    @Bean
    @ConditionalOnProperty(name = "doramcu.galaxy.auto-generate", havingValue = "true", matchIfMissing = false)
    CommandLineRunner initGameSessionAfterGalaxy(GalaxyCreationService galaxyCreationService,
                                                  GameSessionService gameSessionService) {
        return args -> {
            // Always ensure a default game session exists, after galaxy is ready.
            if (gameSessionService.getDefaultSession() == null) {
                log.info("Creating default game session with AI players using GalaxyCreationService...");
                String defaultSessionName = "Galactic Conquest";

                // Use GalaxyCreationService to orchestrate the complete sequence
                GalaxyCreationService.GalaxyCreationResult result =
                        galaxyCreationService.createGalaxyWithAiPlayers(defaultSessionName);

                log.info("Default game session created: {} ({})", result.getSessionId(), defaultSessionName);
                log.info("AI players added: {}", result.getAiPlayers().size());
                for (GalaxyCreationService.AiPlayerInfo aiPlayer : result.getAiPlayers()) {
                    log.info("  - {} (id={}, difficulty={})", aiPlayer.getPlayerName(),
                            aiPlayer.getPlayerId(), aiPlayer.getDifficulty());
                }
                log.info("Galaxy ready for human players to join with empire creation");
            } else {
                log.info("Default game session already exists: {} ({})",
                        gameSessionService.getDefaultSession().getName(),
                        gameSessionService.getDefaultSession().getSessionId());
            }
        };
    }

    /**
     * Generates the game galaxy synchronously with the given progress listener.
     * Used by the desktop launcher to show the startup modal while generating.
     *
     * @param galaxyService the galaxy service
     * @param progressListener the progress listener (e.g., GalaxyStartupModal)
     */
    public static void generateGalaxyWithProgress(GalaxyService galaxyService, GalaxyGenerationProgress progressListener) {
        log.info("Generating galaxy with progress reporting...");
        galaxyService.setProgressListener(progressListener);

        galaxyService.generateAndSaveGalaxy(
                GalaxyFactory.GAME_GALAXY_NAME,
                GalaxyFactory.gameGalaxyConfig().getSystemCount(),
                GalaxyFactory.gameGalaxyConfig().getPlanetsPerSystem(),
                GalaxyFactory.gameGalaxyConfig().getMoonChance(),
                GalaxyFactory.gameGalaxyConfig().getAsteroidBeltChance(),
                GalaxyFactory.gameGalaxyConfig().getGasGiantChance(),
                GalaxyFactory.gameGalaxyConfig().getNebulaChance(),
                GalaxyFactory.gameGalaxyConfig().getBlackHoleChance(),
                GalaxyFactory.gameGalaxyConfig().getCometChance(),
                GalaxyFactory.gameGalaxyConfig().getSpaceStationChance(),
                GalaxyFactory.gameGalaxyConfig().getHyperlaneDensity(),
                GalaxyFactory.GAME_GALAXY_SEED
        );
        log.info("Galaxy generation with progress reporting complete");
    }

    /**
     * CommandLineRunner: signals that multi-galaxy build queue is handled by
     * GalaxyStartupRegistry via ApplicationReadyEvent with high priority order.
     * The registry fires after all game modules have registered themselves.
     * This bean is kept for documentation purposes; the actual work is done
     * by GalaxyStartupRegistry.registerAndProcessGalaxies().
     */
    @Bean
    @ConditionalOnProperty(name = "doramcu.galaxy.multi-galaxy.enabled", havingValue = "true", matchIfMissing = false)
    CommandLineRunner initMultiGalaxyQueue() {
        return args -> {
            log.info("Multi-galaxy build queue is handled by GalaxyStartupRegistry at ApplicationReadyEvent");
        };
    }

    /**
     * Last CommandLineRunner: signals that all server-side initialization is complete.
     * DoraMcuLauncher waits for this runner to finish before launching the desktop app.
     * The desktop app is launched by DoraMcuLauncher after SpringApplication.run() returns,
     * so this runner only logs the completion status.
     */
    @Bean
    CommandLineRunner signalInitializationComplete() {
        return args -> {
            // Detect headless mode — log different message based on mode
            boolean headless = "true".equals(System.getProperty("java.awt.headless"));
            if (!headless) {
                try {
                    headless = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().isHeadlessInstance();
                } catch (java.awt.HeadlessException e) {
                    headless = true;
                }
            }
            if (headless) {
                log.info("All initialization complete — server is ready on port 8080 (headless mode)");
            } else {
                log.info("All server initialization complete — ready to launch GUI");
            }
        };
    }
}
