package com.cyber.server.controller.server;

import com.cyber.server.controller.computer.ComputerController;
import com.cyber.server.database.DatabaseConnection;
import com.cyber.server.model.Account;
import javafx.application.Platform;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final String clientIP;
    private PrintWriter out;

    public ClientHandler(Socket clientSocket, String clientIP) {
        this.clientSocket = clientSocket;
        this.clientIP = clientIP;
    }

    @Override
    public void run() {
        boolean isOffline = false;
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            String message;
            while ((message = in.readLine()) != null) {
                if (message.equalsIgnoreCase("OFFLINE")) {
                    ComputerController computerController=new ComputerController();
                    Platform.runLater(() -> computerController.updateStatus(clientIP, "OFFLINE"));
                    isOffline = true;
                    break;
                }
                if (message.startsWith("LOGIN:")) {
                    String[] parts = message.split(":");
                    if (parts.length == 3) {
                        String username = parts[1];
                        String password = parts[2];
                        Account account = Login.getUserInfo(username, password);
                        if (account != null) {
                            String response = String.format("LOGIN_SUCCESS:%d:%s:%.2f",account.getUser_id(), account.getUsername(), account.getBalance());
                            out.println(response);
                            ServerManager.ipsByUsername.put(username, clientIP);
                        } else {
                            out.println("LOGIN_FAILED");
                        }
                    }
                }
                if (message.equalsIgnoreCase("New Order!!!")) {
                    System.out.println("Received INSERT command from client.");
                    out.println("INSERT_SUCCESS");
                }
                if(message.startsWith("DEPOSIT:")){
                    String[] parts = message.split(":");
                    if (parts.length == 4) {
                        int userId= Integer.parseInt(parts[1]);
                        double oldAmount= Double.parseDouble(parts[2]);
                        double deposit = Double.parseDouble(parts[3]);
                        double newBalance=oldAmount+deposit;
                        boolean success = updateBalance(userId, newBalance);

                        if (success) {
                            out.println("DEPOSIT_SUCCESS:" + newBalance);
                        } else {
                            out.println("DEPOSIT_FAILED");
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("❌ Client disconnected: " + clientIP);
        } finally {
            if (!isOffline) {
                ComputerController computerController=new ComputerController();
                Platform.runLater(() -> computerController.updateStatus(clientIP, "ONLINE"));
            }
            closeSocket();
        }
    }
    private boolean updateBalance(int userId, double newBalance) {
        String updateQuery = "UPDATE users SET balance = ? WHERE user_id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(updateQuery)) {
            statement.setDouble(1, newBalance);
            statement.setInt(2, userId);
            int rowsUpdated = statement.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
            System.out.println("🔒 Sent" + clientIP + ": " + message);
        }
    }

    private void closeSocket() {
        try {
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing socket for " + clientIP + ": " + e.getMessage());
        }
    }
}
