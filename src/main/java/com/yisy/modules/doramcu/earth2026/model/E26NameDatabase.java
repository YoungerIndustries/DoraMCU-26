package com.yisy.modules.doramcu.earth2026.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Name database for Earth 2026 module.
 * Supports both Jackson deserialization from JSON and default fallback data.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class E26NameDatabase {

    private static final String[] DEFAULT_MALE_FIRST_NAMES = {
        "John", "Alex", "David", "Michael", "James", "Robert", "William", "Richard",
        "Thomas", "Charles", "Daniel", "Matthew", "Anthony", "Mark", "Donald"
    };

    private static final String[] DEFAULT_FEMALE_FIRST_NAMES = {
        "Jane", "Maria", "Sarah", "Emma", "Olivia", "Sophia", "Isabella", "Emily",
        "Elizabeth", "Jennifer", "Linda", "Patricia", "Barbara", "Susan", "Jessica"
    };

    private static final String[] DEFAULT_LAST_NAMES = {
        "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller",
        "Davis", "Rodriguez", "Martinez", "Hernandez", "Lopez", "Gonzalez"
    };

    private static final String[] DEFAULT_COMPANION_NAMES = {
        "Spot", "Rex", "Buddy", "Max", "Bella", "Lucy", "Charlie", "Daisy",
        "Cooper", "Molly", "Bear", "Tucker", "Duke", "Rocky", "Shadow"
    };

    private final Random random = new Random();

    // Jackson-deserialized fields (from JSON)
    private List<String> maleFirstNames;
    private List<String> femaleFirstNames;
    private List<String> lastNames;
    private List<String> companionNames;
    private List<String> titlePrefixes;
    private List<String> titleSuffixes;
    private List<String> aiLeaderTitles;
    private List<String> aiGovernorTitles;
    private List<String> aiCaptainTitles;

    public E26NameDatabase() {
    }

    public String generateFirstName() {
        List<String> male = getMaleFirstNames();
        List<String> female = getFemaleFirstNames();
        List<String> all = new ArrayList<>();
        all.addAll(male);
        all.addAll(female);
        return all.get(random.nextInt(all.size()));
    }

    public String generateLastName() {
        List<String> names = getLastNames();
        return names.get(random.nextInt(names.size()));
    }

    public String generateFullName() {
        return generateFirstName() + " " + generateLastName();
    }

    public List<String> getMaleFirstNames() {
        return maleFirstNames != null ? maleFirstNames : List.of(DEFAULT_MALE_FIRST_NAMES);
    }

    public void setMaleFirstNames(List<String> maleFirstNames) {
        this.maleFirstNames = maleFirstNames;
    }

    public List<String> getFemaleFirstNames() {
        return femaleFirstNames != null ? femaleFirstNames : List.of(DEFAULT_FEMALE_FIRST_NAMES);
    }

    public void setFemaleFirstNames(List<String> femaleFirstNames) {
        this.femaleFirstNames = femaleFirstNames;
    }

    public List<String> getLastNames() {
        return lastNames != null ? lastNames : List.of(DEFAULT_LAST_NAMES);
    }

    public void setLastNames(List<String> lastNames) {
        this.lastNames = lastNames;
    }

    public List<String> getCompanionNames() {
        return companionNames != null ? companionNames : List.of(DEFAULT_COMPANION_NAMES);
    }

    public void setCompanionNames(List<String> companionNames) {
        this.companionNames = companionNames;
    }

    public List<String> getAiLeaderTitles() {
        return aiLeaderTitles != null ? aiLeaderTitles : List.of("Director", "Chief", "Commander", "Overseer", "Administrator");
    }

    public void setAiLeaderTitles(List<String> aiLeaderTitles) {
        this.aiLeaderTitles = aiLeaderTitles;
    }

    public List<String> getAiGovernorTitles() {
        return aiGovernorTitles != null ? aiGovernorTitles : List.of("Governor", "Prefect", "Magistrate", "Warden", "Steward");
    }

    public void setAiGovernorTitles(List<String> aiGovernorTitles) {
        this.aiGovernorTitles = aiGovernorTitles;
    }

    public List<String> getAiCaptainTitles() {
        return aiCaptainTitles != null ? aiCaptainTitles : List.of("Captain", "Commander", "Skipper", "Pilot", "Navigator");
    }

    public void setAiCaptainTitles(List<String> aiCaptainTitles) {
        this.aiCaptainTitles = aiCaptainTitles;
    }

    public List<String> getTitlePrefixes() {
        return titlePrefixes != null ? titlePrefixes : List.of("Dr.", "Prof.", "Sir", "Madam", "Hon.");
    }

    public void setTitlePrefixes(List<String> titlePrefixes) {
        this.titlePrefixes = titlePrefixes;
    }

    public List<String> getTitleSuffixes() {
        return titleSuffixes != null ? titleSuffixes : List.of("Jr.", "Sr.", "III", "IV", "Esq.");
    }

    public void setTitleSuffixes(List<String> titleSuffixes) {
        this.titleSuffixes = titleSuffixes;
    }
}
