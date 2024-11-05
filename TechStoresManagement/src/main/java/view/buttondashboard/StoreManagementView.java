package view.buttondashboard;

import controller.StoreController;
import entity.*;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.StoreModel;
import view.stage.AdditionSuccess;
import view.stage.DeletionFailed;
import view.stage.DeletionSuccess;
import view.stage.EditSuccess;
import java.util.List;
public class StoreManagementView extends VBox {
    private TableView<Store> tableView;
    private StoreController storeController;
    private TextField searchField;
    private int currentPage = 1;
    private final int itemsPerPage = 12;
    private int totalPages;
    private final Label pageLabel = new Label();

    public StoreManagementView() {
        storeController = new StoreController();

        // Title
        Label titleLabel = new Label("StoreManager Management");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        // Create layout
        BorderPane borderPane = new BorderPane();
        VBox topLayout = new VBox(10);
        topLayout.setPadding(new Insets(10));

        // Search field and button layout
        searchField = new TextField();
        searchField.setPromptText("Search by name or address...");
        searchField.getStyleClass().add("search-box");
        searchField.textProperty().addListener((observable, oldValue, newValue) -> loadStores());

        HBox searchBar = new HBox(searchField);
        searchBar.setAlignment(Pos.CENTER_RIGHT);
        searchBar.setStyle(" -fx-padding:0 635 10 10;");

        // Create warehouse button
        Button createWarehouseButton = new Button("Create StoreManager");
        createWarehouseButton.setOnAction(e -> showCreateStoreDialog());
        createWarehouseButton.getStyleClass().add("button-pagination");


        // Thêm nút Create Warehouse bên dưới ô search
        HBox topControls = new HBox(10);
        topControls.setStyle("-fx-min-width: 1000");
        topControls.getChildren().addAll(searchBar,createWarehouseButton);
        borderPane.setTop(topControls);


        // Table setup
        tableView = new TableView<>();
        setupTableView(); // Cấu hình bảng
        loadStores();

        borderPane.setCenter(tableView);


        Button prevButton = new Button("<-");
        prevButton.getStyleClass().add("button-pagination");
        Button nextButton = new Button("->");
        nextButton.getStyleClass().add("button-pagination");
        pageLabel.getStyleClass().add("text-pagination");

        prevButton.setOnAction(e -> {
            if (currentPage > 1) {
                currentPage--;
                updateTableData(); // Update the table data for the new page
            }
        });

        nextButton.setOnAction(e -> {
            if (currentPage < totalPages) {
                currentPage++;
                updateTableData(); // Update the table data for the new page
            }
        });
        // HBox chứa các nút phân trang và nhãn số trang
        HBox paginationBox = new HBox(10, prevButton, pageLabel, nextButton);
        paginationBox.setAlignment(Pos.CENTER);
        paginationBox.setSpacing(30);
        paginationBox.setStyle("-fx-padding: 8");


        // Add everything to main layout
        this.getChildren().addAll(titleLabel, borderPane,paginationBox);
        this.getStyleClass().add("vbox");
    }


    private void setupTableView() {
        TableColumn<Store, Integer> indexColumn = new TableColumn<>("STT");
        indexColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(tableView.getItems().indexOf(cellData.getValue()) + 1).asObject());
        indexColumn.setPrefWidth(40);
        indexColumn.getStyleClass().add("column");

        TableColumn<Store, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        nameColumn.setPrefWidth(275);
        nameColumn.getStyleClass().add("column");

