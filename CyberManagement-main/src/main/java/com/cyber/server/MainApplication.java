package com.cyber.server;

import com.cyber.server.controller.server.ServerManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainApplication extends Application {
    private static final Logger logger = Logger.getLogger(MainApplication.class.getName());

    @Override
    public void start(Stage stage) {
        try {
            Thread serverThread = new Thread(ServerManager::startServer);
            serverThread.setDaemon(true);
            serverThread.start();
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/cyber/server/view/layout/MainLayout.fxml"));
            if (fxmlLoader.getLocation() == null) {
                throw new IOException("FXML file not found!");
            }
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            Scene scene = new Scene(fxmlLoader.load(), screenBounds.getWidth(), screenBounds.getHeight());
            Image logo = new Image(Objects.requireNonNull(getClass().getResource("/com/cyber/server/assets/logo.jpg")).toExternalForm());
            stage.getIcons().add(logo);
            stage.setTitle("Cyber Management");
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to load the UI: " + e.getMessage(), e);
        } catch (NullPointerException e) {
            logger.log(Level.SEVERE, "Resource not found: " + e.getMessage(), e);
        }
    }

    @Override
    public void stop() {
        logger.info("Application is closing...");
    }

    public static void main(String[] args) {
        launch();
    }
}