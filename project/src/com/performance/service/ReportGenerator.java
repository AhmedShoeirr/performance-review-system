package com.performance.service;

import com.performance.model.*;
import com.performance.util.DataStore;

import java.util.ArrayList;
import java.util.List;

public class ReportGenerator {

    /**
     * F-4.4: Top Performers Report
     * Shows a list of a manager's employees, sorted by total score
     */
    public String generateTopPerformersReport(User manager) {
        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════════\n");
        sb.append("       TOP PERFORMERS REPORT\n");
        sb.append("       Manager: ").append(manager.getName()).append("\n");
        sb.append("═══════════════════════════════════════════\n\n");

        List<User> team = DataStore.getInstance().getAllDescendants(manager);
        if (team.isEmpty()) {
            sb.append("No team members found.\n");
            return sb.toString();
        }

        // Calculate average scores for each team member
        List<UserScore> scores = new ArrayList<>();
        for (User emp : team) {
            List<Review> reviews = DataStore.getInstance().getReviewsForUser(emp);
            if (!reviews.isEmpty()) {
                double totalScore = 0;
                for (Review r : reviews) {
                    totalScore += r.calculateTotalScore();
                }
                double avgScore = totalScore / reviews.size();
                scores.add(new UserScore(emp, avgScore, reviews.size()));
            } else {
                scores.add(new UserScore(emp, 0, 0));
            }
        }

        // Sort by score descending
        scores.sort((a, b) -> Double.compare(b.score, a.score));

        int rank = 1;
        sb.append(String.format("%-5s %-25s %-10s %-10s\n", "Rank", "Employee", "Avg Score", "Reviews"));
        sb.append("───────────────────────────────────────────\n");
        for (UserScore us : scores) {
            sb.append(String.format("%-5d %-25s %-10.1f %-10d\n",
                    rank++, us.user.getName(), us.score, us.reviewCount));
        }

        return sb.toString();
    }

    /**
     * F-4.5: Year-End Summary Report
     * Shows all scores (for the 3 criteria) and comments for one employee
     */
    public String generateYearEndSummary(User employee) {
        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════════\n");
        sb.append("       YEAR-END SUMMARY REPORT\n");
        sb.append("       Employee: ").append(employee.getName()).append("\n");
        sb.append("═══════════════════════════════════════════\n\n");

        List<Review> reviews = DataStore.getInstance().getReviewsForUser(employee);
        if (reviews.isEmpty()) {
            sb.append("No reviews found for this employee.\n");
            return sb.toString();
        }

        // Aggregate scores
        double totalPunct = 0, totalTeam = 0, totalProd = 0;
        int count = 0;

        sb.append("INDIVIDUAL REVIEWS:\n");
        sb.append("───────────────────────────────────────────\n");

        for (Review r : reviews) {
            count++;
            sb.append("Review #").append(count).append(":\n");
            sb.append("  Punctuality:  ").append(r.getPunctualityScore()).append("/10\n");
            sb.append("  Teamwork:     ").append(r.getTeamworkScore()).append("/10\n");
            sb.append("  Productivity: ").append(r.getProductivityScore()).append("/10\n");
            sb.append("  Comments: ").append(r.getComments() != null ? r.getComments() : "None").append("\n\n");

            totalPunct += r.getPunctualityScore();
            totalTeam += r.getTeamworkScore();
            totalProd += r.getProductivityScore();
        }

        sb.append("───────────────────────────────────────────\n");
        sb.append("YEAR-END AVERAGES:\n");
        sb.append(String.format("  Punctuality:  %.1f / 10\n", totalPunct / count));
        sb.append(String.format("  Teamwork:     %.1f / 10\n", totalTeam / count));
        sb.append(String.format("  Productivity: %.1f / 10\n", totalProd / count));
        sb.append(String.format("  OVERALL:      %.1f / 10\n", (totalPunct + totalTeam + totalProd) / (3 * count)));

        return sb.toString();
    }

