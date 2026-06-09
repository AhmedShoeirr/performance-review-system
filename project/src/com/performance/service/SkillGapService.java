package com.performance.service;

import com.performance.model.*;
import com.performance.util.DataStore;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for skill gap analysis.
 * Compares review scores against thresholds to identify skill gaps.
 */
public class SkillGapService {
    private static SkillGapService instance;
    private static final double GAP_THRESHOLD = 3.0; // Score below this is a gap

    private SkillGapService() {
    }

    public static SkillGapService getInstance() {
        if (instance == null) {
            instance = new SkillGapService();
        }
        return instance;
    }

    /**
     * Represents a skill gap analysis result for a user
     */
    public static class SkillGapResult {
        public User user;
        public double punctuality;
        public double teamwork;
        public double productivity;
        public double leadership;
        public double vision;
        public double managerial;
        public boolean showAllMetrics; // true for managers, false for regular employees
        public List<String> gaps = new ArrayList<>();

        public SkillGapResult(User user, boolean showAllMetrics) {
            this.user = user;
            this.showAllMetrics = showAllMetrics;
        }

        public boolean hasGaps() {
            return !gaps.isEmpty();
        }

        public String getSummary() {
            if (gaps.isEmpty())
                return "No skill gaps identified";
            return String.join(", ", gaps);
        }
    }

    /**
     * Check if a user should show all metrics (is a manager or highboard)
     */
    private boolean shouldShowAllMetrics(User user) {
        if (user == null)
            return false;
        // Show all metrics if: has direct reports OR is at highest tier
        boolean hasReports = user.getDirectReports() != null && !user.getDirectReports().isEmpty();
        boolean isHighboard = user.getTierLevel() == DataStore.MAX_TIERS;
        return hasReports || isHighboard;
    }

    /**
     * Analyze skill gaps for a single user
     */
    public SkillGapResult analyzeUser(User user) {
        if (user == null)
            return null;

        boolean showAll = shouldShowAllMetrics(user);
        SkillGapResult result = new SkillGapResult(user, showAll);

        // Get aggregated scores from reviews
        double[] scores = ReviewService.getInstance().getAggregatedScores(user);
        // scores: [punctuality, teamwork, productivity, reviewCount]

        result.punctuality = scores[0];
        result.teamwork = scores[1];
        result.productivity = scores[2];

        // Check core metrics for gaps
        if (scores[3] > 0) { // Has reviews
            if (scores[0] < GAP_THRESHOLD)
                result.gaps.add("Punctuality");
            if (scores[1] < GAP_THRESHOLD)
                result.gaps.add("Teamwork");
            if (scores[2] < GAP_THRESHOLD)
                result.gaps.add("Productivity");
        }

        // For managers, get extended scores
        if (showAll) {
            double[] extScores = getExtendedScores(user);
            result.leadership = extScores[0];
            result.vision = extScores[1];
            result.managerial = extScores[2];

            if (extScores[3] > 0) { // Has extended reviews
                if (extScores[0] < GAP_THRESHOLD)
                    result.gaps.add("Leadership");
                if (extScores[1] < GAP_THRESHOLD)
                    result.gaps.add("Vision");
                if (extScores[2] < GAP_THRESHOLD)
                    result.gaps.add("Managerial Skills");
            }
        }

        return result;
    }

    /**
     * Get extended scores (leadership, vision, managerial) from reviews
     * Only counts reviews that actually have extended scores set
     */
    private double[] getExtendedScores(User user) {
        List<Review> reviews = DataStore.getInstance().getReviewsForUser(user);
        if (reviews.isEmpty()) {
            return new double[] { 0, 0, 0, 0 }; // [leadership, vision, managerial, count]
        }

        double totalLeadership = 0, totalVision = 0, totalManagerial = 0;
        int count = 0;

        for (Review r : reviews) {
            // Only count reviews that have extended scores set
            if (r.hasExtendedScores()) {
                totalLeadership += r.getLeadershipScore();
                totalVision += r.getVisionScore();
                totalManagerial += r.getManagerialSkillsScore();
                count++;
            }
        }

        if (count == 0) {
            return new double[] { 0, 0, 0, 0 };
        }

        return new double[] {
                totalLeadership / count,
                totalVision / count,
                totalManagerial / count,
                count
        };
    }

    /**
     * Analyze skill gaps for all team members (direct descendants)
     */
    public List<SkillGapResult> analyzeTeam(User manager) {
        List<SkillGapResult> results = new ArrayList<>();
        if (manager == null)
            return results;

        List<User> team = UserService.getInstance().getAllDescendants(manager);
        for (User member : team) {
            SkillGapResult result = analyzeUser(member);
            if (result != null) {
                results.add(result);
            }
        }
        return results;
    }

    /**
     * Analyze skill gaps for all users in the system (Admin only)
     */
    public List<SkillGapResult> analyzeAll() {
        List<SkillGapResult> results = new ArrayList<>();
        for (User user : DataStore.getInstance().getUsers()) {
            if (user instanceof Admin)
                continue;
            SkillGapResult result = analyzeUser(user);
            if (result != null) {
                results.add(result);
            }
        }
        return results;
    }

    /**
     * Get summary statistics for a team's skill gaps
     */
    public String getTeamGapSummary(User manager) {
        List<SkillGapResult> results = analyzeTeam(manager);
        if (results.isEmpty())
            return "No team members to analyze.";

        int withGaps = 0;
        int punctualityGaps = 0, teamworkGaps = 0, productivityGaps = 0;
        int leadershipGaps = 0, visionGaps = 0, managerialGaps = 0;

        for (SkillGapResult r : results) {
            if (r.hasGaps())
                withGaps++;
            for (String gap : r.gaps) {
                switch (gap) {
                    case "Punctuality":
                        punctualityGaps++;
                        break;
                    case "Teamwork":
                        teamworkGaps++;
                        break;
                    case "Productivity":
                        productivityGaps++;
                        break;
                    case "Leadership":
                        leadershipGaps++;
                        break;
                    case "Vision":
                        visionGaps++;
                        break;
                    case "Managerial Skills":
                        managerialGaps++;
                        break;
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Team Size: ").append(results.size()).append("\n");
        sb.append("Members with Gaps: ").append(withGaps).append("\n\n");
        sb.append("Gap Breakdown:\n");
        sb.append("  Punctuality: ").append(punctualityGaps).append(" members\n");
        sb.append("  Teamwork: ").append(teamworkGaps).append(" members\n");
        sb.append("  Productivity: ").append(productivityGaps).append(" members\n");
        if (leadershipGaps > 0 || visionGaps > 0 || managerialGaps > 0) {
            sb.append("  Leadership: ").append(leadershipGaps).append(" members\n");
            sb.append("  Vision: ").append(visionGaps).append(" members\n");
            sb.append("  Managerial: ").append(managerialGaps).append(" members\n");
        }

        return sb.toString();
    }
}
