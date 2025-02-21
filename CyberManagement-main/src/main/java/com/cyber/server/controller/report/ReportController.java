package com.cyber.server.controller.report;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReportController {
    @FXML
    private BarChart<String, Number> barChart;
    @FXML
    private CategoryAxis xAxis;
    @FXML
    private NumberAxis yAxis;
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;
    @FXML
    private Label messageLabel;
    @FXML
    private PieChart revenuePieChart;  // Biểu đồ PieChart cho tỷ lệ doanh thu theo tháng

    private static final String FILE_PATH = "data/hoadon.txt"; // Đường dẫn file hóa đơn
    @FXML
    private void onShowReport() {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        // Kiểm tra nếu người dùng chưa chọn ngày bắt đầu và ngày kết thúc
        if (startDate == null || endDate == null) {
            messageLabel.setText("Please select both start and end dates.");
            System.out.println("Start Date or End Date is missing!");
            return;
        }

        // Kiểm tra nếu ngày bắt đầu lớn hơn ngày kết thúc
        if (startDate.isAfter(endDate)) {
            messageLabel.setText("Start date must be before or equal to end date.");
            System.out.println("Start Date is after End Date!");
            return;
        }

        // Lấy dữ liệu báo cáo
        ObservableList<XYChart.Data<String, Number>> reportData = getReportData(startDate, endDate);

        // Kiểm tra nếu không có dữ liệu doanh thu
        if (reportData.isEmpty()) {
            messageLabel.setText("No revenue data found for the selected date range.");
            System.out.println("No data for the selected date range.");
            return; // Nếu không có dữ liệu, không cần tiếp tục
        }

        // Hiển thị báo cáo nếu có dữ liệu
        System.out.println("Displaying report...");
        displayReport(reportData);
    }

    private ObservableList<XYChart.Data<String, Number>> getReportData(LocalDate startDate, LocalDate endDate) {
        Map<String, Double> revenueByMonth = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            LocalDate billDate = null;
            double amount = 0.0;

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("Date: ")) {
                    try {
                        String dateString = line.substring(6).trim();
                        billDate = LocalDate.parse(dateString.split(" ")[0], DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    } catch (Exception e) {
                        System.out.println("Lỗi định dạng ngày: " + line);
                        continue;
                    }
                } else if (line.startsWith("Final Amount: ")) {
                    try {
                        amount = Double.parseDouble(line.substring(14).replace(",", ".").trim());

                        if (billDate != null && !billDate.isBefore(startDate) && !billDate.isAfter(endDate)) {
                            String monthYear = billDate.getMonth().toString() + " " + billDate.getYear();
                            revenueByMonth.put(monthYear, revenueByMonth.getOrDefault(monthYear, 0.0) + amount);
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Lỗi đọc số tiền: " + line);
                    }
                }
            }
        } catch (IOException e) {
            messageLabel.setText("Error reading file: " + e.getMessage());
            return FXCollections.observableArrayList();
        }

        ObservableList<XYChart.Data<String, Number>> data = FXCollections.observableArrayList();
        for (Map.Entry<String, Double> entry : revenueByMonth.entrySet()) {
            if (entry.getValue() == 0.0) {
                messageLabel.setText("No revenue for " + entry.getKey());
            }
            data.add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        // Kiểm tra nếu không có doanh thu trong tháng
        if (data.isEmpty()) {
            messageLabel.setText("No revenue data found for the selected date range.");
        }

        return data;
    }


    private void displayReport(ObservableList<XYChart.Data<String, Number>> data) {
        xAxis.setLabel("Month"); // Hiển thị "Month" trên trục X
        yAxis.setLabel("Revenue($)");

        // Lấy danh sách các tên tháng từ dữ liệu để làm nhãn trục X
        List<String> months = data.stream()
                .map(XYChart.Data::getXValue)
                .collect(Collectors.toList());

        xAxis.setCategories(FXCollections.observableArrayList(months)); // Đặt các tháng vào trục X

        // Loại bỏ nhãn trên trục x (category axis)
        xAxis.setTickLabelsVisible(true); // Hiển thị nhãn (tick labels) trên trục X

        // Cập nhật BarChart
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.getData().addAll(data);

        barChart.getData().clear();
        barChart.getData().add(series);

        // Thêm khoảng cách giữa các nhóm cột và các cột trong nhóm
        barChart.setCategoryGap(10);  // Khoảng cách giữa các nhóm cột
        barChart.setBarGap(5);  // Khoảng cách giữa các cột

        // Loại bỏ chú thích trong BarChart
        barChart.setLegendVisible(false); // Tắt legend (chú thích) cho BarChart

        // Tạo PieChart cho tỷ lệ doanh thu
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        for (XYChart.Data<String, Number> dataPoint : data) {
            String month = dataPoint.getXValue();
            double revenue = dataPoint.getYValue().doubleValue();
            pieChartData.add(new PieChart.Data(month, revenue));
        }

        revenuePieChart.setData(pieChartData);
    }
}
