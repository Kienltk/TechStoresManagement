package view.buttondashboard;

import controller.HistoryController;
import entity.ProductReceipt;
import entity.Receipt;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.HistoryModel;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

public class HistoryView extends VBox {
    private TableView<Receipt> receiptTable;
    private TextField searchField;
    private HistoryController controller = new HistoryController();
    private int currentPage = 1;
    private final int itemsPerPage = 12;
    private int totalPages;
    private final Label pageLabel = new Label();

    public HistoryView() {
        BorderPane mainLayout = new BorderPane();
        // Title
        Label titleLabel = new Label("History");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-padding: 0;");

        HBox searchBox = new HBox();
        searchField = new TextField();
        searchField.setPromptText("Search");
        searchField.getStyleClass().add("search-box");
        searchField.setOnKeyReleased(e -> {
            currentPage = 1;
            loadReceiptsWithPagination();
        });

        searchBox.getChildren().addAll(searchField);
        searchBox.setAlignment(Pos.CENTER_RIGHT);
        searchBox.setStyle("-fx-padding: 0 10 10 10;");

        receiptTable = new TableView<>();
        configureReceiptTable();

        mainLayout.setCenter(receiptTable);

        // Load all receipts initially
        controller.loadReceipts(receiptTable, null, 1);

        // Load all receipts initially
        // Pagination controls
        Button prevButton = new Button("<-");
        prevButton.getStyleClass().add("button-pagination");
        Button nextButton = new Button("->");
        nextButton.getStyleClass().add("button-pagination");
        pageLabel.getStyleClass().add("text-pagination");

        totalPages = controller.getTotalPages(searchField.getText()); // Lấy tổng số trang

        prevButton.setOnAction(e -> {
            if (currentPage > 1) {
                currentPage--;
                loadReceiptsWithPagination();
            }
        });

        nextButton.setOnAction(e -> {
            if (currentPage < totalPages) {
                currentPage++;
                loadReceiptsWithPagination();
            }
        });

        pageLabel.setText("Page " + currentPage + " / " + totalPages);
        HBox paginationBox = new HBox(10, prevButton, pageLabel, nextButton);
        paginationBox.setAlignment(Pos.CENTER);
        paginationBox.setStyle("-fx-padding: 10;");

        this.getChildren().addAll(titleLabel, searchBox, mainLayout, paginationBox);
        this.getStyleClass().add("vbox");
    }
    // Hàm load receipts có phân trang
    private void loadReceiptsWithPagination() {
        controller.loadReceipts(receiptTable, searchField.getText(), currentPage);
        totalPages = controller.getTotalPages(searchField.getText());
        pageLabel.setText("Page " + currentPage + " / " + totalPages);
    }


