package com.yisy.modules.doramcu.hex.planet;
import org.springframework.stereotype.Service;

import com.yisy.modules.doramcu.gamecore.DungeonRoomService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * In-memory implementation of {@link DungeonRoomService} for use when the
 * full Spring application context is not available (e.g., tests, standalone
 * planet generation).
 * <p>
 * This mock stores rooms in memory and supports all the operations needed
 * by {@link BuildingInteriorGenerator} to create interior rooms with connections,
 * traps, and treasure.
 * </p>
 */
@Component
public class MockDungeonRoomService implements DungeonRoomService {

    private final Map<Long, DungeonRoom> rooms = new HashMap<>();
    private final Map<String, List<Long>> roomsByDungeon = new HashMap<>();
    private final Map<Long, List<Long>> connections = new HashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);

    @Override
    public DungeonRoom createRoom(DungeonRoom room) {
        Long id = nextId.getAndIncrement();
        room.setId(id);
        rooms.put(id, room);
        roomsByDungeon.computeIfAbsent(room.getDungeonId(), k -> new ArrayList<>()).add(id);
        return room;
    }

    @Override
    public DungeonRoom getRoom(Long id) {
        return rooms.get(id);
    }

    @Override
    public List<DungeonRoom> listRoomsByDungeon(String dungeonId) {
        List<Long> ids = roomsByDungeon.getOrDefault(dungeonId, Collections.emptyList());
        List<DungeonRoom> result = new ArrayList<>();
        for (Long id : ids) {
            DungeonRoom room = rooms.get(id);
            if (room != null) result.add(room);
        }
        return result;
    }

    @Override
    public List<DungeonRoom> listRoomsByTheme(String theme) {
        List<DungeonRoom> result = new ArrayList<>();
        for (DungeonRoom room : rooms.values()) {
            if (theme.equals(room.getTheme())) {
                result.add(room);
            }
        }
        return result;
    }

    @Override
    public DungeonRoom discoverRoom(Long id) {
        DungeonRoom room = rooms.get(id);
        if (room != null) {
            room.setDiscovered(true);
        }
        return room;
    }

    @Override
    public void addConnection(Long fromId, Long toId) {
        connections.computeIfAbsent(fromId, k -> new ArrayList<>()).add(toId);
    }

    @Override
    public List<DungeonRoom> getConnectedRooms(Long roomId) {
        List<Long> connectedIds = connections.getOrDefault(roomId, new ArrayList<>());
        List<DungeonRoom> result = new ArrayList<>();
        for (Long id : connectedIds) {
            DungeonRoom room = rooms.get(id);
            if (room != null) result.add(room);
        }
        return result;
    }

    @Override
    public void createCircularConnections(String dungeonId) {
        List<Long> ids = roomsByDungeon.getOrDefault(dungeonId, new ArrayList<>());
        for (int i = 0; i < ids.size(); i++) {
            Long from = ids.get(i);
            Long to = ids.get((i + 1) % ids.size());
            connections.computeIfAbsent(from, k -> new ArrayList<>()).add(to);
        }
    }

    @Override
    public TrapCheckResult makeTrapCheck(Long roomId, int dexModifier, int proficiencyBonus, int roll) {
        DungeonRoom room = rooms.get(roomId);
        if (room == null || room.getTrap() == null) {
            return new TrapCheckResult(roomId, "Unknown", 10, true, false, 1, "None", "0");
        }
        int total = roll + dexModifier + proficiencyBonus;
        int dc = room.getTrapDc() != null ? room.getTrapDc() : 10;
        boolean success = total >= dc;
        boolean criticalFailure = roll == 1;
        int multiplier = criticalFailure ? 2 : (success ? 0 : 1);
        return new TrapCheckResult(roomId, room.getName(), dc, success, criticalFailure, multiplier,
                "Trap", room.getTrapDamageExpr() != null ? room.getTrapDamageExpr() : "1d6");
    }

    @Override
    public TreasureResult discoverTreasure(Long roomId) {
        DungeonRoom room = rooms.get(roomId);
        if (room == null || room.getTreasure() == null) {
            return new TreasureResult(roomId, "Unknown", null, 0, null);
        }
        return new TreasureResult(roomId, room.getName(),
                room.getTreasure(),
                room.getGoldPieces() != null ? room.getGoldPieces() : 0,
                room.getMagicRarity());
    }
}
