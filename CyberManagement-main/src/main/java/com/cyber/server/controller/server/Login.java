package com.cyber.server.controller.server;

import com.cyber.server.database.DatabaseConnection;
import com.cyber.server.model.Account;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class Login {
    public static Account getUserInfo(String username,String password) {
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                int id = resultSet.getInt("user_id");
                String name = resultSet.getString("username");
                double balance = resultSet.getDouble("balance");
                LocalDateTime created = resultSet.getTimestamp("create_date").toLocalDateTime();
                return new Account(id, name, null, balance, created);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}