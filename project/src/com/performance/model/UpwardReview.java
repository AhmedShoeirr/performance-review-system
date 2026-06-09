package com.performance.model;

import java.io.Serializable;
import java.util.Date;

/**
 * UpwardReview entity - represents an anonymous review of a manager by their
 * direct report.
 * Contains upward review-specific business logic.
 */
public class UpwardReview implements Serializable {
    private static final long serialVersionUID = 2L;

    // Score range (1-5 for upward reviews)
    public static final int MIN_SCORE = 1;
    public static final int MAX_SCORE = 5;

    private User reviewer; // Employee giving the review (kept private for anonymity)
    private User manager; // Manager being reviewed

    // Using the same 3 standard criteria as regular reviews
    private int punctualityScore;
    private int teamworkScore;
    private int productivityScore;

    private String comments;
    private Date createdDate;

    public UpwardReview(User reviewer, User manager, int punctualityScore, int teamworkScore,
            int productivityScore, String comments) {
        this.reviewer = reviewer;
        this.manager = manager;
        this.punctualityScore = validateScore(punctualityScore);
        this.teamworkScore = validateScore(teamworkScore);
        this.productivityScore = validateScore(productivityScore);
        this.comments = comments;
        this.createdDate = new Date();
    }

    // ==================== Business Logic ====================

    /**
     * Validate and clamp a score to valid range
     */
    private int validateScore(int score) {
        if (score < MIN_SCORE)
            return MIN_SCORE;
        if (score > MAX_SCORE)
            return MAX_SCORE;
        return score;
    }

    /**
     * Check if a score is valid
     */
    public static boolean isValidScore(int score) {
        return score >= MIN_SCORE && score <= MAX_SCORE;
    }

    /**
     * Calculate total average score
     */
    public int calculateTotalScore() {
        return (punctualityScore + teamworkScore + productivityScore) / 3;
    }

    /**
     * Calculate precise average score
     */
    public double calculateAverageScore() {
        return (punctualityScore + teamworkScore + productivityScore) / 3.0;
    }

    /**
     * Get feedback rating (out of 5)
     */
    public String getFeedbackRating() {
        double avg = calculateAverageScore();
        if (avg >= 4.5)
            return "Excellent";
        if (avg >= 3.5)
            return "Good";
        if (avg >= 2.5)
            return "Average";
        if (avg >= 1.5)
            return "Below Average";
        return "Poor";
    }

    /**
     * Check if review has comments
     */
    public boolean hasComments() {
        return comments != null && !comments.trim().isEmpty();
    }

    /**
     * Get days since review was created
     */
    public long getDaysSinceCreation() {
        long diff = new Date().getTime() - createdDate.getTime();
        return diff / (1000 * 60 * 60 * 24);
    }

    /**
     * Check if this review is recent (within 30 days)
     */
    public boolean isRecent() {
        return getDaysSinceCreation() <= 30;
    }

    // ==================== Getters ====================
    // Note: No setter methods to ensure review immutability after creation
    // Note: Reviewer is accessible but should be hidden in reports for anonymity

    public int getPunctualityScore() {
        return punctualityScore;
    }

    public int getTeamworkScore() {
        return teamworkScore;
    }

    public int getProductivityScore() {
        return productivityScore;
    }

    public User getReviewer() {
        return reviewer; // Note: Should not be exposed in UI for anonymity
    }

    public User getManager() {
        return manager;
    }

    public String getComments() {
        return comments;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    @Override
    public String toString() {
        return "Upward Review for " + (manager != null ? manager.getName() : "Unknown") +
                " [Score: " + calculateTotalScore() + "/5]";
    }
}
