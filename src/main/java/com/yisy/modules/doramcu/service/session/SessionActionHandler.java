package com.yisy.modules.doramcu.service.session;
import com.yisy.modules.doramcu.hex.Ship;
import com.yisy.modules.doramcu.hex.ShipType;
import com.yisy.modules.doramcu.hex.Hex;
import com.yisy.modules.doramcu.hex.ResourcePipelineManager;
import com.yisy.modules.doramcu.hex.PlanetFacility;
import com.yisy.modules.doramcu.service.ai.AiCommanderExperienceService;
import com.yisy.modules.doramcu.service.ai.AiEntityRegistrationService;
import com.yisy.modules.doramcu.dto.station.BuildFacilityRequest;
import com.yisy.modules.doramcu.dto.ship_design.BuildShipRequest;
import com.yisy.modules.doramcu.dto.station.BuildStationRequest;
import com.yisy.modules.doramcu.service.character.CharacterService;
import com.yisy.modules.doramcu.dto.game.ColonizeRequest;
import com.yisy.modules.doramcu.dto.fleet.CreateFleetRequest;
import com.yisy.modules.doramcu.dto.battle.EngageCombatRequest;
import com.yisy.modules.doramcu.service.game.GameStateBuilder;
import com.yisy.modules.doramcu.dto.session.GameStateResponse;
import com.yisy.modules.doramcu.hex.Hyperlane;
import com.yisy.modules.doramcu.dto.fleet.MoveFleetRequest;
import com.yisy.modules.doramcu.dto.battle.ReinforceBattleRequest;
import com.yisy.modules.doramcu.dto.ship_design.ShipDesignRequest;
import com.yisy.modules.doramcu.hex.SpaceStation;
import com.yisy.modules.doramcu.dto.trade.TradeRequest;


import com.yisy.modules.doramcu.ai.AiDelegationService;
import com.yisy.modules.doramcu.ai.AiDecision;
import com.yisy.modules.doramcu.ai.PlayerActionAnalyzer;
import com.yisy.modules.doramcu.dto.fleet.FleetDto;
import com.yisy.modules.doramcu.dto.misc.IncomeProjectionDto;
import com.yisy.modules.doramcu.dto.misc.PipelineDetailsDto;
import com.yisy.modules.doramcu.dto.misc.ResourcePipelineSummary;
import com.yisy.modules.doramcu.dto.players.PlayerSummary;
import com.yisy.modules.doramcu.eve.model.AiCharacter;
import com.yisy.modules.doramcu.gamemode.GameModeRegistry;
import com.yisy.modules.doramcu.hex.Battle;
import com.yisy.modules.doramcu.hex.Empire;
import com.yisy.modules.doramcu.hex.Fleet;
import com.yisy.modules.doramcu.hex.Galaxy;
import com.yisy.modules.doramcu.hex.Game;
import com.yisy.modules.doramcu.hex.Hex3D;
import com.yisy.modules.doramcu.service.character.CharacterService;
import com.yisy.modules.doramcu.hex.Planet;
import com.yisy.modules.doramcu.hex.Player;
import com.yisy.modules.doramcu.hex.ShipOrder;
import com.yisy.modules.doramcu.model.session.GameSession;
import com.yisy.modules.doramcu.traveller.TravellerCharacter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Handles all game action operations within a session: fleet movement, combat,
 * building ships/stations/facilities, creating fleets, colonizing planets,
 * trading, and commander assignment.
 * <p>
 * Extracted from {@link GameSessionService} to separate mutable game actions
 * from session lifecycle and state queries. This component is not a standalone
 * Spring service — it is injected into GameSessionService which owns the
 * session map and provides access to it.
 * </p>
 */
@Component
public class SessionActionHandler {

    private static final Logger log = LoggerFactory.getLogger(SessionActionHandler.class);

    private final GameStateBuilder gameStateBuilder;
    private final CharacterService characterService;
    private final AiDelegationService aiDelegationService;
    private final PlayerActionAnalyzer playerActionAnalyzer;
    private final AiEntityRegistrationService aiEntityRegistrationService;
    private final AiCommanderExperienceService aiCommanderExperienceService;
    private final GameModeRegistry gameModeRegistry;

    public SessionActionHandler(GameStateBuilder gameStateBuilder,
                                CharacterService characterService,
                                @Lazy AiDelegationService aiDelegationService,
                                PlayerActionAnalyzer playerActionAnalyzer,
                                AiEntityRegistrationService aiEntityRegistrationService,
                                AiCommanderExperienceService aiCommanderExperienceService,
                                GameModeRegistry gameModeRegistry) {
        this.gameStateBuilder = gameStateBuilder;
        this.characterService = characterService;
        this.aiDelegationService = aiDelegationService;
        this.playerActionAnalyzer = playerActionAnalyzer;
        this.aiEntityRegistrationService = aiEntityRegistrationService;
        this.aiCommanderExperienceService = aiCommanderExperienceService;
        this.gameModeRegistry = gameModeRegistry;
    }

