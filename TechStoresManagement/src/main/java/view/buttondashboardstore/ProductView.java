package view.buttondashboardstore;

import controller.DirectorController;
import controller.Session;
import entity.Product;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import model.DirectorModel;
import view.stage.AdditionSuccess;
import view.stage.DeletionFailed;
import view.stage.DeletionSuccess;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Objects;

public class ProductView extends VBox {
    private final int idStore = Session.getIdStore();

    ObservableList<Product> productList = FXCollections.observableArrayList();
    ObservableList<Product> filteredList = FXCollections.observableArrayList();
    DirectorModel dm = new DirectorModel();
    HashMap<String, Label> messageLabel = new HashMap<>();


    // Biến cho phân trang
    private Pagination pagination;
    private Label pageLabel; // Label hiển thị số trang
    private int itemsPerPage = 12; // Số sản phẩm hiển thị trên mỗi trang
    private int currentPage = 0; // Trang hiện tại

    TableView<Product> productTable = new TableView<>();
    public ProductView() {
        // Title Label
        Label titleLabel = new Label("Product Management");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        pageLabel = new Label();
        pageLabel.getStyleClass().add("text-pagination");// Khởi tạo pageLabel
        updatePageLabel(); // Cập nhật nhãn số trang

        // Search Bar
        TextField searchField = new TextField();
        searchField.setPromptText("Search Item");
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            isFiltered = !newValue.isEmpty(); // Cập nhật trạng thái lọc
            filterList(newValue);
        });
        searchField.getStyleClass().add("search-box");

        HBox searchBar = new HBox(searchField);
        searchBar.setAlignment(Pos.CENTER_RIGHT);
        searchBar.setStyle(" -fx-padding:0 620 10 10;");

        HBox topControls = new HBox(10);
        topControls.setStyle("-fx-min-width: 1000");
        topControls.getChildren().addAll(searchBar);

        // TableView for Product Data

        productTable.setItems(productList);
        productTable.getStyleClass().add("table-view");

        // Table Columns
        TableColumn<Product, Number> idColumn = new TableColumn<>("No.");
        idColumn.setMinWidth(80);
        idColumn.getStyleClass().add("column");
        idColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Product, Number>, ObservableValue<Number>>() {
            @Override
            public javafx.beans.value.ObservableValue<Number> call(TableColumn.CellDataFeatures<Product, Number> p) {
                return javafx.beans.binding.Bindings.createIntegerBinding(() -> productTable.getItems().indexOf(p.getValue()) + 1);
            }
        });

        TableColumn<Product, HBox> nameColumn = new TableColumn<>("Name");
        nameColumn.setPrefWidth(380);
        nameColumn.getStyleClass().add("column");
        nameColumn.setCellValueFactory(cellData -> {
            Product product = cellData.getValue();
            Label nameLabel = new Label(product.getName());
            HBox hBox = new HBox(10);
            hBox.setAlignment(Pos.CENTER_LEFT);
            hBox.getChildren().addAll(nameLabel);
            return new SimpleObjectProperty<>(hBox);
        });

        TableColumn<Product, String> brandColumn = new TableColumn<>("Brand");
        brandColumn.setMinWidth(180);
        brandColumn.getStyleClass().add("column");
        brandColumn.setCellValueFactory(cellData -> cellData.getValue().brandProperty());

        TableColumn<Product, Double> purchasePriceColumn = new TableColumn<>("Purchase Price");
        purchasePriceColumn.setMinWidth(130);
        purchasePriceColumn.getStyleClass().add("column");
        purchasePriceColumn.setCellValueFactory(cellData -> cellData.getValue().purchasePriceProperty().asObject());

        TableColumn<Product, Double> salePriceColumn = new TableColumn<>("Sale Price");
        salePriceColumn.setMinWidth(130);
        salePriceColumn.getStyleClass().add("column");
        salePriceColumn.setCellValueFactory(cellData -> cellData.getValue().salePriceProperty().asObject());

        // Option Column with Edit and Delete buttons
        TableColumn<Product, Void> optionColumn = new TableColumn<>("        Option");
        optionColumn.setMinWidth(145);
        optionColumn.getStyleClass().add("column");
        optionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button viewButton = new Button();

            {
                // Tạo ImageView cho các icon
                ImageView viewIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/view.png")));

                // Đặt kích thước ban đầu cho icon
                setIconSize(viewIcon, 20);

                // Thêm icon vào nút
                viewButton.setGraphic(viewIcon);

                // Đặt style cho nút
                String defaultStyle = "-fx-background-color: transparent; -fx-border-color: transparent; -fx-padding: 6;";
                viewButton.setStyle(defaultStyle);

                // Thêm sự kiện phóng to khi hover và giảm padding
                addHoverEffect(viewButton, viewIcon);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Product selectedProduct = getTableView().getItems().get(getIndex());
                    viewButton.setOnAction(e -> openProductView(selectedProduct));

                    HBox optionBox = new HBox(viewButton);
                    optionBox.setAlignment(Pos.CENTER);
                    optionBox.setSpacing(10);
                    setGraphic(optionBox);
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

        // Add Columns to Table
        productTable.getColumns().addAll(idColumn, nameColumn, brandColumn, purchasePriceColumn, salePriceColumn, optionColumn);
        // Update the initial call to set the product list
        productList.setAll(dm.getAllForStore(idStore));
        loadPageData();

        // Update the pagination buttons to reflect the filtered list
        Button prevButton = new Button("<-");
        prevButton.getStyleClass().add("button-pagination");
        prevButton.setOnAction(event -> {
            if (currentPage > 0) {
                currentPage--;
                loadPageData();
                updatePageLabel(); // Cập nhật Label số trang
            }
        });

// Cập nhật sự kiện cho nút Next


        Button nextButton = new Button("->");
        nextButton.getStyleClass().add("button-pagination");
        nextButton.setOnAction(event -> {
            if ((currentPage + 1) * itemsPerPage < productList.size()) {
                currentPage++;
                loadPageData();
                updatePageLabel(); // Cập nhật Label số trang
            }
        });
        updatePageLabel();


        // HBox chứa các nút phân trang và nhãn số trang
        HBox paginationBox = new HBox(10, prevButton, pageLabel, nextButton);
        paginationBox.setAlignment(Pos.CENTER);
        paginationBox.setSpacing(30);
        paginationBox.setStyle("-fx-padding: 8");

        // Thêm các thành ph���n vào VBox
        this.getChildren().addAll(titleLabel, topControls, productTable, paginationBox);
        this.getStyleClass().add("vbox");

    }
    private void filterList(String searchTerm) {
        filteredList.clear(); // Xóa danh sách lọc trước khi thêm mới

        for (Product product : dm.getAllForStore(idStore)) {
            if (product.getName().toLowerCase().contains(searchTerm.toLowerCase()) ||
                    product.getBrand().toLowerCase().contains(searchTerm.toLowerCase())) {
                filteredList.add(product);
            }
        }

        // Cập nhật TableView với filteredList
        productTable.setItems(filteredList);
        currentPage = 0; // Đặt lại trang về 0 sau khi tìm kiếm
        int totalPages = (int) Math.ceil((double) filteredList.size() / itemsPerPage);
        pageLabel.setText("Page " + (currentPage + 1) + " of " + totalPages);
        loadPageData(); // Tải lại dữ liệu
    }

    private boolean isFiltered = false;

    private void paginate() {
        ObservableList<Product> sourceList = isFiltered ? filteredList : productList;

        int fromIndex = currentPage * itemsPerPage;
        int toIndex = Math.min(fromIndex + itemsPerPage, sourceList.size());

        if (fromIndex >= sourceList.size()) {
            currentPage = 0;
            fromIndex = 0;
            toIndex = Math.min(itemsPerPage, sourceList.size());
        }
        ObservableList<Product> paginatedList = FXCollections.observableArrayList(sourceList.subList(fromIndex, toIndex));
        productTable.setItems(paginatedList);
    }
    private void loadPageData() {
        paginate();
    }
    private void updatePageLabel() {
        int totalPages = (int) Math.ceil((double) productList.size() / itemsPerPage);
        pageLabel.setText("Page " + (currentPage + 1) + " of " + totalPages);
    }

    private void openProductView(Product product) {
        Stage dialog = new Stage();
        dialog.setTitle("View Product");
        dialog.initModality(Modality.APPLICATION_MODAL);

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setVgap(10);
        grid.setHgap(10);

        // Create Labels with the variable data
        Label productNameLabel = new Label("Product Name:");
        Label productNameData = new Label(product.getName());
        productNameLabel.getStyleClass().add("label-popup");
        productNameData.getStyleClass().add("data-popup");

        Label brandLabel = new Label("Brand:");
        Label brandData = new Label(product.getBrand());
        brandLabel.getStyleClass().add("label-popup");
        brandData.getStyleClass().add("data-popup");

        Label purchasePriceLabel = new Label("Purchase Price:");
        Label purchasePriceData = new Label(String.valueOf(product.getPurchasePrice()));
        purchasePriceLabel.getStyleClass().add("label-popup");
        purchasePriceData.getStyleClass().add("data-popup");

        Label salePriceLabel = new Label("Sale Price:");
        Label salePriceData = new Label(String.valueOf(product.getSalePrice()));
        salePriceLabel.getStyleClass().add("label-popup");
        salePriceData.getStyleClass().add("data-popup");

        Label stockLabel = new Label("Stock (total):");
        Label stockData = new Label(String.valueOf(dm.getTotalStockForStore(product.getId(), idStore)));
        stockLabel.getStyleClass().add("label-popup");
        stockData.getStyleClass().add("data-popup");

        // Add labels and data to the grid
        grid.add(productNameLabel, 0, 0);
        grid.add(productNameData, 1, 0);

        grid.add(brandLabel, 0, 1);
        grid.add(brandData, 1, 1);

        grid.add(purchasePriceLabel, 0, 2);
        grid.add(purchasePriceData, 1, 2);

        grid.add(salePriceLabel, 0, 3);
        grid.add(salePriceData, 1, 3);

        grid.add(stockLabel, 0, 4);
        grid.add(stockData, 1, 4);

        // Image
        ImageView imageView = new ImageView();
        Image originalImage;

        if ((getClass().getResourceAsStream("/view/images/" + product.getImage()) != null)) {
            originalImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/view/images/" + product.getImage())));
            imageView.setImage(originalImage);
        } else {
            originalImage = null;
        }
        imageView.setFitWidth(200);
        imageView.setFitHeight(200);
        VBox imageBox = new VBox(imageView);
        imageBox.setAlignment(Pos.CENTER);
        imageBox.setPadding(new Insets(10));

        // Close button
        Button closeButton = new Button("Close");
        closeButton.setAlignment(Pos.CENTER_RIGHT);
        closeButton.getStyleClass().add("button-pagination");
        closeButton.setOnAction(e -> dialog.close());

        // Add the grid and button to the VBox
        VBox vbox = new VBox(grid, imageBox, closeButton);
        vbox.setSpacing(10);
        vbox.setPadding(new Insets(15));

        Scene dialogScene = new Scene(vbox);
        dialogScene.getStylesheets().add(getClass().getResource("/view/popup.css").toExternalForm());
        dialogScene.getStylesheets().add(getClass().getResource("/view/director.css").toExternalForm());
        dialog.setScene(dialogScene);
        dialog.show();
    }

}