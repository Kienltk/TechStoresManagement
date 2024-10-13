package view.buttondashboard;
import entity.Product;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.DirectorModel;

public class ProductManagementView extends VBox {
    public ProductManagementView() {
        // Title Label
        Label titleLabel = new Label("Product Management");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        // New Product Button
        Button newProductButton = new Button("New Product");
        newProductButton.setStyle("-fx-background-color: #00C2FF; -fx-text-fill: white;");

        // Search Bar
        TextField searchField = new TextField();
        searchField.setPromptText("Search Item");
        Button searchButton = new Button("🔍");

        HBox searchBar = new HBox(searchField, searchButton);
        searchBar.setAlignment(Pos.CENTER_RIGHT);
        searchBar.setSpacing(10);

        // TableView for Product Data
        TableView<Product> productTable = new TableView<>();

        // Table Columns
        TableColumn<Product, Integer> idColumn = new TableColumn<>("No");
        idColumn.setMinWidth(50);
        idColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());

        TableColumn<Product, HBox> nameColumn = new TableColumn<>("Name");
        nameColumn.setPrefWidth(230);
        nameColumn.setCellValueFactory(cellData -> {
            Product product = cellData.getValue();
//            ImageView imageView = product.getImage();
            Label nameLabel = new Label(product.getName());
            HBox hBox = new HBox(10);
            hBox.getChildren().addAll(nameLabel);
            return new SimpleObjectProperty<>(hBox);
        });

        TableColumn<Product, String> brandColumn = new TableColumn<>("Brand");
        brandColumn.setMinWidth(150);
        brandColumn.setCellValueFactory(cellData -> cellData.getValue().brandProperty());

        TableColumn<Product, Double> purchasePriceColumn = new TableColumn<>("Purchase Price");
        purchasePriceColumn.setMinWidth(100);
        purchasePriceColumn.setCellValueFactory(cellData -> cellData.getValue().purchasePriceProperty().asObject());

        TableColumn<Product, Double> salePriceColumn = new TableColumn<>("Sale Price");
        salePriceColumn.setMinWidth(100);
        salePriceColumn.setCellValueFactory(cellData -> cellData.getValue().salePriceProperty().asObject());

        TableColumn<Product, Integer> stockColumn = new TableColumn<>("Stock");
        stockColumn.setMinWidth(100);
        stockColumn.setCellValueFactory(cellData -> cellData.getValue().stockProperty().asObject());

        // Option Column with Edit and Delete buttons
        TableColumn<Product, Void> optionColumn = new TableColumn<>("Option");
        optionColumn.setCellFactory(col -> new TableCell<>() {
            final Button editButton = new Button("Edit");
            final Button deleteButton = new Button("Delete");

            {
                editButton.setStyle("-fx-background-color: yellow;");
                deleteButton.setStyle("-fx-background-color: red; -fx-text-fill: white;");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox optionBox = new HBox(editButton, deleteButton);
                    optionBox.setSpacing(10);
                    setGraphic(optionBox);
                }
            }
        });

        // Add Columns to Table
        productTable.getColumns().addAll(idColumn, nameColumn, brandColumn, purchasePriceColumn, salePriceColumn, stockColumn, optionColumn);

        // ObservableList to hold Product data
        DirectorModel dm = new DirectorModel();
        ObservableList<Product> productList = FXCollections.observableArrayList();
        productList.setAll(dm.getAll(1));

        // Bind the data to the TableView
        productTable.setItems(productList);

        // Thêm các thành phần vào VBox
        this.getChildren().addAll(titleLabel, newProductButton, searchBar, productTable);
    }

}
