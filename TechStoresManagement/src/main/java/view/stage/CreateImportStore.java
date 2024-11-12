package view.stage;

import controller.Session;
import entity.Product;
import entity.Store;
import entity.Warehouse;
import javafx.application.Application;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import model.ImportInvoiceModel;
import view.buttondashboardstore.ImportView;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class CreateImportStore extends Application {
    public static void main(String[] args) {
        launch(args);
    }
    private final int idStore = Session.getIdStore();

    private final ListView<HBox> orderListView = new ListView<>();
    private final Label totalLabel = new Label("Total:                               $0.00");
    private double totalSalePrice = 0;
    private final Map<Integer, Integer> cartItems = new HashMap<>();
    ImportInvoiceModel cm = new ImportInvoiceModel();

    // Data cho TableView
    private final ObservableList<Product> productData = FXCollections.observableArrayList();

    // Thêm thuộc tính productTable
    private final TableView<Product> productTable = new TableView<>();

    private String selectedWarehouseName;

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10, 50, 10, 50));

        // Tạo ComboBox cho Warehouse
        ComboBox<Warehouse> warehouseComboBox = new ComboBox<>();
        ObservableList<Warehouse> warehouseList = FXCollections.observableArrayList(ImportInvoiceModel.getAllWarehouses());
        warehouseComboBox.setItems(warehouseList);
        warehouseComboBox.setPromptText("Select Warehouse");


        warehouseComboBox.getStyleClass().add("combo-box-account");

        DatePicker datePicker = new DatePicker();
        datePicker.getStyleClass().add("text-field-account");

        // Biến để lưu ngày đã chọn
        AtomicReference<LocalDate> selectedDate = new AtomicReference<>();

        // Xử lý sự kiện khi người dùng chọn ngày
        datePicker.setOnAction(event -> {
            selectedDate.set(datePicker.getValue()); // Lấy giá trị ngày đã chọn
            System.out.println("Selected date: " + selectedDate); // In ra ngày đã chọn
        });
        // Khai báo các trường nhập liệu
        TextField inputField = new TextField();
        inputField.getStyleClass().add("text-field-account");
        inputField.setMinHeight(45);
        inputField.setPromptText("Name");

        HBox top = new HBox(30);
        top.setAlignment(Pos.CENTER_LEFT);
        top.getChildren().addAll(inputField, warehouseComboBox, datePicker);
        top.setPadding(new Insets(0, 10, 10, 10));

        HBox contentSection = new HBox(10);
        contentSection.setPadding(new Insets(10, 10, 10, 10));
        contentSection.setAlignment(Pos.CENTER_LEFT);
        TextField searchField = new TextField();
        searchField.setPromptText("Search product...");
        searchField.getStyleClass().add("search-field");
        searchField.setPrefWidth(600);
        searchField.setPrefHeight(30);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterProducts(newValue);
        });


        VBox table = new VBox(10);
        table.setPadding(new Insets(20));


        productTable.setPrefWidth(700);
        productTable.setStyle("-fx-background-color: #FFFFFF");
        productTable.setFixedCellSize(52);
        productTable.setPrefHeight(530);
        productTable.setMinHeight(530);
        productData.addAll(cm.getAll()); // Giả sử cm là một instance của ImportInvoiceModel và getAll() trả về danh sách sản phẩm
        productTable.setItems(productData); // Gán danh sách sản phẩm vào bảng

        HBox.setHgrow(productTable, Priority.ALWAYS);
        productTable.setMaxWidth(Double.MAX_VALUE);

        // Các cột của TableView
        TableColumn<Product, Integer> idColumn = new TableColumn<>("No.");
        idColumn.getStyleClass().add("column");
        idColumn.setPrefWidth(50);
        idColumn.setCellValueFactory(cellData -> {
            int rowIndex = productTable.getItems().indexOf(cellData.getValue());
            return new SimpleObjectProperty<>(rowIndex + 1);
        });


        TableColumn<Product, HBox> nameColumn = new TableColumn<>("Name");
        nameColumn.getStyleClass().add("column");
        nameColumn.setPrefWidth(230);
        nameColumn.setCellValueFactory(cellData -> {
            Product product = cellData.getValue();
            Label nameLabel = new Label(product.getName());
            HBox hBox = new HBox(10);
            hBox.setAlignment(Pos.CENTER_LEFT);
            hBox.getChildren().addAll(nameLabel);
            return new SimpleObjectProperty<>(hBox);
        });

        TableColumn<Product, String> brandColumn = new TableColumn<>("Brand");
        brandColumn.getStyleClass().add("column");
        brandColumn.setPrefWidth(160);
        brandColumn.setCellValueFactory(cellData -> cellData.getValue().brandProperty());


        TableColumn<Product, Double> purchasePriceColumn = new TableColumn<>("PurchasePrice");
        purchasePriceColumn.getStyleClass().add("column");
        purchasePriceColumn.setPrefWidth(150);
        purchasePriceColumn.setCellValueFactory(cellData -> cellData.getValue().purchasePriceProperty().asObject());

        TableColumn<Product, HBox> actionColumn = new TableColumn<>("Action");
        actionColumn.getStyleClass().add("column");
        actionColumn.setPrefWidth(125);
        actionColumn.setCellValueFactory(cellData -> {
            Product product = cellData.getValue();
            HBox actionBox = new HBox(5);
            actionBox.setAlignment(Pos.CENTER_LEFT);// HBox để chứa các nút
            Button addButton = new Button("Add");
            addButton.getStyleClass().add("button-pagination");
            Button viewDetailsButton = new Button("View");
            viewDetailsButton.getStyleClass().add("button-pagination");

            addButton.setOnAction(e -> {
                int productId = product.getId();
                double productPurchasePrice = product.getPurchasePrice();
                int currentQuantity = cartItems.getOrDefault(productId, 0);

                cartItems.put(productId, currentQuantity + 1);
                totalSalePrice += productPurchasePrice;
                totalLabel.setText("Total:                               $" + String.format("%.2f", totalSalePrice));
                updateOrderListView();
            });

            // Xử lý sự kiện khi nhấn nút "View Details"
            viewDetailsButton.setOnAction(e -> {
                showProductDetails(product);  // Hiển thị chi tiết sản phẩm
            });

            actionBox.getChildren().addAll(addButton, viewDetailsButton);
            return new SimpleObjectProperty<>(actionBox);
        });


        productTable.getColumns().addAll(idColumn, nameColumn, brandColumn, purchasePriceColumn, actionColumn);
        // Load dữ liệu vào bảng sản phẩm và khởi tạo phân trang


        // Phần bên phải: Order summary

        VBox orderSummary = new VBox();
        orderSummary.setPadding(new Insets(10, 10, 10, 10));
        orderSummary.setSpacing(10);
        orderSummary.setStyle("-fx-background-color: #FFFFFF ;");
        orderSummary.setMaxWidth(500);
        VBox.setVgrow(orderListView, Priority.ALWAYS);

        totalLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16;");

        Label orderDetailLabel = new Label("Order Detail");
        orderDetailLabel.setId("order-detail-label");
        orderListView.setPrefHeight(200);
        orderListView.setFixedCellSize(50);
        orderListView.setId("order-list-view");
        orderListView.setMaxHeight(Double.MAX_VALUE);
        HBox.setHgrow(orderSummary, Priority.ALWAYS);

        VBox buyNowContainer = new VBox();
        buyNowContainer.setAlignment(Pos.CENTER);
        Button buyNowButton = new Button("Submit");
        buyNowButton.getStyleClass().add("button-pagination");
        buyNowButton.setStyle("-fx-text-alignment: center; ");


        warehouseComboBox.setOnAction(event -> {
            Warehouse selectedWarehouse = warehouseComboBox.getSelectionModel().getSelectedItem();
            if (selectedWarehouse != null) {
                // Gán tên kho vào biến String
                selectedWarehouseName = selectedWarehouse.getName();
            }
        });

        buyNowButton.setOnAction(e -> {
            if (!cartItems.isEmpty()) {
                if (ImportInvoiceModel.createImportInvoiceStore(inputField.getText(), selectedWarehouseName, cm.getStoreNameById(idStore), totalSalePrice, selectedDate.toString(), cartItems) == -1) {
                    System.out.println("Error");
                } else {
                    System.out.println("Submit");
                    primaryStage.close();

                }
            }
        });

        buyNowContainer.getChildren().add(buyNowButton);

