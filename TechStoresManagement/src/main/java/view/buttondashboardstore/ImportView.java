package view.buttondashboardstore;

import controller.ImportInvoiceController;
import controller.Session;
import entity.ImportInvoice;
import entity.ProductInvoice;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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
import model.ImportInvoiceModel;

import java.time.format.DateTimeFormatter;

public class ImportView extends VBox {
    private final int idStore = Session.getIdStore();

    private TableView<ImportInvoice> importInvoiceTable;
    private TextField searchField;
    private ImportInvoiceController controller = new ImportInvoiceController();
    private int currentPage = 1;
    private final int itemsPerPage = 12;
    private int totalPages;
    private final Label pageLabel = new Label();

    public ImportView() {
        BorderPane mainLayout = new BorderPane();
        // Title
        Label titleLabel = new Label("Import Invoice");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-padding: 0;");

        HBox searchBox = new HBox();
        searchField = new TextField();
        searchField.setPromptText("Search");
        searchField.getStyleClass().add("search-box");
        searchField.setOnKeyReleased(e -> {
            currentPage = 1;
            loadImportInvoicesWithPagination();
        });

        searchBox.getChildren().addAll(searchField);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.setStyle("-fx-padding: 0 0 10 10;");

        Button addButton = new Button("Create Import Invoice");
        addButton.getStyleClass().add("button-pagination");
        addButton.setOnAction(e -> openCreateImportInvoiceDialog());

        HBox topControls = new HBox(10);
        topControls.setStyle("-fx-min-width: 1000");
        topControls.getChildren().addAll(searchBox, addButton);

        importInvoiceTable = new TableView<>();
        configureImportInvoiceTable();

        mainLayout.setCenter(importInvoiceTable);

        // Load all receipts initially
        controller.loadImportInvoiceStore(importInvoiceTable, null, 1, idStore);

        // Load all receipts initially
        // Pagination controls
        Button prevButton = new Button("<-");
        prevButton.getStyleClass().add("button-pagination");
        Button nextButton = new Button("->");
        nextButton.getStyleClass().add("button-pagination");
        pageLabel.getStyleClass().add("text-pagination");

        totalPages = controller.getTotalPagesByStore(searchField.getText(), idStore); // Lấy tổng số trang

        prevButton.setOnAction(e -> {
            if (currentPage > 1) {
                currentPage--;
                loadImportInvoicesWithPagination();
            }
        });

        nextButton.setOnAction(e -> {
            if (currentPage < totalPages) {
                currentPage++;
                loadImportInvoicesWithPagination();
            }
        });

        pageLabel.setText("Page " + currentPage + " / " + totalPages);
        HBox paginationBox = new HBox(10, prevButton, pageLabel, nextButton);
        paginationBox.setAlignment(Pos.CENTER);
        paginationBox.setStyle("-fx-padding: 10;");

        this.getChildren().addAll(titleLabel, topControls, mainLayout, paginationBox);
        this.getStyleClass().add("vbox");
    }

    private void loadImportInvoicesWithPagination() {
        controller.loadImportInvoiceStore(importInvoiceTable, searchField.getText(), currentPage, idStore);
        totalPages = controller.getTotalPagesByStore(searchField.getText(), idStore);
        pageLabel.setText("Page " + currentPage + " / " + totalPages);
    }


