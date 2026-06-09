package com.performance.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * TrainingRecommendation entity - represents a training course recommended to
 * an employee.
 * Contains training-specific business logic.
 */
public class TrainingRecommendation implements Serializable {
    private static final long serialVersionUID = 2L;

    // Status constants
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_CANCELLED = "CANCELLED";

    private User recommender;
    private User recipient;
    private String courseName;
    private LocalDate dateRecommended;
    private String status;
    private LocalDate completedDate;

    public TrainingRecommendation(User recommender, User recipient, String courseName) {
        this.recommender = recommender;
        this.recipient = recipient;
        this.courseName = courseName;
        this.dateRecommended = LocalDate.now();
        this.status = STATUS_PENDING;
    }

    // ==================== Business Logic ====================

    /**
     * Check if a user can delete this recommendation (only recommender can delete)
     */
    public boolean canBeDeletedBy(User user) {
        if (user == null || recommender == null)
            return false;
        return recommender.getUsername().equals(user.getUsername());
    }

    /**
     * Check if training can be started
     */
    public boolean canStart() {
        return STATUS_PENDING.equals(status);
    }

    /**
     * Start the training
     */
    public boolean start() {
        if (!canStart())
            return false;
        this.status = STATUS_IN_PROGRESS;
        return true;
    }

    /**
     * Check if training can be completed
     */
    public boolean canComplete() {
        return STATUS_IN_PROGRESS.equals(status) || STATUS_PENDING.equals(status);
    }

    /**
     * Complete the training
     */
    public boolean complete() {
        if (!canComplete())
            return false;
        this.status = STATUS_COMPLETED;
        this.completedDate = LocalDate.now();
        return true;
    }

    /**
     * Cancel the training
     */
    public boolean cancel() {
        if (STATUS_COMPLETED.equals(status))
            return false;
        this.status = STATUS_CANCELLED;
        return true;
    }

    /**
     * Check if training is pending
     */
    public boolean isPending() {
        return STATUS_PENDING.equals(status);
    }

    /**
     * Check if training is in progress
     */
    public boolean isInProgress() {
        return STATUS_IN_PROGRESS.equals(status);
    }

    /**
     * Check if training is completed
     */
    public boolean isCompleted() {
        return STATUS_COMPLETED.equals(status);
    }

    /**
     * Get days since recommendation
     */
    public long getDaysSinceRecommended() {
        return ChronoUnit.DAYS.between(dateRecommended, LocalDate.now());
    }

    /**
     * Check if the recommendation is overdue (pending for more than 30 days)
     */
    public boolean isOverdue() {
        return isPending() && getDaysSinceRecommended() > 30;
    }

    // ==================== Getters and Setters ====================

    public User getRecommender() {
        return recommender;
    }

    public User getRecipient() {
        return recipient;
    }

    public String getCourseName() {
        return courseName;
    }

    public LocalDate getDateRecommended() {
        return dateRecommended;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getCompletedDate() {
        return completedDate;
    }

    public String getRecipientName() {
        return recipient != null ? recipient.getName() : "Unknown";
    }

    public String getRecommenderName() {
        return recommender != null ? recommender.getName() : "Unknown";
    }

    @Override
    public String toString() {
        return courseName + " (Recommended by: " + getRecommenderName() + " on " + dateRecommended + ")";
    }
}
