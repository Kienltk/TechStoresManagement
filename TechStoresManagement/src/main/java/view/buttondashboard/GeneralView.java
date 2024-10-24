package view.buttondashboard;

import controller.GeneralController;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;
import model.GeneralModel;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Map;

public class GeneralView extends VBox {
    private GeneralController controller = new GeneralController();
    private GeneralModel model = new GeneralModel();

    private Text turnoverLabel = new Text("Turnover: $");
    private Text capitalLabel = new Text("Capital: $");
    private Text profitLabel = new Text("Profit: $");
    private Text stockLabel = new Text("Stock: $");

    private BarChart<String, Number> chart;
    private ComboBox<String> criteriaComboBox;
    private ComboBox<Integer> monthComboBox;
    private ComboBox<Integer> yearComboBox;
    private Map<Integer, Map<String, BigDecimal>> financialData;


    public GeneralView() {
        // Tiêu đề "Dashboard"
        Text title = new Text("Dashboard");

        // Các ô hình chữ nhật với thông tin turnover, capital, profit, stock
        StackPane turnoverPane = createInfoPane(turnoverLabel);
        StackPane capitalPane = createInfoPane(capitalLabel);
        StackPane profitPane = createInfoPane(profitLabel);
        StackPane stockPane = createInfoPane(stockLabel);

        // Nút Reload
        Button reloadButton = new Button("Reload");
        reloadButton.setOnAction(e -> {
            controller.handleReload();
            updateData();
            onDataChanged();
        });

        // Sắp xếp các ô thông tin trên cùng một hàng, cách đều nhau 50px
        HBox infoBox = new HBox(50, turnoverPane, capitalPane, profitPane, stockPane);
        infoBox.setPadding(new Insets(20));
        infoBox.setAlignment(Pos.CENTER);

        criteriaComboBox = new ComboBox<>();
        criteriaComboBox.getItems().addAll("Ngày", "Tháng", "Năm");
        criteriaComboBox.setValue("Tháng");

        // ComboBox cho tháng (1-12)
        monthComboBox = new ComboBox<>();
        for (int i = 1; i <= 12; i++) {
            monthComboBox.getItems().add(i);
        }

        // ComboBox cho năm, lấy từ cơ sở dữ liệu
        yearComboBox = new ComboBox<>();
        yearComboBox.getItems().addAll(model.getAvailableYears());

        // Đặt giá trị cho tháng và năm mặc định
        monthComboBox.setValue(Calendar.getInstance().get(Calendar.MONTH) + 1); // Tháng hiện tại
        yearComboBox.setValue(Calendar.getInstance().get(Calendar.YEAR)); // Năm hiện tại

        // In ra tiêu chí mặc định ngay lập tức
        if (criteriaComboBox.getValue().equals("Tháng")) {
            monthComboBox.setVisible(false); // Ẩn ComboBox tháng
        }
        System.out.println("Tiêu chí: Tháng - Năm: " + yearComboBox.getValue());

        criteriaComboBox.setOnAction(event -> onDataChanged());
        yearComboBox.setOnAction(event -> onDataChanged());
        monthComboBox.setOnAction(event -> onDataChanged());


        // Sắp xếp tiêu đề, các ô thông tin và nút Reload
        VBox vbox = new VBox(20, title, infoBox, reloadButton);
        vbox.setPadding(new Insets(20));
        vbox.setAlignment(Pos.CENTER);

        VBox comboBox = new VBox(10);
        comboBox.getChildren().addAll(new Label("Chọn tiêu chí:"), criteriaComboBox, monthComboBox, yearComboBox);


        this.getChildren().addAll(vbox,comboBox, createChart());

        updateData();
    }

    private StackPane createInfoPane(Text label) {
        // Tạo hình chữ nhật kích thước 235x50
        Rectangle rectangle = new Rectangle(235, 50);
        rectangle.setFill(Color.LIGHTGRAY);
        rectangle.setStroke(Color.BLACK);

        // Gói text vào StackPane để canh giữa
        StackPane pane = new StackPane();
        pane.getChildren().addAll(rectangle, label);
        return pane;
    }

