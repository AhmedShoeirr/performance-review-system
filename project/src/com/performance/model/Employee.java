package com.performance.model;

/**
 * Employee entity - represents a tier 1 employee with no direct reports.
 * Contains entity-specific business logic.
 */
public class Employee extends User {
    private static final long serialVersionUID = 2L;

    public Employee(String username, String password, String name) {
        super(username, password, name, UserRole.EMPLOYEE);
    }

    /**
     * Check if this employee can review their manager
     */
    public boolean canReviewManager() {
        return getManager() != null && getStatus() == UserStatus.ACTIVE;
    }

    /**
     * Check if this employee has pending reviews to view
     */
    public boolean hasManager() {
        return getManager() != null;
    }

    /**
     * Get the employee's tier description
     */
    public String getTierDescription() {
        if (getTierLevel() == 1 && (getDirectReports() == null || getDirectReports().isEmpty())) {
            return "Employee (Tier 1)";
        }
        return "Employee & Manager (Tier " + getTierLevel() + ")";
    }

    /**
     * Check if this employee is eligible for promotion (has good reviews)
     * A simple rule: average score > 7 out of 10
     */
    public boolean isEligibleForPromotion(double averageScore) {
        return averageScore >= 7.0 && getStatus() == UserStatus.ACTIVE;
    }

    /**
     * Get display name with role indicator
     */
    public String getDisplayName() {
        return getName() + " (" + getUsername() + ")";
    }

    /**
     * Validate if the employee can have a goal assigned
     */
    public boolean canHaveGoals() {
        return getStatus() == UserStatus.ACTIVE;
    }

    /**
     * Validate if the employee can receive training
     */
    public boolean canReceiveTraining() {
        return getStatus() == UserStatus.ACTIVE;
    }

    @Override
    public String toString() {
        return getUsername() + " (" + getName() + ")";
    }
}
