package view.buttondashboard;
import controller.WarehouseController;
import entity.Product;
import entity.Warehouse;
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
import model.WarehouseModel;

import java.util.List;

public class WarehouseManagementView extends VBox {
    private TableView<Warehouse> tableView;
    private WarehouseController warehouseController;
    private TextField searchField;
    public WarehouseManagementView() {
        warehouseController = new WarehouseController();

        // Title
        Label titleLabel = new Label("Warehouse Management");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        // Create layout
        BorderPane borderPane = new BorderPane();
        VBox topLayout = new VBox(10);
        topLayout.setPadding(new Insets(10));

        // Search field and button layout
        searchField = new TextField();
        searchField.setPromptText("Search by name or address...");
        Button searchButton = new Button("Search");
        searchButton.setOnAction(e -> loadWarehouses());
        searchField.textProperty().addListener((observable, oldValue, newValue) -> loadWarehouses());

        HBox searchLayout = new HBox(10);
        searchLayout.getChildren().addAll(searchField, searchButton);
        HBox.setHgrow(searchField, Priority.ALWAYS); // Đẩy searchField ra toàn bộ chiều ngang

        // Create warehouse button
        Button createWarehouseButton = new Button("Create Warehouse");
        createWarehouseButton.setOnAction(e -> showCreateWarehouseDialog());
        createWarehouseButton.setAlignment(Pos.CENTER_LEFT); // Đặt nút ở bên trái

        // Thêm nút Create Warehouse bên dưới ô search
        VBox topControls = new VBox(10);
        topControls.getChildren().addAll(searchLayout, createWarehouseButton);
        topControls.setSpacing(10);
        topControls.setPadding(new Insets(10, 0, 10, 0));
        topLayout.getChildren().addAll(topControls);
        borderPane.setTop(topLayout);

        // Table setup
        tableView = new TableView<>();
        setupTableView(); // Cấu hình bảng
        loadWarehouses();

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
        TableColumn<Warehouse, Integer> indexColumn = new TableColumn<>("STT");
        indexColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(tableView.getItems().indexOf(cellData.getValue()) + 1).asObject());
        indexColumn.setPrefWidth(50);

        TableColumn<Warehouse, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        nameColumn.setPrefWidth(250);

