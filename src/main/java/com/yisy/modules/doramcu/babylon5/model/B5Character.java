package com.yisy.modules.doramcu.babylon5.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Represents a Babylon 5 character with full personality profile, relationships, and quotes.
 */
public class B5Character {

    private String id;
    private String name;
    private String species;
    private String actor;
    private String role;
    private String description;
    private String firstAppearance;
    private String lastAppearance;
    private B5Personality personality;
    private String archetype;
    private B5Characteristics characteristics;
    private List<String> keySkills;
    private String storyArc;
    private List<B5Relationship> relationships;
    private List<String> famousQuotes;

    public B5Character() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public String getActor() {
        return actor;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFirstAppearance() {
        return firstAppearance;
    }

    public void setFirstAppearance(String firstAppearance) {
        this.firstAppearance = firstAppearance;
    }

    public String getLastAppearance() {
        return lastAppearance;
    }

    public void setLastAppearance(String lastAppearance) {
        this.lastAppearance = lastAppearance;
    }

    public B5Personality getPersonality() {
        return personality;
    }

    public void setPersonality(B5Personality personality) {
        this.personality = personality;
    }

    public String getArchetype() {
        return archetype;
    }

    public void setArchetype(String archetype) {
        this.archetype = archetype;
    }

    public B5Characteristics getCharacteristics() {
        return characteristics;
    }

    public void setCharacteristics(B5Characteristics characteristics) {
        this.characteristics = characteristics;
    }

    public List<String> getKeySkills() {
        return keySkills;
    }

    public void setKeySkills(List<String> keySkills) {
        this.keySkills = keySkills;
    }

    public String getStoryArc() {
        return storyArc;
    }

    public void setStoryArc(String storyArc) {
        this.storyArc = storyArc;
    }

    public List<B5Relationship> getRelationships() {
        return relationships;
    }

    public void setRelationships(List<B5Relationship> relationships) {
        this.relationships = relationships;
    }

    public List<String> getFamousQuotes() {
        return famousQuotes;
    }

    public void setFamousQuotes(List<String> famousQuotes) {
        this.famousQuotes = famousQuotes;
    }

    @Override
    public String toString() {
        return "B5Character{id='" + id + "', name='" + name + "', species='" + species + "'}";
    }
}
