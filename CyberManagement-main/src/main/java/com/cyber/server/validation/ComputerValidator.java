package com.cyber.server.validation;

import com.cyber.server.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ComputerValidator {

    public static boolean isComputerNameExists(String computerName) throws SQLException {
        String sql = "SELECT COUNT(*) FROM computers WHERE computer_name = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, computerName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    public static boolean isComputerIpAddressExists(String computerIpAddress) throws SQLException {
        String sql = "SELECT COUNT(*) FROM computers WHERE ip_address = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, computerIpAddress);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    public static boolean isRoomFull(int roomId, int capacity) throws SQLException {
        String sql = "SELECT COUNT(*) FROM computers WHERE room_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, roomId);
            ResultSet rs = pstmt.executeQuery();
            int computerCount = rs.next() ? rs.getInt(1) : 0;

            return computerCount >= capacity;
        }
    }
}
