package view.buttondashboard;

import entity.Product;
import javafx.beans.property.SimpleObjectProperty;
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
import model.DirectorModel;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Objects;

public class ProductManagementView extends VBox {

    ObservableList<Product> productList = FXCollections.observableArrayList();
    DirectorModel dm = new DirectorModel();
    HashMap<String, Label> messageLabel = new HashMap<>();



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
                    // Handle the edit button click event
                    editButton.setOnAction(event -> {
                        Product selectedProduct = getTableView().getItems().get(getIndex());
                        showProductEditor(selectedProduct);
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

        // Error messages for CRUD
        messageLabel.put("name", new Label());
        messageLabel.put("brand", new Label());
        messageLabel.put("purchasePrice", new Label());
        messageLabel.put("salePrice", new Label());
        messageLabel.forEach((key, value) -> {
            value.setVisible(false);
            value.setManaged(false);
        });

        // Clear the message label
        newProductStage.setOnCloseRequest(event -> {
            messageLabel.clear();
        });

        // Create an ImageView for displaying the uploaded image
        ImageView imageView = new ImageView();
        imageView.setFitWidth(100); // Set the desired width
        imageView.setFitHeight(100); // Set the desired height
        imageView.setPreserveRatio(true); // Preserve aspect ratio


        // Create a button for uploading an image
        Button uploadButton = new Button("Upload Image");
        final File[] tempImageFile = new File[1];
        final Image[] uploadedImage = new Image[1];

        uploadButton.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));
            File file = fileChooser.showOpenDialog(newProductStage);
            if (file != null) {
                try {
                    // Load the image into the ImageView
                    Image image = new Image(new FileInputStream(file));
                    imageView.setImage(image);

                    // Store the uploaded image reference
                    uploadedImage[0] = image;

                    // Destination
                    String targetDirectory = "src/main/resources/view/images";
                    File targetDir = new File(targetDirectory);

                    // Rename uploaded file
                    String tempFileName = "img_temp" + file.getName().substring(file.getName().lastIndexOf("."));
                    Path tempPath = new File(targetDir, tempFileName).toPath();

                    // Copy the file to the destination
                    Files.copy(file.toPath(), tempPath, StandardCopyOption.REPLACE_EXISTING);

                    // Store the temporary file reference
                    tempImageFile[0] = new File(tempPath.toString());

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        // Save button action
        saveButton.setOnAction(event -> {
            boolean flag = true; //TRUE = no error
            int id = dm.getFinalId();
            String name = nameField.getText();

            if (name.isEmpty()) {
                messageLabel.get("name").setText(alertEmptyName());
                showMessage(messageLabel.get("name"));
                flag = false;
            }
            if (!dm.ifUniqueProductName(name)) {
                messageLabel.get("name").setText(alertUniqueProductName());
                showMessage(messageLabel.get("name"));
                flag = false;
            }
            String brand = brandField.getText();
            if (brand.isEmpty()) {
                messageLabel.get("brand").setText(alertEmptyBrand());
                showMessage(messageLabel.get("brand"));
                flag = false;
            }
            double purchasePrice = 0;
            try {
                purchasePrice = Double.parseDouble(purchasePriceField.getText());

            } catch (NumberFormatException e) {
                messageLabel.get("purchasePrice").setText(alertInvalidPurchasePrice());
                showMessage(messageLabel.get("purchasePrice"));
                flag = false;
            }
            double salePrice = 0;
            try {
                salePrice = Double.parseDouble(salePriceField.getText());
            } catch (NumberFormatException e) {
                messageLabel.get("salePrice").setText(alertInvalidSalePrice());
                showMessage(messageLabel.get("salePrice"));
                flag = false;
            }
            String imgAddress = name + ".jpg";
            if (!flag) return;

            // Create a new product (assuming Product constructor takes these parameters)
            Product newProduct = new Product(id, imgAddress, name, brand, purchasePrice, salePrice);  // Assuming stock is initially 0

            // Add the new product to the model (implement dm.addProduct() in your DirectorModel)
            dm.add(newProduct);

            // If there is a temporary image file, rename it to the actual product image
            if (tempImageFile[0] != null) {
                try {
                    // Define the final destination for the product image
                    String finalImageName = newProduct.getName() + ".jpg";
                    Path finalImagePath = new File("src/main/resources/view/images", finalImageName).toPath();

                    // Move the temporary image to the final destination
                    Files.copy(tempImageFile[0].toPath(), finalImagePath, StandardCopyOption.REPLACE_EXISTING);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // Refresh the table data
            productList.setAll(dm.getAll());

            // Close the form after saving
            newProductStage.close();
        });

        // Cancel button action
        cancelButton.setOnAction(event -> {
            newProductStage.close();
        });

        // Layout for the form using HBox
        HBox formLayout = new HBox(20); // Spacing between elements
        formLayout.setPadding(new Insets(20));
        formLayout.setAlignment(Pos.CENTER_LEFT);

        // Create a VBox for the input fields
        VBox inputFields = new VBox(10); // Spacing between input fields
        inputFields.setAlignment(Pos.CENTER_LEFT);

        // Add input fields and labels to the inputFields VBox
        inputFields.getChildren().addAll(
                new Label("Product Name:"), nameField, messageLabel.get("name"),
                new Label("Brand:"), brandField, messageLabel.get("brand"),
                new Label("Purchase Price:"), purchasePriceField, messageLabel.get("purchasePrice"),
                new Label("Sale Price:"), salePriceField, messageLabel.get("salePrice"),
                new HBox(10, saveButton, cancelButton) // Buttons in a horizontal box
        );


        // Create a VBox for the image upload section
        VBox imageUploadSection = new VBox(10);
        imageUploadSection.setAlignment(Pos.CENTER); // Center the image and upload button
        imageUploadSection.getChildren().addAll(imageView, uploadButton);

        // Add the input fields and the image upload section to the main form layout
        formLayout.getChildren().addAll(inputFields, imageUploadSection);

        // Create the scene and set it to the stage
        Scene scene = new Scene(formLayout, 800, 600); // Adjust size as needed
        newProductStage.setScene(scene);
        newProductStage.show();
    }

    private void showProductEditor(Product product) {
        Stage editStage = new Stage();
        editStage.setTitle("Product Editor");

        // Create layout for showing product editor
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setHgap(10);
        grid.setVgap(10);

        // Error messages for CRUD
        messageLabel.put("name", new Label());
        messageLabel.put("brand", new Label());
        messageLabel.put("purchasePrice", new Label());
        messageLabel.put("salePrice", new Label());
        messageLabel.forEach((key, value) -> {
            value.setVisible(false);
            value.setManaged(false);
        });

        // Clear the message label
        editStage.setOnCloseRequest(event -> {
            messageLabel.clear();

        });

        // Create input fields and labels
        TextField nameField = new TextField(product.getName());
        TextField brandField = new TextField(product.getBrand());
        TextField purchasePriceField = new TextField(String.valueOf(product.getPurchasePrice()));
        TextField salePriceField = new TextField(String.valueOf(product.getSalePrice()));

        // Old Data
        final String oldName = nameField.getText();
        final String oldBrand = brandField.getText();
        final double oldPurchasePrice = Double.parseDouble(purchasePriceField.getText());
        final double oldSalePrice = Double.parseDouble(salePriceField.getText());

        // Create an ImageView for displaying the uploaded image
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

        // Create a button for uploading an image
        Button uploadButton = new Button("Upload Image");
        final File[] tempImageFile = new File[1];
        final Image[] uploadedImage = new Image[1];


        uploadButton.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));
            File file = fileChooser.showOpenDialog(editStage);
            if (file != null) {
                try {
                    // Load the image into the ImageView
                    Image image = new Image(new FileInputStream(file));
                    imageView.setImage(image);

                    // Store the uploaded image reference
                    uploadedImage[0] = image;

                    // Destination
                    String targetDirectory = "src/main/resources/view/images";
                    File targetDir = new File(targetDirectory);

                    // Rename uploaded file
                    String tempFileName = "img_temp" + file.getName().substring(file.getName().lastIndexOf("."));
                    Path tempPath = new File(targetDir, tempFileName).toPath();

                    // Copy the file to the destination
                    Files.copy(file.toPath(), tempPath, StandardCopyOption.REPLACE_EXISTING);

                    // Store the temporary file reference
                    tempImageFile[0] = new File(tempPath.toString());

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        // Save button to apply changes
        Button saveButton = new Button("Save");
        saveButton.setOnAction(event -> {
            boolean flag = true; //TRUE = no error

            // Validate Inputs
            String name = nameField.getText();
            if (name.isEmpty()) {
                messageLabel.get("name").setText(alertEmptyName());
                showMessage(messageLabel.get("name"));
                flag = false;
            }
            if (!dm.ifUniqueProductName(name) && (!name.equals(oldName))) {
                messageLabel.get("name").setText(alertUniqueProductName());
                showMessage(messageLabel.get("name"));
                flag = false;
            }
            String brand = brandField.getText();
            if (brand.isEmpty()) {
                messageLabel.get("brand").setText(alertEmptyBrand());
                showMessage(messageLabel.get("brand"));
                flag = false;
            }
            double purchasePrice = 0;
            try {
                purchasePrice = Double.parseDouble(purchasePriceField.getText());

            } catch (NumberFormatException e) {
                messageLabel.get("purchasePrice").setText(alertInvalidPurchasePrice());
                showMessage(messageLabel.get("purchasePrice"));
                flag = false;
            }
            double salePrice = 0;
            try {
                salePrice = Double.parseDouble(salePriceField.getText());
            } catch (NumberFormatException e) {
                messageLabel.get("salePrice").setText(alertInvalidSalePrice());
                showMessage(messageLabel.get("salePrice"));
                flag = false;
            }
            if (!flag) return;

            // Update Product
            Product updatedProduct = new Product(product.getId(), "", name, brand, purchasePrice, salePrice);
            dm.update(updatedProduct);

            // If there is a temporary image file, rename it to the actual product image
            if (tempImageFile[0] != null && tempImageFile[0].exists()) {
                try {
                    // Define the final destination for the product image
                    String finalImageName = product.getImage();
                    Path finalImagePath = new File("src/main/resources/view/images", finalImageName).toPath();

                    // Move the temporary image to the final destination
                    Files.move(tempImageFile[0].toPath(), finalImagePath, StandardCopyOption.REPLACE_EXISTING);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // Refresh the table data
            productList.setAll(dm.getAll());

            // Close
            editStage.close();
        });

        // Reset button
        Button resetButton = new Button("Reset");
        resetButton.setOnAction(event -> {
            // Restore old values
            nameField.setText(oldName);
            brandField.setText(oldBrand);
            purchasePriceField.setText(String.valueOf(oldPurchasePrice));
            salePriceField.setText(String.valueOf(oldSalePrice));
            imageView.setImage(originalImage);
        });

        // Cancel button
        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(event -> editStage.close());

        // Layout for the form using HBox
        HBox formLayout = new HBox(20); // Spacing between elements
        formLayout.setPadding(new Insets(20));
        formLayout.setAlignment(Pos.CENTER_LEFT);

        // Create a VBox for the input fields
        VBox inputFields = new VBox(10); // Spacing between input fields
        inputFields.setAlignment(Pos.CENTER_LEFT);

        // Add input fields and labels to the inputFields VBox
        inputFields.getChildren().addAll(
                new Label("Product Name:"), nameField, messageLabel.get("name"),
                new Label("Brand:"), brandField, messageLabel.get("brand"),
                new Label("Purchase Price:"), purchasePriceField, messageLabel.get("purchasePrice"),
                new Label("Sale Price:"), salePriceField, messageLabel.get("salePrice"),
                new HBox(10, saveButton, resetButton, cancelButton) // Buttons in a horizontal box
        );

        // Create a VBox for the image upload section
        VBox imageUploadSection = new VBox(10);
        imageUploadSection.setAlignment(Pos.CENTER); // Center the image and upload button
        imageUploadSection.getChildren().addAll(imageView, uploadButton);

        // Add the input fields and the image upload section to the main form layout
        formLayout.getChildren().addAll(inputFields, imageUploadSection);

        // Create the scene and set it to the stage
        Scene scene = new Scene(formLayout, 800, 600); // Adjust size as needed
        editStage.setScene(scene);
        editStage.show();
    }

    // ALERT MESSAGES
    private String alertEmptyName() {
        return "Product Name cannot be empty.";
    }

    private String alertUniqueProductName() {
        return "Product Name already exists.";
    }

    private String alertEmptyBrand() {
        return "Brand cannot be empty.";
    }

    private String alertInvalidPurchasePrice() {
        return "Invalid Purchase Price.";
    }

    private String alertInvalidSalePrice() {
        return "Invalid Sale Price.";
    }

    private void showMessage(Label messageLabel) {
        messageLabel.setStyle("-fx-text-fill: red;");
        messageLabel.setVisible(true);
        messageLabel.setManaged(true);
    }

}