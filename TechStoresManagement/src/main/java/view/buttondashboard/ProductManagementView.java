package view.buttondashboard;

import controller.DirectorController;
import java.util.ArrayList;
import java.util.List;
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
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
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

public class ProductManagementView extends VBox {

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


    public ProductManagementView() {
        // Title Label
        Label titleLabel = new Label("Product Management");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        pageLabel = new Label();
        pageLabel.getStyleClass().add("text-pagination");// Khởi tạo pageLabel
        updatePageLabel(); // Cập nhật nhãn số trang
        // New Product Button
        Button newProductButton = new Button("New Product");
        newProductButton.getStyleClass().add("button-pagination");

        newProductButton.setOnAction(event -> showNewProductForm());

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
        topControls.getChildren().addAll( searchBar,newProductButton);

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
            private final Button editButton = new Button();
            private final Button deleteButton = new Button();

            {
                // Tạo ImageView cho các icon
                ImageView editIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/edit.png")));
                ImageView deleteIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/delete.png")));

                // Đặt kích thước ban đầu cho icon
                setIconSize(editIcon, 20);
                setIconSize(deleteIcon, 20);

                // Thêm icon vào nút
                editButton.setGraphic(editIcon);
                deleteButton.setGraphic(deleteIcon);

                // Đặt style cho nút
                String defaultStyle = "-fx-background-color: transparent; -fx-border-color: transparent; -fx-padding: 6;";
                editButton.setStyle(defaultStyle);
                deleteButton.setStyle(defaultStyle);

                // Thêm sự kiện phóng to khi hover và giảm padding
                addHoverEffect(editButton, editIcon);
                addHoverEffect(deleteButton, deleteIcon);
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
                                        Stage stage = new Stage();
                                        DeletionSuccess message = new DeletionSuccess();
                                        message.start(stage);
                                        // Remove the product from the table
                                        productList.remove(selectedProduct);
                                    } else {
                                        Stage stage = new Stage();
                                        DeletionFailed message = new DeletionFailed();
                                        message.start(stage);
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
        productList.setAll(dm.getAll());
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

        for (Product product : dm.getAll()) {
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

    private void showNewProductForm() {
        Stage newProductStage = new Stage();
        newProductStage.setTitle("New Product");

        // Create input fields for the product form
        TextField nameField = new TextField();
        nameField.setPromptText("Product Name");
        nameField.getStyleClass().add("text-field-account");

        TextField brandField = new TextField();
        brandField.setPromptText("Brand");
        brandField.getStyleClass().add("text-field-account");

        TextField purchasePriceField = new TextField();
        purchasePriceField.setPromptText("Purchase Price");
        purchasePriceField.getStyleClass().add("text-field-account");

        TextField salePriceField = new TextField();
        salePriceField.setPromptText("Sale Price");
        salePriceField.getStyleClass().add("text-field-account");

        // Error messages for CRUD
        messageLabel.put("name", new Label());
        messageLabel.put("brand", new Label());
        messageLabel.put("purchasePrice", new Label());
        messageLabel.put("salePrice", new Label());
        messageLabel.forEach((key, value) -> {
            value.setVisible(false);
            value.setManaged(false);
        });

        // Image upload section
        ImageView imageView = new ImageView();
        imageView.setFitWidth(150);
        imageView.setFitHeight(150);
        imageView.setPreserveRatio(true);

        Button uploadButton = new Button("Upload Image");
        uploadButton.getStyleClass().add("button-account");
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

                    // StoreManager the uploaded image reference
                    uploadedImage[0] = image;

                    // Destination
                    String targetDirectory = "src/main/resources/view/images";
                    File targetDir = new File(targetDirectory);

                    // Rename uploaded file
                    String tempFileName = "img_temp" + file.getName().substring(file.getName().lastIndexOf("."));
                    Path tempPath = new File(targetDir, tempFileName).toPath();

                    // Copy the file to the destination
                    Files.copy(file.toPath(), tempPath, StandardCopyOption.REPLACE_EXISTING);

                    // StoreManager the temporary file reference
                    tempImageFile[0] = new File(tempPath.toString());

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        // Create buttons for Save and Cancel
        Button saveButton = new Button("Save");
        saveButton.getStyleClass().add("button-account");

        Button cancelButton = new Button("Cancel");
        cancelButton.getStyleClass().add("button-cancel-account");

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
            Stage stage = new Stage();
            AdditionSuccess message = new AdditionSuccess();
            message.start(stage);

            // Close the form after saving
            newProductStage.close();
        });

        // Cancel button action
        cancelButton.setOnAction(event -> {
            newProductStage.close();
        });


        // Layout using GridPane
        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(15);
        gridPane.setPadding(new Insets(40));

        // Add components to the grid
        gridPane.add(new Label("Product Name:"), 0, 0);
        gridPane.add(nameField, 1, 0);
        gridPane.add(messageLabel.get("name"), 2, 0);

        gridPane.add(new Label("Brand:"), 0, 1);
        gridPane.add(brandField, 1, 1);
        gridPane.add(messageLabel.get("brand"), 2, 1);

        gridPane.add(new Label("Purchase Price:"), 0, 2);
        gridPane.add(purchasePriceField, 1, 2);
        gridPane.add(messageLabel.get("purchasePrice"), 2, 2);

        gridPane.add(new Label("Sale Price:"), 0, 3);
        gridPane.add(salePriceField, 1, 3);
        gridPane.add(messageLabel.get("salePrice"), 2, 3);

        // Image upload section in GridPane
        VBox imageUploadBox = new VBox(10, imageView, uploadButton);
        imageUploadBox.setAlignment(Pos.CENTER);
        gridPane.add(imageUploadBox, 1, 4);

        // Save and Cancel buttons
        HBox buttonBox = new HBox(15, saveButton, cancelButton);
        buttonBox.setAlignment(Pos.CENTER);
        gridPane.add(buttonBox, 1, 5);

        // Create scene and apply CSS
        Scene scene = new Scene(gridPane, 500, 600);
        scene.getStylesheets().add(getClass().getResource("/view/popup.css").toExternalForm());
        newProductStage.setScene(scene);
        newProductStage.show();

    }

    private void showProductEditor(Product product) {
        Stage editStage = new Stage();
        editStage.setTitle("Product Editor");

        // Create layout for showing product editor
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(40));
        grid.setHgap(10);
        grid.setVgap(15);

        // Error messages for CRUD
        messageLabel.put("name", new Label());
        messageLabel.put("brand", new Label());
        messageLabel.put("purchasePrice", new Label());
        messageLabel.put("salePrice", new Label());
        messageLabel.forEach((key, value) -> {
            value.setVisible(false);
            value.setManaged(false);
        });

        // Create input fields and labels
        TextField nameField = new TextField(product.getName());
        nameField.getStyleClass().add("text-field-account"); // Add CSS class

        TextField brandField = new TextField(product.getBrand());
        brandField.getStyleClass().add("text-field-account"); // Add CSS class
        TextField purchasePriceField = new TextField(String.valueOf(product.getPurchasePrice()));
        purchasePriceField.getStyleClass().add("text-field-account"); // Add CSS class
        TextField salePriceField = new TextField(String.valueOf(product.getSalePrice()));
        salePriceField.getStyleClass().add("text-field-account"); // Add CSS class

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

                    // StoreManager the uploaded image reference
                    uploadedImage[0] = image;

                    // Destination
                    String targetDirectory = "src/main/resources/view/images";
                    File targetDir = new File(targetDirectory);

                    // Rename uploaded file
                    String tempFileName = "img_temp_" + product.getImage();
                    Path tempPath = new File(targetDir, tempFileName).toPath();

                    // Copy the file to the destination
                    Files.copy(file.toPath(), tempPath, StandardCopyOption.REPLACE_EXISTING);

                    // StoreManager the temporary file reference
                    tempImageFile[0] = new File(tempPath.toString());

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        // Save button to apply changes
        Button saveButton = new Button("Save");
        saveButton.getStyleClass().add("button-account"); // Add CSS class
        saveButton.setOnAction(event -> {
            boolean flag = true; //TRUE = no error

            // Validate Inputs
            String name = nameField.getText();
            if (name.isEmpty()) {
                messageLabel.get("name").setText(alertEmptyName());
                showMessage(messageLabel.get("name"));
                flag = false;
            }
            if (!dm.ifUniqueProductName(name) && (!name.equals(product.getName()))) {
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
                messageLabel.get("purchase Price").setText(alertInvalidPurchasePrice());
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
                    Files.copy(tempImageFile[0].toPath(), finalImagePath, StandardCopyOption.REPLACE_EXISTING);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // Refresh the table data
            productList.setAll(dm.getAll());

            // Close
            editStage.close();
        });

        // Cancel button
        Button cancelButton = new Button("Cancel");
        cancelButton.getStyleClass().add("button-cancel-account"); // Add CSS class
        cancelButton.setOnAction(event -> editStage.close());

        // Reset button
        Button resetButton = new Button("Reset");
        resetButton.getStyleClass().add("button-account"); // Add CSS class
        resetButton.setOnAction(event -> {
            // Restore old values
            nameField.setText(product.getName());
            brandField.setText(product.getBrand());
            purchasePriceField.setText(String.valueOf(product.getPurchasePrice()));
            salePriceField.setText(String.valueOf(product.getSalePrice()));

            DirectorController.deleteTempProductImage();
            try {
                Files.copy(Path.of("src/main/resources/view/images/" + product.getImage()),
                        Path.of("src/main/resources/view/images/img_temp_" + product.getImage()),
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            imageView.setImage(originalImage);
        });

        // Add components to the grid
        grid.add(new Label("Product Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(messageLabel.get("name"), 2, 0);

        grid.add(new Label("Brand:"), 0, 1);
        grid.add(brandField, 1, 1);
        grid.add(messageLabel.get("brand"), 2, 1);

        grid.add(new Label("Purchase Price:"), 0, 2);
        grid.add(purchasePriceField, 1, 2);
        grid.add(messageLabel.get("purchasePrice"), 2, 2);

        grid.add(new Label("Sale Price:"), 0, 3);
        grid.add(salePriceField, 1, 3);
        grid.add(messageLabel.get("salePrice"), 2, 3);

        // Image upload section in GridPane
        VBox imageUploadBox = new VBox(10, imageView, uploadButton);
        imageUploadBox.setAlignment(Pos.CENTER);
        grid.add(imageUploadBox, 1, 4);

        // Save and Cancel buttons
        HBox buttonBox = new HBox(15, saveButton, resetButton, cancelButton);
        buttonBox.setAlignment(Pos.CENTER);
        grid.add(buttonBox, 1, 5);

        // Create scene and apply CSS
        Scene scene = new Scene(grid, 500, 600);
        scene.getStylesheets().add(getClass().getResource("/view/popup.css").toExternalForm());
        editStage.setScene(scene);
        editStage.show();

        // Handle Close
        editStage.setOnCloseRequest(event -> {
            // Clear error messages
            messageLabel.clear();
        });
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

    public static boolean areFilesIdentical(File file1, File file2) {
        try {
            byte[] file1Bytes = Files.readAllBytes(file1.toPath());
            byte[] file2Bytes = Files.readAllBytes(file2.toPath());

            if (file1Bytes.length != file2Bytes.length) {
                return false; // Different sizes, so not identical
            }

            for (int i = 0; i < file1Bytes.length; i++) {
                if (file1Bytes[i] != file2Bytes[i]) {
                    return false; // Found a difference in bytes
                }
            }

            return true; // Files are identical
        } catch (IOException e) {
            e.printStackTrace();
            return false; // Handle exception (e.g., file not found)
        }
    }
}