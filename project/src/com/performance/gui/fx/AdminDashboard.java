package com.performance.gui.fx;

import javafx.animation.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;

import com.performance.model.*;
import com.performance.service.*;
import com.performance.util.DataStore;
import com.performance.util.Logger;

import java.util.List;

/**
 * Admin Dashboard with full system management capabilities.
 */
public class AdminDashboard {

    private BorderPane root;
    private VBox sidebar;
    private StackPane contentArea;
    private Button activeNavButton;

    public AdminDashboard() {
        createView();
        Logger.info("Admin dashboard loaded.");
    }

    private void createView() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: #0f172a;");

        HBox header = createHeader();
        root.setTop(header);

        sidebar = createSidebar();
        root.setLeft(sidebar);

        contentArea = new StackPane();
        contentArea.setStyle("-fx-background-color: #0f172a; -fx-padding: 24;");
        root.setCenter(contentArea);

        showManageUsers();
    }

    private HBox createHeader() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(16, 24, 16, 24));
        header.setSpacing(16);
        header.setStyle("-fx-background-color: #1e293b; -fx-border-color: #334155; -fx-border-width: 0 0 1 0;");

        Label logo = new Label("📊 Performance Review System");
        logo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #f8fafc;");

        Label roleLabel = new Label("[ ADMIN ]");
        roleLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 12px; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label userLabel = new Label("👤 Administrator");
        userLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 14px;");

        Button logoutBtn = new Button("Logout");
        logoutBtn.setStyle(
                "-fx-background-color: #334155; -fx-text-fill: #f8fafc; -fx-padding: 8 16; -fx-background-radius: 6;");
        logoutBtn.setOnAction(e -> logout());

        header.getChildren().addAll(logo, roleLabel, spacer, userLabel, logoutBtn);
        return header;
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(8);
        sidebar.setPadding(new Insets(24, 16, 24, 16));
        sidebar.setPrefWidth(240);
        sidebar.setStyle("-fx-background-color: #1e293b;");

        Label userLabel = new Label("USER MANAGEMENT");
        userLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px; -fx-font-weight: bold;");
        userLabel.setPadding(new Insets(0, 0, 8, 8));

        // Removed User Approval - approval happens implicitly via hierarchy assignment
        Button usersBtn = createNavButton("👥", "Manage Users", () -> showManageUsers());
        Button hierarchyBtn = createNavButton("🏢", "Hierarchy", () -> showHierarchy());

        Label systemLabel = new Label("SYSTEM");
        systemLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px; -fx-font-weight: bold;");
        systemLabel.setPadding(new Insets(16, 0, 8, 8));

        Button coursesBtn = createNavButton("📚", "Training Courses", () -> showTrainingCourses());
        Button trainingMonitorBtn = createNavButton("📋", "All Training", () -> showAllTraining());
        Button allReviewsBtn = createNavButton("⭐", "All Reviews", () -> showAllReviews());
        Button reportsBtn = createNavButton("📈", "Reports", () -> showReports());
        Button skillGapBtn = createNavButton("🔍", "Skill Gap Analysis", () -> showSkillGapAnalysis());
        Button topPerformersBtn = createNavButton("🏆", "Top Performers", () -> showTopPerformers());

        setActiveNav(usersBtn);

        sidebar.getChildren().addAll(
                userLabel,
                usersBtn,
                hierarchyBtn,
                systemLabel,
                coursesBtn,
                trainingMonitorBtn,
                allReviewsBtn,
                reportsBtn,
                skillGapBtn,
                topPerformersBtn);

        return sidebar;
    }

    private Button createNavButton(String icon, String text, Runnable action) {
        Button btn = new Button(icon + "  " + text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: #94a3b8;" +
                        "-fx-font-size: 14px;" +
                        "-fx-padding: 12 16;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;");

        btn.setOnMouseEntered(e -> {
            if (btn != activeNavButton) {
                btn.setStyle(
                        btn.getStyle().replace("-fx-background-color: transparent;", "-fx-background-color: #334155;"));
            }
        });

        btn.setOnMouseExited(e -> {
            if (btn != activeNavButton) {
                btn.setStyle(
                        btn.getStyle().replace("-fx-background-color: #334155;", "-fx-background-color: transparent;"));
            }
        });

        btn.setOnAction(e -> {
            setActiveNav(btn);
            action.run();
        });

        return btn;
    }

    private void setActiveNav(Button btn) {
        if (activeNavButton != null) {
            activeNavButton.setStyle(
                    "-fx-background-color: transparent;" +
                            "-fx-text-fill: #94a3b8;" +
                            "-fx-font-size: 14px;" +
                            "-fx-padding: 12 16;" +
                            "-fx-background-radius: 8;" +
                            "-fx-cursor: hand;");
        }
        activeNavButton = btn;
        btn.setStyle(
                "-fx-background-color: linear-gradient(to right, #6366f1, #8b5cf6);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;" +
                        "-fx-padding: 12 16;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;");
    }

    private void setContent(Node content) {
        content.setOpacity(0);
        contentArea.getChildren().setAll(content);

        FadeTransition ft = new FadeTransition(Duration.millis(200), content);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    // === User Management Panels ===

    private void showUserApproval() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(24));

        Label title = new Label("✅ Pending User Approvals");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #f8fafc;");

        List<User> pendingUsers = UserService.getInstance().getPendingUsers();

        VBox list = new VBox(12);
        if (pendingUsers.isEmpty()) {
            Label empty = new Label("No pending user approvals.");
            empty.setStyle("-fx-text-fill: #64748b;");
            list.getChildren().add(empty);
        } else {
            for (User user : pendingUsers) {
                list.getChildren().add(createApprovalCard(user));
            }
        }

        ScrollPane scrollPane = new ScrollPane(list);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        panel.getChildren().addAll(title, scrollPane);
        setContent(panel);
    }

    private HBox createApprovalCard(User user) {
        HBox card = new HBox(16);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(16));
        card.setStyle(
                "-fx-background-color: rgba(30, 41, 59, 0.9);" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-radius: 10;" +
                        "-fx-border-color: #f59e0b;" +
                        "-fx-border-width: 0 0 0 4;");

        Label avatar = new Label("👤");
        avatar.setStyle("-fx-font-size: 28px;");

        VBox info = new VBox(4);
        Label name = new Label(user.getName());
        name.setStyle("-fx-text-fill: #f8fafc; -fx-font-size: 16px; -fx-font-weight: bold;");

        Label username = new Label("@" + user.getUsername());
        username.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px;");

        info.getChildren().addAll(name, username);
        HBox.setHgrow(info, Priority.ALWAYS);

        Button approveBtn = new Button("✓ Approve");
        approveBtn.setStyle(
                "-fx-background-color: #22c55e; -fx-text-fill: white; -fx-padding: 8 16; -fx-background-radius: 6;");
        approveBtn.setOnAction(e -> {
            UserService.getInstance().approveUser(user);
            showUserApproval();
        });

        Button rejectBtn = new Button("✗ Reject");
        rejectBtn.setStyle(
                "-fx-background-color: #ef4444; -fx-text-fill: white; -fx-padding: 8 16; -fx-background-radius: 6;");
        rejectBtn.setOnAction(e -> {
            DataStore.getInstance().removeUser(user);
            DataStore.getInstance().saveData();
            showUserApproval();
        });

        card.getChildren().addAll(avatar, info, approveBtn, rejectBtn);
        return card;
    }

    private void showManageUsers() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(24));

        Label title = new Label("👥 Manage Users");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #f8fafc;");

        List<User> users = UserService.getInstance().getNonAdminUsers();

        VBox list = new VBox(8);
        for (User user : users) {
            list.getChildren().add(createUserCard(user));
        }

        ScrollPane scrollPane = new ScrollPane(list);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        panel.getChildren().addAll(title, scrollPane);
        setContent(panel);
    }

    private HBox createUserCard(User user) {
        HBox card = new HBox(16);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(12, 16, 12, 16));
        card.setStyle(
                "-fx-background-color: rgba(30, 41, 59, 0.9);" +
                        "-fx-background-radius: 8;");

        Label avatar = new Label("👤");
        avatar.setStyle("-fx-font-size: 20px;");

        VBox info = new VBox(2);
        Label name = new Label(user.getName());
        name.setStyle("-fx-text-fill: #f8fafc; -fx-font-size: 14px; -fx-font-weight: bold;");

        String statusColor = user.getStatus() == UserStatus.ACTIVE ? "#22c55e"
                : user.getStatus() == UserStatus.PENDING ? "#f59e0b" : "#ef4444";
        Label status = new Label("● " + user.getStatus() + " | Tier " + user.getTierLevel());
        status.setStyle("-fx-text-fill: " + statusColor + "; -fx-font-size: 11px;");

        info.getChildren().addAll(name, status);
        HBox.setHgrow(info, Priority.ALWAYS);

        // Toggle button - suspended users go to "Set Pending", others to "Suspend"
        String toggleLabel = user.getStatus() == UserStatus.SUSPENDED ? "Set Pending" : "Suspend";
        Button toggleBtn = new Button(toggleLabel);
        toggleBtn.setStyle(
                "-fx-background-color: #334155; -fx-text-fill: #f8fafc; -fx-padding: 6 12; -fx-background-radius: 4;");
        toggleBtn.setOnAction(e -> {
            UserService.getInstance().toggleUserStatus(user);
            showManageUsers();
        });

        // Delete button
        Button deleteBtn = new Button("🗑");
        deleteBtn.setStyle(
                "-fx-background-color: #dc2626; -fx-text-fill: white; -fx-padding: 6 12; -fx-background-radius: 4;");
        deleteBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Delete User");
            confirm.setHeaderText("Delete " + user.getName() + "?");
            confirm.setContentText("This action cannot be undone.");
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    UserService.getInstance().deleteUser(user);
                    showManageUsers();
                }
            });
        });

        card.getChildren().addAll(avatar, info, toggleBtn, deleteBtn);
        return card;
    }

    private void showHierarchy() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(24));

        Label title = new Label("🏢 Organization Hierarchy");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #f8fafc;");

        // Tier selector
        HBox tierBox = new HBox(16);
        tierBox.setAlignment(Pos.CENTER_LEFT);

        Label tierLabel = new Label("Set Max Tiers:");
        tierLabel.setStyle("-fx-text-fill: #94a3b8;");

        Spinner<Integer> tierSpinner = new Spinner<>(2, 10, UserService.getInstance().getMaxTiers());
        tierSpinner.setStyle("-fx-background-color: #334155;");
        tierSpinner.setPrefWidth(80);

        Button applyBtn = new Button("Apply");
        applyBtn.setOnAction(e -> {
            int newTiers = tierSpinner.getValue();
            int oldTiers = UserService.getInstance().getMaxTiers();

            if (newTiers != oldTiers) {
                // Show confirmation dialog
                Alert confirm = new Alert(Alert.AlertType.WARNING);
                confirm.setTitle("Change Max Tiers");
                confirm.setHeaderText("⚠ Warning: All Users Will Be Reset");
                confirm.setContentText(
                        "Changing the maximum tier levels from " + oldTiers + " to " + newTiers +
                                " will:\n\n" +
                                "• Reset ALL employees to PENDING status\n" +
                                "• Set ALL employees to Tier 1\n" +
                                "• Remove ALL manager assignments\n" +
                                "• Employees won't be able to login until reassigned\n\n" +
                                "Are you sure you want to proceed?");
                confirm.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

                confirm.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.YES) {
                        UserService.getInstance().setMaxTiers(newTiers);

                        Alert info = new Alert(Alert.AlertType.INFORMATION);
                        info.setTitle("Success");
                        info.setContentText(
                                "Max tiers changed to " + newTiers + ". All users have been reset to PENDING.");
                        info.showAndWait();

                        showHierarchy();
                    }
                });
            }
        });

        tierBox.getChildren().addAll(tierLabel, tierSpinner, applyBtn);

        // Reset All Employees button
        Button resetAllBtn = new Button("🔄 Reset All Employees");
        resetAllBtn.setStyle(
                "-fx-background-color: #dc2626; -fx-text-fill: white; -fx-padding: 8 16; -fx-background-radius: 6;");
        resetAllBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.WARNING);
            confirm.setTitle("Reset All Employees");
            confirm.setHeaderText("⚠ Warning: All Users Will Be Reset");
            confirm.setContentText(
                    "This will:\n\n" +
                            "• Reset ALL employees to PENDING status\n" +
                            "• Set ALL employees to Tier 1\n" +
                            "• Remove ALL manager assignments\n" +
                            "• Employees won't be able to login until reassigned\n\n" +
                            "Are you sure you want to proceed?");
            confirm.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) {
                    UserService.getInstance().resetAllEmployees();

                    Alert info = new Alert(Alert.AlertType.INFORMATION);
                    info.setTitle("Success");
                    info.setContentText("All employees have been reset to PENDING status.");
                    info.showAndWait();

                    showHierarchy();
                }
            });
        });

        HBox actionsBox = new HBox(16, tierBox, resetAllBtn);
        actionsBox.setAlignment(Pos.CENTER_LEFT);

        // Assignment section
        VBox assignCard = createCard();

        Label assignTitle = new Label("Assign Employee to Hierarchy");
        assignTitle.setStyle("-fx-text-fill: #f8fafc; -fx-font-size: 16px; -fx-font-weight: bold;");

        Label infoLabel = new Label("Note: Manager must be tier = employee tier + 1. Highest tier = no manager.");
        infoLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px;");

        // Warning label for tier restrictions
        Label tierWarningLabel = new Label();
        tierWarningLabel.setStyle("-fx-text-fill: #f59e0b; -fx-font-size: 12px; -fx-font-weight: bold;");
        tierWarningLabel.setWrapText(true);

        List<User> users = UserService.getInstance().getNonAdminUsers();
        int maxTiers = UserService.getInstance().getMaxTiers();

        ComboBox<String> employeeCombo = new ComboBox<>();
        employeeCombo.setPromptText("Select Employee");
        for (User u : users) {
            employeeCombo.getItems().add(u.getName() + " (@" + u.getUsername() + ")");
        }
        employeeCombo.setMaxWidth(Double.MAX_VALUE);
        employeeCombo.setStyle("-fx-background-color: #334155;");

        Spinner<Integer> empTierSpinner = new Spinner<>(1, maxTiers, 1);
        empTierSpinner.setStyle("-fx-background-color: #334155;");

        ComboBox<String> managerCombo = new ComboBox<>();
        managerCombo.setPromptText("Select Manager");
        managerCombo.setMaxWidth(Double.MAX_VALUE);
        managerCombo.setStyle("-fx-background-color: #334155;");

        // Update manager combo and tier warnings when employee or tier changes
        Runnable updateManagerCombo = () -> {
            managerCombo.getItems().clear();
            int empIdx = employeeCombo.getSelectionModel().getSelectedIndex();
            int selectedTier = empTierSpinner.getValue();
            User selectedEmployee = empIdx >= 0 ? users.get(empIdx) : null;

            // Update tier warning label
            StringBuilder warnings = new StringBuilder();
            String tierBlockedReason = UserService.getInstance().getTierBlockedReason(selectedTier);
            if (tierBlockedReason != null) {
                warnings.append("⚠ ").append(tierBlockedReason);
            }
            if (selectedEmployee != null) {
                String tierChangeReason = UserService.getInstance().getTierChangeBlockedReason(selectedEmployee,
                        selectedTier);
                if (tierChangeReason != null) {
                    if (warnings.length() > 0)
                        warnings.append("\n");
                    warnings.append("⚠ ").append(tierChangeReason);
                }
            }
            tierWarningLabel.setText(warnings.toString());

            if (selectedTier == maxTiers) {
                // Highest tier = no manager (highboard)
                managerCombo.getItems().add("-- No Manager (Highboard) --");
                managerCombo.getSelectionModel().select(0);
                managerCombo.setDisable(true);
            } else {
                managerCombo.setDisable(false);
                int requiredManagerTier = selectedTier + 1;

                for (User u : users) {
                    // Manager must be tier+1 and not the same as employee
                    if (u.getTierLevel() == requiredManagerTier) {
                        if (selectedEmployee == null || !u.getUsername().equals(selectedEmployee.getUsername())) {
                            managerCombo.getItems()
                                    .add(u.getName() + " (@" + u.getUsername() + ", Tier " + u.getTierLevel() + ")");
                        }
                    }
                }

                if (managerCombo.getItems().isEmpty()) {
                    managerCombo.getItems().add("-- No managers at Tier " + requiredManagerTier + " --");
                }
            }
        };

        employeeCombo.setOnAction(e -> updateManagerCombo.run());
        empTierSpinner.valueProperty().addListener((obs, old, val) -> updateManagerCombo.run());

        Button assignBtn = new Button("Assign & Activate User");
        assignBtn.setMaxWidth(Double.MAX_VALUE);
        assignBtn.setStyle("-fx-background-color: #22c55e; -fx-text-fill: white; -fx-font-weight: bold;");
        assignBtn.setOnAction(e -> {
            int empIdx = employeeCombo.getSelectionModel().getSelectedIndex();
            int mgrIdx = managerCombo.getSelectionModel().getSelectedIndex();
            int selectedTier = empTierSpinner.getValue();

            if (empIdx < 0) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setContentText("Please select an employee.");
                alert.showAndWait();
                return;
            }

            User employee = users.get(empIdx);
            User manager = null;

            if (selectedTier < maxTiers && mgrIdx >= 0) {
                // Find the selected manager
                int requiredManagerTier = selectedTier + 1;
                int managerCount = 0;
                for (User u : users) {
                    if (u.getTierLevel() == requiredManagerTier &&
                            !u.getUsername().equals(employee.getUsername())) {
                        if (managerCount == mgrIdx) {
                            manager = u;
                            break;
                        }
                        managerCount++;
                    }
                }
            }

            // Validate the assignment using new business rules
            String validationError = UserService.getInstance().validateHierarchyAssignment(employee, manager,
                    selectedTier);
            if (validationError != null) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Assignment Blocked");
                alert.setContentText(validationError);
                alert.showAndWait();
                return;
            }

            UserService.getInstance().assignHierarchy(employee, manager, selectedTier);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setContentText("User '" + employee.getName() + "' is now ACTIVE at Tier " + selectedTier +
                    (manager != null ? " under " + manager.getName() : " (Highboard Manager)"));
            alert.showAndWait();
            showHierarchy();
        });

        assignCard.getChildren().addAll(
                assignTitle,
                infoLabel,
                tierWarningLabel,
                new Label("Employee:") {
                    {
                        setStyle("-fx-text-fill: #94a3b8;");
                    }
                },
                employeeCombo,
                new Label("Tier Level:") {
                    {
                        setStyle("-fx-text-fill: #94a3b8;");
                    }
                },
                empTierSpinner,
                new Label("Manager (must be Tier+1):") {
                    {
                        setStyle("-fx-text-fill: #94a3b8;");
                    }
                },
                managerCombo,
                new Region() {
                    {
                        setMinHeight(8);
                    }
                },
                assignBtn);

        panel.getChildren().addAll(title, actionsBox, assignCard);
        setContent(panel);
    }

    private void showTrainingCourses() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(24));

        Label title = new Label("📚 Training Courses");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #f8fafc;");

        // Add course section
        HBox addBox = new HBox(12);
        addBox.setAlignment(Pos.CENTER_LEFT);

        TextField courseField = new TextField();
        courseField.setPromptText("New course name");
        courseField.setStyle(
                "-fx-background-color: #334155; -fx-text-fill: #f8fafc; -fx-padding: 10 16; -fx-background-radius: 6;");
        HBox.setHgrow(courseField, Priority.ALWAYS);

        Button addBtn = new Button("+ Add");
        addBtn.setStyle(
                "-fx-background-color: #22c55e; -fx-text-fill: white; -fx-padding: 10 20; -fx-background-radius: 6;");
        addBtn.setOnAction(e -> {
            if (!courseField.getText().trim().isEmpty()) {
                TrainingService.getInstance().addCourse(courseField.getText().trim());
                showTrainingCourses();
            }
        });

        addBox.getChildren().addAll(courseField, addBtn);

        // ===== Assign Training to Employee Section =====
        VBox assignCard = createCard();

        Label assignTitle = new Label("📤 Assign Training to Employee");
        assignTitle.setStyle("-fx-text-fill: #f8fafc; -fx-font-size: 16px; -fx-font-weight: bold;");

        List<User> activeUsers = UserService.getInstance().getNonAdminUsers().stream()
                .filter(u -> u.getStatus() == UserStatus.ACTIVE)
                .collect(java.util.stream.Collectors.toList());
        List<String> courses = TrainingService.getInstance().getAvailableCourses();

        ComboBox<String> employeeCombo = new ComboBox<>();
        employeeCombo.setPromptText("Select Employee");
        for (User u : activeUsers) {
            employeeCombo.getItems().add(u.getName() + " (@" + u.getUsername() + ")");
        }
        employeeCombo.setMaxWidth(Double.MAX_VALUE);
        employeeCombo.setStyle("-fx-background-color: #334155;");

        ComboBox<String> courseCombo = new ComboBox<>();
        courseCombo.setPromptText("Select from existing courses");
        courseCombo.getItems().addAll(courses);
        courseCombo.setMaxWidth(Double.MAX_VALUE);
        courseCombo.setStyle("-fx-background-color: #334155;");

        // Custom training text field
        TextField customCourseField = new TextField();
        customCourseField.setPromptText("Or type custom training name...");
        customCourseField.setStyle(
                "-fx-background-color: #334155; -fx-text-fill: #f8fafc; -fx-padding: 10 16; -fx-background-radius: 6;");
        customCourseField.setMaxWidth(Double.MAX_VALUE);

        // Clear the other when one is used
        customCourseField.textProperty().addListener((obs, old, val) -> {
            if (!val.isEmpty())
                courseCombo.getSelectionModel().clearSelection();
        });
        courseCombo.setOnAction(e -> {
            if (courseCombo.getValue() != null)
                customCourseField.clear();
        });

        Button assignTrainingBtn = new Button("📤 Assign Training");
        assignTrainingBtn.setMaxWidth(Double.MAX_VALUE);
        assignTrainingBtn.setStyle("-fx-background-color: #6366f1; -fx-text-fill: white; -fx-font-weight: bold;");
        assignTrainingBtn.setOnAction(e -> {
            int empIdx = employeeCombo.getSelectionModel().getSelectedIndex();
            String selectedCourse = courseCombo.getValue();
            String customCourse = customCourseField.getText().trim();

            // Use custom course if provided, otherwise use dropdown selection
            String courseName = !customCourse.isEmpty() ? customCourse : selectedCourse;

            if (empIdx < 0) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setContentText("Please select an employee.");
                alert.showAndWait();
                return;
            }
            if (courseName == null || courseName.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setContentText("Please select a course or enter a custom training name.");
                alert.showAndWait();
                return;
            }

            User recipient = activeUsers.get(empIdx);
            Admin admin = null;
            for (User u : DataStore.getInstance().getUsers()) {
                if (u instanceof Admin) {
                    admin = (Admin) u;
                    break;
                }
            }

            if (admin != null) {
                TrainingRecommendation tr = TrainingService.getInstance().createRecommendation(admin, recipient,
                        courseName);
                if (tr != null) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Success");
                    alert.setContentText("Training '" + courseName + "' assigned to " + recipient.getName());
                    alert.showAndWait();
                    showTrainingCourses();
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setContentText("Failed to assign training.");
                    alert.showAndWait();
                }
            }
        });

        assignCard.getChildren().addAll(
                assignTitle,
                new Label("Employee:") {
                    {
                        setStyle("-fx-text-fill: #94a3b8;");
                    }
                },
                employeeCombo,
                new Label("Course (select or create custom):") {
                    {
                        setStyle("-fx-text-fill: #94a3b8;");
                    }
                },
                courseCombo,
                customCourseField,
                new Region() {
                    {
                        setMinHeight(8);
                    }
                },
                assignTrainingBtn);

        // Courses list
        VBox list = new VBox(8);
        for (String course : courses) {
            HBox row = new HBox(16);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(12, 16, 12, 16));
            row.setStyle("-fx-background-color: rgba(30, 41, 59, 0.9); -fx-background-radius: 8;");

            Label icon = new Label("📖");
            Label name = new Label(course);
            name.setStyle("-fx-text-fill: #f8fafc; -fx-font-size: 14px;");
            HBox.setHgrow(name, Priority.ALWAYS);

            Button delBtn = new Button("🗑");
            delBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ef4444; -fx-cursor: hand;");
            delBtn.setOnAction(e -> {
                TrainingService.getInstance().removeCourse(course);
                showTrainingCourses();
            });

            row.getChildren().addAll(icon, name, delBtn);
            list.getChildren().add(row);
        }

        ScrollPane scrollPane = new ScrollPane(list);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        panel.getChildren().addAll(title, addBox, assignCard, scrollPane);
        setContent(panel);
    }

    private void showAllTraining() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(24));

        Label title = new Label("📋 All Training Recommendations");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #f8fafc;");

        List<TrainingRecommendation> recommendations = TrainingService.getInstance().getAllRecommendations();

        VBox list = new VBox(8);
        if (recommendations.isEmpty()) {
            Label empty = new Label("No training recommendations in the system.");
            empty.setStyle("-fx-text-fill: #64748b;");
            list.getChildren().add(empty);
        } else {
            for (TrainingRecommendation tr : recommendations) {
                HBox card = new HBox(16);
                card.setAlignment(Pos.CENTER_LEFT);
                card.setPadding(new Insets(12, 16, 12, 16));
                card.setStyle("-fx-background-color: rgba(30, 41, 59, 0.9); -fx-background-radius: 8;");

                Label icon = new Label("📚");
                icon.setStyle("-fx-font-size: 20px;");

                VBox info = new VBox(4);
                Label courseLabel = new Label(tr.getCourseName());
                courseLabel.setStyle("-fx-text-fill: #f8fafc; -fx-font-size: 14px; -fx-font-weight: bold;");

                String fromName = tr.getRecommender() != null ? tr.getRecommender().getName() : "Unknown";
                String toName = tr.getRecipient() != null ? tr.getRecipient().getName() : "Unknown";
                Label details = new Label("From: " + fromName + " → To: " + toName);
                details.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px;");

                info.getChildren().addAll(courseLabel, details);
                HBox.setHgrow(info, Priority.ALWAYS);

                Button deleteBtn = new Button("🗑");
                deleteBtn.setStyle(
                        "-fx-background-color: #dc2626; -fx-text-fill: white; -fx-padding: 6 12; -fx-background-radius: 4;");
                deleteBtn.setOnAction(e -> {
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Delete Training");
                    confirm.setContentText("Delete this training recommendation?");
                    confirm.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) {
                            DataStore.getInstance().removeTrainingRecommendation(tr);
                            DataStore.getInstance().saveData();
                            showAllTraining();
                        }
                    });
                });

                card.getChildren().addAll(icon, info, deleteBtn);
                list.getChildren().add(card);
            }
        }

        ScrollPane scrollPane = new ScrollPane(list);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        panel.getChildren().addAll(title, scrollPane);
        setContent(panel);
    }

    private void showAllReviews() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(24));

        Label title = new Label("⭐ All Reviews");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #f8fafc;");

        List<Review> reviews = ReviewService.getInstance().getAllReviews();

        VBox list = new VBox(8);
        if (reviews.isEmpty()) {
            Label empty = new Label("No reviews in the system.");
            empty.setStyle("-fx-text-fill: #64748b;");
            list.getChildren().add(empty);
        } else {
            for (Review review : reviews) {
                HBox cardRow = new HBox(16);
                cardRow.setAlignment(Pos.CENTER_LEFT);
                cardRow.setPadding(new Insets(12, 16, 12, 16));
                cardRow.setStyle("-fx-background-color: rgba(30, 41, 59, 0.9); -fx-background-radius: 8;");

                VBox info = new VBox(4);

                Label reviewer = new Label("From: " + review.getReviewer().getName());
                reviewer.setStyle("-fx-text-fill: #818cf8; -fx-font-size: 12px;");

                Label employee = new Label("To: " + review.getEmployee().getName());
                employee.setStyle("-fx-text-fill: #f8fafc; -fx-font-size: 14px; -fx-font-weight: bold;");

                double avg = (review.getPunctualityScore() + review.getTeamworkScore() + review.getProductivityScore())
                        / 3.0;
                Label score = new Label(String.format("Average: %.1f / 5", avg));
                score.setStyle("-fx-text-fill: #22c55e;");

                info.getChildren().addAll(reviewer, employee, score);
                HBox.setHgrow(info, Priority.ALWAYS);

                Button deleteBtn = new Button("🗑");
                deleteBtn.setStyle(
                        "-fx-background-color: #dc2626; -fx-text-fill: white; -fx-padding: 6 12; -fx-background-radius: 4;");
                deleteBtn.setOnAction(e -> {
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Delete Review");
                    confirm.setContentText("Delete this review permanently?");
                    confirm.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) {
                            ReviewService.getInstance().deleteReviewAdmin(review);
                            showAllReviews();
                        }
                    });
                });

                cardRow.getChildren().addAll(info, deleteBtn);
                list.getChildren().add(cardRow);
            }
        }

        ScrollPane scrollPane = new ScrollPane(list);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        panel.getChildren().addAll(title, scrollPane);
        setContent(panel);
    }

    private void showReports() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(24));

        Label title = new Label("📈 System Reports");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #f8fafc;");

        VBox card = createCard();

        HBox buttons = new HBox(16);

        Button summaryBtn = new Button("📊 Summary Report");
        summaryBtn.setStyle("-fx-background-color: #6366f1; -fx-text-fill: white;");
        summaryBtn.setOnAction(e -> {
            String report = ReportGenerator.generateSummaryReport();
            showReportDialog("Summary Report", report);
        });

        Button trainingBtn = new Button("📚 Training Report");
        trainingBtn.setStyle("-fx-background-color: #6366f1; -fx-text-fill: white;");
        trainingBtn.setOnAction(e -> {
            String report = ReportGenerator.generateTrainingReport();
            showReportDialog("Training Report", report);
        });

        Button exportBtn = new Button("📄 Export All to HTML");
        exportBtn.setStyle("-fx-background-color: #22c55e; -fx-text-fill: white;");
        exportBtn.setOnAction(e -> {
            ReportExporter.exportAllUsersReport(FXApp.getPrimaryStage());
        });

        buttons.getChildren().addAll(summaryBtn, trainingBtn, exportBtn);
        card.getChildren().add(buttons);

        // Stats cards
        HBox statsRow = new HBox(16);
        List<User> allUsers = UserService.getInstance().getAllUsers();
        long activeCount = allUsers.stream().filter(u -> u.getStatus() == UserStatus.ACTIVE).count();
        long pendingCount = allUsers.stream().filter(u -> u.getStatus() == UserStatus.PENDING).count();

        statsRow.getChildren().addAll(
                createStatCard("👥", "Total Users", String.valueOf(allUsers.size()), "#6366f1"),
                createStatCard("✅", "Active", String.valueOf(activeCount), "#22c55e"),
                createStatCard("⏳", "Pending", String.valueOf(pendingCount), "#f59e0b"),
                createStatCard("⭐", "Reviews", String.valueOf(ReviewService.getInstance().getAllReviews().size()),
                        "#8b5cf6"));

        panel.getChildren().addAll(title, card, statsRow);
        setContent(panel);
    }

    private void showSkillGapAnalysis() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(24));

        Label title = new Label("🔍 Skill Gap Analysis");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #f8fafc;");

        Label subtitle = new Label("Identifies employees with scores below 3.0 (threshold for skill gap)");
        subtitle.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 14px;");

        // Filter options
        VBox filterCard = createCard();
        Label filterTitle = new Label("🎯 Analysis Scope");
        filterTitle.setStyle("-fx-text-fill: #f8fafc; -fx-font-size: 16px; -fx-font-weight: bold;");

        ComboBox<String> scopeCombo = new ComboBox<>();
        scopeCombo.getItems().add("All Employees");
        List<User> managers = UserService.getInstance().getNonAdminUsers().stream()
                .filter(u -> (u.getDirectReports() != null && !u.getDirectReports().isEmpty())
                        || u.getTierLevel() == DataStore.MAX_TIERS)
                .collect(java.util.stream.Collectors.toList());
        for (User mgr : managers) {
            scopeCombo.getItems().add("Team of: " + mgr.getName());
        }
        scopeCombo.getSelectionModel().select(0);
        scopeCombo.setMaxWidth(Double.MAX_VALUE);
        scopeCombo.setStyle("-fx-background-color: #334155;");

        filterCard.getChildren().addAll(filterTitle, scopeCombo);

        // Results section
        VBox resultsBox = new VBox(12);
        ScrollPane scroll = new ScrollPane(resultsBox);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        // Update results when scope changes
        Runnable updateResults = () -> {
            resultsBox.getChildren().clear();
            int scopeIdx = scopeCombo.getSelectionModel().getSelectedIndex();
            java.util.List<SkillGapService.SkillGapResult> results;

            if (scopeIdx == 0) {
                results = SkillGapService.getInstance().analyzeAll();
            } else {
                User manager = managers.get(scopeIdx - 1);
                results = SkillGapService.getInstance().analyzeTeam(manager);
            }

            if (results.isEmpty()) {
                Label empty = new Label("No employees to analyze in this scope.");
                empty.setStyle("-fx-text-fill: #64748b;");
                resultsBox.getChildren().add(empty);
            } else {
                // Summary
                int withGaps = 0;
                for (SkillGapService.SkillGapResult r : results) {
                    if (r.hasGaps())
                        withGaps++;
                }
                Label summaryLabel = new Label(
                        "Found " + withGaps + " employees with skill gaps out of " + results.size() + " analyzed.");
                summaryLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 14px;");
                resultsBox.getChildren().add(summaryLabel);

                for (SkillGapService.SkillGapResult result : results) {
                    VBox card = createCard();

                    Label empName = new Label(
                            "👤 " + result.user.getName() + " (Tier " + result.user.getTierLevel() + ")");
                    empName.setStyle("-fx-text-fill: #f8fafc; -fx-font-size: 14px; -fx-font-weight: bold;");

                    String gapColor = result.hasGaps() ? "#ef4444" : "#22c55e";
                    String gapIcon = result.hasGaps() ? "⚠" : "✓";
                    Label gapStatus = new Label(
                            gapIcon + " " + (result.hasGaps() ? "Gaps: " + result.getSummary() : "No gaps identified"));
                    gapStatus.setStyle("-fx-text-fill: " + gapColor + "; -fx-font-size: 12px;");

                    GridPane scores = new GridPane();
                    scores.setHgap(20);
                    scores.setVgap(6);

                    int row = 0;
                    addSkillRow(scores, row++, "Punctuality", result.punctuality);
                    addSkillRow(scores, row++, "Teamwork", result.teamwork);
                    addSkillRow(scores, row++, "Productivity", result.productivity);

                    if (result.showAllMetrics) {
                        addSkillRow(scores, row++, "Leadership", result.leadership);
                        addSkillRow(scores, row++, "Vision", result.vision);
                        addSkillRow(scores, row++, "Managerial", result.managerial);
                    }

                    card.getChildren().addAll(empName, gapStatus, scores);
                    resultsBox.getChildren().add(card);
                }
            }
        };

        scopeCombo.setOnAction(e -> updateResults.run());
        updateResults.run(); // Initial load

        panel.getChildren().addAll(title, subtitle, filterCard, scroll);
        setContent(panel);
    }

    private void addSkillRow(GridPane grid, int row, String label, double score) {
        Label labelNode = new Label(label + ":");
        labelNode.setStyle("-fx-text-fill: #94a3b8;");

        String scoreColor = score < 3.0 ? "#ef4444" : score >= 4.0 ? "#22c55e" : "#f59e0b";
        Label scoreNode = new Label(String.format("%.1f", score));
        scoreNode.setStyle("-fx-text-fill: " + scoreColor + "; -fx-font-weight: bold;");

        grid.add(labelNode, 0, row);
        grid.add(scoreNode, 1, row);
    }

    private void showTopPerformers() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(24));

        Label title = new Label("🏆 Top Performers");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #f8fafc;");

        Label subtitle = new Label("Top 3 performers system-wide and for each manager's hierarchy");
        subtitle.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 14px;");

        VBox resultsList = new VBox(16);

        java.util.List<TopPerformersService.TeamPerformers> analysis = TopPerformersService.getInstance()
                .getAdminTopPerformersAnalysis();

        for (TopPerformersService.TeamPerformers teamResult : analysis) {
            VBox card = createCard();

            String teamIcon = teamResult.manager == null ? "🌐" : "👤";
            Label teamTitle = new Label(teamIcon + " " + teamResult.teamName);
            teamTitle.setStyle("-fx-text-fill: #f8fafc; -fx-font-size: 15px; -fx-font-weight: bold;");

            Label teamInfo = new Label("Total members: " + teamResult.totalMembers);
            teamInfo.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px;");

            GridPane performersGrid = new GridPane();
            performersGrid.setHgap(20);
            performersGrid.setVgap(8);
            performersGrid.setPadding(new Insets(10, 0, 0, 0));

            // Headers
            Label rankHeader = new Label("Rank");
            rankHeader.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px;");
            Label nameHeader = new Label("Employee");
            nameHeader.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px;");
            Label scoreHeader = new Label("Score");
            scoreHeader.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px;");
            performersGrid.add(rankHeader, 0, 0);
            performersGrid.add(nameHeader, 1, 0);
            performersGrid.add(scoreHeader, 2, 0);

            if (teamResult.topPerformers.isEmpty()) {
                Label noData = new Label("No reviews yet");
                noData.setStyle("-fx-text-fill: #64748b;");
                performersGrid.add(noData, 0, 1, 3, 1);
            } else {
                int row = 1;
                for (TopPerformersService.PerformerResult performer : teamResult.topPerformers) {
                    String medal = performer.rank == 1 ? "🥇" : performer.rank == 2 ? "🥈" : "🥉";
                    Label rankLabel = new Label(medal + " #" + performer.rank);
                    rankLabel.setStyle("-fx-text-fill: #f8fafc;");

                    Label nameLabel = new Label(performer.user.getName());
                    nameLabel.setStyle("-fx-text-fill: #f8fafc;");

                    String scoreColor = performer.score >= 7 ? "#22c55e" : performer.score >= 5 ? "#f59e0b" : "#ef4444";
                    Label scoreLabel = new Label(String.format("%.1f", performer.score));
                    scoreLabel.setStyle("-fx-text-fill: " + scoreColor + "; -fx-font-weight: bold;");

                    performersGrid.add(rankLabel, 0, row);
                    performersGrid.add(nameLabel, 1, row);
                    performersGrid.add(scoreLabel, 2, row);
                    row++;
                }
            }

            card.getChildren().addAll(teamTitle, teamInfo, performersGrid);
            resultsList.getChildren().add(card);
        }

        ScrollPane scroll = new ScrollPane(resultsList);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        panel.getChildren().addAll(title, subtitle, scroll);
        setContent(panel);
    }

    private VBox createStatCard(String icon, String label, String value, String color) {
        VBox card = createCard();
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(150);
        card.setMinWidth(120);

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 24px;");

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");

        Label nameLabel = new Label(label);
        nameLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");

        card.getChildren().addAll(iconLabel, valueLabel, nameLabel);
        return card;
    }

    private void showReportDialog(String title, String content) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.initOwner(FXApp.getPrimaryStage());

        TextArea area = new TextArea(content);
        area.setEditable(false);
        area.setPrefWidth(600);
        area.setPrefHeight(400);
        area.setStyle("-fx-background-color: #1e293b; -fx-text-fill: #f8fafc; -fx-control-inner-background: #1e293b;");

        dialog.getDialogPane().setContent(area);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().setStyle("-fx-background-color: #1e293b;");
        dialog.showAndWait();
    }

    private VBox createCard() {
        VBox card = new VBox(12);
        card.setPadding(new Insets(20));
        card.setStyle(
                "-fx-background-color: rgba(30, 41, 59, 0.9);" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-radius: 12;" +
                        "-fx-border-color: #334155;" +
                        "-fx-border-width: 1;");
        card.setEffect(FXTheme.createShadow());
        return card;
    }

    private void logout() {
        Logger.info("Admin logged out.");
        LoginView loginView = new LoginView();
        Scene scene = new Scene(loginView.getRoot(), 500, 600);
        FXApp.navigateTo(scene);
    }

    public Parent getRoot() {
        return root;
    }
}
