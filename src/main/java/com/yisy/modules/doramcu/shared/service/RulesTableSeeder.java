package com.yisy.modules.doramcu.shared.service;
import com.yisy.modules.doramcu.model.players.Player;

import com.yisy.modules.doramcu.shared.model.GameSystem;
import com.yisy.modules.doramcu.shared.model.RulesTable;
import com.yisy.modules.doramcu.shared.repository.UnifiedRulesTableRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Seeds structured {@link RulesTable} entries for game systems that previously stored
 * table data only as free-text in {@code RulesEntry.text}.
 * <p>
 * Previously, only D&D 5e and MechWarrior populated RulesTable entries. This service
 * adds structured table data for:
 * </p>
 * <ul>
 *   <li>Star Wars: Legion attack/defense dice results, X-Wing damage cards, cover modifiers</li>
 *   <li>Babylon 5: ship combat hit locations, damage tables</li>
 *   <li>Starship Troopers: Terran and Alien attack tables by odds ratio</li>
 * </ul>
 * <p>
 * On first startup, tables are seeded into the database. On subsequent restarts, the
 * existing count check skips re-seeding.
 * </p>
 */
public class RulesTableSeeder {

    private static final Logger log = LoggerFactory.getLogger(RulesTableSeeder.class);

    private final UnifiedRulesTableRepository tableRepository;

