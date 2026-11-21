package com.yisy.modules.doramcu.lord_of_the_rings.model;

/**
 * Represents a faction leader in the Lord of the Rings universe.
 */
public class FactionLeader {
    private String name;
    private String faction;
    private String title;
    private String role;

    public FactionLeader() {}

    public FactionLeader(String name, String faction) {
        this.name = name;
        this.faction = faction;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String name() { return name; }
    public String getFaction() { return faction; }
    public void setFaction(String faction) { this.faction = faction; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String role() { return role != null ? role : title; }
    public void setRole(String role) { this.role = role; }
}
