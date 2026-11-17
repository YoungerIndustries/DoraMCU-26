package com.yisy.modules.doramcu.earth2026.model;

import java.math.BigDecimal;

/**
 * Enum defining all available kiosk upgrade types for corporation offices.
 * Each upgrade affects visual appearance and game mechanics.
 */
public enum KioskUpgradeType {

    SINGLE_MONITOR("Single Monitor", "Basic terminal with single screen", new BigDecimal("500"), 1),
    DUAL_MONITOR("Dual Monitor", "Dual-screen workstation for multitasking", new BigDecimal("1500"), 2),
    TRIPLE_MONITOR("Triple Monitor", "Command center setup with three screens", new BigDecimal("3000"), 3),
    QUAD_MONITOR("Quad Monitor", "Full command center with four screens", new BigDecimal("5000"), 4),
    HOLOGRAPHIC_DISPLAY("Holographic Display", "3D holographic projector for R&D visualization", new BigDecimal("10000"), 5),
    SECURE_TERMINAL("Secure Terminal", "Encrypted comms line for sensitive operations", new BigDecimal("2000"), 2),
    COFFEE_MACHINE("Coffee Machine", "Morale boost for employees", new BigDecimal("300"), 1),
    PLANT_DECORATION("Plant Decoration", "Aesthetic improvement with living plants", new BigDecimal("200"), 1),
    WALL_ART("Wall Art", "Corporation branding and decorative wall art", new BigDecimal("800"), 1),
    EXECUTIVE_CHAIR("Executive Chair", "Premium ergonomic chair for the CEO", new BigDecimal("1200"), 2),
    BOOKSHELF("Bookshelf", "Knowledge repository with reference materials", new BigDecimal("600"), 1),
    WHITEBOARD("Whiteboard", "Strategy planning surface", new BigDecimal("400"), 1),
    FLAG_DISPLAY("Flag Display", "Corporation flag and emblem display", new BigDecimal("1000"), 2),
    AMBIENT_LIGHTING("Ambient Lighting", "Customizable mood lighting system", new BigDecimal("700"), 1),
    SOUND_SYSTEM("Sound System", "Background music and ambient sound system", new BigDecimal("900"), 1),
    DATA_CENTER_LINK("Data Center Link", "Direct link to corporation data center", new BigDecimal("8000"), 4),
    RESEARCH_CONSOLE("Research Console", "Dedicated R&D terminal with analysis tools", new BigDecimal("6000"), 3),
    SURVEILLANCE_FEED("Surveillance Feed", "Live surveillance monitoring station", new BigDecimal("4000"), 3),
    TRADE_TERMINAL("Trade Terminal", "Dedicated trading and market analysis terminal", new BigDecimal("3500"), 3),
    COMMAND_CENTER("Command Center", "Full command center transformation", new BigDecimal("20000"), 5);

    private final String displayName;
    private final String description;
    private final BigDecimal cost;
    private final int minBuildingLevel;

    KioskUpgradeType(String displayName, String description, BigDecimal cost, int minBuildingLevel) {
        this.displayName = displayName;
        this.description = description;
        this.cost = cost;
        this.minBuildingLevel = minBuildingLevel;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public int getMinBuildingLevel() {
        return minBuildingLevel;
    }
}
