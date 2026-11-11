package com.yisy.modules.doramcu.dnd5e.service;

import com.yisy.modules.doramcu.dnd5e.model.Dnd5eDungeonRoom;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5eDungeonRoomRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Service for managing D&D 5e Dungeon Rooms.
 * <p>
 * Provides CRUD operations for dungeon rooms, room generation for dungeons,
 * circular connection management, trap checks, treasure discovery, and encounter
 * resolution. Supports thematic room design with non-linear exploration paths.
 * </p>
 */
@Service
public class Dnd5eDungeonRoomService {

    private static final Logger log = LoggerFactory.getLogger(Dnd5eDungeonRoomService.class);

    private final Dnd5eDungeonRoomRepository roomRepository;

    public Dnd5eDungeonRoomService(Dnd5eDungeonRoomRepository roomRepository) {
        this.roomRepository = roomRepository;
        initializeRoomDatabase();
    }

    // ==================== Room CRUD ====================

    @Transactional
    public Dnd5eDungeonRoom createRoom(Dnd5eDungeonRoom room) {
        log.info("Creating dungeon room: {}", room.getName());
        return roomRepository.save(room);
    }

    public Optional<Dnd5eDungeonRoom> getRoom(Long id) {
        return roomRepository.findById(id);
    }

    public List<Dnd5eDungeonRoom> listRooms() {
        return roomRepository.findAll();
    }

    public List<Dnd5eDungeonRoom> listActiveRooms() {
        return roomRepository.findByActiveTrue();
    }

    public List<Dnd5eDungeonRoom> listRoomsByDungeon(String dungeonId) {
        return roomRepository.findByDungeonIdAndActiveTrue(dungeonId);
    }

    public List<Dnd5eDungeonRoom> listRoomsByTheme(String theme) {
        return roomRepository.findByThemeAndActiveTrue(theme);
    }

    public List<Dnd5eDungeonRoom> listRoomsByLevel(int level) {
        return roomRepository.findByCharacterLevel(level);
    }

    public List<Dnd5eDungeonRoom> listRoomsByDungeonAndLevel(String dungeonId, int level) {
        return roomRepository.findByDungeonAndLevel(dungeonId, level);
    }

    public List<Dnd5eDungeonRoom> listDiscoveredRooms() {
        return roomRepository.findByDiscoveredTrue();
    }

    public List<Dnd5eDungeonRoom> listUndiscoveredRooms() {
        return roomRepository.findByDiscoveredFalse();
    }

    public List<Dnd5eDungeonRoom> listDiscoveredRoomsByDungeon(String dungeonId) {
        return roomRepository.findByDungeonIdAndDiscoveredTrue(dungeonId);
    }

    public List<Dnd5eDungeonRoom> listUndiscoveredRoomsByDungeon(String dungeonId) {
        return roomRepository.findByDungeonIdAndDiscoveredFalse(dungeonId);
    }

    public List<Dnd5eDungeonRoom> listRoomsTraps() {
        return roomRepository.findRoomsTraps();
    }

    public List<Dnd5eDungeonRoom> listRoomsTreasure() {
        return roomRepository.findRoomsTreasure();
    }

    public List<Dnd5eDungeonRoom> listRoomsEncounters() {
        return roomRepository.findRoomsEncounters();
    }

    public List<Dnd5eDungeonRoom> searchRooms(String keyword) {
        return roomRepository.searchByKeyword(keyword);
    }

