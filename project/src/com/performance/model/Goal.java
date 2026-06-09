package com.performance.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Goal entity - represents a performance goal assigned to an employee.
 * Contains goal-specific business logic.
 */
public class Goal implements Serializable {
    private static final long serialVersionUID = 2L;

    // Status constants
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_CANCELLED = "CANCELLED";

    private User employee;
    private User creator;
    private String description;
    private String status;
    private Date createdDate;
    private Date completedDate;
    private String completionNotes;

    public Goal(User employee, String description, User creator) {
        this.employee = employee;
        this.description = description;
        this.creator = creator;
        this.status = STATUS_ACTIVE;
        this.createdDate = new Date();
    }

    // Backward compatibility constructor
    public Goal(User employee, String description) {
        this(employee, description, null);
    }

    // ==================== Business Logic ====================

    /**
     * Check if this goal can be completed
     */
    public boolean canComplete() {
        return STATUS_ACTIVE.equals(status);
    }

    /**
     * Complete this goal
     */
    public boolean complete(String notes) {
        if (!canComplete())
            return false;
        this.status = STATUS_COMPLETED;
        this.completedDate = new Date();
        this.completionNotes = notes;
        return true;
    }

    /**
     * Check if this goal can be cancelled
     */
    public boolean canCancel() {
        return STATUS_ACTIVE.equals(status);
    }

    /**
     * Cancel this goal
     */
    public boolean cancel() {
        if (!canCancel())
            return false;
        this.status = STATUS_CANCELLED;
        return true;
    }

    /**
     * Check if a user can delete this goal (only creator can delete)
     */
    public boolean canBeDeletedBy(User user) {
        if (user == null)
            return false;
        if (creator == null) {
            // Legacy goal - allow deletion by the employee
            return employee != null && employee.getUsername().equals(user.getUsername());
        }
        return creator.getUsername().equals(user.getUsername());
    }

    /**
     * Check if goal is active
     */
    public boolean isActive() {
        return STATUS_ACTIVE.equals(status);
    }

    /**
     * Check if goal is completed
     */
    public boolean isCompleted() {
        return STATUS_COMPLETED.equals(status);
    }

    /**
     * Get days since creation
     */
    public long getDaysSinceCreation() {
        long diff = new Date().getTime() - createdDate.getTime();
        return diff / (1000 * 60 * 60 * 24);
    }

    /**
     * Check if goal is overdue (active for more than 90 days)
     */
    public boolean isOverdue() {
        return isActive() && getDaysSinceCreation() > 90;
    }

    // ==================== Getters and Setters ====================

    public User getEmployee() {
        return employee;
    }

    public User getCreator() {
        return creator;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public Date getCompletedDate() {
        return completedDate;
    }

    public String getCompletionNotes() {
        return completionNotes;
    }

    @Override
    public String toString() {
        return description + " [" + status + "]";
    }
}
