package view.buttondashboard;
import controller.WarehouseController;
import entity.Account;
import entity.Product;
import entity.Store;
import entity.Warehouse;
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
import model.WarehouseModel;
import view.stage.AdditionSuccess;
import view.stage.DeletionFailed;
import view.stage.DeletionSuccess;
import view.stage.EditSuccess;

import java.util.List;

public class WarehouseManagementView extends VBox {
    private TableView<Warehouse> tableView;
    private WarehouseController warehouseController;
    private TextField searchField;
    private int currentPage = 1;
    private final int itemsPerPage = 12;
    private int totalPages;
    private final Label pageLabel = new Label();
    public WarehouseManagementView() {
        warehouseController = new WarehouseController();

        // Title
        Label titleLabel = new Label("Warehouse Management");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        // Create layout
        BorderPane borderPane = new BorderPane();

        // Search field and button layout
        searchField = new TextField();
        searchField.setPromptText("Search by name or address...");
        searchField.getStyleClass().add("search-box");
        searchField.textProperty().addListener((observable, oldValue, newValue) -> loadWarehouses());

        HBox searchBar = new HBox(searchField);
        searchBar.setAlignment(Pos.CENTER_RIGHT);
        searchBar.setStyle(" -fx-padding:0 595 10 10;");

        // Create warehouse button
        Button createWarehouseButton = new Button("Create Warehouse");
        createWarehouseButton.setOnAction(e -> showCreateWarehouseDialog());
        createWarehouseButton.getStyleClass().add("button-pagination");
        createWarehouseButton.setAlignment(Pos.CENTER_LEFT);

        // Thêm nút Create Warehouse bên dưới ô search
        HBox topControls = new HBox(10);
        topControls.setStyle("-fx-min-width: 1000");
        topControls.getChildren().addAll(searchBar,createWarehouseButton);
        borderPane.setTop(topControls);

        // Table setup
        tableView = new TableView<>();
        setupTableView(); // Cấu hình bảng
        loadWarehouses();

        // Set kích thước bảng hợp lý cho cửa sổ 1300x1000
        borderPane.setCenter(tableView);

        totalPages = 0;

        // Bind the data to the TableView

        Button prevButton = new Button("<-");
        prevButton.getStyleClass().add("button-pagination");
        Button nextButton = new Button("->");
        nextButton.getStyleClass().add("button-pagination");
        pageLabel.getStyleClass().add("text-pagination");

        prevButton.setOnAction(e -> {
            if (currentPage > 1) {
                currentPage--;
                updateTableData();
                pageLabel.setText("Page " + currentPage + " / " + totalPages); // Cập nhật số trang
            }
        });

        nextButton.setOnAction(e -> {
            if (currentPage < totalPages) {
                currentPage++;
                updateTableData();
                pageLabel.setText("Page " + currentPage + " / " + totalPages); // Cập nhật số trang
            }
        });


        // HBox chứa các nút phân trang và nhãn số trang
        HBox paginationBox = new HBox(10, prevButton, pageLabel, nextButton);
        paginationBox.setAlignment(Pos.CENTER);
        paginationBox.setSpacing(30);
        paginationBox.setStyle("-fx-padding: 8");

        // Add everything to main layout
        this.getChildren().addAll(titleLabel, borderPane , paginationBox);
        this.getStyleClass().add("vbox");
    }

    private void setupTableView() {
        TableColumn<Warehouse, Integer> indexColumn = new TableColumn<>("STT");
        indexColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(tableView.getItems().indexOf(cellData.getValue()) + 1).asObject());
        indexColumn.setPrefWidth(40);
        indexColumn.getStyleClass().add("column");

        TableColumn<Warehouse, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        nameColumn.setPrefWidth(275);
        nameColumn.getStyleClass().add("column");

