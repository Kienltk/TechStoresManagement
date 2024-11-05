package view.buttondashboard;

import controller.HistoryController;
import entity.ProductReceipt;
import entity.Receipt;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
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
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.setStyle("-fx-padding: 0 0 10 10;");

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

        TableColumn<Receipt, String> storeNameCol = new TableColumn<>("StoreManager name");
        storeNameCol.setPrefWidth(200); // Chiều rộng cột StoreManager Name
        storeNameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStoreName()));
        storeNameCol.getStyleClass().add("column");

        TableColumn<Receipt, String> purchaseDateCol = new TableColumn<>("Order time");
        purchaseDateCol.setPrefWidth(195); // Chiều rộng cột Order Time
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
        viewCol.getStyleClass().add("column");
        viewCol.setCellFactory(col -> new TableCell<Receipt, Void>() {
            private final Button viewButton = new Button();

            {
                // Tạo ImageView cho các icon
                ImageView viewIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/view.png")));


                // Đặt kích thước ban đầu cho icon
                setIconSize(viewIcon, 20);


                // Thêm icon vào nút
                viewButton.setGraphic(viewIcon);


                // Đặt style cho nút
                String defaultStyle = "-fx-background-color: transparent; -fx-border-color: transparent; -fx-padding: 6;";
                viewButton.setStyle(defaultStyle);

                // Thêm sự kiện phóng to khi hover và giảm padding
                addHoverEffect(viewButton, viewIcon);
            }


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
            private void setIconSize(ImageView icon, int size) {
                icon.setFitWidth(size);
                icon.setFitHeight(size);
            }

            // Phương thức thêm hiệu ứng hover
            private void addHoverEffect(Button button, ImageView icon) {
                button.setOnMouseEntered(e -> {
                    setIconSize(icon, 25); // Phóng to khi hover
                    button.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-padding: 3.2;"); // Giảm padding khi hover
                });

                button.setOnMouseExited(e -> {
                    setIconSize(icon, 20); // Trở lại kích thước ban đầu khi rời chuột
                    button.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-padding: 6;"); // Khôi phục padding ban đầu
                });
            }
        });

        receiptTable.getColumns().addAll(sttCol, customerNameCol, storeNameCol, purchaseDateCol, totalCol, profitCol, viewCol);
        receiptTable.getStyleClass().add("table-view");
    }

    private void showReceiptDetails(Receipt receipt) {
        Stage detailStage = new Stage();
        detailStage.setTitle("Bill Detail");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setVgap(10);
        grid.setHgap(10);

        // Create labels and get data for the receipt details
        Label customerLabel = new Label("Customer name:");
        Label customerData = new Label(receipt.getCustomerName());

        Label phoneLabel = new Label("Phone number:");
        Label phoneData = new Label(controller.getCustomerPhone(receipt.getCustomerName()));

        Label storeLabel = new Label("StoreManager name:");
        Label storeData = new Label(receipt.getStoreName());

        Label managerLabel = new Label("StoreManager Manager:");
        Label managerData = new Label(controller.getStoreManager(receipt.getStoreName()));

        Label cashierLabel = new Label("Cashier:");
        Label cashierData = new Label(controller.getCashier(receipt.getId()));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedPurchaseDate = receipt.getPurchaseDate().format(formatter);

        Label purchaseDateLabel = new Label("Order time:");
        Label purchaseDateData = new Label(formattedPurchaseDate);

        Label totalLabel = new Label("Total bill:");
        Label totalData = new Label(String.valueOf(receipt.getTotal()));

        Label profitLabel = new Label("Profit bill:");
        Label profitData = new Label(String.valueOf(receipt.getProfit()));

        // Add CSS classes to labels and data
        customerLabel.getStyleClass().add("label-popup");
        customerData.getStyleClass().add("data-popup");

        phoneLabel.getStyleClass().add("label-popup");
        phoneData.getStyleClass().add("data-popup");

        storeLabel.getStyleClass().add("label-popup");
        storeData.getStyleClass().add("data-popup");

        managerLabel.getStyleClass().add("label-popup");
        managerData.getStyleClass().add("data-popup");

        cashierLabel.getStyleClass().add("label-popup");
        cashierData.getStyleClass().add("data-popup");

        purchaseDateLabel.getStyleClass().add("label-popup");
        purchaseDateData.getStyleClass().add("data-popup");

        totalLabel.getStyleClass().add("label-popup");
        totalData.getStyleClass().add("data-popup");

        profitLabel.getStyleClass().add("label-popup");
        profitData.getStyleClass().add("data-popup");

        // Add labels and data to the grid
        grid.add(customerLabel, 0, 0);
        grid.add(customerData, 1, 0);

        grid.add(phoneLabel, 0, 1);
        grid.add(phoneData, 1, 1);

        grid.add(storeLabel, 0, 2);
        grid.add(storeData, 1, 2);

        grid.add(managerLabel, 0, 3);
        grid.add(managerData, 1, 3);

        grid.add(cashierLabel, 0, 4);
        grid.add(cashierData, 1, 4);

        grid.add(purchaseDateLabel, 0, 5);
        grid.add(purchaseDateData, 1, 5);

        grid.add(totalLabel, 0, 6);
        grid.add(totalData, 1, 6);

        grid.add(profitLabel, 0, 7);
        grid.add(profitData, 1, 7);

        // Create a table for products in the receipt
        TableView<ProductReceipt> productTable = new TableView<>();
        configureProductTable(productTable, receipt.getId());

        // Close button
        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> detailStage.close());
        closeButton.getStyleClass().add("button-pagination");

        // Add grid and table to the VBox
        VBox detailLayout = new VBox(10, grid, productTable, closeButton);
        detailLayout.getStyleClass().add("vbox");

        Scene detailScene = new Scene(detailLayout, 830, 600);
        detailScene.getStylesheets().add(getClass().getResource("/view/director.css").toExternalForm());
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
        productSttCol.setStyle("-fx-alignment: center");
        productSttCol.getStyleClass().add("column");
        productSttCol.setCellValueFactory(cellData -> cellData.getValue().idProductProperty().asObject());

        TableColumn<ProductReceipt, String> productNameCol = new TableColumn<>("Product Name");
        productNameCol.setPrefWidth(150); // Chiều rộng cột Product Name
        productNameCol.getStyleClass().add("column");
        productNameCol.setCellValueFactory(cellData -> cellData.getValue().productNameProperty());

        TableColumn<ProductReceipt, String> brandCol = new TableColumn<>("Brand");
        brandCol.setPrefWidth(100); // Chiều rộng cột Brand
        brandCol.getStyleClass().add("column");
        brandCol.setCellValueFactory(cellData -> cellData.getValue().brandProperty());

        TableColumn<ProductReceipt, Double> purchasePriceCol = new TableColumn<>("Purchase price");
        purchasePriceCol.setPrefWidth(100); // Chiều rộng cột Purchase Price
        purchasePriceCol.getStyleClass().add("column");
        purchasePriceCol.setCellValueFactory(cellData -> cellData.getValue().purchasePriceProperty().asObject());

        TableColumn<ProductReceipt, Double> salePriceCol = new TableColumn<>("Sale price");
        salePriceCol.setPrefWidth(100); // Chiều rộng cột Sale Price
        salePriceCol.getStyleClass().add("column");
        salePriceCol.setCellValueFactory(cellData -> cellData.getValue().salePriceProperty().asObject());

        TableColumn<ProductReceipt, Integer> quantityCol = new TableColumn<>("Quantity");
        quantityCol.setPrefWidth(80); // Chiều rộng cột Quantity
        quantityCol.getStyleClass().add("column");
        quantityCol.setCellValueFactory(cellData -> cellData.getValue().quantityProperty().asObject());

        TableColumn<ProductReceipt, Double> totalAmountCol = new TableColumn<>("Total amount");
        totalAmountCol.setPrefWidth(120); // Chiều rộng cột Total Amount
        totalAmountCol.getStyleClass().add("column");
        totalAmountCol.setCellValueFactory(cellData -> cellData.getValue().totalAmountProperty().asObject());

        TableColumn<ProductReceipt, Double> profitCol = new TableColumn<>("Profit");
        profitCol.setPrefWidth(100); // Chiều rộng cột Profit
        profitCol.getStyleClass().add("column");
        profitCol.setCellValueFactory(cellData -> cellData.getValue().profitProperty().asObject());

        productTable.getColumns().addAll(productSttCol, productNameCol, brandCol, purchasePriceCol, salePriceCol, quantityCol, totalAmountCol, profitCol);
        productTable.setStyle("-fx-pref-height: 380; -fx-pref-width :580");
    }
}
