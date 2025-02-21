package com.cyber.server.controller.dashboard;

import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.BarChart;

public class DashBoardController {
    @FXML
    private PieChart usageChart;

    @FXML
    private BarChart<String, Number> revenueChart;

    @FXML
    private void initialize() {
        // Dữ liệu cho PieChart
        usageChart.getData().add(new PieChart.Data("Máy đang sử dụng", 10));
        usageChart.getData().add(new PieChart.Data("Máy trống", 8));
        usageChart.getData().add(new PieChart.Data("Máy bảo trì", 2));

        // Dữ liệu cho BarChart
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Doanh thu theo giờ");
        series.getData().add(new XYChart.Data<>("8h", 200000));
        series.getData().add(new XYChart.Data<>("10h", 400000));
        series.getData().add(new XYChart.Data<>("12h", 300000));
        series.getData().add(new XYChart.Data<>("14h", 500000));
        revenueChart.getData().add(series);
    }
}
