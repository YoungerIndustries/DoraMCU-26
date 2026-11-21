package com.yisy.modules.doramcu.lord_of_the_rings.service;
import com.yisy.modules.doramcu.model.players.Player;

import com.yisy.modules.doramcu.lord_of_the_rings.model.LotrNpc;
import com.yisy.modules.doramcu.lord_of_the_rings.repository.LotrNpcRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Service for managing LOTR NPCs.
 * <p>
 * Tracks all key non-player characters in the Lord of the Rings galaxy, including
 * their race, calling, faction, realm, and abilities. NPCs can be allies, enemies,
 * or neutral characters that drive the narrative.
 * </p>
 * <p>
 * Key mechanics:
 * - Full CRUD for NPCs with race, calling, level, and abilities
 * - Faction and realm tracking for LOTR geography
 * - HP and AC for combat encounters
 * - Generate NPCs from factions with LOTR-themed names
 * </p>
 */
@Service
public class LotrNpcService {

    private static final Logger log = LoggerFactory.getLogger(LotrNpcService.class);

    // LOTR-themed NPC names by race
    private static final Map<String, List<String>> NPC_NAMES_BY_RACE = new HashMap<>();
    static {
        NPC_NAMES_BY_RACE.put("Elf", List.of("Aeglos", "Arwen", "Celeborn", "Elrond", "Erestor", "Galadriel",
                "Glorfindel", "Haldir", "Legolas", "Luthien", "Thranduil"));
        NPC_NAMES_BY_RACE.put("Man", List.of("Aragorn", "Boromir", "Denethor", "Eowyn", "Eomer", "Faramir",
                "Theoden", "Theodred", "Imrahil", "Eldarion", "Elanor"));
        NPC_NAMES_BY_RACE.put("Dwarf", List.of("Balin", "Dwalin", "Gimli", "Gloin", "Liavelle", "Fundin",
                "Bifur", "Bofur", "Bombur", "Ori", "Nori"));
        NPC_NAMES_BY_RACE.put("Hobbit", List.of("Bilbo", "Frodo", "Samwise", "Merry", "Pippin", "Peregrin",
                "Roheryn", "Otho", "Lobelia", "Rosie", "Toby"));
        NPC_NAMES_BY_RACE.put("Ent", List.of("Treebeard", "Finglas", "Fladrif", "Huorn", "Quickbeam",
                "Oakenshield", "Leaflock", "Bramblegard"));
        NPC_NAMES_BY_RACE.put("Orc", List.of("Gorbag", "Shagrat", "Lorgil", "Snaga", "Ugluk", "Lurtz",
                "Grishnakh", "Wormtongue", "Lipkil"));
        NPC_NAMES_BY_RACE.put("Maia", List.of("Gandalf", "Saruman", "Radagast", "Sauron", "Curumo",
                "Alatar", "Pallando"));
    }

    // LOTR callings
    private static final List<String> CALLINGS = List.of("Scout", "Warrior", "Ranger", "Bard", "Mage", "Healer");

    // LOTR factions
    private static final List<String> FACTIONS = List.of(
            "Free Peoples Alliance", "Mordor", "Isengard", "Nazgûl", "Harad", "Rhûn", "Ents"
    );

    // LOTR realms
    private static final List<String> REALMS = List.of(
            "Rivendell", "Lothlórien", "Gondor", "Rohan", "Mordor", "Isengard", "The Shire",
            "Moria", "Númenor", "Harad", "Rhûn", "Forodwaith", "Mirkwood", "Fangorn"
    );

    private final LotrNpcRepository npcRepository;
    private final Random random = new Random();

    public LotrNpcService(LotrNpcRepository npcRepository) {
        this.npcRepository = npcRepository;
    }

    /**
     * Get all NPCs.
     */
    @Transactional(readOnly = true)
    public List<LotrNpc> findAll() {
        return npcRepository.findAll();
    }

    /**
     * Get NPC by ID.
     */
    @Transactional(readOnly = true)
    public Optional<LotrNpc> findById(Long id) {
        return npcRepository.findById(id);
    }

    /**
     * Find NPCs by name (case-insensitive partial match).
     */
    @Transactional(readOnly = true)
    public List<LotrNpc> findByName(String name) {
        return npcRepository.findByNameContainingIgnoreCase(name);
    }

    /**
     * Find NPCs by race.
     */
    @Transactional(readOnly = true)
    public List<LotrNpc> findByRace(String race) {
        return npcRepository.findByRace(race);
    }

    /**
     * Find NPCs by faction.
     */
    @Transactional(readOnly = true)
    public List<LotrNpc> findByFaction(String faction) {
        return npcRepository.findByFaction(faction);
    }

    /**
     * Create a new NPC.
     */
    @Transactional
    public LotrNpc createNpc(String name, String race, String calling, int level, String faction, String realm, String description) {
        LotrNpc npc = new LotrNpc();
        npc.setName(name);
        npc.setRace(race);
        npc.setCalling(calling);
        npc.setLevel(level);
        npc.setFaction(faction);
        npc.setRealm(realm);
        npc.setDescription(description);

        // Set ability scores based on race
        setRaceAbilityScores(npc, race);

        // Set HP and AC based on calling and level
        setCallingStats(npc, calling, level);

        LotrNpc saved = npcRepository.save(npc);
        log.info("Created NPC: {} ({}, {} {})", saved.getName(), saved.getRace(), saved.getCalling(), saved.getLevel());
        return saved;
    }