    /**
     * F-4.6: Skill Gap Analysis Report
     * Shows the average team score for each of the 3 criteria
     */
    public String generateSkillGapAnalysis(User manager) {
        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════════\n");
        sb.append("       SKILL GAP ANALYSIS\n");
        sb.append("       Team of: ").append(manager.getName()).append("\n");
        sb.append("═══════════════════════════════════════════\n\n");

        List<User> team = DataStore.getInstance().getAllDescendants(manager);
        if (team.isEmpty()) {
            sb.append("No team members found.\n");
            return sb.toString();
        }

        double totalPunct = 0, totalTeam = 0, totalProd = 0;
        int reviewCount = 0;

        for (User emp : team) {
            for (Review r : DataStore.getInstance().getReviewsForUser(emp)) {
                totalPunct += r.getPunctualityScore();
                totalTeam += r.getTeamworkScore();
                totalProd += r.getProductivityScore();
                reviewCount++;
            }
        }

        if (reviewCount == 0) {
            sb.append("No reviews found for team members.\n");
            return sb.toString();
        }

        double avgPunct = totalPunct / reviewCount;
        double avgTeam = totalTeam / reviewCount;
        double avgProd = totalProd / reviewCount;

        sb.append("TEAM AVERAGE SCORES:\n");
        sb.append("───────────────────────────────────────────\n");
        sb.append(String.format("  Punctuality:  %.1f / 10  %s\n", avgPunct, getSkillBar(avgPunct)));
        sb.append(String.format("  Teamwork:     %.1f / 10  %s\n", avgTeam, getSkillBar(avgTeam)));
        sb.append(String.format("  Productivity: %.1f / 10  %s\n", avgProd, getSkillBar(avgProd)));
        sb.append("\n");

        // Identify gaps (scores below 5)
        sb.append("SKILL GAP ANALYSIS:\n");
        sb.append("───────────────────────────────────────────\n");

        boolean hasGaps = false;
        if (avgPunct < 5) {
            sb.append("⚠ Punctuality needs improvement (").append(String.format("%.1f", avgPunct)).append(")\n");
            hasGaps = true;
        }
        if (avgTeam < 5) {
            sb.append("⚠ Teamwork needs improvement (").append(String.format("%.1f", avgTeam)).append(")\n");
            hasGaps = true;
        }
        if (avgProd < 5) {
            sb.append("⚠ Productivity needs improvement (").append(String.format("%.1f", avgProd)).append(")\n");
            hasGaps = true;
        }

        if (!hasGaps) {
            sb.append("✓ All skill areas are above threshold (5.0)\n");
        }

        sb.append("\nTotal reviews analyzed: ").append(reviewCount).append("\n");
        sb.append("Team size: ").append(team.size()).append("\n");

        return sb.toString();
    }

    private String getSkillBar(double score) {
        int filled = (int) score;
        StringBuilder bar = new StringBuilder("[");
        for (int i = 0; i < 10; i++) {
            bar.append(i < filled ? "█" : "░");
        }
        bar.append("]");
        return bar.toString();
    }

    /**
     * F-4.7: Upward Feedback Report
     * Shows aggregated/anonymous upward review data to Admins
     */
    public String generateUpwardFeedbackReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════════\n");
        sb.append("       UPWARD FEEDBACK REPORT\n");
        sb.append("       (Anonymous Aggregated Data)\n");
        sb.append("═══════════════════════════════════════════\n\n");

        List<UpwardReview> allUpwardReviews = DataStore.getInstance().getUpwardReviews();
        if (allUpwardReviews.isEmpty()) {
            sb.append("No upward reviews have been submitted yet.\n");
            return sb.toString();
        }

        // Group by manager
        java.util.Map<String, List<UpwardReview>> byManager = new java.util.HashMap<>();
        for (UpwardReview ur : allUpwardReviews) {
            String managerUsername = ur.getManager().getUsername();
            byManager.computeIfAbsent(managerUsername, k -> new ArrayList<>()).add(ur);
        }

