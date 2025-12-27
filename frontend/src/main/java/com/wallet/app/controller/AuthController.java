package com.wallet.app.controller;

import com.wallet.app.util.ThemeManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class AuthController {
    @FXML
    private TextField recoveryPhraseField;
    
    @FXML
    private Label errorLabel;
    
    @FXML
    private void handleUnlock() {
        String phrase = recoveryPhraseField.getText().trim();
        
        // Simple validation: non-empty only
        if (phrase.isEmpty()) {
            errorLabel.setText("Please enter your recovery phrase");
            errorLabel.setVisible(true);
            return;
        }
        
        // Load main layout
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/wallet/app/view/layout/RootLayout.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) recoveryPhraseField.getScene().getWindow();
            Scene scene = new Scene(root, 900, 700);
            
            // Apply theme based on user preference
            ThemeManager.applyTheme(scene);
            
            stage.setScene(scene);
            stage.setTitle("Bitcoin Wallet");
        } catch (IOException e) {
            e.printStackTrace();
            errorLabel.setText("Failed to load main view");
            errorLabel.setVisible(true);
        }
    }
}