        TableColumn<Warehouse, String> addressColumn = new TableColumn<>("Address");
        addressColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getAddress()));
        addressColumn.setPrefWidth(400);

        TableColumn<Warehouse, String> managerColumn = new TableColumn<>("Manager");
        managerColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getManagerName()));
        managerColumn.setPrefWidth(150);

        TableColumn<Warehouse, String> actionColumn = new TableColumn<>("Action");
        actionColumn.setCellFactory(col -> new TableCell<Warehouse, String>() {
            private final Button viewButton = new Button("View");
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() < 0) {
                    setGraphic(null);
                } else {
                    Warehouse warehouse = getTableView().getItems().get(getIndex());
                    viewButton.setOnAction(e -> showWarehouseDetails(warehouse));
                    editButton.setOnAction(e -> showEditWarehouseDialog(warehouse));
                    deleteButton.setOnAction(e -> deleteWarehouse(warehouse));
                    HBox buttons = new HBox(viewButton, editButton, deleteButton);
                    setGraphic(buttons);
                }
            }
        });

        actionColumn.setPrefWidth(150);

        tableView.getColumns().addAll(indexColumn, nameColumn, addressColumn, managerColumn, actionColumn);
    }

    private void loadWarehouses() {
        String search = searchField.getText().trim();
        List<Warehouse> warehouses = warehouseController.getWarehouses(search);
        ObservableList<Warehouse> observableWarehouses = FXCollections.observableArrayList(warehouses);
        tableView.setItems(observableWarehouses);
    }

    private void showCreateWarehouseDialog() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Create Warehouse");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setVgap(8);
        grid.setHgap(10);

        TextField nameField = new TextField();
        TextField addressField = new TextField();
        Button submitButton = new Button("Submit");
        Button cancelButton = new Button("Cancel");

        grid.add(new Label("Warehouse Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Address:"), 0, 1);
        grid.add(addressField, 1, 1);
        grid.add(submitButton, 0, 2);
        grid.add(cancelButton, 1, 2);

        submitButton.setOnAction(e -> {
            String name = nameField.getText().trim();
            String address = addressField.getText().trim();
            if (warehouseController.isWarehouseNameDuplicate(name) && warehouseController.isWarehouseAddressDuplicate(address)) {
                showAlert("Duplicate Warehouse", "Warehouse name or address already exists.");
            } else {
                warehouseController.addWarehouse(name, address);
                dialog.close();
                loadWarehouses();
            }
        });

        cancelButton.setOnAction(e -> dialog.close());

        Scene dialogScene = new Scene(grid);
        dialog.setScene(dialogScene);
        dialog.show();
    }

    private void showWarehouseDetails(Warehouse warehouse) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Warehouse Details");

        VBox vbox = new VBox();
        vbox.setPadding(new Insets(10));
        vbox.setSpacing(8);

        vbox.getChildren().addAll(
                new Label("Warehouse Name: " + warehouse.getName()),
                new Label("Address: " + warehouse.getAddress()),
                new Label("Manager: " + warehouse.getManagerName()),
                new Label("Total Inventory: " + warehouse.getTotalInventory())
        );

        // Create a table for products in warehouse
        TableView<Product> productTable = new TableView<>();
        setupProductTableColumns(productTable);

        // Load product data for the warehouse
        List<Product> products = warehouseController.getProductsByWarehouseId(warehouse.getId());
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


        productTable.getColumns().addAll(nameColumn, brandColumn, stockColumn);
    }


    private void showEditWarehouseDialog(Warehouse warehouse) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Edit Warehouse");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setVgap(8);
        grid.setHgap(10);

        TextField nameField = new TextField(warehouse.getName());
        TextField addressField = new TextField(warehouse.getAddress());
        TextField managerField = new TextField();
        managerField.setPromptText("Nhập tên nhân viên quản lý");
        Button saveButton = new Button("Save");
        Button reloadButton = new Button("Reload");
        Button cancelButton = new Button("Cancel");

        managerField.setText(warehouse.getManagerName());

        grid.add(new Label("Warehouse Name:"), 0, 0);
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
            boolean hasChanges = !(name.equals(warehouse.getName()) && address.equals(warehouse.getAddress()) && managerName.equals(warehouse.getManagerName()));

            if (!hasChanges) {
                dialog.close(); // Đóng cửa sổ nếu không có thay đổi
                return; // Không thực hiện cập nhật
            }

            // Kiểm tra xem nhân viên có tồn tại hay không nếu tên quản lý đã thay đổi
            WarehouseModel warehouseModel = new WarehouseModel();
            String currentManagerName = warehouse.getManagerName();

            // Chỉ kiểm tra vai trò nếu tên người quản lý đã thay đổi
            if (!managerName.equals(currentManagerName)) {
                String employeeRole = warehouseModel.getEmployeeRoleByName(managerName);

                if (employeeRole == null) {
                    showAlert("Invalid Manager", "Manager does not exist.");
                    return;
                } else if (!employeeRole.equals("Employee")) {
                    showAlert("Invalid Manager", "Manager must be an existing employee with the role of 'Employee'.");
                    return;
                }
            }

            // Lấy ID của nhân viên quản lý
            Integer managerId = warehouseModel.getEmployeeIdByName(managerName);

            // Tách riêng kiểm tra trùng lặp tên và địa chỉ
            boolean isNameChanged = !name.equals(warehouse.getName());
            boolean isAddressChanged = !address.equals(warehouse.getAddress());

            boolean nameExists = false;
            boolean addressExists = false;

            // Chỉ kiểm tra tên cửa hàng nếu có sự thay đổi
            if (isNameChanged) {
                nameExists = warehouseController.isWarehouseNameDuplicate(name);
            }

            // Chỉ kiểm tra địa chỉ nếu có sự thay đổi
            if (isAddressChanged) {
                addressExists = warehouseController.isWarehouseAddressDuplicate(address);
            }

            // Kiểm tra xem có tên hoặc địa chỉ bị trùng lặp hay không
            if (nameExists || addressExists) {
                String message = "Warehouse ";
                if (nameExists) {
                    message += "name ";
                }
                if (addressExists) {
                    message += "address ";
                }
                message += "already exists.";
                showAlert("Duplicate Warehouse", message);
            } else {
                // Cập nhật thông tin cửa hàng
                warehouseController.updateWarehouse(warehouse.getId(), name, address, managerId);
                dialog.close();
                loadWarehouses();
            }
        });

        reloadButton.setOnAction(e -> {
            nameField.setText(warehouse.getName());
            addressField.setText(warehouse.getAddress());
            managerField.setText(warehouse.getManagerName());
        });

        cancelButton.setOnAction(e -> dialog.close());

        Scene dialogScene = new Scene(grid);
        dialog.setScene(dialogScene);
        dialog.show();
    }

    private void deleteWarehouse(Warehouse warehouse) {
        if (warehouseController.deleteWarehouse(warehouse.getId())) {
            loadWarehouses();
        } else {
            showAlert("Cannot Delete Warehouse", "Warehouse has products and cannot be deleted.");
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
