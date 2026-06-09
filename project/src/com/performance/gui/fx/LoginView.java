package com.performance.gui.fx;

import javafx.animation.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.effect.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.text.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import com.performance.model.*;
import com.performance.service.UserService;
import com.performance.util.DataStore;
import com.performance.util.Logger;

/**
 * Modern JavaFX Login View with glassmorphism design.
 */
public class LoginView {

    private VBox root;
    private TextField usernameField;
    private PasswordField passwordField;
    private TextField passwordTextField;
    private boolean passwordVisible = false;
    private Label errorLabel;

    public LoginView() {
        createView();
        checkFirstRun();
    }

    private void createView() {
        // Root container with gradient background
        root = new VBox();
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #0f172a, #1e1b4b, #0f172a);");

        // Animated background circles
        Pane backgroundPane = createAnimatedBackground();

        // Main card
        VBox card = createLoginCard();

        // Stack background and card
        StackPane stack = new StackPane(backgroundPane, card);
        VBox.setVgrow(stack, Priority.ALWAYS);
        root.getChildren().add(stack);

        // Animate card entrance
        animateCardEntrance(card);
    }

    private Pane createAnimatedBackground() {
        Pane pane = new Pane();
        pane.setMouseTransparent(true);

        // Create floating circles
        for (int i = 0; i < 5; i++) {
            Circle circle = new Circle(50 + Math.random() * 100);
            circle.setFill(Color.web("#6366f1", 0.1));
            circle.setEffect(new GaussianBlur(40));
            circle.setLayoutX(Math.random() * 500);
            circle.setLayoutY(Math.random() * 600);

            // Floating animation
            TranslateTransition tt = new TranslateTransition(Duration.seconds(4 + Math.random() * 4), circle);
            tt.setByY(-30 + Math.random() * 60);
            tt.setByX(-20 + Math.random() * 40);
            tt.setCycleCount(Animation.INDEFINITE);
            tt.setAutoReverse(true);
            tt.play();

            pane.getChildren().add(circle);
        }

        return pane;
    }

    private VBox createLoginCard() {
        VBox card = new VBox(20);
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(380);
        card.setMaxHeight(500);
        card.setPadding(new Insets(40));
        card.getStyleClass().add("card-elevated");

        // Logo/Icon
        Label logoLabel = new Label("📊");
        logoLabel.setStyle("-fx-font-size: 48px;");

        // Title
        Label titleLabel = new Label("Performance Review");
        titleLabel.getStyleClass().add("title");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #f8fafc;");

        Label subtitleLabel = new Label("Sign in to continue");
        subtitleLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 14px;");

        VBox headerBox = new VBox(8, logoLabel, titleLabel, subtitleLabel);
        headerBox.setAlignment(Pos.CENTER);

        // Username field
        VBox usernameBox = createFieldBox("Username", false);
        usernameField = (TextField) usernameBox.getChildren().get(1);

        // Password field with toggle
        VBox passwordBox = createPasswordFieldBox();

        // Error label
        errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 12px;");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        // Login button
        Button loginBtn = new Button("🔐  Sign In");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setStyle("-fx-font-size: 15px; -fx-padding: 14 24;");
        loginBtn.setOnAction(e -> handleLogin());

        // Register link
        Hyperlink registerLink = new Hyperlink("Don't have an account? Register");
        registerLink.setStyle("-fx-text-fill: #818cf8; -fx-font-size: 13px;");
        registerLink.setOnAction(e -> showRegisterDialog());

        card.getChildren().addAll(
                headerBox,
                new Region() {
                    {
                        setMinHeight(16);
                    }
                },
                usernameBox,
                passwordBox,
                errorLabel,
                new Region() {
                    {
                        setMinHeight(8);
                    }
                },
                loginBtn,
                registerLink);

        // Apply glassmorphism effect
        card.setStyle(card.getStyle() +
                "-fx-background-color: rgba(30, 41, 59, 0.85);" +
                "-fx-background-radius: 20;" +
                "-fx-border-radius: 20;" +
                "-fx-border-color: rgba(255, 255, 255, 0.1);" +
                "-fx-border-width: 1;");
        card.setEffect(FXTheme.createElevatedShadow());

        return card;
    }

    private VBox createFieldBox(String labelText, boolean isPassword) {
        VBox box = new VBox(8);

        Label label = new Label(labelText);
        label.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 13px; -fx-font-weight: bold;");

        TextField field = isPassword ? new PasswordField() : new TextField();
        field.setPromptText("Enter " + labelText.toLowerCase());
        field.setStyle(
                "-fx-background-color: #1e293b;" +
                        "-fx-text-fill: #f8fafc;" +
                        "-fx-prompt-text-fill: #64748b;" +
                        "-fx-padding: 14 16;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-radius: 10;" +
                        "-fx-border-color: #475569;" +
                        "-fx-border-width: 1;");
        field.setMaxWidth(Double.MAX_VALUE);

        // Focus effect
        field.focusedProperty().addListener((obs, old, focused) -> {
            if (focused) {
                field.setStyle(field.getStyle().replace("-fx-border-color: #475569;", "-fx-border-color: #6366f1;"));
                field.setEffect(FXTheme.createGlow());
            } else {
                field.setStyle(field.getStyle().replace("-fx-border-color: #6366f1;", "-fx-border-color: #475569;"));
                field.setEffect(null);
            }
        });

        box.getChildren().addAll(label, field);
        return box;
    }

