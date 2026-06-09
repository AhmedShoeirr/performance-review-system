package com.performance.gui;

import com.performance.model.*;
import com.performance.service.*;
import com.performance.util.DataStore;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Panel for middle-tier employees (tier > 1 and tier < MAX_TIERS)
 * who have direct reports AND report to someone above them
 */
public class ManagerEmployeePanel extends JPanel {
    private User user;

    public ManagerEmployeePanel(User user) {
        this.user = user;
        setLayout(new BorderLayout());

        // Top panel with logout
        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel welcomeLabel = new JLabel("Welcome, " + user.getName() + " (Manager & Employee)",
                SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        JButton logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(e -> logout());

        topPanel.add(welcomeLabel, BorderLayout.CENTER);
        topPanel.add(logoutBtn, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Main tabbed pane - combines manager and employee features
        JTabbedPane tabbedPane = new JTabbedPane();

        // Manager tabs
        tabbedPane.addTab("Create Review", createReviewPanel());
        tabbedPane.addTab("Training", createTrainingPanel());
        tabbedPane.addTab("Team Goals", createTeamGoalsPanel());
        tabbedPane.addTab("Team Review History", createTeamReviewHistoryPanel());
        tabbedPane.addTab("👥 Team Management", createTeamManagementPanel()); // Request tier/suspend
        tabbedPane.addTab("📋 My Requests", createMyRequestsPanel()); // View request status

        // Employee tabs
        tabbedPane.addTab("My Scores", createMyScoresPanel()); // Aggregated scores
        tabbedPane.addTab("Review Manager", createReviewManagerPanel()); // Upward reviews to MY manager
        tabbedPane.addTab("Peer Reviews", createPeerReviewPanel()); // Give peer reviews
        tabbedPane.addTab("My Training", createMyTrainingPanel());
        tabbedPane.addTab("Chain of Command", createChainPanel());
        tabbedPane.addTab("My Goals", createMyGoalsPanel());
        tabbedPane.addTab("My Given Reviews", createMyGivenReviewsPanel());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private void logout() {
        SwingUtilities.getWindowAncestor(this).dispose();
        new LoginFrame().setVisible(true);
    }

    // ==================== MANAGER FUNCTIONALITY ====================

    private JPanel createReviewPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JComboBox<User> employeeCombo = new JComboBox<>();
        for (User e : UserService.getInstance().getAllDescendants(user)) {
            employeeCombo.addItem(e);
        }

        JSlider punctualitySlider = new JSlider(1, 10, 5);
        punctualitySlider.setMajorTickSpacing(1);
        punctualitySlider.setPaintTicks(true);
        punctualitySlider.setPaintLabels(true);

        JSlider teamworkSlider = new JSlider(1, 10, 5);
        teamworkSlider.setMajorTickSpacing(1);
        teamworkSlider.setPaintTicks(true);
        teamworkSlider.setPaintLabels(true);

        JSlider productivitySlider = new JSlider(1, 10, 5);
        productivitySlider.setMajorTickSpacing(1);
        productivitySlider.setPaintTicks(true);
        productivitySlider.setPaintLabels(true);

        JTextArea commentsArea = new JTextArea(3, 20);

        formPanel.add(new JLabel("Employee:"));
        formPanel.add(employeeCombo);
        formPanel.add(new JLabel("Punctuality (1-10):"));
        formPanel.add(punctualitySlider);
        formPanel.add(new JLabel("Teamwork (1-10):"));
        formPanel.add(teamworkSlider);
        formPanel.add(new JLabel("Productivity (1-10):"));
        formPanel.add(productivitySlider);
        formPanel.add(new JLabel("Comments:"));
        formPanel.add(new JScrollPane(commentsArea));

        JPanel buttonPanel = new JPanel();
        JButton submitBtn = new JButton("Submit Review");
        JButton draftBtn = new JButton("Save Draft");
        buttonPanel.add(submitBtn);
        buttonPanel.add(draftBtn);

        formPanel.add(new JLabel(""));
        formPanel.add(buttonPanel);

        panel.add(formPanel, BorderLayout.CENTER);

        submitBtn.addActionListener(e -> {
            User emp = (User) employeeCombo.getSelectedItem();
            if (emp == null) {
                JOptionPane.showMessageDialog(this, "Please select an employee.");
                return;
            }

            ReviewService.getInstance().createReview(emp, user,
                    punctualitySlider.getValue(), teamworkSlider.getValue(), productivitySlider.getValue(),
                    0, 0, 0, commentsArea.getText(), false);
            JOptionPane.showMessageDialog(this, "Review submitted successfully!");

            commentsArea.setText("");
            punctualitySlider.setValue(5);
            teamworkSlider.setValue(5);
            productivitySlider.setValue(5);
        });

        draftBtn.addActionListener(e -> {
            User emp = (User) employeeCombo.getSelectedItem();
            if (emp == null) {
                JOptionPane.showMessageDialog(this, "Please select an employee.");
                return;
            }

            ReviewService.getInstance().createReview(emp, user,
                    punctualitySlider.getValue(), teamworkSlider.getValue(), productivitySlider.getValue(),
                    0, 0, 0, commentsArea.getText(), true);
            JOptionPane.showMessageDialog(this, "Review saved as draft.");
        });

        return panel;
    }

    private JPanel createTrainingPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Recommend Training"));

        JComboBox<User> employeeCombo = new JComboBox<>();
        for (User e : UserService.getInstance().getAllDescendants(user)) {
            employeeCombo.addItem(e);
        }

        JComboBox<String> courseCombo = new JComboBox<>();
        courseCombo.setEditable(true);
        courseCombo.addItem("-- Select or Type Custom --");
        for (String c : TrainingService.getInstance().getAvailableCourses()) {
            courseCombo.addItem(c);
        }

        JButton recommendBtn = new JButton("Recommend");

        inputPanel.add(new JLabel("Employee:"));
        inputPanel.add(employeeCombo);
        inputPanel.add(new JLabel("Course:"));
        inputPanel.add(courseCombo);
        inputPanel.add(new JLabel(""));
        inputPanel.add(recommendBtn);

        panel.add(inputPanel, BorderLayout.NORTH);

        // List of training I've given
        DefaultListModel<TrainingRecommendation> listModel = new DefaultListModel<>();
        JList<TrainingRecommendation> trainingList = new JList<>(listModel);
        trainingList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean sel,
                    boolean focus) {
                super.getListCellRendererComponent(list, value, index, sel, focus);
                if (value instanceof TrainingRecommendation) {
                    TrainingRecommendation tr = (TrainingRecommendation) value;
                    String recipient = tr.getRecipient() != null ? tr.getRecipient().getName() : "Unknown";
                    setText("→ " + recipient + ": " + tr.getCourseName());
                }
                return this;
            }
        });

        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.setBorder(BorderFactory.createTitledBorder("My Training Recommendations"));
        listPanel.add(new JScrollPane(trainingList), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel();
        JButton refreshBtn = new JButton("Refresh");
        JButton deleteBtn = new JButton("Delete");
        btnPanel.add(refreshBtn);
        btnPanel.add(deleteBtn);
        listPanel.add(btnPanel, BorderLayout.SOUTH);

        panel.add(listPanel, BorderLayout.CENTER);

        Runnable refreshList = () -> {
            listModel.clear();
            for (TrainingRecommendation tr : TrainingService.getInstance().getRecommendationsBy(user)) {
                listModel.addElement(tr);
            }
        };
        refreshList.run();

        recommendBtn.addActionListener(e -> {
            User emp = (User) employeeCombo.getSelectedItem();
            String course = (String) courseCombo.getEditor().getItem();
            if (course != null)
                course = course.trim();

            if (emp != null && course != null && !course.isEmpty() && !course.equals("-- Select or Type Custom --")) {
                TrainingService.getInstance().createRecommendation(user, emp, course);
                JOptionPane.showMessageDialog(this, "Training recommended to " + emp.getUsername() + "!");
                refreshList.run();
            } else {
                JOptionPane.showMessageDialog(this, "Please select employee and enter course name.");
            }
        });

        refreshBtn.addActionListener(e -> refreshList.run());

        deleteBtn.addActionListener(e -> {
            TrainingRecommendation selected = trainingList.getSelectedValue();
            if (selected != null) {
                TrainingService.getInstance().deleteRecommendation(selected, user);
                refreshList.run();
            }
        });

        return panel;
    }

    private JPanel createTeamGoalsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // List of team goals
        DefaultListModel<Goal> listModel = new DefaultListModel<>();
        JList<Goal> goalsList = new JList<>(listModel);
        goalsList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean sel,
                    boolean focus) {
                super.getListCellRendererComponent(list, value, index, sel, focus);
                if (value instanceof Goal) {
                    Goal g = (Goal) value;
                    String empName = g.getEmployee() != null ? g.getEmployee().getName() : "Unknown";
                    String creator = g.getCreator() != null ? g.getCreator().getName() : "Unknown";
                    setText(empName + ": " + g.getDescription() + " [" + g.getStatus() + "] - by " + creator);
                }
                return this;
            }
        });

        // Input panel for adding new goals
        JPanel inputPanel = new JPanel(new BorderLayout());
        JPanel formPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JComboBox<User> employeeCombo = new JComboBox<>();
        for (User e : UserService.getInstance().getAllDescendants(user)) {
            employeeCombo.addItem(e);
        }
        employeeCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean sel,
                    boolean focus) {
                super.getListCellRendererComponent(list, value, index, sel, focus);
                if (value instanceof User) {
                    setText(((User) value).getName());
                }
                return this;
            }
        });

        JTextField goalField = new JTextField(20);
        JButton addBtn = new JButton("Add Goal");
        JButton deleteBtn = new JButton("Delete Selected");
        JButton refreshBtn = new JButton("Refresh");

        formPanel.add(new JLabel("Employee:"));
        formPanel.add(employeeCombo);
        formPanel.add(new JLabel("Goal:"));
        formPanel.add(goalField);
        formPanel.add(addBtn);

        JPanel btnPanel = new JPanel();
        btnPanel.add(refreshBtn);
        btnPanel.add(deleteBtn);

        inputPanel.add(formPanel, BorderLayout.CENTER);
        inputPanel.add(btnPanel, BorderLayout.EAST);

        // Refresh function
        Runnable refresh = () -> {
            listModel.clear();
            for (User emp : UserService.getInstance().getAllDescendants(user)) {
                for (Goal g : GoalService.getInstance().getGoalsForUser(emp)) {
                    listModel.addElement(g);
                }
            }
        };
        refresh.run();

        addBtn.addActionListener(e -> {
            User emp = (User) employeeCombo.getSelectedItem();
            String desc = goalField.getText().trim();
            if (emp != null && !desc.isEmpty()) {
                GoalService.getInstance().createGoal(emp, desc, user); // Manager is the creator
                refresh.run();
                goalField.setText("");
                JOptionPane.showMessageDialog(panel, "Goal added for " + emp.getName());
            } else {
                JOptionPane.showMessageDialog(panel, "Please select an employee and enter a goal description.");
            }
        });

        deleteBtn.addActionListener(e -> {
            Goal selected = goalsList.getSelectedValue();
            if (selected != null) {
                boolean deleted = GoalService.getInstance().deleteGoal(selected, user);
                if (deleted) {
                    refresh.run();
                } else {
                    JOptionPane.showMessageDialog(panel, "You can only delete goals you created.");
                }
            }
        });

        refreshBtn.addActionListener(e -> refresh.run());

        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(goalsList), BorderLayout.CENTER);

        return panel;
    }

    // ==================== EMPLOYEE FUNCTIONALITY ====================

    private JPanel createMyReviewsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JTextArea reviewArea = new JTextArea();
        reviewArea.setEditable(false);

        Runnable refresh = () -> {
            reviewArea.setText("");
            List<Review> reviews = DataStore.getInstance().getReviewsForUser(user);
            if (reviews.isEmpty()) {
                reviewArea.setText("No reviews found.");
            } else {
                int count = 1;
                for (Review r : reviews) {
                    // Anonymous reviews - don't show who gave the review
                    reviewArea.append("Review #" + count + ":\n");
                    reviewArea.append("  Punctuality: " + r.getPunctualityScore() + "\n");
                    reviewArea.append("  Teamwork: " + r.getTeamworkScore() + "\n");
                    reviewArea.append("  Productivity: " + r.getProductivityScore() + "\n");
                    if (r.getLeadershipScore() > 0 || r.getVisionScore() > 0 || r.getManagerialSkillsScore() > 0) {
                        reviewArea.append("  Leadership: " + r.getLeadershipScore() + "\n");
                        reviewArea.append("  Vision: " + r.getVisionScore() + "\n");
                        reviewArea.append("  Managerial Skills: " + r.getManagerialSkillsScore() + "\n");
                    }
                    reviewArea.append("  Total: " + r.calculateTotalScore() + "\n");
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

    private JPanel createMyTrainingPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JTextArea trainingArea = new JTextArea();
        trainingArea.setEditable(false);

        Runnable refresh = () -> {
            trainingArea.setText("");
            List<TrainingRecommendation> recs = DataStore.getInstance().getTrainingRecommendations();
            boolean found = false;
            for (TrainingRecommendation tr : recs) {
                if (tr.getRecipient() != null && tr.getRecipient().getUsername().equals(user.getUsername())) {
                    trainingArea.append(tr.toString() + "\n");
                    found = true;
                }
            }
            if (!found) {
                trainingArea.setText("No training recommendations.");
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
            List<User> chain = DataStore.getInstance().getChainOfCommand(user);
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

    private JPanel createMyGoalsPanel() {
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
            for (Goal g : DataStore.getInstance().getGoals()) {
                if (g.getEmployee() != null && g.getEmployee().getUsername().equals(user.getUsername())) {
                    listModel.addElement(g);
                }
            }
        };
        refresh.run();

        addBtn.addActionListener(e -> {
            String desc = goalField.getText().trim();
            if (!desc.isEmpty()) {
                Goal g = new Goal(user, desc, user); // Self-created
                DataStore.getInstance().addGoal(g);
                refresh.run();
                goalField.setText("");
            }
        });

        deleteBtn.addActionListener(e -> {
            Goal selected = goalsList.getSelectedValue();
            if (selected != null) {
                // Only creator can delete
                if (selected.getCreator() != null && selected.getCreator().getUsername().equals(user.getUsername())) {
                    DataStore.getInstance().removeGoal(selected);
                    refresh.run();
                } else if (selected.getCreator() == null) {
                    // Legacy goal without creator
                    DataStore.getInstance().removeGoal(selected);
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

    private JPanel createMyGivenReviewsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        DefaultListModel<Review> listModel = new DefaultListModel<>();
        JList<Review> reviewsList = new JList<>(listModel);
        reviewsList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean sel,
                    boolean focus) {
                super.getListCellRendererComponent(list, value, index, sel, focus);
                if (value instanceof Review) {
                    Review r = (Review) value;
                    String empName = r.getEmployee() != null ? r.getEmployee().getName() : "Unknown";
                    setText("Review for " + empName + " - Score: " + r.calculateTotalScore() + "/10");
                }
                return this;
            }
        });

        JPanel btnPanel = new JPanel();
        JButton refreshBtn = new JButton("Refresh");
        JButton deleteBtn = new JButton("Delete Review");
        btnPanel.add(refreshBtn);
        btnPanel.add(deleteBtn);

        Runnable refresh = () -> {
            listModel.clear();
            List<Review> allReviews = DataStore.getInstance().getReviews();
            for (Review r : allReviews) {
                User reviewer = r.getReviewer();
                if (reviewer != null && reviewer.getUsername() != null
                        && reviewer.getUsername().equals(user.getUsername())) {
                    listModel.addElement(r);
                }
            }
        };
        refresh.run();

        refreshBtn.addActionListener(e -> refresh.run());

        deleteBtn.addActionListener(e -> {
            Review selected = reviewsList.getSelectedValue();
            if (selected != null) {
                int confirm = JOptionPane.showConfirmDialog(panel,
                        "Are you sure you want to delete this review?",
                        "Confirm Delete", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    DataStore.getInstance().removeReview(selected);
                    refresh.run();
                }
            }
        });

        panel.add(new JScrollPane(reviewsList), BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createTeamReviewHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Dropdown to select employee
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Select Employee:"));
        JComboBox<User> employeeCombo = new JComboBox<>();
        for (User e : DataStore.getInstance().getAllDescendants(user)) {
            employeeCombo.addItem(e);
        }
        employeeCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean sel,
                    boolean focus) {
                super.getListCellRendererComponent(list, value, index, sel, focus);
                if (value instanceof User) {
                    setText(((User) value).getName());
                }
                return this;
            }
        });
        topPanel.add(employeeCombo);
        JButton viewBtn = new JButton("View History");
        topPanel.add(viewBtn);

        // Report area
        JTextArea historyArea = new JTextArea();
        historyArea.setEditable(false);
        historyArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        historyArea.setText("Select an employee and click 'View History' to see their review history.");

        viewBtn.addActionListener(e -> {
            User selectedEmp = (User) employeeCombo.getSelectedItem();
            if (selectedEmp == null) {
                historyArea.setText("No employee selected.");
                return;
            }

            com.performance.service.ReportGenerator gen = new com.performance.service.ReportGenerator();
            historyArea.setText(gen.generateYearEndSummary(selectedEmp));
        });

        JButton refreshBtn = new JButton("Refresh Employee List");
        refreshBtn.addActionListener(e -> {
            employeeCombo.removeAllItems();
            for (User emp : DataStore.getInstance().getAllDescendants(user)) {
                employeeCombo.addItem(emp);
            }
        });

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(refreshBtn);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(historyArea), BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    // ==================== NEW PANELS ====================

    /**
     * Team Management - Request tier changes and suspensions (requires admin
     * approval)
     */
    private JPanel createTeamManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("Request Actions (Requires Admin Approval)"));

        JComboBox<User> employeeCombo = new JComboBox<>();
        for (User e : UserService.getInstance().getAllDescendants(user)) {
            employeeCombo.addItem(e);
        }
        employeeCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof User) {
                    User u = (User) value;
                    setText(u.getName() + " (Tier " + u.getTierLevel() + ")");
                }
                return this;
            }
        });

        JComboBox<Integer> tierCombo = new JComboBox<>();
        for (int i = 1; i < user.getTierLevel(); i++) {
            tierCombo.addItem(i);
        }

        formPanel.add(new JLabel("Select Employee:"));
        formPanel.add(employeeCombo);
        formPanel.add(new JLabel("New Tier Level:"));
        formPanel.add(tierCombo);

        JButton requestTierBtn = new JButton("📝 Request Tier Change");
        JButton requestSuspendBtn = new JButton("⚠️ Request Suspension");

        formPanel.add(new JLabel(""));
        formPanel.add(requestTierBtn);
        formPanel.add(new JLabel(""));
        formPanel.add(requestSuspendBtn);

        panel.add(formPanel, BorderLayout.NORTH);

        JTextArea infoArea = new JTextArea();
        infoArea.setEditable(false);
        infoArea.setText("══════════════════════════════════════════\n");
        infoArea.append("       TEAM MANAGEMENT\n");
        infoArea.append("══════════════════════════════════════════\n\n");
        infoArea.append("• Tier changes and suspensions require Admin approval\n");
        infoArea.append("• Suspension requires at least one other employee in same role\n");
        infoArea.append("• You can only modify employees in your hierarchy\n\n");
        infoArea.append("Your direct reports:\n");
        for (User dr : user.getDirectReports()) {
            infoArea.append("  • " + dr.getName() + " (Tier " + dr.getTierLevel() + ")\n");
        }

        requestTierBtn.addActionListener(e -> {
            User emp = (User) employeeCombo.getSelectedItem();
            Integer newTier = (Integer) tierCombo.getSelectedItem();
            if (emp == null || newTier == null) {
                JOptionPane.showMessageDialog(panel, "Select employee and tier.");
                return;
            }
            PendingAction pa = ApprovalService.getInstance().requestTierChange(user, emp, newTier);
            if (pa != null) {
                JOptionPane.showMessageDialog(panel, "Tier change request submitted!\nAwaiting admin approval.");
            } else {
                JOptionPane.showMessageDialog(panel,
                        "Cannot request this change.\nEmployee must be in your hierarchy.");
            }
        });

        requestSuspendBtn.addActionListener(e -> {
            User emp = (User) employeeCombo.getSelectedItem();
            if (emp == null) {
                JOptionPane.showMessageDialog(panel, "Select an employee first.");
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(panel,
                    "Request suspension for " + emp.getName() + "?",
                    "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                PendingAction pa = ApprovalService.getInstance().requestSuspension(user, emp);
                if (pa != null) {
                    JOptionPane.showMessageDialog(panel, "Suspension request submitted!\nAwaiting admin approval.");
                } else {
                    JOptionPane.showMessageDialog(panel,
                            "Cannot request suspension.\nEmployee must have a peer in the same role.");
                }
            }
        });

        panel.add(new JScrollPane(infoArea), BorderLayout.CENTER);
        return panel;
    }

    /**
     * View status of my submitted requests
     */
    private JPanel createMyRequestsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JTextArea requestsArea = new JTextArea();
        requestsArea.setEditable(false);
        requestsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        Runnable refresh = () -> {
            requestsArea.setText("");
            requestsArea.append("══════════════════════════════════════════\n");
            requestsArea.append("       MY SUBMITTED REQUESTS\n");
            requestsArea.append("══════════════════════════════════════════\n\n");

            List<PendingAction> myRequests = ApprovalService.getInstance().getRequestsByUser(user);
            if (myRequests.isEmpty()) {
                requestsArea.append("No requests submitted.\n");
            } else {
                for (PendingAction pa : myRequests) {
                    requestsArea.append("─────────────────────────────────────────\n");
                    requestsArea.append("Action: " + pa.getDescription() + "\n");
                    requestsArea.append("Status: " + pa.getStatus() + "\n");
                    requestsArea.append("Date: " + pa.getRequestedDate() + "\n");
                    if (pa.getRejectionReason() != null) {
                        requestsArea.append("Rejection Reason: " + pa.getRejectionReason() + "\n");
                    }
                    requestsArea.append("\n");
                }
            }
        };
        refresh.run();

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> refresh.run());

        panel.add(new JScrollPane(requestsArea), BorderLayout.CENTER);
        panel.add(refreshBtn, BorderLayout.SOUTH);
        return panel;
    }

    /**
     * My aggregated scores (anonymous - no individual review details)
     */
    private JPanel createMyScoresPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JTextArea scoresArea = new JTextArea();
        scoresArea.setEditable(false);
        scoresArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        Runnable refresh = () -> {
            scoresArea.setText("");
            double[] managerScores = ReviewService.getInstance().getAggregatedScores(user);
            double[] peerScores = ReviewService.getInstance().getAggregatedPeerScores(user);

            scoresArea.append("══════════════════════════════════════════\n");
            scoresArea.append("       MY AGGREGATED SCORES\n");
            scoresArea.append("══════════════════════════════════════════\n\n");

            scoresArea.append("📊 MANAGER REVIEWS (" + (int) managerScores[3] + " reviews)\n");
            scoresArea.append("─────────────────────────────────────────\n");
            if (managerScores[3] == 0) {
                scoresArea.append("   No manager reviews yet.\n\n");
            } else {
                scoresArea.append(String.format("   Punctuality:  %.1f / 10\n", managerScores[0]));
                scoresArea.append(String.format("   Teamwork:     %.1f / 10\n", managerScores[1]));
                scoresArea.append(String.format("   Productivity: %.1f / 10\n", managerScores[2]));
                double overall = (managerScores[0] + managerScores[1] + managerScores[2]) / 3.0;
                scoresArea.append("   ─────────────────\n");
                scoresArea.append(String.format("   OVERALL:      %.1f / 10\n\n", overall));
            }

            scoresArea.append("👥 PEER REVIEWS (" + (int) peerScores[3] + " reviews)\n");
            scoresArea.append("─────────────────────────────────────────\n");
            if (peerScores[3] == 0) {
                scoresArea.append("   No peer reviews yet.\n");
            } else {
                scoresArea.append(String.format("   Collaboration:  %.1f / 5\n", peerScores[0]));
                scoresArea.append(String.format("   Communication:  %.1f / 5\n", peerScores[1]));
                scoresArea.append(String.format("   Teamwork:       %.1f / 5\n", peerScores[2]));
                double peerOverall = (peerScores[0] + peerScores[1] + peerScores[2]) / 3.0;
                scoresArea.append("   ─────────────────\n");
                scoresArea.append(String.format("   OVERALL:        %.1f / 5\n", peerOverall));
            }

            scoresArea.append("\n══════════════════════════════════════════\n");
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
     * Give peer reviews to teammates (same manager)
     */
    private JPanel createPeerReviewPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(6, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createTitledBorder("Give Peer Review (to teammates)"));

        JComboBox<User> peerCombo = new JComboBox<>();
        for (User peer : user.getPeers()) {
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

        JTextArea infoArea = new JTextArea();
        infoArea.setEditable(false);
        infoArea.setText("══════════════════════════════════════════\n");
        infoArea.append("       YOUR TEAMMATES\n");
        infoArea.append("══════════════════════════════════════════\n\n");

        List<User> peers = user.getPeers();
        if (peers.isEmpty()) {
            infoArea.append("You have no teammates (peers under the same manager).\n");
        } else {
            infoArea.append("You can give peer reviews to:\n\n");
            for (User peer : peers) {
                infoArea.append("  • " + peer.getName() + " (" + peer.getUsername() + ")\n");
            }
            infoArea.append("\nNote: Your reviews are anonymous.\n");
        }

        submitBtn.addActionListener(e -> {
            User selectedPeer = (User) peerCombo.getSelectedItem();
            if (selectedPeer == null) {
                JOptionPane.showMessageDialog(panel, "Please select a teammate to review.");
                return;
            }

            PeerReview pr = ReviewService.getInstance().createPeerReview(
                    user, selectedPeer,
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

    /**
     * Panel for reviewing the user's own manager (upward review)
     */
    private JPanel createReviewManagerPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Check if user has a manager
        User manager = user.getManager();
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

        // Sliders for the 3 criteria (1-5)
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
                    user,
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
}
