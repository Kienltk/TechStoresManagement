package view;

import controller.CashierController;
import controller.Session;
import entity.Customer;
import entity.Product;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.image.Image;
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

import java.lang.Integer;
import java.util.*;
import java.util.stream.Collectors;

public class Cashier extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    private final int idStore;
    private final String employeeName;

    public Cashier() {
        this.idStore = Session.getIdStore();
        this.employeeName = Session.getEmployeeName();
    }

    private int currentPage = 1;
    private final int itemsPerPage = 9;
    private int totalPages;
    private final Label pageLabel = new Label();
    private final ListView<HBox> orderListView = new ListView<>();
    private final Label totalLabel = new Label("Total:                                                                        $0.00");
    private double totalSalePrice = 0;
    private String phoneNumber;
    private final Map<Integer, Integer> cartItems = new HashMap<>();
    CashierModel cm = new CashierModel();

    // Data cho TableView
    private final ObservableList<Product> productData = FXCollections.observableArrayList();

    // Thêm thuộc tính productTable
    private final TableView<Product> productTable = new TableView<>();

    @Override
    public void start(Stage primaryStage) {
        if (!Session.isLoggedIn()) {
            try {
                new Login().start(new Stage());
            } catch (Exception e) {
                e.printStackTrace();
            }
            primaryStage.close();
            return;
        }
        System.out.println("Logged in as: " + employeeName + " at store ID: " + idStore);


        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10, 50, 10, 50));

        // Tạo phần trên cùng chứa Search và Employee Box
        HBox topSection = new HBox(10); // HBox chứa Search và Employee Box
        topSection.setAlignment(Pos.CENTER_RIGHT); // Căn giữa và phải
        topSection.setPadding(new Insets(10, 10, 10, 10));

        // Tạo VBox cho tên nhân viên và nút Log Out
        VBox employeeBox = new VBox(5);
        employeeBox.setAlignment(Pos.TOP_RIGHT); // Canh phải cho cả VBox
        employeeBox.setPadding(new Insets(10, 10, 10, 10));

        // Tạo label hiển thị tên nhân viên
        Label employeeNameLabel = new Label(employeeName);
        employeeNameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #4AD4DD;");

        // Tạo nút Log Out, ban đầu ẩn
        Button logoutButton = new Button("Log Out");
        logoutButton.setVisible(false); // Ẩn nút Log Out lúc đầu
        logoutButton.setStyle("-fx-background-color: #4AD4DD; -fx-text-fill: white;");

        // Sự kiện khi nhấn vào tên nhân viên để hiển thị nút Log Out
        employeeNameLabel.setOnMouseClicked(event -> logoutButton.setVisible(!logoutButton.isVisible()));

        // Sự kiện khi nhấn vào nút Log Out
        logoutButton.setOnAction(event -> {
            Session.logout(); // Gọi phương thức đăng xuất
            primaryStage.close(); // Đóng cửa sổ hiện tại
            try {
                new Login().start(new Stage()); // Mở lại màn hình đăng nhập
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Thêm tên nhân viên và nút Log Out vào VBox employeeBox
        employeeBox.getChildren().addAll(employeeNameLabel, logoutButton);

        HBox contentSection = new HBox(10);
        contentSection.setPadding(new Insets(10, 10, 10, 10));
        contentSection.setAlignment(Pos.CENTER_LEFT);

        VBox table = new VBox();


        productTable.setPrefWidth(800);
        productTable.setStyle("-fx-background-color: #FFFFFF");
        productTable.setFixedCellSize(52);
        productTable.setPrefHeight(600);
        productTable.setMaxHeight(600);

        HBox.setHgrow(productTable, Priority.ALWAYS);
        productTable.setMaxWidth(Double.MAX_VALUE);

        // Các cột của TableView
        TableColumn<Product, Integer> idColumn = new TableColumn<>("ID");
        idColumn.getStyleClass().add("column");
        idColumn.setPrefWidth(40);
        idColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());

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
        brandColumn.setPrefWidth(140);
        brandColumn.setCellValueFactory(cellData -> cellData.getValue().brandProperty());

        TableColumn<Product, Integer> stockColumn = new TableColumn<>("Stock");
        stockColumn.getStyleClass().add("column");
        stockColumn.setPrefWidth(100);
        stockColumn.setCellValueFactory(cellData -> cellData.getValue().stockProperty().asObject());

        TableColumn<Product, Double> salePriceColumn = new TableColumn<>("salePrice");
        salePriceColumn.getStyleClass().add("column");
        salePriceColumn.setPrefWidth(120);
        salePriceColumn.setCellValueFactory(cellData -> cellData.getValue().salePriceProperty().asObject());

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
                double productSalePrice = product.getSalePrice();
                int stockQuantity = product.getStock();
                int currentQuantity = cartItems.getOrDefault(productId, 0);

                if (currentQuantity < stockQuantity) {
                    cartItems.put(productId, currentQuantity + 1);
                    totalSalePrice += productSalePrice;
                    totalLabel.setText("Total:                                                                        $" + String.format("%.2f", totalSalePrice));
                    updateOrderListView();
                    updateTableData();  // Cập nhật bảng sản phẩm sau khi thêm vào giỏ
                } else {
                    showStockAlert(product.getName());
                }
            });

            // Xử lý sự kiện khi nhấn nút "View Details"
            viewDetailsButton.setOnAction(e -> {
                showProductDetails(product);  // Hiển thị chi tiết sản phẩm
            });

            actionBox.getChildren().addAll(addButton, viewDetailsButton);
            return new SimpleObjectProperty<>(actionBox);
        });


        productTable.getColumns().addAll(idColumn, nameColumn, brandColumn, stockColumn, salePriceColumn, actionColumn);
        // Load dữ liệu vào bảng sản phẩm và khởi tạo phân trang
        loadData();

        // Tạo một HBox để chứa các button filter
        HBox filterBox = new HBox(17);
        // Tạo thanh tìm kiếm
        VBox searchBox = new VBox();
        searchBox.setPadding(new Insets(0, 500, 0, 10));
        TextField searchField = new TextField();
        searchField.setPromptText("Search product...");
        searchField.getStyleClass().add("search-field");
        searchField.setPrefWidth(600);
        searchField.setPrefHeight(30);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            // Khi search, chuyển filter về "All"
            filterBox.getChildren().forEach(node -> {
                if (node instanceof Button) {
                    node.getStyleClass().remove("selected");
                }
            });

            // Chọn lại button "All"
            for (Node node : filterBox.getChildren()) {
                if (node instanceof Button && ((Button) node).getText().equals("All")) {
                    node.getStyleClass().add("selected");
                }
            }

            // Thực hiện tìm kiếm với từ khóa và filter "All"
            filterProductList(productTable, newValue, null); // null tương đương với "All"
            updateTableData();
            pageLabel.setText("Page " + currentPage + " / " + totalPages);
        });
        searchBox.getChildren().addAll(searchField);
        // Thêm searchField và employeeBox vào HBox topSection
        topSection.getChildren().addAll(searchBox, employeeBox);

        HBox filterSection = new HBox();
        filterSection.setAlignment(Pos.CENTER_LEFT);
        filterBox.setPadding(new Insets(0, 10, 0, 55));


        // Tạo danh sách các button filter
        String[] filterCategories = cm.getAllCategories().toArray(new String[0]);
        String[] allCategories = new String[filterCategories.length + 1];
        allCategories[0] = "All";
        System.arraycopy(filterCategories, 0, allCategories, 1, filterCategories.length);


        // Tạo từng button filter và thêm vào HBox
        for (String category : allCategories) {
            Button filterButton = new Button(category);
            filterButton.getStyleClass().add("category-button");  // Thêm style class mặc định cho nút

            // Nếu category là "All", đặt sáng nút này khi khởi động
            if (category.equals("All")) {
                filterButton.getStyleClass().add("selected");
            }

            filterButton.setOnAction((e) -> {

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
                updateTableData();
                pageLabel.setText("Page " + currentPage + " / " + totalPages);
            });

            filterBox.getChildren().add(filterButton);
        }
        filterSection.getChildren().addAll(filterBox);

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
        Button buyNowButton = new Button("Buy Now");
        buyNowButton.getStyleClass().add("button-pagination");
        buyNowButton.setStyle("-fx-text-alignment: center; ");


        buyNowButton.setOnAction(e -> {
            if (!cartItems.isEmpty()) {
                submitOrder(); // Gọi method submitOrder
            }
        });


        buyNowContainer.getChildren().add(buyNowButton);

