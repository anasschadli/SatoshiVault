package com.wallet.app;

import com.wallet.app.util.ThemeManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load authentication view
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/wallet/app/view/auth/AuthView.fxml"));
        Parent root = loader.load();
        
        Scene scene = new Scene(root, 600, 400);
        
        // Apply theme based on user preference
        ThemeManager.applyTheme(scene);
        
        primaryStage.setTitle("Bitcoin Wallet - Login");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
