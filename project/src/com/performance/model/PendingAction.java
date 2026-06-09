package com.performance.model;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * PendingAction entity - represents a manager's request that requires admin
 * approval.
 * Actions like tier changes, suspensions, deletions require admin approval.
 */
public class PendingAction implements Serializable {
    private static final long serialVersionUID = 1L;

    // Action types
    public static final String ACTION_SUSPEND = "SUSPEND";
    public static final String ACTION_CHANGE_TIER = "CHANGE_TIER";
    public static final String ACTION_DELETE = "DELETE";

    // Status types
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_REJECTED = "REJECTED";

    private String id;
    private User requestedBy; // Manager who made the request
    private User targetUser; // User being affected
    private String actionType; // SUSPEND, CHANGE_TIER, DELETE
    private Object newValue; // New tier level, etc.
    private Date requestedDate;
    private String status;
    private String rejectionReason; // If rejected, why
    private User processedBy; // Admin who approved/rejected
    private Date processedDate;

    public PendingAction(User requestedBy, User targetUser, String actionType, Object newValue) {
        this.id = UUID.randomUUID().toString();
        this.requestedBy = requestedBy;
        this.targetUser = targetUser;
        this.actionType = actionType;
        this.newValue = newValue;
        this.requestedDate = new Date();
        this.status = STATUS_PENDING;
    }

    // ==================== Business Logic ====================

    /**
     * Check if this action is still pending
     */
    public boolean isPending() {
        return STATUS_PENDING.equals(status);
    }

    /**
     * Approve the action
     */
    public void approve(User admin) {
        this.status = STATUS_APPROVED;
        this.processedBy = admin;
        this.processedDate = new Date();
    }

    /**
     * Reject the action
     */
    public void reject(User admin, String reason) {
        this.status = STATUS_REJECTED;
        this.processedBy = admin;
        this.processedDate = new Date();
        this.rejectionReason = reason;
    }

    /**
     * Get description of the action for display
     */
    public String getDescription() {
        String targetName = targetUser != null ? targetUser.getName() : "Unknown";
        switch (actionType) {
            case ACTION_SUSPEND:
                return "Suspend " + targetName;
            case ACTION_CHANGE_TIER:
                return "Change " + targetName + " to Tier " + newValue;
            case ACTION_DELETE:
                return "Delete " + targetName;
            default:
                return actionType + " on " + targetName;
        }
    }

    // ==================== Getters ====================

    public String getId() {
        return id;
    }

    public User getRequestedBy() {
        return requestedBy;
    }

    public User getTargetUser() {
        return targetUser;
    }

    public String getActionType() {
        return actionType;
    }

    public Object getNewValue() {
        return newValue;
    }

    public Date getRequestedDate() {
        return requestedDate;
    }

    public String getStatus() {
        return status;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public User getProcessedBy() {
        return processedBy;
    }

    public Date getProcessedDate() {
        return processedDate;
    }

    @Override
    public String toString() {
        return getDescription() + " [" + status + "]";
    }
}