    /**
     * Update an existing NPC.
     */
    @Transactional
    public LotrNpc updateNpc(Long id, String name, String race, String calling, int level, String faction, String realm, String description) {
        LotrNpc npc = npcRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("NPC not found: " + id));

        npc.setName(name);
        npc.setRace(race);
        npc.setCalling(calling);
        npc.setLevel(level);
        npc.setFaction(faction);
        npc.setRealm(realm);
        npc.setDescription(description);

        setRaceAbilityScores(npc, race);
        setCallingStats(npc, calling, level);

        LotrNpc saved = npcRepository.save(npc);
        log.info("Updated NPC: {}", saved.getName());
        return saved;
    }

    /**
     * Delete an NPC.
     */
    @Transactional
    public void deleteNpc(Long id) {
        if (!npcRepository.existsById(id)) {
            throw new IllegalArgumentException("NPC not found: " + id);
        }
        npcRepository.deleteById(id);
        log.info("Deleted NPC: {}", id);
    }

    /**
     * Generate a random LOTR-themed NPC from a faction.
     */
    @Transactional
    public LotrNpc generateNpcFromFaction(String faction) {
        String race = getRandomRaceForFaction(faction);
        String name = getRandomNameForRace(race);
        String calling = CALLINGS.get(random.nextInt(CALLINGS.size()));
        int level = random.nextInt(10) + 1;
        String realm = getRandomRealmForFaction(faction);
        String description = String.format("A %s %s serving %s.", race.toLowerCase(), calling.toLowerCase(), faction);

        return createNpc(name, race, calling, level, faction, realm, description);
    }

    /**
     * Generate a random LOTR-themed NPC.
     */
    @Transactional
    public LotrNpc generateRandomNpc() {
        String race = getRandomRace();
        String name = getRandomNameForRace(race);
        String calling = CALLINGS.get(random.nextInt(CALLINGS.size()));
        int level = random.nextInt(10) + 1;
        String faction = FACTIONS.get(random.nextInt(FACTIONS.size()));
        String realm = REALMS.get(random.nextInt(REALMS.size()));
        String description = String.format("A wandering %s %s from %s.", race.toLowerCase(), calling.toLowerCase(), realm);

        return createNpc(name, race, calling, level, faction, realm, description);
    }

    /**
     * Damage an NPC in combat.
     */
    @Transactional
    public String damageNpc(Long id, int damage) {
        LotrNpc npc = npcRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("NPC not found: " + id));

        int currentHp = npc.getHitPoints();
        int newHp = Math.max(0, currentHp - damage);
        npc.setHitPoints(newHp);
        npcRepository.save(npc);

        if (newHp <= 0) {
            return String.format("%s has been defeated!", npc.getName());
        }
        return String.format("%s takes %d damage. HP: %d/%d", npc.getName(), damage, newHp, npc.getMaxHitPoints());
    }

    /**
     * Heal an NPC.
     */
    @Transactional
    public String healNpc(Long id, int healing) {
        LotrNpc npc = npcRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("NPC not found: " + id));

        int currentHp = npc.getHitPoints();
        int newHp = Math.min(npc.getMaxHitPoints(), currentHp + healing);
        npc.setHitPoints(newHp);
        npcRepository.save(npc);

        return String.format("%s is healed for %d. HP: %d/%d", npc.getName(), newHp - currentHp, newHp, npc.getMaxHitPoints());
    }

    /**
     * Initialize the NPC database with LOTR-themed characters.
     */
    @Transactional
    public void initializeNpcDatabase() {
        if (!npcRepository.findAll().isEmpty()) {
            log.info("NPC database already initialized, skipping.");
            return;
        }

        // Key LOTR characters
        createNpc("Aragorn", "Man", "Ranger", 10, "Free Peoples Alliance", "Gondor",
                "Heir of Isildur, Ranger of the North, future King of Gondor.");
        createNpc("Gandalf", "Maia", "Mage", 20, "Free Peoples Alliance", "Rivendell",
                "The Grey Pilgrim, a Maia sent to Middle-earth to oppose Sauron.");
        createNpc("Legolas", "Elf", "Scout", 10, "Free Peoples Alliance", "Mirkwood",
                "Prince of Mirkwood, master archer of the Woodland Realm.");
        createNpc("Gimli", "Dwarf", "Warrior", 10, "Free Peoples Alliance", "Moria",
                "Son of Glóin, warrior of the Lonely Mountain.");
        createNpc("Frodo", "Hobbit", "Scout", 5, "Free Peoples Alliance", "The Shire",
                "The Ring-bearer, tasked with destroying the One Ring.");
        createNpc("Samwise", "Hobbit", "Warrior", 5, "Free Peoples Alliance", "The Shire",
                "Frodo's most loyal companion, gardener of Bag End.");
        createNpc("Galadriel", "Elf", "Mage", 20, "Free Peoples Alliance", "Lothlórien",
                "Lady of Lothlórien, keeper of Nenya, one of the Elven Rings.");
        createNpc("Elrond", "Elf", "Healer", 15, "Free Peoples Alliance", "Rivendell",
                "Lord of Rivendell, half-elven master of healing and lore.");
        createNpc("Saruman", "Maia", "Mage", 18, "Isengard", "Isengard",
                "The White, once chief of the Istari, now corrupted by the One Ring.");
        createNpc("Witch-king", "Maia", "Warrior", 15, "Nazgûl", "Mordor",
                "Lord of the Nazgûl, captain of Sauron's servants.");
        createNpc("Boromir", "Man", "Warrior", 8, "Free Peoples Alliance", "Gondor",
                "Captain of the White Tower, son of Denethor.");
        createNpc("Éowyn", "Man", "Warrior", 8, "Free Peoples Alliance", "Rohan",
                "Lady of Rohan, the Shieldmaiden who slew the Witch-king.");
        createNpc("Treebeard", "Ent", "Warrior", 12, "Ents", "Fangorn",
                "The oldest of the Ents, Fangorn's guardian.");
        createNpc("Gollum", "Hobbit", "Scout", 5, null, "Mirkwood",
                "Once Sméagol, now a wretched creature enslaved by the Ring.");
        createNpc("Faramir", "Man", "Ranger", 8, "Free Peoples Alliance", "Gondor",
                "Captain of the Rangers of Ithilien, noble and wise.");

        log.info("Initialized NPC database with {} LOTR characters.", npcRepository.count());
    }

    // --- Helper Methods ---

    private void setRaceAbilityScores(LotrNpc npc, String race) {
        switch (race) {
            case "Elf":
                npc.setDexScore(16);
                npc.setChaScore(14);
                npc.setIntScore(14);
                break;
            case "Man":
                npc.setStrScore(14);
                npc.setConScore(14);
                break;
            case "Dwarf":
                npc.setConScore(16);
                npc.setStrScore(14);
                break;
            case "Hobbit":
                npc.setDexScore(14);
                npc.setWisScore(14);
                npc.setChaScore(14);
                break;
            case "Ent":
                npc.setStrScore(18);
                npc.setConScore(18);
                break;
            case "Orc":
                npc.setStrScore(16);
                npc.setConScore(14);
                break;
            case "Maia":
                npc.setIntScore(18);
                npc.setWisScore(16);
                break;
            default:
                break;
        }
    }

    private void setCallingStats(LotrNpc npc, String calling, int level) {
        int hitDie;
        int baseAc;

        switch (calling) {
            case "Warrior":
                hitDie = 10;
                baseAc = 16;
                break;
            case "Ranger":
                hitDie = 8;
                baseAc = 14;
                break;
            case "Scout":
                hitDie = 6;
                baseAc = 13;
                break;
            case "Mage":
                hitDie = 6;
                baseAc = 12;
                break;
            case "Bard":
                hitDie = 8;
                baseAc = 13;
                break;
            case "Healer":
                hitDie = 8;
                baseAc = 12;
                break;
            default:
                hitDie = 8;
                baseAc = 10;
                break;
        }

        int conMod = (npc.getConScore() - 10) / 2;
        int maxHp = level * hitDie + (conMod * level);
        npc.setMaxHitPoints(maxHp);
        npc.setHitPoints(maxHp);
        npc.setArmorClass(baseAc);
    }

    private String getRandomRace() {
        List<String> races = new ArrayList<>(NPC_NAMES_BY_RACE.keySet());
        return races.get(random.nextInt(races.size()));
    }

    private String getRandomRaceForFaction(String faction) {
        if (faction == null) return getRandomRace();
        switch (faction) {
            case "Mordor":
            case "Nazgûl":
                return "Orc";
            case "Isengard":
                return random.nextBoolean() ? "Orc" : "Man";
            case "Ents":
                return "Ent";
            case "Free Peoples Alliance":
                List<String> freeRaces = List.of("Elf", "Man", "Dwarf", "Hobbit", "Maia");
                return freeRaces.get(random.nextInt(freeRaces.size()));
            default:
                return getRandomRace();
        }
    }

    private String getRandomNameForRace(String race) {
        List<String> names = NPC_NAMES_BY_RACE.getOrDefault(race, List.of("Unknown"));
        return names.get(random.nextInt(names.size()));
    }

    private String getRandomRealmForFaction(String faction) {
        if (faction == null) return REALMS.get(random.nextInt(REALMS.size()));
        switch (faction) {
            case "Mordor":
                return "Mordor";
            case "Isengard":
                return "Isengard";
            case "Ents":
                return "Fangorn";
            case "Free Peoples Alliance":
                List<String> freeRealms = List.of("Rivendell", "Lothlórien", "Gondor", "Rohan", "The Shire");
                return freeRealms.get(random.nextInt(freeRealms.size()));
            default:
                return REALMS.get(random.nextInt(REALMS.size()));
        }
    }
}
