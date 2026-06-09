package com.performance.gui;

import com.performance.model.*;
import com.performance.service.GoalService;
import com.performance.service.ReviewService;
import com.performance.service.TrainingService;
import com.performance.service.UserService;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Panel for Tier 1 employees only (lowest tier, no direct reports)
 */
public class EmployeePanel extends JPanel {
    private User employee;

    public EmployeePanel(User employee) {
        this.employee = employee;
        setLayout(new BorderLayout());

        // Top panel with logout
        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel welcomeLabel = new JLabel("Welcome, " + employee.getName() + " (Employee)", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        JButton logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(e -> logout());

        topPanel.add(welcomeLabel, BorderLayout.CENTER);
        topPanel.add(logoutBtn, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Main tabbed pane
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("My Performance", createPerformanceReportPanel());
        tabbedPane.addTab("My Scores", createScoresPanel()); // Aggregated scores only (anonymous)
        tabbedPane.addTab("Review Manager", createReviewManagerPanel()); // Upward reviews
        tabbedPane.addTab("Peer Reviews", createPeerReviewPanel()); // Give peer reviews
        tabbedPane.addTab("My Given Reviews", createMyGivenReviewsPanel()); // See reviews I gave (detailed)
        tabbedPane.addTab("Training", createTrainingPanel());
        tabbedPane.addTab("Chain of Command", createChainPanel());
        tabbedPane.addTab("My Goals", createGoalsPanel());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createPerformanceReportPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JTextArea reportArea = new JTextArea();
        reportArea.setEditable(false);
        reportArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        Runnable refresh = () -> {
            com.performance.service.PerformanceCalculator.ScoreResult result = com.performance.service.PerformanceCalculator
                    .calculateScore(employee);

            StringBuilder sb = new StringBuilder();
            sb.append("═══════════════════════════════════════════\n");
            sb.append("       MY PERFORMANCE REPORT\n");
            sb.append("═══════════════════════════════════════════\n\n");

            sb.append(String.format("Punctuality:  %.1f / 10\n", result.punctuality));
            sb.append(String.format("Teamwork:     %.1f / 10\n", result.teamwork));
            sb.append(String.format("Productivity: %.1f / 10\n", result.productivity));
            sb.append("───────────────────────────────────────────\n");
            sb.append(String.format("OVERALL:      %.1f / 10\n\n", result.overall));

            sb.append("How it's calculated:\n");
            sb.append(result.breakdown).append("\n\n");

            if (!result.missingCategories.isEmpty()) {
                sb.append("Note: Some review categories are missing.\n");
                sb.append("Scores were redistributed automatically.\n");
            }

            reportArea.setText(sb.toString());
        };
        refresh.run();

        JPanel btnPanel = new JPanel();
        JButton refreshBtn = new JButton("Refresh");
        JButton exportBtn = new JButton("📄 Export PDF");
        refreshBtn.addActionListener(e -> refresh.run());
        exportBtn.addActionListener(e -> com.performance.service.ReportExporter.exportUserReport(employee, this));
        btnPanel.add(refreshBtn);
        btnPanel.add(exportBtn);

        panel.add(new JScrollPane(reportArea), BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);
        return panel;
    }

    private void logout() {
        SwingUtilities.getWindowAncestor(this).dispose();
        new LoginFrame().setVisible(true);
    }

    /**
     * Panel showing aggregated scores only (no individual review details for
     * anonymity)
     */
    private JPanel createScoresPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JTextArea scoresArea = new JTextArea();
        scoresArea.setEditable(false);
        scoresArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        Runnable refresh = () -> {
            scoresArea.setText("");
            double[] managerScores = ReviewService.getInstance().getAggregatedScores(employee);
            double[] peerScores = ReviewService.getInstance().getAggregatedPeerScores(employee);

            scoresArea.append("═══════════════════════════════════════════\n");
            scoresArea.append("       MY AGGREGATED SCORES\n");
            scoresArea.append("═══════════════════════════════════════════\n\n");

            scoresArea.append("📊 MANAGER REVIEWS (" + (int) managerScores[3] + " reviews)\n");
            scoresArea.append("───────────────────────────────────────────\n");
            if (managerScores[3] == 0) {
                scoresArea.append("   No manager reviews yet.\n\n");
            } else {
                scoresArea.append(String.format("   Punctuality:  %.1f / 10\n", managerScores[0]));
                scoresArea.append(String.format("   Teamwork:     %.1f / 10\n", managerScores[1]));
                scoresArea.append(String.format("   Productivity: %.1f / 10\n", managerScores[2]));
                double overall = (managerScores[0] + managerScores[1] + managerScores[2]) / 3.0;
                scoresArea.append(String.format("   ───────────────────\n"));
                scoresArea.append(String.format("   OVERALL:      %.1f / 10\n\n", overall));
            }

            scoresArea.append("👥 PEER REVIEWS (" + (int) peerScores[3] + " reviews)\n");
            scoresArea.append("───────────────────────────────────────────\n");
            if (peerScores[3] == 0) {
                scoresArea.append("   No peer reviews yet.\n\n");
            } else {
                scoresArea.append(String.format("   Collaboration:  %.1f / 5\n", peerScores[0]));
                scoresArea.append(String.format("   Communication:  %.1f / 5\n", peerScores[1]));
                scoresArea.append(String.format("   Teamwork:       %.1f / 5\n", peerScores[2]));
                double peerOverall = (peerScores[0] + peerScores[1] + peerScores[2]) / 3.0;
                scoresArea.append(String.format("   ───────────────────\n"));
                scoresArea.append(String.format("   OVERALL:        %.1f / 5\n", peerOverall));
            }

            scoresArea.append("\n═══════════════════════════════════════════\n");
            scoresArea.append("Note: Individual reviews are anonymous.\n");
            scoresArea.append("You can only see aggregated scores.\n");
        };
        refresh.run();

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> refresh.run());

        panel.add(new JScrollPane(scoresArea), BorderLayout.CENTER);
        panel.add(refreshBtn, BorderLayout.SOUTH);
        return panel;
    }