    /**
     * Finds the nearest enemy fleet to the given position that is within combat range.
     */
    Fleet findNearestEnemyFleet(Game game, Fleet movingFleet, Hex3D targetPosition, Player movingPlayer) {
        Fleet nearest = null;
        int nearestDistance = 3; // Combat range threshold

        for (Fleet other : game.getFleets().values()) {
            if (other.getId() == movingFleet.getId()) continue;
            if (other.isDestroyed()) continue;
            if (other.getOwner() == null) continue;
            if (other.getOwner().getId() == movingPlayer.getId()) continue;
            if (other.getPosition() == null) continue;

            int distance = targetPosition.distance(other.getPosition());
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = other;
            }
        }
        return nearest;
    }

    // ---- Public action methods ----

    public GameStateResponse moveFleet(GameSession session, Long playerId, MoveFleetRequest request) {
        Game game = (Game) session.getGame();
        session.getLock().writeLock().lock();
        try {
            Player player = findPlayerBySessionId(game, session, playerId);
            if (player == null) {
                log.warn("Player {} not found in session {}", playerId, session.getSessionId());
                throw new IllegalStateException("Player not found in session");
            }

            Integer fleetId = request.getFleetId();
            if (!player.getOwnedFleetIds().contains(fleetId)) {
                log.warn("Player {} tried to move fleet {} which they don't own", playerId, fleetId);
                throw new IllegalStateException("Fleet not found or not owned");
            }

            Fleet fleet = game.getFleets().get(fleetId);
            if (fleet == null) {
                throw new IllegalStateException("Fleet not found");
            }

            Hex3D target = new Hex3D(request.getTargetRow(), request.getTargetCol(), 0);
            Integer targetSystemId = request.getSolarSystemId();

            if (targetSystemId == null) {
                Hex3D currentPos = fleet.getPosition();
                if (currentPos != null) {
                    int distance = currentPos.distance(target);
                    int mDriveRange = fleet.getEffectiveFleetMDriveSpeed();
                    if (distance > mDriveRange) {
                        log.warn("Player {} tried to move fleet {} beyond MDrive range: distance={}, range={}",
                                playerId, fleetId, distance, mDriveRange);
                        throw new IllegalStateException(
                                "Target is out of MDrive range: distance " + distance + " exceeds range " + mDriveRange);
                    }
                }
                fleet.setPosition(target);
                log.debug("Player {} moved fleet {} within system to ({}, {}) using MDrive",
                        playerId, fleetId, request.getTargetRow(), request.getTargetCol());
            } else {
                if (!game.getSolarSystems().containsKey(targetSystemId)) {
                    log.warn("Player {} tried to jump fleet {} to non-existent system {}",
                            playerId, fleetId, targetSystemId);
                    throw new IllegalStateException("Target solar system not found: " + targetSystemId);
                }

                int jDriveRange = fleet.getEffectiveFleetJDriveSpeed();
                if (jDriveRange <= 0) {
                    log.warn("Player {} tried to jump fleet {} but JDrive is not operational",
                            playerId, fleetId);
                    throw new IllegalStateException("JDrive is not operational — fleet health too low");
                }

                boolean hasHyperlane = false;
                for (Galaxy galaxy : game.getGalaxies().values()) {
                    for (Hyperlane hyperlane : galaxy.getAllHyperlanes()) {
                        if (hyperlane.connectsTo(targetSystemId)) {
                            hasHyperlane = true;
                            break;
                        }
                    }
                    if (hasHyperlane) break;
                }

                if (!hasHyperlane) {
                    log.warn("Player {} tried to jump fleet {} but no hyperlane connects to system {}",
                            playerId, fleetId, targetSystemId);
                    throw new IllegalStateException("No hyperlane connection to target system");
                }

                fleet.setPosition(target);
                log.debug("Player {} jumped fleet {} to system {} at ({}, {}) using JDrive",
                        playerId, fleetId, targetSystemId, request.getTargetRow(), request.getTargetCol());
            }

            // Check for enemy fleets at or near the destination
            Fleet nearestEnemy = findNearestEnemyFleet(game, fleet, target, player);
            if (nearestEnemy != null) {
                log.info("Auto-combat triggered: fleet {} moved into range of enemy fleet {}",
                        fleet.getName(), nearestEnemy.getName());
                Player defenderPlayer = nearestEnemy.getOwner();

                Planet defenderPlanet = null;
                if (defenderPlayer != null && nearestEnemy.getHomeSystem() != null) {
                    for (Integer planetId : defenderPlayer.getOwnedPlanetIds()) {
                        Planet planet = game.getPlanet(planetId);
                        if (planet != null) {
                            defenderPlanet = planet;
                            break;
                        }
                    }
                }

                Battle battle;
                if (defenderPlanet != null) {
                    battle = game.createBattle(fleet, nearestEnemy, target, defenderPlanet);
                } else {
                    battle = game.createBattle(fleet, nearestEnemy, target);
                }
                Battle.BattleResult result = battle.fightToCompletion();
                applyBattleResult(game, session, battle, result, player, defenderPlayer);
            }

            recordHumanAction(player, game, AiDecision.ActionType.MOVE_FLEET);
            return getGameStateInternal(session, playerId);
        } finally {
            session.getLock().writeLock().unlock();
        }
    }

    public GameStateResponse buildShip(GameSession session, Long playerId, BuildShipRequest request) {
        Game game = (Game) session.getGame();
        session.getLock().writeLock().lock();
        try {
            Player player = findPlayerBySessionId(game, session, playerId);
            if (player == null) {
                log.warn("Player {} not found in session {}", playerId, session.getSessionId());
                throw new IllegalStateException("Player not found in session");
            }

            ShipType shipType;
            try {
                shipType = ShipType.valueOf(request.getShipType());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid ship type: " + request.getShipType());
            }

            Ship ship = new Ship(0, shipType);
            Integer targetFleetId = request.getTargetFleetId();
            if (targetFleetId != null) {
                if (!player.getOwnedFleetIds().contains(targetFleetId)) {
                    throw new IllegalStateException("Fleet not found or not owned");
                }
            }

            Integer buildEmpireId = player.getEmpireId();
            if (buildEmpireId == null) {
                throw new IllegalStateException("Player has no empire");
            }
            ResourcePipelineManager pipeline = game.getPipelineManager(buildEmpireId);
            if (pipeline == null) {
                log.warn("Player {} has no resource pipeline initialized", player.getName());
                throw new IllegalStateException("Resource pipeline not initialized for player");
            }

            ShipOrder order = game.queueShipBuild(player, ship,
                    targetFleetId != null ? targetFleetId : 0, pipeline);
            if (order == null) {
                log.warn("Player {} cannot afford ship {} with current modules",
                        player.getName(), request.getShipType());
                throw new IllegalStateException("Insufficient construction modules for ship build");
            }

            log.info("Ship build queued for {} (pipeline modules): {}", player.getName(), request.getShipType());
            log.debug("Player {} queued build for ship type: {} in fleet: {}", playerId, request.getShipType(), targetFleetId);

            recordHumanAction(player, game, AiDecision.ActionType.BUILD_SHIP);
            return getGameStateInternal(session, playerId);
        } finally {
            session.getLock().writeLock().unlock();
        }
    }

    public GameStateResponse engageCombat(GameSession session, Long playerId, EngageCombatRequest request) {
        Game game = (Game) session.getGame();
        session.getLock().writeLock().lock();
        try {
            Player player = findPlayerBySessionId(game, session, playerId);
            if (player == null) {
                log.warn("Player {} not found in session {}", playerId, session.getSessionId());
                throw new IllegalStateException("Player not found in session");
            }

            Integer attackerFleetId = request.getAttackerFleetId();
            if (!player.getOwnedFleetIds().contains(attackerFleetId)) {
                throw new IllegalStateException("Attacker fleet not found or not owned");
            }

            Fleet attacker = game.getFleets().get(attackerFleetId);
            Fleet defender = game.getFleets().get(request.getDefenderFleetId());
            if (attacker == null || defender == null) {
                throw new IllegalStateException("Fleet not found");
            }

            if (attacker.isDestroyed()) {
                throw new IllegalStateException("Attacker fleet is destroyed");
            }
            if (defender.isDestroyed()) {
                throw new IllegalStateException("Defender fleet is destroyed");
            }

            if (attacker.getPosition() != null && defender.getPosition() != null) {
                int distance = attacker.getPosition().distance(defender.getPosition());
                if (distance > 2) {
                    throw new IllegalStateException("Fleets are too far apart to engage: distance " + distance);
                }
            }

            Planet defenderPlanet = null;
            Player defenderPlayer = defender.getOwner();
            if (defenderPlayer != null && defender.getPosition() != null) {
                if (defender.getHomeSystem() != null) {
                    for (Integer planetId : defenderPlayer.getOwnedPlanetIds()) {
                        Planet planet = game.getPlanet(planetId);
                        if (planet != null) {
                            defenderPlanet = planet;
                            break;
                        }
                    }
                }
            }

            Battle battle;
            if (defenderPlanet != null) {
                battle = game.createBattle(attacker, defender, attacker.getPosition(), defenderPlanet);
                log.debug("Battle created with defender planet bonus: {}", defenderPlanet.getName());
            } else {
                battle = game.createBattle(attacker, defender, attacker.getPosition());
            }
            Battle.BattleResult result = battle.fightToCompletion();
            applyBattleResult(game, session, battle, result, player, defenderPlayer);

            log.info("Player {} engaged combat: fleet {} vs fleet {}, outcome: {}",
                    playerId, attackerFleetId, request.getDefenderFleetId(),
                    result != null ? result.getOutcome() : "null");

            recordHumanAction(player, game, AiDecision.ActionType.ENGAGE_COMBAT);
            return getGameStateInternal(session, playerId);
        } finally {
            session.getLock().writeLock().unlock();
        }
    }

    public GameStateResponse advanceTurn(GameSession session, Long playerId) {
        Game game = (Game) session.getGame();
        session.getLock().writeLock().lock();
        try {
            Player player = findPlayerBySessionId(game, session, playerId);
            if (player == null) {
                log.warn("Player {} not found in session {}", playerId, session.getSessionId());
                throw new IllegalStateException("Player not found in session");
            }

            game.nextTurn();
            // Advance global clock by one turn (one in-game day)
            com.yisy.modules.doramcu.unified.GlobalClock clock = session.getGlobalClock();
            if (clock != null) {
                clock.advanceTurn();
            }

            log.debug("Turn advanced to {} in session: {} (simultaneous turns)",
                    game.getCurrentTurn(), session.getSessionId());

            // Execute AI turns for all players with delegation enabled
            try {
                aiDelegationService.executeAllDelegatedTurns(session.getSessionId());
            } catch (Exception e) {
                log.warn("AI delegation turn execution failed in session {}: {}", session.getSessionId(), e.getMessage());
            }

            // Process game mode modules for this turn (RPG, 4X, Tycoon, etc.)
            try {
                String galaxyName = session.getName();
                int turnNumber = game.getCurrentTurn();
                String playerIdStr = String.valueOf(playerId);
                String gameModeSummary = gameModeRegistry.processTurnForGalaxy(galaxyName, turnNumber, playerIdStr);
                if (!gameModeSummary.isEmpty() && !gameModeSummary.contains("No game modes active")) {
                    log.info("Game mode turn results for {}: {}", galaxyName, gameModeSummary.replace("\n", " | "));
                }
            } catch (Exception e) {
                log.warn("Game mode turn processing failed in session {}: {}", session.getSessionId(), e.getMessage());
            }

            recordHumanAction(player, game, AiDecision.ActionType.WAIT);
            return getGameStateInternal(session, playerId);
        } finally {
            session.getLock().writeLock().unlock();
        }
    }

    public GameStateResponse buildStation(GameSession session, Long playerId, BuildStationRequest request) {
        Game game = (Game) session.getGame();
        session.getLock().writeLock().lock();
        try {
            Player player = findPlayerBySessionId(game, session, playerId);
            if (player == null) {
                throw new IllegalStateException("Player not found in session");
            }

            com.yisy.modules.doramcu.hex.SpaceStationType stationType;
            try {
                stationType = com.yisy.modules.doramcu.hex.SpaceStationType.valueOf(request.getStationType());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid station type: " + request.getStationType());
            }

            Integer stationEmpireId = player.getEmpireId();
            if (stationEmpireId == null) {
                throw new IllegalStateException("Player has no empire");
            }
            if (!game.canBuildStation(stationEmpireId, stationType)) {
                throw new IllegalStateException("Insufficient resources to build " + stationType + " station");
            }

            if (request.getPlanetId() != null) {
                Planet planet = game.getPlanet(request.getPlanetId());
                if (planet == null) {
                    throw new IllegalStateException("Planet not found: " + request.getPlanetId());
                }
            }

            com.yisy.modules.doramcu.hex.SpaceStation station = game.buildStation(player, stationType, null);
            if (station == null) {
                throw new IllegalStateException("Failed to build station");
            }

            log.info("Player {} built {} station (id={})", player.getName(), stationType, station.getId());
            recordHumanAction(player, game, AiDecision.ActionType.BUILD_STATION);
            return getGameStateInternal(session, playerId);
        } finally {
            session.getLock().writeLock().unlock();
        }
    }

    /**
     * Registers a facility as an AI character in the AI character repository.
     * No-op if the AI entity registration service is not available.
     */
    private void registerFacilityAsAi(PlanetFacility facility, String empireName) {
        if (aiEntityRegistrationService != null && facility != null) {
            try {
                aiEntityRegistrationService.registerFacility(facility, empireName);
            } catch (Exception e) {
                // Silently ignore if registration fails
            }
        }
    }

    public GameStateResponse buildFacility(GameSession session, Long playerId, BuildFacilityRequest request) {
        Game game = (Game) session.getGame();
        session.getLock().writeLock().lock();
        try {
            Player player = findPlayerBySessionId(game, session, playerId);
            if (player == null) {
                throw new IllegalStateException("Player not found in session");
            }

            Planet planet = game.getPlanet(request.getPlanetId());
            if (planet == null) {
                throw new IllegalStateException("Planet not found: " + request.getPlanetId());
            }

            if (!player.getOwnedPlanetIds().contains(request.getPlanetId())) {
                throw new IllegalStateException("Planet is not owned by this player");
            }

            com.yisy.modules.doramcu.hex.StartingFacility facility;
            try {
                facility = com.yisy.modules.doramcu.hex.StartingFacility.valueOf(request.getFacilityType());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid facility type: " + request.getFacilityType());
            }

            Integer facilityEmpireId = player.getEmpireId();
            if (facilityEmpireId == null) {
                throw new IllegalStateException("Player has no empire");
            }
            if (!game.canBuildPlanetFacility(facilityEmpireId, facility)) {
                throw new IllegalStateException("Insufficient resources to build " + facility.getDisplayName());
            }

            boolean success = game.buildPlanetFacility(planet, facility, facilityEmpireId);
            if (!success) {
                throw new IllegalStateException("Failed to build facility on planet");
            }

            Empire empire = game.getEmpire(facilityEmpireId);
            if (empire != null) {
                PlanetFacility pf = empire.getFacilities().buildFacility(planet.getId(), facility);
                registerFacilityAsAi(pf, empire.getName());
            }

            log.info("Player {} built {} facility on planet {}", player.getName(), facility.getDisplayName(), request.getPlanetId());
            recordHumanAction(player, game, AiDecision.ActionType.BUILD_STATION);
            return getGameStateInternal(session, playerId);
        } finally {
            session.getLock().writeLock().unlock();
        }
    }

    public GameStateResponse createFleet(GameSession session, Long playerId, CreateFleetRequest request) {
        Game game = (Game) session.getGame();
        session.getLock().writeLock().lock();
        try {
            Player player = findPlayerBySessionId(game, session, playerId);
            if (player == null) {
                throw new IllegalStateException("Player not found in session");
            }

            Fleet fleet = game.createFleet(request.getFleetName(), player);

            Integer fleetEmpireId = player.getEmpireId();
            Empire empire = game.getEmpire(fleetEmpireId);
            if (empire != null) {
                empire.getNavy().addFleet(fleet);
            }

            log.info("Player {} created fleet '{}' (id={})", player.getName(), request.getFleetName(), fleet.getId());
            recordHumanAction(player, game, AiDecision.ActionType.SCOUT);
            return getGameStateInternal(session, playerId);
        } finally {
            session.getLock().writeLock().unlock();
        }
    }

    public GameStateResponse colonizePlanet(GameSession session, Long playerId, ColonizeRequest request) {
        Game game = (Game) session.getGame();
        session.getLock().writeLock().lock();
        try {
            Player player = findPlayerBySessionId(game, session, playerId);
            if (player == null) {
                throw new IllegalStateException("Player not found in session");
            }

            Planet planet = game.getPlanet(request.getPlanetId());
            if (planet == null) {
                throw new IllegalStateException("Planet not found: " + request.getPlanetId());
            }

            boolean success = game.transferPlanet(request.getPlanetId(), player);
            if (!success) {
                throw new IllegalStateException("Failed to colonize planet");
            }

            Integer colonizeEmpireId = player.getEmpireId();
            Empire empire = game.getEmpire(colonizeEmpireId);
            if (empire != null) {
                empire.claimPlanet(planet.getId());
            }

            log.info("Player {} colonized planet {}", player.getName(), request.getPlanetId());
            recordHumanAction(player, game, AiDecision.ActionType.SCOUT);
            return getGameStateInternal(session, playerId);
        } finally {
            session.getLock().writeLock().unlock();
        }
    }

    public GameStateResponse reinforceBattle(GameSession session, Long playerId, int battleId, ReinforceBattleRequest request) {
        Game game = (Game) session.getGame();
        session.getLock().writeLock().lock();
        try {
            Player player = findPlayerBySessionId(game, session, playerId);
            if (player == null) {
                throw new IllegalStateException("Player not found in session");
            }

            Integer fleetId = request.getFleetId();
            if (!player.getOwnedFleetIds().contains(fleetId)) {
                throw new IllegalStateException("Fleet not found or not owned");
            }

            Fleet fleet = game.getFleet(fleetId);
            if (fleet == null) {
                throw new IllegalStateException("Fleet not found");
            }

            int added = game.reinforceAttacker(battleId, fleet);
            log.info("Player {} reinforced battle {} with {} ships from fleet {}",
                    player.getName(), battleId, added, fleetId);

            recordHumanAction(player, game, AiDecision.ActionType.ENGAGE_COMBAT);
            return getGameStateInternal(session, playerId);
        } finally {
            session.getLock().writeLock().unlock();
        }
    }

    public GameStateResponse retreatBattle(GameSession session, Long playerId, int battleId) {
        Game game = (Game) session.getGame();
        session.getLock().writeLock().lock();
        try {
            Player player = findPlayerBySessionId(game, session, playerId);
            if (player == null) {
                throw new IllegalStateException("Player not found in session");
            }

            game.retreatBattle(battleId);
            log.info("Player {} retreated from battle {}", player.getName(), battleId);

            recordHumanAction(player, game, AiDecision.ActionType.RETREAT);
            return getGameStateInternal(session, playerId);
        } finally {
            session.getLock().writeLock().unlock();
        }
    }

    public GameStateResponse buildShipFromDesign(GameSession session, Long playerId, ShipDesignRequest request) {
        Game game = (Game) session.getGame();
        session.getLock().writeLock().lock();
        try {
            Player player = findPlayerBySessionId(game, session, playerId);
            if (player == null) {
                throw new IllegalStateException("Player not found in session");
            }

            int shipId = game.createShipFromDesign(player, request);
            log.info("Player {} built ship from design: {}", player.getName(), request.getName());

            recordHumanAction(player, game, AiDecision.ActionType.BUILD_SHIP);
            return getGameStateInternal(session, playerId);
        } finally {
            session.getLock().writeLock().unlock();
        }
    }

    public GameStateResponse buyCommodity(GameSession session, Long playerId, TradeRequest request) {
        Game game = (Game) session.getGame();
        session.getLock().writeLock().lock();
        try {
            Player player = findPlayerBySessionId(game, session, playerId);
            if (player == null) {
                throw new IllegalStateException("Player not found in session");
            }

            int cost = game.buyCommodity(player, request.getWorldId(), request.getCommodity(), request.getQuantity());
            log.info("Player {} bought {} units of {} for {} credits", player.getName(), request.getQuantity(), request.getCommodity(), cost);

            recordHumanAction(player, game, AiDecision.ActionType.BUILD_STATION);
            return getGameStateInternal(session, playerId);
        } finally {
            session.getLock().writeLock().unlock();
        }
    }

    public GameStateResponse sellCommodity(GameSession session, Long playerId, TradeRequest request) {
        Game game = (Game) session.getGame();
        session.getLock().writeLock().lock();
        try {
            Player player = findPlayerBySessionId(game, session, playerId);
            if (player == null) {
                throw new IllegalStateException("Player not found in session");
            }

            int revenue = game.sellCommodity(player, request.getWorldId(), request.getCommodity(), request.getQuantity());
            log.info("Player {} sold {} units of {} for {} credits", player.getName(), request.getQuantity(), request.getCommodity(), revenue);

            recordHumanAction(player, game, AiDecision.ActionType.BUILD_STATION);
            return getGameStateInternal(session, playerId);
        } finally {
            session.getLock().writeLock().unlock();
        }
    }

    public GameStateResponse assignCommander(GameSession session, Long playerId, int characterId) {
        Game game = (Game) session.getGame();
        session.getLock().writeLock().lock();
        try {
            Player player = findPlayerBySessionId(game, session, playerId);
            if (player == null) {
                throw new IllegalStateException("Player not found in session");
            }

            TravellerCharacter character = characterService.getCharacter(characterId);
            if (character == null) {
                throw new NoSuchElementException("Character not found: " + characterId);
            }

            player.setCommander(character);
            log.info("Player {} assigned commander '{}' (id={})", player.getName(), character.getName(), characterId);

            recordHumanAction(player, game, AiDecision.ActionType.WAIT);
            return getGameStateInternal(session, playerId);
        } finally {
            session.getLock().writeLock().unlock();
        }
    }

    // ---- Human action recording ----

    /**
     * Records a human player action for behavioral personality analysis.
     * <p>
     * Only records actions for human players (IDs < 1000). AI players (IDs >= 1000)
     * are excluded because their actions are generated by the AI, not observed behavior.
     * </p>
     *
     * @param player the player who performed the action
     * @param game the current game state
     * @param actionType the type of action performed
     */
    private void recordHumanAction(Player player, Game game, AiDecision.ActionType actionType) {
        if (player == null || game == null || actionType == null) {
            return;
        }
        int playerId = player.getId();
        // Only record human players; AI players have IDs >= 1000
        if (playerId >= 1000) {
            return;
        }
        try {
            int turn = game.getCurrentTurn();
            playerActionAnalyzer.recordActionFromDecision(playerId, actionType, turn);
        } catch (Exception e) {
            log.warn("Failed to record human player action: {}", e.getMessage());
        }
    }

    // ---- Battle result handling ----

    private void applyBattleResult(Game game, GameSession session,
                                    Battle battle, Battle.BattleResult result,
                                    Player attackerPlayer, Player defenderPlayer) {
        if (result == null) return;

        Fleet attacker = battle.getAttacker();
        Fleet defender = battle.getDefender();
        Battle.BattleOutcome outcome = result.getOutcome();

        applyFleetDamageFromBattle(attacker, battle);
        applyFleetDamageFromBattle(defender, battle);

        // Deactivate AI characters for destroyed ships before removal
        deactivateAiForDestroyedShips(attacker);
        deactivateAiForDestroyedShips(defender);

        // Record battle experience for surviving ships' AI characters
        recordBattleExperienceForFleet(attacker, outcome, true);
        recordBattleExperienceForFleet(defender, outcome, false);

        attacker.removeDestroyedShips();
        defender.removeDestroyedShips();

        if (attacker.isDestroyed()) {
            log.info("Attacker fleet {} completely destroyed in battle {}", attacker.getName(), battle.getId());
            removeFleetFromGame(game, attacker, attackerPlayer);
        }

        if (defender.isDestroyed()) {
            log.info("Defender fleet {} completely destroyed in battle {}", defender.getName(), battle.getId());
            removeFleetFromGame(game, defender, defenderPlayer);
        }

        if (outcome == Battle.BattleOutcome.ATTACKER_VICTORY && defenderPlayer != null) {
            captureDefenderPlanets(game, session, attackerPlayer, defenderPlayer, battle);
        }

        checkPlayerElimination(game, session, attackerPlayer);
        checkPlayerElimination(game, session, defenderPlayer);
    }

    private void applyFleetDamageFromBattle(Fleet actualFleet, Battle battle) {
        boolean isAttacker = actualFleet.getId() == battle.getAttacker().getId();
        int totalDamageDealt = isAttacker ? battle.getTotalDefenderDamage() : battle.getTotalAttackerDamage();

        if (totalDamageDealt > 0) {
            actualFleet.distributeDamage(totalDamageDealt);
        }
    }

    /**
     * Deactivates AI characters for destroyed ships in a fleet.
     * No-op if the AI entity registration service is not available.
     */
    private void deactivateAiForDestroyedShips(Fleet fleet) {
        if (aiEntityRegistrationService == null || fleet == null) {
            return;
        }
        for (Ship ship : fleet.getDestroyedShips()) {
            try {
                aiEntityRegistrationService.deactivateShipAiCharacter(ship.getId());
            } catch (Exception e) {
                // Silently ignore if deactivation fails
            }
        }
    }

    /**
     * Records battle experience for surviving ships' AI characters in a fleet.
     * No-op if the AI commander experience service is not available.
     *
     * @param fleet the fleet to process
     * @param battleOutcome the overall battle outcome
     * @param isAttacker true if this fleet was the attacker in the battle
     */
    private void recordBattleExperienceForFleet(Fleet fleet, Battle.BattleOutcome battleOutcome, boolean isAttacker) {
        if (aiCommanderExperienceService == null || fleet == null) {
            return;
        }
        // Determine the outcome for this fleet's side
        com.yisy.modules.doramcu.eve.model.AiCommanderExperience.Outcome outcome;
        if (battleOutcome == Battle.BattleOutcome.ATTACKER_VICTORY) {
            outcome = isAttacker
                ? com.yisy.modules.doramcu.eve.model.AiCommanderExperience.Outcome.VICTORY
                : com.yisy.modules.doramcu.eve.model.AiCommanderExperience.Outcome.DEFEAT;
        } else if (battleOutcome == Battle.BattleOutcome.DEFENDER_VICTORY) {
            outcome = isAttacker
                ? com.yisy.modules.doramcu.eve.model.AiCommanderExperience.Outcome.DEFEAT
                : com.yisy.modules.doramcu.eve.model.AiCommanderExperience.Outcome.VICTORY;
        } else {
            outcome = com.yisy.modules.doramcu.eve.model.AiCommanderExperience.Outcome.STALEMATE;
        }
        // Record experience for each surviving ship's AI character
        for (Ship ship : fleet.getShips()) {
            if (ship.isDestroyed()) {
                continue;
            }
            try {
                Optional<AiCharacter> aiCharOpt = aiEntityRegistrationService.findShipAiCharacter(ship.getId());
                aiCharOpt.ifPresent(aiChar -> {
                    try {
                        aiCommanderExperienceService.recordBattleExperience(
                            aiChar.getId(),
                            "TRAVELLER",
                            "Naval Battle",
                            outcome,
                            "Naval battle participation",
                            null
                        );
                        // Advance training level if XP threshold is reached
                        aiCommanderExperienceService.advanceTrainingLevel(aiChar.getId());
                    } catch (Exception e) {
                        // Silently ignore if experience recording fails
                    }
                });
            } catch (Exception e) {
                // Silently ignore if AI character lookup fails
            }
        }
    }

    private void removeFleetFromGame(Game game, Fleet fleet, Player owner) {
        if (owner != null) {
            owner.getOwnedFleetIds().remove(fleet.getId());
        }

        if (owner != null) {
            Integer removeEmpireId = owner.getEmpireId();
            if (removeEmpireId != null && removeEmpireId > 0) {
                Empire empire = game.getEmpire(removeEmpireId);
                if (empire != null) {
                    empire.getNavy().removeFleet(fleet);
                }
            }
        }

        game.removeFleet(fleet.getId());
    }

    private void captureDefenderPlanets(Game game, GameSession session,
                                        Player attackerPlayer, Player defenderPlayer, Battle battle) {
        if (defenderPlayer == null || defenderPlayer.getOwnedPlanetIds().isEmpty()) {
            return;
        }

        Fleet defender = battle.getDefender();
        List<Integer> planetsToCapture = new ArrayList<>();

        if (defender.getHomeSystem() != null) {
            for (Integer planetId : defenderPlayer.getOwnedPlanetIds()) {
                if (defenderPlayer.getHomePlanetId() != null && defenderPlayer.getHomePlanetId().equals(planetId)) {
                    continue;
                }
                planetsToCapture.add(planetId);
            }
        }

        if (planetsToCapture.isEmpty()) {
            for (Integer planetId : defenderPlayer.getOwnedPlanetIds()) {
                if (defenderPlayer.getHomePlanetId() != null && defenderPlayer.getHomePlanetId().equals(planetId)) {
                    continue;
                }
                planetsToCapture.add(planetId);
                break;
            }
        }

        for (Integer planetId : planetsToCapture) {
            Planet planet = game.getPlanet(planetId);
            if (planet == null) continue;

            log.info("Planet {} captured by {} from {}", planet.getName(),
                    attackerPlayer.getName(), defenderPlayer.getName());

            game.transferPlanet(planetId, attackerPlayer);

            Integer attackerEmpireId = attackerPlayer.getEmpireId();
            if (attackerEmpireId != null) {
                Empire attackerEmpire = game.getEmpire(attackerEmpireId);
                if (attackerEmpire != null) {
                    attackerEmpire.claimPlanet(planetId);
                }
            }

            Integer defenderEmpireId = defenderPlayer.getEmpireId();
            if (defenderEmpireId != null) {
                Empire defenderEmpire = game.getEmpire(defenderEmpireId);
                if (defenderEmpire != null) {
                    defenderEmpire.releasePlanet(planetId);
                }
            }
        }
    }

    private void checkPlayerElimination(Game game, GameSession session, Player player) {
        if (player == null || player.isEliminated()) return;

        boolean hasFleets = !player.getOwnedFleetIds().isEmpty();
        boolean hasPlanets = !player.getOwnedPlanetIds().isEmpty();

        if (!hasFleets && !hasPlanets) {
            player.eliminate();
            log.warn("Player {} has been eliminated! No fleets or planets remaining.", player.getName());
        }
    }

    // ---- Helpers ----

    private Player findPlayerBySessionId(Game game, GameSession session, Long playerId) {
        if (!session.getPlayerIds().contains(playerId)) {
            return null;
        }
        for (Player player : game.getPlayers()) {
            if (player.getId() == playerId.intValue()) {
                return player;
            }
        }
        return null;
    }

    private GameStateResponse getGameStateInternal(GameSession session, Long playerId) {
        return gameStateBuilder.buildGameStateResponse(session, playerId, true);
    }
}
