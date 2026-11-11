package com.yisy.modules.doramcu.hex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Database of galaxy names for 4X space games.
 * <p>
 * Loads names from {@code /data/galaxy-names.json} on the classpath.
 * Falls back to procedural catalog-style designations when named galaxies are exhausted.
 */
public class GalaxyNameDatabase {

    private static final String RESOURCE_PATH = "/data/galaxy-names.json";

    private List<String> names;
    private final Map<String, List<String>> namesByTheme;
    private final ConcurrentHashMap<String, Boolean> usedNames;
    private final Random random;
    private String catalogPrefix;
    private int proceduralId;
    private String activeTheme;

    /**
     * Creates a new database with default catalog prefix "NGC".
     */
    public GalaxyNameDatabase() {
        this("NGC");
    }

    /**
     * Creates a new database with custom catalog prefix.
     *
     * @param catalogPrefix the prefix for procedural names (e.g., "NGC", "IC", "GAL")
     */
    public GalaxyNameDatabase(String catalogPrefix) {
        this(catalogPrefix, null);
    }

    /**
     * Creates a new database with custom catalog prefix and active theme.
     *
     * @param catalogPrefix the prefix for procedural names
     * @param theme the theme to filter names by (e.g., "astronomical", "mythological", "star")
     */
    public GalaxyNameDatabase(String catalogPrefix, String theme) {
        this.random = new Random();
        this.catalogPrefix = catalogPrefix != null ? catalogPrefix : "NGC";
        this.usedNames = new ConcurrentHashMap<>();
        this.proceduralId = 1;

        this.namesByTheme = loadThemes();
        // Only set activeTheme if it's a recognized theme; otherwise fall back to null (all names)
        if (theme != null && !theme.isEmpty() && namesByTheme.containsKey(theme)) {
            this.activeTheme = theme;
        } else {
            this.activeTheme = null;
        }
        this.names = loadNamesForTheme(theme);
    }

    private List<String> loadNamesForTheme(String theme) {
        if (theme != null && !theme.isEmpty() && namesByTheme.containsKey(theme)) {
            return new ArrayList<>(namesByTheme.get(theme));
        }
        // Fallback: load all names (backward compatibility)
        List<String> loaded = NameDatabaseLoader.loadFlat(RESOURCE_PATH);
        Set<String> seen = new LinkedHashSet<>();
        List<String> unique = new ArrayList<>();
        for (String name : loaded) {
            if (seen.add(name)) {
                unique.add(name);
            }
        }
        return unique;
    }

    private Map<String, List<String>> loadThemes() {
        Map<String, List<String>> themes = NameDatabaseLoader.loadThemes(RESOURCE_PATH);
        // Wrap lists in unmodifiable copies
        Map<String, List<String>> immutable = new LinkedHashMap<>();
        for (Map.Entry<String, List<String>> entry : themes.entrySet()) {
            immutable.put(entry.getKey(), Collections.unmodifiableList(new ArrayList<>(entry.getValue())));
        }
        return Collections.unmodifiableMap(immutable);
    }

    // ---- Core Methods ----

    /**
     * Returns the next sequential galaxy name.
     * <p>
     * Once all named galaxies are exhausted, procedurally generated names are returned.
     *
     * @return the next available galaxy name
     */
    public String getNextName() {
        for (String name : names) {
            if (usedNames.putIfAbsent(name, Boolean.TRUE) == null) {
                return name;
            }
        }
        return getProceduralName();
    }

    /**
     * Returns a random unused galaxy name.
     * <p>
     * Once all named galaxies are exhausted, procedurally generated names are returned.
     *
     * @return a random available galaxy name
     */
    public String getRandomName() {
        List<String> available = new ArrayList<>();
        for (String name : names) {
            if (!usedNames.containsKey(name)) {
                available.add(name);
            }
        }
        if (available.isEmpty()) {
            return getProceduralName();
        }
        String selected = available.get(random.nextInt(available.size()));
        usedNames.put(selected, Boolean.TRUE);
        return selected;
    }

    /**
     * Returns a procedurally generated catalog-style name.
     * Format: "{prefix}-{number}" (e.g., "NGC-00001").
     *
     * @return a procedural galaxy name
     */
    public String getProceduralName() {
        String name = catalogPrefix + "-" + String.format("%05d", proceduralId++);
        return name;
    }

    /**
     * Returns all galaxy names.
     *
     * @return unmodifiable list of all names
     */
    public List<String> getAllNames() {
        return Collections.unmodifiableList(names);
    }

    /**
     * Returns the total number of named galaxies.
     *
     * @return the name count
     */
    public int getNameCount() {
        return names.size();
    }

    /**
     * Returns the number of names used so far.
     *
     * @return the number of names used
     */
    public int getUsedCount() {
        return usedNames.size();
    }

    /**
     * Checks if all named galaxies have been used.
     *
     * @return true if all names are exhausted
     */
    public boolean isExhausted() {
        return usedNames.size() >= names.size();
    }

    /**
     * Resets the used names, allowing names to be reused.
     */
    public void reset() {
        usedNames.clear();
        proceduralId = 1;
    }

    /**
     * Returns the catalog prefix used for procedural names.
     *
     * @return the catalog prefix
     */
    public String getProceduralPrefix() {
        return catalogPrefix;
    }

    /**
     * Sets the catalog prefix for procedural names.
     *
     * @param prefix the new prefix
     */
    public void setProceduralPrefix(String prefix) {
        if (prefix != null && !prefix.isEmpty()) {
            this.catalogPrefix = prefix;
        }
    }

    /**
     * Assigns the next sequential name to the given galaxy.
     *
     * @param galaxy the galaxy to name
     */
    public void assignName(Galaxy galaxy) {
        if (galaxy != null) {
            // Galaxy doesn't have a name field yet, but we provide this for future use
        }
    }

    /**
     * Assigns a random name to the given galaxy.
     *
     * @param galaxy the galaxy to name
     */
    public void assignRandomName(Galaxy galaxy) {
        if (galaxy != null) {
            // Galaxy doesn't have a name field yet, but we provide this for future use
        }
    }

    /**
     * Returns the available theme names.
     *
     * @return unmodifiable set of theme names
     */
    public Set<String> getAvailableThemes() {
        return Collections.unmodifiableSet(namesByTheme.keySet());
    }

    /**
     * Returns the names for a specific theme.
     *
     * @param theme the theme name (e.g., "astronomical", "mythological", "star")
     * @return unmodifiable list of names for the theme, or empty list if theme not found
     */
    public List<String> getNamesByTheme(String theme) {
        if (theme == null || !namesByTheme.containsKey(theme)) {
            return Collections.emptyList();
        }
        return namesByTheme.get(theme);
    }

    /**
     * Sets the active theme for name selection.
     * <p>
     * After calling this method, {@link #getNextName()} and {@link #getRandomName()}
     * will return names from the specified theme.
     * </p>
     *
     * @param theme the theme name, or null to use all names
     */
    public void setActiveTheme(String theme) {
        if (theme != null && !theme.isEmpty() && namesByTheme.containsKey(theme)) {
            this.activeTheme = theme;
            this.names = new ArrayList<>(namesByTheme.get(theme));
        } else {
            this.activeTheme = null;
            this.names = loadNamesForTheme(null);
        }
    }

    /**
     * @return the currently active theme, or null if using all names
     */
    public String getActiveTheme() {
        return activeTheme;
    }

    @Override
    public String toString() {
        return String.format("GalaxyNameDatabase{total=%d, used=%d, prefix='%s', theme='%s'}",
                names.size(), usedNames.size(), catalogPrefix, activeTheme);
    }
}
