package com.performance.service;

import com.performance.model.*;
import com.performance.util.DataStore;

import java.util.*;

/**
 * Calculates weighted performance scores based on user role.
 * 
 * Scoring rules:
 * - Employee: 70% manager + 30% peer
 * - Manager_Employee: 40% subordinate + 20% peer + 40% upper manager
 * - Highboard: 100% admin (peer scores are advisory only)
 * 
 * If a category has no scores, its percentage is redistributed equally.
 */
public class PerformanceCalculator {

    public static class ScoreResult {
        public double punctuality;
        public double teamwork;
        public double productivity;
        public double overall;
        public String breakdown;
        public List<String> missingCategories = new ArrayList<>();

        public ScoreResult(double punct, double team, double prod, String breakdown) {
            this.punctuality = punct;
            this.teamwork = team;
            this.productivity = prod;
            this.overall = (punct + team + prod) / 3.0;
            this.breakdown = breakdown;
        }
    }

    /**
     * Calculate weighted score for a user based on their role.
     */
    public static ScoreResult calculateScore(User user) {
        int tier = user.getTierLevel();
        boolean hasDirectReports = user.getDirectReports() != null && !user.getDirectReports().isEmpty();

        // Determine role category
        if (tier == DataStore.MAX_TIERS) {
            return calculateHighboardScore(user);
        } else if (tier == 1 && !hasDirectReports) {
            return calculateEmployeeScore(user);
        } else {
            return calculateManagerEmployeeScore(user);
        }
    }

    /**
     * Employee: 70% manager + 30% peer
     */
    private static ScoreResult calculateEmployeeScore(User user) {
        List<Review> reviews = getReviewsFor(user);

        List<Review> managerReviews = new ArrayList<>();
        List<Review> peerReviews = new ArrayList<>();

        for (Review r : reviews) {
            User reviewer = r.getManager();
            if (reviewer != null) {
                if (reviewer.equals(user.getManager())) {
                    managerReviews.add(r);
                } else if (reviewer.getTierLevel() == user.getTierLevel()) {
                    peerReviews.add(r);
                }
            }
        }

        // Calculate weights with redistribution
        double managerWeight = 0.70;
        double peerWeight = 0.30;
        List<String> missing = new ArrayList<>();

        if (managerReviews.isEmpty() && peerReviews.isEmpty()) {
            return new ScoreResult(0, 0, 0, "No reviews available");
        }

        if (managerReviews.isEmpty()) {
            missing.add("Manager");
            peerWeight = 1.0;
            managerWeight = 0;
        } else if (peerReviews.isEmpty()) {
            missing.add("Peers");
            managerWeight = 1.0;
            peerWeight = 0;
        }

        double[] managerAvg = averageScores(managerReviews);
        double[] peerAvg = averageScores(peerReviews);

        double punct = managerAvg[0] * managerWeight + peerAvg[0] * peerWeight;
        double team = managerAvg[1] * managerWeight + peerAvg[1] * peerWeight;
        double prod = managerAvg[2] * managerWeight + peerAvg[2] * peerWeight;

        StringBuilder breakdown = new StringBuilder();
        breakdown.append("Manager (").append((int) (managerWeight * 100)).append("%): ");
        breakdown.append(managerReviews.size()).append(" reviews\n");
        breakdown.append("Peers (").append((int) (peerWeight * 100)).append("%): ");
        breakdown.append(peerReviews.size()).append(" reviews");
        if (!missing.isEmpty()) {
            breakdown.append("\n* Missing: ").append(String.join(", ", missing));
        }

        ScoreResult result = new ScoreResult(punct, team, prod, breakdown.toString());
        result.missingCategories = missing;
        return result;
    }

