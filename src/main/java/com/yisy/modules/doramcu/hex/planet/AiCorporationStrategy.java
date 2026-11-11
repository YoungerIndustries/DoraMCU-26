package com.yisy.modules.doramcu.hex.planet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Heuristic-based AI strategy for rival corporations in the Business Tycoon campaign.
 * <p>
 * AI corporations use the same {@link PlanetSurfaceBuilder} and {@link PlanetContractManager}
 * as the player, but with heuristic-based decision making:
 * <ul>
 *   <li>Build where profit is highest</li>
 *   <li>Bid on contracts within budget</li>
 *   <li>Establish routes between high-demand locations</li>
 *   <li>Manage budget: spend on infrastructure vs save</li>
 *   <li>Competitive behavior: block strategic locations, undercut prices</li>
 * </ul>
 * <p>
 * The AI processes one turn at a time, making decisions based on the current
 * state of the corporation and available opportunities.
 * </p>
 */
public class AiCorporationStrategy {

    /**
     * AI personality types that affect decision-making.
     */
    public enum AiPersonality {
        /** Aggressive expansion, high spending, high risk */
        AGGRESSIVE(0.8, 0.3, 0.7),
        /** Balanced approach, moderate spending */
        BALANCED(0.5, 0.5, 0.5),
        /** Cautious growth, high savings, low risk */
        CAUTIOUS(0.2, 0.8, 0.3),
        /** Focus on route optimization over expansion */
        ROUTE_OPTIMIZER(0.4, 0.6, 0.8);

        private final double spendingRatio;
        private final double savingsRatio;
        private final double routePriority;

        AiPersonality(double spendingRatio, double savingsRatio, double routePriority) {
            this.spendingRatio = spendingRatio;
            this.savingsRatio = savingsRatio;
            this.routePriority = routePriority;
        }

        /** @return the ratio of credits to spend each turn (0-1) */
        public double getSpendingRatio() {
            return spendingRatio;
        }

        /** @return the ratio of credits to save each turn (0-1) */
        public double getSavingsRatio() {
            return savingsRatio;
        }

        /** @return the priority given to routes over infrastructure (0-1) */
        public double getRoutePriority() {
            return routePriority;
        }
    }

    private final Random random;
    private final AiPersonality personality;

    /**
     * Creates a new AI strategy with the given personality.
     *
     * @param personality the AI personality type
     * @param seed        random seed for reproducibility
     */
    public AiCorporationStrategy(AiPersonality personality, long seed) {
        this.personality = personality;
        this.random = new Random(seed);
    }

    /**
     * Creates a new AI strategy with a random personality.
     *
     * @param seed random seed for reproducibility
     */
    public AiCorporationStrategy(long seed) {
        this.random = new Random(seed);
        AiPersonality[] personalities = AiPersonality.values();
        this.personality = personalities[random.nextInt(personalities.length)];
    }

    /**
     * Processes one turn for the AI corporation.
     * Makes decisions about contracts, infrastructure, and routes.
     *
     * @param corporation      the AI's corporation
     * @param availableContracts contracts available for acceptance
     * @param contractManager  the contract manager
     * @return list of actions taken this turn
     */
    public List<AiAction> processTurn(TransportCorporation corporation,
                                       List<PlanetContract> availableContracts,
                                       PlanetContractManager contractManager) {
        List<AiAction> actions = new ArrayList<>();

        // Calculate budget for this turn
        long budget = calculateBudget(corporation);

        // Decision 1: Accept profitable contracts within budget
        List<AiAction> contractActions = decideContracts(corporation, availableContracts, budget);
        actions.addAll(contractActions);
        for (AiAction action : contractActions) {
            if (action.type == AiActionType.ACCEPT_CONTRACT && action.contract != null) {
                contractManager.acceptContract(action.contract.getId(), corporation.getId());
                corporation.addContract(action.contract.getId());
            }
        }

        // Decision 2: Spend remaining budget on infrastructure
        long remainingBudget = budget - calculateSpent(actions);
        if (remainingBudget > 0) {
            List<AiAction> infraActions = decideInfrastructure(corporation, remainingBudget);
            actions.addAll(infraActions);
        }

        // Decision 3: Establish new routes if we have stations
        if (corporation.getOwnedStationIds().size() >= 2 && random.nextDouble() < personality.getRoutePriority()) {
            List<AiAction> routeActions = decideRoutes(corporation);
            actions.addAll(routeActions);
        }

        return actions;
    }

    /**
     * Calculates the AI's budget for the current turn.
     *
     * @param corporation the AI's corporation
     * @return budget in credits
     */
    private long calculateBudget(TransportCorporation corporation) {
        double ratio = personality.getSpendingRatio();
        return (long) (corporation.getCredits() * ratio);
    }