// Thêm các thành phần vào orderSummary
        orderSummary.getChildren().addAll(orderDetailLabel, orderListView, totalLabel, buyNowContainer);
        VBox outerVbox = new VBox();
        outerVbox.setPadding(new Insets(0, 0, 0, 0));  // Set the desired margin from top
        outerVbox.getChildren().add(orderSummary);
        outerVbox.setMaxWidth(500);
        outerVbox.setPrefWidth(500);
        table.getChildren().addAll(productTable, paginationBox);
        table.setStyle("-fx-background-color: white;");
        contentSection.getChildren().addAll(table, outerVbox);

        // Tạo VBox chính để đặt các phần thành từng lớp
        VBox mainContent = new VBox(10); // VBox chính để xếp các phần theo chiều dọc
        mainContent.getChildren().addAll(topSection, filterSection, contentSection);

        // Đặt mainContent vào trung tâm của BorderPane
        root.setCenter(mainContent);

        orderListView.setPrefHeight(600); // Chiều cao tối đa cho danh sách đơn hàng
        orderListView.setMaxHeight(600);

        // Scene và Stage
        Scene scene = new Scene(root, 1366, 768);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("cashier.css")).toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setTitle("Cashier App");
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

    public void submitOrder() {
        Stage stage = new Stage();
        stage.setTitle("Submit Order");

// Tạo layout cho scene
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER); // Căn giữa layout

