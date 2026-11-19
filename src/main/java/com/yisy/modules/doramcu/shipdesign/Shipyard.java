package com.yisy.modules.doramcu.shipdesign;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Manages a fleet of ships under construction.
 * <p>
 * A Shipyard tracks ships in various stages of construction, from planning
 * through completion. It supports building ships from templates, custom
 * builders, and design point validation.
 * </p>
 */
public class Shipyard {

    /**
     * The construction stage of a ship.
     */
    public enum Stage {
        PLANNING, UNDER_CONSTRUCTION, COMPLETE, CANCELLED
    }

    /**
     * Represents a ship order in the shipyard.
     */
    public static class ShipOrder {
        private final String orderId;
        private final String shipName;
        private final ShipTemplate template;
        private final ShipBuilder builder;
        private Stage stage;
        private Ship completedShip;
        private final List<String> notes = new ArrayList<>();
        private DesignPoints designPoints;

        /**
         * Creates a new ship order from a template.
         *
         * @param orderId    unique order identifier
         * @param shipName   name for the ship to build
         * @param template   the template to base the ship on
         */
        ShipOrder(String orderId, String shipName, ShipTemplate template) {
            this.orderId = orderId;
            this.shipName = shipName;
            this.template = template;
            this.builder = null;
            this.stage = Stage.PLANNING;
        }

        /**
         * Creates a new ship order from a custom builder.
         *
         * @param orderId    unique order identifier
         * @param builder    the builder configuration
         */
        ShipOrder(String orderId, ShipBuilder builder) {
            this.orderId = orderId;
            this.shipName = null;
            this.template = null;
            this.builder = builder;
            this.stage = Stage.PLANNING;
        }

        public String getOrderId() {
            return orderId;
        }

        public String getShipName() {
            return shipName;
        }

        public ShipTemplate getTemplate() {
            return template;
        }

        public ShipBuilder getBuilder() {
            return builder;
        }

        public Stage getStage() {
            return stage;
        }

        void setStage(Stage stage) {
            this.stage = stage;
        }

        public Ship getCompletedShip() {
            return completedShip;
        }

        void setCompletedShip(Ship ship) {
            this.completedShip = ship;
        }

        public List<String> getNotes() {
            return new ArrayList<>(notes);
        }

        void addNote(String note) {
            notes.add(note);
        }

        public Optional<DesignPoints> getDesignPoints() {
            return Optional.ofNullable(designPoints);
        }

        void setDesignPoints(DesignPoints dp) {
            this.designPoints = dp;
        }

        @Override
        public String toString() {
            return "ShipOrder{" +
                    "id='" + orderId + '\'' +
                    ", name='" + shipName + '\'' +
                    ", stage=" + stage +
                    ", template=" + (template != null ? template.getName() : "custom") +
                    '}';
        }
    }

    private final Map<String, ShipOrder> orders;
    private final List<Ship> completedShips;
    private int nextOrderId;

    /**
     * Creates a new empty shipyard.
     */
    public Shipyard() {
        this.orders = new HashMap<>();
        this.completedShips = new ArrayList<>();
        this.nextOrderId = 1;
    }

    /**
     * Generates a new unique order ID.
     */
    private String generateOrderId() {
        String id = "ORD-" + String.format("%03d", nextOrderId++);
        return id;
    }

    /**
     * Places a new ship order from a template.
     *
     * @param shipName the name for the new ship
     * @param template the template to use
     * @return the created ship order
     */
    public ShipOrder orderFromTemplate(String shipName, ShipTemplate template) {
        String orderId = generateOrderId();
        ShipOrder order = new ShipOrder(orderId, shipName, template);
        orders.put(orderId, order);
        return order;
    }

    /**
     * Places a new ship order from a custom builder.
     *
     * @param builder the ship builder configuration
     * @return the created ship order
     */
    public ShipOrder orderFromBuilder(ShipBuilder builder) {
        String orderId = generateOrderId();
        ShipOrder order = new ShipOrder(orderId, builder);
        orders.put(orderId, order);
        return order;
    }

    /**
     * Validates the design points for a ship order.
     *
     * @param orderId the order to validate
     * @return true if the design is valid, false if over budget
     * @throws IllegalArgumentException if the order is not found
     */
    public boolean validateDesign(String orderId) {
        ShipOrder order = getOrder(orderId);

        if (order.getTemplate() != null) {
            Ship tempShip = order.getTemplate().buildShip(order.getShipName());
            DesignPoints dp = DesignPoints.evaluate(tempShip);
            order.setDesignPoints(dp);
            boolean valid = dp.validate();
            for (String warning : dp.getWarnings()) {
                order.addNote("Validation: " + warning);
            }
            return valid;
        } else if (order.getBuilder() != null) {
            try {
                DesignPoints dp = DesignPoints.evaluate(order.getBuilder());
                order.setDesignPoints(dp);
                boolean valid = dp.validate();
                for (String warning : dp.getWarnings()) {
                    order.addNote("Validation: " + warning);
                }
                return valid;
            } catch (IllegalStateException e) {
                order.addNote("Validation failed: " + e.getMessage());
                return false;
            }
        }

        return false;
    }

