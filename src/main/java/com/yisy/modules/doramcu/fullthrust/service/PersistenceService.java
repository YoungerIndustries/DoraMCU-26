package com.yisy.modules.doramcu.fullthrust.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service that handles persisting and loading game sessions to/from the database.
 * Converts between domain models and JPA entities.
 */
@Service
public class PersistenceService {

    private final GameSessionRepository gameSessionRepository;
    private final PlayerRepository playerRepository;
    private final ShipRepository shipRepository;
    private final ShipClassRepository shipClassRepository;
    private final MissileRepository missileRepository;
    private final EventLogRepository eventLogRepository;
    private final BoardObstacleRepository boardObstacleRepository;
    private final WeaponRepository weaponRepository;

    public PersistenceService(GameSessionRepository gameSessionRepository,
                              PlayerRepository playerRepository,
                              ShipRepository shipRepository,
                              ShipClassRepository shipClassRepository,
                              MissileRepository missileRepository,
                              EventLogRepository eventLogRepository,
                              BoardObstacleRepository boardObstacleRepository,
                              WeaponRepository weaponRepository) {
        this.gameSessionRepository = gameSessionRepository;
        this.playerRepository = playerRepository;
        this.shipRepository = shipRepository;
        this.shipClassRepository = shipClassRepository;
        this.missileRepository = missileRepository;
        this.eventLogRepository = eventLogRepository;
        this.boardObstacleRepository = boardObstacleRepository;
        this.weaponRepository = weaponRepository;
    }

    /**
     * Save a complete game session to the database.
     */
    @Transactional
    public void saveSession(GameSession session) {
        // Save game session
        GameSessionEntity sessionEntity = toSessionEntity(session);
        sessionEntity = gameSessionRepository.save(sessionEntity);

        // Save ship classes
        for (ShipClass shipClass : session.getShipClasses().values()) {
            ShipClassEntity scEntity = toShipClassEntity(shipClass, sessionEntity);
            shipClassRepository.save(scEntity);
        }

        // Save players
        for (Player player : session.getPlayers().values()) {
            PlayerEntity pEntity = toPlayerEntity(player, sessionEntity);
            playerRepository.save(pEntity);
        }

        // Save ships and their weapons
        for (Ship ship : session.getShips().values()) {
            ShipEntity sEntity = toShipEntity(ship, sessionEntity);
            shipRepository.save(sEntity);

            // Delete old weapons for this ship and save new ones
            weaponRepository.deleteByShipId(ship.getId());
            for (Weapon weapon : ship.getWeapons()) {
                WeaponEntity wEntity = toWeaponEntity(weapon, sEntity);
                wEntity.setShipId(sEntity.getId());
                wEntity.setShip(sEntity);
                weaponRepository.save(wEntity);
            }
        }

        // Save missiles
        for (Missile missile : session.getActiveMissiles()) {
            MissileEntity mEntity = toMissileEntity(missile, sessionEntity);
            missileRepository.save(mEntity);
        }

        // Save event log
        saveEventLog(session, sessionEntity);

        // Save board obstacles
        saveBoardObstacles(session, sessionEntity);
    }

    /**
     * Load a complete game session from the database.
     */
    @Transactional
    public Optional<GameSession> loadSession(String sessionId) {
        GameSessionEntity sessionEntity = gameSessionRepository.findById(sessionId).orElse(null);
        if (sessionEntity == null) {
            return Optional.empty();
        }

        GameSession session = fromSessionEntity(sessionEntity);

        // Load ship classes
        List<ShipClassEntity> scEntities = shipClassRepository.findByGameSessionId(sessionId);
        for (ShipClassEntity scEntity : scEntities) {
            session.addShipClass(fromShipClassEntity(scEntity));
        }

        // Load players
        List<PlayerEntity> pEntities = playerRepository.findByGameSessionId(sessionId);
        for (PlayerEntity pEntity : pEntities) {
            session.addPlayer(fromPlayerEntity(pEntity));
        }

        // Load ships and their weapons
        List<ShipEntity> sEntities = shipRepository.findByGameSessionId(sessionId);
        for (ShipEntity sEntity : sEntities) {
            List<WeaponEntity> wEntities = weaponRepository.findByShipId(sEntity.getId());
            sEntity.setWeapons(wEntities);
            Ship ship = fromShipEntity(sEntity);
            session.addShip(ship);
        }

        // Load missiles
        List<MissileEntity> mEntities = missileRepository.findByGameSessionId(sessionId);
        for (MissileEntity mEntity : mEntities) {
            session.getActiveMissiles().add(fromMissileEntity(mEntity));
        }

        // Load event log
        List<EventLogEntity> elEntities = eventLogRepository.findByGameSessionIdOrderByTurnNumberAsc(sessionId);
        for (EventLogEntity elEntity : elEntities) {
            session.getEventLog().add(elEntity.getMessage());
        }

        // Load board obstacles
        List<BoardObstacleEntity> obsEntities = boardObstacleRepository.findByGameSessionId(sessionId);
        for (BoardObstacleEntity obsEntity : obsEntities) {
            session.getBoard().addObstacle(obsEntity.getName(),
                    new HexPosition(obsEntity.getPositionQ(), obsEntity.getPositionR()));
        }

        return Optional.of(session);
    }

