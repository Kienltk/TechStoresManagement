package view.buttondashboard;

import controller.StoreController;
import entity.*;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import model.StoreModel;
import view.stage.AdditionSuccess;
import view.stage.DeletionFailed;
import view.stage.DeletionSuccess;
import view.stage.EditSuccess;

import java.util.List;
import java.util.Optional;

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
        Label titleLabel = new Label("Store Management");
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
        searchBar.setStyle(" -fx-padding:0 10 10 595;");

        // Create warehouse button
        Button createWarehouseButton = new Button("Create Warehouse");
        createWarehouseButton.setOnAction(e -> showCreateStoreDialog());
        createWarehouseButton.getStyleClass().add("button-pagination");
        createWarehouseButton.setAlignment(Pos.CENTER_LEFT); // Đặt nút ở bên trái

        // Thêm nút Create Warehouse bên dưới ô search
        HBox topControls = new HBox(10);
        topControls.setStyle("-fx-min-width: 1000");
        topControls.getChildren().addAll( createWarehouseButton,searchBar);
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
        indexColumn.setStyle("-fx-alignment: center");
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

        TableColumn<Store, String> actionColumn = new TableColumn<>("Action");
        actionColumn.setPrefWidth(190);
        actionColumn.setCellFactory(col -> new TableCell<Store, String>() {
            private final Button viewButton = new Button("View");
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            {
                editButton.setStyle("-fx-background-color: yellow;");
                deleteButton.setStyle("-fx-background-color: red; -fx-text-fill: white;");
                viewButton.setStyle("-fx-background-color: #4AD4DD; -fx-text-fill: white;");
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
                    buttons.setSpacing(10);
                    setGraphic(buttons);
                }
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

        // Get the sublist for the current page
        int fromIndex = (currentPage - 1) * itemsPerPage;
        int toIndex = Math.min(fromIndex + itemsPerPage, stores.size());
        List<Store> paginatedStores = stores.subList(fromIndex, toIndex);

        ObservableList<Store> observableStores = FXCollections.observableArrayList(paginatedStores);
        tableView.setItems(observableStores);
        pageLabel.setText("Page " + currentPage + " / " + totalPages); // Update the page label
    };
    private void showCreateStoreDialog() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Create Store");

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

        grid.add(new Label("Store Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Address:"), 0, 1);
        grid.add(addressField, 1, 1);
        grid.add(submitButton, 0, 2);
        grid.add(cancelButton, 1, 2);

        submitButton.setOnAction(e -> {
            String name = nameField.getText().trim();
            String address = addressField.getText().trim();
            if (storeController.isStoreNameDuplicate(name) && storeController.isStoreAddressDuplicate(address)) {
                showAlert("Duplicate Store", "Store name or address already exists.");
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
        dialog.setTitle("Store Details");

        VBox vbox = new VBox();
        vbox.setPadding(new Insets(10));
        vbox.setSpacing(10);

        // Financial information
        double[] financials = storeController.calculateFinancials(store.getId());
        vbox.getChildren().addAll(
                new Label("Store Name: " + store.getName()),
                new Label("Address: " + store.getAddress()),
                new Label("Manager: " + store.getManagerName()),
                new Label("Total Revenue: " + financials[0]),
                new Label("Total Profit: " + financials[1]),
                new Label("Total Capital: " + financials[2]),
                new Label("Total Inventory: " + store.getTotalInventory())
        );

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
        vbox.getChildren().addAll(productTable, closeButton);

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
        brandColumn.setPrefWidth(150);
        brandColumn.getStyleClass().add("column");

        TableColumn<Product, Integer> stockColumn = new TableColumn<>("Stock");
        stockColumn.setCellValueFactory(cellData -> cellData.getValue().stockProperty().asObject());
        stockColumn.setPrefWidth(70);
        stockColumn.getStyleClass().add("column");

        TableColumn<Product, Integer> soldColumn = new TableColumn<>("Sold Quantity");
        soldColumn.setCellValueFactory(cellData -> cellData.getValue().soldQuantityProperty().asObject()); // Lấy soldQuantity từ Product
        soldColumn.setPrefWidth(70);
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
        dialog.setTitle("Edit Store");

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

        grid.add(new Label("Store Name:"), 0, 0);
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
                String message = "Store ";
                if (nameExists) {
                    message += "name ";
                }
                if (addressExists) {
                    message += "address ";
                }
                message += "already exists.";
                showAlert("Duplicate Store", message);
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
            showAlert("Cannot Delete Store", "Store has products and cannot be deleted.");
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