    private void updateData() {
        turnoverLabel.setText("Turnover: $" + controller.getTurnover());
        capitalLabel.setText("Capital: $" + controller.getCapital());
        profitLabel.setText("Profit: $" + controller.getProfit());
        stockLabel.setText("Stock: $" + controller.getStock());
    }

    public BarChart<String, Number> createChart() {
        // Initialize axes for the chart
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Year");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Value");

        // Create series for turnover, capital, and profit
        XYChart.Series<String, Number> seriesTurnover = new XYChart.Series<>();
        seriesTurnover.setName("Turnover");
        XYChart.Series<String, Number> seriesCapital = new XYChart.Series<>();
        seriesCapital.setName("Capital");
        XYChart.Series<String, Number> seriesProfit = new XYChart.Series<>();
        seriesProfit.setName("Profit");

        chart = new BarChart<>(xAxis, yAxis);

        handleCriteriaChange();

        // Populate the series with data and add labels on top of bars
        for (Map.Entry<Integer, Map<String, BigDecimal>> entry : financialData.entrySet()) {
            String year = String.valueOf(entry.getKey());
            BigDecimal turnover = entry.getValue().get("turnover");
            BigDecimal capital = entry.getValue().get("capital");
            BigDecimal profit = entry.getValue().get("profit");

            XYChart.Data<String, Number> turnoverData = new XYChart.Data<>(year, turnover);
            XYChart.Data<String, Number> capitalData = new XYChart.Data<>(year, capital);
            XYChart.Data<String, Number> profitData = new XYChart.Data<>(year, profit);

            addValueLabel(turnoverData);
            addValueLabel(capitalData);
            addValueLabel(profitData);

            seriesTurnover.getData().add(turnoverData);
            seriesCapital.getData().add(capitalData);
            seriesProfit.getData().add(profitData);
        }

        chart.getData().addAll(seriesTurnover, seriesCapital, seriesProfit);
        chart.setTitle("Financial Data by Year");

