package com.yisy.modules.doramcu.service.game;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Cross-game character service for unified character identity across game universes.
 * <p>
 * Allows a character created in one game system (Traveller, Star Trek, Star Wars, etc.)
 * to be ported to another game system with attribute mapping, skill conversion,
 * and career history preservation.
 * </p>
 * <p>
 * Features:
 * <ul>
 *   <li>Unified character identity across all game universes</li>
 *   <li>Attribute mapping between different game systems</li>
 *   <li>Skill conversion with equivalences</li>
 *   <li>Career history preservation across universes</li>
 *   <li>Character statistics and port history</li>
 *   <li>Universe-specific character adapters</li>
 * </ul>
 * </p>
 */
@Service
public class CrossGameService {

    private static final Logger log = LoggerFactory.getLogger(CrossGameService.class);
    private static final int MAX_CHARACTERS = 1000;
    private static final int MAX_PORT_HISTORY = 50;

    /** Supported game universes. */
    public enum Universe {
        TRAVELLER("Traveller", "Classic Traveller RPG"),
        STAR_TREK("Star Trek", "Star Trek 4X and Captain's Log"),
        STAR_WARS("Star Wars", "Star Wars galaxy"),
        BABYLON5("Babylon 5", "Babylon 5 universe"),
        DND5E("D&D 5E", "Dungeons & Dragons 5th Edition"),
        SFB("Star Fleet Battles", "Star Fleet Battles tactical combat"),
        MECHWARRIOR("MechWarrior", "BattleTech MechWarrior"),
        THIRD_REICH("Third Reich", "WWII ground combat"),
        STARSHIP_TROOPERS("Starship Troopers", "Starship Troopers"),
        ZERO_ZERO_SEVEN("007", "James Bond spy games"),
        EARTH2026("Earth 2026", "Earth 2026 alternate history"),
        GENERIC("Generic", "Generic character without specific universe");

        private final String name;
        private final String description;

        Universe(String name, String description) {
            this.name = name;
            this.description = description;
        }

        public String getName() { return name; }
        public String getDescription() { return description; }
    }

    /** Universal ability scores that map across all game systems. */
    public enum Ability {
        STRENGTH("STR", "Physical power and endurance"),
        DEXTERITY("DEX", "Agility and reflexes"),
        CONSTITUTION("CON", "Health and stamina"),
        INTELLIGENCE("INT", "Knowledge and reasoning"),
        WISDOM("WIS", "Insight and perception"),
        CHARISMA("CHA", "Personality and leadership");

        private final String abbreviation;
        private final String description;

        Ability(String abbreviation, String description) {
            this.abbreviation = abbreviation;
            this.description = description;
        }

        public String getAbbreviation() { return abbreviation; }
        public String getDescription() { return description; }
    }

    /**
     * Port record tracking a character's movement between universes.
     */
    public static class PortRecord {
        private final Universe fromUniverse;
        private final Universe toUniverse;
        private final Instant portedAt;
        private final String notes;
        private final Map<Ability, Integer> fromScores;
        private final Map<Ability, Integer> toScores;

        private PortRecord(Builder builder) {
            this.fromUniverse = builder.fromUniverse;
            this.toUniverse = builder.toUniverse;
            this.portedAt = builder.portedAt;
            this.notes = builder.notes;
            this.fromScores = new HashMap<>(builder.fromScores);
            this.toScores = new HashMap<>(builder.toScores);
        }

        public Universe getFromUniverse() { return fromUniverse; }
        public Universe getToUniverse() { return toUniverse; }
        public Instant getPortedAt() { return portedAt; }
        public String getNotes() { return notes; }
        public Map<Ability, Integer> getFromScores() { return fromScores; }
        public Map<Ability, Integer> getToScores() { return toScores; }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private Universe fromUniverse;
            private Universe toUniverse;
            private Instant portedAt = Instant.now();
            private String notes = "";
            private final Map<Ability, Integer> fromScores = new EnumMap<>(Ability.class);
            private final Map<Ability, Integer> toScores = new EnumMap<>(Ability.class);

