package com.yisy.modules.doramcu.sfb.model;

import com.yisy.modules.doramcu.shared.model.GameSystem;
import com.yisy.modules.doramcu.shared.model.RulesEntry;

import java.util.*;

/**
 * In-memory rules reference database for SFB.
 * <p>
 * Provides 30+ SFB-themed rules covering combat, diplomacy, exploration,
 * economy, and more. Uses the shared unified RulesEntry entity.
 * </p>
 */
public class SfbRulesDatabase {

    private final List<RulesEntry> entries = new ArrayList<>();

    /**
     * Initializes the rules database with SFB-themed rules.
     */
    public void initialize() {
        entries.add(makeEntry("sfb-001", "Dice Mechanics", "SFB Core Rules", "Chapter 1", "Mechanics",
                "All ability checks use 2D6 + modifier. Roll under the target number to succeed.", ""));
        entries.add(makeEntry("sfb-002", "Melee Combat", "SFB Combat Rules", "Chapter 2", "Combat",
                "Attack roll: 2D6 + St vs Parry roll: 2D6 + Dx + weapon parry. Damage: 2D6 + weapon damage - armor.", ""));
        entries.add(makeEntry("sfb-003", "Firearms Combat", "SFB Combat Rules", "Chapter 2", "Combat",
                "Attack roll: 2D6 + Dx vs Dodge roll: 2D6 + Dx. Damage: 2D6 + weapon damage - armor.", ""));
        entries.add(makeEntry("sfb-004", "Ship Combat", "SFB Naval Rules", "Chapter 3", "Combat",
                "Ship combat uses 32-impulse turns. Each impulse, allocate energy to weapons, shields, and movement.", ""));
        entries.add(makeEntry("sfb-005", "Phaser Weapons", "SFB Weapon Rules", "Chapter 3", "Weapons",
                "Phasers have range classes: Near, Intermediate, Far. Damage decreases with range.", ""));
        entries.add(makeEntry("sfb-006", "Photon Torpedoes", "SFB Weapon Rules", "Chapter 3", "Weapons",
                "Torpedoes require targeting lock. Roll 2D6 + Tactics to hit. Damage: 2D6 × torpedo class.", ""));
        entries.add(makeEntry("sfb-007", "Shield Allocation", "SFB Defense Rules", "Chapter 4", "Defense",
                "Shields can be allocated to forward, aft, port, starboard, and dorsal/ventral sectors.", ""));
        entries.add(makeEntry("sfb-008", "Warp Drive", "SFB Movement Rules", "Chapter 5", "Movement",
                "Warp factor determines speed. Warp 2 = 2x light speed, Warp 6 = 400x light speed.", ""));
        entries.add(makeEntry("sfb-009", "Impulse Drive", "SFB Movement Rules", "Chapter 5", "Movement",
                "Impulse drive used within star systems. Speed measured in kilometers per impulse.", ""));
        entries.add(makeEntry("sfb-010", "Character Creation", "SFB Character Rules", "Chapter 6", "Character",
                "Roll 2D6 for each attribute: St, Dx, En, In, Ed, So. Minimum 2, maximum 12.", ""));
        entries.add(makeEntry("sfb-011", "Career Selection", "SFB Character Rules", "Chapter 6", "Character",
                "Choose from Starfleet, Klingon Defense Force, Romulan Sentinel, or Independent careers.", ""));
        entries.add(makeEntry("sfb-012", "Skill Gains", "SFB Character Rules", "Chapter 6", "Character",
                "Each career term, roll on the career skill table for a new skill.", ""));
        entries.add(makeEntry("sfb-013", "Wealth System", "SFB Economy Rules", "Chapter 7", "Economy",
                "Wealth dice determine starting credits. Roll wealth dice × 1000 for credits.", ""));
        entries.add(makeEntry("sfb-014", "Trade Routes", "SFB Trade Rules", "Chapter 8", "Trade",
                "Trade routes connect star systems. Profit = (sell price - buy price) × quantity.", ""));
        entries.add(makeEntry("sfb-015", "Market Conditions", "SFB Trade Rules", "Chapter 8", "Trade",
                "Supply and demand affect prices. High demand increases buy price, low supply increases sell price.", ""));
        entries.add(makeEntry("sfb-016", "Diplomacy", "SFB Diplomacy Rules", "Chapter 9", "Diplomacy",
                "Diplomatic relations range from -100 (war) to +100 (allied). Modify with diplomacy checks.", ""));
        entries.add(makeEntry("sfb-017", "Exploration", "SFB Exploration Rules", "Chapter 10", "Exploration",
                "Scan sectors to discover systems. Roll on encounter tables for random events.", ""));
        entries.add(makeEntry("sfb-018", "World Generation", "SFB World Rules", "Chapter 10", "Exploration",
                "World profiles use 8-character format: size, atmosphere, hydro, temp, government, pop, prod, inf.", ""));
        entries.add(makeEntry("sfb-019", "Klingon Honor", "SFB Faction Rules", "Chapter 11", "Factions",
                "Klingons value honor above all. Combat is the highest form of honor.", ""));
        entries.add(makeEntry("sfb-020", "Romulan Secrecy", "SFB Faction Rules", "Chapter 11", "Factions",
                "Romulans use cloaking devices and deception. Direct confrontation is avoided.", ""));
        entries.add(makeEntry("sfb-021", "Federation Principles", "SFB Faction Rules", "Chapter 11", "Factions",
                "The Federation values exploration, diplomacy, and scientific discovery.", ""));
        entries.add(makeEntry("sfb-022", "Customs Checks", "SFB Trade Rules", "Chapter 8", "Trade",
                "Customs strictness affects contraband detection. Military stations have 90% detection chance.", ""));
        entries.add(makeEntry("sfb-023", "Station Services", "SFB Station Rules", "Chapter 12", "Stations",
                "Stations provide refueling, repairs, and maintenance. Costs vary by station type.", ""));
        entries.add(makeEntry("sfb-024", "Random Encounters", "SFB Encounter Rules", "Chapter 13", "Encounters",
                "Roll on space, urban, or wildlife encounter tables. Severity 1-10 determines danger level.", ""));
        entries.add(makeEntry("sfb-025", "Adventure Events", "SFB Event Rules", "Chapter 13", "Events",
                "Adventure events provide XP rewards. Severity determines difficulty and reward.", ""));
        entries.add(makeEntry("sfb-026", "Energy Allocation", "SFB Naval Rules", "Chapter 3", "Combat",
                "Each impulse, allocate energy points to weapons, shields, and movement systems.", ""));
        entries.add(makeEntry("sfb-027", "Damage Allocation", "SFB Naval Rules", "Chapter 3", "Combat",
                "Damage is allocated to specific ship systems. Critical hits cause system failures.", ""));
        entries.add(makeEntry("sfb-028", "Special Systems", "SFB Naval Rules", "Chapter 3", "Combat",
                "Special systems include cloaking devices, tractor beams, and shuttle bays.", ""));
        entries.add(makeEntry("sfb-029", "Campaign Management", "SFB Campaign Rules", "Chapter 14", "Campaign",
                "Campaigns track fleet deployment, shipbuilding, repairs, and zone of control.", ""));
        entries.add(makeEntry("sfb-030", "Shipbuilding", "SFB Campaign Rules", "Chapter 14", "Campaign",
                "Build ships at shipyards. Cost depends on ship class and available resources.", ""));
    }

