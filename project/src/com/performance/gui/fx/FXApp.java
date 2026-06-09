package com.performance.gui.fx;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.performance.util.Logger;

/**
 * Main JavaFX Application class.
 * Entry point for the modern JavaFX-based GUI.
 */
public class FXApp extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        Logger.info("Starting JavaFX Application...");

        // Apply global theme
        FXTheme.initialize();

        // Show login screen
        LoginView loginView = new LoginView();
        Scene scene = new Scene(loginView.getRoot(), 500, 600);
        try {
            var css = getClass().getResource("/styles/main.css");
            if (css == null) {
                css = getClass().getResource("/resources/styles/main.css");
            }
            if (css != null) {
                scene.getStylesheets().add(css.toExternalForm());
            }
        } catch (Exception e) {
            Logger.info("Could not load CSS: " + e.getMessage());
        }

        stage.setTitle("Performance Review System");
        stage.setScene(scene);
        stage.setMinWidth(400);
        stage.setMinHeight(500);
        stage.show();

        Logger.info("Login view displayed.");
    }

    /**
     * Get the primary stage for navigation
     */
    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    /**
     * Navigate to a new scene
     */
    public static void navigateTo(Scene scene) {
        try {
            var css = FXApp.class.getResource("/styles/main.css");
            if (css == null) {
                css = FXApp.class.getResource("/resources/styles/main.css");
            }
            if (css != null) {
                scene.getStylesheets().add(css.toExternalForm());
            }
        } catch (Exception e) {
            // Ignore CSS loading errors
        }
        primaryStage.setScene(scene);
    }

    /**
     * Launch the application
     */
    public static void launchApp(String[] args) {
        launch(args);
    }
}
