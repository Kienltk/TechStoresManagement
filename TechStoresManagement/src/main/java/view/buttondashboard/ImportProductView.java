package view.buttondashboard;
import controller.ImportController;
import controller.StoreController;
import entity.Import;
import entity.Product;
import entity.Store;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.ImportManagementModel;
import model.StoreModel;
import view.stage.AdditionSuccess;
import view.stage.DeletionFailed;
import view.stage.DeletionSuccess;
import view.stage.EditSuccess;

import java.time.LocalDateTime;
import java.util.List;

public class ImportProductView extends VBox {
    private TableView<Import> tableView;
    private ImportController importController;
    private TextField searchField;
    private int currentPage = 1;
    private final int itemsPerPage = 12;
    private int totalPages;
    private String currentFilter = null;
    private final Label pageLabel = new Label();
    private Button filterWarehouseButton;
    private Button filterStoreButton;
    public ImportProductView() {
        importController = new ImportController();
        ImportManagementModel model = new ImportManagementModel();

        // Title
        Label titleLabel = new Label("Import Management");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        // Create layout
        BorderPane borderPane = new BorderPane();
        VBox topLayout = new VBox(10);
        topLayout.setPadding(new Insets(10));

        // Search field and button layout
        searchField = new TextField();
        searchField.setPromptText("Search by name or address...");
        searchField.getStyleClass().add("search-box");
        searchField.textProperty().addListener((observable, oldValue, newValue) -> loadStores(null));

        HBox searchBar = new HBox(searchField);
        searchBar.setAlignment(Pos.CENTER_RIGHT);
        searchBar.setStyle(" -fx-padding:0 10 10 595;");

        // Filter buttons
        filterWarehouseButton = new Button("Warehouse");
        filterWarehouseButton.setOnAction(e -> {
            loadStores("Warehouse");
            highlightButton(filterWarehouseButton);
        });
        filterWarehouseButton.getStyleClass().add("button-pagination");

        filterStoreButton = new Button("StoreManager");
        filterStoreButton.setOnAction(e -> {
            loadStores("StoreManager");
            highlightButton(filterStoreButton);
        });
        filterStoreButton.getStyleClass().add("button-pagination");

        // Create warehouse button
        Button createWarehouseButton = new Button("Create Invoice");
        createWarehouseButton.setOnAction(e -> showCreateInvoice());
        createWarehouseButton.getStyleClass().add("button-pagination");
        createWarehouseButton.setAlignment(Pos.CENTER_LEFT);

        HBox topControls = new HBox(10);
        topControls.setStyle("-fx-min-width: 1000");
        topControls.getChildren().addAll(createWarehouseButton, filterWarehouseButton, filterStoreButton, searchBar);
        borderPane.setTop(topControls);

        // Table setup
        tableView = new TableView<>();
        setupTableView();
        loadStores(null);

        borderPane.setCenter(tableView);

        Button prevButton = new Button("<-");
        prevButton.getStyleClass().add("button-pagination");
        Button nextButton = new Button("->");
        nextButton.getStyleClass().add("button-pagination");
        pageLabel.getStyleClass().add("text-pagination");

        prevButton.setOnAction(e -> {
            if (currentPage > 1) {
                currentPage--;
                loadStores(null);
            }
        });

        nextButton.setOnAction(e -> {
            if (currentPage < totalPages) {
                currentPage++;
                loadStores(null);
            }
        });

        HBox paginationBox = new HBox(10, prevButton, pageLabel, nextButton);
        paginationBox.setAlignment(Pos.CENTER);
        paginationBox.setSpacing(30);
        paginationBox.setStyle("-fx-padding: 8");

        this.getChildren().addAll(titleLabel, borderPane, paginationBox);
        this.getStyleClass().add("vbox");
    }
    private void highlightButton(Button selectedButton) {
        // Xóa highlight khỏi tất cả nút
        filterWarehouseButton.getStyleClass().remove("button-selected");
        filterStoreButton.getStyleClass().remove("button-selected");

        selectedButton.getStyleClass().add("button-selected");
    }
    private void setupTableView() {
        TableColumn<Import, Integer> indexColumn = new TableColumn<>("STT");
        indexColumn.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(tableView.getItems().indexOf(cellData.getValue()) + 1).asObject());
        indexColumn.setPrefWidth(40);
        indexColumn.setStyle("-fx-alignment: center");
        indexColumn.getStyleClass().add("column");

