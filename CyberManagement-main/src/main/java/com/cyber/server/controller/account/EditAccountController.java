package com.cyber.server.controller.account;

import com.cyber.server.controller.server.ServerManager;
import com.cyber.server.database.DatabaseConnection;
import com.cyber.server.model.Account;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class EditAccountController {

    @FXML private TextField usernameField;
    @FXML private TextField passwordField;
    @FXML private TextField balanceField;

    private Account selectedAccount;
    private Stage stage;

    public void setSelectedAccount(Account selectedAccount) {
        this.selectedAccount = selectedAccount;
        usernameField.setText(selectedAccount.getUsername());
        passwordField.setText(selectedAccount.getPassword());
        balanceField.setText(String.valueOf(selectedAccount.getBalance()));
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void editAccount() {
        System.out.println("hello world");
        String newUsername = usernameField.getText();
        String newPassword = passwordField.getText();
        String newBalance = balanceField.getText();

        if (newUsername.isEmpty() || newPassword.isEmpty() || newBalance.isEmpty()) {
            showAlert("Both username and password must be filled.");
            return;
        }

        String updateQuery = "UPDATE users SET username = ?, password = ? , balance = ? WHERE user_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(updateQuery)) {

            statement.setString(1, newUsername);
            statement.setString(2, newPassword);
            statement.setString(3, newBalance);
            statement.setInt(4, selectedAccount.getUser_id());

            int affectedRows = statement.executeUpdate();
            if (affectedRows > 0) {
                showAlert("Account updated successfully!");
                ServerManager.sendBalanceUpdate(newUsername, Double.parseDouble(newBalance));
                stage.close();

            } else {
                showAlert("Failed to update account. Please try again.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error updating account: " + e.getMessage());
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
