package com.yisy.modules.doramcu.earth2026.model;

/**
 * Enum defining the status of a business meeting between corporations.
 */
public enum MeetingStatus {

    PENDING("Pending", "Meeting request is waiting for response"),
    ACCEPTED("Accepted", "Meeting has been accepted and is scheduled"),
    DECLINED("Declined", "Meeting request was declined"),
    COMPLETED("Completed", "Meeting has taken place and is concluded"),
    CANCELLED("Cancelled", "Meeting was cancelled by the requester");

    private final String displayName;
    private final String description;

    MeetingStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
