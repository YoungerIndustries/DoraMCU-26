package com.yisy.modules.doramcu.lord_of_the_rings.service;

import com.yisy.modules.doramcu.lord_of_the_rings.model.LotrDungeonRoom;
import com.yisy.modules.doramcu.lord_of_the_rings.repository.LotrDungeonRoomRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Service for managing LOTR Dungeon Rooms.
 * <p>
 * Dungeon rooms represent locations like Moria levels, Cirith Ungol passages,
 * and other perilous places. They have types, level ranges, and descriptions.
 * </p>
 */
@Service
public class LotrDungeonRoomService {

    private static final Logger log = LoggerFactory.getLogger(LotrDungeonRoomService.class);

    private final LotrDungeonRoomRepository dungeonRoomRepository;
    private final Random random = new Random();

    public LotrDungeonRoomService(LotrDungeonRoomRepository dungeonRoomRepository) {
        this.dungeonRoomRepository = dungeonRoomRepository;
    }

    /**
     * Get all dungeon rooms.
     */
    @Transactional(readOnly = true)
    public List<LotrDungeonRoom> findAll() {
        return dungeonRoomRepository.findAll();
    }

    /**
     * Get all active dungeon rooms.
     */
    @Transactional(readOnly = true)
    public List<LotrDungeonRoom> findActiveRooms() {
        return dungeonRoomRepository.findAll().stream()
                .filter(LotrDungeonRoom::isActive)
                .toList();
    }

    /**
     * Get dungeon room by ID.
     */
    @Transactional(readOnly = true)
    public Optional<LotrDungeonRoom> findById(Long id) {
        return dungeonRoomRepository.findById(id);
    }

    /**
     * Find dungeon rooms by name (case-insensitive partial match).
     */
    @Transactional(readOnly = true)
    public List<LotrDungeonRoom> findByName(String name) {
        return dungeonRoomRepository.findAll().stream()
                .filter(r -> r.getName().toLowerCase().contains(name.toLowerCase()))
                .toList();
    }

    /**
     * Find dungeon rooms by type.
     */
    @Transactional(readOnly = true)
    public List<LotrDungeonRoom> findByType(String type) {
        return dungeonRoomRepository.findAll().stream()
                .filter(r -> type.equals(r.getType()))
                .toList();
    }

    /**
     * Find dungeon rooms suitable for a character level.
     */
    @Transactional(readOnly = true)
    public List<LotrDungeonRoom> findByLevel(int level) {
        return dungeonRoomRepository.findAll().stream()
                .filter(r -> level >= r.getMinLevel() && level <= r.getMaxLevel() && r.isActive())
                .toList();
    }

    /**
     * Get a random dungeon room suitable for a character level.
     */
    @Transactional(readOnly = true)
    public Optional<LotrDungeonRoom> getRandomRoom(int level) {
        List<LotrDungeonRoom> rooms = findByLevel(level);
        if (rooms.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(rooms.get(random.nextInt(rooms.size())));
    }

    /**
     * Create a new dungeon room.
     */
    @Transactional
    public LotrDungeonRoom createDungeonRoom(String name, String description,
                                               String type, int minLevel, int maxLevel) {
        LotrDungeonRoom room = new LotrDungeonRoom();
        room.setName(name);
        room.setDescription(description);
        room.setType(type);
        room.setMinLevel(minLevel);
        room.setMaxLevel(maxLevel);
        room.setActive(true);

        LotrDungeonRoom saved = dungeonRoomRepository.save(room);
        log.info("Created dungeon room: {} ({})", saved.getName(), saved.getType());
        return saved;
    }

    /**
     * Update an existing dungeon room.
     */
    @Transactional
    public LotrDungeonRoom updateDungeonRoom(Long id, String name, String description,
                                               String type, int minLevel, int maxLevel) {
        LotrDungeonRoom room = dungeonRoomRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Dungeon room not found: " + id));

        room.setName(name);
        room.setDescription(description);
        room.setType(type);
        room.setMinLevel(minLevel);
        room.setMaxLevel(maxLevel);

        LotrDungeonRoom saved = dungeonRoomRepository.save(room);
        log.info("Updated dungeon room: {}", saved.getName());
        return saved;
    }

    /**
     * Delete a dungeon room.
     */
    @Transactional
    public void deleteDungeonRoom(Long id) {
        if (!dungeonRoomRepository.existsById(id)) {
            throw new IllegalArgumentException("Dungeon room not found: " + id);
        }
        dungeonRoomRepository.deleteById(id);
        log.info("Deleted dungeon room: {}", id);
    }

    /**
     * Initialize the dungeon room database with LOTR-themed rooms.
     */
    @Transactional
    public void initializeDungeonRoomDatabase() {
        if (!dungeonRoomRepository.findAll().isEmpty()) {
            log.info("Dungeon room database already initialized, skipping.");
            return;
        }

        // Moria
        createDungeonRoom("First Level of Moria",
                "Ancient dwarf halls, now dark and dangerous. Orcs lurk in the shadows.",
                "Combat", 3, 6);
        createDungeonRoom("Balins Tomb",
                "A grim discovery. Skeletons and an orc camp mark Balin's final rest.",
                "Exploration", 4, 7);
        createDungeonRoom("Chamber of Mazarbul",
                "The guardroom where the Book of Mazarbul lies. A Balrog awaits.",
                "Boss", 5, 8);
        createDungeonRoom("Bridge of Khazad-dum",
                "The iconic bridge where Gandalf faced the Balrog. A chasm yawns below.",
                "Trap", 5, 8);

        // Cirith Ungol
        createDungeonRoom("Shelobs Lair",
                "The giant spider's web-filled lair. Flee or fight for your life.",
                "Boss", 6, 10);
        createDungeonRoom("Prison of Cirith Ungol",
                "Frodo was imprisoned here. Dark memories linger in the cold stone.",
                "Exploration", 5, 9);

        // Other dungeons
        createDungeonRoom("Dol Guldur Halls",
                "The Necromancer's fortress. Dark magic permeates every corridor.",
                "Combat", 7, 12);
        createDungeonRoom("Isengard Underground",
                "Saruman's secret laboratories where Uruk-hai were bred.",
                "Exploration", 5, 10);
        createDungeonRoom("Barad-dûr Depths",
                "The lowest levels of the Dark Tower. Only the bravest dare enter.",
                "Boss", 12, 20);
        createDungeonRoom("Mount Doom Approach",
                "The path to the Cracks of Doom. Heat and danger increase with every step.",
                "Exploration", 10, 20);
        createDungeonRoom("Ancient Ent Tomb",
                "A forgotten burial place of the oldest Ents. Peaceful but mysterious.",
                "Exploration", 3, 8);
        createDungeonRoom("Trollshaw Cave",
                "Trolls guard treasure in this dark cave. Gold and danger await.",
                "Combat", 2, 5);

        log.info("Initialized dungeon room database with {} LOTR rooms.", dungeonRoomRepository.count());
    }
}
