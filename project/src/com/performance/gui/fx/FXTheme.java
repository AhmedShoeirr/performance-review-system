package com.performance.gui.fx;

import javafx.scene.paint.Color;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.BoxBlur;
import javafx.geometry.Insets;

/**
 * Theme constants and utilities for consistent JavaFX styling.
 */
public class FXTheme {

    // === Color Palette (Dark Theme) ===
    public static final Color PRIMARY = Color.web("#6366f1"); // Indigo
    public static final Color PRIMARY_DARK = Color.web("#4f46e5");
    public static final Color PRIMARY_LIGHT = Color.web("#818cf8");

    public static final Color SECONDARY = Color.web("#8b5cf6"); // Violet
    public static final Color ACCENT = Color.web("#06b6d4"); // Cyan

    public static final Color SUCCESS = Color.web("#22c55e"); // Green
    public static final Color WARNING = Color.web("#f59e0b"); // Amber
    public static final Color DANGER = Color.web("#ef4444"); // Red
    public static final Color INFO = Color.web("#3b82f6"); // Blue

    public static final Color BACKGROUND = Color.web("#0f172a"); // Slate 900
    public static final Color SURFACE = Color.web("#1e293b"); // Slate 800
    public static final Color CARD = Color.web("#334155"); // Slate 700
    public static final Color BORDER = Color.web("#475569"); // Slate 600

    public static final Color TEXT_PRIMARY = Color.web("#f8fafc"); // Slate 50
    public static final Color TEXT_SECONDARY = Color.web("#94a3b8"); // Slate 400
    public static final Color TEXT_MUTED = Color.web("#64748b"); // Slate 500

    // === CSS Color Strings ===
    public static final String PRIMARY_CSS = "#6366f1";
    public static final String BACKGROUND_CSS = "#0f172a";
    public static final String SURFACE_CSS = "#1e293b";
    public static final String CARD_CSS = "#334155";
    public static final String TEXT_PRIMARY_CSS = "#f8fafc";
    public static final String TEXT_SECONDARY_CSS = "#94a3b8";

    // === Spacing ===
    public static final double SPACING_XS = 4;
    public static final double SPACING_SM = 8;
    public static final double SPACING_MD = 16;
    public static final double SPACING_LG = 24;
    public static final double SPACING_XL = 32;

    // === Border Radius ===
    public static final double RADIUS_SM = 6;
    public static final double RADIUS_MD = 10;
    public static final double RADIUS_LG = 16;
    public static final double RADIUS_XL = 24;

    // === Font Sizes ===
    public static final double FONT_XS = 12;
    public static final double FONT_SM = 14;
    public static final double FONT_MD = 16;
    public static final double FONT_LG = 20;
    public static final double FONT_XL = 24;
    public static final double FONT_2XL = 32;
    public static final double FONT_3XL = 40;

    /**
     * Initialize theme (called once at startup)
     */
    public static void initialize() {
        // Any global initialization
    }

    /**
     * Create a subtle drop shadow effect
     */
    public static DropShadow createShadow() {
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.3));
        shadow.setRadius(10);
        shadow.setOffsetY(4);
        return shadow;
    }

    /**
     * Create a larger shadow for elevated elements
     */
    public static DropShadow createElevatedShadow() {
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.4));
        shadow.setRadius(20);
        shadow.setOffsetY(8);
        return shadow;
    }

    /**
     * Create a glow effect for focus states
     */
    public static DropShadow createGlow() {
        DropShadow glow = new DropShadow();
        glow.setColor(PRIMARY);
        glow.setRadius(15);
        glow.setSpread(0.3);
        return glow;
    }

    /**
     * Standard padding for cards
     */
    public static Insets cardPadding() {
        return new Insets(SPACING_LG);
    }

    /**
     * Standard padding for containers
     */
    public static Insets containerPadding() {
        return new Insets(SPACING_XL);
    }

    /**
     * Convert Color to CSS hex string
     */
    public static String toHex(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }
}