    /**
     * Starts construction on a ship order.
     *
     * @param orderId the order to start
     * @throws IllegalStateException if the order is not found or already in progress
     */
    public void startConstruction(String orderId) {
        ShipOrder order = getOrder(orderId);

        if (order.getStage() != Stage.PLANNING) {
            throw new IllegalStateException("Order " + orderId + " is not in PLANNING stage");
        }

        order.setStage(Stage.UNDER_CONSTRUCTION);
        order.addNote("Construction started");
    }

    /**
     * Completes a ship order, building the actual ship.
     *
     * @param orderId the order to complete
     * @return the completed ship
     * @throws IllegalStateException if the order is not under construction
     */
    public Ship completeOrder(String orderId) {
        ShipOrder order = getOrder(orderId);

        if (order.getStage() != Stage.UNDER_CONSTRUCTION) {
            throw new IllegalStateException("Order " + orderId + " is not under construction");
        }

        Ship ship;
        if (order.getTemplate() != null) {
            ship = order.getTemplate().buildShip(order.getShipName());
        } else if (order.getBuilder() != null) {
            ship = order.getBuilder().build();
        } else {
            throw new IllegalStateException("Order " + orderId + " has no template or builder");
        }

        order.setCompletedShip(ship);
        order.setStage(Stage.COMPLETE);
        order.addNote("Ship completed: " + ship.getName());
        completedShips.add(ship);

        return ship;
    }

    /**
     * Cancels a ship order.
     *
     * @param orderId the order to cancel
     * @throws IllegalStateException if the order is already complete
     */
    public void cancelOrder(String orderId) {
        ShipOrder order = getOrder(orderId);

        if (order.getStage() == Stage.COMPLETE) {
            throw new IllegalStateException("Cannot cancel completed order: " + orderId);
        }

        order.setStage(Stage.CANCELLED);
        order.addNote("Order cancelled");
    }

    /**
     * Adds a note to a ship order.
     *
     * @param orderId the order to add a note to
     * @param note    the note text
     */
    public void addNote(String orderId, String note) {
        ShipOrder order = getOrder(orderId);
        order.addNote(note);
    }

    /**
     * Gets a ship order by its ID.
     *
     * @param orderId the order ID
     * @return the ship order
     * @throws IllegalArgumentException if not found
     */
    public ShipOrder getOrder(String orderId) {
        if (!orders.containsKey(orderId)) {
            throw new IllegalArgumentException("Order not found: " + orderId);
        }
        return orders.get(orderId);
    }

    /**
     * Gets all ship orders.
     *
     * @return a list of all orders
     */
    public List<ShipOrder> getAllOrders() {
        return new ArrayList<>(orders.values());
    }

    /**
     * Gets all orders in a specific stage.
     *
     * @param stage the stage to filter by
     * @return a list of orders in the given stage
     */
    public List<ShipOrder> getOrdersByStage(Stage stage) {
        List<ShipOrder> result = new ArrayList<>();
        for (ShipOrder order : orders.values()) {
            if (order.getStage() == stage) {
                result.add(order);
            }
        }
        return result;
    }

    /**
     * Returns all completed ships.
     *
     * @return a list of completed ships
     */
    public List<Ship> getCompletedShips() {
        return new ArrayList<>(completedShips);
    }

    /**
     * Returns the number of active (non-complete, non-cancelled) orders.
     */
    public int getActiveOrderCount() {
        int count = 0;
        for (ShipOrder order : orders.values()) {
            if (order.getStage() == Stage.PLANNING || order.getStage() == Stage.UNDER_CONSTRUCTION) {
                count++;
            }
        }
        return count;
    }

    /**
     * Returns the total number of orders placed.
     */
    public int getTotalOrderCount() {
        return orders.size();
    }

    /**
     * Removes a completed order from the shipyard.
     *
     * @param orderId the order to remove
     */
    public void removeOrder(String orderId) {
        orders.remove(orderId);
    }

    /**
     * Processes all orders that are ready for construction.
     * Validates, starts, and completes each order in sequence.
     *
     * @return the number of ships completed
     */
    public int processAllOrders() {
        int completed = 0;
        List<String> orderIds = new ArrayList<>(orders.keySet());

        for (String orderId : orderIds) {
            ShipOrder order = orders.get(orderId);
            if (order.getStage() == Stage.PLANNING) {
                boolean valid = validateDesign(orderId);
                if (valid) {
                    startConstruction(orderId);
                    completeOrder(orderId);
                    completed++;
                } else {
                    order.addNote("Design validation failed, order paused");
                }
            }
        }

        return completed;
    }

    @Override
    public String toString() {
        return "Shipyard{" +
                "totalOrders=" + getTotalOrderCount() +
                ", active=" + getActiveOrderCount() +
                ", completed=" + completedShips.size() +
                '}';
    }
}