    @Transactional
    public Dnd5eDungeonRoom updateRoom(Long id, Dnd5eDungeonRoom updates) {
        return roomRepository.findById(id)
                .map(room -> {
                    if (updates.getName() != null) room.setName(updates.getName());
                    if (updates.getDescription() != null) room.setDescription(updates.getDescription());
                    if (updates.getDungeonId() != null) room.setDungeonId(updates.getDungeonId());
                    if (updates.getTheme() != null) room.setTheme(updates.getTheme());
                    if (updates.getSize() != null) room.setSize(updates.getSize());
                    if (updates.getConnections() != null) room.setConnections(updates.getConnections());
                    if (updates.getTrap() != null) room.setTrap(updates.getTrap());
                    if (updates.getTrapDc() != null) room.setTrapDc(updates.getTrapDc());
                    if (updates.getTrapDamage() != null) room.setTrapDamage(updates.getTrapDamage());
                    if (updates.getTrapDamageExpr() != null) room.setTrapDamageExpr(updates.getTrapDamageExpr());
                    if (updates.getTreasure() != null) room.setTreasure(updates.getTreasure());
                    room.setGoldPieces(updates.getGoldPieces());
                    if (updates.getMagicRarity() != null) room.setMagicRarity(updates.getMagicRarity());
                    if (updates.getEncounter() != null) room.setEncounter(updates.getEncounter());
                    if (updates.getEncounterDifficulty() != null) room.setEncounterDifficulty(updates.getEncounterDifficulty());
                    room.setXpReward(updates.getXpReward());
                    room.setMinLevel(updates.getMinLevel());
                    room.setMaxLevel(updates.getMaxLevel());
                    room.setDiscovered(updates.isDiscovered());
                    room.setActive(updates.isActive());
                    if (updates.getDmSecrets() != null) room.setDmSecrets(updates.getDmSecrets());
                    return roomRepository.save(room);
                })
                .orElseThrow(() -> new RuntimeException("Dungeon room not found with id: " + id));
    }

    @Transactional
    public boolean deleteRoom(Long id) {
        return roomRepository.findById(id)
                .map(room -> {
                    roomRepository.delete(room);
                    log.info("Deleted dungeon room: {}", room.getName());
                    return true;
                })
                .orElse(false);
    }

    @Transactional
    public void toggleRoomActive(Long id) {
        roomRepository.findById(id)
                .ifPresent(room -> {
                    room.setActive(!room.isActive());
                    roomRepository.save(room);
                });
    }

    // ==================== Discovery ====================

    @Transactional
    public Dnd5eDungeonRoom discoverRoom(Long id) {
        return roomRepository.findById(id)
                .map(room -> {
                    room.setDiscovered(true);
                    log.info("Discovered room: {}", room.getName());
                    return roomRepository.save(room);
                })
                .orElseThrow(() -> new RuntimeException("Dungeon room not found with id: " + id));
    }

    @Transactional
    public void undiscoverRoom(Long id) {
        roomRepository.findById(id)
                .ifPresent(room -> {
                    room.setDiscovered(false);
                    roomRepository.save(room);
                });
    }

    @Transactional
    public void discoverAllInDungeon(String dungeonId) {
        roomRepository.findByDungeonId(dungeonId).forEach(room -> {
            room.setDiscovered(true);
            roomRepository.save(room);
        });
    }

    @Transactional
    public void undiscoverAllInDungeon(String dungeonId) {
        roomRepository.findByDungeonId(dungeonId).forEach(room -> {
            room.setDiscovered(false);
            roomRepository.save(room);
        });
    }

    // ==================== Connections ====================

    @Transactional
    public void addConnection(Long fromId, Long toId) {
        Optional<Dnd5eDungeonRoom> fromOpt = roomRepository.findById(fromId);
        Optional<Dnd5eDungeonRoom> toOpt = roomRepository.findById(toId);

        if (fromOpt.isEmpty() || toOpt.isEmpty()) {
            throw new RuntimeException("One or both rooms not found");
        }

        Dnd5eDungeonRoom fromRoom = fromOpt.get();
        Dnd5eDungeonRoom toRoom = toOpt.get();

        List<Long> fromConnections = parseConnections(fromRoom.getConnections());
        if (!fromConnections.contains(toId)) {
            fromConnections.add(toId);
            fromRoom.setConnections(serializeConnections(fromConnections));
            roomRepository.save(fromRoom);
        }

        List<Long> toConnections = parseConnections(toRoom.getConnections());
        if (!toConnections.contains(fromId)) {
            toConnections.add(fromId);
            toRoom.setConnections(serializeConnections(toConnections));
            roomRepository.save(toRoom);
        }
    }

