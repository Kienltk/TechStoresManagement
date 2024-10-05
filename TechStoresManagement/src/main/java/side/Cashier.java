package side;

import entity.Product;
import model.CashierModel;
import javafx.application.Application;
import javafx.beans.property.*;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class Cashier extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Root layout
        HBox root = new HBox();
        root.setPadding(new Insets(10));
        root.setSpacing(20);

        // Left side: Product table
        TableView<Product> productTable = new TableView<>();
        productTable.setPrefWidth(600);

        // Table columns
        TableColumn<Product, ImageView> imageColumn = new TableColumn<>("Image");
        imageColumn.setPrefWidth(100);
        imageColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getImage()));

        TableColumn<Product, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setPrefWidth(150);
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());

        TableColumn<Product, String> brandColumn = new TableColumn<>("Brand");
        brandColumn.setPrefWidth(100);
        brandColumn.setCellValueFactory(cellData -> cellData.getValue().brandProperty());

        TableColumn<Product, Integer> stockColumn = new TableColumn<>("Stock");
        stockColumn.setPrefWidth(80);
        stockColumn.setCellValueFactory(cellData -> cellData.getValue().stockProperty().asObject());

        TableColumn<Product, Double> priceColumn = new TableColumn<>("Price");
        priceColumn.setPrefWidth(100);
        priceColumn.setCellValueFactory(cellData -> cellData.getValue().priceProperty().asObject());

        productTable.getColumns().addAll(imageColumn, nameColumn, brandColumn, stockColumn, priceColumn);

        // Right side: Order summary
        VBox orderSummary = new VBox();
        orderSummary.setPadding(new Insets(10));
        orderSummary.setSpacing(10);

        Label orderDetailLabel = new Label("Order Detail");
        ListView<String> orderListView = new ListView<>();
        orderListView.setPrefHeight(200);

        Label totalLabel = new Label("Total: 0$");
        Button buyNowButton = new Button("Buy Now");

        orderSummary.getChildren().addAll(orderDetailLabel, orderListView, totalLabel, buyNowButton);

        // Add to root layout
        root.getChildren().addAll(productTable, orderSummary);

        // Scene and Stage
        Scene scene = new Scene(root, 800, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Product Order App");
        primaryStage.show();

        CashierModel.loadSampleData(productTable, orderListView, totalLabel);
    }

}
