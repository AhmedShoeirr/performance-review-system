package com.performance.gui;

import com.performance.model.*;
import com.performance.service.*;
import com.performance.util.DataStore;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class AdminPanel extends JPanel {
    private JTable userTable;
    private DefaultTableModel tableModel;
    private JComboBox<User> managerCombo;
    private JComboBox<User> employeeCombo;
    private JComboBox<Integer> tierCombo;

    public AdminPanel() {
        setLayout(new BorderLayout());

        // Top panel with logout
        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel welcomeLabel = new JLabel("Admin Dashboard", SwingConstants.CENTER);
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
        tabbedPane.addTab("User Management", createUserManagementPanel());
        tabbedPane.addTab("Hierarchy Management", createHierarchyPanel());
        tabbedPane.addTab("📋 Approval Queue", createApprovalQueuePanel()); // Manager requests
        tabbedPane.addTab("👥 All Employees", createAllEmployeesPanel()); // Full view
        tabbedPane.addTab("🎯 All Goals", createAllGoalsPanel()); // Goal management
        tabbedPane.addTab("Training Management", createTrainingPanel());
        tabbedPane.addTab("Training Monitor", createTrainingMonitorPanel());
        tabbedPane.addTab("Reviews Monitor", createReviewsMonitorPanel());
        tabbedPane.addTab("Create Review", createAdminReviewPanel());
        tabbedPane.addTab("📊 Reports", createReportsPanel());
        tabbedPane.addTab("System Settings", createSettingsPanel());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createReportsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Left side - report buttons
        JPanel buttonPanel = new JPanel(new GridLayout(5, 1, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JButton exportAllBtn = new JButton("📄 Export All Users Report (PDF)");
        JButton exportSelectedBtn = new JButton("📋 Export Selected User Report");
        JButton upwardFeedbackBtn = new JButton("📊 Upward Feedback Report");
        JButton topPerformersBtn = new JButton("🏆 Top Performers Report");
        JButton skillGapBtn = new JButton("📈 Skill Gap Analysis");

        buttonPanel.add(exportAllBtn);
        buttonPanel.add(exportSelectedBtn);
        buttonPanel.add(upwardFeedbackBtn);
        buttonPanel.add(topPerformersBtn);
        buttonPanel.add(skillGapBtn);

        panel.add(buttonPanel, BorderLayout.WEST);

        // Right side - report display area
        JTextArea reportArea = new JTextArea();
        reportArea.setEditable(false);
        reportArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        reportArea.setText("Select a report to view...");
        panel.add(new JScrollPane(reportArea), BorderLayout.CENTER);

        exportAllBtn.addActionListener(e -> com.performance.service.ReportExporter.exportAllUsersReport(this));

        exportSelectedBtn.addActionListener(e -> {
            User[] users = DataStore.getInstance().getUsers().stream()
                    .filter(u -> !(u instanceof Admin))
                    .toArray(User[]::new);
            if (users.length == 0) {
                JOptionPane.showMessageDialog(this, "No users to export.");
                return;
            }
            User selected = (User) JOptionPane.showInputDialog(this, "Select user:", "Export Report",
                    JOptionPane.QUESTION_MESSAGE, null, users, users[0]);
            if (selected != null) {
                com.performance.service.ReportExporter.exportUserReport(selected, this);
            }
        });

        upwardFeedbackBtn.addActionListener(e -> {
            com.performance.service.ReportGenerator gen = new com.performance.service.ReportGenerator();
            reportArea.setText(gen.generateUpwardFeedbackReport());
        });

        topPerformersBtn.addActionListener(e -> {
            // Select a manager to view their team's top performers
            User[] managers = DataStore.getInstance().getUsers().stream()
                    .filter(u -> u.getDirectReports() != null && !u.getDirectReports().isEmpty())
                    .toArray(User[]::new);
            if (managers.length == 0) {
                reportArea.setText("No managers with direct reports found.");
                return;
            }
            User selected = (User) JOptionPane.showInputDialog(this, "Select manager:", "Top Performers",
                    JOptionPane.QUESTION_MESSAGE, null, managers, managers[0]);
            if (selected != null) {
                com.performance.service.ReportGenerator gen = new com.performance.service.ReportGenerator();
                reportArea.setText(gen.generateTopPerformersReport(selected));
            }
        });

        skillGapBtn.addActionListener(e -> {
            // Select a manager to analyze their team's skill gaps
            User[] managers = DataStore.getInstance().getUsers().stream()
                    .filter(u -> u.getDirectReports() != null && !u.getDirectReports().isEmpty())
                    .toArray(User[]::new);
            if (managers.length == 0) {
                reportArea.setText("No managers with direct reports found.");
                return;
            }
            User selected = (User) JOptionPane.showInputDialog(this, "Select manager:", "Skill Gap Analysis",
                    JOptionPane.QUESTION_MESSAGE, null, managers, managers[0]);
            if (selected != null) {
                com.performance.service.ReportGenerator gen = new com.performance.service.ReportGenerator();
                reportArea.setText(gen.generateSkillGapAnalysis(selected));
            }
        });

        return panel;
    }

    private JPanel createSettingsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(new JLabel("Max Hierarchy Tiers:"));
        JSpinner tierSpinner = new JSpinner(new SpinnerNumberModel(DataStore.MAX_TIERS, 1, 10, 1));
        JButton applyButton = new JButton("Apply Changes");

        applyButton.addActionListener(e -> {
            int newVal = (int) tierSpinner.getValue();
            if (newVal != DataStore.MAX_TIERS) {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Changing hierarchy levels will RESET all users to PENDING and CLEAR hierarchy. Continue?",
                        "Confirm Critical Change", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    DataStore.MAX_TIERS = newVal;
                    resetSystem();
                    DataStore.getInstance().saveData();
                } else {
                    tierSpinner.setValue(DataStore.MAX_TIERS);
                }
            }
        });

        panel.add(tierSpinner);
        panel.add(applyButton);
        return panel;
    }

    private void resetSystem() {
        UserService.getInstance().resetSystem();
        refreshUserTable();
        refreshCombos();
        JOptionPane.showMessageDialog(this, "System reset complete. All non-admin users are now PENDING.");
    }

    private JPanel createUserManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Table
        String[] columns = { "Username", "Name", "Role", "Status", "Tier" };
        tableModel = new DefaultTableModel(columns, 0);
        userTable = new JTable(tableModel);
        refreshUserTable();

        panel.add(new JScrollPane(userTable), BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("Add User");
        JButton editButton = new JButton("Edit User");
        JButton suspendButton = new JButton("Suspend/Activate");
        JButton deleteButton = new JButton("Delete User");

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(suspendButton);
        buttonPanel.add(deleteButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        addButton.addActionListener(e -> showAddUserDialog());
        editButton.addActionListener(e -> editUser());
        suspendButton.addActionListener(e -> toggleUserStatus());
        deleteButton.addActionListener(e -> deleteUser());

        return panel;
    }

    private void toggleUserStatus() {
        int row = userTable.getSelectedRow();
        if (row == -1)
            return;

        String username = (String) tableModel.getValueAt(row, 0);
        User u = UserService.getInstance().findByUsername(username);
        if (u == null)
            return;

        boolean success = UserService.getInstance().toggleUserStatus(u);
        if (!success && u.getStatus() != UserStatus.SUSPENDED) {
            JOptionPane.showMessageDialog(this,
                    "Cannot suspend: User has direct reports. Reassign them first.");
            return;
        }
        refreshUserTable();
    }

    private void deleteUser() {
        int row = userTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user to delete.");
            return;
        }
        String username = (String) tableModel.getValueAt(row, 0);
        User u = UserService.getInstance().findByUsername(username);
        if (u == null)
            return;

        if (u instanceof Admin) {
            JOptionPane.showMessageDialog(this, "Cannot delete Admin users.");
            return;
        }

        // Check if user has direct reports
        if (u.getDirectReports() != null && !u.getDirectReports().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Cannot delete: User has " + u.getDirectReports().size() + " direct reports. Reassign them first.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Delete user " + u.getName() + "?", "Confirm Delete",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            UserService.getInstance().deleteUser(u);
            refreshUserTable();
            refreshCombos();
        }
    }

    private void editUser() {
        int row = userTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user to edit.");
            return;
        }

        String username = (String) tableModel.getValueAt(row, 0);
        User u = findUser(username);
        if (u == null)
            return;

        if (u instanceof Admin) {
            JOptionPane.showMessageDialog(this, "Cannot edit admin user.");
            return;
        }

        // Create edit dialog
        JPanel editPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        JTextField nameField = new JTextField(u.getName(), 20);
        JLabel usernameLabel = new JLabel(u.getUsername());
        JLabel currentRole = new JLabel((String) tableModel.getValueAt(row, 2));

        editPanel.add(new JLabel("Username:"));
        editPanel.add(usernameLabel);
        editPanel.add(new JLabel("Name:"));
        editPanel.add(nameField);
        editPanel.add(new JLabel("Current Role:"));
        editPanel.add(currentRole);

        int result = JOptionPane.showConfirmDialog(this, editPanel, "Edit User",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String newName = nameField.getText().trim();
            if (!newName.isEmpty()) {
                u.setName(newName);
                DataStore.getInstance().saveData();
                refreshUserTable();
                JOptionPane.showMessageDialog(this, "User updated successfully.");
            } else {
                JOptionPane.showMessageDialog(this, "Name cannot be empty.");
            }
        }
    }

    private boolean canModifyUser(User u) {
        int tier = u.getTierLevel();
        if (tier <= 1)
            return true;

        long usersInTier = DataStore.getInstance().getUsers().stream().filter(user -> user.getTierLevel() == tier)
                .count();
        if (usersInTier > 1)
            return true;

        long usersInLowerTier = DataStore.getInstance().getUsers().stream()
                .filter(user -> user.getTierLevel() == tier - 1).count();
        return usersInLowerTier == 0;
    }

    private User findUser(String username) {
        for (User user : DataStore.getInstance().getUsers()) {
            if (user.getUsername().equals(username))
                return user;
        }
        return null;
    }

    private JPanel createHierarchyPanel() {
        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));

        JLabel infoLabel = new JLabel(
                "Note: Tier 1 = Lowest (Employee), Tier " + DataStore.MAX_TIERS + " = Highest (Highboard)");
        infoLabel.setForeground(Color.BLUE);
        panel.add(infoLabel);
        panel.add(new JLabel("")); // Spacer

        employeeCombo = new JComboBox<>();
        managerCombo = new JComboBox<>();
        tierCombo = new JComboBox<>();

        // Custom Renderer
        ListCellRenderer<Object> userRenderer = new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof User) {
                    User u = (User) value;
                    setText(u.getUsername() + " (" + u.getName() + ")");
                }
                return this;
            }
        };
        employeeCombo.setRenderer(userRenderer);
        managerCombo.setRenderer(userRenderer);

        refreshCombos();
        // Tiers are populated by refreshCombos()

        panel.add(new JLabel("Select Employee:"));
        panel.add(employeeCombo);
        panel.add(new JLabel("Select Tier:"));
        panel.add(tierCombo);
        panel.add(new JLabel("Select Manager (Tier + 1):"));
        panel.add(managerCombo);

        JButton assignButton = new JButton("Assign / Update");
        panel.add(new JLabel(""));
        panel.add(assignButton);

        // Logic to update manager combo based on tier selection
        tierCombo.addActionListener(e -> updateManagerSelection());
        employeeCombo.addActionListener(e -> {
            User emp = (User) employeeCombo.getSelectedItem();
            if (emp != null) {
                tierCombo.setSelectedItem(emp.getTierLevel());
                updateManagerSelection();
            }
        });

        assignButton.addActionListener(e -> assignHierarchy());

        return panel;
    }

    private void updateManagerSelection() {
        Integer selectedTier = (Integer) tierCombo.getSelectedItem();
        User selectedEmployee = (User) employeeCombo.getSelectedItem();

        if (selectedTier == null)
            return;

        managerCombo.removeAllItems();
        managerCombo.setEnabled(true);

        if (selectedTier == DataStore.MAX_TIERS) {
            managerCombo.setEnabled(false); // Top tier has no manager
        } else {
            int targetManagerTier = selectedTier + 1;
            List<User> eligibleManagers = DataStore.getInstance().getUsers().stream()
                    .filter(u -> u.getTierLevel() == targetManagerTier)
                    .filter(u -> selectedEmployee == null || !u.getUsername().equals(selectedEmployee.getUsername())) // Exclude
                                                                                                                      // self
                    .collect(Collectors.toList());

            for (User m : eligibleManagers) {
                managerCombo.addItem(m);
            }
        }
    }

    private void assignHierarchy() {
        User emp = (User) employeeCombo.getSelectedItem();
        Integer tier = (Integer) tierCombo.getSelectedItem();
        User mgr = (User) managerCombo.getSelectedItem();

        if (emp == null || tier == null)
            return;

        // Validation: Cannot assign to Tier K if Tier K+1 is empty (unless K is Max)
        if (tier < DataStore.MAX_TIERS) {
            boolean upperTierHasUsers = DataStore.getInstance().getUsers().stream()
                    .anyMatch(u -> u.getTierLevel() == tier + 1);
            if (!upperTierHasUsers) {
                JOptionPane.showMessageDialog(this,
                        "Cannot assign to Tier " + tier + " because Tier " + (tier + 1) + " is empty.");
                return;
            }
            if (mgr == null) {
                JOptionPane.showMessageDialog(this, "Please select a manager from Tier " + (tier + 1));
                return;
            }
        }

        // Apply changes
        emp.setTierLevel(tier);
        emp.setStatus(UserStatus.ACTIVE); // Auto-activate on assignment

        // Set role based on tier
        if (tier == DataStore.MAX_TIERS) {
            emp.setRole(UserRole.HIGHBOARD_MANAGER);
        } else if (tier == 1) {
            emp.setRole(UserRole.EMPLOYEE);
        } else {
            emp.setRole(UserRole.MANAGER_EMPLOYEE);
        }

        if (tier < DataStore.MAX_TIERS) {
            emp.setManager(mgr);
            mgr.addDirectReport(emp);
        } else {
            emp.setManager(null); // Top tier
        }

        DataStore.getInstance().saveData();
        refreshUserTable();
        JOptionPane.showMessageDialog(this, "Hierarchy updated. User is now ACTIVE.");
    }

    private void refreshUserTable() {
        tableModel.setRowCount(0);
        for (User u : DataStore.getInstance().getUsers()) {
            String role = calculateRole(u);
            tableModel.addRow(new Object[] { u.getUsername(), u.getName(), role, u.getStatus(), u.getTierLevel() });
        }
    }

    private String calculateRole(User u) {
        if (u instanceof Admin)
            return "Admin";

        int tier = u.getTierLevel();
        boolean hasDirectReports = u.getDirectReports() != null && !u.getDirectReports().isEmpty();

        if (tier == DataStore.MAX_TIERS) {
            return "Highboard Manager";
        } else if (tier == 1 && !hasDirectReports) {
            return "Employee";
        } else {
            return "Employee and Manager";
        }
    }

    private void refreshCombos() {
        managerCombo.removeAllItems();
        employeeCombo.removeAllItems();
        tierCombo.removeAllItems();

        for (User u : DataStore.getInstance().getUsers()) {
            employeeCombo.addItem(u);
        }

        for (int i = 1; i <= DataStore.MAX_TIERS; i++) {
            tierCombo.addItem(i);
        }
    }

    private void showAddUserDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add User", true);
        dialog.setSize(300, 200);
        dialog.setLayout(new GridLayout(4, 2, 5, 5));

        JTextField userField = new JTextField();
        JPasswordField passField = new JPasswordField();
        JTextField nameField = new JTextField();
        JButton saveButton = new JButton("Save");

        dialog.add(new JLabel("Username:"));
        dialog.add(userField);
        dialog.add(new JLabel("Password:"));
        dialog.add(passField);
        dialog.add(new JLabel("Name:"));
        dialog.add(nameField);
        dialog.add(new JLabel(""));
        dialog.add(saveButton);

        saveButton.addActionListener(e -> {
            String u = userField.getText().trim();
            String p = new String(passField.getPassword());
            String n = nameField.getText().trim();

            if (u.isEmpty() || p.isEmpty() || n.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "All fields required.");
                return;
            }

            // Check if username already exists
            for (User existing : DataStore.getInstance().getUsers()) {
                if (existing.getUsername().equalsIgnoreCase(u)) {
                    JOptionPane.showMessageDialog(dialog, "Username already exists.");
                    return;
                }
            }

            // Always create as Employee with PENDING status
            // Role will be assigned when hierarchy is set
            Employee newUser = new Employee(u, p, n);
            newUser.setStatus(UserStatus.PENDING);

            DataStore.getInstance().addUser(newUser);
            refreshUserTable();
            refreshCombos();
            dialog.dispose();
            JOptionPane.showMessageDialog(this, "User created with PENDING status.\nAssign hierarchy to activate.");
        });

        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private JPanel createTrainingPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // General Courses
        JPanel generalPanel = new JPanel(new BorderLayout());
        generalPanel.setBorder(BorderFactory.createTitledBorder("General Courses"));
        DefaultListModel<String> listModel = new DefaultListModel<>();
        for (String c : TrainingService.getInstance().getAvailableCourses()) {
            listModel.addElement(c);
        }
        JList<String> courseList = new JList<>(listModel);
        generalPanel.add(new JScrollPane(courseList), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel();
        JTextField courseField = new JTextField(15);
        JButton addBtn = new JButton("Add");
        JButton removeBtn = new JButton("Remove");

        btnPanel.add(courseField);
        btnPanel.add(addBtn);
        btnPanel.add(removeBtn);
        generalPanel.add(btnPanel, BorderLayout.SOUTH);

        addBtn.addActionListener(e -> {
            String c = courseField.getText().trim();
            if (!c.isEmpty() && !listModel.contains(c)) {
                TrainingService.getInstance().addCourse(c);
                listModel.addElement(c);
                courseField.setText("");
            }
        });

        removeBtn.addActionListener(e -> {
            String c = courseList.getSelectedValue();
            if (c != null) {
                TrainingService.getInstance().removeCourse(c);
                listModel.removeElement(c);
            }
        });

        // Specific Recommendations
        JPanel specificPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        specificPanel.setBorder(BorderFactory.createTitledBorder("Recommend to User"));

        JComboBox<User> userCombo = new JComboBox<>();
        for (User u : UserService.getInstance().getNonAdminUsers()) {
            userCombo.addItem(u);
        }
        // Use same renderer
        userCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof User) {
                    User u = (User) value;
                    setText(u.getUsername() + " (" + u.getName() + ")");
                }
                return this;
            }
        });

        JComboBox<String> courseCombo = new JComboBox<>();
        courseCombo.setEditable(true); // Allow custom input
        for (String c : TrainingService.getInstance().getAvailableCourses()) {
            courseCombo.addItem(c);
        }

        JButton recommendBtn = new JButton("Recommend");

        specificPanel.add(new JLabel("User:"));
        specificPanel.add(userCombo);
        specificPanel.add(new JLabel("Course (select or type):"));
        specificPanel.add(courseCombo);
        specificPanel.add(new JLabel(""));
        specificPanel.add(recommendBtn);

        recommendBtn.addActionListener(e -> {
            User target = (User) userCombo.getSelectedItem();
            String course = (String) courseCombo.getEditor().getItem();
            if (course != null) {
                course = course.trim();
            }
            if (target != null && course != null && !course.isEmpty()) {
                User admin = UserService.getInstance().getAllUsers().stream()
                        .filter(u -> u instanceof Admin).findFirst().orElse(null);
                TrainingService.getInstance().createRecommendation(admin, target, course);
                JOptionPane.showMessageDialog(this, "Recommendation sent.");
            }
        });

        // Split Pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, generalPanel, specificPanel);
        splitPane.setDividerLocation(200);
        panel.add(splitPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createTrainingMonitorPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        String[] columns = { "Recommender", "Employee", "Course", "Date" };
        DefaultTableModel trainingTableModel = new DefaultTableModel(columns, 0);
        JTable table = new JTable(trainingTableModel);

        JLabel titleLabel = new JLabel("All Training Recommendations (Admin + Managers)", SwingConstants.CENTER);
        titleLabel.setFont(titleLabel.getFont().deriveFont(16f));
        panel.add(titleLabel, BorderLayout.NORTH);

        // Refresh function
        Runnable refreshTraining = () -> {
            trainingTableModel.setRowCount(0);
            for (TrainingRecommendation tr : TrainingService.getInstance().getAllRecommendations()) {
                String recommender = tr.getRecommenderName();
                String employee = tr.getRecipientName();
                String course = tr.getCourseName();
                String date = tr.getDateRecommended() != null ? tr.getDateRecommended().toString() : "N/A";
                trainingTableModel.addRow(new Object[] { recommender, employee, course, date });
            }
        };
        refreshTraining.run();

        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel();
        JButton refreshBtn = new JButton("Refresh");
        JButton deleteBtn = new JButton("Delete Selected");
        refreshBtn.addActionListener(e -> refreshTraining.run());
        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                List<TrainingRecommendation> recs = DataStore.getInstance().getTrainingRecommendations();
                if (row < recs.size()) {
                    recs.remove(row);
                    DataStore.getInstance().saveData();
                    refreshTraining.run();
                }
            }
        });
        btnPanel.add(refreshBtn);
        btnPanel.add(deleteBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createReviewsMonitorPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Top: User selection
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("Select User:"));
        JComboBox<User> userCombo = new JComboBox<>();
        userCombo.addItem(null); // "All users" option
        for (User u : DataStore.getInstance().getUsers()) {
            if (!(u instanceof Admin)) {
                userCombo.addItem(u);
            }
        }
        userCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value == null) {
                    setText("-- All Users --");
                } else {
                    setText(((User) value).getName());
                }
                return this;
            }
        });
        filterPanel.add(userCombo);

        // Score display (for selected user)
        JTextArea scoreArea = new JTextArea(4, 30);
        scoreArea.setEditable(false);
        scoreArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        filterPanel.add(new JScrollPane(scoreArea));

        panel.add(filterPanel, BorderLayout.NORTH);

        // Table
        String[] columns = { "Employee", "Reviewer", "Status", "Punct", "Team", "Prod", "Lead", "Vision", "Mgr",
                "Total" };
        DefaultTableModel reviewTableModel = new DefaultTableModel(columns, 0);
        JTable table = new JTable(reviewTableModel);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        // Refresh function
        Runnable refreshTable = () -> {
            reviewTableModel.setRowCount(0);
            User selected = (User) userCombo.getSelectedItem();

            for (Review review : DataStore.getInstance().getReviews()) {
                if (selected != null) {
                    if (review.getEmployee() == null
                            || !review.getEmployee().getUsername().equals(selected.getUsername())) {
                        continue;
                    }
                }
                String employee = review.getEmployee() != null ? review.getEmployee().getName() : "Unknown";
                String reviewer = review.getManager() != null ? review.getManager().getName() : "Unknown";
                String status = review.getStatus() != null ? review.getStatus() : "N/A";
                int punct = review.getPunctualityScore();
                int team = review.getTeamworkScore();
                int prod = review.getProductivityScore();
                int lead = review.getLeadershipScore();
                int vision = review.getVisionScore();
                int mgr = review.getManagerialSkillsScore();
                int total = review.calculateTotalScore();
                reviewTableModel.addRow(
                        new Object[] { employee, reviewer, status, punct, team, prod, lead, vision, mgr, total });
            }

            // Show calculated score for selected user
            if (selected != null) {
                com.performance.service.PerformanceCalculator.ScoreResult result = com.performance.service.PerformanceCalculator
                        .calculateScore(selected);
                StringBuilder sb = new StringBuilder();
                sb.append("Weighted Score for ").append(selected.getName()).append(":\n");
                sb.append(String.format("Punctuality: %.1f | Teamwork: %.1f | Productivity: %.1f | Overall: %.1f\n",
                        result.punctuality, result.teamwork, result.productivity, result.overall));
                sb.append("Breakdown:\n").append(result.breakdown);
                scoreArea.setText(sb.toString());
            } else {
                scoreArea.setText("Select a user to see weighted performance score.");
            }
        };

        userCombo.addActionListener(e -> refreshTable.run());
        refreshTable.run();

        // Button panel with refresh and delete
        JPanel btnPanel = new JPanel();
        JButton refreshBtn = new JButton("Refresh");
        JButton deleteBtn = new JButton("Delete Selected");

        refreshBtn.addActionListener(e -> refreshTable.run());

        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Please select a review to delete.");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete this review?",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                User selected = (User) userCombo.getSelectedItem();
                List<Review> reviews = DataStore.getInstance().getReviews();

                // Find the matching review based on table row
                int matchIndex = 0;
                for (int i = 0; i < reviews.size(); i++) {
                    Review review = reviews.get(i);
                    if (selected != null) {
                        if (review.getEmployee() == null
                                || !review.getEmployee().getUsername().equals(selected.getUsername())) {
                            continue;
                        }
                    }
                    if (matchIndex == row) {
                        reviews.remove(i);
                        DataStore.getInstance().saveData();
                        refreshTable.run();
                        JOptionPane.showMessageDialog(this, "Review deleted successfully.");
                        break;
                    }
                    matchIndex++;
                }
            }
        });

        btnPanel.add(refreshBtn);
        btnPanel.add(deleteBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createAdminReviewPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Employee selection
        JPanel selectPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        selectPanel.add(new JLabel("Select Employee to Review:"));
        JComboBox<User> employeeCombo = new JComboBox<>();
        for (User u : DataStore.getInstance().getUsers()) {
            if (!(u instanceof Admin)) {
                employeeCombo.addItem(u);
            }
        }
        selectPanel.add(employeeCombo);
        formPanel.add(selectPanel);

        // Basic sliders (all employees)
        JSlider punctualitySlider = createSlider();
        JSlider teamworkSlider = createSlider();
        JSlider productivitySlider = createSlider();

        JPanel basicPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        basicPanel.setBorder(BorderFactory.createTitledBorder("Basic Categories (All Employees)"));
        basicPanel.add(new JLabel("Punctuality (1-10):"));
        basicPanel.add(punctualitySlider);
        basicPanel.add(new JLabel("Teamwork (1-10):"));
        basicPanel.add(teamworkSlider);
        basicPanel.add(new JLabel("Productivity (1-10):"));
        basicPanel.add(productivitySlider);
        formPanel.add(basicPanel);

        // Extended sliders (managers only)
        JSlider leadershipSlider = createSlider();
        JSlider visionSlider = createSlider();
        JSlider managerialSlider = createSlider();

        JPanel extendedPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        extendedPanel.setBorder(BorderFactory.createTitledBorder("Manager Categories (Managers/Highboard Only)"));
        extendedPanel.add(new JLabel("Leadership (1-10):"));
        extendedPanel.add(leadershipSlider);
        extendedPanel.add(new JLabel("Vision (1-10):"));
        extendedPanel.add(visionSlider);
        extendedPanel.add(new JLabel("Managerial Skills (1-10):"));
        extendedPanel.add(managerialSlider);
        formPanel.add(extendedPanel);

        // Comments
        JTextArea commentsArea = new JTextArea(3, 30);
        JPanel commentPanel = new JPanel(new BorderLayout());
        commentPanel.setBorder(BorderFactory.createTitledBorder("Comments"));
        commentPanel.add(new JScrollPane(commentsArea), BorderLayout.CENTER);
        formPanel.add(commentPanel);

        // Submit button
        JButton submitBtn = new JButton("Submit Admin Review");
        JPanel btnPanel = new JPanel();
        btnPanel.add(submitBtn);
        formPanel.add(btnPanel);

        panel.add(new JScrollPane(formPanel), BorderLayout.CENTER);

        // Dynamic enable/disable of extended categories
        Runnable updateExtended = () -> {
            User selected = (User) employeeCombo.getSelectedItem();
            boolean isManager = false;
            if (selected != null) {
                int tier = selected.getTierLevel();
                boolean hasReports = selected.getDirectReports() != null && !selected.getDirectReports().isEmpty();
                isManager = tier > 1 || hasReports;
            }
            leadershipSlider.setEnabled(isManager);
            visionSlider.setEnabled(isManager);
            managerialSlider.setEnabled(isManager);
            extendedPanel.setVisible(isManager);
        };
        employeeCombo.addActionListener(e -> updateExtended.run());
        updateExtended.run();

        submitBtn.addActionListener(e -> {
            User emp = (User) employeeCombo.getSelectedItem();
            if (emp == null) {
                JOptionPane.showMessageDialog(this, "Please select an employee.");
                return;
            }

            // Create review with Admin as reviewer
            Admin admin = null;
            for (User u : DataStore.getInstance().getUsers()) {
                if (u instanceof Admin) {
                    admin = (Admin) u;
                    break;
                }
            }

            Review review = new Review(emp, admin);
            review.setPunctualityScore(punctualitySlider.getValue());
            review.setTeamworkScore(teamworkSlider.getValue());
            review.setProductivityScore(productivitySlider.getValue());

            // Set extended categories if applicable
            if (extendedPanel.isVisible()) {
                review.setLeadershipScore(leadershipSlider.getValue());
                review.setVisionScore(visionSlider.getValue());
                review.setManagerialSkillsScore(managerialSlider.getValue());
            }

            review.setComments(commentsArea.getText());
            DataStore.getInstance().addReview(review);
            JOptionPane.showMessageDialog(this, "Admin review submitted for " + emp.getName() + "!");

            // Reset form
            punctualitySlider.setValue(5);
            teamworkSlider.setValue(5);
            productivitySlider.setValue(5);
            leadershipSlider.setValue(5);
            visionSlider.setValue(5);
            managerialSlider.setValue(5);
            commentsArea.setText("");
        });

        return panel;
    }

    private JSlider createSlider() {
        JSlider slider = new JSlider(1, 10, 5);
        slider.setMajorTickSpacing(1);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        return slider;
    }

    // ==================== Admin-Only Panels ====================

    /**
     * Panel for approving/rejecting manager requests
     */
    private JPanel createApprovalQueuePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JTextArea queueArea = new JTextArea();
        queueArea.setEditable(false);
        queueArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        DefaultListModel<PendingAction> listModel = new DefaultListModel<>();
        JList<PendingAction> actionList = new JList<>(listModel);

        Runnable refresh = () -> {
            listModel.clear();
            List<PendingAction> pending = ApprovalService.getInstance().getPendingRequests();
            for (PendingAction pa : pending) {
                listModel.addElement(pa);
            }
            if (pending.isEmpty()) {
                queueArea.setText("No pending requests.");
            } else {
                queueArea.setText("Select a request from the list.\n\n");
                queueArea.append("Pending: " + pending.size() + " request(s)\n");
            }
        };
        refresh.run();

        actionList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                PendingAction pa = actionList.getSelectedValue();
                if (pa != null) {
                    queueArea.setText("");
                    queueArea.append("═══════════════════════════════════════════\n");
                    queueArea.append("       PENDING REQUEST DETAILS\n");
                    queueArea.append("═══════════════════════════════════════════\n\n");
                    queueArea.append("Requested By: " + pa.getRequestedBy().getName() + "\n");
                    queueArea.append("Target User: " + pa.getTargetUser().getName() + "\n");
                    queueArea.append("Action: " + pa.getDescription() + "\n");
                    queueArea.append("Date: " + pa.getRequestedDate() + "\n");
                    queueArea.append("Status: " + pa.getStatus() + "\n");
                }
            }
        });

        JPanel btnPanel = new JPanel(new FlowLayout());
        JButton approveBtn = new JButton("✅ Approve");
        JButton rejectBtn = new JButton("❌ Reject");
        JButton refreshBtn = new JButton("🔄 Refresh");

        approveBtn.addActionListener(e -> {
            PendingAction pa = actionList.getSelectedValue();
            if (pa == null) {
                JOptionPane.showMessageDialog(panel, "Select a request first.");
                return;
            }
            Admin admin = new Admin("system", "", "System");
            if (ApprovalService.getInstance().approveRequest(pa, admin)) {
                JOptionPane.showMessageDialog(panel, "Request approved!");
                refresh.run();
            }
        });

        rejectBtn.addActionListener(e -> {
            PendingAction pa = actionList.getSelectedValue();
            if (pa == null) {
                JOptionPane.showMessageDialog(panel, "Select a request first.");
                return;
            }
            String reason = JOptionPane.showInputDialog(panel, "Rejection reason:");
            if (reason != null) {
                Admin admin = new Admin("system", "", "System");
                if (ApprovalService.getInstance().rejectRequest(pa, admin, reason)) {
                    JOptionPane.showMessageDialog(panel, "Request rejected.");
                    refresh.run();
                }
            }
        });

        refreshBtn.addActionListener(e -> refresh.run());

        btnPanel.add(approveBtn);
        btnPanel.add(rejectBtn);
        btnPanel.add(refreshBtn);

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(new JLabel("Pending Requests:"), BorderLayout.NORTH);
        leftPanel.add(new JScrollPane(actionList), BorderLayout.CENTER);
        leftPanel.setPreferredSize(new Dimension(300, 0));

        panel.add(leftPanel, BorderLayout.WEST);
        panel.add(new JScrollPane(queueArea), BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);
        return panel;
    }

    /**
     * Panel showing ALL employees with FULL review details (admin only can see who
     * reviewed whom)
     */
    private JPanel createAllEmployeesPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        DefaultListModel<User> listModel = new DefaultListModel<>();
        JList<User> userList = new JList<>(listModel);
        userList.setCellRenderer(new DefaultListCellRenderer() {
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

        JTextArea detailsArea = new JTextArea();
        detailsArea.setEditable(false);
        detailsArea.setFont(new Font("Monospaced", Font.PLAIN, 11));

        Runnable refresh = () -> {
            listModel.clear();
            for (User u : UserService.getInstance().getNonAdminUsers()) {
                listModel.addElement(u);
            }
        };
        refresh.run();

        userList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                User u = userList.getSelectedValue();
                if (u != null) {
                    detailsArea.setText("");
                    detailsArea.append("═══════════════════════════════════════════\n");
                    detailsArea.append("       EMPLOYEE DETAILS (ADMIN VIEW)\n");
                    detailsArea.append("═══════════════════════════════════════════\n\n");
                    detailsArea.append("Name: " + u.getName() + "\n");
                    detailsArea.append("Username: " + u.getUsername() + "\n");
                    detailsArea.append("Tier: " + u.getTierLevel() + "\n");
                    detailsArea.append("Status: " + u.getStatus() + "\n");
                    detailsArea.append(
                            "Manager: " + (u.getManager() != null ? u.getManager().getName() : "None") + "\n\n");

                    // FULL REVIEW DETAILS - Admin can see who reviewed whom
                    detailsArea.append("───────────────────────────────────────────\n");
                    detailsArea.append("MANAGER REVIEWS (FULL DETAILS):\n");
                    detailsArea.append("───────────────────────────────────────────\n");
                    List<Review> reviews = ReviewService.getInstance().getReviewsForUser(u);
                    if (reviews.isEmpty()) {
                        detailsArea.append("  No manager reviews.\n");
                    } else {
                        for (Review r : reviews) {
                            detailsArea.append("  Reviewer: "
                                    + (r.getReviewer() != null ? r.getReviewer().getName() : "Unknown") + "\n");
                            detailsArea.append("    Punctuality: " + r.getPunctualityScore() + "/10\n");
                            detailsArea.append("    Teamwork: " + r.getTeamworkScore() + "/10\n");
                            detailsArea.append("    Productivity: " + r.getProductivityScore() + "/10\n");
                            detailsArea.append("    Comments: " + r.getComments() + "\n\n");
                        }
                    }

                    detailsArea.append("───────────────────────────────────────────\n");
                    detailsArea.append("PEER REVIEWS (FULL DETAILS):\n");
                    detailsArea.append("───────────────────────────────────────────\n");
                    List<PeerReview> peerReviews = ReviewService.getInstance().getPeerReviewsFor(u);
                    if (peerReviews.isEmpty()) {
                        detailsArea.append("  No peer reviews.\n");
                    } else {
                        for (PeerReview pr : peerReviews) {
                            detailsArea.append("  Reviewer: "
                                    + (pr.getReviewer() != null ? pr.getReviewer().getName() : "Anonymous") + "\n");
                            detailsArea.append("    Collaboration: " + pr.getCollaborationScore() + "/5\n");
                            detailsArea.append("    Communication: " + pr.getCommunicationScore() + "/5\n");
                            detailsArea.append("    Teamwork: " + pr.getTeamworkScore() + "/5\n");
                            detailsArea.append("    Comments: " + pr.getComments() + "\n\n");
                        }
                    }

                    detailsArea.append("───────────────────────────────────────────\n");
                    detailsArea.append("AGGREGATED SCORES:\n");
                    detailsArea.append("───────────────────────────────────────────\n");
                    double[] scores = ReviewService.getInstance().getAggregatedScores(u);
                    detailsArea.append(String.format("  Manager Review Avg: %.1f/10 (%d reviews)\n",
                            (scores[0] + scores[1] + scores[2]) / 3.0, (int) scores[3]));
                    double[] peerScores = ReviewService.getInstance().getAggregatedPeerScores(u);
                    detailsArea.append(String.format("  Peer Review Avg: %.1f/5 (%d reviews)\n",
                            (peerScores[0] + peerScores[1] + peerScores[2]) / 3.0, (int) peerScores[3]));
                }
            }
        });

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(new JLabel("All Employees:"), BorderLayout.NORTH);
        leftPanel.add(new JScrollPane(userList), BorderLayout.CENTER);
        leftPanel.setPreferredSize(new Dimension(250, 0));

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> refresh.run());

        panel.add(leftPanel, BorderLayout.WEST);
        panel.add(new JScrollPane(detailsArea), BorderLayout.CENTER);
        panel.add(refreshBtn, BorderLayout.SOUTH);
        return panel;
    }

    /**
     * Panel for viewing and deleting ALL goals in the system
     */
    private JPanel createAllGoalsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        DefaultTableModel goalsModel = new DefaultTableModel(
                new String[] { "User", "Goal", "Target Date", "Status" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable goalsTable = new JTable(goalsModel);

        Runnable refresh = () -> {
            goalsModel.setRowCount(0);
            for (Goal g : GoalService.getInstance().getAllGoals()) {
                goalsModel.addRow(new Object[] {
                        g.getEmployee() != null ? g.getEmployee().getName() : "Unknown",
                        g.getDescription(),
                        g.getCreatedDate(),
                        g.getStatus()
                });
            }
        };
        refresh.run();

        JPanel btnPanel = new JPanel(new FlowLayout());
        JButton deleteBtn = new JButton("🗑️ Delete Selected Goal");
        JButton refreshBtn = new JButton("🔄 Refresh");

        deleteBtn.addActionListener(e -> {
            int row = goalsTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(panel, "Select a goal first.");
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(panel,
                    "Are you sure you want to delete this goal?",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                List<Goal> allGoals = GoalService.getInstance().getAllGoals();
                if (row < allGoals.size()) {
                    GoalService.getInstance().deleteGoalAdmin(allGoals.get(row));
                    refresh.run();
                    JOptionPane.showMessageDialog(panel, "Goal deleted.");
                }
            }
        });

        refreshBtn.addActionListener(e -> refresh.run());

        btnPanel.add(deleteBtn);
        btnPanel.add(refreshBtn);

        panel.add(new JScrollPane(goalsTable), BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);
        return panel;
    }
}
