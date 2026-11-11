package com.yisy.modules.doramcu.dnd5e.service;

import com.yisy.modules.doramcu.dnd5e.model.Dnd5eNpc;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5eNpcRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Service for managing D&D 5e NPC Roster.
 * <p>
 * The NPC roster tracks all key non-player characters in the campaign, including
 * their motives, resources, relationships, and disposition toward player characters.
 * This helps the DM manage a living, reactive world where NPCs have goals and
 * connections that drive the narrative.
 * </p>
 * <p>
 * Key mechanics:
 * - Full CRUD for NPCs with type, race, class, level, and CR
 * - Motive tracking: primary motive and secondary motives/secrets
 * - Resource tracking: gold, items, lair location
 * - Relationship tracking: allies, enemies, family connections
 * - Disposition system: numeric score (-10 to +10) with auto-updating disposition string
 * - Villain and Ally flags for quick filtering
 * - Disposition changes based on player actions
 * </p>
 */
@Service
public class Dnd5eNpcService {

    private static final Logger log = LoggerFactory.getLogger(Dnd5eNpcService.class);

    private final Dnd5eNpcRepository npcRepository;

    public Dnd5eNpcService(Dnd5eNpcRepository npcRepository) {
        this.npcRepository = npcRepository;
        initializeNpcDatabase();
    }

    // ==================== NPC CRUD ====================

    @Transactional
    public Dnd5eNpc createNpc(Dnd5eNpc npc) {
        log.info("Creating NPC: {}", npc.getName());
        return npcRepository.save(npc);
    }

    public Optional<Dnd5eNpc> getNpc(Long id) {
        return npcRepository.findById(id);
    }

    public List<Dnd5eNpc> listNpcs() {
        return npcRepository.findByActiveTrue();
    }

    public List<Dnd5eNpc> listNpcsByType(String npcType) {
        return npcRepository.findByNpcTypeAndActiveTrue(npcType);
    }

    public List<Dnd5eNpc> listNpcsByDisposition(String disposition) {
        return npcRepository.findByDispositionAndActiveTrue(disposition);
    }

    public List<Dnd5eNpc> listNpcsByClass(String npcClass) {
        return npcRepository.findByClass(npcClass);
    }

    public List<Dnd5eNpc> listNpcsByRace(String race) {
        return npcRepository.findByRace(race);
    }

    public List<Dnd5eNpc> listNpcsByCrRange(int minCr, int maxCr) {
        return npcRepository.findByCrRange(minCr, maxCr);
    }

    public List<Dnd5eNpc> listVillains() {
        return npcRepository.findByIsVillainTrueAndActiveTrue();
    }

    public List<Dnd5eNpc> listAllies() {
        return npcRepository.findByIsAllyTrueAndActiveTrue();
    }

    public List<Dnd5eNpc> searchNpcs(String keyword) {
        return npcRepository.searchByKeyword(keyword);
    }

    @Transactional
    public void deleteNpc(Long id) {
        npcRepository.findById(id).ifPresent(npc -> {
            npc.setActive(false);
            npcRepository.save(npc);
            log.info("Deactivated NPC: {}", npc.getName());
        });
    }

    // ==================== Disposition Management ====================

    /**
     * Change the disposition score of an NPC by the specified amount.
     * The disposition string is auto-updated based on the new score.
     *
     * @param id NPC id
     * @param amount amount to change disposition (positive = more friendly, negative = more hostile)
     * @return updated NPC, or null if not found
     */
    @Transactional
    public Dnd5eNpc changeDisposition(Long id, int amount) {
        Optional<Dnd5eNpc> opt = npcRepository.findById(id);
        if (opt.isEmpty()) {
            log.warn("NPC not found: {}", id);
            return null;
        }

        Dnd5eNpc npc = opt.get();
        int oldScore = npc.getDispositionScore();
        int newScore = Math.max(-10, Math.min(10, oldScore + amount));
        npc.setDispositionScore(newScore);
        npcRepository.save(npc);

        log.info("Disposition changed: {} from {} to {} (disposition: {})",
                npc.getName(), oldScore, newScore, npc.getDisposition());
        return npc;
    }

