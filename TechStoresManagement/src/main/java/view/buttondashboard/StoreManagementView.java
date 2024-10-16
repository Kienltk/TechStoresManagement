package view.buttondashboard;

import controller.StoreController;
import entity.Employee;
import entity.Product;
import entity.Store;
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

import java.util.List;
import java.util.Optional;

public class StoreManagementView extends VBox {
    private TableView<Store> tableView;
    private StoreController storeController;
    private TextField searchField;

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
        Button searchButton = new Button("Search");
        searchButton.setOnAction(e -> loadStores());
        searchField.textProperty().addListener((observable, oldValue, newValue) -> loadStores());

        HBox searchLayout = new HBox(10);
        searchLayout.getChildren().addAll(searchField, searchButton);
        HBox.setHgrow(searchField, Priority.ALWAYS); // Đẩy searchField ra toàn bộ chiều ngang

        // Create store button
        Button createStoreButton = new Button("Create Store");
        createStoreButton.setOnAction(e -> showCreateStoreDialog());
        createStoreButton.setAlignment(Pos.CENTER_LEFT); // Đặt nút ở bên trái

        // Thêm nút Create Store bên dưới ô search
        VBox topControls = new VBox(10);
        topControls.getChildren().addAll(searchLayout, createStoreButton);
        topControls.setSpacing(10);
        topControls.setPadding(new Insets(10, 0, 10, 0));
        topLayout.getChildren().addAll(topControls);
        borderPane.setTop(topLayout);

        // Table setup
        tableView = new TableView<>();
        setupTableView(); // Cấu hình bảng
        loadStores();

        // Set kích thước bảng hợp lý cho cửa sổ 1300x1000
        tableView.setPrefSize(1000, 500); // Đặt kích thước phù hợp
        borderPane.setCenter(tableView);

        // Set padding cho bảng và các phần tử
        BorderPane.setMargin(tableView, new Insets(10, 10, 10, 10)); // Cách đều lề của bảng
        borderPane.setPadding(new Insets(20)); // Cách đều các lề của layout

        // Add everything to main layout
        this.getChildren().addAll(titleLabel, borderPane);
    }


    private void setupTableView() {
        TableColumn<Store, Integer> indexColumn = new TableColumn<>("STT");
        indexColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(tableView.getItems().indexOf(cellData.getValue()) + 1).asObject());
        indexColumn.setPrefWidth(50);

        TableColumn<Store, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        nameColumn.setPrefWidth(250);

        TableColumn<Store, String> addressColumn = new TableColumn<>("Address");
        addressColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getAddress()));
        addressColumn.setPrefWidth(400);

        TableColumn<Store, String> managerColumn = new TableColumn<>("Manager");
        managerColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getManagerName()));
        managerColumn.setPrefWidth(150);

        TableColumn<Store, String> actionColumn = new TableColumn<>("Action");
        actionColumn.setCellFactory(col -> new TableCell<Store, String>() {
            private final Button viewButton = new Button("View");
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");

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
                    setGraphic(buttons);
                }
            }
        });

        actionColumn.setPrefWidth(150);

        tableView.getColumns().addAll(indexColumn, nameColumn, addressColumn, managerColumn, actionColumn);
    }

    private void loadStores() {
        String search = searchField.getText().trim();
        List<Store> stores = storeController.getStores(search);
        ObservableList<Store> observableStores = FXCollections.observableArrayList(stores);
        tableView.setItems(observableStores);
    }

    private void showCreateStoreDialog() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Create Store");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setVgap(8);
        grid.setHgap(10);

        TextField nameField = new TextField();
        TextField addressField = new TextField();
        Button submitButton = new Button("Submit");
        Button cancelButton = new Button("Cancel");

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
                dialog.close();
                loadStores();
            }
        });

        cancelButton.setOnAction(e -> dialog.close());

        Scene dialogScene = new Scene(grid);
        dialog.setScene(dialogScene);
        dialog.show();
    }

    private void showStoreDetails(Store store) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Store Details");

        VBox vbox = new VBox();
        vbox.setPadding(new Insets(10));
        vbox.setSpacing(8);

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
        closeButton.setOnAction(e -> dialog.close());
        vbox.getChildren().addAll(productTable, closeButton);

        Scene dialogScene = new Scene(vbox);
        dialog.setScene(dialogScene);
        dialog.show();
    }

    private void setupProductTableColumns(TableView<Product> productTable) {
        TableColumn<Product, String> nameColumn = new TableColumn<>("Product Name");
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());

        TableColumn<Product, String> brandColumn = new TableColumn<>("Brand");
        brandColumn.setCellValueFactory(cellData -> cellData.getValue().brandProperty());

        TableColumn<Product, Integer> stockColumn = new TableColumn<>("Stock");
        stockColumn.setCellValueFactory(cellData -> cellData.getValue().stockProperty().asObject());

        TableColumn<Product, Integer> soldColumn = new TableColumn<>("Sold Quantity");
        soldColumn.setCellValueFactory(cellData -> cellData.getValue().soldQuantityProperty().asObject()); // Lấy soldQuantity từ Product

        TableColumn<Product, Double> profitColumn = new TableColumn<>("Profit");
        profitColumn.setCellValueFactory(cellData -> cellData.getValue().profitProperty().asObject()); // Lấy profit từ Product


        productTable.getColumns().addAll(nameColumn, brandColumn, stockColumn, soldColumn, profitColumn);
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
        TextField addressField = new TextField(store.getAddress());
        TextField managerField = new TextField();
        managerField.setPromptText("Nhập tên nhân viên quản lý");
        Button saveButton = new Button("Save");
        Button reloadButton = new Button("Reload");
        Button cancelButton = new Button("Cancel");

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
        dialog.setScene(dialogScene);
        dialog.show();
    }

    private void deleteStore(Store store) {
        if (storeController.deleteStore(store.getId())) {
            loadStores();
        } else {
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