    /**
     * Manager_Employee: 40% subordinate + 20% peer + 40% upper manager
     */
    private static ScoreResult calculateManagerEmployeeScore(User user) {
        List<Review> reviews = getReviewsFor(user);

        List<Review> subordinateReviews = new ArrayList<>();
        List<Review> peerReviews = new ArrayList<>();
        List<Review> upperManagerReviews = new ArrayList<>();

        Set<String> directReportUsernames = new HashSet<>();
        if (user.getDirectReports() != null) {
            for (User dr : user.getDirectReports()) {
                directReportUsernames.add(dr.getUsername());
            }
        }

        for (Review r : reviews) {
            User reviewer = r.getManager();
            if (reviewer != null) {
                if (directReportUsernames.contains(reviewer.getUsername())) {
                    subordinateReviews.add(r);
                } else if (reviewer.getTierLevel() == user.getTierLevel()) {
                    peerReviews.add(r);
                } else if (reviewer.getTierLevel() > user.getTierLevel()) {
                    upperManagerReviews.add(r);
                }
            }
        }

        // Calculate weights with redistribution
        double subWeight = 0.40;
        double peerWeight = 0.20;
        double upperWeight = 0.40;
        List<String> missing = new ArrayList<>();
        int activeCategories = 3;

        if (subordinateReviews.isEmpty()) {
            missing.add("Subordinates");
            activeCategories--;
            subWeight = 0;
        }
        if (peerReviews.isEmpty()) {
            missing.add("Peers");
            activeCategories--;
            peerWeight = 0;
        }
        if (upperManagerReviews.isEmpty()) {
            missing.add("Upper Managers");
            activeCategories--;
            upperWeight = 0;
        }

        if (activeCategories == 0) {
            return new ScoreResult(0, 0, 0, "No reviews available");
        }

        // Redistribute weights
        double totalWeight = subWeight + peerWeight + upperWeight;
        if (totalWeight > 0 && totalWeight < 1.0) {
            double factor = 1.0 / totalWeight;
            subWeight *= factor;
            peerWeight *= factor;
            upperWeight *= factor;
        }

        double[] subAvg = averageScores(subordinateReviews);
        double[] peerAvg = averageScores(peerReviews);
        double[] upperAvg = averageScores(upperManagerReviews);

        double punct = subAvg[0] * subWeight + peerAvg[0] * peerWeight + upperAvg[0] * upperWeight;
        double team = subAvg[1] * subWeight + peerAvg[1] * peerWeight + upperAvg[1] * upperWeight;
        double prod = subAvg[2] * subWeight + peerAvg[2] * peerWeight + upperAvg[2] * upperWeight;

        StringBuilder breakdown = new StringBuilder();
        breakdown.append("Subordinates (").append((int) (subWeight * 100)).append("%): ");
        breakdown.append(subordinateReviews.size()).append(" reviews\n");
        breakdown.append("Peers (").append((int) (peerWeight * 100)).append("%): ");
        breakdown.append(peerReviews.size()).append(" reviews\n");
        breakdown.append("Upper Managers (").append((int) (upperWeight * 100)).append("%): ");
        breakdown.append(upperManagerReviews.size()).append(" reviews");
        if (!missing.isEmpty()) {
            breakdown.append("\n* Missing: ").append(String.join(", ", missing));
        }

        ScoreResult result = new ScoreResult(punct, team, prod, breakdown.toString());
        result.missingCategories = missing;
        return result;
    }

    /**
     * Highboard: 100% admin (peer scores are advisory only)
     */
    private static ScoreResult calculateHighboardScore(User user) {
        List<Review> reviews = getReviewsFor(user);

        List<Review> adminReviews = new ArrayList<>();
        List<Review> advisoryReviews = new ArrayList<>();

        for (Review r : reviews) {
            User reviewer = r.getManager();
            if (reviewer instanceof Admin) {
                adminReviews.add(r);
            } else {
                advisoryReviews.add(r);
            }
        }

        if (adminReviews.isEmpty()) {
            StringBuilder sb = new StringBuilder("No admin reviews available");
            if (!advisoryReviews.isEmpty()) {
                sb.append("\nAdvisory reviews from lower tiers: ").append(advisoryReviews.size());
            }
            ScoreResult result = new ScoreResult(0, 0, 0, sb.toString());
            result.missingCategories.add("Admin");
            return result;
        }

        double[] adminAvg = averageScores(adminReviews);

        StringBuilder breakdown = new StringBuilder();
        breakdown.append("Admin (100%): ").append(adminReviews.size()).append(" reviews");
        if (!advisoryReviews.isEmpty()) {
            breakdown.append("\nAdvisory (not counted): ").append(advisoryReviews.size()).append(" reviews");
        }

        return new ScoreResult(adminAvg[0], adminAvg[1], adminAvg[2], breakdown.toString());
    }

    /**
     * Get all reviews for a user.
     */
    private static List<Review> getReviewsFor(User user) {
        List<Review> result = new ArrayList<>();
        for (Review r : DataStore.getInstance().getReviews()) {
            if (r.getEmployee() != null &&
                    r.getEmployee().getUsername().equals(user.getUsername())) {
                result.add(r);
            }
        }
        return result;
    }

    /**
     * Calculate average scores from a list of reviews.
     * Returns [punctuality, teamwork, productivity]
     */
    private static double[] averageScores(List<Review> reviews) {
        if (reviews.isEmpty()) {
            return new double[] { 0, 0, 0 };
        }

        double sumPunct = 0, sumTeam = 0, sumProd = 0;
        for (Review r : reviews) {
            sumPunct += r.getPunctualityScore();
            sumTeam += r.getTeamworkScore();
            sumProd += r.getProductivityScore();
        }

        int count = reviews.size();
        return new double[] { sumPunct / count, sumTeam / count, sumProd / count };
    }

    /**
     * Get advisory reviews for a highboard manager (peer reviews that don't count).
     */
    public static List<Review> getAdvisoryReviews(User highboardManager) {
        List<Review> result = new ArrayList<>();
        for (Review r : DataStore.getInstance().getReviews()) {
            if (r.getEmployee() != null &&
                    r.getEmployee().getUsername().equals(highboardManager.getUsername()) &&
                    !(r.getManager() instanceof Admin)) {
                result.add(r);
            }
        }
        return result;
    }
}
