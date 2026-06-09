package com.performance.service;

import com.performance.model.*;
import com.performance.util.DataStore;
import com.performance.util.Logger;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for training recommendation operations.
 * Separates business logic from GUI concerns.
 */
public class TrainingService {
    private static TrainingService instance;
    private final DataStore dataStore;

    private TrainingService() {
        this.dataStore = DataStore.getInstance();
    }

    public static TrainingService getInstance() {
        if (instance == null) {
            instance = new TrainingService();
        }
        return instance;
    }

    /**
     * Get all available courses
     */
    public List<String> getAvailableCourses() {
        return dataStore.getAvailableCourses();
    }

    /**
     * Add a new course
     */
    public boolean addCourse(String courseName) {
        if (courseName == null || courseName.trim().isEmpty()) {
            return false;
        }
        if (dataStore.getAvailableCourses().contains(courseName.trim())) {
            Logger.warn("Course addition failed: '" + courseName + "' already exists.");
            return false; // Course already exists
        }
        dataStore.addCourse(courseName.trim());
        dataStore.saveData();
        Logger.info("Course added: " + courseName);
        return true;
    }

    /**
     * Remove a course
     */
    public void removeCourse(String courseName) {
        dataStore.removeCourse(courseName);
        dataStore.saveData();
        Logger.info("Course removed: " + courseName);
    }

    /**
     * Create a training recommendation
     */
    public TrainingRecommendation createRecommendation(User recommender, User recipient, String courseName) {
        // Permission check:
        // 1. Admin is allowed
        // 2. Manager assigning to subordinate is allowed

        boolean isAdmin = recommender instanceof Admin;
        boolean isManager = false;

        if (!isAdmin) {
            // Check if recommender is a manager of recipient
            List<User> descendants = UserService.getInstance().getAllDescendants(recommender);
            isManager = descendants.stream()
                    .anyMatch(u -> u.getUsername().equals(recipient.getUsername()));
        }

        if (!isAdmin && !isManager) {
            Logger.warn("Training recommendation denied: " + recommender.getUsername() + " tried to assign to "
                    + recipient.getUsername());
            return null; // Permission denied
        }

        TrainingRecommendation tr = new TrainingRecommendation(recommender, recipient, courseName);
        dataStore.addTrainingRecommendation(tr);
        dataStore.saveData();
        Logger.info("Training recommended: '" + courseName + "' for " + recipient.getUsername() + " by "
                + recommender.getUsername());
        return tr;
    }

    /**
     * Delete a recommendation (only if requester created it)
     */
    public boolean deleteRecommendation(TrainingRecommendation recommendation, User requester) {
        if (recommendation.getRecommender() != null &&
                recommendation.getRecommender().getUsername().equals(requester.getUsername())) {
            dataStore.removeTrainingRecommendation(recommendation);
            dataStore.saveData();
            Logger.info("Training recommendation deleted by " + requester.getUsername());
            return true;
        }
        return false;
    }

    /**
     * Get all training recommendations
     */
    public List<TrainingRecommendation> getAllRecommendations() {
        return dataStore.getTrainingRecommendations();
    }

    /**
     * Get recommendations for a specific user (recipient)
     */
    public List<TrainingRecommendation> getRecommendationsFor(User recipient) {
        return dataStore.getTrainingRecommendations().stream()
                .filter(tr -> tr.getRecipient() != null &&
                        tr.getRecipient().getUsername().equals(recipient.getUsername()))
                .collect(Collectors.toList());
    }

    /**
     * Get recommendations given by a specific user
     */
    public List<TrainingRecommendation> getRecommendationsBy(User recommender) {
        return dataStore.getTrainingRecommendations().stream()
                .filter(tr -> tr.getRecommender() != null &&
                        tr.getRecommender().getUsername().equals(recommender.getUsername()))
                .collect(Collectors.toList());
    }
}