    /**
     * Set the disposition score of an NPC to a specific value.
     *
     * @param id NPC id
     * @param score new disposition score (-10 to +10)
     * @return updated NPC, or null if not found
     */
    @Transactional
    public Dnd5eNpc setDispositionScore(Long id, int score) {
        Optional<Dnd5eNpc> opt = npcRepository.findById(id);
        if (opt.isEmpty()) {
            log.warn("NPC not found: {}", id);
            return null;
        }

        Dnd5eNpc npc = opt.get();
        npc.setDispositionScore(score);
        npcRepository.save(npc);
        log.info("Disposition set: {} to {} (disposition: {})", npc.getName(), score, npc.getDisposition());
        return npc;
    }

    /**
     * Get NPCs with disposition score at least the specified value.
     */
    public List<Dnd5eNpc> getNpcsByDispositionScoreAtLeast(int minScore) {
        return npcRepository.findByDispositionScoreAtLeast(minScore);
    }

    /**
     * Get NPCs with disposition score at most the specified value.
     */
    public List<Dnd5eNpc> getNpcsByDispositionScoreAtMost(int maxScore) {
        return npcRepository.findByDispositionScoreAtMost(maxScore);
    }

    // ==================== Relationship Management ====================

    /**
     * Add an ally relationship between two NPCs.
     * If NPC A adds NPC B as an ally, NPC B also adds NPC A as an ally (bidirectional).
     *
     * @param npcId the NPC who is gaining the ally
     * @param allyName the name of the ally NPC
     * @return updated NPC, or null if not found
     */
    @Transactional
    public Dnd5eNpc addAlly(Long npcId, String allyName) {
        Optional<Dnd5eNpc> opt = npcRepository.findById(npcId);
        if (opt.isEmpty()) {
            log.warn("NPC not found: {}", npcId);
            return null;
        }

        Dnd5eNpc npc = opt.get();
        String current = npc.getAllies();
        if (current == null || current.isEmpty()) {
            npc.setAllies(allyName);
        } else if (!current.contains(allyName)) {
            npc.setAllies(current + "," + allyName);
        }

        npcRepository.save(npc);
        log.info("Ally added: {} -> {}", npc.getName(), allyName);

        // Make the relationship bidirectional
        List<Dnd5eNpc> allyList = npcRepository.findByNameContainingIgnoreCase(allyName);
        if (!allyList.isEmpty()) {
            Dnd5eNpc ally = allyList.get(0);
            String allyAllies = ally.getAllies();
            if (allyAllies == null || allyAllies.isEmpty()) {
                ally.setAllies(npc.getName());
            } else if (!allyAllies.contains(npc.getName())) {
                ally.setAllies(allyAllies + "," + npc.getName());
            }
            npcRepository.save(ally);
        }

        return npc;
    }

    /**
     * Add an enemy relationship between two NPCs.
     * If NPC A adds NPC B as an enemy, NPC B also adds NPC A as an enemy (bidirectional).
     *
     * @param npcId the NPC who is gaining the enemy
     * @param enemyName the name of the enemy NPC
     * @return updated NPC, or null if not found
     */
    @Transactional
    public Dnd5eNpc addEnemy(Long npcId, String enemyName) {
        Optional<Dnd5eNpc> opt = npcRepository.findById(npcId);
        if (opt.isEmpty()) {
            log.warn("NPC not found: {}", npcId);
            return null;
        }

        Dnd5eNpc npc = opt.get();
        String current = npc.getEnemies();
        if (current == null || current.isEmpty()) {
            npc.setEnemies(enemyName);
        } else if (!current.contains(enemyName)) {
            npc.setEnemies(current + "," + enemyName);
        }

        npcRepository.save(npc);
        log.info("Enemy added: {} -> {}", npc.getName(), enemyName);

        // Make the relationship bidirectional
        List<Dnd5eNpc> enemyList = npcRepository.findByNameContainingIgnoreCase(enemyName);
        if (!enemyList.isEmpty()) {
            Dnd5eNpc enemy = enemyList.get(0);
            String enemyEnemies = enemy.getEnemies();
            if (enemyEnemies == null || enemyEnemies.isEmpty()) {
                enemy.setEnemies(npc.getName());
            } else if (!enemyEnemies.contains(npc.getName())) {
                enemy.setEnemies(enemyEnemies + "," + npc.getName());
            }
            npcRepository.save(enemy);
        }

        return npc;
    }

