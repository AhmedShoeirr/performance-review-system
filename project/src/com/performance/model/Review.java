package com.performance.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Review entity - represents a performance review of an employee.
 * Contains review-specific business logic.
 */
public class Review implements Serializable {
    private static final long serialVersionUID = 2L;

    // Status constants
    public static final String STATUS_DRAFT = "DRAFT";
    public static final String STATUS_SUBMITTED = "SUBMITTED";
    public static final String STATUS_ACKNOWLEDGED = "ACKNOWLEDGED";

    // Score range
    public static final int MIN_SCORE = 1;
    public static final int MAX_SCORE = 10;

    private User employee;
    private User reviewer;

    // Basic categories (all employees)
    private int punctualityScore;
    private int teamworkScore;
    private int productivityScore;

    // Extended categories (managers only)
    private int leadershipScore;
    private int visionScore;
    private int managerialSkillsScore;

    private String comments;
    private String status;
    private Date createdDate;

    public Review(User employee, User reviewer) {
        this.employee = employee;
        this.reviewer = reviewer;
        this.status = STATUS_SUBMITTED;
        this.createdDate = new Date();
    }

    // ==================== Business Logic ====================

    /**
     * Calculate total average score
     */
    public int calculateTotalScore() {
        if (hasExtendedScores()) {
            return (punctualityScore + teamworkScore + productivityScore +
                    leadershipScore + visionScore + managerialSkillsScore) / 6;
        }
        return (punctualityScore + teamworkScore + productivityScore) / 3;
    }

    /**
     * Calculate precise average (with decimals)
     */
    public double calculateAverageScore() {
        if (hasExtendedScores()) {
            return (punctualityScore + teamworkScore + productivityScore +
                    leadershipScore + visionScore + managerialSkillsScore) / 6.0;
        }
        return (punctualityScore + teamworkScore + productivityScore) / 3.0;
    }

    /**
     * Check if extended (manager) scores are used
     */
    public boolean hasExtendedScores() {
        return leadershipScore > 0 || visionScore > 0 || managerialSkillsScore > 0;
    }

    /**
     * Validate a score value
     */
    public static boolean isValidScore(int score) {
        return score >= MIN_SCORE && score <= MAX_SCORE;
    }

    /**
     * Check if this review is a draft
     */
    public boolean isDraft() {
        return STATUS_DRAFT.equals(status);
    }

    /**
     * Check if this review is submitted
     */
    public boolean isSubmitted() {
        return STATUS_SUBMITTED.equals(status);
    }

    /**
     * Submit the review (change from draft to submitted)
     */
    public boolean submit() {
        if (!isDraft())
            return false;
        this.status = STATUS_SUBMITTED;
        return true;
    }

    /**
     * Mark as acknowledged by employee
     */
    public void acknowledge() {
        this.status = STATUS_ACKNOWLEDGED;
    }

    /**
     * Check if a user can delete this review (only reviewer can delete)
     */
    public boolean canBeDeletedBy(User user) {
        if (user == null || reviewer == null)
            return false;
        return reviewer.getUsername().equals(user.getUsername());
    }

    /**
     * Get performance level based on average score
     */
    public String getPerformanceLevel() {
        double avg = calculateAverageScore();
        if (avg >= 9)
            return "Exceptional";
        if (avg >= 7)
            return "Exceeds Expectations";
        if (avg >= 5)
            return "Meets Expectations";
        if (avg >= 3)
            return "Needs Improvement";
        return "Below Expectations";
    }

    /**
     * Get days since review was created
     */
    public long getDaysSinceCreation() {
        long diff = new Date().getTime() - createdDate.getTime();
        return diff / (1000 * 60 * 60 * 24);
    }

    // ==================== Getters and Setters ====================

    public User getEmployee() {
        return employee;
    }

    public User getReviewer() {
        return reviewer;
    }

    public User getManager() {
        return reviewer;
    } // Backward compatibility

    public int getPunctualityScore() {
        return punctualityScore;
    }

    public void setPunctualityScore(int score) {
        this.punctualityScore = score;
    }

    public int getTeamworkScore() {
        return teamworkScore;
    }

    public void setTeamworkScore(int score) {
        this.teamworkScore = score;
    }

    public int getProductivityScore() {
        return productivityScore;
    }

    public void setProductivityScore(int score) {
        this.productivityScore = score;
    }

    public int getLeadershipScore() {
        return leadershipScore;
    }

    public void setLeadershipScore(int score) {
        this.leadershipScore = score;
    }

    public int getVisionScore() {
        return visionScore;
    }

    public void setVisionScore(int score) {
        this.visionScore = score;
    }

    public int getManagerialSkillsScore() {
        return managerialSkillsScore;
    }

    public void setManagerialSkillsScore(int score) {
        this.managerialSkillsScore = score;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    @Override
    public String toString() {
        return "Review for " + (employee != null ? employee.getName() : "Unknown") +
                " [Score: " + calculateTotalScore() + "/10]";
    }
}