    private VBox createPasswordFieldBox() {
        VBox box = new VBox(8);

        Label label = new Label("Password");
        label.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 13px; -fx-font-weight: bold;");

        // Stack for password field and toggle button
        StackPane fieldStack = new StackPane();
        fieldStack.setAlignment(Pos.CENTER_RIGHT);

        passwordField = new PasswordField();
        passwordField.setPromptText("Enter password");

        passwordTextField = new TextField();
        passwordTextField.setPromptText("Enter password");
        passwordTextField.setVisible(false);
        passwordTextField.setManaged(false);

        // Bind text properties
        passwordTextField.textProperty().bindBidirectional(passwordField.textProperty());

        String fieldStyle = "-fx-background-color: #1e293b;" +
                "-fx-text-fill: #f8fafc;" +
                "-fx-prompt-text-fill: #64748b;" +
                "-fx-padding: 14 50 14 16;" +
                "-fx-background-radius: 10;" +
                "-fx-border-radius: 10;" +
                "-fx-border-color: #475569;" +
                "-fx-border-width: 1;";

        passwordField.setStyle(fieldStyle);
        passwordTextField.setStyle(fieldStyle);
        passwordField.setMaxWidth(Double.MAX_VALUE);
        passwordTextField.setMaxWidth(Double.MAX_VALUE);

        // Toggle button
        Button toggleBtn = new Button("👁");
        toggleBtn.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: #64748b;" +
                        "-fx-font-size: 16px;" +
                        "-fx-cursor: hand;");
        toggleBtn.setOnAction(e -> togglePasswordVisibility());
        StackPane.setAlignment(toggleBtn, Pos.CENTER_RIGHT);
        StackPane.setMargin(toggleBtn, new Insets(0, 8, 0, 0));

        fieldStack.getChildren().addAll(passwordField, passwordTextField, toggleBtn);

        // Focus effects
        passwordField.focusedProperty().addListener((obs, old, focused) -> {
            String base = focused ? "-fx-border-color: #6366f1;" : "-fx-border-color: #475569;";
            passwordField.setStyle(fieldStyle.replace("-fx-border-color: #475569;", base));
            passwordField.setEffect(focused ? FXTheme.createGlow() : null);
        });

