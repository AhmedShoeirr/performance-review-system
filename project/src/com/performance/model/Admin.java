package com.performance.model;

import com.performance.util.DataStore;

/**
 * Admin entity - represents system administrator with full privileges.
 * Admin is at tier MAX_TIERS + 1 (owner level, above all).
 */
public class Admin extends User {
    private static final long serialVersionUID = 2L;

    public Admin(String username, String password, String name) {
        super(username, password, name, UserRole.ADMIN);
        // Admin is always active and at the highest tier (MAX + 1)
        this.status = UserStatus.ACTIVE;
        this.tierLevel = DataStore.MAX_TIERS + 1;
    }

    /**
     * Get admin tier level (always MAX+1)
     */
    @Override
    public int getTierLevel() {
        return DataStore.MAX_TIERS + 1;
    }

    /**
     * Check if a user can be approved (must be pending)
     */
    public boolean canApprove(User user) {
        return user != null &&
                user.getStatus() == UserStatus.PENDING &&
                !(user instanceof Admin);
    }

    /**
     * Approve a pending user - returns true if successful
     */
    public boolean approveUser(User user) {
        if (!canApprove(user)) {
            return false;
        }
        user.setStatus(UserStatus.ACTIVE);
        return true;
    }

    /**
     * Check if a user can be suspended
     */
    public boolean canSuspend(User user) {
        if (user == null || user instanceof Admin)
            return false;
        if (user.getStatus() == UserStatus.SUSPENDED)
            return false;

        // Can't suspend users with direct reports
        return user.getDirectReports() == null || user.getDirectReports().isEmpty();
    }

    /**
     * Suspend a user - returns true if successful
     */
    public boolean suspendUser(User user) {
        if (!canSuspend(user)) {
            return false;
        }
        user.setStatus(UserStatus.SUSPENDED);
        return true;
    }

    /**
     * Reactivate a suspended user
     */
    public boolean reactivateUser(User user) {
        if (user == null || user instanceof Admin)
            return false;
        if (user.getStatus() != UserStatus.SUSPENDED)
            return false;

        user.setStatus(UserStatus.ACTIVE);
        return true;
    }

    /**
     * Check if a user can be deleted
     */
    public boolean canDelete(User user) {
        if (user == null || user instanceof Admin)
            return false;
        // Users with direct reports should have them reassigned first
        return true;
    }

    /**
     * Check if admin can assign an employee to a manager at a specific tier
     */
    public boolean canAssignHierarchy(User employee, User manager, int tier) {
        if (employee == null)
            return false;
        if (employee instanceof Admin)
            return false;
        if (tier < 1)
            return false;

        // If manager is null, employee goes to tier 1 as unassigned
        if (manager == null)
            return tier == 1;

        // Employee tier must be less than manager tier
        return tier < manager.getTierLevel();
    }

    /**
     * Assign an employee to a manager in the hierarchy
     * Returns true if successful
     */
    public boolean assignToHierarchy(User employee, User manager, int tier) {
        if (!canAssignHierarchy(employee, manager, tier)) {
            return false;
        }

        // Remove from old manager
        User oldManager = employee.getManager();
        if (oldManager != null && oldManager.getDirectReports() != null) {
            oldManager.getDirectReports().remove(employee);
        }

        // Assign to new manager
        employee.setManager(manager);
        employee.setTierLevel(tier);
        employee.setStatus(UserStatus.ACTIVE);

        if (manager != null) {
            manager.addDirectReport(employee);
        }

        return true;
    }

    /**
     * Validate tier configuration
     */
    public boolean isValidTierConfig(int tier, int maxTiers) {
        return tier >= 1 && tier <= maxTiers;
    }

    /**
     * Check if can modify max tiers setting
     */
    public boolean canModifyMaxTiers(int newMaxTiers, int currentHighestTier) {
        // Can't set max tiers below the current highest tier in use
        return newMaxTiers >= currentHighestTier && newMaxTiers >= 1;
    }

    @Override
    public String toString() {
        return "Admin: " + getUsername();
    }
}
