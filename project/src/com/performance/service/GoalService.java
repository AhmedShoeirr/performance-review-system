package com.performance.service;

import com.performance.model.*;
import com.performance.util.DataStore;
import com.performance.util.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for goal operations.
 * Separates business logic from GUI concerns.
 */
public class GoalService {
    private static GoalService instance;
    private final DataStore dataStore;

    private GoalService() {
        this.dataStore = DataStore.getInstance();
    }

    public static GoalService getInstance() {
        if (instance == null) {
            instance = new GoalService();
        }
        return instance;
    }

    /**
     * Create a new goal
     */
    public Goal createGoal(User employee, String description, User creator) {
        // Permission check:
        // 1. Self-assignment is allowed
        // 2. Manager assigning to subordinate is allowed
        // 3. Admin is allowed (handled by caller usually, but good to have)

        boolean isSelf = employee.getUsername().equals(creator.getUsername());
        boolean isAdmin = creator instanceof Admin;
        boolean isManager = false;

        if (!isSelf && !isAdmin) {
            // Check if creator is a manager of employee
            List<User> descendants = UserService.getInstance().getAllDescendants(creator);
            // We need to check by username to be safe with object references
            isManager = descendants.stream()
                    .anyMatch(u -> u.getUsername().equals(employee.getUsername()));
        }

        if (!isSelf && !isAdmin && !isManager) {
            Logger.warn("Goal creation denied: " + creator.getUsername() + " tried to assign goal to "
                    + employee.getUsername());
            return null; // Permission denied
        }

        Goal goal = new Goal(employee, description, creator);
        dataStore.addGoal(goal);
        dataStore.saveData();
        Logger.info("Goal created for " + employee.getUsername() + " by " + creator.getUsername());
        return goal;
    }

    /**
     * Delete a goal (only if requester created it, or if no creator)
     */
    public boolean deleteGoal(Goal goal, User requester) {
        if (goal.getCreator() == null) {
            // Legacy goal, allow deletion if it's the employee's goal
            if (goal.getEmployee() != null &&
                    goal.getEmployee().getUsername().equals(requester.getUsername())) {
                dataStore.removeGoal(goal);
                dataStore.saveData();
                Logger.info("Goal deleted by " + requester.getUsername());
                return true;
            }
        }
        if (goal.getCreator() != null &&
                goal.getCreator().getUsername().equals(requester.getUsername())) {
            dataStore.removeGoal(goal);
            dataStore.saveData();
            Logger.info("Goal deleted by " + requester.getUsername());
            return true;
        }
        return false;
    }

    /**
     * Update goal status
     */
    public void updateGoalStatus(Goal goal, String status) {
        goal.setStatus(status);
        dataStore.saveData();
        Logger.info("Goal status updated to '" + status + "' for " + goal.getEmployee().getUsername());
    }

    /**
     * Mark goal as complete
     */
    public void completeGoal(Goal goal) {
        goal.setStatus("COMPLETED");
        dataStore.saveData();
        Logger.info("Goal completed for " + goal.getEmployee().getUsername());
    }

    /**
     * Get all goals
     */
    public List<Goal> getAllGoals() {
        return dataStore.getGoals();
    }

    /**
     * Get goals for a specific user (assigned to them)
     */
    public List<Goal> getGoalsForUser(User user) {
        return dataStore.getGoals().stream()
                .filter(g -> g.getEmployee() != null &&
                        g.getEmployee().getUsername().equals(user.getUsername()))
                .collect(Collectors.toList());
    }

    /**
     * Get goals created by a specific user
     */
    public List<Goal> getGoalsCreatedBy(User creator) {
        return dataStore.getGoals().stream()
                .filter(g -> g.getCreator() != null &&
                        g.getCreator().getUsername().equals(creator.getUsername()))
                .collect(Collectors.toList());
    }

    /**
     * Get all goals for a team (all descendants of a manager)
     */
    public List<Goal> getTeamGoals(User manager) {
        List<User> team = DataStore.getInstance().getAllDescendants(manager);
        List<Goal> teamGoals = new ArrayList<>();

        for (User emp : team) {
            teamGoals.addAll(getGoalsForUser(emp));
        }
        return teamGoals;
    }

    /**
     * Admin-only delete (bypasses ownership check)
     */
    public void deleteGoalAdmin(Goal goal) {
        dataStore.removeGoal(goal);
        dataStore.saveData();
        Logger.info("Goal deleted by Admin");
    }
}
