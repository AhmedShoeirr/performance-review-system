package com.performance.service;

import com.performance.model.*;
import com.performance.util.DataStore;

import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.application.Platform;
import java.awt.Desktop;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Generates performance reports that can be saved as HTML (printable to PDF).
 */
public class ReportExporter {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    /**
     * Export a user's performance report to HTML file.
     */
    public static void exportUserReport(User user, Window owner) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export Performance Report");
        chooser.setInitialFileName(user.getUsername() + "_report.html");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("HTML Files", "*.html"));

        File file = chooser.showSaveDialog(owner);
        if (file != null) {
            if (!file.getName().endsWith(".html")) {
                file = new File(file.getAbsolutePath() + ".html");
            }

            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                writer.println(generateUserReportHTML(user));

                final File finalFile = file;
                showInfoAlert("Export Complete",
                        "Report saved to:\n" + finalFile.getAbsolutePath() +
                                "\n\nOpen in browser and use Print -> Save as PDF");

                // Try to open in browser
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().browse(finalFile.toURI());
                }
            } catch (Exception e) {
                showErrorAlert("Export Failed", "Error: " + e.getMessage());
            }
        }
    }

    /**
     * Export all users' summary report (Admin only).
     */
    public static void exportAllUsersReport(Window owner) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export All Users Report");
        chooser.setInitialFileName("performance_report_all.html");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("HTML Files", "*.html"));

        File file = chooser.showSaveDialog(owner);
        if (file != null) {
            if (!file.getName().endsWith(".html")) {
                file = new File(file.getAbsolutePath() + ".html");
            }

            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                writer.println(generateAllUsersReportHTML());

                final File finalFile = file;
                showInfoAlert("Export Complete",
                        "Report saved! Open in browser and Print -> Save as PDF");

                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().browse(finalFile.toURI());
                }
            } catch (Exception e) {
                showErrorAlert("Export Failed", "Error: " + e.getMessage());
            }
        }
    }

    /**
     * Export team hierarchy report (for managers).
     */
    public static void exportTeamReport(User manager, Window owner) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export Team Report");
        chooser.setInitialFileName(manager.getUsername() + "_team_report.html");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("HTML Files", "*.html"));

        File file = chooser.showSaveDialog(owner);
        if (file != null) {
            if (!file.getName().endsWith(".html")) {
                file = new File(file.getAbsolutePath() + ".html");
            }

            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                writer.println(generateTeamReportHTML(manager));

                final File finalFile = file;
                showInfoAlert("Export Complete",
                        "Team report saved! Open in browser and Print -> Save as PDF");

                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().browse(finalFile.toURI());
                }
            } catch (Exception e) {
                showErrorAlert("Export Failed", "Error: " + e.getMessage());
            }
        }
    }

    private static String generateTeamReportHTML(User manager) {
        List<User> team = UserService.getInstance().getAllDescendants(manager);

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><meta charset='UTF-8'>");
        html.append("<title>Team Performance Report - ").append(manager.getName()).append("</title>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; max-width: 1000px; margin: 0 auto; padding: 20px; }");
        html.append("h1 { color: #2c3e50; border-bottom: 2px solid #3498db; padding-bottom: 10px; }");
        html.append("h2 { color: #34495e; margin-top: 30px; }");
        html.append("table { width: 100%; border-collapse: collapse; margin: 15px 0; }");
        html.append("th, td { padding: 10px; text-align: left; border: 1px solid #ddd; }");
        html.append("th { background: #3498db; color: white; }");
        html.append(".good { background: #d4edda !important; }");
        html.append(".warning { background: #fff3cd !important; }");
        html.append(".poor { background: #f8d7da !important; }");
        html.append("</style></head><body>");

        html.append("<h1>📊 Team Performance Report</h1>");
        html.append("<p><strong>Manager:</strong> ").append(manager.getName()).append("</p>");
        html.append("<p><strong>Team Size:</strong> ").append(team.size()).append(" members</p>");
        html.append("<p><strong>Generated:</strong> ").append(DATE_FORMAT.format(new java.util.Date())).append("</p>");

        html.append("<h2>📈 Team Members Performance</h2>");
        html.append("<table>");
        html.append(
                "<tr><th>Name</th><th>Tier</th><th>Role</th><th>Punct</th><th>Team</th><th>Prod</th><th>Overall</th></tr>");

        for (User u : team) {
            PerformanceCalculator.ScoreResult score = PerformanceCalculator.calculateScore(u);
            String role = getRole(u);
            String rowClass = score.overall >= 7 ? "good" : score.overall >= 5 ? "warning" : "poor";

            html.append("<tr class='").append(rowClass).append("'>");
            html.append("<td>").append(u.getName()).append("</td>");
            html.append("<td>").append(u.getTierLevel()).append("</td>");
            html.append("<td>").append(role).append("</td>");
            html.append("<td>").append(String.format("%.1f", score.punctuality)).append("</td>");
            html.append("<td>").append(String.format("%.1f", score.teamwork)).append("</td>");
            html.append("<td>").append(String.format("%.1f", score.productivity)).append("</td>");
            html.append("<td><strong>").append(String.format("%.1f", score.overall)).append("</strong></td>");
            html.append("</tr>");
        }
        html.append("</table>");

        html.append("<p style='margin-top:20px'><span style='background:#d4edda;padding:5px'>■ Good (7+)</span> ");
        html.append("<span style='background:#fff3cd;padding:5px'>■ Average (5-7)</span> ");
        html.append("<span style='background:#f8d7da;padding:5px'>■ Needs Improvement (<5)</span></p>");

        html.append("<hr><p style='text-align:center; color:#888;'>Generated by Performance Review System</p>");
        html.append("</body></html>");

        return html.toString();
    }

    private static void showInfoAlert(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private static void showErrorAlert(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private static String generateUserReportHTML(User user) {
        PerformanceCalculator.ScoreResult score = PerformanceCalculator.calculateScore(user);
        List<Review> reviews = DataStore.getInstance().getReviewsForUser(user);

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><meta charset='UTF-8'>");
        html.append("<title>Performance Report - ").append(user.getName()).append("</title>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; max-width: 800px; margin: 0 auto; padding: 20px; }");
        html.append("h1 { color: #2c3e50; border-bottom: 2px solid #3498db; padding-bottom: 10px; }");
        html.append("h2 { color: #34495e; margin-top: 30px; }");
        html.append("table { width: 100%; border-collapse: collapse; margin: 15px 0; }");
        html.append("th, td { padding: 12px; text-align: left; border: 1px solid #ddd; }");
        html.append("th { background: #3498db; color: white; }");
        html.append("tr:nth-child(even) { background: #f9f9f9; }");
        html.append(".score-box { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); ");
        html.append("color: white; padding: 20px; border-radius: 10px; margin: 20px 0; }");
        html.append(".score-box h2 { color: white; margin: 0; }");
        html.append(".big-score { font-size: 48px; font-weight: bold; }");
        html.append(".breakdown { background: #f5f5f5; padding: 15px; border-left: 4px solid #3498db; }");
        html.append(".good { background: #d4edda; color: #155724; font-weight: bold; }");
        html.append(".warning { background: #fff3cd; color: #856404; font-weight: bold; }");
        html.append(".poor { background: #f8d7da; color: #721c24; font-weight: bold; }");
        html.append("@media print { .no-print { display: none; } }");
        html.append("</style></head><body>");

        // Header
        html.append("<h1>📊 Performance Report</h1>");
        html.append("<p><strong>Employee:</strong> ").append(user.getName()).append("</p>");
        html.append("<p><strong>Username:</strong> ").append(user.getUsername()).append("</p>");
        html.append("<p><strong>Tier Level:</strong> ").append(user.getTierLevel()).append("</p>");
        html.append("<p><strong>Generated:</strong> ").append(DATE_FORMAT.format(new Date())).append("</p>");

        // Score Box
        html.append("<div class='score-box'>");
        html.append("<h2>Overall Performance Score</h2>");
        html.append("<div class='big-score'>").append(String.format("%.1f", score.overall)).append(" / 10</div>");
        html.append("</div>");

        // Category Breakdown with color-coded cells
        html.append("<h2>📊 Score Breakdown</h2>");
        html.append("<table>");
        html.append("<tr><th>Category</th><th>Score</th><th>Rating</th></tr>");
        html.append(createScoreRow("Punctuality", score.punctuality));
        html.append(createScoreRow("Teamwork", score.teamwork));
        html.append(createScoreRow("Productivity", score.productivity));
        html.append("</table>");

        // Calculation Method
        html.append("<h2>📐 Calculation Method</h2>");
        html.append("<div class='breakdown'><pre>").append(score.breakdown).append("</pre></div>");

        if (!score.missingCategories.isEmpty()) {
            html.append("<p><em>Note: Some review categories were missing. Weights were redistributed.</em></p>");
        }

        // Reviews History with color-coded cells
        html.append("<h2>📝 Reviews Received (").append(reviews.size()).append(")</h2>");
        if (reviews.isEmpty()) {
            html.append("<p>No reviews received yet.</p>");
        } else {
            html.append("<table>");
            html.append("<tr><th>#</th><th>Punct</th><th>Team</th><th>Prod</th><th>Total</th></tr>");
            int i = 1;
            for (Review r : reviews) {
                html.append("<tr>");
                html.append("<td>").append(i++).append("</td>");
                html.append("<td class='").append(getScoreClass(r.getPunctualityScore())).append("'>")
                        .append(r.getPunctualityScore()).append("</td>");
                html.append("<td class='").append(getScoreClass(r.getTeamworkScore())).append("'>")
                        .append(r.getTeamworkScore()).append("</td>");
                html.append("<td class='").append(getScoreClass(r.getProductivityScore())).append("'>")
                        .append(r.getProductivityScore()).append("</td>");
                html.append("<td><strong>").append(r.calculateTotalScore()).append("</strong></td>");
                html.append("</tr>");
            }
            html.append("</table>");
        }

        // Goals
        List<Goal> goals = DataStore.getInstance().getGoals();
        html.append("<h2>🎯 Goals</h2>");
        html.append("<ul>");
        boolean hasGoals = false;
        for (Goal g : goals) {
            if (g.getEmployee() != null && g.getEmployee().getUsername().equals(user.getUsername())) {
                String goalStyle = "COMPLETED".equals(g.getStatus()) ? "color:#28a745"
                        : "CANCELLED".equals(g.getStatus()) ? "color:#dc3545" : "color:#ffc107";
                html.append("<li style='").append(goalStyle).append("'>").append(g.getDescription()).append(" [")
                        .append(g.getStatus()).append("]</li>");
                hasGoals = true;
            }
        }
        if (!hasGoals)
            html.append("<li>No goals set</li>");
        html.append("</ul>");

        html.append("<hr><p style='text-align:center; color:#888;'>Generated by Performance Review System</p>");
        html.append("</body></html>");

        return html.toString();
    }

    private static String createScoreRow(String category, double score) {
        String scoreClass = getScoreClass(score);
        String rating = score >= 7 ? "Excellent" : score >= 5 ? "Good" : "Needs Improvement";
        return "<tr><td>" + category + "</td><td class='" + scoreClass + "'>" + String.format("%.1f", score)
                + "</td><td class='" + scoreClass + "'>" + rating + "</td></tr>";
    }

    private static String getScoreClass(double score) {
        return score >= 7 ? "good" : score >= 5 ? "warning" : "poor";
    }

    private static String generateAllUsersReportHTML() {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><meta charset='UTF-8'>");
        html.append("<title>All Users Performance Report</title>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; max-width: 1000px; margin: 0 auto; padding: 20px; }");
        html.append("h1 { color: #2c3e50; border-bottom: 2px solid #3498db; padding-bottom: 10px; }");
        html.append("table { width: 100%; border-collapse: collapse; margin: 15px 0; }");
        html.append("th, td { padding: 10px; text-align: left; border: 1px solid #ddd; }");
        html.append("th { background: #3498db; color: white; }");
        html.append(".good { background: #d4edda !important; }");
        html.append(".warning { background: #fff3cd !important; }");
        html.append(".poor { background: #f8d7da !important; }");
        html.append("</style></head><body>");

        html.append("<h1>📈 Organization Performance Report</h1>");
        html.append("<p><strong>Generated:</strong> ").append(DATE_FORMAT.format(new Date())).append("</p>");

        html.append("<table>");
        html.append(
                "<tr><th>Name</th><th>Username</th><th>Tier</th><th>Role</th><th>Punct</th><th>Team</th><th>Prod</th><th>Overall</th></tr>");

        for (User u : DataStore.getInstance().getUsers()) {
            if (u instanceof Admin)
                continue;

            PerformanceCalculator.ScoreResult score = PerformanceCalculator.calculateScore(u);
            String role = getRole(u);
            String rowClass = score.overall >= 7 ? "good" : score.overall >= 5 ? "warning" : "poor";

            html.append("<tr class='").append(rowClass).append("'>");
            html.append("<td>").append(u.getName()).append("</td>");
            html.append("<td>").append(u.getUsername()).append("</td>");
            html.append("<td>").append(u.getTierLevel()).append("</td>");
            html.append("<td>").append(role).append("</td>");
            html.append("<td>").append(String.format("%.1f", score.punctuality)).append("</td>");
            html.append("<td>").append(String.format("%.1f", score.teamwork)).append("</td>");
            html.append("<td>").append(String.format("%.1f", score.productivity)).append("</td>");
            html.append("<td><strong>").append(String.format("%.1f", score.overall)).append("</strong></td>");
            html.append("</tr>");
        }
        html.append("</table>");

        html.append("<p style='margin-top:20px'><span style='background:#d4edda;padding:5px'>■ Good (7+)</span> ");
        html.append("<span style='background:#fff3cd;padding:5px'>■ Average (5-7)</span> ");
        html.append("<span style='background:#f8d7da;padding:5px'>■ Needs Improvement (<5)</span></p>");

        html.append("<hr><p style='text-align:center; color:#888;'>Generated by Performance Review System</p>");
        html.append("</body></html>");

        return html.toString();
    }

    private static String getRole(User u) {
        int tier = u.getTierLevel();
        boolean hasReports = u.getDirectReports() != null && !u.getDirectReports().isEmpty();
        if (tier == DataStore.MAX_TIERS)
            return "Highboard Manager";
        if (tier == 1 && !hasReports)
            return "Employee";
        return "Manager & Employee";
    }
}
