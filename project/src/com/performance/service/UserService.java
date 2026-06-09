package com.performance.service;

import com.performance.model.*;
import com.performance.util.DataStore;
import com.performance.util.Logger;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for user management operations.
 * Separates business logic from GUI concerns.
 */
public class UserService {
    private static UserService instance;
    private final DataStore dataStore;

    private UserService() {
        this.dataStore = DataStore.getInstance();
    }

    public static UserService getInstance() {
        if (instance == null) {
            instance = new UserService();
        }
        return instance;
    }

    /**
     * Authenticate a user with username and password
     */
    public User authenticate(String username, String password) {
        User user = dataStore.authenticate(username, password);
        if (user != null) {
            Logger.info("User logged in: " + username + " (" + user.getStatus() + ")");
        } else {
            Logger.warn("Failed login attempt for username: " + username);
        }
        return user;
    }

    /**
     * Register a new user (sets status to PENDING)
     */
    public boolean registerUser(String username, String password, String name) {
        // Check if username exists
        for (User u : dataStore.getUsers()) {
            if (u.getUsername().equals(username)) {
                Logger.warn("Registration failed: Username '" + username + "' already taken.");
                return false; // Username already taken
            }
        }

        Employee newUser = new Employee(username, password, name);
        newUser.setStatus(UserStatus.PENDING);
        newUser.setTierLevel(1);
        dataStore.addUser(newUser);
        dataStore.saveData();
        Logger.info("New user registered: " + username + " (Pending Approval)");
        return true;
    }

    /**
     * Get all users
     */
    public List<User> getAllUsers() {
        return dataStore.getUsers();
    }

    /**
     * Get all non-admin users
     */
    public List<User> getNonAdminUsers() {
        return dataStore.getUsers().stream()
                .filter(u -> !(u instanceof Admin))
                .collect(Collectors.toList());
    }

    /**
     * Get pending users for approval
     */
    public List<User> getPendingUsers() {
        return dataStore.getUsers().stream()
                .filter(u -> u.getStatus() == UserStatus.PENDING)
                .collect(Collectors.toList());
    }

    /**
     * Approve (activate) a pending user
     */
    public void approveUser(User user) {
        user.setStatus(UserStatus.ACTIVE);
        dataStore.saveData();
        Logger.info("User approved: " + user.getUsername());
    }

    /**
     * Suspend a user account
     */
    public boolean suspendUser(User user) {
        if (user instanceof Admin) {
            return false; // Cannot suspend admin
        }
        if (user.getDirectReports() != null && !user.getDirectReports().isEmpty()) {
            Logger.warn("Failed to suspend " + user.getUsername() + ": Has direct reports.");
            return false; // Cannot suspend manager with reports
        }
        user.setStatus(UserStatus.SUSPENDED);
        dataStore.saveData();
        Logger.info("User suspended: " + user.getUsername());
        return true;
    }

    /**
     * Reactivate a suspended user
     */
    public void activateUser(User user) {
        user.setStatus(UserStatus.ACTIVE);
        dataStore.saveData();
        Logger.info("User reactivated: " + user.getUsername());
    }

    /**
     * Toggle user status between ACTIVE/PENDING and SUSPENDED
     * Suspended users return to PENDING (only hierarchy assignment activates)
     */
    public boolean toggleUserStatus(User user) {
        if (user instanceof Admin) {
            return false;
        }
        if (user.getStatus() != UserStatus.SUSPENDED) {
            if (user.getDirectReports() != null && !user.getDirectReports().isEmpty()) {
                Logger.warn("Failed to suspend " + user.getUsername() + ": Has direct reports.");
                return false;
            }
            user.setStatus(UserStatus.SUSPENDED);
            Logger.info("User suspended: " + user.getUsername());
        } else {
            // Return to PENDING, not ACTIVE - only hierarchy assignment activates
            user.setStatus(UserStatus.PENDING);
            Logger.info("User set to pending: " + user.getUsername());
        }
        dataStore.saveData();
        return true;
    }

    /**
     * Edit user's name
     */
    public void editUserName(User user, String newName) {
        user.setName(newName);
        dataStore.saveData();
    }

    /**
     * Delete a user
     */
    public boolean deleteUser(User user) {
        if (user instanceof Admin) {
            return false;
        }

        // Remove from manager's direct reports
        User manager = user.getManager();
        if (manager != null && manager.getDirectReports() != null) {
            manager.getDirectReports().remove(user);
        }

        // Reassign direct reports to null
        if (user.getDirectReports() != null) {
            for (User report : user.getDirectReports()) {
                report.setManager(null);
            }
        }

        dataStore.getUsers().remove(user);
        dataStore.saveData();
        return true;
    }

