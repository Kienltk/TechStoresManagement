package controller;

import entity.Customer;
import entity.Product;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.CashierModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static model.CashierModel.getOne;

public class CashierController {

//    public static Map<Integer, Integer> cartItems = new HashMap<>();
    public static int idStore = 1; // ID cửa hàng mặc định, có thể thay đổi theo yêu cầu

    // Tìm khách hàng theo số điện thoại
    public static List<Customer> searchCustomerByPhone(String phoneNumber) {
        return CashierModel.searchCustomerByPhone(phoneNumber);
    }


    public static double getTotalPurchasePrice(Map<Integer, Integer> cartItems) {
        double totalPurchasePrice = 0;
        for (Map.Entry<Integer, Integer> entry : cartItems.entrySet()) {
            int productId = entry.getKey();
            int quantity = entry.getValue();
            Product product = getOne(productId);

            if (product != null) {
                totalPurchasePrice += product.getPurchasePrice() * quantity;
            }
        }
        return totalPurchasePrice;
    }

    // Xử lý đơn hàng
    public static void processOrder(String customerPhone, Map<Integer, Integer> cartItems, double total) {
        int customerId = CashierModel.getCustomerIdByPhone(customerPhone);
        System.out.println(customerId);
        if (customerId == -1) {
            System.out.println("Customer not found");
            return;
        }

        double profit = total - getTotalPurchasePrice(cartItems);

        // Tạo hóa đơn
        int receiptId = CashierModel.createReceipt(customerId, idStore, total, profit, cartItems);
        System.out.println(receiptId);

        // Thêm sản phẩm vào hóa đơn
        CashierModel.addProductsToReceipt(receiptId, cartItems);
        System.out.println(cartItems);

        // Cập nhật số lượng sản phẩm trong kho và làm trống giỏ hàng
        for (Map.Entry<Integer, Integer> entry : cartItems.entrySet()) {
            int productId = entry.getKey();
            int quantity = entry.getValue();
            CashierModel.handlePurchase(productId, quantity, idStore);
        }

        cartItems.clear();
    }

    // Hiển thị màn hình thêm khách hàng mới
    public static void showAddCustomerScreen() {
        Stage stage = new Stage();
        stage.setTitle("New Customer");

        // Layout và các thành phần UI
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));

        // Tên khách hàng
        Label nameLabel = new Label("Customer Name:");
        TextField nameInput = new TextField();

        // Số điện thoại
        Label phoneLabel = new Label("Phone Number:");
        TextField phoneInput = new TextField();

        // Nút submit để thêm khách hàng
        Button submitButton = new Button("Submit");
        submitButton.setOnAction(e -> {
            String name = nameInput.getText();
            String phoneNumber = phoneInput.getText();
            CashierModel.addNewCustomer(name, phoneNumber);
            stage.close(); // Đóng cửa sổ sau khi thêm thành công
        });

        layout.getChildren().addAll(nameLabel, nameInput, phoneLabel, phoneInput, submitButton);

        Scene scene = new Scene(layout, 300, 200);
        stage.setScene(scene);
        stage.show();
    }
}