// Tiêu đề
        Label titleLabel = new Label("Submit Order");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;"); // Định dạng tiêu đề

// Khách hàng
        Label customerLabel = new Label("Customer:");
        customerLabel.setPadding(new Insets(0 ,10,0,0));
        customerLabel.getStyleClass().add("label-popup");
        TextField customerPhoneInput = new TextField();
        customerPhoneInput.setMaxWidth(150);
        customerPhoneInput.getStyleClass().add("text-field-account");
        customerPhoneInput.setPromptText("Enter customer phone number");

// ListView để hiển thị tên khách hàng
        ListView<Customer> customerListView = new ListView<>();
        customerListView.setPrefHeight(50); // Đặt chiều cao cho ListView
        customerListView.getStyleClass().add("list-view");


// Label để hiển thị tên khách hàng đã chọn
        Label customerNameLabel = new Label();
        customerNameLabel.setVisible(false); // Bắt đầu ẩn label
        customerNameLabel.setManaged(false); // Không quản lý không gian hiển thị
        customerNameLabel.setStyle("-fx-font-weight: bold ; -fx-font-size: 20 ; -fx-text-fill: #4ad4dd;-fx-padding: 10;");

// Nút để đổi lại chọn khách hàng
        Button changeCustomerButton = new Button("Change");
        changeCustomerButton.setVisible(false); // Bắt đầu ẩn nút
        changeCustomerButton.setManaged(false); // Không quản lý không gian hiển thị
        changeCustomerButton.getStyleClass().add("button-account");

