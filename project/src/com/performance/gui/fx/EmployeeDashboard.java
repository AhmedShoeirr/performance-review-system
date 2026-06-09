package com.performance.gui.fx;

import javafx.animation.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.util.Duration;

import com.performance.model.*;
import com.performance.service.*;
import com.performance.util.Logger;

import java.util.List;

/**
 * Employee Dashboard with sidebar navigation.
 */
public class EmployeeDashboard {

    private BorderPane root;
    private VBox sidebar;
    private StackPane contentArea;
    private User currentUser;
    private Button activeNavButton;

    public EmployeeDashboard(User user) {
        this.currentUser = user;
        createView();
        Logger.info("Employee dashboard loaded for: " + user.getName());
    }

    private void createView() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: #0f172a;");

        // Header
        HBox header = createHeader();
        root.setTop(header);

        // Sidebar
        sidebar = createSidebar();
        root.setLeft(sidebar);

        // Content area
        contentArea = new StackPane();
        contentArea.setStyle("-fx-background-color: #0f172a; -fx-padding: 24;");
        root.setCenter(contentArea);

        // Show default content (My Profile)
        showMyProfile();
    }

    private HBox createHeader() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(16, 24, 16, 24));
        header.setSpacing(16);
        header.setStyle("-fx-background-color: #1e293b; -fx-border-color: #334155; -fx-border-width: 0 0 1 0;");

        Label logo = new Label("📊 Performance Review System");
        logo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #f8fafc;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label userLabel = new Label("👤 " + currentUser.getName());
        userLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 14px;");

        Button logoutBtn = new Button("Logout");
        logoutBtn.setStyle(
                "-fx-background-color: #334155; -fx-text-fill: #f8fafc; -fx-padding: 8 16; -fx-background-radius: 6;");
        logoutBtn.setOnAction(e -> logout());

        header.getChildren().addAll(logo, spacer, userLabel, logoutBtn);
        return header;
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(8);
        sidebar.setPadding(new Insets(24, 16, 24, 16));
        sidebar.setPrefWidth(220);
        sidebar.setStyle("-fx-background-color: #1e293b;");

        Label navLabel = new Label("NAVIGATION");
        navLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px; -fx-font-weight: bold;");
        navLabel.setPadding(new Insets(0, 0, 8, 8));

        Button profileBtn = createNavButton("👤", "My Profile", () -> showMyProfile());
        Button goalsBtn = createNavButton("🎯", "My Goals", () -> showMyGoals());
        Button reviewsBtn = createNavButton("⭐", "My Scores", () -> showMyScores());
        Button reviewMgrBtn = createNavButton("📝", "Review Manager", () -> showReviewManager());
        Button trainingBtn = createNavButton("📚", "Training", () -> showTraining());

        // Set first button as active
        setActiveNav(profileBtn);

        sidebar.getChildren().addAll(
                navLabel,
                profileBtn,
                goalsBtn,
                reviewsBtn,
                reviewMgrBtn,
                trainingBtn);

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
        // Animate content change
        content.setOpacity(0);
        contentArea.getChildren().setAll(content);

        FadeTransition ft = new FadeTransition(Duration.millis(200), content);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    // === Content Panels ===

    private void showMyProfile() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(24));

        Label title = new Label("👤 My Profile");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #f8fafc;");

        // Profile card
        VBox card = createCard();

        GridPane grid = new GridPane();
        grid.setHgap(24);
        grid.setVgap(16);

        addProfileRow(grid, 0, "Name", currentUser.getName());
        addProfileRow(grid, 1, "Username", currentUser.getUsername());
        addProfileRow(grid, 2, "Status", currentUser.getStatus().toString());
        addProfileRow(grid, 3, "Tier Level", String.valueOf(currentUser.getTierLevel()));

        if (currentUser.getManager() != null) {
            addProfileRow(grid, 4, "Manager", currentUser.getManager().getName());
        }

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

    private void showMyGoals() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(24));

        Label title = new Label("🎯 My Goals");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #f8fafc;");

        // Add goal button
        Button addBtn = new Button("+ Add Goal");
        addBtn.setStyle(
                "-fx-background-color: #22c55e; -fx-text-fill: white; -fx-padding: 10 20; -fx-background-radius: 6;");
        addBtn.setOnAction(e -> showAddGoalDialog());

        HBox headerBox = new HBox(title, new Region() {
            {
                HBox.setHgrow(this, Priority.ALWAYS);
            }
        }, addBtn);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        // Goals list
        VBox goalsList = new VBox(12);

        List<Goal> goals = GoalService.getInstance().getGoalsForUser(currentUser);
        if (goals.isEmpty()) {
            Label empty = new Label("No goals assigned yet.");
            empty.setStyle("-fx-text-fill: #64748b;");
            goalsList.getChildren().add(empty);
        } else {
            for (Goal goal : goals) {
                goalsList.getChildren().add(createGoalCard(goal));
            }
        }

        ScrollPane scrollPane = new ScrollPane(goalsList);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        panel.getChildren().addAll(headerBox, scrollPane);
        setContent(panel);
    }

    private VBox createGoalCard(Goal goal) {
        VBox card = createCard();
        card.setSpacing(8);

        Label desc = new Label(goal.getDescription());
        desc.setStyle("-fx-text-fill: #f8fafc; -fx-font-size: 15px;");
        desc.setWrapText(true);

        String statusColor = "COMPLETED".equals(goal.getStatus()) ? "#22c55e"
                : "IN_PROGRESS".equals(goal.getStatus()) ? "#f59e0b" : "#64748b";
        Label status = new Label("● " + goal.getStatus());
        status.setStyle("-fx-text-fill: " + statusColor + "; -fx-font-size: 12px;");

        HBox actions = new HBox(8);
        if (!"COMPLETED".equals(goal.getStatus())) {
            Button completeBtn = new Button("Mark Complete");
            completeBtn.setStyle(
                    "-fx-background-color: #22c55e; -fx-text-fill: white; -fx-padding: 6 12; -fx-background-radius: 4; -fx-font-size: 12px;");
            completeBtn.setOnAction(e -> {
                GoalService.getInstance().completeGoal(goal);
                showMyGoals();
            });
            actions.getChildren().add(completeBtn);
        }

        // Delete button - only for goals created by the current user
        if (goal.canBeDeletedBy(currentUser)) {
            Button deleteBtn = new Button("🗑 Delete");
            deleteBtn.setStyle(
                    "-fx-background-color: #ef4444; -fx-text-fill: white; -fx-padding: 6 12; -fx-background-radius: 4; -fx-font-size: 12px;");
            deleteBtn.setOnAction(e -> {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Delete Goal");
                confirm.setContentText("Are you sure you want to delete this goal?");
                confirm.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        GoalService.getInstance().deleteGoal(goal, currentUser);
                        showMyGoals();
                    }
                });
            });
            actions.getChildren().add(deleteBtn);
        }

        card.getChildren().addAll(desc, status, actions);
        return card;
    }

    private void showAddGoalDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Add Goal");
        dialog.initOwner(FXApp.getPrimaryStage());

        VBox content = new VBox(16);
        content.setPadding(new Insets(24));
        content.setStyle("-fx-background-color: #1e293b;");

        Label title = new Label("🎯 Add New Goal");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #f8fafc;");

        TextArea descArea = new TextArea();
        descArea.setPromptText("Enter goal description...");
        descArea.setPrefRowCount(4);
        descArea.setStyle(
                "-fx-background-color: #334155; -fx-text-fill: #f8fafc; -fx-control-inner-background: #334155;");

        Button addBtn = new Button("Add Goal");
        addBtn.setMaxWidth(Double.MAX_VALUE);
        addBtn.setOnAction(e -> {
            if (!descArea.getText().trim().isEmpty()) {
                GoalService.getInstance().createGoal(currentUser, descArea.getText().trim(), currentUser);
                dialog.close();
                showMyGoals();
            }
        });

        content.getChildren().addAll(title, descArea, addBtn);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        dialog.getDialogPane().setStyle("-fx-background-color: #1e293b;");
        dialog.showAndWait();
    }

    private void showMyScores() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(24));

        Label title = new Label("⭐ My Scores");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #f8fafc;");

        // Get reviews for this user
        List<Review> reviews = ReviewService.getInstance().getReviewsForUser(currentUser)
                .stream().filter(r -> "SUBMITTED".equals(r.getStatus())).toList();

        if (reviews.isEmpty()) {
            Label empty = new Label("No reviews received yet.");
            empty.setStyle("-fx-text-fill: #64748b;");
            panel.getChildren().addAll(title, empty);
        } else {
            // Calculate averages (anonymous - don't show reviewer)
            double avgPunctuality = reviews.stream().mapToInt(Review::getPunctualityScore).average().orElse(0);
            double avgTeamwork = reviews.stream().mapToInt(Review::getTeamworkScore).average().orElse(0);
            double avgProductivity = reviews.stream().mapToInt(Review::getProductivityScore).average().orElse(0);
            double overall = (avgPunctuality + avgTeamwork + avgProductivity) / 3;

            VBox card = createCard();

            Label avgLabel = new Label(String.format("Overall Average: %.1f / 5", overall));
            avgLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #22c55e;");

            GridPane grid = new GridPane();
            grid.setHgap(24);
            grid.setVgap(12);

            addScoreRow(grid, 0, "Punctuality", avgPunctuality);
            addScoreRow(grid, 1, "Teamwork", avgTeamwork);
            addScoreRow(grid, 2, "Productivity", avgProductivity);

            Label note = new Label("Scores are aggregated and anonymous.");
            note.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px;");

            card.getChildren().addAll(avgLabel, new Separator(), grid, note);
            panel.getChildren().addAll(title, card);
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

        Label title = new Label("📝 Review Manager");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #f8fafc;");

        User manager = currentUser.getManager();
        if (manager == null) {
            Label noMgr = new Label("You don't have an assigned manager.");
            noMgr.setStyle("-fx-text-fill: #64748b;");
            panel.getChildren().addAll(title, noMgr);
        } else {
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
        }

        setContent(panel);
    }

    private Slider createScoreSlider() {
        Slider slider = new Slider(1, 5, 3);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit(1);
        slider.setMinorTickCount(0);
        slider.setSnapToTicks(true);
        slider.setStyle("-fx-pref-width: 200;");
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

    private void showTraining() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(24));

        Label title = new Label("📚 My Training");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #f8fafc;");

        List<TrainingRecommendation> recs = TrainingService.getInstance().getRecommendationsFor(currentUser);

        VBox list = new VBox(12);
        if (recs.isEmpty()) {
            Label empty = new Label("No training recommendations.");
            empty.setStyle("-fx-text-fill: #64748b;");
            list.getChildren().add(empty);
        } else {
            for (TrainingRecommendation rec : recs) {
                VBox card = createCard();

                Label course = new Label("📖 " + rec.getCourseName());
                course.setStyle("-fx-text-fill: #f8fafc; -fx-font-size: 16px; -fx-font-weight: bold;");

                Label by = new Label(
                        "Recommended by: " + (rec.getRecommender() != null ? rec.getRecommender().getName() : "Admin"));
                by.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px;");

                String statusColor = "COMPLETED".equals(rec.getStatus()) ? "#22c55e" : "#f59e0b";
                Label status = new Label("● " + rec.getStatus());
                status.setStyle("-fx-text-fill: " + statusColor + ";");

                card.getChildren().addAll(course, by, status);
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
