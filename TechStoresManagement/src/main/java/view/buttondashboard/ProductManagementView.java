package view.buttondashboard;

import entity.Product;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.DirectorModel;

public class ProductManagementView extends VBox {

    ObservableList<Product> productList = FXCollections.observableArrayList();
    DirectorModel dm = new DirectorModel();

    public ProductManagementView() {
        // Title Label
        Label titleLabel = new Label("Product Management");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        // New Product Button
        Button newProductButton = new Button("New Product");
        newProductButton.setStyle("-fx-background-color: #00C2FF; -fx-text-fill: white;");

        newProductButton.setOnAction(event -> showNewProductForm());

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
                    // Handle the detail button click event
                    editButton.setOnAction(event -> {
                        Product selectedProduct = getTableView().getItems().get(getIndex());
                        showProductEditor(selectedProduct); // Method to show product details
                    });

                    // Handle the delete button click event
                    deleteButton.setOnAction(event -> {
                        Product selectedProduct = getTableView().getItems().get(getIndex());

                        if (!dm.ifDependencies(selectedProduct.getId())) {
                            // Confirmation alert
                            Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
                            confirmationAlert.setTitle("Delete Product");
                            confirmationAlert.setHeaderText("Are you sure you want to delete this product?");
                            confirmationAlert.setContentText("Product: " + selectedProduct.getName());

                            // Show the confirmation dialog and capture the response
                            confirmationAlert.showAndWait().ifPresent(response -> {
                                if (response == ButtonType.OK) {
                                    // If confirmed, remove the product from the database
                                    boolean deleted = dm.delete(selectedProduct.getId());

                                    if (deleted) {
                                        // Remove the product from the table
                                        productList.remove(selectedProduct);
                                    } else {
                                        // If delete failed, show an error alert
                                        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                                        errorAlert.setTitle("Error");
                                        errorAlert.setHeaderText("Failed to delete the product.");
                                        errorAlert.setContentText("Please try again.");
                                        errorAlert.showAndWait();
                                    }
                                }
                            });
                        } else {
                            // Show an alert if there are dependencies
                            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                            errorAlert.setTitle("Error");
                            errorAlert.setHeaderText("Cannot delete the product.");
                            errorAlert.setContentText("This product still has dependencies. Please delete them first.");
                            errorAlert.showAndWait();
                        }
                    });

                    HBox optionBox = new HBox(editButton, deleteButton);
                    optionBox.setSpacing(10);
                    setGraphic(optionBox);
                }
            }
        });

        // Add Columns to Table
        productTable.getColumns().addAll(idColumn, nameColumn, brandColumn, purchasePriceColumn, salePriceColumn, optionColumn);

        // ObservableList to hold Product data
        productList.setAll(dm.getAll());

        // Bind the data to the TableView
        productTable.setItems(productList);

        // Thêm các thành phần vào VBox
        this.getChildren().addAll(titleLabel, newProductButton, searchBar, productTable);
    }

    private void showNewProductForm() {
        Stage newProductStage = new Stage();
        newProductStage.setTitle("New Product");

        // Create input fields for the product form
        TextField nameField = new TextField();
        nameField.setPromptText("Product Name");

        TextField brandField = new TextField();
        brandField.setPromptText("Brand");

        TextField purchasePriceField = new TextField();
        purchasePriceField.setPromptText("Purchase Price");

        TextField salePriceField = new TextField();
        salePriceField.setPromptText("Sale Price");

        // Create Save and Cancel buttons
        Button saveButton = new Button("Save");
        Button cancelButton = new Button("Cancel");

        // Save button action
        saveButton.setOnAction(event -> {
            int id = dm.getFinalId();
            String name = nameField.getText();
            if (name.isEmpty()) {
                alertEmptyName();
                return;
            }
            if (!dm.ifUniqueProductName(name)) {
                alertUniqueProductName();
                return;
            }
            String brand = brandField.getText();
            if (brand.isEmpty()) {
                alertEmptyBrand();
                return;
            }
            double purchasePrice;
            try {
                purchasePrice = Double.parseDouble(purchasePriceField.getText());

            } catch (NumberFormatException e) {
                alertInvalidPurchasePrice();
                return;
            }
            double salePrice;
            try {
                salePrice = Double.parseDouble(salePriceField.getText());
            } catch (NumberFormatException e) {
                alertInvalidSalePrice();
                return;
            }

            // Create a new product (assuming Product constructor takes these parameters)
            Product newProduct = new Product(id, name, brand, purchasePrice, salePrice);  // Assuming stock is initially 0

            // Add the new product to the model (implement dm.addProduct() in your DirectorModel)
            dm.add(newProduct);

            // Refresh the table data
            productList.setAll(dm.getAll());

            // Close the form after saving
            newProductStage.close();
        });

        // Cancel button action
        cancelButton.setOnAction(event -> newProductStage.close());

        // Layout for the form
        GridPane formLayout = new GridPane();
        formLayout.setPadding(new Insets(10));
        formLayout.setHgap(10);
        formLayout.setVgap(10);
        formLayout.setAlignment(Pos.CENTER);

        // Add input fields and labels to the form
        formLayout.add(new Label("Product Name:"), 0, 0);
        formLayout.add(nameField, 1, 0);
        formLayout.add(new Label("Brand:"), 0, 1);
        formLayout.add(brandField, 1, 1);
        formLayout.add(new Label("Purchase Price:"), 0, 2);
        formLayout.add(purchasePriceField, 1, 2);
        formLayout.add(new Label("Sale Price:"), 0, 3);
        formLayout.add(salePriceField, 1, 3);

        setupMainLayoutAndScene(saveButton, cancelButton, newProductStage, formLayout);
    }

    private void showProductEditor(Product product) {
        Stage editStage = new Stage();
        editStage.setTitle("Product Details");

        // Create layout for showing product editor
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setHgap(10);
        grid.setVgap(10);

        // Product Editor labels and fields
        Label nameLabel = new Label("Name:");
        Label brandLabel = new Label("Brand:");
        Label purchasePriceLabel = new Label("Purchase Price:");
        Label salePriceLabel = new Label("Sale Price:");

        TextField nameField = new TextField(product.getName());
        TextField brandField = new TextField(product.getBrand());
        TextField purchasePriceField = new TextField(String.valueOf(product.getPurchasePrice()));
        TextField salePriceField = new TextField(String.valueOf(product.getSalePrice()));

        // Add components to the grid layout
        grid.add(nameLabel, 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(brandLabel, 0, 1);
        grid.add(brandField, 1, 1);
        grid.add(purchasePriceLabel, 0, 2);
        grid.add(purchasePriceField, 1, 2);
        grid.add(salePriceLabel, 0, 3);
        grid.add(salePriceField, 1, 3);

        // Save button to apply changes
        Button saveButton = new Button("Save");
        saveButton.setOnAction(event -> {
            // Validate and update product information
            String name;
            name = nameField.getText();
            if (name.isEmpty()) {
                alertEmptyName();
                return;
            }
            if (!dm.ifUniqueProductName(name)) {
                alertUniqueProductName();
                return;
            }
            String brand;
            brand = brandField.getText();
            if (brand.isEmpty()) {
                alertEmptyBrand();
                return;
            }
            double purchasePrice;
            try {
                purchasePrice = Double.parseDouble(purchasePriceField.getText());
            } catch (NumberFormatException e) {
                alertInvalidPurchasePrice();
                return;
            }
            double salePrice;
            try {
                salePrice = Double.parseDouble(salePriceField.getText());
            } catch (NumberFormatException e) {
                alertInvalidSalePrice();
                return;
            }

            // Update Product
            Product updatedProduct = new Product(product.getId(), name, brand, purchasePrice, salePrice);
            dm.update(updatedProduct);

            // Refresh the table data
            productList.setAll(dm.getAll());

            // Close
            editStage.close();
        });

        // Cancel button
        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(event -> editStage.close());

        setupMainLayoutAndScene(saveButton, cancelButton, editStage, grid);
    }

    // Same Layout
    private void setupMainLayoutAndScene(Button saveButton, Button cancelButton, Stage stage, GridPane grid) {
        // Button layout
        HBox buttonLayout = new HBox(10, saveButton, cancelButton);
        buttonLayout.setAlignment(Pos.CENTER);

        // Main layout
        VBox mainLayout = new VBox(10, grid, buttonLayout);
        mainLayout.setPadding(new Insets(20));

        // Set the scene and show the pop-up
        Scene editScene = new Scene(mainLayout, 300, 250);
        stage.setScene(editScene);
        stage.initModality(Modality.APPLICATION_MODAL); // Block the parent window
        stage.showAndWait();
    }


    // ALERT
    private void alertEmptyName() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Product Name cannot be empty.");
        alert.showAndWait();
    }

    private void alertUniqueProductName() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Product Name already exists.");
        alert.showAndWait();
    }

    private void alertEmptyBrand() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Brand cannot be empty.");
        alert.showAndWait();
    }

    private void alertInvalidPurchasePrice() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Invalid Purchase Price.");
        alert.showAndWait();
    }

    private void alertInvalidSalePrice() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Invalid Sale Price.");
        alert.showAndWait();
    }

}