        box.getChildren().addAll(label, fieldStack);
        return box;
    }

    private void togglePasswordVisibility() {
        passwordVisible = !passwordVisible;
        passwordField.setVisible(!passwordVisible);
        passwordField.setManaged(!passwordVisible);
        passwordTextField.setVisible(passwordVisible);
        passwordTextField.setManaged(passwordVisible);
    }

    private void animateCardEntrance(VBox card) {
        card.setOpacity(0);
        card.setTranslateY(30);

        FadeTransition fade = new FadeTransition(Duration.millis(600), card);
        fade.setFromValue(0);
        fade.setToValue(1);

        TranslateTransition slide = new TranslateTransition(Duration.millis(600), card);
        slide.setFromY(30);
        slide.setToY(0);
        slide.setInterpolator(Interpolator.EASE_OUT);

        ParallelTransition pt = new ParallelTransition(fade, slide);
        pt.setDelay(Duration.millis(200));
        pt.play();
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordVisible ? passwordTextField.getText() : passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password");
            return;
        }

        UserService userService = UserService.getInstance();
        User user = userService.authenticate(username, password);

        if (user == null) {
            showError("Invalid username or password");
            shakeCard();
            return;
        }

        if (user.getStatus() == UserStatus.PENDING) {
            showError("Account pending approval");
            return;
        }

        if (user.getStatus() == UserStatus.SUSPENDED) {
            showError("Account suspended");
            return;
        }

        Logger.info("Login successful: " + username);
        navigateToDashboard(user);
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);

        // Fade in animation
        FadeTransition ft = new FadeTransition(Duration.millis(200), errorLabel);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    private void shakeCard() {
        // Shake animation for wrong password
        Node card = root.getChildren().get(0);
        TranslateTransition tt = new TranslateTransition(Duration.millis(50), card);
        tt.setByX(10);
        tt.setCycleCount(6);
        tt.setAutoReverse(true);
        tt.play();
    }

    private void navigateToDashboard(User user) {
        Stage stage = FXApp.getPrimaryStage();

        Scene dashboardScene;
        if (user instanceof Admin) {
            AdminDashboard dashboard = new AdminDashboard();
            dashboardScene = new Scene(dashboard.getRoot(), 1200, 800);
        } else {
            int tier = user.getTierLevel();
            boolean hasReports = user.getDirectReports() != null && !user.getDirectReports().isEmpty();

            if (tier == UserService.getInstance().getMaxTiers()) {
                ManagerDashboard dashboard = new ManagerDashboard(user, true);
                dashboardScene = new Scene(dashboard.getRoot(), 1100, 750);
            } else if (tier == 1 && !hasReports) {
                EmployeeDashboard dashboard = new EmployeeDashboard(user);
                dashboardScene = new Scene(dashboard.getRoot(), 1000, 700);
            } else {
                ManagerDashboard dashboard = new ManagerDashboard(user, false);
                dashboardScene = new Scene(dashboard.getRoot(), 1100, 750);
            }
        }

        FXApp.navigateTo(dashboardScene);
        stage.centerOnScreen();
    }

    private void showRegisterDialog() {
        // Create registration dialog
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Create Account");
        dialog.initOwner(FXApp.getPrimaryStage());

        VBox content = new VBox(16);
        content.setPadding(new Insets(24));
        content.setStyle("-fx-background-color: #1e293b;");

        Label title = new Label("📝 Create New Account");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #f8fafc;");

        TextField nameField = new TextField();
        nameField.setPromptText("Full Name");
        styleTextField(nameField);

        TextField regUsernameField = new TextField();
        regUsernameField.setPromptText("Username");
        styleTextField(regUsernameField);

        PasswordField regPasswordField = new PasswordField();
        regPasswordField.setPromptText("Password");
        styleTextField(regPasswordField);

        // Password criteria - shown prominently in 2 lines
        Label criteriaLabel1 = new Label("• 8+ characters  • Uppercase  • Lowercase");
        criteriaLabel1.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px;");
        Label criteriaLabel2 = new Label("• Number  • Special symbol (!@#$%^&*)");
        criteriaLabel2.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px;");

        Label regErrorLabel = new Label();
        regErrorLabel.setStyle("-fx-text-fill: #ef4444;");
        regErrorLabel.setVisible(false);

        Button registerBtn = new Button("Create Account");
        registerBtn.setMaxWidth(Double.MAX_VALUE);

        registerBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            String username = regUsernameField.getText().trim();
            String password = regPasswordField.getText();

            if (name.isEmpty() || username.isEmpty() || password.isEmpty()) {
                regErrorLabel.setText("All fields required");
                regErrorLabel.setVisible(true);
                return;
            }

            if (!isValidPassword(password)) {
                regErrorLabel.setText("Password doesn't meet requirements");
                regErrorLabel.setVisible(true);
                return;
            }

            UserService userService = UserService.getInstance();
            if (userService.findByUsername(username) != null) {
                regErrorLabel.setText("Username already taken");
                regErrorLabel.setVisible(true);
                return;
            }

            userService.registerUser(username, password, name);
            dialog.close();

            // Show success
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText("Registration Successful");
            alert.setContentText("Your account is pending. Admin will assign you to the hierarchy.");
            alert.showAndWait();
        });

        content.getChildren().addAll(title, nameField, regUsernameField, regPasswordField,
                criteriaLabel1, criteriaLabel2, regErrorLabel, registerBtn);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        dialog.getDialogPane().setStyle("-fx-background-color: #1e293b;");

        dialog.showAndWait();
    }

    private void styleTextField(TextField field) {
        field.setStyle(
                "-fx-background-color: #334155;" +
                        "-fx-text-fill: #f8fafc;" +
                        "-fx-prompt-text-fill: #64748b;" +
                        "-fx-padding: 12 16;" +
                        "-fx-background-radius: 8;");
    }

    private boolean isValidPassword(String password) {
        if (password.length() < 8)
            return false;
        boolean hasUpper = false, hasLower = false, hasDigit = false, hasSymbol = false;
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c))
                hasUpper = true;
            else if (Character.isLowerCase(c))
                hasLower = true;
            else if (Character.isDigit(c))
                hasDigit = true;
            else
                hasSymbol = true;
        }
        return hasUpper && hasLower && hasDigit && hasSymbol;
    }

    private void checkFirstRun() {
        UserService userService = UserService.getInstance();
        if (userService.getAllUsers().isEmpty()) {
            Admin admin = new Admin("admin", "admin123", "Super Admin");
            DataStore.getInstance().addUser(admin);
            DataStore.getInstance().saveData();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("First Run");
            alert.setHeaderText("Default Admin Created");
            alert.setContentText("Username: admin\nPassword: admin123");
            alert.showAndWait();
        }
    }

    public Parent getRoot() {
        return root;
    }
}