            public Builder fromUniverse(Universe fromUniverse) { this.fromUniverse = fromUniverse; return this; }
            public Builder toUniverse(Universe toUniverse) { this.toUniverse = toUniverse; return this; }
            public Builder portedAt(Instant portedAt) { this.portedAt = portedAt; return this; }
            public Builder notes(String notes) { this.notes = notes; return this; }
            public Builder fromScore(Ability ability, int score) { this.fromScores.put(ability, score); return this; }
            public Builder toScore(Ability ability, int score) { this.toScores.put(ability, score); return this; }
            public PortRecord build() { return new PortRecord(this); }
        }
    }

    /**
     * Cross-game character with unified identity.
     */
    public static class CrossGameCharacter {
        private final String id;
        private final String name;
        private final Universe originalUniverse;
        private final String originalCharacterId;
        private final Map<Ability, Integer> universalScores;
        private final Map<Universe, Map<String, Object>> universeSpecificData;
        private final List<String> careerHistory;
        private final Instant createdAt;
        private final long creationOrder;
        private Universe currentUniverse;
        private Instant lastPortedAt;

        private CrossGameCharacter(Builder builder) {
            this.id = builder.id;
            this.name = builder.name;
            this.originalUniverse = builder.originalUniverse;
            this.originalCharacterId = builder.originalCharacterId;
            this.universalScores = new EnumMap<>(builder.universalScores);
            this.universeSpecificData = new HashMap<>(builder.universeSpecificData);
            this.careerHistory = new ArrayList<>(builder.careerHistory);
            this.createdAt = builder.createdAt;
            this.creationOrder = builder.creationOrder;
            this.currentUniverse = builder.currentUniverse;
            this.lastPortedAt = builder.lastPortedAt;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public Universe getOriginalUniverse() { return originalUniverse; }
        public String getOriginalCharacterId() { return originalCharacterId; }
        public Map<Ability, Integer> getUniversalScores() { return universalScores; }
        public Map<Universe, Map<String, Object>> getUniverseSpecificData() { return universeSpecificData; }
        public List<String> getCareerHistory() { return careerHistory; }
        public Instant getCreatedAt() { return createdAt; }
        public long getCreationOrder() { return creationOrder; }
        public Universe getCurrentUniverse() { return currentUniverse; }
        public Instant getLastPortedAt() { return lastPortedAt; }
        public int getScore(Ability ability) { return universalScores.getOrDefault(ability, 0); }

        public void setCurrentUniverse(Universe universe) {
            this.currentUniverse = universe;
            this.lastPortedAt = Instant.now();
        }

        public static Builder builder(String id, String name) {
            return new Builder(id, name);
        }

        public static class Builder {
            private final String id;
            private final String name;
            private Universe originalUniverse = Universe.GENERIC;
            private String originalCharacterId = "";
            private final Map<Ability, Integer> universalScores = new EnumMap<>(Ability.class);
            private final Map<Universe, Map<String, Object>> universeSpecificData = new HashMap<>();
            private final List<String> careerHistory = new ArrayList<>();
            private Instant createdAt = Instant.now();
            private long creationOrder = 0;
            private Universe currentUniverse = Universe.GENERIC;
            private Instant lastPortedAt;

            public Builder(String id, String name) {
                this.id = id;
                this.name = name;
                for (Ability ability : Ability.values()) {
                    universalScores.put(ability, 0);
                }
            }

            public Builder originalUniverse(Universe originalUniverse) { this.originalUniverse = originalUniverse; return this; }
            public Builder originalCharacterId(String originalCharacterId) { this.originalCharacterId = originalCharacterId; return this; }
            public Builder score(Ability ability, int score) { this.universalScores.put(ability, score); return this; }
            public Builder universeData(Universe universe, Map<String, Object> data) { this.universeSpecificData.put(universe, new HashMap<>(data)); return this; }
            public Builder career(String career) { this.careerHistory.add(career); return this; }
            public Builder careers(List<String> careers) { this.careerHistory.addAll(careers); return this; }
            public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
            public Builder creationOrder(long creationOrder) { this.creationOrder = creationOrder; return this; }
            public Builder currentUniverse(Universe currentUniverse) { this.currentUniverse = currentUniverse; return this; }
            public Builder lastPortedAt(Instant lastPortedAt) { this.lastPortedAt = lastPortedAt; return this; }
            public CrossGameCharacter build() { return new CrossGameCharacter(this); }
        }
    }

    /**
     * Character summary for API responses.
     */
    public static class CharacterSummary {
        private final String id;
        private final String name;
        private final Universe originalUniverse;
        private final Universe currentUniverse;
        private final Map<Ability, Integer> scores;
        private final int careerCount;
        private final int portCount;
        private final Instant createdAt;

        public CharacterSummary(CrossGameCharacter character, int portCount) {
            this.id = character.getId();
            this.name = character.getName();
            this.originalUniverse = character.getOriginalUniverse();
            this.currentUniverse = character.getCurrentUniverse();
            this.scores = character.getUniversalScores();
            this.careerCount = character.getCareerHistory().size();
            this.portCount = portCount;
            this.createdAt = character.getCreatedAt();
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public Universe getOriginalUniverse() { return originalUniverse; }
        public Universe getCurrentUniverse() { return currentUniverse; }
        public Map<Ability, Integer> getScores() { return scores; }
        public int getCareerCount() { return careerCount; }
        public int getPortCount() { return portCount; }
        public Instant getCreatedAt() { return createdAt; }
    }

    /** Character ID â†’ CrossGameCharacter mapping. */
    private final ConcurrentHashMap<String, CrossGameCharacter> characters = new ConcurrentHashMap<>();

    /** Character ID â†’ port history. */
    private final ConcurrentHashMap<String, List<PortRecord>> portHistory = new ConcurrentHashMap<>();

    /** Counter for character creation order. */
    private final AtomicLong creationOrder = new AtomicLong(0);

    /**
     * Create a new cross-game character.
     */
    public Optional<CrossGameCharacter> createCharacter(String id, String name, Universe universe,
                                                         Map<Ability, Integer> scores) {
        if (characters.size() >= MAX_CHARACTERS) {
            log.warn("Maximum number of cross-game characters ({}) reached", MAX_CHARACTERS);
            return Optional.empty();
        }
        if (characters.containsKey(id)) {
            log.warn("Cross-game character already exists: {}", id);
            return Optional.of(characters.get(id));
        }
        CrossGameCharacter character = CrossGameCharacter.builder(id, name)
                .originalUniverse(universe)
                .currentUniverse(universe)
                .creationOrder(creationOrder.incrementAndGet())
                .build();
        if (scores != null) {
            for (Map.Entry<Ability, Integer> entry : scores.entrySet()) {
                character.getUniversalScores().put(entry.getKey(), entry.getValue());
            }
        }
        characters.put(id, character);
        portHistory.putIfAbsent(id, Collections.synchronizedList(new ArrayList<>()));
        log.info("Cross-game character created: {} ({}) in {}", name, id, universe);
        return Optional.of(character);
    }

    /**
     * Create a cross-game character from full builder.
     */
    public Optional<CrossGameCharacter> createCharacter(CrossGameCharacter character) {
        if (characters.size() >= MAX_CHARACTERS) {
            log.warn("Maximum number of cross-game characters ({}) reached", MAX_CHARACTERS);
            return Optional.empty();
        }
        if (characters.containsKey(character.getId())) {
            log.warn("Cross-game character already exists: {}", character.getId());
            return Optional.of(characters.get(character.getId()));
        }
        characters.put(character.getId(), character);
        portHistory.putIfAbsent(character.getId(), Collections.synchronizedList(new ArrayList<>()));
        log.info("Cross-game character created: {} ({})", character.getName(), character.getId());
        return Optional.of(character);
    }

    /**
     * Get a character by ID.
     */
    public Optional<CrossGameCharacter> getCharacter(String characterId) {
        return Optional.ofNullable(characters.get(characterId));
    }

    /**
     * List all characters.
     */
    public List<CharacterSummary> getAllCharacters() {
        return characters.values().stream()
                .sorted(Comparator.comparingLong(CrossGameCharacter::getCreationOrder))
                .map(c -> new CharacterSummary(c, getPortHistory(c.getId()).size()))
                .collect(Collectors.toList());
    }

    /**
     * Get characters by current universe.
     */
    public List<CharacterSummary> getCharactersByUniverse(Universe universe) {
        return characters.values().stream()
                .filter(c -> c.getCurrentUniverse() == universe)
                .sorted(Comparator.comparingLong(CrossGameCharacter::getCreationOrder))
                .map(c -> new CharacterSummary(c, getPortHistory(c.getId()).size()))
                .collect(Collectors.toList());
    }

    /**
     * Get characters by original universe.
     */
    public List<CharacterSummary> getCharactersByOriginalUniverse(Universe universe) {
        return characters.values().stream()
                .filter(c -> c.getOriginalUniverse() == universe)
                .sorted(Comparator.comparingLong(CrossGameCharacter::getCreationOrder))
                .map(c -> new CharacterSummary(c, getPortHistory(c.getId()).size()))
                .collect(Collectors.toList());
    }

    /**
     * Search characters by name (case-insensitive).
     */
    public List<CharacterSummary> searchCharacters(String query) {
        String lowerQuery = query.toLowerCase();
        return characters.values().stream()
                .filter(c -> c.getName().toLowerCase().contains(lowerQuery))
                .sorted(Comparator.comparingLong(CrossGameCharacter::getCreationOrder))
                .map(c -> new CharacterSummary(c, getPortHistory(c.getId()).size()))
                .collect(Collectors.toList());
    }

    /**
     * Port a character to a different universe.
     * Maps universal scores to the target universe's scale.
     */
    public Optional<PortRecord> portCharacter(String characterId, Universe targetUniverse, String notes) {
        CrossGameCharacter character = characters.get(characterId);
        if (character == null) {
            log.warn("Character not found for porting: {}", characterId);
            return Optional.empty();
        }
        Universe fromUniverse = character.getCurrentUniverse();
        if (fromUniverse == targetUniverse) {
            log.warn("Character already in universe {}: {}", targetUniverse, characterId);
            return Optional.empty();
        }
        Map<Ability, Integer> mappedScores = mapScoresToUniverse(character.getUniversalScores(), targetUniverse);
        PortRecord record = PortRecord.builder()
                .fromUniverse(fromUniverse)
                .toUniverse(targetUniverse)
                .notes(notes != null ? notes : "")
                .build();
        for (Map.Entry<Ability, Integer> entry : character.getUniversalScores().entrySet()) {
            record.getFromScores().put(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<Ability, Integer> entry : mappedScores.entrySet()) {
            record.getToScores().put(entry.getKey(), entry.getValue());
        }
        List<PortRecord> history = portHistory.computeIfAbsent(characterId, k -> Collections.synchronizedList(new ArrayList<>()));
        if (history.size() >= MAX_PORT_HISTORY) {
            history.subList(0, history.size() - MAX_PORT_HISTORY + 1).clear();
        }
        history.add(record);
        character.setCurrentUniverse(targetUniverse);
        for (Map.Entry<Ability, Integer> entry : mappedScores.entrySet()) {
            character.getUniversalScores().put(entry.getKey(), entry.getValue());
        }
        log.info("Character {} ported from {} to {}", characterId, fromUniverse, targetUniverse);
        return Optional.of(record);
    }

    /**
     * Get port history for a character.
     */
    public List<PortRecord> getPortHistory(String characterId) {
        List<PortRecord> records = portHistory.get(characterId);
        if (records == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(records);
    }

    /**
     * Add career entry to a character's history.
     */
    public boolean addCareer(String characterId, String career) {
        CrossGameCharacter character = characters.get(characterId);
        if (character == null) {
            return false;
        }
        character.getCareerHistory().add(career);
        log.info("Career added to {}: {}", characterId, career);
        return true;
    }

    /**
     * Update a character's universal scores.
     */
    public boolean updateScores(String characterId, Map<Ability, Integer> scores) {
        CrossGameCharacter character = characters.get(characterId);
        if (character == null) {
            return false;
        }
        for (Map.Entry<Ability, Integer> entry : scores.entrySet()) {
            character.getUniversalScores().put(entry.getKey(), entry.getValue());
        }
        return true;
    }

    /**
     * Set universe-specific data for a character.
     */
    public boolean setUniverseData(String characterId, Universe universe, Map<String, Object> data) {
        CrossGameCharacter character = characters.get(characterId);
        if (character == null) {
            return false;
        }
        character.getUniverseSpecificData().put(universe, new HashMap<>(data));
        return true;
    }

    /**
     * Remove a character.
     */
    public boolean removeCharacter(String characterId) {
        CrossGameCharacter removed = characters.remove(characterId);
        portHistory.remove(characterId);
        if (removed != null) {
            log.info("Cross-game character removed: {}", characterId);
            return true;
        }
        return false;
    }

    /**
     * Get statistics about cross-game characters.
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCharacters", characters.size());
        Map<Universe, Long> byCurrentUniverse = characters.values().stream()
                .collect(Collectors.groupingBy(CrossGameCharacter::getCurrentUniverse, Collectors.counting()));
        Map<Universe, Long> byOriginalUniverse = characters.values().stream()
                .collect(Collectors.groupingBy(CrossGameCharacter::getOriginalUniverse, Collectors.counting()));
        stats.put("byCurrentUniverse", byCurrentUniverse);
        stats.put("byOriginalUniverse", byOriginalUniverse);
        long totalPorts = portHistory.values().stream().mapToInt(List::size).sum();
        stats.put("totalPorts", totalPorts);
        stats.put("charactersWithPorts", portHistory.values().stream().filter(l -> !l.isEmpty()).count());
        return stats;
    }

    /**
     * Get all supported universes.
     */
    public List<Universe> getSupportedUniverses() {
        return Arrays.asList(Universe.values());
    }

    /**
     * Get all ability types.
     */
    public List<Ability> getAbilities() {
        return Arrays.asList(Ability.values());
    }

    /**
     * Map universal scores to a specific universe's scale.
     * Different universes use different score ranges.
     */
    public Map<Ability, Integer> mapScoresToUniverse(Map<Ability, Integer> universalScores, Universe target) {
        Map<Ability, Integer> mapped = new EnumMap<>(Ability.class);
        for (Ability ability : Ability.values()) {
            int universal = universalScores.getOrDefault(ability, 0);
            int mappedScore;
            switch (target) {
                case TRAVELLER:
                    // Traveller uses 2-12 (3d6)
                    mappedScore = Math.max(2, Math.min(12, (universal * 10 / 20) + 2));
                    break;
                case DND5E:
                    // D&D 5E uses 1-20 (4d6 drop lowest)
                    mappedScore = Math.max(1, Math.min(20, universal));
                    break;
                case STAR_TREK:
                    // Star Trek Captain's Log uses 0-5
                    mappedScore = Math.max(0, Math.min(5, universal / 4));
                    break;
                case STAR_WARS:
                    // Star Wars uses 0-10
                    mappedScore = Math.max(0, Math.min(10, (universal * 10 / 20)));
                    break;
                case MECHWARRIOR:
                    // MechWarrior uses 0-10
                    mappedScore = Math.max(0, Math.min(10, (universal * 10 / 20)));
                    break;
                default:
                    // Generic: keep universal score as-is
                    mappedScore = universal;
                    break;
            }
            mapped.put(ability, mappedScore);
        }
        return mapped;
    }

    /**
     * Clear all characters and port history.
     */
    public void clearAll() {
        int count = characters.size();
        characters.clear();
        portHistory.clear();
        log.info("Cleared {} cross-game characters", count);
    }
}
