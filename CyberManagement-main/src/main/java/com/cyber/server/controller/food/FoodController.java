package com.cyber.server.controller.food;

import com.cyber.server.model.Category;
import com.cyber.server.model.Food;
import com.cyber.server.database.DatabaseConnection;
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
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;

public class FoodController {

    @FXML
    private TableView<Food> tableView;

    @FXML
    private TableColumn<Food, Integer> idColumn;

    @FXML
    private TableColumn<Food, String> nameColumn;

    @FXML
    private TableColumn<Food, String> descriptionColumn;

    @FXML
    private TableColumn<Food, Double> priceColumn;

    @FXML
    private TableColumn<Food, Integer> quantityColumn;

    @FXML
    private TableColumn<Food, String> imageUrlColumn;

    @FXML
    private TableColumn<Food, String> categoryNameColumn;

    @FXML
    private TableColumn<Food, Void> actionColumn;

    @FXML
    private TextField searchField;

    @FXML
    private Button searchButton, addButton;

    private final ObservableList<Food> foodList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        configureTable();
        loadFoodFromDatabase();
        searchButton.setOnAction(event -> searchFood());
        addButton.setOnAction(event -> openNewWindowAddFood());
    }

    private void configureTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        imageUrlColumn.setCellValueFactory(new PropertyValueFactory<>("imageUrl"));
        imageUrlColumn.setCellFactory(column -> new TableCell<Food, String>() {
            private final ImageView imageView = new ImageView();

            @Override
            protected void updateItem(String imageUrl, boolean empty) {
                super.updateItem(imageUrl, empty);
                if (empty || imageUrl == null || imageUrl.isEmpty()) {
                    setGraphic(null);
                } else {
                    try {
                        Image image;
                        if (imageUrl.startsWith("http") || imageUrl.startsWith("https")) {
                            image = new Image(imageUrl, 100, 100, true, true);
                        } else {
                            File file = new File(imageUrl);
                            if (file.exists()) {
                                image = new Image(file.toURI().toString(), 100, 100, true, true);
                            } else {
                                image = new Image("file:default-image.png", 100, 100, true, true); // Dùng ảnh mặc định nếu file không tồn tại
                            }
                        }
                        imageView.setImage(image);
                        setGraphic(imageView);
                    } catch (Exception e) {
                        e.printStackTrace();
                        setGraphic(null);
                    }
                }
            }
        });

        categoryNameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getCategory().getNameCategory())
        );

        actionColumn.setCellFactory(column -> new TableCell<Food, Void>() {
            private final Button deleteButton = new Button("Delete");
            private final Button editButton = new Button("Edit");
            private final HBox buttonBox = new HBox(10, editButton, deleteButton);

            {
                deleteButton.setStyle("-fx-background-color: red; -fx-text-fill: white;");
                editButton.setStyle("-fx-background-color: blue; -fx-text-fill: white;");

                deleteButton.setOnAction(event -> {
                    Food food = getTableView().getItems().get(getIndex());
                    handleDeleteAction(food);
                });

                editButton.setOnAction(event -> {
                    Food food = getTableView().getItems().get(getIndex());
                    handleEditAction(food);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttonBox);
            }
        });

    }

    private void loadFoodFromDatabase() {
        foodList.clear();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT FOODS.*, FOOD_CATEGORIES.category_id, FOOD_CATEGORIES.category_name " +
                             "FROM FOODS " +
                             "INNER JOIN FOOD_CATEGORIES ON FOODS.category_id = FOOD_CATEGORIES.category_id");
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                Category category = new Category(
                        rs.getInt("category_id"),
                        rs.getString("category_name")
                );

                Food food = new Food(
                        rs.getInt("food_id"),
                        rs.getString("food_name"),
                        rs.getString("description"),
                        rs.getDouble("price"),
                        rs.getInt("quantity"),
                        rs.getString("image_url"),
                        category
                );

                foodList.add(food);
            }
            tableView.setItems(foodList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void searchFood() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            loadFoodFromDatabase();
            return;
        }

        ObservableList<Food> filteredList = FXCollections.observableArrayList();
        for (Food food : foodList) {
            if (food.getName().toLowerCase().contains(keyword.toLowerCase())) {
                filteredList.add(food);
            }
        }
        tableView.setItems(filteredList);
    }

    @FXML
    private void openNewWindowAddFood() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/cyber/server/view/food/AddNewFood.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            Image logo = new Image(Objects.requireNonNull(getClass().getResource("/com/cyber/server/assets/logo_food.png")).toExternalForm());
            stage.getIcons().add(logo);
            stage.setTitle("Add New Food");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
            loadFoodFromDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleEditAction(Food food) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/cyber/server/view/food/AddNewFood.fxml"));
            Parent root = loader.load();
            AddFoodController controller = loader.getController();
            controller.setFoodEdit(food);
            Stage stage = new Stage();
            Image logo = new Image(Objects.requireNonNull(getClass().getResource("/com/cyber/server/assets/logo_food.png")).toExternalForm());
            stage.getIcons().add(logo);
            stage.setTitle("Edit Food");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
            loadFoodFromDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleDeleteAction(Food food) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm deletion");
        alert.setHeaderText("Are you sure you want to delete this item?");
        alert.setContentText("This action cannot be undone!");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try (Connection connection = DatabaseConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement("DELETE FROM FOODS WHERE food_id = ?")) {

                statement.setInt(1, food.getId());
                int rowsAffected = statement.executeUpdate();

                if (rowsAffected > 0) {
                    foodList.remove(food);
                    tableView.refresh();
                    showAlert(Alert.AlertType.INFORMATION, "Success", "The food has been successfully deleted.");
                } else {
                    showAlert(Alert.AlertType.WARNING, "Error", "The food could not be deleted.");
                }

            } catch (SQLException e) {
                if (e.getMessage().contains("foreign key constraint")) {
                    showAlert(Alert.AlertType.ERROR, "Error deleting food",
                            "The food cannot be deleted because it is being used in an order.");
                } else {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Database error", "An error occurred while deleting the food.");
                }
            }
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}