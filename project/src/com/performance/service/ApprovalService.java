package com.performance.service;

import com.performance.model.*;
import com.performance.util.DataStore;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing pending actions that require admin approval.
 * Handles tier changes, suspensions, and deletions requested by managers.
 */
public class ApprovalService {
    private static ApprovalService instance;

    private ApprovalService() {
    }

    public static synchronized ApprovalService getInstance() {
        if (instance == null) {
            instance = new ApprovalService();
        }
        return instance;
    }

    // ==================== Request Creation ====================

    /**
     * Create a suspension request
     * Rules:
     * 1. Requester must be manager of target
     * 2. Target must have a peer (another employee under same manager)
     * 3. Target must NOT have direct reports (can't suspend a manager with
     * employees)
     */
    public PendingAction requestSuspension(User requester, User target) {
        // Validate requester is manager of target
        if (!isManagerOf(requester, target)) {
            return null;
        }
        // Check target has no direct reports
        if (hasDirectReports(target)) {
            return null;
        }
        // Check target has a peer (at least one other employee under same manager)
        if (!hasPeer(target)) {
            return null;
        }

        PendingAction action = new PendingAction(requester, target,
                PendingAction.ACTION_SUSPEND, null);
        DataStore.getInstance().addPendingAction(action);
        return action;
    }

    /**
     * Create a tier change request
     * Rules:
     * 1. Requester must be manager of target
     * 2. New tier must be valid (between 1 and requester's tier - 1)
     * 3. Target must NOT have direct reports (can't change tier of manager with
     * employees)
     */
    public PendingAction requestTierChange(User requester, User target, int newTier) {
        // Validate requester is manager of target
        if (!isManagerOf(requester, target)) {
            return null;
        }
        // Check target has no direct reports
        if (hasDirectReports(target)) {
            return null;
        }
        // Validate new tier is valid
        if (newTier < 1 || newTier >= requester.getTierLevel()) {
            return null;
        }

        PendingAction action = new PendingAction(requester, target,
                PendingAction.ACTION_CHANGE_TIER, newTier);
        DataStore.getInstance().addPendingAction(action);
        return action;
    }

    /**
     * Create a deletion request
     * Rules:
     * 1. Requester must be manager of target
     * 2. Target must have a peer
     * 3. Target must NOT have direct reports
     */
    public PendingAction requestDeletion(User requester, User target) {
        // Validate requester is manager of target
        if (!isManagerOf(requester, target)) {
            return null;
        }
        // Check target has no direct reports
        if (hasDirectReports(target)) {
            return null;
        }
        // Check target has a peer
        if (!hasPeer(target)) {
            return null;
        }

        PendingAction action = new PendingAction(requester, target,
                PendingAction.ACTION_DELETE, null);
        DataStore.getInstance().addPendingAction(action);
        return action;
    }

    // ==================== Admin Actions ====================

    /**
     * Get all pending requests (for admin)
     */
    public List<PendingAction> getPendingRequests() {
        return DataStore.getInstance().getPendingActions().stream()
                .filter(PendingAction::isPending)
                .collect(Collectors.toList());
    }

    /**
     * Get all requests (for admin history view)
     */
    public List<PendingAction> getAllRequests() {
        return new ArrayList<>(DataStore.getInstance().getPendingActions());
    }

    /**
     * Approve a pending request
     */
    public boolean approveRequest(PendingAction action, User admin) {
        if (action == null || !action.isPending())
            return false;
        if (!(admin instanceof Admin))
            return false;

        // Execute the action
        User target = action.getTargetUser();
        if (target == null)
            return false;

        switch (action.getActionType()) {
            case PendingAction.ACTION_SUSPEND:
                target.setStatus(UserStatus.SUSPENDED);
                break;
            case PendingAction.ACTION_CHANGE_TIER:
                int newTier = (Integer) action.getNewValue();
                target.setTierLevel(newTier);
                UserService.getInstance().updateUserRole(target);
                break;
            case PendingAction.ACTION_DELETE:
                UserService.getInstance().deleteUser(target);
                break;
            default:
                return false;
        }

        action.approve(admin);
        DataStore.getInstance().saveData();
        return true;
    }

    /**
     * Reject a pending request
     */
    public boolean rejectRequest(PendingAction action, User admin, String reason) {
        if (action == null || !action.isPending())
            return false;
        if (!(admin instanceof Admin))
            return false;

        action.reject(admin, reason);
        DataStore.getInstance().saveData();
        return true;
    }

    // ==================== Request Query ====================

    /**
     * Get requests made by a specific user
     */
    public List<PendingAction> getRequestsByUser(User user) {
        if (user == null || user.getUsername() == null) {
            return new ArrayList<>();
        }
        return DataStore.getInstance().getPendingActions().stream()
                .filter(a -> a.getRequestedBy() != null &&
                        user.getUsername().equals(a.getRequestedBy().getUsername()))
                .collect(Collectors.toList());
    }

    // ==================== Validation Helpers ====================

    /**
     * Check if requester is a manager of target (directly or indirectly)
     */
    private boolean isManagerOf(User requester, User target) {
        if (requester == null || target == null)
            return false;

        // Check direct management
        User manager = target.getManager();
        while (manager != null) {
            if (manager.getUsername().equals(requester.getUsername())) {
                return true;
            }
            manager = manager.getManager();
        }
        return false;
    }

    /**
     * Check if target has at least one peer (another employee with same manager)
     */
    private boolean hasPeer(User target) {
        if (target.getManager() == null)
            return false;
        List<User> siblings = target.getManager().getDirectReports();
        return siblings != null && siblings.size() > 1;
    }

    /**
     * Check if target has direct reports (is a manager with employees)
     */
    private boolean hasDirectReports(User target) {
        if (target == null)
            return false;
        List<User> reports = target.getDirectReports();
        return reports != null && !reports.isEmpty();
    }
}
