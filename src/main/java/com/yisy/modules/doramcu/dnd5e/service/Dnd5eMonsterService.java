package com.yisy.modules.doramcu.dnd5e.service;

import com.yisy.modules.doramcu.dnd5e.model.Dnd5eMonster;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eMonsterAbility;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5eMonsterAbilityRepository;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5eMonsterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class Dnd5eMonsterService {

    private static final Logger log = LoggerFactory.getLogger(Dnd5eMonsterService.class);
    private static final int EXPECTED_MONSTER_COUNT_MIN = 40;

    private static final Map<Double, Integer> XP_BY_CR = new HashMap<>();
    static {
        XP_BY_CR.put(0.0, 50); XP_BY_CR.put(0.125, 25); XP_BY_CR.put(0.25, 100);
        XP_BY_CR.put(0.5, 200); XP_BY_CR.put(1.0, 200); XP_BY_CR.put(2.0, 450);
        XP_BY_CR.put(3.0, 700); XP_BY_CR.put(4.0, 1100); XP_BY_CR.put(5.0, 1800);
        XP_BY_CR.put(6.0, 2300); XP_BY_CR.put(7.0, 2900); XP_BY_CR.put(8.0, 3900);
        XP_BY_CR.put(9.0, 5000); XP_BY_CR.put(10.0, 5900); XP_BY_CR.put(11.0, 7200);
        XP_BY_CR.put(12.0, 8400); XP_BY_CR.put(13.0, 10500); XP_BY_CR.put(14.0, 13000);
        XP_BY_CR.put(15.0, 16000); XP_BY_CR.put(16.0, 19000); XP_BY_CR.put(17.0, 21000);
        XP_BY_CR.put(18.0, 25000); XP_BY_CR.put(19.0, 33000); XP_BY_CR.put(20.0, 41000);
        XP_BY_CR.put(21.0, 50000); XP_BY_CR.put(22.0, 62000); XP_BY_CR.put(23.0, 75000);
        XP_BY_CR.put(24.0, 90000); XP_BY_CR.put(25.0, 105000); XP_BY_CR.put(26.0, 120000);
        XP_BY_CR.put(27.0, 135000); XP_BY_CR.put(28.0, 155000); XP_BY_CR.put(29.0, 185000);
        XP_BY_CR.put(30.0, 230000);
    }

    private final Dnd5eMonsterRepository monsterRepository;
    private final Dnd5eMonsterAbilityRepository abilityRepository;
    private boolean initialized = false;

    public Dnd5eMonsterService(Dnd5eMonsterRepository monsterRepository,
                                Dnd5eMonsterAbilityRepository abilityRepository) {
        this.monsterRepository = monsterRepository;
        this.abilityRepository = abilityRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initializeMonsterDatabase() {
        if (initialized) return;
        long existingCount = monsterRepository.countByActiveTrue();
        if (existingCount >= EXPECTED_MONSTER_COUNT_MIN) {
            log.info("D&D 5e monster database already populated with {} monsters", existingCount);
            initialized = true;
            return;
        }
        log.info("Initializing D&D 5e monster database...");
        populateMonsters();
        populateMonsterAbilities();
        initialized = true;
        log.info("D&D 5e monster database initialized: {} monsters, {} abilities",
                monsterRepository.countByActiveTrue(), abilityRepository.findAll().size());
    }

    // ==================== Monster CRUD ====================

    @Transactional(readOnly = true)
    public Dnd5eMonster getMonsterByName(String name) {
        return monsterRepository.findByName(name)
                .orElseThrow(() -> new IllegalArgumentException("Monster not found: " + name));
    }

    @Transactional(readOnly = true)
    public Dnd5eMonster getMonster(Long id) {
        return monsterRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Monster not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<Dnd5eMonster> listMonsters() {
        return monsterRepository.findByActiveTrue();
    }

    @Transactional(readOnly = true)
    public List<Dnd5eMonster> listMonstersByType(String type) {
        return monsterRepository.findByTypeAndActiveTrue(type);
    }

    @Transactional(readOnly = true)
    public List<Dnd5eMonster> listMonstersBySize(String size) {
        return monsterRepository.findBySizeAndActiveTrue(size);
    }

    @Transactional(readOnly = true)
    public List<Dnd5eMonster> listMonstersByCr(double cr) {
        return monsterRepository.findByCrAndActiveTrue(cr);
    }

    @Transactional(readOnly = true)
    public List<Dnd5eMonster> listMonstersByCrRange(double minCr, double maxCr) {
        return monsterRepository.findByCrRange(minCr, maxCr);
    }

    @Transactional(readOnly = true)
    public List<Dnd5eMonster> listMonstersByXpRange(int minXp, int maxXp) {
        return monsterRepository.findByXpRange(minXp, maxXp);
    }

    @Transactional(readOnly = true)
    public List<Dnd5eMonster> listMonstersByAlignment(String alignment) {
        return monsterRepository.findByAlignmentAndActiveTrue(alignment);
    }

    @Transactional(readOnly = true)
    public List<Dnd5eMonster> listMonstersWithLairActions() {
        return monsterRepository.findByHasLairActionsTrueAndActiveTrue();
    }

    @Transactional(readOnly = true)
    public List<Dnd5eMonster> listMonstersWithLegendaryActions() {
        return monsterRepository.findByHasLegendaryActionsTrueAndActiveTrue();
    }

    @Transactional(readOnly = true)
    public List<Dnd5eMonster> listMonstersWithRegionalEffects() {
        return monsterRepository.findByHasRegionalEffectsTrueAndActiveTrue();
    }

    @Transactional(readOnly = true)
    public List<Dnd5eMonster> searchMonsters(String keyword) {
        return monsterRepository.searchByName(keyword);
    }

    @Transactional
    public Dnd5eMonster createMonster(Dnd5eMonster monster) {
        monster.setActive(true);
        return monsterRepository.save(monster);
    }

    @Transactional
    public Dnd5eMonster updateMonster(Long id, Dnd5eMonster monster) {
        Dnd5eMonster existing = getMonster(id);
        existing.setName(monster.getName());
        existing.setCr(monster.getCr());
        existing.setXp(monster.getXp());
        existing.setSize(monster.getSize());
        existing.setType(monster.getType());
        existing.setSubtype(monster.getSubtype());
        existing.setAlignment(monster.getAlignment());
        existing.setAc(monster.getAc());
        existing.setHp(monster.getHp());
        existing.setSpeed(monster.getSpeed());
        existing.setFlySpeed(monster.getFlySpeed());
        existing.setSwimSpeed(monster.getSwimSpeed());
        existing.setClimbSpeed(monster.getClimbSpeed());
        existing.setStr(monster.getStr());
        existing.setDex(monster.getDex());
        existing.setCon(monster.getCon());
        existing.setIntel(monster.getIntel());
        existing.setWis(monster.getWis());
        existing.setCha(monster.getCha());
        existing.setSavingThrows(monster.getSavingThrows());
        existing.setSkills(monster.getSkills());
        existing.setDamageImmunities(monster.getDamageImmunities());
        existing.setDamageResistances(monster.getDamageResistances());
        existing.setDamageVulnerabilities(monster.getDamageVulnerabilities());
        existing.setConditionImmunities(monster.getConditionImmunities());
        existing.setSenses(monster.getSenses());
        existing.setLanguages(monster.getLanguages());
        existing.setDescription(monster.getDescription());
        existing.setHasLairActions(monster.isHasLairActions());
        existing.setHasLegendaryActions(monster.isHasLegendaryActions());
        existing.setHasRegionalEffects(monster.isHasRegionalEffects());
        return monsterRepository.save(existing);
    }

    @Transactional
    public void deleteMonster(Long id) {
        Dnd5eMonster monster = getMonster(id);
        monster.setActive(false);
        monsterRepository.save(monster);
    }

    // ==================== Ability CRUD ====================

    @Transactional(readOnly = true)
    public List<Dnd5eMonsterAbility> listAbilities(String monsterName) {
        return abilityRepository.findByMonsterNameAndActiveTrue(monsterName);
    }

    @Transactional(readOnly = true)
    public List<Dnd5eMonsterAbility> listAbilitiesByType(String monsterName, String type) {
        return abilityRepository.findByMonsterNameAndTypeAndActiveTrue(monsterName, type);
    }

    @Transactional(readOnly = true)
    public List<Dnd5eMonsterAbility> listAbilitiesBySubtype(String monsterName, String subtype) {
        return abilityRepository.findByMonsterNameAndSubtypeAndActiveTrue(monsterName, subtype);
    }

    @Transactional(readOnly = true)
    public List<Dnd5eMonsterAbility> searchAbilities(String monsterName, String keyword) {
        return abilityRepository.searchByMonsterAndName(monsterName, keyword);
    }

    @Transactional
    public Dnd5eMonsterAbility createAbility(Dnd5eMonsterAbility ability) {
        ability.setActive(true);
        return abilityRepository.save(ability);
    }

    @Transactional
    public Dnd5eMonsterAbility updateAbility(Long id, Dnd5eMonsterAbility ability) {
        Dnd5eMonsterAbility existing = abilityRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ability not found: " + id));
        existing.setMonsterName(ability.getMonsterName());
        existing.setType(ability.getType());
        existing.setSubtype(ability.getSubtype());
        existing.setName(ability.getName());
        existing.setDescription(ability.getDescription());
        existing.setAttackBonus(ability.getAttackBonus());
        existing.setDamage(ability.getDamage());
        existing.setDamageType(ability.getDamageType());
        existing.setSaveDC(ability.getSaveDC());
        existing.setSaveAbility(ability.getSaveAbility());
        existing.setRange(ability.getRange());
        existing.setUsesPerLongRest(ability.getUsesPerLongRest());
        existing.setRecharge(ability.getRecharge());
        return abilityRepository.save(existing);
    }

    @Transactional
    public void deleteAbility(Long id) {
        Dnd5eMonsterAbility ability = abilityRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ability not found: " + id));
        ability.setActive(false);
        abilityRepository.save(ability);
    }

    // ==================== XP Lookup ====================

    @Transactional(readOnly = true)
    public int getXpForCr(double cr) {
        return XP_BY_CR.getOrDefault(cr, 0);
    }

    @Transactional(readOnly = true)
    public Map<Double, Integer> getXpTable() {
        return new HashMap<>(XP_BY_CR);
    }

    // ==================== Reference Data ====================

    @Transactional(readOnly = true)
    public List<String> listMonsterTypes() {
        return List.of("Aberration", "Beast", "Celestial", "Construct", "Dragon",
                "Elemental", "Fey", "Giant", "Humanoid", "Monstrosity", "Plant", "Undead");
    }

    @Transactional(readOnly = true)
    public List<String> listMonsterSizes() {
        return List.of("Tiny", "Small", "Medium", "Large", "Huge", "Gargantuan");
    }

    @Transactional(readOnly = true)
    public List<String> listAlignments() {
        return List.of("Lawful Good", "Neutral Good", "Chaotic Good",
                "Lawful Neutral", "True Neutral", "Chaotic Neutral",
                "Lawful Evil", "Neutral Evil", "Chaotic Evil");
    }

    @Transactional(readOnly = true)
    public List<String> listAbilityTypes() {
        return List.of("Attack", "Spell", "Legendary Action", "Lair Action",
                "Regional Effect", "Trait", "Action", "Reaction", "Bonus Action");
    }

    // ==================== Population ====================

    @Transactional
    public void populateMonsters() {
        // CR 0.125-0.5: Common Fodder
        m("Giant Rat", 0.125, 25, "Small", "Beast", null, "Unaligned", 10, 7, 30, 0, 0, 0, 3, 8, 6, 2, 2, 1, null, null, null, null, null, null, "Darkvision 60ft", "None", false, false, false);
        m("Giant Spider", 0.125, 25, "Small", "Beast", null, "Unaligned", 12, 15, 30, 0, 0, 10, 4, 14, 9, 2, 4, 3, "DEX +4", "Stealth +4", null, null, null, null, "Blindsight 60ft, Darkvision 60ft", "None", false, false, false);
        m("Goblin", 0.25, 100, "Small", "Humanoid", "Goblin", "Chaotic Evil", 15, 7, 30, 0, 0, 0, 8, 14, 10, 10, 8, 8, "DEX +4", "Acrobatics +4, Stealth +4", null, null, null, null, "Darkvision 60ft", "Common, Goblin", false, false, false);
        m("Skeleton", 0.25, 100, "Medium", "Undead", null, "Lawful Evil", 15, 10, 30, 0, 0, 0, 10, 8, 13, 8, 6, 5, "CON +1", "Perception +2", "Petrified, Poison", "Bludgeoning, Piercing, Slashing", null, null, "Darkvision 60ft", "None", false, false, false);
        m("Zombie", 0.25, 100, "Medium", "Undead", null, "Unaligned", 8, 13, 45, 0, 0, 0, 13, 6, 16, 8, 5, 6, "CON +3", null, "Petrified, Poison", "Bludgeoning, Piercing, Slashing", null, null, "Darkvision 60ft", "None", false, false, false);
        m("Kobold", 0.125, 25, "Small", "Humanoid", "Dragon", "Lawful Evil", 15, 5, 30, 0, 0, 0, 9, 14, 10, 8, 6, 6, "DEX +2", "Perception +2, Stealth +4", null, null, null, null, "Darkvision 60ft, Tremorsense 60ft", "Draconic, Common", false, false, false);
        m("Hobgoblin", 0.5, 200, "Medium", "Humanoid", "Goblin", "Lawful Evil", 18, 12, 10, 16, 15, 14, 16, 10, 12, 10, 10, 12, "STR +3 | CON +2", "Athletics +3, Perception +4", null, null, null, null, "Darkvision 60ft", "Common, Goblin", false, false, false);
        m("Giant Bat", 0.125, 25, "Small", "Beast", null, "Unaligned", 10, 15, 20, 30, 0, 0, 9, 14, 12, 2, 11, 6, "DEX +2", "Perception +2", null, null, null, null, "Blindsight 10ft, Darkvision 60ft", "None", false, false, false);
        m("Giant Owl", 0.125, 25, "Large", "Beast", null, "Unaligned", 10, 15, 15, 50, 0, 0, 11, 15, 13, 3, 12, 8, "DEX +2", "Perception +4, Stealth +4", null, null, null, null, "Blindsight 10ft, Darkvision 60ft", "None", false, false, false);
        m("Giant Weasel", 0.25, 100, "Small", "Beast", null, "Unaligned", 12, 16, 15, 0, 0, 0, 11, 16, 12, 4, 12, 6, "DEX +3", "Perception +3, Stealth +5", null, null, null, null, "Darkvision 60ft, Keen Smell", "None", false, false, false);

        // CR 1-3: Low Level
        m("Bugbear", 1, 200, "Medium", "Humanoid", "Goblin", "Neutral Evil", 18, 14, 30, 0, 0, 0, 16, 8, 12, 6, 8, 7, "STR +3", "Stealth +1", null, null, null, null, "Darkvision 60ft", "Common, Goblin", false, false, false);
        m("Ghoul", 1, 200, "Medium", "Undead", null, "Lawful Evil", 14, 12, 15, 30, 0, 0, 14, 12, 15, 8, 10, 7, "STR +2", "Stealth +3", "Petrified, Poison", "Bludgeoning, Piercing, Slashing", null, "Charmed", "Darkvision 60ft", "None", false, false, false);
        m("Giant Wolf", 1, 200, "Large", "Beast", null, "Neutral", 20, 15, 30, 0, 0, 0, 16, 12, 15, 10, 12, 7, "STR +3 | DEX +1 | WIS +1", "Perception +3", null, null, null, null, "Keen Hearing and Smell", "None", false, false, false);
        m("Giant Eagle", 1, 200, "Large", "Beast", null, "Neutral Good", 10, 20, 40, 80, 0, 0, 10, 15, 13, 8, 15, 6, "DEX +2 | WIS +2", "Perception +4", null, null, null, null, "Blindsight 10ft, Keen Sight", "None", false, false, false);
        m("Giant Hyena", 1, 200, "Medium", "Beast", null, "Neutral Evil", 15, 12, 13, 30, 0, 0, 15, 12, 13, 6, 7, 7, "STR +2", "Perception +1", null, null, null, null, "Darkvision 60ft, Keen Smell", "None", false, false, false);
        m("Giant Boar", 1, 200, "Large", "Beast", null, "Unaligned", 13, 27, 40, 0, 0, 0, 19, 9, 16, 4, 6, 6, "STR +4 | CON +3", "Perception +2", null, null, null, null, "Darkvision 30ft, Keen Smell", "None", false, false, false);
        m("Giant Crab", 1, 200, "Medium", "Beast", null, "Unaligned", 17, 18, 20, 0, 20, 0, 19, 12, 12, 2, 6, 4, "STR +4", "Athletics +4, Perception +1", null, "Bludgeoning, Piercing, Slashing", null, null, "Blindsight 60ft", "None", false, false, false);
        m("Giant Badger", 0.5, 200, "Medium", "Beast", null, "Unaligned", 14, 10, 12, 30, 0, 0, 14, 10, 12, 4, 6, 6, "STR +2", "Perception +2", null, null, null, null, "Darkvision 60ft", "None", false, false, false);
        m("Giant Vulture", 0.5, 200, "Large", "Beast", null, "Neutral Evil", 10, 14, 15, 50, 0, 0, 10, 14, 12, 5, 10, 7, "DEX +2", "Perception +2", null, null, null, null, "Keen Sight and Smell", "None", false, false, false);
        m("Giant Wolf Spider", 0.5, 200, "Medium", "Beast", null, "Unaligned", 14, 14, 20, 0, 0, 0, 14, 14, 12, 2, 6, 4, "DEX +2", "Stealth +4", null, null, null, null, "Blindsight 60ft, Darkvision 60ft", "None", false, false, false);
        m("Ogre", 2, 450, "Large", "Giant", null, "Chaotic Evil", 19, 59, 40, 0, 0, 0, 19, 7, 13, 8, 11, 7, "STR +4", "Athletics +4", null, null, null, null, "Darkvision 60ft", "Giant", false, false, false);
        m("Giant Wasp", 1, 200, "Large", "Beast", null, "Unaligned", 11, 15, 30, 0, 0, 0, 11, 15, 12, 2, 4, 4, "DEX +2", "Perception +2", null, null, null, null, "Blindsight 60ft", "None", false, false, false);
        m("Wraith", 3, 700, "Medium", "Undead", null, "Lawful Evil", 13, 67, 0, 0, 0, 0, 7, 15, 13, 13, 14, 12, "DEX +2 | WIS +2", "Stealth +4", "Petrified, Poison", "Bludgeoning, Piercing, Slashing, Cold, Necrotic", null, null, "Blindsight 60ft, Darkvision 60ft", "Common, Infernal", false, false, false);

        // CR 4-6: Mid-Low
        m("Wight", 4, 1100, "Medium", "Undead", null, "Lawful Evil", 15, 10, 16, 30, 0, 0, 15, 10, 16, 13, 12, 10, "STR +2 | CON +3 | CHA +3", "Perception +4", "Petrified, Poison", "Bludgeoning, Piercing, Slashing, Cold, Necrotic", null, null, "Darkvision 60ft", "Common, Infernal", false, false, false);
        m("Mummy Lord", 4, 1100, "Medium", "Undead", null, "Lawful Evil", 18, 58, 20, 0, 0, 0, 18, 8, 16, 12, 16, 14, "STR +4 | CON +3 | WIS +3 | CHA +4", "Medicine +6", "Petrified, Poison", "Bludgeoning, Piercing, Slashing, Acid, Cold, Fire, Lightning, Thunder", null, null, "Truesight 60ft", "Abyssal, Common, Infernal", false, false, false);
        m("Mind Flayer", 4, 1100, "Medium", "Aberration", null, "Lawful Evil", 14, 13, 16, 40, 0, 0, 11, 14, 16, 16, 14, 12, "DEX +2 | CON +3 | INT +3 | WIS +2", "Insight +6, Perception +6, Stealth +4", "Petrified, Poison", null, null, null, "Blindsight 60ft, Darkvision 60ft", "Deep Speech, Undercommon", false, false, false);
        m("Red Dragon Wyrmling", 4, 1100, "Large", "Dragon", null, "Chaotic Evil", 19, 75, 40, 80, 0, 0, 19, 11, 16, 10, 7, 8, "STR +4 | CON +3", "Perception +4", null, "Fire", null, null, "Blindsight 60ft, Darkvision 60ft, Keen Smell", "Draconic", false, false, false);
        m("White Dragon Wyrmling", 2, 450, "Large", "Dragon", null, "Chaotic Evil", 19, 52, 40, 0, 0, 0, 19, 11, 16, 5, 7, 8, "STR +4 | CON +3", "Perception +4", null, "Cold", null, null, "Blindsight 60ft, Darkvision 60ft, Keen Smell", "Draconic", false, false, false);
        m("Young Red Dragon", 6, 2300, "Large", "Dragon", null, "Chaotic Evil", 21, 210, 40, 100, 0, 0, 21, 11, 17, 12, 9, 11, "STR +5 | CON +3", "Perception +4", null, "Fire", null, null, "Blindsight 60ft, Darkvision 60ft, Keen Smell", "Draconic", false, false, false);
        m("Young Black Dragon", 4, 1100, "Large", "Dragon", null, "Chaotic Evil", 19, 136, 40, 0, 40, 0, 19, 12, 17, 14, 12, 13, "STR +4 | CON +3", "Perception +4", null, "Acid", null, null, "Blindsight 60ft, Darkvision 60ft, Keen Smell", "Draconic", false, false, false);
        m("Young Blue Dragon", 6, 2300, "Large", "Dragon", null, "Lawful Evil", 21, 136, 40, 0, 0, 0, 21, 12, 17, 14, 12, 13, "STR +5 | CON +3", "Perception +4", null, "Lightning", null, null, "Blindsight 60ft, Darkvision 60ft, Keen Smell", "Draconic", false, false, false);
        m("Young Green Dragon", 6, 2300, "Large", "Dragon", null, "Lawful Evil", 19, 136, 40, 0, 0, 0, 19, 12, 17, 17, 14, 13, "STR +4 | CON +3 | INT +3", "Deception +4, Perception +4, Stealth +4", null, "Poison", null, null, "Blindsight 60ft, Darkvision 60ft, Keen Smell", "Draconic", false, false, false);
        m("Young White Dragon", 4, 1100, "Large", "Dragon", null, "Chaotic Evil", 19, 108, 40, 0, 0, 0, 19, 11, 17, 5, 7, 8, "STR +4 | CON +3", "Perception +4", null, "Cold", null, null, "Blindsight 60ft, Darkvision 60ft, Keen Smell", "Draconic", false, false, false);

        // CR 7-10: Mid Level
        m("Golem, Iron", 7, 2900, "Large", "Construct", null, "Unaligned", 23, 11, 24, 40, 0, 0, 23, 11, 24, 3, 11, 1, "STR +6 | CON +7", "Athletics +6, Perception +2", "Petrified, Poison, Psychic", "Acid, Cold, Lightning, Thunder", "Bludgeoning, Piercing, Slashing", null, "Darkvision 60ft, Tremorsense 60ft", "None", false, false, false);
        m("Golem, Flesh", 5, 1800, "Huge", "Construct", null, "Unaligned", 8, 13, 21, 30, 0, 0, 27, 9, 21, 2, 3, 1, "STR +8 | CON +5", "Athletics +8", "Petrified, Poison, Psychic", "Bludgeoning, Piercing, Slashing", null, null, "Darkvision 60ft", "None", false, false, false);
        m("Golem, Stone", 7, 2900, "Huge", "Construct", null, "Unaligned", 17, 18, 26, 30, 0, 0, 21, 9, 26, 3, 8, 1, "STR +5 | CON +6", "Athletics +5, Perception +2", "Petrified, Poison, Psychic", "Acid, Cold, Lightning, Thunder", "Bludgeoning, Piercing, Slashing", null, "Darkvision 60ft, Tremorsense 60ft", "None", false, false, false);
        m("Golem, Clay", 9, 5000, "Large", "Construct", null, "Unaligned", 18, 14, 24, 30, 0, 0, 18, 14, 24, 6, 13, 1, "STR +4 | DEX +2 | CON +7 | WIS +3", "Athletics +4, Perception +7", "Petrified, Poison, Psychic", "Acid, Cold, Lightning, Thunder", "Bludgeoning, Piercing, Slashing", null, "Darkvision 60ft", "None", false, false, false);
        m("Black Dragon", 10, 5900, "Huge", "Dragon", null, "Chaotic Evil", 23, 12, 19, 60, 0, 40, 23, 12, 19, 16, 14, 15, "STR +6 | CON +4", "Perception +6", null, "Acid", null, null, "Blindsight 60ft, Darkvision 60ft, Keen Smell", "Draconic", false, true, false);
        m("Green Dragon", 10, 5900, "Huge", "Dragon", null, "Lawful Evil", 21, 12, 19, 40, 0, 0, 21, 12, 19, 19, 16, 15, "STR +5 | CON +4 | INT +4", "Deception +6, Perception +6, Stealth +6", null, "Poison", null, null, "Blindsight 60ft, Darkvision 60ft, Keen Smell", "Draconic", false, true, false);
        m("Blue Dragon", 10, 5900, "Huge", "Dragon", null, "Lawful Evil", 23, 12, 19, 40, 0, 0, 23, 12, 19, 16, 14, 15, "STR +6 | CON +4", "Perception +6", null, "Lightning", null, null, "Blindsight 60ft, Darkvision 60ft, Keen Smell", "Draconic", false, true, false);

        // CR 11-15: High Level
        m("Archmage", 12, 8400, "Medium", "Humanoid", "Any", "Any alignment", 16, 12, 15, 30, 0, 0, 10, 16, 15, 20, 17, 15, "DEX +3 | INT +5 | WIS +3", "Arcana +9, History +9, Insight +7, Perception +7", null, null, null, null, "Darkvision 60ft", "Common, Elvish, Dwarvish, Giant, Gnomish, Halfling, Orc", false, false, false);
        m("Beholder", 13, 10500, "Large", "Aberration", null, "Lawful Evil", 17, 18, 18, 0, 0, 0, 10, 17, 18, 17, 15, 17, "DEX +3 | CON +4 | INT +3 | WIS +2 | CHA +3", "Perception +6", "Petrified, Poison", "Bludgeoning, Piercing, Slashing", null, null, "Blindsight 180ft, Darkvision 180ft", "Deep Speech", true, true, false);
        m("Devil, Pit Fiend", 15, 16000, "Huge", "Fiend", null, "Lawful Evil", 29, 16, 19, 60, 0, 0, 29, 16, 19, 18, 14, 22, "STR +9 | DEX +3 | CON +4 | INT +5 | WIS +4 | CHA +6", "Deception +10, Intimidation +10, Perception +8", "Petrified, Poison", "Acid, Cold, Poison", "Good, Radiant", null, "Blindsight 60ft, Darkvision 120ft", "Abyssal, Common, Deep Speech, Infernal", true, true, false);
        m("Angel, Solar", 16, 19000, "Large", "Celestial", null, "Lawful Good", 25, 16, 25, 50, 0, 0, 25, 16, 25, 26, 25, 25, "STR +7 | DEX +3 | CON +7 | INT +8 | WIS +7 | CHA +7", "Insight +11, Perception +11", "Petrified, Poison", "Cold, Lightning, Necrotic, Psychic, Thunder", null, null, "Blindsight 60ft, Darkvision 120ft, Truesight 60ft", "Celestial, Common", true, true, false);

        // CR 16-20: Very High
        m("Balor", 19, 33000, "Gargantuan", "Fiend", null, "Chaotic Evil", 20, 18, 20, 50, 0, 0, 28, 16, 20, 17, 10, 20, "STR +9 | DEX +3 | CON +5 | INT +4 | CHA +5", "Athletics +9, Intimidation +9, Perception +4", "Petrified, Poison", "Cold, Necrotic, Psychic", "Radiant", null, "Blindsight 180ft, Darkvision 180ft", "Abyssal, Deep Speech, Infernal", true, true, false);
        m("Dragon Turtle", 20, 41000, "Gargantuan", "Beast", null, "Unaligned", 25, 18, 30, 40, 40, 0, 30, 7, 30, 2, 15, 6, "STR +10 | CON +10 | WIS +2", "Athletics +10, Perception +6", null, "Bludgeoning, Piercing, Slashing", null, null, "Blindsight 60ft, Darkvision 60ft, Tremorsense 60ft", "None", true, true, true);

        // CR 21-30: Toplevel
        m("Lich", 21, 50000, "Medium", "Undead", "Humanoid", "Lawful Evil", 18, 12, 18, 30, 0, 0, 10, 8, 18, 21, 19, 18, "DEX +3 | CON +4 | INT +5 | WIS +4 | CHA +5", "Arcana +9, History +9, Insight +8, Perception +8", "Petrified, Poison", "Acid, Cold, Fire, Lightning, Thunder, Necrotic, Psychic", null, null, "Blindsight 60ft, Darkvision 60ft", "All", true, true, false);
        m("Kraken", 23, 75000, "Gargantuan", "Monstrosity", null, "Chaotic Evil", 18, 20, 30, 40, 40, 0, 30, 10, 30, 3, 14, 6, "STR +10 | CON +10 | WIS +2", "Athletics +10, Perception +6", null, "Bludgeoning, Piercing, Slashing", null, null, "Blindsight 120ft, Darkvision 120ft, Tremorsense 120ft", "None", true, true, true);
        m("Ancient Red Dragon", 24, 90000, "Gargantuan", "Dragon", null, "Chaotic Evil", 24, 30, 27, 60, 0, 0, 30, 14, 27, 22, 18, 23, "STR +10 | CON +8 | INT +6 | WIS +4 | CHA +12", "Insight +12, Perception +8", null, "Fire", null, null, "Blindsight 180ft, Darkvision 120ft, Keen Smell", "Abyssal, Common, Draconic, Infernal", true, true, true);
        m("Ancient Blue Dragon", 23, 75000, "Gargantuan", "Dragon", null, "Lawful Evil", 24, 30, 27, 60, 0, 0, 30, 14, 27, 20, 18, 21, "STR +10 | CON +8 | INT +5 | WIS +4 | CHA +11", "Insight +11, Perception +8", null, "Lightning", null, null, "Blindsight 180ft, Darkvision 120ft, Keen Smell", "Abyssal, Common, Draconic, Infernal", true, true, true);
        m("Ancient Green Dragon", 23, 75000, "Gargantuan", "Dragon", null, "Lawful Evil", 24, 30, 27, 40, 0, 0, 27, 14, 27, 25, 20, 21, "STR +8 | CON +8 | INT +7 | WIS +5 | CHA +11", "Deception +11, Insight +11, Perception +8, Stealth +11", null, "Poison", null, null, "Blindsight 180ft, Darkvision 120ft, Keen Smell", "Abyssal, Common, Draconic, Infernal, Sylvan", true, true, true);
        m("Ancient Black Dragon", 22, 62000, "Gargantuan", "Dragon", null, "Chaotic Evil", 24, 30, 27, 40, 0, 40, 27, 14, 27, 18, 16, 19, "STR +8 | CON +8 | INT +4 | WIS +3 | CHA +9", "Perception +7", null, "Acid", null, null, "Blindsight 180ft, Darkvision 120ft, Keen Smell", "Abyssal, Common, Draconic, Infernal", true, true, true);
        m("Ancient White Dragon", 21, 50000, "Gargantuan", "Dragon", null, "Chaotic Evil", 24, 30, 27, 40, 0, 0, 27, 12, 27, 8, 13, 11, "STR +8 | CON +8 | WIS +3", "Perception +7", null, "Cold", null, null, "Blindsight 180ft, Darkvision 120ft, Keen Smell", "Common, Draconic", true, true, true);
        m("Ancient Gold Dragon", 24, 90000, "Gargantuan", "Dragon", null, "Lawful Good", 24, 30, 27, 80, 0, 0, 30, 14, 27, 25, 22, 27, "STR +10 | CON +8 | INT +7 | WIS +6 | CHA +13", "Insight +13, Perception +10", null, "Fire", null, null, "Blindsight 180ft, Darkvision 120ft, Keen Smell", "Abyssal, Common, Draconic, Elvish, Giant, Infernal, Sylvan", true, true, true);
        m("Ancient Silver Dragon", 23, 75000, "Gargantuan", "Dragon", null, "Lawful Good", 24, 30, 27, 80, 0, 0, 27, 16, 27, 20, 18, 21, "STR +8 | DEX +3 | CON +8 | INT +5 | WIS +4 | CHA +11", "Perception +8", null, "Cold", null, null, "Blindsight 180ft, Darkvision 120ft, Keen Smell", "Common, Draconic, Giant", true, true, true);
        m("Elder Brain", 22, 62000, "Gargantuan", "Aberration", null, "Lawful Evil", 20, 18, 1, 25, 15, 6, 1, 1, 1, 25, 15, 15, "INT +7 | WIS +3 | CHA +3", "Insight +7, Perception +7", "Petrified, Poison", "Acid, Cold, Fire, Lightning, Thunder", null, null, "Blindsight 120ft, Darkvision 120ft", "Deep Speech, Telepathy 120ft", true, true, true);
        m("Tarrasque", 30, 230000, "Gargantuan", "Monstrosity", null, "Unaligned", 25, 30, 50, 60, 0, 0, 67, 25, 50, 3, 14, 6, "STR +18 | CON +15 | WIS +2", "Athletics +18, Perception +6", "Petrified, Poison", "Bludgeoning, Piercing, Slashing, Acid, Cold, Fire, Lightning, Necrotic, Psychic, Thunder", null, null, "Blindsight 120ft, Darkvision 120ft, Tremorsense 120ft", "None", true, true, true);

        // Elementals
        m("Air Elemental", 5, 1800, "Large", "Elemental", null, "Neutral", 11, 12, 15, 0, 150, 0, 4, 15, 13, 6, 6, 6, "DEX +2 | CON +1", "Stealth +4", "Petrified, Poison", "Bludgeoning, Piercing, Slashing", null, null, "Darkvision 60ft", "None", false, false, false);
        m("Earth Elemental", 5, 1800, "Large", "Elemental", null, "Neutral", 17, 18, 26, 0, 0, 0, 21, 9, 26, 5, 6, 6, "STR +5 | CON +6", "Athletics +5, Perception +1", "Petrified, Poison", "Acid, Cold, Lightning, Thunder", "Bludgeoning, Piercing, Slashing", null, "Darkvision 60ft, Tremorsense 60ft", "None", false, false, false);
        m("Fire Elemental", 5, 1800, "Large", "Elemental", null, "Neutral", 13, 12, 15, 0, 100, 0, 6, 15, 13, 6, 6, 6, "DEX +2 | CON +1", "Stealth +4", "Petrified, Poison", "Cold", "Acid", null, "Darkvision 60ft", "None", false, false, false);
        m("Water Elemental", 5, 1800, "Large", "Elemental", null, "Neutral", 11, 12, 15, 0, 50, 0, 6, 15, 13, 6, 6, 6, "DEX +2 | CON +1", "Stealth +4", "Petrified, Poison", "Lightning, Thunder", "Bludgeoning, Piercing, Slashing", null, "Darkvision 60ft", "None", false, false, false);

        // Fey
        m("Archfey", 12, 8400, "Medium", "Fey", null, "Neutral", 17, 12, 15, 30, 0, 0, 10, 16, 15, 20, 17, 15, "DEX +3 | INT +5 | WIS +3 | CHA +5", "Deception +9, Insight +7, Perception +7, Persuasion +9", null, null, null, null, "Darkvision 60ft", "Common, Elvish, Sylvan", true, true, false);
        m("Dryad", 1, 200, "Medium", "Fey", null, "Neutral Good", 10, 12, 15, 30, 0, 0, 11, 14, 12, 10, 15, 17, "DEX +2 | WIS +2 | CHA +3", "Athletics +1, Nature +3, Stealth +4", null, null, null, null, "Darkvision 60ft", "Elvish, Sylvan", false, false, false);
        m("Satyr", 1, 200, "Medium", "Fey", null, "Chaotic Good", 14, 12, 15, 30, 0, 0, 14, 12, 15, 10, 10, 16, "STR +2 | DEX +1 | CON +2 | CHA +3", "Performance +5, Persuasion +5", null, null, null, null, "Darkvision 60ft", "Elvish, Sylvan", false, false, false);
        m("Banshee", 4, 1100, "Medium", "Undead", null, "Chaotic Evil", 10, 12, 15, 30, 0, 0, 7, 15, 13, 15, 16, 19, "DEX +2 | WIS +3 | CHA +4", "Insight +7, Perception +7", "Petrified, Poison", "Bludgeoning, Piercing, Slashing, Cold, Necrotic", null, null, "Blindsight 60ft, Darkvision 60ft", "Common", false, false, false);

        // More Undead
        m("Vampire", 13, 10500, "Medium", "Undead", null, "Lawful Evil", 16, 12, 15, 40, 0, 0, 18, 16, 16, 18, 16, 18, "STR +4 | DEX +3 | CON +3 | INT +4 | WIS +3 | CHA +4", "Insight +7, Perception +7, Persuasion +7", "Petrified, Poison", "Bludgeoning, Piercing, Slashing, Cold, Necrotic", "Radiant, Silver", null, "Darkvision 60ft", "Common, Draconic, Infernal", true, true, false);
        m("Vampire Spawn", 5, 1800, "Medium", "Undead", null, "Lawful Evil", 16, 12, 15, 40, 0, 0, 18, 16, 16, 10, 11, 14, "STR +4 | DEX +3 | CON +3", "Insight +2, Perception +2, Persuasion +3", "Petrified, Poison", "Bludgeoning, Piercing, Slashing, Cold, Necrotic", "Radiant, Silver", null, "Darkvision 60ft", "Common", false, false, false);
        m("Ghost", 1, 200, "Medium", "Undead", null, "Lawful Evil", 10, 12, 15, 30, 0, 0, 7, 15, 13, 11, 14, 12, "DEX +2 | WIS +2", "Insight +3, Perception +3, Stealth +4", "Petrified, Poison", "Bludgeoning, Piercing, Slashing, Cold, Necrotic, Psychic", null, null, "Blindsight 60ft, Darkvision 60ft", "Common", false, false, false);
        m("Specter", 1, 200, "Medium", "Undead", null, "Unaligned", 10, 12, 15, 30, 0, 0, 7, 15, 13, 7, 11, 11, "DEX +2", "Stealth +4", "Petrified, Poison", "Bludgeoning, Piercing, Slashing, Cold, Necrotic", null, null, "Blindsight 60ft, Darkvision 60ft", "None", false, false, false);
        m("Shadow", 1, 200, "Medium", "Undead", null, "Chaotic Evil", 10, 12, 15, 30, 0, 0, 7, 15, 13, 3, 11, 11, "DEX +2", "Stealth +4", "Petrified, Poison", "Bludgeoning, Piercing, Slashing, Cold, Necrotic", null, null, "Darkvision 60ft", "None", false, false, false);

        // Genies
        m("Djinni", 9, 5000, "Large", "Elemental", null, "Chaotic Good", 17, 12, 15, 50, 0, 0, 6, 15, 13, 16, 14, 18, "DEX +2 | INT +3 | WIS +2 | CHA +4", "Insight +4, Perception +4, Persuasion +8", "Petrified, Poison", "Bludgeoning, Piercing, Slashing", null, null, "Darkvision 60ft", "Aquan, Common, Giant", false, false, false);
        m("Dao", 9, 5000, "Large", "Elemental", null, "Lawful Neutral", 17, 18, 26, 30, 0, 0, 21, 9, 26, 10, 11, 11, "STR +5 | CON +6", "Athletics +5, Perception +2", "Petrified, Poison", "Acid, Cold, Lightning, Thunder", "Bludgeoning, Piercing, Slashing", null, "Darkvision 60ft, Tremorsense 60ft", "Common, Dwarvish, Terran", false, false, false);
        m("Efreeti", 9, 5000, "Large", "Elemental", null, "Chaotic Evil", 17, 12, 15, 30, 0, 0, 6, 15, 13, 16, 14, 18, "DEX +2 | INT +3 | WIS +2 | CHA +4", "Insight +4, Perception +4, Persuasion +8", "Petrified, Poison", "Bludgeoning, Piercing, Slashing", null, null, "Darkvision 60ft", "Common, Fire Giant, Ignan", false, false, false);
        m("Marid", 11, 7200, "Huge", "Elemental", null, "Chaotic Good", 17, 12, 15, 50, 0, 0, 6, 15, 13, 16, 14, 18, "DEX +2 | INT +3 | WIS +2 | CHA +4", "Insight +4, Perception +4, Persuasion +8", "Petrified, Poison", "Bludgeoning, Piercing, Slashing", null, null, "Darkvision 60ft", "Aquan, Common, Giant", false, false, false);

        // Giants
        m("Giant, Frost", 5, 1800, "Huge", "Giant", null, "Chaotic Evil", 19, 12, 19, 40, 0, 0, 23, 8, 19, 5, 9, 9, "STR +6 | CON +4", "Athletics +6, Perception +2", null, "Cold", null, null, "Darkvision 60ft", "Giant", false, false, false);
        m("Giant, Fire", 5, 1800, "Huge", "Giant", null, "Chaotic Evil", 19, 12, 19, 40, 0, 0, 23, 8, 19, 5, 9, 9, "STR +6 | CON +4", "Athletics +6, Perception +2", null, "Fire", null, null, "Darkvision 60ft", "Giant", false, false, false);
        m("Giant, Storm", 9, 5000, "Huge", "Giant", null, "Chaotic Evil", 19, 12, 19, 40, 0, 0, 23, 8, 19, 16, 14, 15, "STR +6 | CON +4 | INT +3 | WIS +2 | CHA +2", "Athletics +6, Perception +4", null, "Lightning", null, null, "Darkvision 60ft", "Giant", false, false, false);
        m("Giant, Hill", 1, 200, "Huge", "Giant", null, "Chaotic Evil", 19, 12, 19, 30, 0, 0, 19, 8, 14, 6, 10, 8, "STR +4 | CON +2", "Athletics +4, Perception +2", null, null, null, null, "Darkvision 60ft", "Giant", false, false, false);
        m("Giant, Stone", 3, 700, "Huge", "Giant", null, "Chaotic Evil", 19, 12, 19, 30, 0, 0, 21, 8, 16, 10, 8, 8, "STR +5 | CON +3", "Athletics +5, Perception +1", null, null, null, null, "Darkvision 60ft", "Giant", false, false, false);
        m("Giant, Cloud", 9, 5000, "Huge", "Giant", null, "Chaotic Good", 19, 12, 19, 50, 0, 0, 23, 8, 19, 16, 14, 15, "STR +6 | CON +4 | INT +3 | WIS +2 | CHA +2", "Athletics +6, Perception +4", null, null, null, null, "Darkvision 60ft", "Giant", false, false, false);

        // More Humanoids
        m("Kobold Dragonkeeper", 1, 200, "Small", "Humanoid", "Dragon", "Lawful Evil", 9, 14, 10, 10, 10, 8, 14, 2, 8, 10, 10, 8, "DEX +2", "Perception +2, Stealth +4", null, null, null, null, "Darkvision 60ft, Tremorsense 60ft", "Draconic, Common", false, false, false);
        m("Goblin Boss", 1, 200, "Small", "Humanoid", "Goblin", "Chaotic Evil", 13, 14, 10, 15, 10, 12, 13, 10, 10, 10, 10, 10, "DEX +2", "Stealth +4", null, null, null, null, "Darkvision 60ft", "Common, Goblin", false, false, false);

        log.info("Populated {} monsters", monsterRepository.countByActiveTrue());
    }

    private void m(String name, double cr, int xp, String size, String type, String subtype, String alignment,
                    int ac, int hp, int speed, int flySpeed, int swimSpeed, int climbSpeed,
                    int str, int dex, int con, int intel, int wis, int cha,
                    String savingThrows, String skills, String damageImmunities, String damageResistances,
                    String damageVulnerabilities, String conditionImmunities, String senses, String languages,
                    boolean hasLairActions, boolean hasLegendaryActions, boolean hasRegionalEffects) {
        if (monsterRepository.findByName(name).isPresent()) return;
        Dnd5eMonster monster = new Dnd5eMonster();
        monster.setName(name); monster.setCr(cr); monster.setXp(xp);
        monster.setSize(size); monster.setType(type); monster.setSubtype(subtype);
        monster.setAlignment(alignment); monster.setAc(ac); monster.setHp(hp);
        monster.setSpeed(speed); monster.setFlySpeed(flySpeed); monster.setSwimSpeed(swimSpeed);
        monster.setClimbSpeed(climbSpeed);
        monster.setStr(str); monster.setDex(dex); monster.setCon(con);
        monster.setIntel(intel); monster.setWis(wis); monster.setCha(cha);
        monster.setSavingThrows(savingThrows); monster.setSkills(skills);
        monster.setDamageImmunities(damageImmunities); monster.setDamageResistances(damageResistances);
        monster.setDamageVulnerabilities(damageVulnerabilities);
        monster.setConditionImmunities(conditionImmunities);
        monster.setSenses(senses); monster.setLanguages(languages);
        monster.setHasLairActions(hasLairActions); monster.setHasLegendaryActions(hasLegendaryActions);
        monster.setHasRegionalEffects(hasRegionalEffects); monster.setActive(true);
        monsterRepository.save(monster);
    }

    @Transactional
    public void populateMonsterAbilities() {
        a("Goblin", "Attack", "Melee", "Scimitar", "Melee Weapon Attack: +4 to hit, reach 5ft. Hit: 5 (1d6 + 2) slashing.", 4, "1d6 + 2", "Slashing", 0, null, 5, 0, null);
        a("Goblin", "Attack", "Ranged", "Shortbow", "Ranged Weapon Attack: +4 to hit, range 80/320ft. Hit: 5 (1d6 + 2) piercing.", 4, "1d6 + 2", "Piercing", 0, null, 80, 0, null);
        a("Skeleton", "Attack", "Melee", "Any Melee Weapon", "Melee Weapon Attack: +4 to hit, reach 5ft. Hit: 5 (1d8 + 2) slashing.", 4, "1d8 + 2", "Slashing", 0, null, 5, 0, null);
        a("Skeleton", "Trait", null, "Undead Fortitude", "If damage reduces the skeleton to 0 HP, it makes a CON save DC 5 + damage. On 10+, it drops to 1 HP instead.", 0, null, null, 5, "CON", 0, 0, null);
        a("Zombie", "Attack", "Melee", "Slam", "Melee Weapon Attack: +3 to hit, reach 5ft. Hit: 5 (1d6 + 3) bludgeoning.", 3, "1d6 + 3", "Bludgeoning", 0, null, 5, 0, null);
        a("Zombie", "Trait", null, "Undead Fortitude", "If damage reduces the zombie to 0 HP, it makes a CON save DC 5 + damage. On 10+, it drops to 1 HP instead.", 0, null, null, 5, "CON", 0, 0, null);
        a("Beholder", "Attack", "Ranged", "Eye Ray", "Ranged Spell Attack: +8 to hit, range 150ft. Hit: 11 (2d10) psychic, target must succeed on DC 17 WIS save or be charmed 1 minute.", 8, "2d10", "Psychic", 17, "WIS", 150, 0, null);
        a("Beholder", "Legendary Action", null, "Eye Ray", "The beholder fires one eye ray.", 0, null, null, 0, null, 0, 1, null);
        a("Beholder", "Lair Action", null, "Lair Actions", "Initiative 20. The beholder targets one creature within 120 ft. If holding a magic item, DC 17 WIS save or drop it.", 0, null, null, 17, "WIS", 120, 0, null);
        a("Lich", "Attack", "Melee", "Touch", "Melee Spell Attack: +9 to hit, reach 5ft. Hit: 10 (3d6) necrotic.", 9, "3d6", "Necrotic", 0, null, 5, 0, null);
        a("Lich", "Attack", "Ranged", "Life Drain", "Ranged Spell Attack: +9 to hit, range 60ft. Hit: 21 (4d10) necrotic, target's HP max reduced by damage.", 9, "4d10", "Necrotic", 0, null, 60, 0, null);
        a("Lich", "Trait", null, "Legendary Resistance (3/Day)", "If the lich fails a saving throw, it can choose to succeed instead.", 0, null, null, 0, null, 0, 3, null);
        a("Ancient Red Dragon", "Attack", "Melee", "Bite", "Melee Weapon Attack: +17 to hit, reach 15ft. Hit: 23 (4d10 + 10) piercing.", 17, "4d10 + 10", "Piercing", 0, null, 15, 0, null);
        a("Ancient Red Dragon", "Attack", "Melee", "Claw", "Melee Weapon Attack: +17 to hit, reach 15ft. Hit: 19 (3d8 + 10) slashing.", 17, "3d8 + 10", "Slashing", 0, null, 15, 0, null);
        a("Ancient Red Dragon", "Attack", "Ranged", "Fire Breath", "Each creature in a 150ft line must make a DC 24 DEX save. On a failure: 56 (16d6) fire damage. On success: half.", 0, "16d6", "Fire", 24, "DEX", 150, 0, "5-6");
        a("Ancient Red Dragon", "Trait", null, "Legendary Resistance (3/Day)", "If the dragon fails a saving throw, it can choose to succeed instead.", 0, null, null, 0, null, 0, 3, null);
        a("Mind Flayer", "Attack", "Melee", "Tentacles", "Melee Weapon Attack: +4 to hit, reach 5ft. Hit: 7 (2d4 + 2) bludgeoning, target is grappled (escape DC 11).", 4, "2d4 + 2", "Bludgeoning", 11, "STR", 5, 0, null);
        a("Mind Flayer", "Attack", "Melee", "Bite", "Melee Weapon Attack: +4 to hit, reach 5ft. Hit: 10 (3d6 + 2) piercing plus 7 (3d4) psychic, target's HP max reduced.", 4, "3d6 + 2", "Piercing", 0, null, 5, 0, null);
        a("Mind Flayer", "Trait", null, "Magic Resistance", "The mind flayer has advantage on saving throws against spells.", 0, null, null, 0, null, 0, 0, null);
        a("Tarrasque", "Attack", "Melee", "Bite", "Melee Weapon Attack: +18 to hit, reach 5ft. Hit: 36 (4d12 + 18) piercing, target grappled (escape DC 28).", 18, "4d12 + 18", "Piercing", 28, "STR", 5, 0, null);
        a("Tarrasque", "Attack", "Melee", "Horn", "Melee Weapon Attack: +18 to hit, reach 10ft. Hit: 27 (3d10 + 18) piercing.", 18, "3d10 + 18", "Piercing", 0, null, 10, 0, null);
        a("Tarrasque", "Attack", "Melee", "Tail", "Melee Weapon Attack: +18 to hit, reach 15ft. Hit: 31 (4d8 + 18) bludgeoning.", 18, "4d8 + 18", "Bludgeoning", 0, null, 15, 0, null);
        a("Tarrasque", "Trait", null, "Legendary Resistance (3/Day)", "If the Tarrasque fails a saving throw, it can choose to succeed instead.", 0, null, null, 0, null, 0, 3, null);
        a("Kraken", "Attack", "Melee", "Tentacle", "Melee Weapon Attack: +10 to hit, reach 40ft. Hit: 22 (3d8 + 10) bludgeoning, target grappled (escape DC 20).", 10, "3d8 + 10", "Bludgeoning", 20, "STR", 40, 0, null);
        a("Kraken", "Attack", "Melee", "Bite", "Melee Weapon Attack: +10 to hit, reach 15ft. Hit: 27 (4d10 + 10) piercing.", 10, "4d10 + 10", "Piercing", 0, null, 15, 0, null);
        a("Kraken", "Trait", null, "Innate Spellcasting", "The kraken's innate spellcasting ability is CHA (DC 20, +10 to hit). At will: create or destroy water, fog cloud, gust of wind, ice storm, wall of water.", 0, null, null, 20, "CHA", 0, 0, null);
        a("Balor", "Attack", "Melee", "Flame Sword", "Melee Weapon Attack: +14 to hit, reach 10ft. Hit: 22 (3d10 + 9) fire, balor can move without provoking opportunity attacks.", 14, "3d10 + 9", "Fire", 0, null, 10, 0, null);
        a("Balor", "Attack", "Ranged", "Fire Breath", "Each creature in a 150ft line must make a DC 20 DEX save. On a failure: 44 (12d8) fire damage. On success: half.", 0, "12d8", "Fire", 20, "DEX", 150, 0, "5-6");
        a("Balor", "Trait", null, "Legendary Resistance (3/Day)", "If the balor fails a saving throw, it can choose to succeed instead.", 0, null, null, 0, null, 0, 3, null);
        a("Elder Brain", "Attack", "Ranged", "Brain Melter", "Ranged Spell Attack: +13 to hit, range 120ft. Hit: 27 (5d10) acid, target must succeed on DC 19 CON save or be poisoned 1 minute.", 13, "5d10", "Acid", 19, "CON", 120, 0, null);
        a("Elder Brain", "Trait", null, "Magic Resistance", "The elder brain has advantage on saving throws against spells.", 0, null, null, 0, null, 0, 0, null);
        a("Vampire", "Attack", "Melee", "Bite", "Melee Weapon Attack: +7 to hit, reach 5ft. Hit: 11 (2d6 + 4) piercing plus 7 (3d4) necrotic. Target's HP max reduced by necrotic damage.", 7, "2d6 + 4", "Piercing", 0, null, 5, 0, null);
        a("Vampire", "Trait", null, "Legendary Resistance (3/Day)", "If the vampire fails a saving throw, it can choose to succeed instead.", 0, null, null, 0, null, 0, 3, null);
        a("Vampire", "Legendary Action", null, "Bite", "The vampire makes a bite attack.", 0, null, null, 0, null, 0, 1, null);
        a("Vampire", "Lair Action", null, "Lair Actions", "Initiative 20. The vampire can cause fear, darkness, or summon shadows.", 0, null, null, 15, "WIS", 60, 0, null);
        a("Ogre", "Attack", "Melee", "Club", "Melee Weapon Attack: +6 to hit, reach 5ft. Hit: 14 (3d8 + 4) bludgeoning.", 6, "3d8 + 4", "Bludgeoning", 0, null, 5, 0, null);
        a("Ghoul", "Attack", "Melee", "Claw", "Melee Weapon Attack: +4 to hit, reach 5ft. Hit: 7 (2d4 + 2) necrotic plus 7 (2d6) poison. Target must succeed on DC 12 CON save or be paralyzed 1 hour.", 4, "2d4 + 2", "Necrotic", 12, "CON", 5, 0, null);
        a("Ghoul", "Trait", null, "Paralyzing Touch", "When the ghoul hits a humanoid with a claw attack, the target must succeed on a DC 12 CON save or be paralyzed for 1 hour.", 0, null, null, 12, "CON", 0, 0, null);
        a("Wight", "Attack", "Melee", "Scimitar", "Melee Weapon Attack: +4 to hit, reach 5ft. Hit: 5 (1d6 + 2) slashing plus 7 (2d6) necrotic.", 4, "1d6 + 2", "Slashing", 0, null, 5, 0, null);
        a("Wight", "Trait", null, "Undead Fortitude", "If damage reduces the wight to 0 HP, it makes a CON save DC 5 + damage. On 10+, it drops to 1 HP instead.", 0, null, null, 5, "CON", 0, 0, null);
        a("Mummy Lord", "Attack", "Melee", "Slam", "Melee Weapon Attack: +6 to hit, reach 5ft. Hit: 10 (3d4 + 4) bludgeoning plus 7 (3d4) necrotic.", 6, "3d4 + 4", "Bludgeoning", 0, null, 5, 0, null);
        a("Mummy Lord", "Trait", null, "Mummy's Rot", "A creature the mummy lord damages with its slam must succeed on a DC 15 CON save or contract mummy's rot.", 0, null, null, 15, "CON", 0, 0, null);
        a("Black Dragon", "Attack", "Melee", "Bite", "Melee Weapon Attack: +8 to hit, reach 10ft. Hit: 15 (2d10 + 4) piercing.", 8, "2d10 + 4", "Piercing", 0, null, 10, 0, null);
        a("Black Dragon", "Attack", "Ranged", "Acid Breath", "Each creature in a 90ft cone must make a DC 19 DEX save. On a failure: 45 (13d6) acid damage. On success: half.", 0, "13d6", "Acid", 19, "DEX", 90, 0, "5-6");
        a("Black Dragon", "Trait", null, "Legendary Resistance (3/Day)", "If the dragon fails a saving throw, it can choose to succeed instead.", 0, null, null, 0, null, 0, 3, null);
        a("Green Dragon", "Attack", "Melee", "Bite", "Melee Weapon Attack: +7 to hit, reach 10ft. Hit: 13 (2d8 + 4) piercing plus 4 (1d8) poison.", 7, "2d8 + 4", "Piercing", 0, null, 10, 0, null);
        a("Green Dragon", "Attack", "Ranged", "Miasma Breath", "Each creature in a 15ft cube must make a DC 19 CON save. On a failure: 45 (13d6) poison damage. On success: half.", 0, "13d6", "Poison", 19, "CON", 15, 0, "5-6");
        a("Blue Dragon", "Attack", "Melee", "Bite", "Melee Weapon Attack: +8 to hit, reach 10ft. Hit: 15 (2d10 + 4) piercing.", 8, "2d10 + 4", "Piercing", 0, null, 10, 0, null);
        a("Blue Dragon", "Attack", "Ranged", "Lightning Breath", "Each creature in a 90ft line must make a DC 19 DEX save. On a failure: 45 (13d6) lightning damage. On success: half.", 0, "13d6", "Lightning", 19, "DEX", 90, 0, "5-6");
        a("Beholder", "Legendary Action", null, "Bite (Recharge 5-6)", "The beholder makes a bite attack.", 0, null, null, 0, null, 0, 0, "5-6");
        a("Ancient Red Dragon", "Legendary Action", null, "Bite", "The dragon makes a bite attack.", 0, null, null, 0, null, 0, 1, null);
        a("Ancient Red Dragon", "Legendary Action", null, "Wings", "The dragon spreads its wings, knocking prone any Large or smaller creature within 10 ft.", 0, null, null, 0, null, 10, 1, null);
        a("Ancient Red Dragon", "Lair Action", null, "Lair Actions", "Initiative 20. Fire erupts in a 10-ft-radius cylinder. DC 24 DEX save or 11 (2d10) fire damage.", 0, null, null, 24, "DEX", 120, 0, null);
        a("Tarrasque", "Legendary Action", null, "Bite", "The Tarrasque makes a bite attack.", 0, null, null, 0, null, 0, 1, null);
        a("Tarrasque", "Legendary Action", null, "Horn", "The Tarrasque makes a horn attack.", 0, null, null, 0, null, 0, 1, null);
        a("Tarrasque", "Legendary Action", null, "Tail", "The Tarrasque makes a tail attack.", 0, null, null, 0, null, 0, 1, null);
        a("Kraken", "Legendary Action", null, "Tentacle", "The kraken makes a tentacle attack.", 0, null, null, 0, null, 0, 1, null);
        a("Kraken", "Legendary Action", null, "Bite", "The kraken makes a bite attack.", 0, null, null, 0, null, 0, 1, null);
        a("Balor", "Legendary Action", null, "Flame Sword", "The balor makes a flame sword attack.", 0, null, null, 0, null, 0, 1, null);
        a("Balor", "Legendary Action", null, "Teleport", "The balor teleports up to 60 ft to an unoccupied space it can see.", 0, null, null, 0, null, 60, 1, null);
        a("Golem, Iron", "Attack", "Melee", "Slam", "Melee Weapon Attack: +8 to hit, reach 5ft. Hit: 17 (3d8 + 6) bludgeoning.", 8, "3d8 + 6", "Bludgeoning", 0, null, 5, 0, null);
        a("Golem, Iron", "Trait", null, "Magic Immunity", "The golem has immunity to damage from nonmagical weapons and spells.", 0, null, null, 0, null, 0, 0, null);
        a("Golem, Stone", "Attack", "Melee", "Slam", "Melee Weapon Attack: +7 to hit, reach 5ft. Hit: 15 (2d8 + 5) bludgeoning.", 7, "2d8 + 5", "Bludgeoning", 0, null, 5, 0, null);
        a("Golem, Stone", "Trait", null, "Magic Immunity", "The golem has immunity to damage from nonmagical weapons and spells.", 0, null, null, 0, null, 0, 0, null);
        a("Golem, Flesh", "Attack", "Melee", "Slam", "Melee Weapon Attack: +8 to hit, reach 5ft. Hit: 19 (3d8 + 8) bludgeoning.", 8, "3d8 + 8", "Bludgeoning", 0, null, 5, 0, null);
        a("Golem, Clay", "Attack", "Melee", "Slam", "Melee Weapon Attack: +6 to hit, reach 5ft. Hit: 13 (2d8 + 4) bludgeoning.", 6, "2d8 + 4", "Bludgeoning", 0, null, 5, 0, null);
        a("Golem, Clay", "Trait", null, "Healing Touch", "As a bonus action, the golem can heal a creature within 5 ft for 2d8 + 4 HP.", 0, null, null, 0, null, 5, 0, null);
        a("Archfey", "Attack", "Melee", "Fey Blade", "Melee Spell Attack: +9 to hit, reach 5ft. Hit: 14 (3d8 + 4) psychic damage.", 9, "3d8 + 4", "Psychic", 0, null, 5, 0, null);
        a("Archfey", "Trait", null, "Magic Resistance", "The archfey has advantage on saving throws against spells.", 0, null, null, 0, null, 0, 0, null);
        a("Archfey", "Legendary Action", null, "Fey Blade", "The archfey makes a fey blade attack.", 0, null, null, 0, null, 0, 1, null);
        a("Archfey", "Lair Action", null, "Lair Actions", "Initiative 20. The archfey can create illusions, charm creatures, or cause plants to grow.", 0, null, null, 17, "WIS", 120, 0, null);
        a("Devil, Pit Fiend", "Attack", "Melee", "Bite", "Melee Weapon Attack: +11 to hit, reach 5ft. Hit: 11 (2d6 + 5) piercing plus 7 (3d4) fire.", 11, "2d6 + 5", "Piercing", 0, null, 5, 0, null);
        a("Devil, Pit Fiend", "Attack", "Melee", "Claw", "Melee Weapon Attack: +11 to hit, reach 5ft. Hit: 10 (2d4 + 5) slashing.", 11, "2d4 + 5", "Slashing", 0, null, 5, 0, null);
        a("Devil, Pit Fiend", "Trait", null, "Legendary Resistance (3/Day)", "If the devil fails a saving throw, it can choose to succeed instead.", 0, null, null, 0, null, 0, 3, null);
        a("Angel, Solar", "Attack", "Melee", "Morningstar", "Melee Weapon Attack: +11 to hit, reach 5ft. Hit: 14 (2d8 + 7) radiant.", 11, "2d8 + 7", "Radiant", 0, null, 5, 0, null);
        a("Angel, Solar", "Trait", null, "Magic Resistance", "The solar has advantage on saving throws against spells.", 0, null, null, 0, null, 0, 0, null);
        a("Angel, Solar", "Legendary Action", null, "Morningstar", "The solar makes a morningstar attack.", 0, null, null, 0, null, 0, 1, null);
        a("Dragon Turtle", "Attack", "Melee", "Bite", "Melee Weapon Attack: +12 to hit, reach 15ft. Hit: 22 (3d10 + 10) piercing.", 12, "3d10 + 10", "Piercing", 0, null, 15, 0, null);
        a("Dragon Turtle", "Trait", null, "Burrow", "The dragon turtle can burrow into sand or mud as an action.", 0, null, null, 0, null, 0, 0, null);
        a("Dragon Turtle", "Legendary Action", null, "Bite", "The dragon turtle makes a bite attack.", 0, null, null, 0, null, 0, 1, null);
        a("Dragon Turtle", "Lair Action", null, "Lair Actions", "Initiative 20. The dragon turtle can create a tidal wave or cause the ground to shake.", 0, null, null, 20, "STR", 120, 0, null);
        a("Ancient Blue Dragon", "Attack", "Melee", "Bite", "Melee Weapon Attack: +17 to hit, reach 15ft. Hit: 23 (4d10 + 10) piercing.", 17, "4d10 + 10", "Piercing", 0, null, 15, 0, null);
        a("Ancient Blue Dragon", "Attack", "Ranged", "Lightning Breath", "Each creature in a 150ft line must make a DC 24 DEX save. On a failure: 56 (16d6) lightning damage. On success: half.", 0, "16d6", "Lightning", 24, "DEX", 150, 0, "5-6");
        a("Ancient Green Dragon", "Attack", "Melee", "Bite", "Melee Weapon Attack: +14 to hit, reach 15ft. Hit: 19 (3d8 + 8) piercing plus 10 (3d6) poison.", 14, "3d8 + 8", "Piercing", 0, null, 15, 0, null);
        a("Ancient Green Dragon", "Attack", "Ranged", "Miasma Breath", "Each creature in a 15ft cube must make a DC 24 CON save. On a failure: 56 (16d6) poison damage. On success: half.", 0, "16d6", "Poison", 24, "CON", 15, 0, "5-6");
        a("Ancient Black Dragon", "Attack", "Melee", "Bite", "Melee Weapon Attack: +14 to hit, reach 15ft. Hit: 19 (3d8 + 8) piercing.", 14, "3d8 + 8", "Piercing", 0, null, 15, 0, null);
        a("Ancient Black Dragon", "Attack", "Ranged", "Acid Breath", "Each creature in a 150ft cone must make a DC 24 DEX save. On a failure: 56 (16d6) acid damage. On success: half.", 0, "16d6", "Acid", 24, "DEX", 150, 0, "5-6");
        a("Ancient White Dragon", "Attack", "Melee", "Bite", "Melee Weapon Attack: +14 to hit, reach 15ft. Hit: 19 (3d8 + 8) piercing.", 14, "3d8 + 8", "Piercing", 0, null, 15, 0, null);
        a("Ancient White Dragon", "Attack", "Ranged", "Cold Breath", "Each creature in a 90ft cone must make a DC 24 CON save. On a failure: 56 (16d6) cold damage. On success: half.", 0, "16d6", "Cold", 24, "CON", 90, 0, "5-6");
        a("Ancient Gold Dragon", "Attack", "Melee", "Bite", "Melee Weapon Attack: +17 to hit, reach 15ft. Hit: 23 (4d10 + 10) piercing.", 17, "4d10 + 10", "Piercing", 0, null, 15, 0, null);
        a("Ancient Gold Dragon", "Attack", "Ranged", "Fire Breath", "Each creature in a 150ft cone must make a DC 24 DEX save. On a failure: 56 (16d6) fire damage. On success: half.", 0, "16d6", "Fire", 24, "DEX", 150, 0, "5-6");
        a("Ancient Silver Dragon", "Attack", "Melee", "Bite", "Melee Weapon Attack: +14 to hit, reach 15ft. Hit: 19 (3d8 + 8) piercing.", 14, "3d8 + 8", "Piercing", 0, null, 15, 0, null);
        a("Ancient Silver Dragon", "Attack", "Ranged", "Cold Breath", "Each creature in a 90ft cone must make a DC 24 CON save. On a failure: 56 (16d6) cold damage. On success: half.", 0, "16d6", "Cold", 24, "CON", 90, 0, "5-6");
        a("Elder Brain", "Trait", null, "Magic Resistance", "The elder brain has advantage on saving throws against spells.", 0, null, null, 0, null, 0, 0, null);
        a("Elder Brain", "Legendary Action", null, "Brain Melter", "The elder brain fires its brain melter.", 0, null, null, 0, null, 0, 1, null);
        a("Elder Brain", "Lair Action", null, "Lair Actions", "Initiative 20. The elder brain can create illusions or cause psychic damage.", 0, null, null, 19, "WIS", 120, 0, null);
        a("Air Elemental", "Attack", "Melee", "Slam", "Melee Weapon Attack: +4 to hit, reach 5ft. Hit: 11 (2d6 + 2) bludgeoning.", 4, "2d6 + 2", "Bludgeoning", 0, null, 5, 0, null);
        a("Earth Elemental", "Attack", "Melee", "Slam", "Melee Weapon Attack: +7 to hit, reach 5ft. Hit: 15 (2d8 + 5) bludgeoning.", 7, "2d8 + 5", "Bludgeoning", 0, null, 5, 0, null);
        a("Fire Elemental", "Attack", "Melee", "Fire Touch", "Melee Weapon Attack: +4 to hit, reach 5ft. Hit: 11 (2d6 + 2) fire.", 4, "2d6 + 2", "Fire", 0, null, 5, 0, null);
        a("Water Elemental", "Attack", "Melee", "Slam", "Melee Weapon Attack: +4 to hit, reach 5ft. Hit: 11 (2d6 + 2) bludgeoning.", 4, "2d6 + 2", "Bludgeoning", 0, null, 5, 0, null);
        a("Djinni", "Attack", "Melee", "Slam", "Melee Weapon Attack: +4 to hit, reach 5ft. Hit: 11 (2d6 + 2) bludgeoning.", 4, "2d6 + 2", "Bludgeoning", 0, null, 5, 0, null);
        a("Dao", "Attack", "Melee", "Slam", "Melee Weapon Attack: +7 to hit, reach 5ft. Hit: 15 (2d8 + 5) bludgeoning.", 7, "2d8 + 5", "Bludgeoning", 0, null, 5, 0, null);
        a("Efreeti", "Attack", "Melee", "Slam", "Melee Weapon Attack: +4 to hit, reach 5ft. Hit: 11 (2d6 + 2) fire.", 4, "2d6 + 2", "Fire", 0, null, 5, 0, null);
        a("Marid", "Attack", "Melee", "Slam", "Melee Weapon Attack: +4 to hit, reach 5ft. Hit: 11 (2d6 + 2) bludgeoning.", 4, "2d6 + 2", "Bludgeoning", 0, null, 5, 0, null);
        a("Giant, Frost", "Attack", "Melee", "Greatclub", "Melee Weapon Attack: +8 to hit, reach 10ft. Hit: 17 (2d8 + 6) bludgeoning.", 8, "2d8 + 6", "Bludgeoning", 0, null, 10, 0, null);
        a("Giant, Fire", "Attack", "Melee", "Greatclub", "Melee Weapon Attack: +8 to hit, reach 10ft. Hit: 17 (2d8 + 6) bludgeoning.", 8, "2d8 + 6", "Bludgeoning", 0, null, 10, 0, null);
        a("Giant, Storm", "Attack", "Melee", "Greatclub", "Melee Weapon Attack: +8 to hit, reach 10ft. Hit: 17 (2d8 + 6) bludgeoning.", 8, "2d8 + 6", "Bludgeoning", 0, null, 10, 0, null);
        a("Giant, Hill", "Attack", "Melee", "Greatclub", "Melee Weapon Attack: +6 to hit, reach 10ft. Hit: 14 (2d8 + 4) bludgeoning.", 6, "2d8 + 4", "Bludgeoning", 0, null, 10, 0, null);
        a("Giant, Stone", "Attack", "Melee", "Greatclub", "Melee Weapon Attack: +7 to hit, reach 10ft. Hit: 16 (2d8 + 5) bludgeoning.", 7, "2d8 + 5", "Bludgeoning", 0, null, 10, 0, null);
        a("Giant, Cloud", "Attack", "Melee", "Greatclub", "Melee Weapon Attack: +8 to hit, reach 10ft. Hit: 17 (2d8 + 6) bludgeoning.", 8, "2d8 + 6", "Bludgeoning", 0, null, 10, 0, null);
        a("Banshee", "Attack", "Ranged", "Wail", "Ranged Spell Attack: +8 to hit, range 60ft. Hit: 22 (4d10) psychic damage. Target must succeed on DC 15 WIS save or be frightened 1 minute.", 8, "4d10", "Psychic", 15, "WIS", 60, 0, null);
        a("Vampire Spawn", "Attack", "Melee", "Bite", "Melee Weapon Attack: +6 to hit, reach 5ft. Hit: 11 (2d6 + 4) piercing plus 7 (3d4) necrotic.", 6, "2d6 + 4", "Piercing", 0, null, 5, 0, null);
        a("Ghost", "Attack", "Melee", "Life Drain", "Melee Spell Attack: +4 to hit, reach 5ft. Hit: 10 (3d6) necrotic damage.", 4, "3d6", "Necrotic", 0, null, 5, 0, null);
        a("Specter", "Attack", "Melee", "Life Drain", "Melee Spell Attack: +4 to hit, reach 5ft. Hit: 7 (2d4) necrotic damage.", 4, "2d4", "Necrotic", 0, null, 5, 0, null);
        a("Shadow", "Attack", "Melee", "Life Drain", "Melee Spell Attack: +4 to hit, reach 5ft. Hit: 7 (2d4) necrotic damage. Target's STR is reduced by 2d4.", 4, "2d4", "Necrotic", 0, null, 5, 0, null);
        a("Dryad", "Trait", null, "Misty's Veil", "When the dryad drops to 0 HP, it disappears in a mist and returns after 1d4 hours.", 0, null, null, 0, null, 0, 0, null);
        a("Satyr", "Attack", "Melee", "Shortsword", "Melee Weapon Attack: +4 to hit, reach 5ft. Hit: 5 (1d6 + 2) piercing.", 4, "1d6 + 2", "Piercing", 0, null, 5, 0, null);
        a("Archmage", "Attack", "Ranged", "Magic Missile", "Ranged Spell Attack: +9 to hit, range 120ft. Hit: 15 (3d4 + 4) force damage.", 9, "3d4 + 4", "Force", 0, null, 120, 0, null);
        a("Giant Rat", "Attack", "Melee", "Bite", "Melee Weapon Attack: +1 to hit, reach 5ft. Hit: 4 (1d6 + 1) piercing.", 1, "1d6 + 1", "Piercing", 0, null, 5, 0, null);
        a("Giant Spider", "Attack", "Melee", "Bite", "Melee Weapon Attack: +4 to hit, reach 5ft. Hit: 5 (1d4 + 2) piercing plus 3 (1d4) poison.", 4, "1d4 + 2", "Piercing", 0, null, 5, 0, null);
        a("Kobold", "Attack", "Melee", "Shortspear", "Melee Weapon Attack: +2 to hit, reach 5ft. Hit: 3 (1d6) piercing.", 2, "1d6", "Piercing", 0, null, 5, 0, null);
        a("Hobgoblin", "Attack", "Melee", "Longsword", "Melee Weapon Attack: +5 to hit, reach 5ft. Hit: 7 (1d8 + 3) slashing.", 5, "1d8 + 3", "Slashing", 0, null, 5, 0, null);
        a("Bugbear", "Attack", "Melee", "Greataxe", "Melee Weapon Attack: +5 to hit, reach 5ft. Hit: 11 (2d8 + 3) slashing.", 5, "2d8 + 3", "Slashing", 0, null, 5, 0, null);
        a("Giant Wolf", "Attack", "Melee", "Bite", "Melee Weapon Attack: +5 to hit, reach 5ft. Hit: 10 (2d6 + 3) piercing.", 5, "2d6 + 3", "Piercing", 0, null, 5, 0, null);
        a("Giant Eagle", "Attack", "Melee", "Talons", "Melee Weapon Attack: +4 to hit, reach 5ft. Hit: 7 (1d8 + 2) slashing.", 4, "1d8 + 2", "Slashing", 0, null, 5, 0, null);
        a("Giant Hyena", "Attack", "Melee", "Bite", "Melee Weapon Attack: +4 to hit, reach 5ft. Hit: 7 (1d8 + 2) piercing.", 4, "1d8 + 2", "Piercing", 0, null, 5, 0, null);
        a("Giant Boar", "Attack", "Melee", "Charge", "Melee Weapon Attack: +6 to hit, reach 5ft. Hit: 17 (3d8 + 6) piercing. Target must succeed on DC 15 STR save or be knocked prone.", 6, "3d8 + 6", "Piercing", 15, "STR", 5, 0, null);
        a("Giant Crab", "Attack", "Melee", "Claw", "Melee Weapon Attack: +6 to hit, reach 5ft. Hit: 9 (2d6 + 4) bludgeoning, target grappled (escape DC 12).", 6, "2d6 + 4", "Bludgeoning", 12, "STR", 5, 0, null);
        a("Giant Badger", "Attack", "Melee", "Claw", "Melee Weapon Attack: +4 to hit, reach 5ft. Hit: 5 (1d6 + 2) slashing.", 4, "1d6 + 2", "Slashing", 0, null, 5, 0, null);
        a("Giant Vulture", "Attack", "Melee", "Beak", "Melee Weapon Attack: +4 to hit, reach 5ft. Hit: 5 (1d6 + 2) piercing.", 4, "1d6 + 2", "Piercing", 0, null, 5, 0, null);
        a("Giant Wolf Spider", "Attack", "Melee", "Bite", "Melee Weapon Attack: +4 to hit, reach 5ft. Hit: 5 (1d4 + 2) piercing plus 3 (1d4) poison.", 4, "1d4 + 2", "Piercing", 0, null, 5, 0, null);
        a("Giant Wasp", "Attack", "Melee", "Sting", "Melee Weapon Attack: +4 to hit, reach 5ft. Hit: 5 (1d6 + 2) piercing plus 3 (1d4) poison.", 4, "1d6 + 2", "Piercing", 0, null, 5, 0, null);
        a("Giant Bat", "Attack", "Melee", "Bite", "Melee Weapon Attack: +2 to hit, reach 5ft. Hit: 3 (1d4 + 2) piercing.", 2, "1d4 + 2", "Piercing", 0, null, 5, 0, null);
        a("Giant Owl", "Attack", "Melee", "Talons", "Melee Weapon Attack: +4 to hit, reach 5ft. Hit: 5 (1d4 + 2) slashing.", 4, "1d4 + 2", "Slashing", 0, null, 5, 0, null);
        a("Giant Weasel", "Attack", "Melee", "Bite", "Melee Weapon Attack: +5 to hit, reach 5ft. Hit: 6 (1d6 + 3) piercing.", 5, "1d6 + 3", "Piercing", 0, null, 5, 0, null);
        a("Wraith", "Attack", "Melee", "Life Drain", "Melee Spell Attack: +4 to hit, reach 5ft. Hit: 14 (3d6 + 2) necrotic damage. Target's HP max reduced by damage.", 4, "3d6 + 2", "Necrotic", 0, null, 5, 0, null);
        a("Red Dragon Wyrmling", "Attack", "Melee", "Bite", "Melee Weapon Attack: +6 to hit, reach 5ft. Hit: 9 (2d6 + 4) piercing.", 6, "2d6 + 4", "Piercing", 0, null, 5, 0, null);
        a("Red Dragon Wyrmling", "Attack", "Ranged", "Fire Breath", "Each creature in a 30ft line must make a DC 13 DEX save. On a failure: 21 (6d6) fire damage. On success: half.", 0, "6d6", "Fire", 13, "DEX", 30, 0, "5-6");
        a("White Dragon Wyrmling", "Attack", "Melee", "Bite", "Melee Weapon Attack: +6 to hit, reach 5ft. Hit: 9 (2d6 + 4) piercing.", 6, "2d6 + 4", "Piercing", 0, null, 5, 0, null);
        a("White Dragon Wyrmling", "Attack", "Ranged", "Cold Breath", "Each creature in a 15ft cone must make a DC 13 CON save. On a failure: 21 (6d6) cold damage. On success: half.", 0, "6d6", "Cold", 13, "CON", 15, 0, "5-6");
        a("Young Red Dragon", "Attack", "Melee", "Bite", "Melee Weapon Attack: +7 to hit, reach 10ft. Hit: 11 (2d8 + 4) piercing.", 7, "2d8 + 4", "Piercing", 0, null, 10, 0, null);
        a("Young Red Dragon", "Attack", "Ranged", "Fire Breath", "Each creature in a 60ft line must make a DC 17 DEX save. On a failure: 36 (10d6) fire damage. On success: half.", 0, "10d6", "Fire", 17, "DEX", 60, 0, "5-6");
        a("Young Black Dragon", "Attack", "Melee", "Bite", "Melee Weapon Attack: +6 to hit, reach 5ft. Hit: 9 (2d6 + 4) piercing.", 6, "2d6 + 4", "Piercing", 0, null, 5, 0, null);
        a("Young Black Dragon", "Attack", "Ranged", "Acid Breath", "Each creature in a 30ft cone must make a DC 17 DEX save. On a failure: 36 (10d6) acid damage. On success: half.", 0, "10d6", "Acid", 17, "DEX", 30, 0, "5-6");
        a("Young Blue Dragon", "Attack", "Melee", "Bite", "Melee Weapon Attack: +7 to hit, reach 10ft. Hit: 11 (2d8 + 4) piercing.", 7, "2d8 + 4", "Piercing", 0, null, 10, 0, null);
        a("Young Blue Dragon", "Attack", "Ranged", "Lightning Breath", "Each creature in a 60ft line must make a DC 17 DEX save. On a failure: 36 (10d6) lightning damage. On success: half.", 0, "10d6", "Lightning", 17, "DEX", 60, 0, "5-6");
        a("Young Green Dragon", "Attack", "Melee", "Bite", "Melee Weapon Attack: +6 to hit, reach 5ft. Hit: 9 (2d6 + 4) piercing plus 4 (1d8) poison.", 6, "2d6 + 4", "Piercing", 0, null, 5, 0, null);
        a("Young Green Dragon", "Attack", "Ranged", "Miasma Breath", "Each creature in a 15ft cube must make a DC 17 CON save. On a failure: 36 (10d6) poison damage. On success: half.", 0, "10d6", "Poison", 17, "CON", 15, 0, "5-6");
        a("Young White Dragon", "Attack", "Melee", "Bite", "Melee Weapon Attack: +6 to hit, reach 5ft. Hit: 9 (2d6 + 4) piercing.", 6, "2d6 + 4", "Piercing", 0, null, 5, 0, null);
        a("Young White Dragon", "Attack", "Ranged", "Cold Breath", "Each creature in a 30ft cone must make a DC 17 CON save. On a failure: 36 (10d6) cold damage. On success: half.", 0, "10d6", "Cold", 17, "CON", 30, 0, "5-6");
        a("Kobold Dragonkeeper", "Attack", "Ranged", "Shortbow", "Ranged Weapon Attack: +4 to hit, range 80/320ft. Hit: 5 (1d6 + 2) piercing.", 4, "1d6 + 2", "Piercing", 0, null, 80, 0, null);
        a("Goblin Boss", "Attack", "Melee", "Scimitar", "Melee Weapon Attack: +4 to hit, reach 5ft. Hit: 6 (1d6 + 3) slashing.", 4, "1d6 + 3", "Slashing", 0, null, 5, 0, null);
        a("Goblin Boss", "Trait", null, "Pack Tactics", "The goblin boss has advantage on attack rolls against a creature if at least one ally is within 5 ft.", 0, null, null, 0, null, 0, 0, null);
        log.info("Populated {} abilities", abilityRepository.findAll().size());
    }

    private void a(String monsterName, String type, String subtype, String name, String description,
                    int attackBonus, String damage, String damageType, int saveDC, String saveAbility,
                    int range, int usesPerLongRest, String recharge) {
        if (abilityRepository.findByMonsterNameAndTypeAndActiveTrue(monsterName, type).stream()
                .anyMatch(a -> a.getName().equals(name))) return;
        Dnd5eMonsterAbility ability = new Dnd5eMonsterAbility();
        ability.setMonsterName(monsterName); ability.setType(type); ability.setSubtype(subtype);
        ability.setName(name); ability.setDescription(description);
        ability.setAttackBonus(attackBonus); ability.setDamage(damage);
        ability.setDamageType(damageType); ability.setSaveDC(saveDC);
        ability.setSaveAbility(saveAbility); ability.setRange(range);
        ability.setUsesPerLongRest(usesPerLongRest); ability.setRecharge(recharge);
        ability.setActive(true);
        abilityRepository.save(ability);
    }
}