    /**
     * List all game session IDs.
     */
    @Transactional
    public List<String> listAllSessionIds() {
        return gameSessionRepository.findAll().stream()
                .map(GameSessionEntity::getId)
                .collect(Collectors.toList());
    }

    /**
     * Delete a game session and all related data.
     */
    @Transactional
    public void deleteSession(String sessionId) {
        eventLogRepository.deleteByGameSessionId(sessionId);
        missileRepository.deleteByGameSessionId(sessionId);
        boardObstacleRepository.deleteByGameSessionId(sessionId);
        // Delete weapons for all ships in this session
        List<ShipEntity> ships = shipRepository.findByGameSessionId(sessionId);
        for (ShipEntity ship : ships) {
            weaponRepository.deleteByShipId(ship.getId());
        }
        shipRepository.deleteByGameSessionId(sessionId);
        playerRepository.deleteByGameSessionId(sessionId);
        shipClassRepository.deleteByGameSessionId(sessionId);
        gameSessionRepository.deleteById(sessionId);
    }

    // --- Conversion methods: Domain -> Entity ---

    private GameSessionEntity toSessionEntity(GameSession session) {
        GameSessionEntity entity = new GameSessionEntity();
        entity.setId(session.getId());
        entity.setName(session.getName());
        entity.setCurrentTurn(session.getCurrentTurn());
        entity.setCurrentPlayerId(session.getCurrentPlayerId());
        entity.setStarted(session.isStarted());
        entity.setEnded(session.isEnded());
        entity.setWinnerId(session.getWinnerId());
        entity.setBoardWidth(session.getBoard().getWidth());
        entity.setBoardHeight(session.getBoard().getHeight());
        return entity;
    }

    private ShipClassEntity toShipClassEntity(ShipClass shipClass, GameSessionEntity sessionEntity) {
        ShipClassEntity entity = new ShipClassEntity();
        entity.setId(shipClass.getId());
        entity.setName(shipClass.getName());
        entity.setHullPoints(shipClass.getHullPoints());
        entity.setArmorValue(shipClass.getArmorValue());
        entity.setMaxSpeed(shipClass.getMaxSpeed());
        entity.setAcceleration(shipClass.getAcceleration());
        entity.setDeceleration(shipClass.getDeceleration());
        entity.setMaxHeadingChange(shipClass.getMaxHeadingChange());
        entity.setShieldPoints(shipClass.getShieldPoints());
        entity.setShieldRegeneration(shipClass.getShieldRegeneration());
        entity.setFuelCapacity(shipClass.getFuelCapacity());
        entity.setCrew(shipClass.getCrew());
        entity.setCost(shipClass.getCost());
        entity.setDescription(shipClass.getDescription());
        entity.setGameSession(sessionEntity);

        // Weapon slots
        List<WeaponSlotEntity> slots = new ArrayList<>();
        for (ShipClass.WeaponSlot slot : shipClass.getWeaponSlots()) {
            WeaponSlotEntity ws = new WeaponSlotEntity();
            ws.setName(slot.getName());
            ws.setArc(slot.getArc());
            ws.setWeaponType(slot.getWeaponType());
            ws.setShipClass(entity);
            slots.add(ws);
        }
        entity.setWeaponSlots(slots);
        return entity;
    }

