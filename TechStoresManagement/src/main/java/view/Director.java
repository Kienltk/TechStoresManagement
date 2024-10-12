package view;

import controller.Session;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;

import javafx.scene.Scene;

import javafx.stage.Stage;

public class Director extends Application {
    @Override
    public void start(Stage primaryStage)throws Exception {
        if (!Session.isLoggedIn()) {
            try {
                new Login().start(new Stage());
            } catch (Exception e) {
                e.printStackTrace();
            }
            primaryStage.close();
            return;
        }
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Dashboard.fxml"));
        Scene scene = new Scene(loader.load());
        primaryStage.setResizable(false);
        primaryStage.setTitle("Dashboard");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}

//package view;
//
//import controller.Session;
//import entity.Product;
//import javafx.application.Application;
//import javafx.beans.property.SimpleObjectProperty;
//import javafx.collections.FXCollections;
//import javafx.collections.ObservableList;
//import javafx.geometry.Insets;
//import javafx.geometry.Pos;
//import javafx.scene.Scene;
//import javafx.scene.control.*;
//import javafx.scene.image.ImageView;
//import javafx.scene.layout.*;
//import javafx.stage.Stage;
//import model.DirectorModel;
//
//public class Director extends Application {
//
//    DirectorModel dm = new DirectorModel();
//    @Override
//    public void start(Stage primaryStage) {
//        // Check if the user is logged in
////        if (!Session.isLoggedIn()) {
////            try {
////                new Login().start(new Stage());
////            } catch (Exception e) {
////                e.printStackTrace();
////            }
////            primaryStage.close();
////            return;
////        }
//
//        // Title Label
//        Label titleLabel = new Label("Product Management");
//        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
//
//        // New Product Button
//        Button newProductButton = new Button("New Product");
//        newProductButton.setStyle("-fx-background-color: #00C2FF; -fx-text-fill: white;");
//
//        // Search Bar
//        TextField searchField = new TextField();
//        searchField.setPromptText("Search Item");
//        Button searchButton = new Button("🔍");
//
//        HBox searchBar = new HBox(searchField, searchButton);
//        searchBar.setAlignment(Pos.CENTER_RIGHT);
//        searchBar.setSpacing(10);
//
//        // TableView for Product Data
//        TableView<Product> productTable = new TableView<>();
//
//        // Table Columns
//        TableColumn<Product, Integer> idColumn = new TableColumn<>("No");
//        idColumn.setMinWidth(50);
//        idColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
//
//        TableColumn<Product, HBox> nameColumn = new TableColumn<>("Name");
//        nameColumn.setPrefWidth(230);
//        nameColumn.setCellValueFactory(cellData -> {
//            Product product = cellData.getValue();
//            ImageView imageView = product.getImage();
//            Label nameLabel = new Label(product.getName());
//            HBox hBox = new HBox(10);
//            hBox.getChildren().addAll(imageView, nameLabel);
//            return new SimpleObjectProperty<>(hBox);
//        });
//
//
//        TableColumn<Product, String> brandColumn = new TableColumn<>("Brand");
//        brandColumn.setMinWidth(150);
//        brandColumn.setCellValueFactory(cellData -> cellData.getValue().brandProperty());
//
//
//        TableColumn<Product, Double> purchasePriceColumn = new TableColumn<>("Purchase Price");
//        purchasePriceColumn.setMinWidth(100);
//        purchasePriceColumn.setCellValueFactory(cellData -> cellData.getValue().salePriceProperty().asObject());
//
//
//        TableColumn<Product, Double> salePriceColumn = new TableColumn<>("Sale Price");
//        salePriceColumn.setMinWidth(100);
//        salePriceColumn.setCellValueFactory(cellData -> cellData.getValue().salePriceProperty().asObject());
//
//
//        TableColumn<Product, Integer> stockColumn = new TableColumn<>("Stock");
//        stockColumn.setMinWidth(100);
//        stockColumn.setCellValueFactory(cellData -> cellData.getValue().stockProperty().asObject());
//
//
//        // Option Column with Edit and Delete buttons
//        TableColumn<Product, Void> optionColumn = new TableColumn<>("Option");
//        optionColumn.setCellFactory(col -> new TableCell<>() {
//            final Button editButton = new Button("Edit");
//            final Button deleteButton = new Button("Delete");
//
//            {
//                editButton.setStyle("-fx-background-color: yellow;");
//                deleteButton.setStyle("-fx-background-color: red; -fx-text-fill: white;");
//            }
//
//            @Override
//            protected void updateItem(Void item, boolean empty) {
//                super.updateItem(item, empty);
//                if (empty) {
//                    setGraphic(null);
//                } else {
//                    HBox optionBox = new HBox(editButton, deleteButton);
//                    optionBox.setSpacing(10);
//                    setGraphic(optionBox);
//                }
//            }
//        });
//
//        // Add Columns to Table
//        productTable.getColumns().addAll(idColumn, nameColumn, brandColumn, purchasePriceColumn, salePriceColumn, stockColumn, optionColumn);
//        // ObservableList to hold Product data
//        ObservableList<Product> productList = FXCollections.observableArrayList();
//        productList.setAll(dm.getAll(1));
//
//        // Bind the data to the TableView
//        productTable.setItems(productList);
//
//        // Layout for main content
//        VBox layout = new VBox(10, titleLabel, newProductButton, searchBar, productTable);
//        layout.setPadding(new Insets(20));
//
//        // HBox root (as defined earlier) that includes the table layout
//        HBox root = new HBox(layout);
//        root.setPadding(new Insets(10, 50, 10, 50));
//        root.setSpacing(20);
//
//        // Scene setup
//        Scene scene = new Scene(root, 1366, 768);
//        scene.getStylesheets().add(getClass().getResource("cashier.css").toExternalForm());
//        primaryStage.setScene(scene);
//        primaryStage.setTitle("General Director App");
//        primaryStage.setResizable(false);
//        primaryStage.setWidth(1366);
//        primaryStage.setHeight(768);
//        primaryStage.show();
//    }
//
//    public static void main(String[] args) {
//        launch(args);
//    }
//
//}