// Khi nhập số điện thoại, tìm kiếm khách hàng từ database
        customerListView.setVisible(!customerListView.getItems().isEmpty());
        customerListView.setManaged(!customerListView.getItems().isEmpty());

        customerPhoneInput.textProperty().addListener((observable, oldValue, newValue) -> {
            customerListView.getItems().clear();
            if (!newValue.isEmpty()) {
                List<Customer> customers = CashierController.searchCustomerByPhone(newValue);
                if (!customers.isEmpty()) {
                    customerListView.getItems().addAll(customers);
                }
            }

            customerListView.setVisible(!customerListView.getItems().isEmpty());
            customerListView.setManaged(!customerListView.getItems().isEmpty());
        });

// Khi chọn khách hàng từ danh sách, hiển thị tên khách hàng
        customerListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue.getId() != -1) {
                // Hiển thị tên khách hàng
                customerNameLabel.setText(newValue.getName()); // Giả định rằng Customer có phương thức getName()
                customerNameLabel.setVisible(true); // Hiển thị label
                customerNameLabel.setManaged(true); // Quản lý không gian hiển thị

                // Ẩn input và list
                customerPhoneInput.setVisible(false); // Ẩn ô nhập
                customerPhoneInput.setManaged(false); // Không quản lý không gian hiển thị
                phoneNumber = newValue.getPhoneNumber();

                customerListView.setVisible(false); // Ẩn danh sách khách hàng
                customerListView.setManaged(false); // Không quản lý không gian hiển thị

                // Hiện nút đổi
                changeCustomerButton.setVisible(true);
                changeCustomerButton.setManaged(true); // Quản lý không gian hiển thị
            } else {
                // Nếu không có lựa chọn nào, ẩn label
                customerNameLabel.setVisible(false);
                customerNameLabel.setManaged(false); // Không quản lý không gian hiển thị
                changeCustomerButton.setVisible(false);
                changeCustomerButton.setManaged(false); // Không quản lý không gian hiển thị
            }
        });

// Xử lý sự kiện khi nút "Đổi" được nhấn
        changeCustomerButton.setOnAction(event -> {
            // Ẩn tên khách hàng và hiển thị lại ô nhập và danh sách
            customerNameLabel.setVisible(false);
            customerNameLabel.setManaged(false); // Không quản lý không gian hiển thị
            changeCustomerButton.setVisible(false);
            changeCustomerButton.setManaged(false); // Không quản lý không gian hiển thị

            customerPhoneInput.setVisible(true); // Hiện lại ô nhập
            customerPhoneInput.setManaged(true); // Quản lý không gian hiển thị

            customerListView.setVisible(true); // Hiện lại danh sách khách hàng
            customerListView.setManaged(true); // Quản lý không gian hiển thị
            customerPhoneInput.requestFocus(); // Đặt con trỏ vào ô nhập
        });

// Nút thêm khách hàng mới
        Button addCustomerButton = new Button("Add Customer");
        addCustomerButton.getStyleClass().add("button-account");
        addCustomerButton.setOnAction(e -> CashierController.showAddCustomerScreen());

// Tổng tiền đơn hàng
        Label totalPriceLabel = new Label("Total: ");
        totalPriceLabel.getStyleClass().add("label-popup");
        totalPriceLabel.setStyle("-fx-padding: 20 ;");
        Label totalPrice = new Label("$"+ String.format("%.2f", totalSalePrice));
        totalPrice.getStyleClass().add("data-popup");

// Tiền khách trả
        Label paymentLabel = new Label("Customer\n payment:");
        paymentLabel.getStyleClass().add("label-popup");
        TextField paymentInput = new TextField();
        paymentInput.getStyleClass().add("text-field-account");
        paymentInput.setMaxWidth(200);
        paymentInput.setPromptText("Enter customer payment");

// Label cảnh báo nếu tiền trả ít hơn tổng tiền
        Label warningLabel = new Label();
        warningLabel.setTextFill(Color.RED);

// Tổng tiền trả lại
        Label changeLabel = new Label();
        changeLabel.setTextFill(Color.GREEN);