    /**
     * Panel for giving and viewing peer reviews
     */
    private JPanel createPeerReviewPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Top: Give peer review form
        JPanel formPanel = new JPanel(new GridLayout(6, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createTitledBorder("Give Peer Review (to teammates)"));

        JComboBox<User> peerCombo = new JComboBox<>();
        for (User peer : employee.getPeers()) {
            peerCombo.addItem(peer);
        }
        peerCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof User) {
                    User u = (User) value;
                    setText(u.getName() + " (" + u.getUsername() + ")");
                }
                return this;
            }
        });

        JSlider collabSlider = new JSlider(1, 5, 3);
        collabSlider.setMajorTickSpacing(1);
        collabSlider.setPaintTicks(true);
        collabSlider.setPaintLabels(true);

        JSlider commSlider = new JSlider(1, 5, 3);
        commSlider.setMajorTickSpacing(1);
        commSlider.setPaintTicks(true);
        commSlider.setPaintLabels(true);

        JSlider teamSlider = new JSlider(1, 5, 3);
        teamSlider.setMajorTickSpacing(1);
        teamSlider.setPaintTicks(true);
        teamSlider.setPaintLabels(true);

        JTextField commentsField = new JTextField();

        formPanel.add(new JLabel("Teammate:"));
        formPanel.add(peerCombo);
        formPanel.add(new JLabel("Collaboration (1-5):"));
        formPanel.add(collabSlider);
        formPanel.add(new JLabel("Communication (1-5):"));
        formPanel.add(commSlider);
        formPanel.add(new JLabel("Teamwork (1-5):"));
        formPanel.add(teamSlider);
        formPanel.add(new JLabel("Comments (optional):"));
        formPanel.add(commentsField);

        JButton submitBtn = new JButton("Submit Peer Review");
        formPanel.add(new JLabel(""));
        formPanel.add(submitBtn);

        panel.add(formPanel, BorderLayout.NORTH);

        // Bottom: Info about peers
        JTextArea infoArea = new JTextArea();
        infoArea.setEditable(false);

        Runnable refreshInfo = () -> {
            infoArea.setText("");
            List<User> peers = employee.getPeers();
            infoArea.append("═══════════════════════════════════════════\n");
            infoArea.append("       YOUR TEAMMATES\n");
            infoArea.append("═══════════════════════════════════════════\n\n");

            if (peers.isEmpty()) {
                infoArea.append("You have no teammates (peers under the same manager).\n");
                infoArea.append("Peer reviews are only available for teammates.\n");
            } else {
                infoArea.append("You can give peer reviews to:\n\n");
                for (User peer : peers) {
                    infoArea.append("  • " + peer.getName() + " (" + peer.getUsername() + ")\n");
                }
                infoArea.append("\nNote: Your reviews are anonymous.\n");
                infoArea.append("Teammates will only see their aggregated scores.\n");
            }
        };
        refreshInfo.run();

        // Submit action
        submitBtn.addActionListener(e -> {
            User selectedPeer = (User) peerCombo.getSelectedItem();
            if (selectedPeer == null) {
                JOptionPane.showMessageDialog(panel, "Please select a teammate to review.");
                return;
            }

            PeerReview pr = ReviewService.getInstance().createPeerReview(
                    employee, selectedPeer,
                    collabSlider.getValue(), commSlider.getValue(), teamSlider.getValue(),
                    commentsField.getText().trim());

            if (pr != null) {
                JOptionPane.showMessageDialog(panel, "Peer review submitted successfully!");
                commentsField.setText("");
                collabSlider.setValue(3);
                commSlider.setValue(3);
                teamSlider.setValue(3);
            } else {
                JOptionPane.showMessageDialog(panel,
                        "Could not submit review. Make sure you're reviewing a valid teammate.");
            }
        });

        panel.add(new JScrollPane(infoArea), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createReviewsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JTextArea reviewArea = new JTextArea();
        reviewArea.setEditable(false);

        Runnable refresh = () -> {
            reviewArea.setText("");
            List<Review> reviews = ReviewService.getInstance().getReviewsForUser(employee);
            if (reviews.isEmpty()) {
                reviewArea.setText("No reviews found.");
            } else {
                int count = 1;
                for (Review r : reviews) {
                    // Don't show who gave the review - anonymous
                    reviewArea.append("Review #" + count + ":\n");
                    reviewArea.append("  Punctuality: " + r.getPunctualityScore() + "\n");
                    reviewArea.append("  Teamwork: " + r.getTeamworkScore() + "\n");
                    reviewArea.append("  Productivity: " + r.getProductivityScore() + "\n");
                    // Show extended categories if present
                    if (r.getLeadershipScore() > 0) {
                        reviewArea.append("  Leadership: " + r.getLeadershipScore() + "\n");
                        reviewArea.append("  Vision: " + r.getVisionScore() + "\n");
                        reviewArea.append("  Managerial Skills: " + r.getManagerialSkillsScore() + "\n");
                    }
                    reviewArea.append("  Comments: " + (r.getComments() != null ? r.getComments() : "None") + "\n\n");
                    count++;
                }
            }
        };
        refresh.run();

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> refresh.run());

        panel.add(new JScrollPane(reviewArea), BorderLayout.CENTER);
        panel.add(refreshBtn, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createTrainingPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JTextArea trainingArea = new JTextArea();
        trainingArea.setEditable(false);

        Runnable refresh = () -> {
            trainingArea.setText("");
            List<TrainingRecommendation> recs = TrainingService.getInstance().getRecommendationsFor(employee);
            if (recs.isEmpty()) {
                trainingArea.setText("No training recommendations.");
            } else {
                for (TrainingRecommendation tr : recs) {
                    trainingArea.append(tr.toString() + "\n");
                }
            }
        };
        refresh.run();

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> refresh.run());

        panel.add(new JScrollPane(trainingArea), BorderLayout.CENTER);
        panel.add(refreshBtn, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createChainPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JTextArea chainArea = new JTextArea();
        chainArea.setEditable(false);

        Runnable refresh = () -> {
            chainArea.setText("My Management Chain:\n\n");
            List<User> chain = UserService.getInstance().getChainOfCommand(employee);
            if (chain.isEmpty()) {
                chainArea.append("No manager assigned.");
            } else {
                for (int i = 0; i < chain.size(); i++) {
                    User u = chain.get(i);
                    String indent = "  ".repeat(i);
                    chainArea.append(indent + "Level " + (i + 1) + ": " + u.getName() +
                            " (" + u.getUsername() + ")\n");
                }
            }
        };
        refresh.run();

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> refresh.run());

        panel.add(new JScrollPane(chainArea), BorderLayout.CENTER);
        panel.add(refreshBtn, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createGoalsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        DefaultListModel<Goal> listModel = new DefaultListModel<>();
        JList<Goal> goalsList = new JList<>(listModel);
        goalsList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean sel,
                    boolean focus) {
                super.getListCellRendererComponent(list, value, index, sel, focus);
                if (value instanceof Goal) {
                    Goal g = (Goal) value;
                    String creator = g.getCreator() != null ? g.getCreator().getName() : "Self";
                    setText(g.getDescription() + " [" + g.getStatus() + "] - Created by: " + creator);
                }
                return this;
            }
        });

        JPanel inputPanel = new JPanel(new BorderLayout());
        JTextField goalField = new JTextField();
        JButton addBtn = new JButton("Add Goal");
        JButton deleteBtn = new JButton("Delete");
        JPanel btnPanel = new JPanel();
        btnPanel.add(addBtn);
        btnPanel.add(deleteBtn);
        inputPanel.add(goalField, BorderLayout.CENTER);
        inputPanel.add(btnPanel, BorderLayout.EAST);

        Runnable refresh = () -> {
            listModel.clear();
            for (Goal g : GoalService.getInstance().getGoalsForUser(employee)) {
                listModel.addElement(g);
            }
        };
        refresh.run();

        addBtn.addActionListener(e -> {
            String desc = goalField.getText().trim();
            if (!desc.isEmpty()) {
                GoalService.getInstance().createGoal(employee, desc, employee); // Self-created goal
                refresh.run();
                goalField.setText("");
            }
        });

        deleteBtn.addActionListener(e -> {
            Goal selected = goalsList.getSelectedValue();
            if (selected != null) {
                boolean deleted = GoalService.getInstance().deleteGoal(selected, employee);
                if (deleted) {
                    refresh.run();
                } else {
                    JOptionPane.showMessageDialog(panel, "You can only delete goals you created.");
                }
            }
        });

        panel.add(new JScrollPane(goalsList), BorderLayout.CENTER);
        panel.add(inputPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createReviewManagerPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Check if employee has a manager
        User manager = employee.getManager();
        if (manager == null) {
            JLabel noManagerLabel = new JLabel("You have no assigned manager to review.", SwingConstants.CENTER);
            noManagerLabel.setFont(new Font("Arial", Font.ITALIC, 14));
            panel.add(noManagerLabel, BorderLayout.CENTER);
            return panel;
        }

        JPanel formPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Manager info
        formPanel.add(new JLabel("Reviewing Manager:"));
        formPanel.add(new JLabel(manager.getName() + " (" + manager.getUsername() + ")"));

        // Sliders for the 3 criteria (1-5 as per requirements)
        JSlider punctualitySlider = new JSlider(1, 5, 3);
        punctualitySlider.setMajorTickSpacing(1);
        punctualitySlider.setPaintTicks(true);
        punctualitySlider.setPaintLabels(true);

        JSlider teamworkSlider = new JSlider(1, 5, 3);
        teamworkSlider.setMajorTickSpacing(1);
        teamworkSlider.setPaintTicks(true);
        teamworkSlider.setPaintLabels(true);

        JSlider productivitySlider = new JSlider(1, 5, 3);
        productivitySlider.setMajorTickSpacing(1);
        productivitySlider.setPaintTicks(true);
        productivitySlider.setPaintLabels(true);

        formPanel.add(new JLabel("Punctuality (1-5):"));
        formPanel.add(punctualitySlider);
        formPanel.add(new JLabel("Teamwork (1-5):"));
        formPanel.add(teamworkSlider);
        formPanel.add(new JLabel("Productivity (1-5):"));
        formPanel.add(productivitySlider);

        JTextArea commentsArea = new JTextArea(3, 20);
        formPanel.add(new JLabel("Comments (optional):"));
        formPanel.add(new JScrollPane(commentsArea));

        JButton submitBtn = new JButton("Submit Upward Review");
        formPanel.add(new JLabel(""));
        formPanel.add(submitBtn);

        panel.add(formPanel, BorderLayout.CENTER);

        // Info panel
        JTextArea infoArea = new JTextArea();
        infoArea.setEditable(false);
        infoArea.setFont(new Font("Arial", Font.ITALIC, 12));
        infoArea.setText("Note: Your upward review will be submitted anonymously.\n" +
                "Only administrators can view upward review data.\n" +
                "Your manager will NOT see that you submitted this review.");
        infoArea.setBackground(panel.getBackground());
        panel.add(infoArea, BorderLayout.NORTH);

        submitBtn.addActionListener(e -> {
            ReviewService.getInstance().createUpwardReview(
                    employee,
                    manager,
                    punctualitySlider.getValue(),
                    teamworkSlider.getValue(),
                    productivitySlider.getValue(),
                    commentsArea.getText());
            JOptionPane.showMessageDialog(panel,
                    "Your upward review has been submitted anonymously.\nThank you for your feedback!");

            // Reset form
            punctualitySlider.setValue(3);
            teamworkSlider.setValue(3);
            productivitySlider.setValue(3);
            commentsArea.setText("");
        });

        return panel;
    }

    /**
     * Panel showing detailed reviews this employee has given (peer reviews and
     * upward reviews)
     * Shows all score categories, not just total average
     */
    private JPanel createMyGivenReviewsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JTextArea reviewsArea = new JTextArea();
        reviewsArea.setEditable(false);
        reviewsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        Runnable refresh = () -> {
            reviewsArea.setText("");
            reviewsArea.append("══════════════════════════════════════════\n");
            reviewsArea.append("       REVIEWS I HAVE GIVEN\n");
            reviewsArea.append("══════════════════════════════════════════\n\n");

            // Upward reviews given
            reviewsArea.append("📤 UPWARD REVIEWS (to my manager):\n");
            reviewsArea.append("───────────────────────────────────────────\n");
            List<UpwardReview> upwardReviews = ReviewService.getInstance()
                    .getUpwardReviewsGivenBy(employee);
            if (upwardReviews.isEmpty()) {
                reviewsArea.append("  No upward reviews given yet.\n\n");
            } else {
                int num = 1;
                for (UpwardReview ur : upwardReviews) {
                    reviewsArea.append("\n  Review #" + num + " - Manager: " +
                            (ur.getManager() != null ? ur.getManager().getName() : "Unknown") + "\n");
                    reviewsArea.append("    Punctuality:  " + ur.getPunctualityScore() + "/5\n");
                    reviewsArea.append("    Teamwork:     " + ur.getTeamworkScore() + "/5\n");
                    reviewsArea.append("    Productivity: " + ur.getProductivityScore() + "/5\n");
                    double avg = (ur.getPunctualityScore() + ur.getTeamworkScore() +
                            ur.getProductivityScore()) / 3.0;
                    reviewsArea.append(String.format("    AVERAGE:      %.1f/5\n", avg));
                    if (ur.getComments() != null && !ur.getComments().isEmpty()) {
                        reviewsArea.append("    Comments: " + ur.getComments() + "\n");
                    }
                    num++;
                }
                reviewsArea.append("\n");
            }

            // Peer reviews given
            reviewsArea.append("👥 PEER REVIEWS (to teammates):\n");
            reviewsArea.append("───────────────────────────────────────────\n");
            List<PeerReview> peerReviews = ReviewService.getInstance()
                    .getPeerReviewsGivenBy(employee);
            if (peerReviews.isEmpty()) {
                reviewsArea.append("  No peer reviews given yet.\n");
            } else {
                int num = 1;
                for (PeerReview pr : peerReviews) {
                    reviewsArea.append("\n  Review #" + num + " - Peer: " +
                            (pr.getReviewed() != null ? pr.getReviewed().getName() : "Unknown") + "\n");
                    reviewsArea.append("    Collaboration:  " + pr.getCollaborationScore() + "/5\n");
                    reviewsArea.append("    Communication:  " + pr.getCommunicationScore() + "/5\n");
                    reviewsArea.append("    Teamwork:       " + pr.getTeamworkScore() + "/5\n");
                    double avg = (pr.getCollaborationScore() + pr.getCommunicationScore() +
                            pr.getTeamworkScore()) / 3.0;
                    reviewsArea.append(String.format("    AVERAGE:        %.1f/5\n", avg));
                    if (pr.getComments() != null && !pr.getComments().isEmpty()) {
                        reviewsArea.append("    Comments: " + pr.getComments() + "\n");
                    }
                    num++;
                }
            }
        };
        refresh.run();

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> refresh.run());

        panel.add(new JScrollPane(reviewsArea), BorderLayout.CENTER);
        panel.add(refreshBtn, BorderLayout.SOUTH);
        return panel;
    }
}