    /**
     * Find user by username
     */
    public User findByUsername(String username) {
        for (User u : dataStore.getUsers()) {
            if (u.getUsername().equals(username)) {
                return u;
            }
        }
        return null;
    }

    /**
     * Assign an employee to a manager with a specific tier level
     */
    public void assignHierarchy(User employee, User manager, int tierLevel) {
        // Check for circular dependency
        if (manager != null) {
            if (employee.getUsername().equals(manager.getUsername())) {
                // Cannot report to self
                return;
            }
            // Check if manager is already a descendant of employee
            List<User> descendants = getAllDescendants(employee);
            if (descendants.contains(manager)) {
                // Cycle detected! Cannot assign employee to one of their own subordinates
                return;
            }
        }

        // Remove from old manager
        User oldManager = employee.getManager();
        if (oldManager != null && oldManager.getDirectReports() != null) {
            oldManager.getDirectReports().remove(employee);
        }

        // Assign new manager
        employee.setManager(manager);
        employee.setTierLevel(tierLevel);
        employee.setStatus(UserStatus.ACTIVE);

        if (manager != null) {
            manager.addDirectReport(employee);
        }

        dataStore.saveData();
    }

    /**
     * Get all descendants (direct and indirect reports) of a manager
     */
    public List<User> getAllDescendants(User manager) {
        return dataStore.getAllDescendants(manager);
    }

    /**
     * Get chain of command for a user
     */
    public List<User> getChainOfCommand(User user) {
        return dataStore.getChainOfCommand(user);
    }

