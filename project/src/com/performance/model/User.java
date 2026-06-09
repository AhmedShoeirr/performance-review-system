package com.performance.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public abstract class User implements Serializable {
    private static final long serialVersionUID = 2L;

    // Tier constants
    public static final int TIER_UNASSIGNED = 0; // Unassigned/Suspended users
    public static final int TIER_EMPLOYEE = 1; // Lowest active tier

    protected String username;
    protected String password;
    protected String name;
    protected UserStatus status;
    protected UserRole role;
    protected int tierLevel;
    protected int maxDirectReports = 3;
    protected User manager;
    protected List<User> directReports = new ArrayList<>();

    public User(String username, String password, String name, UserRole role) {
        this.username = username;
        this.password = password;
        this.name = name;
        this.role = role;
        this.status = UserStatus.PENDING; // Default to PENDING until assigned
        this.tierLevel = TIER_UNASSIGNED; // Default to tier 0 (unassigned)
    }

    // ==================== Business Logic ====================

    /**
     * Check if this user is assigned (has a valid tier > 0 and is ACTIVE)
     */
    public boolean isAssigned() {
        return tierLevel > TIER_UNASSIGNED && status == UserStatus.ACTIVE;
    }

    /**
     * Check if this user is a manager (has direct reports)
     */
    public boolean isManager() {
        return directReports != null && !directReports.isEmpty();
    }

    /**
     * Get peers (other users with the same manager)
     */
    public List<User> getPeers() {
        List<User> peers = new ArrayList<>();
        if (manager != null && manager.getDirectReports() != null) {
            for (User peer : manager.getDirectReports()) {
                if (!peer.getUsername().equals(this.username)) {
                    peers.add(peer);
                }
            }
        }
        return peers;
    }

    /**
     * Check if another user is a peer (same manager, different person)
     */
    public boolean isPeer(User other) {
        if (other == null || manager == null)
            return false;
        if (other.getUsername().equals(this.username))
            return false;
        if (other.getManager() == null)
            return false;
        return manager.getUsername().equals(other.getManager().getUsername());
    }

    /**
     * Check if this user can review another user (is their manager or peer)
     */
    public boolean canReview(User target) {
        if (target == null)
            return false;
        // Can review if target is a direct report
        if (directReports != null && directReports.contains(target))
            return true;
        // Can peer review if same manager
        return isPeer(target);
    }

    // ==================== Getters and Setters ====================

    public String getUsername() {
        return username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean checkPassword(String inputPassword) {
        return this.password.equals(inputPassword);
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
        // If suspended/pending, reset to tier 0
        if (status == UserStatus.SUSPENDED || status == UserStatus.PENDING) {
            this.tierLevel = TIER_UNASSIGNED;
        }
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public int getTierLevel() {
        return tierLevel;
    }

    public void setTierLevel(int tierLevel) {
        this.tierLevel = tierLevel;
    }

    public int getMaxDirectReports() {
        return maxDirectReports;
    }

    public void setMaxDirectReports(int maxDirectReports) {
        this.maxDirectReports = maxDirectReports;
    }

    public User getManager() {
        return manager;
    }

    public void setManager(User manager) {
        this.manager = manager;
    }

    public List<User> getDirectReports() {
        if (directReports == null) {
            directReports = new ArrayList<>();
        }
        return directReports;
    }

    public void setDirectReports(List<User> reports) {
        this.directReports = reports;
    }

    public void addDirectReport(User u) {
        if (directReports == null) {
            directReports = new ArrayList<>();
        }
        if (!directReports.contains(u)) {
            directReports.add(u);
        }
    }
}
