package view;

import entity.Product;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import model.CashierModel;
import javafx.application.Application;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Cashier extends Application {

    public static void main(String[] args) {
        launch(args);
    }
//    private int storeId;
//
//    public Cashier() {
//    }
//
//    public Cashier(int storeId) {
//        this.storeId = storeId;
//    }
    private int currentPage = 1;
    private final int itemsPerPage = 10;
    private int totalPages;
    private Label pageLabel = new Label();
    private ListView<HBox> orderListView = new ListView<>();
    private Label totalLabel = new Label("Total:                                                                              $0.00");
    private double totalPrice = 0;
    private Map<Integer, Integer> cartItems = new HashMap<>();
    CashierModel cm = new CashierModel();

    // Data cho TableView
    private ObservableList<Product> productData = FXCollections.observableArrayList();

    // Thêm thuộc tính productTable
    private TableView<Product> productTable = new TableView<>();

    @Override
    public void start(Stage primaryStage) {
//        if (!Session.isLoggedIn()) {
//            try {
//                new Login().start(new Stage());
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            primaryStage.close();
//            return;
//        }
//        System.out.println("Logged in as Cashier of store: " + storeId);
        HBox root = new HBox();
        root.setPadding(new Insets(10, 50, 10, 50));
        root.setSpacing(20);


        productTable.setPrefWidth(750);
        productTable.setStyle("-fx-background-color: #FFFFFF");
        productTable.setFixedCellSize(52);
        productTable.setPrefHeight(550);
        productTable.setMaxHeight(550);

        HBox.setHgrow(productTable, Priority.ALWAYS);
        productTable.setMaxWidth(Double.MAX_VALUE);

        // Các cột của TableView
        TableColumn<Product, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setPrefWidth(40);
        idColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());

        TableColumn<Product, HBox> nameColumn = new TableColumn<>("Name");
        nameColumn.setPrefWidth(230);
        nameColumn.setCellValueFactory(cellData -> {
            Product product = cellData.getValue();
            ImageView imageView = product.getImage();
            Label nameLabel = new Label(product.getName());
            HBox hBox = new HBox(10);
            hBox.getChildren().addAll(imageView, nameLabel);
            return new SimpleObjectProperty<>(hBox);
        });

        TableColumn<Product, String> brandColumn = new TableColumn<>("Brand");
        brandColumn.setPrefWidth(140);
        brandColumn.setCellValueFactory(cellData -> cellData.getValue().brandProperty());

        TableColumn<Product, Integer> stockColumn = new TableColumn<>("Stock");
        stockColumn.setPrefWidth(100);
        stockColumn.setCellValueFactory(cellData -> cellData.getValue().stockProperty().asObject());

        TableColumn<Product, Double> priceColumn = new TableColumn<>("Price");
        priceColumn.setPrefWidth(130);
        priceColumn.setCellValueFactory(cellData -> cellData.getValue().priceProperty().asObject());

        TableColumn<Product, Button> actionColumn = new TableColumn<>("Action");
        actionColumn.setPrefWidth(90);
        actionColumn.setCellValueFactory(cellData -> {
            Button addButton = new Button("Add to Cart");
            addButton.setOnAction(e -> {
                Product product = cellData.getValue();
                int productId = product.getId();
                double productPrice = product.getPrice();
                int stockQuantity = product.getStock();
                int currentQuantity = cartItems.getOrDefault(productId, 0);

                if (currentQuantity < stockQuantity) {
                    cartItems.put(productId, currentQuantity + 1);
                    totalPrice += productPrice;
                    totalLabel.setText("Total:                                                                              $" +  String.format("%.2f", totalPrice));
                    updateOrderListView();
                } else {
                    showStockAlert(cm.getOne(productId).getName());
                }
            });
            return new SimpleObjectProperty<>(addButton);
        });

        productTable.getColumns().addAll(idColumn, nameColumn, brandColumn, stockColumn, priceColumn, actionColumn);
        // Load dữ liệu vào bảng sản phẩm và khởi tạo phân trang
        loadData();
        // Tạo thanh tìm kiếm
        TextField searchField = new TextField();
        searchField.setPromptText("Search product...");
        searchField.getStyleClass().add("search-field");
        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterProductList(productTable, newValue, null));

        // Tạo danh sách các button filter
        String[] filterCategories = cm.getAllCategories().toArray(new String[0]);
        String[] allCategories = new String[filterCategories.length + 1];
        allCategories[0] = "All";
        System.arraycopy(filterCategories, 0, allCategories, 1, filterCategories.length);

        // Tạo một HBox để chứa các button filter
        HBox filterBox = new HBox(17);
        filterBox.setPadding(new Insets(20,10,5,30));

        // Tạo từng button filter và thêm vào HBox
        for (String category : allCategories) {
            Button filterButton = new Button(category);
            filterButton.getStyleClass().add("category-button");  // Thêm style class mặc định cho nút

            // Nếu category là "All", đặt sáng nút này khi khởi động
            if (category.equals("All")) {
                filterButton.getStyleClass().add("selected");
            }

            filterButton.setOnAction((e) -> {
                updateTableData();
                pageLabel.setText("Page " + currentPage + " / " + totalPages);
                // Loại bỏ class "selected" khỏi tất cả các nút category
                filterBox.getChildren().forEach(node -> {
                    if (node instanceof Button) {
                        node.getStyleClass().remove("selected");
                    }
                });

                // Thêm class "selected" cho nút vừa được nhấn
                filterButton.getStyleClass().add("selected");

                // Thực hiện hành động lọc sản phẩm theo category
                this.filterProductList(this.productTable, searchField.getText(), category.equals("All") ? null : category);
            });

            filterBox.getChildren().add(filterButton);
        }

        // Thay thế ComboBox bằng HBox chứa các button filter
        Button prevButton = new Button("<-");
        prevButton.getStyleClass().add("category-button");
        Button nextButton = new Button("->");
        nextButton.getStyleClass().add("category-button");
        Label pageLabel = new Label("Page " + currentPage + " / " + totalPages);
        pageLabel.setStyle("-fx-text-fill: #4AD4DD;-fx-font-weight: bold; -fx-font-size: 14; ");

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
        VBox tableContainer = new VBox(10, searchField, filterBox, productTable, paginationBox);


        // Phần bên phải: Order summary

        VBox orderSummary = new VBox();
        orderSummary.setPadding(new Insets(10,10,10,10));
        orderSummary.setSpacing(10);
        orderSummary.setStyle("-fx-background-color: #FFFFFF ;");
        orderSummary.setMaxWidth(600);
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
        Button buyNowButton = new Button("Buy Now");
        buyNowButton.getStyleClass().add("category-button");
        buyNowButton.setStyle("-fx-text-alignment: center; ");

        buyNowButton.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation");
            alert.setHeaderText("Confirm Purchase");
            alert.setContentText("Are you sure you want to proceed with the purchase?");
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    for (Map.Entry<Integer, Integer> entry : cartItems.entrySet()) {
                        int productId = entry.getKey();
                        int quantity = entry.getValue();
                        cm.handlePurchase(productId, quantity);
                    }
                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Purchase Successful");
                    successAlert.setContentText("Thank you for your purchase!");
                    successAlert.showAndWait();
                    cartItems.clear();
                    totalPrice = 0;
                    totalLabel.setText("Total:                                                                              $0.00");
                    updateOrderListView();
                    productTable.getItems().clear();
                }
            });
        });


        buyNowContainer.getChildren().add(buyNowButton);