    private PlayerEntity toPlayerEntity(Player player, GameSessionEntity sessionEntity) {
        PlayerEntity entity = new PlayerEntity();
        entity.setId(player.getId());
        entity.setName(player.getName());
        entity.setFaction(player.getFaction());
        entity.setCredits(player.getCredits());
        entity.setAI(player.isAI());
        entity.setAiDifficulty(player.getAiDifficulty());
        entity.setGameSession(sessionEntity);
        entity.setShipIds(new ArrayList<>(player.getShipIds()));
        return entity;
    }

    private ShipEntity toShipEntity(Ship ship, GameSessionEntity sessionEntity) {
        ShipEntity entity = new ShipEntity();
        entity.setId(ship.getId());
        entity.setName(ship.getName());
        entity.setShipClassId(ship.getShipClassId());
        entity.setPlayerId(ship.getPlayerId());
        entity.setPositionQ(ship.getPosition().getQ());
        entity.setPositionR(ship.getPosition().getR());
        entity.setHeading(ship.getHeading());
        entity.setSpeed(ship.getSpeed());
        entity.setCurrentHull(ship.getCurrentHull());
        entity.setMaxHull(ship.getMaxHull());
        entity.setCurrentShield(ship.getCurrentShield());
        entity.setMaxShield(ship.getMaxShield());
        entity.setCurrentFuel(ship.getCurrentFuel());
        entity.setMaxFuel(ship.getMaxFuel());
        entity.setArmorValue(ship.getArmorValue());
        entity.setDestroyed(ship.isDestroyed());
        entity.setGameSession(sessionEntity);
        entity.setDamagedSystems(new HashSet<>(ship.getDamagedSystems()));
        return entity;
    }

    private WeaponEntity toWeaponEntity(Weapon weapon, ShipEntity shipEntity) {
        WeaponEntity entity = new WeaponEntity();
        // Don't set ID - it's auto-generated
        entity.setName(weapon.getName());
        entity.setArc(weapon.getArc());
        entity.setType(weapon.getType());
        entity.setDamage(weapon.getDamage());
        entity.setRange(weapon.getRange());
        entity.setReloadTime(weapon.getReloadTime());
        entity.setCurrentReload(weapon.getCurrentReload());
        entity.setDisabled(weapon.isDisabled());
        return entity;
    }

    private MissileEntity toMissileEntity(Missile missile, GameSessionEntity sessionEntity) {
        MissileEntity entity = new MissileEntity();
        entity.setId(missile.getId());
        entity.setOwnerId(missile.getOwnerId());
        entity.setOwnerName(missile.getOwnerName());
        entity.setPositionQ(missile.getPosition().getQ());
        entity.setPositionR(missile.getPosition().getR());
        entity.setHeading(missile.getHeading());
        entity.setSpeed(missile.getSpeed());
        entity.setDamage(missile.getDamage());
        entity.setTurnsRemaining(missile.getTurnsRemaining());
        entity.setHit(missile.isHit());
        entity.setGameSession(sessionEntity);
        return entity;
    }

    private void saveWeapon(WeaponEntity weaponEntity) {
        weaponRepository.save(weaponEntity);
    }

    private void saveEventLog(GameSession session, GameSessionEntity sessionEntity) {
        // Clear old event log entries for this session
        eventLogRepository.deleteByGameSessionId(session.getId());

        // Parse event log entries to extract turn numbers
        for (String event : session.getEventLog()) {
            EventLogEntity entity = new EventLogEntity();
            entity.setGameSession(sessionEntity);
            entity.setMessage(event);

            // Try to extract turn number from "Turn N: message" format
            int turn = 0;
            if (event.startsWith("Turn ")) {
                try {
                    int colonIndex = event.indexOf(':');
                    if (colonIndex > 5) {
                        turn = Integer.parseInt(event.substring(5, colonIndex).trim());
                    }
                } catch (NumberFormatException ignored) {
                }
            }
            entity.setTurnNumber(turn);
            eventLogRepository.save(entity);
        }
    }

    private void saveBoardObstacles(GameSession session, GameSessionEntity sessionEntity) {
        // Clear old obstacles
        boardObstacleRepository.deleteByGameSessionId(session.getId());

        for (Map.Entry<String, HexPosition> entry : session.getBoard().getObstacles().entrySet()) {
            BoardObstacleEntity entity = new BoardObstacleEntity();
            entity.setName(entry.getKey());
            entity.setPositionQ(entry.getValue().getQ());
            entity.setPositionR(entry.getValue().getR());
            entity.setGameSession(sessionEntity);
            boardObstacleRepository.save(entity);
        }
    }

