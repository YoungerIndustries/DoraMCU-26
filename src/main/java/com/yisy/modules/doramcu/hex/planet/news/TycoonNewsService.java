package com.yisy.modules.doramcu.hex.planet.news;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Generates news events from game state changes.
 * <p>
 * The service:
 * <ul>
 *   <li>Generates news from game events</li>
 *   <li>Tracks news history</li>
 *   <li>Provides news feed queries</li>
 * </ul>
 * </p>
 */
@Service
public class TycoonNewsService {

    private static final Logger log = LoggerFactory.getLogger(TycoonNewsService.class);

    /** Maximum news items to keep */
    private static final int MAX_NEWS_ITEMS = 100;

    /** All news items */
    private final List<TycoonNewsItem> newsItems;

    /** News items by corporation */
    private final Map<String, List<TycoonNewsItem>> corporationNews;

    public TycoonNewsService() {
        this.newsItems = new CopyOnWriteArrayList<>();
        this.corporationNews = new ConcurrentHashMap<>();
    }

    /**
     * Adds a news item.
     *
     * @param item the news item
     */
    public void addNews(TycoonNewsItem item) {
        newsItems.add(0, item); // newest first

        // Trim old items
        while (newsItems.size() > MAX_NEWS_ITEMS) {
            newsItems.remove(newsItems.size() - 1);
        }

        // Add to corporation-specific news if applicable
        if (item.getSourceId() != null) {
            corporationNews.computeIfAbsent(item.getSourceId(), k -> new CopyOnWriteArrayList<>())
                    .add(0, item);
        }

        log.debug("Added news: {}", item.getHeadline());
    }

    /**
     * Generates news for a new industry built.
     */
    public void onIndustryBuilt(Industry industry, String corporationId) {
        addNews(new TycoonNewsItem(
                TycoonNewsItem.NewsCategory.INDUSTRY,
                "New " + industry.getType().getDisplayName() + " Built",
                industry.getType().getDisplayName() + " has been constructed at " +
                        industry.getLocation() + ".",
                corporationId
        ));
    }

    /**
     * Generates news for a new station built.
     */
    public void onStationBuilt(Station station, String corporationId) {
        addNews(new TycoonNewsItem(
                TycoonNewsItem.NewsCategory.WORLD_EVENTS,
                "New Station: " + station.getName(),
                station.getName() + " station has been opened at " +
                        station.getPosition() + ".",
                corporationId
        ));
    }

    /**
     * Generates news for a vehicle purchased.
     */
    public void onVehiclePurchased(SurfaceVehicle vehicle, String corporationId) {
        addNews(new TycoonNewsItem(
                TycoonNewsItem.NewsCategory.VEHICLE_STATUS,
                vehicle.getType().getDisplayName() + " Purchased",
                "A new " + vehicle.getType().getDisplayName() + " (" + vehicle.getId() +
                        ") has been added to the fleet.",
                corporationId
        ));
    }

    /**
     * Generates news for a route completed.
     */
    public void onRouteCompleted(String routeId, String corporationId, long income) {
        addNews(new TycoonNewsItem(
                TycoonNewsItem.NewsCategory.ECONOMY,
                "Route Completed Successfully",
                "Route " + routeId + " has completed a delivery, earning " + income + " credits.",
                corporationId
        ));
    }

    /**
     * Generates news for AI rival action.
     */
    public void onAiRivalAction(String rivalName, String action, String corporationId) {
        addNews(new TycoonNewsItem(
                TycoonNewsItem.NewsCategory.COMPETITION,
                rivalName + " is Active",
                rivalName + " has " + action + ".",
                corporationId
        ));
    }

    /**
     * Generates news for a loan.
     */
    public void onLoanTaken(String corporationName, long amount, double rate) {
        addNews(new TycoonNewsItem(
                TycoonNewsItem.NewsCategory.LOAN,
                "Loan Approved for " + corporationName,
                corporationName + " has taken a loan of " + amount +
                        " credits at " + (rate * 100) + "% interest.",
                corporationName
        ));
    }

    /**
     * Generates news for a vehicle breakdown.
     */
    public void onVehicleBreakdown(SurfaceVehicle vehicle, String corporationId) {
        addNews(new TycoonNewsItem(
                TycoonNewsItem.NewsCategory.VEHICLE_STATUS,
                vehicle.getType().getDisplayName() + " Breakdown",
                vehicle.getType().getDisplayName() + " (" + vehicle.getId() +
                        ") has broken down at " + vehicle.getCurrentPosition() + ".",
                corporationId
        ));
    }

    /**
     * Generates news for market changes.
     */
    public void onMarketChange(String cargoTypeName, String change, String corporationId) {
        addNews(new TycoonNewsItem(
                TycoonNewsItem.NewsCategory.ECONOMY,
                "Market Update: " + cargoTypeName,
                cargoTypeName + " " + change + ".",
                corporationId
        ));
    }

    /**
     * Gets the latest news items.
     *
     * @param limit maximum number of items
     * @return list of news items
     */
    public List<TycoonNewsItem> getLatestNews(int limit) {
        int size = Math.min(limit, newsItems.size());
        return new ArrayList<>(newsItems.subList(0, size));
    }

    /**
     * Gets news items for a corporation.
     *
     * @param corporationId the corporation ID
     * @param limit maximum number of items
     * @return list of news items
     */
    public List<TycoonNewsItem> getCorporationNews(String corporationId, int limit) {
        List<TycoonNewsItem> items = corporationNews.getOrDefault(corporationId, Collections.emptyList());
        int size = Math.min(limit, items.size());
        return new ArrayList<>(items.subList(0, size));
    }

    /**
     * Gets news items by category.
     *
     * @param category the news category
     * @param limit maximum number of items
     * @return list of news items
     */
    public List<TycoonNewsItem> getNewsByCategory(TycoonNewsItem.NewsCategory category, int limit) {
        List<TycoonNewsItem> filtered = new ArrayList<>();
        for (TycoonNewsItem item : newsItems) {
            if (item.getCategory() == category) {
                filtered.add(item);
                if (filtered.size() >= limit) break;
            }
        }
        return filtered;
    }

    /**
     * Clears all news items.
     */
    public void clearNews() {
        newsItems.clear();
        corporationNews.clear();
    }
}
