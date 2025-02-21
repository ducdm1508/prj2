package com.cyber.server.controller.computer;

import com.cyber.server.controller.server.ServerManager;
import com.cyber.server.model.Computer;
import com.cyber.server.model.Room;
import com.cyber.server.model.Status;
import com.cyber.server.database.DatabaseConnection;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ScheduledExecutorService;

public class ComputerController {

    @FXML
    private TableView<Computer> tableView;

    @FXML
    private TableColumn<Computer, String> nameColumn;

    @FXML
    private TableColumn<Computer, String> statusColumn;

    @FXML
    private TableColumn<Computer, String> specificationsColumn;

    @FXML
    private TableColumn<Computer, String> ipAddressColumn;

    @FXML
    private TableColumn<Computer, String> roomColumn;

    @FXML
    private TableColumn<Computer, String> lastMaintenanceDateColumn;

    @FXML
    private TableColumn<Computer, Void> actionColumn;

    @FXML
    private VBox filterPanel;

    @FXML
    private ComboBox<Room> roomFilter;

    @FXML
    private ComboBox<String> specFilter;

    @FXML
    private ComboBox<Status> statusFilter;

    @FXML
    private TextField searchField;

    private final ObservableList<Computer> computerList = FXCollections.observableArrayList();

    private ScheduledExecutorService scheduler;

    @FXML
    public void initialize() {
        configureTable();
        loadComputersFromDatabase();
        loadRoomsAndStatuses();
        searchField.textProperty().addListener((observable, oldValue, newValue) -> handleSearch());
        startPolling();
    }

