package com.yisy.modules.doramcu.earth2026.model;

/**
 * Enum defining the types of notifications in the Younger Industries HQ.
 */
public enum HqNotificationType {

    RENT_DUE("Rent Due", "Office rent payment is approaching its due date", "warning"),
    RENT_OVERDUE("Rent Overdue", "Office rent payment is past due", "error"),
    RENT_PAID("Rent Paid", "Office rent payment has been processed", "success"),
    EVICTION_WARNING("Eviction Warning", "Office eviction is imminent due to non-payment", "error"),
    EVICTION_NOTICE("Eviction Notice", "Office has been reclaimed due to non-payment", "error"),
    MEETING_REQUEST("Meeting Request", "A corporation has requested a meeting with you", "info"),
    MEETING_ACCEPTED("Meeting Accepted", "Your meeting request has been accepted", "success"),
    MEETING_DECLINED("Meeting Declined", "Your meeting request has been declined", "warning"),
    MEETING_COMPLETED("Meeting Completed", "A meeting has been completed", "success"),
    OFFICE_ASSIGNED("Office Assigned", "A new office unit has been assigned to your corporation", "success"),
    OFFICE_UPGRADED("Office Upgraded", "Your office kiosk has been upgraded", "success"),
    HQ_EVENT("HQ Event", "A special event is happening in the HQ", "info"),
    LOBBY_ANNOUNCEMENT("Lobby Announcement", "A new announcement in the HQ lobby", "info"),
    TRADE_OFFER("Trade Offer", "A corporation has made a trade offer", "info"),
    SECURITY_ALERT("Security Alert", "A security incident has been reported", "error");

    private final String displayName;
    private final String description;
    private final String severity;

    HqNotificationType(String displayName, String description, String severity) {
        this.displayName = displayName;
        this.description = description;
        this.severity = severity;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public String getSeverity() {
        return severity;
    }
}
