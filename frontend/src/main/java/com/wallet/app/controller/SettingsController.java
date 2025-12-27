package com.wallet.app.controller;

import com.wallet.app.util.ThemeManager;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;

public class SettingsController {
    
    @FXML
    private CheckBox darkThemeToggle;
    
    @FXML
    public void initialize() {
        // Set checkbox to current theme preference
        darkThemeToggle.setSelected(ThemeManager.isDarkThemeEnabled());
    }
    
    @FXML
    private void handleDarkThemeToggle() {
        boolean isDarkTheme = darkThemeToggle.isSelected();
        
        // Save preference
        ThemeManager.setDarkThemeEnabled(isDarkTheme);
        
        // Apply theme instantly to current scene
        if (isDarkTheme) {
            ThemeManager.applyDarkTheme(darkThemeToggle.getScene());
        } else {
            ThemeManager.applyLightTheme(darkThemeToggle.getScene());
        }
    }
}
