package view;

import entity.Product;
import javafx.application.Platform;
import javafx.scene.Node;
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
import javafx.scene.control.Pagination;
import org.w3c.dom.ls.LSOutput;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Cashier extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    private ListView<HBox> orderListView = new ListView<>();
    private Label totalLabel = new Label("Total: 0$");
    private double totalPrice = 0;
    private Map<Integer, Integer> cartItems = new HashMap<>();
    private final static int rowsPerPage = 10;
    static CashierModel cm = new CashierModel();
    private final TableView<Product> productTable = createTable();
    private final static int dataSize = cm.getAll().size();

    private ObservableList<Product> currentProductList = FXCollections.observableArrayList(cm.getAll());

    // Thêm thuộc tính productTable

    @Override
    public void start(Stage primaryStage) {
        HBox root = new HBox();
        root.setPadding(new Insets(10));
        root.setSpacing(20);

        Pagination pagination = new Pagination((int) Math.ceil((double) currentProductList.size() / rowsPerPage), 0);
        pagination.setPageFactory(this::createPage);

        // Tạo thanh tìm kiếm
        TextField searchField = new TextField();
        searchField.setPromptText("Search product...");
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterProductList(newValue, null);
            updatePagination(pagination);
        });

        // Tạo danh sách các button filter
        String[] filterCategories = cm.getAllCategories().toArray(new String[0]);
        String[] allCategories = new String[filterCategories.length + 1];
        allCategories[0] = "All";
        System.arraycopy(filterCategories, 0, allCategories, 1, filterCategories.length);

        // Tạo một HBox để chứa các button filter
        HBox filterBox = new HBox(10);
        filterBox.setPadding(new Insets(10));

        // Tạo từng button filter và thêm vào HBox
        for (String category : allCategories) {
            Button filterButton = new Button(category);
            filterButton.setOnAction(e -> {
                filterProductList(searchField.getText(), category.equals("All") ? null : category);
                updatePagination(pagination);
            });
            filterBox.getChildren().add(filterButton);
        }

        // Thay thế ComboBox bằng HBox chứa các button filter
        VBox tableContainer = new VBox(10, searchField, filterBox, pagination);

        // Phần bên phải: Order summary
        VBox orderSummary = new VBox();
        orderSummary.setPadding(new Insets(10));
        orderSummary.setSpacing(10);

        Label orderDetailLabel = new Label("Order Detail");
        orderListView.setPrefHeight(200);
        Button buyNowButton = new Button("Buy Now");

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
                    totalLabel.setText("Total: $0.00");
                    updateOrderListView();
                    productTable.getItems().clear();
                    CashierModel.loadData(productTable, 1);
                }
            });
        });

        orderSummary.getChildren().addAll(orderDetailLabel, orderListView, totalLabel, buyNowButton);

        root.getChildren().addAll(tableContainer, orderSummary);

        // Scene và Stage
        Scene scene = new Scene(root, 1366, 768);
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
    }

    private TableView<Product> createTable() {
        TableView<Product> productTable = new TableView<>();

        productTable.setPrefWidth(630);

        // Các cột của TableView
        TableColumn<Product, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setPrefWidth(30);
        idColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());

        TableColumn<Product, HBox> nameColumn = new TableColumn<>("Name");
        nameColumn.setPrefWidth(150);
        nameColumn.setCellValueFactory(cellData -> {
            Product product = cellData.getValue();
            ImageView imageView = product.getImage();
            Label nameLabel = new Label(product.getName());
            HBox hBox = new HBox(10);
            hBox.getChildren().addAll(imageView, nameLabel);
            return new SimpleObjectProperty<>(hBox);
        });

        TableColumn<Product, String> brandColumn = new TableColumn<>("Brand");
        brandColumn.setPrefWidth(100);
        brandColumn.setCellValueFactory(cellData -> cellData.getValue().brandProperty());

        TableColumn<Product, Integer> stockColumn = new TableColumn<>("Stock");
        stockColumn.setPrefWidth(100);
        stockColumn.setCellValueFactory(cellData -> cellData.getValue().stockProperty().asObject());

        TableColumn<Product, Double> priceColumn = new TableColumn<>("Price");
        priceColumn.setPrefWidth(130);
        priceColumn.setCellValueFactory(cellData -> cellData.getValue().priceProperty().asObject());

        TableColumn<Product, Button> actionColumn = new TableColumn<>("Action");
        actionColumn.setPrefWidth(120);
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
                    totalLabel.setText("Total: $" + String.format("%.2f", totalPrice));
                    updateOrderListView();
                } else {
                    showStockAlert(cm.getOne(productId).getName());
                }
            });
            return new SimpleObjectProperty<>(addButton);
        });

        productTable.getColumns().addAll(idColumn, nameColumn, brandColumn, stockColumn, priceColumn, actionColumn);

        return productTable;
    }

    private Node createPage(int pageIndex) {
        int fromIndex = pageIndex * rowsPerPage;
        int toIndex = Math.min(fromIndex + rowsPerPage, currentProductList.size());
        productTable.setItems(FXCollections.observableArrayList(currentProductList.subList(fromIndex, toIndex)));

        return new BorderPane(productTable);
    }

    private void updatePagination(Pagination pagination) {
        pagination.setPageCount((int) Math.ceil((double) currentProductList.size() / rowsPerPage));
        pagination.setCurrentPageIndex(0);
        pagination.setPageFactory(this::createPage);
    }

    // Hàm lọc sản phẩm dựa trên từ khóa tìm kiếm và loại sản phẩm
    private void filterProductList(String searchKeyword, String filterCategory) {
        currentProductList = cm.getAll().stream()
                .filter(product -> (searchKeyword == null || product.getName().toLowerCase().contains(searchKeyword.toLowerCase()))
                        && (filterCategory == null || filterCategory.equals("All") || product.getCategory().equals(filterCategory)))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
    }

    private void updateOrderListView() {
        orderListView.getItems().clear();
        for (Map.Entry<Integer, Integer> entry : cartItems.entrySet()) {
            String itemName = cm.getOne(entry.getKey()).getName();
            int quantity = entry.getValue();
            HBox itemBox = new HBox();
            Label nameLabel = new Label(itemName);
            Label quantityLabel = new Label("Quantity: " + quantity);
            Button increaseButton = new Button("+");
            Button decreaseButton = new Button("-");

            // Sự kiện cho nút tăng số lượng
            increaseButton.setOnAction(e -> {
                int stockQuantity = cm.getOne(entry.getKey()).getStock();
                if (quantity < stockQuantity) {
                    cartItems.put(entry.getKey(), quantity + 1);
                    totalPrice += getProductPrice(itemName);
                    totalLabel.setText("Total: $" + String.format("%.2f", totalPrice));
                    updateOrderListView();
                    Platform.runLater(() -> increaseButton.requestFocus()); // Giữ focus trên nút tăng
                } else {
                    showStockAlert(itemName);
                }
            });

// Sự kiện cho nút giảm số lượng
            decreaseButton.setOnAction(e -> {
                if (quantity > 1) {
                    cartItems.put(entry.getKey(), quantity - 1);
                    totalPrice -= getProductPrice(itemName);
                    totalLabel.setText("Total: $" + String.format("%.2f", totalPrice));
                    updateOrderListView();
                    Platform.runLater(() -> decreaseButton.requestFocus()); // Giữ focus trên nút giảm
                } else {
                    cartItems.remove(entry.getKey());
                    totalPrice -= getProductPrice(itemName);
                    totalLabel.setText("Total: $" + String.format("%.2f", totalPrice));
                    updateOrderListView();
                    Platform.runLater(() -> decreaseButton.requestFocus()); // Giữ focus trên nút giảm
                }
            });

            itemBox.getChildren().addAll(nameLabel, quantityLabel, increaseButton, decreaseButton);
            orderListView.getItems().add(itemBox);
        }
    }


    private double getProductPrice(String name) {
        for (Product product : cm.getAll()) {
            if (product.getName().equals(name)) {
                return product.getPrice();
            }
        }
        return 0;
    }

    private void showStockAlert(String productName) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Stock Alert");
        alert.setHeaderText("Out of Stock");
        alert.setContentText("Sorry, " + productName + " is out of stock.");
        alert.showAndWait();
    }
}
