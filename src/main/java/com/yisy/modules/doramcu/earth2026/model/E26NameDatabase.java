package com.yisy.modules.doramcu.earth2026.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Name database for a species, used to generate AI character names
 * that match the species' lore and naming conventions.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class E26NameDatabase {

    private List<String> maleFirstNames;
    private List<String> femaleFirstNames;
    private List<String> lastNames;
    private List<String> titlePrefixes;
    private List<String> titleSuffixes;
    private List<String> aiLeaderTitles;
    private List<String> aiGovernorTitles;
    private List<String> aiCaptainTitles;

    public E26NameDatabase() {
    }

    public List<String> getMaleFirstNames() { return maleFirstNames; }
    public void setMaleFirstNames(List<String> maleFirstNames) { this.maleFirstNames = maleFirstNames; }

    public List<String> getFemaleFirstNames() { return femaleFirstNames; }
    public void setFemaleFirstNames(List<String> femaleFirstNames) { this.femaleFirstNames = femaleFirstNames; }

    public List<String> getLastNames() { return lastNames; }
    public void setLastNames(List<String> lastNames) { this.lastNames = lastNames; }

    public List<String> getTitlePrefixes() { return titlePrefixes; }
    public void setTitlePrefixes(List<String> titlePrefixes) { this.titlePrefixes = titlePrefixes; }

    public List<String> getTitleSuffixes() { return titleSuffixes; }
    public void setTitleSuffixes(List<String> titleSuffixes) { this.titleSuffixes = titleSuffixes; }

    public List<String> getAiLeaderTitles() { return aiLeaderTitles; }
    public void setAiLeaderTitles(List<String> aiLeaderTitles) { this.aiLeaderTitles = aiLeaderTitles; }

    public List<String> getAiGovernorTitles() { return aiGovernorTitles; }
    public void setAiGovernorTitles(List<String> aiGovernorTitles) { this.aiGovernorTitles = aiGovernorTitles; }

    public List<String> getAiCaptainTitles() { return aiCaptainTitles; }
    public void setAiCaptainTitles(List<String> aiCaptainTitles) { this.aiCaptainTitles = aiCaptainTitles; }
}
