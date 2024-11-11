package view.buttondashboardstore;

import controller.GeneralController;
import controller.Session;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;
import model.GeneralModel;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Map;

public class GeneralView extends VBox {

    private final int idStore = Session.getIdStore();

    private GeneralController controller = new GeneralController();
    private GeneralModel model = new GeneralModel();

    private Text turnoverLabel = new Text("Turnover: $");
    private Text capitalLabel = new Text("Capital: $");
    private Text profitLabel = new Text("Profit: $");
    private Text stockLabel = new Text("Stock: ");

    private BarChart<String, Number> barChart;
    private ComboBox<String> criteriaComboBox;
    private ComboBox<Integer> monthComboBox;
    private ComboBox<Integer> yearComboBox;
    private Map<Integer, Map<String, BigDecimal>> financialData;

    private ComboBox<String> rankComboBox;

    private Map<String, Integer> stockProductData;
    private PieChart pieChartStockProduct;
    private Label labelStockProduct;
    private BorderPane rootStockProduct;
    public GeneralView() {
        // Tiêu đề "Dashboard"
        Text title = new Text("Dashboard");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        // Các ô hình chữ nhật với thông tin turnover, capital, profit, stock
        StackPane turnoverPane = createInfoPane(turnoverLabel);
        StackPane capitalPane = createInfoPane(capitalLabel);
        StackPane profitPane = createInfoPane(profitLabel);
        StackPane stockPane = createInfoPane(stockLabel);

        // Sắp xếp các ô thông tin trên cùng một hàng, cách đều nhau 50px
        HBox infoBox = new HBox(50, turnoverPane, capitalPane, profitPane, stockPane);
        infoBox.setPadding(new Insets(20));
        infoBox.setAlignment(Pos.CENTER);

        criteriaComboBox = new ComboBox<>();
        criteriaComboBox.getItems().addAll("Total", "Month", "Year");
        criteriaComboBox.setValue("Year");

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
        if (criteriaComboBox.getValue().equals("Year")) {
            monthComboBox.setVisible(false); // Ẩn ComboBox tháng
        }
        System.out.println("Tiêu chí: Tháng - Năm: " + yearComboBox.getValue());

        rankComboBox = new ComboBox<>();
        rankComboBox.getItems().addAll("Highest", "Minimum");
        rankComboBox.setValue("Highest");
        yearComboBox.getStyleClass().add("combo-box-account");
        monthComboBox.getStyleClass().add("combo-box-account");
        criteriaComboBox.getStyleClass().add("combo-box-account");
        rankComboBox.getStyleClass().add("combo-box-account");
        rankComboBox.setOnAction(event -> handleCriteriaPieChange());

        criteriaComboBox.setOnAction(event -> handleCriteriaChange());
        yearComboBox.setOnAction(event -> handleCriteriaChange());


        // Sắp xếp tiêu đề, các ô thông tin và nút Reload
        VBox vbox = new VBox(20, title, infoBox);
        vbox.setPadding(new Insets(20));
        vbox.setAlignment(Pos.CENTER);

        HBox comboBox = new HBox(30);
        comboBox.getChildren().addAll(criteriaComboBox, yearComboBox, monthComboBox, rankComboBox);
        comboBox.setAlignment(Pos.CENTER);

        HBox charts = new HBox(20,  createBarChart(), createPieChartStockStore());

        VBox layout = new VBox(20);
        layout.getChildren().addAll(vbox,comboBox, charts);

        this.getChildren().addAll(layout);
        this.getStyleClass().add("vbox");
        this.getStylesheets().add(getClass().getResource("/view/popup.css").toExternalForm());

        controller.handleReloadDirector();
        updateData();
        updateBarChart(barChart);
        updatePieChartStockStore();
    }

    private StackPane createInfoPane(Text label) {
        // Tạo hình chữ nhật kích thước 235x50
        Rectangle rectangle = new Rectangle(200, 50);
        rectangle.setFill(Color.web("#4AD4DD")); // Đặt màu nền
        rectangle.setArcWidth(7); // Bo góc
        rectangle.setArcHeight(7); // Bo góc
        label.setFill(Color.web("#ffffff"));

        // Gói text vào StackPane để canh giữa
        StackPane pane = new StackPane();
        pane.getChildren().addAll(rectangle, label);

        // Áp dụng CSS cho label
        label.setStyle("-fx-text-fill: #ffffff;" +
                "-fx-font-size: 14px;" +
                "-fx-font-weight: bold;");

        return pane;
    }


