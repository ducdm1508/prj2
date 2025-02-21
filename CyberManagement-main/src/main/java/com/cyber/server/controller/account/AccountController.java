package com.cyber.server.controller.account;

import com.cyber.server.database.DatabaseConnection;
import com.cyber.server.model.Account;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AccountController {

    @FXML
    private TableView<Account> userTable;
    @FXML
    private TableColumn<Account, String> usernameColumn;
    @FXML
    private TableColumn<Account, String> passwordColumn;
    @FXML
    private TableColumn<Account, Double> balanceColumn;
    @FXML
    private TableColumn<Account, String> createdDateColumn;
    @FXML
    private TableColumn<Object, Void> actionColumn;
    @FXML
    private TableColumn<Object, Void> addColumn;

    private ObservableList<Account> accountList;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    @FXML
    public void initialize() {
        configureTable();
        loadUsersFromDatabase();

    }
    public void configureTable() {
        accountList = FXCollections.observableArrayList();
        usernameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUsername()));
        passwordColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPassword()));
        balanceColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getBalance()).asObject());
        createdDateColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getCreatedDateColumn()).asString());
        createdDateColumn.setCellValueFactory(cellData -> {
            LocalDateTime dateTime = cellData.getValue().getCreatedDateColumn();
            String formattedDate = (dateTime != null) ? dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) : "N/A";
            return new SimpleStringProperty(formattedDate);
        });

        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            {
                editButton.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.PENCIL));
                deleteButton.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.TRASH));
                editButton.setOnAction(event -> editAccount((Account) getTableView().getItems().get(getIndex())));
                deleteButton.setOnAction(event -> deleteAccount((Account) getTableView().getItems().get(getIndex())));
                HBox buttons = new HBox(10, editButton, deleteButton);
                buttons.setAlignment(Pos.CENTER);
                setGraphic(buttons);
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : getGraphic());
            }
        });
        addColumn.setCellFactory(param -> new TableCell<>() {
            private final Button addMoneyButton = new Button("💰 Deposit");
            {
                addMoneyButton.setOnAction(event -> depositMoney((Account) getTableView().getItems().get(getIndex())));
                HBox buttons = new HBox(10, addMoneyButton);
                buttons.setAlignment(Pos.CENTER);
                setGraphic(buttons);
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : getGraphic());
            }
        });
        userTable.setItems(accountList);

    }

    private void loadUsersFromDatabase() {
        accountList.clear();
        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("USE netcafedb;");
            ResultSet rs = statement.executeQuery("SELECT * FROM users");

            while (rs.next()) {
                Account account = new Account();
                account.setUser_id(rs.getInt("user_id"));
                account.setUsername(rs.getString("username"));
                account.setPassword(rs.getString("password"));
                account.setBalance(rs.getDouble("balance"));

                java.sql.Timestamp timestamp = rs.getTimestamp("create_date");
                if (timestamp != null) {
                    account.setCreatedDateColumn(timestamp.toLocalDateTime());
                }
                accountList.add(account);
            }
            userTable.setItems(accountList);
            userTable.refresh();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Lỗi tải dữ liệu từ database: " + e.getMessage());
        }
    }

    @FXML
    private void searchAccount() {
        TextInputDialog searchDialog = new TextInputDialog();
        searchDialog.setTitle("Search Account");
        searchDialog.setHeaderText("Enter the first letter of the username:");
        searchDialog.setContentText("First letter:");

        searchDialog.showAndWait().ifPresent(letter -> {
            if (letter != null && !letter.trim().isEmpty()) {
                char searchLetter = letter.trim().toUpperCase().charAt(0);
                ObservableList<Account> filteredList = FXCollections.observableArrayList();
                for (Account account : accountList) {
                    String[] nameParts = account.getUsername().split("\\s+"); // Split the name by spaces
                    if (nameParts.length > 0 && nameParts[0].toUpperCase().charAt(0) == searchLetter) {
                        filteredList.add(account);
                    }
                }

                if (!filteredList.isEmpty()) {
                    userTable.setItems(filteredList);
                } else {
                    showAlert("No account found starting with the letter " + searchLetter);
                }
            }
        });
    }

    @FXML
    private void depositMoney(Account selectedAccount) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/cyber/server/view/account/Deposit.fxml"));
            Parent root = loader.load();
            DepositController controller = loader.getController();
            controller.setSelectedAccount(selectedAccount);
            Stage stage = new Stage();
            stage.setTitle("Deposit Money");
            stage.setScene(new Scene(root));
            stage.showAndWait();
            loadUsersFromDatabase();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void editAccount(Account selectedAccount) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/cyber/server/view/account/EditAccount.fxml"));
            Stage stage = new Stage();
            Scene scene = new Scene(loader.load());
            EditAccountController controller = loader.getController();
            controller.setSelectedAccount(selectedAccount);
            controller.setStage(stage);
            stage.setTitle("Edit Account");
            stage.setScene(scene);
            stage.showAndWait();
            loadUsersFromDatabase();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error opening edit account window.");
        }
    }

    @FXML
    private void addAccount() {
        try {
            // Adjust the path and check the resource loading.
            URL fxmlUrl = getClass().getResource("/com/cyber/server/view/account/AddAccount.fxml");
            if (fxmlUrl == null) {
                showAlert("Could not find AddAccount.fxml file.");
                return; // Exit if FXML is not found
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Stage stage = new Stage();
            Scene scene = new Scene(loader.load());
            AddAccountController controller = loader.getController();
            if (controller != null) {
                controller.setStage(stage);
                stage.setTitle("Add Account");
                stage.setScene(scene);
                stage.showAndWait();
                loadUsersFromDatabase();
            } else {
                showAlert("Failed to load AddAccountController.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error opening add account window: " + e.getMessage());
        }
    }

    private void deleteAccount(Account selectedAccount) {
        if (selectedAccount != null) {
            Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
            confirmDialog.setTitle("Delete Account");
            confirmDialog.setHeaderText("Are you sure you want to delete this account?");
            confirmDialog.setContentText("Username: " + selectedAccount.getUsername());

            confirmDialog.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try (Connection connection = DatabaseConnection.getConnection();
                         PreparedStatement statement = connection.prepareStatement("DELETE FROM users WHERE user_id = ?")) {

                        statement.setInt(1, selectedAccount.getUser_id());

                        int affectedRows = statement.executeUpdate(); // Kiểm tra số dòng bị ảnh hưởng
                        if (affectedRows > 0) {
                            showAlert("Account deleted successfully.");
                            loadUsersFromDatabase(); // Load lại danh sách tài khoản
                        } else {
                            showAlert("Failed to delete account. It may not exist.");
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        showAlert("Error deleting account: " + e.getMessage());
                    }
                }
            });
        } else {
            showAlert("Please select an account to delete.");
        }
    }



    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}