    @Transactional
    public void removeConnection(Long fromId, Long toId) {
        Optional<Dnd5eDungeonRoom> fromOpt = roomRepository.findById(fromId);
        Optional<Dnd5eDungeonRoom> toOpt = roomRepository.findById(toId);

        if (fromOpt.isEmpty() || toOpt.isEmpty()) {
            throw new RuntimeException("One or both rooms not found");
        }

        Dnd5eDungeonRoom fromRoom = fromOpt.get();
        Dnd5eDungeonRoom toRoom = toOpt.get();

        List<Long> fromConnections = parseConnections(fromRoom.getConnections());
        fromConnections.remove(toId);
        fromRoom.setConnections(serializeConnections(fromConnections));
        roomRepository.save(fromRoom);

        List<Long> toConnections = parseConnections(toRoom.getConnections());
        toConnections.remove(fromId);
        toRoom.setConnections(serializeConnections(toConnections));
        roomRepository.save(toRoom);
    }

    public List<Dnd5eDungeonRoom> getConnectedRooms(Long roomId) {
        return roomRepository.findById(roomId)
                .map(room -> {
                    List<Long> connectionIds = parseConnections(room.getConnections());
                    List<Dnd5eDungeonRoom> connected = new ArrayList<>();
                    for (Long connId : connectionIds) {
                        roomRepository.findById(connId).ifPresent(connected::add);
                    }
                    return connected;
                })
                .orElse(Collections.emptyList());
    }

    @Transactional
    public void createCircularConnections(String dungeonId) {
        List<Dnd5eDungeonRoom> rooms = roomRepository.findByDungeonId(dungeonId);
        if (rooms.size() < 2) {
            log.warn("Not enough rooms in dungeon {} for circular connections", dungeonId);
            return;
        }

        rooms.forEach(room -> {
            room.setConnections("[]");
            roomRepository.save(room);
        });

        for (int i = 0; i < rooms.size(); i++) {
            Dnd5eDungeonRoom current = rooms.get(i);
            Dnd5eDungeonRoom next = rooms.get((i + 1) % rooms.size());
            addConnection(current.getId(), next.getId());
        }

        Random random = new Random();
        for (int i = 0; i < rooms.size(); i += 3) {
            int targetIndex;
            do {
                targetIndex = random.nextInt(rooms.size());
            } while (targetIndex == i || targetIndex == (i + 1) % rooms.size());
            addConnection(rooms.get(i).getId(), rooms.get(targetIndex).getId());
        }

        log.info("Created circular connections for dungeon {} with {} rooms", dungeonId, rooms.size());
    }

    // ==================== Trap Checks ====================

    public TrapCheckResult makeTrapCheck(Long roomId, int dexModifier, int proficiencyBonus, int roll) {
        Dnd5eDungeonRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Dungeon room not found with id: " + roomId));

        if (room.getTrap() == null || room.getTrapDc() == null) {
            return new TrapCheckResult(roomId, room.getName(), null, false, false, 0, null, null);
        }

        int total = roll + dexModifier + proficiencyBonus;
        boolean success = total >= room.getTrapDc();
        boolean criticalFailure = roll == 1;
        boolean criticalSuccess = roll == 20;

        int damageMultiplier = 0;
        String damageType = null;

        if (criticalFailure) {
            damageMultiplier = 2;
            damageType = room.getTrapDamage();
        } else if (!success && !criticalSuccess) {
            damageMultiplier = 1;
            damageType = room.getTrapDamage();
        }

        return new TrapCheckResult(roomId, room.getName(), room.getTrapDc(),
                success || criticalSuccess, criticalFailure, damageMultiplier,
                damageType, room.getTrapDamageExpr());
    }

    // ==================== Treasure Discovery ====================

    public TreasureResult discoverTreasure(Long roomId) {
        Dnd5eDungeonRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Dungeon room not found with id: " + roomId));

        return new TreasureResult(roomId, room.getName(), room.getTreasure(),
                room.getGoldPieces(), room.getMagicRarity());
    }

    // ==================== Reference Data ====================

    public List<String> listDungeonIds() {
        return roomRepository.findDistinctDungeonIds();
    }

    public List<String> listThemes() {
        return roomRepository.findDistinctThemes();
    }

    // ==================== Helper Methods ====================