    private void updateData() {
        turnoverLabel.setText("Turnover: $" + controller.getTurnoverStore(idStore));
        capitalLabel.setText("Capital: $" + controller.getCapitalStore(idStore));
        profitLabel.setText("Profit: $" + controller.getProfitStore(idStore));
        stockLabel.setText("Stock: " + controller.getStockStore(idStore));
    }

    public BarChart<String, Number> createBarChart() {
        // Initialize axes for the barChart
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

        barChart = new BarChart<>(xAxis, yAxis);

        financialData = model.getStoreFinancialDataByYear(yearComboBox.getValue(), idStore);

        // Populate the series with data and add labels on top of bars
        for (Map.Entry<Integer, Map<String, BigDecimal>> entry : financialData.entrySet()) {
            String month = String.valueOf(entry.getKey());
            BigDecimal turnover = entry.getValue().get("turnover");
            BigDecimal capital = entry.getValue().get("capital");
            BigDecimal profit = entry.getValue().get("profit");

            XYChart.Data<String, Number> turnoverData = new XYChart.Data<>(month, turnover);
            XYChart.Data<String, Number> capitalData = new XYChart.Data<>(month, capital);
            XYChart.Data<String, Number> profitData = new XYChart.Data<>(month, profit);

            addValueLabel(turnoverData);
            addValueLabel(capitalData);
            addValueLabel(profitData);

            seriesTurnover.getData().add(turnoverData);
            seriesCapital.getData().add(capitalData);
            seriesProfit.getData().add(profitData);
        }

        barChart.getData().addAll(seriesTurnover, seriesCapital, seriesProfit);
        barChart.setTitle("Financial Data by Month");

        return barChart;
    }

    private void addValueLabel(XYChart.Data<String, Number> data) {
        Platform.runLater(() -> {
            Node node = data.getNode();  // Lấy node đại diện cho cột
            if (node != null) {  // Đảm bảo node không null
                StackPane parent = (StackPane) node;

                Tooltip tooltip = new Tooltip(data.getYValue().toString());
                tooltip.setShowDelay(Duration.ZERO); // Hiển thị tooltip ngay lập tức
                Tooltip.install(node, tooltip);

            }
        });
    }

    public void handleCriteriaChange() {
        String selectedCriteria = criteriaComboBox.getValue();
        System.out.println(criteriaComboBox.getValue());

        switch (selectedCriteria) {
            case "Year" -> {
                monthComboBox.setVisible(false); // Ẩn ComboBox tháng
                yearComboBox.setVisible(true); // Hiện ComboBox năm
                System.out.println("Tiêu chí: Tháng");

                // Cập nhật dữ liệu khi thay đổi năm
                yearComboBox.setOnAction(event -> {
                    System.out.println("Tiêu chí: Tháng - Năm: " + yearComboBox.getValue());
                    financialData = model.getStoreFinancialDataByYear(yearComboBox.getValue(), idStore);
                    System.out.println(financialData);
                    updateBarChart(barChart);
                });
                financialData = model.getStoreFinancialDataByYear(yearComboBox.getValue(), idStore);
                System.out.println(financialData);
                updateBarChart(barChart);
            }
            case "Total" -> {
                monthComboBox.setVisible(false); // Ẩn ComboBox tháng
                yearComboBox.setVisible(false); // Ẩn ComboBox năm
                System.out.println("Tiêu chí: Năm");
                financialData = model.getStoreFinancialData(idStore);
                System.out.println(financialData);
                updateBarChart(barChart);
            }
            case "Month" -> {
                monthComboBox.setVisible(true); // Hiện ComboBox tháng
                yearComboBox.setVisible(true); // Hiện ComboBox năm
                System.out.println("Tiêu chí: Ngày - Tháng: " + monthComboBox.getValue() + " - Năm: " + yearComboBox.getValue());

                // Cập nhật dữ liệu khi thay đổi tháng hoặc năm
                yearComboBox.setOnAction(event -> {
                    System.out.println("Tiêu chí: Ngày - Tháng: " + monthComboBox.getValue() + " - Năm: " + yearComboBox.getValue());
                    financialData = model.getStoreFinancialDataByMonth(yearComboBox.getValue(), monthComboBox.getValue(), idStore);
                    System.out.println(financialData);
                    updateBarChart(barChart);
                });
                monthComboBox.setOnAction(event -> {
                    System.out.println("Tiêu chí: Ngày - Tháng: " + monthComboBox.getValue() + " - Năm: " + yearComboBox.getValue());
                    financialData = model.getStoreFinancialDataByMonth(yearComboBox.getValue(), monthComboBox.getValue(), idStore);
                    System.out.println(financialData);
                    updateBarChart(barChart);
                });

                // Gọi lại khi khởi chạy để lấy dữ liệu ngay lập tức
                financialData = model.getStoreFinancialDataByMonth(yearComboBox.getValue(), monthComboBox.getValue(), idStore);
                System.out.println(financialData);
                updateBarChart(barChart);
            }
        }
    }