        return chart;
    }

    private void addValueLabel(XYChart.Data<String, Number> data) {
        Platform.runLater(() -> {
            Node node = data.getNode();  // Lấy node đại diện cho cột
            if (node != null) {  // Đảm bảo node không null
                StackPane parent = (StackPane) node;
                Label label = new Label(data.getYValue().toString());  // Tạo label với giá trị

                // Kiểm tra chiều cao cột
                double barHeight = data.getYValue().doubleValue();
                if (barHeight > 0) {
                    label.setTranslateY(-15);  // Đẩy label lên 15px
                } else {
                    label.setTranslateY(0);  // Điều chỉnh cho cột chiều cao 0
                }

                // Thay đổi kiểu và vị trí của label
                label.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
                parent.getChildren().add(label);  // Thêm label vào stack pane

                Tooltip tooltip = new Tooltip(data.getYValue().toString());
                tooltip.setShowDelay(Duration.ZERO); // Hiển thị tooltip ngay lập tức
                Tooltip.install(node, tooltip);

                Platform.runLater(() -> adjustLabelPosition(label, parent));
            }
        });
    }

    private void adjustLabelPosition(Label label, StackPane parent) {
        // Lấy bề ngang của cột
        double parentWidth = parent.getWidth();
        System.out.println(parentWidth);
        double labelWidth = label.getLayoutBounds().getWidth();
        System.out.println(labelWidth);

        // Kiểm tra xem bề ngang của label có lớn hơn 90% bề ngang của cột hay không
        if (labelWidth > parentWidth) {
            label.setVisible(false);  // Ẩn label nếu nó lớn hơn 90% bề ngang của cột
        } else {
            label.setVisible(true);  // Hiện label nếu không
        }
    }

    public void onDataChanged() {
        updateChart(chart);
    }

    public void handleCriteriaChange() {
        String selectedCriteria = criteriaComboBox.getValue();

        switch (selectedCriteria) {
            case "Tháng":
                monthComboBox.setVisible(false); // Ẩn ComboBox tháng
                yearComboBox.setVisible(true); // Hiện ComboBox năm
                System.out.println("Tiêu chí: Tháng");

                // Cập nhật dữ liệu khi thay đổi năm
                yearComboBox.setOnAction(event -> {
                    System.out.println("Tiêu chí: Tháng - Năm: " + yearComboBox.getValue());
                    financialData = model.getFinancialDataByMonth(yearComboBox.getValue());
                    System.out.println(financialData);
                });
                // Gọi lại để xử lý khi chương trình khởi động
                financialData = model.getFinancialDataByMonth(yearComboBox.getValue());
                System.out.println(financialData);
                break;

            case "Năm":
                monthComboBox.setVisible(false); // Ẩn ComboBox tháng
                yearComboBox.setVisible(false); // Ẩn ComboBox năm
                System.out.println("Tiêu chí: Năm");
                financialData = model.getFinancialDataByYear();
                System.out.println(financialData);
                break;

            case "Ngày":
                monthComboBox.setVisible(true); // Hiện ComboBox tháng
                yearComboBox.setVisible(true); // Hiện ComboBox năm
                System.out.println("Tiêu chí: Ngày - Tháng: " + monthComboBox.getValue() + " - Năm: " + yearComboBox.getValue());

                // Cập nhật dữ liệu khi thay đổi tháng hoặc năm
                yearComboBox.setOnAction(event -> {
                    System.out.println("Tiêu chí: Ngày - Tháng: " + monthComboBox.getValue() + " - Năm: " + yearComboBox.getValue());
                    financialData = model.getFinancialDataByDay(yearComboBox.getValue(), monthComboBox.getValue());
                    System.out.println(financialData);
                });
                monthComboBox.setOnAction(event -> {
                    System.out.println("Tiêu chí: Ngày - Tháng: " + monthComboBox.getValue() + " - Năm: " + yearComboBox.getValue());
                    financialData = model.getFinancialDataByDay(yearComboBox.getValue(), monthComboBox.getValue());
                    System.out.println(financialData);
                });

                // Gọi lại khi khởi chạy để lấy dữ liệu ngay lập tức
                financialData = model.getFinancialDataByDay(yearComboBox.getValue(), monthComboBox.getValue());
                System.out.println(financialData);
                break;
        }
    }

    public void updateChart(BarChart<String, Number> chart) {
        chart.getData().clear();

        handleCriteriaChange();

        XYChart.Series<String, Number> seriesTurnover = new XYChart.Series<>();
        seriesTurnover.setName("Turnover");
        XYChart.Series<String, Number> seriesCapital = new XYChart.Series<>();
        seriesCapital.setName("Capital");
        XYChart.Series<String, Number> seriesProfit = new XYChart.Series<>();
        seriesProfit.setName("Profit");

        for (Map.Entry<Integer, Map<String, BigDecimal>> entry : financialData.entrySet()) {
            String year = String.valueOf(entry.getKey());
            BigDecimal turnover = entry.getValue().get("turnover");
            BigDecimal capital = entry.getValue().get("capital");
            BigDecimal profit = entry.getValue().get("profit");

            // Add data to each series
            XYChart.Data<String, Number> turnoverData = new XYChart.Data<>(year, turnover);
            XYChart.Data<String, Number> capitalData = new XYChart.Data<>(year, capital);
            XYChart.Data<String, Number> profitData = new XYChart.Data<>(year, profit);

            // Add labels to the bars
            addValueLabel(turnoverData);
            addValueLabel(capitalData);
            addValueLabel(profitData);

            seriesTurnover.getData().add(turnoverData);
            seriesCapital.getData().add(capitalData);
            seriesProfit.getData().add(profitData);
        }


        // Thêm lại các series vào biểu đồ
        chart.getData().addAll(seriesTurnover, seriesCapital, seriesProfit);
    }

}
