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
        searchField.setPromptText("Search");
        Button searchButton = new Button("Search");
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
        TableColumn<Receipt, Integer> sttCol = new TableColumn<>("ID");
        sttCol.setCellValueFactory(cellData -> new SimpleIntegerProperty(receiptTable.getItems().indexOf(cellData.getValue()) + 1).asObject());

        TableColumn<Receipt, String> customerNameCol = new TableColumn<>("Customer Name");
        customerNameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCustomerName()));

        TableColumn<Receipt, String> storeNameCol = new TableColumn<>("Store name");
        storeNameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStoreName()));

        TableColumn<Receipt, String> purchaseDateCol = new TableColumn<>("Order time");
        purchaseDateCol.setCellValueFactory(cellData -> {
            LocalDateTime purchaseDate = cellData.getValue().getPurchaseDate();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return new SimpleStringProperty(purchaseDate.format(formatter)); // Định dạng thành String
        });

        TableColumn<Receipt, Double> totalCol = new TableColumn<>("Total bill");
        totalCol.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getTotal()).asObject());

        TableColumn<Receipt, Double> profitCol = new TableColumn<>("Profit bill");
        profitCol.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getProfit()).asObject());

        TableColumn<Receipt, Void> viewCol = new TableColumn<>("View");
        viewCol.setCellFactory(col -> new TableCell<Receipt, Void>() {
            private final Button viewButton = new Button("View");

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
        detailStage.setTitle("Bill Detail");

        VBox detailLayout = new VBox(10);
        Label customerLabel = new Label("Customer name: " + receipt.getCustomerName());
        Label phoneLabel = new Label("Phone number: " + controller.getCustomerPhone(receipt.getCustomerName()));
        Label storeLabel = new Label("Store name: " + receipt.getStoreName());
        Label managerLabel = new Label("Store Manager: " + controller.getStoreManager(receipt.getStoreName()));
        Label cashierLabel = new Label("Cashier: " + controller.getCashier(receipt.getId()));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedPurchaseDate = receipt.getPurchaseDate().format(formatter);

        Label purchaseDateLabel = new Label("Order time: " + formattedPurchaseDate);
        Label totalLabel = new Label("Total bill: " + receipt.getTotal());
        Label profitLabel = new Label("Profit bill: " + receipt.getProfit());

        TableView<ProductReceipt> productTable = new TableView<>();
        configureProductTable(productTable, receipt.getId());

        Button closeButton = new Button("Close");
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
        TableColumn<ProductReceipt, Integer> productSttCol = new TableColumn<>("ID");
        productSttCol.setCellValueFactory(cellData -> cellData.getValue().idProductProperty().asObject());

        TableColumn<ProductReceipt, String> productNameCol = new TableColumn<>("Product Name");
        productNameCol.setCellValueFactory(cellData -> cellData.getValue().productNameProperty());

        TableColumn<ProductReceipt, String> brandCol = new TableColumn<>("Brand");
        brandCol.setCellValueFactory(cellData -> cellData.getValue().brandProperty());

        TableColumn<ProductReceipt, Double> purchasePriceCol = new TableColumn<>("Purchase price");
        purchasePriceCol.setCellValueFactory(cellData -> cellData.getValue().purchasePriceProperty().asObject());

        TableColumn<ProductReceipt, Double> salePriceCol = new TableColumn<>("Sale price");
        salePriceCol.setCellValueFactory(cellData -> cellData.getValue().salePriceProperty().asObject());

        TableColumn<ProductReceipt, Integer> quantityCol = new TableColumn<>("Quantity");
        quantityCol.setCellValueFactory(cellData -> cellData.getValue().quantityProperty().asObject());

        TableColumn<ProductReceipt, Double> totalAmountCol = new TableColumn<>("Total amount");
        totalAmountCol.setCellValueFactory(cellData -> cellData.getValue().totalAmountProperty().asObject());

        TableColumn<ProductReceipt, Double> profitCol = new TableColumn<>("Profit");
        profitCol.setCellValueFactory(cellData -> cellData.getValue().profitProperty().asObject());

        productTable.getColumns().addAll(productSttCol, productNameCol, brandCol, purchasePriceCol, salePriceCol, quantityCol, totalAmountCol, profitCol);
    }
}