// Thêm các thành phần vào orderSummary
        orderSummary.getChildren().addAll(orderDetailLabel, orderListView, totalLabel, buyNowContainer);
        VBox outerVbox = new VBox();
        outerVbox.setPadding(new Insets(100, 0, 0, 0));  // Set the desired margin from top
        outerVbox.getChildren().add(orderSummary);
        outerVbox.setMaxWidth(500);
        outerVbox.setPrefWidth(500);

        root.getChildren().addAll(tableContainer, outerVbox);

        orderListView.setPrefHeight(550); // Chiều cao tối đa cho danh sách đơn hàng
        orderListView.setMaxHeight(550);

        // Scene và Stage
        Scene scene = new Scene(root, 1366, 768);
        scene.getStylesheets().add(getClass().getResource("cashier.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setTitle("Product Order App");
        primaryStage.setResizable(false);
        primaryStage.setWidth(1366);
        primaryStage.setHeight(768);
        primaryStage.show();

        Platform.runLater(() -> {
            productTable.requestFocus();  // Focus vào bảng sản phẩm khi ứng dụng khởi động
            searchField.setFocusTraversable(false);  // Tắt khả năng focus tự động của ô tìm kiếm
            filterBox.getChildren().forEach(node -> node.setFocusTraversable(false)); // Tắt khả năng focus cho các nút filter
        });

        // Load dữ liệu vào bảng sản phẩm
//        CashierModel.loadData(productTable, 1);
    }

    // Hàm lọc sản phẩm dựa trên từ khóa tìm kiếm và loại sản phẩm
    private ObservableList<Product> filteredProductData = FXCollections.observableArrayList(); // New filtered list

    private void loadData() {
        productData.setAll(cm.getAll()); // Load all products into the original data list
        filteredProductData.setAll(productData); // Initialize the filtered list with all products
        totalPages = (int) Math.ceil((double) filteredProductData.size() / itemsPerPage); // Calculate total pages based on filtered data
        updateTableData(); // Update the displayed data
    }

    private void filterProductList(TableView<Product> productTable, String searchKeyword, String filterCategory) {
        // Filter the product data based on the search keyword and category
        filteredProductData.setAll(cm.getAll().stream()
                .filter(product -> (searchKeyword == null || product.getName().toLowerCase().contains(searchKeyword.toLowerCase())) &&
                        (filterCategory == null || filterCategory.equals("All") || product.getCategory().equals(filterCategory)))
                .collect(Collectors.toList()));

        // Reset to first page
        currentPage = 1;

        // Update the total pages based on the filtered results
        totalPages = (int) Math.ceil((double) filteredProductData.size() / itemsPerPage);

        // Update the table with the filtered data
        updateTableData();

        // Update the pagination label
        pageLabel.setText("Page " + currentPage + " / " + totalPages);
    }
    private void updateTableData() {
        int fromIndex = (currentPage - 1) * itemsPerPage;
        int toIndex = Math.min(fromIndex + itemsPerPage, filteredProductData.size());

        if (fromIndex >= filteredProductData.size()) {
            return; // No items to show
        }

        productTable.setItems(FXCollections.observableArrayList(filteredProductData.subList(fromIndex, toIndex)));
        pageLabel.setText("Page " + currentPage + " / " + totalPages);
    }



    private void updateOrderListView() {
        orderListView.getItems().clear();
        for (Map.Entry<Integer, Integer> entry : cartItems.entrySet()) {
            String itemName = cm.getOne(entry.getKey()).getName();
            final int[] quantity = {entry.getValue()};
            double productPrice = cm.getOne(entry.getKey()).getPrice();
            double totalPriceForItem = productPrice * quantity[0]; // Tính tổng tiền cho sản phẩm

            HBox orderItem = new HBox(15); // Thêm khoảng cách giữa các phần tử

            Label itemNameLabel = new Label(itemName);
            itemNameLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold;"); // Đổi kích thước chữ

            // Tạo nút giảm số lượng
            Button decreaseButton = new Button();
            decreaseButton.getStyleClass().add("decrease-button"); // Đảm bảo đã thêm class CSS
            Label decreaseLabel = new Label("\u2212");
            decreaseLabel.setStyle("-fx-text-fill: #4AD4DD;"); // Đổi màu chữ sang xanh
            decreaseButton.setGraphic(decreaseLabel);
            decreaseButton.setOnAction(e -> {
                if (quantity[0] > 1) {
                    quantity[0]--;
                    cartItems.put(entry.getKey(), quantity[0]);
                    totalPrice -= productPrice; // Cập nhật tổng giá
                    totalLabel.setText("Total:                                                                              $" + String.format("%.2f", totalPrice));
                    updateOrderListView();
                }
            });

            // Tạo nút tăng số lượng
            Button increaseButton = new Button();
            increaseButton.getStyleClass().add("increase-button"); // Đảm bảo đã thêm class CSS
            Label increaseLabel = new Label("\u002B");
            increaseLabel.setStyle("-fx-text-fill: #4AD4DD;"); // Đổi màu chữ sang xanh
            increaseButton.setGraphic(increaseLabel);
            increaseButton.setOnAction(e -> {
                int currentStock = cm.getOne(entry.getKey()).getStock();
                if (quantity[0] < currentStock) {
                    quantity[0]++;
                    cartItems.put(entry.getKey(), quantity[0]);
                    totalPrice += productPrice; // Cập nhật tổng giá
                    totalLabel.setText("Total:                                                                              $" + String.format("%.2f", totalPrice));
                    updateOrderListView();
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
                totalPrice -= totalPriceForItem; // Cập nhật tổng giá
                cartItems.remove(entry.getKey());
                totalLabel.setText("Total:                                                                              $" + String.format("%.2f", totalPrice));
                updateOrderListView(); // Cập nhật danh sách đơn hàng
            });

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS); // Đẩy các phần tử tiếp theo sang phải

            // Thêm các thành phần vào HBox
            orderItem.getChildren().addAll(itemNameLabel, spacer, decreaseButton, quantityLabel, increaseButton, totalPriceLabel,removeButton);
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