    public RulesTableSeeder(UnifiedRulesTableRepository tableRepository) {
        this.tableRepository = tableRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initialize() {
        long existingCount = tableRepository.count();
        if (existingCount >= 80) {
            log.info("RulesTable already populated with {} entries, skipping seed", existingCount);
            return;
        }

        log.info("Seeding RulesTable entries for Star Wars, Babylon 5, and Starship Troopers...");
        List<RulesTable> allTables = new ArrayList<>();
        allTables.addAll(createStarWarsTables());
        allTables.addAll(createB5Tables());
        allTables.addAll(createStarshipTroopersTables());

        tableRepository.saveAll(allTables);
        log.info("Seeded {} RulesTable entries", allTables.size());
    }

    // ========================================================================
    // Star Wars Rules Tables
    // ========================================================================

    private List<RulesTable> createStarWarsTables() {
        List<RulesTable> tables = new ArrayList<>();

        // --- Legion Ground Combat: Attack Dice Results ---
        tables.add(createTable(
                GameSystem.STAR_WARS, "Legion Attack Dice Results", "Star Wars: Legion",
                "TABLE_ATTACK_DICE", "Ground Combat", 1,
                "Hit", "Deals 1 damage to the target. Applied to shields first, then hull."));
        tables.add(createTable(
                GameSystem.STAR_WARS, "Legion Attack Dice Results", "Star Wars: Legion",
                "TABLE_ATTACK_DICE", "Ground Combat", 2,
                "Glance", "Deals 1 damage, but can be blocked by certain abilities. Less reliable than a hit."));
        tables.add(createTable(
                GameSystem.STAR_WARS, "Legion Attack Dice Results", "Star Wars: Legion",
                "TABLE_ATTACK_DICE", "Ground Combat", 3,
                "Crit", "Deals 1 damage and triggers a critical effect (e.g., suppression, disruption, or extra damage)."));
        tables.add(createTable(
                GameSystem.STAR_WARS, "Legion Attack Dice Results", "Star Wars: Legion",
                "TABLE_ATTACK_DICE", "Ground Combat", 4,
                "Blank", "No effect."));

        // --- Legion Ground Combat: Defense Dice Results ---
        tables.add(createTable(
                GameSystem.STAR_WARS, "Legion Defense Dice Results", "Star Wars: Legion",
                "TABLE_DEFENSE_DICE", "Ground Combat", 1,
                "Evade", "Removes 1 Hit or Glance from the attack."));
        tables.add(createTable(
                GameSystem.STAR_WARS, "Legion Defense Dice Results", "Star Wars: Legion",
                "TABLE_DEFENSE_DICE", "Ground Combat", 2,
                "Shield", "Removes 1 Hit or Glance. Some units have limited shield tokens."));
        tables.add(createTable(
                GameSystem.STAR_WARS, "Legion Defense Dice Results", "Star Wars: Legion",
                "TABLE_DEFENSE_DICE", "Ground Combat", 3,
                "Blank", "No effect."));

        // --- Legion Cover Modifiers ---
        tables.add(createTable(
                GameSystem.STAR_WARS, "Legion Cover Modifiers", "Star Wars: Legion",
                "TABLE_COVER", "Ground Combat", 1,
                "Light Cover", "+1 yellow defense dice"));
        tables.add(createTable(
                GameSystem.STAR_WARS, "Legion Cover Modifiers", "Star Wars: Legion",
                "TABLE_COVER", "Ground Combat", 2,
                "Heavy Cover", "+2 yellow defense dice; may block line of sight"));

        // --- Legion Range Modifiers ---
        tables.add(createTable(
                GameSystem.STAR_WARS, "Legion Range Modifiers", "Star Wars: Legion",
                "TABLE_RANGE", "Ground Combat", 1,
                "Range 1 (Close)", "Full attack dice, no penalty"));
        tables.add(createTable(
                GameSystem.STAR_WARS, "Legion Range Modifiers", "Star Wars: Legion",
                "TABLE_RANGE", "Ground Combat", 2,
                "Range 2 (Medium)", "Full attack dice, no penalty (most weapons)"));
        tables.add(createTable(
                GameSystem.STAR_WARS, "Legion Range Modifiers", "Star Wars: Legion",
                "TABLE_RANGE", "Ground Combat", 3,
                "Range 3 (Long)", "May reduce attack dice or add penalties"));

        // --- X-Wing Space Combat: Attack Dice Results ---
        tables.add(createTable(
                GameSystem.STAR_WARS, "X-Wing Attack Dice Results", "Star Wars: X-Wing",
                "TABLE_SPACE_ATTACK", "Space Combat", 1,
                "Hit", "Deals 1 damage. Assign a face-down damage card to the defending ship."));
        tables.add(createTable(
                GameSystem.STAR_WARS, "X-Wing Attack Dice Results", "Star Wars: X-Wing",
                "TABLE_SPACE_ATTACK", "Space Combat", 2,
                "Crit", "Deals 1 damage and reveals the damage card face-up (critical hit with ongoing effect)."));
        tables.add(createTable(
                GameSystem.STAR_WARS, "X-Wing Attack Dice Results", "Star Wars: X-Wing",
                "TABLE_SPACE_ATTACK", "Space Combat", 3,
                "Focus", "Can be converted to a Hit, or used to add 1 green defense dice to a friendly ship."));
        tables.add(createTable(
                GameSystem.STAR_WARS, "X-Wing Attack Dice Results", "Star Wars: X-Wing",
                "TABLE_SPACE_ATTACK", "Space Combat", 4,
                "Blank", "No effect."));

        // --- X-Wing Space Combat: Defense Dice Results ---
        tables.add(createTable(
                GameSystem.STAR_WARS, "X-Wing Defense Dice Results", "Star Wars: X-Wing",
                "TABLE_SPACE_DEFENSE", "Space Combat", 1,
                "Evade", "Removes 1 Hit or Crit from the attack."));
        tables.add(createTable(
                GameSystem.STAR_WARS, "X-Wing Defense Dice Results", "Star Wars: X-Wing",
                "TABLE_SPACE_DEFENSE", "Space Combat", 2,
                "Shield", "If the ship has shields, removes 1 Hit or Crit and does not assign a damage card."));
        tables.add(createTable(
                GameSystem.STAR_WARS, "X-Wing Defense Dice Results", "Star Wars: X-Wing",
                "TABLE_SPACE_DEFENSE", "Space Combat", 3,
                "Blank", "No effect."));

        // --- X-Wing Damage Cards (Critical Hits) ---
        tables.add(createTable(
                GameSystem.STAR_WARS, "X-Wing Critical Hit Cards", "Star Wars: X-Wing",
                "TABLE_DAMAGE_CARD", "Space Combat", 1,
                "Stress", "Ship cannot perform actions next activation (most common face-down result)"));
        tables.add(createTable(
                GameSystem.STAR_WARS, "X-Wing Critical Hit Cards", "Star Wars: X-Wing",
                "TABLE_DAMAGE_CARD", "Space Combat", 2,
                "Hit", "No ongoing effect â€” just damage absorption (face-down)"));
        tables.add(createTable(
                GameSystem.STAR_WARS, "X-Wing Critical Hit Cards", "Star Wars: X-Wing",
                "TABLE_DAMAGE_CARD", "Space Combat", 3,
                "Engine Damage", "Ship loses maneuverability"));
        tables.add(createTable(
                GameSystem.STAR_WARS, "X-Wing Critical Hit Cards", "Star Wars: X-Wing",
                "TABLE_DAMAGE_CARD", "Space Combat", 4,
                "Hull Breach", "Ship takes extra damage"));
        tables.add(createTable(
                GameSystem.STAR_WARS, "X-Wing Critical Hit Cards", "Star Wars: X-Wing",
                "TABLE_DAMAGE_CARD", "Space Combat", 5,
                "Shield Modulator Failure", "Shields cannot regenerate"));
        tables.add(createTable(
                GameSystem.STAR_WARS, "X-Wing Critical Hit Cards", "Star Wars: X-Wing",
                "TABLE_DAMAGE_CARD", "Space Combat", 6,
                "Weapon Malfunction", "Ship loses attack dice"));
        tables.add(createTable(
                GameSystem.STAR_WARS, "X-Wing Critical Hit Cards", "Star Wars: X-Wing",
                "TABLE_DAMAGE_CARD", "Space Combat", 7,
                "Pilot Wounded", "Pilot skill reduced"));

        // --- Galaxy-Scale Combat (Rebellion) ---
        tables.add(createTable(
                GameSystem.STAR_WARS, "Rebellion Space Combat d6 Results", "Star Wars: Rebellion",
                "TABLE_GALAXY_COMBAT", "Galaxy Control", 1,
                "Roll 1-3 (Miss)", "No hit â€” attack fails"));
        tables.add(createTable(
                GameSystem.STAR_WARS, "Rebellion Space Combat d6 Results", "Star Wars: Rebellion",
                "TABLE_GALAXY_COMBAT", "Galaxy Control", 2,
                "Roll 4-6 (Hit)", "Deals 1 hit â€” assigned to opposing ships' shields first, then hull"));

        // --- RPG Combat (Force & Destiny) ---
        tables.add(createTable(
                GameSystem.STAR_WARS, "FFG Narrative Dice: Success Results", "Force & Destiny",
                "TABLE_RPG_DICE", "RPG Mechanics", 1,
                "Success", "Attack hits â€” deals damage to target"));
        tables.add(createTable(
                GameSystem.STAR_WARS, "FFG Narrative Dice: Success Results", "Force & Destiny",
                "TABLE_RPG_DICE", "RPG Mechanics", 2,
                "Failure", "Attack misses â€” no damage dealt"));
        tables.add(createTable(
                GameSystem.STAR_WARS, "FFG Narrative Dice: Success Results", "Force & Destiny",
                "TABLE_RPG_DICE", "RPG Mechanics", 3,
                "Advantage", "Side effect: positioning, opening, or tactical benefit"));
        tables.add(createTable(
                GameSystem.STAR_WARS, "FFG Narrative Dice: Success Results", "Force & Destiny",
                "TABLE_RPG_DICE", "RPG Mechanics", 4,
                "Threat", "Side effect: danger, exposure, or tactical penalty"));
        tables.add(createTable(
                GameSystem.STAR_WARS, "FFG Narrative Dice: Success Results", "Force & Destiny",
                "TABLE_RPG_DICE", "RPG Mechanics", 5,
                "Triumph", "Major narrative success â€” always counts as Success + Advantage with special effect"));
        tables.add(createTable(
                GameSystem.STAR_WARS, "FFG Narrative Dice: Success Results", "Force & Destiny",
                "TABLE_RPG_DICE", "RPG Mechanics", 6,
                "Despair", "Major narrative failure â€” always counts as Failure + Threat with special effect"));

        return tables;
    }

    // ========================================================================
    // Babylon 5 Rules Tables
    // ========================================================================

    private List<RulesTable> createB5Tables() {
        List<RulesTable> tables = new ArrayList<>();

        // --- B5 Ship Combat: Hit Location Table (2D6) ---
        tables.add(createTable(
                GameSystem.BABYLON_5, "Ship Combat Hit Location", "B5 RPG Rulebook",
                "TABLE_HIT_LOCATION", "Ship Combat", 2,
                "Hull (Front)", "Direct hit to forward hull â€” structural damage"));
        tables.add(createTable(
                GameSystem.BABYLON_5, "Ship Combat Hit Location", "B5 RPG Rulebook",
                "TABLE_HIT_LOCATION", "Ship Combat", 3,
                "Bridge", "Command center hit â€” possible crew casualties, system degradation"));
        tables.add(createTable(
                GameSystem.BABYLON_5, "Ship Combat Hit Location", "B5 RPG Rulebook",
                "TABLE_HIT_LOCATION", "Ship Combat", 4,
                "Engines", "Engine damage â€” reduced speed or maneuverability"));
        tables.add(createTable(
                GameSystem.BABYLON_5, "Ship Combat Hit Location", "B5 RPG Rulebook",
                "TABLE_HIT_LOCATION", "Ship Combat", 5,
                "Weapons Port", "Weapon systems damaged â€” reduced firepower"));
        tables.add(createTable(
                GameSystem.BABYLON_5, "Ship Combat Hit Location", "B5 RPG Rulebook",
                "TABLE_HIT_LOCATION", "Ship Combat", 6,
                "Shields Generator", "Shield systems degraded â€” reduced shield capacity"));
        tables.add(createTable(
                GameSystem.BABYLON_5, "Ship Combat Hit Location", "B5 RPG Rulebook",
                "TABLE_HIT_LOCATION", "Ship Combat", 7,
                "Hull (Center)", "Direct hit to midship hull â€” structural damage"));
        tables.add(createTable(
                GameSystem.BABYLON_5, "Ship Combat Hit Location", "B5 RPG Rulebook",
                "TABLE_HIT_LOCATION", "Ship Combat", 8,
                "Cargo Bay", "Cargo damage â€” possible fire or decompression"));
        tables.add(createTable(
                GameSystem.BABYLON_5, "Ship Combat Hit Location", "B5 RPG Rulebook",
                "TABLE_HIT_LOCATION", "Ship Combat", 9,
                "Life Support", "Environmental damage â€” crew suffocation risk"));
        tables.add(createTable(
                GameSystem.BABYLON_5, "Ship Combat Hit Location", "B5 RPG Rulebook",
                "TABLE_HIT_LOCATION", "Ship Combat", 10,
                "Hull (Rear)", "Direct hit to aft hull â€” structural damage"));
        tables.add(createTable(
                GameSystem.BABYLON_5, "Ship Combat Hit Location", "B5 RPG Rulebook",
                "TABLE_HIT_LOCATION", "Ship Combat", 11,
                "Sensors", "Sensor damage â€” reduced detection range"));
        tables.add(createTable(
                GameSystem.BABYLON_5, "Ship Combat Hit Location", "B5 RPG Rulebook",
                "TABLE_HIT_LOCATION", "Ship Combat", 12,
                "Critical Systems", "Catastrophic hit â€” possible ship destruction"));

        // --- B5 Damage Severity Table (2D6) ---
        tables.add(createTable(
                GameSystem.BABYLON_5, "Damage Severity", "B5 RPG Rulebook",
                "TABLE_DAMAGE_SEVERITY", "Ship Combat", 2,
                "Minor", "Cosmetic damage â€” no functional impact"));
        tables.add(createTable(
                GameSystem.BABYLON_5, "Damage Severity", "B5 RPG Rulebook",
                "TABLE_DAMAGE_SEVERITY", "Ship Combat", 3,
                "Light", "Slight system degradation â€” -1 to related checks"));
        tables.add(createTable(
                GameSystem.BABYLON_5, "Damage Severity", "B5 RPG Rulebook",
                "TABLE_DAMAGE_SEVERITY", "Ship Combat", 4,
                "Moderate", "System impaired â€” -2 to related checks, repair needed"));
        tables.add(createTable(
                GameSystem.BABYLON_5, "Damage Severity", "B5 RPG Rulebook",
                "TABLE_DAMAGE_SEVERITY", "Ship Combat", 5,
                "Heavy", "System offline â€” complete failure until repaired"));
        tables.add(createTable(
                GameSystem.BABYLON_5, "Damage Severity", "B5 RPG Rulebook",
                "TABLE_DAMAGE_SEVERITY", "Ship Combat", 6,
                "Catastrophic", "System destroyed â€” permanent loss, hull breach possible"));

        // --- B5 Task Attempt Results (2D6) ---
        tables.add(createTable(
                GameSystem.BABYLON_5, "Task Attempt Results", "B5 RPG Rulebook",
                "TABLE_TASK_ATTEMPT", "Dice Mechanics", 2,
                "Fumble", "Task fails catastrophically â€” negative consequences"));
        tables.add(createTable(
                GameSystem.BABYLON_5, "Task Attempt Results", "B5 RPG Rulebook",
                "TABLE_TASK_ATTEMPT", "Dice Mechanics", 3,
                "Failure", "Task fails â€” no effect"));
        tables.add(createTable(
                GameSystem.BABYLON_5, "Task Attempt Results", "B5 RPG Rulebook",
                "TABLE_TASK_ATTEMPT", "Dice Mechanics", 4,
                "Marginal Success", "Task succeeds with difficulty â€” partial benefit"));
        tables.add(createTable(
                GameSystem.BABYLON_5, "Task Attempt Results", "B5 RPG Rulebook",
                "TABLE_TASK_ATTEMPT", "Dice Mechanics", 5,
                "Success", "Task succeeds â€” full benefit"));
        tables.add(createTable(
                GameSystem.BABYLON_5, "Task Attempt Results", "B5 RPG Rulebook",
                "TABLE_TASK_ATTEMPT", "Dice Mechanics", 6,
                "Outstanding Success", "Task succeeds spectacularly â€” extra benefit"));

        return tables;
    }

    // ========================================================================
    // Starship Troopers Rules Tables
    // ========================================================================

    private List<RulesTable> createStarshipTroopersTables() {
        List<RulesTable> tables = new ArrayList<>();

        // --- Terran Attack Table outcomes (representative odds ratios) ---
        String[] outcomeLabels = {"Miss", "Disrupted", "WIA", "KIA"};
        String[] outcomeDescs = {
                "No effect â€” attack fails",
                "Target cannot move or attack next turn",
                "Unit wounded with reduced effectiveness",
                "Unit eliminated from play"
        };

        for (int i = 0; i < outcomeLabels.length; i++) {
            tables.add(createTable(
                    GameSystem.UNIVERSAL, "Combat Outcome Codes", "Starship Troopers",
                    "TABLE_COMBAT_OUTCOME", "Combat", i + 1,
                    outcomeLabels[i], outcomeDescs[i]));
        }

        // --- Terran Attack Table at 2:1 odds (representative) ---
        tables.add(createTable(
                GameSystem.UNIVERSAL, "Terran Attack Table (2:1 Odds)", "Starship Troopers",
                "TABLE_TERRAN_ATTACK", "Combat", 1,
                "Disrupted", "Target cannot move or attack next turn"));
        tables.add(createTable(
                GameSystem.UNIVERSAL, "Terran Attack Table (2:1 Odds)", "Starship Troopers",
                "TABLE_TERRAN_ATTACK", "Combat", 2,
                "Disrupted", "Target cannot move or attack next turn"));
        tables.add(createTable(
                GameSystem.UNIVERSAL, "Terran Attack Table (2:1 Odds)", "Starship Troopers",
                "TABLE_TERRAN_ATTACK", "Combat", 3,
                "WIA", "Unit wounded with reduced effectiveness"));
        tables.add(createTable(
                GameSystem.UNIVERSAL, "Terran Attack Table (2:1 Odds)", "Starship Troopers",
                "TABLE_TERRAN_ATTACK", "Combat", 4,
                "WIA", "Unit wounded with reduced effectiveness"));
        tables.add(createTable(
                GameSystem.UNIVERSAL, "Terran Attack Table (2:1 Odds)", "Starship Troopers",
                "TABLE_TERRAN_ATTACK", "Combat", 5,
                "KIA", "Unit eliminated from play"));
        tables.add(createTable(
                GameSystem.UNIVERSAL, "Terran Attack Table (2:1 Odds)", "Starship Troopers",
                "TABLE_TERRAN_ATTACK", "Combat", 6,
                "KIA", "Unit eliminated from play"));

        // --- Alien Attack Table at 2:1 odds (representative) ---
        tables.add(createTable(
                GameSystem.UNIVERSAL, "Alien Attack Table (2:1 Odds)", "Starship Troopers",
                "TABLE_ALIEN_ATTACK", "Combat", 1,
                "Disrupted", "Target cannot move or attack next turn"));
        tables.add(createTable(
                GameSystem.UNIVERSAL, "Alien Attack Table (2:1 Odds)", "Starship Troopers",
                "TABLE_ALIEN_ATTACK", "Combat", 2,
                "Disrupted", "Target cannot move or attack next turn"));
        tables.add(createTable(
                GameSystem.UNIVERSAL, "Alien Attack Table (2:1 Odds)", "Starship Troopers",
                "TABLE_ALIEN_ATTACK", "Combat", 3,
                "WIA", "Unit wounded with reduced effectiveness"));
        tables.add(createTable(
                GameSystem.UNIVERSAL, "Alien Attack Table (2:1 Odds)", "Starship Troopers",
                "TABLE_ALIEN_ATTACK", "Combat", 4,
                "WIA", "Unit wounded with reduced effectiveness"));
        tables.add(createTable(
                GameSystem.UNIVERSAL, "Alien Attack Table (2:1 Odds)", "Starship Troopers",
                "TABLE_ALIEN_ATTACK", "Combat", 5,
                "KIA", "Unit eliminated from play"));
        tables.add(createTable(
                GameSystem.UNIVERSAL, "Alien Attack Table (2:1 Odds)", "Starship Troopers",
                "TABLE_ALIEN_ATTACK", "Combat", 6,
                "KIA", "Unit eliminated from play"));

        // --- Disrupted Status Effects ---
        tables.add(createTable(
                GameSystem.UNIVERSAL, "Disrupted Status Effects", "Starship Troopers",
                "TABLE_DISRUPTED", "Combat", 1,
                "Cannot Move", "Unit may not move during the next player segment"));
        tables.add(createTable(
                GameSystem.UNIVERSAL, "Disrupted Status Effects", "Starship Troopers",
                "TABLE_DISRUPTED", "Combat", 2,
                "Cannot Tunnel", "Unit may not re-enter tunnel systems"));
        tables.add(createTable(
                GameSystem.UNIVERSAL, "Disrupted Status Effects", "Starship Troopers",
                "TABLE_DISRUPTED", "Combat", 3,
                "Cannot Attack", "Unit may not engage in any type of attack during the next player segment"));

        return tables;
    }

    // ========================================================================
    // Helper
    // ========================================================================

    private RulesTable createTable(
            GameSystem gameSystem, String tableName, String source,
            String category, String chapter, int rollValue,
            String resultLabel, String description) {

        RulesTable table = new RulesTable();
        table.setGameSystem(gameSystem);
        table.setTableName(tableName);
        table.setSource(source);
        table.setCategory(category);
        table.setChapter(chapter);
        table.setRollValue(rollValue);
        table.setResultLabel(resultLabel);
        table.setDescription(description);
        table.setActive(true);
        table.setVersion(1);
        return table;
    }
}