// Kiểm tra khi nhập tiền khách trả
        paymentInput.textProperty().addListener((observable, oldValue, newValue) -> {
            double total = totalSalePrice;
            try {
                double payment = Double.parseDouble(newValue);
                if (payment < total) {
                    warningLabel.setText("Insufficient amount.");
                    changeLabel.setText(""); // Xóa thông báo trả lại
                } else {
                    warningLabel.setText("");
                    changeLabel.setText("Change: $" + String.format("%.2f", payment - total)); // Hiển thị tổng tiền trả lại
                }
            } catch (NumberFormatException ex) {
                warningLabel.setText("Invalid payment amount.");
                changeLabel.setText(""); // Xóa thông báo trả lại
            }
        });

// Nút xác nhận thanh toán
        Button submitButton = new Button("Submit");
        submitButton.getStyleClass().add("button-account");
        submitButton.setOnAction(e -> {
            double total = totalSalePrice;
            String phone = phoneNumber;
            try {
                double payment = Double.parseDouble(paymentInput.getText());
                if (payment >= total) {
                    if (customerPhoneInput.getText() == null || customerPhoneInput.getText().isEmpty()) {
                        phone = "1234567890";
                    }
                    CashierController.processOrder(phone, cartItems, total, employeeName, idStore);
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "Payment successful!");
                    alert.showAndWait();
                    for (Map.Entry<Integer, Integer> entry : cartItems.entrySet()) {
                        int productId = entry.getKey();
                        int quantity = entry.getValue();
                        CashierModel.handlePurchase(productId, quantity, idStore); // Xử lý giao dịch
                    }

                    cartItems.clear();
                    totalSalePrice = 0;
                    totalLabel.setText("Total: $0.00");
                    updateOrderListView();
                    productTable.getItems().clear();
                    loadData();

                    stage.close(); // Đóng cửa sổ sau khi hoàn tất
                } else {
                    warningLabel.setText("Insufficient amount.");
                }
            } catch (NumberFormatException ex) {
                warningLabel.setText("Invalid payment amount.");
            }
        });

// Sử dụng GridPane để sắp xếp các thành phần
        GridPane gridPane = new GridPane();
        gridPane.setVgap(10); // Khoảng cách dọc
        gridPane.setHgap(10); // Khoảng cách ngang
        gridPane.setAlignment(Pos.CENTER_LEFT); // Căn trái grid

// Thêm các thành phần vào grid
        gridPane.add(customerLabel, 0, 0);
        gridPane.add(customerPhoneInput, 1, 0);
        gridPane.add(customerListView, 0, 1, 2, 1); // Chiếm 2 cột cho ListView
        gridPane.add(customerNameLabel, 1, 0); // Hiển thị tên khách hàng
        gridPane.add(changeCustomerButton, 2, 0); // Nút đổi
        gridPane.add(addCustomerButton, 0, 3, 2, 1); // Chiếm 2 cột cho nút thêm
        gridPane.add(totalPriceLabel, 0, 4);// Chiếm 2 cột cho tổng tiền
        gridPane.add(totalPrice, 1, 4);
        gridPane.add(paymentLabel, 0, 5);
        gridPane.add(paymentInput, 1, 5);
        gridPane.add(warningLabel, 0, 6, 2, 1); // Chiếm 2 cột cho cảnh báo
        gridPane.add(changeLabel, 0, 7, 2, 1); // Chiếm 2 cột cho tổng tiền trả lại
        gridPane.add(submitButton, 0, 8, 2, 1); // Chiếm 2 cột cho nút xác nhận

// Thêm tiêu đề và grid vào layout
        layout.getChildren().addAll(titleLabel, gridPane);

