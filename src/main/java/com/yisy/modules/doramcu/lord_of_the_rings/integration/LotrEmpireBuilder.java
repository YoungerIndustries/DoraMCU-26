package com.yisy.modules.doramcu.lord_of_the_rings.integration;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds empire structures for the Lord of the Rings module.
 * Creates factions, territories, and military organizations based on Tolkien's lore.
 */
@Component
public class LotrEmpireBuilder {

    private static final String[] FACTIONS = {
            "Free Peoples Alliance", "Mordor", "Isengard", "Nazgûl",
            "Harad", "Rhûn", "Ents", "Blue Wizards"
    };

    private static final String[] LEADERS = {
            "Aragorn", "Sauron", "Saruman", "Witch-king",
            "Harmast", "Khamûl", "Treebeard", "Alatar"
    };

    private static final String[] TERRITORIES = {
            "Gondor", "Mordor", "Isengard", "Minas Morgul",
            "Harad", "Rhûn", "Fangorn", "Dorwinion"
    };

    /**
     * Generates a list of faction names.
     */
    public List<String> generateFactions(int count) {
        List<String> factions = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            factions.add(FACTIONS[i % FACTIONS.length]);
        }
        return factions;
    }

    /**
     * Generates a list of leader names.
     */
    public List<String> generateLeaders(int count) {
        List<String> leaders = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            leaders.add(LEADERS[i % LEADERS.length]);
        }
        return leaders;
    }

    /**
     * Generates a list of territory names.
     */
    public List<String> generateTerritories(int count) {
        List<String> territories = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            territories.add(TERRITORIES[i % TERRITORIES.length]);
        }
        return territories;
    }

    /**
     * Generates an empire description.
     */
    public String generateEmpireDescription(String factionName) {
        return factionName + " is a powerful faction in the Arda galaxy, " +
                "commanding forces across multiple star systems and realms.";
    }
}
