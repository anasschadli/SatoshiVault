package com.wallet.app.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.Parent;

import java.io.IOException;

public class NavigationController {
    @FXML
    private BorderPane contentPane;
    
    @FXML
    private Button homeButton;
    
    @FXML
    private Button sendButton;
    
    @FXML
    private Button receiveButton;
    
    @FXML
    private Button historyButton;
    
    @FXML
    private Button settingsButton;
    
    @FXML
    public void initialize() {
        // Wire up button actions
        homeButton.setOnAction(e -> showHome());
        sendButton.setOnAction(e -> showSend());
        receiveButton.setOnAction(e -> showReceive());
        historyButton.setOnAction(e -> showHistory());
        settingsButton.setOnAction(e -> showSettings());
        
        // Load home view by default
        showHome();
    }
    
    @FXML
    private void showHome() {
        loadView("/com/wallet/app/view/home/HomeView.fxml");
        updateActiveButton(homeButton);
    }
    
    @FXML
    private void showSend() {
        loadView("/com/wallet/app/view/send/SendView.fxml");
        updateActiveButton(sendButton);
    }
    
    @FXML
    private void showReceive() {
        loadView("/com/wallet/app/view/receive/ReceiveView.fxml");
        updateActiveButton(receiveButton);
    }
    
    @FXML
    public void showHistory() {
        loadView("/com/wallet/app/view/history/HistoryView.fxml");
        updateActiveButton(historyButton);
    }
    
    @FXML
    private void showSettings() {
        loadView("/com/wallet/app/view/settings/SettingsView.fxml");
        updateActiveButton(settingsButton);
    }
    
    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            
            // Pass reference to this NavigationController to child controllers
            Object controller = loader.getController();
            if (controller instanceof HomeController) {
                ((HomeController) controller).setNavigationController(this);
            }
            
            contentPane.setCenter(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void updateActiveButton(Button activeButton) {
        // Reset all buttons
        homeButton.getStyleClass().remove("active");
        sendButton.getStyleClass().remove("active");
        receiveButton.getStyleClass().remove("active");
        historyButton.getStyleClass().remove("active");
        settingsButton.getStyleClass().remove("active");
        
        // Highlight active button
        activeButton.getStyleClass().add("active");
    }
}