        for (java.util.Map.Entry<String, List<UpwardReview>> entry : byManager.entrySet()) {
            List<UpwardReview> reviews = entry.getValue();
            User manager = reviews.get(0).getManager();

            sb.append("Manager: ").append(manager.getName()).append("\n");
            sb.append("───────────────────────────────────────────\n");

            double totalPunct = 0, totalTeam = 0, totalProd = 0;
            for (UpwardReview ur : reviews) {
                totalPunct += ur.getPunctualityScore();
                totalTeam += ur.getTeamworkScore();
                totalProd += ur.getProductivityScore();
            }

            int count = reviews.size();
            sb.append(String.format("  Avg Punctuality:  %.1f / 5\n", totalPunct / count));
            sb.append(String.format("  Avg Teamwork:     %.1f / 5\n", totalTeam / count));
            sb.append(String.format("  Avg Productivity: %.1f / 5\n", totalProd / count));
            sb.append("  Number of reviews: ").append(count).append("\n\n");

            // Show anonymous comments
            sb.append("  Anonymous Feedback:\n");
            for (int i = 0; i < reviews.size(); i++) {
                String comment = reviews.get(i).getComments();
                if (comment != null && !comment.trim().isEmpty()) {
                    sb.append("    • ").append(comment).append("\n");
                }
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    /**
     * Generate a summary report for admin dashboard
     */
    public static String generateSummaryReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════════\n");
        sb.append("       SYSTEM SUMMARY REPORT\n");
        sb.append("═══════════════════════════════════════════\n\n");

        DataStore ds = DataStore.getInstance();
        List<User> allUsers = ds.getUsers();
        List<Review> allReviews = ds.getReviews();

        // User stats
        long active = allUsers.stream().filter(u -> u.getStatus() == UserStatus.ACTIVE).count();
        long pending = allUsers.stream().filter(u -> u.getStatus() == UserStatus.PENDING).count();
        long suspended = allUsers.stream().filter(u -> u.getStatus() == UserStatus.SUSPENDED).count();

        sb.append("USER STATISTICS:\n");
        sb.append("───────────────────────────────────────────\n");
        sb.append("  Total Users:     ").append(allUsers.size()).append("\n");
        sb.append("  Active:          ").append(active).append("\n");
        sb.append("  Pending:         ").append(pending).append("\n");
        sb.append("  Suspended:       ").append(suspended).append("\n\n");

        // Review stats
        long submitted = allReviews.stream().filter(r -> "SUBMITTED".equals(r.getStatus())).count();
        long draft = allReviews.stream().filter(r -> "DRAFT".equals(r.getStatus())).count();

        sb.append("REVIEW STATISTICS:\n");
        sb.append("───────────────────────────────────────────\n");
        sb.append("  Total Reviews:   ").append(allReviews.size()).append("\n");
        sb.append("  Submitted:       ").append(submitted).append("\n");
        sb.append("  Draft:           ").append(draft).append("\n\n");

        // Training stats
        List<TrainingRecommendation> training = ds.getTrainingRecommendations();
        sb.append("TRAINING STATISTICS:\n");
        sb.append("───────────────────────────────────────────\n");
        sb.append("  Total Recommendations: ").append(training.size()).append("\n");
        sb.append("  Available Courses:     ").append(ds.getAvailableCourses().size()).append("\n");

        return sb.toString();
    }

    /**
     * Generate a training report for admin dashboard
     */
    public static String generateTrainingReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════════\n");
        sb.append("       TRAINING REPORT\n");
        sb.append("═══════════════════════════════════════════\n\n");

        DataStore ds = DataStore.getInstance();
        List<TrainingRecommendation> recs = ds.getTrainingRecommendations();
        List<String> courses = ds.getAvailableCourses();

        sb.append("AVAILABLE COURSES:\n");
        sb.append("───────────────────────────────────────────\n");
        for (String course : courses) {
            sb.append("  • ").append(course).append("\n");
        }
        sb.append("\n");

        sb.append("RECENT RECOMMENDATIONS:\n");
        sb.append("───────────────────────────────────────────\n");
        if (recs.isEmpty()) {
            sb.append("  No recommendations yet.\n");
        } else {
            for (TrainingRecommendation tr : recs) {
                sb.append("  ").append(tr.getRecipient().getName())
                        .append(" - ").append(tr.getCourseName())
                        .append(" (by ").append(tr.getRecommender() != null ? tr.getRecommender().getName() : "Admin")
                        .append(") [").append(tr.getStatus()).append("]\n");
            }
        }

        return sb.toString();
    }

    /**
     * Generate a team report for manager dashboard
     */
    public static String generateTeamReport(User manager) {
        ReportGenerator gen = new ReportGenerator();
        StringBuilder sb = new StringBuilder();
        sb.append(gen.generateTopPerformersReport(manager));
        sb.append("\n\n");
        sb.append(gen.generateSkillGapAnalysis(manager));
        return sb.toString();
    }

    // Helper class for sorting
    private static class UserScore {
        User user;
        double score;
        int reviewCount;

        UserScore(User user, double score, int reviewCount) {
            this.user = user;
            this.score = score;
            this.reviewCount = reviewCount;
        }
    }
}