    private void configureImportInvoiceTable() {
        TableColumn<ImportInvoice, Integer> sttCol = new TableColumn<>("ID");
        sttCol.setPrefWidth(95); // Chiều rộng cột ID
        sttCol.setStyle("-fx-alignment: center");
        sttCol.setCellValueFactory(cellData -> new SimpleIntegerProperty(importInvoiceTable.getItems().indexOf(cellData.getValue()) + 1).asObject());
        sttCol.getStyleClass().add("column");

        TableColumn<ImportInvoice, String> invoiceName = new TableColumn<>("Invoice Name");
        invoiceName.setPrefWidth(250); // Chiều rộng cột Customer Name
        invoiceName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getInvoiceName()));
        invoiceName.getStyleClass().add("column");

        TableColumn<ImportInvoice, String> warehouseName = new TableColumn<>("Warehouse Name");
        warehouseName.setPrefWidth(200); // Chiều rộng cột Customer Name
        warehouseName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getWarehouseName()));
        warehouseName.getStyleClass().add("column");

        TableColumn<ImportInvoice, Double> totalCol = new TableColumn<>("Total Invoice");
        totalCol.setPrefWidth(200); // Chiều rộng cột Total Bill
        totalCol.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getTotal()).asObject());
        totalCol.getStyleClass().add("column");

        TableColumn<ImportInvoice, String> statusCol = new TableColumn<>("Status");
        statusCol.setPrefWidth(200); // Chiều rộng cột Customer Name
        statusCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus()));
        statusCol.getStyleClass().add("column");

        TableColumn<ImportInvoice, Void> acctionCol = new TableColumn<>("Action");
        acctionCol.setPrefWidth(100); // Chiều rộng cột View
        acctionCol.getStyleClass().add("column");
        acctionCol.setCellFactory(col -> new TableCell<ImportInvoice, Void>() {
            private final Button viewButton = new Button();
            private final Button receivedButton = new Button();
            private final Button cancelButton = new Button();

            {
                // Tạo ImageView cho các icon
                ImageView viewIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/view.png")));
                ImageView receivedIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/import.png")));
                ImageView cancelIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/cancel.png")));

                // Đặt kích thước ban đầu cho icon
                setIconSize(viewIcon, 20);
                setIconSize(receivedIcon, 20);
                setIconSize(cancelIcon, 20);

                // Thêm icon vào nút
                viewButton.setGraphic(viewIcon);
                receivedButton.setGraphic(receivedIcon);
                cancelButton.setGraphic(cancelIcon);

                // Đặt style cho nút
                String defaultStyle = "-fx-background-color: transparent; -fx-border-color: transparent; -fx-padding: 6;";
                viewButton.setStyle(defaultStyle);
                receivedButton.setStyle(defaultStyle);
                cancelButton.setStyle(defaultStyle);

                // Thêm sự kiện phóng to khi hover và giảm padding
                addHoverEffect(viewButton, viewIcon);
                addHoverEffect(receivedButton, receivedIcon);
                addHoverEffect(cancelButton, cancelIcon);
            }


            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    ImportInvoice invoice = getTableView().getItems().get(getIndex());
                    if ("Requested".equals(invoice.getStatus())) {
                        // Hiển thị tất cả các nút
                        HBox buttons = new HBox(viewButton, cancelButton);
                        buttons.setStyle("-fx-alignment: CENTER_LEFT; -fx-spacing: 10;");
                        setGraphic(buttons);

                        cancelButton.setOnAction(e -> {
                            // Logic hủy hóa đơn
                            cancelInvoice(invoice);
                        });
                    } else if ("Processing".equals(invoice.getStatus())) {
                        // Hiển thị nút "View" và "Received"
                        HBox buttons = new HBox(viewButton, receivedButton);
                        buttons.setStyle("-fx-alignment: CENTER_LEFT; -fx-spacing: 10;");
                        setGraphic(buttons);

                        receivedButton.setOnAction(e -> {
                            // Logic nhận hóa đơn
                            receivedInvoice(invoice);
                        });
                    } else {
                        // Chỉ hiển thị nút "View" khi không phải "Requested" hoặc "Processing"
                        HBox buttons = new HBox(viewButton);
                        buttons.setStyle("-fx-alignment: CENTER_LEFT; -fx-spacing: 10;");
                        setGraphic(buttons);
                    }

                    viewButton.setOnAction(e -> {
                        showImportInvoiceDetails(invoice);
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

            private void receivedInvoice(ImportInvoice invoice) {
                String status = "Imported";
                if (controller.updateImportStatus(invoice.getInvoiceName(), status) && controller.updateProductQuantity(invoice.getInvoiceName())) {
                    invoice.setStatus(status);
                    // Gọi hàm cập nhật cơ sở dữ liệu hoặc thực hiện hành đ��ng phù h��p
                    System.out.println("Hóa đơn đã được nhận: " + invoice.getInvoiceName());
                    loadImportInvoicesWithPagination();
                } else {
                    System.out.println("Không thể nhận hóa đơn: " + invoice.getInvoiceName());
                }

            }

            // Phương thức xử lý khi hủy hóa đơn
            private void cancelInvoice(ImportInvoice invoice) {
                String status = "Cancelled";
                if (controller.updateImportStatus(invoice.getInvoiceName(), status)) {
                    invoice.setStatus(status);
                    System.out.println("Hóa đơn đã bị hủy: " + invoice.getInvoiceName());
                    loadImportInvoicesWithPagination();
                } else {
                    System.out.println("Không thể hủy hóa đơn: " + invoice.getInvoiceName());
                }
            }
        });

        importInvoiceTable.getColumns().addAll(sttCol, invoiceName, warehouseName, totalCol, statusCol, acctionCol);
        importInvoiceTable.getStyleClass().add("table-view");
    }

    private void showImportInvoiceDetails(ImportInvoice invoice) {
        Stage detailStage = new Stage();
        detailStage.setTitle("Bill Detail");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setVgap(10);
        grid.setHgap(10);

        // Create labels and get data for the receipt details
        Label importInvoiceNameLabel = new Label("Import Invoice Name:");
        Label importInvoiceNameData = new Label(invoice.getInvoiceName());

        Label storeNameLabel = new Label("Store Name:");
        Label storeNameData = new Label(invoice.getStoreName());

        Label storeManagerLabel = new Label("Store Manager:");
        Label storeManagerData = new Label(controller.getStoreManager(invoice.getStoreName()));

        Label storeAddressLabel = new Label("Store Address:");
        Label storeAddressData = new Label(invoice.getStoreAddress());

        Label warehouseNameLabel = new Label("Warehouse Name:");
        Label warehouseNameData = new Label(invoice.getWarehouseName());

        Label warehouseManagerLabel = new Label("Warehouse Manager:");
        Label warehouseManagerData = new Label(controller.getWarehouseManager(invoice.getWarehouseName()));

        Label warehouseAddressLabel = new Label("Warehouse Address:");
        Label warehouseAddressData = new Label(invoice.getWarehouseAddress());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedCreated = invoice.getCreatedDate().format(formatter);
        Label createdLabel = new Label("Created at:");
        Label createdData = new Label(formattedCreated);

        String formattedRequestedDate = invoice.getRequestedDate().format(formatter);
        Label requestedDateLabel = new Label("Requested Date:");
        Label requestedDateData = new Label(formattedRequestedDate);

// Check if actual import date is null before formatting
        String formattedActualImportDate = (invoice.getActualImportDate() != null)
                ? invoice.getActualImportDate().format(formatter)
                : "Date not available";
// Create labels as before
        Label actualImportDateLabel = new Label("Actual Import Date:");
        Label actualImportDateData = new Label(formattedActualImportDate);


        Label totalLabel = new Label("Total bill:");
        Label totalData = new Label(String.valueOf(invoice.getTotal()));

        // Add CSS classes to labels and data
        importInvoiceNameLabel.getStyleClass().add("label-popup");
        importInvoiceNameData.getStyleClass().add("data-popup");

        storeNameLabel.getStyleClass().add("label-popup");
        storeNameData.getStyleClass().add("data-popup");

        storeManagerLabel.getStyleClass().add("label-popup");
        storeManagerData.getStyleClass().add("data-popup");

        storeAddressLabel.getStyleClass().add("label-popup");
        storeAddressData.getStyleClass().add("data-popup");

        warehouseNameLabel.getStyleClass().add("label-popup");
        warehouseNameData.getStyleClass().add("data-popup");

        warehouseManagerLabel.getStyleClass().add("label-popup");
        warehouseManagerData.getStyleClass().add("data-popup");

        warehouseAddressLabel.getStyleClass().add("label-popup");
        warehouseAddressData.getStyleClass().add("data-popup");

        createdLabel.getStyleClass().add("label-popup");
        createdData.getStyleClass().add("data-popup");

        requestedDateLabel.getStyleClass().add("label-popup");
        requestedDateData.getStyleClass().add("data-popup");

        actualImportDateLabel.getStyleClass().add("label-popup");
        actualImportDateData.getStyleClass().add("data-popup");

        totalLabel.getStyleClass().add("label-popup");
        totalData.getStyleClass().add("data-popup");

        // Add labels and data to the grid
        grid.add(importInvoiceNameLabel, 0, 0);
        grid.add(importInvoiceNameData, 1, 0);

        grid.add(storeNameLabel, 0, 1);
        grid.add(storeNameData, 1, 1);

        grid.add(storeManagerLabel, 0, 2);
        grid.add(storeManagerData, 1, 2);

        grid.add(storeAddressLabel, 0, 3);
        grid.add(storeAddressData, 1, 3);

        grid.add(warehouseNameLabel, 0, 4);
        grid.add(warehouseNameData, 1, 4);

        grid.add(warehouseManagerLabel, 0, 5);
        grid.add(warehouseManagerData, 1, 5);

        grid.add(warehouseAddressLabel, 0, 6);
        grid.add(warehouseAddressData, 1, 6);

        grid.add(createdLabel, 0, 7);
        grid.add(createdData, 1, 7);

        grid.add(requestedDateLabel, 0, 8);
        grid.add(requestedDateData, 1, 8);

        grid.add(actualImportDateLabel, 0, 9);
        grid.add(actualImportDateData, 1, 9);

        grid.add(totalLabel, 0, 10);
        grid.add(totalData, 1, 10);

        // Create a table for products in the receipt
        TableView<ProductInvoice> productTable = new TableView<>();
        configureProductTable(productTable, invoice.getId());

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


    public void configureProductTable(TableView<ProductInvoice> productTable, int idInvoice) {

        ImportInvoiceModel model = new ImportInvoiceModel();
        ObservableList<ProductInvoice> products = model.getProductInvoice(idInvoice);
        productTable.setItems(products);

        // Configure columns for the product table

        TableColumn<ProductInvoice, Integer> productSttCol = new TableColumn<>("ID");
        productSttCol.setPrefWidth(50); // Chiều rộng cột ID
        productSttCol.getStyleClass().add("column");
        productSttCol.setCellValueFactory(cellData -> cellData.getValue().idProductProperty().asObject());

        TableColumn<ProductInvoice, String> productNameCol = new TableColumn<>("Product Name");
        productNameCol.setPrefWidth(150); // Chiều rộng cột Product Name
        productNameCol.getStyleClass().add("column");
        productNameCol.setCellValueFactory(cellData -> cellData.getValue().productNameProperty());

        TableColumn<ProductInvoice, String> brandCol = new TableColumn<>("Brand");
        brandCol.setPrefWidth(100); // Chiều rộng cột Brand
        brandCol.getStyleClass().add("column");
        brandCol.setCellValueFactory(cellData -> cellData.getValue().brandProperty());

        TableColumn<ProductInvoice, Double> purchasePriceCol = new TableColumn<>("Purchase price");
        purchasePriceCol.setPrefWidth(150); // Chiều rộng cột Purchase Price
        purchasePriceCol.getStyleClass().add("column");
        purchasePriceCol.setCellValueFactory(cellData -> cellData.getValue().purchasePriceProperty().asObject());

        TableColumn<ProductInvoice, Double> salePriceCol = new TableColumn<>("Sale price");
        salePriceCol.setPrefWidth(100); // Chiều rộng cột Sale Price
        salePriceCol.getStyleClass().add("column");
        salePriceCol.setCellValueFactory(cellData -> cellData.getValue().salePriceProperty().asObject());

        TableColumn<ProductInvoice, Integer> quantityCol = new TableColumn<>("Quantity");
        quantityCol.setPrefWidth(120); // Chiều rộng cột Quantity
        quantityCol.getStyleClass().add("column");
        quantityCol.setCellValueFactory(cellData -> cellData.getValue().quantityProperty().asObject());

        TableColumn<ProductInvoice, Double> totalAmountCol = new TableColumn<>("Total amount");
        totalAmountCol.setPrefWidth(125); // Chiều rộng cột Total Amount
        totalAmountCol.getStyleClass().add("column");
        totalAmountCol.setCellValueFactory(cellData -> cellData.getValue().totalAmountProperty().asObject());

        productTable.getColumns().addAll(productSttCol, productNameCol, brandCol, purchasePriceCol, salePriceCol, quantityCol, totalAmountCol);
        productTable.setStyle("-fx-pref-height: 380; -fx-pref-width :800");
    }

    private void openCreateImportInvoiceDialog () {

    }
}