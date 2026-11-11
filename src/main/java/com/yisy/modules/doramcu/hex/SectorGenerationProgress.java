package com.yisy.modules.doramcu.hex;

import java.util.List;

/**
 * Callback interface for sector-level streamed galaxy generation progress reporting.
 * <p>
 * When using streamed galaxy generation, sectors are generated and persisted one at a time,
 * allowing garbage collection between sectors. This interface reports progress at the
 * sector level, so the caller can display incremental progress during the entire galaxy
 * generation process.
 * </p>
 * <p>
 * The generation flow is:
 * <ol>
 *   <li>{@code onSectorStart} — a new sector begins generation</li>
 *   <li>{@code onSystemGenerated} — each system within the sector is generated</li>
 *   <li>{@code onSectorPersisted} — the sector's systems are persisted to the database</li>
 *   <li>{@code onSectorCleared} — the sector's in-memory data is cleared for GC</li>
 *   <li>(repeat for next sector)</li>
 *   <li>{@code onHyperlanesStart} — hyperlane generation begins from persisted data</li>
 *   <li>{@code onHyperlanesGenerated} — hyperlanes are persisted</li>
 *   <li>{@code onComplete} — galaxy generation is fully complete</li>
 * </ol>
 * </p>
 */
public interface SectorGenerationProgress {

    /**
     * Called when a new sector begins generation.
     *
     * @param sectorRow the sector row (0-9)
     * @param sectorCol the sector column (0-9)
     * @param sectorName the display name of the sector
     * @param totalSectors the total number of sectors in the galaxy (always 100)
     * @param sectorsCompleted the number of sectors already completed before this one
     */
    default void onSectorStart(int sectorRow, int sectorCol, String sectorName,
                               int totalSectors, int sectorsCompleted) {
        // Default no-op
    }

    /**
     * Called when a solar system within the current sector is generated.
     *
     * @param system the newly generated solar system
     * @param systemsInSector the number of systems generated in this sector so far
     */
    default void onSystemGenerated(SolarSystem system, int systemsInSector) {
        // Default no-op
    }

    /**
     * Called when a sector's systems have been persisted to the database.
     *
     * @param sectorRow the sector row (0-9)
     * @param sectorCol the sector column (0-9)
     * @param systemsPersisted the number of systems persisted in this sector
     * @param totalSystemsPersisted the cumulative systems persisted across all sectors so far
     */
    default void onSectorPersisted(int sectorRow, int sectorCol,
                                   int systemsPersisted, int totalSystemsPersisted) {
        // Default no-op
    }

    /**
     * Called when a sector's in-memory data has been cleared to allow garbage collection.
     *
     * @param sectorRow the sector row (0-9)
     * @param sectorCol the sector column (0-9)
     * @param sectorsCompleted the number of sectors completed after this clear
     * @param totalSectors the total number of sectors in the galaxy
     */
    default void onSectorCleared(int sectorRow, int sectorCol,
                                 int sectorsCompleted, int totalSectors) {
        // Default no-op
    }

    /**
     * Called when hyperlane generation begins from persisted system data.
     * <p>
     * In streamed mode, hyperlanes are generated after all sectors are persisted,
     * using the database records instead of in-memory galaxy objects.
     * </p>
     */
    default void onHyperlanesStart() {
        // Default no-op
    }

    /**
     * Called when hyperlanes have been generated and persisted.
     *
     * @param totalHyperlanes the number of hyperlanes created
     */
    default void onHyperlanesGenerated(int totalHyperlanes) {
        // Default no-op
    }

    /**
     * Called when the entire galaxy generation is complete.
     *
     * @param totalSystems the total number of systems generated
     * @param totalHyperlanes the total number of hyperlanes created
     * @param galaxyName the name of the generated galaxy
     */
    default void onComplete(int totalSystems, int totalHyperlanes, String galaxyName) {
        // Default no-op
    }

    /**
     * Called when a log message should be appended to the build log.
     *
     * @param message the log message text
     */
    default void onLog(String message) {
        // Default no-op
    }

    /**
     * Called when an error occurs during sector generation.
     *
     * @param sectorRow the sector row where the error occurred (may be -1 if not sector-specific)
     * @param sectorCol the sector column where the error occurred (may be -1 if not sector-specific)
     * @param error the exception that occurred
     */
    default void onError(int sectorRow, int sectorCol, Exception error) {
        // Default no-op
    }
}

