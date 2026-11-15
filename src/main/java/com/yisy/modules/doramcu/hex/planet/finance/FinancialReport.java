package com.yisy.modules.doramcu.hex.planet.finance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Financial report for a corporation over a period of time.
 * <p>
 * Contains income, expenses, profit/loss, and per-route breakdowns.
 * </p>
 */
public class FinancialReport {

    private final String corporationId;
    private final String corporationName;
    private final int startTurn;
    private final int endTurn;

    private final long totalIncome;
    private final long totalExpenses;
    private final long netProfit;
    private final long loanBalance;
    private final long interestPaid;

    private final List<RouteReport> routeReports;

    /**
     * Creates a financial report.
     *
     * @param corporationId the corporation ID
     * @param corporationName the corporation name
     * @param startTurn the starting turn
     * @param endTurn the ending turn
     * @param totalIncome total income
     * @param totalExpenses total expenses
     * @param loanBalance current loan balance
     * @param interestPaid interest paid this period
     */
    public FinancialReport(String corporationId, String corporationName,
                            int startTurn, int endTurn,
                            long totalIncome, long totalExpenses,
                            long loanBalance, long interestPaid) {
        this.corporationId = corporationId;
        this.corporationName = corporationName;
        this.startTurn = startTurn;
        this.endTurn = endTurn;
        this.totalIncome = totalIncome;
        this.totalExpenses = totalExpenses;
        this.netProfit = totalIncome - totalExpenses;
        this.loanBalance = loanBalance;
        this.interestPaid = interestPaid;
        this.routeReports = new ArrayList<>();
    }

    /** @return the corporation ID */
    public String getCorporationId() {
        return corporationId;
    }

    /** @return the corporation name */
    public String getCorporationName() {
        return corporationName;
    }

    /** @return the starting turn */
    public int getStartTurn() {
        return startTurn;
    }

    /** @return the ending turn */
    public int getEndTurn() {
        return endTurn;
    }

    /** @return the total income */
    public long getTotalIncome() {
        return totalIncome;
    }

    /** @return the total expenses */
    public long getTotalExpenses() {
        return totalExpenses;
    }

    /** @return the net profit (income - expenses) */
    public long getNetProfit() {
        return netProfit;
    }

    /** @return the current loan balance */
    public long getLoanBalance() {
        return loanBalance;
    }

    /** @return the interest paid this period */
    public long getInterestPaid() {
        return interestPaid;
    }

    /** @return true if the corporation is profitable */
    public boolean isProfitable() {
        return netProfit > 0;
    }

    /**
     * Adds a route report to this financial report.
     *
     * @param routeReport the route report
     */
    public void addRouteReport(RouteReport routeReport) {
        routeReports.add(routeReport);
    }

    /** @return list of route reports */
    public List<RouteReport> getRouteReports() {
        return Collections.unmodifiableList(routeReports);
    }

    @Override
    public String toString() {
        return String.format("FinancialReport{corp=%s, income=%d, expenses=%d, profit=%d, loans=%d}",
                corporationName, totalIncome, totalExpenses, netProfit, loanBalance);
    }

    /**
     * Report for a single route.
     */
    public static class RouteReport {
        private final String routeId;
        private final String routeName;
        private final long income;
        private final long expenses;
        private final long profit;
        private final int cargoDelivered;
        private final int passengersDelivered;

        public RouteReport(String routeId, String routeName,
                            long income, long expenses,
                            int cargoDelivered, int passengersDelivered) {
            this.routeId = routeId;
            this.routeName = routeName;
            this.income = income;
            this.expenses = expenses;
            this.profit = income - expenses;
            this.cargoDelivered = cargoDelivered;
            this.passengersDelivered = passengersDelivered;
        }

        public String getRouteId() { return routeId; }
        public String getRouteName() { return routeName; }
        public long getIncome() { return income; }
        public long getExpenses() { return expenses; }
        public long getProfit() { return profit; }
        public int getCargoDelivered() { return cargoDelivered; }
        public int getPassengersDelivered() { return passengersDelivered; }
    }
}