    public void updateBarChart(BarChart<String, Number> barChart) {
        barChart.getData().clear();
        barChart.setAnimated(false);

        XYChart.Series<String, Number> seriesTurnover = new XYChart.Series<>();
        seriesTurnover.setName("Turnover");
        XYChart.Series<String, Number> seriesCapital = new XYChart.Series<>();
        seriesCapital.setName("Capital");
        XYChart.Series<String, Number> seriesProfit = new XYChart.Series<>();
        seriesProfit.setName("Profit");

        System.out.println(financialData);

        for (Map.Entry<Integer, Map<String, BigDecimal>> entry : financialData.entrySet()) {
            String time = String.valueOf(entry.getKey());
            BigDecimal turnover = entry.getValue().get("turnover");
            BigDecimal capital = entry.getValue().get("capital");
            BigDecimal profit = entry.getValue().get("profit");

            XYChart.Data<String, Number> turnoverData = new XYChart.Data<>(time, turnover);
            XYChart.Data<String, Number> capitalData = new XYChart.Data<>(time, capital);
            XYChart.Data<String, Number> profitData = new XYChart.Data<>(time, profit);

            addValueLabel(turnoverData);
            addValueLabel(capitalData);
            addValueLabel(profitData);

            seriesTurnover.getData().add(turnoverData);
            seriesCapital.getData().add(capitalData);
            seriesProfit.getData().add(profitData);
        }

        barChart.getData().addAll(seriesTurnover, seriesCapital, seriesProfit);
    }

    public void handleCriteriaPieChange() {
        String selectedCriteria = rankComboBox.getValue();
        stockProductData = model.getRankWarehouseProduct(idStore, selectedCriteria);
        updatePieChartStockStore();
    }