        TableColumn<Store, String> addressColumn = new TableColumn<>("Address");
        addressColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getAddress()));
        addressColumn.setPrefWidth(400);
        addressColumn.getStyleClass().add("column");

        TableColumn<Store, String> managerColumn = new TableColumn<>("Manager");
        managerColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getManagerName()));
        managerColumn.setPrefWidth(150);
        managerColumn.getStyleClass().add("column");

        TableColumn<Store, String> actionColumn = new TableColumn<>("        Action");
        actionColumn.setMinWidth(189);
        actionColumn.getStyleClass().add("column");
        actionColumn.setCellFactory(col -> new TableCell<Store, String>() {
                private final Button viewButton = new Button();
                private final Button editButton = new Button();
                private final Button deleteButton = new Button();

                {
                    // Tạo ImageView cho các icon
                    ImageView viewIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/view.png")));
                    ImageView editIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/edit.png")));
                    ImageView deleteIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/delete.png")));

                    // Đặt kích thước ban đầu cho icon
                    setIconSize(viewIcon, 20);
                    setIconSize(editIcon, 20);
                    setIconSize(deleteIcon, 20);

                    // Thêm icon vào nút
                    viewButton.setGraphic(viewIcon);
                    editButton.setGraphic(editIcon);
                    deleteButton.setGraphic(deleteIcon);

                    // Đặt style cho nút
                    String defaultStyle = "-fx-background-color: transparent; -fx-border-color: transparent; -fx-padding: 6;";
                    viewButton.setStyle(defaultStyle);
                    editButton.setStyle(defaultStyle);
                    deleteButton.setStyle(defaultStyle);

                    // Thêm sự kiện phóng to khi hover và giảm padding
                    addHoverEffect(viewButton, viewIcon);
                    addHoverEffect(editButton, editIcon);
                    addHoverEffect(deleteButton, deleteIcon);
                }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() < 0) {
                    setGraphic(null);
                } else {
                    Store store = getTableView().getItems().get(getIndex());
                    viewButton.setOnAction(e -> showStoreDetails(store));
                    editButton.setOnAction(e -> showEditStoreDialog(store));
                    deleteButton.setOnAction(e -> deleteStore(store));

                    HBox buttons = new HBox(viewButton, editButton, deleteButton);
                    buttons.setStyle("-fx-alignment: CENTER_LEFT; -fx-spacing: 10;");
                    setGraphic(buttons);
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




        tableView.getColumns().addAll(indexColumn, nameColumn, addressColumn, managerColumn, actionColumn);
    }

    private void updateTableData() {
        loadStores(); // Load stores for the current page
    };
    private void loadStores() {
        String search = searchField.getText().trim();
        List<Store> stores = storeController.getStores(search);
        totalPages = (int) Math.ceil((double) stores.size() / itemsPerPage);

        int fromIndex = (currentPage - 1) * itemsPerPage;
        int toIndex = Math.min(fromIndex + itemsPerPage, stores.size());
        List<Store> paginatedStores = stores.subList(fromIndex, toIndex);

        ObservableList<Store> observableStores = FXCollections.observableArrayList(paginatedStores);
        tableView.setItems(observableStores);
        pageLabel.setText("Page " + currentPage + " / " + totalPages);
    };
    private void showCreateStoreDialog() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Create StoreManager");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setVgap(8);
        grid.setHgap(10);

        TextField nameField = new TextField();
        nameField.getStyleClass().add("text-field-account");
        TextField addressField = new TextField();
        addressField.getStyleClass().add("text-field-account");
        Button submitButton = new Button("Submit");
        submitButton.getStyleClass().add("button-account");
        Button cancelButton = new Button("Cancel");
        cancelButton.getStyleClass().add("button-cancel-account");

        grid.add(new Label("StoreManager Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Address:"), 0, 1);
        grid.add(addressField, 1, 1);
        grid.add(submitButton, 0, 2);
        grid.add(cancelButton, 1, 2);

        submitButton.setOnAction(e -> {
            String name = nameField.getText().trim();
            String address = addressField.getText().trim();
            if (storeController.isStoreNameDuplicate(name) && storeController.isStoreAddressDuplicate(address)) {
                showAlert("Duplicate StoreManager", "StoreManager name or address already exists.");
            } else {
                storeController.addStore(name, address);
                Stage stage = new Stage();
                AdditionSuccess message = new AdditionSuccess();
                message.start(stage);
                dialog.close();
                loadStores();
            }
        });

        cancelButton.setOnAction(e -> dialog.close());
        Scene dialogScene = new Scene(grid);
        dialogScene.getStylesheets().add(getClass().getResource("/view/popup.css").toExternalForm());
        dialog.setScene(dialogScene);
        dialog.show();
    }

    private void showStoreDetails(Store store) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("StoreManager Details");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setVgap(10);
        grid.setHgap(10);

        // Financial information
        double[] financials = storeController.calculateFinancials(store.getId());

        // Create variables for store details
        String storeName = store.getName();
        String address = store.getAddress();
        String managerName = store.getManagerName();
        double totalRevenue = financials[0];
        double totalProfit = financials[1];
        double totalCapital = financials[2];
        int totalInventory = store.getTotalInventory();

        // Create Labels with the variable data
        Label storeNameLabel = new Label("StoreManager Name");
        Label storeNameData = new Label(storeName);

        Label addressLabel = new Label("Address");
        Label addressData = new Label(address);

        Label managerNameLabel = new Label("Manager");
        Label managerNameData = new Label(managerName);

        Label totalRevenueLabel = new Label("Total Revenue");
        Label totalRevenueData = new Label(String.valueOf(totalRevenue));

        Label totalProfitLabel = new Label("Total Profit");
        Label totalProfitData = new Label(String.valueOf(totalProfit));

        Label totalCapitalLabel = new Label("Total Capital");
        Label totalCapitalData = new Label(String.valueOf(totalCapital));

        Label totalInventoryLabel = new Label("Total Inventory");
        Label totalInventoryData = new Label(String.valueOf(totalInventory));

        // Add the same CSS class for labels and data with different classes for color
        storeNameLabel.getStyleClass().add("label-popup");
        storeNameData.getStyleClass().add("data-popup");

        addressLabel.getStyleClass().add("label-popup");
        addressData.getStyleClass().add("data-popup");

        managerNameLabel.getStyleClass().add("label-popup");
        managerNameData.getStyleClass().add("data-popup");

        totalRevenueLabel.getStyleClass().add("label-popup");
        totalRevenueData.getStyleClass().add("data-popup");

        totalProfitLabel.getStyleClass().add("label-popup");
        totalProfitData.getStyleClass().add("data-popup");

        totalCapitalLabel.getStyleClass().add("label-popup");
        totalCapitalData.getStyleClass().add("data-popup");

        totalInventoryLabel.getStyleClass().add("label-popup");
        totalInventoryData.getStyleClass().add("data-popup");

        // Add labels and data to the grid
        grid.add(storeNameLabel, 0, 0);
        grid.add(storeNameData, 1, 0);

        grid.add(addressLabel, 0, 1);
        grid.add(addressData, 1, 1);

        grid.add(managerNameLabel, 0, 2);
        grid.add(managerNameData, 1, 2);

        grid.add(totalRevenueLabel, 0, 3);
        grid.add(totalRevenueData, 1, 3);

        grid.add(totalProfitLabel, 0, 4);
        grid.add(totalProfitData, 1, 4);

        grid.add(totalCapitalLabel, 0, 5);
        grid.add(totalCapitalData, 1, 5);

        grid.add(totalInventoryLabel, 0, 6);
        grid.add(totalInventoryData, 1, 6);

        // Create a table for products in store
        TableView<Product> productTable = new TableView<>();
        setupProductTableColumns(productTable);

        // Load product data for the store
        List<Product> products = storeController.getProductsByStoreId(store.getId());
        productTable.getItems().addAll(products);

        // Close button
        Button closeButton = new Button("Close");
        closeButton.getStyleClass().add("button-pagination");
        closeButton.setOnAction(e -> dialog.close());

        // Add the grid and button to the VBox
        VBox vbox = new VBox(grid, productTable, closeButton);
        vbox.setSpacing(10);
        vbox.setPadding(new Insets(15));

        Scene dialogScene = new Scene(vbox);
        dialogScene.getStylesheets().add(getClass().getResource("/view/director.css").toExternalForm());
        dialog.setScene(dialogScene);
        dialog.show();
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

        TableColumn<Product, Integer> stockColumn = new TableColumn<>("Stock");
        stockColumn.setCellValueFactory(cellData -> cellData.getValue().stockProperty().asObject());
        stockColumn.setPrefWidth(70);
        stockColumn.getStyleClass().add("column");

        TableColumn<Product, Integer> soldColumn = new TableColumn<>("Sold Quantity");
        soldColumn.setCellValueFactory(cellData -> cellData.getValue().soldQuantityProperty().asObject()); // Lấy soldQuantity từ Product
        soldColumn.setPrefWidth(90);
        soldColumn.getStyleClass().add("column");

        TableColumn<Product, Double> profitColumn = new TableColumn<>("Profit");
        profitColumn.setCellValueFactory(cellData -> cellData.getValue().profitProperty().asObject()); // Lấy profit từ Product
        profitColumn.setPrefWidth(70);
        profitColumn.getStyleClass().add("column");


        productTable.getColumns().addAll(nameColumn, brandColumn, stockColumn, soldColumn, profitColumn);
        productTable.setStyle("-fx-pref-height: 380; -fx-pref-width :580");
    }


    private void showEditStoreDialog(Store store) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Edit StoreManager");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setVgap(8);
        grid.setHgap(10);

        TextField nameField = new TextField(store.getName());
        nameField.getStyleClass().add("text-field-account");
        TextField addressField = new TextField(store.getAddress());
        addressField.getStyleClass().add("text-field-account");
        TextField managerField = new TextField();
        managerField.getStyleClass().add("text-field-account");
        managerField.setPromptText("Nhập tên nhân viên quản lý");
        Button saveButton = new Button("Save");
        saveButton.getStyleClass().add("button-account");
        Button cancelButton = new Button("Cancel");
        cancelButton.getStyleClass().add("button-cancel-account");
        Button reloadButton = new Button("Reload");
        reloadButton.getStyleClass().add("reload-button");

        managerField.setText(store.getManagerName());

        grid.add(new Label("StoreManager Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Address:"), 0, 1);
        grid.add(addressField, 1, 1);
        grid.add(new Label("Manager:"), 0, 2);
        grid.add(managerField, 1, 2);
        grid.add(saveButton, 0, 4);
        grid.add(reloadButton, 1, 4);
        grid.add(cancelButton, 2, 4);

        saveButton.setOnAction(e -> {
            String name = nameField.getText().trim();
            String address = addressField.getText().trim();
            String managerName = managerField.getText().trim();

            // Kiểm tra nếu không có thay đổi nào
            boolean hasChanges = !(name.equals(store.getName()) && address.equals(store.getAddress()) && managerName.equals(store.getManagerName()));

            if (!hasChanges) {
                dialog.close(); // Đóng cửa sổ nếu không có thay đổi
                return; // Không thực hiện cập nhật
            }

            // Kiểm tra xem nhân viên có tồn tại hay không nếu tên quản lý đã thay đổi
            StoreModel storeModel = new StoreModel();
            String currentManagerName = store.getManagerName();

            // Chỉ kiểm tra vai trò nếu tên người quản lý đã thay đổi
            if (!managerName.equals(currentManagerName)) {
                String employeeRole = storeModel.getEmployeeRoleByName(managerName);

                if (employeeRole == null) {
                    showAlert("Invalid Manager", "Manager does not exist.");
                    return;
                } else if (!employeeRole.equals("Employee")) {
                    showAlert("Invalid Manager", "Manager must be an existing employee with the role of 'Employee'.");
                    return;
                }
            }

            // Lấy ID của nhân viên quản lý
            Integer managerId = storeModel.getEmployeeIdByName(managerName);

            // Tách riêng kiểm tra trùng lặp tên và địa chỉ
            boolean isNameChanged = !name.equals(store.getName());
            boolean isAddressChanged = !address.equals(store.getAddress());

            boolean nameExists = false;
            boolean addressExists = false;

            // Chỉ kiểm tra tên cửa hàng nếu có sự thay đổi
            if (isNameChanged) {
                nameExists = storeController.isStoreNameDuplicate(name);
            }

            // Chỉ kiểm tra địa chỉ nếu có sự thay đổi
            if (isAddressChanged) {
                addressExists = storeController.isStoreAddressDuplicate(address);
            }

            // Kiểm tra xem có tên hoặc địa chỉ bị trùng lặp hay không
            if (nameExists || addressExists) {
                String message = "StoreManager ";
                if (nameExists) {
                    message += "name ";
                }
                if (addressExists) {
                    message += "address ";
                }
                message += "already exists.";
                showAlert("Duplicate StoreManager", message);
            } else {
                // Cập nhật thông tin cửa hàng
                storeController.updateStore(store.getId(), name, address, managerId);
                Stage stage = new Stage();
                EditSuccess message = new EditSuccess();
                message.start(stage);
                dialog.close();
                loadStores();
            }
        });

        reloadButton.setOnAction(e -> {
            nameField.setText(store.getName());
            addressField.setText(store.getAddress());
            managerField.setText(store.getManagerName());
        });

        cancelButton.setOnAction(e -> dialog.close());

        Scene dialogScene = new Scene(grid);
        dialogScene.getStylesheets().add(getClass().getResource("/view/popup.css").toExternalForm());
        dialog.setScene(dialogScene);
        dialog.show();
    }

    private void deleteStore(Store store) {
        if (storeController.deleteStore(store.getId())) {
            Stage stage = new Stage();
            DeletionSuccess message = new DeletionSuccess();
            message.start(stage);
            loadStores();
        } else {
            Stage stage = new Stage();
            DeletionFailed message = new DeletionFailed();
            message.start(stage);
            showAlert("Cannot Delete StoreManager", "StoreManager has products and cannot be deleted.");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