    /**
     * Add a family relationship to an NPC.
     *
     * @param npcId the NPC who is gaining the family member
     * @param familyName the name of the family member
     * @return updated NPC, or null if not found
     */
    @Transactional
    public Dnd5eNpc addFamily(Long npcId, String familyName) {
        Optional<Dnd5eNpc> opt = npcRepository.findById(npcId);
        if (opt.isEmpty()) {
            log.warn("NPC not found: {}", npcId);
            return null;
        }

        Dnd5eNpc npc = opt.get();
        String current = npc.getFamily();
        if (current == null || current.isEmpty()) {
            npc.setFamily(familyName);
        } else if (!current.contains(familyName)) {
            npc.setFamily(current + "," + familyName);
        }

        npcRepository.save(npc);
        log.info("Family added: {} -> {}", npc.getName(), familyName);
        return npc;
    }

    /**
     * Get NPCs that have allies defined.
     */
    public List<Dnd5eNpc> getNpcsWithAllies() {
        return npcRepository.findAllies();
    }

    /**
     * Get NPCs that have enemies defined.
     */
    public List<Dnd5eNpc> getNpcsWithEnemies() {
        return npcRepository.FindEnemies();
    }

    // ==================== Resource Management ====================

    /**
     * Add gold to an NPC's resources.
     *
     * @param id NPC id
     * @param amount gold to add
     * @return updated NPC, or null if not found
     */
    @Transactional
    public Dnd5eNpc addGold(Long id, int amount) {
        Optional<Dnd5eNpc> opt = npcRepository.findById(id);
        if (opt.isEmpty()) {
            log.warn("NPC not found: {}", id);
            return null;
        }

        Dnd5eNpc npc = opt.get();
        npc.setGoldPieces(npc.getGoldPieces() + amount);
        npcRepository.save(npc);
        log.info("Gold added: {} now has {} gp", npc.getName(), npc.getGoldPieces());
        return npc;
    }

    /**
     * Remove gold from an NPC's resources.
     *
     * @param id NPC id
     * @param amount gold to remove
     * @return updated NPC, or null if not found
     */
    @Transactional
    public Dnd5eNpc removeGold(Long id, int amount) {
        Optional<Dnd5eNpc> opt = npcRepository.findById(id);
        if (opt.isEmpty()) {
            log.warn("NPC not found: {}", id);
            return null;
        }

        Dnd5eNpc npc = opt.get();
        npc.setGoldPieces(Math.max(0, npc.getGoldPieces() - amount));
        npcRepository.save(npc);
        log.info("Gold removed: {} now has {} gp", npc.getName(), npc.getGoldPieces());
        return npc;
    }

    /**
     * Set an NPC's hit points (for tracking combat damage).
     *
     * @param id NPC id
     * @param hitPoints new hit points
     * @return updated NPC, or null if not found
     */
    @Transactional
    public Dnd5eNpc setHitPoints(Long id, int hitPoints) {
        Optional<Dnd5eNpc> opt = npcRepository.findById(id);
        if (opt.isEmpty()) {
            log.warn("NPC not found: {}", id);
            return null;
        }

        Dnd5eNpc npc = opt.get();
        npc.setHitPoints(hitPoints);
        npcRepository.save(npc);
        log.info("HP set: {} now has {} HP", npc.getName(), hitPoints);
        return npc;
    }

    // ==================== Disposition Reference ====================

    /**
     * Get all possible disposition values.
     */
    public List<String> getDispositionValues() {
        return Arrays.asList("Hostile", "Unfriendly", "Indifferent", "Friendly", "Helpful");
    }

    /**
     * Get the disposition string for a given score.
     */
    public String getDispositionForScore(int score) {
        score = Math.max(-10, Math.min(10, score));
        if (score <= -6) return "Hostile";
        if (score <= -2) return "Unfriendly";
        if (score <= 1) return "Indifferent";
        if (score <= 5) return "Friendly";
        return "Helpful";
    }

    // ==================== Database Initialization ====================

    void initializeNpcDatabase() {
        if (npcRepository.count() > 0) {
            log.info("NPC roster already populated, skipping initialization");
            return;
        }

        log.info("Populating NPC roster with example NPCs");
        populateExampleNpcs();
    }