    private void configureReceiptTable() {
        TableColumn<Receipt, Integer> sttCol = new TableColumn<>("ID");
        sttCol.setPrefWidth(50); // Chiều rộng cột ID
        sttCol.setStyle("-fx-alignment: center");
        sttCol.setCellValueFactory(cellData -> new SimpleIntegerProperty(receiptTable.getItems().indexOf(cellData.getValue()) + 1).asObject());
        sttCol.getStyleClass().add("column");

        TableColumn<Receipt, String> customerNameCol = new TableColumn<>("Customer Name");
        customerNameCol.setPrefWidth(200); // Chiều rộng cột Customer Name
        customerNameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCustomerName()));
        customerNameCol.getStyleClass().add("column");

        TableColumn<Receipt, String> storeNameCol = new TableColumn<>("Store name");
        storeNameCol.setPrefWidth(200); // Chiều rộng cột Store Name
        storeNameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStoreName()));
        storeNameCol.getStyleClass().add("column");

        TableColumn<Receipt, String> purchaseDateCol = new TableColumn<>("Order time");
        purchaseDateCol.setPrefWidth(200); // Chiều rộng cột Order Time
        purchaseDateCol.setCellValueFactory(cellData -> {
            LocalDateTime purchaseDate = cellData.getValue().getPurchaseDate();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return new SimpleStringProperty(purchaseDate.format(formatter));
        });
        purchaseDateCol.getStyleClass().add("column");

        TableColumn<Receipt, Double> totalCol = new TableColumn<>("Total bill");
        totalCol.setPrefWidth(150); // Chiều rộng cột Total Bill
        totalCol.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getTotal()).asObject());
        totalCol.getStyleClass().add("column");

        TableColumn<Receipt, Double> profitCol = new TableColumn<>("Profit bill");
        profitCol.setPrefWidth(150); // Chiều rộng cột Profit Bill
        profitCol.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getProfit()).asObject());
        profitCol.getStyleClass().add("column");

        TableColumn<Receipt, Void> viewCol = new TableColumn<>("View");
        viewCol.setPrefWidth(100); // Chiều rộng cột View
        viewCol.setStyle("-fx-alignment: center");
        viewCol.setCellFactory(col -> new TableCell<Receipt, Void>() {
            private final Button viewButton = new Button("View");


            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(viewButton);
                    viewButton.setStyle("-fx-background-color: #4AD4DD; -fx-text-fill: white;");
                    viewButton.setOnAction(e -> {
                        Receipt receipt = getTableView().getItems().get(getIndex());
                        showReceiptDetails(receipt);
                    });
                }
            }
        });

        receiptTable.getColumns().addAll(sttCol, customerNameCol, storeNameCol, purchaseDateCol, totalCol, profitCol, viewCol);
        receiptTable.getStyleClass().add("table-view");
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
        productSttCol.setPrefWidth(50); // Chiều rộng cột ID
        productSttCol.setCellValueFactory(cellData -> cellData.getValue().idProductProperty().asObject());

        TableColumn<ProductReceipt, String> productNameCol = new TableColumn<>("Product Name");
        productNameCol.setPrefWidth(150); // Chiều rộng cột Product Name
        productNameCol.setCellValueFactory(cellData -> cellData.getValue().productNameProperty());

        TableColumn<ProductReceipt, String> brandCol = new TableColumn<>("Brand");
        brandCol.setPrefWidth(100); // Chiều rộng cột Brand
        brandCol.setCellValueFactory(cellData -> cellData.getValue().brandProperty());

        TableColumn<ProductReceipt, Double> purchasePriceCol = new TableColumn<>("Purchase price");
        purchasePriceCol.setPrefWidth(100); // Chiều rộng cột Purchase Price
        purchasePriceCol.setCellValueFactory(cellData -> cellData.getValue().purchasePriceProperty().asObject());

        TableColumn<ProductReceipt, Double> salePriceCol = new TableColumn<>("Sale price");
        salePriceCol.setPrefWidth(100); // Chiều rộng cột Sale Price
        salePriceCol.setCellValueFactory(cellData -> cellData.getValue().salePriceProperty().asObject());

        TableColumn<ProductReceipt, Integer> quantityCol = new TableColumn<>("Quantity");
        quantityCol.setPrefWidth(80); // Chiều rộng cột Quantity
        quantityCol.setCellValueFactory(cellData -> cellData.getValue().quantityProperty().asObject());

        TableColumn<ProductReceipt, Double> totalAmountCol = new TableColumn<>("Total amount");
        totalAmountCol.setPrefWidth(120); // Chiều rộng cột Total Amount
        totalAmountCol.setCellValueFactory(cellData -> cellData.getValue().totalAmountProperty().asObject());

        TableColumn<ProductReceipt, Double> profitCol = new TableColumn<>("Profit");
        profitCol.setPrefWidth(100); // Chiều rộng cột Profit
        profitCol.setCellValueFactory(cellData -> cellData.getValue().profitProperty().asObject());

        productTable.getColumns().addAll(productSttCol, productNameCol, brandCol, purchasePriceCol, salePriceCol, quantityCol, totalAmountCol, profitCol);
        productTable.getStyleClass().add("table-view");
    }
}
