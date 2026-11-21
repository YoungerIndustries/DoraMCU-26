package com.yisy.modules.doramcu.hex.planet.news;
import java.time.Instant;
import java.util.UUID;

/**
 * A single news item in the tycoon news feed.
 * <p>
 * News items are generated from game events and delivered to players.
 * </p>
 */
public class TycoonNewsItem {

    public enum NewsCategory {
        ECONOMY, COMPETITION, WORLD_EVENTS, VEHICLE_STATUS, INDUSTRY, LOAN
    }

    private final String id;
    private final NewsCategory category;
    private final String headline;
    private final String message;
    private final Instant timestamp;
    private final String sourceId;

    /**
     * Creates a news item.
     *
     * @param category the news category
     * @param headline the headline
     * @param message the detailed message
     * @param sourceId the source entity ID
     */
    public TycoonNewsItem(NewsCategory category, String headline, String message, String sourceId) {
        this.id = UUID.randomUUID().toString();
        this.category = category;
        this.headline = headline;
        this.message = message;
        this.timestamp = Instant.now();
        this.sourceId = sourceId;
    }

    /** @return the unique news item ID */
    public String getId() {
        return id;
    }

    /** @return the news category */
    public NewsCategory getCategory() {
        return category;
    }

    /** @return the headline */
    public String getHeadline() {
        return headline;
    }

    /** @return the detailed message */
    public String getMessage() {
        return message;
    }

    /** @return the timestamp */
    public Instant getTimestamp() {
        return timestamp;
    }

    /** @return the source entity ID */
    public String getSourceId() {
        return sourceId;
    }

    @Override
    public String toString() {
        return String.format("TycoonNewsItem{category=%s, headline=%s}", category, headline);
    }
}