    private void startPolling() {
        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            Platform.runLater(this::loadComputersFromDatabase);
        }, 0, 5, TimeUnit.SECONDS);
    }

    private void configureTable() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        statusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus().toString()));

        statusColumn.setCellFactory(column -> new TableCell<Computer, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);

                if (empty || status == null) {
                    setGraphic(null);
                    setStyle("");
                    setText(null);
                } else {
                    Circle statusCircle = new Circle(10);
                    setText(status);
                    if (status.equals("ONLINE")) {
                        statusCircle.setFill(Color.GREEN);
                    } else if (status.equals("OFFLINE")) {
                        statusCircle.setFill(Color.RED);
                    } else if (status.equals("MAINTENANCE")) {
                        statusCircle.setFill(Color.YELLOW);
                    } else {
                        statusCircle.setFill(Color.GRAY);
                    }
                    setGraphic(statusCircle);
                    setStyle("-fx-alignment: center;");
                }
            }
        });

        specificationsColumn.setCellValueFactory(new PropertyValueFactory<>("specifications"));
        ipAddressColumn.setCellValueFactory(new PropertyValueFactory<>("ipAddress"));
        roomColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRoom().getName()));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        lastMaintenanceDateColumn.setCellValueFactory(cellData -> {
            LocalDateTime dateTime = cellData.getValue().getLastMaintenanceDate();
            return new SimpleStringProperty(dateTime != null ? dateTime.format(formatter) : "");
        });

        actionColumn.setCellFactory(column -> new TableCell<Computer, Void>() {
            private final Button editButton = new Button();
            private final Button deleteButton = new Button();
            private final Button lockButton = new Button();
            private final Button unlockButton = new Button();
            private final Button maintenanceButton = new Button();

            {
                editButton.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.PENCIL));
                deleteButton.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.TRASH));
                lockButton.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.LOCK));
                unlockButton.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.UNLOCK));
                maintenanceButton.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.WRENCH));

                editButton.setOnAction(event -> {
                    Computer computer = getTableView().getItems().get(getIndex());
                    openEditWindow(computer);
                });

                deleteButton.setOnAction(event -> {
                    Computer computer = getTableView().getItems().get(getIndex());
                    handleDeleteAction(computer);
                });

                lockButton.setOnAction(event -> {
                    Computer computer = getTableView().getItems().get(getIndex());
                    handleLockAction(computer);
                });

                unlockButton.setOnAction(event -> {
                    Computer computer = getTableView().getItems().get(getIndex());
                    handleUnlockAction(computer);
                });

                maintenanceButton.setOnAction(event -> {
                    Computer computer = getTableView().getItems().get(getIndex());
                    handleMaintenance(computer);
                });

                editButton.setStyle("-fx-background-color: green;");
                deleteButton.setStyle("-fx-background-color: red;");
                lockButton.setStyle("-fx-background-color: white;");
                unlockButton.setStyle("-fx-background-color: orange;");
                maintenanceButton.setStyle("-fx-background-color: yellow;");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox buttons = new HBox(10, editButton, deleteButton, lockButton, unlockButton,  maintenanceButton);
                    buttons.setStyle("-fx-alignment: center;");
                    setGraphic(buttons);
                    setStyle("-fx-alignment: center;");
                }
            }
        });
    }

    private void loadComputersFromDatabase() {
        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("USE netcafedb;");
            ResultSet rs = statement.executeQuery(
                    "SELECT c.*, r.room_name FROM computers c " +
                            "LEFT JOIN rooms r ON c.room_id = r.room_id");

            computerList.clear();
            while (rs.next()) {
                Computer computer = new Computer();
                computer.setId(rs.getInt("computer_id"));
                computer.setName(rs.getString("computer_name"));
                computer.setStatus(Status.valueOf(rs.getString("status").toUpperCase()));
                computer.setSpecifications(rs.getString("specifications"));
                computer.setIpAddress(rs.getString("ip_address"));

                if (rs.getTimestamp("last_maintenance_date") != null) {
                    computer.setLastMaintenanceDate(rs.getTimestamp("last_maintenance_date").toLocalDateTime());
                }

                Room room = new Room();
                room.setName(rs.getString("room_name"));
                room.setId(rs.getInt("room_id"));
                computer.setRoom(room);

                computerList.add(computer);
            }
            tableView.setItems(computerList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateStatus(String ipAddress, String status) {
        String query = "UPDATE computers SET status = ? WHERE ip_address = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, status);
            preparedStatement.setString(2, ipAddress);

            int rows = preparedStatement.executeUpdate();
            if (rows > 0) {
                System.out.println("Updated status of " + ipAddress + " to " + status);

            }else{
                System.out.println("No computer found with IP: " + ipAddress);
            }
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadRoomsAndStatuses() {
        List<Room> rooms = loadRoomsFromDB();
        roomFilter.getItems().add(null);
        roomFilter.getItems().addAll(rooms);
        specFilter.getItems().add(null);
        specFilter.getItems().addAll(getUniqueSpecifications());
        statusFilter.getItems().add(null);
        statusFilter.getItems().addAll(Status.values());
    }

    private List<Room> loadRoomsFromDB() {
        List<Room> rooms = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            ResultSet rs = stmt.executeQuery("SELECT * FROM rooms");
            while (rs.next()) {
                Room room = new Room();
                room.setId(rs.getInt("room_id"));
                room.setName(rs.getString("room_name"));
                rooms.add(room);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rooms;
    }

    private List<String> getUniqueSpecifications() {
        List<String> specifications = new ArrayList<>();
        String sql = "SELECT DISTINCT specifications FROM computers";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String spec = rs.getString("specifications");
                if (spec != null && !spec.isEmpty()) {
                    specifications.add(spec);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return specifications;
    }

    @FXML
    private void openNewWindowAddComputer() {
        openAddEditWindow(null);
    }

    public void openEditWindow(Computer computer) {
        openAddEditWindow(computer);
    }

    private void openAddEditWindow(Computer computer) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/cyber/server/view/computer/AddNewComputer.fxml"));
            Parent root = loader.load();

            FormComputerController controller = loader.getController();

            if (computer != null) {
                controller.setComputer(computer);
            }

            Stage stage = new Stage();
            stage.setTitle(computer == null ? "Add New Computer" : "Edit Computer");
            stage.initModality(Modality.APPLICATION_MODAL);
            Scene scene = new Scene(root);
            Image logo = new Image(Objects.requireNonNull(getClass().getResource("/com/cyber/server/assets/logo.jpg")).toExternalForm());
            stage.getIcons().add(logo);
            stage.setWidth(720);
            stage.setHeight(400);
            stage.setResizable(false);
            stage.setScene(scene);
            stage.showAndWait();
            loadComputersFromDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleDeleteAction(Computer computer) {
        if (computer == null) {
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText("Delete Computer");
        alert.setContentText("Are you sure you want to delete the computer: " + computer.getName() + "?");

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try (Connection connection = DatabaseConnection.getConnection();
                 Statement statement = connection.createStatement()) {

                String deleteQuery = "DELETE FROM computers WHERE computer_id = " + computer.getId();
                int rowsAffected = statement.executeUpdate(deleteQuery);

                if (rowsAffected > 0) {
                    computerList.remove(computer);
                    loadComputersFromDatabase();

                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Success");
                    successAlert.setHeaderText(null);
                    successAlert.setContentText("The computer has been successfully deleted.");
                    successAlert.showAndWait();
                } else {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Error");
                    errorAlert.setHeaderText(null);
                    errorAlert.setContentText("Could not delete the computer. Please try again.");
                    errorAlert.showAndWait();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Error");
                errorAlert.setHeaderText(null);
                errorAlert.setContentText("An error occurred while deleting the computer. Please try again.");
                errorAlert.showAndWait();
            }
        }
    }

    private void handleMaintenance(Computer computer) {
        String sql = "UPDATE computers SET last_maintenance_date = ?, status = ? WHERE computer_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            Status newStatus = (computer.getStatus() == Status.MAINTENANCE) ? Status.OFFLINE : Status.MAINTENANCE;
            LocalDateTime now = LocalDateTime.now();
            pstmt.setTimestamp(1, Timestamp.valueOf(now));
            pstmt.setString(2, newStatus.name());
            pstmt.setInt(3, computer.getId());
            pstmt.executeUpdate();

            computer.setStatus(newStatus);
            loadComputersFromDatabase();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void handleSearch() {
        handleFilter();
    }

    public void handleFilter() {
        String selectedRoom = roomFilter.getValue() != null ? roomFilter.getValue().getName().toLowerCase().trim() : "";
        String selectedSpec = specFilter.getValue() != null ? specFilter.getValue().toLowerCase().trim() : "";
        String selectedStatus = statusFilter.getValue() != null ? statusFilter.getValue().name().toLowerCase().trim() : "";

        ObservableList<Computer> filteredList = computerList.filtered(computer -> {
            boolean matchesRoom = selectedRoom.isEmpty() ||
                    computer.getRoom().getName().toLowerCase().contains(selectedRoom);
            boolean matchesSpec = selectedSpec.isEmpty() ||
                    computer.getSpecifications().toLowerCase().contains(selectedSpec);
            boolean matchesStatus = selectedStatus.isEmpty() ||
                    computer.getStatus().name().toLowerCase().contains(selectedStatus);

            return matchesRoom && matchesSpec && matchesStatus;
        });

        String searchTerm = searchField.getText().toLowerCase().trim();
        if (!searchTerm.isEmpty()) {
            filteredList = filteredList.filtered(computer -> {
                String combinedAttributes = (computer.getName() + " "
                        + computer.getStatus().toString() + " "
                        + computer.getSpecifications() + " "
                        + computer.getIpAddress() + " "
                        + computer.getRoom().getName()).toLowerCase();
                return combinedAttributes.contains(searchTerm);
            });
        }
        tableView.setItems(filteredList);
        tableView.refresh();
    }

    @FXML
    public void toggleFilterPanel() {
        boolean isVisible = filterPanel.isVisible();
        filterPanel.setVisible(!isVisible);
        filterPanel.setManaged(!isVisible);
        clearFilters();
        loadComputersFromDatabase();
    }

    private void clearFilters() {
        roomFilter.setValue(null);
        specFilter.setValue(null);
        statusFilter.setValue(null);
    }
    @FXML
    private void handleLockAction(Computer selectedComputer) {
        if (selectedComputer != null) {
            String clientIP = selectedComputer.getIpAddress();
            ServerManager.lockClient(clientIP);
            System.out.println("🔒 Lock command sent to " + clientIP);
        } else {
            System.out.println("⚠️ No computer selected.");
        }
    }

    @FXML
    private void handleUnlockAction(Computer selectedComputer) {
        if (selectedComputer != null) {
            String clientIP = selectedComputer.getIpAddress();
            ServerManager.unlockClient(clientIP);
            System.out.println("🔒 Unlock command sent to " + clientIP);
        } else {
            System.out.println("⚠️ No computer selected.");
        }
    }
}