package com.performance.service;

import com.performance.model.*;
import com.performance.util.DataStore;
import com.performance.util.Logger;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for review operations.
 * Separates business logic from GUI concerns.
 */
public class ReviewService {
    private static ReviewService instance;
    private final DataStore dataStore;

    private ReviewService() {
        this.dataStore = DataStore.getInstance();
    }

    public static ReviewService getInstance() {
        if (instance == null) {
            instance = new ReviewService();
        }
        return instance;
    }

    /**
     * Create and save a new review
     */
    public Review createReview(User employee, User reviewer,
            int punctuality, int teamwork, int productivity,
            int leadership, int vision, int managerialSkills,
            String comments, boolean isDraft) {

        // Check for existing recent review (if not a draft)
        if (!isDraft && hasRecentReview(reviewer, employee)) {
            Logger.warn("Duplicate review blocked: " + reviewer.getUsername() + " -> " + employee.getUsername());
            return null; // Prevent duplicate submission
        }

        Review review = new Review(employee, reviewer);
        review.setPunctualityScore(punctuality);
        review.setTeamworkScore(teamwork);
        review.setProductivityScore(productivity);
        review.setLeadershipScore(leadership);
        review.setVisionScore(vision);
        review.setManagerialSkillsScore(managerialSkills);
        review.setComments(comments);
        review.setStatus(isDraft ? "DRAFT" : "SUBMITTED");

        dataStore.addReview(review);
        dataStore.saveData();
        Logger.info("Review " + (isDraft ? "saved as draft" : "submitted") + ": " + reviewer.getUsername() + " -> "
                + employee.getUsername());
        return review;
    }

    /**
     * Check if reviewer has submitted a review for employee in the last 30 days
     */
    public boolean hasRecentReview(User reviewer, User employee) {
        long thirtyDays = 30L * 24 * 60 * 60 * 1000;
        return dataStore.getReviews().stream()
                .anyMatch(r -> r.getReviewer().getUsername().equals(reviewer.getUsername()) &&
                        r.getEmployee().getUsername().equals(employee.getUsername()) &&
                        "SUBMITTED".equals(r.getStatus()) &&
                        (new java.util.Date().getTime() - r.getCreatedDate().getTime() < thirtyDays));
    }

    /**
     * Create a basic review with 3 criteria
     */
    public Review createBasicReview(User employee, User reviewer,
            int punctuality, int teamwork, int productivity, String comments) {
        return createReview(employee, reviewer, punctuality, teamwork, productivity,
                0, 0, 0, comments, false);
    }

    /**
     * Submit a draft review (mark as final)
     */
    public void submitReview(Review review) {
        review.setStatus("SUBMITTED");
        dataStore.saveData();
        Logger.info("Draft review submitted: " + review.getReviewer().getUsername() + " -> "
                + review.getEmployee().getUsername());
    }

    /**
     * Delete a review (only if requester created it)
     */
    public boolean deleteReview(Review review, User requester) {
        if (review.getReviewer() != null &&
                review.getReviewer().getUsername().equals(requester.getUsername())) {
            dataStore.removeReview(review);
            dataStore.saveData();
            Logger.info("Review deleted by " + requester.getUsername());
            return true;
        }
        return false;
    }

    /**
     * Force delete a review (admin only)
     */
    public void deleteReviewAdmin(Review review) {
        dataStore.removeReview(review);
        dataStore.saveData();
        Logger.info("Review deleted by Admin");
    }

    /**
     * Get all reviews for a user (reviews of that user)
     */
    public List<Review> getReviewsForUser(User user) {
        return dataStore.getReviewsForUser(user);
    }

    /**
     * Get all reviews created by a user
     */
    public List<Review> getReviewsGivenBy(User reviewer) {
        return dataStore.getReviews().stream()
                .filter(r -> r.getReviewer() != null &&
                        r.getReviewer().getUsername().equals(reviewer.getUsername()))
                .collect(Collectors.toList());
    }

    /**
     * Get all reviews in the system
     */
    public List<Review> getAllReviews() {
        return dataStore.getReviews();
    }

    /**
     * Create an upward review (employee reviewing their manager)
     */
    public UpwardReview createUpwardReview(User employee, User manager,
            int punctuality, int teamwork, int productivity, String comments) {

        if (hasRecentUpwardReview(employee, manager)) {
            Logger.warn("Duplicate upward review blocked: " + employee.getUsername() + " -> " + manager.getUsername());
            return null; // Prevent duplicate
        }

        UpwardReview ur = new UpwardReview(employee, manager,
                punctuality, teamwork, productivity, comments);
        dataStore.addUpwardReview(ur);
        dataStore.saveData();
        Logger.info("Upward review submitted: " + employee.getUsername() + " -> " + manager.getUsername());
        return ur;
    }

    public boolean hasRecentUpwardReview(User employee, User manager) {
        long thirtyDays = 30L * 24 * 60 * 60 * 1000;
        return dataStore.getUpwardReviews().stream()
                .anyMatch(r -> r.getReviewer().getUsername().equals(employee.getUsername()) &&
                        r.getManager().getUsername().equals(manager.getUsername()) &&
                        (new java.util.Date().getTime() - r.getCreatedDate().getTime() < thirtyDays));
    }