    /**
     * Get managers (users with direct reports)
     */
    public List<User> getManagers() {
        return dataStore.getUsers().stream()
                .filter(u -> u.getDirectReports() != null && !u.getDirectReports().isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * Calculate role string for a user
     */
    public String calculateRole(User user) {
        if (user instanceof Admin)
            return "Admin";
        int tier = user.getTierLevel();
        boolean hasReports = user.getDirectReports() != null && !user.getDirectReports().isEmpty();
        User mgr = user.getManager();

        int maxTiers = DataStore.MAX_TIERS;
        if (tier == maxTiers)
            return "Highboard Manager";
        if (hasReports && mgr != null)
            return "Manager & Employee";
        if (hasReports)
            return "Manager";
        return "Employee";
    }

    /**
     * Count active employees at a specific tier level
     */
    public int countEmployeesAtTier(int tierLevel) {
        return (int) dataStore.getUsers().stream()
                .filter(u -> !(u instanceof Admin))
                .filter(u -> u.getStatus() == UserStatus.ACTIVE)
                .filter(u -> u.getTierLevel() == tierLevel)
                .count();
    }

    /**
     * Check if an employee can be assigned to a specific tier.
     * For non-highest tiers, there must be at least one active employee in ALL
     * upper tiers.
     */
    public boolean canAssignToTier(int tierLevel) {
        int maxTiers = DataStore.MAX_TIERS;

        // Highest tier is always allowed (highboard managers)
        if (tierLevel == maxTiers) {
            return true;
        }

        // For all other tiers, check that each upper tier has at least one employee
        for (int tier = tierLevel + 1; tier <= maxTiers; tier++) {
            if (countEmployeesAtTier(tier) == 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Get a message explaining why tier assignment is blocked
     */
    public String getTierBlockedReason(int tierLevel) {
        int maxTiers = DataStore.MAX_TIERS;

        if (tierLevel == maxTiers) {
            return null; // Always allowed
        }

        StringBuilder missing = new StringBuilder();
        for (int tier = tierLevel + 1; tier <= maxTiers; tier++) {
            if (countEmployeesAtTier(tier) == 0) {
                if (missing.length() > 0)
                    missing.append(", ");
                missing.append(tier);
            }
        }

        if (missing.length() > 0) {
            return "Need at least one employee in tier(s): " + missing.toString();
        }
        return null;
    }

    /**
     * Check if an employee's tier can be changed.
     * Blocked if: employee is only one at tier OR employee has direct reports.
     */
    public boolean canChangeTier(User employee, int newTier) {
        if (employee == null)
            return false;

        int currentTier = employee.getTierLevel();

        // If not changing tier, always allowed
        if (currentTier == newTier) {
            return true;
        }

        // Block if employee has direct reports (is a manager)
        if (employee.getDirectReports() != null && !employee.getDirectReports().isEmpty()) {
            return false;
        }

        // Block if employee is the only active one at their current tier
        // (changing would leave that tier empty)
        if (employee.getStatus() == UserStatus.ACTIVE && countEmployeesAtTier(currentTier) == 1) {
            return false;
        }

        return true;
    }

    /**
     * Get a message explaining why tier change is blocked
     */
    public String getTierChangeBlockedReason(User employee, int newTier) {
        if (employee == null)
            return "No employee selected";

        int currentTier = employee.getTierLevel();

        if (currentTier == newTier) {
            return null;
        }

        if (employee.getDirectReports() != null && !employee.getDirectReports().isEmpty()) {
            return "Cannot change tier: Employee is a manager with direct reports";
        }

        if (employee.getStatus() == UserStatus.ACTIVE && countEmployeesAtTier(currentTier) == 1) {
            return "Cannot change tier: Only employee at Tier " + currentTier;
        }

        return null;
    }

    /**
     * Validate hierarchy assignment - checks all rules
     * Returns null if valid, error message if invalid
     */
    public String validateHierarchyAssignment(User employee, User manager, int tierLevel) {
        int maxTiers = DataStore.MAX_TIERS;

        // Check tier assignment rule
        String tierBlockedReason = getTierBlockedReason(tierLevel);
        if (tierBlockedReason != null) {
            return tierBlockedReason;
        }

        // Check tier change restriction
        String tierChangeReason = getTierChangeBlockedReason(employee, tierLevel);
        if (tierChangeReason != null) {
            return tierChangeReason;
        }

        // For non-highest tier, manager is required
        if (tierLevel < maxTiers && manager == null) {
            return "Manager is required for Tier " + tierLevel;
        }

        return null; // Valid
    }

    /**
     * Reset all non-admin users to PENDING status
     */
    public void resetSystem() {
        for (User u : dataStore.getUsers()) {
            if (!(u instanceof Admin)) {
                u.setStatus(UserStatus.PENDING);
                u.setManager(null);
                u.getDirectReports().clear();
                u.setTierLevel(User.TIER_UNASSIGNED); // Tier 0 for unassigned
            }
        }
        dataStore.saveData();
    }

    /**
     * Get max tiers setting
     */
    public int getMaxTiers() {
        return DataStore.MAX_TIERS;
    }

    /**
     * Set max tiers setting.
     * When changed, all non-admin users are reset to PENDING and tier 1.
     */
    public void setMaxTiers(int tiers) {
        int oldTiers = DataStore.MAX_TIERS;

        // Only reset users if the tier count actually changed
        if (tiers != oldTiers) {
            DataStore.MAX_TIERS = tiers;

            // Reset all non-admin users to PENDING and tier 1
            for (User user : dataStore.getUsers()) {
                if (!(user instanceof Admin)) {
                    user.setStatus(UserStatus.PENDING);
                    user.setTierLevel(1);
                    user.setManager(null);

                    // Clear direct reports
                    if (user.getDirectReports() != null) {
                        user.getDirectReports().clear();
                    }
                }
            }

            Logger.info("Max tiers changed from " + oldTiers + " to " + tiers + ". All users reset to PENDING.");
        }

        dataStore.saveData();
    }

    /**
     * Reset all employees to PENDING status and tier 1.
     * Clears all manager assignments.
     */
    public void resetAllEmployees() {
        for (User user : dataStore.getUsers()) {
            if (!(user instanceof Admin)) {
                user.setStatus(UserStatus.PENDING);
                user.setTierLevel(1);
                user.setManager(null);

                // Clear direct reports
                if (user.getDirectReports() != null) {
                    user.getDirectReports().clear();
                }
            }
        }

        Logger.info("All employees reset to PENDING status.");
        dataStore.saveData();
    }

    /**
     * Update user role based on current tier and direct reports
     */
    public void updateUserRole(User user) {
        if (user instanceof Admin)
            return;

        int tier = user.getTierLevel();
        boolean hasReports = user.getDirectReports() != null && !user.getDirectReports().isEmpty();

        // Update tier logic based on direct reports
        if (hasReports && tier < 2) {
            user.setTierLevel(2); // Managers must be at least tier 2
        }

        dataStore.saveData();
    }

    /**
     * Get peers of a user (others with same manager)
     */
    public List<User> getPeers(User user) {
        return user.getPeers();
    }

    /**
     * Get team members for a manager
     */
    public List<User> getTeamMembers(User manager) {
        return dataStore.getTeamMembers(manager);
    }
}