    void populateExampleNpcs() {
        // Villains
        Dnd5eNpc vecna = new Dnd5eNpc();
        vecna.setName("Vecna");
        vecna.setNpcType("Undead");
        vecna.setRace("Lich");
        vecna.setNpcClass("Wizard");
        vecna.setLevel(20);
        vecna.setChallengeRating(21);
        vecna.setDescription("The Lord of Secrets, an ancient lich who seeks to regain his power and rule the multiverse.");
        vecna.setMotive("Regain full power and establish dominion over all planes of existence.");
        vecna.setHitPoints(450);
        vecna.setArmorClass(19);
        vecna.setGoldPieces(50000);
        vecna.setVillain(true);
        vecna.setDispositionScore(-10);
        vecna.setLairLocation("The Crypt of Vecna, Far Realm");
        npcRepository.save(vecna);

        Dnd5eNpc runefang = new Dnd5eNpc();
        runefang.setName("Runefang");
        runefang.setNpcType("Undead");
        runefang.setRace("Death Knight");
        runefang.setNpcClass("Paladin");
        runefang.setLevel(15);
        runefang.setChallengeRating(14);
        runefang.setDescription("A fallen knight bound to serve Vecna, wielding a cursed runeblade.");
        runefang.setMotive("Serve Vecna and spread his influence across the land.");
        runefang.setHitPoints(300);
        runefang.setArmorClass(18);
        runefang.setGoldPieces(5000);
        runefang.setVillain(true);
        runefang.setDispositionScore(-8);
        runefang.setLairLocation("The Ruined Keep");
        runefang.setAllies("Vecna");
        vecna.setEnemies("Thalric");
        npcRepository.save(runefang);
        npcRepository.save(vecna);

        Dnd5eNpc tiamat = new Dnd5eNpc();
        tiamat.setName("Tiamat");
        tiamat.setNpcType("Dragon");
        tiamat.setRace("Five-Headed Dragon");
        tiamat.setNpcClass("None");
        tiamat.setLevel(20);
        tiamat.setChallengeRating(27);
        tiamat.setDescription("The Queen of Evil Dragons, ruler of the Nine Hells' dragon legions.");
        tiamat.setMotive("Destroy all good dragons and bring the material plane under chromatic dragon rule.");
        tiamat.setHitPoints(630);
        tiamat.setArmorClass(17);
        tiamat.setGoldPieces(100000);
        tiamat.setVillain(true);
        tiamat.setDispositionScore(-10);
        tiamat.setLairLocation("The Nine Hells");
        npcRepository.save(tiamat);

        // Allies
        Dnd5eNpc thalric = new Dnd5eNpc();
        thalric.setName("Thalric");
        thalric.setNpcType("Humanoid");
        thalric.setRace("Human");
        thalric.setNpcClass("Cleric");
        thalric.setLevel(10);
        thalric.setChallengeRating(5);
        thalric.setDescription("A wise cleric of Lathander who guides the heroes against Vecna's return.");
        thalric.setMotive("Protect the realm from Vecna's influence and guide the heroes to victory.");
        thalric.setHitPoints(120);
        thalric.setArmorClass(16);
        thalric.setGoldPieces(2000);
        thalric.setAlly(true);
        thalric.setDispositionScore(8);
        thalric.setLairLocation("The Temple of Lathander");
        thalric.setEnemies("Vecna,Runefang");
        runefang.setEnemies("Thalric");
        npcRepository.save(thalric);
        npcRepository.save(runefang);

        Dnd5eNpc elara = new Dnd5eNpc();
        elara.setName("Elara Starweaver");
        elara.setNpcType("Humanoid");
        elara.setRace("Elf");
        elara.setNpcClass("Wizard");
        elara.setLevel(12);
        elara.setChallengeRating(6);
        elara.setDescription("A powerful wizard who studies ancient magic and aids the heroes.");
        elara.setMotive("Preserve ancient knowledge and prevent the misuse of forbidden magic.");
        elara.setHitPoints(100);
        elara.setArmorClass(14);
        elara.setGoldPieces(8000);
        elara.setAlly(true);
        elara.setDispositionScore(7);
        elara.setLairLocation("The Starweaver Tower");
        elara.setAllies("Thalric");
        thalric.setAllies("Elara");
        npcRepository.save(elara);
        npcRepository.save(thalric);

        // Neutral NPCs
        Dnd5eNpc garrick = new Dnd5eNpc();
        garrick.setName("Garrick Ironforge");
        garrick.setNpcType("Humanoid");
        garrick.setRace("Dwarf");
        garrick.setNpcClass("Artificer");
        garrick.setLevel(8);
        garrick.setChallengeRating(4);
        garrick.setDescription("A master artificer who sells magical items and information.");
        garrick.setMotive("Build the greatest magical device ever created.");
        garrick.setHitPoints(80);
        garrick.setArmorClass(15);
        garrick.setGoldPieces(15000);
        garrick.setDispositionScore(0);
        garrick.setLairLocation("Ironforge Workshop, Waterdeep");
        npcRepository.save(garrick);

        Dnd5eNpc zara = new Dnd5eNpc();
        zara.setName("Zara Shadowstep");
        zara.setNpcType("Humanoid");
        zara.setRace("Half-Elf");
        zara.setNpcClass("Rogue");
        zara.setLevel(6);
        zara.setChallengeRating(3);
        zara.setDescription("A cunning information broker who knows everyone's secrets.");
        zara.setMotive("Accumulate information and leverage it for personal gain.");
        zara.setHitPoints(60);
        zara.setArmorClass(16);
        zara.setGoldPieces(10000);
        zara.setDispositionScore(2);
        zara.setLairLocation("The Whispering Tavern");
        npcRepository.save(zara);

        Dnd5eNpc brogan = new Dnd5eNpc();
        brogan.setName("Brogan Stoneheart");
        brogan.setNpcType("Humanoid");
        brogan.setRace("Dwarf");
        brogan.setNpcClass("Fighter");
        brogan.setLevel(7);
        brogan.setChallengeRating(4);
        brogan.setDescription("A gruff but honorable mercenary captain for hire.");
        brogan.setMotive("Earn enough gold to reclaim his family's lost mine.");
        brogan.setHitPoints(110);
        brogan.setArmorClass(17);
        brogan.setGoldPieces(3000);
        brogan.setDispositionScore(1);
        brogan.setLairLocation("The Stoneheart Barracks");
        npcRepository.save(brogan);

        // Feywild NPC
        Dnd5eNpc valas = new Dnd5eNpc();
        valas.setName("Valas Hethar");
        valas.setNpcType("Fey");
        valas.setRace("Archfey");
        valas.setNpcClass("None");
        valas.setLevel(15);
        valas.setChallengeRating(19);
        valas.setDescription("The Archfey of the Feywild, capricious and powerful.");
        valas.setMotive("Entertain themselves and maintain the balance of the Feywild.");
        valas.setHitPoints(400);
        valas.setArmorClass(18);
        valas.setGoldPieces(0);
        valas.setDispositionScore(0);
        valas.setLairLocation("The Fey Court");
        npcRepository.save(valas);

        // Dragon ally
        Dnd5eNpc tarr = new Dnd5eNpc();
        tarr.setName("Tarrashnak");
        tarr.setNpcType("Dragon");
        tarr.setRace("Gold Dragon");
        tarr.setNpcClass("None");
        tarr.setLevel(18);
        tarr.setChallengeRating(24);
        tarr.setDescription("An ancient gold dragon who aids the heroes against Tiamat.");
        tarr.setMotive("Protect the material plane from chromatic dragon tyranny.");
        tarr.setHitPoints(550);
        tarr.setArmorClass(19);
        tarr.setGoldPieces(75000);
        tarr.setAlly(true);
        tarr.setDispositionScore(6);
        tarr.setLairLocation("The Golden Peaks");
        tarr.setEnemies("Tiamat");
        tiamat.setEnemies("Tarrashnak");
        npcRepository.save(tarr);
        npcRepository.save(tiamat);

        // Commoner
        Dnd5eNpc milla = new Dnd5eNpc();
        milla.setName("Milla");
        milla.setNpcType("Humanoid");
        milla.setRace("Human");
        milla.setNpcClass("None");
        milla.setLevel(1);
        milla.setChallengeRating(0);
        milla.setDescription("A kind innkeeper who provides shelter and rumors.");
        milla.setMotive("Keep her inn running and protect her town.");
        milla.setHitPoints(10);
        milla.setArmorClass(10);
        milla.setGoldPieces(100);
        milla.setDispositionScore(3);
        milla.setLairLocation("The Golden Dragon Inn");
        npcRepository.save(milla);

        // Merchant
        Dnd5eNpc silas = new Dnd5eNpc();
        silas.setName("Silas Goldvein");
        silas.setNpcType("Humanoid");
        silas.setRace("Halfling");
        silas.setNpcClass("None");
        silas.setLevel(1);
        silas.setChallengeRating(0);
        silas.setDescription("A wealthy merchant who trades in rare artifacts.");
        silas.setMotive("Expand his trading empire across the known world.");
        silas.setHitPoints(12);
        silas.setArmorClass(11);
        silas.setGoldPieces(50000);
        silas.setDispositionScore(1);
        silas.setLairLocation("Goldvein Trading Company");
        npcRepository.save(silas);

        log.info("Populated {} example NPCs", npcRepository.count());
    }
}
