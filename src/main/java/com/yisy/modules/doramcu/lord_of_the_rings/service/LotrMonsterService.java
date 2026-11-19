package com.yisy.modules.doramcu.lord_of_the_rings.service;

import com.yisy.modules.doramcu.lord_of_the_rings.model.LotrMonster;
import com.yisy.modules.doramcu.lord_of_the_rings.model.LotrMonsterAbility;
import com.yisy.modules.doramcu.lord_of_the_rings.repository.LotrMonsterAbilityRepository;
import com.yisy.modules.doramcu.lord_of_the_rings.repository.LotrMonsterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for Lord of the Rings monster management.
 */
@Service
public class LotrMonsterService {

    private static final Logger log = LoggerFactory.getLogger(LotrMonsterService.class);

    private final LotrMonsterRepository repository;
    private final LotrMonsterAbilityRepository abilityRepository;

    public LotrMonsterService(LotrMonsterRepository repository, LotrMonsterAbilityRepository abilityRepository) {
        this.repository = repository;
        this.abilityRepository = abilityRepository;
    }

    public List<LotrMonster> findAll() {
        return repository.findAll();
    }

    public LotrMonster findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Monster not found: " + id));
    }

    public List<LotrMonster> findByType(String type) {
        return repository.findByMonsterType(type);
    }

    public List<LotrMonster> findByFaction(String faction) {
        return repository.findByFaction(faction);
    }

    public List<LotrMonster> findBosses() {
        return repository.findByIsBossTrue();
    }

    @Transactional
    public LotrMonster createMonster(String name, String description, String monsterType,
                                       double challengeRating, int hitPoints, int armorClass) {
        return createMonster(name, description, monsterType, challengeRating, hitPoints, armorClass, false);
    }

    @Transactional
    public LotrMonster createMonster(String name, String description, String monsterType,
                                       double challengeRating, int hitPoints, int armorClass, boolean isBoss) {
        LotrMonster monster = new LotrMonster();
        monster.setName(name);
        monster.setDescription(description);
        monster.setMonsterType(monsterType);
        monster.setChallengeRating(challengeRating);
        monster.setHitPoints(hitPoints);
        monster.setArmorClass(armorClass);
        monster.setXpValue(calculateXp(challengeRating));
        monster.setBoss(isBoss);
        log.info("Created LOTR monster: {} (CR {})", name, challengeRating);
        return repository.save(monster);
    }

    @Transactional
    public LotrMonster save(LotrMonster monster) {
        return repository.save(monster);
    }

    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }

    /**
     * Get XP for a given challenge rating.
     */
    public int getXpForCr(double cr) {
        return calculateXp(cr);
    }

    /**
     * Get the full XP table for all challenge ratings.
     */
    public Map<Double, Integer> getXpTable() {
        Map<Double, Integer> table = new HashMap<>();
        table.put(0.0, 10);
        table.put(0.25, 25);
        table.put(0.5, 50);
        table.put(1.0, 100);
        table.put(2.0, 200);
        table.put(3.0, 350);
        table.put(4.0, 500);
        table.put(5.0, 700);
        table.put(6.0, 900);
        table.put(7.0, 1100);
        table.put(8.0, 1300);
        table.put(9.0, 1500);
        table.put(10.0, 1700);
        table.put(11.0, 1900);
        table.put(12.0, 2200);
        table.put(13.0, 2600);
        table.put(14.0, 3000);
        table.put(15.0, 3600);
        table.put(16.0, 4200);
        table.put(17.0, 5000);
        table.put(18.0, 6200);
        table.put(19.0, 7500);
        table.put(20.0, 9000);
        table.put(21.0, 10500);
        table.put(22.0, 13000);
        table.put(23.0, 16500);
        table.put(24.0, 20000);
        table.put(25.0, 23000);
        table.put(26.0, 27500);
        table.put(27.0, 33000);
        table.put(28.0, 41000);
        table.put(29.0, 50000);
        table.put(30.0, 62000);
        return table;
    }

    // --- Monster Abilities ---

    public List<LotrMonsterAbility> listAbilities(String monsterName) {
        return abilityRepository.findByMonsterName(monsterName);
    }

    public List<LotrMonsterAbility> listAbilitiesByType(String monsterName, String type) {
        return abilityRepository.findByMonsterNameAndType(monsterName, type);
    }

    public List<LotrMonsterAbility> listAbilitiesBySubtype(String monsterName, String subtype) {
        return abilityRepository.findByMonsterNameAndTypeContainingIgnoreCase(monsterName, subtype);
    }

    public List<LotrMonsterAbility> searchAbilities(String monsterName, String keyword) {
        return abilityRepository.searchByMonsterNameAndKeyword(monsterName, keyword);
    }

    @Transactional
    public LotrMonsterAbility createAbility(String monsterName, String name, String type,
                                             String description, int attackBonus, String damageDice,
                                             String damageType, String saveAbility, int saveDc) {
        LotrMonsterAbility ability = new LotrMonsterAbility();
        ability.setMonsterName(monsterName);
        ability.setName(name);
        ability.setType(type);
        ability.setDescription(description);
        ability.setAttackBonus(attackBonus);
        ability.setDamageDice(damageDice);
        ability.setDamageType(damageType);
        ability.setSaveAbility(saveAbility);
        ability.setSaveDc(saveDc);
        ability.setActive(true);
        log.info("Created LOTR monster ability: {} for {}", name, monsterName);
        return abilityRepository.save(ability);
    }

    @Transactional
    public LotrMonsterAbility saveAbility(LotrMonsterAbility ability) {
        return abilityRepository.save(ability);
    }

    @Transactional
    public void deleteAbility(Long id) {
        abilityRepository.deleteById(id);
    }

    public LotrMonsterAbility findAbilityById(Long id) {
        return abilityRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ability not found: " + id));
    }

    // --- Reference Data ---

    public List<String> listMonsterTypes() {
        return repository.findAll().stream()
                .map(LotrMonster::getMonsterType)
                .distinct()
                .collect(Collectors.toList());
    }

    public List<String> listMonsterSizes() {
        // LOTR monster sizes based on Tolkien lore
        return List.of("Tiny", "Small", "Medium", "Large", "Huge", "Gargantuan");
    }

    public List<String> listAlignments() {
        // LOTR alignments based on moral alignment in Tolkien's world
        return List.of("Lawful Good", "Neutral Good", "Chaotic Good",
                "Lawful Neutral", "True Neutral", "Chaotic Neutral",
                "Lawful Evil", "Neutral Evil", "Chaotic Evil");
    }

    public List<String> listAbilityTypes() {
        return abilityRepository.findByIsActiveTrue().stream()
                .map(LotrMonsterAbility::getType)
                .filter(t -> t != null)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Calculate XP value from challenge rating (simplified D&D 5e table).
     */
    private int calculateXp(double cr) {
        if (cr <= 0) return 10;
        if (cr <= 0.5) return 25;
        if (cr <= 1) return 50;
        if (cr <= 2) return 100;
        if (cr <= 3) return 200;
        if (cr <= 4) return 350;
        if (cr <= 5) return 500;
        if (cr <= 6) return 700;
        if (cr <= 7) return 900;
        if (cr <= 8) return 1100;
        if (cr <= 9) return 1300;
        if (cr <= 10) return 1500;
        if (cr <= 15) return 4200;
        if (cr <= 20) return 9500;
        return 19500;
    }
}