    private List<Long> parseConnections(String connectionsJson) {
        if (connectionsJson == null || connectionsJson.isEmpty() || connectionsJson.equals("[]")) {
            return new ArrayList<>();
        }
        String cleaned = connectionsJson.replaceAll("[\\[\\] ]", "");
        if (cleaned.isEmpty()) {
            return new ArrayList<>();
        }
        String[] parts = cleaned.split(",");
        List<Long> result = new ArrayList<>();
        for (String part : parts) {
            try {
                result.add(Long.parseLong(part.trim()));
            } catch (NumberFormatException e) {
                // Skip invalid entries
            }
        }
        return result;
    }

    private String serializeConnections(List<Long> connections) {
        if (connections.isEmpty()) {
            return "[]";
        }
        return "[" + String.join(", ", connections.stream().map(String::valueOf).toArray(String[]::new)) + "]";
    }

    // ==================== Database Initialization ====================

    @Transactional
    void initializeRoomDatabase() {
        if (roomRepository.count() > 0) {
            log.info("Dungeon rooms already populated, skipping initialization");
            return;
        }
        log.info("Initializing dungeon room database with example rooms");
        populateExampleRooms();
    }

    @Transactional
    void populateExampleRooms() {
        String cryptDungeon = "forgotten_crypt";

        Dnd5eDungeonRoom entrance = new Dnd5eDungeonRoom();
        entrance.setName("Crypt Entrance");
        entrance.setDescription("A crumbling stone archway leads into darkness. The air is cold and smells of decay.");
        entrance.setDungeonId(cryptDungeon);
        entrance.setTheme("Entrance");
        entrance.setSize("Large");
        entrance.setMinLevel(1);
        entrance.setMaxLevel(5);
        entrance.setEncounter("2 Skeleton Warriors guard the entrance");
        entrance.setEncounterDifficulty("Easy");
        entrance.setXpReward(50);
        entrance.setDmSecrets("The skeletons rise when characters pass through the archway.");
        roomRepository.save(entrance);

        Dnd5eDungeonRoom trapCorridor = new Dnd5eDungeonRoom();
        trapCorridor.setName("Trap-Lined Corridor");
        trapCorridor.setDescription("A narrow passage with pressure plates barely visible in the dim light.");
        trapCorridor.setDungeonId(cryptDungeon);
        trapCorridor.setTheme("Trap");
        trapCorridor.setSize("Small");
        trapCorridor.setMinLevel(1);
        trapCorridor.setMaxLevel(5);
        trapCorridor.setTrap("Poison darts fire from hidden slits in the walls");
        trapCorridor.setTrapDc(13);
        trapCorridor.setTrapDamage("poison");
        trapCorridor.setTrapDamageExpr("2d4");
        trapCorridor.setDmSecrets("Investigation DC 15 to spot the pressure plates.");
        roomRepository.save(trapCorridor);

        Dnd5eDungeonRoom treasureVault = new Dnd5eDungeonRoom();
        treasureVault.setName("Burial Vault");
        treasureVault.setDescription("An ornate chamber with sarcophagi lining the walls.");
        treasureVault.setDungeonId(cryptDungeon);
        treasureVault.setTheme("Treasure");
        treasureVault.setSize("Medium");
        treasureVault.setMinLevel(1);
        treasureVault.setMaxLevel(5);
        treasureVault.setTreasure("A jeweled chalice and a pouch of gold coins");
        treasureVault.setGoldPieces(150);
        treasureVault.setMagicRarity("Uncommon");
        treasureVault.setDmSecrets("The chalice is a Chalice of Atonement.");
        roomRepository.save(treasureVault);

        Dnd5eDungeonRoom puzzleRoom = new Dnd5eDungeonRoom();
        puzzleRoom.setName("Riddle Chamber");
        puzzleRoom.setDescription("A circular room with four stone statues.");
        puzzleRoom.setDungeonId(cryptDungeon);
        puzzleRoom.setTheme("Puzzle");
        puzzleRoom.setSize("Medium");
        puzzleRoom.setMinLevel(1);
        puzzleRoom.setMaxLevel(5);
        puzzleRoom.setDmSecrets("Arcana DC 14 to understand the riddle.");
        roomRepository.save(puzzleRoom);

        Dnd5eDungeonRoom bossRoom = new Dnd5eDungeonRoom();
        bossRoom.setName("Lich Sanctum");
        bossRoom.setDescription("A grand chamber filled with necromantic energy.");
        bossRoom.setDungeonId(cryptDungeon);
        bossRoom.setTheme("Boss");
        bossRoom.setSize("Huge");
        bossRoom.setMinLevel(3);
        bossRoom.setMaxLevel(5);
        bossRoom.setEncounter("A Lich with 4 Skeleton Warriors");
        bossRoom.setEncounterDifficulty("Deadly");
        bossRoom.setXpReward(5900);
        bossRoom.setTreasure("The lich phylactery and a staff of necromancy");
        bossRoom.setMagicRarity("Very Rare");
        bossRoom.setDmSecrets("The phylactery is hidden in the Burial Vault.");
        roomRepository.save(bossRoom);

        String feyDungeon = "feywild_grove";

        Dnd5eDungeonRoom feyEntrance = new Dnd5eDungeonRoom();
        feyEntrance.setName("Grove Entrance");
        feyEntrance.setDescription("A shimmering portal leads into a beautiful forest.");
        feyEntrance.setDungeonId(feyDungeon);
        feyEntrance.setTheme("Entrance");
        feyEntrance.setSize("Huge");
        feyEntrance.setMinLevel(5);
        feyEntrance.setMaxLevel(10);
        feyEntrance.setDmSecrets("Time flows differently here.");
        roomRepository.save(feyEntrance);

        Dnd5eDungeonRoom feyPuzzle = new Dnd5eDungeonRoom();
        feyPuzzle.setName("Tricksters Game");
        feyPuzzle.setDescription("A mischievous sprite challenges the party to riddles.");
        feyPuzzle.setDungeonId(feyDungeon);
        feyPuzzle.setTheme("Puzzle");
        feyPuzzle.setSize("Small");
        feyPuzzle.setMinLevel(5);
        feyPuzzle.setMaxLevel(10);
        feyPuzzle.setDmSecrets("Three riddles with boons and curses.");
        roomRepository.save(feyPuzzle);

        Dnd5eDungeonRoom feySocial = new Dnd5eDungeonRoom();
        feySocial.setName("Archfey Court");
        feySocial.setDescription("A magnificent court where fey creatures dance.");
        feySocial.setDungeonId(feyDungeon);
        feySocial.setTheme("Social");
        feySocial.setSize("Huge");
        feySocial.setMinLevel(5);
        feySocial.setMaxLevel(10);
        feySocial.setEncounter("Diplomatic encounter with an Archfey");
        feySocial.setEncounterDifficulty("Medium");
        feySocial.setXpReward(450);
        feySocial.setDmSecrets("The Archfey seeks help against a rival.");
        roomRepository.save(feySocial);

        Dnd5eDungeonRoom feyTreasure = new Dnd5eDungeonRoom();
        feyTreasure.setName("Enchanted Spring");
        feyTreasure.setDescription("A crystal-clear spring surrounded by singing flowers.");
        feyTreasure.setDungeonId(feyDungeon);
        feyTreasure.setTheme("Treasure");
        feyTreasure.setSize("Medium");
        feyTreasure.setMinLevel(5);
        feyTreasure.setMaxLevel(10);
        feyTreasure.setTreasure("A vial of enchanted water");
        feyTreasure.setGoldPieces(0);
        feyTreasure.setMagicRarity("Rare");
        feyTreasure.setDmSecrets("The water grants temporary HP.");
        roomRepository.save(feyTreasure);

        String dragonDungeon = "dragons_lair";

        Dnd5eDungeonRoom dragonEntrance = new Dnd5eDungeonRoom();
        dragonEntrance.setName("Mountain Pass");
        dragonEntrance.setDescription("A treacherous mountain path to a massive cave.");
        dragonEntrance.setDungeonId(dragonDungeon);
        dragonEntrance.setTheme("Entrance");
        dragonEntrance.setSize("Huge");
        dragonEntrance.setMinLevel(10);
        dragonEntrance.setMaxLevel(15);
        dragonEntrance.setTrap("Avalanche triggered by pressure plates");
        dragonEntrance.setTrapDc(18);
        dragonEntrance.setTrapDamage("bludgeoning");
        dragonEntrance.setTrapDamageExpr("10d6");
        dragonEntrance.setDmSecrets("Athletics DC 15 to climb to safety.");
        roomRepository.save(dragonEntrance);

        Dnd5eDungeonRoom dragonLore = new Dnd5eDungeonRoom();
        dragonLore.setName("Ancient Library");
        dragonLore.setDescription("A vast library filled with ancient tomes.");
        dragonLore.setDungeonId(dragonDungeon);
        dragonLore.setTheme("Lore");
        dragonLore.setSize("Huge");
        dragonLore.setMinLevel(10);
        dragonLore.setMaxLevel(15);
        dragonLore.setTreasure("Ancient spell scrolls");
        dragonLore.setMagicRarity("Rare");
        dragonLore.setDmSecrets("Books contain dragon weaknesses.");
        roomRepository.save(dragonLore);

        Dnd5eDungeonRoom dragonRest = new Dnd5eDungeonRoom();
        dragonRest.setName("Hidden Sanctuary");
        dragonRest.setDescription("A small hidden chamber with a protective ward.");
        dragonRest.setDungeonId(dragonDungeon);
        dragonRest.setTheme("Rest");
        dragonRest.setSize("Small");
        dragonRest.setMinLevel(10);
        dragonRest.setMaxLevel(15);
        dragonRest.setDmSecrets("Safe from the dragon senses.");
        roomRepository.save(dragonRest);

        Dnd5eDungeonRoom dragonBoss = new Dnd5eDungeonRoom();
        dragonBoss.setName("Dragons Hoard");
        dragonBoss.setDescription("A cavern filled with mountains of gold and gems.");
        dragonBoss.setDungeonId(dragonDungeon);
        dragonBoss.setTheme("Boss");
        dragonBoss.setSize("Huge");
        dragonBoss.setMinLevel(12);
        dragonBoss.setMaxLevel(15);
        dragonBoss.setEncounter("Ancient Red Dragon CR 24");
        dragonBoss.setEncounterDifficulty("Deadly");
        dragonBoss.setXpReward(39000);
        dragonBoss.setTreasure("Mountains of gold and legendary items");
        dragonBoss.setGoldPieces(50000);
        dragonBoss.setMagicRarity("Legendary");
        dragonBoss.setDmSecrets("The dragon has lair actions.");
        roomRepository.save(dragonBoss);

        createCircularConnections(cryptDungeon);
        createCircularConnections(feyDungeon);
        createCircularConnections(dragonDungeon);

        log.info("Populated {} example dungeon rooms across 3 dungeons", roomRepository.count());
    }