// Thiết lập layout cho scene
        Scene scene = new Scene(layout, 450, 450);
        scene.getStylesheets().add(getClass().getResource("/view/popup.css").toExternalForm());
        stage.setScene(scene);
        stage.show();

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
        ImageView imageView = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("images/" + product.getImage()))));
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
        Label priceValue = new Label(String.format("%.2f", product.getSalePrice()) + "$");
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

        Label stockLabel = new Label("Inventory :  " + product.getStock());
        stockLabel.setStyle("-fx-text-fill: #4AD4DD; -fx-font-size: 16px; -fx-font-weight: bold;");
        detailsGrid.add(stockLabel, 2, 2);

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
            double productSalePrice = product.getSalePrice();
            int stockQuantity = product.getStock();

            if (quantity <= stockQuantity) {
                cartItems.put(productId, quantity);
                totalSalePrice += productSalePrice * quantity;
                totalLabel.setText("Total:                                                                        $" + String.format("%.2f", totalSalePrice));
                updateOrderListView();
                updateTableData();  // Cập nhật bảng sản phẩm sau khi thêm vào giỏ
            } else {
                showStockAlert(product.getName());
            }

            dialog.close(); // Đóng cửa sổ chi tiết sản phẩm
        });

        // Sự kiện khi nhấn nút "Skip"
        skipButton.setOnAction(e -> {
            dialog.close(); // Đóng cửa sổ chi tiết sản phẩm
        });

        vbox.getChildren().add(mainLayout);
        // Tạo Scene và hiển thị cửa sổ
        Scene scene = new Scene(vbox);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("cashier.css")).toExternalForm());
        dialog.setScene(scene);
        dialog.setWidth(600);
        dialog.setHeight(400);
        dialog.show();
    }

    // Hàm lọc sản phẩm dựa trên từ khóa tìm kiếm và loại sản phẩm
    private final ObservableList<Product> filteredProductData = FXCollections.observableArrayList(); // New filtered list

    private void loadData() {
        productData.setAll(cm.getAll(idStore)); // Load all products into the original data list
        filteredProductData.setAll(productData); // Initialize the filtered list with all products
        totalPages = (int) Math.ceil((double) filteredProductData.size() / itemsPerPage); // Calculate total pages based on filtered data
        updateTableData();
        pageLabel.setText("Page " + currentPage + " / " + totalPages);// Update the displayed data
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

    private void filterProductList(TableView<Product> productTable, String searchKeyword, String filterCategory) {
        // Filter the product data based on the search keyword and category
        filteredProductData.setAll(cm.getAll(idStore).stream()
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

    private void updateOrderListView() {
        orderListView.getItems().clear();
        for (Map.Entry<Integer, Integer> entry : cartItems.entrySet()) {
            String itemName = cm.getOne(idStore, entry.getKey()).getName();
            final int[] quantity = {entry.getValue()};
            double productPrice = cm.getOne(idStore, entry.getKey()).getSalePrice();
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
                    totalLabel.setText("Total:                                                                        $" + String.format("%.2f", totalSalePrice));
                    updateOrderListView();
                    updateTableData();  // Update the product table after decreasing quantity
                }
            });

            // Tạo nút tăng số lượng
            Button increaseButton = new Button();
            increaseButton.getStyleClass().add("increase-button"); // Đảm bảo đã thêm class CSS
            Label increaseLabel = new Label("\u002B");
            increaseLabel.setStyle("-fx-text-fill: #ffffff;"); // Đổi màu chữ sang xanh
            increaseButton.setGraphic(increaseLabel);
            increaseButton.setOnAction(e -> {
                int currentStock = cm.getOne(idStore, entry.getKey()).getStock();
                if (quantity[0] < currentStock) {
                    quantity[0]++;
                    cartItems.put(entry.getKey(), quantity[0]);
                    totalSalePrice += productPrice; // Update total price
                    totalLabel.setText("Total:                                                                        $" + String.format("%.2f", totalSalePrice));
                    updateOrderListView();
                    updateTableData();  // Update the product table after increasing quantity
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
                totalLabel.setText("Total:                                                                        $" + String.format("%.2f", totalSalePrice));
                updateOrderListView(); // Update order list
                updateTableData();  // Update the product table after removing item
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