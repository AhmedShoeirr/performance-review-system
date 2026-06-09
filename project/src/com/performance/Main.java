package com.performance;

import com.performance.gui.fx.FXApp;
import com.performance.util.Logger;

/**
 * Main entry point for the Performance Review System.
 * Launches the JavaFX-based GUI.
 */
public class Main {
    public static void main(String[] args) {
        Logger.info("=== Performance Review System ===");
        Logger.info("Launching JavaFX Application...");
        FXApp.launchApp(args);
    }
}