    /**
     * Get all upward reviews
     */
    public List<UpwardReview> getAllUpwardReviews() {
        return dataStore.getUpwardReviews();
    }

    /**
     * Get upward reviews for a specific manager
     */
    public List<UpwardReview> getUpwardReviewsForManager(User manager) {
        return dataStore.getUpwardReviews().stream()
                .filter(ur -> ur.getManager().getUsername().equals(manager.getUsername()))
                .collect(Collectors.toList());
    }

    // ==================== Aggregated Scores (Anonymous) ====================

    /**
     * Get aggregated review scores for a user (ANONYMOUS - no individual reviews)
     * Returns array: [avgPunctuality, avgTeamwork, avgProductivity, reviewCount]
     */
    public double[] getAggregatedScores(User user) {
        List<Review> reviews = getReviewsForUser(user).stream()
                .filter(r -> "SUBMITTED".equals(r.getStatus()))
                .collect(Collectors.toList());

        if (reviews.isEmpty()) {
            return new double[] { 0, 0, 0, 0 };
        }

        double sumPunctuality = 0, sumTeamwork = 0, sumProductivity = 0;
        for (Review r : reviews) {
            sumPunctuality += r.getPunctualityScore();
            sumTeamwork += r.getTeamworkScore();
            sumProductivity += r.getProductivityScore();
        }

        int count = reviews.size();
        return new double[] {
                sumPunctuality / count,
                sumTeamwork / count,
                sumProductivity / count,
                count
        };
    }

    /**
     * Get overall average score for a user (all manager reviews)
     */
    public double getOverallAverageScore(User user) {
        double[] scores = getAggregatedScores(user);
        if (scores[3] == 0)
            return 0;
        return (scores[0] + scores[1] + scores[2]) / 3.0;
    }

    /**
     * Get aggregated peer review scores for a user
     * Returns array: [avgCollaboration, avgCommunication, avgTeamwork, reviewCount]
     */
    public double[] getAggregatedPeerScores(User user) {
        List<PeerReview> reviews = dataStore.getPeerReviewsFor(user);

        if (reviews.isEmpty()) {
            return new double[] { 0, 0, 0, 0 };
        }

        double sumCollab = 0, sumComm = 0, sumTeam = 0;
        for (PeerReview r : reviews) {
            sumCollab += r.getCollaborationScore();
            sumComm += r.getCommunicationScore();
            sumTeam += r.getTeamworkScore();
        }

        int count = reviews.size();
        return new double[] {
                sumCollab / count,
                sumComm / count,
                sumTeam / count,
                count
        };
    }

    // ==================== Peer Reviews ====================

    /**
     * Create a peer review (teammate reviewing teammate)
     */
    public PeerReview createPeerReview(User reviewer, User reviewed,
            int collaboration, int communication, int teamwork, String comments) {
        // Validate they are peers
        if (!PeerReview.areValidPeers(reviewer, reviewed)) {
            return null;
        }

        if (hasRecentPeerReview(reviewer, reviewed)) {
            return null; // Prevent duplicate
        }

        PeerReview pr = new PeerReview(reviewer, reviewed, collaboration,
                communication, teamwork, comments);
        dataStore.addPeerReview(pr);
        dataStore.saveData();
        return pr;
    }

    public boolean hasRecentPeerReview(User reviewer, User reviewed) {
        long thirtyDays = 30L * 24 * 60 * 60 * 1000;
        return dataStore.getPeerReviews().stream()
                .anyMatch(r -> r.getReviewer().getUsername().equals(reviewer.getUsername()) &&
                        r.getReviewed().getUsername().equals(reviewed.getUsername()) &&
                        (new java.util.Date().getTime() - r.getCreatedDate().getTime() < thirtyDays));
    }

    /**
     * Get peer reviews for a user (anonymous - only scores)
     */
    public List<PeerReview> getPeerReviewsFor(User user) {
        return dataStore.getPeerReviewsFor(user);
    }

    /**
     * Get all peer reviews (for admin)
     */
    public List<PeerReview> getAllPeerReviews() {
        return dataStore.getPeerReviews();
    }

    // ==================== Reviews Given By User ====================

    /**
     * Get upward reviews given BY a specific user (to their manager)
     */
    public List<UpwardReview> getUpwardReviewsGivenBy(User reviewer) {
        return dataStore.getUpwardReviews().stream()
                .filter(ur -> ur.getReviewer() != null &&
                        ur.getReviewer().getUsername().equals(reviewer.getUsername()))
                .collect(Collectors.toList());
    }

    /**
     * Get peer reviews given BY a specific user (to their peers)
     */
    public List<PeerReview> getPeerReviewsGivenBy(User reviewer) {
        return dataStore.getPeerReviews().stream()
                .filter(pr -> pr.getReviewer() != null &&
                        pr.getReviewer().getUsername().equals(reviewer.getUsername()))
                .collect(Collectors.toList());
    }
}
