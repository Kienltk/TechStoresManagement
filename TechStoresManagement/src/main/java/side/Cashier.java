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

import java.util.HashMap;
import java.util.Map;

public class Cashier extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    private ListView<String> orderListView = new ListView<>();
    private Label totalLabel = new Label("Total: 0$");
    private double totalPrice = 0;
    private Map<Integer, Integer> cartItems = new HashMap<>();
    CashierModel cm = new CashierModel();

    @Override
    public void start(Stage primaryStage) {
        if (!Session.isLoggedIn()) {
            try {
                new LoginSide().start(new Stage());
            } catch (Exception e) {
                e.printStackTrace();
            }
            primaryStage.close();  // Đóng cửa sổ hiện tại
            return;
        }

        HBox root = new HBox();
        root.setPadding(new Insets(10));
        root.setSpacing(20);

        TableView<Product> productTable = new TableView<>();
        productTable.setPrefWidth(630);

        //ID column
        TableColumn<Product, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setPrefWidth(30);
        idColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());

        //Name column
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

        // Brand column
        TableColumn<Product, String> brandColumn = new TableColumn<>("Brand");
        brandColumn.setPrefWidth(100);
        brandColumn.setCellValueFactory(cellData -> cellData.getValue().brandProperty());

        //Stock column
        TableColumn<Product, Integer> stockColumn = new TableColumn<>("Stock");
        stockColumn.setPrefWidth(100);
        stockColumn.setCellValueFactory(cellData -> cellData.getValue().stockProperty().asObject());

        //Price column
        TableColumn<Product, Double> priceColumn = new TableColumn<>("Price");
        priceColumn.setPrefWidth(130);
        priceColumn.setCellValueFactory(cellData -> cellData.getValue().priceProperty().asObject());

        //Action column
        TableColumn<Product, Button> actionColumn = new TableColumn<>("Action");
        actionColumn.setPrefWidth(120);
        actionColumn.setCellValueFactory(cellData -> {
            Button addButton = new Button("Add to Cart");

            // Handle button click
            addButton.setOnAction(e -> {
                Product product = cellData.getValue();
                int productId = product.getId();
                double productPrice = product.getPrice();
                int stockQuantity = product.getStock();

                // Check if product is already in the cart
                int currentQuantity = cartItems.getOrDefault(productId, 0);

                // Check if we can add the product based on stock
                if (currentQuantity < stockQuantity) {

                    // Increase quantity
                    cartItems.put(productId, currentQuantity + 1);

                    // Update total price
                    totalPrice += productPrice;
                    totalLabel.setText("Total: $" + String.format("%.2f", totalPrice));

                    // Update the ListView
                    updateOrderListView();
                } else {
                    // Show an alert if stock is insufficient
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Stock Alert");
                    alert.setHeaderText(null);
                    alert.setContentText("Cannot add more of " + cm.getOne(productId).getName() + " to the cart. Insufficient stock!");
                    alert.showAndWait();
                }
            });

            return new SimpleObjectProperty<>(addButton);
        });

        productTable.getColumns().addAll(idColumn, nameColumn, brandColumn, stockColumn, priceColumn, actionColumn);

        // Right side: Order summary
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

            // Show the dialog and wait for the user response
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    //Purchase Successful
                    for (Map.Entry<Integer, Integer> entry : cartItems.entrySet()) {
                        int productId = entry.getKey();
                        int quantity = entry.getValue();
                        cm.handlePurchase(productId, quantity);
                    }
                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Purchase Successful");
                    successAlert.setHeaderText(null);
                    successAlert.setContentText("Thank you for your purchase!");
                    successAlert.showAndWait();

                    // Clear the cart and reset the total price
                    cartItems.clear();
                    totalPrice = 0;
                    totalLabel.setText("Total: $0.00");
                    updateOrderListView();

                    //Reload
                    productTable.getItems().clear();
                    CashierModel.loadData(productTable, 1);
                }
            });
        });

        ContextMenu contextMenu = new ContextMenu();
        MenuItem removeItem = new MenuItem("Remove");
        MenuItem removeAllItem = new MenuItem("Remove All");
        contextMenu.getItems().addAll(removeItem, removeAllItem);

        removeItem.setOnAction(e -> {
            String selectedItem = orderListView.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                // Extract product name and quantity from the selected item
                String productName = selectedItem.split(" \\(x")[0];
                int cartItemsKey = cm.getProductByName(productName).getId();

                int quantity = cartItems.get(cartItemsKey);

                // Remove the product from cart
                if (quantity > 1) {
                    cartItems.put(cartItemsKey, quantity - 1);
                } else {
                    cartItems.remove(cartItemsKey);
                }

                // Update total price
                totalPrice -= getProductPrice(productName);

                // $-0.00 case
                if (totalPrice < 0) {
                    totalPrice = 0;
                }

                totalLabel.setText("Total: $" + String.format("%.2f", totalPrice));

                // Update the ListView
                updateOrderListView();
            }
        });

        removeAllItem.setOnAction(e -> {
            String selectedItem = orderListView.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                // Extract product name from the selected item
                String productName = selectedItem.split(" \\(x")[0];
                int cartItemsKey = cm.getProductByName(productName).getId();
                int quantity = cartItems.get(cartItemsKey);

                // Remove the product completely
                cartItems.remove(cartItemsKey);

                // Update total price
                totalPrice -= (quantity * getProductPrice(productName));

                // Ensure total price doesn't go negative
                if (totalPrice < 0) {
                    totalPrice = 0;
                }

                totalLabel.setText("Total: $" + String.format("%.2f", totalPrice));

                // Update the ListView
                updateOrderListView();
            }
        });

        orderListView.setContextMenu(contextMenu);

        orderSummary.getChildren().addAll(orderDetailLabel, orderListView, totalLabel, buyNowButton);

        // Add to root layout
        root.getChildren().addAll(productTable, orderSummary);

        // Scene and Stage
        Scene scene = new Scene(root,  1366, 768);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Product Order App");
        primaryStage.show();

        CashierModel.loadData(productTable, 1);
    }

    private double getProductPrice(String productName) {
        // Assuming you have a method to get the product price based on the name
        // Modify this to get the actual price of the product
        for (Product product : cm.getAll()) { // Assuming you have access to the list of products
            if (product.getName().equals(productName)) {
                return product.getPrice();
            }
        }
        return 0.0; // Default if not found
    }

    private void updateOrderListView() {
        orderListView.getItems().clear();  // Clear current ListView

        // Rebuild ListView items based on cart
        for (Map.Entry<Integer, Integer> entry : cartItems.entrySet()) {
            String itemName = cm.getOne(entry.getKey()).getName();
            int quantity = entry.getValue();
            orderListView.getItems().add(itemName + " (x" + quantity + ")");
        }
    }


}