    /**
     * Decides which contracts to accept based on profitability and budget.
     *
     * @param corporation        the AI's corporation
     * @param availableContracts contracts available for acceptance
     * @param budget             the turn budget
     * @return list of actions
     */
    private List<AiAction> decideContracts(TransportCorporation corporation,
                                            List<PlanetContract> availableContracts,
                                            long budget) {
        List<AiAction> actions = new ArrayList<>();

        // Sort contracts by profitability (payment / difficulty)
        List<PlanetContract> sorted = availableContracts.stream()
                .filter(c -> c.getStatus() == PlanetContract.ContractStatus.AVAILABLE)
                .filter(c -> !corporation.getActiveContractIds().contains(c.getId()))
                .sorted(Comparator.comparingInt((PlanetContract c) -> c.getPaymentCredits() / Math.max(1, c.getDifficulty()))
                        .reversed())
                .collect(Collectors.toList());

        long spent = 0;
        for (PlanetContract contract : sorted) {
            if (spent + contract.getPaymentCredits() <= budget) {
                // Check if we can afford the required resources
                if (canAffordResources(corporation, contract)) {
                    actions.add(new AiAction(AiActionType.ACCEPT_CONTRACT, contract,
                            "Accepting contract: " + contract.getTitle()));
                    spent += calculateContractCost(contract);
                }
            }
        }

        return actions;
    }

    /**
     * Decides on infrastructure investments based on remaining budget.
     *
     * @param corporation   the AI's corporation
     * @param remainingBudget remaining budget
     * @return list of actions
     */
    private List<AiAction> decideInfrastructure(TransportCorporation corporation,
                                                 long remainingBudget) {
        List<AiAction> actions = new ArrayList<>();

        // Priority: stations > roads > vehicles
        if (remainingBudget >= 1000 && corporation.getOwnedStationIds().size() < corporation.getRank().getMaxRoutes()) {
            actions.add(new AiAction(AiActionType.BUILD_STATION, null,
                    "Building new station for network expansion"));
        } else if (remainingBudget >= 500) {
            actions.add(new AiAction(AiActionType.BUILD_ROAD, null,
                    "Extending road network"));
        }

        return actions;
    }

    /**
     * Decides on new route establishment.
     *
     * @param corporation the AI's corporation
     * @return list of actions
     */
    private List<AiAction> decideRoutes(TransportCorporation corporation) {
        List<AiAction> actions = new ArrayList<>();

        // If we have enough stations, establish a route
        if (corporation.getOwnedStationIds().size() >= 2) {
            actions.add(new AiAction(AiActionType.ESTABLISH_ROUTE, null,
                    "Establishing new trade route"));
        }

        return actions;
    }

    /**
     * Checks if the corporation can afford the resources for a contract.
     *
     * @param corporation the corporation
     * @param contract    the contract
     * @return true if resources are available
     */
    private boolean canAffordResources(TransportCorporation corporation, PlanetContract contract) {
        // Simplified: check if the corporation has enough credits
        long cost = calculateContractCost(contract);
        return corporation.getCredits() >= cost;
    }

    /**
     * Calculates the cost of executing a contract.
     *
     * @param contract the contract
     * @return cost in credits
     */
    private long calculateContractCost(PlanetContract contract) {
        // Cost is based on difficulty and duration
        return (long) contract.getDifficulty() * contract.getDurationTurns() * 50;
    }

    /**
     * Calculates the total credits spent on actions.
     *
     * @param actions the list of actions
     * @return total credits spent
     */
    private long calculateSpent(List<AiAction> actions) {
        return actions.stream()
                .filter(a -> a.contract != null)
                .mapToLong(a -> calculateContractCost(a.contract))
                .sum();
    }

    /**
     * Returns the AI's personality type.
     *
     * @return the personality
     */
    public AiPersonality getPersonality() {
        return personality;
    }

    /**
     * Represents an action taken by the AI.
     */
    public static class AiAction {
        public final AiActionType type;
        public final PlanetContract contract;
        public final String description;

        AiAction(AiActionType type, PlanetContract contract, String description) {
            this.type = type;
            this.contract = contract;
            this.description = description;
        }

        @Override
        public String toString() {
            return String.format("AiAction{%s: %s}", type, description);
        }
    }

    /**
     * Types of actions the AI can take.
     */
    public enum AiActionType {
        /** Accept a contract */
        ACCEPT_CONTRACT,
        /** Build a station */
        BUILD_STATION,
        /** Build a road */
        BUILD_ROAD,
        /** Build a rail line */
        BUILD_RAIL,
        /** Deploy a vehicle */
        DEPLOY_VEHICLE,
        /** Establish a trade route */
        ESTABLISH_ROUTE,
        /** Save credits */
        SAVE
    }
}