    // --- Conversion methods: Entity -> Domain ---

    private GameSession fromSessionEntity(GameSessionEntity entity) {
        GameSession session = new GameSession();
        session.setId(entity.getId());
        session.setName(entity.getName());
        session.setCurrentTurn(entity.getCurrentTurn());
        session.setCurrentPlayerId(entity.getCurrentPlayerId());
        session.setStarted(entity.isStarted());
        session.setEnded(entity.isEnded());
        session.setWinnerId(entity.getWinnerId());
        session.setBoard(new GameBoard(entity.getBoardWidth(), entity.getBoardHeight()));
        return session;
    }

    private ShipClass fromShipClassEntity(ShipClassEntity entity) {
        ShipClass shipClass = new ShipClass();
        shipClass.setId(entity.getId());
        shipClass.setName(entity.getName());
        shipClass.setHullPoints(entity.getHullPoints());
        shipClass.setArmorValue(entity.getArmorValue());
        shipClass.setMaxSpeed(entity.getMaxSpeed());
        shipClass.setAcceleration(entity.getAcceleration());
        shipClass.setDeceleration(entity.getDeceleration());
        shipClass.setMaxHeadingChange(entity.getMaxHeadingChange());
        shipClass.setShieldPoints(entity.getShieldPoints());
        shipClass.setShieldRegeneration(entity.getShieldRegeneration());
        shipClass.setFuelCapacity(entity.getFuelCapacity());
        shipClass.setCrew(entity.getCrew());
        shipClass.setCost(entity.getCost());
        shipClass.setDescription(entity.getDescription());

        for (WeaponSlotEntity ws : entity.getWeaponSlots()) {
            shipClass.addWeaponSlot(new ShipClass.WeaponSlot(ws.getName(), ws.getArc(), ws.getWeaponType()));
        }
        return shipClass;
    }

    private Player fromPlayerEntity(PlayerEntity entity) {
        Player player = new Player(entity.getId(), entity.getName(), entity.getFaction(), entity.isAI());
        player.setCredits(entity.getCredits());
        player.setAiDifficulty(entity.getAiDifficulty());
        player.setShipIds(new ArrayList<>(entity.getShipIds()));
        return player;
    }

    private Ship fromShipEntity(ShipEntity entity) {
        Ship ship = new Ship();
        ship.setId(entity.getId());
        ship.setName(entity.getName());
        ship.setShipClassId(entity.getShipClassId());
        ship.setPlayerId(entity.getPlayerId());
        ship.setPosition(new HexPosition(entity.getPositionQ(), entity.getPositionR()));
        ship.setHeading(entity.getHeading());
        ship.setSpeed(entity.getSpeed());
        ship.setCurrentHull(entity.getCurrentHull());
        ship.setMaxHull(entity.getMaxHull());
        ship.setCurrentShield(entity.getCurrentShield());
        ship.setMaxShield(entity.getMaxShield());
        ship.setCurrentFuel(entity.getCurrentFuel());
        ship.setMaxFuel(entity.getMaxFuel());
        ship.setArmorValue(entity.getArmorValue());
        ship.setDestroyed(entity.isDestroyed());
        ship.setDamagedSystems(new HashSet<>(entity.getDamagedSystems()));

        // Reconstruct weapons from saved weapon entities
        List<WeaponEntity> weaponEntities = entity.getWeapons();
        if (weaponEntities != null) {
            for (WeaponEntity wEntity : weaponEntities) {
                Weapon weapon = new Weapon();
                weapon.setId(String.valueOf(wEntity.getId()));
                weapon.setName(wEntity.getName());
                weapon.setArc(wEntity.getArc());
                weapon.setType(wEntity.getType());
                weapon.setDamage(wEntity.getDamage());
                weapon.setRange(wEntity.getRange());
                weapon.setReloadTime(wEntity.getReloadTime());
                weapon.setCurrentReload(wEntity.getCurrentReload());
                weapon.setDisabled(wEntity.isDisabled());
                ship.getWeapons().add(weapon);
            }
        }
        return ship;
    }

    private Missile fromMissileEntity(MissileEntity entity) {
        return new Missile(
                entity.getId(),
                entity.getOwnerId(),
                entity.getOwnerName(),
                new HexPosition(entity.getPositionQ(), entity.getPositionR()),
                entity.getHeading(),
                entity.getDamage(),
                entity.getTurnsRemaining()
        );
    }
}
