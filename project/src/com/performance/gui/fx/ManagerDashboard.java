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
 * Manager Dashboard with team management and personal sections.
 * Used for both mid-level managers and highboard managers.
 */
public class ManagerDashboard {

    private BorderPane root;
    private VBox sidebar;
    private StackPane contentArea;
    private User currentUser;
    private Button activeNavButton;
    private boolean isHighboard;

    public ManagerDashboard(User user, boolean isHighboard) {
        this.currentUser = user;
        this.isHighboard = isHighboard;
        createView();
        Logger.info((isHighboard ? "Highboard" : "Manager") + " dashboard loaded for: " + user.getName());
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

        showTeamOverview();
    }

    private HBox createHeader() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(16, 24, 16, 24));
        header.setSpacing(16);
        header.setStyle("-fx-background-color: #1e293b; -fx-border-color: #334155; -fx-border-width: 0 0 1 0;");

        String role = isHighboard ? "Highboard Manager" : "Manager";
        Label logo = new Label("📊 Performance Review System");
        logo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #f8fafc;");

        Label roleLabel = new Label("[ " + role + " ]");
        roleLabel.setStyle("-fx-text-fill: #8b5cf6; -fx-font-size: 12px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label userLabel = new Label("👤 " + currentUser.getName());
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

        // Team section
        Label teamLabel = new Label("TEAM MANAGEMENT");
        teamLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px; -fx-font-weight: bold;");
        teamLabel.setPadding(new Insets(0, 0, 8, 8));

        Button overviewBtn = createNavButton("📊", "Team Overview", () -> showTeamOverview());
        Button reviewTeamBtn = createNavButton("⭐", "Review Team", () -> showReviewTeam());
        Button teamGoalsBtn = createNavButton("🎯", "Team Goals", () -> showTeamGoals());
        Button trainingBtn = createNavButton("📚", "Assign Training", () -> showAssignTraining());
        Button reportsBtn = createNavButton("📈", "Reports", () -> showReports());
        Button skillGapBtn = createNavButton("🔍", "Skill Gap Analysis", () -> showSkillGapAnalysis());
        Button topPerformersBtn = createNavButton("🏆", "Top Performers", () -> showTopPerformers());

        // Personal section
        Label personalLabel = new Label("PERSONAL");
        personalLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px; -fx-font-weight: bold;");
        personalLabel.setPadding(new Insets(16, 0, 8, 8));

        Button myProfileBtn = createNavButton("👤", "My Profile", () -> showMyProfile());
        Button myScoresBtn = createNavButton("📋", "My Scores", () -> showMyScores());

        setActiveNav(overviewBtn);

        sidebar.getChildren().addAll(
                teamLabel,
                overviewBtn,
                reviewTeamBtn,
                teamGoalsBtn,
                trainingBtn,
                reportsBtn,
                skillGapBtn,
                topPerformersBtn,
                personalLabel,
                myProfileBtn,
                myScoresBtn);

        // Add "Review My Manager" if not highboard
        if (!isHighboard && currentUser.getManager() != null) {
            Button reviewMgrBtn = createNavButton("📝", "Review Manager", () -> showReviewManager());
            sidebar.getChildren().add(reviewMgrBtn);
        }

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

    // === Team Management Panels ===

    private void showTeamOverview() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(24));

        Label title = new Label("📊 Team Overview");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #f8fafc;");

        // Use getAllDescendants for full hierarchy visibility (item #11)
        List<User> allTeam = UserService.getInstance().getAllDescendants(currentUser);
        List<User> directReports = currentUser.getDirectReports();

        // Stats cards
        HBox statsRow = new HBox(16);
        statsRow.getChildren().addAll(
                createStatCard("👥", "Full Team", String.valueOf(allTeam != null ? allTeam.size() : 0), "#6366f1"),
                createStatCard("📋", "Direct Reports",
                        String.valueOf(directReports != null ? directReports.size() : 0), "#818cf8"),
                createStatCard("🎯", "Active Goals",
                        String.valueOf(GoalService.getInstance().getTeamGoals(currentUser).stream()
                                .filter(g -> !"COMPLETED".equals(g.getStatus())).count()),
                        "#22c55e"),
                createStatCard("📚", "Training Assigned",
                        String.valueOf(TrainingService.getInstance().getRecommendationsBy(currentUser).size()),
                        "#f59e0b"));

        // Team members list
        Label membersLabel = new Label("Direct Reports");
        membersLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 14px; -fx-font-weight: bold;");

        VBox membersList = new VBox(8);
        if (allTeam == null || allTeam.isEmpty()) {
            Label empty = new Label("No team members in your hierarchy.");
            empty.setStyle("-fx-text-fill: #64748b;");
            membersList.getChildren().add(empty);
        } else {
            for (User member : allTeam) {
                membersList.getChildren().add(createMemberCard(member));
            }
        }

        ScrollPane scrollPane = new ScrollPane(membersList);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        panel.getChildren().addAll(title, statsRow, membersLabel, scrollPane);
        setContent(panel);
    }

    private VBox createStatCard(String icon, String label, String value, String color) {
        VBox card = createCard();
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(180);
        card.setMinWidth(150);

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 28px;");

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");

        Label nameLabel = new Label(label);
        nameLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");

        card.getChildren().addAll(iconLabel, valueLabel, nameLabel);
        return card;
    }

    private HBox createMemberCard(User member) {
        HBox card = new HBox(16);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(16));
        card.setStyle(
                "-fx-background-color: rgba(30, 41, 59, 0.9);" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-radius: 10;" +
                        "-fx-border-color: #334155;" +
                        "-fx-border-width: 1;");

        Label avatar = new Label("👤");
        avatar.setStyle("-fx-font-size: 24px;");

        VBox info = new VBox(4);
        Label name = new Label(member.getName());
        name.setStyle("-fx-text-fill: #f8fafc; -fx-font-size: 15px; -fx-font-weight: bold;");

        String statusColor = member.getStatus() == UserStatus.ACTIVE ? "#22c55e" : "#ef4444";
        Label status = new Label("● " + member.getStatus());
        status.setStyle("-fx-text-fill: " + statusColor + "; -fx-font-size: 11px;");

        info.getChildren().addAll(name, status);
        HBox.setHgrow(info, Priority.ALWAYS);

        card.getChildren().addAll(avatar, info);
        return card;
    }

    private void showReviewTeam() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(24));

        Label title = new Label("⭐ Review Team Member");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #f8fafc;");

        // Get all descendants (entire hierarchy) instead of just direct reports
        List<User> allDescendants = UserService.getInstance().getAllDescendants(currentUser);
        if (allDescendants == null || allDescendants.isEmpty()) {
            Label empty = new Label("No team members to review.");
            empty.setStyle("-fx-text-fill: #64748b;");
            panel.getChildren().addAll(title, empty);
            setContent(panel);
            return;
        }

        ComboBox<String> memberCombo = new ComboBox<>();
        memberCombo.setPromptText("Select team member (any tier)");
        for (User member : allDescendants) {
            String tierInfo = "Tier " + member.getTierLevel();
            memberCombo.getItems().add(member.getName() + " (" + member.getUsername() + ") - " + tierInfo);
        }
        memberCombo.setStyle("-fx-background-color: #334155; -fx-text-fill: #f8fafc;");
        memberCombo.setMaxWidth(400);

        VBox formCard = createCard();
        formCard.setVisible(false);

        GridPane form = new GridPane();
        form.setHgap(16);
        form.setVgap(16);

        Slider punctuality = createScoreSlider();
        Slider teamwork = createScoreSlider();
        Slider productivity = createScoreSlider();
        Slider leadership = createScoreSlider();
        Slider vision = createScoreSlider();
        Slider managerial = createScoreSlider();

        addFormRow(form, 0, "Punctuality", punctuality);
        addFormRow(form, 1, "Teamwork", teamwork);
        addFormRow(form, 2, "Productivity", productivity);
        addFormRow(form, 3, "Leadership", leadership);
        addFormRow(form, 4, "Vision", vision);
        addFormRow(form, 5, "Managerial Skills", managerial);

        TextArea comments = new TextArea();
        comments.setPromptText("Comments (optional)");
        comments.setPrefRowCount(3);
        comments.setStyle(
                "-fx-background-color: #334155; -fx-text-fill: #f8fafc; -fx-control-inner-background: #334155;");

        Button submitBtn = new Button("Submit Review");
        submitBtn.setMaxWidth(Double.MAX_VALUE);

        formCard.getChildren().addAll(form, comments, submitBtn);

        memberCombo.setOnAction(e -> {
            formCard.setVisible(memberCombo.getValue() != null);
        });

        submitBtn.setOnAction(e -> {
            int selectedIndex = memberCombo.getSelectionModel().getSelectedIndex();
            if (selectedIndex >= 0) {
                User selectedMember = allDescendants.get(selectedIndex);

                Review review = ReviewService.getInstance().createReview(
                        selectedMember, currentUser,
                        (int) punctuality.getValue(),
                        (int) teamwork.getValue(),
                        (int) productivity.getValue(),
                        (int) leadership.getValue(),
                        (int) vision.getValue(),
                        (int) managerial.getValue(),
                        comments.getText(),
                        false);

                Alert alert = new Alert(review != null ? Alert.AlertType.INFORMATION : Alert.AlertType.WARNING);
                alert.setTitle(review != null ? "Success" : "Already Reviewed");
                alert.setContentText(review != null ? "Review submitted!" : "Already reviewed this employee recently.");
                alert.showAndWait();
            }
        });

        panel.getChildren().addAll(title, memberCombo, formCard);
        setContent(panel);
    }

    private Slider createScoreSlider() {
        Slider slider = new Slider(1, 5, 3);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit(1);
        slider.setMinorTickCount(0);
        slider.setSnapToTicks(true);
        slider.setPrefWidth(200);
        return slider;
    }

    private void addFormRow(GridPane grid, int row, String label, Slider slider) {
        Label labelNode = new Label(label + ":");
        labelNode.setStyle("-fx-text-fill: #94a3b8;");

        Label valueLabel = new Label(String.valueOf((int) slider.getValue()));
        valueLabel.setStyle("-fx-text-fill: #f8fafc; -fx-font-weight: bold;");
        slider.valueProperty().addListener((obs, old, val) -> valueLabel.setText(String.valueOf(val.intValue())));

        grid.add(labelNode, 0, row);
        grid.add(slider, 1, row);
        grid.add(valueLabel, 2, row);
    }

    private void showTeamGoals() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(24));

        Label title = new Label("🎯 Team Goals");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #f8fafc;");

        Button addBtn = new Button("+ Assign Goal");
        addBtn.setStyle(
                "-fx-background-color: #22c55e; -fx-text-fill: white; -fx-padding: 10 20; -fx-background-radius: 6;");
        addBtn.setOnAction(e -> showAssignGoalDialog());

        HBox headerBox = new HBox(title, new Region() {
            {
                HBox.setHgrow(this, Priority.ALWAYS);
            }
        }, addBtn);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        List<Goal> teamGoals = GoalService.getInstance().getTeamGoals(currentUser);

        VBox goalsList = new VBox(12);
        if (teamGoals.isEmpty()) {
            Label empty = new Label("No goals assigned to team.");
            empty.setStyle("-fx-text-fill: #64748b;");
            goalsList.getChildren().add(empty);
        } else {
            for (Goal goal : teamGoals) {
                VBox card = createCard();

                Label empLabel = new Label("👤 " + goal.getEmployee().getName());
                empLabel.setStyle("-fx-text-fill: #818cf8; -fx-font-size: 12px;");

                Label desc = new Label(goal.getDescription());
                desc.setStyle("-fx-text-fill: #f8fafc; -fx-font-size: 14px;");
                desc.setWrapText(true);

                String statusColor = "COMPLETED".equals(goal.getStatus()) ? "#22c55e" : "#f59e0b";
                Label status = new Label("● " + goal.getStatus());
                status.setStyle("-fx-text-fill: " + statusColor + "; -fx-font-size: 11px;");

                card.getChildren().addAll(empLabel, desc, status);
                goalsList.getChildren().add(card);
            }
        }

        ScrollPane scrollPane = new ScrollPane(goalsList);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        panel.getChildren().addAll(headerBox, scrollPane);
        setContent(panel);
    }

    private void showAssignGoalDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Assign Goal");
        dialog.initOwner(FXApp.getPrimaryStage());

        VBox content = new VBox(16);
        content.setPadding(new Insets(24));
        content.setStyle("-fx-background-color: #1e293b;");

        Label title = new Label("🎯 Assign Goal to Team Member");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #f8fafc;");

        List<User> allDescendants = UserService.getInstance().getAllDescendants(currentUser);
        ComboBox<String> memberCombo = new ComboBox<>();
        memberCombo.setPromptText("Select employee");
        for (User member : allDescendants) {
            memberCombo.getItems().add(member.getName() + " (" + member.getUsername() + ")");
        }
        memberCombo.setMaxWidth(Double.MAX_VALUE);
        memberCombo.setStyle("-fx-background-color: #334155;");

        TextArea descArea = new TextArea();
        descArea.setPromptText("Goal description...");
        descArea.setPrefRowCount(4);
        descArea.setStyle(
                "-fx-background-color: #334155; -fx-text-fill: #f8fafc; -fx-control-inner-background: #334155;");

        Button assignBtn = new Button("Assign Goal");
        assignBtn.setMaxWidth(Double.MAX_VALUE);
        assignBtn.setOnAction(e -> {
            int idx = memberCombo.getSelectionModel().getSelectedIndex();
            if (idx >= 0 && !descArea.getText().trim().isEmpty()) {
                User selected = allDescendants.get(idx);
                GoalService.getInstance().createGoal(selected, descArea.getText().trim(), currentUser);
                dialog.close();
                showTeamGoals();
            }
        });

        content.getChildren().addAll(title, memberCombo, descArea, assignBtn);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        dialog.getDialogPane().setStyle("-fx-background-color: #1e293b;");
        dialog.showAndWait();
    }

    private void showAssignTraining() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(24));

        Label title = new Label("📚 Assign Training");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #f8fafc;");

        VBox formCard = createCard();

        List<User> allDescendants = UserService.getInstance().getAllDescendants(currentUser);
        ComboBox<String> memberCombo = new ComboBox<>();
        memberCombo.setPromptText("Select employee");
        for (User member : allDescendants) {
            memberCombo.getItems().add(member.getName());
        }
        memberCombo.setMaxWidth(Double.MAX_VALUE);
        memberCombo.setStyle("-fx-background-color: #334155;");

        List<String> courses = TrainingService.getInstance().getAvailableCourses();
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

        Button assignBtn = new Button("Assign Training");
        assignBtn.setMaxWidth(Double.MAX_VALUE);
        assignBtn.setOnAction(e -> {
            int empIdx = memberCombo.getSelectionModel().getSelectedIndex();
            String selectedCourse = courseCombo.getValue();
            String customCourse = customCourseField.getText().trim();

            // Use custom course if provided, otherwise use dropdown
            String courseName = !customCourse.isEmpty() ? customCourse : selectedCourse;

            if (empIdx >= 0 && courseName != null && !courseName.isEmpty()) {
                User selected = allDescendants.get(empIdx);
                TrainingRecommendation rec = TrainingService.getInstance().createRecommendation(currentUser, selected,
                        courseName);

                Alert alert = new Alert(rec != null ? Alert.AlertType.INFORMATION : Alert.AlertType.WARNING);
                alert.setTitle(rec != null ? "Success" : "Error");
                alert.setContentText(
                        rec != null ? "Training '" + courseName + "' assigned!" : "Could not assign training.");
                alert.showAndWait();
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setContentText("Please select an employee and a course (or enter custom training).");
                alert.showAndWait();
            }
        });

        formCard.getChildren().addAll(
                new Label("Employee:") {
                    {
                        setStyle("-fx-text-fill: #94a3b8;");
                    }
                },
                memberCombo,
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
                assignBtn);

        panel.getChildren().addAll(title, formCard);
        setContent(panel);
    }

    private void showReports() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(24));

        Label title = new Label("📈 Team Reports");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #f8fafc;");

        VBox card = createCard();

        Button genBtn = new Button("📋 Generate Report");
        genBtn.setMaxWidth(Double.MAX_VALUE);
        genBtn.setStyle("-fx-background-color: #6366f1; -fx-text-fill: white; -fx-font-weight: bold;");
        genBtn.setOnAction(e -> {
            String report = ReportGenerator.generateTeamReport(currentUser);
            showReportDialog("Team Performance Report", report);
        });

        Button exportBtn = new Button("📄 Export to HTML");
        exportBtn.setMaxWidth(Double.MAX_VALUE);
        exportBtn.setStyle("-fx-background-color: #22c55e; -fx-text-fill: white; -fx-font-weight: bold;");
        exportBtn.setOnAction(e -> {
            ReportExporter.exportTeamReport(currentUser, FXApp.getPrimaryStage());
        });

        Label exportNote = new Label("Export creates an HTML file that can be printed to PDF");
        exportNote.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px;");

        card.getChildren().addAll(genBtn, exportBtn, exportNote);
        panel.getChildren().addAll(title, card);
        setContent(panel);
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

    private void showSkillGapAnalysis() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(24));

        Label title = new Label("🔍 Skill Gap Analysis");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #f8fafc;");

        Label subtitle = new Label("Identifies team members with scores below 3.0 (threshold for skill gap)");
        subtitle.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 14px;");

        // Summary card
        VBox summaryCard = createCard();
        Label summaryTitle = new Label("📊 Team Summary");
        summaryTitle.setStyle("-fx-text-fill: #f8fafc; -fx-font-size: 16px; -fx-font-weight: bold;");

        String summary = SkillGapService.getInstance().getTeamGapSummary(currentUser);
        Label summaryText = new Label(summary);
        summaryText.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 13px;");

        summaryCard.getChildren().addAll(summaryTitle, summaryText);

        // Individual results
        Label resultsTitle = new Label("👥 Individual Analysis");
        resultsTitle.setStyle("-fx-text-fill: #f8fafc; -fx-font-size: 16px; -fx-font-weight: bold;");

        VBox resultsList = new VBox(10);
        java.util.List<SkillGapService.SkillGapResult> results = SkillGapService.getInstance().analyzeTeam(currentUser);

        if (results.isEmpty()) {
            Label empty = new Label("No team members to analyze.");
            empty.setStyle("-fx-text-fill: #64748b;");
            resultsList.getChildren().add(empty);
        } else {
            for (SkillGapService.SkillGapResult result : results) {
                VBox card = createCard();

                Label empName = new Label("👤 " + result.user.getName());
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
                resultsList.getChildren().add(card);
            }
        }

        ScrollPane scroll = new ScrollPane(resultsList);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        panel.getChildren().addAll(title, subtitle, summaryCard, resultsTitle, scroll);
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

        Label subtitle = new Label("Top 3 performers in your hierarchy and sub-teams");
        subtitle.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 14px;");

        VBox resultsList = new VBox(16);

        java.util.List<TopPerformersService.TeamPerformers> analysis = TopPerformersService.getInstance()
                .getManagerTopPerformersAnalysis(currentUser);

        for (TopPerformersService.TeamPerformers teamResult : analysis) {
            VBox card = createCard();

            Label teamTitle = new Label("👥 " + teamResult.teamName);
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

    // === Personal Panels ===

    private void showMyProfile() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(24));

        Label title = new Label("👤 My Profile");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #f8fafc;");

        VBox card = createCard();
        GridPane grid = new GridPane();
        grid.setHgap(24);
        grid.setVgap(16);

        addProfileRow(grid, 0, "Name", currentUser.getName());
        addProfileRow(grid, 1, "Username", currentUser.getUsername());
        addProfileRow(grid, 2, "Role", isHighboard ? "Highboard Manager" : "Manager");
        addProfileRow(grid, 3, "Tier Level", String.valueOf(currentUser.getTierLevel()));
        addProfileRow(grid, 4, "Direct Reports",
                String.valueOf(currentUser.getDirectReports() != null ? currentUser.getDirectReports().size() : 0));

        card.getChildren().add(grid);
        panel.getChildren().addAll(title, card);
        setContent(panel);
    }

    private void addProfileRow(GridPane grid, int row, String label, String value) {
        Label labelNode = new Label(label + ":");
        labelNode.setStyle("-fx-text-fill: #64748b; -fx-font-weight: bold;");

        Label valueNode = new Label(value);
        valueNode.setStyle("-fx-text-fill: #f8fafc; -fx-font-size: 15px;");

        grid.add(labelNode, 0, row);
        grid.add(valueNode, 1, row);
    }

    private void showMyScores() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(24));

        Label title = new Label("📋 My Performance Scores");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #f8fafc;");

        Label subtitle = new Label("Your aggregated scores from reviews (anonymous - source hidden)");
        subtitle.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 14px;");

        // Get aggregated manager review scores - returns [avgPunctuality, avgTeamwork,
        // avgProductivity, reviewCount]
        double[] scores = ReviewService.getInstance().getAggregatedScores(currentUser);

        VBox card = createCard();

        if (scores[3] == 0) {
            Label empty = new Label("No manager reviews received yet.");
            empty.setStyle("-fx-text-fill: #64748b;");
            card.getChildren().add(empty);
        } else {
            double overall = (scores[0] + scores[1] + scores[2]) / 3;

            Label avgLabel = new Label(String.format("Overall Average: %.1f / 5", overall));
            avgLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #22c55e;");

            GridPane grid = new GridPane();
            grid.setHgap(24);
            grid.setVgap(12);

            addScoreRow(grid, 0, "Punctuality", scores[0]);
            addScoreRow(grid, 1, "Teamwork", scores[1]);
            addScoreRow(grid, 2, "Productivity", scores[2]);

            Label note = new Label("Based on " + (int) scores[3] + " reviews. Source is anonymous.");
            note.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px;");

            card.getChildren().addAll(avgLabel, new Separator(), grid, note);
        }

        // Also show peer review scores if any
        double[] peerScores = ReviewService.getInstance().getAggregatedPeerScores(currentUser);

        panel.getChildren().addAll(title, subtitle, card);

        if (peerScores[3] > 0) {
            VBox peerCard = createCard();

            Label peerTitle = new Label("Peer Review Scores");
            peerTitle.setStyle("-fx-text-fill: #818cf8; -fx-font-size: 16px; -fx-font-weight: bold;");

            double peerOverall = (peerScores[0] + peerScores[1] + peerScores[2]) / 3;
            Label peerAvg = new Label(String.format("Peer Average: %.1f / 5", peerOverall));
            peerAvg.setStyle("-fx-text-fill: #94a3b8;");

            Label peerNote = new Label("Based on " + (int) peerScores[3] + " peer reviews (anonymous).");
            peerNote.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px;");

            peerCard.getChildren().addAll(peerTitle, peerAvg, peerNote);
            panel.getChildren().add(peerCard);
        }

        setContent(panel);
    }

    private void addScoreRow(GridPane grid, int row, String label, double score) {
        Label labelNode = new Label(label + ":");
        labelNode.setStyle("-fx-text-fill: #94a3b8;");

        ProgressBar bar = new ProgressBar(score / 5.0);
        bar.setPrefWidth(200);
        bar.setStyle("-fx-accent: #6366f1;");

        Label valueNode = new Label(String.format("%.1f", score));
        valueNode.setStyle("-fx-text-fill: #f8fafc; -fx-font-weight: bold;");

        grid.add(labelNode, 0, row);
        grid.add(bar, 1, row);
        grid.add(valueNode, 2, row);
    }

    private void showReviewManager() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(24));

        Label title = new Label("📝 Review My Manager");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #f8fafc;");

        User manager = currentUser.getManager();
        if (manager == null) {
            Label noMgr = new Label("You don't have an assigned manager.");
            noMgr.setStyle("-fx-text-fill: #64748b;");
            panel.getChildren().addAll(title, noMgr);
            setContent(panel);
            return;
        }

        Label mgrLabel = new Label("Manager: " + manager.getName());
        mgrLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 16px;");

        VBox card = createCard();

        GridPane form = new GridPane();
        form.setHgap(16);
        form.setVgap(16);

        Slider punctuality = createScoreSlider();
        Slider teamwork = createScoreSlider();
        Slider productivity = createScoreSlider();

        addFormRow(form, 0, "Punctuality", punctuality);
        addFormRow(form, 1, "Teamwork", teamwork);
        addFormRow(form, 2, "Productivity", productivity);

        TextArea comments = new TextArea();
        comments.setPromptText("Additional comments (optional)");
        comments.setPrefRowCount(3);
        comments.setStyle(
                "-fx-background-color: #334155; -fx-text-fill: #f8fafc; -fx-control-inner-background: #334155;");

        Button submitBtn = new Button("Submit Review");
        submitBtn.setMaxWidth(Double.MAX_VALUE);
        submitBtn.setOnAction(e -> {
            UpwardReview review = ReviewService.getInstance().createUpwardReview(
                    currentUser, manager,
                    (int) punctuality.getValue(),
                    (int) teamwork.getValue(),
                    (int) productivity.getValue(),
                    comments.getText());

            Alert alert = new Alert(review != null ? Alert.AlertType.INFORMATION : Alert.AlertType.WARNING);
            alert.setTitle(review != null ? "Success" : "Already Reviewed");
            alert.setContentText(review != null ? "Review submitted successfully!"
                    : "You've already reviewed this manager recently.");
            alert.showAndWait();
        });

        card.getChildren().addAll(form, comments, submitBtn);
        panel.getChildren().addAll(title, mgrLabel, card);
        setContent(panel);
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
        Logger.info("User logged out: " + currentUser.getUsername());
        LoginView loginView = new LoginView();
        Scene scene = new Scene(loginView.getRoot(), 500, 600);
        FXApp.navigateTo(scene);
    }

    public Parent getRoot() {
        return root;
    }
}
