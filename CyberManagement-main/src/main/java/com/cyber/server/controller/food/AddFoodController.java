package com.cyber.server.controller.food;

import com.cyber.server.database.DatabaseConnection;
import com.cyber.server.model.Category;
import com.cyber.server.model.Food;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AddFoodController {
    @FXML
    private Label titleAction;
    @FXML
    private Button saveButton;
    @FXML
    private TextField nameInput;
    @FXML
    private TextField specificationsInput;
    @FXML
    private TextField priceInput;
    @FXML
    private TextField quantityInput;
    @FXML
    private Label imageUrlLabel;
    @FXML
    private ComboBox<String> categoryComboBox;
    @FXML
    private ImageView previewImageView;

    @FXML
    private TextField customCategoryField;

    private File selectedFile;

    private boolean editMode = false;

    private Food foodEdit;

    @FXML
    public void initialize() {
        categoryComboBox.getItems().add("Khác");
        String query = "SELECT * FROM food_categories";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                String categoryName = resultSet.getString("category_name");
                categoryComboBox.getItems().addAll(categoryName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        customCategoryField.setVisible(false);

        // Lắng nghe sự kiện chọn trong ComboBox
        categoryComboBox.valueProperty().addListener((obs, oldValue, newValue) -> {
            if ("Khác".equals(newValue)) {
                customCategoryField.setVisible(true);
                customCategoryField.requestFocus(); // Focus vào ô nhập
            } else {
                customCategoryField.setVisible(false);
            }
        });
    }

    public void setFoodEdit(Food foodEdit) {
        this.foodEdit = foodEdit;
        this.editMode = true;

        nameInput.setText(foodEdit.getName());
        specificationsInput.setText(foodEdit.getDescription());
        priceInput.setText(String.valueOf(foodEdit.getPrice()));
        quantityInput.setText(String.valueOf(foodEdit.getQuantity()));
        categoryComboBox.setValue(foodEdit.getCategory().getNameCategory());

        if (foodEdit.getImageUrl() != null && !foodEdit.getImageUrl().isEmpty()) {
            selectedFile = new File(foodEdit.getImageUrl());
            imageUrlLabel.setText(selectedFile.getAbsolutePath());
            previewImageView.setImage(new Image(selectedFile.toURI().toString()));
        }
        saveButton.setText("Update");
        titleAction.setText("Update Food");
    }
    @FXML
    private void handleUploadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        selectedFile = fileChooser.showOpenDialog(nameInput.getScene().getWindow());

        if (selectedFile != null) {
            String filePath = selectedFile.getAbsolutePath();
            String shortPath = filePath.length() > 20 ? "..." + filePath.substring(filePath.length() - 20) : filePath;
            imageUrlLabel.setText(shortPath);
            imageUrlLabel.setTooltip(new Tooltip(filePath));
            Image image = new Image(selectedFile.toURI().toString());
            previewImageView.setImage(image);
        }
    }
    private Category category;
    public void insertCategory() {
        String categoryName = customCategoryField.getText().trim();

        if (categoryName.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Vui lòng nhập tên danh mục.");
            return;
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            // Kiểm tra xem danh mục đã tồn tại chưa
            String checkQuery = "SELECT category_id FROM food_categories WHERE category_name = ?";
            PreparedStatement checkStmt = connection.prepareStatement(checkQuery);
            checkStmt.setString(1, categoryName);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Danh mục đã tồn tại.");
            } else {
                // Nếu chưa tồn tại, chèn danh mục mới
                String insertQuery = "INSERT INTO food_categories (category_name) VALUES (?)";
                PreparedStatement insertStmt = connection.prepareStatement(insertQuery);
                insertStmt.setString(1, categoryName);
                insertStmt.executeUpdate();

                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Danh mục đã được thêm.");
                categoryComboBox.getItems().add(categoryName);
                categoryComboBox.setValue(categoryName);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể thêm danh mục.");
        }
    }

    @FXML
    private void handleSaveButton() {
        String name = nameInput.getText();
        String specifications = specificationsInput.getText();
        String priceStr = priceInput.getText();
        String quantityStr = quantityInput.getText();
        String category = categoryComboBox.getValue();

        insertCategory();

        if (name.isEmpty() || specifications.isEmpty() || priceStr.isEmpty() || quantityStr.isEmpty() || category == null) {
            showAlert(Alert.AlertType.ERROR, "Form Incomplete", "Please fill all fields.");
            return;
        }

        String imageUrl = (selectedFile != null) ? selectedFile.getAbsolutePath() : (foodEdit != null ? foodEdit.getImageUrl() : "");

        try (Connection connection = DatabaseConnection.getConnection()) {
            if (editMode) {
                PreparedStatement statement = connection.prepareStatement(
                        "UPDATE FOODS SET food_name=?, description=?, price=?, quantity=?, image_url=?, category_id=? WHERE food_id=?");
                statement.setString(1, name);
                statement.setString(2, specifications);
                statement.setDouble(3, Double.parseDouble(priceStr));
                statement.setInt(4, Integer.parseInt(quantityStr));
                statement.setString(5, imageUrl);
                statement.setInt(6, foodEdit.getCategory().getIdCategory());
                statement.setInt(7, foodEdit.getId());
                statement.executeUpdate();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Food updated successfully.");
            } else {
                PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO FOODS (food_name, description, price, quantity, image_url, category_id) VALUES (?, ?, ?, ?, ?, ?)");
                statement.setString(1, name);
                statement.setString(2, specifications);
                statement.setDouble(3, Double.parseDouble(priceStr));
                statement.setInt(4, Integer.parseInt(quantityStr));
                statement.setString(5, imageUrl);
                statement.setInt(6, 1);
                statement.executeUpdate();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Food added successfully.");
            }
            Stage stage = (Stage) nameInput.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to save food.");
        }
    }

    @FXML
    private void handleCancelButton() {
        nameInput.clear();
        specificationsInput.clear();
        priceInput.clear();
        quantityInput.clear();
        imageUrlLabel.setText("No image selected");
        categoryComboBox.getSelectionModel().clearSelection();
        previewImageView.setImage(null);
        selectedFile= null;
    }

    private void showAlert(AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}