// Thêm các thành phần vào orderSummary
        orderSummary.getChildren().addAll(orderDetailLabel, orderListView, totalLabel, buyNowContainer);
        VBox outerVbox = new VBox();
        outerVbox.setPadding(new Insets(0, 0, 0, 0));  // Set the desired margin from top
        outerVbox.getChildren().add(orderSummary);
        outerVbox.setMaxWidth(500);
        outerVbox.setMinHeight(600);
        outerVbox.setPrefWidth(500);
        table.getChildren().addAll(searchField, productTable);
        table.setMinHeight(600);
        table.setStyle("-fx-background-color: white;");
        contentSection.getChildren().addAll(table, outerVbox);

        // Tạo VBox chính để đặt các phần thành từng lớp
        VBox mainContent = new VBox(10); // VBox chính để xếp các phần theo chiều dọc
        mainContent.getChildren().addAll(contentSection);

        // Đặt mainContent vào trung tâm của BorderPane
        root.setCenter(mainContent);
        root.setTop(top);

        orderListView.setPrefHeight(500); // Chiều cao tối đa cho danh sách đơn hàng
        orderListView.setMaxHeight(500);

        // Scene và Stage
        Scene scene = new Scene(root, 1366, 768);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/view/cashier.css")).toExternalForm());
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/view/popup.css")).toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setTitle("Create Import");
        primaryStage.setResizable(false);
        primaryStage.setWidth(1366);
        primaryStage.setHeight(768);
        primaryStage.show();

    }

    private void filterProducts(String searchTerm) {
        ObservableList<Product> filteredProducts = FXCollections.observableArrayList();

        for (Product product : cm.getAll()) { // Giả sử cm.getAll() trả về danh sách sản phẩm
            if (product.getName().toLowerCase().contains(searchTerm.toLowerCase())) {
                filteredProducts.add(product);
            }
        }

        productTable.setItems(filteredProducts);
    }

    private void showProductDetails(Product product) {
        // Tạo một cửa sổ mới (dialog box)
        Stage dialog = new Stage();
        dialog.setTitle("Product Details");

        // Tạo một VBox để chứa các thành phần
        VBox vbox = new VBox();

        HBox titleBox = new HBox();
        Label titleLabel = new Label(product.getName());
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #4AD4DD;");
        titleBox.getChildren().addAll(titleLabel);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        titleBox.setSpacing(10); // Image
        VBox mainLayout = new VBox();
        mainLayout.setPadding(new Insets(20));
        mainLayout.setSpacing(20);

// Create a HBox to hold the image and details
        HBox hbox = new HBox();
        hbox.setSpacing(20);

// Add the image to the left
        ImageView imageView = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/view/images/" + product.getImage()))));
        imageView.setFitWidth(200);
        imageView.setFitHeight(200);
        hbox.getChildren().add(imageView);

// Create a VBox to hold the details
        VBox detailsBox = new VBox();
        detailsBox.setSpacing(10);

// Add the price, brand, and quantity to the details box
        GridPane detailsGrid = new GridPane();
        detailsGrid.setHgap(10);
        detailsGrid.setVgap(10);

        Label priceLabel = new Label("Price:");
        Label priceValue = new Label(String.format("%.2f", product.getPurchasePrice()) + "$");
        priceValue.setStyle("-fx-text-fill: #4AD4DD; -fx-font-size: 16px; -fx-font-weight: bold;");
        detailsGrid.add(priceLabel, 0, 0);
        detailsGrid.add(priceValue, 1, 0);
        // Add the sale price, discount, and tax to the details box
        Label brandLabel = new Label("Brand:");
        Label brandValue = new Label(product.getBrand());
        brandValue.setStyle("-fx-text-fill: #4AD4DD; -fx-font-size: 16px; -fx-font-weight: bold;");
        detailsGrid.add(brandLabel, 0, 1);
        detailsGrid.add(brandValue, 1, 1);

        Label quantityLabel = new Label("Quantity:");
        Label quantityValue = new Label("1");
        quantityValue.setStyle("-fx-text-fill: #4AD4DD; -fx-font-size: 16px; -fx-font-weight: bold;");

        Button decrementButton = new Button("-");
        decrementButton.getStyleClass().add("button-pagination");  // Thêm style class mặc định cho nút
        ;
        decrementButton.setOnAction(e -> {
            int currentValue = Integer.parseInt(quantityValue.getText());
            if (currentValue > 1) {
                quantityValue.setText(String.valueOf(currentValue - 1));
            }
        });

        Button incrementButton = new Button("+");
        incrementButton.getStyleClass().add("button-pagination");  // Thêm style class mặc đ��nh cho nút
        incrementButton.setOnAction(e -> {
            int currentValue = Integer.parseInt(quantityValue.getText());
            quantityValue.setText(String.valueOf(currentValue + 1));
        });

        HBox quantityBox = new HBox();
        quantityBox.getChildren().addAll(decrementButton, quantityValue, incrementButton);
        quantityBox.setSpacing(15);

        detailsGrid.add(quantityLabel, 0, 2);
        detailsGrid.add(quantityBox, 1, 2);
        detailsBox.getChildren().add(detailsGrid);

// Add the details box to the right of the image
        hbox.getChildren().add(detailsBox);

        HBox buttonBox = new HBox();
        Button skipButton = new Button("Skip");
        skipButton.setStyle("-fx-background-color: lightgray;");
        Button doneButton = new Button("Done");
        doneButton.getStyleClass().add("button-pagination");  // Thêm style class mặc định cho nút
        ;
        buttonBox.getChildren().addAll(skipButton, doneButton);
        buttonBox.setSpacing(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
// Add the title box and button box to the main layout
        mainLayout.getChildren().addAll(titleBox, hbox, buttonBox);
        doneButton.setOnAction(e -> {
            int productId = product.getId();
            int quantity = Integer.parseInt(quantityValue.getText());
            double productSalePrice = product.getPurchasePrice();

            cartItems.put(productId, quantity);
            totalSalePrice += productSalePrice * quantity;
            totalLabel.setText("Total:                               $" + String.format("%.2f", totalSalePrice));
            updateOrderListView();
            // Cập nhật bảng sản phẩm sau khi thêm vào giỏ

            dialog.close(); // Đóng cửa sổ chi tiết sản phẩm
        });

        // Sự kiện khi nhấn nút "Skip"
        skipButton.setOnAction(e -> {
            dialog.close(); // Đóng cửa sổ chi tiết sản phẩm
        });

        vbox.getChildren().add(mainLayout);
        // Tạo Scene và hiển thị cửa sổ
        Scene scene = new Scene(vbox);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/view/cashier.css")).toExternalForm());
        dialog.setScene(scene);
        dialog.setWidth(600);
        dialog.setHeight(400);
        dialog.show();
    }

    private void updateOrderListView() {
        orderListView.getItems().clear();
        for (Map.Entry<Integer, Integer> entry : cartItems.entrySet()) {
            String itemName = cm.getOne(entry.getKey()).getName();
            final int[] quantity = {entry.getValue()};
            double productPrice = cm.getOne(entry.getKey()).getPurchasePrice();
            double totalPriceForItem = productPrice * quantity[0]; // Tính tổng tiền cho sản phẩm

            HBox orderItem = new HBox(15); // Thêm khoảng cách giữa các phần tử

            Label itemNameLabel = new Label(itemName);
            itemNameLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold;"); // Đổi kích thước chữ

            // Tạo nút giảm số lượng
            Button decreaseButton = new Button();
            decreaseButton.getStyleClass().add("decrease-button"); // Đảm bảo đã thêm class CSS
            Label decreaseLabel = new Label("\u2212");
            decreaseLabel.setStyle("-fx-text-fill: #ffffff;"); // Đổi màu chữ sang xanh
            decreaseButton.setGraphic(decreaseLabel);
            decreaseButton.setOnAction(e -> {
                if (quantity[0] > 1) {
                    quantity[0]--;
                    cartItems.put(entry.getKey(), quantity[0]);
                    totalSalePrice -= productPrice; // Update total price
                    totalLabel.setText("Total:                               $" + String.format("%.2f", totalSalePrice));
                    updateOrderListView();
                    // Update the product table after decreasing quantity
                }
            });

            // Tạo nút tăng số lượng
            Button increaseButton = new Button();
            increaseButton.getStyleClass().add("increase-button"); // Đảm bảo đã thêm class CSS
            Label increaseLabel = new Label("\u002B");
            increaseLabel.setStyle("-fx-text-fill: #ffffff;"); // Đổi màu chữ sang xanh
            increaseButton.setGraphic(increaseLabel);
            increaseButton.setOnAction(e -> {
                int currentStock = cm.getOne(entry.getKey()).getStock();
                if (quantity[0] < currentStock) {
                    quantity[0]++;
                    cartItems.put(entry.getKey(), quantity[0]);
                    totalSalePrice += productPrice; // Update total price
                    totalLabel.setText("Total:                               $" + String.format("%.2f", totalSalePrice));
                    updateOrderListView();
                    // Update the product table after increasing quantity
                } else {
                    showStockAlert(itemName);
                }
            });

            // Thêm label hiển thị số lượng
            Label quantityLabel = new Label(String.valueOf(quantity[0]));
            quantityLabel.setStyle("-fx-font-size: 13;"); // Đổi kích thước chữ

            // Thêm label hiển thị tổng tiền cho sản phẩm
            Label totalPriceLabel = new Label(String.format("$%.2f", totalPriceForItem));
            totalPriceLabel.setStyle("-fx-font-size: 13; -fx-text-fill: #4AD4DD;"); // Đổi màu chữ

            // Tạo nút xóa sản phẩm
            Button removeButton = new Button("\u2716"); // Biểu tượng "x"
            removeButton.setStyle("-fx-font-size: 10; -fx-text-fill: red;-fx-background-color: #FFFFFF;-fx-border-color: #4AD4DD;-fx-border-radius: 100px;");
            removeButton.setOnAction(e -> {
                totalSalePrice -= totalPriceForItem; // Update total price
                cartItems.remove(entry.getKey());
                totalLabel.setText("Total:                               $" + String.format("%.2f", totalSalePrice));
                updateOrderListView(); // Update order list
                // Update the product table after removing item
            });

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS); // Đẩy các phần tử tiếp theo sang phải

            // Thêm các thành phần vào HBox
            orderItem.getChildren().addAll(itemNameLabel, spacer, decreaseButton, quantityLabel, increaseButton, totalPriceLabel, removeButton);
            orderListView.getItems().add(orderItem);

            orderItem.setOnMouseClicked(event -> {
                // Xóa kiểu đã chọn cho tất cả các hàng
                for (HBox item : orderListView.getItems()) {
                    item.setStyle("");
                    for (Node node : orderItem.getChildren()) {
                        if (node instanceof Label) {
                            ((Label) node).setTextFill(Color.web("#4AD4DD"));
                        }
                    }
                }
            });
        }
    }

    private void showStockAlert(String productName) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText("Stock Warning");
        alert.setContentText("The product " + productName + " is out of stock.");
        alert.showAndWait();
    }
}
