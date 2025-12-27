package com.wallet.app.util;

import javafx.scene.Scene;

import java.util.prefs.Preferences;

public class ThemeManager {
    private static final String DARK_THEME_KEY = "darkTheme";
    private static final String DARK_THEME_CSS = "/com/wallet/app/styles/bitcoin-dark.css";
    private static final String LIGHT_THEME_CSS = "/com/wallet/app/styles/bitcoin-light.css";
    private static final Preferences prefs = Preferences.userNodeForPackage(ThemeManager.class);
    
    /**
     * Check if dark theme is enabled
     */
    public static boolean isDarkThemeEnabled() {
        return prefs.getBoolean(DARK_THEME_KEY, false);
    }
    
    /**
     * Set dark theme preference
     */
    public static void setDarkThemeEnabled(boolean enabled) {
        prefs.putBoolean(DARK_THEME_KEY, enabled);
    }
    
    /**
     * Apply theme to a scene based on current preference
     */
    public static void applyTheme(Scene scene) {
        if (isDarkThemeEnabled()) {
            applyDarkTheme(scene);
        } else {
            applyLightTheme(scene);
        }
    }
    
    /**
     * Apply dark theme to a scene
     */
    public static void applyDarkTheme(Scene scene) {
        scene.getStylesheets().clear();
        String css = ThemeManager.class.getResource(DARK_THEME_CSS).toExternalForm();
        scene.getStylesheets().add(css);
    }
    
    /**
     * Apply light theme to a scene
     */
    public static void applyLightTheme(Scene scene) {
        scene.getStylesheets().clear();
        String css = ThemeManager.class.getResource(LIGHT_THEME_CSS).toExternalForm();
        scene.getStylesheets().add(css);
    }
}
