package com.yisy.modules.doramcu.traveller.service;
import org.springframework.stereotype.Service;

import com.yisy.modules.doramcu.gamecore.AiBehaviorLevel;
import com.yisy.modules.doramcu.traveller.model.TravellerMonster;
import com.yisy.modules.doramcu.traveller.model.TravellerMonsterAbility;
import com.yisy.modules.doramcu.traveller.repository.TravellerMonsterAbilityRepository;
import com.yisy.modules.doramcu.traveller.repository.TravellerMonsterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TravellerMonsterService {

    private static final Logger log = LoggerFactory.getLogger(TravellerMonsterService.class);
    private static final int EXPECTED_MONSTER_COUNT_MIN = 20;


    private final TravellerMonsterRepository monsterRepository;
    private final TravellerMonsterAbilityRepository abilityRepository;
    private boolean initialized = false;

    public TravellerMonsterService(TravellerMonsterRepository monsterRepository,
                                    TravellerMonsterAbilityRepository abilityRepository) {
        this.monsterRepository = monsterRepository;
        this.abilityRepository = abilityRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initializeMonsterDatabase() {
        if (initialized) return;
        long existingCount = monsterRepository.countByActiveTrue();
        if (existingCount >= EXPECTED_MONSTER_COUNT_MIN) {
            log.info("Traveller monster database already populated with {} monsters", existingCount);
            initialized = true;
            return;
        }
        log.info("Initializing Traveller monster database...");
        populateMonsters();
        populateMonsterAbilities();
        initialized = true;
        log.info("Traveller monster database initialized: {} monsters, {} abilities",
                monsterRepository.countByActiveTrue(), abilityRepository.findAll().size());
    }

    // ==================== Monster CRUD ====================

    @Transactional(readOnly = true)
    public TravellerMonster getMonsterByName(String name) {
        return monsterRepository.findByName(name)
                .orElseThrow(() -> new IllegalArgumentException("Monster not found: " + name));
    }

    @Transactional(readOnly = true)
    public List<TravellerMonster> listMonsters() {
        return monsterRepository.findByThreatLevelRange(1, 10);
    }

    @Transactional(readOnly = true)
    public List<TravellerMonster> listMonstersByThreatLevel(int threatLevel) {
        return monsterRepository.findByThreatLevelAndActiveTrue(threatLevel);
    }

    @Transactional(readOnly = true)
    public List<TravellerMonster> listMonstersByType(String type) {
        return monsterRepository.findByTypeAndActiveTrue(type);
    }

    @Transactional(readOnly = true)
    public List<TravellerMonster> listMonstersByAiBehaviorLevel(AiBehaviorLevel level) {
        return monsterRepository.findByAiBehaviorLevelAndActiveTrue(level);
    }

    // ==================== Ability CRUD ====================

    @Transactional(readOnly = true)
    public List<TravellerMonsterAbility> listAbilities(String monsterName) {
        return abilityRepository.findByMonsterNameAndActiveTrue(monsterName);
    }

    // ==================== Population ====================

    @Transactional
    public void populateMonsters() {
        // TL1: Common Fodder
        m("Alien Rat", 1, 25, "Small", "Beast", null, "Unaligned", 10, 7, 30, 0, 0, 0, 3, 8, 6, 2, 2, 1, null, null, null, null, null, "Darkvision 60ft", false);
        m("Giant Insect", 1, 25, "Small", "Beast", "Insectoid", "Unaligned", 12, 11, 30, 0, 0, 10, 4, 14, 9, 2, 4, 3, "DEX +2", null, null, null, null, "Blindsight 60ft", false);
        m("Feral Dog", 1, 25, "Medium", "Beast", null, "Unaligned", 12, 11, 30, 0, 0, 0, 11, 12, 10, 6, 8, 6, null, null, null, null, null, "Keen Smell", false);
        m("Mutant Rat", 1, 25, "Small", "Mutant", null, "Unaligned", 11, 10, 30, 0, 0, 0, 5, 10, 8, 3, 2, 2, null, null, null, null, "Acid", "Darkvision 60ft", false);
        m("Alien Spider", 1, 25, "Small", "Beast", "Insectoid", "Unaligned", 13, 15, 30, 0, 0, 10, 4, 14, 9, 2, 4, 3, "DEX +2", null, null, null, null, "Blindsight 60ft", false);

        // TL2: Low Level
        m("Alien Scout", 2, 100, "Medium", "Alien", "Humanoid", "Neutral", 15, 14, 30, 0, 0, 0, 10, 14, 10, 10, 8, 8, "DEX +2", "Stealth +4", null, null, null, "Darkvision 60ft", false);
        m("Alien Warrior", 2, 100, "Medium", "Alien", "Humanoid", "Neutral Evil", 16, 18, 30, 0, 0, 0, 14, 12, 12, 8, 8, 8, "STR +2", "Athletics +4", null, null, null, "Darkvision 60ft", false);
        m("Alien Hunter", 2, 100, "Medium", "Alien", "Reptilian", "Neutral", 14, 16, 30, 0, 0, 0, 12, 14, 10, 8, 10, 6, "DEX +2", "Perception +2, Stealth +4", null, null, null, "Darkvision 60ft, Keen Smell", false);
        m("Alien Shaman", 2, 100, "Medium", "Alien", "Humanoid", "Neutral", 12, 12, 10, 30, 0, 0, 8, 12, 10, 12, 14, 12, "INT +1 | WIS +2", "Arcana +3, Insight +3", null, null, null, "Darkvision 60ft", false);
        m("Alien Beast", 2, 100, "Large", "Beast", null, "Unaligned", 14, 22, 40, 0, 0, 0, 16, 10, 12, 4, 6, 4, "STR +3", null, null, null, null, "Darkvision 60ft", false);

        // TL3: Mid-Low
        m("Protean Beast", 3, 200, "Large", "Alien", null, "Chaotic Evil", 16, 30, 40, 0, 0, 0, 18, 12, 14, 6, 8, 6, "STR +4", null, null, null, null, "Darkvision 60ft", false);
        m("Void Spawn", 3, 200, "Medium", "Void Creature", null, "Chaotic Evil", 14, 25, 30, 0, 0, 0, 12, 14, 12, 8, 10, 10, "DEX +2", "Stealth +4", null, "Bludgeoning, Piercing, Slashing", null, "Blindsight 60ft", false);
        m("Alien War Beast", 3, 200, "Huge", "Beast", null, "Unaligned", 16, 35, 40, 0, 0, 0, 20, 8, 14, 3, 6, 4, "STR +5", null, null, null, null, "Darkvision 60ft", false);
        m("Alien Berserker", 3, 200, "Medium", "Alien", "Humanoid", "Chaotic Evil", 17, 28, 30, 0, 0, 0, 16, 14, 12, 6, 6, 8, "STR +3 | DEX +2", "Athletics +5", null, null, null, "Darkvision 60ft", false);
        m("Alien Assassin", 3, 200, "Medium", "Alien", "Humanoid", "Lawful Evil", 16, 22, 30, 0, 0, 0, 12, 16, 10, 12, 10, 10, "DEX +3", "Stealth +5, Perception +2", null, null, null, "Darkvision 60ft", false);

        // TL4: Mid
        m("Robot Guard", 4, 450, "Medium", "Robot", null, "Unaligned", 18, 30, 30, 0, 0, 0, 16, 12, 14, 6, 8, 4, "STR +3", "Perception +2", "Poison, Psychic", "Bludgeoning, Piercing, Slashing", null, "Darkvision 60ft, Motion Detection", false);
        m("Robot Worker", 4, 450, "Medium", "Robot", null, "Unaligned", 16, 25, 20, 0, 0, 0, 14, 10, 12, 4, 6, 2, "STR +2", null, "Poison, Psychic", null, null, "Darkvision 60ft", false);
        m("Robot Scout", 4, 450, "Medium", "Robot", null, "Unaligned", 16, 20, 30, 0, 0, 0, 10, 16, 10, 8, 10, 4, "DEX +3", "Stealth +5, Perception +5", "Poison, Psychic", null, null, "Darkvision 60ft, Infrared Vision", false);
        m("Alien Commander", 4, 450, "Medium", "Alien", "Humanoid", "Lawful Evil", 17, 35, 30, 0, 0, 0, 14, 14, 12, 14, 12, 14, "INT +2 | WIS +1 | CHA +2", "Intimidation +4, Perception +3", null, null, null, "Darkvision 60ft", false);
        m("Alien Elite", 4, 450, "Medium", "Alien", "Humanoid", "Lawful Evil", 18, 32, 30, 0, 0, 0, 16, 14, 12, 10, 10, 10, "STR +3 | DEX +2", "Athletics +5, Stealth +4", null, null, null, "Darkvision 60ft", false);

        // TL5: Mid-High
        m("Void Creature", 5, 700, "Large", "Void Creature", null, "Chaotic Evil", 16, 40, 0, 60, 0, 0, 14, 16, 14, 10, 12, 10, "DEX +3 | INT +0 | WIS +1", "Stealth +5", null, "Bludgeoning, Piercing, Slashing", "Force", "Blindsight 120ft", false);
        m("Alien Overlord", 5, 700, "Large", "Alien", "Humanoid", "Lawful Evil", 19, 45, 30, 0, 0, 0, 18, 12, 14, 16, 14, 16, "STR +4 | INT +3 | CHA +3", "Intimidation +6, Perception +4", null, null, null, "Darkvision 120ft", false);
        m("Alien Warlord", 5, 700, "Large", "Alien", "Humanoid", "Chaotic Evil", 19, 42, 30, 0, 0, 0, 18, 14, 14, 12, 10, 12, "STR +4 | DEX +2", "Athletics +6, Intimidation +4", null, null, null, "Darkvision 60ft", false);
        m("Alien Psionic", 5, 700, "Medium", "Alien", "Humanoid", "Neutral", 14, 35, 30, 0, 0, 0, 10, 14, 12, 16, 16, 14, "INT +3 | WIS +3", "Insight +5, Perception +5", null, null, null, "Darkvision 60ft, Psionic Sense", false);
        m("Alien Sorcerer", 5, 700, "Medium", "Alien", "Humanoid", "Chaotic Evil", 13, 30, 30, 0, 0, 0, 8, 14, 10, 16, 14, 16, "INT +3 | WIS +2 | CHA +3", "Arcana +6, Deception +5", null, null, null, "Darkvision 60ft", false);

        // TL6: High
        m("Alien Hive Mind", 6, 1100, "Gargantuan", "Alien", null, "Lawful Evil", 18, 60, 0, 0, 0, 0, 10, 10, 16, 20, 16, 14, "INT +5 | WIS +3 | CHA +2", "Insight +6, Perception +6", null, "Bludgeoning, Piercing, Slashing", null, "Blindsight 180ft, Telepathy 120ft", false);
        m("Alien Dreadnought", 6, 1100, "Huge", "Alien", "Humanoid", "Lawful Evil", 22, 55, 30, 0, 0, 0, 20, 10, 16, 8, 8, 8, "STR +5 | CON +3", "Athletics +7", null, null, null, "Darkvision 120ft", false);
        m("Alien War Machine", 6, 1100, "Huge", "Construct", null, "Unaligned", 20, 50, 30, 0, 0, 0, 18, 10, 16, 4, 6, 2, "STR +4 | CON +3", "Athletics +6", "Poison, Psychic", "Bludgeoning, Piercing, Slashing", null, "Darkvision 60ft, Motion Detection", false);
        m("Alien Archon", 6, 1100, "Large", "Alien", "Humanoid", "Lawful Evil", 18, 50, 30, 0, 0, 0, 16, 14, 14, 18, 16, 18, "INT +4 | WIS +3 | CHA +4", "Intimidation +8, Arcana +8", null, null, null, "Darkvision 120ft", false);
        m("Alien Emperor", 6, 1100, "Large", "Alien", "Humanoid", "Lawful Evil", 19, 55, 30, 0, 0, 0, 18, 14, 14, 16, 14, 20, "STR +4 | INT +3 | CHA +5", "Intimidation +9, Persuasion +9", null, null, null, "Darkvision 120ft", false);

        // TL7: Very High
        m("Mutant Abomination", 7, 1800, "Huge", "Mutant", null, "Chaotic Evil", 18, 65, 30, 0, 0, 0, 20, 10, 16, 6, 8, 6, "STR +5 | CON +3", "Athletics +7", null, null, null, "Darkvision 60ft", false);
        m("Mutant Hulk", 7, 1800, "Huge", "Mutant", null, "Chaotic Evil", 17, 60, 30, 0, 0, 0, 20, 8, 16, 4, 6, 4, "STR +5 | CON +3", "Athletics +7", null, null, null, "Darkvision 60ft", false);
        m("Mutant Spider", 7, 1800, "Large", "Mutant", null, "Chaotic Evil", 16, 50, 40, 0, 0, 0, 16, 16, 14, 6, 6, 4, "STR +3 | DEX +3", "Stealth +5", null, null, null, "Blindsight 60ft", false);
        m("Mutant Horror", 7, 1800, "Large", "Mutant", null, "Chaotic Evil", 17, 55, 30, 0, 0, 0, 18, 12, 14, 8, 10, 8, "STR +4 | DEX +1", "Intimidation +4", null, null, null, "Darkvision 60ft", false);
        m("Mutant Swarm", 7, 1800, "Tiny", "Mutant", null, "Chaotic Evil", 12, 10, 30, 0, 0, 0, 6, 14, 8, 2, 4, 2, "DEX +2", "Stealth +4", null, null, null, "Darkvision 30ft", false);

        // TL8: Elite
        m("Pirate Captain", 8, 2900, "Medium", "Humanoid", "Human", "Chaotic Evil", 17, 45, 30, 0, 0, 0, 14, 14, 12, 14, 12, 16, "DEX +2 | INT +2 | CHA +3", "Intimidation +6, Persuasion +6", null, null, null, "Normal", false);
        m("Pirate Raider", 8, 2900, "Medium", "Humanoid", "Human", "Chaotic Evil", 17, 35, 30, 0, 0, 0, 14, 14, 12, 10, 10, 8, "STR +2 | DEX +2", "Athletics +4, Stealth +4", null, null, null, "Normal", false);
        m("Pirate Boarder", 8, 2900, "Medium", "Humanoid", "Human", "Chaotic Evil", 18, 38, 30, 0, 0, 0, 16, 12, 12, 8, 8, 6, "STR +3", "Athletics +5", null, null, null, "Normal", false);
        m("Pirate Gunner", 8, 2900, "Medium", "Humanoid", "Human", "Chaotic Evil", 14, 30, 30, 0, 0, 0, 10, 16, 10, 10, 8, 6, "DEX +3", "Perception +2", null, null, null, "Normal", false);
        m("Pirate Scout", 8, 2900, "Medium", "Humanoid", "Human", "Chaotic Evil", 14, 28, 30, 0, 0, 0, 10, 16, 10, 12, 10, 8, "DEX +3 | INT +1", "Stealth +5, Perception +3", null, null, null, "Normal", false);

        // TL9: Legendary
        m("Mercenary Commander", 9, 5900, "Medium", "Humanoid", "Human", "Neutral", 18, 55, 30, 0, 0, 0, 16, 14, 14, 16, 14, 16, "STR +3 | DEX +2 | INT +3 | CHA +3", "Intimidation +6, Persuasion +6", null, null, null, "Normal", false);
        m("Mercenary Elite", 9, 5900, "Medium", "Humanoid", "Human", "Neutral", 18, 50, 30, 0, 0, 0, 16, 14, 14, 12, 10, 10, "STR +3 | DEX +2", "Athletics +5, Stealth +4", null, null, null, "Normal", false);
        m("Mercenary Sniper", 9, 5900, "Medium", "Humanoid", "Human", "Neutral", 14, 40, 30, 0, 0, 0, 10, 18, 10, 12, 12, 8, "DEX +4 | WIS +1", "Perception +5, Stealth +5", null, null, null, "Normal", false);
        m("Mercenary Heavy", 9, 5900, "Medium", "Humanoid", "Human", "Neutral", 19, 55, 30, 0, 0, 0, 18, 12, 14, 8, 8, 6, "STR +4 | CON +2", "Athletics +6", null, null, null, "Normal", false);
        m("Mercenary Medic", 9, 5900, "Medium", "Humanoid", "Human", "Neutral Good", 14, 45, 30, 0, 0, 0, 10, 14, 12, 14, 16, 12, "INT +2 | WIS +3", "Medicine +7, Insight +5", null, null, null, "Normal", false);

        // TL10: Toplevel
        m("Alien God", 10, 230000, "Gargantuan", "Alien", null, "Lawful Evil", 25, 200, 0, 100, 0, 0, 20, 20, 20, 20, 20, 20, "INT +5 | WIS +5 | CHA +5", "Intimidation +10, Arcana +10", null, "Bludgeoning, Piercing, Slashing", null, "Truesight 1000ft", false);
        m("Void Leviathan", 10, 230000, "Gargantuan", "Void Creature", null, "Chaotic Evil", 22, 180, 0, 80, 40, 0, 20, 16, 20, 10, 14, 10, "STR +5 | DEX +3 | CON +5", "Athletics +10", null, "Bludgeoning, Piercing, Slashing", "Force", "Blindsight 500ft", false);
        m("Alien Titan", 10, 230000, "Gargantuan", "Alien", "Humanoid", "Lawful Evil", 24, 190, 40, 0, 0, 0, 20, 14, 20, 14, 12, 14, "STR +5 | CON +5", "Athletics +10", null, null, null, "Darkvision 500ft", false);
        m("Alien Primordial", 10, 230000, "Gargantuan", "Alien", null, "Chaotic Evil", 23, 195, 0, 60, 0, 0, 20, 16, 20, 18, 18, 18, "INT +4 | WIS +4 | CHA +4", "Intimidation +9, Arcana +9", null, "Bludgeoning, Piercing, Slashing", null, "Truesight 500ft", false);
        m("Alien Apocalypse", 10, 230000, "Gargantuan", "Alien", null, "Chaotic Evil", 25, 210, 0, 100, 0, 0, 20, 20, 20, 20, 20, 20, "INT +5 | WIS +5 | CHA +5", "Intimidation +10, Arcana +10", null, "Bludgeoning, Piercing, Slashing", null, "Truesight 1000ft", false);

        log.info("Populated {} Traveller monsters", monsterRepository.countByActiveTrue());
    }

    private void m(String name, int threatLevel, int xp, String size, String type, String subtype, String alignment,
                    int ac, int hp, int speed, int flySpeed, int swimSpeed, int climbSpeed,
                    int str, int dex, int con, int intel, int wis, int cha,
                    String skills, String damageImmunities, String damageResistances,
                    String damageVulnerabilities, String conditionImmunities, String senses, boolean isFictional) {
        if (monsterRepository.findByName(name).isPresent()) return;
        TravellerMonster monster = new TravellerMonster();
        monster.setName(name); monster.setThreatLevel(threatLevel); monster.setXp(xp);
        monster.setSize(size); monster.setType(type); monster.setSubtype(subtype);
        monster.setAlignment(alignment); monster.setAc(ac); monster.setHp(hp);
        monster.setSpeed(speed); monster.setFlySpeed(flySpeed); monster.setSwimSpeed(swimSpeed);
        monster.setClimbSpeed(climbSpeed);
        monster.setStr(str); monster.setDex(dex); monster.setCon(con);
        monster.setIntel(intel); monster.setWis(wis); monster.setCha(cha);
        monster.setSkills(skills);
        monster.setDamageImmunities(damageImmunities); monster.setDamageResistances(damageResistances);
        monster.setDamageVulnerabilities(damageVulnerabilities);
        monster.setConditionImmunities(conditionImmunities);
        monster.setSenses(senses); monster.setFictional(isFictional); monster.setActive(true);
        monster.setAiBehaviorLevel(AiBehaviorLevel.fromIntelligence(intel));
        monsterRepository.save(monster);
    }

    @Transactional
    public void populateMonsterAbilities() {
        // TL1 abilities
        a("Alien Rat", "Attack", "Melee", "Bite", "Melee Attack: +1 to hit, reach 5ft. Hit: 3 (1d4 + 1) piercing.", 1, "1d4 + 1", "Piercing", 0, null, 5, 0, null);
        a("Giant Insect", "Attack", "Melee", "Sting", "Melee Attack: +4 to hit, reach 5ft. Hit: 5 (1d4 + 2) piercing plus 3 (1d4) poison.", 4, "1d4 + 2", "Piercing", 0, null, 5, 0, null);
        a("Feral Dog", "Attack", "Melee", "Bite", "Melee Attack: +3 to hit, reach 5ft. Hit: 6 (1d6 + 3) piercing.", 3, "1d6 + 3", "Piercing", 0, null, 5, 0, null);
        a("Mutant Rat", "Attack", "Melee", "Acid Bite", "Melee Attack: +2 to hit, reach 5ft. Hit: 4 (1d4 + 2) acid.", 2, "1d4 + 2", "Acid", 0, null, 5, 0, null);
        a("Alien Spider", "Attack", "Melee", "Venom Bite", "Melee Attack: +4 to hit, reach 5ft. Hit: 5 (1d4 + 2) piercing plus 3 (1d4) poison.", 4, "1d4 + 2", "Piercing", 0, null, 5, 0, null);

        // TL2 abilities
        a("Alien Scout", "Attack", "Ranged", "Laser Pistol", "Ranged Attack: +4 to hit, range 30/120ft. Hit: 6 (1d8 + 2) energy.", 4, "1d8 + 2", "Energy", 0, null, 30, 0, null);
        a("Alien Warrior", "Attack", "Melee", "Plasma Blade", "Melee Attack: +5 to hit, reach 5ft. Hit: 8 (1d10 + 3) energy.", 5, "1d10 + 3", "Energy", 0, null, 5, 0, null);
        a("Alien Hunter", "Attack", "Ranged", "Energy Rifle", "Ranged Attack: +4 to hit, range 60/240ft. Hit: 7 (1d8 + 3) energy.", 4, "1d8 + 3", "Energy", 0, null, 60, 0, null);
        a("Alien Shaman", "Tech", null, "Psionic Bolt", "Ranged Spell Attack: +3 to hit, range 60ft. Hit: 7 (2d6) psychic.", 3, "2d6", "Psychic", 0, null, 60, 0, null);
        a("Alien Beast", "Attack", "Melee", "Claw", "Melee Attack: +5 to hit, reach 5ft. Hit: 10 (2d6 + 3) slashing.", 5, "2d6 + 3", "Slashing", 0, null, 5, 0, null);

        // TL3 abilities
        a("Protean Beast", "Attack", "Melee", "Shape Shift", "The beast shifts form, gaining resistance to the damage type it last took.", 0, null, null, 0, null, 0, 0, null);
        a("Protean Beast", "Attack", "Melee", "Claw", "Melee Attack: +6 to hit, reach 5ft. Hit: 12 (2d8 + 4) slashing.", 6, "2d8 + 4", "Slashing", 0, null, 5, 0, null);
        a("Void Spawn", "Attack", "Melee", "Void Touch", "Melee Attack: +4 to hit, reach 5ft. Hit: 8 (2d6 + 2) force, target must succeed on DC 12 STR save or be pushed 10ft.", 4, "2d6 + 2", "Force", 12, "STR", 5, 0, null);
        a("Alien War Beast", "Attack", "Melee", "Bite", "Melee Attack: +7 to hit, reach 10ft. Hit: 14 (2d10 + 4) piercing.", 7, "2d10 + 4", "Piercing", 0, null, 10, 0, null);
        a("Alien Berserker", "Attack", "Melee", "Power Axe", "Melee Attack: +6 to hit, reach 5ft. Hit: 11 (2d8 + 3) slashing.", 6, "2d8 + 3", "Slashing", 0, null, 5, 0, null);
        a("Alien Assassin", "Attack", "Ranged", "Needle Rifle", "Ranged Attack: +6 to hit, range 60/240ft. Hit: 8 (1d10 + 3) piercing plus 7 (2d6) poison.", 6, "1d10 + 3", "Piercing", 0, null, 60, 0, null);

        // TL4 abilities
        a("Robot Guard", "Attack", "Ranged", "Laser Blast", "Ranged Attack: +6 to hit, range 60/240ft. Hit: 10 (2d8 + 3) energy.", 6, "2d8 + 3", "Energy", 0, null, 60, 0, null);
        a("Robot Guard", "Trait", null, "Damage Resistance", "The robot has resistance to non-magical bludgeoning, piercing, and slashing damage.", 0, null, null, 0, null, 0, 0, null);
        a("Robot Scout", "Attack", "Ranged", "Laser Sight", "Ranged Attack: +5 to hit, range 120/480ft. Hit: 8 (1d10 + 3) energy.", 5, "1d10 + 3", "Energy", 0, null, 120, 0, null);
        a("Alien Commander", "Attack", "Ranged", "Energy Rifle", "Ranged Attack: +5 to hit, range 60/240ft. Hit: 9 (1d10 + 3) energy.", 5, "1d10 + 3", "Energy", 0, null, 60, 0, null);
        a("Alien Elite", "Attack", "Melee", "Plasma Sword", "Melee Attack: +6 to hit, reach 5ft. Hit: 11 (2d8 + 3) energy.", 6, "2d8 + 3", "Energy", 0, null, 5, 0, null);

        // TL5 abilities
        a("Void Creature", "Attack", "Ranged", "Void Beam", "Ranged Attack: +5 to hit, range 120ft. Hit: 14 (3d8 + 2) force.", 5, "3d8 + 2", "Force", 0, null, 120, 0, null);
        a("Alien Overlord", "Attack", "Melee", "Power Fist", "Melee Attack: +7 to hit, reach 5ft. Hit: 14 (2d10 + 4) bludgeoning.", 7, "2d10 + 4", "Bludgeoning", 0, null, 5, 0, null);
        a("Alien Psionic", "Tech", null, "Mind Blast", "Ranged Spell Attack: +5 to hit, range 60ft. Hit: 14 (4d6) psychic. Target must succeed on DC 14 WIS save or be stunned until end of next turn.", 5, "4d6", "Psychic", 14, "WIS", 60, 0, null);
        a("Alien Sorcerer", "Tech", null, "Energy Storm", "Each creature in a 20ft radius must make a DC 15 DEX save. On a failure: 18 (4d8) energy damage. On success: half.", 0, "4d8", "Energy", 15, "DEX", 20, 0, "5-6");

        // TL6 abilities
        a("Alien Hive Mind", "Attack", "Ranged", "Mind Control", "Ranged Spell Attack: +7 to hit, range 120ft. Hit: target must succeed on DC 17 WIS save or be charmed for 1 minute.", 0, null, null, 17, "WIS", 120, 0, null);
        a("Alien Hive Mind", "Trait", null, "Swarm Command", "The hive mind can control up to 10 smaller aliens within 60ft.", 0, null, null, 0, null, 60, 0, null);
        a("Alien Dreadnought", "Attack", "Melee", "Power Fist", "Melee Attack: +8 to hit, reach 10ft. Hit: 17 (2d12 + 5) bludgeoning.", 8, "2d12 + 5", "Bludgeoning", 0, null, 10, 0, null);
        a("Alien Archon", "Tech", null, "Reality Warp", "Each creature in a 30ft radius must make a DC 18 WIS save. On a failure: 21 (6d6) psychic damage. On success: half.", 0, "6d6", "Psychic", 18, "WIS", 30, 0, "5-6");
        a("Alien Emperor", "Attack", "Ranged", "Imperial Beam", "Ranged Attack: +8 to hit, range 120ft. Hit: 16 (3d8 + 4) energy.", 8, "3d8 + 4", "Energy", 0, null, 120, 0, null);

        // TL7 abilities
        a("Mutant Abomination", "Attack", "Melee", "Tentacle Slam", "Melee Attack: +8 to hit, reach 10ft. Hit: 17 (3d8 + 5) bludgeoning, target grappled (escape DC 15).", 8, "3d8 + 5", "Bludgeoning", 15, "STR", 10, 0, null);
        a("Mutant Hulk", "Attack", "Melee", "Slam", "Melee Attack: +8 to hit, reach 10ft. Hit: 16 (3d8 + 5) bludgeoning.", 8, "3d8 + 5", "Bludgeoning", 0, null, 10, 0, null);
        a("Mutant Spider", "Attack", "Melee", "Venom Bite", "Melee Attack: +6 to hit, reach 5ft. Hit: 10 (2d6 + 3) piercing plus 7 (2d6) poison.", 6, "2d6 + 3", "Piercing", 0, null, 5, 0, null);
        a("Mutant Swarm", "Trait", null, "Swarm", "The swarm can squeeze through openings as small as 1 inch.", 0, null, null, 0, null, 0, 0, null);

        // TL8 abilities
        a("Pirate Captain", "Attack", "Ranged", "Heavy Laser", "Ranged Attack: +6 to hit, range 60/240ft. Hit: 12 (2d10 + 3) energy.", 6, "2d10 + 3", "Energy", 0, null, 60, 0, null);
        a("Pirate Raider", "Attack", "Melee", "Cutlass", "Melee Attack: +5 to hit, reach 5ft. Hit: 8 (1d10 + 3) slashing.", 5, "1d10 + 3", "Slashing", 0, null, 5, 0, null);
        a("Pirate Gunner", "Attack", "Ranged", "Heavy Cannon", "Ranged Attack: +6 to hit, range 120/480ft. Hit: 14 (2d10 + 3) piercing.", 6, "2d10 + 3", "Piercing", 0, null, 120, 0, null);

        // TL9 abilities
        a("Mercenary Commander", "Attack", "Ranged", "Assault Rifle", "Ranged Attack: +7 to hit, range 60/240ft. Hit: 13 (2d10 + 3) piercing.", 7, "2d10 + 3", "Piercing", 0, null, 60, 0, null);
        a("Mercenary Sniper", "Attack", "Ranged", "Sniper Rifle", "Ranged Attack: +8 to hit, range 200/800ft. Hit: 16 (3d10 + 4) piercing.", 8, "3d10 + 4", "Piercing", 0, null, 200, 0, null);
        a("Mercenary Heavy", "Attack", "Ranged", "Heavy Cannon", "Ranged Attack: +7 to hit, range 120/480ft. Hit: 15 (2d12 + 4) piercing.", 7, "2d12 + 4", "Piercing", 0, null, 120, 0, null);

        // TL10 abilities
        a("Alien God", "Tech", null, "Divine Judgment", "Each creature in a 100ft radius must make a DC 25 WIS save. On a failure: 45 (10d8) psychic damage. On success: half.", 0, "10d8", "Psychic", 25, "WIS", 100, 0, "5-6");
        a("Void Leviathan", "Attack", "Ranged", "Void Maw", "Ranged Attack: +10 to hit, range 200ft. Hit: 27 (4d10 + 5) force.", 10, "4d10 + 5", "Force", 0, null, 200, 0, null);
        a("Alien Titan", "Attack", "Melee", "Titan Fist", "Melee Attack: +10 to hit, reach 15ft. Hit: 22 (3d12 + 5) bludgeoning.", 10, "3d12 + 5", "Bludgeoning", 0, null, 15, 0, null);
        a("Alien Apocalypse", "Tech", null, "World Ender", "Each creature in a 200ft radius must make a DC 25 DEX save. On a failure: 56 (16d6) energy damage. On success: half.", 0, "16d6", "Energy", 25, "DEX", 200, 0, "5-6");

        log.info("Populated {} Traveller abilities", abilityRepository.findAll().size());
    }

    private void a(String monsterName, String type, String subtype, String name, String description,
                    int attackBonus, String damage, String damageType, int saveDC, String saveAbility,
                    int range, int usesPerLongRest, String recharge) {
        if (abilityRepository.findByMonsterNameAndTypeAndActiveTrue(monsterName, type).stream()
                .anyMatch(a -> a.getName().equals(name))) return;
        TravellerMonsterAbility ability = new TravellerMonsterAbility();
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
