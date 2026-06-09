package com.performance.model;

/**
 * Manager entity - represents a user who has direct reports.
 * Contains manager-specific business logic.
 */
public class Manager extends User {
    private static final long serialVersionUID = 2L;

    public Manager(String username, String password, String name) {
        super(username, password, name, UserRole.MANAGER_EMPLOYEE);
    }

    /**
     * Check if this manager can review a specific employee
     */
    public boolean canReview(User employee) {
        if (employee == null)
            return false;
        if (this.getStatus() != UserStatus.ACTIVE)
            return false;

        // Can review direct reports
        if (getDirectReports() != null && getDirectReports().contains(employee)) {
            return true;
        }

        // Can also review indirect reports (descendants)
        return isDescendant(employee);
    }

    /**
     * Check if an employee is a descendant (direct or indirect report)
     */
    public boolean isDescendant(User employee) {
        if (getDirectReports() == null)
            return false;

        for (User report : getDirectReports()) {
            if (report.getUsername().equals(employee.getUsername())) {
                return true;
            }
            // Check descendants recursively
            if (report.getDirectReports() != null && !report.getDirectReports().isEmpty()) {
                if (isDescendantOf(report, employee)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isDescendantOf(User manager, User target) {
        if (manager.getDirectReports() == null)
            return false;
        for (User report : manager.getDirectReports()) {
            if (report.getUsername().equals(target.getUsername())) {
                return true;
            }
            if (isDescendantOf(report, target)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if this manager can accept more direct reports
     */
    public boolean canAcceptMoreReports() {
        int currentCount = getDirectReports() != null ? getDirectReports().size() : 0;
        return currentCount < getMaxDirectReports();
    }

    /**
     * Get count of direct reports
     */
    public int getDirectReportCount() {
        return getDirectReports() != null ? getDirectReports().size() : 0;
    }

    /**
     * Get all descendants count (recursive)
     */
    public int getTotalTeamSize() {
        return countDescendants(this);
    }

    private int countDescendants(User user) {
        int count = 0;
        if (user.getDirectReports() != null) {
            count = user.getDirectReports().size();
            for (User report : user.getDirectReports()) {
                count += countDescendants(report);
            }
        }
        return count;
    }

    /**
     * Check if manager can assign training to an employee
     */
    public boolean canAssignTraining(User employee) {
        return canReview(employee) && employee.getStatus() == UserStatus.ACTIVE;
    }

    /**
     * Check if manager can set goals for an employee
     */
    public boolean canSetGoalsFor(User employee) {
        return canReview(employee) && employee.getStatus() == UserStatus.ACTIVE;
    }

    /**
     * Validate a review score (should be 1-10)
     */
    public boolean isValidScore(int score) {
        return score >= 1 && score <= 10;
    }

    /**
     * Create review scores with validation
     */
    public Review createValidatedReview(User employee, int punct, int team, int prod) {
        if (!canReview(employee)) {
            throw new IllegalArgumentException("Cannot review this employee");
        }
        if (!isValidScore(punct) || !isValidScore(team) || !isValidScore(prod)) {
            throw new IllegalArgumentException("Scores must be between 1 and 10");
        }

        Review review = new Review(employee, this);
        review.setPunctualityScore(punct);
        review.setTeamworkScore(team);
        review.setProductivityScore(prod);
        return review;
    }

    @Override
    public String toString() {
        return getUsername() + " (" + getName() + ")";
    }
}
