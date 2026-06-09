package com.performance.model;

import java.io.Serializable;
import java.util.Date;

/**
 * PeerReview entity - represents an anonymous review from a teammate.
 * Employees can only review peers under the same manager.
 */
public class PeerReview implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final int MIN_SCORE = 1;
    public static final int MAX_SCORE = 5;

    private User reviewer; // Employee giving review (kept anonymous)
    private User reviewed; // Teammate being reviewed
    private int collaborationScore;
    private int communicationScore;
    private int teamworkScore;
    private String comments;
    private Date createdDate;

    public PeerReview(User reviewer, User reviewed, int collaboration, int communication,
            int teamwork, String comments) {
        this.reviewer = reviewer;
        this.reviewed = reviewed;
        this.collaborationScore = validateScore(collaboration);
        this.communicationScore = validateScore(communication);
        this.teamworkScore = validateScore(teamwork);
        this.comments = comments;
        this.createdDate = new Date();
    }

    // ==================== Business Logic ====================

    private int validateScore(int score) {
        if (score < MIN_SCORE)
            return MIN_SCORE;
        if (score > MAX_SCORE)
            return MAX_SCORE;
        return score;
    }

    /**
     * Calculate average score
     */
    public double calculateAverageScore() {
        return (collaborationScore + communicationScore + teamworkScore) / 3.0;
    }

    /**
     * Check if reviewer and reviewed are valid peers (same manager)
     */
    public static boolean areValidPeers(User user1, User user2) {
        if (user1 == null || user2 == null)
            return false;
        if (user1.getManager() == null || user2.getManager() == null)
            return false;
        if (user1.getUsername().equals(user2.getUsername()))
            return false; // Can't review self
        return user1.getManager().getUsername().equals(user2.getManager().getUsername());
    }

    /**
     * Check if this review belongs to a specific user (anonymous - don't expose
     * reviewer)
     */
    public boolean isAbout(User user) {
        return reviewed != null && reviewed.getUsername().equals(user.getUsername());
    }

    // ==================== Getters (Reviewer is protected for anonymity)
    // ====================

    /**
     * Get reviewer - ONLY for admin use, should not be exposed to normal users
     */
    public User getReviewer() {
        return reviewer;
    }

    public User getReviewed() {
        return reviewed;
    }

    public int getCollaborationScore() {
        return collaborationScore;
    }

    public int getCommunicationScore() {
        return communicationScore;
    }

    public int getTeamworkScore() {
        return teamworkScore;
    }

    public String getComments() {
        return comments;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    @Override
    public String toString() {
        // Anonymous - don't show reviewer name
        return "Peer Review for " + (reviewed != null ? reviewed.getName() : "Unknown") +
                " [Score: " + String.format("%.1f", calculateAverageScore()) + "/5]";
    }
}
