package com.performance.gui;

import com.performance.model.*;
import com.performance.service.*;
import com.performance.util.DataStore;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Panel for Highboard Managers (top tier) - combines manager and employee
 * functionality
 */
public class HighboardManagerPanel extends JPanel {
    private User highboardManager;
    private JComboBox<User> employeeCombo;
    private JSlider punctualitySlider, teamworkSlider, productivitySlider;
    private JTextArea commentsArea;

    public HighboardManagerPanel(User highboardManager) {
        this.highboardManager = highboardManager;
        setLayout(new BorderLayout());

        // Top panel with logout
        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel welcomeLabel = new JLabel("Welcome, " + highboardManager.getName() + " (Highboard Manager)",
                SwingConstants.CENTER);
        welcomeLabel.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 16));
        JButton logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(e -> {
            SwingUtilities.getWindowAncestor(this).dispose();
            new LoginFrame().setVisible(true);
        });
        topPanel.add(welcomeLabel, BorderLayout.CENTER);
        topPanel.add(logoutBtn, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();

        // Manager tabs
        tabbedPane.addTab("Create Reviews", createReviewPanel());
        tabbedPane.addTab("My Team Goals", createTeamGoalsPanel());
        tabbedPane.addTab("Team Review History", createTeamReviewHistoryPanel());
        tabbedPane.addTab("Training", createTrainingPanel());
        tabbedPane.addTab("👥 Team Management", createTeamManagementPanel()); // Request tier/suspend
        tabbedPane.addTab("📋 My Requests", createMyRequestsPanel()); // View request status

        // Employee tabs (NO upward reviews - Highboard has no manager above)
        tabbedPane.addTab("My Scores", createMyScoresPanel()); // Aggregated scores only
        tabbedPane.addTab("My Goals", createMyGoalsPanel());
        tabbedPane.addTab("My Training", createMyTrainingPanel());
        tabbedPane.addTab("My Given Reviews", createMyGivenReviewsPanel());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createReviewPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(7, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        employeeCombo = new JComboBox<>();
        refreshEmployeeCombo();

        punctualitySlider = new JSlider(1, 10, 5);
        punctualitySlider.setMajorTickSpacing(1);
        punctualitySlider.setPaintTicks(true);
        punctualitySlider.setPaintLabels(true);

        teamworkSlider = new JSlider(1, 10, 5);
        teamworkSlider.setMajorTickSpacing(1);
        teamworkSlider.setPaintTicks(true);
        teamworkSlider.setPaintLabels(true);

        productivitySlider = new JSlider(1, 10, 5);
        productivitySlider.setMajorTickSpacing(1);
        productivitySlider.setPaintTicks(true);
        productivitySlider.setPaintLabels(true);

        commentsArea = new JTextArea(3, 20);
        JScrollPane commentsScroll = new JScrollPane(commentsArea);

        formPanel.add(new JLabel("Employee:"));
        formPanel.add(employeeCombo);
        formPanel.add(new JLabel("Punctuality (1-10):"));
        formPanel.add(punctualitySlider);
        formPanel.add(new JLabel("Teamwork (1-10):"));
        formPanel.add(teamworkSlider);
        formPanel.add(new JLabel("Productivity (1-10):"));
        formPanel.add(productivitySlider);
        formPanel.add(new JLabel("Comments:"));
        formPanel.add(commentsScroll);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton submitButton = new JButton("Submit Review");
        JButton draftButton = new JButton("Save Draft");
        buttonPanel.add(submitButton);
        buttonPanel.add(draftButton);

        formPanel.add(new JLabel(""));
        formPanel.add(buttonPanel);

        panel.add(formPanel, BorderLayout.CENTER);

        submitButton.addActionListener(e -> submitReview(false));
        draftButton.addActionListener(e -> submitReview(true));

        return panel;
    }

    private void submitReview(boolean isDraft) {
        Employee emp = (Employee) employeeCombo.getSelectedItem();
        if (emp == null) {
            JOptionPane.showMessageDialog(this, "Select an employee first.");
            return;
        }

        ReviewService.getInstance().createReview(emp, highboardManager,
                punctualitySlider.getValue(), teamworkSlider.getValue(), productivitySlider.getValue(),
                0, 0, 0, commentsArea.getText(), isDraft);
        JOptionPane.showMessageDialog(this, isDraft ? "Review saved as draft." : "Review submitted successfully!");

        commentsArea.setText("");
        punctualitySlider.setValue(5);
        teamworkSlider.setValue(5);
        productivitySlider.setValue(5);
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
        for (User e : DataStore.getInstance().getAllDescendants(highboardManager)) {
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
            for (User emp : DataStore.getInstance().getAllDescendants(highboardManager)) {
                for (Goal g : DataStore.getInstance().getGoals()) {
                    if (g.getEmployee() != null && g.getEmployee().getUsername().equals(emp.getUsername())) {
                        listModel.addElement(g);
                    }
                }
            }
        };
        refresh.run();

        addBtn.addActionListener(e -> {
            User emp = (User) employeeCombo.getSelectedItem();
            String desc = goalField.getText().trim();
            if (emp != null && !desc.isEmpty()) {
                Goal g = new Goal(emp, desc, highboardManager); // Highboard manager is the creator
                DataStore.getInstance().addGoal(g);
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
                // Only the creator can delete
                if (selected.getCreator() != null
                        && selected.getCreator().getUsername().equals(highboardManager.getUsername())) {
                    int confirm = JOptionPane.showConfirmDialog(panel,
                            "Delete this goal?", "Confirm", JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        DataStore.getInstance().removeGoal(selected);
                        refresh.run();
                    }
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

    private JPanel createTrainingPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JTabbedPane trainingTabs = new JTabbedPane();

        // Tab 1: Recommend to Individual
        JPanel individualPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        individualPanel.setBorder(BorderFactory.createTitledBorder("Recommend Training to Employee"));

        JComboBox<User> empCombo = new JComboBox<>();
        for (User e : DataStore.getInstance().getTeamMembers(highboardManager)) {
            empCombo.addItem(e);
        }

        JComboBox<String> courseCombo = new JComboBox<>();
        courseCombo.setEditable(true);
        courseCombo.addItem("-- Select or Type Custom --");
        for (String c : DataStore.getInstance().getAvailableCourses()) {
            courseCombo.addItem(c);
        }

        JButton recommendBtn = new JButton("Recommend");

        individualPanel.add(new JLabel("Employee:"));
        individualPanel.add(empCombo);
        individualPanel.add(new JLabel("Course (select or type):"));
        individualPanel.add(courseCombo);
        individualPanel.add(new JLabel(""));
        individualPanel.add(recommendBtn);

        recommendBtn.addActionListener(e -> {
            Employee target = (Employee) empCombo.getSelectedItem();
            String course = (String) courseCombo.getEditor().getItem();
            if (course != null)
                course = course.trim();

            if (target != null && course != null && !course.isEmpty()
                    && !course.equals("-- Select or Type Custom --")) {
                TrainingRecommendation tr = new TrainingRecommendation(highboardManager, target, course);
                DataStore.getInstance().addTrainingRecommendation(tr);
                JOptionPane.showMessageDialog(this, "Training recommended to " + target.getUsername() + "!");
            } else {
                JOptionPane.showMessageDialog(this, "Please select an employee and enter a course name.");
            }
        });

        // Tab 2: General Training for Team
        JPanel teamPanel = new JPanel(new BorderLayout());
        teamPanel.setBorder(BorderFactory.createTitledBorder("Recommend General Training to All Team"));

        JPanel teamInputPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        JComboBox<String> teamCourseCombo = new JComboBox<>();
        teamCourseCombo.setEditable(true);
        teamCourseCombo.addItem("-- Select or Type Custom --");
        for (String c : DataStore.getInstance().getAvailableCourses()) {
            teamCourseCombo.addItem(c);
        }

        JButton recommendAllBtn = new JButton("Recommend to All Team");

        teamInputPanel.add(new JLabel("Course (select or type):"));
        teamInputPanel.add(teamCourseCombo);
        teamInputPanel.add(new JLabel(""));
        teamInputPanel.add(recommendAllBtn);

        teamPanel.add(teamInputPanel, BorderLayout.NORTH);

        JTextArea teamResultArea = new JTextArea();
        teamResultArea.setEditable(false);
        teamPanel.add(new JScrollPane(teamResultArea), BorderLayout.CENTER);

        recommendAllBtn.addActionListener(e -> {
            String course = (String) teamCourseCombo.getEditor().getItem();
            if (course != null)
                course = course.trim();

            if (course != null && !course.isEmpty() && !course.equals("-- Select or Type Custom --")) {
                int count = 0;
                for (User emp : DataStore.getInstance().getTeamMembers(highboardManager)) {
                    TrainingRecommendation tr = new TrainingRecommendation(highboardManager, emp, course);
                    DataStore.getInstance().addTrainingRecommendation(tr);
                    count++;
                }
                teamResultArea.setText("Successfully recommended '" + course + "' to " + count + " team members!");
                JOptionPane.showMessageDialog(this, "Training recommended to all team members!");
            } else {
                JOptionPane.showMessageDialog(this, "Please enter a course name.");
            }
        });

        trainingTabs.addTab("Individual", individualPanel);
        trainingTabs.addTab("Team-Wide", teamPanel);

        // Tab 3: My Given Recommendations (with delete)
        JPanel myRecsPanel = new JPanel(new BorderLayout());
        myRecsPanel.setBorder(BorderFactory.createTitledBorder("Training I've Recommended"));

        DefaultListModel<TrainingRecommendation> recsListModel = new DefaultListModel<>();
        JList<TrainingRecommendation> recsList = new JList<>(recsListModel);
        recsList.setCellRenderer(new DefaultListCellRenderer() {
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

        JPanel recsButtonPanel = new JPanel();
        JButton recsRefreshBtn = new JButton("Refresh");
        JButton recsDeleteBtn = new JButton("Delete");
        recsButtonPanel.add(recsRefreshBtn);
        recsButtonPanel.add(recsDeleteBtn);

        Runnable refreshRecs = () -> {
            recsListModel.clear();
            for (TrainingRecommendation tr : DataStore.getInstance().getTrainingRecommendations()) {
                if (tr.getRecommender() != null
                        && tr.getRecommender().getUsername().equals(highboardManager.getUsername())) {
                    recsListModel.addElement(tr);
                }
            }
        };
        refreshRecs.run();

        recsRefreshBtn.addActionListener(e -> refreshRecs.run());

        recsDeleteBtn.addActionListener(e -> {
            TrainingRecommendation selected = recsList.getSelectedValue();
            if (selected != null) {
                DataStore.getInstance().removeTrainingRecommendation(selected);
                refreshRecs.run();
            }
        });

        myRecsPanel.add(new JScrollPane(recsList), BorderLayout.CENTER);
        myRecsPanel.add(recsButtonPanel, BorderLayout.SOUTH);

        trainingTabs.addTab("My Recommendations", myRecsPanel);

        panel.add(trainingTabs, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createMyReviewsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JTextArea reviewArea = new JTextArea();
        reviewArea.setEditable(false);

        Runnable refresh = () -> {
            reviewArea.setText("");
            List<Review> reviews = DataStore.getInstance().getReviewsForUser(highboardManager);
            if (reviews.isEmpty()) {
                reviewArea.setText("No reviews found.\n\n");
                reviewArea.append(
                        "As a Highboard Manager, you may receive reviews from admin or through upward reviews.");
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
        panel.add(new JScrollPane(goalsList), BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout());
        JTextField goalField = new JTextField();
        JButton addBtn = new JButton("Add Goal");
        JButton deleteBtn = new JButton("Delete");
        JPanel btnPanel = new JPanel();
        btnPanel.add(addBtn);
        btnPanel.add(deleteBtn);
        inputPanel.add(goalField, BorderLayout.CENTER);
        inputPanel.add(btnPanel, BorderLayout.EAST);
        panel.add(inputPanel, BorderLayout.SOUTH);

        Runnable refresh = () -> {
            listModel.clear();
            for (Goal g : DataStore.getInstance().getGoals()) {
                if (g.getEmployee() != null && g.getEmployee().getUsername().equals(highboardManager.getUsername())) {
                    listModel.addElement(g);
                }
            }
        };
        refresh.run();

        addBtn.addActionListener(e -> {
            String desc = goalField.getText().trim();
            if (!desc.isEmpty()) {
                Goal g = new Goal(highboardManager, desc, highboardManager); // Self-created
                DataStore.getInstance().addGoal(g);
                refresh.run();
                goalField.setText("");
            }
        });

        deleteBtn.addActionListener(e -> {
            Goal selected = goalsList.getSelectedValue();
            if (selected != null) {
                // Only creator can delete
                if (selected.getCreator() != null
                        && selected.getCreator().getUsername().equals(highboardManager.getUsername())) {
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

        return panel;
    }

    private JPanel createMyTrainingPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JTextArea trainingArea = new JTextArea();
        trainingArea.setEditable(false);

        List<TrainingRecommendation> recs = DataStore.getInstance().getTrainingRecommendations();
        boolean found = false;
        for (TrainingRecommendation tr : recs) {
            if (tr.getRecipient() != null && tr.getRecipient().getUsername().equals(highboardManager.getUsername())) {
                trainingArea.append(tr.toString() + "\\n");
                found = true;
            }
        }
        if (!found)
            trainingArea.append("No training recommendations received.");

        panel.add(new JScrollPane(trainingArea), BorderLayout.CENTER);
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
                        && reviewer.getUsername().equals(highboardManager.getUsername())) {
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
        JComboBox<User> teamMemberCombo = new JComboBox<>();
        for (User e : DataStore.getInstance().getAllDescendants(highboardManager)) {
            teamMemberCombo.addItem(e);
        }
        teamMemberCombo.setRenderer(new DefaultListCellRenderer() {
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
        topPanel.add(teamMemberCombo);
        JButton viewBtn = new JButton("View History");
        topPanel.add(viewBtn);

        // Report area
        JTextArea historyArea = new JTextArea();
        historyArea.setEditable(false);
        historyArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        historyArea.setText("Select an employee and click 'View History' to see their review history.");

        viewBtn.addActionListener(e -> {
            User selectedEmp = (User) teamMemberCombo.getSelectedItem();
            if (selectedEmp == null) {
                historyArea.setText("No employee selected.");
                return;
            }

            com.performance.service.ReportGenerator gen = new com.performance.service.ReportGenerator();
            historyArea.setText(gen.generateYearEndSummary(selectedEmp));
        });

        JButton refreshBtn = new JButton("Refresh Employee List");
        refreshBtn.addActionListener(e -> {
            teamMemberCombo.removeAllItems();
            for (User emp : DataStore.getInstance().getAllDescendants(highboardManager)) {
                teamMemberCombo.addItem(emp);
            }
        });

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(refreshBtn);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(historyArea), BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void refreshEmployeeCombo() {
        employeeCombo.removeAllItems();
        // Use getAllDescendants to show ALL subordinates recursively (not just direct
        // reports)
        for (User e : DataStore.getInstance().getAllDescendants(highboardManager)) {
            employeeCombo.addItem(e);
        }
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

        JComboBox<User> empCombo = new JComboBox<>();
        for (User e : UserService.getInstance().getAllDescendants(highboardManager)) {
            empCombo.addItem(e);
        }
        empCombo.setRenderer(new DefaultListCellRenderer() {
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
        for (int i = 1; i < highboardManager.getTierLevel(); i++) {
            tierCombo.addItem(i);
        }

        formPanel.add(new JLabel("Select Employee:"));
        formPanel.add(empCombo);
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
        infoArea.append("       TEAM MANAGEMENT (HIGHBOARD)\n");
        infoArea.append("══════════════════════════════════════════\n\n");
        infoArea.append("• Tier changes and suspensions require Admin approval\n");
        infoArea.append("• Suspension requires at least one other employee in same role\n");
        infoArea.append("• You can modify all employees in your hierarchy\n\n");
        infoArea.append("Your direct reports:\n");
        for (User dr : highboardManager.getDirectReports()) {
            infoArea.append("  • " + dr.getName() + " (Tier " + dr.getTierLevel() + ")\n");
        }

        requestTierBtn.addActionListener(e -> {
            User emp = (User) empCombo.getSelectedItem();
            Integer newTier = (Integer) tierCombo.getSelectedItem();
            if (emp == null || newTier == null) {
                JOptionPane.showMessageDialog(panel, "Select employee and tier.");
                return;
            }
            PendingAction pa = ApprovalService.getInstance().requestTierChange(highboardManager, emp, newTier);
            if (pa != null) {
                JOptionPane.showMessageDialog(panel, "Tier change request submitted!\nAwaiting admin approval.");
            } else {
                JOptionPane.showMessageDialog(panel,
                        "Cannot request this change.\nEmployee must be in your hierarchy.");
            }
        });

        requestSuspendBtn.addActionListener(e -> {
            User emp = (User) empCombo.getSelectedItem();
            if (emp == null) {
                JOptionPane.showMessageDialog(panel, "Select an employee first.");
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(panel,
                    "Request suspension for " + emp.getName() + "?",
                    "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                PendingAction pa = ApprovalService.getInstance().requestSuspension(highboardManager, emp);
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

            List<PendingAction> myRequests = ApprovalService.getInstance().getRequestsByUser(highboardManager);
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
     * Highboard managers may not have many reviews since they have no manager
     */
    private JPanel createMyScoresPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JTextArea scoresArea = new JTextArea();
        scoresArea.setEditable(false);
        scoresArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        Runnable refresh = () -> {
            scoresArea.setText("");
            double[] managerScores = ReviewService.getInstance().getAggregatedScores(highboardManager);
            double[] peerScores = ReviewService.getInstance().getAggregatedPeerScores(highboardManager);

            scoresArea.append("══════════════════════════════════════════\n");
            scoresArea.append("       MY AGGREGATED SCORES\n");
            scoresArea.append("══════════════════════════════════════════\n\n");

            scoresArea.append("📊 UPWARD REVIEWS (" + (int) managerScores[3] + " reviews)\n");
            scoresArea.append("─────────────────────────────────────────\n");
            if (managerScores[3] == 0) {
                scoresArea.append("   No upward reviews yet.\n\n");
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
            scoresArea.append("Note: As a Highboard Manager, you have no manager.\n");
            scoresArea.append("Your reviews come from upward feedback.\n");
        };
        refresh.run();

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> refresh.run());

        panel.add(new JScrollPane(scoresArea), BorderLayout.CENTER);
        panel.add(refreshBtn, BorderLayout.SOUTH);
        return panel;
    }

    /**
     * Panel to view upward reviews received from direct reports (anonymous - scores
     * only)
     */
    private JPanel createUpwardReviewsReceivedPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JTextArea reviewsArea = new JTextArea();
        reviewsArea.setEditable(false);
        reviewsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        Runnable refresh = () -> {
            reviewsArea.setText("");
            reviewsArea.append("══════════════════════════════════════════\n");
            reviewsArea.append("       UPWARD REVIEWS RECEIVED\n");
            reviewsArea.append("       (From Direct Reports)\n");
            reviewsArea.append("══════════════════════════════════════════\n\n");

            List<UpwardReview> upwardReviews = ReviewService.getInstance()
                    .getUpwardReviewsForManager(highboardManager);

            if (upwardReviews.isEmpty()) {
                reviewsArea.append("No upward reviews received yet.\n\n");
                reviewsArea.append("Your direct reports can submit upward reviews\n");
                reviewsArea.append("to provide feedback on your management.\n");
            } else {
                reviewsArea.append("Total: " + upwardReviews.size() + " upward review(s)\n\n");

                // Calculate aggregated scores
                double sumPunct = 0, sumTeam = 0, sumProd = 0;
                for (UpwardReview ur : upwardReviews) {
                    sumPunct += ur.getPunctualityScore();
                    sumTeam += ur.getTeamworkScore();
                    sumProd += ur.getProductivityScore();
                }
                int count = upwardReviews.size();

                reviewsArea.append("───────────────────────────────────────────\n");
                reviewsArea.append("AGGREGATED SCORES:\n");
                reviewsArea.append("───────────────────────────────────────────\n");
                reviewsArea.append(String.format("  Punctuality:  %.1f / 10\n", sumPunct / count));
                reviewsArea.append(String.format("  Teamwork:     %.1f / 10\n", sumTeam / count));
                reviewsArea.append(String.format("  Productivity: %.1f / 10\n", sumProd / count));
                double overall = (sumPunct / count + sumTeam / count + sumProd / count) / 3.0;
                reviewsArea.append("  ─────────────────\n");
                reviewsArea.append(String.format("  OVERALL:      %.1f / 10\n\n", overall));

                reviewsArea.append("───────────────────────────────────────────\n");
                reviewsArea.append("INDIVIDUAL REVIEWS (ANONYMOUS):\n");
                reviewsArea.append("───────────────────────────────────────────\n");
                int num = 1;
                for (UpwardReview ur : upwardReviews) {
                    reviewsArea.append("\nReview #" + num + ":\n");
                    reviewsArea.append("  Punctuality:  " + ur.getPunctualityScore() + "/10\n");
                    reviewsArea.append("  Teamwork:     " + ur.getTeamworkScore() + "/10\n");
                    reviewsArea.append("  Productivity: " + ur.getProductivityScore() + "/10\n");
                    if (ur.getComments() != null && !ur.getComments().isEmpty()) {
                        reviewsArea.append("  Comments: " + ur.getComments() + "\n");
                    }
                    num++;
                }
            }

            reviewsArea.append("\n══════════════════════════════════════════\n");
            reviewsArea.append("Note: Reviewer identities are hidden.\n");
            reviewsArea.append("You can only see scores and comments.\n");
        };
        refresh.run();

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> refresh.run());

        panel.add(new JScrollPane(reviewsArea), BorderLayout.CENTER);
        panel.add(refreshBtn, BorderLayout.SOUTH);
        return panel;
    }
}
