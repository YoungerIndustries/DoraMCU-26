package com.yisy.modules.doramcu.earth2026.tycoon;

import java.time.LocalDateTime;

/**
 * Represents an employee in the Corporation Tycoon game.
 * Employees provide bonuses to resource generation based on their role.
 */
public class TycoonEmployee {

    public enum EmployeeRole {
        ANALYST("Analyst", 10, 5, 0, 0),
        ENGINEER("Engineer", 5, 0, 10, 5),
        MARKETER("Marketer", 5, 10, 0, 0),
        SECURITY("Security", 0, 5, 0, 5),
        MANAGER("Manager", 15, 5, 5, 5),
        SCIENTIST("Scientist", 5, 0, 15, 0),
        PILOT("Pilot", 0, 0, 5, 10),
        DOCTOR("Doctor", 5, 0, 5, 5);

        private final String displayName;
        private final int creditBonus;
        private final int influenceBonus;
        private final int techBonus;
        private final int materialsBonus;

        EmployeeRole(String displayName, int creditBonus, int influenceBonus, int techBonus, int materialsBonus) {
            this.displayName = displayName;
            this.creditBonus = creditBonus;
            this.influenceBonus = influenceBonus;
            this.techBonus = techBonus;
            this.materialsBonus = materialsBonus;
        }

        public String getDisplayName() {
            return displayName;
        }

        public int getCreditBonus() {
            return creditBonus;
        }

        public int getInfluenceBonus() {
            return influenceBonus;
        }

        public int getTechBonus() {
            return techBonus;
        }

        public int getMaterialsBonus() {
            return materialsBonus;
        }
    }

    private String name;
    private String role;
    private long dailySalary;
    private boolean active;
    private int efficiency;
    private int creditBonus;
    private int influenceBonus;
    private int techBonus;
    private int materialsBonus;
    private LocalDateTime hiredAt;

    public TycoonEmployee() {
    }

    public void calculateStats() {
        // Base efficiency is 70-100%
        this.efficiency = 70 + (int) (Math.random() * 31);

        // Set bonuses based on role
        try {
            EmployeeRole roleEnum = EmployeeRole.valueOf(role.toUpperCase().replace(" ", "_"));
            this.creditBonus = roleEnum.getCreditBonus();
            this.influenceBonus = roleEnum.getInfluenceBonus();
            this.techBonus = roleEnum.getTechBonus();
            this.materialsBonus = roleEnum.getMaterialsBonus();
        } catch (IllegalArgumentException e) {
            // Default bonuses for unknown roles
            this.creditBonus = 5;
            this.influenceBonus = 5;
            this.techBonus = 0;
            this.materialsBonus = 0;
        }
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public long getDailySalary() {
        return dailySalary;
    }

    public void setDailySalary(long dailySalary) {
        this.dailySalary = dailySalary;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getEfficiency() {
        return efficiency;
    }

    public void setEfficiency(int efficiency) {
        this.efficiency = efficiency;
    }

    public int getCreditBonus() {
        return creditBonus;
    }

    public void setCreditBonus(int creditBonus) {
        this.creditBonus = creditBonus;
    }

    public int getInfluenceBonus() {
        return influenceBonus;
    }

    public void setInfluenceBonus(int influenceBonus) {
        this.influenceBonus = influenceBonus;
    }

    public int getTechBonus() {
        return techBonus;
    }

    public void setTechBonus(int techBonus) {
        this.techBonus = techBonus;
    }

    public int getMaterialsBonus() {
        return materialsBonus;
    }

    public void setMaterialsBonus(int materialsBonus) {
        this.materialsBonus = materialsBonus;
    }

    public LocalDateTime getHiredAt() {
        return hiredAt;
    }

    public void setHiredAt(LocalDateTime hiredAt) {
        this.hiredAt = hiredAt;
    }

    @Override
    public String toString() {
        return "TycoonEmployee{" +
                "name='" + name + '\'' +
                ", role='" + role + '\'' +
                ", active=" + active +
                ", efficiency=" + efficiency +
                '}';
    }
}
