package com.yisy.modules.doramcu.shipdesign;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a Captain's Log character sheet for Star Trek Adventures.
 */
public class CharSheet {

    /**
     * The six core attributes of a character.
     */
    public enum Attribute {
        CONTROL, DARING, FITNESS, INSIGHT, PRESENCE, REASON
    }

    /**
     * The six disciplines of a character.
     */
    public enum Discipline {
        COMMAND, CONN, SECURITY, ENGINEERING, SCIENCE, MEDICINE
    }

    // Identity
    private String name;
    private String pronouns;
    private String environment;
    private String earlyOutlook;
    private String education;
    private String rank;
    private String assignment;
    private String careerLength;

    // Traits, Values, Equipment
    private List<String> traits;
    private List<String> values;
    private List<String> equipment;

    // Attributes (1-5 rating per attribute)
    private int[] attributes; // indexed by Attribute.ordinal()

    // Disciplines (1-5 rating per discipline)
    private int[] disciplines; // indexed by Discipline.ordinal()

    // Focuses per discipline
    private List<String>[] focuses; // indexed by Discipline.ordinal()

    // Career events
    private List<String> careerEvents;

    /**
     * Creates a new empty character sheet.
     */
    public CharSheet() {
        this.traits = new ArrayList<>();
        this.values = new ArrayList<>();
        this.equipment = new ArrayList<>();
        this.attributes = new int[Attribute.values().length];
        this.disciplines = new int[Discipline.values().length];
        this.focuses = new ArrayList[Discipline.values().length];
        for (int i = 0; i < focuses.length; i++) {
            focuses[i] = new ArrayList<>();
        }
        this.careerEvents = new ArrayList<>();
    }

    // --- Identity Getters and Setters ---

    /**
     * Returns the character's name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the character's name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the character's pronouns.
     */
    public String getPronouns() {
        return pronouns;
    }

    /**
     * Sets the character's pronouns.
     */
    public void setPronouns(String pronouns) {
        this.pronouns = pronouns;
    }

    /**
     * Returns the character's environment (background).
     */
    public String getEnvironment() {
        return environment;
    }

    /**
     * Sets the character's environment (background).
     */
    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    /**
     * Returns the character's early outlook.
     */
    public String getEarlyOutlook() {
        return earlyOutlook;
    }

    /**
     * Sets the character's early outlook.
     */
    public void setEarlyOutlook(String earlyOutlook) {
        this.earlyOutlook = earlyOutlook;
    }

    /**
     * Returns the character's education.
     */
    public String getEducation() {
        return education;
    }

    /**
     * Sets the character's education.
     */
    public void setEducation(String education) {
        this.education = education;
    }

    /**
     * Returns the character's rank.
     */
    public String getRank() {
        return rank;
    }

    /**
     * Sets the character's rank.
     */
    public void setRank(String rank) {
        this.rank = rank;
    }

    /**
     * Returns the character's assignment.
     */
    public String getAssignment() {
        return assignment;
    }

    /**
     * Sets the character's assignment.
     */
    public void setAssignment(String assignment) {
        this.assignment = assignment;
    }

    /**
     * Returns the character's career length.
     */
    public String getCareerLength() {
        return careerLength;
    }

    /**
     * Sets the character's career length.
     */
    public void setCareerLength(String careerLength) {
        this.careerLength = careerLength;
    }

    // --- Traits ---

    /**
     * Returns the list of character traits.
     */
    public List<String> getTraits() {
        return traits;
    }

    /**
     * Adds a trait to the character.
     */
    public void addTrait(String trait) {
        traits.add(trait);
    }

    // --- Values ---

    /**
     * Returns the list of character values.
     */
    public List<String> getValues() {
        return values;
    }

    /**
     * Adds a value to the character.
     */
    public void addValue(String value) {
        values.add(value);
    }

    // --- Equipment ---

    /**
     * Returns the list of equipment or other details.
     */
    public List<String> getEquipment() {
        return equipment;
    }

    /**
     * Adds an equipment item to the character.
     */
    public void addEquipment(String item) {
        equipment.add(item);
    }

    // --- Attributes ---

    /**
     * Returns the rating for the given attribute (1-5).
     */
    public int getAttribute(Attribute attribute) {
        return attributes[attribute.ordinal()];
    }

    /**
     * Sets the rating for the given attribute (1-5).
     */
    public void setAttribute(Attribute attribute, int rating) {
        if (rating < 0 || rating > 5) {
            throw new IllegalArgumentException("Attribute rating must be between 0 and 5");
        }
        attributes[attribute.ordinal()] = rating;
    }

    // --- Disciplines ---

    /**
     * Returns the rating for the given discipline (1-5).
     */
    public int getDiscipline(Discipline discipline) {
        return disciplines[discipline.ordinal()];
    }

    /**
     * Sets the rating for the given discipline (1-5).
     */
    public void setDiscipline(Discipline discipline, int rating) {
        if (rating < 0 || rating > 5) {
            throw new IllegalArgumentException("Discipline rating must be between 0 and 5");
        }
        disciplines[discipline.ordinal()] = rating;
    }

    // --- Focuses ---

    /**
     * Returns the list of focuses for the given discipline.
     */
    public List<String> getFocuses(Discipline discipline) {
        return focuses[discipline.ordinal()];
    }

    /**
     * Adds a focus to the given discipline.
     */
    public void addFocus(Discipline discipline, String focus) {
        focuses[discipline.ordinal()].add(focus);
    }

    // --- Career Events ---

    /**
     * Returns the list of career events.
     */
    public List<String> getCareerEvents() {
        return careerEvents;
    }

    /**
     * Adds a career event to the character.
     */
    public void addCareerEvent(String event) {
        careerEvents.add(event);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("CharSheet{\n");
        sb.append("  name=").append(name).append("\n");
        sb.append("  pronouns=").append(pronouns).append("\n");
        sb.append("  environment=").append(environment).append("\n");
        sb.append("  earlyOutlook=").append(earlyOutlook).append("\n");
        sb.append("  education=").append(education).append("\n");
        sb.append("  rank=").append(rank).append("\n");
        sb.append("  assignment=").append(assignment).append("\n");
        sb.append("  careerLength=").append(careerLength).append("\n");
        sb.append("  traits=").append(traits).append("\n");
        sb.append("  values=").append(values).append("\n");
        sb.append("  equipment=").append(equipment).append("\n");
        sb.append("  careerEvents=").append(careerEvents).append("\n");

        // Attributes
        sb.append("  attributes: ");
        for (Attribute attr : Attribute.values()) {
            sb.append(attr).append("=").append(getAttribute(attr)).append(" ");
        }
        sb.append("\n");

        // Disciplines with focuses
        sb.append("  disciplines: ");
        for (Discipline disc : Discipline.values()) {
            sb.append(disc).append("=").append(getDiscipline(disc));
            if (!getFocuses(disc).isEmpty()) {
                sb.append("(").append(getFocuses(disc)).append(")");
            }
            sb.append(" ");
        }
        sb.append("\n");

        sb.append("}");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CharSheet charSheet = (CharSheet) o;
        return Objects.equals(name, charSheet.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
