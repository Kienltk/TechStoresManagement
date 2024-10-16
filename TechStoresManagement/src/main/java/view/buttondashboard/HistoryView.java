package view.buttondashboard;

import controller.HistoryController;
import entity.ProductReceipt;
import entity.Receipt;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.HistoryModel;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

public class HistoryView extends VBox {
    private TableView<Receipt> receiptTable;
    private TextField searchField;
    private HistoryController controller = new HistoryController();

    public HistoryView() {
        BorderPane mainLayout = new BorderPane();
        VBox searchBox = new VBox(10);
        searchField = new TextField();
        searchField.setPromptText("Tìm kiếm theo tên khách hàng");
        Button searchButton = new Button("Tìm kiếm");
        searchButton.setOnAction(e -> controller.loadReceipts(receiptTable, searchField.getText()));

        searchBox.getChildren().addAll(searchField, searchButton);
        mainLayout.setTop(searchBox);

        receiptTable = new TableView<>();
        configureReceiptTable();

        mainLayout.setCenter(receiptTable);

        // Load all receipts initially
        controller.loadReceipts(receiptTable, null);

        this.getChildren().addAll(searchBox, mainLayout);
    }

    private void configureReceiptTable() {
        TableColumn<Receipt, Integer> sttCol = new TableColumn<>("STT");
        sttCol.setCellValueFactory(cellData -> new SimpleIntegerProperty(receiptTable.getItems().indexOf(cellData.getValue()) + 1).asObject());

        TableColumn<Receipt, String> customerNameCol = new TableColumn<>("Tên Khách Hàng");
        customerNameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCustomerName()));

        TableColumn<Receipt, String> storeNameCol = new TableColumn<>("Tên Cửa Hàng");
        storeNameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStoreName()));

        TableColumn<Receipt, String> purchaseDateCol = new TableColumn<>("Thời Gian Mua Hàng");
        purchaseDateCol.setCellValueFactory(cellData -> {
            LocalDateTime purchaseDate = cellData.getValue().getPurchaseDate();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return new SimpleStringProperty(purchaseDate.format(formatter)); // Định dạng thành String
        });

        TableColumn<Receipt, Double> totalCol = new TableColumn<>("Tổng Tiền Hóa Đơn");
        totalCol.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getTotal()).asObject());

        TableColumn<Receipt, Double> profitCol = new TableColumn<>("Lợi Nhuận Hóa Đơn");
        profitCol.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getProfit()).asObject());

        TableColumn<Receipt, Void> viewCol = new TableColumn<>("Hành Động");
        viewCol.setCellFactory(col -> new TableCell<Receipt, Void>() {
            private final Button viewButton = new Button("Xem");

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(viewButton);
                    viewButton.setOnAction(e -> {
                        Receipt receipt = getTableView().getItems().get(getIndex());
                        showReceiptDetails(receipt);
                    });
                }
            }
        });

        receiptTable.getColumns().addAll(sttCol, customerNameCol, storeNameCol, purchaseDateCol, totalCol, profitCol, viewCol);
    }

    private void showReceiptDetails(Receipt receipt) {
        Stage detailStage = new Stage();
        detailStage.setTitle("Chi Tiết Hóa Đơn");

        VBox detailLayout = new VBox(10);
        Label customerLabel = new Label("Tên Khách Hàng: " + receipt.getCustomerName());
        Label phoneLabel = new Label("Số Điện Thoại: " + controller.getCustomerPhone(receipt.getCustomerName()));
        Label storeLabel = new Label("Tên Cửa Hàng: " + receipt.getStoreName());
        Label managerLabel = new Label("Quản Lí Cửa Hàng: " + controller.getStoreManager(receipt.getStoreName()));
        Label cashierLabel = new Label("Tên Thu Ngân: " + controller.getCashier(receipt.getId()));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedPurchaseDate = receipt.getPurchaseDate().format(formatter);

        Label purchaseDateLabel = new Label("Thời Gian Mua Hàng: " + formattedPurchaseDate);
        Label totalLabel = new Label("Tổng Tiền Hóa Đơn: " + receipt.getTotal());
        Label profitLabel = new Label("Lợi Nhuận Hóa Đơn: " + receipt.getProfit());

        TableView<ProductReceipt> productTable = new TableView<>();
        configureProductTable(productTable, receipt.getId());

        Button closeButton = new Button("Đóng");
        closeButton.setOnAction(e -> detailStage.close());

        detailLayout.getChildren().addAll(customerLabel, phoneLabel, storeLabel, managerLabel, cashierLabel, purchaseDateLabel, totalLabel, profitLabel, productTable, closeButton);
        Scene detailScene = new Scene(detailLayout, 800, 600);
        detailStage.initModality(Modality.APPLICATION_MODAL);
        detailStage.setScene(detailScene);
        detailStage.show();
    }

    public void configureProductTable(TableView<ProductReceipt> productTable, int receiptId) {
        HistoryModel model = new HistoryModel();
        ObservableList<ProductReceipt> products = model.getProductReceipts(receiptId);
        productTable.setItems(products);

        // Configure columns for the product table
        TableColumn<ProductReceipt, Integer> productSttCol = new TableColumn<>("STT");
        productSttCol.setCellValueFactory(cellData -> cellData.getValue().idProductProperty().asObject());

        TableColumn<ProductReceipt, String> productNameCol = new TableColumn<>("Tên Sản Phẩm");
        productNameCol.setCellValueFactory(cellData -> cellData.getValue().productNameProperty());

        TableColumn<ProductReceipt, String> brandCol = new TableColumn<>("Brand");
        brandCol.setCellValueFactory(cellData -> cellData.getValue().brandProperty());

        TableColumn<ProductReceipt, Double> purchasePriceCol = new TableColumn<>("Giá Nhập");
        purchasePriceCol.setCellValueFactory(cellData -> cellData.getValue().purchasePriceProperty().asObject());

        TableColumn<ProductReceipt, Double> salePriceCol = new TableColumn<>("Giá Bán");
        salePriceCol.setCellValueFactory(cellData -> cellData.getValue().salePriceProperty().asObject());

        TableColumn<ProductReceipt, Integer> quantityCol = new TableColumn<>("Số Lượng Mua");
        quantityCol.setCellValueFactory(cellData -> cellData.getValue().quantityProperty().asObject());

        TableColumn<ProductReceipt, Double> totalAmountCol = new TableColumn<>("Tổng Tiền");
        totalAmountCol.setCellValueFactory(cellData -> cellData.getValue().totalAmountProperty().asObject());

        TableColumn<ProductReceipt, Double> profitCol = new TableColumn<>("Lợi Nhuận");
        profitCol.setCellValueFactory(cellData -> cellData.getValue().profitProperty().asObject());

        productTable.getColumns().addAll(productSttCol, productNameCol, brandCol, purchasePriceCol, salePriceCol, quantityCol, totalAmountCol, profitCol);
    }
}
