package com.yisy.modules.doramcu.dnd5e.service;

import com.yisy.modules.doramcu.dnd5e.model.Dnd5eSpell;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5eSpellRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for D&D 5e spell management.
 */
@Service
public class Dnd5eSpellService {

    private static final Logger log = LoggerFactory.getLogger(Dnd5eSpellService.class);
    private static final int EXPECTED_SPELL_COUNT = 30;

    private final Dnd5eSpellRepository spellRepository;
    private boolean initialized = false;

    public Dnd5eSpellService(Dnd5eSpellRepository spellRepository) {
        this.spellRepository = spellRepository;
    }

    // ==================== Initialization ====================

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initializeSpellList() {
        if (initialized) {
            return;
        }
        long existingCount = spellRepository.findByActiveTrue().size();
        if (existingCount >= EXPECTED_SPELL_COUNT) {
            log.info("D&D 5e spell list already populated with {} spells", existingCount);
            initialized = true;
            return;
        }

        log.info("Initializing D&D 5e spell list...");
        populateSpells();
        initialized = true;
        log.info("D&D 5e spell list initialized: {} spells", spellRepository.findByActiveTrue().size());
    }

    // ==================== Spell Population ====================

    @Transactional
    public void populateSpells() {
        List<Dnd5eSpell> spells = new ArrayList<>();

        // --- Cantrips (Level 0) ---
        spells.add(createSpell("Acid Splash", 0, "Evocation", "1 action", "60 feet", "V, S", "Instantaneous", false, false,
                "1d10 acid", null, "You hurl a bubble of acid.", "[Wizard]"));
        spells.add(createSpell("Blade Ward", 0, "Abjuration", "1 action", "Self", "S", "1 round", false, false,
                null, null, "Your hands glow red. You gain +1 AC until the start of your next turn.", "[Sorcerer, Wizard]"));
        spells.add(createSpell("Chill Touch", 0, "Necromancy", "1 action", "120 feet", "V, S", "1 round", false, false,
                "1d8 necrotic", null, "A ghostly green hand appears. On a hit, target can't regain HP until next turn.", "[Sorcerer, Wizard]"));
        spells.add(createSpell("Fire Bolt", 0, "Evocation", "1 action", "120 feet", "V, S", "Instantaneous", false, false,
                "1d10 fire", null, "You hurl a mote of fire at a target.", "[Sorcerer, Wizard]"));
        spells.add(createSpell("Guidance", 0, "Divination", "1 action", "Touch", "V, S", "1 minute", true, false,
                null, null, "You touch a creature. It gains +1d4 to one ability check of your choice.", "[Bard, Druid, Paladin, Ranger]"));
        spells.add(createSpell("Light", 0, "Evocation", "1 action", "Touch", "V, M", "1 hour", false, false,
                null, null, "An object you touch shines with bright light in a 30-foot radius.", "[Bard, Sorcerer, Wizard]"));
        spells.add(createSpell("Mage Hand", 0, "Conjuration", "1 action", "30 feet", "V, S", "1 minute", false, false,
                null, null, "A spectral hand appears and can manipulate objects.", "[Bard, Sorcerer, Wizard]"));
        spells.add(createSpell("Mending", 0, "Transmutation", "1 minute", "Touch", "V, S, M", "Instantaneous", false, false,
                null, null, "You repair an object no larger than 1 cubic foot.", "[Bard, Cleric, Druid]"));
        spells.add(createSpell("Message", 0, "Illusion", "1 action", "120 feet", "V, M", "1 round", false, false,
                null, null, "You whisper a message to one creature within range.", "[Bard, Sorcerer, Wizard]"));
        spells.add(createSpell("Prestidigitation", 0, "Transmutation", "1 action", "10 feet", "V", "1 hour", false, false,
                null, null, "A minor magical effect: clean, light, flavor, or small sensory effect.", "[Bard, Sorcerer, Wizard]"));
        spells.add(createSpell("Ray of Frost", 0, "Evocation", "1 action", "60 feet", "V, S", "Instantaneous", false, false,
                "1d8 cold", null, "A chilling ray of blue light strikes a target.", "[Wizard]"));
        spells.add(createSpell("Sacred Flame", 0, "Evocation", "1 action", "60 feet", "V, S", "Instantaneous", false, false,
                "1d8 radiant", null, "Flame light appears above a creature. DEX save or take damage. No cover.", "[Cleric]"));
        spells.add(createSpell("Spare the Dying", 0, "Necromancy", "1 action", "Touch", "V, S", "Instantaneous", false, false,
                null, null, "A dying creature becomes stable.", "[Cleric, Druid]"));
        spells.add(createSpell("Thaumaturgy", 0, "Transmutation", "1 action", "30 feet", "V", "1 minute", false, false,
                null, null, "Your voice booms, eyes glow, or other minor sensory effects.", "[Cleric]"));
        spells.add(createSpell("True Strike", 0, "Divination", "1 action", "30 feet", "V", "1 round", false, false,
                null, null, "You gain advantage on the next attack roll before the spell ends.", "[Sorcerer, Wizard]"));

        // --- Level 1 Spells ---
        spells.add(createSpell("Burning Hands", 1, "Evocation", "1 action", "Self (15-foot cone)", "V, S", "Instantaneous", false, false,
                "3d6 fire", null, "Flames burst from your hands. Each creature in a 15-foot cone makes a DEX save.", "[Bard, Sorcerer, Wizard]"));
        spells.add(createSpell("Cure Wounds", 1, "Evocation", "1 action", "Touch", "V, S", "Instantaneous", false, false,
                null, "1d8+casting mod healing", "A creature you touch regains hit points.", "[Bard, Cleric, Druid, Paladin, Ranger]"));
        spells.add(createSpell("Detect Magic", 1, "Divination", "1 action", "Self", "V, S", "10 minutes", true, true,
                null, null, "You sense magic within 30 feet. Concentration.", "[Bard, Cleric, Druid, Paladin, Ranger, Sorcerer, Wizard, Warlock]"));
        spells.add(createSpell("Disguise Self", 1, "Illusion", "1 action", "Self", "V", "1 hour", true, false,
                null, null, "You disguise yourself with a Charisma check vs. Wisdom (Insight). Concentration.", "[Bard, Sorcerer, Wizard]"));
        spells.add(createSpell("Expeditious Retreat", 1, "Abjuration", "1 bonus action", "Self", "V, S", "10 minutes", true, false,
                null, null, "Your speed increases by 10 feet. Concentration.", "[Bard, Rogue, Sorcerer, Wizard]"));
        spells.add(createSpell("Fog Cloud", 1, "Conjuration", "1 action", "120 feet", "V, S", "1 hour", true, false,
                null, null, "A 20-foot sphere of fog heavily obscures the area. Concentration.", "[Druid, Sorcerer, Wizard]"));
        spells.add(createSpell("Goodberry", 1, "Transmutation", "1 action", "Touch", "V, S, M", "1 hour", false, false,
                null, "1 healing per berry", "Up to 5 berries restore 1 HP and cure poisoned when eaten.", "[Druid, Ranger]"));
        spells.add(createSpell("Healing Word", 1, "Evocation", "1 bonus action", "60 feet", "V", "Instantaneous", false, false,
                null, "1d4+casting mod healing", "A creature you can see regains hit points.", "[Bard, Cleric, Druid]"));
        spells.add(createSpell("Identify", 1, "Divination", "1 minute", "Touch", "V, M", "Instantaneous", false, true,
                null, null, "You learn the properties and attunement requirements of a magic item.", "[Bard, Wizard]"));
        spells.add(createSpell("Shield", 1, "Abjuration", "1 reaction", "Self", "V, S", "1 round", false, false,
                null, null, "An invisible barrier gives you +5 AC until the start of your next turn.", "[Sorcerer, Wizard]"));
        spells.add(createSpell("Sleep", 1, "Enchantment", "1 action", "60 feet", "V, M", "1 minute", false, false,
                null, null, "Creatures with 5 HP or less fall unconscious. Total HP = 5d8.", "[Bard, Druid, Sorcerer, Wizard]"));
        spells.add(createSpell("Thunderwave", 1, "Evocation", "1 action", "Self (15-foot cube)", "V, S", "Instantaneous", false, false,
                "2d6 thunder", null, "A wave of thunderous force pushes creatures back 10 feet.", "[Cleric, Sorcerer, Wizard]"));

        // --- Level 2 Spells ---
        spells.add(createSpell("Aid", 2, "Abjuration", "1 action", "120 feet", "V, S, M", "8 hours", false, false,
                null, "4d8+casting mod healing", "Up to 3 creatures gain +2 max HP and current HP.", "[Cleric, Paladin]"));
        spells.add(createSpell("Blindness/Deafness", 2, "Necromancy", "1 action", "60 feet", "V", "1 minute", true, false,
                null, null, "A creature is blinded or deafened until the end of its next turn. Concentration.", "[Bard, Sorcerer, Wizard]"));
        spells.add(createSpell("Crown of Madness", 2, "Enchantment", "1 action", "60 feet", "V, S, M", "1 minute", true, false,
                null, null, "A creature is charmed and you control it as a puppet. Concentration.", "[Bard, Sorcerer, Wizard, Warlock]"));
        spells.add(createSpell("Darkness", 2, "Evocation", "1 action", "60 feet", "V", "10 minutes", false, false,
                null, null, "Magical darkness fills a 15-foot radius. Lasts until dispelled.", "[Sorcerer, Warlock, Wizard]"));
        spells.add(createSpell("Enhance Ability", 2, "Transmutation", "1 action", "Touch", "V, S, M", "1 hour", true, false,
                null, null, "A creature gains advantage on one type of ability check. Concentration.", "[Bard, Cleric, Druid]"));
        spells.add(createSpell("Find Familiar", 2, "Conjuration", "1 hour", "30 feet", "V, S, M", "Until dismissed", false, true,
                null, null, "You gain a familiar: bat, cat, frog, hawk, mouse, owl, raven, toad, vulture, weasel, wolf, or imp/pseudodragon.", "[Wizard]"));
        spells.add(createSpell("Flame Blade", 2, "Evocation", "1 bonus action", "Self (5-foot radius)", "V", "10 minutes", true, false,
                "3d6 fire", null, "You create a blade of flame. Melee weapon attack. Concentration.", "[Druid, Sorcerer]"));
        spells.add(createSpell("Gust of Wind", 2, "Evocation", "1 action", "Self (60-foot line)", "V, S, M", "1 round", false, false,
                null, null, "A line of wind knocks creatures prone and extinguishes flames.", "[Druid, Ranger, Sorcerer, Wizard]"));
        spells.add(createSpell("Lesser Restoration", 2, "Abjuration", "1 action", "Touch", "V, S", "Instantaneous", false, false,
                null, null, "A creature ends one condition: blinded, deafened, paralyzed, or poisoned.", "[Bard, Cleric, Druid, Paladin, Ranger]"));
        spells.add(createSpell("Misty Step", 2, "Conjuration", "1 bonus action", "Self", "V", "Instantaneous", false, false,
                null, null, "You teleport up to 30 feet to an unoccupied space you can see.", "[Sorcerer, Warlock, Wizard]"));
        spells.add(createSpell("Scorching Ray", 2, "Evocation", "1 action", "120 feet", "V, S", "Instantaneous", false, false,
                "2d6 fire per ray", null, "You create three rays. Make a ranged spell attack for each.", "[Sorcerer, Wizard]"));
        spells.add(createSpell("See Invisibility", 2, "Divination", "1 action", "Self", "V, M", "1 hour", false, false,
                null, null, "You see invisible creatures and objects as if they were visible.", "[Bard, Ranger, Sorcerer, Wizard]"));
        spells.add(createSpell("Shatter", 2, "Evocation", "1 action", "60 feet", "V", "Instantaneous", false, false,
                "3d8 thunder", null, "A burst of sound in a 10-foot sphere. CON save or take damage.", "[Bard, Sorcerer, Wizard]"));
        spells.add(createSpell("Spider Climb", 2, "Transmutation", "1 action", "Touch", "V, S, M", "1 hour", true, false,
                null, null, "A creature can walk on ceilings and walls. Concentration.", "[Sorcerer, Wizard]"));
        spells.add(createSpell("Web", 2, "Conjuration", "1 action", "60 feet", "V, S, M", "1 hour", true, false,
                null, null, "A 20-foot cube of webbing. Difficult terrain, restrained on fail. Concentration.", "[Wizard]"));

        // --- Level 3 Spells ---
        spells.add(createSpell("Animate Dead", 3, "Necromancy", "1 minute", "10 feet", "V, S, M", "Until dispelled", false, false,
                null, null, "You create an undead servant: skeleton or zombie.", "[Cleric, Wizard]"));
        spells.add(createSpell("Bestow Curse", 3, "Necromancy", "1 action", "Touch", "V, S", "1 minute", true, false,
                null, null, "A creature suffers a curse: disadvantage on checks, max HP reduced, etc. Concentration.", "[Bard, Cleric, Sorcerer, Wizard, Warlock]"));
        spells.add(createSpell("Counterspell", 3, "Abjuration", "1 reaction", "60 feet", "S", "Instantaneous", false, false,
                null, null, "You interrupt a creature casting a spell. Auto-counterspell 3rd level or lower.", "[Sorcerer, Wizard, Warlock]"));
        spells.add(createSpell("Dispel Magic", 3, "Abjuration", "1 action", "120 feet", "V, S", "Instantaneous", false, false,
                null, null, "You attempt to end a spell on a creature, object, or area.", "[Bard, Cleric, Druid, Paladin, Ranger, Sorcerer, Wizard, Warlock]"));
        spells.add(createSpell("Fireball", 3, "Evocation", "1 action", "150 feet", "V, S, M", "Instantaneous", false, false,
                "8d6 fire", null, "A bright streak flashes and explodes in a 20-foot radius. DEX save.", "[Sorcerer, Wizard]"));
        spells.add(createSpell("Fear", 3, "Illusion", "1 action", "60 feet", "V, S, M", "1 minute", true, false,
                null, null, "Creatures in a 30-foot cone make WIS saves or flee. Concentration.", "[Bard, Sorcerer, Warlock, Wizard]"));
        spells.add(createSpell("Fly", 3, "Transmutation", "1 action", "Touch", "V, S, M", "10 minutes", true, false,
                null, null, "A creature gains a flying speed of 60 feet. Concentration.", "[Bard, Sorcerer, Wizard, Warlock]"));
        spells.add(createSpell("Haste", 3, "Transmutation", "1 action", "30 feet", "V, S, M", "1 minute", true, false,
                null, null, "Target's speed doubles, +2 AC, bonus action attack. Concentration.", "[Sorcerer, Wizard]"));
        spells.add(createSpell("Lightning Bolt", 3, "Evocation", "1 action", "Self (100-foot line)", "V, S, M", "Instantaneous", false, false,
                "8d6 lightning", null, "A stroke of lightning in a 100-foot line. DEX save.", "[Druid, Sorcerer, Wizard]"));
        spells.add(createSpell("Magic Missile", 3, "Evocation", "1 action", "120 feet", "V, S", "Instantaneous", false, false,
                "1d4+1 force per missile", null, "Three darts of force hit automatically. No save, no miss.", "[Sorcerer, Wizard]"));
        spells.add(createSpell("Remove Curse", 3, "Abjuration", "1 action", "Touch", "V, S", "Instantaneous", false, false,
                null, null, "You end one curse on a creature or object.", "[Bard, Cleric, Paladin]"));
        spells.add(createSpell("Spiritual Weapon", 3, "Evocation", "1 bonus action", "60 feet", "V, S", "1 minute", false, false,
                "1d8+casting mod force", null, "A spectral weapon appears. Bonus action to attack.", "[Cleric, Paladin]"));
        spells.add(createSpell("Water Breathing", 3, "Transmutation", "1 action", "30 feet", "V, M", "24 hours", false, false,
                null, null, "Up to 10 creatures can breathe underwater.", "[Druid]"));

        spellRepository.saveAll(spells);
        log.info("Populated {} D&D 5e spells", spells.size());
    }

