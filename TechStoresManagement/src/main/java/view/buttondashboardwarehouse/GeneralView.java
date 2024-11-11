package view.buttondashboardwarehouse;

import controller.GeneralController;
import controller.Session;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
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
import model.GeneralModel;

import java.util.Map;

public class GeneralView extends VBox {
    private final int idWarehouse = Session.getIdWarehouse();

    private GeneralController controller = new GeneralController();
    private GeneralModel model = new GeneralModel();

    private Text stockLabel = new Text("Stock: ");

    private ComboBox<String> rankComboBox;

    private Map<String, Integer> stockProductData;
    private PieChart pieChartStockProduct;
    private Label labelStockProduct;
    private BorderPane rootStockProduct;

    public GeneralView() {
        // Tiêu đề "Dashboard"
        Text title = new Text("Dashboard");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        StackPane stockPane = createInfoPane(stockLabel);

        // Sắp xếp các ô thông tin trên cùng một hàng, cách đều nhau 50px
        HBox infoBox = new HBox(50, stockPane);
        infoBox.setPadding(new Insets(20));
        infoBox.setAlignment(Pos.CENTER);

        // ComboBox cho xếp hạng
        rankComboBox = new ComboBox<>();
        rankComboBox.getItems().addAll("Highest", "Minimum");
        rankComboBox.setValue("Highest");
        rankComboBox.getStyleClass().add("combo-box-account");
        rankComboBox.setOnAction(event -> handleCriteriaChange());

        // Sắp xếp tiêu đề, các ô thông tin và nút Reload
        VBox vbox = new VBox(20, title, infoBox);
        vbox.setPadding(new Insets(20));
        vbox.setAlignment(Pos.CENTER);
        vbox.setMinWidth(1000);

        HBox comboBox = new HBox(30);
        comboBox.getChildren().addAll(rankComboBox);
        comboBox.setAlignment(Pos.CENTER);
        comboBox.setMinWidth(1000);

        HBox charts = new HBox(20, createPieChartStockWarehouse());
        charts.setMinWidth(1000);

        VBox layout = new VBox(20);
        layout.getChildren().addAll(vbox, comboBox, charts);
        layout.setAlignment(Pos.CENTER);
        layout.setMinWidth(1000);

        this.getChildren().addAll(layout);
        this.setAlignment(Pos.CENTER);
        this.setMinWidth(1000);
        this.getStyleClass().add("vbox");
        this.getStylesheets().add(getClass().getResource("/view/popup.css").toExternalForm());

        controller.handleReloadDWarehouse(idWarehouse);
        updateData();
        updatePieChartStockWarehouse();
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
        stockLabel.setText("Stock: " + controller.getStockWarehouse(idWarehouse));
    }

    public void handleCriteriaChange() {
        String selectedCriteria = rankComboBox.getValue();
        stockProductData = model.getRankWarehouseProduct(idWarehouse, selectedCriteria);
        updatePieChartStockWarehouse();
    }

    private BorderPane createPieChartStockWarehouse() {
        ObservableList<PieChart.Data> details = FXCollections.observableArrayList();
        stockProductData = model.getRankWarehouseProduct(idWarehouse, rankComboBox.getValue());

        // Khởi tạo layout
        rootStockProduct = new BorderPane();
        rootStockProduct.setMinWidth(1000);
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
        labelStockProduct.setFont(Font.font("SanSerif", FontWeight.BOLD, 15));
        labelStockProduct.setStyle("-fx-text-fill: #4AD4DD; -fx-font-weight: bold ; -fx-font-size: 20 ; ");// Cài đặt font cho label

        // Thêm sự kiện cho từng phần của biểu đồ
        pieChartStockProduct.getData().forEach(data -> {
            // Sự kiện cho khi chuột vào vùng của phần dữ liệu
            data.getNode().addEventHandler(MouseEvent.MOUSE_ENTERED, e -> {
                double percentage = (data.getPieValue() / totalValue) * 100; // Tính tỷ lệ phần trăm
                labelStockProduct.setText("Name : "+data.getName() +
                        "                             Revenue : " + (int) data.getPieValue() +
                        "                             Percentage : " + String.format("%.2f", percentage) + "%"); // Cập nhật thông tin cho label
            });

            // Sự kiện cho khi chuột rời khỏi vùng của phần dữ liệu
            data.getNode().addEventHandler(MouseEvent.MOUSE_EXITED, e -> {
                labelStockProduct.setText(""); // Xóa nội dung của label khi chuột rời khỏi vùng
            });
        });

        VBox v = new VBox();
        v.getChildren().addAll(labelStockProduct);
        v.setMinWidth(350);
        v.setMaxHeight(350);
        v.setAlignment(Pos.CENTER_LEFT);
        v.setStyle("-fx-border-color: #ffffff; -fx-border-width: 3px; -fx-border-radius: 5px;-fx-background-color: white;-fx-padding: 20 ;");
        rootStockProduct.setBottom(v); // Đặt label vào dưới cùng layout
        return rootStockProduct; // Trả về layout chứa biểu đồ
    }


    private void updatePieChartStockWarehouse() {
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
                labelStockProduct.setText("Name : "+data.getName() +
                        "                             Revenue : " + (int) data.getPieValue() +
                        "                             Percentage : " + String.format("%.2f", percentage) + "%"); // Cập nhật thông tin cho label
            });

            // Sự kiện cho khi chuột rời khỏi vùng của phần dữ liệu
            data.getNode().addEventHandler(MouseEvent.MOUSE_EXITED, e -> {
                labelStockProduct.setText(""); // Xóa nội dung của label khi chuột rời khỏi vùng
            });
        });
    }
}