        TableColumn<Warehouse, String> addressColumn = new TableColumn<>("Address");
        addressColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getAddress()));
        addressColumn.setPrefWidth(400);
        addressColumn.getStyleClass().add("column");

        TableColumn<Warehouse, String> managerColumn = new TableColumn<>("Manager");
        managerColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getManagerName()));
        managerColumn.setPrefWidth(150);
        managerColumn.getStyleClass().add("column");

        TableColumn<Warehouse, String> actionColumn = new TableColumn<>("        Action");
        actionColumn.setMinWidth(189);
        actionColumn.getStyleClass().add("column");
        actionColumn.setCellFactory(col -> new TableCell<Warehouse, String>() {
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
                    Warehouse Warehouse = getTableView().getItems().get(getIndex());
                    viewButton.setOnAction(e -> showWarehouseDetails(Warehouse));
                    editButton.setOnAction(e -> showEditWarehouseDialog(Warehouse));
                    deleteButton.setOnAction(e -> deleteWarehouse(Warehouse));

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

    private void loadWarehouses() {
        String search = searchField.getText().trim();
        List<Warehouse> warehouses = warehouseController.getWarehouses(search);

        // Calculate total pages
        totalPages = (int) Math.ceil((double) warehouses.size() / itemsPerPage);

        // Get the sublist for the current page
        int fromIndex = (currentPage - 1) * itemsPerPage;
        int toIndex = Math.min(fromIndex + itemsPerPage, warehouses.size());
        List<Warehouse> paginatedWarehouses = warehouses.subList(fromIndex, toIndex);

        ObservableList<Warehouse> observableWarehouses = FXCollections.observableArrayList(paginatedWarehouses);
        tableView.setItems(observableWarehouses);
        pageLabel.setText("Page " + currentPage + " / " + totalPages); // Update the page label
    }

    private void updateTableData() {
        loadWarehouses(); // Load warehouses for the current page
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
        nameField.getStyleClass().add("text-field-account");
        TextField addressField = new TextField();
        addressField.getStyleClass().add("text-field-account");
        Button submitButton = new Button("Submit");
        submitButton.getStyleClass().add("button-account");
        Button cancelButton = new Button("Cancel");
        cancelButton.getStyleClass().add("button-cancel-account");

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
                Stage stage = new Stage();
                AdditionSuccess message = new AdditionSuccess();
                message.start(stage);
                dialog.close();
                loadWarehouses();
            }
        });

        cancelButton.setOnAction(e -> dialog.close());

        Scene dialogScene = new Scene(grid);
        dialogScene.getStylesheets().add(getClass().getResource("/view/popup.css").toExternalForm());
        dialog.setScene(dialogScene);
        dialog.show();
    }

    private void showWarehouseDetails(Warehouse warehouse) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Warehouse Details");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setVgap(10);
        grid.setHgap(10);

        // Create variables for warehouse details
        String warehouseName = warehouse.getName();
        String address = warehouse.getAddress();
        String managerName = warehouse.getManagerName();
        int totalInventory = warehouse.getTotalInventory();

        // Create Labels with the variable data
        Label warehouseNameLabel = new Label("Warehouse Name");
        Label warehouseNameData = new Label(warehouseName);

        Label addressLabel = new Label("Address");
        Label addressData = new Label(address);

        Label managerNameLabel = new Label("Manager");
        Label managerNameData = new Label(managerName);

        Label totalInventoryLabel = new Label("Total Inventory");
        Label totalInventoryData = new Label(String.valueOf(totalInventory));

        // Add the same CSS class for labels and data with different classes for color
        warehouseNameLabel.getStyleClass().add("label-popup");
        warehouseNameData.getStyleClass().add("data-popup");

        addressLabel.getStyleClass().add("label-popup");
        addressData.getStyleClass().add("data-popup");

        managerNameLabel.getStyleClass().add("label-popup");
        managerNameData.getStyleClass().add("data-popup");

        totalInventoryLabel.getStyleClass().add("label-popup");
        totalInventoryData.getStyleClass().add("data-popup");

        // Add labels and data to the grid
        grid.add(warehouseNameLabel, 0, 0);
        grid.add(warehouseNameData, 1, 0);

        grid.add(addressLabel, 0, 1);
        grid.add(addressData, 1, 1);

        grid.add(managerNameLabel, 0, 2);
        grid.add(managerNameData, 1, 2);

        grid.add(totalInventoryLabel, 0, 3);
        grid.add(totalInventoryData, 1, 3);

        // Create a table for products in warehouse
        TableView<Product> productTable = new TableView<>();
        setupProductTableColumns(productTable);

        // Load product data for the warehouse
        List<Product> products = warehouseController.getProductsByWarehouseId(warehouse.getId());
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
        brandColumn.setPrefWidth(150);
        brandColumn.getStyleClass().add("column");

        TableColumn<Product, Integer> stockColumn = new TableColumn<>("Stock");
        stockColumn.setCellValueFactory(cellData -> cellData.getValue().stockProperty().asObject());
        stockColumn.setPrefWidth(70);
        stockColumn.getStyleClass().add("column");


        productTable.getColumns().addAll(nameColumn, brandColumn, stockColumn);
        productTable.setStyle("-fx-pref-height: 380; -fx-pref-width :430");
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
        nameField.getStyleClass().add("text-field-account");
        TextField addressField = new TextField(warehouse.getAddress());
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
                Stage stage = new Stage();
                EditSuccess message = new EditSuccess();
                message.start(stage);
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
        dialogScene.getStylesheets().add(getClass().getResource("/view/popup.css").toExternalForm());
        dialog.setScene(dialogScene);
        dialog.show();
    }

    private void deleteWarehouse(Warehouse warehouse) {
        if (warehouseController.deleteWarehouse(warehouse.getId())) {
            Stage stage = new Stage();
            DeletionSuccess message = new DeletionSuccess();
            message.start(stage);
            loadWarehouses();
        } else {
            Stage stage = new Stage();
            DeletionFailed message = new DeletionFailed();
            message.start(stage);
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