    // ==================== DTOs ====================

    public static class TrapCheckResult {
        private final Long roomId;
        private final String roomName;
        private final Integer dc;
        private final boolean success;
        private final boolean criticalFailure;
        private final int damageMultiplier;
        private final String damageType;
        private final String damageExpr;

        public TrapCheckResult(Long roomId, String roomName, Integer dc, boolean success,
                               boolean criticalFailure, int damageMultiplier,
                               String damageType, String damageExpr) {
            this.roomId = roomId;
            this.roomName = roomName;
            this.dc = dc;
            this.success = success;
            this.criticalFailure = criticalFailure;
            this.damageMultiplier = damageMultiplier;
            this.damageType = damageType;
            this.damageExpr = damageExpr;
        }

        public Long getRoomId() { return roomId; }
        public String getRoomName() { return roomName; }
        public Integer getDc() { return dc; }
        public boolean isSuccess() { return success; }
        public boolean isCriticalFailure() { return criticalFailure; }
        public int getDamageMultiplier() { return damageMultiplier; }
        public String getDamageType() { return damageType; }
        public String getDamageExpr() { return damageExpr; }
    }

    public static class TreasureResult {
        private final Long roomId;
        private final String roomName;
        private final String treasure;
        private final int goldPieces;
        private final String magicRarity;

        public TreasureResult(Long roomId, String roomName, String treasure,
                              int goldPieces, String magicRarity) {
            this.roomId = roomId;
            this.roomName = roomName;
            this.treasure = treasure;
            this.goldPieces = goldPieces;
            this.magicRarity = magicRarity;
        }

        public Long getRoomId() { return roomId; }
        public String getRoomName() { return roomName; }
        public String getTreasure() { return treasure; }
        public int getGoldPieces() { return goldPieces; }
        public String getMagicRarity() { return magicRarity; }
    }
}
