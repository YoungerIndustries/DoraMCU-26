package com.yisy.modules.doramcu.admin.model;

/**
 * Enum defining the types of notifications for AI admin characters.
 * Each type carries a display label, a default description, and a severity level
 * for UI coloring and priority sorting.
 */
public enum AiAdminNotificationType {

    ORDER_ASSIGNED("Order Assigned", "A new work order has been assigned to you", "info"),
    ORDER_COMPLETED("Order Completed", "A work order has been completed successfully", "success"),
    ORDER_FAILED("Order Failed", "A work order has failed during execution", "error"),
    ORDER_RETRIED("Order Retried", "A failed work order is being retried", "warning"),
    ORDER_STALE_RECLAIMED("Stale Order Reclaimed", "A stuck work order was reclaimed and reassigned", "warning"),
    WORKLOAD_WARNING("Workload Warning", "You are approaching the maximum concurrent order limit", "warning"),
    XP_MILESTONE("XP Milestone", "You have reached a new experience milestone", "success"),
    SYSTEM_ALERT("System Alert", "A system-level alert requires your attention", "error");

    private final String label;
    private final String description;
    private final String severity;

    AiAdminNotificationType(String label, String description, String severity) {
        this.label = label;
        this.description = description;
        this.severity = severity;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    public String getSeverity() {
        return severity;
    }
}
