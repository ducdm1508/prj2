package com.cyber.server.controller.food;

import com.cyber.server.database.DatabaseConnection;
import com.cyber.server.model.*;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class OrderController {
    @FXML private TableView<Order_Details> orderTable;
    @FXML private TableColumn<Order_Details, Integer> idColumn;
    @FXML private TableColumn<Order_Details, String> userColumn;
    @FXML private TableColumn<Order_Details, LocalDateTime> dateColumn;
    @FXML private TableColumn<Order_Details, String> foodNamesColumn;
    @FXML private TableColumn <Order_Details, String> quantityColumn;
    @FXML private TableColumn<Order_Details, String> imageColumn;
    @FXML private TableColumn<Order_Details, BigDecimal> totalAmountColumn;
    @FXML private TableColumn <Order_Details, Double> discountColumn;
    @FXML private TableColumn<Order_Details, Order_status> statusColumn;

    private Button refreshButton;

    private final ObservableList<Order_Details> orderList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupColumns();
        loadOrders();
    }

    private void setupColumns() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");


        idColumn.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getOrder().getOrderId()).asObject()
        );
        userColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getOrder().getAccount().getUsername())
        );
        dateColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getOrder().getOrderDate())
        );

        dateColumn.setCellFactory(column -> new TableCell<Order_Details, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(date.format(formatter));
                }
            }
        });
        foodNamesColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getFood().getName())
        );
        quantityColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getQuantity())
        );
        imageColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getFood().getImageUrl())
        );
        imageColumn.setCellFactory(column -> new TableCell<Order_Details, String>() {
            @Override
            protected void updateItem(String imageUrls, boolean empty) {
                super.updateItem(imageUrls, empty);
                HBox hbox = new HBox(5);
                hbox.setAlignment(Pos.CENTER_LEFT);
                if (empty || imageUrls == null || imageUrls.isEmpty()) {
                    setGraphic(null);
                } else {
                    String[] urls = imageUrls.split(", ");
                    for (String url : urls) {
                        if (!url.trim().isEmpty()) {
                            ImageView imageView = new ImageView(new Image(url, 50, 50, true, true));
                            hbox.getChildren().add(imageView);
                        }
                    }
                }
                setGraphic(hbox);
            }
        });
        totalAmountColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(BigDecimal.valueOf(cellData.getValue().getTotalprice()))
        );
        discountColumn.setCellValueFactory(cellData ->
                new ReadOnlyObjectWrapper<>(cellData.getValue().getDiscount())
        );
        statusColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getOrder().getStatus())
        );

        statusColumn.setCellFactory(column -> new TableCell<Order_Details, Order_status>() {
            @Override
            protected void updateItem(Order_status status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status.name());
                    switch (status) {
                        case COMPLETED:
                            setStyle("-fx-background-color: green; -fx-text-fill: white;");
                            break;
                        case PENDING:
                            setStyle("-fx-background-color: yellow; -fx-text-fill: black;");
                            break;
                        case CANCELED:
                            setStyle("-fx-background-color: red; -fx-text-fill: white;");
                            break;
                        default:
                            setStyle("");
                            break;
                    }
                }
            }
        });
        orderTable.setItems(orderList);
        orderTable.setEditable(true);
    }

    @FXML
    private void loadOrders() {
        orderList.clear();
        Map<Integer, Order> orderMap = new HashMap<>();

        String query = "SELECT " +
                "orders.order_id, " +
                "users.username, " +
                "orders.order_date, " +
                "order_details.order_detail_id, " +
                "GROUP_CONCAT(foods.food_name SEPARATOR ', ') AS food_names, " +
                "GROUP_CONCAT(foods.image_url SEPARATOR ', ') AS image_urls, " +
                "SUM(order_details.price * order_details.quantity) AS total_price, " +
                "GROUP_CONCAT(order_details.quantity) AS total_quantity, " +
                "SUM(order_details.discount) AS total_discount, " +
                "orders.total_amount, " +
                "orders.status, " +
                "food_categories.category_id, " +
                "food_categories.category_name, " +
                "foods.*, " +
                "users.*, " +
                "order_details.discount " +
                "FROM order_details " +
                "JOIN orders ON order_details.order_id = orders.order_id " +
                "JOIN foods ON order_details.food_id = foods.food_id " +
                "JOIN users ON orders.user_id = users.user_id " +
                "JOIN food_categories ON foods.category_id = food_categories.category_id " +
                "GROUP BY orders.order_id, users.username, orders.order_date, orders.total_amount, orders.status";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int orderId = rs.getInt("order_id");
                int userId = rs.getInt("user_id");
                String username = rs.getString("username");
                LocalDateTime orderDate = rs.getTimestamp("order_date").toLocalDateTime();
                BigDecimal totalAmount = rs.getBigDecimal("total_amount");
                Order_status status = Order_status.valueOf(rs.getString("status"));
                int categoryId = rs.getInt("category_id");
                String categoryName = rs.getString("category_name");
                int foodId = rs.getInt("food_id");
                String foodName = rs.getString("food_names");
                String description = rs.getString("description");
                double price = rs.getDouble("price");
                String imageUrl = rs.getString("image_urls");
                int quantity = rs.getInt("quantity");
                String quantity_order = rs.getString("total_quantity");
                double discount = rs.getDouble("discount");
                Category category = new Category(categoryId, categoryName);
                Food food = new Food(foodId, foodName, description, price, quantity, imageUrl, category);
                Order order = orderMap.get(orderId);
                if (order == null) {
                    Account user = new Account(userId, username, null, 0, null);
                    order = new Order(orderId, user, orderDate, totalAmount, status);
                    orderMap.put(orderId, order);
                }

                Order_Details orderDetail = new Order_Details(0, order, food, quantity_order, price, price * quantity, discount);
                orderList.add(orderDetail);
            }
            orderTable.setItems(orderList);
        } catch (SQLException e) {
            showError("Lỗi khi tải danh sách đơn hàng: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