    // ==================== Helper Methods ====================

    private Dnd5eSpell createSpell(String name, int level, String school, String castingTime, String range,
                                    String components, String duration, boolean concentration, boolean ritual,
                                    String damageFormula, String healingFormula, String description, String classes) {
        Dnd5eSpell spell = new Dnd5eSpell();
        spell.setName(name);
        spell.setLevel(level);
        spell.setSchool(school);
        spell.setCastingTime(castingTime);
        spell.setRange(range);
        spell.setComponents(components);
        spell.setDuration(duration);
        spell.setConcentration(concentration);
        spell.setRitual(ritual);
        spell.setDamageFormula(damageFormula);
        spell.setHealingFormula(healingFormula);
        spell.setDescription(description);
        spell.setClasses(classes);
        spell.setActive(true);
        return spell;
    }

    // ==================== Query Methods ====================

    /**
     * Get all active spells.
     */
    public List<Dnd5eSpell> getAllSpells() {
        return spellRepository.findByActiveTrue();
    }

    /**
     * Get spells by level.
     */
    public List<Dnd5eSpell> getSpellsByLevel(int level) {
        return spellRepository.findByLevelAndActiveTrue(level);
    }

    /**
     * Get spells by school.
     */
    public List<Dnd5eSpell> getSpellsBySchool(String school) {
        return spellRepository.findBySchoolAndActiveTrue(school);
    }

    /**
     * Search spells by name.
     */
    public List<Dnd5eSpell> searchSpells(String keyword) {
        return spellRepository.findByNameContainingIgnoreCaseAndActiveTrue(keyword);
    }
}
