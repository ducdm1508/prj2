package com.cyber.server.controller.account;

import com.cyber.server.database.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;

public class AddAccountController {

    @FXML private TextField usernameField;
    @FXML private TextField passwordField;
    @FXML private TextField balanceField;
    @FXML private Button addButton;

    private Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void addAccount() {
        String newUsername = usernameField.getText();
        String newPassword = passwordField.getText();
        String balanceStr = balanceField.getText();

        if (newUsername.isEmpty() || newPassword.isEmpty() || balanceStr.isEmpty()) {
            showAlert("All fields must be filled.");
            return;
        }

        try {
            double initialBalance = Double.parseDouble(balanceStr);
            LocalDateTime now = LocalDateTime.now();

            String insertQuery = "INSERT INTO users (username, password, balance, create_date) VALUES (?, ?, ?, ?)";
            try (Connection connection = DatabaseConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(insertQuery)) {

                statement.setString(1, newUsername);
                statement.setString(2, newPassword);
                statement.setDouble(3, initialBalance);
                statement.setTimestamp(4, java.sql.Timestamp.valueOf(now));  // Correct format

                int rowsInserted = statement.executeUpdate();
                if (rowsInserted > 0) {
                    showAlert("Account added successfully!");
                    stage.close();
                } else {
                    showAlert("Failed to add account.");
                }
            }

        } catch (NumberFormatException e) {
            showAlert("Please enter a valid balance.");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error adding account: " + e.getMessage());
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(message);
        alert.showAndWait();
    }
}