    private BorderPane createPieChartStockStore() {
        ObservableList<PieChart.Data> details = FXCollections.observableArrayList();
        stockProductData = model.getRankStoreProduct(idStore, rankComboBox.getValue());

        // Khởi tạo layout
        rootStockProduct = new BorderPane();

        // Tạo tiêu đề cho biểu đồ
        Label chartTitle = new Label("Stock Product");
        chartTitle.setFont(Font.font("SanSerif", FontWeight.BOLD, 15)); // Cài đặt font cho tiêu đề
        rootStockProduct.setTop(chartTitle); // Đặt tiêu đề vào trên cùng layout

        // Kiểm tra xem turnoverStoreData có dữ liệu hay không
        if (stockProductData.isEmpty()) {
            // Nếu không có dữ liệu, hiển thị thông báo
            Label noDataLabel = new Label("Không có dữ liệu");
            noDataLabel.setFont(Font.font("SanSerif", FontWeight.BOLD, 15)); // Cài đặt font cho label
            noDataLabel.setTextFill(Color.RED); // Đặt màu cho chữ
            rootStockProduct.setCenter(noDataLabel); // Đặt thông báo vào giữa layout
            return rootStockProduct; // Trả về layout chứa thông báo
        }

        // Nếu có dữ liệu, tiếp tục xử lý
        for (Map.Entry<String, Integer> storeEntry : stockProductData.entrySet()) {
            String nameStore = storeEntry.getKey();  // Lấy tên cửa hàng
            Integer totalTurnover = storeEntry.getValue(); // Lấy tổng doanh thu

            // Chuyển đổi BigDecimal sang double và thêm vào danh sách
            details.add(new PieChart.Data(nameStore, totalTurnover.doubleValue()));
        }

        // Tính tổng giá trị của biểu đồ
        double totalValue = details.stream().mapToDouble(PieChart.Data::getPieValue).sum();

        pieChartStockProduct = new PieChart(); // Tạo biểu đồ hình tròn
        pieChartStockProduct.setData(details); // Gán dữ liệu cho biểu đồ

        // Đặt biểu đồ vào giữa layout
        rootStockProduct.setCenter(pieChartStockProduct);

        labelStockProduct = new Label(); // Khởi tạo label để hiển thị thông tin
        labelStockProduct.setFont(Font.font("SanSerif", FontWeight.BOLD, 15)); // Cài đặt font cho label

        // Thêm sự kiện cho từng phần của biểu đồ
        pieChartStockProduct.getData().forEach(data -> {
            // Sự kiện cho khi chuột vào vùng của phần dữ liệu
            data.getNode().addEventHandler(MouseEvent.MOUSE_ENTERED, e -> {
                double percentage = (data.getPieValue() / totalValue) * 100; // Tính tỷ lệ phần trăm
                labelStockProduct.setText(data.getName() + " Revenue: " + (int) data.getPieValue() +
                        "\nPercentage: " + String.format("%.2f", percentage) + "%"); // Cập nhật thông tin cho label
            });

            // Sự kiện cho khi chuột rời khỏi vùng của phần dữ liệu
            data.getNode().addEventHandler(MouseEvent.MOUSE_EXITED, e -> {
                labelStockProduct.setText(""); // Xóa nội dung của label khi chuột rời khỏi vùng
            });
        });

        rootStockProduct.setBottom(labelStockProduct); // Đặt label vào dưới cùng layout
        BorderPane.setMargin(labelStockProduct, new Insets(0, 0, 10, 120));

        return rootStockProduct; // Trả về layout chứa biểu đồ
    }

    private void updatePieChartStockStore() {
        ObservableList<PieChart.Data> details = FXCollections.observableArrayList();

        if (pieChartStockProduct == null) {
            labelStockProduct = new Label();
            labelStockProduct.setText("Không có dữ liệu");
            return;
        }

        if (stockProductData.isEmpty()) {
            pieChartStockProduct.setData(FXCollections.observableArrayList()); // Xóa dữ liệu biểu đồ
            labelStockProduct.setText("Không có dữ liệu"); // Cập nhật thông báo
            return; // Kết thúc phương thức nếu không có dữ liệu
        } else {
            labelStockProduct.setText(""); // Nếu có dữ liệu, xóa thông báo
        }

        // Cập nhật dữ liệu biểu đồ
        for (Map.Entry<String, Integer> storeEntry : stockProductData.entrySet()) {
            String nameStore = storeEntry.getKey();  // Lấy tên cửa hàng
            Integer totalTurnover = storeEntry.getValue(); // Lấy tổng doanh thu

            // Chuyển đổi BigDecimal sang double và thêm vào danh sách
            details.add(new PieChart.Data(nameStore, totalTurnover.doubleValue()));
        }

        // Cập nhật dữ liệu cho biểu đồ
        pieChartStockProduct.setData(details);

        // Tính tổng giá trị của biểu đồ
        double totalValue = details.stream().mapToDouble(PieChart.Data::getPieValue).sum();

        // Cập nhật sự kiện cho từng phần của biểu đồ
        pieChartStockProduct.getData().forEach(data -> {
            // Sự kiện cho khi chuột vào vùng của phần dữ liệu
            data.getNode().addEventHandler(MouseEvent.MOUSE_ENTERED, e -> {
                double percentage = (data.getPieValue() / totalValue) * 100; // Tính tỷ lệ phần trăm
                labelStockProduct.setText(data.getName() + " Revenue: " + (int) data.getPieValue() +
                        "\nPercentage: " + String.format("%.2f", percentage) + "%"); // Cập nhật thông tin cho label
            });

            // Sự kiện cho khi chuột rời khỏi vùng của phần dữ liệu
            data.getNode().addEventHandler(MouseEvent.MOUSE_EXITED, e -> {
                labelStockProduct.setText(""); // Xóa nội dung của label khi chuột rời khỏi vùng
            });
        });
    }
}