        TableColumn<Import, String> nameColumn = new TableColumn<>("Import name");
        nameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getImportName()));
        nameColumn.setPrefWidth(150);
        nameColumn.getStyleClass().add("column");
        TableColumn<Import, String> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getStatus()));
        statusColumn.setPrefWidth(150);
        statusColumn.getStyleClass().add("column");
        TableColumn<Import, String> addressColumn = new TableColumn<>("Warehouse Name");
        addressColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getWarehouseName()));
        addressColumn.setPrefWidth(200);
        addressColumn.getStyleClass().add("column");

        TableColumn<Import, String> managerColumn = new TableColumn<>("Total");
        managerColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.valueOf(cellData.getValue().getTotal())));
        managerColumn.setPrefWidth(100);
        managerColumn.getStyleClass().add("column");

        TableColumn<Import, LocalDateTime> receivedDate = new TableColumn<>("Received Date");
        receivedDate.setCellValueFactory(cellData -> {
            LocalDateTime date = cellData.getValue().getDate();
            return new SimpleObjectProperty<>(date);
        });
        receivedDate.getStyleClass().add("column");
        receivedDate.setPrefWidth(150);

        TableColumn<Import, String> actionColumn = new TableColumn<>("Action");
        actionColumn.setPrefWidth(190);
        actionColumn.setCellFactory(col -> new TableCell<Import, String>() {
            private final Button viewButton = new Button("View");

            {
                viewButton.setStyle("-fx-background-color: #4AD4DD; -fx-text-fill: white;");
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() < 0) {
                    setGraphic(null);
                } else {
                    Import storeImport = getTableView().getItems().get(getIndex());
                    viewButton.setOnAction(e -> showStoreDetails(storeImport));
                    HBox buttons = new HBox(viewButton);
                    buttons.setSpacing(10);
                    setGraphic(buttons);
                }
            }
        });
        tableView.getColumns().addAll(indexColumn, nameColumn, addressColumn, receivedDate, managerColumn,statusColumn, actionColumn);
    }
    private void setupProductTableColumns(TableView<Product> productTable) {
        TableColumn<Product, String> nameColumn = new TableColumn<>("Product Name");
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        nameColumn.setPrefWidth(200);
        nameColumn.getStyleClass().add("column");

        TableColumn<Product, String> brandColumn = new TableColumn<>("Brand");
        brandColumn.setCellValueFactory(cellData -> cellData.getValue().brandProperty());
        brandColumn.setPrefWidth(130);
        brandColumn.getStyleClass().add("column");
//        TableColumn<Product,Double> purcharsePriceColumn = new TableColumn<>("Purchase Price");
//        purcharsePriceColumn.setCellValueFactory(cellData -> cellData.getValue().purchasePriceProperty());
//        purcharsePriceColumn.setPrefWidth(90);
//        purcharsePriceColumn.getStyleClass().add("column");

//        TableColumn<Product, Double> soldColumn = new TableColumn<>("Purchase Price");
//        soldColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getPurchasePrice()).asObject());
//        soldColumn.setPrefWidth(90);
//        soldColumn.getStyleClass().add("column");




        productTable.getColumns().addAll(nameColumn, brandColumn);
        productTable.setStyle("-fx-pref-height: 380; -fx-pref-width :580");
    }

    private void loadStores(String filterType) {
        if (filterType != null) {
            currentFilter = filterType;
            currentPage = 1;
        }

        // Xóa cột "StoreManager Name" nếu đã tồn tại
        tableView.getColumns().removeIf(column -> column.getText().equals("StoreManager Name"));

        String search = searchField.getText().trim().toLowerCase();
        List<Import> filteredImports;

        // Lọc theo loại hiện tại (currentFilter)
        if ("Warehouse".equals(currentFilter)) {
            filteredImports = importController.getAllImportedWarehouse("");
        } else if ("StoreManager".equals(currentFilter)) {
            filteredImports = importController.getAllImportedStore("");

            // Thêm cột "StoreManager Name" vào khi filter là "StoreManager"
            TableColumn<Import, String> storeNameColumn = new TableColumn<>("StoreManager Name");
            storeNameColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getStoreName()));
            storeNameColumn.setPrefWidth(150);
            storeNameColumn.getStyleClass().add("column");

            tableView.getColumns().add(2, storeNameColumn); // Thêm cột vào vị trí mong muốn
        } else {
            // Mặc định là Warehouse nếu chưa chọn filter
            filteredImports = importController.getAllImportedWarehouse("");
        }

        // Áp dụng tìm kiếm chỉ trên dữ liệu của filter hiện tại
        List<Import> searchedImports = filteredImports.stream()
                .filter(item -> item.getImportName().toLowerCase().contains(search)
                        || item.getWarehouseName().toLowerCase().contains(search))
                .toList();

        // Xác định số trang dựa trên kết quả tìm kiếm
        totalPages = (int) Math.ceil((double) searchedImports.size() / itemsPerPage);
        int fromIndex = (currentPage - 1) * itemsPerPage;
        int toIndex = Math.min(fromIndex + itemsPerPage, searchedImports.size());

        // Phân trang các kết quả tìm kiếm
        List<Import> paginatedImports = searchedImports.subList(fromIndex, toIndex);

        ObservableList<Import> observableImports = FXCollections.observableArrayList(paginatedImports);
        tableView.setItems(observableImports);
        pageLabel.setText("Page " + currentPage + " / " + totalPages);
    }

    private void showCreateInvoice() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Create Invoice");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setVgap(8);
        grid.setHgap(10);

        // Tạo TextField cho Invoice Name
        TextField nameField = new TextField();
        nameField.getStyleClass().add("text-field-account");

        // Tạo TextField làm ô tìm kiếm cho Warehouse Name
        TextField searchField = new TextField();
        searchField.setPromptText("Search Product");
        searchField.getStyleClass().add("text-field-account");

        // Tạo ComboBox cho Warehouse Name
        ComboBox<String> warehouseComboBox = new ComboBox<>();
        warehouseComboBox.getStyleClass().add("text-field-account");
        warehouseComboBox.setPromptText("Select Warehouse");

        // Lấy danh sách tên kho từ Database và thêm vào ComboBox
        ObservableList<String> warehouseNames = ImportManagementModel.getWarehouseNames();
        warehouseComboBox.setItems(warehouseNames);

        // Thêm sự kiện để tìm kiếm trong ComboBox
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            // Lọc danh sách các tên kho dựa trên đầu vào tìm kiếm
            ObservableList<String> filteredList = FXCollections.observableArrayList();
            for (String warehouse : warehouseNames) {
                if (warehouse.toLowerCase().contains(newValue.toLowerCase())) {
                    filteredList.add(warehouse);
                }
            }
            warehouseComboBox.setItems(filteredList);
        });

        // Tạo và cấu hình bảng sản phẩm
        TableView<Product> productTable = new TableView<>();
        setupProductTableColumns(productTable);

        // Lấy danh sách sản phẩm và đưa vào bảng
        List<Product> products = importController.getAllProducts();
        productTable.getItems().addAll(products);

        // Thêm các thành phần vào GridPane
        grid.add(new Label("Invoice Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Search Product:"), 0, 1);
        grid.add(searchField, 1, 1);
        grid.add(new Label("Warehouse Name:"), 0, 2);
        grid.add(warehouseComboBox, 1, 2);

        // Thêm bảng sản phẩm vào GridPane
        grid.add(productTable, 0, 3, 2, 1); // Span qua 2 cột để bảng trông đẹp hơn

        // Cài đặt cảnh cho Dialog
        Scene dialogScene = new Scene(grid);
        dialogScene.getStylesheets().add(getClass().getResource("/view/popup.css").toExternalForm());
        dialog.setScene(dialogScene);
        dialog.show();
        dialog.setWidth(1300);
        dialog.setHeight(600);
    }


    private void showStoreDetails(Import imported) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Invoice Details");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setVgap(10);
        grid.setHgap(10);

        // Lấy thông tin từ đối tượng imported
        String warehouseName = imported.getWarehouseName();
        String nameInvoice = imported.getImportName();
        LocalDateTime receivedDate = imported.getDate();
        double total = imported.getTotal();
        String status = imported.getStatus();

        // Tạo các Labels với dữ liệu
        Label invoiceNameLabel = new Label("Invoice Name:");
        Label invoiceNameData = new Label(nameInvoice);

        Label warehouseNameLabel = new Label("Warehouse Name:");
        Label warehouseNameData = new Label(warehouseName);

        Label receivedDateLabel = new Label("Received Date:");
        Label receivedDateData = new Label(receivedDate.toString());

        Label totalLabel = new Label("Total:");
        Label totalData = new Label(String.valueOf(total));

        Label statusLabel = new Label("Status:");
        Label statusData = new Label(status);

        // Áp dụng style cho các nhãn và dữ liệu
        invoiceNameLabel.getStyleClass().add("label-popup");
        invoiceNameData.getStyleClass().add("data-popup");

        warehouseNameLabel.getStyleClass().add("label-popup");
        warehouseNameData.getStyleClass().add("data-popup");

        receivedDateLabel.getStyleClass().add("label-popup");
        receivedDateData.getStyleClass().add("data-popup");

        totalLabel.getStyleClass().add("label-popup");
        totalData.getStyleClass().add("data-popup");

        statusLabel.getStyleClass().add("label-popup");
        statusData.getStyleClass().add("data-popup");

        // Thêm các thành phần vào GridPane
        grid.add(invoiceNameLabel, 0, 0);
        grid.add(invoiceNameData, 1, 0);

        grid.add(warehouseNameLabel, 0, 1);
        grid.add(warehouseNameData, 1, 1);

        grid.add(receivedDateLabel, 0, 2);
        grid.add(receivedDateData, 1, 2);

        grid.add(totalLabel, 0, 3);
        grid.add(totalData, 1, 3);

        grid.add(statusLabel, 0, 4);
        grid.add(statusData, 1, 4);

        // Nút đóng
        Button closeButton = new Button("Close");
        closeButton.getStyleClass().add("button-pagination");
        closeButton.setOnAction(e -> dialog.close());

        // Thêm GridPane và nút vào VBox
        VBox vbox = new VBox(grid, closeButton);
        vbox.setSpacing(10);
        vbox.setPadding(new Insets(15));

        // Cài đặt cảnh cho dialog
        Scene dialogScene = new Scene(vbox);
        dialogScene.getStylesheets().add(getClass().getResource("/view/director.css").toExternalForm());
        dialog.setScene(dialogScene);
        dialog.show();
    }


}


