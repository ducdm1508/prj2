package com.cyber.server.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {
    public static void main(String[] args) {
        String createDatabaseSQL = "CREATE DATABASE IF NOT EXISTS NetCafeDB";

        String createUsersTableSQL = """
            CREATE TABLE IF NOT EXISTS USERS (
                user_id INT AUTO_INCREMENT PRIMARY KEY,
                username VARCHAR(255) UNIQUE NOT NULL,
                password VARCHAR(255) NOT NULL,
                balance DECIMAL(10, 2) DEFAULT 0,
                create_date DATETIME DEFAULT CURRENT_TIMESTAMP
            )
        """;

        String createAdminsTableSQL = """
            CREATE TABLE IF NOT EXISTS ADMINS (
                admin_id INT AUTO_INCREMENT PRIMARY KEY,
                adminname VARCHAR(255) UNIQUE NOT NULL,
                password VARCHAR(255) NOT NULL,
                create_date DATETIME DEFAULT CURRENT_TIMESTAMP
            )
        """;

        String createComputersTableSQL = """
            CREATE TABLE IF NOT EXISTS COMPUTERS (
                computer_id INT AUTO_INCREMENT PRIMARY KEY,
                computer_name VARCHAR(255) NOT NULL,
                status ENUM('ONLINE', 'OFFLINE', 'MAINTENANCE') DEFAULT 'OFFLINE',
                specifications TEXT,
                ip_address VARCHAR(45),
                last_maintenance_date DATETIME,
                room_id INT,
                FOREIGN KEY (room_id) REFERENCES ROOMS(room_id)
            )
        """;

        String createRoomTypesTableSQL = """
            CREATE TABLE IF NOT EXISTS ROOM_TYPES (
                room_type_id INT AUTO_INCREMENT PRIMARY KEY,
                type_name ENUM('VIP', 'Standard') NOT NULL,
                description TEXT
            )
        """;

        String createRoomsTableSQL = """
            CREATE TABLE IF NOT EXISTS ROOMS (
                room_id INT AUTO_INCREMENT PRIMARY KEY,
                room_name VARCHAR(255) NOT NULL,
                room_type_id INT,
                capacity INT NOT NULL,
                FOREIGN KEY (room_type_id) REFERENCES ROOM_TYPES(room_type_id)
            )
        """;

        String createSessionsTableSQL = """
            CREATE TABLE IF NOT EXISTS SESSIONS (
                session_id INT AUTO_INCREMENT PRIMARY KEY,
                user_id INT,
                computer_id INT,
                start_time DATETIME NOT NULL,
                end_time DATETIME,
                total_time DECIMAL(10, 2),
                session_cost DECIMAL(10, 2),
                FOREIGN KEY (user_id) REFERENCES USERS(user_id),
                FOREIGN KEY (computer_id) REFERENCES COMPUTERS(computer_id)
            )
        """;

        String createFoodsTableSQL = """
            CREATE TABLE IF NOT EXISTS FOODS (
                food_id INT AUTO_INCREMENT PRIMARY KEY,
                food_name VARCHAR(255) NOT NULL,
                description TEXT,
                price DECIMAL(10, 2) NOT NULL,
                quantity INT DEFAULT 0,
                image_url VARCHAR(255),
                category_id INT,
                FOREIGN KEY (category_id) REFERENCES FOOD_CATEGORIES(category_id)
            )
        """;

        String createFoodCategoriesTableSQL = """
            CREATE TABLE IF NOT EXISTS FOOD_CATEGORIES (
                category_id INT AUTO_INCREMENT PRIMARY KEY,
                category_name VARCHAR(255) NOT NULL
            )
        """;

        String createOrdersTableSQL = """
            CREATE TABLE IF NOT EXISTS ORDERS (
                order_id INT AUTO_INCREMENT PRIMARY KEY,
                user_id INT,
                order_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                total_amount DECIMAL(10, 2),
                status ENUM('PENDING', 'COMPLETED', 'CANCELED') DEFAULT 'PENDING',
                FOREIGN KEY (user_id) REFERENCES USERS(user_id)
            )
        """;

        String createOrderDetailsTableSQL = """
            CREATE TABLE IF NOT EXISTS ORDER_DETAILS (
                order_detail_id INT AUTO_INCREMENT PRIMARY KEY,
                order_id INT,
                food_id INT,
                quantity INT NOT NULL,
                price DECIMAL(10, 2),
                total_price DECIMAL(10, 2),
                discount DECIMAL(10, 2),
                FOREIGN KEY (order_id) REFERENCES ORDERS(order_id),
                FOREIGN KEY (food_id) REFERENCES FOODS(food_id)
            )
        """;

        String createTopUpHistoryTableSQL = """
            CREATE TABLE IF NOT EXISTS TOP_UP_HISTORY (
                top_up_id INT AUTO_INCREMENT PRIMARY KEY,
                user_id INT,
                amount DECIMAL(10, 2) NOT NULL,
                top_up_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                payment_method ENUM('cash', 'card', 'e-wallet') NOT NULL,
                FOREIGN KEY (user_id) REFERENCES USERS(user_id)
            )
        """;

        try (Connection connection = DatabaseConnection.getConnection()) {
            Statement statement = connection.createStatement();

            statement.executeUpdate(createDatabaseSQL);
            System.out.println("Database 'NetCafeDB' created successfully!");

            String useDatabaseSQL = "USE NetCafeDB";
            statement.executeUpdate(useDatabaseSQL);
            System.out.println("Using 'NetCafeDB' database!");

            statement.executeUpdate(createUsersTableSQL);
            statement.executeUpdate(createAdminsTableSQL);
            statement.executeUpdate(createRoomTypesTableSQL);
            statement.executeUpdate(createRoomsTableSQL);
            statement.executeUpdate(createComputersTableSQL);
            statement.executeUpdate(createSessionsTableSQL);
            statement.executeUpdate(createFoodCategoriesTableSQL);
            statement.executeUpdate(createFoodsTableSQL);
            statement.executeUpdate(createOrdersTableSQL);
            statement.executeUpdate(createOrderDetailsTableSQL);
            statement.executeUpdate(createTopUpHistoryTableSQL);

            System.out.println("Tables created successfully!");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
