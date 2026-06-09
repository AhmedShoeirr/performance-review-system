package com.performance.service;

import com.performance.model.*;
import com.performance.util.DataStore;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for identifying top performers across the organization.
 * Supports system-wide, team, and hierarchy analysis.
 */
public class TopPerformersService {
    private static TopPerformersService instance;
    private static final int DEFAULT_TOP_COUNT = 3;

    private TopPerformersService() {
    }

    public static TopPerformersService getInstance() {
        if (instance == null) {
            instance = new TopPerformersService();
        }
        return instance;
    }

    /**
     * Represents a performer with their score
     */
    public static class PerformerResult {
        public User user;
        public double score;
        public int reviewCount;
        public int rank;

        public PerformerResult(User user, double score, int reviewCount, int rank) {
            this.user = user;
            this.score = score;
            this.reviewCount = reviewCount;
            this.rank = rank;
        }
    }

    /**
     * Represents top performers for a specific scope (system, team, or
     * sub-hierarchy)
     */
    public static class TeamPerformers {
        public String teamName;
        public User manager; // null for system-wide
        public List<PerformerResult> topPerformers;
        public int totalMembers;

        public TeamPerformers(String teamName, User manager, List<PerformerResult> topPerformers, int totalMembers) {
            this.teamName = teamName;
            this.manager = manager;
            this.topPerformers = topPerformers;
            this.totalMembers = totalMembers;
        }
    }

    /**
     * Get top N performers from a list of users
     */
    private List<PerformerResult> getTopPerformers(List<User> users, int topCount) {
        // Calculate scores and sort
        List<PerformerResult> results = new ArrayList<>();
        for (User user : users) {
            // Calculate score and review count in one pass
            List<Review> reviews = DataStore.getInstance().getReviewsForUser(user);
            int reviewCount = reviews.size();
            double score = 0;
            if (!reviews.isEmpty()) {
                double total = 0;
                for (Review r : reviews) {
                    total += r.calculateAverageScore();
                }
                score = total / reviewCount;
            }
            results.add(new PerformerResult(user, score, reviewCount, 0));
        }

        // Sort by score descending
        results.sort((a, b) -> Double.compare(b.score, a.score));

        // Assign ranks and limit to top count
        List<PerformerResult> topResults = new ArrayList<>();
        for (int i = 0; i < Math.min(topCount, results.size()); i++) {
            PerformerResult r = results.get(i);
            r.rank = i + 1;
            topResults.add(r);
        }

        return topResults;
    }

    /**
     * Get top performers system-wide (Admin only)
     */
    public TeamPerformers getSystemTopPerformers(int topCount) {
        List<User> allUsers = DataStore.getInstance().getUsers().stream()
                .filter(u -> !(u instanceof Admin))
                .filter(u -> u.getStatus() == UserStatus.ACTIVE)
                .collect(Collectors.toList());

        List<PerformerResult> top = getTopPerformers(allUsers, topCount);
        return new TeamPerformers("System-Wide Top Performers", null, top, allUsers.size());
    }

    /**
     * Get top performers for a manager's direct team
     */
    public TeamPerformers getDirectTeamTopPerformers(User manager, int topCount) {
        List<User> directReports = manager.getDirectReports();
        if (directReports == null || directReports.isEmpty()) {
            return new TeamPerformers("Direct Reports of " + manager.getName(), manager,
                    new ArrayList<>(), 0);
        }

        List<PerformerResult> top = getTopPerformers(directReports, topCount);
        return new TeamPerformers("Direct Reports of " + manager.getName(), manager, top, directReports.size());
    }

    /**
     * Get top performers for a manager's full hierarchy
     */
    public TeamPerformers getHierarchyTopPerformers(User manager, int topCount) {
        List<User> hierarchy = UserService.getInstance().getAllDescendants(manager);
        if (hierarchy == null || hierarchy.isEmpty()) {
            return new TeamPerformers("Hierarchy of " + manager.getName(), manager,
                    new ArrayList<>(), 0);
        }

        List<PerformerResult> top = getTopPerformers(hierarchy, topCount);
        return new TeamPerformers("Full Hierarchy of " + manager.getName(), manager, top, hierarchy.size());
    }

    /**
     * Get comprehensive top performers analysis for Admin
     * Returns: system-wide top 3, plus top 3 for each manager's hierarchy
     */
    public List<TeamPerformers> getAdminTopPerformersAnalysis() {
        List<TeamPerformers> results = new ArrayList<>();

        // System-wide top performers
        results.add(getSystemTopPerformers(DEFAULT_TOP_COUNT));

        // Get all managers (users with direct reports or highboard)
        List<User> managers = UserService.getInstance().getNonAdminUsers().stream()
                .filter(u -> u.getStatus() == UserStatus.ACTIVE)
                .filter(u -> (u.getDirectReports() != null && !u.getDirectReports().isEmpty())
                        || u.getTierLevel() == DataStore.MAX_TIERS)
                .collect(Collectors.toList());

        // Sort managers by tier (highest first)
        managers.sort((a, b) -> Integer.compare(b.getTierLevel(), a.getTierLevel()));

        // Top performers for each manager's team
        for (User manager : managers) {
            TeamPerformers teamTop = getHierarchyTopPerformers(manager, DEFAULT_TOP_COUNT);
            if (teamTop.totalMembers > 0) {
                results.add(teamTop);
            }
        }

        return results;
    }

    /**
     * Get comprehensive top performers analysis for a Manager
     * Returns: own hierarchy top 3, plus top 3 for each sub-manager's team
     */
    public List<TeamPerformers> getManagerTopPerformersAnalysis(User manager) {
        List<TeamPerformers> results = new ArrayList<>();

        // Own full hierarchy top performers
        results.add(getHierarchyTopPerformers(manager, DEFAULT_TOP_COUNT));

        // Own direct reports top performers (if different from hierarchy)
        if (manager.getDirectReports() != null && manager.getDirectReports().size() > 0) {
            results.add(getDirectTeamTopPerformers(manager, DEFAULT_TOP_COUNT));
        }

        // Find sub-managers in own hierarchy
        List<User> hierarchy = UserService.getInstance().getAllDescendants(manager);
        List<User> subManagers = hierarchy.stream()
                .filter(u -> u.getDirectReports() != null && !u.getDirectReports().isEmpty())
                .collect(Collectors.toList());

        // Top performers for each sub-manager's team
        for (User subMgr : subManagers) {
            TeamPerformers subTeamTop = getHierarchyTopPerformers(subMgr, DEFAULT_TOP_COUNT);
            if (subTeamTop.totalMembers > 0) {
                results.add(subTeamTop);
            }
        }

        return results;
    }
}