    private RulesEntry makeEntry(String id, String title, String source, String chapter,
                                  String category, String text, String notes) {
        RulesEntry entry = new RulesEntry();
        entry.setGameSystem(GameSystem.SFB);
        entry.setTitle(title);
        entry.setSource(source);
        entry.setChapter(chapter);
        entry.setCategory(category);
        entry.setText(text);
        entry.setNotes(notes);
        entry.setActive(true);
        return entry;
    }

    /**
     * Gets all rules entries.
     */
    public List<RulesEntry> getAllEntries() {
        return entries;
    }

    /**
     * Gets entries by category.
     */
    public List<RulesEntry> getEntriesByCategory(String category) {
        List<RulesEntry> result = new ArrayList<>();
        for (RulesEntry entry : entries) {
            if (entry.getCategory().equalsIgnoreCase(category)) {
                result.add(entry);
            }
        }
        return result;
    }

    /**
     * Gets entries by source.
     */
    public List<RulesEntry> getEntriesBySource(String source) {
        List<RulesEntry> result = new ArrayList<>();
        for (RulesEntry entry : entries) {
            if (entry.getSource().equalsIgnoreCase(source)) {
                result.add(entry);
            }
        }
        return result;
    }

    /**
     * Searches rules by keyword.
     */
    public List<RulesEntry> search(String keyword) {
        List<RulesEntry> result = new ArrayList<>();
        for (RulesEntry entry : entries) {
            if (entry.getTitle().toLowerCase().contains(keyword.toLowerCase()) ||
                entry.getText().toLowerCase().contains(keyword.toLowerCase())) {
                result.add(entry);
            }
        }
        return result;
    }

    /**
     * Gets an entry by ID (uses title as ID for shared RulesEntry).
     */
    public RulesEntry getEntryById(String id) {
        for (RulesEntry entry : entries) {
            if (entry.getTitle().equals(id) || entry.getNotes() != null && entry.getNotes().contains(id)) {
                return entry;
            }
        }
        return null;
    }

    /**
     * Gets all unique categories.
     */
    public Set<String> getCategories() {
        Set<String> categories = new HashSet<>();
        for (RulesEntry entry : entries) {
            categories.add(entry.getCategory());
        }
        return categories;
    }

    /**
     * Gets all unique sources.
     */
    public Set<String> getSources() {
        Set<String> sources = new HashSet<>();
        for (RulesEntry entry : entries) {
            sources.add(entry.getSource());
        }
        return sources;
    }
}
