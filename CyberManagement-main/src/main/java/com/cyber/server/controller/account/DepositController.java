package com.cyber.server.controller.account;

import com.cyber.server.controller.server.ServerManager;
import com.cyber.server.database.DatabaseConnection;
import com.cyber.server.model.Account;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class DepositController {

    @FXML private TextField amountField;
    @FXML private TextField discountField;
    @FXML private Button depositButton;
    @FXML private Label messageLabel;

    private Account selectedAccount;

    public void setSelectedAccount(Account account) {
        this.selectedAccount = account;
    }

    @FXML
    public void handleDeposit() {
        if (selectedAccount == null) {
            messageLabel.setText("Please select an account.");
            return;
        }

        String amountText = amountField.getText().trim();
        if (amountText.isEmpty()) {
            messageLabel.setText("Please enter an amount.");
            return;
        }

        try {
            double depositAmount = Double.parseDouble(amountText);
            if (depositAmount <= 0) {
                messageLabel.setText("Amount must be greater than 0.");
                return;
            }

            String discountCode = discountField.getText().trim();
            double discountRate = getDiscountRate(discountCode);
            double finalAmount = depositAmount - (depositAmount * discountRate);

            updateBalanceInDatabase(finalAmount);
            saveTransactionToFile(finalAmount, discountCode, discountRate);

            messageLabel.setText("Deposit successful! New balance: " + selectedAccount.getBalance());
            ServerManager.sendBalanceUpdate(selectedAccount.getUsername(), selectedAccount.getBalance());
            Stage stage = (Stage) depositButton.getScene().getWindow();
            stage.close();
        } catch (NumberFormatException e) {
            messageLabel.setText("Invalid amount.");
        }

    }
    private double getDiscountRate(String code) {
        return switch (code.toUpperCase()) {
            case "DISCOUNT10" -> 0.10;
            case "DISCOUNT20" -> 0.20;
            default -> 0.0;
        };
    }

    private void updateBalanceInDatabase(double amount) {
        if (selectedAccount == null) {
            System.out.println("Error: selectedAccount is null. Cannot update balance.");
            return;
        }

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE users SET balance = balance + ? WHERE user_id = ?")) {

            statement.setDouble(1, amount);
            statement.setInt(2, selectedAccount.getUser_id());

            int rowsUpdated = statement.executeUpdate();
            if (rowsUpdated > 0) {
                double newBalance = selectedAccount.getBalance() + amount;
                selectedAccount.setBalance(newBalance);
                String username = selectedAccount.getUsername();
                System.out.println("✅ Balance updated in database: User = " + username + ", New Balance = " + newBalance);
                ServerManager.sendBalanceUpdate(selectedAccount.getUsername(), selectedAccount.getBalance());
            } else {
                System.out.println("⚠️ Failed to update balance for user_id: " + selectedAccount.getUser_id());
                messageLabel.setText("Failed to update balance.");
            }
        } catch (SQLException e) {
            System.err.println("❌ Database error while updating balance: " + e.getMessage());
            e.printStackTrace();
            messageLabel.setText("Database error: " + e.getMessage());
        }
    }

    private void saveTransactionToFile(double amount, String discountCode, double discountRate) {
        String fileName = "data/hoadon.txt";
        String transactionDetails = String.format(
                "Date: %s\nUsername: %s\nAmount Deposited: %.2f\nDiscount Code: %s (%.0f%% off)\nFinal Amount: %.2f\n\n",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")),
                selectedAccount.getUsername(),
                amount / (1 - discountRate),
                discountCode,
                discountRate * 100,
                amount
        );

        try (FileWriter writer = new FileWriter(fileName, true)) {
            writer.write(transactionDetails);
        } catch (IOException e) {
            e.printStackTrace();
            messageLabel.setText("Failed to save transaction record.");
        }
    }
}
