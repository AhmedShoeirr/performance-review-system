package com.performance.gui;

import com.performance.model.*;
import com.performance.util.DataStore;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ManagerPanel extends JPanel {
    private Manager manager;

    public ManagerPanel(Manager manager) {
        this.manager = manager;
        setLayout(new BorderLayout());

        // Top panel with logout
        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel welcomeLabel = new JLabel("Welcome, " + manager.getName() + " (Manager)", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        JButton logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(e -> logout());

        topPanel.add(welcomeLabel, BorderLayout.CENTER);
        topPanel.add(logoutBtn, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Main tabbed pane
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Create Review", createReviewPanel());
        tabbedPane.addTab("Training", createTrainingPanel());
        tabbedPane.addTab("Team Goals", createTeamGoalsPanel());
        tabbedPane.addTab("My Reviews", createMyReviewsPanel());
        tabbedPane.addTab("My Goals", createMyGoalsPanel());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private void logout() {
        SwingUtilities.getWindowAncestor(this).dispose();
        new LoginFrame().setVisible(true);
    }

    private JPanel createReviewPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JComboBox<Employee> employeeCombo = new JComboBox<>();
        for (Employee e : manager.getDirectReports()) {
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
            Employee emp = (Employee) employeeCombo.getSelectedItem();
            if (emp == null) {
                JOptionPane.showMessageDialog(this, "Please select an employee.");
                return;
            }

            Review review = new Review(emp, manager);
            review.setPunctualityScore(punctualitySlider.getValue());
            review.setTeamworkScore(teamworkSlider.getValue());
            review.setProductivityScore(productivitySlider.getValue());
            review.setComments(commentsArea.getText());
            review.setStatus("SUBMITTED");

            DataStore.getInstance().addReview(review);
            JOptionPane.showMessageDialog(this, "Review submitted successfully!");

            commentsArea.setText("");
            punctualitySlider.setValue(5);
            teamworkSlider.setValue(5);
            productivitySlider.setValue(5);
        });

        draftBtn.addActionListener(e -> {
            Employee emp = (Employee) employeeCombo.getSelectedItem();
            if (emp == null) {
                JOptionPane.showMessageDialog(this, "Please select an employee.");
                return;
            }

            Review review = new Review(emp, manager);
            review.setPunctualityScore(punctualitySlider.getValue());
            review.setTeamworkScore(teamworkSlider.getValue());
            review.setProductivityScore(productivitySlider.getValue());
            review.setComments(commentsArea.getText());
            review.setStatus("DRAFT");

            DataStore.getInstance().addReview(review);
            JOptionPane.showMessageDialog(this, "Review saved as draft.");
        });

        return panel;
    }

    private JPanel createTrainingPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Recommend Training"));

        JComboBox<Employee> employeeCombo = new JComboBox<>();
        for (Employee e : manager.getDirectReports()) {
            employeeCombo.addItem(e);
        }

        JComboBox<String> courseCombo = new JComboBox<>();
        courseCombo.setEditable(true);
        courseCombo.addItem("-- Select or Type Custom --");
        for (String c : DataStore.getInstance().getAvailableCourses()) {
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

        recommendBtn.addActionListener(e -> {
            Employee emp = (Employee) employeeCombo.getSelectedItem();
            String course = (String) courseCombo.getEditor().getItem();
            if (course != null)
                course = course.trim();

            if (emp != null && course != null && !course.isEmpty() && !course.equals("-- Select or Type Custom --")) {
                TrainingRecommendation tr = new TrainingRecommendation(manager, emp, course);
                DataStore.getInstance().addTrainingRecommendation(tr);
                JOptionPane.showMessageDialog(this, "Training recommended to " + emp.getUsername() + "!");
            } else {
                JOptionPane.showMessageDialog(this, "Please select employee and enter course name.");
            }
        });

        return panel;
    }

    private JPanel createTeamGoalsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JTextArea goalsArea = new JTextArea();
        goalsArea.setEditable(false);

        StringBuilder sb = new StringBuilder("Team Goals:\n\n");
        for (Employee emp : manager.getDirectReports()) {
            sb.append(emp.getName()).append(":\n");
            for (Goal g : DataStore.getInstance().getGoals()) {
                if (g.getEmployee() != null && g.getEmployee().getUsername().equals(emp.getUsername())) {
                    sb.append("  - ").append(g.getDescription()).append(" [").append(g.getStatus()).append("]\n");
                }
            }
            sb.append("\n");
        }
        goalsArea.setText(sb.toString());

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> {
            StringBuilder refreshSb = new StringBuilder("Team Goals:\n\n");
            for (Employee emp : manager.getDirectReports()) {
                refreshSb.append(emp.getName()).append(":\n");
                for (Goal g : DataStore.getInstance().getGoals()) {
                    if (g.getEmployee().getUsername().equals(emp.getUsername())) {
                        refreshSb.append("  - ").append(g.getDescription()).append(" [").append(g.getStatus())
                                .append("]\n");
                    }
                }
                refreshSb.append("\n");
            }
            goalsArea.setText(refreshSb.toString());
        });

        panel.add(new JScrollPane(goalsArea), BorderLayout.CENTER);
        panel.add(refreshBtn, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createMyReviewsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JTextArea reviewArea = new JTextArea();
        reviewArea.setEditable(false);

        // Get reviews where the manager is being reviewed (as an employee in hierarchy)
        List<Review> allReviews = DataStore.getInstance().getReviews();
        boolean found = false;
        for (Review r : allReviews) {
            if (r.getEmployee() != null && r.getEmployee().getUsername().equals(manager.getUsername())) {
                String mgrName = (r.getManager() != null) ? r.getManager().getName() : "Unknown";
                reviewArea.append("Review by " + mgrName + ":\n");
                reviewArea.append("  Punctuality: " + r.getPunctualityScore() + "\n");
                reviewArea.append("  Teamwork: " + r.getTeamworkScore() + "\n");
                reviewArea.append("  Productivity: " + r.getProductivityScore() + "\n");
                reviewArea.append("  Total: " + r.calculateTotalScore() + "\n");
                reviewArea.append("  Comments: " + r.getComments() + "\n\n");
                found = true;
            }
        }
        if (!found) {
            reviewArea.setText("No reviews received yet.");
        }

        panel.add(new JScrollPane(reviewArea), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createMyGoalsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        DefaultListModel<String> listModel = new DefaultListModel<>();
        JList<String> goalsList = new JList<>(listModel);

        JPanel inputPanel = new JPanel(new BorderLayout());
        JTextField goalField = new JTextField();
        JButton addBtn = new JButton("Add Goal");
        inputPanel.add(goalField, BorderLayout.CENTER);
        inputPanel.add(addBtn, BorderLayout.EAST);

        Runnable refresh = () -> {
            listModel.clear();
            for (Goal g : DataStore.getInstance().getGoals()) {
                if (g.getEmployee() != null && g.getEmployee().getUsername().equals(manager.getUsername())) {
                    listModel.addElement(g.toString());
                }
            }
        };
        refresh.run();

        addBtn.addActionListener(e -> {
            String desc = goalField.getText().trim();
            if (!desc.isEmpty()) {
                // Manager can have goals too - they're also employees in hierarchy
                Goal g = new Goal(manager, desc);
                DataStore.getInstance().addGoal(g);
                refresh.run();
                goalField.setText("");
            }
        });

        panel.add(new JScrollPane(goalsList), BorderLayout.CENTER);
        panel.add(inputPanel, BorderLayout.SOUTH);

        return panel;
    }
}
